package yaffs2.port;

import yaffs2.utils.*;
import static yaffs2.utils.Utils.*;

public class yaffs_CheckpointDevice extends SerializableObject
{
	static final int SERIALIZED_LENGTH = 10*4; 

	public yaffs_CheckpointDevice()
	{
		super(SERIALIZED_LENGTH);
	}

	/* The CheckpointDevice structure holds the device information that changes at runtime and
	 * must be preserved over unmount/mount cycles.
	 */
	//typedef struct {
	int structType()
	{
		return getIntFromByteArray(serialized, offset+0);
	}
	void setStructType(int value)
	{
		writeIntToByteArray(serialized, offset+0, value);
	}
	int nErasedBlocks()
	{
		return getIntFromByteArray(serialized, offset+4);
	}
	void setNErasedBlocks(int value)
	{
		writeIntToByteArray(serialized, offset+4, value);
	}
	int allocationBlock() 	/* Current block being allocated off */
	{
		return getIntFromByteArray(serialized, offset+8);
	}
	void setAllocationBlock(int value)
	{
		writeIntToByteArray(serialized, offset+8, value);
	}
	/**__u32*/ int allocationPage()
	{
		return getIntFromByteArray(serialized, offset+12);
	}
	void setAllocationPage(int value)
	{
		writeIntToByteArray(serialized, offset+12, value);
	}
	int nFreeChunks()
	{
		return getIntFromByteArray(serialized, offset+16);
	}	
	void setNFreeChunks(int value)
	{
		writeIntToByteArray(serialized, offset+16, value);
	}


	int nDeletedFiles()		/* Count of files awaiting deletion;*/
	{
		return getIntFromByteArray(serialized, offset+20);
	}
	void setNDeletedFiles(int value)
	{
		writeIntToByteArray(serialized, offset+20, value);
	}
	int nUnlinkedFiles()		/* Count of unlinked files. */
	{
		return getIntFromByteArray(serialized, offset+24);
	}
	void setNUnlinkedFiles(int value)
	{
		writeIntToByteArray(serialized, offset+24, value);
	}
	int nBackgroundDeletions()	/* Count of background deletions. */
	{
		return getIntFromByteArray(serialized, offset+28);
	}
	void setNBackgroundDeletions(int value)
	{
		writeIntToByteArray(serialized, offset+28, value);
	}

	/* yaffs2 runtime stuff */ // XXX check conversion
	/**unsigned*/ int sequenceNumber()	/* Sequence number of currently allocating block */
	{
		return getIntFromByteArray(serialized, offset+32);
	}
	/**@param value unsigned*/ void setSequenceNumber(int value)
	{
		writeIntToByteArray(serialized, offset+32, value);
	}
	/**unsigned*/ int oldestDirtySequence()	// XXX check conversion
	{
		return getIntFromByteArray(serialized, offset+36);
	}
	/**@param value unsigned*/ void setOldestDirtySequence(int value)
	{
		writeIntToByteArray(serialized, offset+36, value);
	}

	@Override
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}

	//} yaffs_CheckpointDevice;
}
