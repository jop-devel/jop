package libcsp.csp.util;

import com.jopdesign.io.I2Cport;

import csp.Constants;

public class Const {

	/* Limitation of the number of allowed hosts and ports */
	public static final byte MAX_NETWORK_HOSTS = 32;

	/* Incoming port range */
	public static final byte MAX_SERVICE_PORTS = 8;
	public static final byte MAX_BIND_PORTS = 40;
	public static final byte MAX_INCOMING_PORTS = (MAX_SERVICE_PORTS + MAX_BIND_PORTS);
	/* +1 is for PORT_ANY */
	public static final byte MAX_PORTS = MAX_BIND_PORTS + 1;

	/* Outgoing port range */
	public static final byte MAX_OUTGOING_PORTS = 16;

	/* Default Pool capacities that are used if the user does not supply any */
	public static final byte DEFAULT_MAX_CONNECTIONS = 4;
	public static final byte DEFAULT_MAX_CONNECTION_PER_SOCKET = 2;
	public static final byte DEFAULT_PACKET_QUEUE_SIZE_ROUTING = 10;
	public static final byte DEFAULT_PACKET_QUEUE_SIZE_PER_CONNECTION = 4;
	public static final byte DEFAULT_MAX_SOCKETS = 3;
	public static final byte DEFAULT_MAX_PACKETS = 15;
	
	/**
	 * RESERVED PORTS (SERVICES)
	 */
	public static final int CSP_CMP = 0;
	public static final int CSP_PING = 1;
	public static final int CSP_PS = 2;
	public static final int CSP_MEMFREE = 3;
	public static final int CSP_REBOOT = 4;
	public static final int CSP_BUF_FREE = 5;
	public static final int CSP_UPTIME = 6;
	public static final int CSP_ANY = (Constants.CSP_MAX_BIND_PORT + 1);
	public static final int CSP_PROMISC = (Constants.CSP_MAX_BIND_PORT + 2);
	
	public static final int MAX_PAYLOAD_SIZE_IN_BYTES = I2Cport.BUFFER_SIZE - 5;
	
	public static final int MAX_LOG_EVENTS = 1024;
	public static final boolean ENABLE_LOG = true;
	public static final boolean CSP_USE_CRC32 	= false; // Don't use CRC
}
