package yaffs2.utils;

import java.io.InputStream;

public abstract class DebugUtils
{
	public static byte[] intToByteArray(int value)
	{
		byte[] intBuf = new byte[4];
		Utils.writeIntToByteArray(intBuf, 0, value);
		return intBuf;
	}
	
	public static int readIntFromInputStream(InputStream in)
	{
		byte[] intBuf = new byte[4];

		try
		{
			in.read(intBuf);
		}
		catch (Exception e)
		{
			throw new UnexpectedException();
		}
		
		return Utils.getIntFromByteArray(intBuf, 0);
	}
}
