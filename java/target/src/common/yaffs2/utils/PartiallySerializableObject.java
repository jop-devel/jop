package yaffs2.utils;

public abstract class PartiallySerializableObject extends SerializableObject
{
	// the other constructors could pose "data integrity" problems 
	
	/**
	 * The object is not "contained" in another struct.
	 *
	 */
	public PartiallySerializableObject(int serializedLength)
	{
		super(serializedLength);
	}

}
