package ravenrt;

public class RtThread extends RtTask {

	private RtThread() {}

	protected static final int SOFT_RANGE = 10;
	public static final int HARD_RANGE = 10;

	public static final int MIN_PRIORITY = Thread.MAX_PRIORITY+SOFT_RANGE+1;
	public static final int MAX_PRIORITY = MIN_PRIORITY+HARD_RANGE;

	public RtThread(int priority, RelativeTime period) {
	}

	public RtThread(int priority, RelativeTime period, Memory mem) {
	}


	public void run() {
	}

	public boolean waitForNextPeriod() {
		return true;
	}

}
