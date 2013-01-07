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
package org.n52.client.control;

import static org.n52.ext.link.sos.PermalinkParameter.BEGIN;
import static org.n52.ext.link.sos.PermalinkParameter.END;
import static org.n52.ext.link.sos.PermalinkParameter.FEATURES;
import static org.n52.ext.link.sos.PermalinkParameter.OFFERINGS;
import static org.n52.ext.link.sos.PermalinkParameter.PHENOMENONS;
import static org.n52.ext.link.sos.PermalinkParameter.PROCEDURES;
import static org.n52.ext.link.sos.PermalinkParameter.SERVICES;
import static org.n52.ext.link.sos.PermalinkParameter.VERSIONS;

import java.util.List;
import java.util.Map;

import org.eesgmbh.gimv.client.event.StateChangeEvent;
import org.n52.client.control.service.SOSController;
import org.n52.client.control.service.SesController;
import org.n52.client.eventBus.EventBus;
import org.n52.client.eventBus.EventCallback;
import org.n52.client.eventBus.events.DatesChangedEvent;
import org.n52.client.eventBus.events.InitEvent;
import org.n52.client.eventBus.events.dataEvents.sos.GetFeatureEvent;
import org.n52.client.eventBus.events.dataEvents.sos.GetOfferingEvent;
import org.n52.client.eventBus.events.dataEvents.sos.GetPhenomenonsEvent;
import org.n52.client.eventBus.events.dataEvents.sos.GetProcedureEvent;
import org.n52.client.eventBus.events.dataEvents.sos.GetStationEvent;
import org.n52.client.eventBus.events.dataEvents.sos.NewSOSMetadataEvent;
import org.n52.client.eventBus.events.dataEvents.sos.OverviewIntervalChangedEvent;
import org.n52.client.eventBus.events.dataEvents.sos.OverviewIntervalChangedEvent.IntervalType;
import org.n52.client.eventBus.events.dataEvents.sos.RequestDataEvent;
import org.n52.client.eventBus.events.dataEvents.sos.StoreSOSMetadataEvent;
import org.n52.client.i18n.I18N;
import org.n52.client.model.communication.requestManager.RequestManager;
import org.n52.client.model.data.dataManagers.DataManagerSosImpl;
import org.n52.client.model.data.dataManagers.TimeManager;
import org.n52.client.view.View;
import org.n52.client.view.gui.widgets.Toaster;
import org.n52.client.view.gui.widgets.stationPicker.StationSelector;
import org.n52.ext.link.sos.PermalinkParameter;
import org.n52.ext.link.sos.TimeRange;
import org.n52.shared.Constants;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.SOSMetadataBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

public final class Application {
    
    private static boolean HAS_STARTED = false;
    
    public static boolean isHasStarted() {
    	return HAS_STARTED;
    }
    
    public static void setHasStarted(boolean hasStarted) {
    	HAS_STARTED = hasStarted;
    }
    
	public static void start() {
		// TODO refactor startup to be more explicit
        PropertiesManager.getInstance();
	}

    public static void continueStartup() {

        // init handlers before throwing events
        DataManagerSosImpl.getInst();
        new I18N();
        new SOSController();
        if (PropertiesManager.getInstance().getTabsFromPropertiesFile().contains("SesTab")) {
            new SesController();
        }
        View.getInstance();
        Element element = Document.get().getElementById("loadingWrapper");
        while (element.hasChildNodes()) {
            element.removeChild(element.getFirstChild());
        }

        Application.finishStartup();
    }

    public static void finishStartup() {
        try {
            String currentUrl = URL.decode(Window.Location.getHref());
            if (hasQueryString(currentUrl) && !isGwtHostedModeParameterOnly()) {
                String[] services = getDecodedParameters(SERVICES);
                String[] versions = getDecodedParameters(VERSIONS);
                String[] features = getDecodedParameters(FEATURES);
                String[] offerings = getDecodedParameters(OFFERINGS);
                String[] procedures = getDecodedParameters(PROCEDURES);
                String[] phenomenons = getDecodedParameters(PHENOMENONS);
                TimeRange timeRange = createTimeRange();
                
                if (!isAllParametersAvailable(services, versions, offerings, features, procedures, phenomenons)) {
                    Toaster.getInstance().addErrorMessage(I18N.sosClient.errorUrlParsing());
                    return;
                }

                if (timeRange.isSetStartAndEnd()) {
                    fireNewTimeRangeEvent(timeRange);
                }

                for (int i = 0; i < services.length; i++) {
                    final String service = services[i];
                    final String version = versions[i];
                    final String offering = offerings[i];
                    final String procedure = procedures[i];
                    final String phenomenon = phenomenons[i];
                    final String feature = features[i];

                    SOSMetadataBuilder builder = new SOSMetadataBuilder();
                    builder.addServiceURL(service);
                    builder.addServiceVersion(version);
                    SOSMetadata sosMetadata = new SOSMetadata(builder);
                    StoreSOSMetadataEvent event = new StoreSOSMetadataEvent(sosMetadata);
                    EventBus.getMainEventBus().fireEvent(event);
                    
                    GetPhenomenonsEvent getPhensEvt = new GetPhenomenonsEvent.Builder(service).build();
                    EventBus.getMainEventBus().fireEvent(getPhensEvt);
                    GetFeatureEvent getFoiEvt = new GetFeatureEvent(service, feature);
                    EventBus.getMainEventBus().fireEvent(getFoiEvt);
                    GetOfferingEvent getOffEvt = new GetOfferingEvent(service, offering);
                    EventBus.getMainEventBus().fireEvent(getOffEvt);
                    GetProcedureEvent getProcEvt = new GetProcedureEvent(service, procedure);
                    EventBus.getMainEventBus().fireEvent(getProcEvt);
                    GetStationEvent getStationEvt = new GetStationEvent(service, offering, procedure, phenomenon, feature);
                    EventBus.getMainEventBus().fireEvent(getStationEvt);
                    
                    new PermaLinkController(service, offering, procedure, phenomenon, feature);
                    EventBus.getMainEventBus().fireEvent(new NewSOSMetadataEvent());
                }
            } else {
                showStationSelectorWhenConfigured();
			}
        } catch (Exception e) {
            if (!GWT.isProdMode()) {
                GWT.log("Error evaluating permalink!", e);
            }
            showStationSelectorWhenConfigured();
            Toaster.getInstance().addErrorMessage(I18N.sosClient.errorUrlParsing());
        } finally {
            finalEvents();
        }
    }

