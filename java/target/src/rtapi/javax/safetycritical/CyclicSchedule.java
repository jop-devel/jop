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
	
	private Frame[] frames_;
	private RelativeTime cycleDuration;
	
  @Allocate({ Area.THIS })
  @MemoryAreaEncloses(inner = { "this" }, outer = { "frames" })
  @SCJAllowed
  public CyclicSchedule(Frame[] frames)
  {
	  frames_ = new Frame[frames.length];
	  System.arraycopy(frames, 0, frames_, 0, frames.length);
	  cycleDuration = new RelativeTime();
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
	  
	  for (int i = 0; i < frames_.length-1; i++){
		  cycleDuration = cycleDuration.add(cycleDuration,frames_[i].duration_);
	  }
	  
      return cycleDuration;
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
    return frames_;
  }
}
