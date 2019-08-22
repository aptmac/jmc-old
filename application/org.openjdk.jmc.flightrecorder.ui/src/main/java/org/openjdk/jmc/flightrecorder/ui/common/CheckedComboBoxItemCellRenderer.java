package org.openjdk.jmc.flightrecorder.ui.common;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.openjdk.jmc.flightrecorder.ui.messages.internal.Messages;

public class CheckedComboBoxItemCellRenderer<E extends CheckedComboBoxItem> implements ListCellRenderer<E> {
	private final JLabel label = new JLabel();
	private final JCheckBox check = new JCheckBox();

	@SuppressWarnings("rawtypes")
	@Override
	public Component getListCellRendererComponent(JList list, CheckedComboBoxItem item, int index, boolean isSelected, boolean cellHasFocus) {
		if (index == -1) {
			label.setText(Messages.ThreadsPage_LANE_FILTER_HEADER);
			return label;
		} else {
			list.setBackground(item.getColor());
			check.setText(item.getText());
			check.setSelected(item.isSelected());
			check.setBackground(list.getBackground());
			check.setForeground(list.getForeground());
			return check;
		}
	}
}