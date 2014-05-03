package me.segabor.roundtable.ui;

import me.segabor.roundtable.audiograph.data.Link;
import processing.core.PApplet;
import processing.core.PVector;

public class EdgeDrawer {
	private static DrawContext ctx;
	
	public static void initContext(DrawContext dctx) {
		EdgeDrawer.ctx = dctx;
	}

	public static void draw(Link l) {
		PVector p0 = l.getIn().getCoords(),
				p1 = l.getOut().getCoords();


		float t[] = ctx.surface.transform2(p0, p1);
		
		ctx.gfx.stroke(255, 255, 255);
		ctx.gfx.strokeWeight(1);
		ctx.gfx.line(t[0], t[1], t[2], t[3]);

		// arrow head
		ctx.gfx.pushMatrix();
		ctx.gfx.translate(t[2], t[3]);
		// TODO CHECK if still works
		float a = PApplet.atan2(p0.x - p1.x, p1.y - p0.y /* x1-x2, y2-y1 */);
		ctx.gfx.rotate(a);
		ctx.gfx.line(0, 0, -10, -20);
		ctx.gfx.line(0, 0, 10, -20);
		ctx.gfx.popMatrix();
	}
}
