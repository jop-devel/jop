package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_CheckpointValidity extends SerializableObject
{
	static final int SERIALIZED_LENGTH = 4*4; 
	
	public yaffs_CheckpointValidity()
	{
		super(SERIALIZED_LENGTH);
	}

	//typedef struct {
	int structType()
	{
		return Utils.getIntFromByteArray(serialized, offset);
	}
	void setStructType(int value)
	{
		Utils.writeIntToByteArray(serialized, offset, value);
	}
	/** __u32 */ int magic()
	{
		return Utils.getIntFromByteArray(serialized, offset+4);
	}
	/** __u32 */ void setMagic(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+4, value);
	}
	/** __u32 */ int version()
	{
		return Utils.getIntFromByteArray(serialized, offset+8);
	}
	/** __u32 */ void setVersion(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+8, value);
	}
	/** __u32 */ int head()
	{
		return Utils.getIntFromByteArray(serialized, offset+12);
	}
	/** __u32 */ void setHead(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+12, value);
	}

	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
	
	//} yaffs_CheckpointValidity;

}
