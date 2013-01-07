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

package org.n52.client.view.gui.elements.legend;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.control.PropertiesManager;
import org.n52.client.control.service.SOSController;
import org.n52.client.eventBus.EventBus;
import org.n52.client.eventBus.events.ChangeTimeSeriesStyleEvent;
import org.n52.client.eventBus.events.DatesChangedEvent;
import org.n52.client.eventBus.events.LegendElementSelectedEvent;
import org.n52.client.eventBus.events.TimeSeriesChangedEvent;
import org.n52.client.eventBus.events.UpdateScaleEvent;
import org.n52.client.eventBus.events.dataEvents.sos.DeleteTimeSeriesEvent;
import org.n52.client.eventBus.events.dataEvents.sos.ExportEvent;
import org.n52.client.eventBus.events.dataEvents.sos.FirstValueOfTimeSeriesEvent;
import org.n52.client.eventBus.events.dataEvents.sos.StoreTimeSeriesLastValueEvent;
import org.n52.client.eventBus.events.dataEvents.sos.StoreTimeSeriesPropsEvent;
import org.n52.client.eventBus.events.dataEvents.sos.SwitchAutoscaleEvent;
import org.n52.client.eventBus.events.dataEvents.sos.TimeSeriesHasDataEvent;
import org.n52.client.eventBus.events.dataEvents.sos.handler.StoreTimeSeriesFirstValueEventHandler;
import org.n52.client.eventBus.events.dataEvents.sos.handler.StoreTimeSeriesLastValueEventHandler;
import org.n52.client.eventBus.events.dataEvents.sos.handler.SwitchAutoscaleEventHandler;
import org.n52.client.eventBus.events.dataEvents.sos.handler.TimeSeriesHasDataEventHandler;
import org.n52.client.eventBus.events.handler.LegendElementSelectedEventHandler;
import org.n52.client.eventBus.events.handler.TimeSeriesChangedEventHandler;
import org.n52.client.eventBus.events.handler.UpdateScaleEventHandler;
import org.n52.client.i18n.I18N;
import org.n52.client.model.data.DataStoreTimeSeriesImpl;
import org.n52.client.model.data.dataManagers.TimeManager;
import org.n52.client.model.data.representations.TimeSeries;
import org.n52.client.model.data.representations.interfaces.DataWrapper;
import org.n52.client.view.View;
import org.n52.client.view.gui.elements.interfaces.LegendElement;
import org.n52.client.view.gui.widgets.Toaster;
import org.n52.client.view.gui.widgets.buttons.ImageButton;
import org.n52.client.view.gui.widgets.buttons.SmallButton;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ContentsType;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DragStopEvent;
import com.smartgwt.client.widgets.events.DragStopHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.ColorPickerItem;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SliderItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

public class LegendEntryTimeSeries extends Layout implements LegendElement {

	private static final String LINE_STYLE_LINE_DOTS = "5";

	private static final String LINE_STYLE_DASHED = "4";

	private static final String LINE_STYLE_DOTS = "3";

	private static final String LINE_STYLE_AREA = "2";

	private static final String LINE_STYLE_LINE = "1";

	protected String timeseriesID;

	protected HLayout legendEntryHead;

	private VLayout legendEntry;

	private SmallButton titleCol;

	private Label titleLabel;

	@SuppressWarnings("unused")
	private String width;

	private String height;

	protected boolean isSelected = false;

	protected Img noDataSign;

	protected Img loadingSpinner;

	private VLayout legendEntryFoot;

	private VLayout legendInfo;

	private Label phenonmenonLabel;

	protected Label firstValueInterval;

	protected Label lastValueInterval;

	private Label stationLabel;

	private VLayout refvalLayout;

	protected SliderItem slider;

	protected ComboBoxItem seriesType;

	protected ComboBoxItem lineStyles;
	
	protected ComboBoxItem lineWidth;

	protected RadioGroupItem scale;

	private DynamicForm cpForm;

	protected ColorPickerItem colors;

	protected Window styleChanger;

	private LegendEntryTimeSeriesEventBroker eventBroker;

	private SmallButton colorButton;

	private SmallButton infoButton;
	
	private Window informationWindow;

	private SmallButton deleteButton;

	private boolean didUpdateRefVals = false;

	protected boolean wasDragged = false;

	private DateTimeFormat formatter = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

	public LegendEntryTimeSeries(TimeSeries ts, String width, String height) {

		this.width = width;
		this.height = height;
		this.timeseriesID = ts.getId();
		this.eventBroker = new LegendEntryTimeSeriesEventBroker();

		init();
	}

	public LegendEntryTimeSeriesEventBroker getEventBroker() {
		return this.eventBroker;
	}

	@Override
	protected void onDraw() {
		super.onDraw();
		if (this.wasDragged) {

			// members are updated earlier than children!!
			Canvas[] members = View.getInstance().getLegend().getLegendStack()
					.getMembers();
			for (int entryIndex = 0; entryIndex < members.length; entryIndex++) {
				Canvas canvas = members[entryIndex];
				if (canvas instanceof LegendElement) {
					LegendElement element = (LegendElement) canvas;
					element.setOrdering(entryIndex);
					DataStoreTimeSeriesImpl dataStore = DataStoreTimeSeriesImpl
							.getInst();
					dataStore.getDataItem(element.getElemId()).setOrdering(
							entryIndex);
				}
			}
			this.wasDragged = false;
			EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
		}
	}

