package csp;

// This is the Sink or Destination end of a CSP channel
public class InPort {
	MessageList globalList;
	int localID;
//	OutPort outEnd; // for ack
	
	public InPort(int localID, MessageList globalList) {
		this.globalList = globalList;
		this.localID = localID;
//		this.outEnd = outEnd;
	}
	
	// nonblocking receive, does not require ack
	protected int[] noAck_receive() {
		int m[] = null;
		do {
			//globalList.print("rec list");
			m = globalList.receive(localID);
		} while(m == null);
		return m;
	}	
}