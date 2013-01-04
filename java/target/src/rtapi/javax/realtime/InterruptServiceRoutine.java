package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import java.util.Vector;

import javax.safetycritical.ManagedInterruptServiceRoutine;
import javax.safetycritical.Mission;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * A first level interrupt handling mechanisms. Override the handle method to
 * provide the first level interrupt handler. The constructors for this class
 * are invoked by the infrastructure and are therefore not visible to the
 * application.
 * 
 */
public abstract class InterruptServiceRoutine {
	
	public String name;

	/**
	 * 
	 * @param interrupt
	 * 
	 * @return the ISR registered with the given interrupt. Null is returned if
	 *         nothing is registered.
	 */
	@SCJAllowed(LEVEL_1)
	public static InterruptServiceRoutine getISR(int interrupt) {
		
		return ManagedInterruptServiceRoutine.getInterruptServiceRoutine(interrupt);
	}

	/**
	 * Every interrupt has an affinity that indicates which processors might
	 * service a hardware interrupt request. The returned set is preallocated
	 * and resides in immortal memory.
	 * 
	 * @param InterruptId
	 * 
	 * @return The affinity set of the processors
	 * 
	 * @throws IllegalArgument
	 *             if unsupported InterruptId
	 */
	@SCJAllowed(LEVEL_1)
	public static AffinitySet getInterruptAffinity(int InterruptId) {
		return null;
	}

	/**
	 * Every interrupt has an implementation-defined integer id.
	 * 
	 * @param InterruptId
	 *            Implementation-defined integer id.
	 * @return The priority of the code that the first-level interrupts code
	 *         executes. The returned value is always greater than
	 *         PriorityScheduler.getMaxPriority().
	 * 
	 * @throws IllegalArgument
	 *             If unsupported InterruptId.
	 */
	@SCJAllowed(LEVEL_1)
	public static int getInterruptPriority(int InterruptId) {
		return 0;
	}

	/**
	 * Get the name of this interrupt service routine.
	 * 
	 * @return The name of this interrupt service routine.
	 */
	@SCJAllowed(LEVEL_1)
	public final String getName() {
		return name;
	}

	/**
	 * The code to execute for first level interrupt handling. A subclass
	 * defines this to give the proper behavior. No code that could self-suspend
	 * may be called here. Unless the overridden method is synchronized, the
	 * infrastructure shall provide no synchronization for the execution of this
	 * method.
	 */
	@SCJAllowed(LEVEL_1)
	// @SCJRestricted(phase = INTERRUPT_SERVICE_ROUTINE, maySelfSuspend = false)
	protected abstract void handle();
	
	
}
