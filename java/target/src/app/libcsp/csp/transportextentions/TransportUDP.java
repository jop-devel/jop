package libcsp.csp.transportextentions;

import libcsp.csp.core.ConnectionCore;
import libcsp.csp.core.PacketCore;

public class TransportUDP implements ITransportExtension {

	@Override
	public void deliverPacket(ConnectionCore connection, PacketCore packet) {
		connection.processPacket(packet);
	}
}