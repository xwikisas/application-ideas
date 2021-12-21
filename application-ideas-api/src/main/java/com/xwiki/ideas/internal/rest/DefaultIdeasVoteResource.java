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
package com.xwiki.ideas.internal.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.internal.resources.pages.ModifiablePageResource;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xwiki.ideas.IdeasException;
import com.xwiki.ideas.IdeasManager;
import com.xwiki.ideas.internal.IdeaMapper;
import com.xwiki.ideas.model.jaxb.Idea;
import com.xwiki.ideas.rest.IdeasVoteResource;

/**
 * Default implementation of {@link com.xwiki.ideas.rest.IdeasVoteResource}.
 *
 * @version $Id$
 * @since 1.14
 */
@Component
@Named("com.xwiki.ideas.internal.rest.DefaultIdeasVoteResource")
@Singleton
public class DefaultIdeasVoteResource extends ModifiablePageResource implements IdeasVoteResource
{
    private static final String SUPPORTERS_GROUP = "supporters";
    @Inject
    private IdeasManager manager;

    @Inject
    private ContextualAuthorizationManager authorizationManager;

    @Override
    public Idea vote(String xwikiName, String spaceName, String pageName, String voterGroup)
        throws XWikiRestException
    {
        DocumentReference documentReference = new DocumentReference(pageName, getSpaceReference(spaceName, xwikiName));
        if (!authorizationManager.hasAccess(Right.EDIT, documentReference)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        try {
            if (manager.exists(documentReference)) {
                return IdeaMapper.from(manager.vote(documentReference, SUPPORTERS_GROUP.equals(voterGroup)));
            }
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (IdeasException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Idea undoVote(String xwikiName, String spaceName, String pageName, String voterGroup)
        throws XWikiRestException
    {
        DocumentReference documentReference = new DocumentReference(pageName, getSpaceReference(spaceName, xwikiName));
        if (!authorizationManager.hasAccess(Right.EDIT, documentReference)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        try {
            if (manager.exists(documentReference)) {
                return IdeaMapper.from(manager.removeVote(documentReference, SUPPORTERS_GROUP.equals(voterGroup)));
            }
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (IdeasException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
