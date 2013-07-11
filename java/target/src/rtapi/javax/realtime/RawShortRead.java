package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import static javax.safetycritical.annotate.Phase.*;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawShortRead {

	/**
	 * Get the value of this raw short.
	 * 
	 * @return the short from raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false, phase = ALL)
	public short get();

}
