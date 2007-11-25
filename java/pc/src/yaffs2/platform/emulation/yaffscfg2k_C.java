package yaffs2.platform.emulation;

import yaffs2.port.*;
import yaffs2.utils.Globals;

import yaffs2.utils.*;
import yaffs2.utils.debug.communication.DebugDevice;

public class yaffscfg2k_C implements yaffs2.port.yaffscfg2k_C
{
	/*
	 * YAFFS: Yet Another Flash File System. A NAND-flash specific file system.
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License version 2 as
	 * published by the Free Software Foundation.
	 */

	/*
	 * yaffscfg2k.c  The configuration for the "direct" use of yaffs.
	 *
	 * This file is intended to be modified to your requirements.
	 * There is no need to redistribute this file.
	 */

//	#include "yaffscfg.h"
//	#include "yaffsfs.h"
//	#include "yaffs_fileem2k.h"
//	#include "yaffs_nandemul2k.h"

//	#include <errno.h>

	// PORT CONFIGURATION
	
	protected static final boolean USE_SPARE_TAGS = true;
	
	
	public /*unsigned*/ int yaffs_traceMask()
	{
		return
		yportenv.YAFFS_TRACE_SCAN |  
		yportenv.YAFFS_TRACE_GC | yportenv.YAFFS_TRACE_GC_DETAIL | 
		yportenv.YAFFS_TRACE_WRITE  | yportenv.YAFFS_TRACE_ERASE | 
		yportenv.YAFFS_TRACE_TRACING | 
		yportenv.YAFFS_TRACE_ALLOCATE | 
		yportenv.YAFFS_TRACE_CHECKPOINT |
		yportenv.YAFFS_TRACE_BAD_BLOCKS |

		// PORT user configured
		
//		yportenv.PORT_TRACE_CHECKSUMS |
//		yportenv.PORT_TRACE_TOPLEVEL |
//		yportenv.PORT_TRACE_TALLNESS |
//		yportenv.PORT_TRACE_NANDSIM |
//	    yportenv.PORT_TRACE_TNODE |
		yportenv.YAFFS_TRACE_ALWAYS |
		(~0) |

		0;
	}



	public void yaffsfs_SetError(int err)
	{
		if (err != 0)
		{
			//Do whatever to set error
			Globals.logStream.println("Error number " + err + "!");
//			new UnexpectedException().printStackTrace();
//			new Exception().getStackTrace()[1].getClassName() + ":" +
//			new Exception().getStackTrace()[1].getMethodName() + ":" +
//			new Exception().getStackTrace()[1].getLineNumber());
		}

//		errno = err;
	}

	public void yaffsfs_Lock()
	{
	}

	public void yaffsfs_Unlock()
	{
	}

	public /*__u32*/ int yaffsfs_CurrentTime()
	{
		return 0;
	}

	public void yaffsfs_LocalInitialisation()
	{
		// Define locking semaphore.
	}

//	Configuration for:
//	/ram  2MB ramdisk
//	/boot 2MB boot disk (flash)
//	/flash 14MB flash disk (flash)
//	NB Though /boot and /flash occupy the same physical device they
//	are still disticnt "yaffs_Devices. You may think of these as "partitions"
//	using non-overlapping areas in the same device.


//	#include "yaffs_ramdisk.h"
//	#include "yaffs_flashif.h"
//	#include "yaffs_nandemul2k.h"

	public static yaffs_Device ramDev = new yaffs_Device();
	public static yaffs_Device device = DebugDevice.getDebugDevice();
	public static yaffs_Device flashDev = new yaffs_Device();
	public static yaffs_Device ram2kDev = new yaffs_Device();

