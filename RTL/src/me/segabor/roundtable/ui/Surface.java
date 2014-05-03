package me.segabor.roundtable.ui;

import java.io.Serializable;

import processing.core.PVector;

/**
 * A POJO 
 * @author segabor
 *
 */
public class Surface implements Serializable {
	private static final long serialVersionUID = -2988521362016098037L;

	public final float tableWidth;
	public final float tableHeight;

	// derived values
	public final float tableDiameter;
	public final float tableRadius;

	public final float halfWidth;
	public final float halfHeight;

	public Surface(float tableWidth, float tableHeight) {
		this.tableWidth = tableWidth;
		this.tableHeight = tableHeight;

		this.tableDiameter = Math.min(tableWidth, tableHeight);
		this.tableRadius = tableDiameter / 2;
		this.halfWidth = tableWidth / 2;
		this.halfHeight = tableHeight / 2;
	}

	/**
	 * Transform internal coordinates to surface space
	 * Practically, scale them up [0,1] to [0,tableW/H]
	 * 
	 * @param p vector
	 * @return array of upscaled coordinates
	 */
	public float[] transform(PVector p) {
		return new float[] { tableWidth*p.x, tableHeight*p.y};
	}

	/**
	 * Transform pair of internal coordinates to surface space
	 * Practically, scale them up [0,1] to [0,tableW/H]
	 * 
	 * @param p0 first vector
	 * @param p1 second vector
	 * @return array of upscaled coordinates
	 */
	public float[] transform2(PVector p0, PVector p1) {
		return new float[] { tableWidth*p0.x, tableHeight*p0.y, tableWidth*p1.x, tableHeight*p1.y};
	}
}
