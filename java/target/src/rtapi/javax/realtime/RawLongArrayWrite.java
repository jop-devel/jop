package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * An longerface to a long array accessor object for writing. An accessor object
 * encapsules the protocol required to write access a long array in raw memory.
 * 
 */
@SCJAllowed(LEVEL_0)
public interface RawLongArrayWrite {

	/**
	 * Store the long in the associated Raw memory array.
	 * 
	 * @param value
	 *            The long to be stored.
	 * @param offset
	 *            The position in array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(long value, long offset);

	/**
	 * Store the long array value in the associated Raw memory.
	 * 
	 * @param array
	 *            The array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(long[] array);

}
