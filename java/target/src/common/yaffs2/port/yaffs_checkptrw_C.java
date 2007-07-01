package yaffs2.port;

import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;


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
	    "$Id: yaffs_checkptrw_C.java,v 1.3 2007/07/01 01:29:50 alexander.dejaco Exp $";


//	#include "yaffs_checkptrw.h"


	static boolean yaffs_CheckpointSpaceOk(yaffs_Device dev)
	{

		int blocksAvailable = dev.subField3.nErasedBlocks - dev.subField1.nReservedBlocks;
		
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,
			("checkpt blocks available = %d" + ydirectenv.TENDSTR),
			PrimitiveWrapperFactory.get(blocksAvailable));
			
		
		return (blocksAvailable <= 0) ? false : true;
	}



	static boolean yaffs_CheckpointErase(yaffs_Device dev)
	{
		
		int i;
		

		if(!(dev.subField1.eraseBlockInNAND != null))	
			return false;
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("checking blocks %d to %d"+ydirectenv.TENDSTR),
				PrimitiveWrapperFactory.get(dev.subField2.internalStartBlock),PrimitiveWrapperFactory.get(dev.subField2.internalEndBlock));
			
		for(i = dev.subField2.internalStartBlock; i <= dev.subField2.internalEndBlock; i++) {
			yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev,i);
			if(bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_CHECKPOINT){
				yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("erasing checkpt block %d"+ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(i));
				if(dev.subField1.eraseBlockInNAND.eraseBlockInNAND(dev,i- dev.subField2.blockOffset /* realign */)){
					bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_EMPTY);
					dev.subField3.nErasedBlocks++;
					dev.subField3.nFreeChunks += dev.subField1.nChunksPerBlock;
				}
				else {
					dev.subField1.markNANDBlockBad.markNANDBlockBad(dev,i);
					bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_DEAD);
				}
			}
		}
		
		dev.subField2.blocksInCheckpoint = 0;
		
		return true;
	}


	static void yaffs_CheckpointFindNextErasedBlock(yaffs_Device dev)
	{
		int  i;
		int blocksAvailable = dev.subField3.nErasedBlocks - dev.subField1.nReservedBlocks;
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,
			("allocating checkpt block: erased %d reserved %d avail %d next %d "+ydirectenv.TENDSTR),
			PrimitiveWrapperFactory.get(dev.subField3.nErasedBlocks),PrimitiveWrapperFactory.get(dev.subField1.nReservedBlocks),PrimitiveWrapperFactory.get(blocksAvailable),PrimitiveWrapperFactory.get(dev.subField2.checkpointNextBlock));
			
		if(dev.subField2.checkpointNextBlock >= 0 &&
		   dev.subField2.checkpointNextBlock <= dev.subField2.internalEndBlock &&
		   blocksAvailable > 0){
		
			for(i = dev.subField2.checkpointNextBlock; i <= dev.subField2.internalEndBlock; i++){
				yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev,i);
				if(bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_EMPTY){
					dev.subField2.checkpointNextBlock = i + 1;
					dev.subField2.checkpointCurrentBlock = i;
					yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("allocating checkpt block %d"+ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(i));
					return;
				}
			}
		}
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("out of checkpt blocks"+ydirectenv.TENDSTR));
		
		dev.subField2.checkpointNextBlock = -1;
		dev.subField2.checkpointCurrentBlock = -1;
	}

	static void yaffs_CheckpointFindNextCheckpointBlock(yaffs_Device dev)
	{
		int  i;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("find next checkpt block: start:  blocks %d next %d" + ydirectenv.TENDSTR),
				PrimitiveWrapperFactory.get(dev.subField2.blocksInCheckpoint), PrimitiveWrapperFactory.get(dev.subField2.checkpointNextBlock));
			
		if(dev.subField2.blocksInCheckpoint < dev.subField2.checkpointMaxBlocks) 
			for(i = dev.subField2.checkpointNextBlock; i <= dev.subField2.internalEndBlock; i++){
				int chunk = i * dev.subField1.nChunksPerBlock;
				int realignedChunk = chunk - dev.subField2.chunkOffset;

				dev.subField1.readChunkWithTagsFromNAND.readChunkWithTagsFromNAND(dev,realignedChunk,null,0,tags);
				yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("find next checkpt block: search: block %d oid %d seq %d eccr %d" + ydirectenv.TENDSTR), 
						PrimitiveWrapperFactory.get(i), PrimitiveWrapperFactory.get(tags.objectId),PrimitiveWrapperFactory.get(tags.sequenceNumber),PrimitiveWrapperFactory.get(tags.eccResult));
							      
				if(tags.sequenceNumber == Guts_H.YAFFS_SEQUENCE_CHECKPOINT_DATA){
					/* Right kind of block */
					dev.subField2.checkpointNextBlock = tags.objectId;
					dev.subField2.checkpointCurrentBlock = i;
					dev.subField2.checkpointBlockList[dev.subField2.blocksInCheckpoint] = i;
					dev.subField2.blocksInCheckpoint++;
					yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("found checkpt block %d"+ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(i));
					return;
				}
			}

		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("found no more checkpt blocks"+ydirectenv.TENDSTR));

		dev.subField2.checkpointNextBlock = -1;
		dev.subField2.checkpointCurrentBlock = -1;
	}


	static boolean yaffs_CheckpointOpen(yaffs_Device dev, boolean forWriting)
	{
		
		/* Got the functions we need? */
		if (!(dev.subField1.writeChunkWithTagsToNAND != null) ||
		    !(dev.subField1.readChunkWithTagsFromNAND != null) ||
		    !(dev.subField1.eraseBlockInNAND != null) ||
		    !(dev.subField1.markNANDBlockBad != null))
			return false;

		if(forWriting && !yaffs_CheckpointSpaceOk(dev))
			return false;
				
		if(!(dev.subField2.checkpointBuffer != null))
		{
			dev.subField2.checkpointBuffer = ydirectenv.YMALLOC_DMA(dev.subField1.nDataBytesPerChunk);
			dev.subField2.checkpointBufferIndex = 0;
		}
		if(!(dev.subField2.checkpointBuffer != null))
			return false;

		
		dev.subField2.checkpointPageSequence = 0;
		
		dev.subField2.checkpointOpenForWrite = forWriting;
		
		dev.subField2.checkpointByteCount = 0;
		dev.subField2.checkpointCurrentBlock = -1;
		dev.subField2.checkpointCurrentChunk = -1;
		dev.subField2.checkpointNextBlock = dev.subField2.internalStartBlock;
		
		/* Erase all the blocks in the checkpoint area */
		if(forWriting){
			Unix.memset(dev.subField2.checkpointBuffer,dev.subField2.checkpointBufferIndex,(byte)0,dev.subField1.nDataBytesPerChunk);
			dev.subField2.checkpointByteOffset = 0;
			return yaffs_CheckpointErase(dev);
			
			
		} else {
			int i;
			/* Set to a value that will kick off a read */
			dev.subField2.checkpointByteOffset = dev.subField1.nDataBytesPerChunk;
			/* A checkpoint block list of 1 checkpoint block per 16 block is (hopefully)
			 * going to be way more than we need */
			dev.subField2.blocksInCheckpoint = 0;
			dev.subField2.checkpointMaxBlocks = (dev.subField2.internalEndBlock - dev.subField2.internalStartBlock)/16 + 2;
			dev.subField2.checkpointBlockList = ydirectenv.YMALLOC_INT(/*sizeof(int) **/ dev.subField2.checkpointMaxBlocks);
			for(i = 0; i < dev.subField2.checkpointMaxBlocks; i++)
				dev.subField2.checkpointBlockList[i] = -1;
		}
		
		return true;
	}

	static boolean yaffs_CheckpointFlushBuffer(yaffs_Device dev)
	{

		int chunk;
		int realignedChunk;

		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		
		if(dev.subField2.checkpointCurrentBlock < 0){
			yaffs_CheckpointFindNextErasedBlock(dev);
			dev.subField2.checkpointCurrentChunk = 0;
		}
		
		if(dev.subField2.checkpointCurrentBlock < 0)
			return false;
		
		tags.chunkDeleted = false;
		tags.objectId = dev.subField2.checkpointNextBlock; /* Hint to next place to look */
		tags.chunkId = dev.subField2.checkpointPageSequence + 1;
		tags.sequenceNumber =  Guts_H.YAFFS_SEQUENCE_CHECKPOINT_DATA;
		tags.byteCount = dev.subField1.nDataBytesPerChunk;
		if(dev.subField2.checkpointCurrentChunk == 0){
			/* First chunk we write for the block? Set block state to
			   checkpoint */
			yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev,dev.subField2.checkpointCurrentBlock);
			bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_CHECKPOINT);
			dev.subField2.blocksInCheckpoint++;
		}
		
		chunk = dev.subField2.checkpointCurrentBlock * dev.subField1.nChunksPerBlock + dev.subField2.checkpointCurrentChunk;

		
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("checkpoint wite buffer nand %d(%d:%d) objid %d chId %d" + ydirectenv.TENDSTR),
				PrimitiveWrapperFactory.get(chunk), PrimitiveWrapperFactory.get(dev.subField2.checkpointCurrentBlock), PrimitiveWrapperFactory.get(dev.subField2.checkpointCurrentChunk),PrimitiveWrapperFactory.get(tags.objectId),PrimitiveWrapperFactory.get(tags.chunkId),null,null,null,null); 
		
		realignedChunk = chunk - dev.subField2.chunkOffset;
		
		dev.subField1.writeChunkWithTagsToNAND.writeChunkWithTagsToNAND(dev,realignedChunk,dev.subField2.checkpointBuffer,
				dev.subField2.checkpointBufferIndex,tags);
		dev.subField2.checkpointByteOffset = 0;
		dev.subField2.checkpointPageSequence++;	   
		dev.subField2.checkpointCurrentChunk++;
		if(dev.subField2.checkpointCurrentChunk >= dev.subField1.nChunksPerBlock){
			dev.subField2.checkpointCurrentChunk = 0;
			dev.subField2.checkpointCurrentBlock = -1;
		}
		Unix.memset(dev.subField2.checkpointBuffer,dev.subField2.checkpointBufferIndex,(byte)0,
				dev.subField1.nDataBytesPerChunk);
		
		return true;
	}


	static int yaffs_CheckpointWrite(yaffs_Device dev,/**const void **/ byte[] data, int dataIndex, int nBytes)
	{
		int i=0;
		boolean ok = true;

		
		/**__u8 * */ byte[] dataBytes = /**(__u8 *)*/ (data);
		int dataBytesIndex = dataIndex;	
		

		
		if(!(dev.subField2.checkpointBuffer != null))
			return 0;

		while(i < nBytes && ok) {
			

			
			 dev.subField2.checkpointBuffer[dev.subField2.checkpointBufferIndex+dev.subField2.checkpointByteOffset] = dataBytes[dataBytesIndex];
			dev.subField2.checkpointByteOffset++;
			i++;
			dataBytesIndex++;
			dev.subField2.checkpointByteCount++;
			
			
			if(dev.subField2.checkpointByteOffset < 0 ||
			   dev.subField2.checkpointByteOffset >= dev.subField1.nDataBytesPerChunk) 
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
			
		if(!(dev.subField2.checkpointBuffer != null))
			return 0;

		while(i < nBytes && ok) {
		
		
			if(dev.subField2.checkpointByteOffset < 0 ||
			   dev.subField2.checkpointByteOffset >= dev.subField1.nDataBytesPerChunk) {
			   
			   	if(dev.subField2.checkpointCurrentBlock < 0){
					yaffs_CheckpointFindNextCheckpointBlock(dev);
					dev.subField2.checkpointCurrentChunk = 0;
				}
				
				if(dev.subField2.checkpointCurrentBlock < 0)
					ok = false;
				else {
				
					chunk = dev.subField2.checkpointCurrentBlock * dev.subField1.nChunksPerBlock + 
					          dev.subField2.checkpointCurrentChunk;

					realignedChunk = chunk - dev.subField2.chunkOffset;

		   			/* read in the next chunk */
		   			/* printf("read checkpoint page %d\n",dev.checkpointPage); */
					dev.subField1.readChunkWithTagsFromNAND.readChunkWithTagsFromNAND(dev, realignedChunk, 
								       dev.subField2.checkpointBuffer, dev.subField2.checkpointBufferIndex,
								      tags);
							      
					if(tags.chunkId != (dev.subField2.checkpointPageSequence + 1) ||
					   tags.sequenceNumber != Guts_H.YAFFS_SEQUENCE_CHECKPOINT_DATA)
					   ok = false;

					dev.subField2.checkpointByteOffset = 0;
					dev.subField2.checkpointPageSequence++;
					dev.subField2.checkpointCurrentChunk++;
				
					if(dev.subField2.checkpointCurrentChunk >= dev.subField1.nChunksPerBlock)
						dev.subField2.checkpointCurrentBlock = -1;
				}
			}
			
			if(ok){
				dataBytes[dataBytesIndex] = dev.subField2.checkpointBuffer[dev.subField2.checkpointBufferIndex+dev.subField2.checkpointByteOffset];
				dev.subField2.checkpointByteOffset++;
				i++;
				dataBytesIndex++;
				dev.subField2.checkpointByteCount++;
			}
		}
		
		return 	i;
	}

	static boolean yaffs_CheckpointClose(yaffs_Device dev)
	{

		if(dev.subField2.checkpointOpenForWrite){	
			if(dev.subField2.checkpointByteOffset != 0)
				yaffs_CheckpointFlushBuffer(dev);
		} else {
			int i;
			for(i = 0; i < dev.subField2.blocksInCheckpoint && dev.subField2.checkpointBlockList[i] >= 0; i++){
				yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev,dev.subField2.checkpointBlockList[i]);
				if(bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_EMPTY)
					bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_CHECKPOINT);
				else {
					// Todo this looks odd...
				}
			}
			ydirectenv.YFREE(dev.subField2.checkpointBlockList);
			dev.subField2.checkpointBlockList = null;
		}

		dev.subField3.nFreeChunks -= dev.subField2.blocksInCheckpoint * dev.subField1.nChunksPerBlock;
		dev.subField3.nErasedBlocks -= dev.subField2.blocksInCheckpoint;

			
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("checkpoint byte count %d" + ydirectenv.TENDSTR),
				PrimitiveWrapperFactory.get(dev.subField2.checkpointByteCount));
				
		if(dev.subField2.checkpointBuffer != null){
			/* free the buffer */	
			ydirectenv.YFREE(dev.subField2.checkpointBuffer);
			dev.subField2.checkpointBuffer = null;
			return true;
		}
		else
			return false;
		
	}

	static boolean yaffs_CheckpointInvalidateStream(yaffs_Device dev)
	{
		/* Erase the first checksum block */

		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,(("checkpoint invalidate" + ydirectenv.TENDSTR)));

		if(!yaffs_CheckpointSpaceOk(dev))
			return false;

		return yaffs_CheckpointErase(dev);
	}




}
