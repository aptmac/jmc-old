package org.openjdk.jmc.ui.charts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.misc.ChartCanvas;

public class ChartDisplayControlBar extends Composite {

	Scale scale;
	Text text;
	XYChart chart;
	ChartCanvas canvas;
	int zoomValue = 0;

	public void setChart(XYChart chart) {
		this.chart = chart;
	}
	
	public void setCanvas(ChartCanvas canvas) {
		this.canvas = canvas;
	}
	
	public ChartDisplayControlBar(Composite parent) {
		super(parent, SWT.NO_BACKGROUND);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		this.setLayout(layout);

		Button selectionBtn = new Button(this, SWT.TOGGLE);
		selectionBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_SELECTION));
		selectionBtn.setSelection(true);

		// SPACE

		Button zoomInBtn = new Button(this, SWT.PUSH);
		zoomInBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_IN));
		zoomInBtn.addListener(SWT.Selection,  new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (scale.getSelection() > 0) {
					scale.setSelection(scale.getSelection() - scale.getIncrement());
					int value = scale.getMaximum() - scale.getSelection() + scale.getMinimum();
					text.setText(Integer.toString(value));
					zoomValue++;
					chart.zoom(1);
					canvas.redrawChart();
				}
			}
		});

		scale = new Scale(this, SWT.VERTICAL);
		scale.setMinimum(0);
		scale.setMaximum(50);
		scale.setIncrement(1);
		scale.setSelection(50);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int value = scale.getMaximum() - scale.getSelection() + scale.getMinimum();
				text.setText(Integer.toString(value));
				if (scale.getSelection() > scale.getMinimum() || scale.getSelection() < scale.getMaximum()) {
					if (zoomValue < value) {
						chart.zoom(1);
						zoomValue = value;
						canvas.redrawChart();
					} else if (zoomValue > value) {
						chart.zoom(-1);
						zoomValue = value;
						canvas.redrawChart();
					}
				}
			}
		});
		text = new Text(this, SWT.BORDER | SWT.SINGLE);
		text.setEditable(false);
		text.setText(Integer.toString(0));
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Button zoomOutBtn = new Button(this, SWT.PUSH);
		zoomOutBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_OUT));
		zoomOutBtn.addListener(SWT.Selection,  new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (scale.getSelection() < scale.getMaximum()) {
					scale.setSelection(scale.getSelection() + scale.getIncrement());
					int value = scale.getMaximum() - scale.getSelection() + scale.getMinimum();
					text.setText(Integer.toString(value));
					chart.zoom(-1);
					zoomValue--;
					canvas.redrawChart();
				}
			}
		});

		// SPACE

		Button movementBtn = new Button(this, SWT.TOGGLE);
		movementBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_PAN));

		Button durationBtn = new Button(this, SWT.TOGGLE);
		durationBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_SCALE_TO_FIT));
	}

	public void resetZoomScale() {
//		this.scale.setSelection(scale.getMaximum());
//		zoomValue = 0;
//		text.setText(Integer.toString(0));
	}
}
