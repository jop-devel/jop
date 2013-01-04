package csp;

public class ConnectionPool {

	private Connection[] connectionPool;

	public ConnectionPool() {

		connectionPool = new Connection[Constants.CSP_MAX_CONNECTIONS];

		// Initialize connection pool
		for (int i = 0; i < Constants.CSP_MAX_CONNECTIONS; i++) { // @WCA loop = Constants.CSP_MAX_CONNECTIONS
			connectionPool[i] = new Connection();
		}

	}

	/**
	 * Find a free connection from the pool of available connections.
	 * 
	 * @param src
	 *            Address of the source CSP node
	 * @param dest
	 *            Address of the destination CSP node
	 * @param s_port
	 *            CSP port number of the source node
	 * @param d_port
	 *            CSP port number of the destination node
	 * @param prio
	 *            CSP priority of the packets sent through this connection
	 * @param res_flags
	 *            Flags and reserved field
	 * @return Reference to a free connection in the static pool of connection
	 *         objects.
	 */
	public Connection getConnection(int src, int dest, int s_port, int d_port,
			int prio, int res_flags, IOInterface iface, int capacity) {

		// Look into all the connections until a free one is found
		for (int i = 0; i < Constants.CSP_MAX_CONNECTIONS; i++) { // @WCA loop = Constants.CSP_MAX_CONNECTIONS
			if (connectionPool[i].free) {
				connectionPool[i].free = false;
				connectionPool[i].source = src;
				connectionPool[i].destination = dest;
				connectionPool[i].source_port = s_port;
				connectionPool[i].dest_port = d_port;
				connectionPool[i].prio = prio;
				connectionPool[i].res_flags = res_flags;
				connectionPool[i].iface = iface;
				connectionPool[i].queue = new PacketQueue(capacity);
				return connectionPool[i];
			}
		}

		System.out.println("No available connections");
		return null;
	}

	public Connection findConnection(int src, int dest, int s_port, int d_port) {

		Connection conn = null;
		for (int i = 0; i < Constants.CSP_MAX_CONNECTIONS; i++) { // @WCA loop = Constants.CSP_MAX_CONNECTIONS
			if ((!connectionPool[i].free) && (connectionPool[i].source == dest)
					&& (connectionPool[i].destination == src)
					&& (connectionPool[i].source_port == d_port)
					&& (connectionPool[i].dest_port == s_port)) {
				conn = connectionPool[i];
				break;
			}
		}
		return conn;
	}

	public void releaseConnection(Connection c) {
		c.free = false;
		c.source = 0;
		c.destination = 0;
		c.source_port = 0;
		c.dest_port = 0;
		c.prio = 0;
		c.res_flags = 0;
		c.iface = null;
		c.queue = null;
	}
}
