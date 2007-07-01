package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_PackedTags2TagsPart extends SerializableObject 
{
	public yaffs_PackedTags2TagsPart(SerializableObject owner, int offset)
	{
		super(owner, offset);
	}

//	typedef struct {
//	unsigned sequenceNumber;
	int sequenceNumber()
	{
		return Utils.getIntFromByteArray(serialized, 0);
	}
	void setSequenceNumber(int value)
	{
		Utils.writeIntToByteArray(serialized, 0, value);
	}
//	unsigned objectId;
	int objectId()
	{
		return Utils.getIntFromByteArray(serialized, 4);
	}
	void setObjectId(int value)
	{
		Utils.writeIntToByteArray(serialized, 4, value);
	}
	void andObjectId(int value)
	{
		Utils.writeIntToByteArray(serialized, 4, value & Utils.getIntFromByteArray(serialized, 4));		
	}
	void orObjectId(int value)
	{
		Utils.writeIntToByteArray(serialized, 4, value | Utils.getIntFromByteArray(serialized, 4));
	}
//	unsigned chunkId;
	int chunkId()
	{
		return Utils.getIntFromByteArray(serialized, 8);
	}
	void setChunkId(int value)
	{
		Utils.writeIntToByteArray(serialized, 8, value);
	}
	void orChunkId(int value)
	{
		Utils.writeIntToByteArray(serialized, 8, value | Utils.getIntFromByteArray(serialized, 8));		
	}
//	unsigned byteCount;
	int byteCount()
	{
		return Utils.getIntFromByteArray(serialized, 12);
	}
	void setByteCount(int value)
	{
		Utils.writeIntToByteArray(serialized, 12, value);
	}
//	} yaffs_PackedTags2TagsPart;

	public static final int SERIALIZED_LENGTH = 4*4;

	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
