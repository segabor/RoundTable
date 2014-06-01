package me.segabor.roundtable.audiograph.data;

import java.util.HashMap;
import java.util.Map;

import me.segabor.roundtable.audiograph.data.TuioMapperUtil.TypeMap;
import TUIO.TuioObject;

public class NodeFactory {
	private static Map<NodeKey, Node> nodeCache = new HashMap<NodeKey, Node>();

	
	/**
	 * Acquire a Node
	 * 
	 * @param key tuio object
	 * 
	 * @return new or existing node
	 */
	public static Node getNode(TuioObject obj) {
		TypeMap map = TuioMapperUtil.mapTuio(obj);
		final NodeKey key = NodeKey.toKey(map);
		return getOrCreateNode(key, map.type, map.subtype);
	}

	
	private synchronized static Node getOrCreateNode(NodeKey key, NodeType type, NodeSubtype subType) throws IllegalArgumentException {
		if (key == null) {
			throw new IllegalArgumentException("Missing parameter");
		}

		Node node = null;
		synchronized (nodeCache) {
			if (!nodeCache.keySet().contains(key)) {
				if (type != null) {
					// create a new node
					node = new Node(key, type, subType);
					
					nodeCache.put(key, node);
				} else {
					return null;
				}
				
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
