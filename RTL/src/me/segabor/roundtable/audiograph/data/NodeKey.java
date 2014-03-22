package me.segabor.roundtable.audiograph.data;

import TUIO.TuioObject;


public class NodeKey extends Key<NodeType, Long> {
	public NodeKey(NodeType type, Long id) {
		super(type, id);
	}

	/**
	 * Creates a node key from a {@link TuioObject}
	 * 
	 * @param obj
	 * 
	 * @throws {@link IllegalArgumentException} parameter could not be mapped to a valid key
	 * 
	 * @return
	 */
	public static NodeKey toKey(TuioObject obj) {
		final NodeType _t = NodeType.mapType(obj.getSymbolID());
		final long _id = genID(obj); 

		return new NodeKey(_t, _id);
	}


	/**
	 * Map Tuio ID to node ID
	 * 
	 * A TUIO object identifier is composed of two data
	 * 
	 * <ul>
	 * <li>Session ID - this ID is bound to actual TUIO session</li>
	 * <li>Symbol ID - Fiducial Symbol ID</li>
	 * </ul>
	 * 
	 * @param obj A {@link TuioObject} 
	 * @return A key
	 */
	private static long genID(TuioObject obj) {
		final long sessionId = obj.getSessionID();
		final int symbolId = obj.getSymbolID();

		return sessionId << 8 | symbolId;
	}


	public static final long EXTREME_ID_OFFSET = 255;

	public static final long EXTREME_ID1 = EXTREME_ID_OFFSET + 0;

	public static final NodeKey NKEY_ORIGO = new NodeKey(NodeType.OUT, EXTREME_ID1);
}
