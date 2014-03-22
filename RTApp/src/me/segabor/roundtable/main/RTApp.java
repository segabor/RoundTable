package me.segabor.roundtable.main;

import java.util.List;

import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeKey;
import me.segabor.roundtable.audiograph.logic.AudioGraphBuilder;
import me.segabor.roundtable.ui.Surface;
import processing.core.PApplet;
import processing.core.PVector;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;


public class RTApp extends PApplet {
	private static final long serialVersionUID = 1L;

	private Surface surface;
	
	private TuioClient tuioClient;
	
	
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
			tuioClient.addTuioListener(_Listener);
		} else {
			// WHAT?
			System.exit(1);
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
	class Listener implements TuioListener {
		List<TuioContainer> x;
		
		@Override
		public void addTuioCursor(TuioCursor cur) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void addTuioObject(TuioObject obj) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void refresh(TuioTime time) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void removeTuioCursor(TuioCursor cur) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void removeTuioObject(TuioObject obj) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void updateTuioCursor(TuioCursor cur) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void updateTuioObject(TuioObject obj) {
			// TODO Auto-generated method stub
			
		}
	}
	Listener _Listener = new Listener();
}
