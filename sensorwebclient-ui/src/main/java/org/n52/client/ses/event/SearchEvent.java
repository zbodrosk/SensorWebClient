/**
 * Copyright (C) 2012-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.client.ses.event;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.ses.event.handler.SearchEventHandler;

/**
 * The Class ChangeLayoutEvent.
 */
public class SearchEvent extends FilteredDispatchGwtEvent<SearchEventHandler> {

    /** The TYPE. */
    public static Type<SearchEventHandler> TYPE = new Type<SearchEventHandler>();
    
    private String text;
    
    private int criterion;
    
    private String userID;

    /**
     * Instantiates a new change layout event.
     * @param text 
     * @param criterion 
     * @param userID 
     * @param blockedHandlers
     *            the blocked handlers
     */
    public SearchEvent(String text, int criterion, String userID, SearchEventHandler... blockedHandlers) {
        super(blockedHandlers);
        this.text = text;
        this.criterion = criterion;
        this.userID = userID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent#onDispatch(com
     * .google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void onDispatch(SearchEventHandler handler) {
        handler.onSearch(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SearchEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * @return criterion
     */
    public int getCriterion() {
        return this.criterion;
    }

    /**
     * @return text
     */
    public String getText() {
        return this.text;
    }

    /**
     * @return userID
     */
    public String getUserID() {
        return this.userID;
    }
}