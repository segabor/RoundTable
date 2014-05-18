import me.segabor.roundtable.audiograph.logic.*;
import me.segabor.roundtable.audiograph.data.*;
import me.segabor.roundtable.audiograph.*;
import me.segabor.roundtable.*;

import vialab.SMT.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import TUIO.TuioObject;

final int RW = 800;
final int RH = 600;




void setup() {
  size(RW,RH, P3D);
  SMT.init(this, TouchSource.MULTIPLE);
  
  // setup
  // nodeMap.put(NKEY0, globalOut);
  globalOut.setCoords( new PVector(RW/2, RH/2));
  
  // logic.setGlobalOut(globalOut);
}



// == Global variables ==

Map<Long,Node> nodeMap = new ConcurrentHashMap<Long,Node>();
Set<Link> links = new HashSet<Link>();

public Link findLink(Node n) {
  for (Link l : links) {
    if (n.equals(l.getIn()) || n.equals(l.getOut())) {
      return l;
    }
  }
  return null;
}

final Node globalOut = new Node(SymbolToNodeKeyMapperUtil.NKEY0, Node.NODE_TYPE_OUT);
AudioGraphBuilder builder = new AudioGraphBuilder();


void draw() {
  processTUIOEvents( SMT.getTuioObjects() );

  Set<Link> linx = new HashSet<Link>();
  List<Node> nodez = new ArrayList(nodeMap.values());
  
  DistanceMatrix dm = new DistanceMatrix(globalOut, nodez);
  builder.buildAudioGraph(dm, linx);

  // draw phase

  // L1. background
  background(51);
  
  
  // L2. center node (FIXME: perhaps draw over edges?)
  drawNode(globalOut);

  // L3. audio graph - edges
  // draw links  
  // DEBUG System.out.println("Will draw " + linx.size() + " lines...");
  for (Link l : linx) {
    // DRAW
    // pushMatrix();
    
    PVector p0 = l.getIn().getCoords(), p1 = l.getOut().getCoords(); 
    
    stroke(255,255,255);
    strokeWeight(1);
    // System.out.println("Connect " + p0.x + ";" + p0.y + " with " + p1.x + ";" + p1.y);
    line(p0.x, p0.y, p1.x, p1.y);

    // arrow head
    pushMatrix();
    translate(p1.x, p1.y);
    float a = atan2(p0.x-p1.x, p1.y-p0.y /*  x1-x2, y2-y1 */);
    rotate(a);
    line(0, 0, -10, -20);
    line(0, 0, 10, -20);
    popMatrix();
    
    // popMatrix();
  }

  // L4. audio graph - nodes
  // draw nodes
  for (Node n : nodeMap.values()) {
    drawNode(n);
  }
  
}

void drawNode(Node n) {
  final int t = n.getType();
  final TuioObject obj = n.getEvent();

  // draw rotated square
  pushMatrix();
  
  rectMode(CENTER);

  // draw center node
  if (t == 0) {
    translate(RW/2, RH/2);
    fill(255,255,255);
    ellipse(0,0,10,10);
  } else {
  
    translate(obj.getX()*RW, obj.getY()*RH);
    if (t == 1 || t == -1) {
      // generator - SQUARE
      rotate(obj.getAngle());
      rect(0, 0, 40, 40);
    } else if (t == 2) {
      // controller - CIRCLE
      ellipse(0, 0, 40, 40);
    } else if (t == 3) {
      // effect - ROUNDED SQUARE
      rotate(obj.getAngle());
      rect(0, 0, 40, 40, 5);
    } else if (t == 4) {
      // global controller - 
    }
  }
  popMatrix();
}




void processTUIOEvents(Collection<TuioObject> events) {
  // the list contains newly added or changed objects
  // 
  Set<Long> inKeys = new HashSet<Long>();
  for (TuioObject obj : events) {
    final int type = SymbolToNodeKeyMapperUtil.findObjectType( obj.getSymbolID() );
    if (type < 0)
      continue;
    
    final long k = SymbolToNodeKeyMapperUtil.genKey(obj);
    inKeys.add(k);
    
    // handle events
    final int state = obj.getTuioState();

    if (nodeMap.get(k) == null) {
      Node n = new Node(k, type); n.setEvent(obj); nodeMap.put(k, n); //  nodeMap.add(n);
      n.setCoords(new PVector( obj.getX()*RW, obj.getY()*RH ));
    } else {
      nodeMap.get(k).setEvent(obj);
      nodeMap.get(k).getCoords().set(obj.getX()*RW, obj.getY()*RH);
    }
  }

  // throw out no longer used nodes
  for (long k : nodeMap.keySet() ) {
    if (!inKeys.contains(k)) {
      nodeMap.remove(k);
    }
  }
}

