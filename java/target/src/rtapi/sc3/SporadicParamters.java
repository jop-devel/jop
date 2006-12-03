package sc3;

public class SporadicParamters {

	RelativeTime period, deadline;
	
	public SporadicParamters(RelativeTime minInterval,
			RelativeTime cost,
			RelativeTime deadline) {
	
		this.period = minInterval;
		// I don't know what to do with cost
		this.deadline = deadline;
	}
}
