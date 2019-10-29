/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Red Hat Inc. All rights reserved.
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
package org.openjdk.jmc.flightrecorder.uitest;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openjdk.jmc.test.jemmy.MCJemmyTestBase;
import org.openjdk.jmc.test.jemmy.MCUITestRule;
import org.openjdk.jmc.test.jemmy.misc.wrappers.MCChartCanvas;
import org.openjdk.jmc.test.jemmy.misc.wrappers.JfrNavigator;
import org.openjdk.jmc.test.jemmy.misc.wrappers.JfrUi;
import org.openjdk.jmc.test.jemmy.misc.wrappers.MCButton;
import org.openjdk.jmc.test.jemmy.misc.wrappers.MCMenu;
import org.openjdk.jmc.test.jemmy.misc.wrappers.MCTable;
import org.openjdk.jmc.test.jemmy.misc.wrappers.MCText;
import org.openjdk.jmc.test.jemmy.misc.wrappers.MCToolBar;
import org.openjdk.jmc.ui.UIPlugin;

public class JfrThreadsPageTest extends MCJemmyTestBase {

	private static final String PLAIN_JFR = "plain_recording.jfr";
	private static final String TABLE_COLUMN_HEADER = "Thread";
	private static final String OK_BUTTON = "OK";
	private static final String RESET_BUTTON = "Reset";
	private static final String FILTER_BUTTON = "Filter";
	private static final String START_TIME = "08:06:19:489";
	private static final String NEW_START_TIME = "08:06:19:500";
	private static final String DEFAULT_ZOOM = "100.00 %";
	private static final String HIDE_THREAD = org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages.ThreadsPage_HIDE_THREAD_ACTION;
	private static final String RESET_CHART = org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages.ThreadsPage_RESET_CHART_TO_SELECTION_ACTION;
	private static final String TABLE_TOOLTIP = org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages.ThreadsPage_VIEW_THREAD_DETAILS;
	private static final String TABLE_SHELL_TEXT = org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages.ThreadsPage_TABLE_POPUP_TITLE;

	private static MCChartCanvas chartCanvas;
	private static MCTable threadsTable;

	@Rule
	public MCUITestRule testRule = new MCUITestRule(verboseRuleOutput) {
		@Override
		public void before() {
			JfrUi.openJfr(materialize("jfr", PLAIN_JFR, JfrThreadsPageTest.class));
			JfrNavigator.selectTab(JfrUi.Tabs.THREADS);
			chartCanvas = MCChartCanvas.getChartCanvas();
		}

		@Override
		public void after() {
			MCMenu.closeActiveEditor();
		}
	};

