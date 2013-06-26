package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawIntWrite {

	/**
	 * Store the int value in the associated Raw memory.
	 * 
	 * @param value
	 *            is the value to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(int value);

}
