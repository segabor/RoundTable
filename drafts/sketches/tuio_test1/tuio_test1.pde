import vialab.SMT.*;
import java.util.Set;
import java.util.HashSet;
import TUIO.TuioObject;

final int RW = 640;
final int RH = 480;

void setup() {
  size(RW,RH, P3D);
  SMT.init(this, TouchSource.MULTIPLE);
}

void draw() {
  background(51);
  
  rectMode(CENTER);
  for (TuioObject obj : SMT.getTuioObjects()) {
    pushMatrix();
    translate(obj.getX()*RW, obj.getY()*RH);
    rotate(obj.getAngle());
    rect(0, 0, 40, 40);
    popMatrix();
  }
}


