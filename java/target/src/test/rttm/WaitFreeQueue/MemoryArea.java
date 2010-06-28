package rttm.WaitFreeQueue;

public class MemoryArea {

	private Object[] objectArray;

	public Object[] newObjectArray(int size) {
		System.out.println("MemoryArea.newObjectArray");
		this.objectArray = new Object[size];
		return this.objectArray;
	}
}
