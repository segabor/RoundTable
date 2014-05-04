package me.segabor.roundtable.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.segabor.roundtable.audiograph.data.AudioGraph;
import me.segabor.roundtable.audiograph.data.Dirty;
import me.segabor.roundtable.audiograph.data.Link;
import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeFactory;
import me.segabor.roundtable.audiograph.data.NodeKey;
import me.segabor.roundtable.audiograph.logic.AudioGraphBuilder;
import me.segabor.roundtable.ui.DrawContext;
import me.segabor.roundtable.ui.EdgeDrawer;
import me.segabor.roundtable.ui.NodeDrawer;
import me.segabor.roundtable.ui.Surface;
import processing.core.PApplet;
import processing.core.PVector;
import TUIO.TuioClient;
import TUIO.TuioContainer;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;


public class RTApp extends PApplet {
	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = Logger.getLogger(RTApp.class.getName());

	private TuioClient tuioClient;

	/**
	 * TUIO listeners
	 */
	private Listener _listener = new Listener();

	/**
	 * The center knob
	 */
	Node globalOut;

	AudioGraphBuilder builder = new AudioGraphBuilder();

	AudioGraph ag;
	
	@Override
	public void setup() {
		// Setup models / audio graph
		LOGGER.fine("[INIT#1] Setup models / audio graph");
		{
			globalOut = new Node(NodeKey.NKEY_ORIGO);
			globalOut.setLabel("O");
			globalOut.setCoords(new PVector(0.5f, 0.5f));
	
			// FIXME this is ugly!
			ag = new AudioGraph(globalOut, new HashSet<Node>());
		}


		// Setup draw context
		LOGGER.fine("[INIT#2] Setup draw context");
		{
			DrawContext dctx = new DrawContext();
	
			Surface surface = new Surface(800, 600);
			size((int) surface.tableWidth, (int) surface.tableHeight);
			dctx.gfx = this;
			dctx.surface = surface;
			
			NodeDrawer.initContext(dctx);
			EdgeDrawer.initContext(dctx);
		}
		

		// Setup TUIO listener
		LOGGER.fine("[INIT#3] Setup TUIO listener");
		{
			tuioClient = new TuioClient();
			
			tuioClient.connect();
			if (tuioClient.isConnected()) {
				tuioClient.addTuioListener(_listener);
			} else {
				LOGGER.log(Level.SEVERE, "Failed to kick off TUIO listeren, aborting!");
				System.exit(1);
			}
		}
		
		LOGGER.fine("[INIT] Initialization finished.");
	}
	
	
	@Override
	public void draw() {
		// handle input events
		handleInput();

		// the real draw phase
		
		// L1. background
		background(51);

		// L2. center node (FIXME: perhaps draw over edges?)
		NodeDrawer.draw(globalOut);

		// L3. audio graph - edges
		// draw links
		for (Link l : ag.edges) {
			LOGGER.finer("Draw edge " + l);
			EdgeDrawer.draw(l);
		}

		// L4. audio graph - nodes
		// draw nodes
		for (Node n : ag.nodes) {
			LOGGER.finer("Draw node " + n);
			NodeDrawer.draw(n);
		}
	}


	private void handleInput() {
		InputEvents result = _listener.getEvents();
		if (result.isEmpty())
			return;
		
		if (result.reset) {
			// TODO reset global state
			// reinitalize audio graph
			LOGGER.fine("Received reset event, re-create graph ");
			ag = new AudioGraph(globalOut, new HashSet<Node>());
		}
		

		// Process object events, make changes to node set
		Node n;
		int dc = 0; // dirty counter
		for (TuioObject obj : result.objectEvents) {
			n = NodeFactory.getNode(obj);
			switch(obj.getTuioState()) {
			case TuioContainer.TUIO_REMOVED:
				// Remove object
				LOGGER.finer(">> Remove " + n.getKey());
				if (ag.nodes.remove(n)) {
					// graph has been reduced --> mark graph dirty
					ag.markDirty(); dc++;
					
					// when fiducial object gets removed from the surface
					// remove the internal representation from the cache as well
					NodeFactory.invalidate(n);
				}
				break;
			case TuioContainer.TUIO_ADDED:
			default:
				// add node and fall through default case
				if (ag.nodes.add(n)) {
					LOGGER.finer(">> Add " + n.getKey());
					// new node added --> mark graph dirty
					n.setState(obj);
					ag.markDirty(); dc++;
				} else {
					LOGGER.finer(">> Modify " + n.getKey());
					n.setState(obj);
					n.markDirty();  dc++;// <-- mark the node itself dirty (node state changed)
				}
			}
		}


		if (dc > 0 /* ag.isDirty()*/) {
			// recalculate things
			LOGGER.fine("Graph got dirty, initiate recalculate");
			LOGGER.finer("DC = " + dc);

			// update distance matrix according to latest node changes
			ag.calculate();
			// rebuild audio tree
			//   practically, reconnect edges
			builder.build(ag);


			// Cleanup dirty objects
			{
				// cleanup children
				for (Dirty d : ag.nodes) {
					if (d.isDirty()) {
						d.cleanup();
					}
				}
				
				/* AT THIS POINT NO EDGE GETS DIRTY
				 * 
				for (Dirty d : ag.edges) {
					if (d.isDirty()) {
						d.cleanup();
					}
				}
				*/
				
				// cleanup graph object itself
				ag.cleanup();
			}
		}
	}
	
	
	/**
	 * The entry point
	 * @param args
	 */
	public static void main(String[] args) {
		// full screen param --present
		PApplet.main(new String[] { RTApp.class.getCanonicalName() });
	}


