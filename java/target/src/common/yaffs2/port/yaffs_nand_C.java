package yaffs2.port;

import yaffs2.utils.*;

import static yaffs2.port.Guts_H.*;
import static yaffs2.port.yportenv.*;
import static yaffs2.port.ydirectenv.*;
import static yaffs2.port.yaffs_guts_C.*;
import static yaffs2.port.yaffs_tagsvalidity_C.*;
import static yaffs2.port.yaffs_tagscompat_C.*;

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
	    "$Id: yaffs_nand_C.java,v 1.1 2007/06/07 14:37:29 peter.hilber Exp $";

	/*#include "yaffs_nand.h"
	#include "yaffs_tagscompat.h"
	#include "yaffs_tagsvalidity.h"*/


	static boolean yaffs_ReadChunkWithTagsFromNAND(yaffs_Device dev, int chunkInNAND,
						   byte[] buffer, int bufferIndex,
						   yaffs_ExtendedTags tags)
	{
		boolean result;
		yaffs_ExtendedTags localTags = new yaffs_ExtendedTags();
		
		int realignedChunkInNAND = chunkInNAND - dev.chunkOffset;
		
		/* If there are no tags provided, use local tags to get prioritised gc working */
		if(tags == null)
			tags = localTags;

		if (dev.readChunkWithTagsFromNAND != null)
			result = dev.readChunkWithTagsFromNAND.readChunkWithTagsFromNAND(dev, realignedChunkInNAND, buffer, bufferIndex,
							      tags);
		else
			result = yaffs_TagsCompatabilityReadChunkWithTagsFromNAND(dev,
										realignedChunkInNAND,
										buffer, bufferIndex,
										tags);	
		if((tags != null) && 
		   tags.eccResult > YAFFS_ECC_RESULT_NO_ERROR){
		
			yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev, chunkInNAND/dev.nChunksPerBlock);
	                yaffs_HandleChunkError(dev,bi);
		}
									
		return result;
	}

	static boolean yaffs_WriteChunkWithTagsToNAND(yaffs_Device dev,
							   int chunkInNAND,
							   byte[] buffer, int bufferIndex,
							   yaffs_ExtendedTags tags)
	{
		chunkInNAND -= dev.chunkOffset;

		
		if (tags != null) {
			tags.sequenceNumber = (int)dev.sequenceNumber;
			tags.chunkUsed = true;
			if (!yaffs_ValidateTags(tags)) {
				T(YAFFS_TRACE_ERROR,
				  TSTR("Writing uninitialised tags" + TENDSTR));
				yaffs2.utils.Globals.portConfiguration.YBUG();
			}
			T(YAFFS_TRACE_WRITE,
			  TSTR("Writing chunk %d tags %d %d" + TENDSTR), chunkInNAND,
			   tags.objectId, tags.chunkId);
		} else {
			T(YAFFS_TRACE_ERROR, TSTR("Writing with no tags" + TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		if (dev.writeChunkWithTagsToNAND != null)
			return dev.writeChunkWithTagsToNAND.writeChunkWithTagsToNAND(dev, chunkInNAND, buffer, bufferIndex,
							     tags);
		else
			return yaffs_TagsCompatabilityWriteChunkWithTagsToNAND(dev,
									       chunkInNAND,
									       buffer, bufferIndex,
									       tags);
	}

	static boolean yaffs_MarkBlockBad(yaffs_Device dev, int blockNo)
	{
		blockNo -= dev.blockOffset;

	;
		if (dev.markNANDBlockBad != null)
			return dev.markNANDBlockBad.markNANDBlockBad(dev, blockNo);
		else
			return yaffs_TagsCompatabilityMarkNANDBlockBad(dev, blockNo);
	}

	static boolean yaffs_QueryInitialBlockState(yaffs_Device dev,
							 int blockNo,
							 /*yaffs_BlockState*/ IntegerPointer state,
							 /*unsigned **/ IntegerPointer sequenceNumber)
	{
		blockNo -= dev.blockOffset;

		if (dev.queryNANDBlock != null)
			return dev.queryNANDBlock.queryNANDBlock(dev, blockNo, state, sequenceNumber);
		else
			return yaffs_TagsCompatabilityQueryNANDBlock(dev, blockNo,
								     state,
								     sequenceNumber);
	}


	static boolean yaffs_EraseBlockInNAND(yaffs_Device dev,
					  int blockInNAND)
	{
		boolean result;

		blockInNAND -= dev.blockOffset;


		dev.nBlockErasures++;
		result = dev.eraseBlockInNAND.eraseBlockInNAND(dev, blockInNAND);

		return result;
	}

	static boolean yaffs_InitialiseNAND(yaffs_Device dev)
	{
		return dev.initialiseNAND.initialiseNAND(dev);
	}
}
