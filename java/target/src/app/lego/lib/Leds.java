package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides access to diagnostic leds (LED0-LED3). 
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Leds
{
	public static final int IO_LEDS = Const.IO_LEGO + 0;	
	protected static int value;
	
	/**
	 * Sets diagnostic leds.
	 * @param state Leds 0-3 state is set by the corresponding bits.
	 * All other bits are ignored.
	 */
	public static final void setLeds(int state)
	{
		value = state;
		Native.wr(state, IO_LEDS);
	}
	
	/**
	 * Returns the state of the diagnostic leds.
	 * @return State of the leds 0-3 in the corresponding bit.
	 * All other bits are set to zero.
	 */
	public static final int getLeds()
	{
		return value;
	}
	
	/**
	 * Returns the state of a diagnostic led.
	 * @param index 0-3.
	 */
	public static final boolean getLed(int index)
	{
		return ((value >> index) & 1) != 0;
	}
	
	/**
	 * Sets the state of a diagnostic led.
	 * @param index 0-3.
	 * @param on
	 */
	public static final void setLed(int index, boolean on)
	{
		setLeds((getLeds() & ~(1<<index)) | ((on?1:0)<<index));
	}
}
