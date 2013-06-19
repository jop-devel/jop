package prelude;

public abstract class PreludeTask {
	public final String name;

	public final int period;
	public final int initialRelease;
	public final int wcet;
	public final int deadline;

	public abstract void run();

	public PreludeTask(String name, 
					   int period, int initialRelease, int wcet, 
					   int deadline) {
		this.name = name;
		this.period = period;
		this.initialRelease = initialRelease;
		this.wcet = wcet;
		this.deadline = deadline;
	}
}
