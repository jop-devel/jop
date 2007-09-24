package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_PackedTags1 extends yaffs2.utils.SerializableObject
{
	public yaffs_PackedTags1()
	{
		super(SERIALIZED_LENGTH);
	}

//	typedef struct {
//	unsigned chunkId:20;
	int chunkId()
	{
		return Utils.byteAsUnsignedByte(serialized[offset]) | (Utils.byteAsUnsignedByte(serialized[offset+1]) << 8) | 
		((serialized[offset+2] & 0xF) << 16);
	}
	void setChunkId(int value)
	{
		serialized[offset] = (byte)value;
		serialized[offset+1] = (byte)(value >>> 8);
		serialized[offset+2] = (byte)((serialized[offset+2] & ~0xF) | 
				((value >>> 16) & 0xF));
	}
//	unsigned serialNumber:2;
	byte serialNumber()
	{
		return (byte)((serialized[offset+2] >>> 4) & 0x3); 
	}
	void setSerialNumber(byte value)
	{
		serialized[offset+2] = (byte)((serialized[offset+2] & ~(0x3 << 4)) |
				((value & 0x3) << 4));
	}
//	unsigned byteCount:10;
	int byteCount()
	{
		return ((Utils.byteAsUnsignedByte(serialized[offset+2]) >>> 6) | (Utils.byteAsUnsignedByte(serialized[offset+3]) << 2));
	}
	void setByteCount(int value)
	{
		serialized[offset+2] = (byte)((serialized[offset+2] & (~(0x3 << 6)) | 
				((value & 0x3) << 6)));
		serialized[offset+3] = (byte)(value >>> 2);
	}
//	unsigned objectId:18;
	int objectId()
	{
		return Utils.byteAsUnsignedByte(serialized[offset+4]) | (Utils.byteAsUnsignedByte(serialized[offset+5]) << 8) |
		((serialized[offset+6] & 0x3) << 16);
	}
	void setObjectId(int value)
	{
		serialized[offset+4] = (byte)value;
		serialized[offset+5] = (byte)(value >>> 8);
		serialized[offset+6] = (byte)((value >>> 16) & 0x3); 
	}
//	unsigned ecc:12;
	int ecc()
	{
		return (Utils.byteAsUnsignedByte(serialized[offset+6]) >>> 2) | ((serialized[offset+7] & 0x3F) << 6);
	}
	void setEcc(int value)
	{
		serialized[offset+6] = (byte)((serialized[offset+6] & ~(0x3F << 2) |
				((value & 0x3F) << 2)));
		serialized[offset+7] = (byte)((serialized[offset+7] & ~(0x3F)) |
				((value >>> 8) & 0x3F));
	}
//	unsigned deleted:1;
	boolean deleted()
	{
		return ((serialized[offset+7] & 0x40) != 0);
	}
	void setDeleted(boolean value)
	{
		serialized[offset+7] = (byte)((serialized[offset+7] & ~0x40) |
				(value ? 0x40 : 0));
	}
//	unsigned unusedStuff:1;
	boolean unusedStuff()
	{
		return ((serialized[offset+7] & 0x80) != 0);
	}
	void setUnusedStuff(boolean value)
	{
		serialized[offset+7] = (byte)((serialized[offset+7] & ~0x80) |
				(value ? 0x80 : 0));			
	}
//	unsigned shouldBeFF;
	int shouldBeFF()
	{
		return Utils.getIntFromByteArray(serialized, offset+8);
	}
	void setShouldBeFF(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+8, value);
	}

//	} yaffs_PackedTags1;

	public static final int SERIALIZED_LENGTH = 8+4;

	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
