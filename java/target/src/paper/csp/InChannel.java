package csp;

// InChannel does not need more than this!
public class InChannel {
	MessageList globalList;
	int localID;
	OutChannel outEnd;
	
	public InChannel(OutChannel outEnd, int localID, MessageList globalList) {
		this.globalList = globalList;
		this.localID = localID;
		this.outEnd = outEnd;
	}
	
	// nonblocking receive, does not wait for ack
	protected int[] nb_receive() {
		int m[] = null;
		do {
			m = globalList.receive(localID);
		} while(m == null);
		return m;
	}
	
	public int[] receive() {
		int r[] = nb_receive();
		// in true CSP semantics, this receive must be acknowledged
		// back to the sender
		outEnd.nb_send(r, 0);
		return r;
	}
}