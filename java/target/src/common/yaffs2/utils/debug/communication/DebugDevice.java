package yaffs2.utils.debug.communication;

import yaffs2.port.yaffs_Device;

public class DebugDevice
{
	public static yaffs_Device createDebugDevice()
	{
		yaffs_Device bootDev = new yaffs_Device();
		
		// /boot
//		memset(bootDev);
		bootDev.subField1.nDataBytesPerChunk = 512;
		bootDev.subField1.nChunksPerBlock = 32;
		bootDev.subField1.nReservedBlocks = 5;
		bootDev.subField1.startBlock = 0; // Can use block 0
		bootDev.subField1.endBlock = 2047; // Last block
		//bootDev.useNANDECC = 0; // use YAFFS's ECC
		bootDev.subField1.nShortOpCaches = 10; // Use caches
		bootDev.subField1.genericDevice = /*(void *)*/ 1;	// Used to identify the device in fstat.
		
		return bootDev;
	}
}
