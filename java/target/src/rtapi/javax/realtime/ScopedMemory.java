package javax.realtime;

import com.jopdesign.io.IOFactory;
import com.jopdesign.sys.Scope;

public abstract class ScopedMemory extends MemoryArea {

	Scope sc;

	public ScopedMemory(long size) {
		// super does nothing, but we have to invoke it
		super(size);
		sc = new Scope(size);
	}

	public ScopedMemory(int[] localMem) {
		// super does nothing
		super(0);
		sc = new Scope(localMem);
	}
	
	ScopedMemory(Object type, long size) {
		// super does nothing
		super(0);
		if (type==PhysicalMemoryManager.ON_CHIP_PRIVATE) {
			sc = new Scope(IOFactory.getFactory().getScratchpadMemory());
		} else {
			sc = new Scope(size);
		}
		// TODO Auto-generated constructor stub
	}

	public void enter(Runnable logic) throws RuntimeException {

		sc.enter(logic);
		
	}
	
	public long size() {
		return sc.getSize();
	}

}
