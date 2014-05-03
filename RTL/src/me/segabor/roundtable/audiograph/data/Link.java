package me.segabor.roundtable.audiograph.data;

/**
 * @author segabor
 *
 */
public class Link implements Dirty {
	Node in;
	Node out;
	int type = 0; // flow type: 0 = audio, 1 = control flow
	boolean hard = false; // hard link never breaks when node moves

	/**
	 * Muted edges do not forward audio / control data
	 */
	boolean mute = false;

	/**
	 * Hidden edges is not drawn onto table
	 * Typically global controllers are connected to global out with hidden edge
	 */
	boolean hidden = false;

	public Link(int type, Node in, Node out) {
		this.type = type;
		this.in = in;
		this.out = out;
	}

	@Override
	public int hashCode() {
		return 31 * 31 * type + (in != null ? 31 * in.hashCode() : 0)
				+ (out != null ? out.hashCode() : 0);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Link) {
			Link ol = (Link) other;
			return type == ol.type && in.equals(ol.in) && out.equals(ol.out);
		}
		return false;
	}

	public int getType() {
		return type;
	}

	public Node getIn() {
		return in;
	}

	public Node getOut() {
		return out;
	}

	public boolean isHard() {
		return hard;
	}

	public void setHard(boolean f) {
		hard = f;
	}

	public boolean isMute() {
		return mute;
	}

	public void setMute(boolean f) {
		mute = f;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean f) {
		hidden = f;
	}

	// -- dirty state --

	private boolean dirty = false;
	
	@Override
	public boolean isDirty() {
		if (dirty)
			return true;
		
		if (in != null && in.isDirty())
			return true;
		
		if (out != null && out.isDirty())
			return true;

		return false;
	}

	/**
	 * Mark this edge dirty.
	 * Mostly invoked by the event handler
	 * When an edge attribute was changed
	 */
	@Override
	public void markDirty() {
		this.dirty = true;
	}

	/**
	 * Remove dirty state from the edge
	 * Note this action does not affect the linked nodes!
	 */
	@Override
	public boolean cleanup() {
		final boolean oldState = dirty;

		if (dirty) {
			dirty = false;
		}
		
		return oldState;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		if (in != null) {
			buf.append(in.toString());
		} else {
			buf.append("<NIL>");
		}
		buf.append(" ==|> ");
		if (out != null) {
			buf.append(out.toString());
		} else {
			buf.append("<NIL>");
		}

		return buf.toString();
	}
}