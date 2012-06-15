package csp;

import com.jopdesign.io.I2Cport;

public class CSPconnection {

	// For now, a connection is nothing more than an object with
	// some of the CSP packet header parameters. It can have two
	// variants:
	// 		1. For a client, it is a point to point connection so it
	//		needs both end points to be specified.
	//
	//		2. For a server, it can have only the local end point
	// 		and obtain the remote end point from a received packet
	//		(passive mode) or it can specify it explicitly (active
	//		mode)

	public int source;
	public int destination;

	public int source_port;
	public int dest_port;

	public int prio;
	public int res_flags;

	public I2Cport tx_port;
	public I2Cport rx_port;

	// Passive server connection
	public CSPconnection(int src, int s_port, int prio, int res_flags, I2Cport port) {

		this(src, 0, s_port, 0, prio, res_flags, port, port);

	}


	/**
	 *
	 * @param src: Source address (CSP node)
	 * @param dest: Destination address (CSP node)
	 * @param s_port: Source port
	 * @param d_port: Destination port
	 * @param prio: Priority
	 * @param res_flags: Flags and reserved field
	 * @param port: Tx/Rx I2C  port
	 */
	public CSPconnection(int src, int dest, int s_port, int d_port, int prio, int res_flags, I2Cport port) {

		// Tx and Rx port are the same
		this(src, dest, s_port, d_port, prio, res_flags, port, port);

	}

	/**
	 *
	 * @param src: Source address (CSP node)
	 * @param dest: Destination address (CSP node)
	 * @param s_port: Source port
	 * @param d_port: Destination port
	 * @param prio: Priority
	 * @param res_flags: Flags and reserved field
	 * @param tx_port: I2C transmission port
	 * @param rx_port: I2C receiver port
	 */
	public CSPconnection(int src, int dest, int s_port, int d_port, int prio, int res_flags, I2Cport tx_port, I2Cport rx_port) {

		this.source = src;
		this.destination = dest;
		this.source_port = s_port;
		this.dest_port = d_port;
		this.prio = prio;
		this.res_flags = res_flags;

		this.tx_port = tx_port;
		this.rx_port = rx_port;

	}


}
