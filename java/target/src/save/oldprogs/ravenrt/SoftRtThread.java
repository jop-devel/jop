package ravenrt;

public class SoftRtThread extends RtThread {

	public SoftRtThread(int priority) {
		super(priority-RtThread.SOFT_RANGE, null);
	}

	public void sleep(RelativeTime time) {
	}

}
