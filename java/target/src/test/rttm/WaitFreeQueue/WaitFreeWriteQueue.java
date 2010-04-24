package rttm.WaitFreeQueue;

public class WaitFreeWriteQueue extends WaitFreeReadWriteQueue {

	public WaitFreeWriteQueue(int capacity) {
		super(capacity);
		System.out.println("WaitFreeWriteQueue constructor");
	}

	public synchronized Object read(QueueThread queueThread) {
		Object localObject = null;
		while (this.fullSem.tryDown(queueThread)) {
			localObject = this.theQueue[this.head];
			this.head = ((this.head + 1) % this.queueSize);
			this.emptySem.up(queueThread);
		}
		System.out.println("empty");
		return localObject;
	}

	public boolean force(Object paramObject, QueueThread queueThread) {
		if (write(paramObject, queueThread))
			return false;
		int i = this.tail;
		if (i == 0)
			i = this.queueSize - 1;
		this.theQueue[(--i)] = paramObject;
		return (this.tail != this.head);
	}

	public boolean write(Object paramObject, QueueThread queueThread) {
		if (this.emptySem.tryDown(queueThread)) {
			this.theQueue[this.tail] = paramObject;
			this.tail = ((this.tail + 1) % this.queueSize);
			this.fullSem.up(queueThread);
			return true;
		}
		return false;
	}
}