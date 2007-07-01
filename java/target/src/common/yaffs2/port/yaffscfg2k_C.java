package yaffs2.port;

/**
 * PORT See XXX for an example configuration.
 * PORT See also original yaffscfg2k.c.
 *
 */
public interface yaffscfg2k_C
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
//
//	#include <errno.h>

	/*unsigned*/ public int yaffs_traceMask();	

	/**
	 * Do whatever to set error
	 * @param err
	 */
	public void yaffsfs_SetError(int err);
	
	public void yaffsfs_Lock();

	public void yaffsfs_Unlock();

	/*__u32*/ public int yaffsfs_CurrentTime();

	/**
	 * Define locking semaphore.
	 */
	public void yaffsfs_LocalInitialisation();

////	 Configuration for:
////	 /ram  2MB ramdisk
////	 /boot 2MB boot disk (flash)
////	 /flash 14MB flash disk (flash)
////	 NB Though /boot and /flash occupy the same physical device they
////	 are still disticnt "yaffs_Devices. You may think of these as "partitions"
////	 using non-overlapping areas in the same device.
////	 
//
////	#include "yaffs_ramdisk.h"
////	#include "yaffs_flashif.h"
////	#include "yaffs_nandemul2k.h"
//
//	static yaffs_Device ramDev;
//	static yaffs_Device bootDev;
//	static yaffs_Device flashDev;
//	static yaffs_Device ram2kDev;

	public yaffsfs_DeviceConfiguration[] yaffsfs_config();



	/**
	 * XXX After calling yaffs2.utils.Globals.startUp(), call this method.
	 */  
	public int yaffs_StartUp();


//	public void SetCheckpointReservedBlocks(int n);
//	{
//		flashDev.nCheckpointReservedBlocks = n;
//	}
}
