package libcsp.csp.core;

/**
 * The port class is used to associate a port with a socket. The routing logic
 * will be able to use objects of the port class to determine if a given port is
 * open upon receiving new packets and retrieve the associated socket.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public class Port {
	public SocketCore socket;
	public boolean isOpen;
}
