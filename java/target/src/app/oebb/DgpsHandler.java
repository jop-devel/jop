package oebb;

import ejip.Packet;
import ejip.UdpHandler;

public class DgpsHandler extends UdpHandler {

	final static int PORT = 2006;
	
	public void request(Packet p) {
		Gps.dgps(p);
		p.setStatus(Packet.FREE);
	}

}
