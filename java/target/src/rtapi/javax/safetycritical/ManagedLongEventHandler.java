package javax.safetycritical;

import javax.realtime.BoundAsyncLongEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.ReleaseParameters;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.Memory;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;
import static javax.safetycritical.annotate.Level.SUPPORT;

/**
 * ManagedLongEventHandler
 * 
 */
@SCJAllowed
public abstract class ManagedLongEventHandler extends
		BoundAsyncLongEventHandler implements ManagedSchedulable {
	/**
	 * Constructor to create an event handler.
	 * <p>
	 * Does not perform memory allocation. Does not allow this to escape local
	 * scope. Builds links from this to priority, parameters, and name so those
	 * three arguments must reside in scopes that enclose this.
	 * 
	 * @param priority
	 *            specifies the priority parameters for this periodic event
	 *            handler. Must not be null.
	 * 
	 * @param release
	 *            specifies the periodic release parameters, in particular the
	 *            start time and period. Note that a relative start time is not
	 *            relative to NOW but relative to the point in time when
	 *            initialization is finished and the timers are started. This
	 *            argument must not be null.
	 * 
	 * @param scp
	 *            The scp parameter describes the organization of memory
	 *            dedicated to execution of the underlying thread. (added by MS)
	 * 
	 * @throws IllegalArgumentException
	 *             if priority parameters are null.
	 */

	private String name;
	
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	ManagedLongEventHandler(PriorityParameters priority,
			ReleaseParameters release, StorageParameters storage, String name) {
		this.name = name;
	}

	/**
	 * Application developers override this method with code to be executed when
	 * this event handler's execution is disabled (upon termination of the
	 * enclosing mission).
	 * 
	 */
	@Override
	@SCJAllowed(SUPPORT)
	public void cleanUp() {
		System.out.println("MLEH cleanup");
	}

	/**
	 * Application developers override this method with code to be executed
	 * whenever the event(s) to which this event handler is bound is fired.
	 */
	@SCJAllowed
	public abstract void handleAsyncEvent(long data);

	/**
	 * @return the name of this event handler.
	 */
	@SCJAllowed
	public String getName() {
		return name;
	}

//	/**
//	 * @see javax.safetycritical.ManagedSchedulable#register()
//	 */
//	@SCJAllowed
//	public void register() {
//
//	}

}
