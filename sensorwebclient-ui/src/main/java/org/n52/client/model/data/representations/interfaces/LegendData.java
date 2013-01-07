/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.client.model.data.representations.interfaces;

import org.n52.client.view.gui.elements.legend.LegendElement;

/**
 * The Interface DataWrapper.
 * 
 * @author <a href="mailto:f.bache@52north.de">Felix Bache</a>
 */
public interface LegendData {

    /**
     * Gets the iD.
     * 
     * @return the iD
     */
    public String getId();

    /**
     * Gets the ordering.
     * 
     * @return the ordering
     */
    public int getOrdering();

    /**
     * Sets the ordering.
     * 
     * @param ordering
     *            the new ordering
     */
    public void setOrdering(int ordering);

    /**
     * Gets the legend element.
     * 
     * @return the legend element
     */
    public LegendElement getLegendElement();

    /**
     * Sets the legend element.
     * 
     * @param elem
     *            the new legend element
     */
    public void setLegendElement(LegendElement elem);

    /**
     * Checks for data.
     * 
     * @return true, if successful
     */
    public boolean hasData();

}
