package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides access to general purpose digital inputs (I0-I2).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class DigitalInputs
{
	public static final int IO_DIGITALINPUTS = Const.IO_LEGO + 5;

	/**
	 * Read digital input.
	 * @param index Valid indices are 0, 1, 2.
	 */	
	public static boolean getDigitalInput(int index)
	{
		//if (index<0 || index>3)
		//	throw new RuntimeException("Invalid button index specified!");
		return ((Native.rd(IO_DIGITALINPUTS) >> index) & 1) != 0;
	}
	
	/**
	 * Reads all digital inputs into the respective bits.
	 * @return The digital inputs are numbered from 0 to 2.
	 * All other bits are set to zero. 
	 */
	public static int getDigitalInputs()
	{
		return Native.rd(IO_DIGITALINPUTS);
	}
}
