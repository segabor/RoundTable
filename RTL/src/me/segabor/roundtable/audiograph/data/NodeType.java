package me.segabor.roundtable.audiograph.data;


/**
 * @author segabor
 *
 */
public enum NodeType {
	OUT, GENERATOR, CONTROLLER, EFFECT, GLOBAL_CONTROLLER;



	/**
	 * Map TUIO Symbol ID to type
	 * 
	 * @param symbolID TUIO Symbol ID
	 * 
	 * @throws {@link IllegalArgumentException} if symbol ID could not be mapped 
	 * 
	 * @return {@link NodeType} type
	 */
	public static NodeType mapType(final int symbolID) {
		if ((symbolID >= 0 && symbolID < 4) ||
				symbolID == 12 ||
				symbolID == 16 ||
				symbolID == 20 ||
				symbolID == 24) {
			// generator
			return GENERATOR;
		} else if (symbolID >= 4 && symbolID < 8) {
			// controller
			return CONTROLLER;
		} else if (symbolID >= 8 && symbolID < 12) {
			// effect
			return EFFECT;
		} else if (symbolID >= 32 && symbolID < 26) {
			// global controller
			return GLOBAL_CONTROLLER;
		}

		// cannot map
		throw new IllegalArgumentException("No type for symbol ID " + symbolID);
	}
}
