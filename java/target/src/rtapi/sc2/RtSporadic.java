package sc2;

public class RtSporadic extends RtTask {

	/**
	 * 
	 * @param event A HW interrupt
	 * @param logic 
	 * @param period
	 * @param deadline
	 * @param offset
	 */
	public RtSporadic(String event, RtLogic logic, int period, int deadline, int offset) {
		super(logic, period, deadline, offset);
	}

	public RtSporadic(String event, RtLogic logic, int period, int deadline) {
		super(logic, period, deadline);
	}

	public RtSporadic(String event, RtLogic logic, int period) {
		super(logic, period);
	}

	/**
	 * Trigger a software event
	 *
	 */
	public void fire() {
		// fire an event
	}
}
