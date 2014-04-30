package me.segabor.roundtable.ui;

import me.segabor.roundtable.audiograph.data.Link;
import processing.core.PApplet;
import processing.core.PVector;

public class EdgeDrawer {
	public static void draw(Link l, PApplet ctx, Surface surface) {

		// DRAW
		// pushMatrix();

		PVector p0 = l.getIn().getCoords(), p1 = l.getOut().getCoords();

		ctx.stroke(255, 255, 255);
		ctx.strokeWeight(1);
		// System.out.println("Connect " + p0.x + ";" + p0.y + " with " +
		// p1.x + ";" + p1.y);
		ctx.line(p0.x, p0.y, p1.x, p1.y);

		// arrow head
		ctx.pushMatrix();
		ctx.translate(p1.x, p1.y);
		float a = ctx.atan2(p0.x - p1.x, p1.y - p0.y /* x1-x2, y2-y1 */);
		ctx.rotate(a);
		ctx.line(0, 0, -10, -20);
		ctx.line(0, 0, 10, -20);
		ctx.popMatrix();

		// popMatrix();

	}
}
