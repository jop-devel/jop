package csp;


/**
 * A container to hold the information of a CSP packet separated by fields.
 *
 *
 */
public class CSPbuffer {

	// Packet length
	public int[] length;

	// CRC32 field
	public int[] crc32;

	// Header
	public int[] header;

	// Payload data
	public int[] data;

	public boolean free;

	public CSPbuffer() {

		 this.length = new int[2];

		 if (Conf.CSP_USE_CRC32){
			 this.crc32 = new int[4];
		 }

		 this.header = new int[4];

		 this.data = new int[Conf.BUFFER_SIZE];

		 this.free = true;

	}
}
