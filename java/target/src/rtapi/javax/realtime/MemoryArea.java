package javax.realtime;

public abstract class MemoryArea {

	public MemoryArea(long size) {
		super();
		// create the memory area
	}

	public void enter(Runnable logic) {

	}
	
	public long size() {
		// dummy return
		return 0L;
	}

}
