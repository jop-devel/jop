package csp;

public class BufferPool {


	// Next free CSPbuffer
	private int next = 0;

	//
	private CSPbuffer[] pool;

	public BufferPool() {

		pool = new CSPbuffer[Conf.MAX_BUFFER_COUNT];

		// Initialize buffer pool
		for (int i = 0; i< Conf.MAX_BUFFER_COUNT; i++){
			pool[i] = new CSPbuffer();
		}


	}

	public CSPbuffer getCSPbuffer(){

			// Look into all the buffers until a free one is found
			for(int i = 0; i < Conf.MAX_BUFFER_COUNT; i++){
				if (pool[i].free){
					pool[i].free = false;
					return pool[i];
				}
			}

			System.out.println("No available buffers");
		return null;
	}

	public void freeCSPbuffer(CSPbuffer buffer){

		buffer.free = true;

	}
}
