package org.openjdk.jmc.ui.charts;

import java.awt.Graphics2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.IRange;
import org.openjdk.jmc.ui.common.PatternFly.Palette;
import org.openjdk.jmc.ui.common.util.Environment;
import org.openjdk.jmc.ui.misc.AwtCanvas;
import org.openjdk.jmc.ui.misc.ChartCanvas;

public class TimelineCanvas extends Canvas {
	private static final int RANGE_INDICATOR_HEIGHT = 10;
	private static final int RANGE_INDICATOR_Y_OFFSET = 25;
	private final double xScale = Display.getDefault().getDPI().x / Environment.getNormalDPI();
	private final double yScale = Display.getDefault().getDPI().y / Environment.getNormalDPI();
	private int x1;
	private int x2;
	private int xOffset;
	private AwtCanvas awtCanvas;
	private ChartCanvas chartCanvas;
	private Graphics2D g2d;
	private IRange<IQuantity> chartRange;
	private Rectangle dragRect;
	private Rectangle indicatorRect;
	private Rectangle timelineRect;
	private SashForm sashForm;
	private SubdividedQuantityRange xTickRange;
	private XYChart chart;

	public TimelineCanvas(Composite parent, ChartCanvas chartCanvas, SashForm sashForm) {
		super(parent, SWT.NONE);
		this.chartCanvas = chartCanvas;
		this.sashForm = sashForm;
		xOffset = calculateXOffset();
		awtCanvas = new AwtCanvas();
		addPaintListener(new TimelineCanvasPainter());
		DragDetector dragDetector = new DragDetector();
		addMouseListener(dragDetector);
		addMouseMoveListener(dragDetector);
	}

	private int calculateXOffset() {
		return sashForm.getChildren()[0].getSize().x + sashForm.getSashWidth();
	}

	public void renderRangeIndicator(int x1, int x2) {
		this.x1 = x1;
		this.x2 = x2;
		this.redraw();
	}

	public void renderAxis(SubdividedQuantityRange xTickRange) {
		this.xTickRange = xTickRange;
		this.redraw();
	}

	public void setChart(XYChart chart) {
		this.chart = chart;
		chartRange = chart.getVisibleRange();
	}

	private class TimelineCanvasPainter implements PaintListener {

		@Override
		public void paintControl(PaintEvent e) {
			xOffset = calculateXOffset();

			Rectangle rect = getClientArea();
			g2d = awtCanvas.getGraphics(rect.width, rect.height);

			// Draw the background
			Point adjusted = translateDisplayToImageCoordinates(rect.width, rect.height);
			g2d.setColor(Palette.PF_BLACK_100.getAWTColor());
			g2d.fillRect(0, 0, adjusted.x, adjusted.y);

			// Draw the horizontal axis
			if (xTickRange != null) {
				g2d.setColor(Palette.PF_BLACK.getAWTColor());
				AWTChartToolkit.drawAxis(g2d, xTickRange, 0, false, 1, false, xOffset);
			}

			// Draw the range indicator
			if (dragRect != null) {
				indicatorRect = dragRect;
				dragRect = null;
			} else {
				indicatorRect = new Rectangle(x1 + xOffset, RANGE_INDICATOR_Y_OFFSET, x2 - x1, RANGE_INDICATOR_HEIGHT);
			}

			g2d.setPaint(Palette.PF_ORANGE_400.getAWTColor());
			g2d.fillRect(indicatorRect.x, indicatorRect.y, indicatorRect.width, indicatorRect.height);

			Point totalSize = sashForm.getChildren()[1].getSize();
			adjusted = translateDisplayToImageCoordinates(totalSize.x,totalSize.y);
			timelineRect = new Rectangle(xOffset, RANGE_INDICATOR_Y_OFFSET, sashForm.getChildren()[1].getSize().x, RANGE_INDICATOR_HEIGHT);
			g2d.setPaint(Palette.PF_BLACK_600.getAWTColor());
			g2d.drawRect(timelineRect.x, timelineRect.y, timelineRect.width, timelineRect.height);

			awtCanvas.paint(e, 0, 0);
		}
	}

	private Point translateDisplayToImageCoordinates(int x, int y) {
		int xImage = (int) Math.round(x / xScale);
		int yImage = (int) Math.round(y / yScale);
		return new Point(xImage, yImage);
	}

	private class DragDetector extends MouseAdapter implements MouseMoveListener {

		boolean isDrag = false;
		Point currentSelection;
		Point lastSelection;

		@Override
		public void mouseDown(MouseEvent e) {
			if (isDrag || e.button == 1 && timelineRect.contains(e.x, e.y)) {
				isDrag = true;
				currentSelection = translateDisplayToImageCoordinates(e.x, e.y);
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			isDrag = false;
			chart.setIsZoomPanDrag(false);
		}

		@Override
		public void mouseMove(MouseEvent e) {
			if (isDrag || isDrag && timelineRect.contains(e.x, e.y)) {
				lastSelection = currentSelection;
				chart.setIsZoomPanDrag(true);
				currentSelection = translateDisplayToImageCoordinates(e.x, e.y);
				int xdiff = currentSelection.x - lastSelection.x;
				updateTimelineIndicatorFromDrag(xdiff);
			}
		}

		private void updateTimelineIndicatorFromDrag(int xdiff) {
			if (xdiff != 0 &&
					(indicatorRect.x + xdiff) >= timelineRect.x &&
					(indicatorRect.x + xdiff + indicatorRect.width) <= timelineRect.x + timelineRect.width) {
				indicatorRect.x = indicatorRect.x + xdiff;
				SubdividedQuantityRange xAxis = new SubdividedQuantityRange(chartRange.getStart(), chartRange.getEnd(), timelineRect.width, 1);
				chart.setVisibleRange(xAxis.getQuantityAtPixel(indicatorRect.x - xOffset),
						xAxis.getQuantityAtPixel(indicatorRect.x - xOffset + indicatorRect.width));
				dragRect = indicatorRect;
				chartCanvas.redrawChart();
			}
		}
	}

}
