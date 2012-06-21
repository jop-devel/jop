package csp;

import csp.*;

import java.util.Vector;

import util.Timer;

import joprt.RtThread;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

public class TestLocalChannels implements Runnable {
	
	// supposed to be shared!
	static volatile MessageList messages = new MessageList();
	static Vector msg;

		
	int id;

	public TestLocalChannels(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		msg = new Vector();

		// TODO Auto-generated method stub
		System.out.println("Hello CSP world from processor 0.");

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		Runnable r = new TestLocalChannels(1);
		Startup.setRunnable(r, 0);

		// start the other CPUs
		sys.signal = 1;
		// set the WD LED for the simulation
		sys.wd = 1;

		// an In channels requires an Out end for sending ACK
		InPort c_in = new InPort(2, messages);
		OutPort ack = new OutPortLocal(1, messages);
		Channel ch = new Channel(ack, c_in);
		int inbuf[];
		System.out.println("P0 to receive.");
		RtThread.sleepMs(10);
		int size = msg.size();
		if (size!=0) {
			StringBuffer sb = (StringBuffer) msg.remove(0);
			System.out.println(sb);
		}			
		inbuf = ch.receive();
		System.out.println("received "+inbuf[0]+", "+inbuf[1]+", "+inbuf[2]);
		System.out.println("P0 done.");		
		
	}

	public void run() {
		int outbuf[] = {2,13,15};
		StringBuffer sb = new StringBuffer();
		sb.append("Hello World from CPU ");
		sb.append(id);

		// an out channel requires and In end for receiving ACK
		OutPort c_out = new OutPortLocal(2, messages);
		InPort  ack = new InPort(1, messages);
		Channel ch = new Channel(c_out, ack);
		sb.append("P1 to send.");
		msg.addElement(sb);
		ch.send(outbuf, 3);
		sb.append("P1 done.");
		msg.addElement(sb);	
	//	RtThread.sleepMs(id*100);

	}

	
}
