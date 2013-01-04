package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawIntArrayWrite {

	/**
	 * Store the int in the associated Raw memory array.
	 * 
	 * @param value
	 *            is the int to be stored.
	 * @param offset
	 *            is the position in array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(int value, long offset);

	/**
	 * Store the int array value in the associated Raw memory.
	 * 
	 * @param array
	 *            is the array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(int[] array);

}
