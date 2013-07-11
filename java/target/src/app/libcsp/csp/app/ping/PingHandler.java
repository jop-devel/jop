package libcsp.csp.app.ping;

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
import libcsp.csp.util.Const;

public class PingHandler extends PeriodicEventHandler {

	private CSPManager cspManager;
	private int data;

	public PingHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp,
			long scopeSize, CSPManager manager) {
		super(priority, parameters, scp, scopeSize);

		this.cspManager = manager;
		this.data = 0;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {
		Connection conn = cspManager.createConnection(1, Const.CSP_PING,
				ImmortalEntry.TIMEOUT_NONE, null);

		if (conn != null) {
			Packet p = cspManager.createPacket();
			p.setContent(data);
			data++;

			conn.send(p);

			Packet response = conn.read(400);
			if (response != null)
				System.out.println("Response: " + response.readContent());

			conn.close();
		}

	}

}
