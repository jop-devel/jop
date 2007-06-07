package yaffs2.port;

import yaffs2.utils.*;
import static yaffs2.utils.Utils.*;

public class yaffs_CheckpointObject extends SerializableObject
{
	static final int SERIALIZED_LENGTH = 28;

	public yaffs_CheckpointObject()
	{
		super(SERIALIZED_LENGTH);
	}

	/* yaffs_CheckpointObject holds the definition of an object as dumped 
	 * by checkpointing.
	 */

	//typedef struct {
	int structType()
	{
		return getIntFromByteArray(serialized, offset);
	}
	void setStructType(int value)
	{
		writeIntToByteArray(serialized, offset, value);
	}

	/**__u32*/ int objectId()
	{
		return getIntFromByteArray(serialized, offset+4);
	}
	void setObjectId(int value)
	{
		writeIntToByteArray(serialized, offset+4, value);
	}

	/**__u32*/ int parentId()
	{
		return getIntFromByteArray(serialized, offset+8);
	}
	void setParentId(int value)
	{
		writeIntToByteArray(serialized, offset+8, value);
	}

	int chunkId()
	{
		return getIntFromByteArray(serialized, offset+12);
	}	
	void setChunkId(int value)
	{
		writeIntToByteArray(serialized, offset+12, value);
	}


//	yaffs_ObjectType variantType:3;
	int variantType()
	{
		return serialized[offset+16] & 0x7;
	}
	void setVariantType(int value)
	{
		serialized[offset+16] = (byte)((value & 0x7) | (serialized[offset+16] & ~0x7));
	}

//	__u8 deleted:1;		
	boolean deleted()
	{
		return (serialized[offset+16] & (1<<3)) != 0;
	}
	void setDeleted(boolean value)
	{
		serialized[offset+16] = (byte)((value ? 1<<3 : 0) | (serialized[offset+16] & ~(1<<3)));
	}

//	__u8 softDeleted:1;
	boolean softDeleted()
	{
		return (serialized[offset+16] & (1<<4)) != 0;
	}
	void setSoftDeleted(boolean value)
	{
		serialized[offset+16] = (byte)((value ? 1<<4 : 0) | (serialized[offset+16] & ~(1<<4)));
	}

//	__u8 unlinked:1;
	boolean unlinked()
	{
		return (serialized[offset+16] & (1<<5)) != 0;
	}
	void setUnlinked(boolean value)
	{
		serialized[offset+16] = (byte)((value ? 1<<5 : 0) | (serialized[offset+16] & ~(1<<5)));
	}

//	__u8 fake:1;
	boolean fake()
	{
		return (serialized[offset+16] & (1<<6)) != 0;
	}
	void setFake(boolean value)
	{
		serialized[offset+16] = (byte)((value ? 1<<6 : 0) | (serialized[offset+16] & ~(1<<6)));
	}

//	__u8 renameAllowed:1;
	boolean renameAllowed()
	{
		return (serialized[offset+16] & (1<<7)) != 0;
	}
	void setRenameAllowed(boolean value)
	{
		serialized[offset+16] = (byte)((value ? 1<<7 : 0) | (serialized[offset+16] & ~(1<<7)));
	}

//	__u8 unlinkAllowed:1;
	boolean unlinkAllowed()
	{
		return (serialized[offset+17] & (1<<0)) != 0;
	}
	void setUnlinkAllowed(boolean value)
	{
		serialized[offset+16] = (byte)((value ? 1<<0 : 0) | (serialized[offset+16] & ~(1<<0)));
	}

//	__u8 serial;
	byte serial()
	{
		return serialized[offset+18];
	}
	void setSerial(byte value)
	{
		serialized[offset+18] = value;
	}


	int nDataChunks()
	{
		return getIntFromByteArray(serialized, offset+20);
	}	
	void setNDataChunks(int value)
	{
		writeIntToByteArray(serialized, offset+20, value);
	}

	/**__u32*/ int fileSizeOrEquivalentObjectId()
	{
		return getIntFromByteArray(serialized, offset+24);
	}	
	void setFileSizeOrEquivalentObjectId(int value)
	{
		writeIntToByteArray(serialized, offset+24, value);
	}

	@Override
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}

	//}yaffs_CheckpointObject;
}
