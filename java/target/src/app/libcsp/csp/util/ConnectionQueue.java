package libcsp.csp.util;

import libcsp.csp.core.ConnectionCore;

/*
 * Once in a while it must be possible to search for a specific connection in the queue using
 * an id (src, sport, dst, dport), hence Queue<T> is extended and type parameterized 
 * with Connection. This would not be possible using the Queue<T> as T is a generic type
 */
public class ConnectionQueue extends Queue<ConnectionCore> {

	public ConnectionQueue(byte capacity) {
		super(capacity);
	}
		
	/*
	 * Checks the ids of the connections in the queue with 
	 * the supplied connection id and returns the matching 
	 * connection if found
	 */
	public synchronized ConnectionCore getConnection(int id) {
		Element element = null;
		
		for(byte i = 0; i < super.count; i++) {
			if(i == 0) {
				element = super.head;
			}
			if(element.value.id == id) {
				break;
			}				
			element = (element.next == null ? super.start : element.next);
		}
		return element.value;
	}
}
