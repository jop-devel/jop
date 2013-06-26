package csp;

/**
 * Defines a CSP node for statistics purposes
 * 
 *
 */
public class Node {
	
	int address;
	boolean isAlive = true;
	int packetsSent, packetsReceived;
	
	Node(int address){
		this.address = address;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public int getAddress() {
		return address;
	}
	
	public void incPacketSent(){
		packetsSent++;
	}
	
	public void incPacketReceived(){
		packetsReceived++;
	}

	public int getPacketsSent() {
		return packetsSent;
	}

	public int getPacketsReceived() {
		return packetsReceived;
	}

}
