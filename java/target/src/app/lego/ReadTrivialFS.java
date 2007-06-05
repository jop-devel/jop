package lego;

import lego.utils.TrivialFS;

public class ReadTrivialFS
{
	static final int BLOCK_SIZE = 0x10000;
	static final int FLASH_SIZE = BLOCK_SIZE * 8;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int count = TrivialFS.getFileCount();
		System.out.print("File count: ");
		System.out.println(count);
		
		for (int i = 0; i < count; i++)
		{
			System.out.print("File address: ");
			System.out.print(TrivialFS.getFileAddress(i));
			System.out.print(", file size: ");
			System.out.println(TrivialFS.getFileSize(i));
		}
	}
}
