package csp;

/**
 * For now, a connection is nothing more than an object with some of the CSP
 * packet header parameters. It can have two variants:
 * 
 * 1. For a client, it is a point to point connection so it needs both end
 * points to be specified.
 * 
 * 2. For a server, it can have only the local end point and obtain the remote
 * end point from a received packet (passive mode) or it can specify it
 * explicitly (active mode).
 */
public class Connection {

	public int source;
	public int destination;

	public int source_port;
	public int dest_port;

	public int prio;
	public int res_flags;

	public PacketQueue queue;
	public IOInterface iface;

	boolean free = true;

}