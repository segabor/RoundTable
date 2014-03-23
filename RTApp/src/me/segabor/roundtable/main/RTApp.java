package me.segabor.roundtable.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeKey;
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

	private Surface surface;
	
	private TuioClient tuioClient;
	
	private Listener _listener = new Listener();

	/**
	 * The center knob
	 */
	Node globalOut;

	AudioGraphBuilder builder = new AudioGraphBuilder();

	
	@Override
	public void setup() {
		// core setup
		surface = new Surface(800, 600);

		
		globalOut = new Node(NodeKey.NKEY_ORIGO);
		globalOut.setLabel("O");
		globalOut.setCoords(new PVector(surface.halfWidth, surface.halfHeight));


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
		_listener.getEvents();
		
		super.draw();
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
	}

	
	class Listener implements TuioListener {
		private long currentSession = -1L;
		private boolean sessionReset = false;

		private List<TuioObject> objectEvents = new ArrayList<TuioObject>();
		private List<TuioCursor> cursorEvents = new ArrayList<TuioCursor>();

		/**
		 * Event sink
		 * @param ev
		 */
		private void processEvent(TuioContainer ev) {
			final long l = ev.getSessionID();
			
			if (currentSession == -1L) {
				// FIRST (NEW) SESSION
				currentSession = l;
			} else {
				sessionReset = !(currentSession == l); 
				if (sessionReset) {
					// drop events collected so far
					objectEvents.clear();
					cursorEvents.clear();
				}
				// record current id
				currentSession = l;					
			}
		}


		/**
		 * The public event collection getter method
		 * @return
		 */
		public synchronized InputEvents getEvents() {
			InputEvents result = new InputEvents();

			// setup result set
			result.reset = sessionReset;
			result.objectEvents = Collections.unmodifiableList(objectEvents);
			result.objectEvents = Collections.unmodifiableList(objectEvents);

			// clear in queue
			objectEvents.clear();
			cursorEvents.clear();

			return result;
		}
		
		
		@Override
		public void addTuioCursor(TuioCursor cur) {
			synchronized (this) {
				processEvent(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void addTuioObject(TuioObject obj) {
			synchronized (this) {
				processEvent(obj);
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
				processEvent(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void removeTuioObject(TuioObject obj) {
			synchronized (this) {
				processEvent(obj);
				objectEvents.add(obj);
			}
		}



		@Override
		public void updateTuioCursor(TuioCursor cur) {
			synchronized (this) {
				processEvent(cur);
				cursorEvents.add(cur);
			}
		}



		@Override
		public void updateTuioObject(TuioObject obj) {
			synchronized (this) {
				processEvent(obj);
				objectEvents.add(obj);
			}
		}
	}
}
