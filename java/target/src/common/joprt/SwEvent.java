package joprt;

public class SwEvent extends RtThread {


	public SwEvent(int priority, int minTime) {

		super(priority, minTime);
		thr.setEvent();
	}

	public final void fire() {
		thr.fire();
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
			thr.blockEvent();
		}
	}

	public void handle() {
	}

}
