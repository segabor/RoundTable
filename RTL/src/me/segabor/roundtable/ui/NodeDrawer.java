package me.segabor.roundtable.ui;

import me.segabor.roundtable.audiograph.data.Node;
import processing.core.PApplet;

public class NodeDrawer {
	public static void draw(Node n, PApplet ctx, Surface surface) {

		final float RW = surface.tableWidth;
		final float RH = surface.tableHeight;
		
		ctx.pushMatrix();
		ctx.rectMode(PApplet.CENTER);

		switch(n.getKey().getType()) {
		case OUT:
			// center node
			ctx.translate(RW / 2, RH / 2);
			ctx.fill(255, 255, 255);
			ctx.ellipse(0, 0, 10, 10);
			break;
		case GENERATOR:
			// generator - SQUARE
			ctx.translate(RW * n.getCoords().x, RH * n.getCoords().y);
			ctx.rotate(n.getAngle());
			ctx.rect(0, 0, 40, 40);
			break;
		case CONTROLLER:
			// controller - CIRCLE
			ctx.translate(RW * n.getCoords().x, RH * n.getCoords().y);
			ctx.ellipse(0, 0, 40, 40);
			break;
		case EFFECT:
			// effect - ROUNDED SQUARE
			ctx.translate(RW * n.getCoords().x, RH * n.getCoords().y);
			ctx.rotate(n.getAngle());
			ctx.rect(0, 0, 40, 40, 5);
			break;
		case GLOBAL_CONTROLLER:
			break;
		default:
			break;
		}

		ctx.popMatrix();
			
	}
}
