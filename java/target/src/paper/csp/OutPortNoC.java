package csp;

public class OutPortNoC implements OutPort {
	// these are the IDs of the destination
	int hostID;
	int localID;
	
	// inEnd is required for receiving the ack for each message
	// hostID and localID are the destination channel NoC address and local ID
	public OutPortNoC(InPort inEnd, int hostID, int localID) {
		this.localID = localID;
		this.hostID = hostID;
	}

	// This should be handled better - could be problems
	// with concurrent threads trying to write at the same time
	public void noAck_send(int[] buffer, int cnt) {
		while(!NoC.sendIfFree(hostID, localID, cnt, buffer)); 
	}

}
