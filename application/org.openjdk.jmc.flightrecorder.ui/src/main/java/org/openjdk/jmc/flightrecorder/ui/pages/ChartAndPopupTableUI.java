/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.flightrecorder.ui.pages;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.openjdk.jmc.common.IState;
import org.openjdk.jmc.common.IWritableState;
import org.openjdk.jmc.common.item.IAttribute;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemFilter;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.ui.IPageContainer;
import org.openjdk.jmc.flightrecorder.ui.IPageUI;
import org.openjdk.jmc.flightrecorder.ui.StreamModel;
import org.openjdk.jmc.flightrecorder.ui.common.ChartDisplayControlBar;
import org.openjdk.jmc.flightrecorder.ui.common.ChartFilterControlBar;
import org.openjdk.jmc.flightrecorder.ui.common.DataPageToolkit;
import org.openjdk.jmc.flightrecorder.ui.common.FilterComponent;
import org.openjdk.jmc.flightrecorder.ui.common.FlavorSelector;
import org.openjdk.jmc.flightrecorder.ui.common.FlavorSelector.FlavorSelectorState;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram;
import org.openjdk.jmc.flightrecorder.ui.common.ScrolledCompositeToolkit;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram.HistogramSelection;
import org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages;
import org.openjdk.jmc.flightrecorder.ui.selection.SelectionStoreActionToolkit;
import org.openjdk.jmc.ui.charts.IXDataRenderer;
import org.openjdk.jmc.ui.charts.RendererToolkit;
import org.openjdk.jmc.ui.charts.TimelineCanvas;
import org.openjdk.jmc.ui.charts.XYChart;
import org.openjdk.jmc.ui.column.ColumnMenusFactory;
import org.openjdk.jmc.ui.common.PatternFly.Palette;
import org.openjdk.jmc.ui.handlers.ActionToolkit;
import org.openjdk.jmc.ui.handlers.MCContextMenuManager;
import org.openjdk.jmc.ui.misc.ActionUiToolkit;
import org.openjdk.jmc.ui.misc.ChartCanvas;
import org.openjdk.jmc.ui.misc.PersistableSashForm;

abstract class ChartAndPopupTableUI implements IPageUI {

	private static final String SASH = "sash"; //$NON-NLS-1$
	private static final String TABLE = "table"; //$NON-NLS-1$
	private static final String CHART = "chart"; //$NON-NLS-1$
	private static final String SELECTED = "selected"; //$NON-NLS-1$
	private final IItemFilter pageFilter;
	protected final StreamModel model;
	protected CheckboxTableViewer chartLegend;
	protected final Form form;
	protected final Composite chartContainer;
	protected final ChartCanvas chartCanvas;
	protected FilterComponent tableFilterComponent;
	protected ItemHistogram table;
	protected ItemHistogram hiddenTable;
	protected final SashForm sash;
	protected final IPageContainer pageContainer;
	protected List<IAction> allChartSeriesActions;
	private IItemCollection selectionItems;
	private IRange<IQuantity> timeRange;
	protected XYChart chart;
	protected FlavorSelector flavorSelector;
	private Composite hiddenTableContainer;

	protected Composite controls;
	public ChartFilterControlBar ctf;

