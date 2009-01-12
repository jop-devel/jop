package ejip_old;

/**
 * Represents a single TCP connection.
 * 
 * 
 * 
 * @author Martin
 * 
 */

public class TcpConnection {
	
	/**
	 * State of the TCP connection.
	 */
	int state;
	int localIP; // do we need it?
	int remoteIP;
	int localPort;
	int remotePort;
	
	/**
	 * The next expected receive sequence number.
	 * Without the length of the incomming package.
	 */
	int rcvNxt;
	/**
	 * The last sent sequence number.
	 */
	int sndNxt;
	/**
	 * The outstandig packet. We only allow one packet on the fly.
	 */
	Packet outStanding;
	/**
	 * Timeout for retransmit of the outstanding packet. Will be
	 * decremented and retransmit on 0
	 */
	int timeout;
	
	/**
	 * Maximum number of active TCP connections
	 */
	final static int CNT = 10;
	static TcpConnection[] connections;
	
	private static Object mutex = new Object();
	static {
		connections = new TcpConnection[CNT];
		for (int i=0; i<CNT; ++i) {
			connections[i] = new TcpConnection();
		}
	}
	
	
	private TcpConnection() {
		state = Tcp.FREE;
		Packet os = outStanding;
		outStanding = null;
		if (os!=null) {
			os.setStatus(Packet.FREE);			
		}
	}
	
	public static TcpConnection findConnection(Packet p) {
		
		int[] buf = p.buf;

		int dstPort = buf[Tcp.HEAD];
		int srcPort = dstPort >>> 16;
		dstPort &= 0xffff;
		int src = buf[Ip.SOURCE];
		int dest = buf[Ip.DESTINATION];
		
		TcpConnection free = null;
		TcpConnection conn = null;
				
		synchronized (mutex) {
			for (int i=0; i<CNT; ++i) {
				TcpConnection tc = connections[i];
				if (tc.state!=Tcp.FREE) {
					if (dstPort==tc.localPort &&
						srcPort==tc.remotePort &&
						src==tc.remoteIP &&
						dest==tc.localIP) {
						
						conn = tc;
						break;
					}
				} else {
					if (free==null) {
						free = tc;
					}
				}
			}
			// if not found get a new one when possible
			if (conn==null) {
				conn = free;
				if (free!=null) {
					free.state = Tcp.CLOSED;
					free.localPort = dstPort;
					free.remotePort = srcPort;
					free.remoteIP = src;
					free.localIP = dest;
				}
			}
		}
		
		int cnt=0;
		for (int i=0; i<CNT; ++i ) {
			if (connections[i].state!=Tcp.FREE) {
				++cnt;
			}
		}
		System.out.print("getCon: con in use:");
		System.out.println(cnt);
		
		return conn;
	}
	
	public static TcpConnection getFreeConnection() {
		
		// local port number is the connection number
		// tc.localPort = 1024+i;
		return null;
	}

	/**
	 * Close the connection and return it to the pool.
	 *
	 */
	public void close() {
		synchronized (mutex) {
			state = Tcp.FREE;
			outStanding = null;
		}
	}
}