	private void init() {

		setAutoHeight();
		setStyleName("sensorweb_client_legendEntryLayout");
		setCanDrag(true);
		setCanDrop(true);
		setKeepInParentRect(true);
		setWidth("100%");

		addDragStopHandler(new DragStopHandler() {
			public void onDragStop(DragStopEvent event) {
				LegendEntryTimeSeries.this.wasDragged = true;
			}
		});

		this.legendEntry = new VLayout();
		this.legendEntry.setMinWidth(220);
		this.legendEntry.setWidth("100%");

		// legend head
		this.legendEntryHead = new HLayout();
		this.legendEntryHead.setTabIndex(-1);
		this.legendEntryHead.setStyleName("sensorweb_client_legendEntryHeaderNoData");
		this.legendEntryHead.setCursor(Cursor.POINTER);
		this.legendEntryHead.setHeight(this.height);

		this.noDataSign = new Img("../img/icons/exclamation.png");
		this.noDataSign.setTooltip(I18N.sosClient.noDataAvailable());
		this.noDataSign.setWidth(16);
		this.noDataSign.setHeight(16);
		this.noDataSign.setPadding(6);

		this.loadingSpinner = new Img("../img/loader.gif");
		this.loadingSpinner.setWidth(16);
		this.loadingSpinner.setHeight(16);
		this.loadingSpinner.setPadding(6);

		this.titleCol = new SmallButton(new Label(), I18N.sosClient.changeColor(),
				I18N.sosClient.changeColorExtended());
		this.titleCol.hide();
		this.titleCol.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LegendEntryTimeSeries.this.styleChanger.show();
			}
		});
		
		this.titleLabel = new Label();
		this.titleLabel.setWidth100();
		this.titleLabel.setStyleName("sensorweb_client_legendEntryTitle");
		this.titleLabel.setOverflow(Overflow.HIDDEN);
		this.titleLabel.setCursor(Cursor.POINTER);
		this.titleLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isFooterVisible()) {
					hideFooter();
				} else {
					showFooter();
				}
				EventBus.getMainEventBus().fireEvent(
						new LegendElementSelectedEvent(
								LegendEntryTimeSeries.this, false));
			}
		});
		
		this.legendEntryHead.addMember(this.loadingSpinner);
		this.legendEntryHead.addMember(this.noDataSign);
		this.legendEntryHead.addMember(this.titleLabel);
		this.legendEntryHead.addMember(this.titleCol);
		this.legendEntryHead.addMember(createLegendTools());

		// legend foot
		this.legendEntryFoot = new VLayout();
		this.legendEntryFoot.setStyleName("sensorweb_client_legendEntryFooter");
		HLayout hLegendInfos = new HLayout();
		
		HStack separator = new HStack();
		separator.setWidth(10);
		hLegendInfos.addMember(separator);
		hLegendInfos.addMember(createLegendInfo());
		this.legendEntryFoot.addMember(hLegendInfos);
		this.legendEntryFoot.addMember(createRefValueLayout());
		
		createStyleToolsWindow();

		this.legendEntryFoot.hide();

		this.legendEntry.addMember(this.legendEntryHead);
		this.legendEntry.addMember(this.legendEntryFoot);
		addMember(this.legendEntry);
	}

	private Canvas createLegendInfo() {

		this.legendInfo = new VLayout();
		this.legendInfo.setTabIndex(-1);
		// this.offeringLabel = new Label();
		// this.procedureLabel = new Label();
		this.phenonmenonLabel = new Label();
		this.stationLabel = new Label();
		this.firstValueInterval = new Label();
		this.lastValueInterval = new Label();
		
		this.phenonmenonLabel.setStyleName("sensorweb_client_legendInfoRow");
		this.stationLabel.setStyleName("sensorweb_client_legendInfoRow");

		// this.offeringLabel.setHeight(15);
		// this.procedureLabel.setHeight(15);
		this.phenonmenonLabel.setAutoHeight();
		this.stationLabel.setAutoHeight();

		// this.legendInfo.addMember(this.offeringLabel);
		// this.legendInfo.addMember(this.procedureLabel);
		this.legendInfo.addMember(this.phenonmenonLabel);
		this.legendInfo.addMember(this.stationLabel);
		this.legendInfo.addMember(createValueIntervalLabel());
		
		return this.legendInfo;
	}

	private Canvas createValueIntervalLabel() {
		Layout interval = new HLayout();
		interval.setAutoWidth();
		interval.setStyleName("sensorweb_client_legendInfoRow");
		this.firstValueInterval.setAutoWidth();
		this.firstValueInterval.setWrap(false);
		this.firstValueInterval.setStyleName("sensorweb_client_legendlink");
		this.lastValueInterval.setAutoWidth();
		this.lastValueInterval.setWrap(false);
		this.lastValueInterval.setStyleName("sensorweb_client_legendlink");
		Label separator = new Label(I18N.sosClient.to());
		separator.setAlign(Alignment.CENTER);
		separator.setWidth(20);
		interval.addMember(this.firstValueInterval);
		interval.addMember(separator);
		interval.addMember(this.lastValueInterval);
		return interval;
	}

	private Canvas createRefValueLayout() {
		this.refvalLayout = new VLayout();
		this.refvalLayout.setTabIndex(-1);
		this.refvalLayout.setHeight("*");
		this.refvalLayout.setOverflow(Overflow.VISIBLE);
		this.refvalLayout.setStyleName("sensorweb_client_refValLayout");

		return this.refvalLayout;
	}

	protected TimeSeries getTimeSeries() {
		return ((TimeSeries) getDataWrapper());
	}

	private Canvas createLegendTools() {
		createColorChangeButton();
		createInformationButton();
		createDeleteLegendEntryButton();

		HLayout tools = new HLayout();
		tools.addMember(this.infoButton);
		tools.addMember(this.deleteButton);

		return tools;
	}

	@SuppressWarnings("unused")
	private ImageButton createJumpToLastValueButton() {
		final ImageButton jumpLast = new ImageButton("jumpLast",
				"../img/icons/control_end_blue.png",
				I18N.sosClient.jumpToLast(),
				I18N.sosClient.jumpToLastExtended());
		View.getInstance().registerTooltip(jumpLast);
		jumpLast.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				long date = LegendEntryTimeSeries.this.getTimeSeries()
						.getLastValueDate();
				if (date == 0) {
					Toaster.getInstance().addMessage(
							I18N.sosClient.errorSOS() + ": "
									+ I18N.sosClient.jumpToLast());
					return;
				}
				long interval = TimeManager.getInst().getEnd() - date;

				long begin = TimeManager.getInst().getBegin() - interval;

				EventBus.getMainEventBus().fireEvent(
						new DatesChangedEvent(begin, date));

			}
		});
		return jumpLast;
	}

	@SuppressWarnings("unused")
	private ImageButton createJumpToFirstValueButton() {
		ImageButton jumpFirst = new ImageButton("jumpFirst",
				"../img/icons/control_start_blue.png",
				I18N.sosClient.jumpToFirst(),
				I18N.sosClient.jumpToFirstExtended());
		View.getInstance().registerTooltip(jumpFirst);
		jumpFirst.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				long date = LegendEntryTimeSeries.this.getTimeSeries()
						.getFirstValueDate();

				if (date == 0) {
					Toaster.getInstance().addMessage(
							I18N.sosClient.errorSOS() + ": "
									+ I18N.sosClient.jumpToFirst());
					return;
				}

				long interval = TimeManager.getInst().getBegin() - date;

				long end = TimeManager.getInst().getEnd() - interval;

				EventBus.getMainEventBus().fireEvent(
						new DatesChangedEvent(date, end));

			}
		});
		return jumpFirst;
	}

	@SuppressWarnings("unused")
	private ImageButton createPDFExportButton() {
		ImageButton exportPDF = new ImageButton("exportPDF",
				"../img/icons/page_white_acrobat.png",
				I18N.sosClient.exportPDF(), I18N.sosClient.exportPDFExtended());
		View.getInstance().registerTooltip(exportPDF);
		exportPDF.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				ArrayList<TimeSeries> series = new ArrayList<TimeSeries>();
				series.add(getTimeSeries());
				EventBus.getMainEventBus().fireEvent(
						new ExportEvent(series, ExportEvent.ExportType.PDF));

			}
		});
		return exportPDF;
	}

	@SuppressWarnings("unused")
	private ImageButton createXLSExportButton() {
		ImageButton exportXLS = new ImageButton(
				"exportXLS", "../img/icons/page_white_excel.png", //$NON-NLS-2$
				I18N.sosClient.exportXLS(), I18N.sosClient.exportXLSExtended());
		View.getInstance().registerTooltip(exportXLS);
		exportXLS.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				List<TimeSeries> series = new ArrayList<TimeSeries>();
				series.add(getTimeSeries());
				EventBus.getMainEventBus().fireEvent(
						new ExportEvent(series, ExportEvent.ExportType.XLS));

			}
		});
		return exportXLS;
	}

	@SuppressWarnings("unused")
	private ImageButton createCSVExportButton() {
		ImageButton exportCSV = new ImageButton(
				"exportCSV", "../img/icons/table.png", //$NON-NLS-2$
				I18N.sosClient.exportCSV(), I18N.sosClient.exportCSVExtended());
		View.getInstance().registerTooltip(exportCSV);
		exportCSV.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				List<TimeSeries> series = new ArrayList<TimeSeries>();
				series.add(getTimeSeries());
				EventBus.getMainEventBus().fireEvent(
						new ExportEvent(series, ExportEvent.ExportType.CSV));

			}
		});
		return exportCSV;
	}

	private void createDeleteLegendEntryButton() {
		this.deleteButton = new SmallButton(new Img("../img/icons/del.png"),
				I18N.sosClient.delete(), I18N.sosClient.deleteExtended());
		this.deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent evt) {
				if (SOSController.isDeletingTS) {
					Toaster.getInstance().addMessage(I18N.sosClient.deleteTimeSeriesActiv());
				} else {
					SOSController.isDeletingTS = true;
					LegendEntryTimeSeries.this.getEventBroker().unregister();
					EventBus.getMainEventBus().fireEvent(
							new DeleteTimeSeriesEvent(LegendEntryTimeSeries.this
									.getElemId()));	
				}
			}
		});
	}

	private void createInformationButton() {
		this.infoButton = new SmallButton(new Img("../img/icons/info.png"),
				I18N.sosClient.infos(), I18N.sosClient.infoExtended());
		this.infoButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (informationWindow == null) {
					createInformationWindow();
				}
				informationWindow.show();
			}
		});
	}
	
	private void createInformationWindow() {
		informationWindow = new Window();
		informationWindow.setTitle(LegendEntryTimeSeries.this.getTimeSeries().getTimeSeriesLabel());
		informationWindow.setWidth(450);
		informationWindow.setHeight(500);
		informationWindow.setShowMinimizeButton(false);
		informationWindow.centerInPage();
		HTMLPane htmlPane = new HTMLPane();
		htmlPane.setContentsURL(LegendEntryTimeSeries.this
				.getTimeSeries().getMetadataUrl());
		htmlPane.setContentsType(ContentsType.PAGE);
		informationWindow.addItem(htmlPane);
	}

	private void createColorChangeButton() {
		this.colorButton = new SmallButton(
				new Img("../img/icons/bullet_wrench.png"), I18N.sosClient.changeColor(),
				I18N.sosClient.changeColorExtended());
//		View.getInstance().registerTooltip(this.colorButton);
		this.colorButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				if (LegendEntryTimeSeries.this.styleChanger.isVisible()) {
					LegendEntryTimeSeries.this.styleChanger.hide();
				} else {
					LegendEntryTimeSeries.this.styleChanger.show();
				}
			}
		});
	}

	public void update() {

		TimeSeries dw = (TimeSeries) getDataWrapper();
		
		String phenomenon = this.getTimeSeries().getProperties()
				.getPhenomenon().getLabel();

		this.titleLabel.setContents("<span>" + phenomenon + "@"
				+ getStationName(dw) + "</span>");
		this.titleCol.getCanvas().setBackgroundColor(dw.getColor());
		this.styleChanger.setTitle(dw.getPhenomenonId() + "@" + getStationName(dw));
		if (dw.hasData()) {
			this.noDataSign.hide();
			if (this.isSelected) {
				this.legendEntryHead.setStyleName("sensorweb_client_legendEntryHeaderSelected");
			} else {
				this.legendEntryHead.setStyleName("sensorweb_client_legendEntryHeader");
			}
		} else {
			this.noDataSign.show();
			if (this.isSelected) {
				this.legendEntryHead
						.setStyleName("sensorweb_client_legendEntryHeaderSelectedNoData");
			} else {
				this.legendEntryHead.setStyleName("sensorweb_client_legendEntryHeaderNoData");
			}

		}

		this.seriesType.setValue(getTimeSeries().getGraphStyle());
		this.lineStyles.setValue(getTimeSeries().getLineStyle());
		this.lineWidth.setValue(getTimeSeries().getLineWidth());
		if (getTimeSeries().isAutoScale()) {
			this.scale.setDefaultValue(I18N.sosClient.autoScale());
		} else {
			this.scale.setDefaultValue(I18N.sosClient.zeroScale());
		}
		this.slider.setValue(getTimeSeries().getOpacity());
		this.colors.setValue(getTimeSeries().getColor());

		// String offering =
		// this.getTimeSeries().getProperties().getOffering().getTitle();
		// comment out, for eventually later use
		// if (offering.contains("/")) {
		// offering = offering.substring(offering.lastIndexOf("/") + 1);
		// }
		// this.offeringLabel.setContents("<span style='font-weight:bold;'>" +
		// I18N.sosClient.offeringLabel()
		// + "</span>: " + offering);

		// String procedure =
		// this.getTimeSeries().getProperties().getProcedure().getDescription();
		// if (procedure.contains("/")) {
		// procedure = procedure.substring(procedure.lastIndexOf("/") + 1);
		// }
		// this.procedureLabel.setContents("<span style='font-weight:bold;'>" +
		// I18N.sosClient.procedureLabel()
		// + "</span>: " + procedure);

		String uom = this.getTimeSeries().getProperties().getUom();
		// if (phenomenon.contains("/")) {
		// phenomenon = phenomenon.substring(phenomenon.lastIndexOf("/") + 1);
		// }
		StringBuilder phenomenonHtmlContent = new StringBuilder();
		phenomenonHtmlContent.append("<span>");
		phenomenonHtmlContent.append(I18N.sosClient.phenomenonLabel());
		phenomenonHtmlContent.append(":</span> ");
		phenomenonHtmlContent.append(phenomenon);
		if (uom != null && !uom.isEmpty()) {
		    phenomenonHtmlContent.append(" [");
		    phenomenonHtmlContent.append(uom);
		    phenomenonHtmlContent.append("]");
        }
        this.phenonmenonLabel.setContents(phenomenonHtmlContent.toString());
		setFirstValueInterval();
		setLastValueInterval();

		// if (station.contains("/")) {
		// station = station.substring(station.lastIndexOf("/") + 1);
		// }
		this.stationLabel.setContents("<span>"
				+ I18N.sosClient.foiLabel() + ":</span> "
				+ getStationName(this.getTimeSeries()));

		Set<String> values = this.getTimeSeries().getProperties()
				.getrefValues();

		if (!this.didUpdateRefVals) {
			if (values.size() != 0) {
				for (final String value : values) {
					DynamicForm refValForm = new DynamicForm();
					refValForm.setWidth(15);
					HLayout refValRow = new HLayout();
					refValRow.setTabIndex(-1);

					final CheckboxItem check = new CheckboxItem();
					// check.setTitle("");
					check.setShowTitle(false);
					check.setWidth(15);
					// check.setTitleStyle("width=80px;");
					// check.setTitleOrientation(TitleOrientation.RIGHT);
					check.setShowLabel(false);
					check.setLabelAsTitle(false);
					check.setValue(this.getTimeSeries().getProperties()
							.getRefValue(value).show());

					check.addChangedHandler(new ChangedHandler() {

						public void onChanged(ChangedEvent event) {
							getTimeSeries().getProperties().getRefValue(value)
									.setShow(check.getValueAsBoolean());

							EventBus.getMainEventBus().fireEvent(
									new StoreTimeSeriesPropsEvent(
											getTimeSeries().getId(),
											getTimeSeries().getProperties()));
							EventBus.getMainEventBus().fireEvent(
									new LoadImageDataEvent());
						}
					});

					refValForm.setFields(check);

					Label label = new Label(value
							+ ": "
							+ this.getTimeSeries().getProperties()
									.getRefValue(value).getValue());
					label.setAutoHeight();
					label.setWidth100();
					label.setMargin(5);

					Layout colorLayout = new Layout();
					colorLayout.setHeight(20);
					colorLayout.setWidth(20);
					String color = getTimeSeries().getProperties().getRefValue(value).getColor();
					colorLayout.setStyleName("sensorweb_client_legendEntryTitleColorIndicatorSmall");
					colorLayout.setBackgroundColor(color);

					refValRow.addMember(refValForm);
					refValRow.addMember(label);
					refValRow.addMember(colorLayout);

					this.refvalLayout.addMember(refValRow);
					this.didUpdateRefVals = true;
				}
			} else {
				this.refvalLayout.hide();
			}

		}
		this.loadingSpinner.hide();
	}

	private String getStationName(TimeSeries ts) {
		// TODO perhaps use regular expressions
		String station = ts.getStationName();
		// remove phenomenon identifier
		station = station.replace(ts.getPhenomenonId(), "");
		// remove first '-'
		if (station.startsWith("-")) {
			station = station.replaceFirst("-", "");
		}
		// replace '_' with ' '
		station = station.replace("_", " ");
		return station;
	}

	protected void setFirstValueInterval() {
		if (this.getTimeSeries().getFirstValueDate() != 0) {
			this.firstValueInterval.setContents(this.formatter.format(new Date(
					this.getTimeSeries().getFirstValueDate()))
					+ " ("
					+ this.getTimeSeries().getFirstValue()
					+ " "
					+ this.getTimeSeries().getUnitOfMeasure() + ")");
			this.firstValueInterval.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					long date = LegendEntryTimeSeries.this.getTimeSeries()
							.getFirstValueDate();
					if (date == 0) {
						Toaster.getInstance().addMessage(
								I18N.sosClient.errorSOS() + ": "
										+ I18N.sosClient.jumpToFirst());
						return;
					}
					long interval = TimeManager.getInst().getBegin() - date;
					long end = TimeManager.getInst().getEnd() - interval;
					EventBus.getMainEventBus().fireEvent(
							new DatesChangedEvent(date, end));
				}
			});
		} else {
			this.firstValueInterval.setContents(I18N.sosClient.noData());
		}
	}

	protected void setLastValueInterval() {
		if (this.getTimeSeries().getLastValueDate() != 0) {
			this.lastValueInterval.setContents(this.formatter.format(new Date(
					this.getTimeSeries().getLastValueDate()))
					+ " ("
					+ this.getTimeSeries().getLastValue()
					+ " "
					+ this.getTimeSeries().getUnitOfMeasure() + ")");
			this.lastValueInterval.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					long date = LegendEntryTimeSeries.this.getTimeSeries()
							.getLastValueDate();
					if (date == 0) {
						Toaster.getInstance().addMessage(
								I18N.sosClient.errorSOS() + ": "
										+ I18N.sosClient.jumpToLast());
						return;
					}
					long interval = TimeManager.getInst().getEnd() - date;
					long begin = TimeManager.getInst().getBegin() - interval;
					EventBus.getMainEventBus().fireEvent(
							new DatesChangedEvent(begin, date));
				}
			});
		} else {
			this.lastValueInterval.setContents(I18N.sosClient.noData());
		}
	}

	private Canvas createStyleToolsWindow() {
		this.styleChanger = new Window();
		this.styleChanger.setShowModalMask(true);
		this.styleChanger.setWidth(250);
		this.styleChanger.setHeight(280);
		this.styleChanger.setIsModal(true);
		this.styleChanger.centerInPage();
		this.styleChanger.setCanDragResize(true);
		this.styleChanger.setShowCloseButton(true);
		
		this.setCanDrag(true);
		// opacity-slider
		this.slider = new SliderItem();
		this.slider.setTitle(I18N.sosClient.Opacity());
		this.slider.setWidth(120);
		this.slider.setHeight(30);
		this.slider.setMinValue(0f);
		this.slider.setMaxValue(100f);
		this.slider.setValue(this.slider.getMaxValue());

		this.seriesType = new ComboBoxItem();
		String levelLine = I18N.sosClient.levelLine();
		String sumLine = I18N.sosClient.sumLine();
		LinkedHashMap<String, String> levelstyle = new LinkedHashMap<String, String>();
		levelstyle.put(TimeSeries.GRAPH_STYLE_GAUGELINE, levelLine);
		levelstyle.put(TimeSeries.GRAPH_STYLE_SUMLINE, sumLine);
		this.seriesType.setValueMap(levelstyle);
		this.seriesType.setWidth(85);
		this.seriesType.setTitle(I18N.sosClient.seriesType());

		this.seriesType.addChangedHandler(new ChangedHandler() {

			public void onChanged(ChangedEvent event) {
				PropertiesManager propertiesManager = PropertiesManager
						.getInstance();
				String selectedSeriesType = LegendEntryTimeSeries.this.seriesType
						.getValue().toString();

				if (selectedSeriesType.equals(TimeSeries.GRAPH_STYLE_GAUGELINE)) {
					String defaultHydrographStyle = propertiesManager
							.getParameterAsString("defaultHydrographStyle");
					LegendEntryTimeSeries.this.lineStyles
							.setValue(defaultHydrographStyle);
				} else if (selectedSeriesType
						.equals(TimeSeries.GRAPH_STYLE_SUMLINE)) {
					String defaultSumlineStyle = propertiesManager
							.getParameterAsString("defaultSumLineStyle");
					LegendEntryTimeSeries.this.lineStyles
							.setValue(defaultSumlineStyle);
				}

			}
		});

		LinkedHashMap<String, String> styles = new LinkedHashMap<String, String>();
		styles.put(LINE_STYLE_LINE, I18N.sosClient.lineStyle());
		styles.put(LINE_STYLE_DOTS, I18N.sosClient.dottedStyle());
		styles.put(LINE_STYLE_AREA, I18N.sosClient.areaStyle());
		styles.put(LINE_STYLE_DASHED, I18N.sosClient.dashedStyle());
		styles.put(LINE_STYLE_LINE_DOTS, I18N.sosClient.lineDotsStyle());

		this.lineStyles = new ComboBoxItem();
		this.lineStyles.setTitle(I18N.sosClient.linestyle());
		this.lineStyles.setShowTitle(true);
		this.lineStyles.setValueMap(styles);
		this.lineStyles.setWidth(85);
		
		this.lineWidth = new ComboBoxItem();
		this.lineWidth.setTitle(I18N.sosClient.lineWidth());
		this.lineWidth.setShowTitle(true);
		this.lineWidth.setWidth(85);
		this.lineWidth.setValueMap("1","2","3","4","5","6","7","8","9","10");

		this.scale = new RadioGroupItem();
		this.scale.setTitle(I18N.sosClient.scale());
		this.scale.setValueMap(I18N.sosClient.zeroScale(),
				I18N.sosClient.autoScale());

		this.cpForm = new DynamicForm();
		this.cpForm.setNumCols(1);
		this.cpForm.setAlign(Alignment.LEFT);
		this.cpForm.setAutoWidth();

		this.colors = new ColorPickerItem();
		this.colors.setShowTitle(true);
		this.colors.setTitle(I18N.sosClient.color());
		this.colors.setWidth(85);
		this.cpForm.setShowComplexFields(false);
		this.cpForm.setFields(this.scale, this.seriesType, this.lineStyles, 
				this.lineWidth, this.colors, this.slider);
		this.cpForm.setSaveOnEnter(true);

		SmallButton conf = new SmallButton(
				new Img("../img/icons/acc.png"),
				I18N.sosClient.setAndRefresh(),
				I18N.sosClient.setAndRefreshExt());
		conf.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				LegendEntryTimeSeries.this.styleChanger.hide();

				TimeSeries timeseries = DataStoreTimeSeriesImpl.getInst()
						.getDataItem(LegendEntryTimeSeries.this.timeseriesID);
				timeseries.setLineStyle(LegendEntryTimeSeries.this.lineStyles
						.getValue().toString());
				timeseries.setSeriesType(LegendEntryTimeSeries.this.seriesType
						.getValue().toString());
				timeseries.setLineWidth(Integer
						.valueOf(LegendEntryTimeSeries.this.lineWidth
								.getValueAsString()));
				boolean scaleToNullCheck = false;
				boolean autoScaleCheck = false;
				if (LegendEntryTimeSeries.this.scale.getValueAsString().equals(
						I18N.sosClient.zeroScale())) {
					scaleToNullCheck = true;
				} else {
					autoScaleCheck = true;
				}
				EventBus.getMainEventBus().fireEvent(
						new UpdateScaleEvent(LegendEntryTimeSeries.this
								.getTimeSeries().getPhenomenonId(), scaleToNullCheck,
								autoScaleCheck));
				EventBus.getMainEventBus()
						.fireEvent(
								new ChangeTimeSeriesStyleEvent(
										LegendEntryTimeSeries.this
												.getTimeSeries().getId(),
										LegendEntryTimeSeries.this.colors
												.getValue().toString(),
										new Double(
												LegendEntryTimeSeries.this.slider
														.getValue().toString()),
										scaleToNullCheck,
										LegendEntryTimeSeries.this.lineStyles
												.getValue().toString(),
										autoScaleCheck));
				EventBus.getMainEventBus().fireEvent(
						new StoreTimeSeriesPropsEvent(getTimeSeries().getId(),
								getTimeSeries().getProperties()));
			}
		});
		SmallButton cancelColor = new SmallButton(new Img(
				"../img/icons/del.png"), I18N.sosClient.cancel(),
				I18N.sosClient.cancel());
		cancelColor.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LegendEntryTimeSeries.this.styleChanger.hide();
			}
		});

		HLayout buttonsStyle = new HLayout();
		buttonsStyle.setAutoHeight();
		buttonsStyle.setAlign(Alignment.RIGHT);
		buttonsStyle.addMember(conf);
		buttonsStyle.addMember(cancelColor);

		HLayout hlayout = new HLayout();
		hlayout.setAlign(Alignment.CENTER);
		hlayout.addMember(this.cpForm);
