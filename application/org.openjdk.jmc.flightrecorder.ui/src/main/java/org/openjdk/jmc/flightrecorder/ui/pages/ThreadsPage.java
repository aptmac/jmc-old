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

import static org.openjdk.jmc.common.item.Aggregators.max;
import static org.openjdk.jmc.common.item.Aggregators.min;

//import java.awt.Button;
import java.awt.Color;
//import java.awt.MenuItem;
//import java.awt.Menu;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.common.IMCThread;
import org.openjdk.jmc.common.IState;
import org.openjdk.jmc.common.IWritableState;
import org.openjdk.jmc.common.item.Aggregators;
import org.openjdk.jmc.common.item.IAggregator;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemFilter;
import org.openjdk.jmc.common.item.ItemFilters;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import org.openjdk.jmc.flightrecorder.rules.util.JfrRuleTopics;
import org.openjdk.jmc.flightrecorder.ui.EventTypeFolderNode;
import org.openjdk.jmc.flightrecorder.ui.EventTypeFolderNode.EventTypeNode;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.flightrecorder.ui.IDataPageFactory;
import org.openjdk.jmc.flightrecorder.ui.IDisplayablePage;
import org.openjdk.jmc.flightrecorder.ui.IPageContainer;
import org.openjdk.jmc.flightrecorder.ui.IPageDefinition;
import org.openjdk.jmc.flightrecorder.ui.IPageUI;
import org.openjdk.jmc.flightrecorder.ui.StreamModel;
import org.openjdk.jmc.flightrecorder.ui.common.AbstractDataPage;
import org.openjdk.jmc.flightrecorder.ui.common.CheckedComboBoxItem;
//import org.openjdk.jmc.flightrecorder.ui.common.CheckedComboBox;
import org.openjdk.jmc.flightrecorder.ui.common.FilterComponent;
import org.openjdk.jmc.flightrecorder.ui.common.FlavorSelector.FlavorSelectorState;
import org.openjdk.jmc.flightrecorder.ui.common.ImageConstants;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram.HistogramSelection;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram.ItemHistogramBuilder;
import org.openjdk.jmc.flightrecorder.ui.common.ItemRow;
import org.openjdk.jmc.flightrecorder.ui.common.ThreadGraphLanes;
import org.openjdk.jmc.flightrecorder.ui.common.TypeLabelProvider;
import org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages;
import org.openjdk.jmc.flightrecorder.ui.selection.SelectionStoreActionToolkit;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.charts.IXDataRenderer;
import org.openjdk.jmc.ui.charts.QuantitySpanRenderer;
import org.openjdk.jmc.ui.charts.RendererToolkit;
import org.openjdk.jmc.ui.column.ColumnManager.SelectionState;
import org.openjdk.jmc.ui.column.ColumnMenusFactory;
import org.openjdk.jmc.ui.column.TableSettings;
import org.openjdk.jmc.ui.handlers.ActionToolkit;
import org.openjdk.jmc.ui.handlers.MCContextMenuManager;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;

public class ThreadsPage extends AbstractDataPage {

	public static class ThreadsPageFactory implements IDataPageFactory {

		@Override
		public String getName(IState state) {
			return Messages.ThreadsPage_NAME;
		}

		@Override
		public String[] getTopics(IState state) {
			return new String[] {JfrRuleTopics.THREADS_TOPIC};
		}

		@Override
		public ImageDescriptor getImageDescriptor(IState state) {
			return FlightRecorderUI.getDefault().getMCImageDescriptor(ImageConstants.PAGE_THREADS);
		}

		@Override
		public IDisplayablePage createPage(IPageDefinition definition, StreamModel items, IPageContainer editor) {
			return new ThreadsPage(definition, items, editor);
		}

	}

