package sc4;

public abstract class PeriodicThread extends RealtimeThread {

	public PeriodicThread(int period, int deadline, int offset, int memSize) {
		super(period, deadline, offset, memSize);
	}
	
	public PeriodicThread(int period) {
		super(period, period, 0, 0);		
	}

}
