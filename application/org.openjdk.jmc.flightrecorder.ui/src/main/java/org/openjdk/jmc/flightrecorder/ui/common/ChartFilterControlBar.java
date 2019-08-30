package org.openjdk.jmc.flightrecorder.ui.common;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class ChartFilterControlBar extends Composite {

	private DateTime fromTime;
	private DateTime toTime;
	
	public ChartFilterControlBar(Composite parent, Listener resetListener) {
		super(parent, SWT.NO_BACKGROUND);

		RowLayout layout = new RowLayout();
		layout.spacing = 5;

		this.setLayout(layout);
		Label nameLabel = new Label(this, SWT.CENTER | SWT.HORIZONTAL);
		nameLabel.setText("Threads");
		nameLabel.setLayoutData(new RowData(180, SWT.DEFAULT));
		nameLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		Label eventsLabel = new Label(this, SWT.LEFT | SWT.HORIZONTAL);
		eventsLabel.setText("Filter events");
		eventsLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		fromTime = new DateTime(this, SWT.TIME | SWT.MEDIUM);
		fromTime.setFont(JFaceResources.getDefaultFont());
		fromTime.setTime(9, 9, 9);
		fromTime.addSelectionListener (new SelectionAdapter () {
		    public void widgetSelected (SelectionEvent e) {
		    	//
		    }
		});

		Label to = new Label(this, SWT.CENTER);
		to.setText("to");

		toTime = new DateTime(this, SWT.TIME | SWT.MEDIUM);
		toTime.setTime(10, 10, 10);
		toTime.setFont(JFaceResources.getDefaultFont());
		toTime.addSelectionListener (new SelectionAdapter () {
		    public void widgetSelected (SelectionEvent e) {
		    	//
		    }
		});

		Button filterBtn = new Button(this, SWT.PUSH);
		filterBtn.setText("Filter");
		filterBtn.setLayoutData(new RowData(60, 20));
		filterBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				//
			}
		});

		Button resetBtn = new Button(this, SWT.PUSH);
		resetBtn.setText("Reset");
		resetBtn.setLayoutData(new RowData(60, 20));
		resetBtn.addListener(SWT.Selection, resetListener);
	}
}
