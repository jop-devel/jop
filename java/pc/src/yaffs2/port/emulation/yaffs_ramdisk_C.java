package yaffs2.port.emulation;

import yaffs2.port.Guts_H;
import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_ExtendedTags;
import yaffs2.port.yaffs_PackedTags1;
import yaffs2.port.yaffs_packedtags1_C;
import yaffs2.port.ydirectenv;
import yaffs2.port.yportenv;
import yaffs2.port.yaffs_Device.eraseBlockInNANDInterface;
import yaffs2.port.yaffs_Device.initialiseNANDInterface;
import yaffs2.port.yaffs_Device.readChunkWithTagsFromNANDInterface;
import yaffs2.port.yaffs_Device.writeChunkWithTagsToNANDInterface;
import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;


public class yaffs_ramdisk_C implements 
	yaffs_Device.writeChunkWithTagsToNANDInterface,
	yaffs_Device.readChunkWithTagsFromNANDInterface, 
	yaffs_Device.eraseBlockInNANDInterface, 
	yaffs_Device.initialiseNANDInterface
{
	// PORT
	public static final yaffs_ramdisk_C instance = new yaffs_ramdisk_C();
	
//	/*
//	 * YAFFS: Yet Another Flash File System. A NAND-flash specific file system.
//	 *
//	 * Copyright (C) 2002-2007 Aleph One Ltd.
//	 *   for Toby Churchill Ltd and Brightstar Engineering
//	 *
//	 * Created by Charles Manning <charles@aleph1.co.uk>
//	 *
//	 * This program is free software; you can redistribute it and/or modify
//	 * it under the terms of the GNU General Public License version 2 as
//	 * published by the Free Software Foundation.
//	 */
//
//	/*
//	 * yaffs_ramdisk.c: yaffs ram disk component
//	 * This provides a ram disk under yaffs.
//	 * NB this is not intended for NAND emulation.
//	 * Use this with dev->useNANDECC enabled, then ECC overheads are not required.
//	 */
//
//	const char *yaffs_ramdisk_c_version = "$Id: yaffs_ramdisk_C.java,v 1.1 2007/09/24 13:31:33 peter.hilber Exp $";
//
//
//	#include "yportenv.h"
//
//	#include "yaffs_ramdisk.h"
//	#include "yaffs_guts.h"
//	#include "devextras.h"
//	#include "yaffs_packedtags1.h"



	/*#define*/static final int SIZE_IN_MB = 2;

	/*#define*/static final int BLOCK_SIZE = (32 * 528); //16896
	/*#define*/static final int BLOCKS_PER_MEG = ((1024*1024)/(32 * 512)); //64

	static yramdisk_Device ramdisk;

	static int _STATIC_LOCAL_CheckInit_initialised = 0;
	
	static boolean CheckInit(yaffs_Device dev)
	{
//		static int initialised = 0;
		
		int i;
		int fail = 0;
		//int nBlocks; 
		int nAllocated = 0;
		
		if(_STATIC_LOCAL_CheckInit_initialised != 0) 
		{
			return Guts_H.YAFFS_OK;
		}

		_STATIC_LOCAL_CheckInit_initialised = 1;
		
		ramdisk.set_nBlocks((SIZE_IN_MB * 1024 * 1024)/(16 * 1024));
		
		ramdisk.set_block();
		
		//ramdisk.block = YMALLOC(sizeof(yramdisk_Block *) * ramdisk.nBlocks);
		
		yramdisk_BlockPointer blockPointer = new yramdisk_BlockPointer(ramdisk.block[0]); // XXX
//		ramdisk.block = blockPointer.dereferenced; 
		
		if(!(ramdisk.block != null)) return false;
		
		for(i=0; i <ramdisk.nBlocks; i++)
		{
			ramdisk.block[i] = null;
		}
		
		for(i=0; i <ramdisk.nBlocks && !(fail != 0); i++)
		{
			if((ramdisk.block[i] = new yramdisk_Block()) == null)
			{
				fail = 1;
			}
			else
			{
				/*yramdisk_*/ instance.eraseBlockInNAND(dev,i);
				nAllocated++;
			}
		}
		
		if(fail != 0)
		{
			for(i = 0; i < nAllocated; i++)
			{
				ydirectenv.YFREE(ramdisk.block[i]);
			}
			ydirectenv.YFREE(/*ramdisk.block*/blockPointer.dereferenced); // XXX ???
			
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,"Allocation failed, could only allocate %dMB of %dMB requested.\n",
					PrimitiveWrapperFactory.get(nAllocated/64),PrimitiveWrapperFactory.get(ramdisk.nBlocks * 528));
			return false;
		}
		
		
		return true;
	}

	public boolean /*yramdisk_*/ writeChunkWithTagsToNAND(yaffs_Device dev,int chunkInNAND,/*const __u8 **/byte[] data,int dataIndex, yaffs_ExtendedTags tags)
	{
		int blk;
		int pg;
		

		CheckInit(dev);
		
		blk = chunkInNAND/32;
		pg = chunkInNAND%32;
		
		
		if(data != null)
		{
			Unix.memcpy(ramdisk.block[blk].page[pg].data,0,data,dataIndex,512);
		}
		
		
		if(tags != null)
		{
			yaffs_PackedTags1 pt = new yaffs_PackedTags1();

			yaffs_packedtags1_C.yaffs_PackTags1(pt,tags);	// made it a static method

			Unix.memcpy(ramdisk.block[blk].page[pg].data,512,pt.serialized,pt.offset,/*sizeof(*/pt.SERIALIZED_LENGTH/*)*/);
		}

		return Guts_H.YAFFS_OK;	

	}


	public boolean /*yramdisk_*/ readChunkWithTagsFromNAND(yaffs_Device dev,int chunkInNAND, /*__u8 **/byte[] data, int dataIndex, yaffs_ExtendedTags tags)
	{
		int blk;
		int pg;

		
		CheckInit(dev);
		
		blk = chunkInNAND/32;
		pg = chunkInNAND%32;
		
		
		if(data != null)
		{
			Unix.memcpy(data,dataIndex,ramdisk.block[blk].page[pg].data,0,512);
		}
		
		
		if(tags != null)
		{
			yaffs_PackedTags1 pt = new yaffs_PackedTags1();
			
			Unix.memcpy(pt.serialized,pt.offset,ramdisk.block[blk].page[pg].data,512,/*sizeof(*/pt.SERIALIZED_LENGTH/*)*/);
			yaffs_packedtags1_C.yaffs_UnpackTags1(tags,pt);
			
		}

		return Guts_H.YAFFS_OK;
	}


	static boolean yramdisk_CheckChunkErased(yaffs_Device dev,int chunkInNAND)
	{
		int blk;
		int pg;
		int i;

		
		CheckInit(dev);
		
		blk = chunkInNAND/32;
		pg = chunkInNAND%32;
		
		
		for(i = 0; i < 528; i++)
		{
			if(ramdisk.block[blk].page[pg].data[i] != 0xFF)
			{
				return Guts_H.YAFFS_FAIL;
			}
		}

		return Guts_H.YAFFS_OK;

	}

	public boolean /*yramdisk_*/ eraseBlockInNAND(yaffs_Device dev, int blockNumber)
	{
		
		CheckInit(dev);
		
		if(blockNumber < 0 || blockNumber >= ramdisk.nBlocks)
		{
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,"Attempt to erase non-existant block %d\n",PrimitiveWrapperFactory.get(blockNumber));
			return Guts_H.YAFFS_FAIL;
		}
		else
		{
			yaffs2.utils.emulation.RamEmulationUnix.memset(ramdisk.block[blockNumber],(byte)0xFF/*,sizeof(yramdisk_Block*)*/);
			return Guts_H.YAFFS_OK;
		}
		
	}

	public boolean /*yramdisk_*/ initialiseNAND(yaffs_Device dev)
	{
		//dev->useNANDECC = 1; // force on useNANDECC which gets faked. 
							 // This saves us doing ECC checks.
		
		return Guts_H.YAFFS_OK;
	}



}
