package oebb;

import ejip.Ejip;
import ejip.Packet;
import ejip.UdpHandler;

public class DgpsHandler implements UdpHandler {

	final static int PORT = 2006;
	private static Ejip ejip;

	public DgpsHandler(Ejip ejipRef) {
		ejip = ejipRef;
	}

	public void request(Packet p) {
		Gps.dgps(p);
		ejip.returnPacket(p);	// mark packet free
	}

	public void loop() {
		// do nothing
	}

}
