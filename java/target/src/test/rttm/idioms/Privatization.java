package rttm.idioms;

import rttm.atomic;
import rttm.common.AtomicLinkedList;
import rttm.common.AtomicLinkedList.LinkedObject;

public class Privatization {

	AtomicLinkedList<String> atomicList;
	
	@atomic public void insertString(String data, LinkedObject<String> lo) {
		atomicList.insert(lo);
		lo.setData(data);
	}
	
	public void removeAndPrintString() { // privatization
		LinkedObject<String> lo = atomicList.remove(); // remove atomically 
		// do I/O outside of transaction
		if (lo != null) {
			System.out.println(lo.getData()); // this is safe
		}
	}
	
}
