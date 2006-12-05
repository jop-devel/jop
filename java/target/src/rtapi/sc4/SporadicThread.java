package sc4;

public abstract class SporadicThread extends RealtimeThread {

	public SporadicThread(String event, int minInterval, int deadline, int memSize) {
		super(event, minInterval, deadline, memSize);
	}
	
	public SporadicThread(String event, int minInterval) {
		super(event, minInterval, 0, 0);		
	}

	public void fire() {
		RealtimeSystem.fire(this);
	}
	
}
