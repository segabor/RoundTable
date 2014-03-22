package me.segabor.roundtable.audiograph.data;

/**
 * @author segabor
 *
 */
public class Link {
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
}