/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.ideas.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.tools.JSONTool;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xwiki.ideas.IdeasException;
import com.xwiki.ideas.IdeasManager;

/**
 * @version $Id$
 * @since 1.4
 */
@Component
@Singleton
public class DefaultIdeasManager implements IdeasManager
{
    static final LocalDocumentReference IDEA_CLASS_REFERENCE = new LocalDocumentReference("Ideas", "IdeasClass");

    private static final String VOTERS_AGAINST_KEY = "against";
    private static final String NUMBER_OF_AGAINST_VOTES_KEY = "nbagainst";

    private static final String VOTERS_FOR_KEY = "supporters";
    private static final String NUMBER_OF_FOR_VOTES_KEY = "nbvotes";

    private static final String COMMA = ",";

    private static final Map<String, String> SUPP_TO_NR_KEY = new HashMap<>();
    static {
        SUPP_TO_NR_KEY.put(VOTERS_AGAINST_KEY, NUMBER_OF_AGAINST_VOTES_KEY);
        SUPP_TO_NR_KEY.put(VOTERS_FOR_KEY, NUMBER_OF_FOR_VOTES_KEY);
    }


    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override public String getRestUrl(DocumentReference documentReference)
    {
        String contextPath = contextProvider.get().getRequest().getContextPath();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(contextPath);
            stringBuilder.append("/rest/wikis/");
            stringBuilder
                .append(URLEncoder.encode(documentReference.getWikiReference().getName(), XWiki.DEFAULT_ENCODING));
            for (SpaceReference spaceReference : documentReference.getSpaceReferences()) {
                stringBuilder.append("/spaces/");
                stringBuilder.append(URLEncoder.encode(spaceReference.getName(), XWiki.DEFAULT_ENCODING));
            }
            stringBuilder.append("/pages/");
            stringBuilder.append(URLEncoder.encode(documentReference.getName(), XWiki.DEFAULT_ENCODING));
            stringBuilder.append("/idea");
            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(String.format("Failed to retrieve the REST URL of the document: [%s]",
                documentReference), e);
        }
    }

    @Override public String vote(DocumentReference documentReference, String voteType) throws IdeasException
    {
        XWikiContext xcontext = contextProvider.get();
        XWiki xWiki = xcontext.getWiki();
        Map<String, Object> result = new HashMap<>();
        try {
            XWikiDocument mydoc = xWiki.getDocument(documentReference, xcontext);
            BaseObject ideasObj = mydoc.getXObject(IDEA_CLASS_REFERENCE);
            XWikiUser user = xWiki.checkAuth(xcontext);

            if (!mydoc.isNew() && null != ideasObj) {
                Map<String, Boolean> doRemoveMap = new HashMap<>();
                // Action : Add a supporter (vote)
                if (voteType.equals("addVote")) {
                    doRemoveMap = addVote(VOTERS_FOR_KEY, VOTERS_AGAINST_KEY, ideasObj, user, result, xcontext);
                } else if (voteType.equals("addVoteOpp")) {
                    doRemoveMap = addVote(VOTERS_AGAINST_KEY, VOTERS_FOR_KEY, ideasObj, user, result, xcontext);
                }
                // Action : Remove a supporter (vote)
                if (voteType.equals("removeVote") || doRemoveMap.get(VOTERS_FOR_KEY)) {
                    // Remove user from user list
                    decrementVote(VOTERS_FOR_KEY, ideasObj, user, xcontext, result);
                } else if (doRemoveMap.get(VOTERS_AGAINST_KEY)) {
                    // Remove user from user list
                    decrementVote(VOTERS_AGAINST_KEY, ideasObj, user, xcontext, result);
                }
                // Save document
                xWiki.saveDocument(mydoc, "New Vote", xcontext);
            } else {
                throw new IdeasException(String.format("Failed to vote for [%s] on behalf of [%s].",
                    documentReference, user.getUserReference()));
            }
            JSONTool jsonTool = new JSONTool();
            return jsonTool.serialize(result);
        } catch (XWikiException e) {
            throw new IdeasException(
                String.format("Failed to do a document specific action on [%s]", documentReference), e);
        }
    }

    private Map<String, Boolean> addVote(String voterKey, String voterOpponentKey, BaseObject ideasObj, XWikiUser user,
        Map<String, Object> result, XWikiContext xcontext) {
        Map<String, Boolean> removeMap = new HashMap<>();
        removeMap.put(voterKey, false);
        removeMap.put(voterOpponentKey, false);

        String userList = ideasObj.getStringValue(voterKey);
        String userListSup = ideasObj.getStringValue(voterOpponentKey);
        int nbvotes = ideasObj.getIntValue(SUPP_TO_NR_KEY.get(voterKey));
        int nbopp = ideasObj.getIntValue(SUPP_TO_NR_KEY.get(voterOpponentKey));

        if (StringUtils.contains(userList, user.toString())) {
            removeMap.put(voterKey, true);
        } else {
            // Add user in userList
            if (StringUtils.contains(userListSup, user.toString())) {
                removeMap.put(voterOpponentKey, true);
                result.put("remove", true);
                result.put("nbopp", nbopp - 1);
            }
            if (userList == null) {
                userList = COMMA + user;
            } else {
                userList = userList + COMMA + user;
            }
            ideasObj.set(voterKey, userList, xcontext);
            // Increase vote by one
            nbvotes = nbvotes + 1;
            ideasObj.set(SUPP_TO_NR_KEY.get(voterKey), nbvotes, xcontext);
            result.put(SUPP_TO_NR_KEY.get(voterKey), nbvotes);
        }
        return  removeMap;
    }

    private void decrementVote(String voterKey, BaseObject ideasObj, XWikiUser user, XWikiContext xcontext,
        Map<String, Object> result) {
        String userList = ideasObj.getStringValue(voterKey);
        int nbvotes = ideasObj.getIntValue(SUPP_TO_NR_KEY.get(voterKey));
        String newUserList = StringUtils.replaceOnce(userList, COMMA + user.toString(), "");
        newUserList = StringUtils.replaceOnce(newUserList, user + COMMA, "");
        newUserList = StringUtils.replaceOnce(newUserList, user.toString(), "");
        ideasObj.set(voterKey, newUserList, xcontext);
        // Decrease vote by one
        nbvotes--;
        ideasObj.set(SUPP_TO_NR_KEY.get(voterKey), nbvotes, xcontext);
        result.put(SUPP_TO_NR_KEY.get(voterKey), nbvotes);
    }
}
