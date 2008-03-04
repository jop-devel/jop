package javax.realtime;

import com.jopdesign.sys.Scope;

public abstract class ScopedMemory extends MemoryArea {

	Scope sc;

	public ScopedMemory(long size) {
		// super does nothing
		super(size);
		sc = new Scope(size);
	}

	public ScopedMemory(int[] localMem) {
		// super does nothing
		super(0);
		sc = new Scope(localMem);
	}
	public void enter(Runnable logic) throws RuntimeException {

		sc.enter(logic);
		
	}
	
	public long size() {
		return sc.getSize();
	}

}