	// ---- TUIO EVENTS ----
	public static class InputEvents {
		public boolean reset;
		public List<TuioObject> objectEvents;
		public List<TuioCursor> cursorEvents;
		
		public boolean isEmpty() {
			return (objectEvents == null || objectEvents.size() == 0)
					&& (cursorEvents == null || cursorEvents.size() == 0);
		}
	}

	
	class Listener implements TuioListener {
		private boolean resetEvents = false;

		// max session ids
		private final int MAX_IDS = 32;
		// session ID -> symbol ID
		private int[] ids = new int[MAX_IDS];

		private List<TuioObject> objectEvents = new ArrayList<TuioObject>();
		private List<TuioCursor> cursorEvents = new ArrayList<TuioCursor>();

		public Listener() {
			// reset symbol values
			for (int i=0; i<MAX_IDS; i++) {
				ids[i] = -1;
			}
		}

		/**
		 * Event sink
		 * @param ev
		 */
		private void processSessionId(TuioContainer ev) {
			// Session ID is a unique id starting from zero
			final int sessId = (int) ev.getSessionID();
			// Symbol ID determines a Fiducial ID (fingers do not have symbol IDs)
			final int symId;
			
			if (ev instanceof TuioObject) {
				symId = ((TuioObject) ev).getSymbolID();
			} else {
				symId = 65535; // fake ID - must be bigger than any Symbol IDs
			}

			if (sessId >= MAX_IDS) {
				// TBD: session ID ran out of our limited range, don't care
				return;
			}

			if (ids[sessId] == -1) {
				ids[sessId] = symId;
			} else if (ids[sessId] != symId) {
				// reset state!
				// drop events collected so far
				LOGGER.info("RESET EVENTS");
				resetEvents = true;
				
				objectEvents.clear();
				cursorEvents.clear();

				// reset symbol values
				for (int i=0; i<MAX_IDS; i++) {
					ids[i] = -1;
				}

				ids[sessId] = symId;
			}
		}

		private void resetSessionId(TuioContainer ev) {
			// Session ID is a unique id starting from zero
			final int sessId = (int) ev.getSessionID();

			ids[sessId] = -1;
		}
		
		
		/**
		 * The public event collection getter method
		 * @return
		 */
		public synchronized InputEvents getEvents() {
			InputEvents result = new InputEvents();

			// setup result set
			result.reset = resetEvents;
			if (objectEvents.size() == 0) {
				result.objectEvents = Collections.emptyList();
			} else {
				result.objectEvents = objectEvents;
				objectEvents = new ArrayList<TuioObject>();
			}
			if (cursorEvents.size() == 0) {
				result.cursorEvents = Collections.emptyList();
			} else {
				result.cursorEvents = cursorEvents;
				cursorEvents = new ArrayList<TuioCursor>();
			}

			// clear in queue
			resetEvents = false;

			return result;
		}
		
		
		@Override
		public void addTuioCursor(TuioCursor cur) {
			synchronized (this) {
				// processSessionId(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void addTuioObject(TuioObject obj) {
			synchronized (this) {
				// processSessionId(obj);
				objectEvents.add(obj);
			}
		}



		@Override
		public void refresh(TuioTime time) {
			// TODO Auto-generated method stub
		}



		@Override
		public void removeTuioCursor(TuioCursor cur) {
			synchronized (this) {
				// resetSessionId(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void removeTuioObject(TuioObject obj) {
			synchronized (this) {
				// resetSessionId(obj);
				objectEvents.add(obj);
			}
		}



		@Override
		public void updateTuioCursor(TuioCursor cur) {
			synchronized (this) {
				// processSessionId(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void updateTuioObject(TuioObject obj) {
			synchronized (this) {
				// processSessionId(obj);
				objectEvents.add(obj);
			}
		}
	}
}
