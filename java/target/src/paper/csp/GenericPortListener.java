package csp;

import java.io.IOException;
// generic class for receiving messages in any medium
// The subclasses should handle specific media
// and implement getOneMessage that stores the
// message in the local variables
abstract public class GenericPortListener implements Runnable {
	MessageList globalList;
	final static int MAX_RCVBUF = 256;
//	int sourceAddress;
	int buffer[];
	int count;
	
	public GenericPortListener(MessageList globalList) {
		this.globalList = globalList;
		buffer = new int[MAX_RCVBUF];
		count = 0;
	}
	
	// this is the method all should implement to listen to the channel
	abstract void getOneMessage() throws IOException;
	
	public void run() {
		// MS: there is no Thread.interrupted in JOP. One should use periodic threads.
		// This code might have never been executed on JOP.... so shall we keep it?
//		while(!Thread.interrupted()) {
		while (true) {
			try {
				getOneMessage();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			// save it in the NoCMessageList
			globalList.add(buffer, count);
			count = 0;
		}
	}

}
