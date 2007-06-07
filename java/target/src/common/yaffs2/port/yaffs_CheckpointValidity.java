package yaffs2.port;

import static yaffs2.utils.Utils.*;
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
		return getIntFromByteArray(serialized, offset);
	}
	void setStructType(int value)
	{
		writeIntToByteArray(serialized, offset, value);
	}
	/** __u32 */ int magic()
	{
		return getIntFromByteArray(serialized, offset+4);
	}
	/** __u32 */ void setMagic(int value)
	{
		writeIntToByteArray(serialized, offset+4, value);
	}
	/** __u32 */ int version()
	{
		return getIntFromByteArray(serialized, offset+8);
	}
	/** __u32 */ void setVersion(int value)
	{
		writeIntToByteArray(serialized, offset+8, value);
	}
	/** __u32 */ int head()
	{
		return getIntFromByteArray(serialized, offset+12);
	}
	/** __u32 */ void setHead(int value)
	{
		writeIntToByteArray(serialized, offset+12, value);
	}

	@Override
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
	
	//} yaffs_CheckpointValidity;

}
