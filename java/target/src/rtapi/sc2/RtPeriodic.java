package sc2;

public class RtPeriodic extends RtTask {

	/**
	 * 
	 * @param logic
	 * @param period in micro seconds
	 * @param deadline in micor seconds
	 * @param offset in micro seconds
	 */
	public RtPeriodic(RtLogic logic, int period, int deadline, int offset) {
		super(logic, period, deadline, offset);
	}

	public RtPeriodic(RtLogic logic, int period, int deadline) {
		super(logic, period, deadline);
	}

	public RtPeriodic(RtLogic logic, int period) {
		super(logic, period);
	}

}
