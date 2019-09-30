package org.openjdk.jmc.ui.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.common.PatternFly.Palette;
import org.openjdk.jmc.ui.misc.ChartCanvas;
import org.openjdk.jmc.ui.misc.ChartTextCanvas;
import org.openjdk.jmc.ui.misc.DisplayToolkit;

public class ChartDisplayControlBar extends Composite {
	private static final String ZOOM_IN_CURSOR = "zoomInCursor";
	private static final String ZOOM_OUT_CURSOR = "zoomOutCursor";
	private static final String DEFAULT_CURSOR = "defaultCursor";
	private static final int ZOOM_AMOUNT = 1;
	private Map< String, Cursor > cursors = new HashMap<> ();
	private Scale scale;
	private Text text;
	private XYChart chart;
	private ChartCanvas chartCanvas;
	private ChartTextCanvas textCanvas;
	private List<Button> buttonGroup = new ArrayList<>();;
	private Button zoomInBtn;
	private Button zoomOutBtn;
	private Button selectionBtn;
	private Button zoomPanBtn;
	private ZoomPan zoomPan;
	int zoomValue = 0;

	public void setChart(XYChart chart) {
		this.chart = chart;
	}

	public void setChartCanvas(ChartCanvas chartCanvas) {
		this.chartCanvas = chartCanvas;
	}

	public void setTextCanvas(ChartTextCanvas textCanvas) {
		this.textCanvas = textCanvas;
	}

	public void createZoomPan(Composite parent) {
		zoomPan = new ZoomPan(parent);
		parent.setVisible(false);
	}

	public void setZoomOnClickData() {
		boolean shouldZoom = zoomInBtn.getSelection() || zoomOutBtn.getSelection() ;
		if (shouldZoom) {
			int zoomAmount = zoomInBtn.getSelection() ? 1 : -1;
			chart.clearSelection();
			zoomInOut(zoomAmount);
			textCanvas.redrawChartText();
		}
	}

	public void zoomToSelection() {
		if (zoomInBtn.getSelection()) {
			IQuantity selectionStart = chart.getSelectionStart();
			IQuantity selectionEnd = chart.getSelectionEnd();
			if (selectionStart == null || selectionEnd == null) {
				chart.clearVisibleRange();
			} else {
				chart.setVisibleRange(selectionStart, selectionEnd);
			chartCanvas.redrawChart();
			}
		}
	}

	public ChartDisplayControlBar(Composite parent) {
		super(parent, SWT.NO_BACKGROUND);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		this.setLayout(layout);

		cursors.put(DEFAULT_CURSOR, Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
		cursors.put(ZOOM_IN_CURSOR, new Cursor(getDisplay(),
				UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_IN).getImageData(), 0, 0));
		cursors.put(ZOOM_OUT_CURSOR, new Cursor(getDisplay(),
				UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_OUT).getImageData(), 0, 0));

