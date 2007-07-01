package yaffs2.port;

import yaffs2.utils.SerializableObject;

public class yaffs_BlockInfo extends yaffs2.utils.SerializableObject
{

//		int softDeletions:10;	/* number of soft deleted pages */
//		int pagesInUse:10;	/* number of pages in use */
//		yaffs_BlockState blockState:4;	/* One of the above block states */
//		__u32 needsRetiring:1;	/* Data has failed on this block, need to get valid data off */
//	                        	/* and retire the block. */
//		__u32 skipErasedCheck: 1; /* If this is set we can skip the erased check on this block */
//		__u32 gcPrioritise: 1; 	/* An ECC check or bank check has failed on this block. 
//					   It should be prioritised for GC */
//	        __u32 chunkErrorStrikes:3; /* How many times we've had ecc etc failures on this block and tried to reuse it */
//
//	#ifdef CONFIG_YAFFS_YAFFS2
//	__u32 hasShrinkHeader:1; /* This block has at least one shrink object header */
//	__u32 sequenceNumber;	 /* block sequence number for yaffs2 */
//#endif

	public yaffs_BlockInfo(SerializableObject owner, int offset)
	{
		super(owner, offset);
	}
	
	public yaffs_BlockInfo(byte[] array, int offset)
	{
		super(array, offset);
	}
	
	public yaffs_BlockInfo()
	{
		super(SERIALIZED_LENGTH);
	}
	
	static final int SERIALIZED_LENGTH = 4+4; 
	
	int softDeletions()
	{
		return yaffs2.utils.Utils.byteAsUnsignedByte(serialized[offset+0]) | ((serialized[offset+1] & 0x3) << 8); 
	}
	void setSoftDeletions(int value)
	{
		serialized[offset+0] = (byte)value;
		serialized[offset+1] = (byte)(serialized[offset+1] & ~0x3 | ((value & 0x300) >>> 8));
	}
	int pagesInUse()
	{
		return (yaffs2.utils.Utils.byteAsUnsignedByte(serialized[offset+1]) >>> 2) | ((serialized[offset+2] & 0xf ) << 2);
	}
	void setPagesInUse(int value)
	{
		serialized[offset+1] = (byte)(serialized[offset+1] & 0x3 | (value << 2));
		serialized[offset+2] = (byte)(serialized[offset+2] & ~0xf | ((value & 0x3C0) >>> 6));
	}
	int blockState()
	{
		return (yaffs2.utils.Utils.byteAsUnsignedByte(serialized[offset+2]) >>> 4);
	}
	void setBlockState(int value)
	{
		serialized[offset+2] = (byte)(serialized[offset+2] & ~0xF0 | (value << 4));
	}
	boolean needsRetiring()
	{
		return (serialized[offset+3] & 0x1) != 0;
	}
	void setNeedsRetiring(boolean value)
	{
		serialized[offset+3] = (byte)(serialized[offset+3] & ~0x1 | (value ? 0x1 : 0));
	}
	boolean skipErasedCheck()
	{
		return (serialized[offset+3] & 0x2) != 0;
	}
	void setSkipErasedCheck(boolean value)
	{
		serialized[offset+3] = (byte)(serialized[offset+3] & ~0x2 | (value ? 0x2 : 0));
	}
	boolean gcPrioritise()
	{
		return (serialized[offset+3] & 0x4) != 0;
	}
	void setGcPrioritise(boolean value)
	{
		serialized[offset+3] = (byte)(serialized[offset+3] & ~0x4 | (value ? 0x4 : 0));
	}
	int chunkErrorStrikes()
	{
		return (serialized[offset+3] >>> 3) & 0x7;
	}
	void setChunkErrorStrikes(int value)
	{
		serialized[offset+3] = (byte)(serialized[offset+3] & (~0x38) | ((value & 0x7) << 3));
	}

	//#ifdef CONFIG_YAFFS_YAFFS2 
	
	/*__u32*/ boolean hasShrinkHeader() /* This block has at least one shrink object header */
	{
		return ((serialized[offset+3] >>> 6) & 0x1) != 0;
	}
	void setHasShrinkHeader(boolean value)
	{
		serialized[offset+3] = (byte)(serialized[offset+3] & ~0x40 | (value ? 0x40 : 0));
	}
	// XXX PORT may be larger than Integer.MAX_VALUE, which causes sign extension for longs!
	// XXX check every invocation
	/*__u32*/ int sequenceNumber()	 /* block sequence number for yaffs2 */
	{
		return yaffs2.utils.Utils.getIntFromByteArray(serialized, offset+4);
	}
	void setSequenceNumber(/*__u32*/ int value)
	{
		yaffs2.utils.Utils.writeIntToByteArray(serialized, offset+4, value);
	}
	//#endif

	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
