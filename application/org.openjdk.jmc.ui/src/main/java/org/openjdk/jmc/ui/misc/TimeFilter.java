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

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.ui.charts.XYChart;
import org.openjdk.jmc.ui.misc.PatternFly.Palette;

public class TimeFilter extends Composite {

	private ChartCanvas chartCanvas;
	private IRange<IQuantity> recordingRange;
	private XYChart chart;
	private TimeDisplay startDisplay;
	private TimeDisplay endDisplay;

	public TimeFilter(Composite parent, IRange<IQuantity> recordingRange, Listener resetListener) {
		super(parent, SWT.NONE);
		this.recordingRange = recordingRange;
		this.setBackground(Palette.PF_BLACK_300.getSWTColor());
		this.setLayout(new GridLayout(7, false));
		Label eventsLabel = new Label(this, SWT.LEFT);
		eventsLabel.setText(Messages.TimeFilter_FILTER_EVENTS);
		eventsLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		eventsLabel.setBackground(Palette.PF_BLACK_300.getSWTColor());

		Label fromLabel = new Label(this, SWT.CENTER);
		fromLabel.setText(Messages.TimeFilter_FROM);
		fromLabel.setBackground(Palette.PF_BLACK_300.getSWTColor());

		startDisplay = new TimeDisplay(this);

		Label toLabel = new Label(this, SWT.CENTER);
		toLabel.setText(Messages.TimeFilter_TO);
		toLabel.setBackground(Palette.PF_BLACK_300.getSWTColor());

		endDisplay = new TimeDisplay(this);

		Button resetBtn = new Button(this, SWT.PUSH);
		resetBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		resetBtn.setText(Messages.TimeFilter_RESET);
		resetBtn.addListener(SWT.Selection, resetListener);
	}

