package csp;


// messages are untyped
public class OutPortLocal implements OutPort {
	MessageList globalList;
	int dstID;
	
	public OutPortLocal(int dstID, MessageList globalList) {
		this.globalList = globalList;
		this.dstID = dstID;
	}
	
	// normally you would not do both send and receive
	// For out channels the localID is the destination id
	// For in channels the localID is still the destination id
	public void noAck_send(int buffer[], int cnt) {
		globalList.add(dstID, buffer, cnt);
	}
	
}
