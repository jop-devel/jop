package csp;
// Listens to the NoC and receives messages
// that are placed in a common list.
// Other type of channels, such as Serial ports, should have
// their own listeners, but still use the same common list
//
// NOTE: this should be RTThread?
public class NoCListener extends GenericChannelListener {
	// initialization stuff
	public NoCListener(MessageList globalList) {
		super(globalList);
		// should this be done somewhere else?
		NoC.initialize();
	}
	
	// main loop for reading messages from the network
	public void getOneMessage() {		
			// wait for a message
			while(!NoC.isReceiving());
			// must start receiving here
			sourceAddress = NoC.getSourceAddress();
			count = 0;
			// receive it all
			do {
				// read all i can get in the buffer
				while(!NoC.isReceiveBufferEmpty()) 
					buffer[count++] = NoC.readData();
			} while(!(NoC.isEoD() && NoC.isReceiveBufferEmpty()));
			// Need to reset for another receive!
			NoC.writeReset();
			// the superclass takes care of storing the message
	}
}