	private static final String THREAD_START_COL = "threadStart"; //$NON-NLS-1$
	private static final String THREAD_END_COL = "threadEnd"; //$NON-NLS-1$
	private static final String THREAD_DURATION_COL = "threadDuration"; //$NON-NLS-1$
	private static final String THREAD_LANE = "threadLane"; //$NON-NLS-1$

	private static final IItemFilter pageFilter = ItemFilters.hasAttribute(JfrAttributes.EVENT_THREAD);
	private static final ItemHistogramBuilder HISTOGRAM = new ItemHistogramBuilder();

	static {
		HISTOGRAM.addColumn(JdkAttributes.EVENT_THREAD_GROUP_NAME);
		HISTOGRAM.addColumn(JdkAttributes.EVENT_THREAD_ID);
		HISTOGRAM.addColumn(THREAD_START_COL,
				min(Messages.JavaApplicationPage_COLUMN_THREAD_START,
						Messages.JavaApplicationPage_COLUMN_THREAD_START_DESC, JdkTypeIDs.JAVA_THREAD_START,
						JfrAttributes.EVENT_TIMESTAMP));
		/*
		 * Will order empty cells before first end time.
		 * 
		 * It should be noted that no event (empty column cell) is considered less than all values
		 * (this is common for all columns), which causes the column to sort threads without end
		 * time (indicating that the thread ended after the end of the recording) is ordered before
		 * the thread that ended first. While this is not optimal, we decided to accept it as it's
		 * not obviously better to have this particular column ordering empty cells last in contrast
		 * to all other columns.
		 */
		HISTOGRAM.addColumn(THREAD_END_COL,
				max(Messages.JavaApplicationPage_COLUMN_THREAD_END, Messages.JavaApplicationPage_COLUMN_THREAD_END_DESC,
						JdkTypeIDs.JAVA_THREAD_END, JfrAttributes.EVENT_TIMESTAMP));
		HISTOGRAM.addColumn(THREAD_DURATION_COL, ic -> {
			IQuantity threadStart = ic.apply(ItemFilters.type(JdkTypeIDs.JAVA_THREAD_START))
					.getAggregate((IAggregator<IQuantity, ?>) Aggregators.min(JfrAttributes.EVENT_TIMESTAMP));
			IQuantity threadEnd = ic.apply(ItemFilters.type(JdkTypeIDs.JAVA_THREAD_END))
					.getAggregate((IAggregator<IQuantity, ?>) Aggregators.max(JfrAttributes.EVENT_TIMESTAMP));
			if (threadStart != null && threadEnd != null) {
				return threadEnd.subtract(threadStart);
			}
			return null;
		}, Messages.JavaApplicationPage_COLUMN_THREAD_DURATION,
				Messages.JavaApplicationPage_COLUMN_THREAD_DURATION_DESC);
	}

	private class ThreadsPageUi extends ChartAndPopupTableUI {
		private static final String THREADS_TABLE_FILTER = "threadsTableFilter"; //$NON-NLS-1$
		private static final String HIDE_THREAD = "hideThread"; //$NON-NLS-1$
		private static final String RESET_CHART = "resetChart"; //$NON-NLS-1$
		private static final String TABLE = "table"; //$NON-NLS-1$
		private Boolean isChartMenuActionsInit;
		private Boolean isChartModified;
		private Boolean isToolbarAction = false;
		private Boolean reloadThreads;
		private IAction hideThreadAction;
		private IAction resetChartAction;
		private List<IXDataRenderer> threadRows;
		private MCContextMenuManager mm;
		private ThreadGraphLanes lanes;

