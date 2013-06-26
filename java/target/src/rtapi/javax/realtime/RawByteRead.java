package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * An interface to a byte accessor object. An accessor object encapsulates the
 * protocol required to read a byte in raw memory.
 * 
 */
@SCJAllowed(LEVEL_0)
public interface RawByteRead {

	/**
	 * Get the value of this raw byte.
	 * 
	 * @return the byte from raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public byte get();

}
