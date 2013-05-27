package libcsp.csp.app;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import libcsp.csp.CSPManager;
import libcsp.csp.ImmortalEntry;
import libcsp.csp.handlers.ISRHandler;
import libcsp.csp.handlers.RouteHandler;
import libcsp.csp.interfaces.IMACProtocol;
import libcsp.csp.interfaces.InterfaceLoopback;

public abstract class CSPGenericMission extends Mission{
	
	protected CSPManager manager;
	
	/* A reference to the router handler */
	protected RouteHandler routeHandler;
	
	/* Reference to packet interrupt handlers */
	protected ISRHandler isrHandlers;
	
	/* Expected memory consumption for router handler */
	protected StorageParameters routeHandlerStorageParameters;
	
	protected PriorityParameters routingPriorityParameters;
	
	protected PeriodicParameters routingPeriodicParameters;
	
	/**
	 * This must be the first invoked method in the mission initialization
	 * phase. It initializes the router handler with default memory parameters
	 * and creates an entry for this node in the route table through the
	 * LoopBack interface.
	 * 
	 * @param nodeAddress
	 *            The specified address of the host (must be in the range 0-30)
	 */
	@SCJAllowed(Level.LEVEL_1)
	public void init(int nodeAddress, int intNumber,
			IMACProtocol interruptProtocolInterface) {
		
		manager = new CSPManager();

		CSPManager.nodeAddress = (byte) nodeAddress;
		ImmortalEntry.outgoingPorts = 0;

		/* Initialize router handler with default memory parameters */
		initializeDefaultRouteHandler();
		
		initilaizeInterruptHandler(intNumber, interruptProtocolInterface);

		manager.routeSet(nodeAddress, InterfaceLoopback.getInterface(), 0x0);
	}

	private void initializeDefaultRouteHandler() {

		final int ROUTING_HANDLER_RELEASE_PERIOD_IN_MS = 20;
		final int ROUTING_HANDLER_PRIORITY = 20;
		final int ROUTE_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 2048;
		final int ROUTE_HANDLER_SCOPE_SIZE_IN_BYTES = 1024;

		routingPriorityParameters = new PriorityParameters(
				ROUTING_HANDLER_PRIORITY);

		routingPeriodicParameters = new PeriodicParameters(new RelativeTime(0,
				0), new RelativeTime(ROUTING_HANDLER_RELEASE_PERIOD_IN_MS, 0));

		routeHandlerStorageParameters = new StorageParameters(
				ROUTE_HANDLER_BACKING_STORE_SIZE_IN_BYTES, null, 0, 0);

		routeHandler = new RouteHandler(routingPriorityParameters,
				routingPeriodicParameters, routeHandlerStorageParameters,
				ROUTE_HANDLER_SCOPE_SIZE_IN_BYTES);

		routeHandler.register();
	}
	
	/**
	 * 
	 * @param intNumber
	 *            Total number of interrupt
	 */
	private void initilaizeInterruptHandler(int intNumber, IMACProtocol interruptProtocolInterface){
		
		ISRHandler isr = new ISRHandler(new StorageParameters(512, null), 256, interruptProtocolInterface);
		isr.register(intNumber);
		
	}
}
