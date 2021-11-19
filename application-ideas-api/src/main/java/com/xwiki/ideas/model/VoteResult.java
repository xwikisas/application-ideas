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
package com.xwiki.ideas.model;

/**
 * Represents a serializable version of a vote result.
 *
 * @version $Id$
 * @since 1.14
 */
public class VoteResult
{
    private boolean remove;

    private int nbagainst;

    private int nbvotes;

    private int nbopp;

    /**
     * @return whether a vote has been removed or not
     */
    public boolean isRemove()
    {
        return remove;
    }

    /**
     * Sets the remove flag as true denoting the fact that a vote has been removed.
     */
    public void setRemove()
    {
        this.remove = true;
    }

    /**
     * @return then current number of votes against the idea that has been voted.
     */
    public int getNbagainst()
    {
        return nbagainst;
    }

    /**
     * Sets the number of votes against the idea.
     *
     * @param nbagainst the number of votes against the idea.
     */
    public void setNbagainst(int nbagainst)
    {
        this.nbagainst = nbagainst;
    }

    /**
     * @return the number of votes pro the idea that has been voted
     */
    public int getNbvotes()
    {
        return nbvotes;
    }

    /**
     * @param nbvotes the number of votes pro idea
     */
    public void setNbvotes(int nbvotes)
    {
        this.nbvotes = nbvotes;
    }

    /**
     * @return the number of votes that remain after the removal of one.
     */
    public int getNbopp()
    {
        return nbopp;
    }

    /**
     * Sets the remaining number of votes after one was removed. This number will either represent the pro or against
     * votes.
     *
     * @param nbopp current number of votes after removal.
     */
    public void setNbopp(int nbopp)
    {
        this.nbopp = nbopp;
    }
}
