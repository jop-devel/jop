package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawByteWrite {

	/**
	 * Store the byte value in the associated raw memory.
	 * 
	 * @param value
	 *            is the byte to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(byte value);
}
