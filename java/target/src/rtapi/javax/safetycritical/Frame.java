package javax.safetycritical;

import javax.realtime.RelativeTime;
import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

@SCJAllowed
public final class Frame {
	/**
	 * Allocates and retains private shallow copies of the duration and handlers
	 * array within the same memory area as this. The elements within the copy
	 * of the handlers array are the exact same elements as in the handlers
	 * array. Thus, it is essential that the elements of the handlers array
	 * reside in memory areas that enclose this. Under normal circumstances,
	 * this Frame object is instantiated within the MissionMemory area that
	 * corresponds to the Level0Mission that is to be scheduled.
	 * <p>
	 * Within each execution frame of the CyclicSchedule, the
	 * PeriodicEventHandler objects represented by the handlers array will be
	 * fired in same order as they appear within this array. Normally,
	 * PeriodicEventHandlers are sorted into decreasing priority order prior to
	 * invoking this constructor.
	 */
	
	RelativeTime duration_;
    PeriodicEventHandler[] handlers_;
	
	@Allocate({ Area.THIS })
	@MemoryAreaEncloses(inner = { "this", "this" }, outer = { "duration",
			"handlers" })
	@SCJAllowed
	public Frame(RelativeTime duration, PeriodicEventHandler[] handlers) {
		
		duration_ = duration;
		handlers_ = handlers;
	}
	
	
}