		ThreadsPageUi(Composite parent, FormToolkit toolkit, IPageContainer editor, IState state) {
			super(pageFilter, getDataSource(), parent, toolkit, editor, state, getName(), pageFilter, getIcon(),
					flavorSelectorState);
			mm = (MCContextMenuManager) chartCanvas.getContextMenu();
			sash.setOrientation(SWT.HORIZONTAL);
			addActionsToContextMenu(mm);
			// FIXME: The lanes field is initialized by initializeChartConfiguration which is called by the super constructor. This is too indirect for SpotBugs to resolve and should be simplified.
			lanes.updateContextMenu(mm, false);
			form.getToolBarManager()
					.add(ActionToolkit.action(() -> lanes.openEditLanesDialog(mm, false), Messages.ThreadsPage_EDIT_LANES,
							FlightRecorderUI.getDefault().getMCImageDescriptor(ImageConstants.ICON_LANES_EDIT)));
			form.getToolBarManager()
			.add(ActionToolkit.action(() -> openViewThreadDetailsDialog(state), Messages.ThreadsPage_VIEW_THREAD_DETAILS,
					FlightRecorderUI.getDefault().getMCImageDescriptor(ImageConstants.ICON_TABLE)));
			form.getToolBarManager().update(true);
			chartLegend.getControl().dispose();
			buildChart();
			chart.setVisibleRange(visibleRange.getStart(), visibleRange.getEnd());
			onFilterChange(tableFilter);

			Button button = new Button(ctf, SWT.PUSH | SWT.ARROW);
			button.setText("Thread State Selection");
			button.setLayoutData(new RowData(SWT.DEFAULT, 20));
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					lanes.openEditLanesDialog(mm, false);
				}
			});
			controls.pack();
			
