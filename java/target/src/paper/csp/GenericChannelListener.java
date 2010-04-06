package csp;
// generic class for receiving messages in any medium
// The subclasses should handle specific media
// and implement getOneMessage that stores the
// message in the local variables
abstract public class GenericChannelListener implements Runnable {
	MessageList globalList;
	final static int MAX_RCVBUF = 256;
	int sourceAddress;
	int buffer[];
	int count;
	
	public GenericChannelListener(MessageList globalList) {
		this.globalList = globalList;
		buffer = new int[MAX_RCVBUF];
		count = 0;
	}
	
	// this is the method all should implement to listen to the channel
	abstract void getOneMessage();
	
	public void run() {
		while(true) { 
			getOneMessage();
			// save it in the NoCMessageList
			globalList.add(sourceAddress, buffer, count);
			count = 0;
		}
	}

}
