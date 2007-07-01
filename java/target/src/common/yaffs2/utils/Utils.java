package yaffs2.utils;

public class Utils
{
	// performance hogs, only for debugging
	
	public static int __LINE__()
	{			
		if (Globals.debugConfiguration != null)
			return Globals.debugConfiguration.__LINE__();
		else
			return -1;
	}
	
	public static String __FILE__()
	{
		if (Globals.debugConfiguration != null)
			return Globals.debugConfiguration.__FILE__();
		else
			return "<no file information>";
	}
	
	/**
	 * XXX byte order - little endian for now
	 * @param array
	 * @param index
	 * @return
	 */
	public static int getIntFromByteArray(byte[] array, int index)
	{
		return array[index+3] << 24 | (array[index+2] & 0xff) << 16 |
		(array[index+1] & 0xff) << 8 | (array[index] & 0xff); 
	}
	
	/**
	 * XXX byte order - little endian for now
	 * @param array
	 * @param index
	 * @param value
	 */
	public static void writeIntToByteArray(byte[] array, int index, int value)
	{
		array[index+3] = (byte)(value >>> 24);
		array[index+2] = (byte)(value >>> 16);
		array[index+1] = (byte)(value >>> 8);
		array[index+0] = (byte)value;
	}

	/**
	 * XXX byte order - little endian for now
	 * @param array
	 * @param index
	 * @return
	 */
	public static short getShortFromByteArray(byte[] array, int index)
	{
		return (short)(array[index+1] << 8 | (array[index] & 0xff));
	}
	
	/**
	 * XXX byte order - little endian for now
	 * @param array
	 * @param index
	 * @param value
	 */
	public static void writeShortToByteArray(byte[] array, int index, short value)
	{
		array[index+1] = (byte)(value >>> 8);
		array[index] = (byte)(value);
	}
	
	public static long intAsUnsignedInt(int interpretedAsUnsigned)
	{
		return interpretedAsUnsigned & 0xffffffffl;
	}
	
	public static int byteAsUnsignedByte(byte interpretedAsUnsigned)
	{
		return interpretedAsUnsigned & 0xff;
	}
	
	public static void writeBooleanAsIntToByteArray(byte[] array, int index, boolean value)
	{
		writeIntToByteArray(array, index, value ? 1 : 0);
	}

	public static boolean getBooleanAsIntFromByteArray(byte[] array, int index)
	{
		return getIntFromByteArray(array, index) != 0;
	}
	
	/**
	 * 
	 * @param o May be null.
	 * @return Returns 0 when the reference is null.
	 */
	public static int hashCode(Object o)
	{
		return o != null ? o.hashCode() : 0;
	}
	
	/**
	 * XXX Check where it's used.
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
}
