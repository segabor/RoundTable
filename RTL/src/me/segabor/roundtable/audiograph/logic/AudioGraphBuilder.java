package me.segabor.roundtable.audiograph.logic;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import me.segabor.roundtable.audiograph.data.AudioGraph;
import me.segabor.roundtable.audiograph.data.DistanceMatrix;
import me.segabor.roundtable.audiograph.data.Link;
import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeType;

public class AudioGraphBuilder {
	private final static Logger LOGGER = Logger.getLogger(AudioGraphBuilder.class.getName());

	/**
	 * (dis)connection matrix
	 * CMAT[t1][t2] = No edge can be drawn from Node with type t1  to Node with type t2
	 */
	private static final boolean cmat[][] = new boolean[NodeType.values().length][NodeType.values().length];
	static {
		for (int i=0; i<NodeType.values().length; i++) {
			for (int j=0; j<NodeType.values().length; j++) {
				cmat[i][j] = !canConnect(i, j);
			}
		}
	}


	public boolean buildAudioGraph(DistanceMatrix mat, Set<Link> edges) {
		// count of nodes
		final int s = mat.size();

		// no nodes
		if (s == 0)
			return false;

		Node n, n2;
		Node _origo = mat.getOrigo();





		for (int z = 0; z < mat.distList.length; z++) {
			int i = mat.distList[z];

			Node m = null;
			int ix_min = -1; // destination index
			float d_min = DistanceMatrix.D_INF; // min(distance(n,m))

			n = mat.nodes[i];


			/** Find matching node **/

			// find the closest node to n to connect
			for (int j = 0; j < s + 1; j++) {
				// skip candidate if ...
				if (i == j || /* it's myself */
					mat.dm[i][j] == DistanceMatrix.D_INF || /* it is at extreme distance */
					mat.dm[i][s] < mat.dm[j][s] || /* it is farther from origo then the given */
					mat.dm[i][s] < mat.dm[i][j] || /* it is farther from the given than origo */
					cmat[ mat.t[i] ][ mat.t[j] ] /* types are cannot be connected */
				)
					continue;

				n2 = j == s ? _origo : mat.nodes[j];
				// find the minimal weight
				// check targed node is not yet painted
				if (mat.dm[i][j] < d_min) {
					d_min = mat.dm[i][j]; // update min(distance)
					ix_min = j; // update index
					// remember destination node
					m = n2;
				}
			}

			/** Connect nodes **/

			// closest node 'm' found with distance 'd'
			// establish edge 'n'->'m'
			if (m != null) {
				// Log result
				LOGGER.fine("Link(" + edges.size() + ") " + n + " -> " + m);

				Link li = new Link(0, n, m); // TODO: type: 0: AR, 1: CR
				if (n.getType() == NodeType.GLOBAL_CONTROLLER.ordinal()
						&& m.getType() == NodeType.OUT.ordinal()) {
					li.setHidden(true);
				}
				edges.add(li);

				// update matrix, kick out used up slots
				mat.dm[i][ix_min] = mat.dm[ix_min][i] = DistanceMatrix.D_INF;
				mat.markDirty();
			}
		}

		return true;
	}


	public void build(AudioGraph ag) {
		Set<Link> edges = new HashSet<Link>(/*ag.edges*/);

		// at this stage DM should exist and be complete
		buildAudioGraph(ag.dm, edges);
		
		ag.updateEdges(edges);
	}


	private static boolean canConnect(final int t_start, final int t_end) {
		if ((t_start == NodeType.GENERATOR.ordinal() && (t_end == NodeType.EFFECT.ordinal() || t_end == NodeType.OUT.ordinal()))
				|| (t_start == NodeType.EFFECT.ordinal() && (t_end == NodeType.EFFECT.ordinal() || t_end == NodeType.OUT.ordinal()))
				|| (t_start == NodeType.CONTROLLER.ordinal() && (t_end == NodeType.GENERATOR.ordinal() || t_end == NodeType.EFFECT.ordinal()))
				|| (t_start == NodeType.GLOBAL_CONTROLLER.ordinal() && (t_end == NodeType.GENERATOR.ordinal() || t_end == NodeType.OUT.ordinal())))
			return true;

		return false;
	}
}
