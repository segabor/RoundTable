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

final int RW = 640;
final int RH = 480;

// node types
final int NODE_TYPE_OUT = 0;
final int NODE_TYPE_GEN = 1;
final int NODE_TYPE_CNTR = 2;
final int NODE_TYPE_FX = 3;
final int NODE_TYPE_GLB_CNTR = 4;



void setup() {
  size(RW,RH, P3D);
  SMT.init(this, TouchSource.MULTIPLE);
  
  // setup
  // nodeMap.put(NKEY0, globalOut);
  globalOut.setCoords( new PVector(RW/2, RH/2));
}


// special key for center node
final long NKEY0 = 255;
// generate key from a TUIO event object
long genKey(TuioObject obj) {
  final long sessionId = obj.getSessionID();
  final int symbolId = obj.getSymbolID();

  return sessionId << 8 | symbolId;  
}


// Convert Symbol type to Reactable node type
//
// @param s Symbol ID
// @return Reactable type
int findObjectType(int s) {
  if ( (s >= 0 && s < 4) || s == 12 || s == 16  || s == 20 || s == 24) {
    // generator
    return NODE_TYPE_GEN;
  } else if (s >= 4 && s < 8) {
    // controller
    return NODE_TYPE_CNTR;
  } else if ( s >= 8 && s < 12 ) {
    // effect
    return NODE_TYPE_FX;
  } else if (s >= 32 && s < 26) {
    // global controller
    return NODE_TYPE_GLB_CNTR;
  }

  // unknown
  return -1;
}


class Node {
  long key;
  int type;

  // recent TUIO event  
  TuioObject event;

  PVector coords; // two dimensional cartesian coordinates
  
  public Node(long key, int type) {
    this.key = key;
    this.type = type;
  }

  public int hashCode() { return Long.valueOf(key).hashCode(); }
  
  public boolean equals(Object other) {
    return other instanceof Node && key == ((Node) other).key;
  }

  public int getType() { return type;}

  public void setEvent(TuioObject obj) {this.event = obj;}
  public TuioObject getEvent() { return this.event;}

  public PVector getCoords() { return coords;}
  public void setCoords(PVector coords) { this.coords = coords;}
}


class Link {
  Node in;
  Node out;
  int type = 0; // flow type: 0 = audio, 1 = control flow
  boolean hard = false; // hard link never breaks when node moves
  boolean mute = false;
  boolean hidden = false;
  
  public Link(int type, Node in, Node out) {
    this.type = type; this.in = in; this.out = out;
  }

  public int hashCode() {
    return 31*31*type + ( in != null ? 31*in.hashCode() : 0 ) + ( out != null ? out.hashCode() : 0 );
  }

  public boolean equals(Object other) {
    if (other instanceof Link) {
      Link ol = (Link) other; return type == ol.type && in.equals(ol.in) && out.equals(ol.out);
    }
    return false;
  }

  public int getType() { return type;}
  public Node getIn() { return in;}
  public Node getOut() { return out;}

  public boolean isHard() { return hard;}
  public void setHard(boolean f) { hard = f;}
  
  public boolean isMute() { return mute;}
  public void setMute(boolean f) { mute = f;}
  
  public boolean isHidden() { return hidden;}
  public void setHidden(boolean f) { hidden = f;}
}


// == Global variables ==

Map<Long,Node> nodeMap = new ConcurrentHashMap<Long,Node>();
Set<Link> links = new HashSet<Link>();

public Link findLink(Node n) {
  for (Link l : links) {
    if (n.equals(l.in) || n.equals(l.out)) {
      return l;
    }
  }
  return null;
}

final Node globalOut = new Node(NKEY0, NODE_TYPE_OUT);




