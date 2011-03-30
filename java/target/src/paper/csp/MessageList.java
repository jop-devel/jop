package csp;

// circular list of messages, dummy first node
public class MessageList {
	
	// used to store received messages
	protected class MessageEntry {
		// source address
		// DONT NEED THIS really!
//		int address;
		// first word in the buffer, the local id
		int message0;
		// the rest of the
		int message[];
		
		MessageEntry next;
		
		public MessageEntry() {
//			this.address = address;
			message = null;
			next = null;
		}
		
		// constructor that splits the header
		public MessageEntry(int buf[], int cnt) {
//			address = src;
			message = new int[cnt-1];
			System.arraycopy(buf,1,message,0,cnt-1);
			message0 = buf[0];
			next = null;
		}

		// constructor that already has the header split
		public MessageEntry(int buf0, int buf[], int cnt) {
//			address = src;
			message = new int[cnt];
			System.arraycopy(buf,0,message,0,cnt);
			message0 = buf0;
			next = null;
		}

		
	}
	
	volatile MessageEntry first;
	volatile MessageEntry last;
	
	public MessageList() {
		first = last = new MessageEntry();
		first.next = first;
	}

	// adds a new entry to the end of the list
	private synchronized MessageEntry add(MessageEntry m) {
		m.next = last.next;
		last.next = m;
		last = m;
		return m;
	}
	
	// adds a new entry from elementary data
	// just to make synchronized part small
	// NOTE: Local channels can use this as a "send" method
	public MessageEntry add(int buf[], int cnt) {
		MessageEntry m = new MessageEntry(buf,cnt);
		return add(m);
	}

	public MessageEntry add(int buf0, int buf[], int cnt) {
		MessageEntry m = new MessageEntry(buf0, buf,cnt);
		return add(m);		
	}
	
	// deletes an entry from the list
	private synchronized void del(MessageEntry m) {
		MessageEntry n;
		for(n=first; (n.next != m) && (n.next != first); n = n.next);
		if(n.next == first) // not found
			return;
		if(m == last) last = n;
		n.next = m.next;
	}
	
	
	
	// useful for finding exact channels!
	// could be sped up if the first word in the message is also stored
	// in a separate attribute
	private synchronized MessageEntry find(int id) {
		MessageEntry n;
		for(n = first.next; (n != first) && (n.message0 != id); n = n.next);
		if(n == first) // not found
			return null;
		return n;	
	}
	
	// this is the actual function that should be used by channels
	// returns null if no message is received
	// returns a table of int otherwise
	// used for POLLING
	public int[] receive(int adr) {
		MessageEntry m = find(adr);
		if(m == null) { 
			return null; 	// not found
		}
		del(m);
		return m.message;
	}
	
	public void print(String m) {
		MessageEntry n;
		System.out.print(m+"[");
		System.out.print("("+first+"@ "+first.message0+" "+first.next+")");
		for(n = first.next; (n != first); n = n.next)
		{
			System.out.print("("+n+"@ "+n.message0+" "+n.next+")");
		}
		System.out.println("]");
	}

}