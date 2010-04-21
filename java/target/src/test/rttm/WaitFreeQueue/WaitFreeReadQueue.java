package rttm.WaitFreeQueue;

public class WaitFreeReadQueue extends WaitFreeReadWriteQueue {

	public WaitFreeReadQueue(int capacity) {
		super(capacity);
		System.out.println("WaitFreeReadQueue constructor");
	}

	public Object read(QueueThread queueThread) {
		Object localObject = null;
		if (this.fullSem.tryDown(queueThread)) {
			localObject = this.theQueue[this.head];
			this.head = ((this.head + 1) % this.queueSize);
			this.emptySem.up(queueThread);
		}
		return localObject;
	}

	public boolean write(Object paramObject, QueueThread queueThread) {
		synchronized (this) {
			this.emptySem.down(queueThread);
			this.theQueue[this.tail] = paramObject;
			this.tail = ((this.tail + 1) % this.queueSize);
		}
		this.fullSem.up(queueThread);
		return true;
	}
}
