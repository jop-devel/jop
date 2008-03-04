package javax.realtime;

public abstract class MemoryArea {

	public MemoryArea(long size) {
	}

	public void enter(Runnable logic) {
		// dummy enter
	}
	
	public long size() {
		// dummy return
		return 0L;
	}

}