	ChartAndPopupTableUI(IItemFilter pageFilter, StreamModel model, Composite parent, FormToolkit toolkit,
			IPageContainer pageContainer, IState state, String sectionTitle, IItemFilter tableFilter, Image icon,
			FlavorSelectorState flavorSelectorState) {
		this.pageFilter = pageFilter;
		this.model = model;
		this.pageContainer = pageContainer;
		form = DataPageToolkit.createForm(parent, toolkit, sectionTitle, icon);
		sash = new SashForm(form.getBody(), SWT.VERTICAL);
		toolkit.adapt(sash);

		hiddenTableContainer = new Composite(sash, SWT.NONE);
		hiddenTableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hiddenTableContainer.setVisible(false);

		hiddenTable = buildHistogram(hiddenTableContainer,state.getChild(TABLE));
		hiddenTable.getManager().getViewer().addSelectionChangedListener(e -> buildChart());

		tableFilterComponent = FilterComponent.createFilterComponent(hiddenTable.getManager().getViewer().getControl(),
				hiddenTable.getManager(), tableFilter, model.getItems().apply(pageFilter),
				pageContainer.getSelectionStore()::getSelections, this::onFilterChange);
		
		chartContainer = toolkit.createComposite(sash);
		chartContainer.setLayout(new GridLayout(1, false));
		chartContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create a container to hold the filter toolbar (2 layouts)
		controls = new Composite(chartContainer, SWT.NO_BACKGROUND);
		controls.setLayout(new GridLayout(1, false));
		controls.setBackground(Palette.PF_BLACK_300.getSWTColor());
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ctf = new ChartFilterControlBar(controls);

		// Create a container to hold the Chart + Display Toolbar
		Composite graphContainer = toolkit.createComposite(chartContainer);
		graphContainer.setLayout(new GridLayout(2, false));
		graphContainer.setBackground(Palette.PF_LIGHT_BLUE_100.getSWTColor());
		graphContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		Composite chartAndTimelineContainer = toolkit.createComposite(graphContainer);
		chartAndTimelineContainer.setLayout(new GridLayout(1, false));
		chartAndTimelineContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ScrolledCompositeToolkit sct = new ScrolledCompositeToolkit(Display.getCurrent());
		ScrolledComposite sc = sct.createScrolledComposite(chartAndTimelineContainer);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		chartCanvas = new ChartCanvas(sc);
		chartCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setContent(chartCanvas);
		sc.setAlwaysShowScrollBars(true);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		ChartDisplayControlBar cdcb = new ChartDisplayControlBar(graphContainer);

//		timelineContainer = toolkit.createComposite(chartAndTimelineContainer);
//		timelineContainer.setLayout(new GridLayout(1, false));
//		timelineContainer.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		chartAndTimelineContainer.setBackground(Palette.PF_PURPLE_100.getSWTColor());
		TimelineCanvas timelineCanvas = new TimelineCanvas(chartAndTimelineContainer);
		timelineCanvas.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		allChartSeriesActions = initializeChartConfiguration(state);
		IState chartState = state.getChild(CHART);
		ActionToolkit.loadCheckState(chartState, allChartSeriesActions.stream());
		chartLegend = ActionUiToolkit.buildCheckboxViewer(chartContainer, allChartSeriesActions.stream());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd.widthHint = 180;
		chartLegend.getControl().setLayoutData(gd);
		PersistableSashForm.loadState(sash, state.getChild(SASH));
		DataPageToolkit.createChartTimestampTooltip(chartCanvas);

		// Going to need to create a new container for the chart + timeline view and add it to the pageContainer.
		chart = new XYChart(pageContainer.getRecordingRange(), RendererToolkit.empty(), 180, timelineCanvas);
		DataPageToolkit.setChart(chartCanvas, chart, pageContainer::showSelection);
		SelectionStoreActionToolkit.addSelectionStoreRangeActions(pageContainer.getSelectionStore(), chart,
				JfrAttributes.LIFETIME, NLS.bind(Messages.ChartAndTableUI_TIMELINE_SELECTION, form.getText()),
				chartCanvas.getContextMenu());
		buildChart();

		// Temp: Pass the chart into the toolbars for information retrieval
		cdcb.setCanvas(chartCanvas);
		cdcb.setChart(chart);
		ctf.setChart(chart);

		if (chartState != null) {
			final String legendSelection = chartState.getAttribute(SELECTED);

			if (legendSelection != null) {
				allChartSeriesActions.stream().filter(ia -> legendSelection.equals(ia.getId())).findFirst()
						.ifPresent(a -> chartLegend.setSelection(new StructuredSelection(a)));
			}
		}

		flavorSelector = FlavorSelector.itemsWithTimerange(form, pageFilter, model.getItems(), pageContainer,
				this::onFlavorSelected, this::onSetRange, flavorSelectorState);
	}

	protected void onFilterChange(IItemFilter filter) {
		IItemCollection items = getItems();
		if (tableFilterComponent.isVisible()) {
			table.show(items.apply(filter));
			tableFilterComponent.setColor(table.getAllRows().getRowCount());
		} else if (table != null){
			table.show(items);
		}
	}

	@Override
	public void saveTo(IWritableState writableState) {
		PersistableSashForm.saveState(sash, writableState.createChild(SASH));
		getUndisposedTable().getManager().getSettings().saveState(writableState.createChild(TABLE));
		IWritableState chartState = writableState.createChild(CHART);

		ActionToolkit.saveCheckState(chartState, allChartSeriesActions.stream());
		Object legendSelection = ((IStructuredSelection) chartLegend.getSelection()).getFirstElement();
		if (legendSelection != null) {
			chartState.putString(SELECTED, ((IAction) legendSelection).getId());
		}
	}

	private void onSetRange(Boolean useRange) {
		IRange<IQuantity> range = useRange ? timeRange : pageContainer.getRecordingRange();
		chart.setVisibleRange(range.getStart(), range.getEnd());
		buildChart();
	}

	private void onFlavorSelected(IItemCollection items, IRange<IQuantity> timeRange) {
		this.selectionItems = items;
		this.timeRange = timeRange;
		hiddenTable.show(getItems());
		if (selectionItems != null) {
			Object[] tableInput = (Object[]) hiddenTable.getManager().getViewer().getInput();
			if (tableInput != null) {
				hiddenTable.getManager().getViewer().setSelection(new StructuredSelection(tableInput));
			} else {
				hiddenTable.getManager().getViewer().setSelection(null);
			}
		}
	}

	protected ItemHistogram getUndisposedTable() {
		return isDisposed(table) ? hiddenTable : table;
	}

	private boolean isDisposed(ItemHistogram histogram) {
		return histogram == null ? true : histogram.getManager().getViewer().getControl().isDisposed();
	}

	protected void buildChart() {
		IXDataRenderer rendererRoot = getChartRenderer(getItems(), getUndisposedTable().getSelection());
		chartCanvas.replaceRenderer(rendererRoot);
	}

	private IItemCollection getItems() {
		return selectionItems != null ? selectionItems.apply(pageFilter) : model.getItems().apply(pageFilter);
	}

	protected boolean isAttributeEnabled(IAttribute<IQuantity> attr) {
		Optional<IAction> action = allChartSeriesActions.stream().filter(a -> attr.getIdentifier().equals(a.getId()))
				.findAny();
		return action.isPresent() && action.get().isChecked();
	}

	protected abstract ItemHistogram buildHistogram(Composite parent, IState state);

	protected abstract IXDataRenderer getChartRenderer(IItemCollection itemsInTable, HistogramSelection selection);

	protected abstract List<IAction> initializeChartConfiguration(IState state);

	}
