package yaffs2.utils;

public interface Yaffs1NANDInterfacePrimitives
{
	public boolean writeChunkToNAND (int deviceGenericDevice, 
			int devicenDataBytesPerChunk,
			 int chunkInNAND, byte[] data, int dataIndex,
			 byte[] spare, int spareIndex);
	
	public boolean initialiseNAND (int deviceGenericDevice, 
			int devicenDataBytesPerChunk);
	
	public boolean eraseBlockInNAND (int deviceGenericDevice, 
			int devicenDataBytesPerChunk,
			 int blockInNAND);
	
	public boolean readChunkFromNAND (int deviceGenericDevice, 
			int devicenDataBytesPerChunk,
			  int chunkInNAND, byte[] data, int dataIndex,
			  byte[] spare, int spareIndex);
}
