package me.segabor.roundtable.audiograph.data;

import me.segabor.roundtable.audiograph.data.TuioMapperUtil.TypeMap;
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
		TypeMap tm = TuioMapperUtil.mapTuio(obj);
		return toKey(tm);
	}

	public static NodeKey toKey(TypeMap tm) {
		return new NodeKey(tm.type, tm.id);
	}

	public static final long EXTREME_ID_OFFSET = 255;

	public static final long EXTREME_ID1 = EXTREME_ID_OFFSET + 0;

	public static final NodeKey NKEY_ORIGO = new NodeKey(NodeType.OUT, EXTREME_ID1);
}
