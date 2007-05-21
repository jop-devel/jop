package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides access to the speaker output. 
 * @author Alexander Dejaco (alexander.dejaco@student.tuwien.ac.at)
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Speaker
{
	public static final int IO_SPEAKER= Const.IO_LEGO + 7;	
	
	public static final int MAX_VALUE = 0xff;
	
	protected static int value;
	
	/**
	 * Sets speaker output.
	 */
	public static final void write(boolean value)
	{
		Native.wr(value ? MAX_VALUE : 0, IO_SPEAKER);
	}
	
	/**
	 * Sets speaker output.
	 * @param value Valid input range is 0..0xff. 
	 */
	public static final void write(int value)
	{
		Native.wr(value, IO_SPEAKER);
	}
}
