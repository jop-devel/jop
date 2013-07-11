package libcsp.csp;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * The connection class is used to represent incoming and outgoing activity
 * between two end-points. Applications send and read packets between end-points
 * through this entity. A connection is always associated with a socket on the
 * initial receiving side.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public interface Connection {

	/**
	 * Attempt to read any packet received in FIFO order.
	 * 
	 * @param timeout
	 *            Maximum time in milliseconds to wait for an unused packet from
	 *            the packet pool
	 * @return Next packet received on the connection or null if none
	 */
	@SCJAllowed(Level.LEVEL_1)
	public Packet read(int timeout);

	/**
	 * Sends a packet to the destination of the current connection.
	 * 
	 * @param packet
	 *            The packet with specified data to be sent
	 */
	@SCJAllowed(Level.LEVEL_1)
	public void send(Packet packet);

	/**
	 * Closes the connection if open
	 */
	@SCJAllowed(Level.LEVEL_1)
	public void close();
}
