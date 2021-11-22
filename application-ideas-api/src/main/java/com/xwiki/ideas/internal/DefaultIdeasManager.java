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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.ideas.IdeasDocumentOperationException;
import com.xwiki.ideas.IdeasException;
import com.xwiki.ideas.IdeasManager;
import com.xwiki.ideas.model.VoteResult;

/**
 * @version $Id$
 * @since 1.14
 */
@Component
@Singleton
public class DefaultIdeasManager implements IdeasManager
{
    static final LocalDocumentReference IDEA_CLASS_REFERENCE = new LocalDocumentReference("Ideas", "IdeasClass");

    static final String VOTERS_AGAINST_KEY = "against";

    static final String NUMBER_OF_AGAINST_VOTES_KEY = "nbagainst";

    static final String VOTERS_FOR_KEY = "supporters";

    static final String NUMBER_OF_FOR_VOTES_KEY = "nbvotes";

    static final String COMMA = ",";

    private static final Map<String, String> VOTER_KEY_TO_NR_KEY = new HashMap<>();

    private static final String NOT_FOUND_ERROR =
        "The document [%s] does not exists or it does not have an Idea Object.";

    static {
        VOTER_KEY_TO_NR_KEY.put(VOTERS_AGAINST_KEY, NUMBER_OF_AGAINST_VOTES_KEY);
        VOTER_KEY_TO_NR_KEY.put(VOTERS_FOR_KEY, NUMBER_OF_FOR_VOTES_KEY);
    }

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override public VoteResult vote(DocumentReference documentReference, boolean pro)
        throws IdeasException, IdeasDocumentOperationException
    {
        XWikiContext xcontext = contextProvider.get();
        XWiki xWiki = xcontext.getWiki();
        VoteResult result = new VoteResult();
        XWikiDocument mydoc;
        try {
            mydoc = xWiki.getDocument(documentReference, xcontext);
        } catch (XWikiException e) {
            throw new IdeasException(String.format(NOT_FOUND_ERROR, documentReference), e);
        }
        BaseObject ideasObj = mydoc.getXObject(IDEA_CLASS_REFERENCE);
        DocumentReference user = xcontext.getUserReference();
        if (!mydoc.isNew() && null != ideasObj) {
            Map<String, Boolean> doRemoveMap = new HashMap<>();
            doRemoveMap.put(VOTERS_FOR_KEY, false);
            doRemoveMap.put(VOTERS_AGAINST_KEY, false);
            // Action : Add a supporter (vote)
            if (pro) {
                addVote(VOTERS_FOR_KEY, VOTERS_AGAINST_KEY, ideasObj, user, result, xcontext, doRemoveMap);
            } else {
                addVote(VOTERS_AGAINST_KEY, VOTERS_FOR_KEY, ideasObj, user, result, xcontext, doRemoveMap);
            }
            // Action : Remove a supporter (vote)
            if (doRemoveMap.get(VOTERS_FOR_KEY)) {
                // Remove user from user list
                decrementVote(VOTERS_FOR_KEY, ideasObj, user, xcontext, result);
            } else if (doRemoveMap.get(VOTERS_AGAINST_KEY)) {
                // Remove user from user list
                decrementVote(VOTERS_AGAINST_KEY, ideasObj, user, xcontext, result);
            }
            // Save document

            try {
                xWiki.saveDocument(mydoc, "New Vote", xcontext);
            } catch (XWikiException e) {
                throw new IdeasDocumentOperationException(
                    String.format("Failed to vote the Ideas of [%s] on behalfof [%s]", documentReference, user), e);
            }
        } else {
            throw new IdeasException(String.format(NOT_FOUND_ERROR, documentReference));
        }
        return result;
    }

    private void addVote(String voterKey, String voterOpponentKey, BaseObject ideasObj, DocumentReference user,
        VoteResult result, XWikiContext xcontext, Map<String, Boolean> removeMap)
    {

        String userList = ideasObj.getStringValue(voterKey);
        String userListSup = ideasObj.getStringValue(voterOpponentKey);
        int nbvotes = ideasObj.getIntValue(VOTER_KEY_TO_NR_KEY.get(voterKey));
        int nbopp = ideasObj.getIntValue(VOTER_KEY_TO_NR_KEY.get(voterOpponentKey));

        if (StringUtils.contains(userList, user.toString())) {
            removeMap.put(voterKey, true);
        } else {
            // Add user in userList
            if (StringUtils.contains(userListSup, user.toString())) {
                removeMap.put(voterOpponentKey, true);
                result.setRemove();
                result.setNbopp(nbopp - 1);
            }
            if (userList == null || userList.isEmpty()) {
                userList = user.toString();
            } else {
                userList = userList + COMMA + user;
            }
            ideasObj.set(voterKey, userList, xcontext);
            // Increase vote by one
            nbvotes = nbvotes + 1;
            ideasObj.set(VOTER_KEY_TO_NR_KEY.get(voterKey), nbvotes, xcontext);
            if (voterKey.equals(VOTERS_FOR_KEY)) {
                result.setNbvotes(nbvotes);
            } else {
                result.setNbagainst(nbvotes);
            }
        }
    }

    private void decrementVote(String voterKey, BaseObject ideasObj, DocumentReference user, XWikiContext xcontext,
        VoteResult result)
    {
        String userList = ideasObj.getStringValue(voterKey);
        int nbvotes = ideasObj.getIntValue(VOTER_KEY_TO_NR_KEY.get(voterKey));
        String newUserList = StringUtils.replaceOnce(userList, COMMA + user, "");
        newUserList = StringUtils.replaceOnce(newUserList, user + COMMA, "");
        newUserList = StringUtils.replaceOnce(newUserList, user.toString(), "");
        ideasObj.set(voterKey, newUserList, xcontext);
        // Decrease vote by one
        nbvotes--;
        ideasObj.set(VOTER_KEY_TO_NR_KEY.get(voterKey), nbvotes, xcontext);
        if (voterKey.equals(VOTERS_FOR_KEY)) {
            result.setNbvotes(nbvotes);
        } else {
            result.setNbagainst(nbvotes);
        }
    }
}
