package me.segabor.roundtable.audiograph.data;

import TUIO.TuioObject;

public class TuioMapperUtil {
	public static class TypeMap {
		/**
		 * ID generated from session and symbol ids
		 */
		public long id;
		public NodeType type;
		public NodeSubtype subtype = NodeSubtype.UNKNOWN;
	}

	public static TypeMap mapTuio(TuioObject obj) {
		TypeMap m = new TypeMap();
		
		final long sessionID = obj.getSessionID();
		final int symbolID = obj.getSymbolID();

		m.id = sessionID << 8 | symbolID;

		if ((symbolID >= 0 && symbolID < 4) ||
				symbolID == 12 ||
				symbolID == 16 ||
				symbolID == 20 ||
				symbolID == 24) {
			// generator
			m.type = NodeType.GENERATOR;
			
			if (symbolID == 0) {
				m.subtype = NodeSubtype.OSCILLATOR;
			}
		} else if (symbolID >= 4 && symbolID < 8) {
			// controller
			m.type = NodeType.CONTROLLER;
		} else if (symbolID >= 8 && symbolID < 12) {
			// effect
			m.type =  NodeType.EFFECT;
		} else if (symbolID >= 32 && symbolID < 26) {
			// global controller
			m.type =  NodeType.GLOBAL_CONTROLLER;
		}

		return m;
	}
}
