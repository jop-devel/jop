package csp;

public class NoCOutChannel extends OutChannel {
	// these are the IDs of the destination
	int hostID;
	int localID;
	
	public NoCOutChannel(InChannel inEnd, int hostID, int localID) {
		super(inEnd);
		this.localID = localID;
		this.hostID = hostID;
	}

	// This should be handled better - could be problems
	// with concurrent threads trying to write at the same time
	protected void nb_send(int[] buffer, int cnt) {
		while(!NoC.sendIfFree(hostID, localID, cnt, buffer)); 
	}

}
