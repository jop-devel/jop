package libcsp.csp.core;

import libcsp.csp.CSPManager;
import libcsp.csp.Connection;
import libcsp.csp.ImmortalEntry;
import libcsp.csp.Packet;
import libcsp.csp.handlers.RouteHandler;
import libcsp.csp.util.Const;
import libcsp.csp.util.IDispose;
import libcsp.csp.util.Queue;

/**
 * Specific implementation of a CSP Connection that implements all the logic
 * needed for communication to take place.
 * 
 * A ConnectionCore is uniquely identified with an identifier. The connection
 * identifier format is: S000000000 | SRC:5 | SPORT:6 | DST:5 | DPORT:6 |
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public class ConnectionCore implements IDispose, Connection {

	/* Connection masks */
	public final static int MASK_SRC = 0x003E0000;
	public final static int MASK_SPORT = 0x0001F800;
	public final static int MASK_DST = 0x000007C0;
	public final static int MASK_DPORT = 0x0000003F;

	/*
	 * Connection identifier Format: 
	 * S000000000 | SRC:5 | SPORT:6 | DST:5 | DPORT:6 |
	 */
	public int id;
	/* Packets can only be exchanged when the connection is open */
	public boolean isOpen;
	/* Packets stored by the Connection object */
	public Queue<PacketCore> packets;

	/**
	 * Create a new <code>ConnectionCore</code> object that can hold the
	 * specified number of packets.
	 * 
	 * @param packetsCapacity
	 *            The maximum number of packets that can be stored by the
	 *            ConnectionCore object
	 */
	public ConnectionCore(byte packetsCapacity) {
		this.packets = new Queue<PacketCore>(packetsCapacity);
	}

	/**
	 * Set the identifier for this Connection
	 * 
	 * @param SRC
	 *            Source address
	 * @param SPORT
	 *            Source port
	 * @param DST
	 *            Destination address
	 * @param DPORT
	 *            Destination port
	 */
	public void setId(byte SRC, byte SPORT, byte DST, byte DPORT) {
		setSRC(SRC);
		setSPORT(SPORT);
		setDST(DST);
		setDPORT(DPORT);
	}

	/**
	 * Returns the destination port of this connection
	 * 
	 * @return The destination port of this connection
	 */
	public byte getDPORT() {
		return (byte) (id & MASK_DPORT);
	}

	/**
	 * Returns the destination address of this connection
	 * 
	 * @return The destination address of this connection
	 */
	public byte getDST() {
		return (byte) ((id & MASK_DST) >>> 6);
	}

	/**
	 * Returns the source port of this connection
	 * 
	 * @return The source port of this connection
	 */
	public byte getSPORT() {
		return (byte) ((id & MASK_SPORT) >>> 11);
	}

	/**
	 * Returns the source address of this connection
	 * 
	 * @return The source address of this connection
	 */
	public byte getSRC() {
		return (byte) ((id & MASK_SRC) >>> 17);
	}

	/**
	 * Sets the destination port for this connection
	 * 
	 * @param DPORT
	 *            The destination port
	 */
	public void setDPORT(byte DPORT) {
		id &= ~(MASK_DPORT);
		id |= (int) DPORT;
	}

	/**
	 * Sets the destination address for this connection
	 * 
	 * @param DST
	 *            The destination address
	 */
	public void setDST(byte DST) {
		id &= ~(MASK_DST);
		id |= ((int) DST << 6);
	}

	/**
	 * Sets the source port for this connection
	 * 
	 * @param SPORT
	 *            The source port
	 */
	public void setSPORT(byte SPORT) {
		id &= ~(MASK_SPORT);
		id |= ((int) SPORT << 11);
	}

	/**
	 * Sets the source address for this connection
	 * 
	 * @param SRC
	 *            The source address
	 */
	public void setSRC(byte SRC) {
		id &= ~(MASK_SRC);
		id |= ((int) SRC << 17);
	}

	/**
	 * Attempt to read any packet received in FIFO order. If the packet queue is
	 * not empty, then the packet is removed from the Queue associated with this
	 * ConnectionCore object. A local copy of the packet is created in the scope
	 * of the caller and the dequeued packet is returned to pool of available
	 * Packets.
	 * 
	 * A read operation reads from the local packet queue.
	 * 
	 * @param timeout
	 *            Maximum time in milliseconds to wait for an unused packet from
	 *            the packet pool
	 * @return Next packet received on the connection or null if the queue is
	 *         empty or the timeout expires
	 */
	public PacketCore read(int timeout) {
		PacketCore packet = packets.dequeue(timeout);
		if (packet != null) {
			PacketCore packetCopy = new PacketCore(packet.header, packet.data);
			ImmortalEntry.resourcePool.putPacket(packet);
			return packetCopy;
		}
		return null;
	}

	/**
	 * Attempts to send a packet by inserting it into the router's send queue.
	 * If the router queue has not reached its maximum capacity, then the packet
	 * is enqueued in the router's packet queue. Otherwise the packet is
	 * discarded.
	 * 
	 * The send operation writes to the router packet queue.
	 */
	public void send(Packet packet) {
		PacketCore p = (PacketCore) packet;
		p.setSRC(CSPManager.nodeAddress);
		p.setSPORT(getSPORT());
		p.setDST(getDST());
		p.setDPORT(getDPORT());

		if (ImmortalEntry.packetsToBeProcessed.count < ImmortalEntry.packetsToBeProcessed.capacity) {
			ImmortalEntry.packetsToBeProcessed.enqueue(p);
		} else {
			//TODO: Isn't it the same as this?
			//p.dispose();
			ImmortalEntry.resourcePool.packets.enqueue(p);
		}
	}

	/**
	 * If the connection is in an open state and the packet queue is not full,
	 * this method adds the packet passed as parameter to the tail of the packet
	 * queue. Otherwise the packet is discarded and returned to its resource
	 * pool.
	 * 
	 * The processPacket operation writes to the local packet queue.
	 * 
	 * @param packet
	 */
	public synchronized void processPacket(PacketCore packet) {
		if (isOpen && !packets.isFull()) {
			packets.enqueue(packet);
		} else {
			packet.dispose();
		}
	}

	/**
	 * Closes a connection and returns the Connection object to its resource
	 * pool
	 */
	public synchronized void close() {
		if (isOpen) {
			byte SPORT = getSPORT();
			if (SPORT > Const.MAX_INCOMING_PORTS) {
				SPORT -= (Const.MAX_INCOMING_PORTS + 1);
				ImmortalEntry.outgoingPorts &= ~(1 << SPORT);
			}
			dispose();
		}
	}

	/**
	 * Returns the connection ID from a packet header
	 * 
	 * @param packet
	 *            The packet from where the connection ID will be extracted
	 * @return The connection ID from a packet header
	 * 
	 */
	public static int getConnectionIdFromPacketHeader(PacketCore packet) {
		int connectionId = 0;

		connectionId = (packet.getDST() << 17);
		connectionId |= (packet.getDPORT() << 11);
		connectionId |= (packet.getSRC() << 6);
		connectionId |= packet.getSPORT();

		return connectionId;
	}

	/**
	 * Returns the connection object into its corresponding resource pool. When
	 * this method is called, the connection state is reset and the local packet
	 * queue is flushed of any remaining packet.
	 */
	@Override
	public void dispose() {
		this.isOpen = false;
		this.id = 0;
		this.packets.reset();
		ImmortalEntry.resourcePool.putConnection(this);
	}
}