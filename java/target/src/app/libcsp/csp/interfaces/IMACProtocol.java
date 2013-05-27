package libcsp.csp.interfaces;

import libcsp.csp.core.PacketCore;

public interface IMACProtocol {
	
	public void initialize(int MACAddress);
	
	public void transmitPacket(PacketCore packet);
	
	public void receiveFrame();
}
