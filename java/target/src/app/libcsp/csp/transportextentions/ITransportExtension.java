package libcsp.csp.transportextentions;

import libcsp.csp.core.ConnectionCore;
import libcsp.csp.core.PacketCore;

public interface ITransportExtension {
	public void deliverPacket(ConnectionCore connection, PacketCore packet);
}
