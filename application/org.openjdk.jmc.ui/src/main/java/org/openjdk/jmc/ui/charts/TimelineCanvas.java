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
	private static final int RANGE_INDICATOR_HEIGHT = 10;
	private AwtCanvas awtCanvas;
	private Graphics2D g2d;

	public TimelineCanvas(Composite parent) {
		super (parent, SWT.NONE);
		awtCanvas = new AwtCanvas();
		addPaintListener(new Painter());
	}

	public void renderTimeline(int x1, int x2, int axisWidth) {
		// TODO
	}

	class Painter implements PaintListener {

		@Override
		public void paintControl(PaintEvent e) {
			Rectangle rect = getClientArea();
			g2d = awtCanvas.getGraphics(rect.width, rect.height);
			g2d.setColor(Palette.PF_BLACK_100.getAWTColor());
			g2d.fillRect(0, 0, rect.width, rect.height);
			awtCanvas.paint(e, 0, 0);
		}
	}

}
