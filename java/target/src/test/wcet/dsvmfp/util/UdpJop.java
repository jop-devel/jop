package wcet.dsvmfp.util;

import com.jopdesign.sys.Native;

import wcet.dsvmfp.JopClient;
import util.Dbg;
import joprt.RtThread;
import ejip_old.CS8900;
import ejip_old.LinkLayer;
import ejip_old.Packet;
import ejip_old.Net;
import ejip_old.Udp;
import ejip_old.UdpHandler;
/**
 * 
 * @author rup.inf
 * GPL
 */
public class UdpJop {
	static Net net;

	static LinkLayer ipLink;

	static UdpHandler adder;

  static Packet p;

	// Send and receive destPort
  static int destPort;

	// Destination IP
  static int destIp;

	// Receive port
  static int receivePort;

  static int cnt;

  static boolean packetToSend;

	public UdpJop(int destIpprm, int destPortprm, int receivePortprm) {

		destIp = destIpprm;
		destPort = destPortprm;
		receivePort = receivePortprm;
		cnt = 0;

		packetToSend = false;
    
		net = Net.init();
		// TODO: the following code does not compile
//    ipLink = CS8900.init(Net.eth, Net.ip);
    
		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					net.loop(); //If the request is called if something is received
				}
			}
		};

    new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.loop();
				}
			}
		};

		new RtThread(6, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					// RtThread.sleepMs(100);
					if (packetToSend) {
//						System.out.println("send");
						int pwait = 0;
						p = null;
						for (; p == null;) {
//							System.out
//									.println("Loop " + (pwait++) + " p==null");
							p = Packet.getPacket(Packet.FREE, Packet.ALLOC);
							// RtThread.sleepMs(1000);
						}

						// System.out.println("dsvmPacket.getCommand()
						// "+dsvmPacket.getCommand());
						// System.out.println("dsvmPacket.getid()
						// "+dsvmPacket.getId());
						DsvmUtilFP.arrayCopy(DsvmPacket.pLoad, 0, p.buf, Udp.DATA,
                DsvmPacket.length);
						// System.out.println("dsvmPacket.getPayloadInt().length
						// "+dsvmPacket.getPayloadInt().length);
						p.len = (Udp.DATA + DsvmPacket.length) << 2;
						// for(int i=0;i<(Udp.DATA +
						// dsvmPacket.getPayloadInt().length);i++){
						// System.out.println("p.buf["+i+"] "+p.buf[i]);
						// }

						// System.out.println("p.len"+p.len);
						Udp.build(p, destIp, destPort);
						packetToSend = false;
					}
				}
			}
		};

		adder = new UdpHandler() {
			public void request(Packet p) {
//				System.out.println("UdpHandler.request");
//				System.out.println("p.len " + p.len);
				//System.out.print("Udp.DATA[0] ");
        //System.out.println(Udp.DATA[0]);
				DsvmPacket.clear();
        DsvmPacket.setIntPayload(p.buf, p.len / 4-Udp.DATA, Udp.DATA);
//        for(int i=(p.len / 4-Udp.DATA);i<p.len / 4;i++){
//          System.out.print("p[i] ");
//          System.out.println(p.buf[i]);
//        }
				p.setStatus(Packet.FREE);

//				for (int i = 0; i < rcv.length; i++)
//					System.out.println("UdpJop.request, rcv.payLoad[" + i + "] "
//							+ rcv.payLoad[i]);

				JopClient.receive();

			}
		};
		Udp.addHandler(receivePort, adder);

	}

	public void send() {
		packetToSend = true;
	}
}