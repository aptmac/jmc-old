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
import org.openjdk.jmc.ui.handlers.MCContextMenuManager;

public class DropdownLaneFilter extends Composite {

	private static final String QUICK_FILTER_ID = "Quick Filter"; //$NON-NLS-1$
	private static final String THREAD_STATE_SELECTION = "Thread State Selection"; //$NON-NLS-1$
	private Button dropdownButton;
	private GridLayout layout;
	private Shell shell;
	private ShellAdapter shellDisposeAdapter;
	private MCContextMenuManager mm;
	private ThreadGraphLanes lanes;
	private TypeFilterBuilder filterEditor;

	public DropdownLaneFilter(Composite parent, ThreadGraphLanes lanes, MCContextMenuManager mm) {
		super(parent, SWT.NO_BACKGROUND);
		this.lanes = lanes;
		this.mm = mm;
		this.layout = createGridLayout();
		this.shellDisposeAdapter = createDisposeAdapter();
		setLayout(layout);
		dropdownButton = new Button(this, SWT.TOGGLE);
		dropdownButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		dropdownButton.setText(THREAD_STATE_SELECTION);
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
		shell.setSize(shellRect.width + 100, 400);
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
	 * Prepares the ShellAdapter which will be used when the dropdown
	 * is to be disposed of.
	 * @return ShellAdapter to be used by the dropdown shell
	 */
	private ShellAdapter createDisposeAdapter() {
		return new ShellAdapter() {
			public void shellDeactivated(ShellEvent event) {
				if (dropdownButton.getSelection()) {
					disposeDropdown();
					dropdownButton.setSelection(false);
				}
			}
		};
	}

	/**
	 * Creates a new filter, Quick Filter, to be used by the dropdown lane filter.
	 * Sets the quick filter to be active, and update the context menu.
	 */
	private void onTypeFilterChange() {
		LaneDefinition quickLaneDef = new LaneDefinition(QUICK_FILTER_ID, true,
					ItemFilters.type(filterEditor.getCheckedTypeIds().collect(Collectors.toSet())), false);
		lanes.useDropdownFilter(quickLaneDef);
		lanes.updateContextMenu(mm, false);
	}
}
