package yaffs2.port;

import static yaffs2.utils.Unix.*;
import static yaffs2.port.Guts_H.*;
import static yaffs2.port.yportenv.*;
import static yaffs2.port.ydirectenv.*;

public class yaffs_checkptrw_C
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

	static final String yaffs_checkptrw_c_version =
	    "$Id: yaffs_checkptrw_C.java,v 1.1 2007/06/07 14:37:29 peter.hilber Exp $";


//	#include "yaffs_checkptrw.h"


	static boolean yaffs_CheckpointSpaceOk(yaffs_Device dev)
	{

		int blocksAvailable = dev.nErasedBlocks - dev.nReservedBlocks;
		
		T(YAFFS_TRACE_CHECKPOINT,
			TSTR("checkpt blocks available = %d" + TENDSTR),
			blocksAvailable);
			
		
		return (blocksAvailable <= 0) ? false : true;
	}



	static boolean yaffs_CheckpointErase(yaffs_Device dev)
	{
		
		int i;
		

		if(!(dev.eraseBlockInNAND != null))	
			return false;
		T(YAFFS_TRACE_CHECKPOINT,TSTR("checking blocks %d to %d"+TENDSTR),
			dev.internalStartBlock,dev.internalEndBlock);
			
		for(i = dev.internalStartBlock; i <= dev.internalEndBlock; i++) {
			yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev,i);
			if(bi.blockState() == YAFFS_BLOCK_STATE_CHECKPOINT){
				T(YAFFS_TRACE_CHECKPOINT,TSTR("erasing checkpt block %d"+TENDSTR),i);
				if(dev.eraseBlockInNAND.eraseBlockInNAND(dev,i- dev.blockOffset /* realign */)){
					bi.setBlockState(YAFFS_BLOCK_STATE_EMPTY);
					dev.nErasedBlocks++;
					dev.nFreeChunks += dev.nChunksPerBlock;
				}
				else {
					dev.markNANDBlockBad.markNANDBlockBad(dev,i);
					bi.setBlockState(YAFFS_BLOCK_STATE_DEAD);
				}
			}
		}
		
		dev.blocksInCheckpoint = 0;
		
		return true;
	}


	static void yaffs_CheckpointFindNextErasedBlock(yaffs_Device dev)
	{
		int  i;
		int blocksAvailable = dev.nErasedBlocks - dev.nReservedBlocks;
		T(YAFFS_TRACE_CHECKPOINT,
			TSTR("allocating checkpt block: erased %d reserved %d avail %d next %d "+TENDSTR),
			dev.nErasedBlocks,dev.nReservedBlocks,blocksAvailable,dev.checkpointNextBlock);
			
		if(dev.checkpointNextBlock >= 0 &&
		   dev.checkpointNextBlock <= dev.internalEndBlock &&
		   blocksAvailable > 0){
		
			for(i = dev.checkpointNextBlock; i <= dev.internalEndBlock; i++){
				yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev,i);
				if(bi.blockState() == YAFFS_BLOCK_STATE_EMPTY){
					dev.checkpointNextBlock = i + 1;
					dev.checkpointCurrentBlock = i;
					T(YAFFS_TRACE_CHECKPOINT,TSTR("allocating checkpt block %d"+TENDSTR),i);
					return;
				}
			}
		}
		T(YAFFS_TRACE_CHECKPOINT,TSTR("out of checkpt blocks"+TENDSTR));
		
		dev.checkpointNextBlock = -1;
		dev.checkpointCurrentBlock = -1;
	}

	static void yaffs_CheckpointFindNextCheckpointBlock(yaffs_Device dev)
	{
		int  i;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		
		T(YAFFS_TRACE_CHECKPOINT,TSTR("find next checkpt block: start:  blocks %d next %d" + TENDSTR),
			dev.blocksInCheckpoint, dev.checkpointNextBlock);
			
		if(dev.blocksInCheckpoint < dev.checkpointMaxBlocks) 
			for(i = dev.checkpointNextBlock; i <= dev.internalEndBlock; i++){
				int chunk = i * dev.nChunksPerBlock;
				int realignedChunk = chunk - dev.chunkOffset;

				dev.readChunkWithTagsFromNAND.readChunkWithTagsFromNAND(dev,realignedChunk,null,0,tags);
				T(YAFFS_TRACE_CHECKPOINT,TSTR("find next checkpt block: search: block %d oid %d seq %d eccr %d" + TENDSTR), 
					i, tags.objectId,tags.sequenceNumber,tags.eccResult);
							      
				if(tags.sequenceNumber == YAFFS_SEQUENCE_CHECKPOINT_DATA){
					/* Right kind of block */
					dev.checkpointNextBlock = tags.objectId;
					dev.checkpointCurrentBlock = i;
					dev.checkpointBlockList[dev.blocksInCheckpoint] = i;
					dev.blocksInCheckpoint++;
					T(YAFFS_TRACE_CHECKPOINT,TSTR("found checkpt block %d"+TENDSTR),i);
					return;
				}
			}

		T(YAFFS_TRACE_CHECKPOINT,TSTR("found no more checkpt blocks"+TENDSTR));

		dev.checkpointNextBlock = -1;
		dev.checkpointCurrentBlock = -1;
	}


	static boolean yaffs_CheckpointOpen(yaffs_Device dev, boolean forWriting)
	{
		
		/* Got the functions we need? */
		if (!(dev.writeChunkWithTagsToNAND != null) ||
		    !(dev.readChunkWithTagsFromNAND != null) ||
		    !(dev.eraseBlockInNAND != null) ||
		    !(dev.markNANDBlockBad != null))
			return false;

		if(forWriting && !yaffs_CheckpointSpaceOk(dev))
			return false;
				
		if(!(dev.checkpointBuffer != null))
		{
			dev.checkpointBuffer = YMALLOC_DMA(dev.nDataBytesPerChunk);
			dev.checkpointBufferIndex = 0;
		}
		if(!(dev.checkpointBuffer != null))
			return false;

		
		dev.checkpointPageSequence = 0;
		
		dev.checkpointOpenForWrite = forWriting;
		
		dev.checkpointByteCount = 0;
		dev.checkpointCurrentBlock = -1;
		dev.checkpointCurrentChunk = -1;
		dev.checkpointNextBlock = dev.internalStartBlock;
		
		/* Erase all the blocks in the checkpoint area */
		if(forWriting){
			memset(dev.checkpointBuffer,dev.checkpointBufferIndex,(byte)0,dev.nDataBytesPerChunk);
			dev.checkpointByteOffset = 0;
			return yaffs_CheckpointErase(dev);
			
			
		} else {
			int i;
			/* Set to a value that will kick off a read */
			dev.checkpointByteOffset = dev.nDataBytesPerChunk;
			/* A checkpoint block list of 1 checkpoint block per 16 block is (hopefully)
			 * going to be way more than we need */
			dev.blocksInCheckpoint = 0;
			dev.checkpointMaxBlocks = (dev.internalEndBlock - dev.internalStartBlock)/16 + 2;
			dev.checkpointBlockList = YMALLOC_INT(/*sizeof(int) **/ dev.checkpointMaxBlocks);
			for(i = 0; i < dev.checkpointMaxBlocks; i++)
				dev.checkpointBlockList[i] = -1;
		}
		
		return true;
	}

	static boolean yaffs_CheckpointFlushBuffer(yaffs_Device dev)
	{

		int chunk;
		int realignedChunk;

		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		
		if(dev.checkpointCurrentBlock < 0){
			yaffs_CheckpointFindNextErasedBlock(dev);
			dev.checkpointCurrentChunk = 0;
		}
		
		if(dev.checkpointCurrentBlock < 0)
			return false;
		
		tags.chunkDeleted = false;
		tags.objectId = dev.checkpointNextBlock; /* Hint to next place to look */
		tags.chunkId = dev.checkpointPageSequence + 1;
		tags.sequenceNumber =  YAFFS_SEQUENCE_CHECKPOINT_DATA;
		tags.byteCount = dev.nDataBytesPerChunk;
		if(dev.checkpointCurrentChunk == 0){
			/* First chunk we write for the block? Set block state to
			   checkpoint */
			yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev,dev.checkpointCurrentBlock);
			bi.setBlockState(YAFFS_BLOCK_STATE_CHECKPOINT);
			dev.blocksInCheckpoint++;
		}
		
		chunk = dev.checkpointCurrentBlock * dev.nChunksPerBlock + dev.checkpointCurrentChunk;

		
		T(YAFFS_TRACE_CHECKPOINT,TSTR("checkpoint wite buffer nand %d(%d:%d) objid %d chId %d" + TENDSTR),
			chunk, dev.checkpointCurrentBlock, dev.checkpointCurrentChunk,tags.objectId,tags.chunkId); 
		
		realignedChunk = chunk - dev.chunkOffset;
		
		dev.writeChunkWithTagsToNAND.writeChunkWithTagsToNAND(dev,realignedChunk,dev.checkpointBuffer,
				dev.checkpointBufferIndex,tags);
		dev.checkpointByteOffset = 0;
		dev.checkpointPageSequence++;	   
		dev.checkpointCurrentChunk++;
		if(dev.checkpointCurrentChunk >= dev.nChunksPerBlock){
			dev.checkpointCurrentChunk = 0;
			dev.checkpointCurrentBlock = -1;
		}
		memset(dev.checkpointBuffer,dev.checkpointBufferIndex,(byte)0,
				dev.nDataBytesPerChunk);
		
		return true;
	}


	static int yaffs_CheckpointWrite(yaffs_Device dev,/**const void **/ byte[] data, int dataIndex, int nBytes)
	{
		int i=0;
		boolean ok = true;

		
		/**__u8 * */ byte[] dataBytes = /**(__u8 *)*/ (data);
		int dataBytesIndex = dataIndex;	
		

		
		if(!(dev.checkpointBuffer != null))
			return 0;

		while(i < nBytes && ok) {
			

			
			 dev.checkpointBuffer[dev.checkpointBufferIndex+dev.checkpointByteOffset] = dataBytes[dataBytesIndex];
			dev.checkpointByteOffset++;
			i++;
			dataBytesIndex++;
			dev.checkpointByteCount++;
			
			
			if(dev.checkpointByteOffset < 0 ||
			   dev.checkpointByteOffset >= dev.nDataBytesPerChunk) 
				ok = yaffs_CheckpointFlushBuffer(dev);

		}
		
		return 	i;
	}

	static int yaffs_CheckpointRead(yaffs_Device dev, /**void **/ byte[] data, int dataIndex, int nBytes)
	{
		int i=0;
		boolean ok = true;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();

		
		int chunk;
		int realignedChunk;

		/**__u8 **/ byte[] dataBytes = /**(__u8 *)*/ data;
		int dataBytesIndex = dataIndex;
			
		if(!(dev.checkpointBuffer != null))
			return 0;

		while(i < nBytes && ok) {
		
		
			if(dev.checkpointByteOffset < 0 ||
			   dev.checkpointByteOffset >= dev.nDataBytesPerChunk) {
			   
			   	if(dev.checkpointCurrentBlock < 0){
					yaffs_CheckpointFindNextCheckpointBlock(dev);
					dev.checkpointCurrentChunk = 0;
				}
				
				if(dev.checkpointCurrentBlock < 0)
					ok = false;
				else {
				
					chunk = dev.checkpointCurrentBlock * dev.nChunksPerBlock + 
					          dev.checkpointCurrentChunk;

					realignedChunk = chunk - dev.chunkOffset;

		   			/* read in the next chunk */
		   			/* printf("read checkpoint page %d\n",dev.checkpointPage); */
					dev.readChunkWithTagsFromNAND.readChunkWithTagsFromNAND(dev, realignedChunk, 
								       dev.checkpointBuffer, dev.checkpointBufferIndex,
								      tags);
							      
					if(tags.chunkId != (dev.checkpointPageSequence + 1) ||
					   tags.sequenceNumber != YAFFS_SEQUENCE_CHECKPOINT_DATA)
					   ok = false;

					dev.checkpointByteOffset = 0;
					dev.checkpointPageSequence++;
					dev.checkpointCurrentChunk++;
				
					if(dev.checkpointCurrentChunk >= dev.nChunksPerBlock)
						dev.checkpointCurrentBlock = -1;
				}
			}
			
			if(ok){
				dataBytes[dataBytesIndex] = dev.checkpointBuffer[dev.checkpointBufferIndex+dev.checkpointByteOffset];
				dev.checkpointByteOffset++;
				i++;
				dataBytesIndex++;
				dev.checkpointByteCount++;
			}
		}
		
		return 	i;
	}

	static boolean yaffs_CheckpointClose(yaffs_Device dev)
	{

		if(dev.checkpointOpenForWrite){	
			if(dev.checkpointByteOffset != 0)
				yaffs_CheckpointFlushBuffer(dev);
		} else {
			int i;
			for(i = 0; i < dev.blocksInCheckpoint && dev.checkpointBlockList[i] >= 0; i++){
				yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev,dev.checkpointBlockList[i]);
				if(bi.blockState() == YAFFS_BLOCK_STATE_EMPTY)
					bi.setBlockState(YAFFS_BLOCK_STATE_CHECKPOINT);
				else {
					// Todo this looks odd...
				}
			}
			YFREE(dev.checkpointBlockList);
			dev.checkpointBlockList = null;
		}

		dev.nFreeChunks -= dev.blocksInCheckpoint * dev.nChunksPerBlock;
		dev.nErasedBlocks -= dev.blocksInCheckpoint;

			
		T(YAFFS_TRACE_CHECKPOINT,TSTR("checkpoint byte count %d" + TENDSTR),
				dev.checkpointByteCount);
				
		if(dev.checkpointBuffer != null){
			/* free the buffer */	
			YFREE(dev.checkpointBuffer);
			dev.checkpointBuffer = null;
			return true;
		}
		else
			return false;
		
	}

	static boolean yaffs_CheckpointInvalidateStream(yaffs_Device dev)
	{
		/* Erase the first checksum block */

		T(YAFFS_TRACE_CHECKPOINT,(TSTR("checkpoint invalidate" + TENDSTR)));

		if(!yaffs_CheckpointSpaceOk(dev))
			return false;

		return yaffs_CheckpointErase(dev);
	}




}
