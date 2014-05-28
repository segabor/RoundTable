package me.segabor.roundtable.audiograph.data;

import processing.core.PVector;
import TUIO.TuioObject;

public class Node implements Dirty {
	/**
	 * ID
	 */
	private NodeKey key;
	
	private NodeType type;

	/**
	 * Node label (optional)
	 */
	private String label;
	
	/**
	 * If node is disabled
	 * - it is not drawn on table
	 * - doesn't take part in audio chain
	 * but retains its state until taken back to table top
	 */
	private boolean disabled = false;
	
	/**
	 * Two dimensional cartesian coordinates
	 */
	private PVector coords;

	/**
	 * Rotation angle
	 */
	private float angle;

	/**
	 * @param key ID
	 * @param type {@see NODE_TYPE values}
	 */
	@Deprecated
	public Node(NodeKey key) {
		this(key, key.getType());
	}

	public Node(NodeKey key, NodeType type) {
		this.key = key;
		this.type = type;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + (disabled ? 0 : 1);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (disabled != other.disabled)
			return false;
		return true;
	}

	/**
	 * Returns key
	 * @return
	 */
	public NodeKey getKey() {
		return key;
	}
	
	/**
	 * Return node type
	 * @return
	 */
	public int getType() {
		return type.ordinal();
	}

	public NodeType getNodeType() {
		return type;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public boolean isEnabled() {
		return !disabled;
	}


	public PVector getCoords() {
		return coords;
	}

	/**
	 * Coordinates of the node
	 * 
	 * Allowed range: [0,0]->[1,1]
	 * @param coords
	 */
	public void setCoords(PVector coords) {
		this.coords = coords;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public void setAngle(float angle) {
		this.angle = angle;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("<");
		
		if (label != null)
			buf.append(label);
		else {
			buf.append(key);
		}
		
		if (coords != null) {
			buf.append(" ")
				.append("[")
				.append( String.format("%.2f", coords.x) )
				.append(";")
				.append( String.format("%.2f", coords.y) )
				.append("]")
			;
		}

		buf.append(">");
		
		return buf.toString();
	}


	/**
	 * Apply state changes came with the TUIO event
	 * @param obj TUIO event holder object
	 */
	public void setState(TuioObject obj) {
		coords = new PVector(obj.getX(), obj.getY());
		angle = obj.getAngle();
	}


	// -- dirty state --
	
	private boolean dirty = false;

	@Override
	public boolean isDirty() {
		return dirty;
	}


	/**
	 * Mark node dirty
	 * Invoked by the event handler
	 */
	@Override
	public void markDirty() {
		dirty = true;
	}


	/**
	 * Remove dirty state from this node
	 */
	@Override
	public boolean cleanup() {
		boolean flag = dirty; if (dirty) {dirty = false;}
		return flag;
	}
}
