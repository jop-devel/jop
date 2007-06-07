package yaffs2.assertions;

import yaffs2.utils.*;

public class Assert
{
	public static boolean isUnsignedChar(int x)
	{
		return (x >= 0 && x <= Constants.UNSIGNED_CHAR_MAX); 
	}
	
	public static boolean isUnsignedInt(long x)
	{
		return (x >= 0 && x <= Constants.UNSIGNED_INT_MAX); 
	}
}
