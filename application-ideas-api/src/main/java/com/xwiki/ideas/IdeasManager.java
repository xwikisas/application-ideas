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

import com.xwiki.ideas.model.VoteResult;

/**
 * @version $Id$
 * @since 1.14
 */
@Role
@Unstable
public interface IdeasManager
{
    /**
     * @param documentReference a reference to a document that contains an Idea
     * @param pro true if the user agrees with the Idea; false otherwise
     * @return an Object representation of the vote result
     * @throws IdeasException if the document does not exist or it doesn't have an Idea
     * @throws IdeasDocumentOperationException if the server fails to do a document specific operation such as saving
     */
    VoteResult vote(DocumentReference documentReference, boolean pro)
        throws IdeasException, IdeasDocumentOperationException;
}
