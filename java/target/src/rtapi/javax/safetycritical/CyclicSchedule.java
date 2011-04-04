package javax.safetycritical;

import javax.realtime.RelativeTime;
import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;
import static javax.safetycritical.annotate.Allocate.Area.THIS;

/**
 * A CyclicSchedule represents a time-driven sequence of firings
 * for deterministic scheduling of periodic event handlers.  The static
 * cyclic scheduler repeatedly executes the firing sequence.
 */
@SCJAllowed
public final class CyclicSchedule
{
  @SCJAllowed
  public final static class Frame
  {
    /**
     * Allocates and retains private shallow copies of the duration
     * and handlers array within the same memory
     * area as this. The elements within the copy of the handlers array are
     * the exact same elements as in the handlers array. Thus, it is
     * essential that the elements of the handlers array reside in memory
     * areas that enclose this.  Under normal circumstances, this
     * Frame object is instantiated within the MissionMemory area that
     * corresponds to the Level0Mission that is to be scheduled.
     * <p>
     * Within each execution frame of the CyclicSchedule, the
     * PeriodicEventHandler objects represented by the handlers 
     * array will be fired in same order as they appear within this
     * array.  Normally, PeriodicEventHandlers are sorted into
     * decreasing priority order prior to invoking this constructor.
     */
    @Allocate({ Area.THIS })
    @MemoryAreaEncloses(inner = { "this",     "this"     },
                        outer = { "duration", "handlers" })
    @SCJAllowed
    public Frame(RelativeTime duration, PeriodicEventHandler[] handlers)
    {
    }

    /**
     * TBD: Kelvin proposes to make this package access and final.
     * That way, we don't have to copy the returned value.  Ok?
     *
     * Performs no allocation. Returns a reference to the internal
     * representation of the frame duration, which is 
     * intended to be treated as read-only.
     * Any modifications to the returned RelativeTime object will have
     * potentially disastrous, but undefined results.  
     * The returned object resides in the
     * same scope as this Frame object.  Under normal
     * circumstances, this Frame object resides in the
     * MissionMemory area that corresponds to the Level0Mission that
     * it is scheduling.
     */
    // @SCJAllowed
    final RelativeTime getDuration()
    {
      return null;
    }

    /**
     * TBD: Kelvin proposes to make this package access and final.  That way, we
     * don't need to copy the handlers array.  
     *
     * @return a reference to the shared internal representation of
     * the array of PeriodicEventHandler objects that are to be
     * executed during this frame of time.  The array is sorted in
     * order of decreasing priority.  Thus, the highest priority event
     * handler is the first entry in the array.  Event handlers are
     * executed in the same order as they are listed in the returned
     * array.  Note that the returned array is shared with this
     * Frame object and is intended to be treated as read-only.
     * Any modifications to the array will have
     * potentially disastrous, but undefined results.  
     * The returned object resides in the
     * same scope as this Frame object.  Under normal
     * circumstances, this Frame object resides in the same MissionMemory area
     * as the Level0Mission that it is scheduling.  
     */
    // @SCJAllowed
    final PeriodicEventHandler[] getHandlers()
    {
      return null;
    }
  }

  /**
   * Construct a cyclic schedule by copying the frames array into a
   * private array within the same memory area as this newly
   * constructed CyclicSchedule object.  Under normal circumstances,
   * the CyclicSchedule is constructed within the MissionMemory area
   * that corresponds to the Level0Mission that is to be scheduled.
   * <p>
   * The frames array represents the order in which event handlers are
   * to be scheduled.  Note that some Frame entries within this array may
   * have zero PeriodicEventHandlers associated with them.  This would
   * represent a period of time during which the Level0Mission is idle.
   */
  @Allocate({ Area.THIS })
  @MemoryAreaEncloses(inner = { "this" }, outer = { "frames" })
  @SCJAllowed
  public CyclicSchedule(Frame[] frames)
  {
  }

  /**
   * TBD: Somebody had commented this out in the specsrc directory.
   * Does everyone else agree with removing it?  If not complete
   * removal, can we make it package access?  Ok for me to mark this final?
   *
   * @return a shared reference to a RelativeTime object representing
   * the sum of the durations of all of the Frame objects that
   * comprise this CyclicSchedule.
   * The returned RelativeTime object is shared with this
   * CyclicSchedule and is intended to be treated as read-only.
   * Any modifications to the RelativeTime object will have
   * potentially disastrous, but undefined results.  
   * The returned object resides in the
   * same scope as this CyclicSchedule object.  Under normal
   * circumstances, this object resides in the same MissionMemory area
   * as the Level0Mission that it is scheduling.  
   */
  // @Allocate( { Area.CURRENT })
  // @SCJAllowed
  final RelativeTime getCycleDuration() {
      return null;
  }

  /**
   * Returns the array of Frames that represents this particular
   * CyclicSchedule. The elements of the Frames array are sorted
   * according to chronological order.  So the event handlers
   * represents by entry 0 are executed before the event handlers of
   * entry 1, and so on.
   * <p>
   * The returned array is shared with this
   * CyclicSchedule and is intended to be treated as read-only.
   * Any modifications to the array will have potentially disastrous,
   * but undefined results.  The returned array resides in the same
   * scope as this CyclicSchedule object.  Under normal circumstances,
   * this CyclicSchedule object will reside in the MissionMemory that
   * corresponds to the Level0Mission that is being scheduled.
   *
   * TBD: is it ok to make this package acccess and final?  Doing so
   * would allow us to reduce the need for allocating array copies.
   */
   // @SCJAllowed
   // @Allocate({ THIS })
  final Frame[] getFrames()
  {
    return null;
  }
}
