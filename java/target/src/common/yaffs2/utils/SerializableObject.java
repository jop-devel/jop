package yaffs2.utils;

import yaffs2.utils.factory.PooledObject;

public abstract class SerializableObject extends PooledObject // XXX remove if not using pooling
{
	/**
	 * The object is "contained" in the owner struct.
	 * XXX Does not need a pool.
	 * @param owner
	 * @param offset
	 */
	public SerializableObject(SerializableObject owner, int offset)
	{
		this.serialized = owner.serialized;
		this.offset = offset;
	}
	
	/**
	 * The object is "contained" in an array buffer.
	 * XXX Needs a pool.
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
//		assert serializedLength > 0;
		
		serialized = new byte[serializedLength]; 
	}

	public int offset;
	public byte[] serialized;
	
	public abstract int getSerializedLength();
}
