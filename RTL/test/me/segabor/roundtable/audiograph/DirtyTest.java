package me.segabor.roundtable.audiograph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import me.segabor.roundtable.audiograph.data.Link;
import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeKey;
import me.segabor.roundtable.audiograph.data.NodeType;

import org.junit.Test;


public class DirtyTest {
	private final static NodeKey TEST1 = new NodeKey(NodeType.OUT, 65536+0L);
	private final static NodeKey TEST2 = new NodeKey(NodeType.OUT, 65536+1L);
	
	@Test
	public void testBasic() {
		// check node
		{
			Node n = new Node(TEST1);
			
			assertFalse(n.isDirty());
			n.markDirty();
			assertTrue(n.isDirty());
			n.cleanup();
			assertFalse(n.isDirty());
		}
		
		// check edge
		{
			Node n1 = new Node(TEST1);
			Node n2 = new Node(TEST2);
			Link edge = new Link(0, n1, n2);
			
			assertFalse(n1.isDirty());
			assertFalse(n2.isDirty());
			assertFalse(edge.isDirty());
			
			edge.markDirty();
			assertFalse(n1.isDirty());
			assertFalse(n2.isDirty());
			assertTrue(edge.isDirty());
			
			edge.cleanup();
			assertFalse(n1.isDirty());
			assertFalse(n2.isDirty());
			assertFalse(edge.isDirty());
			
			// -- taint a (sub)node
			n1.markDirty();
			assertTrue(n1.isDirty());
			assertFalse(n2.isDirty());
			assertTrue(edge.isDirty());
			
			edge.markDirty();
			assertTrue(n1.isDirty());
			assertFalse(n2.isDirty());
			assertTrue(edge.isDirty());

			// cleanup restores only internal state
			//   and does not fix sub-node states
			edge.cleanup();
			assertTrue(n1.isDirty());
			assertFalse(n2.isDirty());
			assertTrue(edge.isDirty());
		}
	}
}