    private static boolean isGwtHostedModeParameterOnly() {
        Map<String, List<String>> parameters = Window.Location.getParameterMap();
        boolean hasGwtCodesrvParameter = parameters.containsKey("gwt.codesvr");
        return !GWT.isProdMode() && parameters.size() == 1 && hasGwtCodesrvParameter;
    }

    static TimeRange createTimeRange() {
        String begin = getDecodedParameter(BEGIN);
        String end = getDecodedParameter(END);
        TimeRange timeRange =  TimeRange.createTimeRange(begin, end);
        return timeRange;
    }
    
    private static String getDecodedParameter(PermalinkParameter parameter) {
        return Window.Location.getParameter(parameter.nameLowerCase());
    }

    private static String[] getDecodedParameters(PermalinkParameter parameter) {
        return Window.Location.getParameter(parameter.nameLowerCase()).split(",");
    }

    private static void fireNewTimeRangeEvent(TimeRange timeRange) {
        try {
            DateTimeFormat formatter = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);
            long begin = formatter.parseStrict(timeRange.getStart()).getTime();
            long end = formatter.parseStrict(timeRange.getEnd()).getTime();
            Toaster.getInstance().addMessage("Begin: " + timeRange.getStart());
            Toaster.getInstance().addMessage("End: " + timeRange.getEnd());
            EventBus.getMainEventBus().fireEvent(new DatesChangedEvent(begin, end, true));
        } catch (Exception e) {
            if (!GWT.isProdMode()) {
                GWT.log("Unparsable TimeRange: " + timeRange, e);
            }
        }
    }

    private static void showStationSelectorWhenConfigured() {
        PropertiesManager properties = PropertiesManager.getInstance();
        boolean showAtStartup = properties.getParameterAsBoolean("showStationSelectorAtStartup");
        if (showAtStartup) {
            StationSelector.getInst().show();
        }
    }

    private static boolean hasQueryString(String currentUrl) {
        return currentUrl.indexOf("?") > -1;
    }
    
    private static boolean isAllParametersAvailable(String[] serviceUrls, String[] versions, String[] offerings,
                                                    String[] fois, String[] procedures, String[] phenomenons) {
        boolean serviceUrlAvailable = serviceUrls.length != 0;
        boolean versionAvailalbe = versions.length != 0;
        boolean offeringAvailable = offerings.length != 0;
        boolean featuresAvailable = fois.length != 0;
        boolean proceduresAvailable = procedures.length != 0;
        boolean phenomenonAvailable = phenomenons.length != 0;
        return serviceUrlAvailable && versionAvailalbe && offeringAvailable && featuresAvailable && proceduresAvailable && phenomenonAvailable ;
    }

    private static void finalEvents() {
        // check for time intervals bigger than the default overview interval
        // (in days)
        PropertiesManager propertiesMgr = PropertiesManager.getInstance();
        int days = propertiesMgr.getParamaterAsInt(Constants.DEFAULT_OVERVIEW_INTERVAL, 5);

        TimeManager timeMgr = TimeManager.getInst();
        long timeInterval = timeMgr.daysToMillis(days);
        long currentInterval = timeMgr.getEnd() - timeMgr.getBegin();

        if (timeInterval <= currentInterval) {
            timeInterval += timeMgr.getOverviewOffset(timeMgr.getBegin(), timeMgr.getEnd());
        }

        EventBus.getMainEventBus().fireEvent(new OverviewIntervalChangedEvent(timeInterval, IntervalType.DAY));
        EventBus.getMainEventBus().fireEvent(new InitEvent(), new EventCallback() {
            
            public void onEventFired() {
                EventBus.getOverviewChartEventBus().fireEvent(StateChangeEvent.createMove());
                final Timer t = new Timer() {

                    @Override
                    public void run() {
                        if (RequestManager.hasUnfinishedRequests()) {
                            this.schedule(200);
                        } else {
                        	HAS_STARTED = false;
                            EventBus.getMainEventBus().fireEvent(new RequestDataEvent());
                        }
                    }
                };
                t.schedule(200);
            }
        });
    }
}
