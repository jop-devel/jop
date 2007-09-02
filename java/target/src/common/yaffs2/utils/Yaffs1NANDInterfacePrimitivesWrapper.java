package yaffs2.utils;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;

public class Yaffs1NANDInterfacePrimitivesWrapper 
	implements Yaffs1NANDInterfacePrimitives
{
	yaffs_Device dev;
	Yaffs1NANDInterface intfce;
	
	public Yaffs1NANDInterfacePrimitivesWrapper(yaffs_Device dev, 
			Yaffs1NANDInterface intfce)
	{
		this.dev = dev;
		this.intfce = intfce;
	}
	
	public boolean eraseBlockInNAND(int deviceGenericDevice,
			int devicenDataBytesPerChunk, int blockInNAND)
	{
		return intfce.eraseBlockInNAND(dev, blockInNAND);
	}

	public boolean initialiseNAND(int deviceGenericDevice,
			int devicenDataBytesPerChunk)
	{
		return intfce.initialiseNAND(dev);
	}

	public boolean readChunkFromNAND(int deviceGenericDevice,
			int devicenDataBytesPerChunk, int chunkInNAND, byte[] data,
			int dataIndex, byte[] spare, int spareIndex)
	{
		return intfce.readChunkFromNAND(dev, chunkInNAND, data, dataIndex,
				spare == null ? null : new yaffs_Spare(spare, spareIndex));
	}

	public boolean writeChunkToNAND(int deviceGenericDevice,
			int devicenDataBytesPerChunk, int chunkInNAND, byte[] data,
			int dataIndex, byte[] spare, int spareIndex)
	{
		return intfce.writeChunkToNAND(dev, chunkInNAND, data, dataIndex,
				spare == null ? null : new yaffs_Spare(spare, spareIndex));
	}	
}
