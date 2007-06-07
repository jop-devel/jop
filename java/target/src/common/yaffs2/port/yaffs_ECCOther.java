package yaffs2.port;

import yaffs2.utils.*;
import static yaffs2.utils.Utils.*;

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
	byte colParity() // XXX changed int => byte
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
		return getIntFromByteArray(serialized, offset+4);
	}
	/**
	 * unsigned 
	 */
	void setlineParity(int value)
	{
		writeIntToByteArray(serialized, offset+4, value);
	}

	/**
	 * unsigned
	 */
	int lineParityPrime()
	{
		return getIntFromByteArray(serialized, offset+8);
	}
	/**
	 * unsigned
	 */
	void setlineParityPrime(int value)
	{
		writeIntToByteArray(serialized, offset+8, value);
	}
	
	public static final int SERIALIZED_LENGTH = 12;
	
	@Override
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
} 

