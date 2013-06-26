package libcsp.csp;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * The socket class is used to define an end-point that can be bound to a
 * specific port that an application should listen on. Furthermore this will
 * provide the access to accept new incoming connections.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public interface Socket {

	/**
	 * Sets the socket in a state where it can receive new connections. When a
	 * new packet arrives for the port on which the socket listens a new
	 * connection is created.
	 * 
	 * @param timeout
	 *            Timeout in milliseconds to wait for new connection
	 * @return A new established connection upon receiving a new packet, or null
	 *         on timeout
	 */
	@SCJAllowed(Level.LEVEL_1)
	public Connection accept(int timeout);

	/**
	 * Closes the socket and unbinds the used port.
	 */
	@SCJAllowed(Level.LEVEL_1)
	public void close();
}
