package lego;

import java.io.IOException;

import lego.lib.Leds;
import util.Amd;

/**
 * Reads in a TrivialFS.
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class FlashProgrammer
{
	static final int BLOCK_SIZE = 0x10000;
	static final int FLASH_SIZE = BLOCK_SIZE * 8;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		Leds.setLeds(0x3);
		
		{
			for (int i = 0; i < FLASH_SIZE; i += BLOCK_SIZE)
				Amd.erase(i);
		}
		
		System.out.println("Done erasing, ready for input.");
		
		Leds.setLeds(0x3 << 2);

		int index = 0;
		int fileIndex = 0;
		int length = 0;

		while (true)
		{
			if (fileIndex == 4 + length)
				fileIndex = 0;				
		
			int ch = System.in.read();
		
			if (fileIndex == 0)				
				length = ch << 24;
			if (fileIndex == 1)
				length |= ch << 16;
			if (fileIndex == 2)
				length |= ch << 8;
			if (fileIndex == 3)
			{
				length |= ch;				
			}			
			
			Amd.program(index, ch);
			//System.out.print((char)0x55);
			
//			System.out.print("Programming ");
//			System.out.print(index);
//			System.out.print(": ");
//			System.out.print(bin);
//			System.out.print(" ");
//			System.out.println(Amd.read(index) == bin ? "succeeded" : "failed");

			fileIndex++;
			index++;
			
			if (fileIndex == 4)
			{
//				System.out.print("File length: ");
//				System.out.println(length);	
			
				if (length == 0)
					break;
			}			
		}
		
		Leds.setLeds(~0);

		System.out.print("Written: ");
		System.out.println(index);		
		
		ReadTrivialFS.main(args);
	}
}
