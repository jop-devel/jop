package lego.utils;

import util.Amd;

public class TrivialFS
{
	public static int getFileCount()
	{
		int index = -4;
		int length = 0;
		int count = 0;
		
		while (true)
		{
			index += 4 + length;
			
			length = Amd.read(index+0) << 24;
			length |= Amd.read(index+1) << 16;
			length |= Amd.read(index+2) << 8;
			length |= Amd.read(index+3);
			
			if (length != 0)
				count++;
			else
				break;
		}
		
		return count;		
	}
	
	public static int getFileAddress(int fileNumber)
	{
		int index = -4;
		int length = 0;
		
		for (int i = 0; i <= fileNumber; i++)
		{
			index += 4 + length;
			
			length = Amd.read(index+0) << 24;
			length |= Amd.read(index+1) << 16;
			length |= Amd.read(index+2) << 8;
			length |= Amd.read(index+3);
		}
		
		return index + 4;
	}
	
	public static int getFileSize(int fileNumber)
	{
		int index = -4;
		int length = 0;
		
		for (int i = 0; i <= fileNumber; i++)
		{
			index += 4 + length;
			
			length = Amd.read(index+0) << 24;
			length |= Amd.read(index+1) << 16;
			length |= Amd.read(index+2) << 8;
			length |= Amd.read(index+3);
		}
		
		return length;		
	}
}
