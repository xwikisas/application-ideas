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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.ideas.IdeasException;
import com.xwiki.ideas.IdeasManager;
import com.xwiki.ideas.model.xjc.Idea;

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

    private static final Map<String, String> VOTER_KEY_TO_NR_KEY = new HashMap<>();

    private static final String NOT_FOUND_ERROR =
        "The document [%s] does not exists or it does not have an Idea Object.";

    private static final String FAILED_LOAD_EXCEPTION = "Failed to load idea document [%s].";

    private static final String COMMA = ",";

    static {
        VOTER_KEY_TO_NR_KEY.put(VOTERS_AGAINST_KEY, NUMBER_OF_AGAINST_VOTES_KEY);
        VOTER_KEY_TO_NR_KEY.put(VOTERS_FOR_KEY, NUMBER_OF_FOR_VOTES_KEY);
    }

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> serializer;

    @Override public Idea vote(DocumentReference documentReference, boolean pro)
        throws IdeasException
    {
        XWikiContext xcontext = contextProvider.get();
        XWiki xWiki = xcontext.getWiki();
        try {
            XWikiDocument mydoc = xWiki.getDocument(documentReference, xcontext);

            BaseObject ideasObj = mydoc.getXObject(IDEA_CLASS_REFERENCE);
            DocumentReference user = xcontext.getUserReference();
            if (!mydoc.isNew() && null != ideasObj) {
                String serializedUser = serializer.serialize(user, documentReference.getWikiReference());

                if (pro) {
                    addVote(VOTERS_FOR_KEY, ideasObj, serializedUser, xcontext);
                    removeVote(VOTERS_AGAINST_KEY, ideasObj, serializedUser, xcontext);
                } else {
                    addVote(VOTERS_AGAINST_KEY, ideasObj, serializedUser, xcontext);
                    removeVote(VOTERS_FOR_KEY, ideasObj, serializedUser, xcontext);
                }
                List<String> proUsers = getListValue(VOTERS_FOR_KEY, ideasObj);
                List<String> againstUsers = getListValue(VOTERS_AGAINST_KEY, ideasObj);
                Idea result = new Idea();
                result.setNbagainst(againstUsers.size());
                result.setNbvotes(proUsers.size());
                result.getAgainst().addAll(againstUsers);
                result.getSupporters().addAll(proUsers);

                // Save document
                xWiki.saveDocument(mydoc, "New Vote", xcontext);
                return result;
            } else {
                throw new IdeasException(String.format(NOT_FOUND_ERROR, documentReference));
            }
        } catch (XWikiException e) {
            throw new IdeasException(String.format(FAILED_LOAD_EXCEPTION, documentReference));
        }
    }

    @Override public boolean exists(DocumentReference documentReference) throws IdeasException
    {
        try {
            XWikiContext xcontext = contextProvider.get();
            XWiki xWiki = xcontext.getWiki();
            XWikiDocument mydoc = xWiki.getDocument(documentReference, xcontext);
            BaseObject ideasObject = mydoc.getXObject(IDEA_CLASS_REFERENCE);
            return ideasObject != null;
        } catch (XWikiException e) {
            throw new IdeasException(String.format(FAILED_LOAD_EXCEPTION, documentReference), e);
        }
    }

    private void addVote(String voterKey, BaseObject ideasObj, String user, XWikiContext xcontext)
    {
        List<String> userList = getListValue(voterKey, ideasObj);

        if (userList.contains(user)) {
            userList.remove(user);
        } else {
            userList.add(user);
        }
        ideasObj.set(voterKey, StringUtils.join(userList, COMMA), xcontext);
        ideasObj.set(VOTER_KEY_TO_NR_KEY.get(voterKey), userList.size(), xcontext);
    }

    private void removeVote(String voterKey, BaseObject ideasObj, String user, XWikiContext xcontext)
    {
        List<String> userList = getListValue(voterKey, ideasObj);
        userList.remove(user);
        ideasObj.set(voterKey, StringUtils.join(userList, COMMA), xcontext);
        ideasObj.set(VOTER_KEY_TO_NR_KEY.get(voterKey), userList.size(), xcontext);
    }

    private List<String> getListValue(String voterKey, BaseObject ideasObj)
    {
        String userListAsString = ideasObj.getStringValue(voterKey);
        List<String> userList;
        if (userListAsString.isEmpty()) {
            userList = new ArrayList<>();
        } else {
            userList = new ArrayList<>(Arrays.asList(userListAsString.split(COMMA)));
        }
        return userList;
    }
}
