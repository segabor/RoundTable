package me.segabor.roundtable.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import me.segabor.roundtable.audiograph.data.AudioGraph;
import me.segabor.roundtable.audiograph.data.Dirty;
import me.segabor.roundtable.audiograph.data.Link;
import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeFactory;
import me.segabor.roundtable.audiograph.data.NodeKey;
import me.segabor.roundtable.audiograph.data.NodeType;
import me.segabor.roundtable.audiograph.logic.AudioGraphBuilder;
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

	private Surface surface;
	
	private TuioClient tuioClient;
	
	private Listener _listener = new Listener();

	/**
	 * The center knob
	 */
	Node globalOut;

	AudioGraphBuilder builder = new AudioGraphBuilder();

	AudioGraph ag;
	
	@Override
	public void setup() {
		// core setup
		surface = new Surface(800, 600);

		// -- initialize global state --
		
		globalOut = new Node(NodeKey.NKEY_ORIGO);
		globalOut.setLabel("O");
		globalOut.setCoords(new PVector(surface.halfWidth, surface.halfHeight));

		// FIXME this is ugly!
		ag = new AudioGraph(globalOut, new HashSet<Node>());



		// set table top
		size((int) surface.tableWidth, (int) surface.tableHeight);

		// set TUIO events
		tuioClient = new TuioClient();
		
		tuioClient.connect();
		if (tuioClient.isConnected()) {
			tuioClient.addTuioListener(_listener);
		} else {
			// WHAT?
			System.exit(1);
		}
	}
	
	
	@Override
	public void draw() {
		// handle input events
		handleInput();
		
		// draw nodes
		
		// L1. background
		background(51);

		// L2. center node (FIXME: perhaps draw over edges?)
		drawNode(globalOut);

		// L3. audio graph - edges
		// draw links
		// DEBUG System.out.println("Will draw " + linx.size() + " lines...");
		for (Link l : ag.edges) {
			// DRAW
			// pushMatrix();

			PVector p0 = l.getIn().getCoords(), p1 = l.getOut().getCoords();

			stroke(255, 255, 255);
			strokeWeight(1);
			// System.out.println("Connect " + p0.x + ";" + p0.y + " with " +
			// p1.x + ";" + p1.y);
			line(p0.x, p0.y, p1.x, p1.y);

			// arrow head
			pushMatrix();
			translate(p1.x, p1.y);
			float a = atan2(p0.x - p1.x, p1.y - p0.y /* x1-x2, y2-y1 */);
			rotate(a);
			line(0, 0, -10, -20);
			line(0, 0, 10, -20);
			popMatrix();

			// popMatrix();
		}

		// L4. audio graph - nodes
		// draw nodes
		for (Node n : ag.nodes) {
			drawNode(n);
		}
	}

	
	private void drawNode(Node n) {
		final float RW = surface.tableWidth;
		final float RH = surface.tableHeight;
		
		pushMatrix();
		rectMode(CENTER);

		switch(n.getKey().getType()) {
		case OUT:
			// center node
			translate(RW / 2, RH / 2);
			fill(255, 255, 255);
			ellipse(0, 0, 10, 10);
			break;
		case GENERATOR:
			// generator - SQUARE
			translate(RW * n.getCoords().x, RH * n.getCoords().y);
			rotate(n.getAngle());
			rect(0, 0, 40, 40);
			break;
		case CONTROLLER:
			// controller - CIRCLE
			translate(RW * n.getCoords().x, RH * n.getCoords().y);
			ellipse(0, 0, 40, 40);
			break;
		case EFFECT:
			// effect - ROUNDED SQUARE
			translate(RW * n.getCoords().x, RH * n.getCoords().y);
			rotate(n.getAngle());
			rect(0, 0, 40, 40, 5);
			break;
		case GLOBAL_CONTROLLER:
			break;
		default:
			break;
		}

		popMatrix();
	}


	private void handleInput() {
		InputEvents result = _listener.getEvents();
		if (result.isEmpty())
			return;
		
		if (result.reset) {
			// TODO reset global state
			// reinitalize audio graph
			LOGGER.info("Received reset event, re-create graph ");
			ag = new AudioGraph(globalOut, new HashSet<Node>());
		}
		
		// -- manipulate node set here --
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
				}
				break;
			case TuioContainer.TUIO_ADDED:
				// add node and fall through default case
				if (ag.nodes.add(n)) {
					LOGGER.finer(">> Add " + n.getKey());
					// new node added --> mark graph dirty
					ag.markDirty(); dc++;
				}
			default:
				// Carry through state
				LOGGER.finer(">> Modify " + n.getKey());
				n.setState(obj);
				n.markDirty();  dc++;// <-- mark the node itself dirty (node state changed)
			}
		}


		if (dc > 0 /* ag.isDirty()*/) {
			// recalculate things
			LOGGER.fine("Graph got dirty, initiate recalculate");
			LOGGER.finer("DC = " + dc);

			// update distance matrix
			ag.calculate();
			// calculate edges
			builder.build(ag);

			// CLEANUP PHASE
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
			objectEvents.clear();
			cursorEvents.clear();

			resetEvents = false;

			return result;
		}
		
		
		@Override
		public void addTuioCursor(TuioCursor cur) {
			synchronized (this) {
				processSessionId(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void addTuioObject(TuioObject obj) {
			synchronized (this) {
				processSessionId(obj);
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
				resetSessionId(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void removeTuioObject(TuioObject obj) {
			synchronized (this) {
				resetSessionId(obj);
				objectEvents.add(obj);
			}
		}



		@Override
		public void updateTuioCursor(TuioCursor cur) {
			synchronized (this) {
				processSessionId(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void updateTuioObject(TuioObject obj) {
			synchronized (this) {
				processSessionId(obj);
				objectEvents.add(obj);
			}
		}
	}
}
