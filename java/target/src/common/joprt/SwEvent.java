package joprt;

public class SwEvent extends RtThread {


	public SwEvent(int priority, int minTime) {
		super(priority, minTime);
isEvent = true;
	}

	public final void fire() {
		event[this.nr] = EV_FIRED;
		// if prio higher...
// should not be allowed befor startMission
		RtThread.genInt();
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
			event[this.nr] = EV_WAITING;
			RtThread.genInt();
		}
	}

	public void handle() {
	}

}
