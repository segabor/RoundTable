import vialab.SMT.*;
import java.util.Set;
import java.util.HashSet;
import TUIO.TuioObject;

import java.util.Timer;
import java.util.TimerTask;
import java.lang.Runnable;

// SuperCollider dependencies
import supercollider.*;
import oscP5.*;


/*******************************/
/*  Configurations, Constants  */
/*******************************/


// table size
final int RW = 640;
final int RH = 480;
final float EPS = 0.00000001; 

// pulse
final color cPulseNormal = #2222FF;
final color cPulseHighlight = #4444FF;

// object size
final float wBox = 40.0;

// metronome params
int mTempo = 120;
int mBeats = 4;


/******************/
/*  SOUND SYSTEM  */
/******************/

// == SuperCollider ==

// Sample synth
Synth synth, delay;
Bus delaybus;


// OBJECTS
// 0 = CONTROLLERS
//   1 = oscillator (sine wave, sawtooth, square wave, white noise, user created)
// 1 = EFFECTS AND FILTERS
// 2 =  

interface RTObject {
  boolean hasAudioInputs();
  boolean hasAudioOutputs();

  boolean hasControlInputs();
  boolean hasControlOutputs();
}

// central knob
class RTCenter implements RTObject {
  boolean hasAudioInputs() {return true;}
  boolean hasAudioOutputs() {return false;}

  boolean hasControlInputs() {return true;}
  boolean hasControlOutputs() {return false;}
}

abstract class RTController implements RTObject {
}

// oscillator types
final int OSC_SINE_WAVE = 0;
final int OSC_SAWTOOTH = 1;
final int OSC_WHITE_NOISE = 2;
final int OSC_USER = 3;

class RTOscillator extends RTController {
  int type = OSC_SINE_WAVE;

  boolean hasAudioInputs() {return false;}
  boolean hasAudioOutputs() {return true;}

  boolean hasControlInputs() {return true;}
  boolean hasControlOutputs() {return false;}
}

// temporary solution
boolean isGenEnabled = false;
int _oct = 3;
int _tun = 4;
float _amp = 0.5; // 0..1

// current angle value
float _angle, _last_angle;

// position
float _px, _py;
float _px_last, _py_last;

// rotation value
float _rotation = 0;
float _rot_step = 20.0; // (160 deg / 8 octaves)

// touch zones
Zone ampSlider;
int oscType = 0; // 0 = sin, 1 = sawtooth, 2 = square, 3 = white noise
Zone oscTypeBtn;
boolean _dragged;

// calculates frequency based on octave and tune numbers
// @param o: octave number. Must be between 0 and 7
// @param t: tune number. Must fall between 0 and 11
float calcFreq(int o, int t) {
  if (o < 0 || o > 7 || t < 0 || t > 11) return 0; // ERROR!
  // System.out.println("  OCT: " + o + "; TUNE: " + t);
  float z = (o-3) + ( ((float)t)/12.0);
  // System.out.println("  z: " + z + ", 2**z = " + pow(2, z) + ", val=" + 440.0*pow(2,z) );
  
  return (float) (440.0 * pow(2, z) );
}

// -- //

class PCoord {
  // value in degrees
  public float angle;
  public float mag;
}

/*
 * @param x0, y0 - center of the object
 * @param x, y - an arbitrary point
 */
PCoord getAngle(float x0, float y0, float x, float y) {
  final PVector z = new PVector(1, 0); // directed towards zero angle
  final PVector v = new PVector(x-x0, y-y0);

  PCoord pc = new PCoord();
  pc.mag = v.mag();

  v.normalize();

  // a dot b = |a||b|cos(angle)
  //  -> cos(angle) = (a dot b)/|a||b|
  
  // final float angle = acos( z.dot(v));
  
  pc.angle = degrees( PVector.angleBetween(z, v));
  // pc.angle = degrees( PVector.angleBetween(v, z));
  
  return pc;
}



void enableSineWaveGen() {
  synth.set("amp", _amp);
  final float frq = calcFreq(_oct, _tun);
  // [debug] System.out.println("[E]: " + frq);
  synth.set("freq", frq);

  // create touch zone
  if (ampSlider == null)
    ampSlider = new Zone("ampSlider");
  if (oscTypeBtn == null)
    oscTypeBtn = new Zone("oscTypeButton");
  
  SMT.add(ampSlider);
  SMT.add(oscTypeBtn);
}

