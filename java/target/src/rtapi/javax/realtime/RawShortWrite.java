package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import static javax.safetycritical.annotate.Phase.*;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawShortWrite {

	/**
	 * Store the short value in the associated Raw memory.
	 * 
	 * @param value
	 *            is the short to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false, phase = ALL)
	public void set(short value);
}
