package ejip123.examples;

import com.jopdesign.sys.Const;
import ejip123.*;
import joprt.RtThread;
import ejip123.util.Dbg;
import ejip123.util.DbgUdp;
import ejip123.util.Serial;
import util.Timer;

/**
 This example shows how the pinging interfaces should be implemented and used. Jop starts pinging himself, a host and
 the whole local net.
 */
public class HelloWorldHereIPing{
private HelloWorldHereIPing(){
}

	final static boolean TWO_SERIAL = false;

public static void main(String[] args){

	PacketPool.init(10, 1500); // inits 10 packet buffers with 1500B each

	Serial ser;
	if (TWO_SERIAL) {
		Dbg.initSer(); // serial debug output		
		ser = new Serial(10, 1000, Const.IO_UART_BG_MODEM_BASE); // simulator
	} else {
		DbgUdp.init(); // sends debug output over the network to 192.168.2.1:10000 (see init method)		
		ser = new Serial(10, 1000, Const.IO_UART1_BASE); // one byte every ~400us at 19200 baud
	}

	Router.init(3); // initializes a routing table with 3 routes
	LinkLayer slip = Slip.init(9, 10000, ser, Ip.Ip(192, 168, 1, 2), 1500);
	Ip.init(6, 50000); // ip (and therefore icmp and tcp) loop thread: period 50ms
	Router.addRoute(new Route(Ip.Ip(192, 168, 2, 0), Ip.Ip(255, 255, 255, 0), slip));
	Router.setDefaultInterface(slip); // where should packets go which are not matched by a route?

	RtThread.startMission();

	Router.print();
	forever();
}

private static void forever(){
	int toggle = 0;

	PingReplyHandler loopHandler = new PingReplyHandler(){
		public void pingReply(int ms){
			Dbg.wr("got reply from myself after ");
			Dbg.intVal(ms);
			Dbg.wr("ms\n");
		}

		public void pingTimeout(){
			Dbg.wr("ping from myself timed out. i don't feel well, i guess.\n");
		}
	};
	PingReplyHandler pingHandler = new PingReplyHandler(){
		public void pingReply(int ms){
			Dbg.wr("got reply from host after ");
			Dbg.intVal(ms);
			Dbg.wr("ms\n");
		}

		public void pingTimeout(){
			Dbg.wr("ping timed out.\n");
		}
	};

	for(; ;){
		switch(toggle++){
			case 0:
				Dbg.wr("pinging host... ");
				if(!Icmp.ping(Ip.Ip(192, 168, 2, 1), pingHandler))
					Dbg.wr("another ping in progress.\n");
				break;
			case 1:
				Dbg.wr("pinging loopback... ");
				if(!Icmp.ping(Ip.Ip(127, 0, 0, 1), loopHandler))
					Dbg.wr("another ping in progress.\n");
				break;
			case 2:
				Dbg.wr("pinging bc net... ");
				if(!Icmp.ping(Ip.Ip(192, 168, 0, 255), pingHandler))
					Dbg.wr("another ping in progress.\n");
				break;
			default:
				toggle = 0;
				break;
		}

		for(int i = 0; i < 10; ++i){
			RtThread.sleepMs(500);
			Timer.wd();
		}
	}
}
}
