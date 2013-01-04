package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public interface RawIntArrayRead {
	
	/**
	 * 	Get the value of a int from this raw int array.
	 * 
	 * @param offset
	 * @return the int from raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public int get(long offset);
	
	/**
	 * 	Get the value of this raw int array into array.
	 * 
	 * @param array is the array to place the data.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void get(int[] array);
	

}
