package me.segabor.roundtable.ui.controls;

import java.util.logging.Logger;

import me.segabor.roundtable.app.Tick;
import me.segabor.roundtable.audiograph.data.Node;
import me.segabor.roundtable.audiograph.data.NodeKey;
import me.segabor.roundtable.audiograph.data.NodeSubtype;
import me.segabor.roundtable.audiograph.data.NodeType;
import me.segabor.roundtable.ui.DrawContext;
import me.segabor.roundtable.ui.NodeDrawer;
import me.segabor.roundtable.ui.Surface;
import processing.core.PApplet;
import processing.core.PVector;

public class OscillatorTest extends PApplet {
	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = Logger.getLogger(OscillatorTest.class.getSimpleName());
	
	DrawContext dctx;

	Node node;
	
	@Override
	public void setup() {
		// Setup draw context
		LOGGER.fine("[INIT#1] Setup draw context");
		{
			dctx = new DrawContext();
	
			Surface surface = new Surface(800, 600);
			size((int) surface.tableWidth, (int) surface.tableHeight);
			dctx.gfx = this;
			dctx.surface = surface;
			
			// timing
			dctx.tick = new Tick( System.currentTimeMillis(), 120, 4 );

			// init drawer modules
			// TableTopDrawer.initContext(dctx);
			NodeDrawer.initContext(dctx);
			// EdgeDrawer.initContext(dctx);
		}

		NodeType _t  = NodeType.CONTROLLER;
		node = new Node(new NodeKey(_t, 0xdeadbabeL), _t, NodeSubtype.OSCILLATOR);

		node.setCoords(new PVector(0.5f, 0.5f));
		
		// TODO Auto-generated method stub
		// super.setup();
	}


	/**
	 * The entry point
	 * @param args
	 */
	public static void main(String[] args) {
		// full screen param --present
		PApplet.main(new String[] { OscillatorTest.class.getCanonicalName() });
	}


	@Override
	public void draw() {
		// TODO Auto-generated method stub
		// super.draw();
		NodeDrawer.draw(node);
	}
	
	@Override
	public void mousePressed() {
		// TODO Auto-generated method stub
		// super.mousePressed();
		LOGGER.info("PRESSED");
	}
	
	@Override
	public void mouseDragged() {
		// TODO Auto-generated method stub
		//super.mouseDragged();
		LOGGER.info("DRAGGED: " + mouseX + ":" + mouseY);

	}

	@Override
	public void mouseReleased() {
		// TODO Auto-generated method stub
		// super.mouseReleased();
		LOGGER.info("RELEASED");

	}
}
