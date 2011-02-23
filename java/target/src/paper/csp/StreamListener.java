package csp;

import java.io.DataInputStream;
import java.io.IOException;

public class StreamListener extends GenericPortListener {
	DataInputStream is;
	// initialization stuff
	public StreamListener(MessageList globalList, DataInputStream is) {
		super(globalList);
		this.is = is;
	}
	
	@Override
	void getOneMessage() throws IOException {
		count = is.readInt();
		for(int i=0;i<count;i++) buffer[i] = is.readInt();
	}
}
