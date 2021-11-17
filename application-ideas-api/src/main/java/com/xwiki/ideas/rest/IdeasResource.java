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
package com.xwiki.ideas.rest;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.xwiki.rest.XWikiRestException;
import org.xwiki.stability.Unstable;

/**
 * @version $Id$
 * @since 1.14
 */
@Path("/wikis/{wikiName}/spaces/{spaceName: .+}/pages/{pageName}/idea")
@Unstable
public interface IdeasResource
{
    /**
     * Casts the vote of the current user to the page containing an idea.
     *
     * @param xwikiName The name of the wiki in which the page resides
     * @param spaceName The spaces associated with the page
     * @param pageName The name of the page
     * @param voteType The intention of the user with regards to the idea
     * @return A status of the undergone process
     * @throws XWikiRestException when the document is missing or lacks an Ideas poll
     */
    @PUT
    Response castVote(
        @PathParam("wikiName") String xwikiName,
        @PathParam("spaceName") String spaceName,
        @PathParam("pageName") String pageName,
        @QueryParam("voteType") String voteType
    ) throws XWikiRestException;
}
