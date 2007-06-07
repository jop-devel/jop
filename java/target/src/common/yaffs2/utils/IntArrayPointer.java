package yaffs2.utils;

import static yaffs2.utils.Utils.*;

/**
 * N.B. that modifications of the index will propagate downward.  
 *
 */
public class IntArrayPointer
{
	public IntArrayPointer(byte[] array, int index)
	{
		this.array = array;
		this.index = index;
	}

	public IntArrayPointer()
	{
	}

	public void set(int value)
	{
		writeIntToByteArray(array, index, value);
	}
	
	public int get()
	{
		return getIntFromByteArray(array, index);
	}

	public byte[] array;
	public int index;
}
