package test;

import csp.*;
import java.io.*;

// This app tests the Stream based channels:
// In fact only the direct channel is a Stream (PipedStream)
// while the ack channel is a regular Local channel
public class TestStreamChannels {
	
	static MessageList messages = new MessageList();
	
	static PipedOutputStream po1 = new PipedOutputStream();
    static PipedInputStream pi2 = new PipedInputStream();
    
	// gotta make 2 threads that send messages to each other
	public class T1 extends Thread {
		
		
		public void run() {			
			DataOutputStream os2 = new DataOutputStream(po1);
			
			int outbuf[] = {2,13,15};
			// an out channel requires and In end for receiving ACK
			OutPort c_out = new OutPortStream(2, os2);
			InPort  ack = new InPort(1, messages);
			Channel ch = new Channel(c_out, ack);
			ch.send(outbuf, 3);
			System.out.println("T1 done.");
		}
	}
	
	// gotta make 2 threads that send messages to each other
	public class T2 extends Thread {

		public void run() {
			// start listening to this stream!
			DataInputStream is2 = new DataInputStream(pi2);
			StreamListener sl = new StreamListener(messages, is2);
			Thread slt = new Thread(sl);
			slt.start();
			
			// an In channels requires an Out end for sending ACK
			InPort c_in = new InPort(2, messages);
			OutPort ack = new OutPortLocal(1, messages);
			Channel ch = new Channel(ack, c_in);
			int inbuf[];			
			inbuf = ch.receive();
			System.out.println("received "+inbuf.length+": "+inbuf[0]+", "+inbuf[1]+", "+inbuf[2]);
			System.out.println("T2 done.");
			
			// kill the thread that is listening to that stream
			slt.interrupt();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestStreamChannels tt=new TestStreamChannels();
		
		try {
			pi2.connect(po1);
//			po1.connect(pi2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tt.new T2().start();
		tt.new T1().start();
	}

}
