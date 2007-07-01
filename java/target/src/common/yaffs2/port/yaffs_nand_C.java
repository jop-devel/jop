package yaffs2.port;

import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class yaffs_nand_C
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
	 
	static final String yaffs_nand_c_version =
	    "$Id: yaffs_nand_C.java,v 1.3 2007/07/01 01:29:51 alexander.dejaco Exp $";

	/*#include "yaffs_nand.h"
	#include "yaffs_tagscompat.h"
	#include "yaffs_tagsvalidity.h"*/


	static boolean yaffs_ReadChunkWithTagsFromNAND(yaffs_Device dev, int chunkInNAND,
						   byte[] buffer, int bufferIndex,
						   yaffs_ExtendedTags tags)
	{
		boolean result;
		yaffs_ExtendedTags localTags = new yaffs_ExtendedTags();
		
		int realignedChunkInNAND = chunkInNAND - dev.subField2.chunkOffset;
		
		/* If there are no tags provided, use local tags to get prioritised gc working */
		if(tags == null)
			tags = localTags;

		if (dev.subField1.readChunkWithTagsFromNAND != null)
			result = dev.subField1.readChunkWithTagsFromNAND.readChunkWithTagsFromNAND(dev, realignedChunkInNAND, buffer, bufferIndex,
							      tags);
		else
			result = yaffs_tagscompat_C.yaffs_TagsCompatabilityReadChunkWithTagsFromNAND(dev,
										realignedChunkInNAND,
										buffer, bufferIndex,
										tags);	
		if((tags != null) && 
		   tags.eccResult > Guts_H.YAFFS_ECC_RESULT_NO_ERROR){
		
			yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev, chunkInNAND/dev.subField1.nChunksPerBlock);
			yaffs_guts_C.yaffs_HandleChunkError(dev,bi);
		}
									
		return result;
	}

	static boolean yaffs_WriteChunkWithTagsToNAND(yaffs_Device dev,
							   int chunkInNAND,
							   byte[] buffer, int bufferIndex,
							   yaffs_ExtendedTags tags)
	{
		chunkInNAND -= dev.subField2.chunkOffset;

		
		if (tags != null) {
			tags.sequenceNumber = (int)dev.sequenceNumber;
			tags.chunkUsed = true;
			if (!yaffs_tagsvalidity_C.yaffs_ValidateTags(tags)) {
				yportenv.T(yportenv.YAFFS_TRACE_ERROR,
				  ("Writing uninitialised tags" + ydirectenv.TENDSTR));
				yaffs2.utils.Globals.portConfiguration.YBUG();
			}
			yportenv.T(yportenv.YAFFS_TRACE_WRITE,
			  ("Writing chunk %d tags %d %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND),
			  PrimitiveWrapperFactory.get(tags.objectId), PrimitiveWrapperFactory.get(tags.chunkId));
		} else {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR, ("Writing with no tags" + ydirectenv.TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		if (dev.subField1.writeChunkWithTagsToNAND != null)
			return dev.subField1.writeChunkWithTagsToNAND.writeChunkWithTagsToNAND(dev, chunkInNAND, buffer, bufferIndex,
							     tags);
		else
			return yaffs_tagscompat_C.yaffs_TagsCompatabilityWriteChunkWithTagsToNAND(dev,
									       chunkInNAND,
									       buffer, bufferIndex,
									       tags);
	}

	static boolean yaffs_MarkBlockBad(yaffs_Device dev, int blockNo)
	{
		blockNo -= dev.subField2.blockOffset;

	;
		if (dev.subField1.markNANDBlockBad != null)
			return dev.subField1.markNANDBlockBad.markNANDBlockBad(dev, blockNo);
		else
			return yaffs_tagscompat_C.yaffs_TagsCompatabilityMarkNANDBlockBad(dev, blockNo);
	}

	static boolean yaffs_QueryInitialBlockState(yaffs_Device dev,
							 int blockNo,
							 /*yaffs_BlockState*/ IntegerPointer state,
							 /*unsigned **/ IntegerPointer sequenceNumber)
	{
		blockNo -= dev.subField2.blockOffset;

		if (dev.subField1.queryNANDBlock != null)
			return dev.subField1.queryNANDBlock.queryNANDBlock(dev, blockNo, state, sequenceNumber);
		else
			return yaffs_tagscompat_C.yaffs_TagsCompatabilityQueryNANDBlock(dev, blockNo,
								     state,
								     sequenceNumber);
	}


	static boolean yaffs_EraseBlockInNAND(yaffs_Device dev,
					  int blockInNAND)
	{
		boolean result;

		blockInNAND -= dev.subField2.blockOffset;


		dev.subField3.nBlockErasures++;
		result = dev.subField1.eraseBlockInNAND.eraseBlockInNAND(dev, blockInNAND);

		return result;
	}

	static boolean yaffs_InitialiseNAND(yaffs_Device dev)
	{
		return dev.subField1.initialiseNAND.initialiseNAND(dev);
	}
}
