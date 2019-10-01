package org.openjdk.jmc.ui.charts;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.ui.misc.ChartCanvas;

public class ChartFilterControlBar extends Composite {

	private static final String THREADS_LABEL = "Threads";

	private TimeFilter timeFilter;

	public ChartFilterControlBar(Composite parent, Listener resetListener, IRange<IQuantity> recordingRange) {
		super(parent, SWT.NO_BACKGROUND);
		this.setLayout(new GridLayout(3, false));

		Label nameLabel = new Label(this, SWT.CENTER | SWT.HORIZONTAL);
		nameLabel.setText(THREADS_LABEL);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, true);
		gd.widthHint = 180;
		nameLabel.setLayoutData(gd);
		nameLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		timeFilter = new TimeFilter(this, recordingRange, resetListener);
		timeFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
	}

	public void setChart(XYChart chart) {
		timeFilter.setChart(chart);
	}

	public void setChartCanvas(ChartCanvas canvas) {
		timeFilter.setChartCanvas(canvas);
	}

	public void setStartTime(IQuantity startTime) {
		timeFilter.setStartTime(startTime);
	}

	public void setEndTime(IQuantity endTime) {
		timeFilter.setEndTime(endTime);
	}
}
