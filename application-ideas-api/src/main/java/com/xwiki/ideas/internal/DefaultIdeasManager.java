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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xwiki.ideas.IdeasManager;

/**
 * @version $Id$
 * @since 1.4
 */
@Component
@Singleton
public class DefaultIdeasManager implements IdeasManager
{

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override public String getRestUrl(DocumentReference documentReference, String action)
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
            stringBuilder.append("/action/");
            stringBuilder.append(action);
            stringBuilder.append("/ideas");
            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(String.format("Failed to retrieve the REST URL of the document: [%s]",
                documentReference), e);
        }
    }
}
