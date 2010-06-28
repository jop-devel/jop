package rttm.WaitFreeQueue;

public abstract class WaitFreeReadWriteQueue {
	protected Object[] theQueue;
	protected int head;
	protected int tail;
	protected int queueSize;
	protected MemoryArea memArea;
	protected SemHolder fullSem;
	protected SemHolder emptySem;

	public WaitFreeReadWriteQueue(int capacity) {
		System.out.println("WaitFreeReadWriteQueue constructor");
		this.head = 0;
		this.tail = 0;
		this.queueSize = 0;
		init(capacity);
	}

	private void init(int capacity) throws IllegalArgumentException {
		System.out.println("WaitFreeReadWriteQueue init");
		if (capacity <= 0)
			throw new IllegalArgumentException(
					"maximum cannot be less than or equal to 0");
		this.emptySem = new SemHolder(capacity);
		this.fullSem = new SemHolder(0);

		this.memArea = ImmortalMemory.instance();
		this.theQueue = this.memArea.newObjectArray(capacity + 1);
		this.queueSize = (capacity + 1);
	}

	// public void clear() {
	// this.head = (this.tail = 0);
	// while (this.fullSem.tryDown())
	// this.emptySem.up();
	// }

	public boolean isEmpty() {
		return (this.fullSem.getCount() == 0);
	}

	public boolean isFull() {
		return (this.emptySem.getCount() == 0);
	}

	public int size() {
		if (this.head == this.tail) {
			if (isEmpty())
				return 0;
			return this.queueSize;
		}
		if (this.head > this.tail)
			return (this.queueSize - (this.head - this.tail));
		return (this.tail - this.head);
	}

	public abstract Object read(QueueThread queueThread);

	public abstract boolean write(Object paramObject, QueueThread queueThread);

}
