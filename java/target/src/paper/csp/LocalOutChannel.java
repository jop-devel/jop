package csp;

// just a primitive channel type...
// messages are untyped
public class LocalOutChannel extends OutChannel {
	MessageList globalList;
	int dstID;
	InChannel inEnd;
	
	public LocalOutChannel(InChannel inEnd, int dstID, MessageList globalList) {
		super(inEnd);
		this.globalList = globalList;
		this.dstID = dstID;
	}
	
	// normally you would not do both send and receive
	// For out channels the localID is the destination id
	// For in channels the localID is still the destination id
	protected void nb_send(int buffer[], int cnt) {
		globalList.add(dstID, buffer, cnt);
	}
	
}