void disableSineWaveGen() {
  synth.set("amp", 0.0);

  if (ampSlider != null)
    SMT.remove(ampSlider);
  if (oscTypeBtn != null)
    SMT.remove(oscTypeBtn);

  // [debug] System.out.println("[D]");
}




void doEnableObject(TuioObject to) {
  final int sym = to.getSymbolID();
  // System.out.println("Add / enable object " + sym);
  
  if (sym == 0 && !isGenEnabled) {
    isGenEnabled = true;
    enableSineWaveGen();
  }
}

void doDisableObject(TuioObject to) {
  final int sym = to.getSymbolID();
  // System.out.println("Disable object " + sym);

  if (sym == 0 && isGenEnabled) {
    disableSineWaveGen();
    isGenEnabled = false;
  }
}

void doRemoveObject(TuioObject to) {
  final int sym = to.getSymbolID();
  // System.out.println("Remove object " + sym);

  if (sym == 0 && isGenEnabled) {
    disableSineWaveGen();
    isGenEnabled = false;
  }
}

void doUpdateWithRotation(float r) {
  // FIXME: 8 := max octaves
  final double rot_max = 12*_rot_step;

  int step_dir = 0;
  float _old_rotation = _rotation;
  _rotation += r;
  if (_rotation >= rot_max) {
    _rotation -= rot_max;
    step_dir = 1;
  } else if (_rotation < 0) {
    _rotation += rot_max;
    step_dir = -1;
  }

  if (step_dir == 1) {
    if (_oct < (8-1)) {
      _oct++;
      _tun = (int)( _rotation / _rot_step);
    } else {
      // overflow!
      _rotation = 0;
      _tun = 0;
    }
  } else if (step_dir == -1) {
    if (_oct > 0) {
      _oct--;
      _tun = (int)( _rotation / _rot_step);
    } else {
      // overflow!
      _rotation = (float) (rot_max-1);
      _tun = (int)( _rotation / _rot_step);
    }
  } else if (step_dir == 0) {
    _tun = (int)( _rotation / _rot_step);
  }

  // update frequency
  System.out.println("O: " + _oct + ": T: " + _tun);
  final float frq = calcFreq(_oct, _tun);
  synth.set("freq", frq);
}

// update volume (amplitude)
// @param amp value between 0 and 1
void doUpdateAmplitude(float amp) {
  if (isGenEnabled) {
    _amp = amp;
    synth.set("amp", _amp);
  }
}

// invoked by touch event
// step over the subsequent type
void doChangeOscType() {
  oscType = oscType+1;
  if (oscType > 3)
    oscType = 0;
}

/**************************/
/*  RHYTHM, TEMPO, TICKS  */
/**************************/


// Classes, Types
class Frame {
  // actual time
  public final long start;
  public final int bpm;
  public final int periods;

  public double _stretch;

  // current time in millisecs
  public long tcur;
  public int beats; // number of beats since start
  public int tick; // current tick (0..periods-1)

  public double perc_beat;

  public Frame(long t0, int bpm, int periods) {
    this.start = t0; 
    this.bpm = bpm;
    this.periods = periods;

    // beat "stretch" in millsecs
    this._stretch = 1000.0*( 60.0/((double) this.bpm)); // beat stretch in millisecs
  }

  public Frame(Frame other) {
    this.start = other.start; this.bpm = other.bpm; this.periods = other.periods;
  }

  public void update(final long tcur) {
    this.tcur = tcur;

    // time delta
    int d_ms = (int) (tcur - this.start);

    // beat span in millisecs
    //   60 bpm example:
    //     60 beats = 1 sec
    //     1 beat = 60/60 secs = (60/60)*1000 msecs
    //     120 beats = 1/2 sec
    //     1 beat = 60/120 secs = (120/60)*1000 msecs
    // final double stretch = 1000.0*( 60.0/((double) this.bpm)); // beat stretch in millisecs
    // final double stretch = ( ((double)bpm)*periods*1000.0) / 60.0; // beat stretch in millisecs
    final int ilen = (int) _stretch; // same in integer

    this.beats = d_ms / ilen;
    
    // beat percentage
    // double perc_beat =  ((double) (d_ms - (ilen * this.beats)))/_stretch;
    this.perc_beat =  ((double) (d_ms % ilen))/_stretch;

    this.tick = beats % this.periods;

  }  

