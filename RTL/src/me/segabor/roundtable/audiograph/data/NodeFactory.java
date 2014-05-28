package me.segabor.roundtable.audiograph.data;

import java.util.HashMap;
import java.util.Map;

import TUIO.TuioObject;

public class NodeFactory {
	private static Map<NodeKey, Node> nodeCache = new HashMap<NodeKey, Node>();

	
	/**
	 * Acquire a Node
	 * 
	 * @param key node key
	 * 
	 * @return new or existing node
	 */
	public static Node getNode(final NodeKey key) {
		return getOrCreateNode(key);
	}


	/**
	 * Acquire a Node
	 * 
	 * @param key tuio object
	 * 
	 * @return new or existing node
	 */
	public static Node getNode(TuioObject obj) {
		// At this point an IllegalArgumentException might be rased
		// if key cannot be created from TUIO object
		final NodeKey key = NodeKey.toKey(obj);

		return getOrCreateNode(key);
	}

	
	private synchronized static Node getOrCreateNode(NodeKey key) throws IllegalArgumentException {
		if (key == null) {
			throw new IllegalArgumentException("Missing parameter");
		}

		Node node = null;
		synchronized (nodeCache) {
			if (!nodeCache.keySet().contains(key)) {
				// create a new node
				node = new Node(key, key.getType());
				
				nodeCache.put(key, node);
			} else {
				node = nodeCache.get(key);
			}
		}
		return node;
	}

	
	
	/**
	 * Clear the node cache
	 * This is expected to be invoked when table top
	 * sends 'reset' event
	 */
	public static void clear() {
		synchronized (nodeCache) {
			nodeCache.clear();
		}
	}


	/**
	 * Invalidate node
	 * @param n
	 */
	public static void invalidate(Node n) {
		synchronized (nodeCache) {
			nodeCache.remove(n.getKey());
		}
	}
}
