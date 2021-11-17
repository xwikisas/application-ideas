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
package com.xwiki.ideas;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

/**
 * @version $Id$
 * @since 1.4
 */
@Role
@Unstable
public interface IdeasManager
{
    /**
     * @param documentReference the document that we want to get the URL for
     * @return the Ideas REST URL specific to the document
     */
    String getRestUrl(DocumentReference documentReference);

    /**
     *
     * @param documentReference a reference to a document that contains an Idea
     * @param voteType the type of the vote that the user wants to cast
     * @return a string in a JSON format describing the results of the vote
     * @throws IdeasException if document specific operations fail such as document retrieval, missing Idea
     * object, document save
     */
    String vote(DocumentReference documentReference, String voteType) throws IdeasException;
}
