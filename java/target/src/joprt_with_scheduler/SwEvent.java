package joprt;

public class SwEvent extends RtThread {


	public SwEvent(int priority, int minTime) {
		super(priority, minTime);
isEvent = true;
	}

	public final void fire() {
		PriorityScheduler.event[this.nr] = PriorityScheduler.EV_FIRED;
		// if prio higher...
// should not be allowed befor startMission
		Scheduler.genInt();
	}

	public final void run() {

// shure to not run on startThread:
/* not necessary: run gets called on first schedul
if (event[this.nr] == EV_WAITING) {
	RtThread.genInt();	// schedule another thread
}
*/

		for (;;) {
			handle();
			// oder so? PriorityScheduler.handlerDone();
			PriorityScheduler.event[this.nr] = PriorityScheduler.EV_WAITING;
			Scheduler.genInt();
		}
	}

	public void handle() {
	}

}
