package libcsp.csp.app.clientserver;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import libcsp.csp.CSPManager;
import libcsp.csp.Connection;
import libcsp.csp.ImmortalEntry;
import libcsp.csp.Packet;

public class ClientHandler extends PeriodicEventHandler {

	private CSPManager cspManager;
	private char data;
	
	public ClientHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize,
			 CSPManager manager, char data) {
		super(priority, parameters, scp, scopeSize);
		
		this.cspManager = manager;
		this.data = data;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {
		
		Connection conn = cspManager.createConnection(ImmortalEntry.NODE_ADDRESS, 12, ImmortalEntry.TIMEOUT_NONE, null);

		if (conn != null) {	
			Packet p = cspManager.createPacket();
			p.setContent((int) data);
			
			conn.send(p);
			
			Packet response = conn.read(ImmortalEntry.TIMEOUT_NONE);
			System.out.println("Response: " + (char) response.readContent());
			
			conn.close();
		}
		
	}
	


}
