package rttm.common;

import rttm.atomic; 

public class AtomicLinkedList<T> {
	
	LinkedObject<T> head = null;
	LinkedObject<T> tail = null;
	
	@atomic public void insert(LinkedObject<T> entry) {
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
	@atomic public LinkedObject<T> remove() {		
		LinkedObject<T> result = head;
		if (head != null) {
			head = head.next;
			if (head == null) {
				tail = null;
			}
		}
		return result;
	}
	
	@atomic public int size() {
		int size = 0;
		for (LinkedObject<T> o = head; o != null; o = o.next) {
			size++;
		}
		return size;
	}
	
	public static class LinkedObject<T> {
		T data;
		LinkedObject<T> next;
		
		public LinkedObject(T data) {
			this.data = data;
		}
		
		public T getData() {
			return data;
		}
		
		public void setData(T data) {
			this.data = data;
		}		
	}
}
