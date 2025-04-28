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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.script.service.ScriptService;

/**
 * Script service for retrieving information about the Ideas Application.
 *
 * @version $Id$
 * @since 1.16.0
 */
@Component
@Named("ideas")
@Singleton
public class IdeasScriptService implements ScriptService
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    /**
     * Check if an idea with the given status allows voting.
     * @param status the status to check
     * @return true if an idea with the given status is open for voting, false otherwise
     */
    public boolean isFormActive(String status)
    {
        try {
            Query query = queryManager.createQuery(
                "select prop3.value from BaseObject as obj,"
                    + " StringProperty as prop1, IntegerProperty as prop3"
                    + " where obj.className='Ideas.Code.StatusClass' and"
                    + " obj.id=prop1.id.id and prop1.id.name='status' and prop1.id.value=:status"
                    + " and obj.id=prop3.id.id and prop3.id.name='formActive'", Query.HQL);
            List<Object> results = query.setLimit(1).bindValue("status", status).execute();
            if (!results.isEmpty()) {
                return results.get(0).equals(1);
            } else {
                return false;
            }
        } catch (QueryException e) {
            logger.error("Failed to retrieve the form active property for ideas status [{}]", status, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return a list of statuses sorted by their order property.
     */
    public List<Object[]> getSortedStatuses()
    {
        try {
            Query query = queryManager.createQuery("select prop1.value, prop3.value from BaseObject as obj,"
                + " StringProperty as prop1, IntegerProperty as prop2, IntegerProperty as prop3"
                + " where obj.className='Ideas.Code.StatusClass' and obj.id=prop1.id.id and prop1.id.name='status'"
                + " and obj.id=prop2.id.id and prop2.id.name='order'"
                + " and obj.id=prop3.id.id and prop3.id.name='formActive' order by prop2.value", Query.HQL);
            return query.execute();
        } catch (QueryException e) {
            logger.error("Failed to retrieve the sorted idea statuses.", e);
            throw new RuntimeException(e);
        }
    }
}
