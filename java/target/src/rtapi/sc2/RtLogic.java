package sc2;

public interface RtLogic extends Runnable {

	public abstract void init();
	public abstract void terminate();
	public abstract void restart();
}
