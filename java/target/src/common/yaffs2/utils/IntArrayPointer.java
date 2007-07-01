package yaffs2.utils;

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
		Utils.writeIntToByteArray(array, index, value);
	}
	
	public int get()
	{
		return Utils.getIntFromByteArray(array, index);
	}

	public byte[] array;
	public int index;
}