  public boolean isCommonBase(Frame other) {
     return this.start == other.start && this.beats == other.beats && this.periods == other.periods;
  }

  public Frame copy(Frame other) {
    if (other == null) {
      other = new Frame(this);
    }
    other.beats = this.beats;
    other.tick = this.tick;
    return other;
  }

  public void debug() {
    System.out.println("BEAT: " + this.beats + " t: " + this.tick);
  }
  
  public int ticks() {
    return ( this.beats * this.periods ) + this.tick;
  }
  
  public boolean isTick(Frame other) {
    return (other != null &&
      /* this.tcur > other.tcur && */
      this.ticks() > other.ticks()
    );
  }
}



/*************/
/*  DRAWING  */
/*************/

// Sine wave drawer
void sineTo(PVector p1, PVector p2, float freq, float amp)
{
  float d = PVector.dist(p1,p2); // distance
  float a = atan2(p2.y-p1.y,p2.x-p1.x); // angle

  noFill();
  pushMatrix();

  translate(p1.x, p1.y);
  rotate(a);
  beginShape();
  for (float i = 0; i <= d; i += 1) {
    vertex(i, amp * sin( (i*TWO_PI*freq) /d) );
  }
  endShape();

  popMatrix();
}



/**********/
/*  MAIN  */
/**********/

// initial bpm : 120, beats: 4 / 4
Frame f = new Frame( System.currentTimeMillis(), mTempo, mBeats );
Frame last_f;

void setup() {
  size(RW,RH, P3D);
  // frameRate(30); // default is 60 fps

  // == Initialize Simple Multi-Touch lib == 
  SMT.init(this, TouchSource.MULTIPLE);

  // == Initialize SuperCollider module ==
  {
    // delaybus = new Bus("audio", 2);
    
    synth = new Synth("sine");
    synth.set("amp", 0.0);
    // synth.set("outbus", delaybus.index);
    synth.create();
  
    /* delay = new Synth("fx_comb");
    delay.set("wet", 0.5);
    delay.set("delaytime", 0.05);
    delay.set("inbus", delaybus.index); */
  
    // delay.addToTail();
  }

  // smooth();
}

void exit()
{
  
  // == Free up SuperCollider resources ==
  synth.free();
  // delay.free();
  // delaybus.free();

  super.exit();
}


