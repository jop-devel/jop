package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

@SCJAllowed(LEVEL_1)
public abstract class AperiodicEventHandler extends ManagedEventHandler {

  /**
   * Constructor to create an aperiodic event handler.
   * <p>
   * Does not perform memory allocation. Does not allow this to escape local
   * scope. Builds links from this to priority and parameters, so those two
   * arguments must reside in scopes that enclose this.
   * Builds a link from ``this" to event, so event must reside
   * in memory that encloses ``this".
   * 
   * @param priority
   *            specifies the priority parameters for this periodic event
   *            handler. Must not be null.
   * 
   * @param release_info
   *            specifies the periodic release parameters, in particular the
   *            start time, period and deadline miss and cost overrun
   *            handlers. Note that a relative start time is not relative to
   *            NOW but relative to the point in time when initialization is
   *            finished and the timers are started. This argument must not be
   *            null. TBD whether we support deadline misses and cost overrun
   *            detection.
   * 
   * @param scp
   *            The mem_info parameter describes the organization of memory
   *            dedicated to execution of the underlying thread.
   * 
   * @param event
   *            the aperiodic event that when fired should release this handler.
   *            
   * @throws IllegalArgumentException
   *             if priority, parameters or event is null.
   */
  @MemoryAreaEncloses(inner = { "this", "this", "this", "this" },
                      outer = { "priority", "release_info", "mem_info", "event" })
  @SCJAllowed(LEVEL_1)
  @SCJRestricted(phase = INITIALIZATION)
  public AperiodicEventHandler(PriorityParameters priority,
                               AperiodicParameters release,
                               StorageParameters scp,
                               AperiodicEvent event)
  {
    super(null, null, null, null);
  }
  
  
  /**
   * Constructor to create an aperiodic event handler.
   * <p>
   * Does not perform memory allocation. Does not allow this to escape local
   * scope. Builds links from this to priority and parameters, so those two
   * arguments must reside in scopes that enclose this.
   * Builds a link from ``this" to event, so event must reside
   * in memory that encloses ``this".
   * 
   * @param priority
   *            specifies the priority parameters for this periodic event
   *            handler. Must not be null.
   * 
   * @param release_info
   *            specifies the periodic release parameters, in particular the
   *            start time, period and deadline miss and cost overrun
   *            handlers. Note that a relative start time is not relative to
   *            NOW but relative to the point in time when initialization is
   *            finished and the timers are started. This argument must not be
   *            null. TBD whether we support deadline misses and cost overrun
   *            detection.
   * 
   * @param scp
   *            The mem_info parameter describes the organization of memory
   *            dedicated to execution of the underlying thread.
   * 
   * @param name
   *            the name by which this event handler is identified.
   *
   * @param event
   *            the aperiodic event that when fired should release this handler.
   *            
   * @throws IllegalArgumentException
   *             if priority, parameters or event is null.
   */
  @MemoryAreaEncloses(inner = { "this", "this", "this", "this", "this" },
                      outer = { "priority", "release_info", "scp", "event", "name" })
  @SCJAllowed(LEVEL_1)
  @SCJRestricted(phase = INITIALIZATION)
  public AperiodicEventHandler(PriorityParameters priority,
                               AperiodicParameters release,
                               StorageParameters scp,
                               AperiodicEvent event, String name)
  {  
    super(null, null, null, null);
  }
  
  
  /**
   * Constructor to create an aperiodic event handler.
   * <p>
   * Does not perform memory allocation. Does not allow this to escape local
   * scope. Builds links from this to priority and parameters, so those two
   * arguments must reside in scopes that enclose this.
   * Builds a link from ``this" to events, so events must reside
   * in memory that encloses ``this".
   * 
   * @param priority
   *            specifies the priority parameters for this periodic event
   *            handler. Must not be null.
   * 
   * @param release_info
   *            specifies the periodic release parameters, in particular the
   *            start time, period and deadline miss and cost overrun
   *            handlers. Note that a relative start time is not relative to
   *            NOW but relative to the point in time when initialization is
   *            finished and the timers are started. This argument must not be
   *            null. TBD whether we support deadline misses and cost overrun
   *            detection.
   * 
   * @param scp
   *            The mem_info parameter describes the organization of memory
   *            dedicated to execution of the underlying thread.
   * 
   * @param events
   *            the array of aperiodic events that if anyone of the
   *            events is fired should release this handler.
   *            
   * @throws IllegalArgumentException
   *             if priority, parameters or event is null.
   */
  @MemoryAreaEncloses(inner = { "this", "this", "this", "this" },
                      outer = { "priority", "release_info", "scp", "events" })
  @SCJAllowed(LEVEL_1)
  @SCJRestricted(phase = INITIALIZATION)
  public AperiodicEventHandler(PriorityParameters priority,
                               AperiodicParameters release,
                               StorageParameters scp, AperiodicEvent[] events)
  {
    super(null, null, null, null);
  }
  
  
  /**
   * Constructor to create an aperiodic event handler.
   * <p>
   * Does not perform memory allocation. Does not allow this to escape local
   * scope. Builds links from this to priority and parameters, so those two
   * arguments must reside in scopes that enclose this.
   * Builds a link from ``this" to events, so events must reside
   * in memory that encloses ``this".
   * 
   * @param priority
   *            specifies the priority parameters for this periodic event
   *            handler. Must not be null.
   * 
   * @param release_info
   *            specifies the periodic release parameters, in particular the
   *            start time, period and deadline miss and cost overrun
   *            handlers. Note that a relative start time is not relative to
   *            NOW but relative to the point in time when initialization is
   *            finished and the timers are started. This argument must not be
   *            null. TBD whether we support deadline misses and cost overrun
   *            detection.
   * 
   * @param scp
   *            The mem_info parameter describes the organization of memory
   *            dedicated to execution of the underlying thread.
   * 
   * @param name
   *            the name by which this event handler is identified.
   *
   * @param events
   *            the array of aperiodic events that if anyone of the
   *            events is fired should release this handler.
   *            
   * @throws IllegalArgumentException
   *             if priority, parameters or event is null.
   */
  @MemoryAreaEncloses(inner = { "this", "this", "this", "this", "this" },
                      outer = {"priority", "release_info", "scp", "events", "name" })
  @SCJAllowed(LEVEL_1)
  @SCJRestricted(phase = INITIALIZATION)
  public AperiodicEventHandler(PriorityParameters priority,
                               AperiodicParameters release,
                               StorageParameters scp,
                               AperiodicEvent[] events, String name)
  {
    super(null, null, null, null);
  }
  
  /**
   * @see javax.safetycritical.ManagedSchedulable#register()
   * Registers this event handler with the current mission and attaches
   * this handler to all the aperiodic events passed during construction.
   * Registers all the aperiodic events passed during constructions.
   */
  @Override
  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION)
  public final void register() {}
}
