package yaffs2.utils;

public abstract class SerializableObject
{
	// XXX does not need a pool
	/**
	 * The object is "contained" in the owner struct. 
	 * @param owner
	 * @param offset
	 */
	public SerializableObject(SerializableObject owner, int offset)
	{
		this.serialized = owner.serialized;
		this.offset = offset;
	}
	
	// XXX needs a separate pool(?)
	/**
	 * The object is "contained" in an array buffer.
	 * @param array
	 * @param offset
	 */
	public SerializableObject(byte[] array, int offset)
	{
		this.serialized = array;
		this.offset = offset;
	}
	
	/**
	 * The object is not "contained" in another struct.
	 *
	 */
	public SerializableObject(int serializedLength)
	{
		assert serializedLength > 0;
		
		serialized = new byte[serializedLength]; 
	}

	public int offset;
	public byte[] serialized;
	
	public abstract int getSerializedLength();
}