	@Test
	public void testZoom() {
		MCButton zoomInBtn = MCButton.getByImage(
				UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_IN));
		MCButton zoomOutBtn = MCButton.getByImage(
				UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_OUT));
		MCText zoomDisplay = MCText.getByText(DEFAULT_ZOOM);

		//zoom with display bar
		Assert.assertEquals(zoomDisplay.getText(), DEFAULT_ZOOM);
		zoomInBtn.click();
		chartCanvas.clickChart();
		Assert.assertNotEquals(zoomDisplay.getText(), DEFAULT_ZOOM);

		zoomOutBtn.click();
		chartCanvas.clickChart();
		Assert.assertEquals(zoomDisplay.getText(), DEFAULT_ZOOM);

		//zoom with controls
		chartCanvas.clickChart();
		chartCanvas.keyboardZoomIn();
		Assert.assertNotEquals(zoomDisplay.getText(), DEFAULT_ZOOM);

		chartCanvas.keyboardZoomOut();
		Assert.assertEquals(zoomDisplay.getText(), DEFAULT_ZOOM);
	}

	@Test
	public void testResetButtons() {
		MCText StartTimeField = MCText.getByText(START_TIME);
		MCText zoomDisplay = MCText.getByText(DEFAULT_ZOOM);
		MCButton filterBtn = MCButton.getByLabel(FILTER_BUTTON);
		MCButton resetBtn = MCButton.getByLabel(RESET_BUTTON);
		MCButton scaleToFitBtn = MCButton.getByImage(
				UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_SCALE_TO_FIT));

		StartTimeField.setText(NEW_START_TIME);
		filterBtn.click();
		Assert.assertNotEquals(START_TIME, StartTimeField.getText());
		Assert.assertNotEquals(zoomDisplay.getText(), DEFAULT_ZOOM);

		resetBtn.click();
		Assert.assertEquals(START_TIME, StartTimeField.getText());
		Assert.assertEquals(zoomDisplay.getText(), DEFAULT_ZOOM);

		StartTimeField.setText(NEW_START_TIME);
		filterBtn.click();
		Assert.assertNotEquals(START_TIME, StartTimeField.getText());
		Assert.assertNotEquals(zoomDisplay.getText(), DEFAULT_ZOOM);

		scaleToFitBtn.click();
		Assert.assertEquals(zoomDisplay.getText(), DEFAULT_ZOOM);
		Assert.assertEquals(START_TIME, StartTimeField.getText());
	}

	@Test
	public void testMenuItemEnablement() {
		openThreadsTable();
		final int numThreads = threadsTable.getItemCount();
		closeThreadsTable();

		Assert.assertTrue(numThreads > 0);

		Assert.assertFalse(chartCanvas.isContextMenuItemEnabled(RESET_CHART));
		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(HIDE_THREAD));

		chartCanvas.clickContextMenuItem(HIDE_THREAD);

		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(RESET_CHART));
		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(HIDE_THREAD));

		chartCanvas.clickContextMenuItem(RESET_CHART);

		Assert.assertFalse(chartCanvas.isContextMenuItemEnabled(RESET_CHART));
		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(HIDE_THREAD));
	}

	@Test
	public void testHideAllThreads() {
		final int numSelection = 7;

		openThreadsTable();
		final int numThreads = threadsTable.getItemCount();
		closeThreadsTable();

		Assert.assertTrue(numThreads > 0 && numThreads >= numSelection);
		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(HIDE_THREAD));
		Assert.assertFalse(chartCanvas.isContextMenuItemEnabled(RESET_CHART));

		openThreadsTable();
		// Select a limited number of threads in the chart using the table
		threadsTable.selectItems(0, numSelection - 1);
		closeThreadsTable();

		// Hide all the threads from the chart
		for (int i = 0; i < numSelection; i++) {
			chartCanvas.clickContextMenuItem(HIDE_THREAD);
		}

		// Once all threads are hidden from the chart, the hide thread menu item will be disabled
		Assert.assertFalse(chartCanvas.isContextMenuItemEnabled(HIDE_THREAD));
		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(RESET_CHART));

		chartCanvas.clickContextMenuItem(RESET_CHART);

		// Verify the menu item isEnabled values are back to their default values
		Assert.assertTrue(chartCanvas.isContextMenuItemEnabled(HIDE_THREAD));
		Assert.assertFalse(chartCanvas.isContextMenuItemEnabled(RESET_CHART));
	}

	@Test
	public void testPopupTableSelection() {
		openThreadsTable();
		final int numSelection = 7;
		final int numThreads = threadsTable.getItemCount();
		Assert.assertTrue(numThreads > 0 && numThreads >= numSelection);

		threadsTable.selectItems(0, numSelection - 1);
		int originalSelection = threadsTable.getSelectionCount();
		Assert.assertEquals(numSelection, originalSelection);
		closeThreadsTable();

		openThreadsTable();
		int newSelection = threadsTable.getSelectionCount();
		Assert.assertEquals(newSelection, originalSelection);
		closeThreadsTable();
	}

	private void openThreadsTable() {
		if (threadsTable == null) {
			MCToolBar.focusMc();
			MCToolBar tb = MCToolBar.getByToolTip(TABLE_TOOLTIP);
			tb.clickToolItem(TABLE_TOOLTIP);
			threadsTable = MCTable.getByColumnHeader(TABLE_SHELL_TEXT, TABLE_COLUMN_HEADER);
		}
	}

	private void closeThreadsTable() {
		if (threadsTable != null) {
			MCButton okButton = MCButton.getByLabel(TABLE_SHELL_TEXT, OK_BUTTON);
			okButton.click();
			threadsTable = null;
			MCToolBar.focusMc();
		}
	}

}
