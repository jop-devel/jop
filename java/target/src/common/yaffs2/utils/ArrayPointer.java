package yaffs2.utils;

/**
 * N.B. that modifications of the index will propagate downward.  
 *
 */
public class ArrayPointer
{
	public ArrayPointer(ArrayPointer arrayPointer, int index)
	{
		this.array = arrayPointer.array;
		this.index = arrayPointer.index + index;
	}
	
	public ArrayPointer(ArrayPointer arrayPointer)
	{
		this.array = arrayPointer.array;
		this.index = arrayPointer.index;
	}
	
	public ArrayPointer(byte[] array, int index)
	{
		this.array = array;
		this.index = index;
	}

	public ArrayPointer(byte[] array)
	{
		this.array = array;
	}
	
	public ArrayPointer()
	{
		
	}

	
	public void set(int relativeIndex, byte value)
	{
		array[index + relativeIndex] = value;
	}
	public void set(byte value)
	{
		array[index] = value;
	}
	
	public byte get(int relativeIndex)
	{
		return array[index + relativeIndex];
	}
	
	public byte get()
	{
		return array[index];
	}
	
	public void increment()
	{
		index++;
	}
	public void increment(int incBy)
	{
		index += incBy;
	}
	
	public boolean isNotZero()
	{
		return array[index] != 0;
	}
	
	public byte[] array;
	public int index;
}
