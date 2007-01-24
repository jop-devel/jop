/* 
 * Copyright  (c) 2006-2007 Graz University of Technology. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The names "Graz University of Technology" and "IAIK of Graz University of
 *    Technology" must not be used to endorse or promote products derived from
 *    this software without prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY  OF SUCH DAMAGE.
 */

package ejip.jtcpip.util;


/**
 * Class to handle Bitmaps
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 939 $ $Date: 2007/01/24 19:36:48 $
 */
public class Bitmap
{
	/** Size of the bitmap in bits */
	int size;

	/** Bit pool */
	byte[] bitmap;

	/**
	 * Initalizes the bitmap.
	 * 
	 * @param size
	 *            Desired number of bits in the bitmap
	 */
	public Bitmap(int size)
	{
		this.size = size;
		bitmap = new byte[NumFunctions.divRoundUp(size, 8)];
		// if the size doesn't fall onto a byte boundary add another byte
	}

	/**
	 * Clears the complete bitmap.
	 */
	public void clearBitmap()
	{
		for (int i = 0; i < bitmap.length; i++)
			bitmap[i] = 0;
	}

	/**
	 * Sets a single bit in the bitmap.
	 * 
	 * @param pos
	 *            Position of the bit to set
	 */
	public void setBit(int pos)
	{
		if (pos < size)
			bitmap[pos / 8] |= (byte) (1 << (pos % 8));
	}

	/**
	 * Clears a single bit in the bitmap
	 * 
	 * @param pos
	 */
	public void clearBit(int pos)
	{
		if (pos < size)
			bitmap[pos / 8] &= ~(byte) (1 << (pos % 8));
	}

	/**
	 * Sets multiple bits. Set <code>count</code> bits, starting at
	 * <code>pos</code>.
	 * 
	 * @param pos
	 *            Position of the first bit to set
	 * @param count
	 *            Number of bits to set
	 */
	public void setBits(int pos, int count)
	{
		if (pos >= size)
			return;

		if ((pos + count) >= size)
			count = (size - pos);

		if (pos % 8 > 0) // pos doesn'f fall on a byte boundary
		{
			// => handle the first bits bit per bit
			int i;
			for (i = pos; i < (pos + Math.min(8 - (pos % 8), count)); i++)
				setBit(i);

			count -= (i - pos);
			pos = i;
		}

		if (count == 0)
			return;

		if (count % 8 > 0) // the last bits do not fall on a byte boundary
		{
			// => handle the last bits bit per bit
			for (int i = (pos + count - (count % 8)); i < (pos + count); i++)
				setBit(i);

			count -= (pos + count) % 8;
		}

	

		if (count == 0)
			return;

		for (int i = (pos / 8); i < (pos / 8) + (count / 8); i++)
			// handle the rest (whole bytes in the middle)
			bitmap[i] = (byte) 0xFF;
	}

	/**
	 * Clears multiple bits. Clear <code>count</code> bits, starting at
	 * <code>pos</code>.
	 * 
	 * @param pos
	 *            Position of the first bit to clear
	 * @param count
	 *            Number of bits to clear
	 */
	public void clearBits(int pos, int count)
	{
		if (pos >= size)
			return;

		if ((pos + count) >= size)
			count = (size - pos);

		if (pos % 8 > 0) // pos doesn'f fall on a byte boundary
		{
			// => handle the first bits bit per bit
			int i;
			for (i = pos; i < (pos + Math.min(8 - (pos % 8), count)); i++)
				clearBit(i);

			count -= (i - pos);
			pos = i;
		}

		if (count == 0)
			return;

		if (count % 8 > 0) // the last bits do not fall on a byte boundary
		{
			// => handle the last bits bit per bit
			for (int i = (pos + count - (count % 8)); i < (pos + count); i++)
				clearBit(i);

			count -= (pos + count) % 8;
		}

		

		if (count == 0)
			return;

		for (int i = (pos / 8); i < (pos / 8) + (count / 8); i++)
			// handle the rest (whole bytes in the middle)
			bitmap[i] = 0;
	}

	/**
	 * Check whether a bit at a given position is set.
	 * 
	 * @param pos
	 *            Position of the bit to check
	 * @return Whether the bit is set
	 */
	public boolean isSet(int pos)
	{
		if (pos >= size)
			return false;
		int i = (byte) (1 << (pos % 8)) & bitmap[pos / 8];
		return i != 0;
	}

	/**
	 * Tests if all bits of the bitmap are set.
	 * 
	 * @return True if all bits are set
	 */
	public boolean allSet()
	{
		if (size % 8 > 0) // the last byte is not filled by the bitmap
		{
			// Set all bits that exceed the bitmap to 1
			int i = 0xFF << (size % 8);
			bitmap[bitmap.length - 1] |= i;
		}

		for (int i = 0; i < bitmap.length; i++)
			if (bitmap[i] != (byte) 0xFF)
				return false;

		return true;
	}

	/**
	 * Tests if all bits of the bitmap until a certain position are set.
	 * 
	 * @param until
	 *            Number of bits to check from the start
	 * @return True if all bits until <code>until</code> are set
	 */
	public boolean allSet(int until)
	{
		if (until >= size)
			return allSet();

		for (int i = 0; i < until / 8; i++)
			if (bitmap[i] != (byte) 0xFF)
				return false;

		if (until % 8 > 0) // the last byte is not filled by the bitmap
		{
			// Set all bits that ecxeed the bitmap to 1
			byte b = bitmap[until / 8];

			int m = 0xFF << (until % 8);
			b |= m;

			if (b != (byte) 0xFF)
				return false;
		}

		return true;
	}

	/**
	 * Returns a string that shows every bit in the given byte.
	 * 
	 * @param b
	 *            The byte
	 * @return string representation of a byte
	 */
	public String byteToBitString(byte b)
	{
		String res = "|";
		for (byte j = 0; j < 8; j++)
			res += ((b >> j) & (byte) 1) == 1 ? "1|" : "0|";

		return res;
	}

	/**
	 * Prints the content of the bitmap to {@link System#out}.
	 */
	public void print()
	{
		System.out.println("+---------------+");
		for (int i = 0; i < bitmap.length; i++)
			if ((i == bitmap.length - 1) && (size % 8 > 0))
			{
				char[] s = byteToBitString(bitmap[i]).toCharArray();
				for (byte b = (byte) (size % 8); b < 8; b++)
					s[b * 2 + 1] = 'x';
				System.out.println(s);
			}
			else
				System.out.println(byteToBitString(bitmap[i]));
		System.out.println("+---------------+");
	}
}