	public yaffsfs_DeviceConfiguration[] yaffsfs_config()
	{
//		#if 0
//		{ "/ram", &ramDev},
//		{ "/boot", &bootDev},
//		{ "/flash/", &flashDev},
//		{ "/ram2k", &ram2kDev},
//		{(void *)0,(void *)0}
//		#else
		return new yaffsfs_DeviceConfiguration[] {
				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/ram"), 0, ramDev), // XXX bad style for demo
				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/"), 0, device),
				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/flash/flash"), 0, flashDev),
				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/ram2k"), 0, ram2kDev),
				new yaffsfs_DeviceConfiguration(null, 0, null) /* Null entry to terminate list */
		};
//		#endif
	};


	public int yaffs_StartUp()
	{
		// Stuff to configure YAFFS
		// Stuff to initialise anything special (eg lock semaphore).
		yaffsfs_LocalInitialisation();

		// Set up devices
		// /ram
//		memset(ramDev/*,0,sizeof(ramDev)*/);
		ramDev.subField1.nDataBytesPerChunk = 512;
		ramDev.subField1.nChunksPerBlock = 32;
		ramDev.subField1.nReservedBlocks = 2; // Set this smaller for RAM
		ramDev.subField1.startBlock = 0; // Can use block 0
		ramDev.subField1.endBlock = 127; // Last block in 2MB.	
		//ramDev.useNANDECC = 1;
		ramDev.subField1.nShortOpCaches = 0;	// Disable caching on this device.
		ramDev.subField1.genericDevice = /*(void *)*/ 0;	// Used to identify the device in fstat.
//		ramDev.writeChunkWithTagsToNAND = yramdisk_WriteChunkWithTagsToNAND;
//		ramDev.readChunkWithTagsFromNAND = yramdisk_ReadChunkWithTagsFromNAND;
//		ramDev.eraseBlockInNAND = yramdisk_EraseBlockInNAND;
//		ramDev.initialiseNAND = yramdisk_InitialiseNAND;
		ramDev.subField1.writeChunkWithTagsToNAND = yaffs2.port.emulation.yaffs_ramdisk_C.instance;
		ramDev.subField1.readChunkWithTagsFromNAND = yaffs2.port.emulation.yaffs_ramdisk_C.instance;
		ramDev.subField1.eraseBlockInNAND = yaffs2.port.emulation.yaffs_ramdisk_C.instance;
		ramDev.subField1.initialiseNAND = yaffs2.port.emulation.yaffs_ramdisk_C.instance;

		// /boot
////		memset(bootDev);
//		device.subField1.nDataBytesPerChunk = 512;
//		device.subField1.nChunksPerBlock = 32;
//		device.subField1.nReservedBlocks = 5;
//		device.subField1.startBlock = 1; // Can use block 0
//		device.subField1.endBlock = 63; // Last block
//		//bootDev.useNANDECC = 0; // use YAFFS's ECC
//		device.subField1.nShortOpCaches = 10; // Use caches
//		device.subField1.genericDevice = /*(void *)*/ 1;	// Used to identify the device in fstat.
////		bootDev.writeChunkWithTagsToNAND = yflash_WriteChunkWithTagsToNAND;
////		bootDev.readChunkWithTagsFromNAND = yflash_ReadChunkWithTagsFromNAND;
////		bootDev.eraseBlockInNAND = yflash_EraseBlockInNAND;
////		bootDev.initialiseNAND = yflash_InitialiseNAND;
////		bootDev.markNANDBlockBad = yflash_MarkNANDBlockBad;
////		bootDev.queryNANDBlock = yflash_QueryNANDBlock;
		
		if (!USE_SPARE_TAGS)
		{
			device.subField1.writeChunkWithTagsToNAND = yaffs2.port.emulation.yaffs_fileem2k_C.instance;
			device.subField1.readChunkWithTagsFromNAND = yaffs2.port.emulation.yaffs_fileem2k_C.instance;
			device.subField1.eraseBlockInNAND = yaffs2.port.emulation.yaffs_fileem2k_C.instance;
			device.subField1.initialiseNAND = yaffs2.port.emulation.yaffs_fileem2k_C.instance;
			device.subField1.markNANDBlockBad = yaffs2.port.emulation.yaffs_fileem2k_C.instance;
			device.subField1.queryNANDBlock = yaffs2.port.emulation.yaffs_fileem2k_C.instance;
		}
		else
		{
			device.subField1.writeChunkToNAND = yaffs2.port.emulation.port_fileem2k_C.instance;
			device.subField1.readChunkFromNAND = yaffs2.port.emulation.port_fileem2k_C.instance;
			device.subField1.eraseBlockInNAND = yaffs2.port.emulation.port_fileem2k_C.instance;
			device.subField1.initialiseNAND = yaffs2.port.emulation.port_fileem2k_C.instance;
		}


//		XXX implement and uncomment
//		// /flash
//		// Set this puppy up to use
//		// the file emulation space as
//		// 2kpage/64chunk per block/128MB device
////		memset(flashDev);

//		flashDev.nDataBytesPerChunk = 2048;
//		flashDev.nChunksPerBlock = 64;
//		flashDev.nReservedBlocks = 5;
//		flashDev.nCheckpointReservedBlocks = 5;
//		//flashDev.checkpointStartBlock = 1;
//		//flashDev.checkpointEndBlock = 20;
//		flashDev.startBlock = 0;
//		flashDev.endBlock = 200; // Make it smaller
//		//flashDev.endBlock = yflash_GetNumberOfBlocks()-1;
//		flashDev.isYaffs2 = true;
//		flashDev.wideTnodesDisabled=false;
//		flashDev.nShortOpCaches = 10; // Use caches
//		flashDev.genericDevice = /*(void *)*/ 2;	// Used to identify the device in fstat.
//		flashDev.writeChunkWithTagsToNAND = yflash_WriteChunkWithTagsToNAND;
//		flashDev.readChunkWithTagsFromNAND = yflash_ReadChunkWithTagsFromNAND;
//		flashDev.eraseBlockInNAND = yflash_EraseBlockInNAND;
//		flashDev.initialiseNAND = yflash_InitialiseNAND;
//		flashDev.markNANDBlockBad = yflash_MarkNANDBlockBad;
//		flashDev.queryNANDBlock = yflash_QueryNANDBlock;

//		// /ram2k
//		// Set this puppy up to use
//		// the file emulation space as
//		// 2kpage/64chunk per block/128MB device
////		memset(ram2kDev);

//		ram2kDev.nDataBytesPerChunk = nandemul2k_GetBytesPerChunk();
//		ram2kDev.nChunksPerBlock = nandemul2k_GetChunksPerBlock();
//		ram2kDev.nReservedBlocks = 5;
//		ram2kDev.startBlock = 0; // First block after /boot
//		//ram2kDev.endBlock = 127; // Last block in 16MB
//		ram2kDev.endBlock = nandemul2k_GetNumberOfBlocks() - 1; // Last block in 512MB
//		ram2kDev.isYaffs2 = true;
//		ram2kDev.nShortOpCaches = 10; // Use caches
//		ram2kDev.genericDevice = /*(void *)*/ 3;	// Used to identify the device in fstat.
//		ram2kDev.writeChunkWithTagsToNAND = nandemul2k_WriteChunkWithTagsToNAND;
//		ram2kDev.readChunkWithTagsFromNAND = nandemul2k_ReadChunkWithTagsFromNAND;
//		ram2kDev.eraseBlockInNAND = nandemul2k_EraseBlockInNAND;
//		ram2kDev.initialiseNAND = nandemul2k_InitialiseNAND;
//		ram2kDev.markNANDBlockBad = nandemul2k_MarkNANDBlockBad;
//		ram2kDev.queryNANDBlock = nandemul2k_QueryNANDBlock;

		yaffs2.port.yaffsfs_C.yaffs_initialise(yaffsfs_config());

		return 0;
	}



	public void SetCheckpointReservedBlocks(int n)
	{
		flashDev.subField1.nCheckpointReservedBlocks = n;
	}
}