void draw() {
  // what is the current time frame (timing, animations)
  last_f = f.copy(last_f);;
  f.update(System.currentTimeMillis());

  // System.out.println("[draw] start");

  /* if (f.isTick(last_f)) {
    f.debug();
  } else {
    System.out.println( (f.perc_beat * 100) + "%");
  } */

  // table diameter
  double border = Math.min(RW,RH);
  // table radius
  float bigR = (float)(border/2);
  float cx = RW/2, cy = RH/2;

  
  // -- draw background -- //
  
  background(#000088);

  noStroke();
  fill(#0000FF);
  ellipse(cx, cy, (float)border, (float)border);

  
  // -- animate beat pulse -- //
  float r_beat = (float)border*log(1.0 +(float)(f.perc_beat));
  if (f.tick == 0) {
    fill(cPulseHighlight);
  } else {
    fill(cPulseNormal);
  }
  ellipse(cx, cy, r_beat, r_beat);
  double ring_width = 0.2 /* f.tick == 0 ? 0.2 : 0.1 */;
  if (f.perc_beat > ring_width) {
    float r_ibeat = (float)border*log( 1.0 + (float)(f.perc_beat-ring_width));
    fill(#0000FF);
    ellipse(cx, cy, (float)r_ibeat, (float)r_ibeat);
  }


  // -- center knob (a.k.a The Output) -- //

  fill(#FFFFFF);
  ellipse(cx, cy, 10, 10);


  // === Features (objects and edges) ===

  fill(#FFFFFF);  
  rectMode(CENTER);
  for (TuioObject obj : SMT.getTuioObjects()) {
    final int _state = obj.getTuioState();
    // System.out.println("S: " + _state);
    
    if (_state == TuioObject.TUIO_REMOVED) {
      doRemoveObject(obj);

      continue;
    } else if (_state == TuioObject.TUIO_STOPPED) {
      // System.out.println("STOPPED");
    } else if (_state == TuioObject.TUIO_ROTATING) {
      System.out.println("[R] " + obj.getRotationSpeed());
      doUpdateWithRotation(obj.getRotationSpeed());      
    } else if (_state == TuioObject.TUIO_ADDED) {
      // doEnableObject(obj);
      _angle = _last_angle = obj.getAngleDegrees();
    }

    _last_angle = _angle;
    _angle = obj.getAngleDegrees();
    
    float _adiff = _angle-_last_angle;
    if ( _adiff > 180) {
      doUpdateWithRotation(_adiff-360);      
    } else if (_adiff < -180) {
      doUpdateWithRotation(_adiff+360);      
    } else if (  Math.abs(_adiff) > 0.00001 ) {
      doUpdateWithRotation(_adiff);      
    } else {
      // System.out.println("[debug] S: " + _state);
    }    

    
    float rx = obj.getX()*RW, ry = obj.getY()*RH;

    // whoami?    
    int sid = obj.getSymbolID();
    float _dx = cx-rx, _dy = cy-ry;
    float distance = (float)Math.sqrt( (_dx*_dx) + (_dy*_dy)); 
    
    // draw edge

    // enable square
    boolean _moved = false;
    if (distance <= bigR) {
      _px_last = _px; _py_last = _py;
      _px = obj.getX()*RW; _py = obj.getY()*RH;
      
      _moved = ( Math.abs(_px_last - _px) < EPS && Math.abs(_py_last - _py) < EPS ); 

      // enable feature
      doEnableObject(obj);

      // distance between center and the box boundary
      float _dminor = distance - (wBox/2);
      
      float _dmx = (_dx*_dminor)/distance,
            _dmy = (_dy*_dminor)/distance;
      
      // draw edge ..
      pushMatrix();     
      stroke(#FFFFFF);
      strokeWeight(1);
      line((float) (cx-_dmx), (float)(cy-_dmy), (float) cx, (float) cy);
      popMatrix();
    } else {
      // disable feature
      doDisableObject(obj);
    }

    
    // -- features (squares) -- //


    pushMatrix();
    translate(_px, _py);
    if (sid == 0) {
      // -- Surrounding controls
      final float pWidth = wBox * 2 /* 16 + (wBox*1.42) */; // sqrt(2)

      { // amplitude curve
        noFill();
        strokeWeight(3);
        stroke(255, 255, 255, 255/3);
        arc(0, 0, pWidth, pWidth, radians(0+15.0), radians(180.0-15.0));
      }
      

      if (true) { // amplitude knob       
        pushMatrix();
        
        fill(255,255,255);
        rotate( radians(15.0+(150.0*(1-_amp) )) ); 
        translate(pWidth/2, 0);
        ellipse(0,0,6,6);
        
        popMatrix();

        //System.out.println("[draw]");
      }
      
      { // octave control

        float _step = ((180.0-30.0)/8);
        for (int i=0; i<8; i=i+1) {
          noFill();
          strokeWeight(4);
          if ( i == _oct) {
            stroke(255, 255, 255);
          } else {
            stroke(255, 255, 255, 255/3);
          }
          arc(0, 0, pWidth-10, pWidth-10,
            radians( 180.0+15.0+(i*_step)), radians( 180.0+15.0-1+((i+1)*_step) ) );
        }
      }
      
      { // tune control

        float _step = ((180.0-30.0)/12);
        for (int i=0; i<12; i=i+1) {
          noFill();
          strokeWeight(4);
          if ( i == _tun) {
            stroke(255, 255, 255);
          } else {
            stroke(255, 255, 255, 255/3);
          }
          arc(0, 0, pWidth, pWidth,
            radians( 180.0+15.0+(i*_step)), radians( 180.0+15.0-1+((i+1)*_step) ) );
        }
      }
      
      // -- Draw rotated square --
      
      // sine wave symbol
      if (false) {
        pushMatrix();

        final float _symSize = 18;

        translate( -(pWidth/2), 0);
        rotate( radians(90));

        final float _s = (_symSize/2) -1 /*-4*/;
        PVector pStart = new PVector(- _s, 0);
        PVector pEnd = new PVector(_s, 0);
        
        // FIXME why is sine wave drawn upside down? Hence the negative amplitude
        float gAmp = -((_symSize/2) -3 /*-6*/); // amplitude in pixels
  
        // draw sign inside box
        stroke(#FFFFFF);
        strokeWeight(1);
        sineTo(pStart, pEnd, 1 /* freq */, gAmp);
        
        popMatrix();
      }
      
      
      noStroke();
      fill(#FF1111);

      rotate(obj.getAngle());
      rect(0, 0, wBox, wBox);


    } else {
      // Unknown object type
      fill(#FFFFFF);
      rect(0, 0, wBox, wBox);
    }
    
    
    popMatrix();
  }
  // System.out.println("[draw] end");
}





// zones

void drawAmpSlider(Zone zone){
  // System.out.println("[drawTouch] start");

  if (!_dragged) {
    float angle = (30.0/2)+(150.0*(1-_amp));

    PVector v = new PVector(wBox,0);
    v.rotate( radians( angle ) );
    v.add(new PVector(_px, _py ));
    zone.setLocation( v.x, v.y );
  }

  noFill();
  strokeWeight(1);
  stroke(255, 255, 255);
  ellipse(0,0, 6, 6);
  
  // System.out.println("[drawTouch] end");
}

void pickDrawAmpSlider(Zone zone){
  if (!_dragged) {
    float angle = (30.0/2)+(150.0*(1-_amp));

    PVector v = new PVector(wBox,0);
    v.rotate( radians( angle ) );
    v.add(new PVector(_px, _py ));
    zone.setLocation( v.x, v.y );
  }
  
  // TODO: make this a bit bigger for the human thumb
  // fill(255,0,0);
  ellipse(0,0, 8, 8);

  // reset here
  _dragged = false;

  // System.out.println("[pickDrawAmpSlider]");
}

void touchAmpSlider(Zone zone){
  // System.out.println("[drag] start");
  zone.drag();
  
  
  // Update amplitude
  if (ampSlider != null && ampSlider.equals(zone)) {
    PCoord pc = getAngle(_px, _py, zone.getX(), zone.getY());
    // System.out.println("mag: " + pc.mag + " angle: " + pc.angle);
    // System.out.println("[drag] end");
  
    // update amplitude
    float a = pc.angle;
  
    // check boundaries
    if (a < 15) {
      a = 15;
    } else if (a > (150+15)) {
      a = 150+15;
    }
  
    doUpdateAmplitude( 1-( (a-15)/150 ) );
  
    _dragged = true;
  }
}


void drawOscTypeButton(Zone zone) {
  // System.out.println("h");
  PVector v = new PVector(-wBox,0);
  v.add(new PVector(_px, _py ));
  zone.setLocation( v.x, v.y );
  
  final float _symSize = 18;
  rotate( radians(-90) );
  if (oscType == 0) {

    final float _s = (_symSize/2) -1;
    PVector pStart = new PVector(- _s, 0);
    PVector pEnd = new PVector(_s, 0);
    
    // FIXME why is sine wave drawn upside down? Hence the negative amplitude
    float gAmp = -((_symSize/2) -3 /*-6*/); // amplitude in pixels

    // draw sign inside box
    stroke(#FFFFFF);
    strokeWeight(1);
    sineTo(pStart, pEnd, 1 /* freq */, gAmp);
  } else if (oscType == 1) {
    // sawtooth
    final float _s = (_symSize/2) -1;

    stroke(#FFFFFF);
    strokeWeight(1);

    line(-_s, -_s, _s, _s);    
    line(_s, _s, _s, -_s);    
  } else if (oscType == 2) {
    // square wave
  } else if (oscType == 3) {
    // white noise
  } else {
    // invalid osc type
  }
}

void pickDrawOscTypeButton(Zone zone) {
  // System.out.println("p");
  PVector v = new PVector(0, 0);
  v.rotate( radians( 90 ) );
  v.add(new PVector(_px-wBox, _py ));
  zone.setLocation( v.x, v.y );
  
  // draw sign inside box
  // fill(128,0,0);
  final float _symSize = 18;
  rectMode(CENTER);
  rect(0,0, _symSize, _symSize);
}

void touchDownOscTypeButton(Zone zone) {
  doChangeOscType();
}
