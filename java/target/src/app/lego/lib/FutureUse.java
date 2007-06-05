package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides access to as yet unused pins connected to the JOP through
 * the PLD (IN0-IN9). XXX name
 * Depending whether they are configured as input or output in 
 * lego_pld_pack.vhd, they can be read or written.
 * When writing or reading to a pin configured for the opposite use, 
 * nothing will happen.
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class FutureUse
{
	public static final int IO_FUTUREUSE = Const.IO_LEGO + 6;

	/**
	 * Returns the unused pins IN0-IN9 of the PLD in the corresponding bits.
	 * Pins configured as output are read as 0.
	 */
	public static int readPins()
	{
		return Native.rd(IO_FUTUREUSE);
	}
	
	/**
	 * Writes the corresponding bits to the unused pins IN0-IN9 of the PLD.
	 * Pins configured as input are unaffected.
	 */
	public static void writePins(int value)
	{
		Native.wr(value, IO_FUTUREUSE);
	}	
}
