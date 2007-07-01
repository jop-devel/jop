package yaffs2.utils.emulation;

import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public abstract class CheckSum {
	public static byte checksumOfBytesUnused(byte[] array, int index)
	{
		final byte divisor = 0x15;
		byte sum = 0, temp;
		
		temp = (byte)(array[index] << 4);
		sum = (byte)(array[index]>>3 ^ divisor);
		
		for(int i = 0;i < 7; i++)
		{
			sum = (byte)((sum << 1) | (temp >> (6-i) & 1));			
			sum = (byte)((sum & 0x1f)^divisor);			
		}
		
		return sum = (byte)(sum & 0x0f);
	}
	
	public static void checksumOfBytes(byte[] array, int index, int n)
	{
		int temp = 0;
		byte out = 0;
		for(int i = 0; i < n; i++)
		{
			temp = 0;
			for(int j = 0; j < 4; j++)
			{
				temp += array[index+(4*i)+j];
			}
			out = (byte)((~temp)+1);
			
			Unix.xprintfArgs[0] = PrimitiveWrapperFactory.get(yaffs2.utils.Utils.byteAsUnsignedByte(out)); 
			Unix.printf("%02x");
			if ((i>0)&&(i%32) == 0)
				Unix.printf("\n");
				
		}	
		Unix.printf("\n");
	}
}
