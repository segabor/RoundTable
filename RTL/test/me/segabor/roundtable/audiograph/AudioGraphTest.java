package me.segabor.roundtable.audiograph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.segabor.roundtable.audiograph.data.DistanceMatrix;
import me.segabor.roundtable.audiograph.data.Link;
import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeKey;
import me.segabor.roundtable.audiograph.data.NodeType;
import me.segabor.roundtable.audiograph.logic.AudioGraphBuilder;

import org.junit.Before;
import org.junit.Test;

import processing.core.PVector;

public class AudioGraphTest {
	private AudioGraphBuilder builder;
	private Node globalOut;
	
	private static final PVector ORIGO = new PVector(0,0);
	
	@Before
	public void init() {
		
		globalOut = new Node(NodeKey.NKEY_ORIGO);
		globalOut.setLabel("O");
		globalOut.setCoords(ORIGO);

		builder = new AudioGraphBuilder();
	}


	/**
	 * Simple cases
	 */
	@Test
	public void testSimple() {
		Node n;
		List<Node> nodes = new ArrayList<Node>();

		// Generator
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.GENERATOR, 1l));
			n.setLabel("Generator");
			n.setCoords(new PVector(10,0));
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(1, result.size());
			assertTrue(findEdge("Generator", "O", result));
			assertTrue(!result.iterator().next().isHidden());
		}
		
		// Effect
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.EFFECT, 1l));
			n.setLabel("Effect");
			n.setCoords(new PVector(10,0));
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(1, result.size());
			assertTrue(findEdge("Effect", "O", result));
			assertTrue(!result.iterator().next().isHidden());
		}

		
		// Controller
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.CONTROLLER, 1l));
			n.setLabel("Controller");
			n.setCoords(new PVector(10,0));
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(0, result.size());
		}

		// Global Controller
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.GLOBAL_CONTROLLER, 1l));
			n.setLabel("GlobalController");
			n.setCoords(new PVector(10,0));
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(1, result.size());
			assertTrue(findEdge("GlobalController", "O", result));
			assertTrue(result.iterator().next().isHidden());
		}
	}

	/**
	 * Simple cases
	 */
	@Test
	public void testDisabled() {
		Node n;
		List<Node> nodes = new ArrayList<Node>();

		// Generator
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.GENERATOR, 1l));
			n.setLabel("Generator");
			n.setCoords(new PVector(10,0));
			n.setDisabled(true);
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(0, result.size());
		}
		
		// Effect
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.EFFECT, 1l));
			n.setLabel("Effect");
			n.setCoords(new PVector(10,0));
			n.setDisabled(true);
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(0, result.size());
		}

		
		// Controller
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.CONTROLLER, 1l));
			n.setLabel("Controller");
			n.setCoords(new PVector(10,0));
			n.setDisabled(true);
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(0, result.size());
		}

		// Global Controller
		{
			nodes.clear();
			n = new Node(new NodeKey(NodeType.GLOBAL_CONTROLLER, 1l));
			n.setLabel("GlobalController");
			n.setCoords(new PVector(10,0));
			n.setDisabled(true);
			
			nodes.add(n);
			Set<Link> result = buildEdges(nodes);
			
			assertEquals(0, result.size());
		}
	}
	
	
	@Test
	public void testExample1() {
		int k = 1;
		
		// generators
		final Node g1 = new Node(new NodeKey(NodeType.GENERATOR, (long) k++)); g1.setLabel("G1");
		g1.setCoords(new PVector(5, -1));
		final Node g2 = new Node(new NodeKey(NodeType.GENERATOR, (long) k++)); g2.setLabel("G2");
		g2.setCoords(new PVector(-2.5f, -2.5f));
		final Node g3 = new Node(new NodeKey(NodeType.GENERATOR, (long) k++)); g3.setLabel("G3");
		g3.setCoords(new PVector(-3, 0));
		
		// effects
		final Node e1 = new Node(new NodeKey(NodeType.EFFECT, (long) k++)); e1.setLabel("E1");
		e1.setCoords(new PVector(0, 1.5f));
		final Node e2 = new Node(new NodeKey(NodeType.EFFECT, (long) k++)); e2.setLabel("E2");
		e2.setCoords(new PVector(1.5f, -1));
		final Node e3 = new Node(new NodeKey(NodeType.EFFECT, (long) k++)); e3.setLabel("E3");
		e3.setCoords(new PVector(3, 0));

		// controllers
		final Node c1 = new Node(new NodeKey(NodeType.CONTROLLER, (long) k++)); c1.setLabel("C1");
		c1.setCoords(new PVector(1, 3));

		List<Node> nodes = new ArrayList<Node>(7);
		nodes.add(g1);
		nodes.add(g2);
		nodes.add(g3);
		nodes.add(e1);
		nodes.add(e2);
		nodes.add(e3);
		nodes.add(c1);

		Set<Link> result = buildEdges(nodes);

		assertEquals(7, result.size());
		
		assertTrue(findEdge("C1", "E1", result));
		assertTrue(findEdge("E1", "O", result));
		assertTrue(findEdge("G2", "O", result));
		assertTrue(findEdge("G3", "O", result));
		assertTrue(findEdge("G1", "E3", result));
		assertTrue(findEdge("E3", "E2", result));
		assertTrue(findEdge("E2", "O", result));
	}

	@Test
	public void testEmpty() {
		Set<Link> edges = buildEdges(Collections.<Node>emptyList());
		
		assertTrue(edges.size() == 0);
	}

	
	private Set<Link> buildEdges(Collection<Node> nodes) {
		DistanceMatrix dm = new DistanceMatrix(globalOut, nodes);
		Set<Link> edges = new HashSet<Link>();

		builder.buildAudioGraph(dm, edges);

		return edges;
	}
	

	private boolean findEdge(String startTag, String endTag, Collection<Link> edges) {
		for (Link link : edges) {
			if (	startTag.equalsIgnoreCase( link.getIn().getLabel()) &&
					endTag.equalsIgnoreCase( link.getOut().getLabel())
			) {
				return true;
			}
		}
		return false;
	}
}
