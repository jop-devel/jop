package libcsp.csp.app.clientserver;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import libcsp.csp.ImmortalEntry;
import libcsp.csp.app.CSPGenericMission;
import libcsp.csp.interfaces.InterfaceI2C_A;
import libcsp.csp.interfaces.InterfaceI2C_B;
import libcsp.csp.app.clientserver.ClientHandler;
import libcsp.csp.app.clientserver.ServerHandler;

public class ClientServerMission extends CSPGenericMission implements Safelet<Mission>{

	/* Safelet methods */
	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		ImmortalEntry.setup();		
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public MissionSequencer<Mission> getSequencer() {
		return new LinearMissionSequencer<Mission>(new PriorityParameters(20),
				new StorageParameters(8000, null), false, this);
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public long immortalMemorySize() {
		return 10000;
	}

	/* Mission methods */
	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void initialize() {

		/* Initialize router task, interrupt tasks, and Loopback route */
		init((byte) ImmortalEntry.NODE_ADDRESS, 2, null);
		
		/* Initialize application specific handlers */
		initializeClientHandler(5, 'A');
		initializeClientHandler(6, 'B');
		initializeServerHandler();
	}

	@Override
	@SCJAllowed
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void initializeClientHandler(int priority, char data) {
		
		final int CLIENT_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 1024;
		final int CLIENT_HANDLER_SCOPE_SIZE_IN_BYTES = 800;
		final int CLIENT_HANDLER_RELEASE_PERIOD_IN_MS = 800;
		final int CLIENT_HANDLER_PRIORITY = priority;

		PriorityParameters clientHandlerPriorityParameters = new PriorityParameters(
				CLIENT_HANDLER_PRIORITY);
		PeriodicParameters clientHandlerPeriodicParameters = new PeriodicParameters(
				new RelativeTime(200, 0), new RelativeTime(
						CLIENT_HANDLER_RELEASE_PERIOD_IN_MS, 0));
		StorageParameters clientHandlerStorageParameters = new StorageParameters(
				CLIENT_HANDLER_BACKING_STORE_SIZE_IN_BYTES, null, 0, 0);

		ClientHandler client = new ClientHandler(
				clientHandlerPriorityParameters,
				clientHandlerPeriodicParameters,
				clientHandlerStorageParameters,
				CLIENT_HANDLER_SCOPE_SIZE_IN_BYTES, manager, data);

		client.register();
	}
	
	private void initializeServerHandler() {
		final int SERVER_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 1024;
		final int SERVER_HANDLER_SCOPE_SIZE_IN_BYTES = 800;
		final int SERVER_HANDLER_RELEASE_PERIOD_IN_MS = 400;
		final int SERVER_HANDLER_PRIORITY = 15;

		PriorityParameters serverHandlerPriorityParameters = new PriorityParameters(
				SERVER_HANDLER_PRIORITY);
		PeriodicParameters serverHandlerPeriodicParameters = new PeriodicParameters(
				new RelativeTime(0, 0), new RelativeTime(
						SERVER_HANDLER_RELEASE_PERIOD_IN_MS, 0));
		StorageParameters serverHandlerStorageParameters = new StorageParameters(
				SERVER_HANDLER_BACKING_STORE_SIZE_IN_BYTES, null, 0, 0);

		ServerHandler server = new ServerHandler(
				serverHandlerPriorityParameters,
				serverHandlerPeriodicParameters,
				serverHandlerStorageParameters,
				SERVER_HANDLER_SCOPE_SIZE_IN_BYTES, manager);

		server.register();
	}


}
