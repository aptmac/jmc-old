package org.openjdk.jmc.flightrecorder.ui.common;

import java.awt.Color;

public class CheckedComboBoxItem {
	private final String text;
	private boolean isSelected;
	private Color color;

	public CheckedComboBoxItem(String text, Color color) {
		this(text, color, false);
	}

	public CheckedComboBoxItem(String text, Color color, boolean isSelected) {
		this.text = text;
		this.color = color;
		this.isSelected = isSelected;
	}

	public Color getColor() {
		return this.color;
	}
	
	public String getText() {
		return this.text;
	}

	public boolean isSelected() {
		return this.isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}