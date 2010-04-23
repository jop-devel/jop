package rttm.common;


public class LinkedList {
	
	LinkedObject head = null;
	LinkedObject tail = null;
	
	public void insertAtTail(LinkedObject entry) {
		entry.next = null;
		if (tail == null) {
			head = entry;
		} else {
			tail.next = entry;
		}
		tail = entry;
	}
	
	/**
	 * @return null if list is empty.
	 */
	public LinkedObject removeFromHead() {		
		LinkedObject result = head;
		if (head != null) {
			head = head.next;
			if (head == null) {
				tail = null;
			}
		}
		return result;
	}
	
	public int size() {
		int size = 0;
		for (LinkedObject o = head; o != null; o = o.next) {
			size++;
		}
		return size;
	}
	
	public static class LinkedObject {
		Object data;
		LinkedObject next;
		
		public LinkedObject(Object data) {
			this.data = data;
		}
		
		public Object getData() {
			return data;
		}
		
		public void setData(Object data) {
			this.data = data;
		}		
	}
}
