package me.segabor.roundtable.ui;

import me.segabor.roundtable.audiograph.data.Node;
import processing.core.PApplet;

public class NodeDrawer {
	public static void draw(Node n, PApplet ctx, Surface surface) {
		ctx.pushMatrix();
		ctx.rectMode(PApplet.CENTER);

		final float[] t = surface.transform(n.getCoords());
		
		switch(n.getKey().getType()) {
		case OUT:
			// center node
			ctx.translate(t[0], t[1]);
			ctx.fill(255, 255, 255);
			ctx.ellipse(0, 0, 10, 10);
			break;
		case GENERATOR:
			// generator - SQUARE
			ctx.translate(t[0], t[1]);
			ctx.rotate(n.getAngle());
			ctx.rect(0, 0, 40, 40);
			break;
		case CONTROLLER:
			// controller - CIRCLE
			ctx.translate(t[0], t[1]);
			ctx.ellipse(0, 0, 40, 40);
			break;
		case EFFECT:
			// effect - ROUNDED SQUARE
			ctx.translate(t[0], t[1]);
			ctx.rotate(n.getAngle());
			ctx.rect(0, 0, 40, 40, 5);
			break;
		case GLOBAL_CONTROLLER:
			// HIDDEN
			break;
		default:
			break;
		}

		ctx.popMatrix();
			
	}
}
