package org.openjdk.jmc.flightrecorder.ui.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ScrolledCompositeToolkit extends FormToolkit {

	public ScrolledCompositeToolkit(Display display) {
		super(display);
	}

	public ScrolledComposite createScrolledComposite(Composite parent) {
		return new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
	}

}