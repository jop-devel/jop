package javax.safetycritical;

import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.ReleaseParameters;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;
import static javax.safetycritical.annotate.Phase.CLEANUP;
import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Level.INFRASTRUCTURE;


@SCJAllowed
public abstract class ManagedEventHandler extends BoundAsyncEventHandler
  implements ManagedSchedulable
{
  /**
   * Constructor to create an event handler.
   * <p>
   * Does not perform memory allocation. Does not allow this to escape local
   * scope. Builds links from this to priority, parameters, and name so those
   * three arguments must reside in scopes that enclose this.
   *
   * @param priority
   *        specifies the priority parameters for this periodic event
   *        handler. Must not be null.
   *
   * @param release
   *        specifies the periodic release parameters, in particular the
   *        start time and period. Note that a relative start time is not
   *        relative to NOW but relative to the point in time when
   *        initialization is finished and the timers are started. This
   *        argument must not be null.
   *         
   * @param scp
   *        The scp parameter describes the organization of memory
   *        dedicated to execution of the underlying thread. (added by MS)
   *
   * @throws IllegalArgumentException
   *         if priority parameters are null.
   */
  @SCJAllowed(INFRASTRUCTURE)
  @SCJRestricted(phase = INITIALIZATION)
  ManagedEventHandler(PriorityParameters priority,
		      ReleaseParameters release,
		      StorageParameters scp,
		      String name)
  {
  }

  /**
   * Application developers override this method with code to be executed when
   * this event handler's execution is disabled (upon termination of the
   * enclosing mission).
   *
   */
  @Override
  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = CLEANUP)
  public void cleanUp() {}

  /**
   * Application developers override this method with code to be executed
   * whenever the event(s) to which this event handler is bound is fired.
   */
  @Override
  @SCJAllowed(SUPPORT)
  public abstract void handleAsyncEvent();

  /**
   * @return the name of this event handler.
   */
  @SCJAllowed
  public String getName()
  {
    return null;
  }

  /**
   * @see javax.safetycritical.ManagedSchedulable#register()
   */
  @Override
  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION)
  public void register() {}
}
