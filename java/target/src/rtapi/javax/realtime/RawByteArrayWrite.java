package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawByteArrayWrite {

	/**
	 * Store the byte in array in the associated Raw memory.
	 * 
	 * @param b
	 *            is the byte to be stored.
	 * @param offset
	 *            is the location of the byte to be stored relative to the raw
	 *            memory area.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(byte b, long offset);

	/**
	 * Store the byte array value in the associated Raw memory.
	 * 
	 * @param i
	 *            is the array to be stored.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(byte[] i);

}
