package me.segabor.roundtable.audiograph.data;

import processing.core.PVector;
import TUIO.TuioObject;

public class Node {
	/**
	 * ID
	 */
	private NodeKey key;

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
	
	
	// recent TUIO event
	TuioObject event;

	/**
	 * Two dimensional cartesian coordinates
	 */
	PVector coords;

	/**
	 * @param key ID
	 * @param type {@see NODE_TYPE values}
	 */
	public Node(NodeKey key) {
		this.key = key;
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
	 * Return node type
	 * @return
	 */
	public int getType() {
		return key.getType().ordinal();
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


	/**
	 * Set actual event
	 * @param obj
	 */
	public void setEvent(TuioObject obj) {
		this.event = obj;
	}

	public TuioObject getEvent() {
		return this.event;
	}

	public PVector getCoords() {
		return coords;
	}

	public void setCoords(PVector coords) {
		this.coords = coords;
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		if (label != null)
			buf.append(label);
		else {
			buf.append("<").append(key).append(">");
		}
		
		return buf.toString();
	}
}
