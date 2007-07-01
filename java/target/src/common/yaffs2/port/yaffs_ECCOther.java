package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_ECCOther extends SerializableObject {
	public yaffs_ECCOther(SerializableObject owner, int offset)
	{
		super(owner, offset);
	}

	public yaffs_ECCOther()
	{
		super(SERIALIZED_LENGTH);
	}
	
	/**
	 * unsigned char
	 */
	byte colParity()
	{
		return serialized[offset]; 
	}
	/**
	 * unsigned char
	 */
	void setcolParity(byte value)
	{
		serialized[offset] = value; 
	}
	
	/**
	 * unsigned 
	 */
	int lineParity()
	{
		return Utils.getIntFromByteArray(serialized, offset+4);
	}
	/**
	 * unsigned 
	 */
	void setlineParity(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+4, value);
	}

	/**
	 * unsigned
	 */
	int lineParityPrime()
	{
		return Utils.getIntFromByteArray(serialized, offset+8);
	}
	/**
	 * unsigned
	 */
	void setlineParityPrime(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+8, value);
	}
	
	public static final int SERIALIZED_LENGTH = 12;
	
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
} 

