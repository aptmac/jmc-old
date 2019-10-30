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
package org.openjdk.jmc.flightrecorder.ui.common;

import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.openjdk.jmc.common.item.ItemFilters;
import org.openjdk.jmc.flightrecorder.ui.common.LaneEditor.LaneDefinition;
import org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages;
import org.openjdk.jmc.ui.handlers.MCContextMenuManager;

public class DropdownLaneFilter extends Composite {
	private static final int EXTRA_SHELL_WIDTH = 100;
	private static final int SHELL_HEIGHT = 400;
	private Button dropdownButton;
	private GridLayout layout;
	private MCContextMenuManager mm;
	private Shell shell;
	private ShellAdapter shellDisposeAdapter;
	private ThreadGraphLanes lanes;
	private TypeFilterBuilder filterEditor;

	public DropdownLaneFilter(Composite parent, ThreadGraphLanes lanes, MCContextMenuManager mm) {
		super(parent, SWT.NO_BACKGROUND);
		this.lanes = lanes;
		this.mm = mm;
		this.layout = createGridLayout();
		this.shellDisposeAdapter = new ShellAdapter() {
			public void shellDeactivated(ShellEvent event) {
				if (dropdownButton.isDisposed()) { return; }
				if (dropdownButton.getSelection()) {
					disposeDropdown();
					dropdownButton.setSelection(false);
				}
			}
		};
		setLayout(layout);
		dropdownButton = new Button(this, SWT.TOGGLE);
		dropdownButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		dropdownButton.setText(Messages.DropdownLaneFilter_THREAD_STATE_SELECTION);
		dropdownButton.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!dropdownButton.getSelection()) {
					displayDropdown();
				} else {
					disposeDropdown();
				}
			}
		});
	}

	/**
	 * Creates a new shell which is positioned underneath the dropdown button.
	 * This new shell creates the appearance of a dropdown component, and it's
	 * contents will be the TypeFilterBuilder as found in the Edit Thread Lanes
	 * dialog.
	 */
	private void displayDropdown() {
		Point p = dropdownButton.getParent().toDisplay(dropdownButton.getLocation());
		Point size = dropdownButton.getSize();
		Rectangle shellRect = new Rectangle(p.x, p.y + size.y, size.x, 0);

		shell = new Shell(DropdownLaneFilter.this.getShell(), SWT.BORDER);
		shell.setLayout(this.layout);
		shell.setSize(shellRect.width + EXTRA_SHELL_WIDTH, SHELL_HEIGHT);
		shell.setLocation(shellRect.x, shellRect.y);
		shell.addShellListener(this.shellDisposeAdapter);

		filterEditor = new TypeFilterBuilder(shell, this::onTypeFilterChange);
		filterEditor.setInput(lanes.getTypeTree());
		filterEditor.selectTypes(lanes.getEnabledLanes());
		filterEditor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.open();
	}

	private void disposeDropdown() {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
	}

	private GridLayout createGridLayout() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}

	/**
	 * Creates a new filter, Quick Filter, to be used by the dropdown lane filter.
	 * Sets the quick filter to be active, and update the context menu.
	 */
	private void onTypeFilterChange() {
		LaneDefinition quickLaneDef = new LaneDefinition(Messages.DropdownLaneFilter_QUICK_FILTER, true,
					ItemFilters.type(filterEditor.getCheckedTypeIds().collect(Collectors.toSet())), false);
		lanes.useDropdownFilter(quickLaneDef);
		lanes.updateContextMenu(mm, false);
	}
}
