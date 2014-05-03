package me.segabor.roundtable.ui;

import me.segabor.roundtable.audiograph.data.Link;
import processing.core.PApplet;
import processing.core.PVector;

public class EdgeDrawer {
	public static void draw(Link l, PApplet ctx, Surface surface) {
		PVector p0 = l.getIn().getCoords(),
				p1 = l.getOut().getCoords();


		float t[] = surface.transform2(p0, p1);
		
		ctx.stroke(255, 255, 255);
		ctx.strokeWeight(1);
		ctx.line(t[0], t[1], t[2], t[3]);

		// arrow head
		ctx.pushMatrix();
		ctx.translate(t[2], t[3]);
		// TODO CHECK if still works
		float a = PApplet.atan2(p0.x - p1.x, p1.y - p0.y /* x1-x2, y2-y1 */);
		ctx.rotate(a);
		ctx.line(0, 0, -10, -20);
		ctx.line(0, 0, 10, -20);
		ctx.popMatrix();
	}
}
