package org.openjdk.jmc.ui.charts;

import java.awt.Graphics2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.openjdk.jmc.ui.common.PatternFly.Palette;
import org.openjdk.jmc.ui.misc.AwtCanvas;

public class TimelineCanvas extends Canvas {
	private static final int RANGE_INDICATOR_HEIGHT = 7;
	private static final int RANGE_INDICATOR_Y_OFFSET = 30;
	private final int xOffset;
	private AwtCanvas awtCanvas;
	private Graphics2D g2d;
	private int x1;
	private int x2;
	private int axisWidth;
	private SubdividedQuantityRange xTickRange;

	public TimelineCanvas(Composite parent, int xOffset) {
		super (parent, SWT.NONE);
		this.xOffset = xOffset + 9;
		awtCanvas = new AwtCanvas();
		addPaintListener(new TimelineCanvasPainter());
	}

	public void renderRangeIndicator(int x1, int x2, int axisWidth) {
		this.x1 = x1;
		this.x2 = x2;
		this.axisWidth = axisWidth;
		this.redraw();
	}

	public void renderAxis(SubdividedQuantityRange xTickRange) {
		this.xTickRange = xTickRange;
		this.redraw();
	}

	private class TimelineCanvasPainter implements PaintListener {

		@Override
		public void paintControl(PaintEvent e) {
			Rectangle rect = getClientArea();
			g2d = awtCanvas.getGraphics(rect.width, rect.height);

			// Draw the background
			g2d.setColor(Palette.PF_BLACK_100.getAWTColor());
			g2d.fillRect(0, 0, rect.width, rect.height);

			// Draw the horizontal axis
			if (xTickRange != null) {
				g2d.setColor(Palette.PF_BLACK.getAWTColor());
				AWTChartToolkit.drawAxis(g2d, xTickRange, 0, false, 1 - xOffset, false);
			}

			// Draw the range indicator
			g2d.setPaint(Palette.PF_ORANGE_400.getAWTColor());
			g2d.fillRect(x1 + xOffset, RANGE_INDICATOR_Y_OFFSET, x2 - x1, RANGE_INDICATOR_HEIGHT);
			g2d.setPaint(Palette.PF_BLACK_600.getAWTColor());
			g2d.drawRect(xOffset, RANGE_INDICATOR_Y_OFFSET, axisWidth, RANGE_INDICATOR_HEIGHT);

			awtCanvas.paint(e, 0, 0);
		}
	}

}
