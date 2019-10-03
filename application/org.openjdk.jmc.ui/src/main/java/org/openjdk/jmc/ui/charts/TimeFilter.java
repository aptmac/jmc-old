package org.openjdk.jmc.ui.charts;

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
import org.openjdk.jmc.ui.misc.ChartCanvas;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class TimeFilter extends Composite {

	private static final String FILTER_EVENTS = "Filter Events";
	private static final String FROM = "From";
	private static final String TO = "to";
	private static final String FILTER = "Filter";
	private static final String RESET = "Reset";
	private static final String ERROR = "Time Filter Error";
	private static final String START_TIME_PRECEEDS_ERROR = "The selected start time preceeds the range of the recording.";
	private static final String END_TIME_EXCEEDS_ERROR = "The selected end time exceeds the range of the recording.";
	private static final String START_TIME_LONGER_THAN_END_ERROR = "The selected start time exceeds the specified end time.";
	private static final String INVALID_FORMAT_ERROR = "Invalid time format.";

	private ChartCanvas chartCanvas;
	private XYChart chart;
	IRange<IQuantity> recordingRange;
	private TimeDisplay startDisplay;
	private TimeDisplay endDisplay;

	public TimeFilter(Composite parent, IRange<IQuantity> recordingRange, Listener resetListener) {
		super(parent, SWT.NO_BACKGROUND);
		this.setLayout(new GridLayout(7, false));
		Label eventsLabel = new Label(this, SWT.LEFT);
		eventsLabel.setText(FILTER_EVENTS);
		eventsLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		Label from = new Label(this, SWT.CENTER);
		from.setText(FROM);

		startDisplay = new TimeDisplay(this);

		Label to = new Label(this, SWT.CENTER);
		to.setText(TO);

		endDisplay = new TimeDisplay(this);

		Button filterBtn = new Button(this, SWT.PUSH);
		filterBtn.setText(FILTER);
		filterBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (startDisplay.isFormatValid() && endDisplay.isFormatValid()) {
					Long startDisplayEpoch = startDisplay.getDisplayTime().in(UnitLookup.EPOCH_MS).longValue();
					Long endDisplayEpoch = endDisplay.getDisplayTime().in(UnitLookup.EPOCH_MS).longValue();
					Long endEpoch = recordingRange.getEnd().in(UnitLookup.EPOCH_MS).longValue();
					Long startEpoch = recordingRange.getStart().in(UnitLookup.EPOCH_MS).longValue();
					if (startDisplayEpoch < startEpoch) {
						DialogToolkit.showWarning(Display.getCurrent().getActiveShell(), ERROR, START_TIME_PRECEEDS_ERROR);
					} else if (endDisplayEpoch > endEpoch) {
						DialogToolkit.showWarning(Display.getCurrent().getActiveShell(), ERROR, END_TIME_EXCEEDS_ERROR);
					} else if (startDisplayEpoch > endDisplayEpoch) {
						DialogToolkit.showWarning(Display.getCurrent().getActiveShell(), ERROR, START_TIME_LONGER_THAN_END_ERROR);
					} else {
						chart.setVisibleRange(startDisplay.getDisplayTime(), endDisplay.getDisplayTime());
						chartCanvas.redrawChart();
					}
				} else {
					DialogToolkit.showWarning(Display.getCurrent().getActiveShell(), ERROR, INVALID_FORMAT_ERROR);
				}
			}
		});

		Button resetBtn = new Button(this, SWT.PUSH);
		resetBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		resetBtn.setText(RESET);
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
