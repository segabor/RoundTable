package me.segabor.roundtable.audiograph.data;

import java.util.HashSet;
import java.util.Set;

public class AudioGraph implements Dirty {
	public Node root;
	public Set<Node> nodes;
	public Set<Link> edges;

	// -- calculated fields --
	public DistanceMatrix dm;
	
	public AudioGraph(Node root, Set<Node> nodes) {
		this.root = root;
		this.nodes = nodes;
		
		this.edges = new HashSet<Link>();

		calculate();
	}

	/**
	 * Calculate distance matrix
	 */
	public void calculate() {
		if (root == null || nodes == null) {
			throw new NullPointerException("You shall not pass!");
		}

		// Build a new distance matrix 
		if (dm == null) {
			dm = new DistanceMatrix(root, nodes);
			return;
		}
		
		// rebuild matrix if needed (lazy mode) 
		if (isDirty()) {
			dm.build();
			
			// Cleanup must be invoked at a subsequential step
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// dirty status ?
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
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
		AudioGraph other = (AudioGraph) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}


	/**
	 * Update graph with the new set
	 * 
	 * @param edges
	 */
	public void updateEdges(Set<Link> edges) {
		// TODO elaborate this !
		this.edges = edges;
	}


	// -- dirty state --
	private boolean dirty = false;
	
	@Override
	public boolean isDirty() {
		if (dirty || ( dm!=null && dm.isDirty()) )
			return true;

		// Nodes are marked dirty by event handler
		// Let's check them so
		for (Dirty n : nodes) {
			if (n.isDirty()) {
				return true;
			}
		}

		// check if an edge was changed (ie. link became hard)
		for (Dirty e : edges) {
			if (e.isDirty()) {
				return true;
			}
		}
		
		return false;
	}


	/**
	 * Mark the graph dirty manually.
	 * 
	 * It should be invoked when a new node is added
	 * or a no longer needed instance is removed from the graph
	 */
	@Override
	public void markDirty() {
		dirty = true;
	}


	/**
	 * One shall never invoke this!
	 */
	@Override
	public boolean cleanup() {
		final boolean oldDirty = dirty;
		if (dirty) {
			dirty = false;
		}
		return oldDirty;
	}	
}
