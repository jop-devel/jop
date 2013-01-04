package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;
import static javax.safetycritical.annotate.Phase.ALL;

/**
 * An interface to a short array accessor object for writing. An accessor object
 * encapsules the protocol required to write access a short array in raw memory.
 * 
 */
@SCJAllowed(LEVEL_0)
public interface RawShortArrayWrite {

	/**
	 * Store the short in the associated Raw memory array.
	 * 
	 * @param value
	 *            Is the short to be stored.
	 * @param offset
	 *            Is the position in array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false, phase = ALL)
	public void put(short value, long offset);

	/**
	 * Store the short array value in the associated Raw memory.
	 * 
	 * @param array
	 *            Is the array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false, phase = ALL)
	public void put(short[] array);

}
