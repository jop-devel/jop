package yaffs2.platform.jop;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.Yaffs1NANDInterface;

// XXX change to implement Yaffs1NANDInterface, add constructor for initialization
public class InternalNANDYaffs1NANDInterface implements Yaffs1NANDInterface 
{
	public static InternalNANDYaffs1NANDInterface instance = 
		new InternalNANDYaffs1NANDInterface(
				InternalNANDYaffs1NANDInterfacePrimitives.instance);
	
	protected InternalNANDYaffs1NANDInterfacePrimitives primitivesInstance; 
	
	protected InternalNANDYaffs1NANDInterface(InternalNANDYaffs1NANDInterfacePrimitives
			instance)
	{
		this.primitivesInstance = instance;
	}
	
	/**
	 * 
	 *  writes data to nand at the address given
	 *  returns true if error occurred
	 */
	public boolean writeChunkToNAND(yaffs_Device dev, int chunkInNAND, 
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		return primitivesInstance.writeChunkToNAND(
				dev.subField1.genericDevice, dev.subField1.nDataBytesPerChunk, 
				chunkInNAND, data, dataIndex, spare.serialized, spare.offset); 
	}
	
	public boolean eraseBlockInNAND(yaffs_Device dev, int blockNumber)
	{
		return primitivesInstance.eraseBlockInNAND(
				dev.subField1.genericDevice, dev.subField1.nDataBytesPerChunk,
				blockNumber);
	}
	
	
	/*public byte[] readFromNAND(int chunkInNAND) */
	public boolean readChunkFromNAND(yaffs_Device dev, int chunkInNAND, byte[] data, int dataIndex, yaffs_Spare spare)
	{
		return primitivesInstance.readChunkFromNAND(
				dev.subField1.genericDevice, dev.subField1.nDataBytesPerChunk,
				chunkInNAND, data, dataIndex, spare.serialized, spare.offset);
	}
	
	public boolean initialiseNAND(yaffs_Device dev)
	{
		return primitivesInstance.initialiseNAND(
				dev.subField1.genericDevice, dev.subField1.nDataBytesPerChunk);
	}
}