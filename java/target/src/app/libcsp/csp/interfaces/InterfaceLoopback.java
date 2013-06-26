package libcsp.csp.interfaces;

import libcsp.csp.ImmortalEntry;
import libcsp.csp.core.PacketCore;

public class InterfaceLoopback implements IMACProtocol {

	private static InterfaceLoopback instance;
	
	private InterfaceLoopback() { }
	
	public static InterfaceLoopback getInterface() {
		if(instance == null) {
			instance = new InterfaceLoopback();
		}
		
		return instance;
	}
	
	@Override
	public void initialize(int MACAddress) { }

	@Override
	public void transmitPacket(PacketCore packet) {
		ImmortalEntry.packetsToBeProcessed.enqueue(packet);
	}

	@Override
	public void receiveFrame() { }
}