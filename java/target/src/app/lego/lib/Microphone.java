package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides access to microphone ADC input (MIC1).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Microphone
{
	//public static final int IO_MICROPHONE = Const.IO_MICRO;
	public static final int IO_MICROPHONE = Const.IO_LEGO + 1;
	
	/**
	 * Returns the value last read from the microphone.
	 * @return 9 bit ADC value.
	 * XXX Expected value range.
	 */
	public static int readMicrophone()
	{
		return Native.rd(IO_MICROPHONE);
	}
}
