package org.openjdk.jmc.ui.charts;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.ui.common.PatternFly.Palette;

public class TimeDisplay extends Composite {

		private final String TIME_FORMAT_REGEX = "\\d{2}\\:\\d{2}\\:\\d{2}\\:\\d{3}";
		private final Pattern timePattern = Pattern.compile(TIME_FORMAT_REGEX);
		private final Pattern digitPattern = Pattern.compile("\\d{3}|\\d{2}");
		private Calendar currentCalendar; // currently active time in Calendar form
		private IQuantity currentTime; // currently active time
		private IQuantity displayTime; // time shown in the widget (may or not be what's active)
		private StringBuilder sb;
		private Text timeText;

		public TimeDisplay(Composite parent) {
			super(parent, SWT.NO_BACKGROUND);
			this.setLayout(new GridLayout());
			timeText = new Text(this, SWT.SEARCH | SWT.SINGLE);
			timeText.setTextLimit(12);
			timeText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					displayTime = null;
					if (isFormatValid() && isValidTime()) {
						timeText.setForeground(Palette.PF_BLACK.getSWTColor());
					} else {
						timeText.setForeground(Palette.PF_RED_100.getSWTColor());
					}
				}
			});
		}

		// Convert epoch ms timestamp to Calendar object
		private Calendar convertEpochToCalendar(long epoch) {
			currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(new Date(epoch));
			return currentCalendar;
		}

		// Locally store the new time, and format it for displaying in the Text widget
		public void setTime(IQuantity time) {
			this.currentTime = time;
			displayTime(formatTimeString(convertEpochToCalendar(time.in(UnitLookup.EPOCH_MS).longValue())));
		}

		// Returns the IQuantity time stamp of the time displayed in the widget
		public IQuantity getDisplayTime() {
			if (displayTime != null) {
				formatTimeString(convertEpochToCalendar(displayTime.in(UnitLookup.EPOCH_MS).longValue()));
				return displayTime;
			}
			if (isFormatValid() && isValidTime()) {
				IQuantity time = currentTime;
				Matcher m = digitPattern.matcher(timeText.getText());
				int i = 0;
				while(m.find()) {
					int value = Integer.parseInt(m.group());
					switch(i) {
					case 0:
						value = value - currentCalendar.get(Calendar.HOUR);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.HOUR.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					case 1:
						value = value - currentCalendar.get(Calendar.MINUTE);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.MINUTE.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					case 2:
						value = value - currentCalendar.get(Calendar.SECOND);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.SECOND.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					case 3:
						value = value - currentCalendar.get(Calendar.MILLISECOND);
						time = time.in(UnitLookup.EPOCH_NS).add(UnitLookup.MILLISECOND.quantity(value).in(UnitLookup.NANOSECOND));
						break;
					}
					i++;
				}
				this.displayTime = time;
				return time;
			}
			return null;
		}

		// Format the calendar time to a string HH:mm:ss:SSS
		private String formatTimeString(Calendar cal) {
			sb = new StringBuilder();
			sb.append(String.format("%02d", cal.get(Calendar.HOUR)));
			sb.append(":");
			sb.append(String.format("%02d", cal.get(Calendar.MINUTE)));
			sb.append(":");
			sb.append(String.format("%02d", cal.get(Calendar.SECOND)));
			sb.append(":");
			sb.append(String.format("%03d", cal.get(Calendar.MILLISECOND)));
			return sb.toString();
		}

		public void displayTime(String time) {
			timeText.setText(time);
		}

		/**
		 * Verify that the time string inside the text widget matches the
		 * expected time format of HH:mm:ss:SSS
		 * @return true if the text corresponds to a HH:mm:ss:SSS format
		 */
		protected boolean isFormatValid() {
			if (!timePattern.matcher(timeText.getText()).matches()) {
				// not in HH:mm:ss:SSS format
				return false;
			}
			return true;
		}

		/**
		 * Verify that the string inside the text widget is a valid
		 * 24-hour clock time
		 * @return true if the text corresponds to a valid 24-hour time
		 */
		private boolean isValidTime() {
			Matcher m = digitPattern.matcher(timeText.getText());
			int i = 0;
			while(m.find()) {
				int value = Integer.parseInt(m.group());
				if (i == 0 && value >= 24) {
					return false;
				} else if ((i == 1 || i == 2) && value >= 60) {
					return false;
				}
				i++;
			}
			return true;
		}
	}
