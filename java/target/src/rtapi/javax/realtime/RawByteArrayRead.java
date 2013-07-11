package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawByteArrayRead {

	/**
	 * 	Get the value from this raw byte array.
	 * 
	 * @param offset
	 * @return the byte from the array in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public byte get(long offset);

	
	/**
	 * 	Get the value of this raw byte array into array.

	 * @param array
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void get(byte [] array);

}
