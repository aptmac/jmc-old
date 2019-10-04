package org.openjdk.jmc.ui.charts;

import java.awt.Graphics2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openjdk.jmc.ui.common.PatternFly.Palette;
import org.openjdk.jmc.ui.common.util.Environment;
import org.openjdk.jmc.ui.misc.AwtCanvas;

public class TimelineCanvas extends Canvas {
	private static final int RANGE_INDICATOR_HEIGHT = 7;
	private static final int RANGE_INDICATOR_Y_OFFSET = 30;
	private final double xScale = Display.getDefault().getDPI().x / Environment.getNormalDPI();
	private final double yScale = Display.getDefault().getDPI().y / Environment.getNormalDPI();
	private int xOffset;
	private AwtCanvas awtCanvas;
	private Graphics2D g2d;
	private int x1;
	private int x2;
	private SashForm sashForm;
	private SubdividedQuantityRange xTickRange;

	public TimelineCanvas(Composite parent, SashForm sash) {
		super (parent, SWT.NONE);
		sashForm = sash;
		xOffset = calculateXOffset();
		awtCanvas = new AwtCanvas();
		addPaintListener(new TimelineCanvasPainter());
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
			g2d.setPaint(Palette.PF_ORANGE_400.getAWTColor());
			g2d.fillRect(x1 + xOffset, RANGE_INDICATOR_Y_OFFSET, x2 - x1, RANGE_INDICATOR_HEIGHT);
			g2d.setPaint(Palette.PF_BLACK_600.getAWTColor());
			Point totalSize = sashForm.getChildren()[1].getSize();
			adjusted = translateDisplayToImageCoordinates(totalSize.x,totalSize.y);
			g2d.drawRect(xOffset, RANGE_INDICATOR_Y_OFFSET, adjusted.x, RANGE_INDICATOR_HEIGHT);
			awtCanvas.paint(e, 0, 0);
		}
	}

	private Point translateDisplayToImageCoordinates(int x, int y) {
		int xImage = (int) Math.round(x / xScale);
		int yImage = (int) Math.round(y / yScale);
		return new Point(xImage, yImage);
	}

}
