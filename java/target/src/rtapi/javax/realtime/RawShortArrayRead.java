package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;
import static javax.safetycritical.annotate.Phase.ALL;

/**
 * An interface to a short array accessor object. An accessor object encapsules
 * the protocol required to access a short array in raw memory.
 * 
 */
@SCJAllowed(LEVEL_0)
public interface RawShortArrayRead {

	/**
	 * Get the value of a short from this raw short array.
	 * 
	 * @param offset
	 * @return Is the short from raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false, phase = ALL)
	public short get(long offset);

	/**
	 * Get the value of this raw short array into array.
	 * 
	 * @param array
	 *            Is the array to place the data.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false, phase = ALL)
	public void get(short[] array);

}
