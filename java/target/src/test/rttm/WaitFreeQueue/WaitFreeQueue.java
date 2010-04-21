package rttm.WaitFreeQueue;

public abstract class WaitFreeQueue {

	public abstract Object poll(long msecs);

	public abstract Object take();

	public abstract Object peek();

	public abstract boolean offer(Object x, long msecs);

	public abstract void put(Object x);

}
