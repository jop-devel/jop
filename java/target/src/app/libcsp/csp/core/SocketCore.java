package libcsp.csp.core;

import libcsp.csp.CSPManager;
import libcsp.csp.ImmortalEntry;
import libcsp.csp.handlers.RouteHandler;
import libcsp.csp.util.ConnectionQueue;
import libcsp.csp.util.IDispose;

public class SocketCore implements IDispose, libcsp.csp.Socket {
	
	public byte port;
	public ConnectionQueue connections;
	
	public SocketCore(byte connectionsCapacity) {
		this.connections = new ConnectionQueue(connectionsCapacity);
	}

	public ConnectionCore accept(int timeout) {
		return connections.dequeue(timeout);
	}
	
	public synchronized void processConnection(ConnectionCore connection) {
		if(port != -1) {
			connections.enqueue(connection);
		}
	}
	
	public synchronized void close() {
		if(ImmortalEntry.portTable[port].isOpen) {
			ImmortalEntry.portTable[port].isOpen = false;
			ImmortalEntry.portTable[port].socket = null;
			dispose();
		}	
	}
	
	@Override
	public void dispose() {
		this.port = -1;
		this.connections.reset();
		ImmortalEntry.resourcePool.putSocket(this);
	}
}