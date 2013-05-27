package libcsp.csp.core;

import libcsp.csp.interfaces.IMACProtocol;

// This seems more like the elements of a route, not of a CSP node.
// Probably a CSP node will have 
public class Node {
	
	/* Physical address of the interface to reach the node */
	public byte nextHopMacAddress;
	
	/* Physical interface to reach the node*/
	public IMACProtocol protocolInterface;
}
