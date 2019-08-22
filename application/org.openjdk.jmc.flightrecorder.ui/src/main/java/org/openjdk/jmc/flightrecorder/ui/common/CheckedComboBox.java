package org.openjdk.jmc.flightrecorder.ui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JComboBox;


@SuppressWarnings("serial")
public class CheckedComboBox<E extends CheckedComboBoxItem> extends JComboBox<E> {

	private boolean isOpen;

	public CheckedComboBox(E[] items) {
		super(items);
		this.isOpen = false;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setRenderer(new CheckedComboBoxItemCellRenderer<>());
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (InputEvent.MOUSE_EVENT_MASK != 0) {
					if (isPopupVisible()) {
						int index = getSelectedIndex();
						E item = getItemAt(index);
						item.setSelected(!item.isSelected());
						removeItemAt(index);
						insertItemAt(item, index);
						setSelectedItem(item);
					}
					isOpen = true;
				}
			}
		});
	}

	@Override
	public void setPopupVisible(boolean b) {
		if (isOpen) {
			isOpen = false;
		} else {
			super.setPopupVisible(b);
		}
	}
}
