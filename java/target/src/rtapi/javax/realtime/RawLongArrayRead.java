package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * An longerface to a long array accessor object. An accessor object encapsules
 * the protocol required to access a long array in raw memory.
 * 
 */

@SCJAllowed(LEVEL_0)
public interface RawLongArrayRead {

	/**
	 * Get the value of a long from this raw long array.
	 * 
	 * @param offset
	 * @return The long from raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public long get(long offset);

	/**
	 * Get the value of this raw long array.
	 * 
	 * @param array
	 *            Is the array to place the data.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void get(long[] array);

}
