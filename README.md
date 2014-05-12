# Project RoundTable

## What is this?
An attempt to rewrite [ReacTable](http://reactable.com/products/live/) software. RoundTable is a TUIO client written in Java language and based on [Processing](http://processing.org/) visual framework.

## Current Status
Basically it's in an early, experimental stage so everything may (and will) change. A few things are already working but there is a lot of work to be done. List of more or less complete functions:

* Fiducials - Detect objects and draw their basic shapes
* Audio graph - Link objects into a single audio graph. Tests included.

I'm currently focusing on the visualization part (object details, editor panel, edges, animations, etc.). The next big thing to work out will certainly be the audio part which I plan to build upon [SuperCollider](http://supercollider.github.io/).

### The Future
The final goal is to run RoundTable on cheap hardware, possibly a multi-core ARM board. Ideally, audio and visual parts would run on different boards so design will follow this idea.

## How can I try it out
Actually it can be run from [Eclipse](http://eclipse.org), no binary is available yet for download. Clone the source to your desktop and import the projects into Eclipse. Other things you will need:

* [TUIO_JAVA](http://www.tuio.org/?software) - Download the source, create a TUIO_JAVA project in Eclipse and import the source into that.
* [reacTIVision](http://reactivision.sourceforge.net/) - This piece of software will do the real fiducial detection and send TUIO events towards RoundTable.
* [TuioSimulator](http://prdownloads.sourceforge.net/reactivision/TUIO_Simulator-1.4.zip?download) - You will really need this if you don't have your own tangible tabletop yet.

## What's next?
Create a roadmap, backlog, wiki.

Should you need help or have a question, contact me.
Cheers,

Gábor SEBESTYÉN
Twitter: @segabor