	protected synchronized void validateTimeRange() {
		if (startDisplay.isValidFormat() && endDisplay.isValidFormat()) {
			Long startDisplayEpoch = startDisplay.getDisplayedTime().in(UnitLookup.EPOCH_MS).longValue();
			Long endDisplayEpoch = endDisplay.getDisplayedTime().in(UnitLookup.EPOCH_MS).longValue();
			Long endEpoch = recordingRange.getEnd().in(UnitLookup.EPOCH_MS).longValue();
			Long startEpoch = recordingRange.getStart().in(UnitLookup.EPOCH_MS).longValue();
			if (startDisplayEpoch < startEpoch) {
				DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
						Messages.TimeFilter_ERROR, Messages.TimeFilter_START_TIME_PRECEEDS_ERROR);
				startDisplay.setTextForeground(Palette.PF_RED_100.getSWTColor());
			} else if (endDisplayEpoch > endEpoch) {
				DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
						Messages.TimeFilter_ERROR, Messages.TimeFilter_END_TIME_EXCEEDS_ERROR);
				endDisplay.setTextForeground(Palette.PF_RED_100.getSWTColor());
			} else if (startDisplayEpoch > endDisplayEpoch) {
				DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
						Messages.TimeFilter_ERROR, Messages.TimeFilter_START_TIME_LONGER_THAN_END_ERROR);
				startDisplay.setTextForeground(Palette.PF_RED_100.getSWTColor());
			} else {
				chart.setVisibleRange(startDisplay.getDisplayedTime(), endDisplay.getDisplayedTime());
				chartCanvas.redrawChart();
			}
		} else {
			DialogToolkit.showWarning(Display.getCurrent().getActiveShell(),
					Messages.TimeFilter_ERROR, Messages.TimeFilter_INVALID_FORMAT_ERROR);
		}
	}

	public void setChart(XYChart chart) {
		this.chart = chart;
	}

	public void setChartCanvas(ChartCanvas canvas) {
		this.chartCanvas = canvas;
	}
	
	public void setStartTime(IQuantity time) {
		startDisplay.setTime(time);
	}

	public void setEndTime(IQuantity time) {
		endDisplay.setTime(time);
	}

	private class TimeDisplay extends Composite {

		private static final String TIME_FORMAT_REGEX = "\\d{2}\\:\\d{2}\\:\\d{2}\\:\\d{3}";
		private static final String DIGIT_FORMAT_REGEX = "\\d{3}|\\d{2}";
		private final Pattern timePattern = Pattern.compile(TIME_FORMAT_REGEX);
		private final Pattern digitPattern = Pattern.compile(DIGIT_FORMAT_REGEX);
		private boolean bypassListener;
		private Calendar currentCalendar;
		private IQuantity currentTime; // current valid time
		private IQuantity displayedTime; // the time displayed in the Text, used for validation
		private StringBuilder sb;
		private Text timeText;
		private int lastEventTime;

		public TimeDisplay(TimeFilter parent) {
			super(parent, SWT.NONE);
			this.setBackground(Palette.PF_BLACK_300.getSWTColor());
			this.setLayout(new GridLayout());
			timeText = new Text(this, SWT.SEARCH | SWT.SINGLE);
			timeText.setTextLimit(12);
			timeText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (getBypassListener()) {
						setBypassListener(false);
						return;
					}

					/**
					 * If the user edits a Text by highlighting a character and
					 * overwrites it with a new one, the ModifyListener will fire twice.
					 *
					 * To prevent validation (and potential error dialogs) from occurring
					 * twice, compare the time of the current ModifyEvent to the last
					 * seen ModifyEvent.
					 */
					if (e.time == lastEventTime) {
						return;
					} else {
						lastEventTime = e.time;
					}
					displayedTime = null; // reset the value of displayedTime
					if (isValidFormat() && isValidTime()) {
						setTextForeground(Palette.PF_BLACK.getSWTColor());
						parent.validateTimeRange(); // figure out how to stop initial check
					} else {
						setTextForeground(Palette.PF_RED_100.getSWTColor());
					}
				}
			});
		}

		protected void setTextForeground(Color color) {
			timeText.setForeground(color);
		}

		// Convert epoch ms timestamp to Calendar object
		private Calendar convertEpochToCalendar(long epoch) {
			Calendar tempCalendar = Calendar.getInstance();
			tempCalendar.setTime(new Date(epoch));
			return tempCalendar;
		}

		private void setCurrentCalendar(long epoch) {
			currentCalendar = convertEpochToCalendar(epoch);
		}

		// Locally store the new time, and format it for displaying in the Text widget
		public synchronized void setTime(IQuantity time) {
			if (currentTime != null && time.longValue() == currentTime.longValue() &&
					displayedTime != null && currentTime.longValue() == displayedTime.longValue()) {
				return;
			}
			this.displayedTime = time;
			this.currentTime = time;
			setCurrentCalendar(time.in(UnitLookup.EPOCH_MS).longValue());
			displayTime(formatTimeString(currentCalendar));
			setTextForeground(Palette.PF_BLACK.getSWTColor());
		}

		// Returns the IQuantity time stamp of the time displayed in the widget
		public IQuantity getDisplayedTime() {
			if (displayedTime != null) {
				return displayedTime;
			}
			if (isValidFormat() && isValidTime()) {
				IQuantity time = currentTime;
				Matcher m = digitPattern.matcher(timeText.getText());
				int i = 0;
				while(m.find()) {
					int value = Integer.parseInt(m.group());
					switch(i) {
					case 0:
						value = value - currentCalendar.get(Calendar.HOUR);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.HOUR.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					case 1:
						value = value - currentCalendar.get(Calendar.MINUTE);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.MINUTE.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					case 2:
						value = value - currentCalendar.get(Calendar.SECOND);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.SECOND.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					case 3:
						value = value - currentCalendar.get(Calendar.MILLISECOND);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.MILLISECOND.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					}
					i++;
				}
				return time;
			}
			return null;
		}

		// Format the calendar time to a string HH:mm:ss:SSS
		private String formatTimeString(Calendar cal) {
			sb = new StringBuilder();
			sb.append(String.format("%02d", cal.get(Calendar.HOUR)));
			sb.append(":");
			sb.append(String.format("%02d", cal.get(Calendar.MINUTE)));
			sb.append(":");
			sb.append(String.format("%02d", cal.get(Calendar.SECOND)));
			sb.append(":");
			sb.append(String.format("%03d", cal.get(Calendar.MILLISECOND)));
			return sb.toString();
		}

		// displayTime() results in calling Text.setText(), use a boolean
		// to prevent unnecessarily activating the ModifyListener
		private synchronized boolean getBypassListener() {
			return bypassListener;
		}

		protected synchronized void setBypassListener(boolean bypassListener) {
			this.bypassListener = bypassListener;
		}

		private void displayTime(String time) {
			setBypassListener(true); // boolean flag to not activate the modify listener via setText()
			timeText.setText(time);
			setBypassListener(false);
		}

		/**
		 * Verify that the time string inside the text widget matches the
		 * expected time format of HH:mm:ss:SSS
		 * @return true if the text corresponds to a HH:mm:ss:SSS format
		 */
		private boolean isValidFormat() {
			if (!timePattern.matcher(timeText.getText()).matches()) {
				// not in HH:mm:ss:SSS format
				return false;
			}
			return true;
		}

		/**
		 * Verify that the string inside the text widget is a valid
		 * 24-hour clock time
		 * @return true if the text corresponds to a valid 24-hour time
		 */
		private boolean isValidTime() {
			Matcher m = digitPattern.matcher(timeText.getText());
			int i = 0;
			while(m.find()) {
				int value = Integer.parseInt(m.group());
				if (i == 0 && value >= 24) {
					return false;
				} else if ((i == 1 || i == 2) && value >= 60) {
					return false;
				}
				i++;
			}
			return true;
		}
	}
}
