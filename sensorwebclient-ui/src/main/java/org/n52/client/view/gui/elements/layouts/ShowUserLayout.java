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
package org.n52.client.view.gui.elements.layouts;

import java.util.List;

import org.n52.client.eventBus.EventBus;
import org.n52.client.eventBus.events.ses.DeleteUserEvent;
import org.n52.client.eventBus.events.ses.GetAllUsersEvent;
import org.n52.client.i18n.I18N;
import org.n52.client.model.data.representations.UserDS;
import org.n52.client.model.data.representations.UserRecord;
import org.n52.client.view.gui.elements.interfaces.Layout;
import org.n52.client.view.gui.widgets.CreateNewUserWindow;
import org.n52.client.view.gui.widgets.EditUserWindow;
import org.n52.shared.serializable.pojos.UserDTO;

import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

/**
 * The Class ShowUserLayout.
 * 
 * This view is for admins and shows a table with all registered and activated
 * users. Here the admin can delete or edit user data.
 * 
 * @author <a href="mailto:osmanov@52north.org">Artur Osmanov</a>
 */
public class ShowUserLayout extends Layout {

    /** The user grid. */
    private ListGrid userGrid;

    private UserDS dataSource;
    
    private boolean first = true;

    /**
     * Instantiates a new show user layout.
     */
    public ShowUserLayout() {
        super(I18N.sesClient.userManagement());
        
        // init database
        this.dataSource = new UserDS();
        
        init();
    }

    /**
     * Inits the layout.
     */
    private void init() {
        this.userGrid = new ListGrid() {
            @Override
            protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {

                String fieldName = this.getFieldName(colNum);

                if (fieldName.equals("editField")) {

                    // edit button
                    IButton editButton = new IButton(I18N.sesClient.edit());
                    editButton.setShowDown(false);
                    editButton.setShowRollOver(false);
                    editButton.setLayoutAlign(Alignment.CENTER);
                    editButton.setPrompt(I18N.sesClient.editUserData());
                    editButton.setHeight(16);
                    editButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            EditUserWindow.init((UserRecord)record);
                        }
                    });

                    return editButton;

                } else if (fieldName.equals("deleteField")) {

                    // delete button
                    IButton deleteButton = new IButton(I18N.sesClient.delete());
                    deleteButton.setShowDown(false);
                    deleteButton.setShowRollOver(false);
                    deleteButton.setLayoutAlign(Alignment.CENTER);
                    deleteButton.setPrompt(I18N.sesClient.deleteUserData());
                    deleteButton.setHeight(16);
                    deleteButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            SC.ask(I18N.sesClient.reallyDeleteUser() + ": " + record.getAttribute("userName") +  "?", new BooleanCallback() {
                                public void execute(Boolean value) {
                                    if (value) {
                                        EventBus.getMainEventBus().fireEvent(
                                                new DeleteUserEvent(record.getAttribute("parameterId")));
                                        EventBus.getMainEventBus().fireEvent(new GetAllUsersEvent());
                                    }
                                }
                            });
                        }
                    });
                    return deleteButton;
                } else {
                    return null;
                }
            }
        };

        // grid config
        this.userGrid.setShowRecordComponents(true);
        this.userGrid.setShowRecordComponentsByCell(true);
        this.userGrid.setWidth100();
        this.userGrid.setHeight100();
        this.userGrid.setShowAllRecords(false);
        this.userGrid.setShowFilterEditor(true);
        this.userGrid.setFilterOnKeypress(true);
        this.userGrid.setDataSource(this.dataSource);
        this.userGrid.setAutoFetchData(true);
        this.userGrid.setShowRollOver(false);
        this.userGrid.sort(0, SortDirection.ASCENDING);
        this.userGrid.setCanResizeFields(false);

        // grid fields
        ListGridField userNameField = new ListGridField("userName", I18N.sesClient.userName());
        userNameField.setAlign(Alignment.CENTER);
        
        ListGridField nameField = new ListGridField("name", I18N.sesClient.name());
        nameField.setAlign(Alignment.CENTER);

        ListGridField emailField = new ListGridField("eMail", I18N.sesClient.emailAddress());
        emailField.setAlign(Alignment.CENTER);

        ListGridField handyField = new ListGridField("handy", I18N.sesClient.handyNumber());
        handyField.setAlign(Alignment.CENTER);

        ListGridField roleField = new ListGridField("role", I18N.sesClient.role());
        roleField.setWidth(90);
        roleField.setAlign(Alignment.CENTER);

        ListGridField editField = new ListGridField("editField", I18N.sesClient.edit());
        editField.setWidth(110);
        editField.setAlign(Alignment.CENTER);
        editField.setCanFilter(false);

        ListGridField deleteField = new ListGridField("deleteField", I18N.sesClient.delete());
        deleteField.setWidth(110);
        deleteField.setAlign(Alignment.CENTER);
        deleteField.setCanFilter(false);

        this.userGrid.setFields(userNameField, nameField, emailField, handyField, roleField, editField, deleteField);

        // createNewUser button
        IButton createUserButton = new IButton(I18N.sesClient.createNewUser());
        createUserButton.setShowDown(false);
        createUserButton.setShowRollOver(false);
        createUserButton.setLayoutAlign(Alignment.LEFT);
        createUserButton.setPrompt(I18N.sesClient.createNewUser());
        createUserButton.setHeight(20);
        createUserButton.setWidth(130);
        createUserButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                new CreateNewUserWindow();
                CreateNewUserWindow.init();
            }
        });

        this.form.setFields(this.headerItem);
        addMember(this.form);
        addMember(this.userGrid);
        addMember(this.spacer);
        addMember(createUserButton);
    }

    /**
     * Sets the data of the table.
     * @param list 
     */
    public void setData(List<UserDTO> list) {
        UserDTO user;
        UserRecord userRecord;
        
        if (!this.first) {
            this.userGrid.selectAllRecords();
            this.userGrid.removeSelectedData(); 
        }

        for (int i = 0; i < list.size(); i++) {
            user = list.get(i);
            userRecord = new UserRecord(String.valueOf(user.getId()), user.getUserName(), user.getName(), user.getPassword(), user.geteMail(), user.getHandyNr(), user.getRole().toString());

            this.userGrid.addData(userRecord);
        }
        
        this.first = false;
        this.userGrid.fetchData();
    }
}