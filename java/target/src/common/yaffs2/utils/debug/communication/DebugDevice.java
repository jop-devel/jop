package yaffs2.utils.debug.communication;

import yaffs2.port.yaffs_Device;

public class DebugDevice
{
	protected static yaffs_Device dev = createDebugDevice();
	
	protected static yaffs_Device createDebugDevice()
	{
		yaffs_Device bootDev = new yaffs_Device();
		
//		memset(bootDev);
		bootDev.subField1.nDataBytesPerChunk = DebugSettings.NDATABYTESPERCHUNK;
		bootDev.subField1.nChunksPerBlock = 32;
		bootDev.subField1.nReservedBlocks = 5;
		bootDev.subField1.startBlock = 1; // Can't use block 0
		bootDev.subField1.endBlock = 63; // Last block
		bootDev.subField1.useNANDECC = true; // use YAFFS's ECC
		bootDev.subField1.nShortOpCaches = 10; // Use caches
		bootDev.subField1.genericDevice = /*(void *)*/ DebugSettings.GENERIC_DEVICE;	// Used to identify the device in fstat.
		
		return bootDev;		
	}
	
	public static yaffs_Device getDebugDevice()
	{
		return dev;
	}
}
