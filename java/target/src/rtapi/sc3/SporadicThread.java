package sc3;

public class SporadicThread extends RealtimeTask {
	
	public SporadicThread(SporadicParamters sp, String happening) {
		super(happening, sp.period, sp.deadline);
	}
	
	public void run() {
		// nothing to do
	}

	public void fire() {
		RTlet.fire(this);
	}
}