//		hlayout.setBorder("1px solid red");
		VLayout vlayout = new VLayout();
		vlayout.setAlign(Alignment.CENTER);
//		vlayout.setBorder("1px solid blue");
		vlayout.addMember(hlayout);
		vlayout.addMember(buttonsStyle);
		
		this.styleChanger.setStyleName("sensorweb_client_styleChangerForm");
		this.styleChanger.addItem(vlayout);

		this.styleChanger.hide();

		return this.styleChanger;
	}

	public String getElemId() {
		return this.timeseriesID;
	}

	public DataWrapper getDataWrapper() {
		return DataStoreTimeSeriesImpl.getInst().getDataItem(this.timeseriesID);
	}

	public void updateLayout() {
		// TODO not needed?
	}

	@SuppressWarnings("unused")
	private HLayout createOrderEntryTools() {
		HLayout ordButt = new HLayout();
		ordButt.setTabIndex(-1);
		ordButt.setWidth(40);
		ordButt.setAlign(Alignment.RIGHT);

		ImageButton up = new ImageButton("up", "../img/icons/arrow_up.png",
				I18N.sosClient.up(), I18N.sosClient.upExt());
		ImageButton down = new ImageButton("down",
				"../img/icons/arrow_down.png", I18N.sosClient.down(),
				I18N.sosClient.downExt());
		up.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Canvas[] members = View.getInstance().getLegend()
						.getLegendStack().getMembers();

				for (int i = 0; i < members.length; i++) {

					if (members[i] instanceof LegendElement) {

						LegendElement le = (LegendElement) members[i];

						if (le.equals(LegendEntryTimeSeries.this)) {
							try {
								EventBus.getMainEventBus().fireEvent(
										new LegendElementSelectedEvent(le,
												false));
								LegendElement pred = (LegendElement) members[i - 1];
								int tmp = pred.getOrdering();
								pred.setOrdering(le.getOrdering());
								le.setOrdering(tmp);

								DataStoreTimeSeriesImpl.getInst()
										.getDataItem(le.getElemId())
										.setOrdering(le.getOrdering());
								DataStoreTimeSeriesImpl.getInst()
										.getDataItem(pred.getElemId())
										.setOrdering(pred.getOrdering());

							} catch (Exception e) {
								// was the first one, so do nothing
								return;
							}
						}
					}
				}

				EventBus.getMainEventBus().fireEvent(
						new TimeSeriesChangedEvent());
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
			}
		});

		down.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Canvas[] members = View.getInstance().getLegend()
						.getLegendStack().getMembers();

				for (int i = 0; i < members.length; i++) {

					if (members[i] instanceof LegendElement) {

						LegendElement le = (LegendElement) members[i];

						if (le.equals(LegendEntryTimeSeries.this)) {
							try {
								EventBus.getMainEventBus().fireEvent(
										new LegendElementSelectedEvent(le,
												false));
								LegendElement succ = (LegendElement) members[i + 1];
								int tmp = succ.getOrdering();
								succ.setOrdering(le.getOrdering());
								le.setOrdering(tmp);

								DataStoreTimeSeriesImpl.getInst()
										.getDataItem(le.getElemId())
										.setOrdering(le.getOrdering());
								DataStoreTimeSeriesImpl.getInst()
										.getDataItem(succ.getElemId())
										.setOrdering(succ.getOrdering());

							} catch (Exception e) {
								// was the last one, so do nothing
								return;
							}
						}
					}
				}
				EventBus.getMainEventBus().fireEvent(
						new TimeSeriesChangedEvent());
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
			}
		});

		ordButt.addMember(up);
		ordButt.addMember(down);
		return ordButt;
	}

	public int getOrdering() {
		return this.getTimeSeries().getOrdering();
	}

	public void setOrdering(int ordering) {
		this.getTimeSeries().setOrdering(ordering);
	}

	public void hideFooter() {
		this.infoButton.hide();
		this.deleteButton.hide();
		this.legendEntryFoot.hide();
	}

	public void showFooter() {
		this.infoButton.show();
		this.deleteButton.show();
		this.legendEntryFoot.show();
	}

	public Canvas getClickTarget() {
		return this.legendEntryHead;
	}

	public void setTargetToDrag(Canvas c) {
		setDragTarget(c);
	}

	@Override
	public boolean isVisible() {
		// TODO check interface
		return false;
	}

	@Override
	public void setVisible(boolean b) {
		// TODO Auto-generated method stub

	}

	public void setSelected(boolean b) {
		// TODO Auto-generated method stub

	}

	public void setHasNoData(boolean b) {
		// TODO Auto-generated method stub

	}

	public void setFooterVisible(boolean b) {
		// TODO Auto-generated method stub

	}

	public boolean isFooterVisible() {
		return this.legendEntryFoot.isVisible();
	}

	public Layout getLayout() {
		return this;
	}

	public void setHasData(boolean b) {
		// XXX do nothing? check interface
	}

	public boolean getHasData() {
		// XXX always return false? check interface
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((timeseriesID == null) ? 0 : timeseriesID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LegendEntryTimeSeries other = (LegendEntryTimeSeries) obj;
		if (timeseriesID == null) {
			if (other.timeseriesID != null)
				return false;
		} else if (!timeseriesID.equals(other.timeseriesID))
			return false;
		return true;
	}

	public class LegendEntryTimeSeriesEventBroker implements
			TimeSeriesChangedEventHandler,
			LegendElementSelectedEventHandler,
			TimeSeriesHasDataEventHandler,
			StoreTimeSeriesFirstValueEventHandler,
			StoreTimeSeriesLastValueEventHandler,
			UpdateScaleEventHandler,
			SwitchAutoscaleEventHandler {

		public LegendEntryTimeSeriesEventBroker() {
			EventBus.getMainEventBus().addHandler(TimeSeriesChangedEvent.TYPE,
					this);
			EventBus.getMainEventBus().addHandler(
					LegendElementSelectedEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(TimeSeriesHasDataEvent.TYPE,
					this);
			EventBus.getMainEventBus().addHandler(
					FirstValueOfTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(
					StoreTimeSeriesLastValueEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(UpdateScaleEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(SwitchAutoscaleEvent.TYPE, this);
		}

		public void onTimeSeriesChanged(TimeSeriesChangedEvent evt) {
			LegendEntryTimeSeries.this.update();
		}

		public void unregister() {
			EventBus.getMainEventBus().removeHandler(
					TimeSeriesChangedEvent.TYPE, this);
			EventBus.getMainEventBus().removeHandler(
					LegendElementSelectedEvent.TYPE, this);
			EventBus.getMainEventBus().removeHandler(
					TimeSeriesHasDataEvent.TYPE, this);
			EventBus.getMainEventBus().removeHandler(
					FirstValueOfTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().removeHandler(
					StoreTimeSeriesLastValueEvent.TYPE, this);
			EventBus.getMainEventBus().removeHandler(UpdateScaleEvent.TYPE,
					this);
			EventBus.getMainEventBus().removeHandler(SwitchAutoscaleEvent.TYPE, this);
		}

		public void onSelected(LegendElementSelectedEvent evt) {
			if (evt.isNewAdded()) {
				LegendEntryTimeSeries.this.titleCol.show();
				LegendEntryTimeSeries.this.showFooter();
			}
			if (evt.getElement().equals(LegendEntryTimeSeries.this)) {
				LegendEntryTimeSeries.this.isSelected = true;
				if (LegendEntryTimeSeries.this.getTimeSeries().hasData()) {
					LegendEntryTimeSeries.this.noDataSign.hide();
					LegendEntryTimeSeries.this.legendEntryHead
							.setStyleName("sensorweb_client_legendEntryHeaderSelected");
				} else {
					LegendEntryTimeSeries.this.noDataSign.show();
					LegendEntryTimeSeries.this.legendEntryHead
							.setStyleName("sensorweb_client_legendEntryHeaderSelectedNoData");
				}
			} else {
				LegendEntryTimeSeries.this.isSelected = false;
				LegendEntryTimeSeries.this.hideFooter();
				if (LegendEntryTimeSeries.this.getTimeSeries().hasData()) {
					LegendEntryTimeSeries.this.noDataSign.hide();
					LegendEntryTimeSeries.this.legendEntryHead
							.setStyleName("sensorweb_client_legendEntryHeader");
				} else {
					LegendEntryTimeSeries.this.noDataSign.show();
					LegendEntryTimeSeries.this.legendEntryHead
							.setStyleName("sensorweb_client_legendEntryHeaderNoData");
				}
			}
		}

		public void onHasData(TimeSeriesHasDataEvent evt) {

			if (LegendEntryTimeSeries.this.getTimeSeries() != null
					&& evt.getTSID().equals(
							LegendEntryTimeSeries.this.getTimeSeries().getId())) {
				if (LegendEntryTimeSeries.this.isSelected) {
					if (evt.hasData()) {
						LegendEntryTimeSeries.this.noDataSign.hide();
						LegendEntryTimeSeries.this.legendEntryHead
								.setStyleName("sensorweb_client_legendEntryHeaderSelected");
					} else {
						LegendEntryTimeSeries.this.noDataSign.show();
						LegendEntryTimeSeries.this.legendEntryHead
								.setStyleName("sensorweb_client_legendEntryHeaderSelectedNoData");
						Toaster.getInstance().addMessage(
								I18N.sosClient.noDataAvailable()
										+ LegendEntryTimeSeries.this
												.getTimeSeries().getProcedureId());
					}
				} else {
					if (evt.hasData()) {
						LegendEntryTimeSeries.this.noDataSign.hide();
						LegendEntryTimeSeries.this.legendEntryHead
								.setStyleName("sensorweb_client_legendEntryHeader");
					} else {
						LegendEntryTimeSeries.this.noDataSign.show();
						LegendEntryTimeSeries.this.legendEntryHead
								.setStyleName("sensorweb_client_legendEntryHeaderNoData");
						Toaster.getInstance().addMessage(
								I18N.sosClient.noDataAvailable()
										+ LegendEntryTimeSeries.this
												.getTimeSeries().getProcedureId());
					}
				}
			}
		}

		public void onStore(FirstValueOfTimeSeriesEvent evt) {
			if (evt.getTsID().equals(LegendEntryTimeSeries.this.timeseriesID)) {
				setFirstValueInterval();
			}
		}

		public void onStore(StoreTimeSeriesLastValueEvent evt) {
			if (evt.getTsID().equals(LegendEntryTimeSeries.this.timeseriesID)) {
				setLastValueInterval();
			}
		}

		public void onUpdateScale(UpdateScaleEvent evt) {
			if (LegendEntryTimeSeries.this.getTimeSeries().getPhenomenonId()
					.equals(evt.getPhenomenonID())) {
				if (evt.isAutoScale()) {
					LegendEntryTimeSeries.this.getTimeSeries().setAutoScale(
							evt.isAutoScale());
					LegendEntryTimeSeries.this.scale.setValue(I18N.sosClient
							.autoScale());
				}
				if (evt.isScaleToNull()) {
					LegendEntryTimeSeries.this.getTimeSeries().setScaleToZero(
							evt.isScaleToNull());
					LegendEntryTimeSeries.this.scale.setValue(I18N.sosClient
							.zeroScale());
				}
			}
		}

		@Override
		public void onSwitch(SwitchAutoscaleEvent evt) {
			LegendEntryTimeSeries.this.getTimeSeries().setAutoScale(evt.getSwitch());
			LegendEntryTimeSeries.this.scale.setValue(I18N.sosClient.autoScale());
			LegendEntryTimeSeries.this.getTimeSeries().setScaleToZero(false);
		}
	}

}