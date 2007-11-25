package yaffs2.utils;

import yaffs2.utils.*;

/**
 * These methods should not be used if you want to avoid garbage collection. 
 *
 */
public abstract class EmulationUtils
{
	/**
	 * 
	 * @param array String, terminated with 0.
	 * @param index
	 * @return
	 */
	public static String byteArrayToString(byte[] array, int index)
	{
		int length = Unix.strlen(array, index);
		StringBuffer result = new StringBuffer(length);
		result.setLength(length);
		
		for (int i = 0; i < length; i++)
			result.setCharAt(i, (char)(array[index+i] & 0xff));
		return result.toString();
	}
	
	/**
	 * XXX No terminating 0 is appended.
	 * @param string
	 * @param array
	 * @param offset
	 * @return 
	 */
	public static int StringToByteArraySafe(String string, byte[] array, int offset)
	{
		for (int i = 0; i < string.length() && offset+i < array.length; i++)
			array[offset+i] = (byte)string.charAt(i);
		
		return Math.min(offset+string.length(), array.length-1);
	}
}
