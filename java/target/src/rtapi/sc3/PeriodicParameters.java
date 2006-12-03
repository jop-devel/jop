package sc3;

public class PeriodicParameters {

	RelativeTime start, period, deadline;
	
	public PeriodicParameters(RelativeTime start,
			RelativeTime period,
			RelativeTime cost,
			RelativeTime deadline) {
	
		this.start = start;
		this.period = period;
		// I don't know what to do with cost
		this.deadline = deadline;
	}
}
