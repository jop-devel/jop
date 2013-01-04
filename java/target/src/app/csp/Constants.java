package csp;

import csp.scj.watchdog.I2CBusController;

public class Constants {

	// Constants for the CSP protocol. Not all of them are currently used.

	/**
	 * RESERVED PORTS (SERVICES)
	 */
	public static final int CSP_CMP 		= 0;
	public static final int CSP_PING 		= 1;
	public static final int CSP_PS 		= 2;
	public static final int CSP_MEMFREE 	= 3;
	public static final int CSP_REBOOT 	= 4;
	public static final int CSP_BUF_FREE = 5;
	public static final int CSP_UPTIME 	= 6;
	public static final int CSP_ANY 		= (Constants.CSP_MAX_BIND_PORT + 1);
	public static final int CSP_PROMISC 	= (Constants.CSP_MAX_BIND_PORT + 2);

	/**
	 * PRIORITIES
	 */
	public static final int CSP_PRIO_CRITICAL		= 0;
	public static final int CSP_PRIO_HIGH 		= 1;
	public static final int CSP_PRIO_NORM 		= 2;
	public static final int CSP_PRIO_LOW 			= 3;

	/** CSP Socket options */
	public static final int CSP_SO_NONE 			= 0x00000000; // No socket options
	public static final int CSP_SO_RDPREQ 		= 0x00000001; // Require RDP
	public static final int CSP_SO_RDPPROHIB 		= 0x00000002; // Prohibit RDP
	public static final int CSP_SO_HMACREQ 		= 0x00000004; // Require HMAC
	public static final int CSP_SO_HMACPROHIB 	= 0x00000008; // Prohibit HMAC
	public static final int CSP_SO_XTEAREQ 		= 0x00000010; // Require XTEA
	public static final int CSP_SO_XTEAPROHIB 	= 0x00000020; // Prohibit HMAC
	public static final int CSP_SO_CRC32REQ 		= 0x00000040; // Require CRC32
	public static final int CSP_SO_CRC32PROHIB 	= 0x00000080; // Prohibit CRC32
	public static final int CSP_SO_CONN_LESS 		= 0x00000100; // Enable
															// connectionless
															// mode

	public static final boolean CSP_USE_QOS = false;

	/** CSP Socket options */
	public static final boolean CSP_USE_RDP 		= false; // Require RDP
	public static final boolean CSP_USE_XTEA 	= false; // Prohibit RDP
	public static final boolean CSP_USE_HMAC 	= false; // Require HMAC
	public static final boolean CSP_USE_CRC32 	= false; // Don't use CRC

	/**
	 * Implementation specific constants
	 */
	public static final int CSP_MAX_CONNECTIONS 	= 31;
	public static final int CSP_MAX_BIND_PORT 	= 31;

	public static final int MAX_BUFFER_COUNT 		= 5;
	public static final int CSP_HEADER_SIZE 		= 4;
	public static final int CSP_PACKET_SIZE 		= 2;
	public static int HEADER_SIZE;

	static {
		if (CSP_USE_CRC32) {
			HEADER_SIZE = CSP_HEADER_SIZE + CSP_PACKET_SIZE + 4;
		} else {
			HEADER_SIZE = CSP_HEADER_SIZE + CSP_PACKET_SIZE;
		}
	}

	public static final int MAX_PAYLOAD_SIZE = Math
			.floor(((I2CBusController.BUFFER_SIZE - 1) - Constants.HEADER_SIZE) >> 2);

	public static final int MAX_LOG_EVENTS = 1024;
	public static final boolean ENABLE_LOG = true;

	/**
	 * I2C port A address
	 */
	public static final int DEVICE_A_ADDR = 15;

	/**
	 * I2C port B address
	 */
	public static final int DEVICE_B_ADDR = 3;

	/**
	 * Number of salves nodes that the watchdog task will check
	 */
	public static final int NUM_SLAVES = 10;

	/**
	 * Watchdog ping timeout in milliseconds
	 */
	public static final int WD_TIMEOUT = 6;

	/**
	 * If true, then checking for received packets will be done polled mode, if
	 * false, it is done in an interrupt based fashion
	 */
	public static final boolean POLLED_MODE = false;
}