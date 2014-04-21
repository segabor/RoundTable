package me.segabor.roundtable.audiograph.data;

import java.util.Collection;

import processing.core.PVector;

public class DistanceMatrix implements Dirty {
	/**
	 * Infinity value
	 */
	public static final float D_INF = Float.POSITIVE_INFINITY;


	public final Node origo;
	public final Node[] nodes;
	
	private boolean dirty = false;

	/**
	 * Distance matrix indexed by the order of nodes in 
	 */
	public final float dm[][];

	/**
	 * Index to Type map t[N]->node type
	 * 
	 * @see {@link NodeType}
	 */
	public final int t[];
	
	/**
	 * Map of type -> matrix rows / col indices where t2x[type][0] = length
	 * of array t2x[type][1] = first index t2x[type][2] = second index ...
	 */
	public final int t2x[][];

	/**
	 * Ordered list of nodes by their distance to origo.
	 * Farthest comes first.
	 */
	public final int[] distList;



	public DistanceMatrix(Node origo, Collection<Node> nodeList) {
		this.origo = origo;
		
		int i = 0;
		nodes = new Node[nodeList.size()];
		for (Node n : nodeList) {
			this.nodes[i++] = n;
		}

		// -- init vars --
		{
			final int s = nodes.length;
			dm = new float[s + 1][s + 1];
			
			t = new int[s + 1]; // node(i) types
			for (int k = 0; k < s; k++) {
				t[k] = nodes[k].getType();
			}
			t[s] = NodeType.OUT.ordinal(); // expand type array
			
			
			t2x = new int[NodeType.values().length][s + 1];
			for (int k = 0; k < t2x.length; k++) {
				t2x[k][0] = 0;
			}

			distList = new int[s];
		}
		// -- end init --
		
		build(true);
	}

	/**
	 * DM is dirty if the matrix itself is modified since last generation
	 * or one of node is dirty.
	 */
	@Override
	public boolean isDirty() {
		if (dirty)
			return true;
		
		// check nodes
		for (Dirty n : nodes) {
			if (n.isDirty())
				return true;
		}

		return false;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
	}


	public Node getOrigo() {
		return origo;
	}

	public int size() {
		return nodes.length;
	}



	/**
	 * Rebuild matrix if necessary
	 */
	public void build() {
		build(false);
	}
	
	
	private void build(boolean force) {
		if (dirty || force) {
			final int s = nodes.length;

			Node n, n2;
			
			/**
			 * Calculate Distance Matrix
			 */
			final PVector O = origo.getCoords();
			for (int j = 0; j < s; j++) {
				n = nodes[j];

				// fill in the type to raw map
				t2x[ t[j] ][0]++;
				t2x[ t[j] ][ t2x[ t[j] ][0] ] = j; // store row index




				// handle disabled node here
				if (n.isDisabled()) {
					for (int k = 0; k<s+1; k++) {
						dm[j][k] = dm[k][j] = D_INF;
					}

					continue;
				}


				
				// Calculate the upper right triangle
				// of matrix D(j+1 .. s-1)(j)
				//
				//    0...O
				//     0..O
				//      0.O
				//       0O
				//    OOOO0
				//

				dm[j][j] = 0;
				// calc dist(O, node(j))
				dm[s][j] = dm[j][s] = O.dist( n.getCoords() );
				// now the inner part (section marked by dots )
				for (int k = j + 1; k < s; k++) {
					n2 = nodes[k];

					// TODO replace PVectors to plain coordinate pairs
					// and implement my distance function instead of builtin
					dm[j][k] = dm[k][j] = n.getCoords().dist(
							n2.getCoords());
				}
			}
			dm[s][s] = 0;

			
			// ----------------- //

			if (s>0){
				// int j=0; // current length of 'list'
				// TODO Suppose number of nodes is greater than null
				distList[0] = 0;
				for (int k = 1; k < s; k++) {
					float d0 = dm[k][s]; // D(K,O)
					int l = k;
					// step backwards and find th
					while (l > 0 && dm[distList[l - 1]][s] < d0) {
						l--;
					}
					// l == 0 or list[l-1] >= d0
					for (int m = k; m > l; m--) {
						distList[m] = distList[m - 1];
					}
					distList[l] = k;
				}
			}

			// ----------------- //

			// NOTE: this call does not cleanup nodes themselves
			cleanup();
		}
	}


	@Override
	public boolean cleanup() {
		final boolean flag = dirty;
		dirty = false;
		return flag;
	}
}
