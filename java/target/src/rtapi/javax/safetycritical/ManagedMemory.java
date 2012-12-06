package javax.safetycritical;

import javax.realtime.InaccessibleAreaException;
import javax.realtime.LTMemory;
import javax.realtime.SizeEstimator;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;


import com.jopdesign.sys.Memory;

/**
 * This class is not 'really' visible. Do we need it? We need it for the static
 * methods and enterPrivateMemoery. However, we probably don't need
 * PrivateMemory.
 * 
 * martin: We might overload all methods from RTSJ classes that are needed here to
 * keep all implementation code within the SCJ package. Delegation to the private class
 * Memory.java is fine.
 */
@SCJAllowed
public abstract class ManagedMemory extends LTMemory {

//	ManagedMemory(int size, int bsSize) {
//		super(size, bsSize);
//	}

//	ManagedMemory(int size) {
//		this(size, 0);
//	}

	// public ManagedMemory(long size) {
	// super(size);
	// }

	// ManagedMemory(SizeEstimator estimator) { super(estimator); }

//	/**
//	 * @return the current managed memory area.
//	 */
//	@SCJAllowed
//	public static ManagedMemory getCurrentManagedMemory() {
//		return null;
//	}
//
//	/**
//	 * @return The maximum size for a new managed memory area.
//	 */
//	@SCJAllowed
//	public static long getMaxManagedMemorySize() {
//		return 0l;
//	}

	/**
	 * @param size
	 * @param logic
	 */
	@SCJAllowed
	public static void enterPrivateMemory(long size, Runnable logic) {
		Memory m = Memory.getCurrentMemory();
		m.enterPrivateMemory((int) size, logic);
	}
	
	public static void executeInAreaOf(Object obj, Runnable logic){
		Memory m = Memory.getMemoryArea(obj);
		m.executeInArea(logic);
	}
	
	public static void executeInOuterArea(Runnable logic) throws InaccessibleAreaException{
		Memory m = Memory.getCurrentMemory();
		// Objects representing memory areas, except for Immortal Memory, 
		// hold a reference to the memory area were they were created
		m = Memory.getMemoryArea(m);
		if(m == null){
			throw new InaccessibleAreaException("Not possible to move to an area outer than Immortal Memory" );
		}
		m.executeInArea(logic);
	}

//	/**
//	 * @return
//	 */
//	@SCJAllowed
//	public ManagedSchedulable getOwner() {
//		return null;
//	}

	/**
	 * A simple test. This method is not in spec source. Override the inherited
	 * method to avoid implementing the logic in RTSJ classes.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long size() {
		return 123L; // dummy return
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long memoryConsumed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long memoryRemaining() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static long getCurrentSize() {
		Memory m = Memory.getCurrentMemory();
		return m.size();
	}

}