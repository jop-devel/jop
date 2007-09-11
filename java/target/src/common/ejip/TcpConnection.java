package ejip;

/**
 * Represents a single TCP connection.
 * 
 * 
 * 
 * @author Martin
 * 
 */

public class TcpConnection {
	
	public final static int FREE = -1;
	public final static int CLOSED = 0;
	public final static int LISTEN = 1;
	public final static int SYN_RCVD = 2;
	public final static int SYN_SENT = 3;
	public final static int ESTABLISHED = 4;
	public final static int CLOSE_WAIT = 5;
	public final static int LAST_ACK = 6;
	public final static int FIN_WAIT_1 = 7;
	public final static int FIN_WAIT_2 = 8;
	public final static int CLOSING = 9;
	public final static int TIME_WAIT = 10;
	
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
	 * Maximum number of active TCP connections
	 */
	final static int CNT = 10;
	private static TcpConnection[] connections;
	
	private static Object mutex = new Object();
	static {
		connections = new TcpConnection[CNT];
		for (int i=0; i<CNT; ++i) {
			connections[i] = new TcpConnection();
		}
	}
	
	
	private TcpConnection() {
		state = FREE;
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
				if (tc.state!=FREE) {
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
					free.state = CLOSED;
					free.localPort = dstPort;
					free.remotePort = srcPort;
					free.remoteIP = src;
					free.localIP = dest;
				}
			}
		}
		
		return conn;
	}
	
	public static TcpConnection getFreeConnection() {
		
		// local port number is the connection number
		// tc.localPort = 1024+i;
		return null;
	}

	public void setStatus(int s) {
		synchronized (mutex) {
			state = s;
		}
	}
}
