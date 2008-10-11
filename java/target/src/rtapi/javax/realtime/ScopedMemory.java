package javax.realtime;

import com.jopdesign.io.IOFactory;
import com.jopdesign.sys.Scope;
import com.jopdesign.sys.Startup;

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
	
	/**
	 * We can only use one physical memory per CPU core
	 */
	private static boolean physInUse[] = new boolean[Runtime.getRuntime().availableProcessors()];

	/**
	 * Package private constructor to be used by LTPhysicalMemory
	 * @param type
	 * @param size
	 */
	ScopedMemory(Object type, long size) {
		// super does nothing
		super(0);
		if (type==PhysicalMemoryManager.ON_CHIP_PRIVATE) {
			synchronized(physInUse) {
				if (size>Startup.getSPMSize()) {
					throw new RuntimeException("Local memory is not big enough");
				}
				IOFactory fact = IOFactory.getFactory();
				if (physInUse[fact.getSysDevice().cpuId]) {
					throw new RuntimeException("Physical memory already in use");
				}
				physInUse[fact.getSysDevice().cpuId] = true;				
				sc = new Scope(fact.getScratchpadMemory());
			}
		} else {
			sc = new Scope(size);
		}
	}

	public void enter(Runnable logic) throws RuntimeException {
		sc.enter(logic);
	}
	
	public long size() {
		return sc.getSize();
	}

}
