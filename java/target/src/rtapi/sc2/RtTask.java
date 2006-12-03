package sc2;

public abstract class RtTask {
	
	protected RtLogic logic;
	
	public RtTask(RtLogic logic, int period, int deadline, int offset) {
		this.logic = logic;
	}
	
	public RtTask(RtLogic logic, int period, int deadline) {
		this(logic, period, deadline, 0);
	}
	
	public RtTask(RtLogic logic, int period) {
		this(logic, period, period, 0);
	}

}
