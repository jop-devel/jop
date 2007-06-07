package yaffs2.utils.emulation;

import static yaffs2.utils.Unix.*;

/**
 * These methods should not be used if you want to avoid garbage collection. 
 *
 */
public abstract class Utils
{
	public static String byteArrayToString(byte[] array, int index)
	{
		int length = strlen(array, index);
		StringBuffer result = new StringBuffer(length);
		result.setLength(length);
		
		for (int i = 0; i < length; i++)
			result.setCharAt(i, (char)(array[index+i] & 0xff));
		return result.toString();
	}
	
	/**
	 * XXX A terminating 0 is appended. 
	 * @param string
	 * @return
	 */
	public static byte[] StringToByteArray(String string)
	{
		byte[] result = new byte[string.length()+1];
		for (int i = 0; i < string.length(); i++)
			result[i] = (byte)string.charAt(i);
		result[result.length-1] = 0;
		
		return result;
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
