package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_Tags extends yaffs2.utils.SerializableObject
{
	/* Tags structures in RAM
	 * NB This uses bitfield. Bitfields should not straddle a u32 boundary otherwise
	 * the structure size will get blown out.
	 */

	//#ifndef CONFIG_YAFFS_NO_YAFFS1
	

	static final int SERIALIZED_LENGTH = 8;

	public yaffs_Tags()
	{
		super(SERIALIZED_LENGTH);
	}
	
	
	void setChunkId(int chunkId)
	{
		serialized[0] = (byte)chunkId;
		serialized[1] = (byte)(chunkId >>> 8);
		serialized[2] = (byte)(((chunkId >>> 16) & 0xf) | (serialized[2] & ~0xf));
	}
	int getChunkId()
	{
		return Utils.byteAsUnsignedByte(serialized[0]) | 
		(Utils.byteAsUnsignedByte(serialized[1]) << 8) | ((serialized[2] & 0xf) << 16);
	}
	
	void setSerialNumber(int serialNumber)
	{
		serialized[2] = (byte)((serialNumber & 0x3) << 4 | (serialized[2] & ~(0x3 << 4))); 
	}
	int getSerialNumber()
	{
		return (serialized[2] >>> 4) & 0x3;
	}

	void setByteCount(int byteCount)
	{
		serialized[2] = (byte)((byteCount << 6) | (serialized[2] & ~(-1 << 6)));
		serialized[3] = (byte)(byteCount >>> 2);
	}
	int getByteCount()
	{
		return (Utils.byteAsUnsignedByte(serialized[2]) >>> 6) | (Utils.byteAsUnsignedByte(serialized[3]) << 2);
	}
	
	void setObjectID(int objectId)
	{
		serialized[4] = (byte)objectId; 
		serialized[5] = (byte)(objectId >>> 8);
		serialized[6] = (byte)(((objectId >>> 16) & 0x3) | (serialized[6] & ~0x3)); 
	}
	int getObjectId()
	{
		return Utils.byteAsUnsignedByte(serialized[4]) | (Utils.byteAsUnsignedByte(serialized[5]) << 8) | 
		((serialized[6] & 0x3) << 16);
	}
		
	void setEcc(int ecc)
	{
		serialized[6] = (byte)((ecc << 2) | (serialized[6] & ~(-1 << 2)));
		serialized[7] = (byte)(((ecc >>> 6) & 0x3f) | (serialized[7] & ~0x3f));
	}	
	int getEcc()
	{
		return (Utils.byteAsUnsignedByte(serialized[6]) >>> 2) | ((serialized[7] & 0x3f) << 6);
	}	
	
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
	
	//#endif
}
