
package embsys;

/**
* Almost all microcontroller have some kind of timer or counter.
* <code>Clock</code> provides a standard way to query counter values.
* If the device does not support a counter all return values are 0.
* <p>
* This class contains only static methods and is not intended to be
* instanziated.
* 
* 
* 
*/

public class Clock {

	private Clock() {}

	/**
	* Read the internal counter. Tick frequency is device dependent
	* and be queried with {@link #ticksPerSecond()}.
	* @return The current time in clock ticks.
	*/
	public final static int count() {
		return 0;
	}

	/**
	* Querry device dependent tick frequency. Resolution of the counter
	* can be less than an int, see {@link #resolutionInBits()}.
	* @return The tick frequency.
	*/
	public final static int ticksPerSecond() {
		return 0;
	}

	/**
	* Querry device dependent tick count per millisecond. 
	* This method is necessary if the clock frequency is higher than 2.15 GHz :-)
	* @return The tick count per millisecond.
	*/
	public final static int ticksPerMs() {
		return 0;
	}

	/**
	* Querry length of counter register.
	* @return The numeric resolution in bits.
	*/
	public final static int resolutionInBits() {
		return 0;
	}

}
