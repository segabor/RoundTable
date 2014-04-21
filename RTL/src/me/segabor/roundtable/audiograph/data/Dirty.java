package me.segabor.roundtable.audiograph.data;

/**
 * Simple interface for 'dirty' state
 * 
 * @author segabor
 */
public interface Dirty {
	/**
	 * Check dirty state of object
	 * @return
	 */
	boolean isDirty();

	/**
	 * Mark object dirty
	 * Some implementation may be empty intentionally
	 * Note, call should mark the callee only. 
	 */
	void markDirty();

	/**
	 * Remove dirty state (if needed)
	 * and returns the previous state
	 * @return
	 */
	boolean cleanup();
}
