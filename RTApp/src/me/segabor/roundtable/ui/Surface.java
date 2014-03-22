package me.segabor.roundtable.ui;

import java.io.Serializable;

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
}
