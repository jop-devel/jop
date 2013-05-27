package libcsp.csp;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * The packet class represents the CSP entity packet that is sent through
 * connections. This contains the header and payload.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public interface Packet {

	/**
	 * Sets the payload of the packet.
	 * 
	 * @param data
	 *            Payload data
	 */
	@SCJAllowed(Level.LEVEL_1)
	public void setContent(int data);

	/**
	 * Gets the payload of the packet.
	 * 
	 * @return Payload data
	 */
	@SCJAllowed(Level.LEVEL_1)
	public int readContent();
}
