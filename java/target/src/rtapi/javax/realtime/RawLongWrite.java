package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;
import static javax.safetycritical.annotate.Level.LEVEL_0;

/**
 * An interface to a long accessor object. An accessor object encapsulates the
 * protocol required to write a long in raw memory.
 * 
 * 
 */

@SCJAllowed(LEVEL_0)
public interface RawLongWrite {

	/**
	 * Store the long value in the associated Raw memory.
	 * 
	 * @param The
	 *            value to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(long value);

}
