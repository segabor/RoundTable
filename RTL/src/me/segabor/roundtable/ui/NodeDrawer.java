package me.segabor.roundtable.ui;

import me.segabor.roundtable.audiograph.data.Node;
import processing.core.PApplet;

public class NodeDrawer {
	private static DrawContext ctx;
	
	public static void initContext(DrawContext dctx) {
		NodeDrawer.ctx = dctx;
	}

	public static void draw(Node n) {
		ctx.gfx.pushMatrix();
		ctx.gfx.rectMode(PApplet.CENTER);

		final float[] t = ctx.surface.transform(n.getCoords());

		switch(n.getKey().getType()) {
		case OUT:
			// center node
			ctx.gfx.translate(t[0], t[1]);
			ctx.gfx.fill(255, 255, 255);
			ctx.gfx.ellipse(0, 0, 10, 10);
			break;
		case GENERATOR:
			// generator - SQUARE
			ctx.gfx.translate(t[0], t[1]);
			ctx.gfx.rotate(n.getAngle());
			ctx.gfx.rect(0, 0, 40, 40);
			break;
		case CONTROLLER:
			// controller - CIRCLE
			ctx.gfx.translate(t[0], t[1]);
			ctx.gfx.ellipse(0, 0, 40, 40);
			break;
		case EFFECT:
			// effect - ROUNDED SQUARE
			ctx.gfx.translate(t[0], t[1]);
			ctx.gfx.rotate(n.getAngle());
			ctx.gfx.rect(0, 0, 40, 40, 5);
			break;
		case GLOBAL_CONTROLLER:
			// HIDDEN
			break;
		default:
			break;
		}

		ctx.gfx.popMatrix();
	}
}
