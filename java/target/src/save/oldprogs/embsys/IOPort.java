
package embsys;

/**
* <code>IOPort</code> allows low-level access to I/O ports.
* <p>
* This class contains only static methods and is not intended to be
* instanziated. Port numbers and meaning of values are device depended.
* 
* 
* 
* 
*/

public class IOPort {

	private IOPort() {}

	/**
	* Read a value from an input port.
	* @param address The address of the input port (device dependend).
	* @return The value of the input port (device dependend).
	*/
	public final static int read(int address) {
		return 0;
	}

	/**
	* Write a value to an output port.
	* @param value The data to be written.
	* @param address The address of the output port (device dependend).
	*/
	public final static void write(int value, int address) {
	}
}