void draw() {
  processTUIOEvents( SMT.getTuioObjects() );

  Set<Link> linx = new HashSet<Link>();
  calculateAudioGraph(linx);

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
    
    PVector p0 = l.in.getCoords(), p1 = l.out.getCoords(); 
    
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
    final int type = findObjectType( obj.getSymbolID() );
    if (type < 0)
      continue;
    
    final long k = genKey(obj);
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

static final float d_inf = Float.POSITIVE_INFINITY;
float weight(int t_start, int t_end, boolean sameNode, float dist) {
  if (sameNode || abs(dist) < 0.000000001f) {
    return d_inf;
  }
  
  if (
      (t_start == NODE_TYPE_GEN && (t_end == NODE_TYPE_FX || t_end == NODE_TYPE_OUT ))
      ||
      (t_start == NODE_TYPE_FX && (t_end == NODE_TYPE_FX || t_end == NODE_TYPE_OUT ))
      ||
      (t_start == NODE_TYPE_CNTR && (t_end == NODE_TYPE_GEN || t_end == NODE_TYPE_FX ))
      ||
      (t_start == NODE_TYPE_GLB_CNTR && (t_end == NODE_TYPE_GEN || t_end == NODE_TYPE_OUT ))
    )
  {
    // return normal distance as default weight
    return dist;
  } else {
    return d_inf;
  }
}

void calculateAudioGraph(Set<Link> edges) {
  
  List<Node> nl = new ArrayList<Node>(nodeMap.values());

  // no nodes
  if (nl.size() == 0)
       return;

  final int s = nl.size();

  // s+1 = available nodes + global out
  float dm[][] = new float[s+1][s+1];
  int t[] = new int[s+1]; // node(i) types
  int t2x[][] = new int[5][s+1]; // type to indices map:
  // where [type][0] = count of indices, [type][1] = i1, ...
  for (int k=0; k<5; k++) { t2x[k][0] = 0;}
  for (int k=0; k<s; k++) { t[k] = nl.get(k).getType(); }
  t[s] = NODE_TYPE_OUT; // expand type array

  Node n, n2; int nt;

  // put colored nodes here
  Set<Node> _painted = new HashSet<Node>();

  // setup distance matrix
  for (int j=0; j<s; j++) {
    n = nl.get(j); nt = n.getType();
    // System.out.println("Will try to update t2x["+nt+"]["+t2x[nt][0]+"]");
    t2x[nt][ (t2x[nt][0])+1 ] = j; // store index
    t2x[nt][0]++;
    
    // calc dist(out, node(j))
    dm[s][j] = dm[j][s] = globalOut.getCoords().dist( nl.get(j).getCoords());
    for (int k=j+1; k<s+1; k++) {
      /* if (k == j) {
        dm[j][k] = d_inf;
        continue;
      } */
      
      int n2t = NODE_TYPE_OUT;
      if (k == s) {
        n2 = globalOut;
      } else {
        n2 = nl.get(k);
        n2t = n2.getType();
      }

      // determine weight
      dm[j][k] = dm[k][j] = weight(t[j], n2t, k == j, k == j ? 0 : n.getCoords().dist(n2.getCoords()) );
    }
  }
  dm[s][s] = d_inf; // dist(globalOut,globalOut) = 0


  // see above step

  // update weights with
  // - hard link
  // - sequence number (ie. G->E: 3,  E->E: 2,  E->O: 1)


  // Set<Link> edges = new HashSet<Link>();

  // pick a source node (phase #1)
  final int startTypes[] = new int[] {NODE_TYPE_CNTR, NODE_TYPE_GEN, NODE_TYPE_FX, NODE_TYPE_GLB_CNTR};
  
  // System.out.println("GO0");
  for (int k=0; k<startTypes.length; k++) {
    int cur_t = startTypes[k];
    
    for (int i=0; i<t2x[ cur_t ][0]; i++) {
      Node m = null;
      int y = -1;  // destination index
      float d = d_inf; // min(distance(n,m))
  
      final int ix = t2x[ cur_t ][i+1];
      // DEBUG System.out.println("Try to pick node of type " + startTypes[k] + " at index " + ix);
      n = nl.get(ix);
      // find the closest one
      
      // find the closest node for n to connect
      for (int j=0; j<s+1; j++) {
        // skip infinite distance
        if ( ix != j /* && dm[ix][j] > 0 */ && dm[ix][j] < d_inf) {
          n2 = j == s ? globalOut : nl.get(j);
          // find the minimal weight
          // check targed node is not yet painted
          if (/* !_painted.contains(n2) && */ dm[ix][j] < d) {
            d = dm[ix][j]; // update min(distance)
            y = j; // update index

            // remember destination node
            m = n2;
            // DEBUG System.out.println("OK0");
          } else {
            // DEBUG System.out.println("SK1");
          }
        } else {
          // DEBUG  System.out.println("SK0");
        }
      }
      
      // closest node 'm' found with distance 'd'
      // establish edge 'n'->'m'
      if (m != null) {
        System.out.println("Link("+edges.size()+") " + n.getType() + " -> " + m.getType());
        Link li = new Link(0, n, m); // TODO: type: 0: AR, 1: CR
        if (n.getType() == NODE_TYPE_GLB_CNTR && m.getType() == NODE_TYPE_OUT) {
          li.setHidden(true);
        }
        edges.add(li);
        
        // TODO: extreme distance can be used as well
        // to indicate painted status
        // if (m.getType() != NODE_TYPE_OUT)
        //  _painted.add(m);
        dm[ix][y] = dm[y][ix] = d_inf;
        // cur_t = t[y];
      }
    }

    /// return edges;
  }
}
