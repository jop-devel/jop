package yaffs2.port;

import yaffs2.utils.*;
import static yaffs2.utils.Utils.*;

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
		return getIntFromByteArray(serialized, 0);
	}
	void setSequenceNumber(int value)
	{
		writeIntToByteArray(serialized, 0, value);
	}
//	unsigned objectId;
	int objectId()
	{
		return getIntFromByteArray(serialized, 4);
	}
	void setObjectId(int value)
	{
		writeIntToByteArray(serialized, 4, value);
	}
	void andObjectId(int value)
	{
		writeIntToByteArray(serialized, 4, value & getIntFromByteArray(serialized, 4));		
	}
	void orObjectId(int value)
	{
		writeIntToByteArray(serialized, 4, value | getIntFromByteArray(serialized, 4));
	}
//	unsigned chunkId;
	int chunkId()
	{
		return getIntFromByteArray(serialized, 8);
	}
	void setChunkId(int value)
	{
		writeIntToByteArray(serialized, 8, value);
	}
	void orChunkId(int value)
	{
		writeIntToByteArray(serialized, 8, value | getIntFromByteArray(serialized, 8));		
	}
//	unsigned byteCount;
	int byteCount()
	{
		return getIntFromByteArray(serialized, 12);
	}
	void setByteCount(int value)
	{
		writeIntToByteArray(serialized, 12, value);
	}
//	} yaffs_PackedTags2TagsPart;

	public static final int SERIALIZED_LENGTH = 4*4;

	@Override
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
