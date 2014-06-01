package me.segabor.roundtable.audiograph.data;

import TUIO.TuioObject;

public class TuioId implements Comparable<TuioId> {
	// Non-existent fiducial symbol ID
	public static int EXTREME_SYM_ID = 256;
	
	public static TuioId CUSTOM_ID1 = new TuioId(256, EXTREME_SYM_ID);
	public static TuioId CUSTOM_ID2 = new TuioId(257, EXTREME_SYM_ID);
	public static TuioId CUSTOM_ID3 = new TuioId(258, EXTREME_SYM_ID);
	
	private int sessionId;
	private int symboldId;

	protected TuioId(int sessionId, int symbolId) {
		this.sessionId = sessionId;
		this.symboldId = symbolId;
	}
	
	public static TuioId getId(TuioObject obj) {
		return new TuioId((int) obj.getSessionID(), obj.getSymbolID());
	}

	public static TuioId getTestId(NodeType t, long k) {
		// TODO infer subtype as well
		return new TuioId(t.ordinal(), (int) k);
	}
	
	public int getSessionId() {
		return sessionId;
	}
	
	public int getSymboldId() {
		return symboldId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sessionId;
		result = prime * result + symboldId;
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
		TuioId other = (TuioId) obj;
		if (sessionId != other.sessionId)
			return false;
		if (symboldId != other.symboldId)
			return false;
		return true;
	}

	@Override
	public int compareTo(TuioId o) {
		return Integer.compare(this.sessionId, o.sessionId);		
	}	
}