		selectionBtn = new Button(this, SWT.TOGGLE);
		selectionBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_SELECTION));
		selectionBtn.setSelection(true);
		selectionBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setButtonSelectionStates(selectionBtn, null);
				changeCursor(DEFAULT_CURSOR);
			};
		});
		buttonGroup.add(selectionBtn);

		// SPACE

		zoomInBtn = new Button(this, SWT.TOGGLE);
		zoomInBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_IN));
		zoomInBtn.setSelection(false);
		zoomInBtn.addListener(SWT.Selection,  new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (scale.getSelection() > 0) {
					setButtonSelectionStates(zoomInBtn, zoomPanBtn);
					changeCursor(ZOOM_IN_CURSOR);
				} else {
					setButtonSelectionStates(selectionBtn, null);
					changeCursor(DEFAULT_CURSOR);
				}
			}
		});
		zoomInBtn.addMouseListener(new LongPressListener(ZOOM_AMOUNT));
		buttonGroup.add(zoomInBtn);

		scale = new Scale(this, SWT.VERTICAL);
		scale.setMinimum(0);
		scale.setMaximum(50);
		scale.setIncrement(1);
		scale.setSelection(50);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setButtonSelectionStates(null, null);
				changeCursor(DEFAULT_CURSOR);
				zoomInOut(getZoomValueByScale() - zoomValue);
			}
		});

		text = new Text(this, SWT.BORDER | SWT.SINGLE);
		text.setEditable(false);
		text.setText(Integer.toString(0));
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		zoomOutBtn = new Button(this, SWT.TOGGLE);
		zoomOutBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_OUT));
		zoomOutBtn.setSelection(false);
		zoomOutBtn.addListener(SWT.Selection,  new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (scale.getSelection() < scale.getMaximum()) {
					setButtonSelectionStates(zoomOutBtn, zoomPanBtn);
					changeCursor(ZOOM_OUT_CURSOR);
				} else {
					setButtonSelectionStates(selectionBtn, null);
					changeCursor(DEFAULT_CURSOR);
				}
			}
		});
		zoomOutBtn.addMouseListener(new LongPressListener(-ZOOM_AMOUNT));
		buttonGroup.add(zoomOutBtn);

		// SPACE

		zoomPanBtn = new Button(this, SWT.TOGGLE);
		zoomPanBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_ZOOM_PAN));
		zoomPanBtn.setSelection(false);
		zoomPanBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				showZoomPanDisplay(zoomPanBtn.getSelection());
			}
		});
		buttonGroup.add(zoomPanBtn);

		Button scaleToFitBtn = new Button(this, SWT.PUSH);
		scaleToFitBtn.setImage(UIPlugin.getDefault().getImage(UIPlugin.ICON_FA_SCALE_TO_FIT));
		scaleToFitBtn.setSelection(false);
		scaleToFitBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setButtonSelectionStates(scaleToFitBtn, null);
				changeCursor(DEFAULT_CURSOR);
			}

		});
		buttonGroup.add(scaleToFitBtn);

	}

	private void changeCursor(String cursorName) {
		chartCanvas.changeCursor(cursors.get(cursorName));
	}

	private void setButtonSelectionStates(Button buttonSelected, Button dependentButton) {
		for (Button button : buttonGroup) {
			if ((button.getStyle() & SWT.TOGGLE) != 0) {
				if (button.equals(buttonSelected)) {
					button.setSelection(true);
				} else if (dependentButton != null ) {
					if (button.equals(dependentButton)) {
						button.setSelection(true);
					} else {
						button.setSelection(false);
					}
				} else {
					button.setSelection(false);
				}
			}
			showZoomPanDisplay(zoomPanBtn.getSelection());
		}
	}

	private class LongPressListener extends MouseAdapter {

		private static final long LONG_PRESS_TIME = 500;
		private Timer timer;
		private int zoomAmount;

		LongPressListener(int zoomAmount) {
			this.zoomAmount = zoomAmount;
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if(e.button == 1) {
				timer = new Timer();
				timer.schedule(new LongPress(), LONG_PRESS_TIME, (long) (LONG_PRESS_TIME*1.5));
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			timer.cancel();
		}

		public class LongPress extends TimerTask {

			@Override
			public void run() {
				doZoomInOut(zoomAmount);
			}
		}

		private void doZoomInOut(int zoomAmount) {
			DisplayToolkit.inDisplayThread().execute( () -> zoomInOut(zoomAmount));;
		}
	}

	private void zoomInOut(int zoomAmount) {
			int currentZoomValue = getZoomValueByScale();
			if(currentZoomValue == zoomValue) {
				scale.setSelection(scale.getSelection() - zoomAmount*scale.getIncrement());
				currentZoomValue = getZoomValueByScale();
			}
			chart.zoom(currentZoomValue - zoomValue);
			zoomValue = currentZoomValue;
			int value = scale.getMaximum() - scale.getSelection() + scale.getMinimum();
			text.setText(Integer.toString(value));

			chartCanvas.redrawChart();
	}

	private int getZoomValueByScale() {
		return (scale.getMaximum() -  scale.getSelection())/scale.getIncrement();
	}

	private void showZoomPanDisplay(boolean show) {
		if(show) {
			zoomPan.getParent().setVisible(true);
			zoomPan.redrawZoomPan();
		} else {
			zoomPan.getParent().setVisible(false);
		}
	}

	private class ZoomPan extends Canvas  {
		private IRange<IQuantity> chartRange;
		private static final int BORDER_PADDING = 2;
		private Rectangle zoomRect;

		public ZoomPan(Composite parent) {
			super(parent, SWT.NO_BACKGROUND);
			addPaintListener(new Painter());
			chartRange = chart.getVisibleRange();
		}

		public void redrawZoomPan() {
			redraw();
		}

		class Painter implements PaintListener {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle backgroundRect = new Rectangle(0, 0,
						getParent().getSize().x, getParent().getSize().y);
				GC gc = e.gc;

				gc.setBackground(Palette.PF_BLACK_400.getSWTColor());
				gc.fillRectangle(backgroundRect);
				gc.setForeground(Palette.PF_BLACK_900.getSWTColor());
				gc.drawRectangle(backgroundRect);

				updateZoomPanBounds();

				gc.setBackground(Palette.PF_BLACK_100.getSWTColor());
				gc.fillRectangle(zoomRect);
				gc.setForeground(Palette.PF_BLACK_900.getSWTColor());
				gc.drawRectangle(zoomRect);
			}
		}

		private void updateZoomPanBounds() {
			Rectangle zoomCanvasBounds = new Rectangle(0, 0, getParent().getSize().x, getParent().getSize().y);
			IRange<IQuantity> zoomedRange = chart.getVisibleRange();
			IQuantity visibleWidth = chartRange.getExtent();
			double visibleHeight =  chartCanvas.getParent().getBounds().height;
			Rectangle totalBounds = chartCanvas.getBounds();

			if (zoomRect == null ) {
				zoomRect = new Rectangle(0, 0, 0, 0);
			}

			double ratio = getVisibilityRatio(zoomedRange.getStart(), chartRange.getStart(), visibleWidth);
			int start = getPixelLocation(ratio, zoomCanvasBounds.width, 0);

			ratio = getVisibilityRatio(zoomedRange.getEnd(), chartRange.getEnd(), visibleWidth);
			int end = getPixelLocation(ratio, zoomCanvasBounds.width, zoomCanvasBounds.width);

			zoomRect.x = start + BORDER_PADDING;
			zoomRect.width = end - start - 2 * BORDER_PADDING;

			ratio = getVisibilityRatio(0, totalBounds.y, totalBounds.height);
			int top = getPixelLocation(ratio, zoomCanvasBounds.height, 0);

			ratio = getVisibilityRatio(visibleHeight, totalBounds.height + totalBounds.y, totalBounds.height);
			int bottom = getPixelLocation(ratio, zoomCanvasBounds.height, zoomCanvasBounds.height);

			zoomRect.y  = top + BORDER_PADDING;
			zoomRect.height = bottom - top - 2 * BORDER_PADDING;

		}

		private double getVisibilityRatio(double visibleBound, double borderBound, double totalLength) {
			double diff = visibleBound - borderBound;
			return diff/totalLength;
		}

		private double getVisibilityRatio(IQuantity visibleBound, IQuantity borderBound, IQuantity totalLength) {
			IQuantity diff = visibleBound.subtract(borderBound);
			return diff.ratioTo(totalLength);
		}

		private int getPixelLocation(double visibliyRatio, int totalLength, int offset) {
			return offset + (int) (visibliyRatio * totalLength);
		}

	}

	public void resetZoomScale() {
//		this.scale.setSelection(scale.getMaximum());
//		zoomValue = 0;
//		text.setText(Integer.toString(0));
	}
}
