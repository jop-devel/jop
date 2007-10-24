package yaffs2.port.emulation;

import java.io.RandomAccessFile;

/*import yaffs2.port.yaffs_Device.eraseBlockInNANDInterface;
import yaffs2.port.yaffs_Device.initialiseNANDInterface;
import yaffs2.port.yaffs_Device.readChunkFromNANDInterface;
import yaffs2.port.yaffs_Device.writeChunkToNANDInterface;
import yaffs2.utils.Yaffs1NANDInterface;*/
import yaffs2.utils.factory.PrimitiveWrapperFactory;
import yaffs2.port.*;

import yaffs2.utils.*;
import yaffs2.utils.emulation.*;

public class port_fileem2k_C implements Yaffs1NANDInterface
{
	// PORT
	public static final port_fileem2k_C instance = new port_fileem2k_C(); 
	
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
	 * This provides a YAFFS nand emulation on a file for emulating 2kB pages.
	 * This is only intended as test code to test persistence etc.
	 */

	static final String yaffs_flashif_c_version = "$Id: port_fileem2k_C.java,v 1.2 2007/10/24 11:00:34 peter.hilber Exp $";


//	#include "yportenv.h"
//
//	#include "yaffs_flashif.h"
//	#include "yaffs_guts.h"
//	#include "devextras.h"
//
//	#include <sys/types.h>
//	#include <sys/stat.h>
//	#include <fcntl.h>
//	#include <unistd.h> 
//
//	#include "yaffs_fileem2k.h"
//	#include "yaffs_packedtags2.h"

	static final boolean SIMULATE_FAILURES = false;

//	typedef struct 
//	{
//		__u8 data[PAGE_SIZE]; // Data + spare
//	} yflash_Page;

//	typedef struct
//	{
//		yflash_Page page[PAGES_PER_BLOCK]; // The pages in the block
//		
//	} yflash_Block;



	static final int MAX_HANDLES = 20;
	static final int BLOCKS_PER_HANDLE = 64*16; // XXX why * 16?

//	typedef struct
//	{
//		int handle[MAX_HANDLES];
//		int nBlocks;
//	} yflash_Device;

	static yflash_Device filedisk = new yflash_Device();

	static boolean yaffs_testPartialWrite = false;




	static /*__u8*/ byte[] localBuffer = new byte[yaffs_fileem2k_H.PAGE_SIZE];
	static final int localBufferIndex = 0;

	static /*char * */ String NToName(/*char *buf,*/int n)
	{
		return "emfile" + n; 
	}

	static /*char*/ byte[] dummyBuffer = new byte[yaffs_fileem2k_H.BLOCK_SIZE];
	static final int dummyBufferSize = dummyBuffer.length;
	static final int dummyBufferIndex = 0;

