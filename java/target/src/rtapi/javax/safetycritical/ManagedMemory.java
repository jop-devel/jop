package javax.safetycritical;

import javax.realtime.LTMemory;
import javax.realtime.SizeEstimator;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.Memory;

/**
 * This class is not 'really' visible. Do we need it?
 * We need it for the static methods and enterPrivateMemoery.
 * However, we probably don't need PrivateMemory.
 */
@SCJAllowed
public abstract class ManagedMemory extends LTMemory {

	public ManagedMemory(long size) {
		super(size);
	}

	// ManagedMemory(SizeEstimator estimator) { super(estimator); }

	/**
	 * @return
	 */
	@SCJAllowed
	public static ManagedMemory getCurrentManagedMemory() {
		return null;
	}

	/**
	 * @return
	 */
	@SCJAllowed
	public static long getMaxManagedMemorySize() {
		return 0l;
	}

	/**
	 * @param size
	 * @param logic
	 */
	@SCJAllowed
	public static void enterPrivateMemory(long size, Runnable logic) {
		Memory m = Memory.getCurrentMemory();
		m.enterPrivateMemory((int) size, logic); 
	}

	/**
	 * @return
	 */
	@SCJAllowed
	public ManagedSchedulable getOwner() {
		return null;
	}

	/**
	 * A simple test. This method is not in spec source.
	 * Override the inherited method to avoid implementing the logic in RTSJ
	 * classes.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long size() {
		return 123L; // dummy return
	}

}
