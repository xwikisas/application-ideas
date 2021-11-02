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

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.internal.resources.pages.ModifiablePageResource;

import com.xwiki.ideas.rest.IdeasResource;

/**
 * Default implementation of {@link IdeasResource}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("com.xwiki.ideas.internal.rest.DefaultIdeasResource")
@Singleton
public class DefaultIdeasResource extends ModifiablePageResource implements IdeasResource
{
    @Override public Response test()
    {
        return Response.ok("This is a test").build();
    }
}
