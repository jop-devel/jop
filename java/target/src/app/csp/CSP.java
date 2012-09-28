package csp;

public class CSP {

	// Constants for the CSP protocol. Not all of them are currently used.

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
	public static final int CSP_ANY = (Conf.CSP_MAX_BIND_PORT + 1);
	public static final int CSP_PROMISC = (Conf.CSP_MAX_BIND_PORT + 2);

	/**
	 * PRIORITIES
	 */
	public static final int CSP_PRIO_CRITICAL = 0;
	public static final int CSP_PRIO_HIGH = 1;
	public static final int CSP_PRIO_NORM = 2;
	public static final int CSP_PRIO_LOW = 3;

	/** CSP Socket options */
	public static final int CSP_SO_NONE 		= 0x00000000; // No socket options
	public static final int CSP_SO_RDPREQ 		= 0x00000001; // Require RDP
	public static final int CSP_SO_RDPPROHIB 	= 0x00000002; // Prohibit RDP
	public static final int CSP_SO_HMACREQ 		= 0x00000004; // Require HMAC
	public static final int CSP_SO_HMACPROHIB 	= 0x00000008; // Prohibit HMAC
	public static final int CSP_SO_XTEAREQ	 	= 0x00000010; // Require XTEA
	public static final int CSP_SO_XTEAPROHIB 	= 0x00000020; // Prohibit HMAC
	public static final int CSP_SO_CRC32REQ 	= 0x00000040; // Require CRC32
	public static final int CSP_SO_CRC32PROHIB 	= 0x00000080; // Prohibit CRC32
	public static final int CSP_SO_CONN_LESS 	= 0x00000100; // Enable connectionless mode

	public static CSPbuffer[] pool;

	public static void initBufferPool() {

		pool = new CSPbuffer[Conf.MAX_BUFFER_COUNT];

		// Initialize buffer pool
		for (int i = 0; i < Conf.MAX_BUFFER_COUNT; i++) {
			pool[i] = new CSPbuffer();
		}
	}

	public static synchronized CSPbuffer getCSPbuffer() {

		// Look sequetially into all the buffers until a free one is found.
		// A more efficient way to search can be implemented in the future...
		for (int i = 0; i < Conf.MAX_BUFFER_COUNT; i++) {
			if (pool[i].free) {
				pool[i].free = false;
				return pool[i];
			}
		}

		System.out.println("No available buffers");
		return null;
	}

	public static synchronized void freeCSPbuffer(CSPbuffer buffer) {

		buffer.free = true;

	}

	public static void printBuffer(CSPbuffer buffer) {

		for (int i = 0; i < buffer.length.length; i++) {
			System.out.println(buffer.length[i]);
		}

		if (Conf.CSP_USE_CRC32) {
			for (int i = 0; i < buffer.crc32.length; i++) {
				System.out.println(buffer.crc32[i]);
			}
		}

		for (int i = 0; i < buffer.header.length; i++) {
			System.out.println(buffer.header[i]);
		}

		for (int i = 0; i < buffer.data.length; i++) {
			System.out.println(buffer.data[i]);
		}

	}

}
