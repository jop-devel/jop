package csp;

/**
 * A container to hold the information of a CSP packet, separated by fields.
 *
 */
public class Buffer {

	// Packet size
	public int[] size;

	// CRC32 field
	public int[] crc32;

	// Header
	public int[] header;

	// Payload data
	public int[] data;

	// Is the buffer free?
	public boolean free;
	
	// Total length of the buffer including all fields
	public int length;
	
	/**
	 * Constructs a 
	 */
	Buffer() {
		
		this.size = new int[Constants.CSP_PACKET_SIZE];
		this.header = new int[Constants.CSP_HEADER_SIZE];
		this.free = true;
		
		if (Constants.CSP_USE_CRC32) {
			this.crc32 = new int[4];
		}
		
		length = Constants.HEADER_SIZE + Constants.MAX_PAYLOAD_SIZE*4;
		this.data = new int[Constants.MAX_PAYLOAD_SIZE];
	}
	
}
