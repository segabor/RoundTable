package me.segabor.roundtable.audiograph.data;

import java.util.HashSet;
import java.util.Set;

public class AudioGraph {
	public Node root;
	public Set<Node> nodes;
	public Set<Link> edges;

	// -- calculated fields --
	public DistanceMatrix dm;
	private long _hc;

	public AudioGraph(Node root, Set<Node> nodes) {
		this.root = root;
		this.nodes = nodes;
		
		this.edges = new HashSet<Link>();

		calculate();
	}

	
	public void calculate() {
		if (root == null || nodes == null) {
			throw new NullPointerException("You shall not pass!");
		}

		if (dm == null) {
			dm = new DistanceMatrix(root, nodes);
			_hc = hashCode();
			return;
		}
		
		if (dm.isDirty() || _hc != hashCode()) {
			dm.build();
			_hc = hashCode();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
}
