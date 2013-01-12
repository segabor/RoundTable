package me.segabor.roundtable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements TuioListener {
	private static final String LOG_NAME = MainActivity.class.getSimpleName();

	private GLSurfaceView glView;
	private Triangle triangle1;
	private Triangle triangle2;

	private TuioClient receiver;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		glView = new GLSurfaceView(this);
		
		// configure gl view
		glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 8 );
		// glView.setEGLContextClientVersion(2);

		glView.setRenderer(new MyOpenGLRenderer());
		setContentView(glView);

		// instantiate tuio receiver
		receiver = new TuioClient();
		receiver.addTuioListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// start listening to TUIO events
		receiver.connect();
	}

	@Override
	protected void onPause() {
		// stop listening to events
		receiver.disconnect();

		super.onPause();
	}
	
	
	class MyOpenGLRenderer implements Renderer {

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Log.d(LOG_NAME, "Surface changed. Width="
					+ width + " Height=" + height);

			triangle1 = new Triangle(0.5f, 1, 0, 0);
			triangle2 = new Triangle(0.5f, 0, 1, 0);
			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
					100.0f);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			
			gl.glLoadIdentity();
			gl.glTranslatef(0.0f, 0.0f, -5.0f);
			triangle1.draw(gl);
			gl.glTranslatef(2.0f, 0.0f, -5.0f);
			triangle2.draw(gl);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			Log.d(LOG_NAME, "Surface created");
		}
	}

	
	// ---- TUIO Events --- //
	
	@Override
	public void addTuioObject(TuioObject tobj) {
		Log.d(LOG_NAME, "ADD");
	}

	@Override
	public void updateTuioObject(TuioObject tobj) {
		Log.d(LOG_NAME, "UPDATE");
	}

	@Override
	public void removeTuioObject(TuioObject tobj) {
		Log.d(LOG_NAME, "REMOVE");
	}

	@Override
	public void addTuioCursor(TuioCursor tcur) {
		Log.d(LOG_NAME, "ADD CUR");
	}

	@Override
	public void updateTuioCursor(TuioCursor tcur) {
		Log.d(LOG_NAME, "UPDATE CUR");
	}

	@Override
	public void removeTuioCursor(TuioCursor tcur) {
		Log.d(LOG_NAME, "REM CUR");
	}

	@Override
	public void refresh(TuioTime ftime) {
		Log.d(LOG_NAME, "REFRESH T");
	}
}
