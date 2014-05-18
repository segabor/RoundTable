package me.segabor.roundtable.ui;

import me.segabor.roundtable.app.Tick;


public class TableTopDrawer {
	static final int ALPHA = 0xFF000000;

	// color defs
	static final int C_BACKGROUND = ALPHA | 0x000088;
	static final int C_ROUNDTABLE = ALPHA | 0x0000FF;

	static final int C_PULSE_HIGHLIGHT = ALPHA | 0x4444FF;
	static final int C_PULSE_NORMAL = ALPHA | 0x2222FF;


	private static DrawContext ctx;

	/**
	 * Radius of active area
	 */
	private static float radius;


	public static void initContext(DrawContext dctx) {
		TableTopDrawer.ctx = dctx;
		TableTopDrawer.radius = Math.min(ctx.surface.tableWidth, ctx.surface.tableHeight);
	}


	public static void draw() {
		float cx = ctx.surface.halfWidth, cy = ctx.surface.halfHeight;

		Tick f = ctx.tick;		

		// TODO: shall matrix be saved
		
		ctx.gfx.background(C_BACKGROUND);

		// -- the active area (blue)
		ctx.gfx.noStroke();
		ctx.gfx.fill(C_ROUNDTABLE);
		ctx.gfx.ellipse(cx, cy, radius, radius);

		// -- beat pulse, outer circle -- //
		float r_outer = radius * (float)Math.log(1.0f + f.subBeat);

		ctx.gfx.fill(f.tick == 0 ? C_PULSE_HIGHLIGHT : C_PULSE_NORMAL);
		ctx.gfx.ellipse(cx, cy, r_outer, r_outer);

		// paint inner circle with background
		double ring_width = 0.2 /* f.tick == 0 ? 0.2 : 0.1 */;
		if (f.subBeat > ring_width) {
			float r_inner = radius * (float) Math
					.log(1.0 + f.subBeat - ring_width);

			ctx.gfx.fill(C_ROUNDTABLE);
			ctx.gfx.ellipse(cx, cy, r_inner, r_inner);
		}
	}
}
