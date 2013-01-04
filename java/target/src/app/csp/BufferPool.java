package csp;

public class BufferPool {

	private Buffer[] pool;
	private int used = 0;

	public BufferPool() {

		pool = new Buffer[Constants.MAX_BUFFER_COUNT];

		// Initialize buffer pool
		for (int i = 0; i < Constants.MAX_BUFFER_COUNT; i++) {
			pool[i] = new Buffer();
		}

	}

	public Buffer getCSPbuffer() {

		// Look into all the buffers until a free one is found
		for (int i = 0; i < Constants.MAX_BUFFER_COUNT; i++) { // @WCA loop = Constants.MAX_BUFFER_COUNT
			if (pool[i].free) {
				pool[i].free = false;
				used++;
				return pool[i];
			}
		}

		System.out.println("No available buffers");
		return null;
	}

	public void freeCSPbuffer(Buffer buffer) {
		buffer.free = true;
		used--;
	}
	
	public int getOccupancy(){
		return Constants.MAX_BUFFER_COUNT - used;
	}
}
