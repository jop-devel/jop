package rttm.idioms;

import rttm.Atomic;
import rttm.swtest.AtomicLinkedList;
import rttm.swtest.AtomicLinkedList.LinkedObject;

public class Privatization {

	AtomicLinkedList<String> atomicList;
	
	@Atomic public void insertString(String data, LinkedObject<String> lo) {
		atomicList.insert(lo);
		lo.setData(data);
	}
	
	public void removeAndPrintString() {
		LinkedObject<String> lo = atomicList.remove(); // remove atomically 
		// do I/O outside of transaction
		if (lo != null) {
			System.out.println(lo.getData()); // this is safe
		}
	}
	
}
