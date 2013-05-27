package libcsp.csp.core;

import libcsp.csp.util.ConnectionQueue;
import libcsp.csp.util.Queue;

public class ResourcePool {
	
	/* Pools */
	public Queue<SocketCore> sockets;
	public ConnectionQueue connections;
	public Queue<PacketCore> packets;
	
	/* Static connection pool containing all connections */
	public ConnectionCore[] globalConnections;
	
	public ResourcePool(byte socketsCapacity,
			byte connectionsPerSocketCapacity,
			byte connectionsCapacity,
			byte packetsPerConnectionCapacity,
			byte packetsCapacity) {
		
		initializeSocketPool(socketsCapacity, connectionsPerSocketCapacity);
		initializeConnectionPool(connectionsCapacity, packetsCapacity);
		initializePacketPool(packetsCapacity);
	}
	
	private void initializeSocketPool(byte socketsCapacity, byte connectionsPerSocketCapacity) {
		this.sockets = new Queue<SocketCore>(socketsCapacity);
		
		SocketCore socket;
		for(byte i = 0; i < socketsCapacity; i++) {
			socket = new SocketCore(connectionsPerSocketCapacity);
			sockets.enqueue(socket);
		}
	}
	
	private void initializeConnectionPool(byte connectionsCapacity, byte packetsCapacity) {
		this.connections = new ConnectionQueue(connectionsCapacity);
		this.globalConnections = new ConnectionCore[connectionsCapacity];
		
		ConnectionCore connection;
		for(byte i = 0; i < connectionsCapacity; i++) {
			connection = new ConnectionCore(packetsCapacity);
			connections.enqueue(connection);
			globalConnections[i] = connection;
		}
	}
	
	private void initializePacketPool(byte packetsCapacity) {
		this.packets = new Queue<PacketCore>(packetsCapacity);
		
		PacketCore packet;
		for(byte i = 0; i < packetsCapacity; i++) {
			packet = new PacketCore(0, 0);
			packets.enqueue(packet);
		}
	}
	
	public SocketCore getSocket(int timeout) {
		return sockets.dequeue(timeout);
	}
	
	public void putSocket(SocketCore socket) {
		sockets.enqueue(socket);
	}
	
	public ConnectionCore getConnection(int timeout) {
		return connections.dequeue(timeout);
	}
	
	public void putConnection(ConnectionCore connection) {
		connections.enqueue(connection);
	}
	
	public PacketCore getPacket(int timeout) {
		return packets.dequeue(timeout);
	}
	
	public void putPacket(PacketCore packet) {
		packets.enqueue(packet);
	}
	
	public ConnectionCore getGlobalConnection(int id) {
		for(ConnectionCore connection : globalConnections) {
			if(connection.id == id && connection.isOpen) {
				return connection;
			}
		}
		return null;
	}
}
