package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;
import static javax.safetycritical.annotate.Level.LEVEL_0;

/**
 * An interface to a long accessor object. An accessor object encapsulates the
 * protocol required to read a long in raw memory.
 * 
 */

@SCJAllowed(LEVEL_0)
public interface RawLongRead {

	/**
	 * Get the value of this raw long.
	 * 
	 * @return the long from raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public long get();

}
