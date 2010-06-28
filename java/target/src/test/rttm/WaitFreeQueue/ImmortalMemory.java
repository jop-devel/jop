package rttm.WaitFreeQueue;

public class ImmortalMemory {

	public static MemoryArea instance() {
		System.out.println("ImmortalMemory.instance");
		return new MemoryArea();
	}

}
