package sc3;

public class PeriodicThread extends RealtimeTask {
	
	public PeriodicThread(PeriodicParameters pp) {
		super(pp.period, pp.deadline, pp.start);
	}
	
	public void run() {
		// nothing to do
	}

}
