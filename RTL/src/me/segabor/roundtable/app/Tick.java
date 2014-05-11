package me.segabor.roundtable.app;

public class Tick {
	/**
	 * Time when the ticking starts to count
	 */
	public final long startTime;

	/**
	 * Number of beats (the upper number)
	 */
	public final int nBeats;

	/**
	 * The beat unit (the lower number)
	 */
	public final int beatUnit;

	protected double _stretch;

	/**
	 * Actual time in millisecs
	 */
	public long tcur;

	/**
	 * Number of beats since start time
	 */
	public int beats; // number of beats since start
	/**
	 * Current tick in range of (0..periods-1)
	 */
	public int tick;

	/**
	 * Sub beat value given in percentage
	 * It is calculated by {@link #update()} method
	 */
	public double subBeat;

	/**
	 * The Constructor
	 * 
	 * @param t0 The time when the ticker starts ticking
	 * @param beats Number of notes in a beat
	 * @param unit
	 */
	public Tick(long t0, int beats, int unit) {
		this.startTime = t0;
		
		// setup 'time signature'
		this.nBeats = beats;
		this.beatUnit = unit;

		// beat "stretch" in millsecs
		this._stretch = 1000.0 * (60.0 / ((double) this.nBeats)); // beat stretch
																// in millisecs
	}

	// convenience method
	public void update() {
		update(System.currentTimeMillis());
	}

	public void update(final long tcur) {
		this.tcur = tcur;

		// time delta
		final int d_ms = (int) (tcur - this.startTime);

		// beat span in millisecs
		// 60 bpm example:
		// 60 beats = 1 sec
		// 1 beat = 60/60 secs = (60/60)*1000 msecs
		// 120 beats = 1/2 sec
		// 1 beat = 60/120 secs = (120/60)*1000 msecs
		// final double stretch = 1000.0*( 60.0/((double) this.bpm)); // beat
		// stretch in millisecs
		// final double stretch = ( ((double)bpm)*periods*1000.0) / 60.0; //
		// beat stretch in millisecs
		final int ilen = (int) _stretch; // same in integer

		this.beats = d_ms / ilen;

		// beat percentage
		// double perc_beat = ((double) (d_ms - (ilen * this.beats)))/_stretch;
		this.subBeat = ((double) (d_ms % ilen)) / _stretch;

		this.tick = beats % this.beatUnit;

	}

	public boolean isCommonBase(Tick other) {
		return this.startTime == other.startTime && this.beats == other.beats
				&& this.beatUnit == other.beatUnit;
	}


	/**
	 * Copy current state to other tick object
	 * @param other
	 */
	public Tick copyTo(Tick other) {
		if (other == null) {
			other = new Tick(this.startTime, this.nBeats, this.beatUnit);
		}

		// copy calculated fields
		other.tcur = this.tcur;
		other.beats = this.beats;
		other.subBeat = this.subBeat;
		other.tick = this.tick;
		
		return other;
	}


	/**
	 * Copy constructor
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Tick other = new Tick(this.startTime, this.nBeats, this.beatUnit);

		// copy calculated fields
		other.tcur = this.tcur;
		other.beats = this.beats;
		other.subBeat = this.subBeat;
		other.tick = this.tick;

		return other;
	}


	public void debug() {
		System.out.println("BEAT: " + this.beats + " t: " + this.tick);
	}


	/**
	 * Return the number of ticks since start time
	 * @return
	 */
	public int ticks() {
		return (this.beats * this.beatUnit) + this.tick;
	}

	public boolean isTick(Tick other) {
		return (other != null &&
		/* this.tcur > other.tcur && */
		this.ticks() > other.ticks());
	}
}
