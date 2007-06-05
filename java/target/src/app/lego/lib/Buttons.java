package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides button states (BTN0-BTN3).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Buttons
{
	public static final int IO_BUTTONS = Const.IO_LEGO + 4;

	/**
	 * Reads whether button is depressed.
	 * @param index Valid indices are 0, 1, 2, 3.
	 */	
	public static boolean getButton(int index)
	{
		//if (index<0 || index>3)
		//	throw new RuntimeException("Invalid button index specified!");
		return ((Native.rd(IO_BUTTONS) >> index) & 1) != 0;
	}
	
	/**
	 * Reads all button states into the respective bits. 
	 * @return The buttons are numbered from 0 to 3.
	 * All other bits are set to zero.
	 */
	public static int getButtons()
	{
		return Native.rd(IO_BUTTONS);
	}
}
