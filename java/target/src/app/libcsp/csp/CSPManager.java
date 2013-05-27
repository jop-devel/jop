package libcsp.csp;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import libcsp.csp.core.ConnectionCore;
import libcsp.csp.core.Port;
import libcsp.csp.core.SocketCore;
import libcsp.csp.interfaces.IMACProtocol;
import libcsp.csp.interfaces.InterfaceI2C_A;
import libcsp.csp.interfaces.InterfaceI2C_B;
import libcsp.csp.interfaces.InterfaceLoopback;
import libcsp.csp.util.Const;

/**
 * This class creates the shared data structures and the router handler. It also
 * provides service functions.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public class CSPManager {

	/* MAC-layer Protocols */
	public static final byte INTERFACE_I2C_A = 1;
	public static final byte INTERFACE_I2C_B = 2;
	public static final byte INTERFACE_LOOPBACK = 3;

	/* Local node address */
	public static byte nodeAddress;

	/**
	 * Specifies a route in the routing table for outgoing packets. init() must
	 * be invoked before setting routes.
	 * 
	 * @param nodeAddress
	 *            Destination address (must be in the range 0-30)
	 * @param protocol
	 *            Outgoing interface protocol used for the next hop
	 * @param nextHopMacAddress
	 *            Next hop mac address for the interface protocol
	 */
	@SCJAllowed(Level.LEVEL_1)
	public void routeSet(int nodeAddress, IMACProtocol protocol,
			int nextHopMacAddress) {
		
		ImmortalEntry.routeTable[nodeAddress].nextHopMacAddress = (byte) nextHopMacAddress;
		ImmortalEntry.routeTable[nodeAddress].protocolInterface = protocol;
	}

	/**
	 * Retrieves the object implementing a specific MAC-layer protocol.
	 * 
	 * @param Identifier
	 *            Protocol identifier
	 * @return A singleton object implementing the protocol
	 */
	@SCJAllowed(Level.LEVEL_1)
	public IMACProtocol getIMACProtocol(int type) {
		switch (type) {
		
		case INTERFACE_I2C_A:
			return InterfaceI2C_A.getInterface();
		case INTERFACE_I2C_B:
			return InterfaceI2C_B.getInterface();
		case INTERFACE_LOOPBACK:
			return InterfaceLoopback.getInterface();
		}

		return null;
	}

	/**
	 * Creates and binds a socket to a specific port.
	 * 
	 * @param port
	 *            Port number to use
	 * @param options
	 *            Socket options
	 * @return Socket object bound to the port or null if none available
	 */
	@SCJAllowed(Level.LEVEL_1)
	public synchronized Socket createSocket(int port, Object options) {
		Port p = ImmortalEntry.portTable[port];
		if (!p.isOpen) {
			Socket socket = ImmortalEntry.resourcePool
					.getSocket(ImmortalEntry.TIMEOUT_SINGLE_ATTEMPT);
			if (socket != null) {
				p.isOpen = true;
				p.socket = (SocketCore) socket;
				p.socket.port = (byte) port;
				return socket;
			}
		}
		return null;
	}

	/**
	 * Creates a connection to another network node. This only prepares the
	 * connection - i.e no data is actually sent to the node. A successive call
	 * to this method e.g. from the next release in a periodic handler may not
	 * get the same <code>Connection</code> object from the pool as in the
	 * previous release.
	 * 
	 * This method also iterates over the currently free ports in order to set
	 * the source port for the future communication.
	 * 
	 * When a <code>Connection</code> object is successfully returned, its
	 * identifier is set according to the passed parameters and on the free port
	 * that was previously found by iterating over the currently free ports.
	 * 
	 * @param address
	 *            Destination address (must in range 0-32)
	 * @param port
	 *            Destination port (must be in range 0-47)
	 * @param timeout
	 *            Maximum time in milliseconds to wait for an unused connection
	 *            from the connection pool
	 * @param options
	 *            Connection options
	 * @return Newly created connection
	 */
	@SCJAllowed(Level.LEVEL_1)
	public Connection createConnection(int address, int port, int timeout,
			Object options) {
		ConnectionCore connection = ImmortalEntry.resourcePool.getConnection(timeout);

		if (connection != null) {
			byte nodePort = findUnusedOutgoingPort();
			if (nodePort != -1) {
				nodePort += (Const.MAX_INCOMING_PORTS + 1);
				connection.setId(nodeAddress, nodePort, (byte) address,
						(byte) port);
				connection.isOpen = true;
				return connection;
			}
		}
		return null;
	}

	private synchronized byte findUnusedOutgoingPort() {
		short mask;
		for (short index = 0; index < 15; index++) {
			mask = (short) (1 << index);
			if ((ImmortalEntry.outgoingPorts & mask) == 0) {
				ImmortalEntry.outgoingPorts |= mask;
				return (byte) index;
			}
		}
		return -1;
	}

	/**
	 * Provides a new empty packet that can be transmitted over an open
	 * connection.
	 * 
	 * @return Empty packet
	 */
	@SCJAllowed(Level.LEVEL_1)
	public Packet createPacket() {
		return ImmortalEntry.resourcePool
				.getPacket(ImmortalEntry.TIMEOUT_SINGLE_ATTEMPT);
	}
}
