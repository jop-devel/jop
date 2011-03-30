package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import javax.realtime.AsyncEvent;
import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import javax.safetycritical.annotate.Allocate.Area;

/**
 * TBD(kdn - july 5, 2010): Note that Mission.requestTermination() must 
 * disable all 
 * AperiodicEvent objects associated with the Mission, in order to 
 * arrange that all AperiodicEventHandlers associated with the Mission
 * can be terminated and joined.  This means that the Mission needs to 
 * keep track of all AperiodicEvents, so we really need to "manage" 
 * AperiodicEvents, and I believe this means
 * we'll have to register each one in the Mission.initialize() code.
 * Should this class extend an abstract ManagedEvent class or interface?
 */
@SCJAllowed(LEVEL_1)
public class AperiodicEvent extends AsyncEvent
{
  /**
   * Constructor for an aperiodic event.
   * <p>
   * Does not allocate memory. Does not allow this to escape the local
   * variables. 
   */
  @Allocate( { Area.THIS })
  @MemoryAreaEncloses(inner = { "this" }, outer = { "handler" })
  @SCJAllowed(LEVEL_1)
  @SCJRestricted(phase = INITIALIZATION)
  public AperiodicEvent() {}
}
