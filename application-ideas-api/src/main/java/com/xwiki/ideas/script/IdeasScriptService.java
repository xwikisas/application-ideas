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
package com.xwiki.ideas.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.xwiki.ideas.IdeasManager;

/**
 * @version $Id$
 * @since 1.14
 */
@Component
@Named("ideas")
@Singleton
public class IdeasScriptService implements ScriptService
{
    @Inject
    private IdeasManager ideasManager;

    /**
     * @param documentReference the document that we want to get the URL for
     * @param action the action that needs to be performed over the document
     * @return the Ideas REST URL specific to the document
     */
    public String url(DocumentReference documentReference, String action) {
        return ideasManager.getRestUrl(documentReference, action);
    }
}