//			Composite embed = new Composite(controls, SWT.EMBEDDED);
//			embed.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//			Frame frame = SWT_AWT.new_Frame(embed);
//			List<CheckedComboBoxItem> list = getActivityLanes(); 
//			CheckedComboBox<CheckedComboBoxItem> ccb = new CheckedComboBox<CheckedComboBoxItem>(list.toArray(new CheckedComboBoxItem[list.size()]));
//			frame.add(ccb);
//			controls.pack();
		}

		private List<CheckedComboBoxItem> getActivityLanes() {
			List<CheckedComboBoxItem> list = new ArrayList<>();
			Set<String> enabled = lanes.getEnabledLanes();
			lanes.getTypeTree().getChildren();
			for (Object child : lanes.getTypeTree().getChildren()) {
				searchFolder(child, list, enabled);
			}
			return list;
		}

		private void searchFolder(Object node, List<CheckedComboBoxItem> list, Set<String> enabled) {
			if (node instanceof EventTypeFolderNode) {
				for (Object child : ((EventTypeFolderNode) node).getChildren()) {
					searchFolder(child, list, enabled);
				}
			} else if (node instanceof EventTypeNode) {
				EventTypeNode n = ((EventTypeNode) node);
				int count = n.getCount().numberValue().intValue();
				String typeId = n.getType().getIdentifier() + " (" + Integer.toString(count) + ")" ;
				Color color = TypeLabelProvider.getColorOrDefault(n.getType().getIdentifier().toString());
				Boolean checked = enabled.contains(n.getType().getIdentifier().toString());
				list.add(new CheckedComboBoxItem(typeId, color, checked));
			}
		}

		/**
		 * Hides a thread from the chart and rebuilds the chart
		 */
		private void hideThread(Object thread) {
			if (this.threadRows != null && this.threadRows.size() > 0 && thread instanceof IMCThread) {
				int index = indexOfThread(thread);
				if (index != -1) {
					this.threadRows.remove(index);
					this.reloadThreads = false;
					buildChart();
					if (!this.isChartModified) {
						this.isChartModified = true;
						setResetChartActionEnablement(true);
					}
				}
				if (this.threadRows.size() == 0) {
					setHideThreadActionEnablement(false);
				}
			}
		}

		/**
		 * Locates the index of the target Thread in the current selection list
		 *
		 * @param thread
		 *            the thread of interest
		 * @return the index of the thread in the current selection, or -1 if not found
		 */
		private int indexOfThread(Object thread) {
			for (int i = 0; i < this.threadRows.size() && thread != null; i++) {
				if (this.threadRows.get(i) instanceof QuantitySpanRenderer) {
					if (thread.equals(((QuantitySpanRenderer) this.threadRows.get(i)).getData())) {
						return i;
					}
				}
			}
			return -1;
		}

		/**
		 * Update the context menu to include actions to hide threads and reset the chart
		 */
		private void addActionsToContextMenu(MCContextMenuManager mm) {
			mm.add(new Separator());

			IAction hideThreadAction = ActionToolkit.action(() -> this.hideThread(chartCanvas.getHoveredItemData()),
					Messages.ThreadsPage_HIDE_THREAD_ACTION,
					UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_DELETE));
			hideThreadAction.setId(HIDE_THREAD);
			this.hideThreadAction = hideThreadAction;
			mm.add(hideThreadAction);

			IAction resetChartAction = ActionToolkit.action(() -> this.resetChartToSelection(),
					Messages.ThreadsPage_RESET_CHART_TO_SELECTION_ACTION,
					UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_REFRESH));
			resetChartAction.setId(RESET_CHART);
			resetChartAction.setEnabled(this.isChartModified);
			this.resetChartAction = resetChartAction;
			mm.add(resetChartAction);

			this.isChartMenuActionsInit = true;
		}

		/**
		 * Redraws the chart, and disables the reset chart menu action
		 */
		private void resetChartToSelection() {
			buildChart();
			this.isChartModified = false;
			setResetChartActionEnablement(false);
			setHideThreadActionEnablement(true);
		}

		private void setHideThreadActionEnablement(Boolean enabled) {
			this.hideThreadAction.setEnabled(enabled);
		}
		private void setResetChartActionEnablement(Boolean enabled) {
			this.resetChartAction.setEnabled(enabled);
		}

		@Override
		protected ItemHistogram buildHistogram(Composite parent, IState state) {
			ItemHistogram build = HISTOGRAM.buildWithoutBorder(parent, JfrAttributes.EVENT_THREAD,
					TableSettings.forState(state));
			return build;
		}

		@Override
		protected IXDataRenderer getChartRenderer(IItemCollection itemsInTable, HistogramSelection tableSelection) {
			List<IXDataRenderer> rows = new ArrayList<>();
			ItemHistogram histogram = getUndisposedTable();
			IItemCollection selectedItems;
			HistogramSelection selection;
			if (tableSelection.getRowCount() == 0) {
				selectedItems = itemsInTable;
				selection = histogram.getAllRows();
			} else {
				selectedItems = tableSelection.getItems();
				selection = tableSelection;
			}
			boolean useDefaultSelection = rows.size() > 1;
			if (lanes.getLaneDefinitions().stream().anyMatch(a -> a.isEnabled()) && selection.getRowCount() > 0) {
				if (this.reloadThreads) {
					this.threadRows = selection
							.getSelectedRows((object, items) -> lanes.buildThreadRenderer(object, items))
							.collect(Collectors.toList());
					chartCanvas.setNumItems(this.threadRows.size());
					this.isChartModified = false;
					if (this.isChartMenuActionsInit) {
						setResetChartActionEnablement(false);
						setHideThreadActionEnablement(true);
					}
				} else {
					this.reloadThreads = true;
				}

				double threadsWeight = Math.sqrt(threadRows.size()) * 0.15;
				double otherRowWeight = Math.max(threadsWeight * 0.1, (1 - threadsWeight) / rows.size());
				List<Double> weights = Stream
						.concat(Stream.generate(() -> otherRowWeight).limit(rows.size()), Stream.of(threadsWeight))
						.collect(Collectors.toList());
				rows.add(RendererToolkit.uniformRows(this.threadRows));
				useDefaultSelection = true;
				rows = Arrays.asList(RendererToolkit.weightedRows(rows, weights));
			}
			IXDataRenderer root = rows.size() == 1 ? rows.get(0) : RendererToolkit.uniformRows(rows);
			// We don't use the default selection when there is only one row. This is to get the correct payload.
			return useDefaultSelection ? new ItemRow(root, selectedItems.apply(lanes.getEnabledLanesFilter())) : root;
		}

		@Override
		protected void onFilterChange(IItemFilter filter) {
			super.onFilterChange(filter);
			tableFilter = filter;
		}

		@Override
		public void saveTo(IWritableState state) {
			super.saveTo(state);
			saveToLocal();
		}

		private void saveToLocal() {
			flavorSelectorState = flavorSelector.getFlavorSelectorState();
			histogramSelectionState = getUndisposedTable().getManager().getSelectionState();
			visibleRange = chart.getVisibleRange();
		}

		@Override
		protected List<IAction> initializeChartConfiguration(IState state) {
			this.isChartMenuActionsInit = false;
			this.isChartModified = false;
			this.reloadThreads = true;
			lanes = new ThreadGraphLanes(() -> getDataSource(), () -> buildChart());
			return lanes.initializeChartConfiguration(Stream.of(state.getChildren(THREAD_LANE)));
		}

		public void openViewThreadDetailsDialog(IState state) {
			TablePopup tablePopup = new TablePopup(state);
			OnePageWizardDialog.open(tablePopup, 500, 600);
		}

		private class TablePopup extends WizardPage {

			private IState state;

			protected TablePopup(IState state) {
				super("ThreadDetailsPage"); //$NON-NLS-1$
				this.state = state;
				setTitle(Messages.ThreadsPage_TABLE_POPUP_TITLE);
				setDescription(Messages.ThreadsPage_TABLE_POPUP_DESCRIPTION);
			}

			@Override
			public void createControl(Composite parent) {

				table = buildHistogram(parent, state.getChild(TABLE));
				MCContextMenuManager mm = MCContextMenuManager.create(table.getManager().getViewer().getControl());
				ColumnMenusFactory.addDefaultMenus(table.getManager(), mm);
				table.getManager().getViewer().addSelectionChangedListener(e -> buildChart());
				table.getManager().getViewer()
						.addSelectionChangedListener(e -> pageContainer.showSelection(table.getSelection().getItems()));
				SelectionStoreActionToolkit.addSelectionStoreActions(pageContainer.getSelectionStore(), table,
						NLS.bind(Messages.ChartAndTableUI_HISTOGRAM_SELECTION, getName()), mm);
				tableFilterComponent = FilterComponent.createFilterComponent(table.getManager().getViewer().getControl(),
						table.getManager(), tableFilter, model.getItems().apply(pageFilter),
						pageContainer.getSelectionStore()::getSelections, this::onFilterChangeHelper);
				mm.add(tableFilterComponent.getShowFilterAction());
				mm.add(tableFilterComponent.getShowSearchAction());

				table.getManager().setSelectionState(histogramSelectionState);
				tableFilterComponent.loadState(state.getChild(THREADS_TABLE_FILTER));
				onFilterChange(tableFilter);

				setControl(parent);
			}

			private void onFilterChangeHelper(IItemFilter filter) {
				onFilterChange(filter);
			}
		}
	}

	private FlavorSelectorState flavorSelectorState;
	private SelectionState histogramSelectionState;
	private IItemFilter tableFilter;
	private IRange<IQuantity> visibleRange;

	public ThreadsPage(IPageDefinition definition, StreamModel model, IPageContainer editor) {
		super(definition, model, editor);
		visibleRange = editor.getRecordingRange();
		editor.getRecordingRange().getEnd();
		editor.getRecordingRange().getExtent();
//		editor.getRecordingRange().getStart().in(UnitLookup.MILLISECOND);
//		editor.getRecordingRange().getStart().
	}

	@Override
	public IPageUI display(Composite parent, FormToolkit toolkit, IPageContainer editor, IState state) {
		return new ThreadsPageUi(parent, toolkit, editor, state);
	}

}
