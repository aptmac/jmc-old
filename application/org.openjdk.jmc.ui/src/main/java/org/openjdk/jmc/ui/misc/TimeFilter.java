/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.jmc.ui.misc;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.ui.charts.XYChart;

public class TimeFilter extends Composite {

	private ChartCanvas chartCanvas;
	private IRange<IQuantity> recordingRange;
	private XYChart chart;
	private TimeDisplay startDisplay;
	private TimeDisplay endDisplay;

	public TimeFilter(Composite parent, IRange<IQuantity> recordingRange, Listener resetListener) {
		super(parent, SWT.NO_BACKGROUND);
		this.recordingRange = recordingRange;
		this.setLayout(new GridLayout(7, false));
		Label eventsLabel = new Label(this, SWT.LEFT);
		eventsLabel.setText(Messages.TimeFilter_FILTER_EVENTS);
		eventsLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		Label from = new Label(this, SWT.CENTER);
		from.setText(Messages.TimeFilter_FROM);

		startDisplay = new TimeDisplay(this);

		Label to = new Label(this, SWT.CENTER);
		to.setText(Messages.TimeFilter_TO);

		endDisplay = new TimeDisplay(this);

		Button filterBtn = new Button(this, SWT.PUSH);
		filterBtn.setText(Messages.TimeFilter_FILTER);
		filterBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (startDisplay.isFormatValid() && endDisplay.isFormatValid()) {
					Long startDisplayEpoch = startDisplay.getDisplayTime().in(UnitLookup.EPOCH_MS).longValue();
					Long endDisplayEpoch = endDisplay.getDisplayTime().in(UnitLookup.EPOCH_MS).longValue();
					Long endEpoch = recordingRange.getEnd().in(UnitLookup.EPOCH_MS).longValue();
					Long startEpoch = recordingRange.getStart().in(UnitLookup.EPOCH_MS).longValue();
					if (startDisplayEpoch < startEpoch) {
						DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
								Messages.TimeFilter_ERROR, Messages.TimeFilter_START_TIME_PRECEEDS_ERROR);
					} else if (endDisplayEpoch > endEpoch) {
						DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
								Messages.TimeFilter_ERROR, Messages.TimeFilter_END_TIME_EXCEEDS_ERROR);
					} else if (startDisplayEpoch > endDisplayEpoch) {
						DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
								Messages.TimeFilter_ERROR, Messages.TimeFilter_START_TIME_LONGER_THAN_END_ERROR);
					} else {
						chart.setVisibleRange(startDisplay.getDisplayTime(), endDisplay.getDisplayTime());
						chartCanvas.redrawChart();
					}
				} else {
					DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
							Messages.TimeFilter_ERROR, Messages.TimeFilter_INVALID_FORMAT_ERROR);
				}
			}
		});

		Button resetBtn = new Button(this, SWT.PUSH);
		resetBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		resetBtn.setText(Messages.TimeFilter_RESET);
		resetBtn.addListener(SWT.Selection, resetListener);
	}

	public void setChart(XYChart chart) {
		this.chart = chart;
	}

	public void setChartCanvas(ChartCanvas canvas) {
		this.chartCanvas = canvas;
	}
	
	public boolean setStartTime(IQuantity time) {
		startDisplay.setTime(time);
		return true;
	}

	public void setEndTime(IQuantity time) {
		endDisplay.setTime(time);
	}
}