	static RandomAccessFile GetBlockFileHandle(int n)
	{
		RandomAccessFile h;
		int requiredSize;
		
		/*char*/ String name = NToName(n);
		int fSize;
		int i;
		
		h =  FileEmulationUnix.open(name, yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		if(h != null){
			// FIXME rewriting every program launch
//			fSize = lseek(h,0,SEEK_END);
//			requiredSize = BLOCKS_PER_HANDLE * BLOCK_SIZE;
//			if(fSize < requiredSize){
			   for(i = 0; i < BLOCKS_PER_HANDLE; i++)
			   	if(FileEmulationUnix.write(h,dummyBuffer,dummyBufferIndex,yaffs_fileem2k_H.BLOCK_SIZE) != yaffs_fileem2k_H.BLOCK_SIZE)
					return null;
				
//			}
		}
		
		return h;

	}

	static boolean _STATIC_LOCAL_CheckInit_initialised = false;
	
	static boolean CheckInit()
	{
//		static boolean initialised = false;
		int h;
		int i;

		
		/*off_t*/ int fSize;
		/*off_t*/ int requiredSize;
		int written;
		int blk;
		
		yflash_Page p = new yflash_Page();
		
		if(_STATIC_LOCAL_CheckInit_initialised) 
		{
			return Guts_H.YAFFS_OK;
		}

		_STATIC_LOCAL_CheckInit_initialised = true;
		
		Unix.memset(dummyBuffer,dummyBufferIndex,(byte)0xff,dummyBufferSize);
		
		
		filedisk.nBlocks = yaffs_fileem2k_H.SIZE_IN_MB * yaffs_fileem2k_H.BLOCKS_PER_MB;

		for(i = 0; i <  MAX_HANDLES; i++)
			filedisk.handle[i] = null;
		
		for(i = 0,blk = 0; blk < filedisk.nBlocks; blk+=BLOCKS_PER_HANDLE,i++)
			filedisk.handle[i] = GetBlockFileHandle(i);
		
		
		return true;
	}


	static int yflash_GetNumberOfBlocks()
	{
		CheckInit();
		
		return filedisk.nBlocks;
	}

	public boolean writeChunkToNAND(yaffs_Device dev, int chunkInNAND, 
			byte[] data, int dataIndex, yaffs_Spare spare)
//	public boolean /*yflash_*/writeChunkWithTagsToNAND(yaffs_Device dev,int chunkInNAND,
//			/*const __u8 **/ byte[] data, int dataIndex, yaffs_ExtendedTags tags)
	{
		int written;
		int pos;
		RandomAccessFile h;
		int i;
		int nRead;
		int error;

		
		yportenv.T(yportenv.YAFFS_TRACE_MTD,("write chunk %d data %x tags %x"+ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(chunkInNAND),/*(unsigned)*/PrimitiveWrapperFactory.get(yaffs2.utils.Utils.hashCode(data)), /*(unsigned)*/PrimitiveWrapperFactory.get(yaffs2.utils.Utils.hashCode(spare)));

		CheckInit();
		
		
		
		if(data != null)
		{
			pos = (chunkInNAND % (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE)) * yaffs_fileem2k_H.PAGE_SIZE;
			h = filedisk.handle[(chunkInNAND / (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE))];
			
			FileEmulationUnix.lseek(h,pos,yaffsfs_H.SEEK_SET);
			nRead =  FileEmulationUnix.read(h, localBuffer,localBufferIndex, dev.subField1.nDataBytesPerChunk);
			for(i = error = 0; i < dev.subField1.nDataBytesPerChunk && !(error != 0); i++){
				if(yaffs2.utils.Utils.byteAsUnsignedByte(localBuffer[localBufferIndex+i]) != 0xFF){
					//FIXME
					yportenv.T(yportenv.PORT_TRACE_NANDSIM, "nand simulation: chunk %d data byte %d was %02x\n",
							PrimitiveWrapperFactory.get(chunkInNAND),PrimitiveWrapperFactory.get(i),PrimitiveWrapperFactory.get(Utils.byteAsUnsignedByte(localBuffer[localBufferIndex+i])));

//					printf("nand simulation: chunk %d data byte %d was %02x\n", // PORT I think %0x2 is a typo.
//						chunkInNAND,i,byteAsUnsignedByte(localBuffer[localBufferIndex+i]));
					
					error = 1;
				}
			}
			
			for(i = 0; i < dev.subField1.nDataBytesPerChunk; i++)
			  localBuffer[localBufferIndex+i] &= data[dataIndex+i];
			  
			if(Unix.memcmp(localBuffer,localBufferIndex,data,dataIndex,dev.subField1.nDataBytesPerChunk) != 0)
				Unix.printf("nand simulator: data does not match\n");
				
			FileEmulationUnix.lseek(h,pos,yaffsfs_H.SEEK_SET);
			written = FileEmulationUnix.write(h,localBuffer,localBufferIndex,dev.subField1.nDataBytesPerChunk);
			
			// TODO FIXME
//			if ((yportenv.PORT_TRACE_CHECKSUMS & yaffs2.utils.Globals.yaffs_traceMask) != 0)
//				CheckSum.checksumOfBytes(localBuffer,localBufferIndex,dev.subField1.nDataBytesPerChunk);
				
			if(yaffs_testPartialWrite){
				FileEmulationUnix.close(h);
				System.exit(1);
			}
			
	if (SIMULATE_FAILURES)
	{
				if((chunkInNAND >> 6) == 100) 
				  written = 0;

				if((chunkInNAND >> 6) == 110) 
				  written = 0;
	}
//	#endif


			if(written != dev.subField1.nDataBytesPerChunk) return Guts_H.YAFFS_FAIL;
		}
		
		if(spare != null)
		{
			pos = (chunkInNAND % (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE)) * yaffs_fileem2k_H.PAGE_SIZE + yaffs_fileem2k_H.PAGE_DATA_SIZE ;
			h = filedisk.handle[(chunkInNAND / (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE))];
			
			FileEmulationUnix.lseek(h,pos,yaffsfs_H.SEEK_SET);

//			if( false && dev.isYaffs2)
//			{
//				
//				byte[] tagsBuf = new byte[yaffs_ExtendedTags.SERIALIZED_LENGTH];
//				tags.writeTagsToByteArray(tagsBuf, 0);
//				written = write(h,tagsBuf,0,yaffs_ExtendedTags.SERIALIZED_LENGTH);
//				if(written != yaffs_ExtendedTags.SERIALIZED_LENGTH) return YAFFS_FAIL;
//			}
//			else
			{
//				yaffs_PackedTags2 pt = new yaffs_PackedTags2();
//				yaffs_PackTags2(pt,tags);
//				/*__u8 **/ byte[] ptab = /*(__u8 *)&*/ pt.serialized;
//				final int ptabIndex = pt.offset; 

				nRead = FileEmulationUnix.read(h,localBuffer,localBufferIndex,spare.SERIALIZED_LENGTH);
				for(i = error = 0; i < spare.SERIALIZED_LENGTH && !(error != 0); i++){
					if(yaffs2.utils.Utils.byteAsUnsignedByte(localBuffer[i]) != 0xFF){
						
//						FIXME
						yportenv.T(yportenv.PORT_TRACE_NANDSIM, "nand simulation: chunk %d oob byte %d was %02x\n",
								PrimitiveWrapperFactory.get(chunkInNAND),PrimitiveWrapperFactory.get(i),PrimitiveWrapperFactory.get(Utils.byteAsUnsignedByte(localBuffer[i])));					

//						printf("nand simulation: chunk %d oob byte %d was %02x\n", // PORT I think %0x2 is a typo.
//							chunkInNAND,i,byteAsUnsignedByte(localBuffer[i]));
							error = 1;
					}
				}
			
				for(i = 0; i < spare.SERIALIZED_LENGTH; i++)
				  localBuffer[i] &= spare.serialized[spare.offset+i];
				 
				if(Unix.memcmp(localBuffer,localBufferIndex,spare.serialized,spare.offset,
						spare.SERIALIZED_LENGTH) != 0)
//					FIXME					

					yportenv.T(yportenv.PORT_TRACE_NANDSIM, "nand sim: tags corruption\n", null, null, null);			
//					printf("nand sim: tags corruption\n");
					
				FileEmulationUnix.lseek(h,pos,yaffsfs_H.SEEK_SET);
				
				written = FileEmulationUnix.write(h,localBuffer,localBufferIndex,spare.SERIALIZED_LENGTH);
				if(written != spare.SERIALIZED_LENGTH) return Guts_H.YAFFS_FAIL;
			}
		}
		

		return Guts_H.YAFFS_OK;	

	}

	static boolean yaffs_CheckAllFF(/*const __u8 **/ byte[] ptr, int ptrIndex, int n)
	{
		while(n != 0)
		{
			n--;
			if(yaffs2.utils.Utils.byteAsUnsignedByte(ptr[ptrIndex])!=0xFF) return false;
			ptrIndex++;
		}
		return true;
	}


	static int fail300 = 1;
	static int fail320 = 1;

	static int failRead10 = 2;

	public boolean readChunkFromNAND(yaffs_Device dev, int chunkInNAND, byte[] data, int dataIndex, yaffs_Spare spare)
	{
		int nread;
		int pos;
		RandomAccessFile h;
		
//		yportenv.T(yportenv.YAFFS_TRACE_MTD,("read chunk %d data %x tags %x"+ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(chunkInNAND),/*(unsigned)*/PrimitiveWrapperFactory.get(Utils.hashCode(data)), /*(unsigned)*/PrimitiveWrapperFactory.get(Utils.hashCode(spare)));
		
		CheckInit();
		
		
		
		if(data != null)
		{

			pos = (chunkInNAND % (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE)) * yaffs_fileem2k_H.PAGE_SIZE;
			h = filedisk.handle[(chunkInNAND / (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE))];		
			FileEmulationUnix.lseek(h,pos,yaffsfs_H.SEEK_SET);
			nread = FileEmulationUnix.read(h,data,dataIndex,dev.subField1.nDataBytesPerChunk);
			
			
			if(nread != dev.subField1.nDataBytesPerChunk) return Guts_H.YAFFS_FAIL;
		}
		
		if(spare != null)
		{
			pos = (chunkInNAND % (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE)) * yaffs_fileem2k_H.PAGE_SIZE + yaffs_fileem2k_H.PAGE_DATA_SIZE;
			h = filedisk.handle[(chunkInNAND / (yaffs_fileem2k_H.PAGES_PER_BLOCK * BLOCKS_PER_HANDLE))];		
			FileEmulationUnix.lseek(h,pos,yaffsfs_H.SEEK_SET);

//			if(false && dev.isYaffs2)
//			{
//				byte[] tagsBuf = new byte[yaffs_ExtendedTags.SERIALIZED_LENGTH];
//				
//				nread= read(h,tagsBuf,0,yaffs_ExtendedTags.SERIALIZED_LENGTH);
//				if(nread != yaffs_ExtendedTags.SERIALIZED_LENGTH) return YAFFS_FAIL;
//				if(yaffs_CheckAllFF(/*(__u8 *)*/tagsBuf,0,yaffs_ExtendedTags.SERIALIZED_LENGTH))
//				{
//					tags.readTagsFromByteArray(tagsBuf, 0);
//					yaffs2.port.yaffs_tagsvalidity_C.yaffs_InitialiseTags(tags);
//				}
//				else
//				{
//					tags.readTagsFromByteArray(tagsBuf, 0);
//					tags.chunkUsed = true;
//				}
//			}
//			else
			{
				nread= FileEmulationUnix.read(h,spare.serialized,spare.offset,spare.SERIALIZED_LENGTH);

				// XXX simulate failures
//	if (SIMULATE_FAILURES)
//	{
//				if((chunkInNAND >> 6) == 100) {
//				    if(fail300 != 0 && tags.eccResult == YAFFS_ECC_RESULT_NO_ERROR){
//				       tags.eccResult = YAFFS_ECC_RESULT_FIXED;
//				       fail300 = 0;
//				    }
//				    
//				}
//				if((chunkInNAND >> 6) == 110) {
//				    if(fail320 != 0 && tags.eccResult == YAFFS_ECC_RESULT_NO_ERROR){
//				       tags.eccResult = YAFFS_ECC_RESULT_FIXED;
//				       fail320 = 0;
//				    }
//				}
//	}
////	#endif
				if(failRead10>0 && chunkInNAND == 10){
					failRead10--;
					nread = 0;
				}
				
				if(nread != spare.SERIALIZED_LENGTH) return Guts_H.YAFFS_FAIL;
			}
		}
		

		return Guts_H.YAFFS_OK;	

	}


	// PORT not needed for spare style interface 
//	public boolean /*yflash_*/ markNANDBlockBad(yaffs_Device dev, int blockNo)
//	{
//		int written;
//		RandomAccessFile h;
//		
//		yaffs_PackedTags2 pt = new yaffs_PackedTags2();
//
//		CheckInit();
//		
//		memset(pt,(byte)0);
//		h = filedisk.handle[(blockNo / ( BLOCKS_PER_HANDLE))];
//		lseek(h,((blockNo % BLOCKS_PER_HANDLE) * dev.nChunksPerBlock) * PAGE_SIZE + PAGE_DATA_SIZE,SEEK_SET);
//		written = write(h,pt.serialized,pt.offset,pt.SERIALIZED_LENGTH);
//			
//		if(written != pt.SERIALIZED_LENGTH) return YAFFS_FAIL;
//		
//		
//		return YAFFS_OK;
//		
//	}

	public boolean /*yflash_*/ eraseBlockInNAND(yaffs_Device dev, int blockNumber)
	{

		int i;
		RandomAccessFile h;
			
		CheckInit();
		

//		yportenv.T(yportenv.YAFFS_TRACE_ERASE,"erase block %d\n",PrimitiveWrapperFactory.get(blockNumber));

//		printf("erase block %d\n",blockNumber);
		
		if(blockNumber == 320)
			fail320 = 1;
		
		if(blockNumber < 0 || blockNumber >= filedisk.nBlocks)
		{
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,"Attempt to erase non-existant block %d\n",PrimitiveWrapperFactory.get(blockNumber));
			return Guts_H.YAFFS_FAIL;
		}
		else
		{
		
			/*__u8*/ byte[] pg = new byte[yaffs_fileem2k_H.PAGE_SIZE];
			final int pgIndex = 0;
			int syz = yaffs_fileem2k_H.PAGE_SIZE;
			int pos;
			
			Unix.memset(pg,pgIndex,(byte)0xff,syz);
			

			h = filedisk.handle[(blockNumber / ( BLOCKS_PER_HANDLE))];
			FileEmulationUnix.lseek(h,((blockNumber % BLOCKS_PER_HANDLE) * dev.subField1.nChunksPerBlock) * yaffs_fileem2k_H.PAGE_SIZE,yaffsfs_H.SEEK_SET);		
			for(i = 0; i < dev.subField1.nChunksPerBlock; i++)
			{
				FileEmulationUnix.write(h,pg,pgIndex,yaffs_fileem2k_H.PAGE_SIZE);
			}
			pos = FileEmulationUnix.lseek(h, 0,yaffsfs_H.SEEK_CUR);
			
			return Guts_H.YAFFS_OK;
		}
		
	}

	public boolean /*yflash_*/ initialiseNAND(yaffs_Device dev)
	{
		CheckInit();
		
		return Guts_H.YAFFS_OK;
	}




	// PORT not needed for spare style interface
//	public boolean /*yflash_*/ queryNANDBlock(yaffs_Device dev, int blockNo, /*yaffs_BlockState*/ IntegerPointer state, IntegerPointer sequenceNumber)
//	{
//		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
//		int chunkNo;
//
//		sequenceNumber.dereferenced = 0;
//		
//		chunkNo = blockNo * dev.nChunksPerBlock;
//		
//		/*yflash_*/ readChunkFromNAND(dev,chunkNo,null,0,tags);
//		if(tags.blockBad)
//		{
//			state.dereferenced = YAFFS_BLOCK_STATE_DEAD;
//		}
//		else if(!tags.chunkUsed)
//		{
//			state.dereferenced = YAFFS_BLOCK_STATE_EMPTY;
//		}
//		else if(tags.chunkUsed)
//		{
//			state.dereferenced = YAFFS_BLOCK_STATE_NEEDS_SCANNING;
//			sequenceNumber.dereferenced = tags.sequenceNumber;
//		}
//		return YAFFS_OK;
//	}
}
