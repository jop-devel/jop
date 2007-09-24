package yaffs2.port;

import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class yaffs_guts_C
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

	static final String yaffs_guts_c_version =
		"$Id: yaffs_guts_C.java,v 1.6 2007/09/24 13:30:33 peter.hilber Exp $";

	/*#include "yportenv.h"

	#include "yaffsinterface.h"
	#include "yaffs_guts.h"
	#include "yaffs_tagsvalidity.h"

	#include "yaffs_tagscompat.h"
	#ifndef CONFIG_YAFFS_OWN_SORT
	#include "yaffs_qsort.h"
	#endif
	#include "yaffs_nand.h"

	#include "yaffs_checkptrw.h"

	#include "yaffs_nand.h"
	#include "yaffs_packedtags2.h"*/


	/*#ifdef CONFIG_YAFFS_WINCE
	void yfsd_LockYAFFS(BOOL fsLockOnly);
	void yfsd_UnlockYAFFS(BOOL fsLockOnly);
	#endif*/

	static final int YAFFS_PASSIVE_GC_CHUNKS = 2;

	//#include "yaffs_ecc.h"


//	/* Robustification (if it ever comes about...) */
//	static void yaffs_RetireBlock(yaffs_Device * dev, int blockInNAND);
//	static void yaffs_HandleWriteChunkError(yaffs_Device * dev, int chunkInNAND, int erasedOk);
//	static void yaffs_HandleWriteChunkOk(yaffs_Device * dev, int chunkInNAND,
//	const __u8 * data,
//	const yaffs_ExtendedTags * tags);
//	static void yaffs_HandleUpdateChunk(yaffs_Device * dev, int chunkInNAND,
//	const yaffs_ExtendedTags * tags);

//	/* Other local prototypes */
//	static int yaffs_UnlinkObject( yaffs_Object *obj);
//	static int yaffs_ObjectHasCachedWriteData(yaffs_Object *obj);

//	static void yaffs_HardlinkFixup(yaffs_Device *dev, yaffs_Object *hardList);

//	static int yaffs_WriteNewChunkWithTagsToNAND(yaffs_Device * dev,
//	const __u8 * buffer,
//	yaffs_ExtendedTags * tags,
//	int useReserve);
//	static int yaffs_PutChunkIntoFile(yaffs_Object * in, int chunkInInode,
//	int chunkInNAND, int inScan);

//	static yaffs_Object *yaffs_CreateNewObject(yaffs_Device * dev, int number,
//	yaffs_ObjectType type);
//	static void yaffs_AddObjectToDirectory(yaffs_Object * directory,
//	yaffs_Object * obj);
//	static int yaffs_UpdateObjectHeader(yaffs_Object * in, const YCHAR * name,
//	int force, int isShrink, int shadows);
//	static void yaffs_RemoveObjectFromDirectory(yaffs_Object * obj);
//	static int yaffs_CheckStructures(void);
//	static int yaffs_DeleteWorker(yaffs_Object * in, yaffs_Tnode * tn, __u32 level,
//	int chunkOffset, int *limit);
//	static int yaffs_DoGenericObjectDeletion(yaffs_Object * in);

//	static yaffs_BlockInfo *yaffs_GetBlockInfo(yaffs_Device * dev, int blockNo);

//	static __u8 *yaffs_GetTempBuffer(yaffs_Device * dev, int lineNo);
//	static void yaffs_ReleaseTempBuffer(yaffs_Device * dev, __u8 * buffer,
//	int lineNo);

//	static int yaffs_CheckChunkErased(struct yaffs_DeviceStruct *dev,
//	int chunkInNAND);

//	static int yaffs_UnlinkWorker(yaffs_Object * obj);
//	static void yaffs_DestroyObject(yaffs_Object * obj);

//	static int yaffs_TagsMatch(const yaffs_ExtendedTags * tags, int objectId,
//	int chunkInObject);

//	loff_t yaffs_GetFileSize(yaffs_Object * obj);

//	static int yaffs_AllocateChunk(yaffs_Device * dev, int useReserve, yaffs_BlockInfo **blockUsedPtr);

//	static void yaffs_VerifyFreeChunks(yaffs_Device * dev);

//	#ifdef YAFFS_PARANOID
//	static int yaffs_CheckFileSanity(yaffs_Object * in);
//	#else
//	#define yaffs_CheckFileSanity(in)	// PORT
	static void yaffs_CheckFileSanity(yaffs_Object in)
	{}
//	#endif

//	static void yaffs_InvalidateWholeChunkCache(yaffs_Object * in);
//	static void yaffs_InvalidateChunkCache(yaffs_Object * object, int chunkId);

//	static void yaffs_InvalidateCheckpoint(yaffs_Device *dev);



	/* Function to calculate chunk and offset */

	static void yaffs_AddrToChunk(yaffs_Device dev, int addr, IntegerPointer chunk, IntegerPointer offset)
	{
		if(dev.subField2.chunkShift != 0){
			/* Easy-peasy power of 2 case */
			chunk.dereferenced  = /*(__u32)*/(addr >>> dev.subField2.chunkShift);
			offset.dereferenced = /*(__u32)*/(addr & dev.subField2.chunkMask);
		}
		else if(dev.subField2.crumbsPerChunk != 0)
		{
			/* Case where we're using "crumbs" */
			offset.dereferenced = /*(__u32)*/(addr & dev.subField2.crumbMask);
			addr >>>= dev.subField2.crumbShift;
			chunk.dereferenced = (/*(__u32)*/addr)/dev.subField2.crumbsPerChunk;
			offset.dereferenced += ((addr - (chunk.dereferenced * dev.subField2.crumbsPerChunk)) << dev.subField2.crumbShift);
		}
		else
			yaffs2.utils.Globals.portConfiguration.YBUG();
	}

	/* Function to return the number of shifts for a power of 2 greater than or equal 
	 * to the given number
	 * Note we don't try to cater for all possible numbers and this does not have to
	 * be hellishly efficient.
	 */

	static int ShiftsGE(/*__u32*/ int x)
	{
		int extraBits;
		int nShifts;

		nShifts = extraBits = 0;

		while(Utils.intAsUnsignedInt(x)>1){
			if((x & 1) != 0)extraBits++;
			x>>>=1;
			nShifts++;
		}

		if(extraBits != 0) 
			nShifts++;

		return nShifts;
	}

	/* Function to return the number of shifts to get a 1 in bit 0
	 */

	static int ShiftDiv(int x)
	{
		int nShifts;

		nShifts =  0;

		if(!(x != 0)) return 0;

		while( !((x&1) != 0)){
			x>>>=1;
			nShifts++;
		}

		return nShifts;
	}



	/* 
	 * Temporary buffer manipulations.
	 */

	static byte[] yaffs_GetTempBuffer(yaffs_Device dev, int lineNo)
	{
		int i, j;
		for (i = 0; i < Guts_H.YAFFS_N_TEMP_BUFFERS; i++) {
			if (dev.tempBuffer[i].line == 0) {
				dev.tempBuffer[i].line = lineNo;
				if ((i + 1) > dev.maxTemp) {
					dev.maxTemp = i + 1;
					for (j = 0; j <= i; j++)
						dev.tempBuffer[j].maxLine =
							dev.tempBuffer[j].line;
				}

				return dev.tempBuffer[i].buffer;
			}
		}

		yportenv.T(yportenv.YAFFS_TRACE_BUFFERS,
				("Out of temp buffers at line %d, other held by lines:"),
				PrimitiveWrapperFactory.get(lineNo));
		for (i = 0; i < Guts_H.YAFFS_N_TEMP_BUFFERS; i++) {
			yportenv.T(yportenv.YAFFS_TRACE_BUFFERS, (" %d "), PrimitiveWrapperFactory.get(dev.tempBuffer[i].line));
		}
		yportenv.T(yportenv.YAFFS_TRACE_BUFFERS, ((" " + ydirectenv.TENDSTR)));

		/*
		 * If we got here then we have to allocate an unmanaged one
		 * This is not good.
		 */

		dev.unmanagedTempAllocations++;
		return ydirectenv.YMALLOC(dev.subField1.nDataBytesPerChunk);

	}

	static void yaffs_ReleaseTempBuffer(yaffs_Device dev, byte[] buffer,
			int lineNo)
	{
		int i;
		for (i = 0; i < Guts_H.YAFFS_N_TEMP_BUFFERS; i++) {
			if (dev.tempBuffer[i].buffer == buffer) {
				dev.tempBuffer[i].line = 0;
				return;
			}
		}

		if (buffer != null) {
			/* assume it is an unmanaged one. */
			yportenv.T(yportenv.YAFFS_TRACE_BUFFERS,
					("Releasing unmanaged temp buffer in line %d" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(lineNo));
			ydirectenv.YFREE(buffer);
			dev.unmanagedTempDeallocations++;
		}

	}

	/*
	 * Determine if we have a managed buffer.
	 */
	static boolean yaffs_IsManagedTempBuffer(yaffs_Device dev, byte[] buffer)
	{
		int i;
		for (i = 0; i < Guts_H.YAFFS_N_TEMP_BUFFERS; i++) {
			if (dev.tempBuffer[i].buffer == buffer)
				return true;

		}

		for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
			if( dev.srCache[i].data == buffer )
				return true;

		}

		if (buffer == dev.subField2.checkpointBuffer)
			return true;

		yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
				("yaffs: unmaged buffer detected.\n" + ydirectenv.TENDSTR));
		return false;
	}

	/*
	 * Chunk bitmap manipulations
	 */

	static /*Y_INLINE*/ /*byte[]*/ ArrayPointer yaffs_BlockBits(yaffs_Device dev, int blk)
	{
		if (blk < dev.subField2.internalStartBlock || blk > dev.subField2.internalEndBlock) {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					("**>> yaffs: BlockBits block %d is not valid" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(blk));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		return new ArrayPointer(dev.subField2.chunkBits, (dev.subField3.chunkBitmapStride * (blk - dev.subField2.internalStartBlock)));
	}

	static /*Y_INLINE*/ void yaffs_ClearChunkBits(yaffs_Device dev, int blk)
	{
		ArrayPointer blkBits = yaffs_BlockBits(dev, blk);

		Unix.memset(blkBits.array, blkBits.index, (byte)0, dev.subField3.chunkBitmapStride);
	}

	static /*Y_INLINE*/ void yaffs_ClearChunkBit(yaffs_Device dev, int blk, int chunk)
	{
		ArrayPointer blkBits = yaffs_BlockBits(dev, blk);

		byte _buf = blkBits.get(chunk / 8);
		_buf &= ~(1 << (chunk & 7));
		blkBits.set(chunk / 8, _buf);
	}

	static /*Y_INLINE*/ void yaffs_SetChunkBit(yaffs_Device dev, int blk, int chunk)
	{
		ArrayPointer blkBits = yaffs_BlockBits(dev, blk);

		byte _buf = blkBits.get(chunk / 8);
		_buf  |= (1 << (chunk & 7));
		blkBits.set(chunk / 8, _buf);
	}

	static /*Y_INLINE*/ boolean yaffs_CheckChunkBit(yaffs_Device dev, int blk, int chunk)
	{
		ArrayPointer blkBits = yaffs_BlockBits(dev, blk);
		return ((blkBits.get(chunk / 8) & (1 << (chunk & 7))) != 0) ? true : false;
	}

	static /*Y_INLINE*/ boolean yaffs_StillSomeChunkBits(yaffs_Device dev, int blk)
	{
		ArrayPointer blkBits = yaffs_BlockBits(dev, blk);
		int i;
		for (i = 0; i < dev.subField3.chunkBitmapStride; i++) {
			if (blkBits.get() != 0)
				return true;
			blkBits.increment();
		}
		return false;
	}

	/*
	 *  Simple hash function. Needs to have a reasonable spread
	 */

	static /*Y_INLINE*/ int yaffs_HashFunction(int n)
	{
		n = Math.abs(n);
		return (n % Guts_H.YAFFS_NOBJECT_BUCKETS);
	}

	/*
	 * Access functions to useful fake objects
	 */

	static yaffs_Object yaffs_Root(yaffs_Device dev)
	{
		return dev.rootDir;
	}

	static yaffs_Object yaffs_LostNFound(yaffs_Device dev)
	{
		return dev.lostNFoundDir;
	}


	/*
	 *  Erased NAND checking functions
	 */

	static boolean yaffs_CheckFF(byte[] buffer, int bufferIndex, int nBytes)
	{
		/* Horrible, slow implementation */
		int i = 0;
		while ((nBytes--) != 0) {
			if (Utils.byteAsUnsignedByte(buffer[bufferIndex+i]) != 0xFF)
				return false;
			i++;
		}
		return true;
	}

	static boolean yaffs_CheckChunkErased(yaffs_Device dev,
			int chunkInNAND)
	{

		boolean retval = Guts_H.YAFFS_OK;
		byte[] data = yaffs_GetTempBuffer(dev, 1 /*Utils.__LINE__()*/);
		final int dataIndex = 0;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		boolean result;

		result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, chunkInNAND, data, dataIndex, tags);

		if(tags.eccResult > Guts_H.YAFFS_ECC_RESULT_NO_ERROR)
			retval = Guts_H.YAFFS_FAIL;


		if (!yaffs_CheckFF(data, dataIndex, dev.subField1.nDataBytesPerChunk) || tags.chunkUsed) {
			yportenv.T(yportenv.YAFFS_TRACE_NANDACCESS,
					("Chunk %d not erased" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
			retval = Guts_H.YAFFS_FAIL;
		}

		yaffs_ReleaseTempBuffer(dev, data, 2 /*Utils.__LINE__()*/);

		return retval;

	}


	static int yaffs_WriteNewChunkWithTagsToNAND(yaffs_Device dev,
			byte[] data, int dataIndex,
			yaffs_ExtendedTags tags,
			boolean useReserve)
	{
		int chunk;

		boolean writeOk = false;
		boolean erasedOk = true;
		int attempts = 0;
		yaffs_BlockInfo bi;

		yaffs_InvalidateCheckpoint(dev);

		do {
			yaffs_BlockInfoPointer biPointer = new yaffs_BlockInfoPointer();
			chunk = yaffs_AllocateChunk(dev, useReserve, biPointer);
			bi = biPointer.dereferenced;

			if (chunk >= 0) {
				/* First check this chunk is erased, if it needs checking.
				 * The checking policy (unless forced always on) is as follows:
				 * Check the first page we try to write in a block.
				 * - If the check passes then we don't need to check any more.
				 * - If the check fails, we check again...
				 * If the block has been erased, we don't need to check.
				 *
				 * However, if the block has been prioritised for gc, then
				 * we think there might be something odd about this block
				 * and stop using it.
				 *
				 * Rationale:
				 * We should only ever see chunks that have not been erased
				 * if there was a partially written chunk due to power loss
				 * This checking policy should catch that case with very
				 * few checks and thus save a lot of checks that are most likely not
				 * needed.
				 */

				if(bi.gcPrioritise()){
					yaffs_DeleteChunk(dev, chunk, true, 3 /*Utils.__LINE__()*/);
				} else {
					/*#ifdef CONFIG_YAFFS_ALWAYS_CHECK_CHUNK_ERASED

				bi->skipErasedCheck = 0;

#endif*/
					if(!bi.skipErasedCheck()){
						erasedOk = yaffs_CheckChunkErased(dev, chunk);
						if(erasedOk && !bi.gcPrioritise())
							bi.setSkipErasedCheck(true);
					}

					if (!erasedOk) {
						yportenv.T(yportenv.YAFFS_TRACE_ERROR,

								("**>> yaffs chunk %d was not erased"
										+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunk));
					} else {
						writeOk =
							yaffs_nand_C.yaffs_WriteChunkWithTagsToNAND(dev, chunk,
									data, dataIndex, tags);
					}

					attempts++;

					if (writeOk) {
						/*
						 *  Copy the data into the robustification buffer.
						 *  NB We do this at the end to prevent duplicates in the case of a write error.
						 *  Todo
						 */
						yaffs_HandleWriteChunkOk(dev, chunk, data, dataIndex, tags);

					} else {
						/* The erased check or write failed */
						yaffs_HandleWriteChunkError(dev, chunk, erasedOk);
					}
				}
			}

		} while (chunk >= 0 && !writeOk);

		if (attempts > 1) {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					("**>> yaffs write required %d attempts" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(attempts));
			dev.subField3.nRetriedWrites += (attempts - 1);
		}

		return chunk;
	}


	/*
	 * Block retiring for handling a broken block.
	 */

	static void yaffs_RetireBlock(yaffs_Device dev, int blockInNAND)
	{
		yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev, blockInNAND);

		yaffs_InvalidateCheckpoint(dev);

		yaffs_nand_C.yaffs_MarkBlockBad(dev, blockInNAND);

		bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_DEAD);
		bi.setGcPrioritise(false);
		bi.setNeedsRetiring(false);

		dev.subField3.nRetiredBlocks++;
	}

	/*
	 * Functions for robustisizing TODO
	 *
	 */

	static void yaffs_HandleWriteChunkOk(yaffs_Device dev, int chunkInNAND,
			byte[] data, int dataIndex,
			yaffs_ExtendedTags tags)
	{
	}

	static void yaffs_HandleUpdateChunk(yaffs_Device dev, int chunkInNAND,
			yaffs_ExtendedTags tags)
	{
	}

	static void yaffs_HandleChunkError(yaffs_Device dev, yaffs_BlockInfo bi)
	{
		if(!bi.gcPrioritise()){
			bi.setGcPrioritise(true);
			dev.hasPendingPrioritisedGCs = true;
			bi.setChunkErrorStrikes(bi.chunkErrorStrikes() + 1);

			if(bi.chunkErrorStrikes() > 3){
				bi.setNeedsRetiring(true); /* Too many stikes, so retire this */
				yportenv.T(yportenv.YAFFS_TRACE_ALWAYS, ("yaffs: Block struck out" + ydirectenv.TENDSTR));

			}

		}
	}

	static void yaffs_ReportOddballBlocks(yaffs_Device dev)
	{
		int i;

		for(i = dev.subField2.internalStartBlock; i <= dev.subField2.internalEndBlock && ((yaffs2.utils.Globals.yaffs_traceMask & yportenv.YAFFS_TRACE_BAD_BLOCKS) != 0); i++){
			yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev,i);
			if(bi.needsRetiring() || bi.gcPrioritise())
				yportenv.T(yportenv.YAFFS_TRACE_BAD_BLOCKS, ("yaffs block %d%s%s" + ydirectenv.TENDSTR),
						PrimitiveWrapperFactory.get(i),
						PrimitiveWrapperFactory.get(bi.needsRetiring() ? " needs retiring" : ""), // XXX hope no gc
						PrimitiveWrapperFactory.get(bi.gcPrioritise() ?  " gc prioritised" : ""));

		}
	}

	static void yaffs_HandleWriteChunkError(yaffs_Device dev, int chunkInNAND, boolean erasedOk)
	{

		int blockInNAND = chunkInNAND / dev.subField1.nChunksPerBlock;
		yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev, blockInNAND);

		yaffs_HandleChunkError(dev,bi);


		if(erasedOk ) {
			/* Was an actual write failure, so mark the block for retirement  */
			bi.setNeedsRetiring(true);
			yportenv.T(yportenv.YAFFS_TRACE_ERROR | yportenv.YAFFS_TRACE_BAD_BLOCKS,
					("**>> Block %d needs retiring" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blockInNAND));


		}

		/* Delete the chunk */
		yaffs_DeleteChunk(dev, chunkInNAND, true, 4 /*Utils.__LINE__()*/);
	}


	/*---------------- Name handling functions ------------*/ 

	/**
	 *  @return _u16
	 */
	static short yaffs_CalcNameSum(byte[] name, int nameIndex)
	{
		short sum = 0;
		int i = 1;

		byte[] bname = name;
		int bnameIndex = nameIndex;		
		if (bname != null) {
			while (bname[bnameIndex] != 0 && (i <= Guts_H.YAFFS_MAX_NAME_LENGTH)) {

				/*#ifdef CONFIG_YAFFS_CASE_INSENSITIVE
			sum += yaffs_toupper(*bname) * i;
#else*/
				sum += Utils.byteAsUnsignedByte(bname[bnameIndex]) * i;
				/*#endif*/
				i++;
				bnameIndex++;
			}
		}
		return sum;
	}

	static void yaffs_SetObjectName(yaffs_Object obj, byte[] name, int nameIndex)
	{
		/*#ifdef CONFIG_YAFFS_SHORT_NAMES_IN_RAM*/
		if ((name != null) && ydirectenv.yaffs_strlen(name, nameIndex) <= Guts_H.YAFFS_SHORT_NAME_LENGTH) {
			ydirectenv.yaffs_strcpy(obj.shortName, 0, name, nameIndex);
		} else {
			obj.shortName[0] = ((byte)0);
		}
		/*#endif*/
		obj.sum = yaffs_CalcNameSum(name, nameIndex);
	}

	/*-------------------- TNODES -------------------

	 * List of spare tnodes
	 * The list is hooked together using the first pointer
	 * in the tnode.
	 */

	/* yaffs_CreateTnodes creates a bunch more tnodes and
	 * adds them to the tnode free list.
	 * Don't use this function directly
	 */

	static boolean yaffs_CreateTnodes(yaffs_Device dev, int nTnodes)
	{
		int i;
		int tnodeSize;
		yaffs_Tnode[] newTnodes;
		//byte[] mem;
		yaffs_Tnode curr;
		yaffs_Tnode next;
		yaffs_TnodeList tnl;

		if (nTnodes < 1)
			return Guts_H.YAFFS_OK;

		/* Calculate the tnode size in bytes for variable width tnode support.
		 * Must be a multiple of 32-bits  */
		tnodeSize = (dev.subField2.tnodeWidth * Guts_H.YAFFS_NTNODES_LEVEL0)/8;

		/* make these things */

		newTnodes = /*ydirectenv.YMALLOC(nTnodes * tnodeSize)*/ ydirectenv.YMALLOC_TNODE(nTnodes); 
		//mem = (__u8 *)newTnodes;

		if (newTnodes == null) {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					(("yaffs: Could not allocate Tnodes" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		/* Hook them into the free list */
		/*#if 0
	for (i = 0; i < nTnodes - 1; i++) {
		newTnodes[i].internal[0] = &newTnodes[i + 1];
#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
		newTnodes[i].internal[YAFFS_NTNODES_INTERNAL] = (void *)1;
#endif
	}

	newTnodes[nTnodes - 1].internal[0] = dev->freeTnodes;
#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
	newTnodes[nTnodes - 1].internal[YAFFS_NTNODES_INTERNAL] = (void *)1;
#endif
	dev->freeTnodes = newTnodes;
#else*/
		/* New hookup for wide tnodes */
		for(i = 0; i < nTnodes -1; i++) {
			curr = /*(yaffs_Tnode *) &mem[i * tnodeSize]*/ newTnodes[i];
			next = /*(yaffs_Tnode *) &mem[(i+1) * tnodeSize]*/ newTnodes[i+1];
			curr.internal[0] = next;
		}

		curr = /*(yaffs_Tnode *) &mem[(nTnodes - 1) * tnodeSize]*/ newTnodes[nTnodes - 1];
		curr.internal[0] = dev.subField3.freeTnodes; 
		dev.subField3.freeTnodes = /*(yaffs_Tnode *)mem*/ newTnodes[0];

		/*#endif*/


		dev.subField3.nFreeTnodes += nTnodes;
		dev.subField3.nTnodesCreated += nTnodes;

		/* Now add this bunch of tnodes to a list for freeing up.
		 * NB If we can't add this to the management list it isn't fatal
		 * but it just means we can't free this bunch of tnodes later.
		 */

		tnl = /*ydirectenv.YMALLOC(sizeof(yaffs_TnodeList))*/ new yaffs_TnodeList();
		if (tnl == null) {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					(
							("yaffs: Could not add tnodes to management list" + ydirectenv.TENDSTR)));

		} else {
			tnl.tnodes = newTnodes;
			tnl.next = dev.subField3.allocatedTnodeList;
			dev.subField3.allocatedTnodeList = tnl;
		}

		yportenv.T(yportenv.YAFFS_TRACE_ALLOCATE, (("yaffs: Tnodes added" + ydirectenv.TENDSTR)));

		return Guts_H.YAFFS_OK;
	}


	/* GetTnode gets us a clean tnode. Tries to make allocate more if we run out */

	static yaffs_Tnode yaffs_GetTnodeRaw(yaffs_Device dev)
	{
		yaffs_Tnode tn = null;

		/* If there are none left make more */
		if (dev.subField3.freeTnodes == null) {
			yaffs_CreateTnodes(dev, Guts_H.YAFFS_ALLOCATION_NTNODES);
		}

		if (dev.subField3.freeTnodes != null) {
			tn = dev.subField3.freeTnodes;
//			#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
//			if (tn->internal[YAFFS_NTNODES_INTERNAL] != (void *)1) {
//			/* Hoosterman, this thing looks like it isn't in the list */
//			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
//			(("yaffs: Tnode list bug 1" ydirectenv.TENDSTR)));
//			}
//			#endif
			dev.subField3.freeTnodes = dev.subField3.freeTnodes.internal[0];
			dev.subField3.nFreeTnodes--;
		}

		return tn;
	}

	static yaffs_Tnode yaffs_GetTnode(yaffs_Device dev)
	{
		yaffs_Tnode tn = yaffs_GetTnodeRaw(dev);

		if(tn != null)
			Unix.memset(tn/*, 0, (dev.tnodeWidth * Guts_H.YAFFS_NTNODES_LEVEL0)/8*/);

		return tn;	
	}

	/* FreeTnode frees up a tnode and puts it back on the free list */
	static void yaffs_FreeTnode(yaffs_Device dev, yaffs_Tnode tn)
	{
		if (tn != null) {
//			#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
//			if (tn->internal[YAFFS_NTNODES_INTERNAL] != 0) {
//			/* Hoosterman, this thing looks like it is already in the list */
//			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
//			(("yaffs: Tnode list bug 2" ydirectenv.TENDSTR)));
//			}
//			tn->internal[YAFFS_NTNODES_INTERNAL] = (void *)1;
//			#endif
			tn.internal[0] = dev.subField3.freeTnodes;
			dev.subField3.freeTnodes = tn;
			dev.subField3.nFreeTnodes++;
		}
	}

//	XXX does this work? only needed for aborted checkpoint loading, and deinitialising
	static void yaffs_DeinitialiseTnodes(yaffs_Device dev)
	{
		/* Free the list of allocated tnodes */
		yaffs_TnodeList tmp;

		while (dev.subField3.allocatedTnodeList != null) {
			tmp = dev.subField3.allocatedTnodeList.next;

			// XXX has it a chance to work?
			// XXX or try another strategy?
			/*ydirectenv.YFREE(dev->allocatedTnodeList->tnodes)*/ dev.subField3.allocatedTnodeList.tnodes = null;
			/*ydirectenv.YFREE(dev->allocatedTnodeList)*/ dev.subField3.allocatedTnodeList = null;
			dev.subField3.allocatedTnodeList = tmp;

		}

		dev.subField3.freeTnodes = null;
		dev.subField3.nFreeTnodes = 0;
	}

	static void yaffs_InitialiseTnodes(yaffs_Device dev)
	{
		dev.subField3.allocatedTnodeList = null;
		dev.subField3.freeTnodes = null;
		dev.subField3.nFreeTnodes = 0;
		dev.subField3.nTnodesCreated = 0;

	}


	static void yaffs_PutLevel0Tnode(yaffs_Device dev, yaffs_Tnode tn, int pos, int val)
	{
		/*__u32 map = (__u32 *)tn*/;
		int bitInMap;
		int bitInWord;
		int wordInMap;
		int mask;

		pos &= Guts_H.YAFFS_TNODES_LEVEL0_MASK;
		val >>>= dev.subField1.chunkGroupBits;

		bitInMap = pos * dev.subField2.tnodeWidth;
		wordInMap = bitInMap /32;
		bitInWord = bitInMap & (32 -1);

		mask = dev.subField2.tnodeMask << bitInWord;

		tn.andLevel0AsInt(wordInMap, ~mask);
		tn.orLevel0AsInt(wordInMap, (mask & (val << bitInWord)));

		if(dev.subField2.tnodeWidth > (32-bitInWord)) {
			bitInWord = (32 - bitInWord);
			wordInMap++;;
			mask = dev.subField2.tnodeMask >>> (/*dev->tnodeWidth -*/ bitInWord);
			tn.andLevel0AsInt(wordInMap, ~mask);
			tn.orLevel0AsInt(wordInMap, mask & (val >>> bitInWord));
		}

		// FIXME
		yportenv.T(yportenv.PORT_TRACE_TNODE, "PutLevel0Tnode: pos %d val %d map[wordInMap]: %d\n", PrimitiveWrapperFactory.get(pos), PrimitiveWrapperFactory.get(val), PrimitiveWrapperFactory.get(tn.level0AsInt(wordInMap)));
	}

	static int yaffs_GetChunkGroupBase(yaffs_Device dev, yaffs_Tnode tn, int pos)
	{
		/*__u32 *map = (__u32 *)tn;*/
		int bitInMap;
		int bitInWord;
		int wordInMap;
		int val;

		pos &= Guts_H.YAFFS_TNODES_LEVEL0_MASK;

		bitInMap = pos * dev.subField2.tnodeWidth;
		wordInMap = bitInMap /32;
		bitInWord = bitInMap & (32 -1);

		val = tn.level0AsInt(wordInMap) >>> bitInWord;

		if(dev.subField2.tnodeWidth > (32-bitInWord)) {
			bitInWord = (32 - bitInWord);
			wordInMap++;;
			val |= (tn.level0AsInt(wordInMap) << bitInWord);
		}

		val &= dev.subField2.tnodeMask;
		val <<= dev.subField1.chunkGroupBits;

		return val;
	}

	/* ------------------- End of individual tnode manipulation -----------------*/

	/* ---------Functions to manipulate the look-up tree (made up of tnodes) ------
	 * The look up tree is represented by the top tnode and the number of topLevel
	 * in the tree. 0 means only the level 0 tnode is in the tree.
	 */

	/* FindLevel0Tnode finds the level 0 tnode, if one exists. */
	static yaffs_Tnode yaffs_FindLevel0Tnode(yaffs_Device dev,
			yaffs_FileStructure fStruct,
			int chunkId)
	{

		yaffs_Tnode tn = fStruct.top;
		int i;
		int requiredTallness;
		int level = fStruct.topLevel;

		/* Check sane level and chunk Id */
		if (level < 0 || level > Guts_H.YAFFS_TNODES_MAX_LEVEL) {
			return null;
		}

		if (chunkId > Guts_H.YAFFS_MAX_CHUNK_ID) {
			return null;
		}

		/* First check we're tall enough (ie enough topLevel) */

		i = chunkId >>> Guts_H.YAFFS_TNODES_LEVEL0_BITS;
		requiredTallness = 0;
		while (i != 0) {
			i >>>= Guts_H.YAFFS_TNODES_INTERNAL_BITS;
			requiredTallness++;
		}

		if (requiredTallness > fStruct.topLevel) {
			/* Not tall enough, so we can't find it, return NULL. */
			return null;
		}

		/* Traverse down to level 0 */
		while (level > 0 && tn != null) {
			tn = tn.
			internal[(chunkId >>>
			( Guts_H.YAFFS_TNODES_LEVEL0_BITS + 
					(level - 1) *
					Guts_H.YAFFS_TNODES_INTERNAL_BITS)
			) &
			Guts_H.YAFFS_TNODES_INTERNAL_MASK];
			level--;

		}

		return tn;
	}


	/* AddOrFindLevel0Tnode finds the level 0 tnode if it exists, otherwise first expands the tree.
	 * This happens in two steps:
	 *  1. If the tree isn't tall enough, then make it taller.
	 *  2. Scan down the tree towards the level 0 tnode adding tnodes if required.
	 *
	 * Used when modifying the tree.
	 *
	 *  If the tn argument is NULL, then a fresh tnode will be added otherwise the specified tn will
	 *  be plugged into the ttree.
	 */

	static yaffs_Tnode yaffs_AddOrFindLevel0Tnode(yaffs_Device dev,
			yaffs_FileStructure fStruct,
			int chunkId,
			yaffs_Tnode passedTn)
	{

		int requiredTallness;
		int i;
		int l;
		yaffs_Tnode tn;

		int x;


		/* Check sane level and page Id */
		if (fStruct.topLevel < 0 || fStruct.topLevel > Guts_H.YAFFS_TNODES_MAX_LEVEL) {
			return null;
		}

		if (Utils.intAsUnsignedInt(chunkId) > Guts_H.YAFFS_MAX_CHUNK_ID) { 
			return null;
		}

		/* First check we're tall enough (ie enough topLevel) */

		x = chunkId >>> Guts_H.YAFFS_TNODES_LEVEL0_BITS;
		requiredTallness = 0;
		while (x != 0) {
			x >>>= Guts_H.YAFFS_TNODES_INTERNAL_BITS;
			requiredTallness++;
		}


		if (requiredTallness > fStruct.topLevel) {
			/* Not tall enough,gotta make the tree taller */
			for (i = fStruct.topLevel; i < requiredTallness; i++) {

				tn = yaffs_GetTnode(dev);

				if (tn != null) {
					tn.internal[0] = fStruct.top;
					fStruct.top = tn;
				} else {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
							(("yaffs: no more tnodes" + ydirectenv.TENDSTR)));
				}
			}

			// FIXME
			if (requiredTallness > 0) 
				yportenv.T(yportenv.PORT_TRACE_TALLNESS, "Required tallness: %d\n", PrimitiveWrapperFactory.get(requiredTallness)); 
			fStruct.topLevel = requiredTallness;
		}

		/* Traverse down to level 0, adding anything we need */

		l = fStruct.topLevel;
		tn = fStruct.top;

		if(l > 0) {
			while (l > 0 && tn != null) {
				x = (chunkId >>>
				( Guts_H.YAFFS_TNODES_LEVEL0_BITS +
						(l - 1) * Guts_H.YAFFS_TNODES_INTERNAL_BITS)) &
						Guts_H.YAFFS_TNODES_INTERNAL_MASK;


				if((l>1) && (tn.internal[x] == null)){
					/* Add missing non-level-zero tnode */
					tn.internal[x] = yaffs_GetTnode(dev);

				} else if(l == 1) {
					/* Looking from level 1 at level 0 */
					if (passedTn != null) {
						/* If we already have one, then release it.*/
						if(tn.internal[x] != null)
							yaffs_FreeTnode(dev,tn.internal[x]);
						tn.internal[x] = passedTn;

					} else if(tn.internal[x] == null) {
						/* Don't have one, none passed in */
						tn.internal[x] = yaffs_GetTnode(dev);
					}
				}

				tn = tn.internal[x];
				l--;
			}
		} else {
			/* top is level 0 */
			if(passedTn != null) {
				Unix.memcpy(tn,passedTn/*,(dev.tnodeWidth * Guts_H.YAFFS_NTNODES_LEVEL0)/8*/); // XXX only copy level0?
				yaffs_FreeTnode(dev,passedTn);
			}
		}

		return tn;
	}

	static int yaffs_FindChunkInGroup(yaffs_Device dev, int theChunk,
			yaffs_ExtendedTags tags, int objectId,
			int chunkInInode)
	{
		int j;

		for (j = 0; theChunk != 0 && j < dev.subField1.chunkGroupSize; j++) {
			if (yaffs_CheckChunkBit
					(dev, theChunk / dev.subField1.nChunksPerBlock,
							theChunk % dev.subField1.nChunksPerBlock)) {
				yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, theChunk, null, 0,
						tags);
				if (yaffs_TagsMatch(tags, objectId, chunkInInode)) {
					/* found it; */
					return theChunk;

				}
			}
			theChunk++;
		}
		return -1;
	}


	/* DeleteWorker scans backwards through the tnode tree and deletes all the
	 * chunks and tnodes in the file
	 * Returns 1 if the tree was deleted. 
	 * Returns 0 if it stopped early due to hitting the limit and the delete is incomplete.
	 */
	// XXX recursive

	static boolean yaffs_DeleteWorker(yaffs_Object in, yaffs_Tnode tn, int level,
			int chunkOffset, IntegerPointer limit)
	{
		int i;
		int chunkInInode;
		int theChunk;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		int foundChunk;
		yaffs_Device dev = in.myDev;

		boolean allDone = true;

		if (tn != null) {
			if (level > 0) {

				for (i = Guts_H.YAFFS_NTNODES_INTERNAL - 1; allDone && i >= 0;
				i--) {
					if (tn.internal[i] != null) {
						if (limit != null && (limit.dereferenced) < 0) {
							allDone = false;
						} else {
							allDone =
								yaffs_DeleteWorker(in,
										tn.
										internal
										[i],
										level -
										1,
										(chunkOffset
												<<
												Guts_H.YAFFS_TNODES_INTERNAL_BITS)
												+ i,
												limit);
						}
						if (allDone) {
							yaffs_FreeTnode(dev,
									tn.
									internal[i]);
							tn.internal[i] = null;
						}
					}

				}
				return (allDone) ? true : false;
			} else if (level == 0) {
				int hitLimit = 0;

				for (i = Guts_H.YAFFS_NTNODES_LEVEL0 - 1; i >= 0 && (hitLimit == 0);
				i--) {
					theChunk = yaffs_GetChunkGroupBase(dev,tn,i);
					if (theChunk != 0) {

						chunkInInode =
							(chunkOffset <<
									Guts_H.YAFFS_TNODES_LEVEL0_BITS) + i;

						foundChunk =
							yaffs_FindChunkInGroup(dev,
									theChunk,
									tags,
									in.objectId,
									chunkInInode);

						if (foundChunk > 0) {
							yaffs_DeleteChunk(dev,
									foundChunk, true,
									5 /*Utils.__LINE__()*/);
							in.nDataChunks--;
							if (limit != null) {
								limit.dereferenced = limit.dereferenced - 1;
								if (limit.dereferenced <= 0) {
									hitLimit = 1;
								}
							}

						}

						yaffs_PutLevel0Tnode(dev,tn,i,0);
					}

				}
				return (i < 0) ? true : false;

			}

		}

		return true;

	}

	static void yaffs_SoftDeleteChunk(yaffs_Device dev, int chunk)
	{

		yaffs_BlockInfo theBlock;

		yportenv.T(yportenv.YAFFS_TRACE_DELETION, ("soft delete chunk %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunk));

		theBlock = Guts_H.yaffs_GetBlockInfo(dev, chunk / dev.subField1.nChunksPerBlock);
		if (theBlock != null) {
			theBlock.setSoftDeletions(theBlock.softDeletions()+1);
			dev.subField3.nFreeChunks++;
		}
	}

	/* SoftDeleteWorker scans backwards through the tnode tree and soft deletes all the chunks in the file.
	 * All soft deleting does is increment the block's softdelete count and pulls the chunk out
	 * of the tnode.
	 * Thus, essentially this is the same as DeleteWorker except that the chunks are soft deleted.
	 */
	// XXX recursive

	static boolean yaffs_SoftDeleteWorker(yaffs_Object in, yaffs_Tnode tn,
			int level, int chunkOffset)
	{
		int i;
		int theChunk;
		boolean allDone = true;
		yaffs_Device dev = in.myDev;

		if (tn != null) {
			if (level > 0) {

				for (i = Guts_H.YAFFS_NTNODES_INTERNAL - 1; (allDone) && i >= 0;
				i--) {
					if (tn.internal[i] != null) {
						allDone =
							yaffs_SoftDeleteWorker(in,
									tn.
									internal[i],
									level - 1,
									(chunkOffset
											<<
											Guts_H.YAFFS_TNODES_INTERNAL_BITS)
											+ i);
						if (allDone) {
							yaffs_FreeTnode(dev,
									tn.
									internal[i]);
							tn.internal[i] = null;
						} else {
							/* Hoosterman... how could this happen? */
						}
					}
				}
				return (allDone) ? true : false;
			} else if (level == 0) {

				for (i = Guts_H.YAFFS_NTNODES_LEVEL0 - 1; i >= 0; i--) {
					theChunk = yaffs_GetChunkGroupBase(dev,tn,i);
					if (theChunk != 0) {
						/* Note this does not find the real chunk, only the chunk group.
						 * We make an assumption that a chunk group is not larger than 
						 * a block.
						 */
						yaffs_SoftDeleteChunk(dev, theChunk);
						yaffs_PutLevel0Tnode(dev,tn,i,0);
					}

				}
				return true;

			}

		}

		return true;

	}

	static void yaffs_SoftDeleteFile(yaffs_Object obj)
	{
		if (obj.sub.deleted &&
				obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE && !obj.sub.softDeleted) {
			if (obj.nDataChunks <= 0) {
				/* Empty file with no duplicate object headers, just delete it immediately */
				yaffs_FreeTnode(obj.myDev,
						obj.variant.fileVariant().top);
				obj.variant.fileVariant().top = null;
				yportenv.T(yportenv.YAFFS_TRACE_TRACING,
						("yaffs: Deleting empty file %d" + ydirectenv.TENDSTR),
						PrimitiveWrapperFactory.get(obj.objectId));
				yaffs_DoGenericObjectDeletion(obj);
			} else {
				yaffs_SoftDeleteWorker(obj,
						obj.variant.fileVariant().top,
						obj.variant.fileVariant().
						topLevel, 0);
				obj.sub.softDeleted = true;
			}
		}
	}

	/* Pruning removes any part of the file structure tree that is beyond the
	 * bounds of the file (ie that does not point to chunks).
	 *
	 * A file should only get pruned when its size is reduced.
	 *
	 * Before pruning, the chunks must be pulled from the tree and the
	 * level 0 tnode entries must be zeroed out.
	 * Could also use this for file deletion, but that's probably better handled
	 * by a special case.
	 */

	static yaffs_Tnode yaffs_PruneWorker(yaffs_Device dev, yaffs_Tnode tn,
			int level, boolean del0)
	{
		int i;
		int hasData;

		if (tn != null) {
			hasData = 0;

			for (i = 0; i < Guts_H.YAFFS_NTNODES_INTERNAL; i++) {
//				assert (level > 0 ? 
//				Utils.getIntFromByteArray(tn.serialized, i*4) == 0 : 
//				tn.internal[i] == null); 

				if (tn.internal[i] != null && level > 0) {
					tn.internal[i] =	// XXX recursive call
						yaffs_PruneWorker(dev, tn.internal[i],
								level - 1,
								(i == 0) ? del0 : true);
				}

				if ((tn.internal[i] != null) || // PORT union
						(Utils.getIntFromByteArray(tn.serialized, tn.offset+i*4) != 0)) {
					hasData++;
				}
			}

			if (hasData == 0 && del0) {
				/* Free and return NULL */

				yaffs_FreeTnode(dev, tn);
				tn = null;
			}

		}

		return tn;

	}

	static boolean yaffs_PruneFileStructure(yaffs_Device dev,
			yaffs_FileStructure fStruct)
	{
		int i;
		int hasData;
		boolean done = false;
		yaffs_Tnode tn;

		if (fStruct.topLevel > 0) {
			fStruct.top =
				yaffs_PruneWorker(dev, fStruct.top, fStruct.topLevel, false);

			/* Now we have a tree with all the non-zero branches NULL but the height
			 * is the same as it was.
			 * Let's see if we can trim internal tnodes to shorten the tree.
			 * We can do this if only the 0th element in the tnode is in use 
			 * (ie all the non-zero are NULL)
			 */

			while (fStruct.topLevel != 0 && !done) {
				tn = fStruct.top;

				hasData = 0;
				for (i = 1; i < Guts_H.YAFFS_NTNODES_INTERNAL; i++) {
					if (tn.internal[i] != null) {
						hasData++;
					}
				}

				if (hasData == 0) {
					fStruct.top = tn.internal[0];
					fStruct.topLevel--;
					// FIXME
					yportenv.T(yportenv.PORT_TRACE_TOPLEVEL, "Reducing topLevel: %d\n", PrimitiveWrapperFactory.get(fStruct.topLevel));
					yaffs_FreeTnode(dev, tn);
				} else {
					done = true;
				}
			}
		}

		return Guts_H.YAFFS_OK;
	}

	/*-------------------- End of File Structure functions.-------------------*/

	/* yaffs_CreateFreeObjects creates a bunch more objects and
	 * adds them to the object free list.
	 */
	static boolean yaffs_CreateFreeObjects(yaffs_Device dev, int nObjects)
	{
		int i;
		yaffs_Object[] newObjects;
		yaffs_ObjectList list;

		if (nObjects < 1)
			return Guts_H.YAFFS_OK;

		/* make these things */
		newObjects = ydirectenv.YMALLOC_OBJECT(nObjects/* * sizeof(yaffs_Object)*/ );

		if (!(newObjects != null)) {
			yportenv.T(yportenv.YAFFS_TRACE_ALLOCATE,
					(("yaffs: Could not allocate more objects" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		/* Hook them into the free list */
		for (i = 0; i < nObjects - 1; i++) {
			newObjects[i].siblings.next =
				/*(list_head)*/ (newObjects[i + 1]);
		}

		newObjects[nObjects - 1].siblings.next = /*(void *)*/ dev.subField3.freeObjects; 
		dev.subField3.freeObjects = newObjects[0];
		dev.subField3.nFreeObjects += nObjects;
		dev.subField3.nObjectsCreated += nObjects;

		/* Now add this bunch of Objects to a list for freeing up. */

		list = /*ydirectenv.YMALLOC(sizeof(yaffs_ObjectList))*/ new yaffs_ObjectList();
		if (!(list != null)) {
			yportenv.T(yportenv.YAFFS_TRACE_ALLOCATE,
					(("Could not add objects to management list" + ydirectenv.TENDSTR)));
		} else {
			list.objects = newObjects;
			list.next = dev.subField3.allocatedObjectList;
			dev.subField3.allocatedObjectList = list;
		}

		return Guts_H.YAFFS_OK;
	}


	/* AllocateEmptyObject gets us a clean Object. Tries to make allocate more if we run out */
	static yaffs_Object yaffs_AllocateEmptyObject(yaffs_Device dev)
	{
		yaffs_Object tn = null;

		/* If there are none left make more */
		if (!(dev.subField3.freeObjects != null)) {
			yaffs_CreateFreeObjects(dev, Guts_H.YAFFS_ALLOCATION_NOBJECTS);
		}

		if (dev.subField3.freeObjects != null) {
			tn = dev.subField3.freeObjects;
			dev.subField3.freeObjects =
				(yaffs_Object) (dev.subField3.freeObjects.siblings.next);
			dev.subField3.nFreeObjects--;

			/* Now sweeten it up... */

			Unix.memset(tn/*, 0, sizeof(yaffs_Object)*/ );
			tn.myDev = dev;
			tn.chunkId = -1;
			tn.variantType = Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN;
			devextras.INIT_LIST_HEAD((tn.hardLinks));
			devextras.INIT_LIST_HEAD((tn.hashLink));
			devextras.INIT_LIST_HEAD(tn.siblings);

			/* Add it to the lost and found directory.
			 * NB Can't put root or lostNFound in lostNFound so
			 * check if lostNFound exists first
			 */
			if (dev.lostNFoundDir != null) {
				yaffs_AddObjectToDirectory(dev.lostNFoundDir, tn);
			}
		}

		return tn;
	}

	static yaffs_Object yaffs_CreateFakeDirectory(yaffs_Device dev, int number,
			int mode)
	{

		yaffs_Object obj =
			yaffs_CreateNewObject(dev, number, Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY);
		if (obj != null) {
			obj.sub.fake = true;		/* it is fake so it has no NAND presence... */
			obj.renameAllowed = false;	/* ... and we're not allowed to rename it... */
			obj.unlinkAllowed = false;	/* ... or unlink it */
			obj.sub.deleted = false;
			obj.sub.unlinked = false;
			obj.yst_mode = mode;
			obj.myDev = dev;
			obj.chunkId = 0;	/* Not a valid chunk. */
		}

		return obj;

	}

	static void yaffs_UnhashObject(yaffs_Object tn)
	{
		int bucket;
		yaffs_Device dev = tn.myDev;

		/* If it is still linked into the bucket list, free from the list */
		if (!devextras.list_empty(tn.hashLink)) {
			devextras.list_del_init(tn.hashLink);
			bucket = yaffs_HashFunction(tn.objectId);
			dev.subField3.objectBucket[bucket].count--;
		}

	}

	/*  FreeObject frees up a Object and puts it back on the free list */
	static void yaffs_FreeObject(yaffs_Object tn)
	{

		yaffs_Device dev = tn.myDev;

//		#ifdef  __KERNEL__
//		if (tn.myInode) {
//		/* We're still hooked up to a cached inode.
//		* Don't delete now, but mark for later deletion
//		*/
//		tn.deferedFree = 1;
//		return;
//		}
//		#endif

		yaffs_UnhashObject(tn);

		/* Link into the free list. */
		tn.siblings.next = /*(list_head)*/ (dev.subField3.freeObjects);
		dev.subField3.freeObjects = tn;
		dev.subField3.nFreeObjects++;
	}

//	#ifdef __KERNEL__

//	void yaffs_HandleDeferedFree(yaffs_Object * obj)
//	{
//	if (obj.deferedFree) {
//	yaffs_FreeObject(obj);
//	}
//	}

//	#endif

	static void yaffs_DeinitialiseObjects(yaffs_Device dev)
	{
		/* Free the list of allocated Objects */

		yaffs_ObjectList tmp;

		while (dev.subField3.allocatedObjectList != null) {
			tmp = dev.subField3.allocatedObjectList.next;
			ydirectenv.YFREE(dev.subField3.allocatedObjectList.objects);
			ydirectenv.YFREE(dev.subField3.allocatedObjectList);

			dev.subField3.allocatedObjectList = tmp;
		}

		dev.subField3.freeObjects = null;
		dev.subField3.nFreeObjects = 0;
	}

	static void yaffs_InitialiseObjects(yaffs_Device dev)
	{
		int i;

		dev.subField3.allocatedObjectList = null;
		dev.subField3.freeObjects = null;
		dev.subField3.nFreeObjects = 0;

		for (i = 0; i < Guts_H.YAFFS_NOBJECT_BUCKETS; i++) {
			devextras.INIT_LIST_HEAD(dev.subField3.objectBucket[i].list);
			dev.subField3.objectBucket[i].count = 0;
		}

	}

	static int _STATIC_LOCAL_yaffs_FindNiceObjectBucket_x = 0;
	static int yaffs_FindNiceObjectBucket(yaffs_Device dev)
	{

		int i;
		int l = 999;
		int lowest = 999999;

		/* First let's see if we can find one that's empty. */

		for (i = 0; i < 10 && lowest > 0; i++) {
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x++;
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x %= Guts_H.YAFFS_NOBJECT_BUCKETS;
			if (dev.subField3.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count < lowest) {
				lowest = dev.subField3.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count;
				l = _STATIC_LOCAL_yaffs_FindNiceObjectBucket_x;
			}

		}

		/* If we didn't find an empty list, then try
		 * looking a bit further for a short one
		 */

		for (i = 0; i < 10 && lowest > 3; i++) {
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x++;
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x %= Guts_H.YAFFS_NOBJECT_BUCKETS;
			if (dev.subField3.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count < lowest) {
				lowest = dev.subField3.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count;
				l = _STATIC_LOCAL_yaffs_FindNiceObjectBucket_x;
			}

		}

		return l;
	}

	static int yaffs_CreateNewObjectNumber(yaffs_Device dev)
	{
		int bucket = yaffs_FindNiceObjectBucket(dev);

		/* Now find an object value that has not already been taken
		 * by scanning the list.
		 */

		boolean found = false;
		list_head i;

		/*__u32*/ int n = /*(__u32)*/ bucket;	// XXX // ???

		/* yaffs_CheckObjectHashSanity();  */

		while (!found) {
			found = true;
			n += Guts_H.YAFFS_NOBJECT_BUCKETS;
			if (true || dev.subField3.objectBucket[bucket].count > 0) {
//				list_for_each(i, &dev->objectBucket[bucket].list) {
				for (i = dev.subField3.objectBucket[bucket].list.next(); i != dev.subField3.objectBucket[bucket].list;
				i =	i.next())
				{
					/* If there is already one in the list */
					if (i != null
							&& ((yaffs_Object)i.list_entry).objectId == n) {
						found = false;
					}

				}
			}
		}


		return n;
	}

	static void yaffs_HashObject(yaffs_Object in)
	{
		int bucket = yaffs_HashFunction(in.objectId);
		yaffs_Device dev = in.myDev;

		devextras.list_add(in.hashLink, dev.subField3.objectBucket[bucket].list);
		dev.subField3.objectBucket[bucket].count++;

	}

	static yaffs_Object yaffs_FindObjectByNumber(yaffs_Device dev, /*__u32*/ int number)
	{
		int bucket = yaffs_HashFunction(number);
		list_head i;
		yaffs_Object in;

//		list_for_each(i, &dev.objectBucket[bucket].list) {
		for (i = dev.subField3.objectBucket[bucket].list.next(); i != dev.subField3.objectBucket[bucket].list;
		i = i.next()) {
			/* Look if it is in the list */
			if (i != null) {
				in = /*list_entry(i, yaffs_Object, hashLink)*/ ((yaffs_Object)i.list_entry);
				if (in.objectId == number) {
//					#ifdef __KERNEL__
//					/* Don't tell the VFS about this one if it is defered free */
//					if (in.deferedFree)
//					return null;
//					#endif

					return in;
				}
			}
		}

		return null;
	}

	static yaffs_Object yaffs_CreateNewObject(yaffs_Device dev, int number,
			/*yaffs_ObjectType*/ int type)
	{

		yaffs_Object theObject;

		if (number < 0) {
			number = yaffs_CreateNewObjectNumber(dev);
		}

		theObject = yaffs_AllocateEmptyObject(dev);

		if (theObject != null) {
			theObject.sub.fake = false;
			theObject.renameAllowed = true;
			theObject.unlinkAllowed = true;
			theObject.objectId = number;
			yaffs_HashObject(theObject);
			theObject.variantType = type;
//			#ifdef CONFIG_YAFFS_WINCE
//			yfsd_WinFileTimeNow(theObject.win_atime);
//			theObject.win_ctime[0] = theObject.win_mtime[0] =
//			theObject.win_atime[0];
//			theObject.win_ctime[1] = theObject.win_mtime[1] =
//			theObject.win_atime[1];

//			#else

			theObject.yst_atime = theObject.yst_mtime =
				theObject.yst_ctime = ydirectenv.Y_CURRENT_TIME();
//			#endif
			switch (type) {	// XXX either allocate/get from pool the corresponding variant here, or create all possible variants on object creation
			case Guts_H.YAFFS_OBJECT_TYPE_FILE:
				theObject.variant.fileVariant().fileSize = 0;
				theObject.variant.fileVariant().scannedFileSize = 0;
				theObject.variant.fileVariant().shrinkSize = 0xFFFFFFFF;	/* max __u32 */
				theObject.variant.fileVariant().topLevel = 0;
				theObject.variant.fileVariant().top =
					yaffs_GetTnode(dev);
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
				devextras.INIT_LIST_HEAD(theObject.variant.directoryVariant().
						children);
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
			case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
			case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
				/* No action required */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:
				/* todo this should not happen */
				break;
			}
		}

		return theObject;
	}

	static yaffs_Object yaffs_FindOrCreateObjectByNumber(yaffs_Device dev,
			int number,
			/*yaffs_ObjectType*/ int type)
	{
		yaffs_Object theObject = null;

		if (number > 0) {
			theObject = yaffs_FindObjectByNumber(dev, number);
		}

		if (theObject == null) {
			theObject = yaffs_CreateNewObject(dev, number, type);
		}

		return theObject;

	}


	// XXX this is REALLY a bad function if we dont want to allocate memory
	// XXX see if it is used after startup
	// XXX only if it is done after startup see if we could replace it somehow
	static byte[] yaffs_CloneString(byte[] str, int strIndex)
	{
		byte[] newStr = null;

		if (str != null && str[0] != 0) {
			newStr = ydirectenv.YMALLOC((ydirectenv.yaffs_strlen(str, strIndex) + 1)/* * sizeof(YCHAR)*/);
			ydirectenv.yaffs_strcpy(newStr, 0, str, strIndex);
		}

		return newStr;

	}

	/*
	 * Mknod (create) a new object.
	 * equivalentObject only has meaning for a hard link;
	 * aliasString only has meaning for a sumlink.
	 * rdev only has meaning for devices (a subset of special objects)
	 */

	static yaffs_Object yaffs_MknodObject(/*yaffs_ObjectType*/ int type,
			yaffs_Object parent,
			byte[] name, int nameIndex,
			int mode,
			int uid,
			int gid,
			yaffs_Object  equivalentObject,
			byte[] aliasString, int aliasStringIndex, 
			int rdev)
	{
		yaffs_Object in;

		yaffs_Device dev = parent.myDev;

		/* Check if the entry exists. If it does then fail the call since we don't want a dup.*/
		if (yaffs_FindObjectByName(parent, name, nameIndex) != null) {
			return null;
		}

		in = yaffs_CreateNewObject(dev, -1, type);

		if (in != null) {
			in.chunkId = -1;
			in.valid = true;
			in.variantType = type;

			in.yst_mode = mode;

//			#ifdef CONFIG_YAFFS_WINCE
//			yfsd_WinFileTimeNow(in.win_atime);
//			in.win_ctime[0] = in.win_mtime[0] = in.win_atime[0];
//			in.win_ctime[1] = in.win_mtime[1] = in.win_atime[1];

//			#else
			in.yst_atime = in.yst_mtime = in.yst_ctime = ydirectenv.Y_CURRENT_TIME();

			in.yst_rdev = rdev;
			in.yst_uid = uid;
			in.yst_gid = gid;
//			#endif
			in.nDataChunks = 0;

			yaffs_SetObjectName(in, name, nameIndex);
			in.dirty = true;

			yaffs_AddObjectToDirectory(parent, in);

			in.myDev = parent.myDev;

			switch (type) {
			case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
				in.variant.symLinkVariant().alias =
					yaffs_CloneString(aliasString, aliasStringIndex);
				in.variant.symLinkVariant().aliasIndex = 0;
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
				in.variant.hardLinkVariant().equivalentObject =
					equivalentObject;
				in.variant.hardLinkVariant().equivalentObjectId =
					equivalentObject.objectId;
				devextras.list_add(in.hardLinks, equivalentObject.hardLinks);
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_FILE:	
			case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
			case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
			case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:
				/* do nothing */
				break;
			}

			if (yaffs_UpdateObjectHeader(in, name, nameIndex, false, false, 0) < 0) {
				/* Could not create the object header, fail the creation */
				yaffs_DestroyObject(in);
				in = null;
			}

		}

		return in;
	}

	static yaffs_Object yaffs_MknodFile(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid)
	{
		return yaffs_MknodObject(Guts_H.YAFFS_OBJECT_TYPE_FILE, parent, name, nameIndex, mode,
				uid, gid, null, null, 0, 0);
	}

	static yaffs_Object yaffs_MknodDirectory(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid)
	{
		return yaffs_MknodObject(Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY, parent, name, nameIndex,
				mode, uid, gid, null, null, 0, 0);
	}

	static yaffs_Object yaffs_MknodSpecial(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid, /*__u32*/ int rdev)
	{
		return yaffs_MknodObject(Guts_H.YAFFS_OBJECT_TYPE_SPECIAL, parent, name, nameIndex, 
				mode, uid, gid, null, null, 0, rdev);
	}

	static yaffs_Object yaffs_MknodSymLink(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid,
			byte[] alias, int aliasIndex)
	{
		return yaffs_MknodObject(Guts_H.YAFFS_OBJECT_TYPE_SYMLINK, parent, name, nameIndex,
				mode, uid, gid, null, alias, aliasIndex, 0);
	}

	/* yaffs_Link returns the object id of the equivalent object.*/
	static yaffs_Object yaffs_Link(yaffs_Object  parent, byte[] name, int nameIndex,
			yaffs_Object  equivalentObject)
	{
		/* Get the real object in case we were fed a hard link as an equivalent object */
		equivalentObject = yaffs_GetEquivalentObject(equivalentObject);

		if (yaffs_MknodObject
				(Guts_H.YAFFS_OBJECT_TYPE_HARDLINK, parent, name, nameIndex, 0, 0, 0,
						equivalentObject, null, 0, 0) != null) {
			return equivalentObject;
		} else {
			return null;
		}

	}

	static boolean yaffs_ChangeObjectName(yaffs_Object  obj, yaffs_Object  newDir,
			byte[] newName, int newNameIndex, boolean force, int shadows)
	{
		boolean unlinkOp;
		boolean deleteOp;

		yaffs_Object existingTarget;

		if (newDir == null) {
			newDir = obj.parent;	/* use the old directory */
		}

		if (newDir.variantType != Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,

					("tragendy: yaffs_ChangeObjectName: newDir is not a directory"
							+ ydirectenv.TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		/* TODO: Do we need this different handling for YAFFS2 and YAFFS1?? */
		if (obj.myDev.subField1.isYaffs2) {
			unlinkOp = (newDir == obj.myDev.unlinkedDir);
		} else {
			unlinkOp = (newDir == obj.myDev.unlinkedDir
					&& obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE);
		}

		deleteOp = (newDir == obj.myDev.deletedDir);

		existingTarget = yaffs_FindObjectByName(newDir, newName, newNameIndex);

		/* If the object is a file going into the unlinked directory, 
		 *   then it is OK to just stuff it in since duplicate names are allowed.
		 *   else only proceed if the new name does not exist and if we're putting 
		 *   it into a directory.
		 */
		if ((unlinkOp ||
				deleteOp ||
				force ||
				(shadows > 0) ||
				!(existingTarget != null)) &&
				newDir.variantType == Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY) {
			yaffs_SetObjectName(obj, newName, newNameIndex);
			obj.dirty = true;

			yaffs_AddObjectToDirectory(newDir, obj);

			if (unlinkOp)
				obj.sub.unlinked = true;

			/* If it is a deletion then we mark it as a shrink for gc purposes. */
			if (yaffs_UpdateObjectHeader(obj, newName, newNameIndex, false, deleteOp, shadows)>= 0)
				return Guts_H.YAFFS_OK;
		}

		return Guts_H.YAFFS_FAIL;
	}

	static boolean yaffs_RenameObject(yaffs_Object  oldDir, byte[] oldName, int oldNameIndex,
			yaffs_Object  newDir, byte[] newName, int newNameIndex)
	{
		yaffs_Object obj;
		yaffs_Object existingTarget;
		boolean force = false;

//		#ifdef CONFIG_YAFFS_CASE_INSENSITIVE
//		/* Special case for case insemsitive systems (eg. WinCE).
//		* While look-up is case insensitive, the name isn't.
//		* Therefore we might want to change x.txt to X.txt
//		*/
//		if (oldDir == newDir && yaffs_strcmp(oldName, newName) == 0) {
//		force = 1;
//		}
//		#endif

		obj = yaffs_FindObjectByName(oldDir, oldName, oldNameIndex);
		/* Check new name to long. */
		if (obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_SYMLINK &&
				ydirectenv.yaffs_strlen(newName, newNameIndex) > Guts_H.YAFFS_MAX_ALIAS_LENGTH)
			/* ENAMETOOLONG */
			return Guts_H.YAFFS_FAIL;
		else if (obj.variantType != Guts_H.YAFFS_OBJECT_TYPE_SYMLINK &&
				ydirectenv.yaffs_strlen(newName, newNameIndex) > Guts_H.YAFFS_MAX_NAME_LENGTH)
			/* ENAMETOOLONG */
			return Guts_H.YAFFS_FAIL;

		if (obj != null && obj.renameAllowed) {

			/* Now do the handling for an existing target, if there is one */

			existingTarget = yaffs_FindObjectByName(newDir, newName, newNameIndex);
			if (existingTarget != null &&
					existingTarget.variantType == Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY &&
					!devextras.list_empty(existingTarget.variant.directoryVariant().children)) {
				/* There is a target that is a non-empty directory, so we fail */
				return Guts_H.YAFFS_FAIL;	/* EEXIST or ENOTEMPTY */
			} else if (existingTarget != null && existingTarget != obj) {
				/* Nuke the target first, using shadowing, 
				 * but only if it isn't the same object
				 */
				yaffs_ChangeObjectName(obj, newDir, newName, newNameIndex, force,
						existingTarget.objectId);
				yaffs_UnlinkObject(existingTarget);
			}

			return yaffs_ChangeObjectName(obj, newDir, newName, newNameIndex, true, 0);
		}
		return Guts_H.YAFFS_FAIL;
	}

	/*------------------------- Block Management and Page Allocation ----------------*/

	static boolean yaffs_InitialiseBlocks(yaffs_Device dev)	// XXX only done once?
	{
		int nBlocks = dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1;

		dev.subField3.allocationBlock = -1;	/* force it to get a new one */

		/* Todo we're assuming the malloc will pass. */
		dev.subField2.blockInfo = /*ydirectenv.YMALLOC(nBlocks * sizeof(yaffs_BlockInfo))*/ ydirectenv.YMALLOC_BLOCKINFO(nBlocks);
		if(!(dev.subField2.blockInfo != null)){
			// XXX makes no sense in Java
			throw new NotImplementedException();
//			dev.blockInfo = ydirectenv.YMALLOC_ALyportenv.T(nBlocks * sizeof(yaffs_BlockInfo));
//			dev.blockInfoAlt = 1;
		}
		else
			dev.subField2.blockInfoAlt = false;

		/* Set up dynamic blockinfo stuff. */
		dev.subField3.chunkBitmapStride = (dev.subField1.nChunksPerBlock + 7) / 8; /* round up bytes */
		dev.subField2.chunkBits = ydirectenv.YMALLOC(dev.subField3.chunkBitmapStride * nBlocks);
		dev.subField2.chunkBitsIndex = 0;
		if(!(dev.subField2.chunkBits != null)){
			throw new NotImplementedException();
//			dev.chunkBits = ydirectenv.YMALLOC_ALyportenv.T(dev.chunkBitmapStride * nBlocks);
//			dev.chunkBitsAlt = 1;
		}
		else
			dev.subField2.chunkBitsAlt = false;

		if ((dev.subField2.blockInfo != null) && (dev.subField2.chunkBits != null)) {
			// PORT already done on object creation
			// XXX not if not using pool
			Unix.memset(dev.subField2.blockInfo, (byte)0/*, nBlocks * sizeof(yaffs_BlockInfo)*/);
			Unix.memset(dev.subField2.chunkBits, dev.subField2.chunkBitsIndex, (byte)0, dev.subField3.chunkBitmapStride * nBlocks);
			return Guts_H.YAFFS_OK;
		}

		return Guts_H.YAFFS_FAIL;

	}

	static void yaffs_DeinitialiseBlocks(yaffs_Device dev) // XXX
	{
		if(dev.subField2.blockInfoAlt)
			ydirectenv.YFREE_ALT(dev.subField2.blockInfo);
		else
			ydirectenv.YFREE(dev.subField2.blockInfo);
		dev.subField2.blockInfoAlt = false;

		dev.subField2.blockInfo = null;

		if(dev.subField2.chunkBitsAlt)
			ydirectenv.YFREE_ALT(dev.subField2.chunkBits);
		else
			ydirectenv.YFREE(dev.subField2.chunkBits);
		dev.subField2.chunkBitsAlt = false;
		dev.subField2.chunkBits = null;
	}

	static boolean yaffs_BlockNotDisqualifiedFromGC(yaffs_Device dev,
			yaffs_BlockInfo bi)
	{
		int i;
		/*__u32*/ long seq;
		yaffs_BlockInfo b;

		if (!dev.subField1.isYaffs2)
			return true;	/* disqualification only applies to yaffs2. */

		if (!bi.hasShrinkHeader()) 
			return true;	/* can gc */

		/* Find the oldest dirty sequence number if we don't know it and save it
		 * so we don't have to keep recomputing it.
		 */
		if (!(dev.oldestDirtySequence != 0)) {
			seq = dev.sequenceNumber;

			for (i = dev.subField2.internalStartBlock; i <= dev.subField2.internalEndBlock;
			i++) {
				b = Guts_H.yaffs_GetBlockInfo(dev, i);
				if (b.blockState() == Guts_H.YAFFS_BLOCK_STATE_FULL &&
						(b.pagesInUse() - b.softDeletions()) <
						dev.subField1.nChunksPerBlock && Utils.intAsUnsignedInt(b.sequenceNumber()) < seq) {
					seq = Utils.intAsUnsignedInt(b.sequenceNumber());
				}
			}
			dev.oldestDirtySequence = seq;
		}

		/* Can't do gc of this block if there are any blocks older than this one that have
		 * discarded pages.
		 */
		return (Utils.intAsUnsignedInt(bi.sequenceNumber()) <= dev.oldestDirtySequence);

	}

	/* FindDiretiestBlock is used to select the dirtiest block (or close enough)
	 * for garbage collection.
	 */

	static int _STATIC_LOCAL_yaffs_FindBlockForGarbageCollection_nonAggressiveSkip = 0;
	static int yaffs_FindBlockForGarbageCollection(yaffs_Device dev,
			boolean aggressive)
	{

		int b = dev.subField3.currentDirtyChecker;

		int i;
		int iterations;
		int dirtiest = -1;
		int pagesInUse = 0; // PORT Compiler is complaining that variable may not have been initialized.
		boolean prioritised=false;
		yaffs_BlockInfo bi;
		//static int nonAggressiveSkip = 0;
		boolean pendingPrioritisedExist = false;

		/* First let's see if we need to grab a prioritised block */
		if(dev.hasPendingPrioritisedGCs){
			for(i = dev.subField2.internalStartBlock; i < dev.subField2.internalEndBlock && !prioritised; i++){

				bi = Guts_H.yaffs_GetBlockInfo(dev, i);
				if(bi.gcPrioritise()) {
					pendingPrioritisedExist = true;
					if(bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_FULL &&
							yaffs_BlockNotDisqualifiedFromGC(dev, bi)){
						pagesInUse = (bi.pagesInUse() - bi.softDeletions());
						dirtiest = i;
						prioritised = true;
						aggressive = true; /* Fool the non-aggressive skip logiv below */
					}
				}
			}

			if(!pendingPrioritisedExist) /* None found, so we can clear this */
				dev.hasPendingPrioritisedGCs = false;
		}

		/* If we're doing aggressive GC then we are happy to take a less-dirty block, and
		 * search harder.
		 * else (we're doing a leasurely gc), then we only bother to do this if the
		 * block has only a few pages in use.
		 */

		_STATIC_LOCAL_yaffs_FindBlockForGarbageCollection_nonAggressiveSkip--;

		if (!aggressive && (_STATIC_LOCAL_yaffs_FindBlockForGarbageCollection_nonAggressiveSkip > 0)) {
			return -1;
		}

		if(!prioritised)
			pagesInUse =
				(aggressive) ? dev.subField1.nChunksPerBlock : YAFFS_PASSIVE_GC_CHUNKS + 1;

		if (aggressive) {
			iterations =
				dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1;
		} else {
			iterations =
				dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1;
			iterations = iterations / 16;
			if (iterations > 200) {
				iterations = 200;
			}
		}

		for (i = 0; i <= iterations && pagesInUse > 0 && !prioritised; i++) {
			b++;
			if (b < dev.subField2.internalStartBlock || b > dev.subField2.internalEndBlock) {
				b = dev.subField2.internalStartBlock;
			}

			if (b < dev.subField2.internalStartBlock || b > dev.subField2.internalEndBlock) {
				yportenv.T(yportenv.YAFFS_TRACE_ERROR,
						("**>> Block %d is not valid" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(b));
				yaffs2.utils.Globals.portConfiguration.YBUG();
			}

			bi = Guts_H.yaffs_GetBlockInfo(dev, b);

//			#if 0
//			if (bi.blockState == YAFFS_BLOCK_STATE_CHECKPOINT) {
//			dirtiest = b;
//			pagesInUse = 0;
//			}
//			else 
//			#endif

			if (bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_FULL &&
					(bi.pagesInUse() - bi.softDeletions()) < pagesInUse &&
					(yaffs_BlockNotDisqualifiedFromGC(dev, bi))) {
				dirtiest = b;
				pagesInUse = (bi.pagesInUse() - bi.softDeletions());
			}
		}

		dev.subField3.currentDirtyChecker = b;

		if (dirtiest > 0) {
			yportenv.T(yportenv.YAFFS_TRACE_GC,
					("GC Selected block %d with %d free, prioritised:%b" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(dirtiest),
					PrimitiveWrapperFactory.get(dev.subField1.nChunksPerBlock - pagesInUse),PrimitiveWrapperFactory.get(prioritised));
		}

		dev.oldestDirtySequence = 0;

		if (dirtiest > 0) {
			_STATIC_LOCAL_yaffs_FindBlockForGarbageCollection_nonAggressiveSkip = 4;
		}

		return dirtiest;
	}

	static void yaffs_BlockBecameDirty(yaffs_Device dev, int blockNo)
	{
		yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev, blockNo);

		boolean erasedOk = false;

		/* If the block is still healthy erase it and mark as clean.
		 * If the block has had a data failure, then retire it.
		 */

		yportenv.T(yportenv.YAFFS_TRACE_GC | yportenv.YAFFS_TRACE_ERASE,
				("yaffs_BlockBecameDirty block %d state %d %s"+ ydirectenv.TENDSTR),
				PrimitiveWrapperFactory.get(blockNo), PrimitiveWrapperFactory.get(bi.blockState()), PrimitiveWrapperFactory.get((bi.needsRetiring()) ? "needs retiring" : ""));

		bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_DIRTY);

		if (!bi.needsRetiring()) {
			yaffs_InvalidateCheckpoint(dev);
			erasedOk = yaffs_nand_C.yaffs_EraseBlockInNAND(dev, blockNo);
			if (!erasedOk) {
				dev.subField3.nErasureFailures++;
				yportenv.T(yportenv.YAFFS_TRACE_ERROR | yportenv.YAFFS_TRACE_BAD_BLOCKS,
						("**>> Erasure failed %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blockNo));
			}
		}

		if (erasedOk && ((yaffs2.utils.Globals.yaffs_traceMask & yportenv.YAFFS_TRACE_ERASE) != 0)) {
			int i;
			for (i = 0; i < dev.subField1.nChunksPerBlock; i++) {
				if (!yaffs_CheckChunkErased
						(dev, blockNo * dev.subField1.nChunksPerBlock + i)) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,

							(">>Block %d erasure supposedly OK, but chunk %d not erased"
									+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blockNo), PrimitiveWrapperFactory.get(i));
				}
			}
		}

		if (erasedOk) {
			/* Clean it up... */
			bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_EMPTY);
			dev.subField3.nErasedBlocks++;
			bi.setPagesInUse(0);
			bi.setSoftDeletions(0);
			bi.setHasShrinkHeader(false);
			bi.setSkipErasedCheck(true);  /* This is clean, so no need to check */
			bi.setGcPrioritise(false);
			yaffs_ClearChunkBits(dev, blockNo);

			yportenv.T(yportenv.YAFFS_TRACE_ERASE,
					("Erased block %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blockNo));
		} else {
			dev.subField3.nFreeChunks -= dev.subField1.nChunksPerBlock;	/* We lost a block of free space */

			yaffs_RetireBlock(dev, blockNo);
			yportenv.T(yportenv.YAFFS_TRACE_ERROR | yportenv.YAFFS_TRACE_BAD_BLOCKS,
					("**>> Block %d retired" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blockNo));
		}
	}

	static int yaffs_FindBlockForAllocation(yaffs_Device dev)
	{
		int i;

		yaffs_BlockInfo bi;

		if (dev.subField3.nErasedBlocks < 1) {
			/* Hoosterman we've got a problem.
			 * Can't get space to gc
			 */
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					(("yaffs tragedy: no more eraased blocks" + ydirectenv.TENDSTR)));

			return -1;
		}

		/* Find an empty block. */

		for (i = dev.subField2.internalStartBlock; i <= dev.subField2.internalEndBlock; i++) {
			dev.subField3.allocationBlockFinder++;
			if (dev.subField3.allocationBlockFinder < dev.subField2.internalStartBlock
					|| dev.subField3.allocationBlockFinder > dev.subField2.internalEndBlock) {
				dev.subField3.allocationBlockFinder = dev.subField2.internalStartBlock;
			}

			bi = Guts_H.yaffs_GetBlockInfo(dev, dev.subField3.allocationBlockFinder);

			if (bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_EMPTY) {
				bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_ALLOCATING);
				dev.sequenceNumber++;
				bi.setSequenceNumber((int)dev.sequenceNumber);
				dev.subField3.nErasedBlocks--;
				yportenv.T(yportenv.YAFFS_TRACE_ALLOCATE,
						("Allocated block %d, seq  %d, %d left" + ydirectenv.TENDSTR),
						PrimitiveWrapperFactory.get(dev.subField3.allocationBlockFinder), PrimitiveWrapperFactory.get((int)dev.sequenceNumber),
						PrimitiveWrapperFactory.get(dev.subField3.nErasedBlocks));
				return dev.subField3.allocationBlockFinder;
			}
		}

		yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,

				("yaffs tragedy: no more eraased blocks, but there should have been %d"
						+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(dev.subField3.nErasedBlocks));

		return -1;
	}


//	Check if there's space to allocate...
//	Thinks.... do we need top make this ths same as yaffs_GetFreeChunks()?
	static boolean yaffs_CheckSpaceForAllocation(yaffs_Device dev)
	{
		int reservedChunks;
		int reservedBlocks = dev.subField1.nReservedBlocks;
		int checkpointBlocks;

		checkpointBlocks =  dev.subField1.nCheckpointReservedBlocks - dev.subField2.blocksInCheckpoint;
		if(checkpointBlocks < 0)
			checkpointBlocks = 0;

		reservedChunks = ((reservedBlocks + checkpointBlocks) * dev.subField1.nChunksPerBlock);

		return (dev.subField3.nFreeChunks > reservedChunks);
	}

	/**
	 * 
	 * @param dev
	 * @param useReserve
	 * @param blockUsedPtr Out
	 * @return
	 */
	static int yaffs_AllocateChunk(yaffs_Device dev, boolean useReserve, yaffs_BlockInfoPointer blockUsedPtr)
	{
		int retVal;
		yaffs_BlockInfo bi;

		if (dev.subField3.allocationBlock < 0) {
			/* Get next block to allocate off */
			dev.subField3.allocationBlock = yaffs_FindBlockForAllocation(dev);
			dev.subField3.allocationPage = 0;
		}

		if (!useReserve && !yaffs_CheckSpaceForAllocation(dev)) {
			/* Not enough space to allocate unless we're allowed to use the reserve. */
			return -1;
		}

		if (dev.subField3.nErasedBlocks < dev.subField1.nReservedBlocks
				&& dev.subField3.allocationPage == 0) {
			yportenv.T(yportenv.YAFFS_TRACE_ALLOCATE, (("Allocating reserve" + ydirectenv.TENDSTR)));
		}

		/* Next page please.... */
		if (dev.subField3.allocationBlock >= 0) {
			bi = Guts_H.yaffs_GetBlockInfo(dev, dev.subField3.allocationBlock);

			retVal = (dev.subField3.allocationBlock * dev.subField1.nChunksPerBlock) +
			dev.subField3.allocationPage;
			bi.setPagesInUse(bi.pagesInUse()+ 1);
			yaffs_SetChunkBit(dev, dev.subField3.allocationBlock,
					dev.subField3.allocationPage);

			dev.subField3.allocationPage++;

			dev.subField3.nFreeChunks--;

			/* If the block is full set the state to full */
			if (dev.subField3.allocationPage >= dev.subField1.nChunksPerBlock) {
				bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_FULL);
				dev.subField3.allocationBlock = -1;
			}

			if(blockUsedPtr != null)
				blockUsedPtr.dereferenced = bi;

			return retVal;
		}

		yportenv.T(yportenv.YAFFS_TRACE_ERROR,
				("!!!!!!!!! Allocator out !!!!!!!!!!!!!!!!!" + ydirectenv.TENDSTR));

		return -1;
	}

	static int yaffs_GetErasedChunks(yaffs_Device dev)
	{
		int n;

		n = dev.subField3.nErasedBlocks * dev.subField1.nChunksPerBlock;

		if (dev.subField3.allocationBlock > 0) {
			n += (dev.subField1.nChunksPerBlock - dev.subField3.allocationPage);
		}

		return n;

	}

	static boolean yaffs_GarbageCollectBlock(yaffs_Device dev, int block)
	{
		int oldChunk;
		int newChunk;
		int chunkInBlock;
		boolean markNAND;
		boolean retVal = Guts_H.YAFFS_OK;
		int cleanups = 0;
		int i;
		boolean isCheckpointBlock;

		int chunksBefore = yaffs_GetErasedChunks(dev);
		int chunksAfter;

		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();

		yaffs_BlockInfo bi = Guts_H.yaffs_GetBlockInfo(dev, block);

		yaffs_Object object;

		isCheckpointBlock = (bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_CHECKPOINT);

		bi.setBlockState(Guts_H.YAFFS_BLOCK_STATE_COLLECTING);

		yportenv.T(yportenv.YAFFS_TRACE_TRACING,
				("Collecting block %d, in use %d, shrink %b, " + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(block),
				PrimitiveWrapperFactory.get(bi.pagesInUse()), PrimitiveWrapperFactory.get(bi.hasShrinkHeader()));

		/*yaffs_VerifyFreeChunks(dev); */

		bi.setHasShrinkHeader(false);	/* clear the flag so that the block can erase */

		/* Take off the number of soft deleted entries because
		 * they're going to get really deleted during GC.
		 */
		dev.subField3.nFreeChunks -= bi.softDeletions();

		dev.subField3.isDoingGC = true;

		if (isCheckpointBlock ||
				!yaffs_StillSomeChunkBits(dev, block)) {
			yportenv.T(yportenv.YAFFS_TRACE_TRACING,

					("Collecting block %d that has no chunks in use" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(block));
			yaffs_BlockBecameDirty(dev, block);
		} else {

			byte[] buffer = yaffs_GetTempBuffer(dev, 6 /*Utils.__LINE__()*/);

			for (chunkInBlock = 0, oldChunk = block * dev.subField1.nChunksPerBlock;
			chunkInBlock < dev.subField1.nChunksPerBlock
			&& yaffs_StillSomeChunkBits(dev, block);
			chunkInBlock++, oldChunk++) {
				if (yaffs_CheckChunkBit(dev, block, chunkInBlock)) {

					/* This page is in use and might need to be copied off */

					markNAND = true;

					yaffs_tagsvalidity_C.yaffs_InitialiseTags(tags);

					yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, oldChunk,
							buffer, 0, tags);

					object =
						yaffs_FindObjectByNumber(dev,
								tags.objectId);

					yportenv.T(yportenv.YAFFS_TRACE_GC_DETAIL,

							("Collecting page %d, %d %d %d " + ydirectenv.TENDSTR),
							PrimitiveWrapperFactory.get(chunkInBlock), PrimitiveWrapperFactory.get(tags.objectId), PrimitiveWrapperFactory.get(tags.chunkId),
							PrimitiveWrapperFactory.get(tags.byteCount));

					if (!(object != null)) {
						yportenv.T(yportenv.YAFFS_TRACE_ERROR,

								("page %d in gc has no object "
										+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(oldChunk));
					}

					if (object != null && object.sub.deleted
							&& tags.chunkId != 0) {
						/* Data chunk in a deleted file, throw it away
						 * It's a soft deleted data chunk,
						 * No need to copy this, just forget about it and 
						 * fix up the object.
						 */

						object.nDataChunks--;

						if (object.nDataChunks <= 0) {
							/* remeber to clean up the object */
							dev.subField3.gcCleanupList[cleanups] =
								tags.objectId;
							cleanups++;
						}
						markNAND = false;
					} else if (false
							/* Todo object && object.deleted && object.nDataChunks == 0 */
					) {
						/* Deleted object header with no data chunks.
						 * Can be discarded and the file deleted.
						 */
						object.chunkId = 0;
						yaffs_FreeTnode(object.myDev,
								object.variant.
								fileVariant().top);
						object.variant.fileVariant().top = null;
						yaffs_DoGenericObjectDeletion(object);

					} else if (object != null) {
						/* It's either a data chunk in a live file or
						 * an ObjectHeader, so we're interested in it.
						 * NB Need to keep the ObjectHeaders of deleted files
						 * until the whole file has been deleted off
						 */
						tags.serialNumber++;

						dev.subField3.nGCCopies++;

						if (tags.chunkId == 0) {
							/* It is an object Id,
							 * We need to nuke the shrinkheader flags first
							 * We no longer want the shrinkHeader flag since its work is done
							 * and if it is left in place it will mess up scanning.
							 * Also, clear out any shadowing stuff
							 */

							yaffs_ObjectHeader oh;
							oh = /*(yaffs_ObjectHeader)buffer*/ new yaffs_ObjectHeader(buffer, 0);
							oh.setIsShrink(false);
							oh.setShadowsObject(-1);
							tags.extraShadows = false;
							tags.extraIsShrinkHeader = false;
						}

						newChunk =
							yaffs_WriteNewChunkWithTagsToNAND(dev, buffer, 0, tags, true);

						if (newChunk < 0) {
							retVal = Guts_H.YAFFS_FAIL;
						} else {

							/* Ok, now fix up the Tnodes etc. */

							if (tags.chunkId == 0) {
								/* It's a header */
								object.chunkId =  newChunk;
								object.serial = (byte)tags.serialNumber;
							} else {
								/* It's a data chunk */
								yaffs_PutChunkIntoFile
								(object,
										tags.chunkId,
										newChunk, 0);
							}
						}
					}

					yaffs_DeleteChunk(dev, oldChunk, markNAND, 7 /*Utils.__LINE__()*/);

				}
			}

			yaffs_ReleaseTempBuffer(dev, buffer, 8 /*Utils.__LINE__()*/);


			/* Do any required cleanups */
			for (i = 0; i < cleanups; i++) {
				/* Time to delete the file too */
				object =
					yaffs_FindObjectByNumber(dev,
							dev.subField3.gcCleanupList[i]);
				if (object != null) {
					yaffs_FreeTnode(dev,
							object.variant.fileVariant().
							top);
					object.variant.fileVariant().top = null;
					yportenv.T(yportenv.YAFFS_TRACE_GC,

							("yaffs: About to finally delete object %d"
									+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(object.objectId));
					yaffs_DoGenericObjectDeletion(object);
					object.myDev.nDeletedFiles--;
				}

			}

		}

		if (chunksBefore >= (chunksAfter = yaffs_GetErasedChunks(dev))) {
			yportenv.T(yportenv.YAFFS_TRACE_GC,
					("gc did not increase free chunks before %d after %d"
							+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunksBefore), PrimitiveWrapperFactory.get(chunksAfter));
		}

		dev.subField3.isDoingGC = false;

		return Guts_H.YAFFS_OK;
	}

	/* New garbage collector
	 * If we're very low on erased blocks then we do aggressive garbage collection
	 * otherwise we do "leasurely" garbage collection.
	 * Aggressive gc looks further (whole array) and will accept less dirty blocks.
	 * Passive gc only inspects smaller areas and will only accept more dirty blocks.
	 *
	 * The idea is to help clear out space in a more spread-out manner.
	 * Dunno if it really does anything useful.
	 */
	static boolean yaffs_CheckGarbageCollection(yaffs_Device dev)
	{
		int block;
		boolean aggressive;
		boolean gcOk = Guts_H.YAFFS_OK;
		int maxTries = 0;

		int checkpointBlockAdjust;

		if (dev.subField3.isDoingGC) {
			/* Bail out so we don't get recursive gc */
			return Guts_H.YAFFS_OK;
		}

		/* This loop should pass the first time.
		 * We'll only see looping here if the erase of the collected block fails.
		 */

		do {
			maxTries++;

			checkpointBlockAdjust = (dev.subField1.nCheckpointReservedBlocks - dev.subField2.blocksInCheckpoint);
			if(checkpointBlockAdjust < 0)
				checkpointBlockAdjust = 0;

			if (dev.subField3.nErasedBlocks < (dev.subField1.nReservedBlocks + checkpointBlockAdjust)) {
				/* We need a block soon...*/
				aggressive = true;
			} else {
				/* We're in no hurry */
				aggressive = false;
			}

			block = yaffs_FindBlockForGarbageCollection(dev, aggressive);

			if (block > 0) {
				dev.subField3.garbageCollections++;
				if (!aggressive) {
					dev.subField3.passiveGarbageCollections++;
				}

				yportenv.T(yportenv.YAFFS_TRACE_GC,

						("yaffs: GC erasedBlocks %d aggressive %b" + ydirectenv.TENDSTR),
						PrimitiveWrapperFactory.get(dev.subField3.nErasedBlocks), PrimitiveWrapperFactory.get(aggressive));

				gcOk = yaffs_GarbageCollectBlock(dev, block);
			}

			if (dev.subField3.nErasedBlocks < (dev.subField1.nReservedBlocks) && block > 0) {
				yportenv.T(yportenv.YAFFS_TRACE_GC,

						("yaffs: GC !!!no reclaim!!! erasedBlocks %d after try %d block %d"
								+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(dev.subField3.nErasedBlocks), PrimitiveWrapperFactory.get(maxTries), PrimitiveWrapperFactory.get(block));
			}
		} while ((dev.subField3.nErasedBlocks < dev.subField1.nReservedBlocks) && (block > 0)
				&& (maxTries < 2));

		return aggressive ? gcOk : Guts_H.YAFFS_OK;
	}

	/*-------------------------  TAGS --------------------------------*/

	static boolean yaffs_TagsMatch(yaffs_ExtendedTags tags, int objectId,
			int chunkInObject)
	{
		return (tags.chunkId == chunkInObject &&
				tags.objectId == objectId && !tags.chunkDeleted) ? true : false;

	}


	/*-------------------- Data file manipulation -----------------*/

	static int yaffs_FindChunkInFile(yaffs_Object  in, int chunkInInode,
			yaffs_ExtendedTags tags)
	{
		/*Get the Tnode, then get the level 0 offset chunk offset */
		yaffs_Tnode tn;
		int theChunk = -1;
		yaffs_ExtendedTags localTags = new yaffs_ExtendedTags();
		int retVal = -1;

		yaffs_Device dev = in.myDev;

		if (!(tags != null)) {
			/* Passed a null, so use our own tags space */
			tags = localTags;
		}

		tn = yaffs_FindLevel0Tnode(dev, in.variant.fileVariant(), chunkInInode);

		if (tn != null) {
			theChunk = yaffs_GetChunkGroupBase(dev,tn,chunkInInode);

			retVal =
				yaffs_FindChunkInGroup(dev, theChunk, tags, in.objectId,
						chunkInInode);
		}
		return retVal;
	}

	static int yaffs_FindAndDeleteChunkInFile(yaffs_Object  in, int chunkInInode,
			yaffs_ExtendedTags tags)
	{
		/* Get the Tnode, then get the level 0 offset chunk offset */
		yaffs_Tnode tn;
		int theChunk = -1;
		yaffs_ExtendedTags localTags = new yaffs_ExtendedTags();

		yaffs_Device dev = in.myDev;
		int retVal = -1;

		if (!(tags != null)) {
			/* Passed a null, so use our own tags space */
			tags = localTags;
		}

		tn = yaffs_FindLevel0Tnode(dev, in.variant.fileVariant(), chunkInInode);

		if ((tn != null)) {

			theChunk = yaffs_GetChunkGroupBase(dev,tn,chunkInInode);

			retVal =
				yaffs_FindChunkInGroup(dev, theChunk, tags, in.objectId,
						chunkInInode);

			/* Delete the entry in the filestructure (if found) */
			if (retVal != -1) {
				yaffs_PutLevel0Tnode(dev,tn,chunkInInode,0);
			}
		} else {
			/*yportenv.T(("No level 0 found for %d\n", chunkInInode)); */
		}

		if (retVal == -1) {
			/* yportenv.T(("Could not find %d to delete\n",chunkInInode)); */
		}
		return retVal;
	}


//	dev is not defined
//	#ifdef YAFFS_PARANOID

//	static boolean yaffs_CheckFileSanity(yaffs_Object  in)
//	{
//	int chunk;
//	int nChunks;
//	int fSize;
//	boolean failed = false;
//	int objId;
//	yaffs_Tnode tn;
//	yaffs_Tags localTags = new yaffs_Tags();
//	yaffs_Tags tags = localTags;
//	int theChunk;
//	boolean chunkDeleted;

//	if (in.variantType != Guts_H.YAFFS_OBJECT_TYPE_FILE) {
//	/* yportenv.T(("Object not a file\n")); */
//	return Guts_H.YAFFS_FAIL;
//	}

//	objId = in.objectId;
//	fSize = in.variant.fileVariant().fileSize;
//	nChunks =
//	(fSize + in.myDev.nDataBytesPerChunk - 1) / in.myDev.nDataBytesPerChunk;

//	for (chunk = 1; chunk <= nChunks; chunk++) {
//	tn = yaffs_FindLevel0Tnode(in.myDev, in.variant.fileVariant(),
//	chunk);

//	if (tn != null) {

//	theChunk = yaffs_GetChunkGroupBase(dev,tn,chunk);

//	if (yaffs_CheckChunkBits
//	(dev, theChunk / dev.nChunksPerBlock,
//	theChunk % dev.nChunksPerBlock)) {

//	yaffs_ReadChunkTagsFromNAND(in.myDev, theChunk,
//	tags,
//	chunkDeleted);
//	if (yaffs_TagsMatch
//	(tags, in.objectId, chunk, chunkDeleted)) {
//	/* found it; */

//	}
//	} else {

//	failed = true;
//	}

//	} else {
//	/* yportenv.T(("No level 0 found for %d\n", chunk)); */
//	}
//	}

//	return failed ? Guts_H.YAFFS_FAIL : Guts_H.YAFFS_OK;
//	}

//	#endif

	static boolean yaffs_PutChunkIntoFile(yaffs_Object  in, int chunkInInode,
			int chunkInNAND, int inScan)
	{
		/* NB inScan is zero unless scanning. 
		 * For forward scanning, inScan is > 0; 
		 * for backward scanning inScan is < 0
		 */

		yaffs_Tnode tn;
		yaffs_Device dev = in.myDev;
		int existingChunk;
		yaffs_ExtendedTags existingTags = new yaffs_ExtendedTags();
		yaffs_ExtendedTags newTags = new yaffs_ExtendedTags();
		/*unsigned*/ int existingSerial, newSerial;

		if (in.variantType != Guts_H.YAFFS_OBJECT_TYPE_FILE) {
			/* Just ignore an attempt at putting a chunk into a non-file during scanning
			 * If it is not during Scanning then something went wrong!
			 */
			if (!(inScan != 0)) {
				yportenv.T(yportenv.YAFFS_TRACE_ERROR,

						("yaffs tragedy:attempt to put data chunk into a non-file"
								+ ydirectenv.TENDSTR));
				yaffs2.utils.Globals.portConfiguration.YBUG();
			}

			yaffs_DeleteChunk(dev, chunkInNAND, true, 9 /*Utils.__LINE__()*/);
			return Guts_H.YAFFS_OK;
		}

		tn = yaffs_AddOrFindLevel0Tnode(dev, 
				in.variant.fileVariant(),
				chunkInInode,
				null);
		if (!(tn != null)) {
			return Guts_H.YAFFS_FAIL;
		}

		existingChunk = yaffs_GetChunkGroupBase(dev,tn,chunkInInode);

		if (inScan != 0) {
			/* If we're scanning then we need to test for duplicates
			 * NB This does not need to be efficient since it should only ever 
			 * happen when the power fails during a write, then only one
			 * chunk should ever be affected.
			 *
			 * Correction for YAFFS2: This could happen quite a lot and we need to think about efficiency! TODO
			 * Update: For backward scanning we don't need to re-read tags so this is quite cheap.
			 */

			if (existingChunk != 0) {
				/* NB Right now existing chunk will not be real chunkId if the device >= 32MB
				 *    thus we have to do a FindChunkInFile to get the real chunk id.
				 *
				 * We have a duplicate now we need to decide which one to use:
				 *
				 * Backwards scanning YAFFS2: The old one is what we use, dump the new one.
				 * Forward scanning YAFFS2: The new one is what we use, dump the old one.
				 * YAFFS1: Get both sets of tags and compare serial numbers.
				 */

				if (inScan > 0) {
					/* Only do this for forward scanning */
					yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev,
							chunkInNAND,
							null, 0, newTags);

					/* Do a proper find */
					existingChunk =
						yaffs_FindChunkInFile(in, chunkInInode,
								existingTags);
				}

				if (existingChunk <= 0) {
					/*Hoosterman - how did this happen? */

					yportenv.T(yportenv.YAFFS_TRACE_ERROR,

							("yaffs tragedy: existing chunk < 0 in scan"
									+ ydirectenv.TENDSTR));

				}

				/* NB The deleted flags should be false, otherwise the chunks will 
				 * not be loaded during a scan
				 */

				newSerial = newTags.serialNumber;
				existingSerial = existingTags.serialNumber;

				if ((inScan > 0) &&
						(in.myDev.subField1.isYaffs2 ||
								existingChunk <= 0 ||
								((existingSerial + 1) & 3) == newSerial)) {
					/* Forward scanning.                            
					 * Use new
					 * Delete the old one and drop through to update the tnode
					 */
					yaffs_DeleteChunk(dev, existingChunk, true,
							10 /*Utils.__LINE__()*/);
				} else {
					/* Backward scanning or we want to use the existing one
					 * Use existing.
					 * Delete the new one and return early so that the tnode isn't changed
					 */
					yaffs_DeleteChunk(dev, chunkInNAND, true,
							11 /*Utils.__LINE__()*/);
					return Guts_H.YAFFS_OK;
				}
			}

		}

		if (existingChunk == 0) {
			in.nDataChunks++;
		}

		yaffs_PutLevel0Tnode(dev,tn,chunkInInode,chunkInNAND);

		return Guts_H.YAFFS_OK;
	}

	static boolean yaffs_ReadChunkDataFromObject(yaffs_Object  in, int chunkInInode,
			byte[] buffer, int bufferIndex)
	{
		int chunkInNAND = yaffs_FindChunkInFile(in, chunkInInode, null);

		if (chunkInNAND >= 0) {
			return yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(in.myDev, chunkInNAND,
					buffer,bufferIndex,null);
		} else {
			yportenv.T(yportenv.YAFFS_TRACE_NANDACCESS,
					("Chunk %d not found zero instead" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(chunkInNAND));
			/* get sane (zero) data if you read a hole */
			Unix.memset(buffer, bufferIndex, (byte)0, in.myDev.subField1.nDataBytesPerChunk);	
			return false;
		}

	}

	static void yaffs_DeleteChunk(yaffs_Device dev, int chunkId, boolean markNAND, int lyn)
	{
		int block;
		int page;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		yaffs_BlockInfo bi;

		if (chunkId <= 0)
			return;

		dev.nDeletions++;
		block = chunkId / dev.subField1.nChunksPerBlock;
		page = chunkId % dev.subField1.nChunksPerBlock;

		bi = Guts_H.yaffs_GetBlockInfo(dev, block);

		yportenv.T(yportenv.YAFFS_TRACE_DELETION,
				("line %d delete of chunk %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(lyn), PrimitiveWrapperFactory.get(chunkId));

		if (markNAND &&
				bi.blockState() != Guts_H.YAFFS_BLOCK_STATE_COLLECTING && !dev.subField1.isYaffs2) {

			yaffs_tagsvalidity_C.yaffs_InitialiseTags(tags);

			tags.chunkDeleted = true;

			yaffs_nand_C.yaffs_WriteChunkWithTagsToNAND(dev, chunkId, null, 0, tags);
			yaffs_HandleUpdateChunk(dev, chunkId, tags);
		} else {
			dev.nUnmarkedDeletions++;
		}

		/* Pull out of the management area.
		 * If the whole block became dirty, this will kick off an erasure.
		 */
		if (bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_ALLOCATING ||
				bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_FULL ||
				bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING ||
				bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_COLLECTING) {
			dev.subField3.nFreeChunks++;

			yaffs_ClearChunkBit(dev, block, page);

			bi.setPagesInUse(bi.pagesInUse()-1);

			if (bi.pagesInUse() == 0 &&
					!bi.hasShrinkHeader() &&
					bi.blockState() != Guts_H.YAFFS_BLOCK_STATE_ALLOCATING &&
					bi.blockState() != Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING) {
				yaffs_BlockBecameDirty(dev, block);
			}

		} else {
			/* yportenv.T(("Bad news deleting chunk %d\n",chunkId)); */
		}

	}

	static int yaffs_WriteChunkDataToObject(yaffs_Object  in, int chunkInInode,
			byte[] buffer, int bufferIndex, int nBytes,
			boolean useReserve)
	{
		/* Find old chunk Need to do this to get serial number
		 * Write new one and patch into tree.
		 * Invalidate old tags.
		 */

		int prevChunkId;
		yaffs_ExtendedTags prevTags = new yaffs_ExtendedTags();

		int newChunkId;
		yaffs_ExtendedTags newTags = new yaffs_ExtendedTags();

		yaffs_Device dev = in.myDev;

		yaffs_CheckGarbageCollection(dev);

		/* Get the previous chunk at this location in the file if it exists */
		prevChunkId = yaffs_FindChunkInFile(in, chunkInInode, prevTags);

		/* Set up new tags */
		yaffs_tagsvalidity_C.yaffs_InitialiseTags(newTags);

		newTags.chunkId = chunkInInode;
		newTags.objectId = in.objectId;
		newTags.serialNumber =
			((prevChunkId >= 0) ? prevTags.serialNumber + 1 : 1);
		newTags.byteCount = nBytes;

		newChunkId =
			yaffs_WriteNewChunkWithTagsToNAND(dev, buffer, bufferIndex, newTags,
					useReserve);

		if (newChunkId >= 0) {
			yaffs_PutChunkIntoFile(in, chunkInInode, newChunkId, 0);

			if (prevChunkId >= 0) {
				yaffs_DeleteChunk(dev, prevChunkId, true, 12 /*Utils.__LINE__()*/);

			}

			yaffs_CheckFileSanity(in);
		}
		return newChunkId;

	}

	/* UpdateObjectHeader updates the header on NAND for an object.
	 * If name is not null, then that new name is used.
	 */
	static int yaffs_UpdateObjectHeader(yaffs_Object  in, byte[] name, int nameIndex, 
			boolean force, boolean isShrink, int shadows)
	{

		yaffs_BlockInfo bi;

		yaffs_Device dev = in.myDev;

		int prevChunkId;
		int retVal = 0;
		boolean result = false;

		int newChunkId;
		yaffs_ExtendedTags newTags = new yaffs_ExtendedTags();

		byte[] buffer = null; final int bufferIndex = 0;
		byte[] oldName = new byte[Guts_H.YAFFS_MAX_NAME_LENGTH + 1]; final int oldNameIndex = 0;

		yaffs_ObjectHeader oh = null;

		if (!in.sub.fake || force) {

			yaffs_CheckGarbageCollection(dev);

			buffer = yaffs_GetTempBuffer(in.myDev, 13 /*Utils.__LINE__()*/);
			oh = /*(yaffs_ObjectHeader *) buffer*/ new yaffs_ObjectHeader(buffer,0);

			prevChunkId = in.chunkId;

			if (prevChunkId >= 0) {
				result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, prevChunkId,
						buffer, bufferIndex, null);
				Unix.memcpy(oldName, oldNameIndex, oh.name(), oh.nameIndex(), (yaffs_ObjectHeader.SIZEOF_name));
			}

			Unix.memset(buffer, bufferIndex, (byte)0xFF, dev.subField1.nDataBytesPerChunk);

			oh.setType(in.variantType);
			oh.setYst_mode(in.yst_mode);
			oh.setShadowsObject(shadows);

//			#ifdef CONFIG_YAFFS_WINCE
//			oh.win_atime[0] = in.win_atime[0];
//			oh.win_ctime[0] = in.win_ctime[0];
//			oh.win_mtime[0] = in.win_mtime[0];
//			oh.win_atime[1] = in.win_atime[1];
//			oh.win_ctime[1] = in.win_ctime[1];
//			oh.win_mtime[1] = in.win_mtime[1];
//			#else
			oh.setYst_uid(in.yst_uid);
			oh.setYst_gid(in.yst_gid);
			oh.setYst_atime(in.yst_atime);
			oh.setYst_mtime(in.yst_mtime);
			oh.setYst_ctime(in.yst_ctime);
			oh.setYst_rdev(in.yst_rdev);
//			#endif
			if (in.parent != null) {
				oh.setParentObjectId(in.parent.objectId);
			} else {
				oh.setParentObjectId(0);
			}

			if (name != null && name[nameIndex] != 0) {
				Unix.memset(oh.name(), oh.nameIndex(), (byte)0, yaffs_ObjectHeader.SIZEOF_name);
				ydirectenv.yaffs_strncpy(oh.name(), oh.nameIndex(), name, nameIndex, Guts_H.YAFFS_MAX_NAME_LENGTH);
			} else if (prevChunkId != 0) {
				Unix.memcpy(oh.name(), oh.nameIndex(), oldName, oldNameIndex, yaffs_ObjectHeader.SIZEOF_name);
			} else {
				Unix.memset(oh.name(), oh.nameIndex(), (byte)0, yaffs_ObjectHeader.SIZEOF_name);
			}

			oh.setIsShrink(isShrink);

			switch (in.variantType) {
			case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:
				/* Should not happen */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_FILE:
				oh.setFileSize(
						(oh.parentObjectId() == Guts_H.YAFFS_OBJECTID_DELETED
								|| oh.parentObjectId() ==
									Guts_H.YAFFS_OBJECTID_UNLINKED) ? 0 : in.variant.
											fileVariant().fileSize);
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
				oh.setEquivalentObjectId(
						in.variant.hardLinkVariant().equivalentObjectId);
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
				/* Do nothing */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
				/* Do nothing */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
				ydirectenv.yaffs_strncpy(oh.alias(), oh.aliasIndex(),
						in.variant.symLinkVariant().alias,
						in.variant.symLinkVariant().aliasIndex,
						Guts_H.YAFFS_MAX_ALIAS_LENGTH);
				oh.alias()[oh.aliasIndex()+Guts_H.YAFFS_MAX_ALIAS_LENGTH] = 0;
				break;
			}

			/* Tags */
			yaffs_tagsvalidity_C.yaffs_InitialiseTags(newTags);
			in.serial++;
			newTags.chunkId = 0;
			newTags.objectId = in.objectId;
			newTags.serialNumber = Utils.byteAsUnsignedByte(in.serial);

			/* Add extra info for file header */

			newTags.extraHeaderInfoAvailable = true;
			newTags.extraParentObjectId = oh.parentObjectId();
			newTags.extraFileLength = oh.fileSize();
			newTags.extraIsShrinkHeader = oh.isShrink();
			newTags.extraEquivalentObjectId = oh.equivalentObjectId();
			newTags.extraShadows = (oh.shadowsObject() > 0) ? true : false;
			newTags.extraObjectType = in.variantType;

			/* Create new chunk in NAND */
			newChunkId =
				yaffs_WriteNewChunkWithTagsToNAND(dev, buffer, bufferIndex, newTags,
						(prevChunkId >= 0) ? true : false);

			if (newChunkId >= 0) {

				in.chunkId = newChunkId;

				if (prevChunkId >= 0) {
					yaffs_DeleteChunk(dev, prevChunkId, true,
							14 /*Utils.__LINE__()*/);
				}

				if(!yaffs_ObjectHasCachedWriteData(in))
					in.dirty = false;

				/* If this was a shrink, then mark the block that the chunk lives on */
				if (isShrink) {
					bi = Guts_H.yaffs_GetBlockInfo(in.myDev,
							newChunkId /in.myDev.	subField1.nChunksPerBlock);
					bi.setHasShrinkHeader(true);
				}

			}

			retVal = newChunkId;

		}

		if (buffer != null)
			yaffs_ReleaseTempBuffer(dev, buffer, 15 /*Utils.__LINE__()*/);

		return retVal;
	}

	/*------------------------ Short Operations Cache ----------------------------------------
	 *   In many situations where there is no high level buffering (eg WinCE) a lot of
	 *   reads might be short sequential reads, and a lot of writes may be short 
	 *   sequential writes. eg. scanning/writing a jpeg file.
	 *   In these cases, a short read/write cache can provide a huge perfomance benefit 
	 *   with dumb-as-a-rock code.
	 *   In Linux, the page cache provides read buffering aand the short op cache provides write 
	 *   buffering.
	 *
	 *   There are a limited number (~10) of cache chunks per device so that we don't
	 *   need a very intelligent search.
	 */

	static boolean yaffs_ObjectHasCachedWriteData(yaffs_Object obj)
	{
		yaffs_Device dev = obj.myDev;
		int i;
		yaffs_ChunkCache cache;
		int nCaches = obj.myDev.subField1.nShortOpCaches;

		for(i = 0; i < nCaches; i++){
			cache = dev.srCache[i];
			if (cache.object == obj &&
					cache.dirty)
				return true;
		}

		return false;
	}


	static void yaffs_FlushFilesChunkCache(yaffs_Object  obj)
	{
		yaffs_Device dev = obj.myDev;
		int lowest = -99;	/* Stop compiler whining. */
		int i;
		yaffs_ChunkCache cache;
		int chunkWritten = 0;
		int nCaches = obj.myDev.subField1.nShortOpCaches;

		if (nCaches > 0) {
			do {
				cache = null;

				/* Find the dirty cache for this object with the lowest chunk id. */
				for (i = 0; i < nCaches; i++) {
					if (dev.srCache[i].object == obj &&
							dev.srCache[i].dirty) {
						if (!(cache != null)					
								|| dev.srCache[i].chunkId <
								lowest) {
							cache = dev.srCache[i];				// nBytes given 156
							lowest = cache.chunkId;
						}
					}
				}

				if ((cache != null) && !cache.locked) {
					/* Write it out and free it up */

					chunkWritten =
						yaffs_WriteChunkDataToObject(cache.object,
								cache.chunkId,
								cache.data, cache.dataIndex,
								cache.nBytes,
								true);
					cache.dirty = false;
					cache.object = null;
				}

			} while ((cache != null) && chunkWritten > 0);

			if (cache != null) {
				/* Hoosterman, disk full while writing cache out. */
				yportenv.T(yportenv.YAFFS_TRACE_ERROR,
						(("yaffs tragedy: no space during cache write" + ydirectenv.TENDSTR)));

			}
		}

	}

	/*yaffs_FlushEntireDeviceCache(dev)
	 *
	 *
	 */

	static void yaffs_FlushEntireDeviceCache(yaffs_Device dev)
	{
		yaffs_Object obj;
		int nCaches = dev.subField1.nShortOpCaches;
		int i;

		/* Find a dirty object in the cache and flush it...
		 * until there are no further dirty objects.
		 */
		do {
			obj = null;
			for( i = 0; i < nCaches && !(obj != null); i++) {
				if ((dev.srCache[i].object != null) &&
						dev.srCache[i].dirty)
					obj = dev.srCache[i].object;

			}
			if(obj != null)
				yaffs_FlushFilesChunkCache(obj);

		} while(obj != null);

	}


	/* Grab us a cache chunk for use.
	 * First look for an empty one. 
	 * Then look for the least recently used non-dirty one.
	 * Then look for the least recently used dirty one...., flush and look again.
	 */
	static yaffs_ChunkCache yaffs_GrabChunkCacheWorker(yaffs_Device dev)
	{
		int i;
		int usage;
		int theOne;

		if (dev.subField1.nShortOpCaches > 0) {
			for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
				if (!(dev.srCache[i].object != null)) 
					return dev.srCache[i];
			}

			return null;

			// PORT the Java compiler whines about the rest of the block to be not reachable
//			theOne = -1;
//			usage = 0;	/* just to stop the compiler grizzling */

//			for (i = 0; i < dev.nShortOpCaches; i++) {
//			if (!dev.srCache[i].dirty &&
//			((dev.srCache[i].lastUse < usage && theOne >= 0) ||
//			theOne < 0)) {
//			usage = dev.srCache[i].lastUse;
//			theOne = i;
//			}
//			}


//			return theOne >= 0 ? dev.srCache[theOne] : null;
		} else {
			return null;
		}

	}

	static yaffs_ChunkCache yaffs_GrabChunkCache(yaffs_Device dev)
	{
		yaffs_ChunkCache cache;
		yaffs_Object theObj;
		int usage;
		int i;
		int pushout;

		if (dev.subField1.nShortOpCaches > 0) {
			/* Try find a non-dirty one... */

			cache = yaffs_GrabChunkCacheWorker(dev);

			if (!(cache != null)) {
				/* They were all dirty, find the last recently used object and flush
				 * its cache, then  find again.
				 * NB what's here is not very accurate, we actually flush the object
				 * the last recently used page.
				 */

				/* With locking we can't assume we can use entry zero */

				theObj = null;
				usage = -1;
				cache = null;
				pushout = -1;

				for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
					if ((dev.srCache[i].object != null) &&
							!dev.srCache[i].locked &&
							(dev.srCache[i].lastUse < usage || !(cache != null)))
					{
						usage = dev.srCache[i].lastUse;
						theObj = dev.srCache[i].object;
						cache = dev.srCache[i];
						pushout = i;
					}
				}

				if (!(cache != null) || cache.dirty) {
					/* Flush and try again */
					yaffs_FlushFilesChunkCache(theObj);
					cache = yaffs_GrabChunkCacheWorker(dev);
				}

			}
			return cache;
		} else
			return null;

	}

	/* Find a cached chunk */
	static yaffs_ChunkCache yaffs_FindChunkCache(yaffs_Object  obj,
			int chunkId)
	{
		yaffs_Device dev = obj.myDev;
		int i;
		if (dev.subField1.nShortOpCaches > 0) {
			for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
				if (dev.srCache[i].object == obj &&
						dev.srCache[i].chunkId == chunkId) {
					dev.cacheHits++;

					return dev.srCache[i];
				}
			}
		}
		return null;
	}

	/* Mark the chunk for the least recently used algorithym */
	static void yaffs_UseChunkCache(yaffs_Device dev, yaffs_ChunkCache cache,
			boolean isAWrite)
	{

		if (dev.subField1.nShortOpCaches > 0) {
			if (dev.srLastUse < 0 || dev.srLastUse > 100000000) {
				/* Reset the cache usages */
				int i;
				for (i = 1; i < dev.subField1.nShortOpCaches; i++) {
					dev.srCache[i].lastUse = 0;
				}
				dev.srLastUse = 0;
			}

			dev.srLastUse++;

			cache.lastUse = dev.srLastUse;

			if (isAWrite) {
				cache.dirty = true;
			}
		}
	}

	/* Invalidate a single cache page.
	 * Do this when a whole page gets written,
	 * ie the short cache for this page is no longer valid.
	 */
	static void yaffs_InvalidateChunkCache(yaffs_Object  object, int chunkId)
	{
		if (object.myDev.subField1.nShortOpCaches > 0) {
			yaffs_ChunkCache cache = yaffs_FindChunkCache(object, chunkId);

			if (cache != null) {
				cache.object = null;
			}
		}
	}

	/* Invalidate all the cache pages associated with this object
	 * Do this whenever ther file is deleted or resized.
	 */
	static void yaffs_InvalidateWholeChunkCache(yaffs_Object  in)
	{
		int i;
		yaffs_Device dev = in.myDev;

		if (dev.subField1.nShortOpCaches > 0) {
			/* Invalidate it. */
			for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
				if (dev.srCache[i].object == in) {
					dev.srCache[i].object = null;
				}
			}
		}
	}

	/*--------------------- Checkpointing --------------------*/


	static boolean yaffs_WriteCheckpointValidityMarker(yaffs_Device dev,int head)
	{
		yaffs_CheckpointValidity cp = new yaffs_CheckpointValidity();
		cp.setStructType(/*sizeof(cp)*/ yaffs_CheckpointValidity.SERIALIZED_LENGTH);
		cp.setMagic(Guts_H.YAFFS_MAGIC);
		cp.setVersion(Guts_H.YAFFS_CHECKPOINT_VERSION);
		cp.setHead((head != 0) ? 1 : 0);

		return (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,
				/*sizeof(cp)*/ yaffs_CheckpointValidity.SERIALIZED_LENGTH) 
				== /*sizeof(cp)*/ yaffs_CheckpointValidity.SERIALIZED_LENGTH) ? true : false;
	}

	static boolean yaffs_ReadCheckpointValidityMarker(yaffs_Device dev, int head)
	{
		yaffs_CheckpointValidity cp = new yaffs_CheckpointValidity();
		boolean ok;

		ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,cp.serialized,cp.offset,
				yaffs_CheckpointValidity.SERIALIZED_LENGTH) == yaffs_CheckpointValidity.SERIALIZED_LENGTH);

		if(ok)
			ok = (cp.structType() == yaffs_CheckpointValidity.SERIALIZED_LENGTH) &&
			(cp.magic() == Guts_H.YAFFS_MAGIC) &&
			(cp.version() == Guts_H.YAFFS_CHECKPOINT_VERSION) &&
			(cp.head() == ((head != 0) ? 1 : 0));
		return (ok) ? true : false;
	}

	static void yaffs_DeviceToCheckpointDevice(yaffs_CheckpointDevice cp, 
			yaffs_Device dev)
	{
		cp.setNErasedBlocks(dev.subField3.nErasedBlocks);
		cp.setAllocationBlock(dev.subField3.allocationBlock);
		cp.setAllocationPage(dev.subField3.allocationPage);
		cp.setNFreeChunks(dev.subField3.nFreeChunks);

		cp.setNDeletedFiles(dev.nDeletedFiles);
		cp.setNUnlinkedFiles(dev.nUnlinkedFiles);
		cp.setNBackgroundDeletions(dev.nBackgroundDeletions);
		cp.setSequenceNumber((int)dev.sequenceNumber);
		cp.setOldestDirtySequence((int)dev.oldestDirtySequence);

	}

	static void yaffs_CheckpointDeviceToDevice(yaffs_Device dev,
			yaffs_CheckpointDevice cp)
	{
		dev.subField3.nErasedBlocks = cp.nErasedBlocks();
		dev.subField3.allocationBlock = cp.allocationBlock();
		dev.subField3.allocationPage = cp.allocationPage();
		dev.subField3.nFreeChunks = cp.nFreeChunks();

		dev.nDeletedFiles = cp.nDeletedFiles();
		dev.nUnlinkedFiles = cp.nUnlinkedFiles();
		dev.nBackgroundDeletions = cp.nBackgroundDeletions();
		dev.sequenceNumber = Utils.intAsUnsignedInt(cp.sequenceNumber());
		dev.oldestDirtySequence = Utils.intAsUnsignedInt(cp.oldestDirtySequence());
	}


	static boolean yaffs_WriteCheckpointDevice(yaffs_Device dev)
	{
		yaffs_CheckpointDevice cp = new yaffs_CheckpointDevice();
		/**__u32*/ int nBytes;
		/**__u32*/ int nBlocks = (dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1);

		boolean ok;

		/* Write device runtime values*/
		yaffs_DeviceToCheckpointDevice(cp,dev);
		cp.setStructType(yaffs_CheckpointDevice.SERIALIZED_LENGTH);

		ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,yaffs_CheckpointDevice.SERIALIZED_LENGTH) == yaffs_CheckpointDevice.SERIALIZED_LENGTH);

		/* Write block info */
		if(ok) {
			nBytes = nBlocks * yaffs_BlockInfo.SERIALIZED_LENGTH;
			// PORT We want to write the data for the whole array.
			// The data for all yaffs_BlockInfo array members is stored in the same array.
			ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,dev.subField2.blockInfo[0].serialized,
					dev.subField2.blockInfo[0].offset,nBytes) == nBytes);
		}

		/* Write chunk bits */		
		if(ok) {
			nBytes = nBlocks * dev.subField3.chunkBitmapStride;
			ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,dev.subField2.chunkBits,dev.subField2.chunkBitsIndex,nBytes) == nBytes);
		}
		return	 ok ? true : false;

	}

	static boolean yaffs_ReadCheckpointDevice(yaffs_Device dev)
	{
		yaffs_CheckpointDevice cp = new yaffs_CheckpointDevice();
		/**__u32*/ int nBytes;
		/**__u32*/ int nBlocks = (dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1);

		boolean ok;	

		ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,cp.serialized,cp.offset,yaffs_CheckpointDevice.SERIALIZED_LENGTH) == yaffs_CheckpointDevice.SERIALIZED_LENGTH);
		if(!(ok))
			return false;

		if(cp.structType() != yaffs_CheckpointDevice.SERIALIZED_LENGTH)
			return false;


		yaffs_CheckpointDeviceToDevice(dev,cp);

		nBytes = nBlocks * yaffs_BlockInfo.SERIALIZED_LENGTH;

		ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,dev.subField2.blockInfo[0].serialized,dev.subField2.blockInfo[0].offset,
				nBytes) == nBytes);

		if(!(ok))
			return false;
		nBytes = nBlocks * dev.subField3.chunkBitmapStride;

		ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,dev.subField2.chunkBits,dev.subField2.chunkBitsIndex,nBytes) == nBytes);

		return ok ? true : false;
	}

	static void yaffs_ObjectToCheckpointObject(yaffs_CheckpointObject cp,
			yaffs_Object obj)
	{

		cp.setObjectId(obj.objectId);
		cp.setParentId((obj.parent != null) ? obj.parent.objectId : 0);
		cp.setChunkId(obj.chunkId);
		cp.setVariantType(obj.variantType);			
		cp.setDeleted(obj.sub.deleted);
		cp.setSoftDeleted(obj.sub.softDeleted);
		cp.setUnlinked(obj.sub.unlinked);
		cp.setFake(obj.sub.fake);
		cp.setRenameAllowed(obj.renameAllowed);
		cp.setUnlinkAllowed(obj.unlinkAllowed);
		cp.setSerial(obj.serial);
		cp.setNDataChunks(obj.nDataChunks);

		if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE)
			cp.setFileSizeOrEquivalentObjectId(obj.variant.fileVariant().fileSize);
		else if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK)
			cp.setFileSizeOrEquivalentObjectId(obj.variant.hardLinkVariant().equivalentObjectId);
	}

	static void yaffs_CheckpointObjectToObject( yaffs_Object obj,yaffs_CheckpointObject cp)
	{

		yaffs_Object parent;

		obj.objectId = cp.objectId();

		if(cp.parentId() != 0)
			parent = yaffs_FindOrCreateObjectByNumber(
					obj.myDev,
					cp.parentId(),
					Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY);
		else
			parent = null;

		if(parent != null)
			yaffs_AddObjectToDirectory(parent, obj);

		obj.chunkId = cp.chunkId();
		obj.variantType = cp.variantType();			
		obj.sub.deleted = cp.deleted();
		obj.sub.softDeleted = cp.softDeleted();
		obj.sub.unlinked = cp.unlinked();
		obj.sub.fake = cp.fake();
		obj.renameAllowed = cp.renameAllowed();
		obj.unlinkAllowed = cp.unlinkAllowed();
		obj.serial = cp.serial();
		obj.nDataChunks = cp.nDataChunks();

		if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE)
			obj.variant.fileVariant().fileSize = cp.fileSizeOrEquivalentObjectId();
		else if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK)
			obj.variant.hardLinkVariant().equivalentObjectId = cp.fileSizeOrEquivalentObjectId();

		if(obj.objectId >= Guts_H.YAFFS_NOBJECT_BUCKETS)
			obj.lazyLoaded = true;
	}



	// PORT Only leafs (16 bit page address) are written here.
	static boolean yaffs_CheckpointTnodeWorker(yaffs_Object  in, yaffs_Tnode tn,
			/**__u32*/ int level, int chunkOffset)
	{
		int i;
		yaffs_Device dev = in.myDev;
		boolean ok = true;
		int nTnodeBytes = (dev.subField2.tnodeWidth * Guts_H.YAFFS_NTNODES_LEVEL0)/8;

		if (tn != null) {
			if (level > 0) {

				for (i = 0; i < Guts_H.YAFFS_NTNODES_INTERNAL && ok; i++){
					if (tn.internal[i] != null) {
						ok = yaffs_CheckpointTnodeWorker(in,
								tn.internal[i],
								level - 1,
								(chunkOffset<<Guts_H.YAFFS_TNODES_INTERNAL_BITS) + i);
					}
				}
			} else if (level == 0) {
				/*__u32*/ byte[] baseOffset = new byte[4]; final int baseOffsetIndex = 0;
				Utils.writeIntToByteArray(baseOffset, baseOffsetIndex, 
						chunkOffset <<  Guts_H.YAFFS_TNODES_LEVEL0_BITS);
				/* printf("write tnode at %d\n",baseOffset); */
				ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,baseOffset,baseOffsetIndex,4) == 4);
				if(ok)
					ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,tn.serialized,tn.offset,nTnodeBytes) == nTnodeBytes);
			}
		}

		return ok;

	}

	static boolean yaffs_WriteCheckpointTnodes(yaffs_Object obj)
	{
		/**__u32*/ byte[] endMarker = new byte[Constants.SIZEOF_INT]; final int endMarkerIndex = 0;
		Utils.writeIntToByteArray(endMarker, endMarkerIndex, ~0);
		boolean ok = true;

		if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE){
			ok = yaffs_CheckpointTnodeWorker(obj,
					obj.variant.fileVariant().top,
					obj.variant.fileVariant().topLevel,
					0);
			if(ok)
				ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(obj.myDev,endMarker,endMarkerIndex,Constants.SIZEOF_INT) == 
					Constants.SIZEOF_INT);
		}

		return ok ? true : false;
	}

	static boolean yaffs_ReadCheckpointTnodes(yaffs_Object obj)
	{
		/**__u32*/ byte[] baseChunk = new byte[Constants.SIZEOF_INT];
		final int baseChunkIndex = 0;
		boolean ok = true;
		yaffs_Device dev = obj.myDev;
		yaffs_FileStructure fileStructPtr = obj.variant.fileVariant();
		yaffs_Tnode tn;

		ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,baseChunk,baseChunkIndex,Constants.SIZEOF_INT) == Constants.SIZEOF_INT);

		while(ok && (~Utils.getIntFromByteArray(baseChunk, baseChunkIndex)) != 0){
			/* Read level 0 tnode */

			/* printf("read  tnode at %d\n",baseChunk); */
			tn = yaffs_GetTnodeRaw(dev);
			if(tn != null)
				ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,tn.serialized,tn.offset,
						(dev.subField2.tnodeWidth * Guts_H.YAFFS_NTNODES_LEVEL0)/8) ==
							(dev.subField2.tnodeWidth * Guts_H.YAFFS_NTNODES_LEVEL0)/8);
			else
				ok = false;

			if(tn != null && ok){
				ok = yaffs_AddOrFindLevel0Tnode(dev,
						fileStructPtr,
						Utils.getIntFromByteArray(baseChunk, baseChunkIndex),
						tn) != null ? true : false;
			}

			if(ok)
				ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,baseChunk,baseChunkIndex,
						Constants.SIZEOF_INT) == 
							Constants.SIZEOF_INT);

		}

		return ok ? true : false;	
	}


	static boolean yaffs_WriteCheckpointObjects(yaffs_Device dev)
	{
		yaffs_Object obj;
		yaffs_CheckpointObject cp = new yaffs_CheckpointObject();
		int i;
		boolean ok = true;
		list_head lh;


		/* Iterate through the objects in each hash entry,
		 * dumping them to the checkpointing stream.
		 */

		for(i = 0; ok &&  i <  Guts_H.YAFFS_NOBJECT_BUCKETS; i++){
//			list_for_each(lh, &dev.objectBucket[i].list) {
			for (lh = dev.subField3.objectBucket[i].list.next(); lh != dev.subField3.objectBucket[i].list;
			lh = lh.next()) {
				if (lh != null) {
					obj = /*list_entry(lh, yaffs_Object, hashLink)*/ (yaffs_Object)lh.list_entry;
					if (!obj.deferedFree) {
						Unix.memset(cp.serialized,0,(byte)0,cp.getSerializedLength());
						yaffs_ObjectToCheckpointObject(cp,obj);
						cp.setStructType(yaffs_CheckpointObject.SERIALIZED_LENGTH);

						yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,
								("Checkpoint write object %d parent %d type %d chunk %d obj addr %x" + ydirectenv.TENDSTR),
								PrimitiveWrapperFactory.get(cp.objectId()),PrimitiveWrapperFactory.get(cp.parentId()),
								PrimitiveWrapperFactory.get(cp.variantType()),PrimitiveWrapperFactory.get(cp.chunkId()),
								/*(unsigned)*/PrimitiveWrapperFactory.get(yaffs2.utils.Utils.hashCode(obj)),
								null, null, null, null);

						ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,
								yaffs_CheckpointObject.SERIALIZED_LENGTH)) == yaffs_CheckpointObject.SERIALIZED_LENGTH;

						if(ok && obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE){
							ok = yaffs_WriteCheckpointTnodes(obj);
						}
					}
				}
			}
		}

		/* Dump end of list */
		Unix.memset(cp,(byte)0xFF/*,sizeof(yaffs_CheckpointObject)*/);
		cp.setStructType(yaffs_CheckpointObject.SERIALIZED_LENGTH);

		if(ok)
			ok = (yaffs_checkptrw_C.yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,
					yaffs_CheckpointObject.SERIALIZED_LENGTH) == yaffs_CheckpointObject.SERIALIZED_LENGTH);

		return ok ? true : false;
	}

	static boolean yaffs_ReadCheckpointObjects(yaffs_Device dev)
	{
		yaffs_Object obj;
		yaffs_CheckpointObject cp = new yaffs_CheckpointObject();
		boolean ok = true;
		boolean done = false;
		yaffs_Object hardList = null;

		while(ok && !done) {
			ok = (yaffs_checkptrw_C.yaffs_CheckpointRead(dev,cp.serialized,cp.offset,
					yaffs_CheckpointObject.SERIALIZED_LENGTH) == yaffs_CheckpointObject.SERIALIZED_LENGTH);
			if(cp.structType() != yaffs_CheckpointObject.SERIALIZED_LENGTH) {
				/* printf("structure parsing failed\n"); */
				ok = false;
			}

			if(ok && cp.objectId() == ~0)
				done = true;
			else if(ok){
				obj = yaffs_FindOrCreateObjectByNumber(dev,cp.objectId(), cp.variantType());
				yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("Checkpoint read object %d parent %d type %d chunk %d obj addr %x" + ydirectenv.TENDSTR),
						PrimitiveWrapperFactory.get(cp.objectId()),PrimitiveWrapperFactory.get(cp.parentId()),
						PrimitiveWrapperFactory.get(cp.variantType()),PrimitiveWrapperFactory.get(cp.chunkId()),
						/*(unsigned)*/PrimitiveWrapperFactory.get(yaffs2.utils.Utils.hashCode(obj)),
						null, null, null, null);
				if(obj != null) {
					yaffs_CheckpointObjectToObject(obj,cp);
					if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE) {
						ok = yaffs_ReadCheckpointTnodes(obj);
					} else if(obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK) {
						obj.hardLinks.next =
							/*(list_head)*/ 
							hardList;
						hardList = obj;
					}

				}
			}
		}

		if(ok)
			yaffs_HardlinkFixup(dev,hardList);

		return ok ? true : false;
	}

	static boolean yaffs_WriteCheckpointData(yaffs_Device dev)
	{

		boolean ok;

		ok = yaffs_checkptrw_C.yaffs_CheckpointOpen(dev,true) ? true : false;

		if(ok)
			ok = yaffs_WriteCheckpointValidityMarker(dev,1);
		if(ok)
			ok = yaffs_WriteCheckpointDevice(dev) ? true : false;
		if(ok)
			ok = yaffs_WriteCheckpointObjects(dev) ? true : false;
		if(ok)
			ok = yaffs_WriteCheckpointValidityMarker(dev,0);

		if(!yaffs_checkptrw_C.yaffs_CheckpointClose(dev))
			ok = false;

		if(ok)
			dev.subField2.isCheckpointed = true;
		else 
			dev.subField2.isCheckpointed = false;

		return dev.subField2.isCheckpointed;
	}

	static boolean yaffs_ReadCheckpointData(yaffs_Device dev)
	{
		boolean ok;

		ok = yaffs_checkptrw_C.yaffs_CheckpointOpen(dev,false); /* open for read */

		if(ok)
			ok = yaffs_ReadCheckpointValidityMarker(dev,1);
		if(ok)
			ok = yaffs_ReadCheckpointDevice(dev);
		if(ok)
			ok = yaffs_ReadCheckpointObjects(dev);
		if(ok)
			ok = yaffs_ReadCheckpointValidityMarker(dev,0);



		if(!yaffs_checkptrw_C.yaffs_CheckpointClose(dev))
			ok = false;

		if(ok)
			dev.subField2.isCheckpointed = true;
		else 
			dev.subField2.isCheckpointed = false;

		return ok ? true : false;

	}

	static void yaffs_InvalidateCheckpoint(yaffs_Device dev)
	{
		if(dev.subField2.isCheckpointed || 
				dev.subField2.blocksInCheckpoint > 0){
			dev.subField2.isCheckpointed = false;
			yaffs_checkptrw_C.yaffs_CheckpointInvalidateStream(dev);
			if(dev.subField1.superBlock != null && dev.subField1.markSuperBlockDirty != null)
				dev.subField1.markSuperBlockDirty.markSuperBlockDirty(dev.subField1.superBlock);
		}
	}


	static boolean yaffs_CheckpointSave(yaffs_Device dev)
	{
		yaffs_ReportOddballBlocks(dev);
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("save entry: isCheckpointed %b"+ ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(dev.subField2.isCheckpointed));

		if(!dev.subField2.isCheckpointed)
			yaffs_WriteCheckpointData(dev);

		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("save exit: isCheckpointed %b"+ ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(dev.subField2.isCheckpointed));

		return dev.subField2.isCheckpointed;
	}

	static boolean yaffs_CheckpointRestore(yaffs_Device dev)
	{
		boolean retval;
		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("restore entry: isCheckpointed %b"+ ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(dev.subField2.isCheckpointed));

		retval = yaffs_ReadCheckpointData(dev);

		yportenv.T(yportenv.YAFFS_TRACE_CHECKPOINT,("restore exit: isCheckpointed %b"+ ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(dev.subField2.isCheckpointed));

		yaffs_ReportOddballBlocks(dev);

		return retval;
	}

	/*--------------------- File read/write ------------------------
	 * Read and write have very similar structures.
	 * In general the read/write has three parts to it
	 * An incomplete chunk to start with (if the read/write is not chunk-aligned)
	 * Some complete chunks
	 * An incomplete chunk to end off with
	 *
	 * Curve-balls: the first chunk might also be the last chunk.
	 */

	static int yaffs_ReadDataFromFile(yaffs_Object  in, /*__u8 **/ byte[] buffer, int bufferIndex,  
			/*loff_t*/ int offset,
			int nBytes)
	{
		int chunk;
		int start;
		int nToCopy;
		int n = nBytes;
		int nDone = 0;
		yaffs_ChunkCache cache;

		yaffs_Device dev;

		dev = in.myDev;

		while (n > 0) {
			//chunk = offset / dev.nDataBytesPerChunk + 1;
			//start = offset % dev.nDataBytesPerChunk;
			IntegerPointer chunkPointer = new IntegerPointer();
			IntegerPointer startPointer = new IntegerPointer();
			yaffs_AddrToChunk(dev,offset,chunkPointer,startPointer);
			chunk = chunkPointer.dereferenced;
			start = startPointer.dereferenced;
			chunk++;

			/* OK now check for the curveball where the start and end are in
			 * the same chunk.      
			 */
			if ((start + n) < dev.subField1.nDataBytesPerChunk) {
				nToCopy = n;
			} else {
				nToCopy = dev.subField1.nDataBytesPerChunk - start;
			}

			cache = yaffs_FindChunkCache(in, chunk);

			/* If the chunk is already in the cache or it is less than a whole chunk
			 * then use the cache (if there is caching)
			 * else bypass the cache.
			 */
			if (cache != null || nToCopy != dev.subField1.nDataBytesPerChunk) {
				if (dev.subField1.nShortOpCaches > 0) {

					/* If we can't find the data in the cache, then load it up. */

					if (!(cache != null)) {
						cache = yaffs_GrabChunkCache(in.myDev);
						cache.object = in;
						cache.chunkId = chunk;
						cache.dirty = false;
						cache.locked = false;
						yaffs_ReadChunkDataFromObject(in, chunk,
								cache.data, cache.dataIndex);
						cache.nBytes = 0;
					}

					yaffs_UseChunkCache(dev, cache, false);

					cache.locked = true;

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_UnlockYAFFS(TRUE);
//					#endif
					Unix.memcpy(buffer, bufferIndex, cache.data, cache.dataIndex+start, nToCopy);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_LockYAFFS(TRUE);
//					#endif
					cache.locked = false;
				} else {
					/* Read into the local buffer then copy..*/

					/*__u8 **/ byte[] localBuffer =
						yaffs_GetTempBuffer(dev, 17 /*Utils.__LINE__()*/);
					final int localBufferIndex = 0;
					yaffs_ReadChunkDataFromObject(in, chunk,
							localBuffer, localBufferIndex);
//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_UnlockYAFFS(TRUE);
//					#endif
					Unix.memcpy(buffer, bufferIndex, localBuffer, localBufferIndex+start, nToCopy);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_LockYAFFS(TRUE);
//					#endif
					yaffs_ReleaseTempBuffer(dev, localBuffer,
							18 /*Utils.__LINE__()*/);
				}

			} else {
//				#ifdef CONFIG_YAFFS_WINCE
//				__u8 *localBuffer = yaffs_GetTempBuffer(dev, Utils.__LINE__);

//				/* Under WinCE can't do direct transfer. Need to use a local buffer.
//				* This is because we otherwise screw up WinCE's memory mapper
//				*/
//				yaffs_ReadChunkDataFromObject(in, chunk, localBuffer);

//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_UnlockYAFFS(TRUE);
//				#endif
//				Unix.memcpy(buffer, localBuffer, dev.nDataBytesPerChunk);

//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_LockYAFFS(TRUE);
//				yaffs_ReleaseTempBuffer(dev, localBuffer, Utils.__LINE__);
//				#endif

//				#else
				/* A full chunk. Read directly into the supplied buffer. */
				yaffs_ReadChunkDataFromObject(in, chunk, buffer, bufferIndex);
//				#endif
			}

			n -= nToCopy;
			offset += nToCopy;
			bufferIndex += nToCopy;
			nDone += nToCopy;

		}

		return nDone;
	}

	static int yaffs_WriteDataToFile(yaffs_Object  in, /*const __u8 **/ byte[] buffer, 
			int bufferIndex, /*loff_t*/ int offset,
			int nBytes, boolean writeThrough)
	{
		int chunk;
		int start;
		int nToCopy;
		int n = nBytes;
		int nDone = 0;
		int nToWriteBack;
		int startOfWrite = offset;
		int chunkWritten = 0;
		int nBytesRead;

		yaffs_Device dev;

		dev = in.myDev;

		while (n > 0 && chunkWritten >= 0) {
			//chunk = offset / dev.nDataBytesPerChunk + 1;
			//start = offset % dev.nDataBytesPerChunk;
			IntegerPointer chunkPointer = new IntegerPointer();
			IntegerPointer startPointer = new IntegerPointer();
			yaffs_AddrToChunk(dev,offset,chunkPointer,startPointer);
			chunk = chunkPointer.dereferenced;
			start = startPointer.dereferenced;
			chunk++;

			/* OK now check for the curveball where the start and end are in
			 * the same chunk.
			 */

			if ((start + n) < dev.subField1.nDataBytesPerChunk) {
				nToCopy = n;

				/* Now folks, to calculate how many bytes to write back....
				 * If we're overwriting and not writing to then end of file then
				 * we need to write back as much as was there before.
				 */

				nBytesRead =
					in.variant.fileVariant().fileSize -
					((chunk - 1) * dev.subField1.nDataBytesPerChunk);

				if (nBytesRead > dev.subField1.nDataBytesPerChunk) {
					nBytesRead = dev.subField1.nDataBytesPerChunk;
				}

				nToWriteBack =
					(nBytesRead >
					(start + n)) ? nBytesRead : (start + n);

			} else {
				nToCopy = dev.subField1.nDataBytesPerChunk - start;
				nToWriteBack = dev.subField1.nDataBytesPerChunk;
			}

			if (nToCopy != dev.subField1.nDataBytesPerChunk) {
				/* An incomplete start or end chunk (or maybe both start and end chunk) */
				if (dev.subField1.nShortOpCaches > 0) {
					yaffs_ChunkCache cache;
					/* If we can't find the data in the cache, then load the cache */
					cache = yaffs_FindChunkCache(in, chunk);

					if (!(cache != null)
							&& yaffs_CheckSpaceForAllocation(in.
									myDev)) {
						cache = yaffs_GrabChunkCache(in.myDev);
						cache.object = in;
						cache.chunkId = chunk;
						cache.dirty = false;
						cache.locked = false;
						yaffs_ReadChunkDataFromObject(in, chunk,
								cache.data, cache.dataIndex);
					}
					else if(cache != null && 
							!cache.dirty &&
							!yaffs_CheckSpaceForAllocation(in.myDev)){
						/* Drop the cache if it was a read cache item and
						 * no space check has been made for it.
						 */ 
						cache = null;
					}

					if (cache != null) {
						yaffs_UseChunkCache(dev, cache, true);
						cache.locked = true;
//						#ifdef CONFIG_YAFFS_WINCE
//						yfsd_UnlockYAFFS(TRUE);
//						#endif

						Unix.memcpy(cache.data, cache.dataIndex+start, buffer, bufferIndex,
								nToCopy);

//						#ifdef CONFIG_YAFFS_WINCE
//						yfsd_LockYAFFS(TRUE);
//						#endif
						cache.locked = false;
						cache.nBytes = nToWriteBack;

						if (writeThrough) {
							chunkWritten =
								yaffs_WriteChunkDataToObject
								(cache.object,
										cache.chunkId,
										cache.data, cache.dataIndex, cache.nBytes,
										true);
							cache.dirty = false;
						}

					} else {
						chunkWritten = -1;	/* fail the write */
					}
				} else {
					/* An incomplete start or end chunk (or maybe both start and end chunk)
					 * Read into the local buffer then copy, then copy over and write back.
					 */

					/*__u8 **/ byte[] localBuffer =
						yaffs_GetTempBuffer(dev, 19 /*Utils.__LINE__()*/);
					final int localBufferIndex = 0;

					yaffs_ReadChunkDataFromObject(in, chunk,
							localBuffer, localBufferIndex);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_UnlockYAFFS(TRUE);
//					#endif

					Unix.memcpy(localBuffer, start, buffer, bufferIndex, nToCopy);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_LockYAFFS(TRUE);
//					#endif
					chunkWritten =
						yaffs_WriteChunkDataToObject(in, chunk,
								localBuffer, localBufferIndex,
								nToWriteBack,
								false);

					yaffs_ReleaseTempBuffer(dev, localBuffer,
							20 /*Utils.__LINE__()*/);

				}

			} else {

//				#ifdef CONFIG_YAFFS_WINCE
//				/* Under WinCE can't do direct transfer. Need to use a local buffer.
//				* This is because we otherwise screw up WinCE's memory mapper
//				*/
//				__u8 *localBuffer = yaffs_GetTempBuffer(dev, Utils.__LINE__);
//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_UnlockYAFFS(TRUE);
//				#endif
//				Unix.memcpy(localBuffer, buffer, dev.nDataBytesPerChunk);
//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_LockYAFFS(TRUE);
//				#endif
//				chunkWritten =
//				yaffs_WriteChunkDataToObject(in, chunk, localBuffer,
//				dev.nDataBytesPerChunk,
//				0);
//				yaffs_ReleaseTempBuffer(dev, localBuffer, Utils.__LINE__);
//				#else
				/* A full chunk. Write directly from the supplied buffer. */
				chunkWritten =
					yaffs_WriteChunkDataToObject(in, chunk, buffer, bufferIndex,
							dev.subField1.nDataBytesPerChunk,
							false);
//				#endif
				/* Since we've overwritten the cached data, we better invalidate it. */
				yaffs_InvalidateChunkCache(in, chunk);
			}

			if (chunkWritten >= 0) {
				n -= nToCopy;
				offset += nToCopy;
				bufferIndex += nToCopy;
				nDone += nToCopy;
			}

		}

		/* Update file object */

		if ((startOfWrite + nDone) > in.variant.fileVariant().fileSize) {
			in.variant.fileVariant().fileSize = (startOfWrite + nDone);
		}

		in.dirty = true;

		return nDone;
	}


	/* ---------------------- File resizing stuff ------------------ */

	static void yaffs_PruneResizedChunks(yaffs_Object  in, int newSize)
	{

		yaffs_Device dev = in.myDev;
		int oldFileSize = in.variant.fileVariant().fileSize;

		int lastDel = 1 + (oldFileSize - 1) / dev.subField1.nDataBytesPerChunk;

		int startDel = 1 + (newSize + dev.subField1.nDataBytesPerChunk - 1) /
		dev.subField1.nDataBytesPerChunk;
		int i;
		int chunkId;

		/* Delete backwards so that we don't end up with holes if
		 * power is lost part-way through the operation.
		 */
		for (i = lastDel; i >= startDel; i--) {
			/* NB this could be optimised somewhat,
			 * eg. could retrieve the tags and write them without
			 * using yaffs_DeleteChunk
			 */

			chunkId = yaffs_FindAndDeleteChunkInFile(in, i, null);
			if (chunkId > 0) {
				if (chunkId <
						(dev.subField2.internalStartBlock * dev.subField1.nChunksPerBlock)
						|| chunkId >=
							((dev.subField2.internalEndBlock +
									1) * dev.subField1.nChunksPerBlock)) {
					yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
							("Found daft chunkId %d for %d" + ydirectenv.TENDSTR),
							PrimitiveWrapperFactory.get(chunkId), PrimitiveWrapperFactory.get(i));
				} else {
					in.nDataChunks--;
					yaffs_DeleteChunk(dev, chunkId, true, 21 /*Utils.__LINE__()*/);
				}
			}
		}

	}

	static int yaffs_ResizeFile(yaffs_Object  in, /*loff_t*/ int newSize)
	{

		int oldFileSize = in.variant.fileVariant().fileSize;
		int newSizeOfPartialChunk;
		int newFullChunks;

		yaffs_Device dev = in.myDev;

		IntegerPointer newFullChunksPointer = new IntegerPointer();
		IntegerPointer newSizeOfPartialChunkPointer = new IntegerPointer();
		yaffs_AddrToChunk(dev, newSize, newFullChunksPointer, newSizeOfPartialChunkPointer);
		newFullChunks = newFullChunksPointer.dereferenced;
		newSizeOfPartialChunk = newSizeOfPartialChunkPointer.dereferenced;

		yaffs_FlushFilesChunkCache(in);
		yaffs_InvalidateWholeChunkCache(in);

		yaffs_CheckGarbageCollection(dev);

		if (in.variantType != Guts_H.YAFFS_OBJECT_TYPE_FILE) {
			return yaffs_GetFileSize(in);
		}

		if (newSize == oldFileSize) {
			return oldFileSize;
		}

		if (newSize < oldFileSize) {

			yaffs_PruneResizedChunks(in, newSize);

			if (newSizeOfPartialChunk != 0) {
				int lastChunk = 1 + newFullChunks;

				/*__u8 **/ byte[] localBuffer = yaffs_GetTempBuffer(dev, 22 /*Utils.__LINE__()*/);
				final int localBufferIndex = 0;

				/* Got to read and rewrite the last chunk with its new size and zero pad */
				yaffs_ReadChunkDataFromObject(in, lastChunk,
						localBuffer, localBufferIndex);

				Unix.memset(localBuffer, localBufferIndex+newSizeOfPartialChunk, (byte)0,
						dev.subField1.nDataBytesPerChunk - newSizeOfPartialChunk);

				yaffs_WriteChunkDataToObject(in, lastChunk, localBuffer, localBufferIndex,
						newSizeOfPartialChunk, true);

				yaffs_ReleaseTempBuffer(dev, localBuffer, 23 /*Utils.__LINE__()*/);
			}

			in.variant.fileVariant().fileSize = newSize;

			yaffs_PruneFileStructure(dev, in.variant.fileVariant());
		} else {
			/* newsSize > oldFileSize */
			in.variant.fileVariant().fileSize = newSize;
		}



		/* Write a new object header.
		 * show we've shrunk the file, if need be
		 * Do this only if the file is not in the deleted directories.
		 */
		if (in.parent.objectId != Guts_H.YAFFS_OBJECTID_UNLINKED &&
				in.parent.objectId != Guts_H.YAFFS_OBJECTID_DELETED) {
			yaffs_UpdateObjectHeader(in, null, 0, false,
					(newSize < oldFileSize) ? true : false, 0);
		}

		return newSize;
	}

	static /*loff_t*/ int yaffs_GetFileSize(yaffs_Object  obj)
	{
		obj = yaffs_GetEquivalentObject(obj);

		switch (obj.variantType) {
		case Guts_H.YAFFS_OBJECT_TYPE_FILE:
			return obj.variant.fileVariant().fileSize;
		case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
			return ydirectenv.yaffs_strlen(obj.variant.symLinkVariant().alias,
					obj.variant.symLinkVariant().aliasIndex);
		default:
			return 0;
		}
	}



	static boolean yaffs_FlushFile(yaffs_Object  in, boolean updateTime)
	{
		boolean retVal;
		if (in.dirty) {
			yaffs_FlushFilesChunkCache(in);
			if (updateTime) {
//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_WinFileTimeNow(in.win_mtime);
//				#else

				in.yst_mtime = ydirectenv.Y_CURRENT_TIME();

//				#endif
			}

			retVal =
				(yaffs_UpdateObjectHeader(in, null, 0, false, false, 0) >=
					0) ? Guts_H.YAFFS_OK : Guts_H.YAFFS_FAIL;
		} else {
			retVal = Guts_H.YAFFS_OK;
		}

		return retVal;

	}

	static boolean yaffs_DoGenericObjectDeletion(yaffs_Object  in)
	{

		/* First off, invalidate the file's data in the cache, without flushing. */
		yaffs_InvalidateWholeChunkCache(in);

		if (in.myDev.subField1.isYaffs2 && (in.parent != in.myDev.deletedDir)) {
			/* Move to the unlinked directory so we have a record that it was deleted. */
			yaffs_ChangeObjectName(in, in.myDev.deletedDir, null, 0, false, 0);

		}

		yaffs_RemoveObjectFromDirectory(in);
		yaffs_DeleteChunk(in.myDev, in.chunkId, true, 24 /*Utils.__LINE__()*/);
		in.chunkId = -1;

		yaffs_FreeObject(in);
		return Guts_H.YAFFS_OK;

	}

	/* yaffs_DeleteFile deletes the whole file data
	 * and the inode associated with the file.
	 * It does not delete the links associated with the file.
	 */
	static boolean yaffs_UnlinkFile(yaffs_Object  in)
	{

		boolean retVal;
		boolean immediateDeletion = false;

		if (true) {
//			#ifdef __KERNEL__
//			if (!in.myInode) {
//			immediateDeletion = 1;

//			}
//			#else
			if (in.inUse <= 0) {
				immediateDeletion = true;

			}
//			#endif
			if (immediateDeletion) {
				retVal =
					yaffs_ChangeObjectName(in, in.myDev.deletedDir,
							null, 0, false, 0);
				yportenv.T(yportenv.YAFFS_TRACE_TRACING,
						("yaffs: immediate deletion of file %d" + ydirectenv.TENDSTR),
						PrimitiveWrapperFactory.get(in.objectId));
				in.sub.deleted = true;
				in.myDev.nDeletedFiles++;
				if (false && in.myDev.subField1.isYaffs2) {
					yaffs_ResizeFile(in, 0);
				}
				yaffs_SoftDeleteFile(in);
			} else {
				retVal =
					yaffs_ChangeObjectName(in, in.myDev.unlinkedDir,
							null, 0, false, 0);
			}

		}
		return retVal;
	}

	static boolean yaffs_DeleteFile(yaffs_Object  in)
	{
		boolean retVal = Guts_H.YAFFS_OK;

		if (in.nDataChunks > 0) {
			/* Use soft deletion if there is data in the file */
			if (!in.sub.unlinked) {
				retVal = yaffs_UnlinkFile(in);
			}
			if (retVal == Guts_H.YAFFS_OK && in.sub.unlinked && !in.sub.deleted) {
				in.sub.deleted = true;
				in.myDev.nDeletedFiles++;
				yaffs_SoftDeleteFile(in);
			}
			return in.sub.deleted ? Guts_H.YAFFS_OK : Guts_H.YAFFS_FAIL;
		} else {
			/* The file has no data chunks so we toss it immediately */
			yaffs_FreeTnode(in.myDev, in.variant.fileVariant().top);
			in.variant.fileVariant().top = null;
			yaffs_DoGenericObjectDeletion(in);

			return Guts_H.YAFFS_OK;
		}
	}

	static boolean yaffs_DeleteDirectory(yaffs_Object  in)
	{
		/* First check that the directory is empty. */
		if (devextras.list_empty(in.variant.directoryVariant().children)) {
			return yaffs_DoGenericObjectDeletion(in);
		}

		return Guts_H.YAFFS_FAIL;

	}

	static boolean yaffs_DeleteSymLink(yaffs_Object  in)
	{
		ydirectenv.YFREE(in.variant.symLinkVariant().alias);

		return yaffs_DoGenericObjectDeletion(in);
	}

	static boolean yaffs_DeleteHardLink(yaffs_Object  in)
	{
		/* remove this hardlink from the list assocaited with the equivalent
		 * object
		 */
		devextras.list_del(in.hardLinks);
		return yaffs_DoGenericObjectDeletion(in);
	}

	static void yaffs_DestroyObject(yaffs_Object  obj)
	{
		switch (obj.variantType) {
		case Guts_H.YAFFS_OBJECT_TYPE_FILE:
			yaffs_DeleteFile(obj);
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
			yaffs_DeleteDirectory(obj);
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
			yaffs_DeleteSymLink(obj);
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
			yaffs_DeleteHardLink(obj);
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
			yaffs_DoGenericObjectDeletion(obj);
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:
			break;		/* should not happen. */
		}
	}

	static boolean yaffs_UnlinkWorker(yaffs_Object  obj)
	{

		if (obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK) {
			return yaffs_DeleteHardLink(obj);
		} else if (!devextras.list_empty(obj.hardLinks)) {
			/* Curve ball: We're unlinking an object that has a hardlink.
			 *
			 * This problem arises because we are not strictly following
			 * The Linux link/inode model.
			 *
			 * We can't really delete the object.
			 * Instead, we do the following:
			 * - Select a hardlink.
			 * - Unhook it from the hard links
			 * - Unhook it from its parent directory (so that the rename can work)
			 * - Rename the object to the hardlink's name.
			 * - Delete the hardlink
			 */

			yaffs_Object hl;
			boolean retVal;
			/*YCHAR*/ byte[] name = new byte[Guts_H.YAFFS_MAX_NAME_LENGTH + 1];
			final int nameIndex = 0;

			hl = /*list_entry(obj.hardLinks.next, yaffs_Object, hardLinks)*/ (yaffs_Object)obj.parent;

			devextras.list_del_init(hl.hardLinks);
			devextras.list_del_init(hl.siblings);

			yaffs_GetObjectName(hl, name, nameIndex, Guts_H.YAFFS_MAX_NAME_LENGTH + 1);

			retVal = yaffs_ChangeObjectName(obj, hl.parent, name, nameIndex, false, 0);

			if (retVal == Guts_H.YAFFS_OK) {
				retVal = yaffs_DoGenericObjectDeletion(hl);
			}
			return retVal;

		} else {
			switch (obj.variantType) {
			case Guts_H.YAFFS_OBJECT_TYPE_FILE:
				return yaffs_UnlinkFile(obj);
//				break;
			case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
				return yaffs_DeleteDirectory(obj);
//				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
				return yaffs_DeleteSymLink(obj);
//				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
				return yaffs_DoGenericObjectDeletion(obj);
//				break;
			case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
			case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:
			default:
				return Guts_H.YAFFS_FAIL;
			}
		}
	}


	static boolean yaffs_UnlinkObject( yaffs_Object obj)
	{

		if (obj != null && obj.unlinkAllowed) {
			return yaffs_UnlinkWorker(obj);
		}

		return Guts_H.YAFFS_FAIL;

	}
	static boolean yaffs_Unlink(yaffs_Object  dir, /*const YCHAR **/ byte[] name,
			int nameIndex)
	{
		yaffs_Object obj;

		obj = yaffs_FindObjectByName(dir, name, nameIndex);
		return yaffs_UnlinkObject(obj);
	}

	/*----------------------- Initialisation Scanning ---------------------- */

	static void yaffs_HandleShadowedObject(yaffs_Device dev, int objId,
			boolean backwardScanning)
	{
		yaffs_Object obj;

		if (!backwardScanning) {
			/* Handle YAFFS1 forward scanning case
			 * For YAFFS1 we always do the deletion
			 */

		} else {
			/* Handle YAFFS2 case (backward scanning)
			 * If the shadowed object exists then ignore.
			 */
			if (yaffs_FindObjectByNumber(dev, objId) != null) {
				return;
			}
		}

		/* Let's create it (if it does not exist) assuming it is a file so that it can do shrinking etc.
		 * We put it in unlinked dir to be cleaned up after the scanning
		 */
		obj =
			yaffs_FindOrCreateObjectByNumber(dev, objId,
					Guts_H.YAFFS_OBJECT_TYPE_FILE);
		yaffs_AddObjectToDirectory(dev.unlinkedDir, obj);
		obj.variant.fileVariant().shrinkSize = 0;
		obj.valid = true;		/* So that we don't read any other info for this file */

	}

//	typedef struct {
//	int seq;
//	int block;
//	} yaffs_BlockIndex;


	static void yaffs_HardlinkFixup(yaffs_Device dev, yaffs_Object hardList)
	{
		yaffs_Object hl;
		yaffs_Object in;

		while (hardList != null) {
			hl = hardList;
			hardList = (yaffs_Object) (hardList.hardLinks.next);

			in = yaffs_FindObjectByNumber(dev,
					hl.variant.hardLinkVariant().
					equivalentObjectId);

			if (in != null) {
				/* Add the hardlink pointers */
				hl.variant.hardLinkVariant().equivalentObject = in;
				devextras.list_add(hl.hardLinks, in.hardLinks);
			} else {
				/* Todo Need to report/handle this better.
				 * Got a problem... hardlink to a non-existant object
				 */
				hl.variant.hardLinkVariant().equivalentObject = null;
				devextras.INIT_LIST_HEAD(hl.hardLinks);

			}

		}

	}





	// XXX low priority - its only used for quicksort
	// XXX i thinks its a SerializableObject
	static int ybicmp(/*const void **/ Object a, /*const void **/ Object b){
		/*register */ int aseq = ((yaffs_BlockIndex)a).seq;
		/*register */ int bseq = ((yaffs_BlockIndex)b).seq;
		/*register */ int ablock = ((yaffs_BlockIndex)a).block;
		/*register */ int bblock = ((yaffs_BlockIndex)b).block;
		if( aseq == bseq )
			return ablock - bblock;
		else
			return aseq - bseq;

	}

	static yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
	static int blk;
	static int blockIterator;
	static int startIterator;
	static int endIterator;
	static int nBlocksToScan = 0;
	static boolean result;

	static int chunk;
	static int c;
	static int deleted;
	static /*yaffs_BlockState*/ int state;
	static yaffs_Object hardList = null;
	static yaffs_Object hl;
	static yaffs_BlockInfo bi;
	static long sequenceNumber;
	static yaffs_ObjectHeader oh;
	static yaffs_Object in;
	static yaffs_Object parent;
	static yaffs_BlockIndex[] blockIndex = null;
	static /*__u8 **/ byte[] chunkData;
	static final int chunkDataIndex = 0;

	static boolean yaffs_Scan(yaffs_Device dev)
	{
		nBlocksToScan = 0;
		hardList = null;
		blockIndex = null;

		int nBlocks = dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1;
		if (dev.subField1.isYaffs2) {
			yportenv.T(yportenv.YAFFS_TRACE_SCAN,
					("yaffs_Scan is not for YAFFS2!" + ydirectenv.TENDSTR));
			return Guts_H.YAFFS_FAIL;
		}

		//TODO  Throw all the yaffs2 stuuf out of yaffs_Scan since it is only for yaffs1 format.

		yportenv.T(yportenv.YAFFS_TRACE_SCAN,
				("yaffs_Scan starts  intstartblk %d intendblk %d..." + ydirectenv.TENDSTR),
				PrimitiveWrapperFactory.get(dev.subField2.internalStartBlock), PrimitiveWrapperFactory.get(dev.subField2.internalEndBlock));

		chunkData = yaffs_GetTempBuffer(dev, 25 /*Utils.__LINE__()*/);
//		chunkDataIndex = 0;

		dev.sequenceNumber = Guts_H.YAFFS_LOWEST_SEQUENCE_NUMBER;

		if (dev.subField1.isYaffs2) {
			blockIndex = ydirectenv.YMALLOC_BLOCKINDEX(nBlocks/* * sizeof(yaffs_BlockIndex)*/ );
		}

		/* Scan all the blocks to determine their state */
		for (blk = dev.subField2.internalStartBlock; blk <= dev.subField2.internalEndBlock; blk++) {
			bi = Guts_H.yaffs_GetBlockInfo(dev, blk);
			yaffs_ClearChunkBits(dev, blk);
			bi.setPagesInUse(0);
			bi.setSoftDeletions(0);

			IntegerPointer statePointer = new IntegerPointer();
			IntegerPointer sequenceNumberPointer = new IntegerPointer();
			yaffs_nand_C.yaffs_QueryInitialBlockState(dev, blk, statePointer, sequenceNumberPointer);
			state = statePointer.dereferenced;
			sequenceNumber = sequenceNumberPointer.dereferenced;

			bi.setBlockState(state);
			bi.setSequenceNumber((int)sequenceNumber);

			yportenv.T(yportenv.YAFFS_TRACE_SCAN_DEBUG,
					("Block scanning block %d state %d seq %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk),
					PrimitiveWrapperFactory.get(state), PrimitiveWrapperFactory.get((int)sequenceNumber));

			if (state == Guts_H.YAFFS_BLOCK_STATE_DEAD) {
				yportenv.T(yportenv.YAFFS_TRACE_BAD_BLOCKS,
						("block %d is bad" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk));
			} else if (state == Guts_H.YAFFS_BLOCK_STATE_EMPTY) {
				yportenv.T(yportenv.YAFFS_TRACE_SCAN_DEBUG,
						(("Block empty " + ydirectenv.TENDSTR)));
				dev.subField3.nErasedBlocks++;
				dev.subField3.nFreeChunks += dev.subField1.nChunksPerBlock;
			} else if (state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING) {

				/* Determine the highest sequence number */
				if (dev.subField1.isYaffs2 &&
						sequenceNumber >= Guts_H.YAFFS_LOWEST_SEQUENCE_NUMBER &&
						sequenceNumber < Guts_H.YAFFS_HIGHEST_SEQUENCE_NUMBER) {

					blockIndex[nBlocksToScan].seq = (int)sequenceNumber;
					blockIndex[nBlocksToScan].block = blk;

					nBlocksToScan++;

					if (sequenceNumber >= dev.sequenceNumber) {
						dev.sequenceNumber = sequenceNumber;
					}
				} else if (dev.subField1.isYaffs2) {
					/* TODO: Nasty sequence number! */
					yportenv.T(yportenv.YAFFS_TRACE_SCAN,

							("Block scanning block %d has bad sequence number %d"
									+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk), PrimitiveWrapperFactory.get((int)sequenceNumber));

				}
			}
		}

		/* Sort the blocks
		 * Dungy old bubble sort for now...
		 */
		if (dev.subField1.isYaffs2) {
			yaffs_BlockIndex temp = new yaffs_BlockIndex();
			int i;
			int j;

			for (i = 0; i < nBlocksToScan; i++)
				for (j = i + 1; j < nBlocksToScan; j++)
					if (blockIndex[i].seq > blockIndex[j].seq) {
						temp = blockIndex[j];
						blockIndex[j] = blockIndex[i];
						blockIndex[i] = temp;
					}
		}

		/* Now scan the blocks looking at the data. */
		if (dev.subField1.isYaffs2) {
			startIterator = 0;
			endIterator = nBlocksToScan - 1;
			yportenv.T(yportenv.YAFFS_TRACE_SCAN_DEBUG,
					("%d blocks to be scanned" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(nBlocksToScan));
		} else {
			startIterator = dev.subField2.internalStartBlock;
			endIterator = dev.subField2.internalEndBlock;
		}
		return yaffs_Scan_Sub1(dev);
	}

	static boolean yaffs_Scan_Sub1(yaffs_Device dev)
	{
		/* For each block.... */
		for (blockIterator = startIterator; blockIterator <= endIterator;
		blockIterator++) {

			if (dev.subField1.isYaffs2) {
				/* get the block to scan in the correct order */
				blk = blockIndex[blockIterator].block;
			} else {
				blk = blockIterator;
			}

			bi = Guts_H.yaffs_GetBlockInfo(dev, blk);
			state = bi.blockState();

			deleted = 0;

			/* For each chunk in each block that needs scanning....*/
			for (c = 0; c < dev.subField1.nChunksPerBlock &&
			state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING; c++) {
				/* Read the tags and decide what to do */
				chunk = blk * dev.subField1.nChunksPerBlock + c;

				result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, chunk, null, 0,
						tags);

				/* Let's have a good look at this chunk... */

				if (!dev.subField1.isYaffs2 && tags.chunkDeleted) {
					/* YAFFS1 only...
					 * A deleted chunk
					 */
					deleted++;
					dev.subField3.nFreeChunks++;
					/*yportenv.T((" %d %d deleted\n",blk,c)); */
				} else if (!tags.chunkUsed) {
					/* An unassigned chunk in the block
					 * This means that either the block is empty or 
					 * this is the one being allocated from
					 */

					if (c == 0) {
						/* We're looking at the first chunk in the block so the block is unused */
						state = Guts_H.YAFFS_BLOCK_STATE_EMPTY;
						dev.subField3.nErasedBlocks++;
					} else {
						/* this is the block being allocated from */
						yportenv.T(yportenv.YAFFS_TRACE_SCAN,

								(" Allocating from %d %d" + ydirectenv.TENDSTR),
								PrimitiveWrapperFactory.get(blk), PrimitiveWrapperFactory.get(c));
						state = Guts_H.YAFFS_BLOCK_STATE_ALLOCATING;
						dev.subField3.allocationBlock = blk;
						dev.subField3.allocationPage = c;
						dev.subField3.allocationBlockFinder = blk;	
						/* Set it to here to encourage the allocator to go forth from here. */

						/* Yaffs2 sanity check:
						 * This should be the one with the highest sequence number
						 */
						if (dev.subField1.isYaffs2
								&& (dev.sequenceNumber !=
									Utils.intAsUnsignedInt(bi.sequenceNumber()))) {
							yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,

									("yaffs: Allocation block %d was not highest sequence id:" +
											" block seq = %d, dev seq = %d"
											+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk),PrimitiveWrapperFactory.get((int)bi.sequenceNumber()),PrimitiveWrapperFactory.get((int)dev.sequenceNumber));
						}
					}

					dev.subField3.nFreeChunks += (dev.subField1.nChunksPerBlock - c);
				} else if (tags.chunkId > 0) {
					/* chunkId > 0 so it is a data chunk... */
					/*unsigned int*/ int endpos;

					yaffs_SetChunkBit(dev, blk, c);
					bi.setPagesInUse(bi.pagesInUse()+1);

					in = yaffs_FindOrCreateObjectByNumber(dev,
							tags.objectId,
							Guts_H.YAFFS_OBJECT_TYPE_FILE);
					/* PutChunkIntoFile checks for a clash (two data chunks with
					 * the same chunkId).
					 */
					yaffs_PutChunkIntoFile(in, tags.chunkId, chunk,
							1);
					endpos =
						(tags.chunkId - 1) * dev.subField1.nDataBytesPerChunk +
						tags.byteCount;
					if (in.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE
							&& in.variant.fileVariant().scannedFileSize <
							endpos) {
						in.variant.fileVariant().
						scannedFileSize = endpos;
						if (!dev.subField1.useHeaderFileSize) {
							in.variant.fileVariant().
							fileSize =
								in.variant.fileVariant().
								scannedFileSize;
						}

					}
					/* yportenv.T((" %d %d data %d %d\n",blk,c,tags.objectId,tags.chunkId));   */
				} else {
					yaffs_Scan_Sub11(dev);
				}
			}

			if (state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING) {
				/* If we got this far while scanning, then the block is fully allocated.*/
				state = Guts_H.YAFFS_BLOCK_STATE_FULL;
			}

			bi.setBlockState(state);

			/* Now let's see if it was dirty */
			if (bi.pagesInUse() == 0 &&
					!bi.hasShrinkHeader() &&
					bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_FULL) {
				yaffs_BlockBecameDirty(dev, blk);
			}

		}

		if (blockIndex != null) {
			ydirectenv.YFREE(blockIndex);
		}


		/* Ok, we've done all the scanning.
		 * Fix up the hard link chains.
		 * We should now have scanned all the objects, now it's time to add these 
		 * hardlinks.
		 */

		yaffs_HardlinkFixup(dev,hardList);

		/* Handle the unlinked files. Since they were left in an unlinked state we should
		 * just delete them.
		 */
		{
			list_head i;
			list_head n;

			yaffs_Object l;
			/* Soft delete all the unlinked files */
			i = dev.unlinkedDir.variant.directoryVariant().children.next();
			n = i.next();
//			list_for_each_safe(i, n,
//			&dev.unlinkedDir.variant.directoryVariant().
//			children) {
			while (i != dev.unlinkedDir.variant.directoryVariant().children) {
				if (i != null) {
					l = /*list_entry(i, yaffs_Object, siblings)*/ (yaffs_Object)i.list_entry;
					yaffs_DestroyObject(l);					
				}
				i = n;
				n = i.next();
			}
		}

		yaffs_ReleaseTempBuffer(dev, chunkData, 26 /*Utils.__LINE__()*/);

		yportenv.T(yportenv.YAFFS_TRACE_SCAN, (("yaffs_Scan ends" + ydirectenv.TENDSTR)));

		return Guts_H.YAFFS_OK;
	}

	// begin

	static void yaffs_Scan_Sub11(yaffs_Device dev)
	{
		/* chunkId == 0, so it is an ObjectHeader.
		 * Thus, we read in the object header and make the object
		 */
		yaffs_SetChunkBit(dev, blk, c);
		bi.setPagesInUse(bi.pagesInUse()+1);

		result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, chunk,
				chunkData, chunkDataIndex,
				null);

		oh = /*(yaffs_ObjectHeader *) chunkData*/ 
			new yaffs_ObjectHeader(chunkData, chunkDataIndex);

		in = yaffs_FindObjectByNumber(dev,
				tags.objectId);
		if (in != null && in.variantType != oh.type()) {
			/* This should not happen, but somehow
			 * Wev'e ended up with an objectId that has been reused but not yet 
			 * deleted, and worse still it has changed type. Delete the old object.
			 */

			yaffs_DestroyObject(in);

			in = null;
		}

		in = yaffs_FindOrCreateObjectByNumber(dev,
				tags.objectId,
				oh.type());

		if (oh.shadowsObject() > 0) {
			yaffs_HandleShadowedObject(dev,
					oh.shadowsObject(),
					false);
		}

		if (in.valid) {
			/* We have already filled this one. We have a duplicate and need to resolve it. */

			/*unsigned*/ int existingSerial = Utils.byteAsUnsignedByte(in.serial);
			/*unsigned*/ int newSerial = tags.serialNumber;

			if (dev.subField1.isYaffs2 ||
					((existingSerial + 1) & 3) ==
						newSerial) {
				/* Use new one - destroy the exisiting one */
				yaffs_DeleteChunk(dev,
						in.chunkId,
						true, 27 /*Utils.__LINE__()*/);
				in.valid = false;
			} else {
				/* Use existing - destroy this one. */
				yaffs_DeleteChunk(dev, chunk, true,
						28 /*Utils.__LINE__()*/);
			}
		}

		if (!in.valid &&
				(tags.objectId == Guts_H.YAFFS_OBJECTID_ROOT ||
						tags.objectId == Guts_H.YAFFS_OBJECTID_LOSTNFOUND)) {
			/* We only load some info, don't fiddle with directory structure */
			in.valid = true;
			in.variantType = oh.type();

			in.yst_mode = oh.yst_mode();
//			#ifdef CONFIG_YAFFS_WINCE
//			in.win_atime[0] = oh.win_atime[0];
//			in.win_ctime[0] = oh.win_ctime[0];
//			in.win_mtime[0] = oh.win_mtime[0];
//			in.win_atime[1] = oh.win_atime[1];
//			in.win_ctime[1] = oh.win_ctime[1];
//			in.win_mtime[1] = oh.win_mtime[1];
//			#else
			in.yst_uid = oh.yst_uid();
			in.yst_gid = oh.yst_gid();
			in.yst_atime = oh.yst_atime();
			in.yst_mtime = oh.yst_mtime();
			in.yst_ctime = oh.yst_ctime();
			in.yst_rdev = oh.yst_rdev();
//			#endif
			in.chunkId = chunk;

		} else if (!in.valid) {
			/* we need to load this info */

			in.valid = true;
			in.variantType = oh.type();

			in.yst_mode = oh.yst_mode();
//			#ifdef CONFIG_YAFFS_WINCE
//			in.win_atime[0] = oh.win_atime[0];
//			in.win_ctime[0] = oh.win_ctime[0];
//			in.win_mtime[0] = oh.win_mtime[0];
//			in.win_atime[1] = oh.win_atime[1];
//			in.win_ctime[1] = oh.win_ctime[1];
//			in.win_mtime[1] = oh.win_mtime[1];
//			#else
			in.yst_uid = oh.yst_uid();
			in.yst_gid = oh.yst_gid();
			in.yst_atime = oh.yst_atime();
			in.yst_mtime = oh.yst_mtime();
			in.yst_ctime = oh.yst_ctime();
			in.yst_rdev = oh.yst_rdev();
//			#endif
			in.chunkId = chunk;

			yaffs_SetObjectName(in, oh.name(), oh.nameIndex());
			in.dirty = false;

			/* directory stuff...
			 * hook up to parent
			 */

			parent =
				yaffs_FindOrCreateObjectByNumber
				(dev, oh.parentObjectId(),
						Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY);
			if (parent.variantType ==
				Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN) {
				/* Set up as a directory */
				parent.variantType =
					Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY;
				devextras.INIT_LIST_HEAD(parent.variant.
						directoryVariant().
						children);
			} else if (parent.variantType !=
				Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY)
			{
				/* Hoosterman, another problem....
				 * We're trying to use a non-directory as a directory
				 */

				yportenv.T(yportenv.YAFFS_TRACE_ERROR,

						("yaffs tragedy: attempting to use non-directory as" +
								" a directory in scan. Put in lost+found."
								+ ydirectenv.TENDSTR));
				parent = dev.lostNFoundDir;
			}
		}

		yaffs_AddObjectToDirectory(parent, in);

		if (false && (parent == dev.deletedDir ||
				parent == dev.unlinkedDir)) {
			in.sub.deleted = true;	/* If it is unlinked at start up then it wants deleting */
			dev.nDeletedFiles++;
		}
		/* Note re hardlinks.
		 * Since we might scan a hardlink before its equivalent object is scanned
		 * we put them all in a list.
		 * After scanning is complete, we should have all the objects, so we run through this
		 * list and fix up all the chains.              
		 */

		switch (in.variantType) {
		case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:	
			/* Todo got a problem */
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_FILE:
			if (dev.subField1.isYaffs2
					&& oh.isShrink()) {
				/* Prune back the shrunken chunks */
				yaffs_PruneResizedChunks
				(in, oh.fileSize());
				/* Mark the block as having a shrinkHeader */
				bi.setHasShrinkHeader(true);
			}

			if (dev.subField1.useHeaderFileSize)

				in.variant.fileVariant().
				fileSize =
					oh.fileSize();

			break;
		case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
			in.variant.hardLinkVariant().
			equivalentObjectId =
				oh.equivalentObjectId();
			in.hardLinks.next =
				/*(list_head)*/ 
				hardList;
			hardList = in;
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
			/* Do nothing */
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
			/* Do nothing */
			break;
		case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
			// XXX PORT Dynamic memory allocation, but this is during scan.
			in.variant.symLinkVariant().alias =
				yaffs_CloneString(oh.alias(), oh.aliasIndex());
			in.variant.symLinkVariant().aliasIndex = 0;
			break;
		}

		if (parent == dev.deletedDir) {
			yaffs_DestroyObject(in);
			bi.setHasShrinkHeader(true);
		}
	}


	// end

	static void yaffs_CheckObjectDetailsLoaded(yaffs_Object in)
	{
		/*__u8 **/ byte[] chunkData; final int chunkDataIndex;
		yaffs_ObjectHeader oh;
		yaffs_Device dev = in.myDev;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		boolean result;

//		#if 0
//		yportenv.T(yportenv.YAFFS_TRACE_SCAN,(("details for object %d %s loaded" + ydirectenv.TENDSTR),
//		in.objectId,
//		in.lazyLoaded ? "not yet" : "already"));
//		#endif

		if(in.lazyLoaded){
			in.lazyLoaded = false;
			chunkData = yaffs_GetTempBuffer(dev, 29 /*Utils.__LINE__()*/);
			chunkDataIndex = 0;

			result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev,in.chunkId,chunkData,chunkDataIndex,tags);
			oh = /*(yaffs_ObjectHeader *) chunkData*/ new yaffs_ObjectHeader(chunkData,chunkDataIndex);		

			in.yst_mode = oh.yst_mode();
//			#ifdef CONFIG_YAFFS_WINCE
//			in.win_atime[0] = oh.win_atime[0];
//			in.win_ctime[0] = oh.win_ctime[0];
//			in.win_mtime[0] = oh.win_mtime[0];
//			in.win_atime[1] = oh.win_atime[1];
//			in.win_ctime[1] = oh.win_ctime[1];
//			in.win_mtime[1] = oh.win_mtime[1];
//			#else
			in.yst_uid = oh.yst_uid();
			in.yst_gid = oh.yst_gid();
			in.yst_atime = oh.yst_atime();
			in.yst_mtime = oh.yst_mtime();
			in.yst_ctime = oh.yst_ctime();
			in.yst_rdev = oh.yst_rdev();

//			#endif
			yaffs_SetObjectName(in, oh.name(),oh.nameIndex());

			if(in.variantType == Guts_H.YAFFS_OBJECT_TYPE_SYMLINK)
			{
				in.variant.symLinkVariant().alias =
					yaffs_CloneString(oh.alias(),oh.aliasIndex());
				in.variant.symLinkVariant().aliasIndex = 0;
			}

			yaffs_ReleaseTempBuffer(dev,chunkData, 30 /*Utils.__LINE__()*/);
		}
	}

	static boolean itsUnlinked;
	static int fileSize;
	static boolean isShrink;
	static boolean foundChunksInBlock;
	static int equivalentObjectId;
	static boolean altBlockIndex = false;

	static boolean yaffs_ScanBackwards(yaffs_Device dev)
	{
		tags = new yaffs_ExtendedTags();
//		int blk;
//		int blockIterator;
//		int startIterator;
//		int endIterator;
		nBlocksToScan = 0;

//		int chunk;
//		boolean result;
//		int c;
//		boolean deleted;
//		/*yaffs_BlockState*/ int state;
		hardList = null;
//		yaffs_BlockInfo bi;
//		long sequenceNumber;
//		yaffs_ObjectHeader oh;
//		yaffs_Object in;
//		yaffs_Object parent;
		int nBlocks = dev.subField2.internalEndBlock - dev.subField2.internalStartBlock + 1;
//		boolean itsUnlinked;
//		/*__u8 **/ byte[] chunkData;
//		final int chunkDataIndex;

//		int fileSize;
//		boolean isShrink;
//		boolean foundChunksInBlock;
//		int equivalentObjectId;


		blockIndex = null;
		altBlockIndex = false;

		if (!dev.subField1.isYaffs2) {
			yportenv.T(yportenv.YAFFS_TRACE_SCAN,
					(("yaffs_ScanBackwards is only for YAFFS2!" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		yportenv.T(yportenv.YAFFS_TRACE_SCAN,

				("yaffs_ScanBackwards starts  intstartblk %d intendblk %d..."
						+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(dev.subField2.internalStartBlock), PrimitiveWrapperFactory.get(dev.subField2.internalEndBlock));


		dev.sequenceNumber = Guts_H.YAFFS_LOWEST_SEQUENCE_NUMBER;

		blockIndex = ydirectenv.YMALLOC_BLOCKINDEX(nBlocks/* * sizeof(yaffs_BlockIndex)*/);

		if(!(blockIndex != null)) {
			blockIndex = ydirectenv.YMALLOC_ALT_BLOCKINDEX(nBlocks/* * sizeof(yaffs_BlockIndex)*/);
			altBlockIndex = true;
		}

		if(!(blockIndex != null)) {
			yportenv.T(yportenv.YAFFS_TRACE_SCAN,
					(("yaffs_Scan() could not allocate block index!" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		chunkData = yaffs_GetTempBuffer(dev, 31 /*Utils.__LINE__()*/);
		//chunkDataIndex = 0; XXX

		/* Scan all the blocks to determine their state */
		for (blk = dev.subField2.internalStartBlock; blk <= dev.subField2.internalEndBlock; blk++) {
			bi = Guts_H.yaffs_GetBlockInfo(dev, blk);
			yaffs_ClearChunkBits(dev, blk);
			bi.setPagesInUse(0);
			bi.setSoftDeletions(0);

			IntegerPointer statePointer = new IntegerPointer();
			IntegerPointer sequenceNumberPointer = new IntegerPointer();
			yaffs_nand_C.yaffs_QueryInitialBlockState(dev, blk, statePointer, sequenceNumberPointer);
			state = statePointer.dereferenced;
			sequenceNumber = sequenceNumberPointer.dereferenced;

			bi.setBlockState(state);
			bi.setSequenceNumber((int)sequenceNumber);

			if(bi.sequenceNumber() == Guts_H.YAFFS_SEQUENCE_CHECKPOINT_DATA)
				bi.setBlockState(state = Guts_H.YAFFS_BLOCK_STATE_CHECKPOINT);

			yportenv.T(yportenv.YAFFS_TRACE_SCAN_DEBUG,
					("Block scanning block %d state %d seq %d" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk),
					PrimitiveWrapperFactory.get(state), PrimitiveWrapperFactory.get((int)sequenceNumber));


			if(state == Guts_H.YAFFS_BLOCK_STATE_CHECKPOINT){
				/* todo .. fix free space ? */

			} else if (state == Guts_H.YAFFS_BLOCK_STATE_DEAD) {
				yportenv.T(yportenv.YAFFS_TRACE_BAD_BLOCKS,
						("block %d is bad" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk));
			} else if (state == Guts_H.YAFFS_BLOCK_STATE_EMPTY) {
				yportenv.T(yportenv.YAFFS_TRACE_SCAN_DEBUG,
						("Block empty " + ydirectenv.TENDSTR));
				dev.subField3.nErasedBlocks++;
				dev.subField3.nFreeChunks += dev.subField1.nChunksPerBlock;
			} else if (state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING) {

				/* Determine the highest sequence number */
				if (dev.subField1.isYaffs2 &&
						sequenceNumber >= Guts_H.YAFFS_LOWEST_SEQUENCE_NUMBER &&
						sequenceNumber < Guts_H.YAFFS_HIGHEST_SEQUENCE_NUMBER) {

					blockIndex[nBlocksToScan].seq = (int)sequenceNumber;
					blockIndex[nBlocksToScan].block = blk;

					nBlocksToScan++;

					if (sequenceNumber >= dev.sequenceNumber) {
						dev.sequenceNumber = sequenceNumber;
					}
				} else if (dev.subField1.isYaffs2) {
					/* TODO: Nasty sequence number! */
					yportenv.T(yportenv.YAFFS_TRACE_SCAN,

							("Block scanning block %d has bad sequence number %d"
									+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk), PrimitiveWrapperFactory.get((int)sequenceNumber));

				}
			}
		}

		yportenv.T(yportenv.YAFFS_TRACE_SCAN,
				("%d blocks to be sorted..." + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(nBlocksToScan));



		ydirectenv.YYIELD();

		/* Sort the blocks */	// XXX use the qsort impl. taken from http://www.google.com/codesearch?hl=en&q=+lang:java+quicksort+show:GbJN4YS8fv4:bWk7RV6TTho:R2hTeVo7FHc&sa=N&cd=2&ct=rc&cs_p=http://ftp.mozilla.org/pub/mozilla.org/mozilla/releases/mozilla1.8a3/src/mozilla-source-1.8a3.tar.bz2&cs_f=mozilla/gc/boehm/leaksoup/QuickSort.java#a0
//		#ifndef CONFIG_YAFFS_USE_OWN_SORT
//		{
//		/* Use qsort now. */
//		qsort(blockIndex, nBlocksToScan, sizeof(yaffs_BlockIndex), ybicmp);
//		}
//		#else
		{
			/* Dungy old bubble sort... */

			yaffs_BlockIndex temp = new yaffs_BlockIndex();
			int i;
			int j;

			for (i = 0; i < nBlocksToScan; i++)
				for (j = i + 1; j < nBlocksToScan; j++)
					if (blockIndex[i].seq > blockIndex[j].seq) {
						temp = blockIndex[j];
						blockIndex[j] = blockIndex[i];
						blockIndex[i] = temp;
					}
		}
//		#endif

		ydirectenv.YYIELD();

		yportenv.T(yportenv.YAFFS_TRACE_SCAN, ("...done" + ydirectenv.TENDSTR));

		/* Now scan the blocks looking at the data. */
		startIterator = 0;
		endIterator = nBlocksToScan - 1;
		yportenv.T(yportenv.YAFFS_TRACE_SCAN_DEBUG,
				("%d blocks to be scanned" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(nBlocksToScan));

		return yaffs_ScanBackward_Sub0(dev);
	}

	static boolean yaffs_ScanBackward_Sub0(yaffs_Device dev)
	{
		/* For each block.... backwards */
		for (blockIterator = endIterator; blockIterator >= startIterator;
		blockIterator--) {
			/* Cooperative multitasking! This loop can run for so
			   long that watchdog timers expire. */
			ydirectenv.YYIELD();

			/* get the block to scan in the correct order */
			blk = blockIndex[blockIterator].block;

			bi = Guts_H.yaffs_GetBlockInfo(dev, blk);
			state = bi.blockState();

			deleted = 0;

			/* For each chunk in each block that needs scanning.... */
			foundChunksInBlock = false;
			for (c = dev.subField1.nChunksPerBlock - 1; c >= 0 &&
			(state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING ||
					state == Guts_H.YAFFS_BLOCK_STATE_ALLOCATING); c--) {
				/* Scan backwards... 
				 * Read the tags and decide what to do
				 */
				chunk = blk * dev.subField1.nChunksPerBlock + c;

				result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev, chunk, null, 0,
						tags);

				/* Let's have a good look at this chunk... */

				if (!tags.chunkUsed) {
					/* An unassigned chunk in the block.
					 * If there are used chunks after this one, then
					 * it is a chunk that was skipped due to failing the erased
					 * check. Just skip it so that it can be deleted.
					 * But, more typically, We get here when this is an unallocated
					 * chunk and his means that either the block is empty or 
					 * this is the one being allocated from
					 */

					if(foundChunksInBlock)
					{
						/* This is a chunk that was skipped due to failing the erased check */

					} else if (c == 0) {
						/* We're looking at the first chunk in the block so the block is unused */
						state = Guts_H.YAFFS_BLOCK_STATE_EMPTY;
						dev.subField3.nErasedBlocks++;
					} else {
						if (state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING ||
								state == Guts_H.YAFFS_BLOCK_STATE_ALLOCATING) {
							if(dev.sequenceNumber == Utils.intAsUnsignedInt(bi.sequenceNumber())) {
								/* this is the block being allocated from */

								yportenv.T(yportenv.YAFFS_TRACE_SCAN,

										(" Allocating from %d %d"
												+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blk), PrimitiveWrapperFactory.get(c));

								state = Guts_H.YAFFS_BLOCK_STATE_ALLOCATING;
								dev.subField3.allocationBlock = blk;
								dev.subField3.allocationPage = c;
								dev.subField3.allocationBlockFinder = blk;	
							}
							else {
								/* This is a partially written block that is not
								 * the current allocation block. This block must have
								 * had a write failure, so set up for retirement.
								 */

								bi.setNeedsRetiring(true);
								bi.setGcPrioritise(true);

								yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
										("Partially written block %d being set for retirement" + ydirectenv.TENDSTR),
										PrimitiveWrapperFactory.get(blk));
							}

						}

					}

					dev.subField3.nFreeChunks++;

				} else if (tags.chunkId > 0) {
					/* chunkId > 0 so it is a data chunk... */
					/*unsigned int*/ int endpos;
					/*__u32*/ int chunkBase =
						(tags.chunkId - 1) * dev.subField1.nDataBytesPerChunk;

					foundChunksInBlock = true;


					yaffs_SetChunkBit(dev, blk, c);
					bi.setPagesInUse(bi.pagesInUse()+1);

					in = yaffs_FindOrCreateObjectByNumber(dev,
							tags.
							objectId,
							Guts_H.YAFFS_OBJECT_TYPE_FILE);
					if (in.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE
							&& chunkBase <
							Utils.intAsUnsignedInt(in.variant.fileVariant().shrinkSize)) {
						/* This has not been invalidated by a resize */
						yaffs_PutChunkIntoFile(in, tags.chunkId,
								chunk, -1);

						/* File size is calculated by looking at the data chunks if we have not 
						 * seen an object header yet. Stop this practice once we find an object header.
						 */
						endpos =
							(tags.chunkId -
									1) * dev.subField1.nDataBytesPerChunk +
									tags.byteCount;

						if (!in.valid &&	/* have not got an object header yet */
								in.variant.fileVariant().
								scannedFileSize < endpos) {
							in.variant.fileVariant().
							scannedFileSize = endpos;
							in.variant.fileVariant().
							fileSize =
								in.variant.fileVariant().
								scannedFileSize;
						}

					} else {
						/* This chunk has been invalidated by a resize, so delete */
						yaffs_DeleteChunk(dev, chunk, true, 32 /*Utils.__LINE__()*/);

					}
				} else {
					/* chunkId == 0, so it is an ObjectHeader.
					 * Thus, we read in the object header and make the object
					 */
					foundChunksInBlock = true;

					yaffs_SetChunkBit(dev, blk, c);
					bi.setPagesInUse(bi.pagesInUse()+1);

					oh = null;
					in = null;

					if (tags.extraHeaderInfoAvailable) {
						in = yaffs_FindOrCreateObjectByNumber
						(dev, tags.objectId,
								tags.extraObjectType);
					}

					if (!(in != null) ||
//							#ifdef CONFIG_YAFFS_DISABLE_LAZY_LOAD
//							!in.valid ||
//							#endif
							tags.extraShadows ||
							(!in.valid &&
									(tags.objectId == Guts_H.YAFFS_OBJECTID_ROOT ||
											tags.objectId == Guts_H.YAFFS_OBJECTID_LOSTNFOUND))
					) {

						/* If we don't have  valid info then we need to read the chunk
						 * TODO In future we can probably defer reading the chunk and 
						 * living with invalid data until needed.
						 */

						result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(dev,
								chunk,
								chunkData, chunkDataIndex,
								null);

						oh = /*(yaffs_ObjectHeader *)*/ new yaffs_ObjectHeader(chunkData,
								chunkDataIndex);

						if (!(in != null))
							in = yaffs_FindOrCreateObjectByNumber(dev, tags.objectId, oh.type());

					}

					if (!(in != null)) {
						/* TODO Hoosterman we have a problem! */
						yportenv.T(yportenv.YAFFS_TRACE_ERROR,

								("yaffs tragedy: Could not make object for object  %d  " +
										"at chunk %d during scan"
										+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(tags.objectId), PrimitiveWrapperFactory.get(chunk));

					}

					yaffs_ScanBackward_Subminus1(dev);
				}
			}

			if (state == Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING) {
				/* If we got this far while scanning, then the block is fully allocated. */
				state = Guts_H.YAFFS_BLOCK_STATE_FULL;
			}

			bi.setBlockState(state);

			/* Now let's see if it was dirty */
			if (bi.pagesInUse() == 0 &&
					!bi.hasShrinkHeader() &&
					bi.blockState() == Guts_H.YAFFS_BLOCK_STATE_FULL) {
				yaffs_BlockBecameDirty(dev, blk);
			}

		}
		return yaffs_ScanBackward_Sub1(dev);
	}

	static void yaffs_ScanBackward_Subminus1(yaffs_Device dev)
	{
		if (in.valid) {
			/* We have already filled this one.
			 * We have a duplicate that will be discarded, but 
			 * we first have to suck out resize info if it is a file.
			 */

			if ((in.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE) && 
					((oh != null && 
							oh.type() == Guts_H.YAFFS_OBJECT_TYPE_FILE)||
							(tags.extraHeaderInfoAvailable  &&
									tags.extraObjectType == Guts_H.YAFFS_OBJECT_TYPE_FILE))
			) {
				/*__u32*/ int thisSize =
					(oh != null) ? oh.fileSize() : tags.
							extraFileLength;
					/*__u32*/ int parentObjectId =
						(oh != null) ? oh.
								parentObjectId() : tags.
								extraParentObjectId;
								// PORT changed the name of the inner variable 
								/*unsigned*/ boolean isShrinkInner =	
									(oh != null) ? oh.isShrink() : tags.
											extraIsShrinkHeader;

									/* If it is deleted (unlinked at start also means deleted)
									 * we treat the file size as being zeroed at this point.
									 */
									if (parentObjectId ==
										Guts_H.YAFFS_OBJECTID_DELETED
										|| parentObjectId ==
											Guts_H.YAFFS_OBJECTID_UNLINKED) {
										thisSize = 0;
										isShrinkInner = true;
									}

									if (isShrinkInner &&
											Utils.intAsUnsignedInt(in.variant.fileVariant().
													shrinkSize) > thisSize) {
										in.variant.fileVariant().
										shrinkSize =
											thisSize;
									}

									if (isShrinkInner) {
										bi.setHasShrinkHeader(true);
									}

			}
			/* Use existing - destroy this one. */
			yaffs_DeleteChunk(dev, chunk, true, 34 /*Utils.__LINE__()*/);

		}

		if (!in.valid &&
				(tags.objectId == Guts_H.YAFFS_OBJECTID_ROOT ||
						tags.objectId ==
							Guts_H.YAFFS_OBJECTID_LOSTNFOUND)) {
			/* We only load some info, don't fiddle with directory structure */
			in.valid = true;

			if(oh != null) {
				in.variantType = oh.type();

				in.yst_mode = oh.yst_mode();
//				#ifdef CONFIG_YAFFS_WINCE
//				in.win_atime[0] = oh.win_atime[0];
//				in.win_ctime[0] = oh.win_ctime[0];
//				in.win_mtime[0] = oh.win_mtime[0];
//				in.win_atime[1] = oh.win_atime[1];
//				in.win_ctime[1] = oh.win_ctime[1];
//				in.win_mtime[1] = oh.win_mtime[1];
//				#else
				in.yst_uid = oh.yst_uid();
				in.yst_gid = oh.yst_gid();
				in.yst_atime = oh.yst_atime();
				in.yst_mtime = oh.yst_mtime();
				in.yst_ctime = oh.yst_ctime();
				in.yst_rdev = oh.yst_rdev();

//				#endif
			} else {
				in.variantType = tags.extraObjectType;
				in.lazyLoaded = true;
			}

			in.chunkId = chunk;

		} else if (!in.valid) {
			/* we need to load this info */

			in.valid = true;
			in.chunkId = chunk;

			if(oh != null) {
				in.variantType = oh.type();

				in.yst_mode = oh.yst_mode();
//				#ifdef CONFIG_YAFFS_WINCE
//				in.win_atime[0] = oh.win_atime[0];
//				in.win_ctime[0] = oh.win_ctime[0];
//				in.win_mtime[0] = oh.win_mtime[0];
//				in.win_atime[1] = oh.win_atime[1];
//				in.win_ctime[1] = oh.win_ctime[1];
//				in.win_mtime[1] = oh.win_mtime[1];
//				#else
				in.yst_uid = oh.yst_uid();
				in.yst_gid = oh.yst_gid();
				in.yst_atime = oh.yst_atime();
				in.yst_mtime = oh.yst_mtime();
				in.yst_ctime = oh.yst_ctime();
				in.yst_rdev = oh.yst_rdev();
//				#endif

				if (oh.shadowsObject() > 0) 
					yaffs_HandleShadowedObject(dev,
							oh.
							shadowsObject(),
							true);


				yaffs_SetObjectName(in, oh.name(), oh.nameIndex());
				parent =
					yaffs_FindOrCreateObjectByNumber
					(dev, oh.parentObjectId(),
							Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY);

				fileSize = oh.fileSize();
				isShrink = oh.isShrink();
				equivalentObjectId = oh.equivalentObjectId();

			}
			else {
				in.variantType = tags.extraObjectType;
				parent =
					yaffs_FindOrCreateObjectByNumber
					(dev, tags.extraParentObjectId,
							Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY);
				fileSize = tags.extraFileLength;
				isShrink = tags.extraIsShrinkHeader;
				equivalentObjectId = tags.extraEquivalentObjectId;
				in.lazyLoaded = true;

			}
			in.dirty = false;

			/* directory stuff...
			 * hook up to parent
			 */

			if (parent.variantType ==
				Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN) {
				/* Set up as a directory */
				parent.variantType =
					Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY;
				devextras.INIT_LIST_HEAD(parent.variant.
						directoryVariant().
						children);
			} else if (parent.variantType !=
				Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY)
			{
				/* Hoosterman, another problem....
				 * We're trying to use a non-directory as a directory
				 */

				yportenv.T(yportenv.YAFFS_TRACE_ERROR,

						("yaffs tragedy: attempting to use non-directory as" +
								" a directory in scan. Put in lost+found."
								+ ydirectenv.TENDSTR));
				parent = dev.lostNFoundDir;
			}

			yaffs_AddObjectToDirectory(parent, in);

			itsUnlinked = (parent == dev.deletedDir) ||
			(parent == dev.unlinkedDir);

			if (isShrink) {
				/* Mark the block as having a shrinkHeader */
				bi.setHasShrinkHeader(true);
			}

			/* Note re hardlinks.
			 * Since we might scan a hardlink before its equivalent object is scanned
			 * we put them all in a list.
			 * After scanning is complete, we should have all the objects, so we run
			 * through this list and fix up all the chains.              
			 */

			switch (in.variantType) {
			case Guts_H.YAFFS_OBJECT_TYPE_UNKNOWN:	
				/* Todo got a problem */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_FILE:

				if (in.variant.fileVariant().
						scannedFileSize < fileSize) {
					/* This covers the case where the file size is greater
					 * than where the data is
					 * This will happen if the file is resized to be larger 
					 * than its current data extents.
					 */
					in.variant.fileVariant().fileSize = fileSize;
					in.variant.fileVariant().scannedFileSize =
						in.variant.fileVariant().fileSize;
				}

				if (isShrink &&
						Utils.intAsUnsignedInt(in.variant.fileVariant().shrinkSize) > fileSize) {
					in.variant.fileVariant().shrinkSize = fileSize;
				}

				break;
			case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
				if(!itsUnlinked) {
					in.variant.hardLinkVariant().equivalentObjectId =
						equivalentObjectId;
					in.hardLinks.next =
						/*(list_head)*/ hardList;
					hardList = in;
				}
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
				/* Do nothing */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
				/* Do nothing */
				break;
			case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
				if(oh != null)
				{
					in.variant.symLinkVariant().alias =
						yaffs_CloneString(oh.
								alias(), oh.aliasIndex());
					in.variant.symLinkVariant().aliasIndex = 0;
				}
				break;
			}
		}
	}

	static boolean yaffs_ScanBackward_Sub1(yaffs_Device dev)
	{

		if (altBlockIndex) 
			ydirectenv.YFREE_ALT(blockIndex);
		else
			ydirectenv.YFREE(blockIndex);

		/* Ok, we've done all the scanning.
		 * Fix up the hard link chains.
		 * We should now have scanned all the objects, now it's time to add these 
		 * hardlinks.
		 */
		yaffs_HardlinkFixup(dev,hardList);


		/*
		 *  Sort out state of unlinked and deleted objects.
		 */
		{
			list_head i;
			list_head n;

			yaffs_Object l;

			/* Soft delete all the unlinked files */
			i = dev.unlinkedDir.variant.directoryVariant().children.next();
			n = i.next();
//			list_for_each_safe(i, n,
//			dev.unlinkedDir.variant.directoryVariant().
//			children) {
			while (i != dev.unlinkedDir.variant.directoryVariant().children) {
				if (i != null) {
					l = /*list_entry(i, yaffs_Object, siblings)*/ (yaffs_Object)i.list_entry;
					yaffs_DestroyObject(l);
				}
				i = n;
				n = i.next();
			}

			/* Soft delete all the deletedDir files */
			i = dev.deletedDir.variant.directoryVariant().children.next();
			n = i.next();
//			list_for_each_safe(i, n,
//			dev.deletedDir.variant.directoryVariant().
//			children) {
			while (i != dev.deletedDir.variant.directoryVariant().children) {
				if (i != null) {
					l = /*list_entry(i, yaffs_Object, siblings)*/ (yaffs_Object)i.list_entry;
					yaffs_DestroyObject(l);
				}
				i = n;
				n = i.next();
			}
		}

		yaffs_ReleaseTempBuffer(dev, chunkData, 35 /*Utils.__LINE__()*/);

		yportenv.T(yportenv.YAFFS_TRACE_SCAN, (("yaffs_ScanBackwards ends" + ydirectenv.TENDSTR)));

		return Guts_H.YAFFS_OK;
	}

	/*------------------------------  Directory Functions ----------------------------- */

	static void yaffs_RemoveObjectFromDirectory(yaffs_Object  obj)
	{
		yaffs_Device dev = obj.myDev;

		if(dev != null && dev.subField1.removeObjectCallback != null)
			dev.subField1.removeObjectCallback.yaffsfs_RemoveObjectCallback(obj);

		devextras.list_del_init(obj.siblings);
		obj.parent = null;
	}


	static void yaffs_AddObjectToDirectory(yaffs_Object  directory,
			yaffs_Object  obj)
	{

		if (!(directory != null)) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,

					("tragedy: Trying to add an object to a null pointer directory"
							+ ydirectenv.TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		if (directory.variantType != Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,

					("tragedy: Trying to add an object to a non-directory"
							+ ydirectenv.TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		if (obj.siblings.prev == null) {
			/* Not initialised */
			devextras.INIT_LIST_HEAD(obj.siblings);

		} else if (!devextras.list_empty(obj.siblings)) {
			/* If it is holed up somewhere else, un hook it */
			yaffs_RemoveObjectFromDirectory(obj);
		}
		/* Now add it */
		devextras.list_add(obj.siblings, directory.variant.directoryVariant().children);
		obj.parent = directory;

		if (directory == obj.myDev.unlinkedDir
				|| directory == obj.myDev.deletedDir) {
			obj.sub.unlinked = true;
			obj.myDev.nUnlinkedFiles++;
			obj.renameAllowed = false;
		}
	}

	static yaffs_Object yaffs_FindObjectByName(yaffs_Object  directory,
			/*const YCHAR **/ byte[] name, int nameIndex)
	{
		short sum;

		list_head i;
		/*YCHAR buffer[Guts_H.YAFFS_MAX_NAME_LENGTH + 1]*/ byte[] buffer = new byte[Guts_H.YAFFS_MAX_NAME_LENGTH + 1];
		final int bufferIndex = 0;

		yaffs_Object l;

		if (!(name != null)) {
			return null;
		}

		if (!(directory != null)) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(
							("tragedy: yaffs_FindObjectByName: null pointer directory"
									+ ydirectenv.TENDSTR)));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		if (directory.variantType != Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(
							("tragedy: yaffs_FindObjectByName: non-directory" + ydirectenv.TENDSTR)));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		sum = yaffs_CalcNameSum(name, nameIndex);

//		list_for_each(i, &directory.variant.directoryVariant.children) {
		for (i = directory.variant.directoryVariant().children.next(); 
		i != directory.variant.directoryVariant().children; i = i.next()) {
			if (i != null) {
				l = /*list_entry(i, yaffs_Object, siblings)*/ (yaffs_Object)i.list_entry;

				yaffs_CheckObjectDetailsLoaded(l);

				/* Special case for lost-n-found */
				if (l.objectId == Guts_H.YAFFS_OBJECTID_LOSTNFOUND) {
					if (ydirectenv.yaffs_strcmp(name, nameIndex, ydirectenv.YAFFS_LOSTNFOUND_NAME, 0) == 0) {
						return l;
					}
				} else if (ydirectenv.yaffs_SumCompare(l.sum, sum) || l.chunkId <= 0)	
				{
					/* LostnFound cunk called Objxxx
					 * Do a real check
					 */
					yaffs_GetObjectName(l, buffer, bufferIndex,
							Guts_H.YAFFS_MAX_NAME_LENGTH);
					if (ydirectenv.yaffs_strcmp(name, nameIndex, buffer, bufferIndex) == 0) {
						return l;
					}

				}
			}
		}

		return null;
	}


//	#if 0
//	int yaffs_ApplyToDirectoryChildren(yaffs_Object  theDir,
//	int (*fn) (yaffs_Object ))
//	{
//	struct list_head *i;
//	yaffs_Object l;

//	if (!theDir) {
//	yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
//	(TSTR
//	("tragedy: yaffs_FindObjectByName: null pointer directory"
//	+ ydirectenv.TENDSTR)));
//	YBUG();
//	}
//	if (theDir.variantType != Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY) {
//	yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
//	(TSTR
//	("tragedy: yaffs_FindObjectByName: non-directory" + ydirectenv.TENDSTR)));
//	YBUG();
//	}

//	list_for_each(i, &theDir.variant.directoryVariant.children) {
//	if (i) {
//	l = list_entry(i, yaffs_Object, siblings);
//	if (l && !fn(l)) {
//	return Guts_H.YAFFS_FAIL;
//	}
//	}
//	}

//	return Guts_H.YAFFS_OK;

//	}
//	#endif

	/* GetEquivalentObject dereferences any hard links to get to the
	 * actual object.
	 */

	static yaffs_Object yaffs_GetEquivalentObject(yaffs_Object  obj)
	{
		if (obj != null && obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK) {
			/* We want the object id of the equivalent object, not this one */
			obj = obj.variant.hardLinkVariant().equivalentObject;
		}
		return obj;

	}

	static int yaffs_GetObjectName(yaffs_Object  obj, /*YCHAR **/ byte[] name, 
			int nameIndex,int buffSize)
	{
		Unix.memset(name, nameIndex, (byte)0, buffSize/* * sizeof(YCHAR)*/ );

		yaffs_CheckObjectDetailsLoaded(obj);

		if (obj.objectId == Guts_H.YAFFS_OBJECTID_LOSTNFOUND) {
			ydirectenv.yaffs_strncpy(name, nameIndex, ydirectenv.YAFFS_LOSTNFOUND_NAME, 0, buffSize - 1);
		} else if (obj.chunkId <= 0) {
			/*YCHAR locName[20]*/ byte[] locName = new byte[20];
			final int locNameIndex = 0;
			/* make up a name */
			Unix.xprintfArgs[0] = PrimitiveWrapperFactory.get(ydirectenv.YAFFS_LOSTNFOUND_PREFIX); 
			Unix.xprintfArgs[1] = PrimitiveWrapperFactory.get(0);
			Unix.xprintfArgs[2] = PrimitiveWrapperFactory.get(obj.objectId);
			Unix.sprintf(locName, locNameIndex, "%a%d");
			ydirectenv.yaffs_strncpy(name, nameIndex, locName, locNameIndex, buffSize - 1);

		}
//		#ifdef CONFIG_YAFFS_SHORT_NAMES_IN_RAM
		else if (obj.shortName[obj.shortNameIndex] != 0) {
			ydirectenv.yaffs_strcpy(name, nameIndex, obj.shortName, obj.shortNameIndex);
		}
//		#endif
		else {
			boolean result;
			/*__u8 **/ byte[] buffer = yaffs_GetTempBuffer(obj.myDev, 36 /*Utils.__LINE__()*/);
			final int bufferIndex = 0;

			yaffs_ObjectHeader oh = /*(yaffs_ObjectHeader) buffer*/ 
				new yaffs_ObjectHeader(buffer, bufferIndex);

			Unix.memset(buffer, bufferIndex, (byte)0, obj.myDev.subField1.nDataBytesPerChunk);

			if (obj.chunkId >= 0) {
				result = yaffs_nand_C.yaffs_ReadChunkWithTagsFromNAND(obj.myDev,
						obj.chunkId, buffer, bufferIndex,
						null);
			}
			ydirectenv.yaffs_strncpy(name, nameIndex, oh.name(), oh.nameIndex(), buffSize - 1);

			yaffs_ReleaseTempBuffer(obj.myDev, buffer, 37 /*Utils.__LINE__()*/);
		}

		return ydirectenv.yaffs_strlen(name, nameIndex);
	}

	static int yaffs_GetObjectFileLength(yaffs_Object  obj)
	{

		/* Dereference any hard linking */
		obj = yaffs_GetEquivalentObject(obj);

		if (obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_FILE) {
			return obj.variant.fileVariant().fileSize;
		}
		if (obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_SYMLINK) {
			return ydirectenv.yaffs_strlen(obj.variant.symLinkVariant().alias,
					obj.variant.symLinkVariant().aliasIndex);
		} else {
			/* Only a directory should drop through to here */
			return obj.myDev.subField1.nDataBytesPerChunk;
		}
	}

	static int yaffs_GetObjectLinkCount(yaffs_Object  obj)
	{
		int count = 0;
		list_head i;

		if (!obj.sub.unlinked) {
			count++;	/* the object itself */
		}
//		list_for_each(i, &obj.hardLinks) {
		for (i = obj.hardLinks.next(); i != obj.hardLinks; i = i.next()) {
			count++;	/* add the hard links; */
		}
		return count;

	}

	static int yaffs_GetObjectInode(yaffs_Object  obj)
	{
		obj = yaffs_GetEquivalentObject(obj);

		return obj.objectId;
	}

	static /*unsigned*/ int yaffs_GetObjectType(yaffs_Object  obj)
	{
		obj = yaffs_GetEquivalentObject(obj);

		switch (obj.variantType) {
		case Guts_H.YAFFS_OBJECT_TYPE_FILE:
			return devextras.DT_REG;
//			break;
		case Guts_H.YAFFS_OBJECT_TYPE_DIRECTORY:
			return devextras.DT_DIR;
//			break;
		case Guts_H.YAFFS_OBJECT_TYPE_SYMLINK:
			return devextras.DT_LNK;
//			break;
		case Guts_H.YAFFS_OBJECT_TYPE_HARDLINK:
			return devextras.DT_REG;
//			break;
		case Guts_H.YAFFS_OBJECT_TYPE_SPECIAL:
			if (Unix.S_ISFIFO(obj.yst_mode))
				return devextras.DT_FIFO;
			if (Unix.S_ISCHR(obj.yst_mode))
				return devextras.DT_CHR;
			if (Unix.S_ISBLK(obj.yst_mode))
				return devextras.DT_BLK;
			if (Unix.S_ISSOCK(obj.yst_mode))
				return devextras.DT_SOCK;
		default:
			return devextras.DT_REG;
//		break;
		}
	}

	static /*YCHAR **/ byte[] yaffs_GetSymlinkAlias(yaffs_Object  obj)
	{
		obj = yaffs_GetEquivalentObject(obj);
		if (obj.variantType == Guts_H.YAFFS_OBJECT_TYPE_SYMLINK) {
			return yaffs_CloneString(obj.variant.symLinkVariant().alias,
					obj.variant.symLinkVariant().aliasIndex);
		} else {
			return yaffs_CloneString(new byte[] {'\0'}, 0);
		}
	}

//	#ifndef CONFIG_YAFFS_WINCE

	static boolean yaffs_SetAttributes(yaffs_Object  obj, iattr attr)
	{
		/*unsigned int*/ int valid = attr.ia_valid;

		if ((valid & devextras.ATTR_MODE) != 0)
			obj.yst_mode = attr.ia_mode;
		if ((valid & devextras.ATTR_UID) != 0)
			obj.yst_uid = attr.ia_uid;
		if ((valid & devextras.ATTR_GID) != 0)
			obj.yst_gid = attr.ia_gid;

		if ((valid & devextras.ATTR_ATIME) != 0)
			obj.yst_atime = /*Y_TIME_CONVERyportenv.T(*/ attr.ia_atime/*)*/;
		if ((valid & devextras.ATTR_CTIME) != 0)
			obj.yst_ctime = /*Y_TIME_CONVERyportenv.T(*/ attr.ia_ctime/*)*/;
		if ((valid & devextras.ATTR_MTIME) != 0)
			obj.yst_mtime = /*Y_TIME_CONVERyportenv.T(*/ attr.ia_mtime/*)*/;

		if ((valid & devextras.ATTR_SIZE) != 0)
			yaffs_ResizeFile(obj, attr.ia_size);

		yaffs_UpdateObjectHeader(obj, null, 0, true, false, 0);

		return Guts_H.YAFFS_OK;

	}
	static boolean yaffs_GetAttributes(yaffs_Object  obj, iattr attr)
	{
		/*unsigned int*/ int valid = 0;

		attr.ia_mode = obj.yst_mode;
		valid |= devextras.ATTR_MODE;
		attr.ia_uid = obj.yst_uid;
		valid |= devextras.ATTR_UID;
		attr.ia_gid = obj.yst_gid;
		valid |= devextras.ATTR_GID;

		/*Y_TIME_CONVERyportenv.T(*/ attr.ia_atime/*)*/ = obj.yst_atime;
		valid |= devextras.ATTR_ATIME;
		/*Y_TIME_CONVERyportenv.T(*/ attr.ia_ctime/*)*/ = obj.yst_ctime;
		valid |= devextras.ATTR_CTIME;
		/*Y_TIME_CONVERyportenv.T(*/ attr.ia_mtime/*)*/ = obj.yst_mtime;
		valid |= devextras.ATTR_MTIME;

		attr.ia_size = yaffs_GetFileSize(obj);
		valid |= devextras.ATTR_SIZE;

		attr.ia_valid = valid;

		return Guts_H.YAFFS_OK;

	}

//	#endif

//	#if 0
//	int yaffs_DumpObject(yaffs_Object  obj)
//	{
//	YCHAR name[257];

//	yaffs_GetObjectName(obj, name, 256);

//	yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
//	(TSTR
//	("Object %d, inode %d \"%s\"\n dirty %d valid %d serial %d sum %d"
//	" chunk %d type %d size %d\n"
//	+ ydirectenv.TENDSTR), obj.objectId, yaffs_GetObjectInode(obj), name,
//	obj.dirty, obj.valid, obj.serial, obj.sum, obj.chunkId,
//	yaffs_GetObjectType(obj), yaffs_GetObjectFileLength(obj)));

//	return Guts_H.YAFFS_OK;
//	}
//	#endif

	/*---------------------------- Initialisation code -------------------------------------- */

	static boolean yaffs_CheckDevFunctions(yaffs_Device dev)
	{

		/* Common functions, gotta have */
		if (!(dev.subField1.eraseBlockInNAND != null) || !(dev.subField1.initialiseNAND != null))
			return false;

//		#ifdef CONFIG_YAFFS_YAFFS2

		/* Can use the "with tags" style interface for yaffs1 or yaffs2 */
		if (dev.subField1.writeChunkWithTagsToNAND != null &&
				dev.subField1.readChunkWithTagsFromNAND != null &&
				!(dev.subField1.writeChunkToNAND != null) &&
				!(dev.subField1.readChunkFromNAND != null) &&
				dev.subField1.markNANDBlockBad != null && dev.subField1.queryNANDBlock != null)
			return true;
//		#endif

		/* Can use the "spare" style interface for yaffs1 */
		if (!dev.subField1.isYaffs2 &&
				!(dev.subField1.writeChunkWithTagsToNAND != null) &&
				!(dev.subField1.readChunkWithTagsFromNAND != null) &&
				dev.subField1.writeChunkToNAND != null &&
				dev.subField1.readChunkFromNAND != null &&
				!(dev.subField1.markNANDBlockBad != null) && !(dev.subField1.queryNANDBlock != null))
			return true;

		return false;		/* bad */
	}


	static void yaffs_CreateInitialDirectories(yaffs_Device dev)
	{
		/* Initialise the unlinked, deleted, root and lost and found directories */

		dev.lostNFoundDir = dev.rootDir =  null;
		dev.unlinkedDir = dev.deletedDir = null;

		dev.unlinkedDir =
			yaffs_CreateFakeDirectory(dev, Guts_H.YAFFS_OBJECTID_UNLINKED, Unix.S_IFDIR);
		dev.deletedDir =
			yaffs_CreateFakeDirectory(dev, Guts_H.YAFFS_OBJECTID_DELETED, Unix.S_IFDIR);

		dev.rootDir =
			yaffs_CreateFakeDirectory(dev, Guts_H.YAFFS_OBJECTID_ROOT,
					ydirectenv.YAFFS_ROOT_MODE | Unix.S_IFDIR);
		dev.lostNFoundDir =
			yaffs_CreateFakeDirectory(dev, Guts_H.YAFFS_OBJECTID_LOSTNFOUND,
					ydirectenv.YAFFS_LOSTNFOUND_MODE | Unix.S_IFDIR);
		yaffs_AddObjectToDirectory(dev.rootDir, dev.lostNFoundDir);
	}

	// XXX mark staticialized variables
	/*unsigned*/ static int x;
	static int bits;
	
	static boolean yaffs_GutsInitialise(yaffs_Device dev)
	{


		yportenv.T(yportenv.YAFFS_TRACE_TRACING, (("yaffs: yaffs_GutsInitialise()" + ydirectenv.TENDSTR)));

		/* Check stuff that must be set */

		if (!(dev != null)) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS, (("yaffs: Need a device" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		dev.subField2.internalStartBlock = dev.subField1.startBlock;
		dev.subField2.internalEndBlock = dev.subField1.endBlock;
		dev.subField2.blockOffset = 0;
		dev.subField2.chunkOffset = 0;
		dev.subField3.nFreeChunks = 0;

		if (dev.subField1.startBlock == 0) {
			dev.subField2.internalStartBlock = dev.subField1.startBlock + 1;
			dev.subField2.internalEndBlock = dev.subField1.endBlock + 1;
			dev.subField2.blockOffset = 1;
			dev.subField2.chunkOffset = dev.subField1.nChunksPerBlock;
		}

		/* Check geometry parameters. */

		if ((dev.subField1.isYaffs2 && dev.subField1.nDataBytesPerChunk < 1024) || 
				(!dev.subField1.isYaffs2 && dev.subField1.nDataBytesPerChunk != 512) || 
				dev.subField1.nChunksPerBlock < 2 || 
				dev.subField1.nReservedBlocks < 2 || 
				dev.subField2.internalStartBlock <= 0 || 
				dev.subField2.internalEndBlock <= 0 || 
				dev.subField2.internalEndBlock <= (dev.subField2.internalStartBlock + dev.subField1.nReservedBlocks + 2)	// otherwise it is too small
		) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,

					("yaffs: NAND geometry problems: chunk size %d, type is yaffs%s "
							+ ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(dev.subField1.nDataBytesPerChunk), PrimitiveWrapperFactory.get(dev.subField1.isYaffs2 ? "2" : ""));
			return Guts_H.YAFFS_FAIL;
		}

		if (yaffs_nand_C.yaffs_InitialiseNAND(dev) != Guts_H.YAFFS_OK) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(("yaffs: InitialiseNAND failed" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		/* Got the right mix of functions? */
		if (!yaffs_CheckDevFunctions(dev)) {
			/* Function missing */
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(
							("yaffs: device function(s) missing or wrong\n" + ydirectenv.TENDSTR)));

			return Guts_H.YAFFS_FAIL;
		}

		/* This is really a compilation check. */
		if (!yaffs_CheckStructures()) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(("yaffs_CheckStructures failed\n" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		if (dev.subField2.isMounted) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(("yaffs: device already mounted\n" + ydirectenv.TENDSTR)));
			return Guts_H.YAFFS_FAIL;
		}

		/* Finished with most checks. One or two more checks happen later on too. */

		dev.subField2.isMounted = true;



		/* OK now calculate a few things for the device */

		/*
		 *  Calculate all the chunk size manipulation numbers: 
		 */
		/* Start off assuming it is a power of 2 */
		dev.subField2.chunkShift = ShiftDiv(dev.subField1.nDataBytesPerChunk);
		dev.subField2.chunkMask = (1<<dev.subField2.chunkShift) - 1;

		if(dev.subField1.nDataBytesPerChunk == (dev.subField2.chunkMask + 1)){
			/* Yes it is a power of 2, disable crumbs */
			dev.subField2.crumbMask = 0;
			dev.subField2.crumbShift = 0;
			dev.subField2.crumbsPerChunk = 0;
		} else {
			/* Not a power of 2, use crumbs instead */
			dev.subField2.crumbShift = ShiftDiv(yaffs_PackedTags2TagsPart.SERIALIZED_LENGTH);
			dev.subField2.crumbMask = (1<<dev.subField2.crumbShift)-1;
			dev.subField2.crumbsPerChunk = dev.subField1.nDataBytesPerChunk/(1 << dev.subField2.crumbShift);
			dev.subField2.chunkShift = 0;
			dev.subField2.chunkMask = 0;
		}

		return yaffs_GutsInitialise_Sub0(dev);
	}
	
	protected static boolean yaffs_GutsInitialise_Sub0(yaffs_Device dev)
	{
		/*
		 * Calculate chunkGroupBits.
		 * We need to find the next power of 2 > than internalEndBlock
		 */

		x = dev.subField1.nChunksPerBlock * (dev.subField2.internalEndBlock + 1);	// XXX understand, I'm too tired now

		bits = ShiftsGE(x);

		/* Set up tnode width if wide tnodes are enabled. */
		if(!dev.subField1.wideTnodesDisabled){
			/* bits must be even so that we end up with 32-bit words */
			if((bits & 1) != 0)
				bits++;
			if(bits < 16)
				dev.subField2.tnodeWidth = 16;
			else
				dev.subField2.tnodeWidth = bits;
		}
		else
			dev.subField2.tnodeWidth = 16;

		dev.subField2.tnodeMask = (1<<dev.subField2.tnodeWidth)-1;

		/* Level0 Tnodes are 16 bits or wider (if wide tnodes are enabled),
		 * so if the bitwidth of the
		 * chunk range we're using is greater than 16 we need
		 * to figure out chunk shift and chunkGroupSize
		 */

		if (bits <= dev.subField2.tnodeWidth)
			dev.subField1.chunkGroupBits = 0;
		else
			dev.subField1.chunkGroupBits = bits - dev.subField2.tnodeWidth;


		dev.subField1.chunkGroupSize = 1 << dev.subField1.chunkGroupBits;

		if (dev.subField1.nChunksPerBlock < dev.subField1.chunkGroupSize) {
			/* We have a problem because the soft delete won't work if
			 * the chunk group size > chunks per block.
			 * This can be remedied by using larger "virtual blocks".
			 */
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					(("yaffs: chunk group too large\n" + ydirectenv.TENDSTR)));

			return Guts_H.YAFFS_FAIL;
		}

		/* OK, we've finished verifying the device, lets continue with initialisation */

		/* More device initialisation */
		dev.subField3.garbageCollections = 0;
		dev.subField3.passiveGarbageCollections = 0;
		dev.subField3.currentDirtyChecker = 0;
		dev.bufferedBlock = -1;
		dev.doingBufferedBlockRewrite = 0;
		dev.nDeletedFiles = 0;
		dev.nBackgroundDeletions = 0;
		dev.nUnlinkedFiles = 0;
		dev.subField3.eccFixed = 0;
		dev.subField3.eccUnfixed = 0;
		dev.subField3.tagsEccFixed = 0;
		dev.tagsEccUnfixed = 0;
		dev.subField3.nErasureFailures = 0;
		dev.subField3.nErasedBlocks = 0;
		dev.subField3.isDoingGC = false;
		dev.hasPendingPrioritisedGCs = true; /* Assume the worst for now, will get fixed on first GC */

		/* Initialise temporary buffers and caches. */
		{
			int i;
			for (i = 0; i < Guts_H.YAFFS_N_TEMP_BUFFERS; i++) {
				dev.tempBuffer[i].line = 0;	/* not in use */
				dev.tempBuffer[i].buffer =
					ydirectenv.YMALLOC_DMA(dev.subField1.nDataBytesPerChunk);
			}
		}

		if (dev.subField1.nShortOpCaches > 0) {
			int i;

			if (dev.subField1.nShortOpCaches > Guts_H.YAFFS_MAX_SHORT_OP_CACHES) {
				dev.subField1.nShortOpCaches = Guts_H.YAFFS_MAX_SHORT_OP_CACHES;
			}

			dev.srCache =
				ydirectenv.YMALLOC_CHUNKCACHE(dev.subField1.nShortOpCaches/* * sizeof(yaffs_ChunkCache)*/);

			for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
				dev.srCache[i].object = null;
				dev.srCache[i].lastUse = 0;
				dev.srCache[i].dirty = false;
				dev.srCache[i].data = ydirectenv.YMALLOC_DMA(dev.subField1.nDataBytesPerChunk);
				dev.srCache[i].dataIndex = 0;
			}
			dev.srLastUse = 0;
		}

		dev.cacheHits = 0;

		dev.subField3.gcCleanupList = ydirectenv.YMALLOC_INT(dev.subField1.nChunksPerBlock /*sizeof(__u32)*/);

		if (dev.subField1.isYaffs2) {
			dev.subField1.useHeaderFileSize = true;
		}

		yaffs_InitialiseBlocks(dev);
		yaffs_InitialiseTnodes(dev);
		yaffs_InitialiseObjects(dev);

		yaffs_CreateInitialDirectories(dev);


		/* Now scan the flash. */
		if (dev.subField1.isYaffs2) {
			if(yaffs_CheckpointRestore(dev)) {
				yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
						(("yaffs: restored from checkpoint" + ydirectenv.TENDSTR)));
			} else {

				/* Clean up the mess caused by an aborted checkpoint load 
				 * and scan backwards. 
				 */
				yaffs_DeinitialiseBlocks(dev);
				yaffs_DeinitialiseTnodes(dev);
				yaffs_DeinitialiseObjects(dev);
				yaffs_InitialiseBlocks(dev);
				yaffs_InitialiseTnodes(dev);
				yaffs_InitialiseObjects(dev);
				yaffs_CreateInitialDirectories(dev);

				yaffs_ScanBackwards(dev);
			}
		}else
			yaffs_Scan(dev);

		/* Zero out stats */
		dev.subField3.nPageReads = 0;
		dev.subField3.nPageWrites = 0;
		dev.subField3.nBlockErasures = 0;
		dev.subField3.nGCCopies = 0;
		dev.subField3.nRetriedWrites = 0;

		dev.subField3.nRetiredBlocks = 0;

		yaffs_VerifyFreeChunks(dev);

		yportenv.T(yportenv.YAFFS_TRACE_TRACING,
				(("yaffs: yaffs_GutsInitialise() done.\n" + ydirectenv.TENDSTR)));
		return Guts_H.YAFFS_OK;
	}

	static void yaffs_Deinitialise(yaffs_Device dev)
	{
		if (dev.subField2.isMounted) {
			int i;

			yaffs_DeinitialiseBlocks(dev);
			yaffs_DeinitialiseTnodes(dev);
			yaffs_DeinitialiseObjects(dev);
			if (dev.subField1.nShortOpCaches > 0) {

				for (i = 0; i < dev.subField1.nShortOpCaches; i++) {
					ydirectenv.YFREE(dev.srCache[i].data);
				}

				ydirectenv.YFREE(dev.srCache);
			}

			ydirectenv.YFREE(dev.subField3.gcCleanupList);

			for (i = 0; i < Guts_H.YAFFS_N_TEMP_BUFFERS; i++) {
				ydirectenv.YFREE(dev.tempBuffer[i].buffer);
			}

			dev.subField2.isMounted = false;
		}

	}

	static int yaffs_CountFreeChunks(yaffs_Device dev)
	{
		int nFree;
		int b;

		yaffs_BlockInfo blk;

		for (nFree = 0, b = dev.subField2.internalStartBlock; b <= dev.subField2.internalEndBlock;
		b++) {
			blk = Guts_H.yaffs_GetBlockInfo(dev, b);

			switch (blk.blockState()) {
			case Guts_H.YAFFS_BLOCK_STATE_EMPTY:
			case Guts_H.YAFFS_BLOCK_STATE_ALLOCATING:
			case Guts_H.YAFFS_BLOCK_STATE_COLLECTING:
			case Guts_H.YAFFS_BLOCK_STATE_FULL:
				nFree +=
					(dev.subField1.nChunksPerBlock - blk.pagesInUse() +
							blk.softDeletions());
				break;
			default:
				break;
			}

		}

		return nFree;
	}

	static int yaffs_GetNumberOfFreeChunks(yaffs_Device dev)
	{
		/* This is what we report to the outside world */

		int nFree;
		int nDirtyCacheChunks;
		int blocksForCheckpoint;

//		#if 1
		nFree = dev.subField3.nFreeChunks;
//		#else
//		nFree = yaffs_CountFreeChunks(dev);
//		#endif

		nFree += dev.nDeletedFiles;

		/* Now count the number of dirty chunks in the cache and subtract those */

		{
			int i;
			for (nDirtyCacheChunks = 0, i = 0; i < dev.subField1.nShortOpCaches; i++) {
				if (dev.srCache[i].dirty)
					nDirtyCacheChunks++;
			}
		}

		nFree -= nDirtyCacheChunks;

		nFree -= ((dev.subField1.nReservedBlocks + 1) * dev.subField1.nChunksPerBlock);

		/* Now we figure out how much to reserve for the checkpoint and report that... */
		blocksForCheckpoint = dev.subField1.nCheckpointReservedBlocks - dev.subField2.blocksInCheckpoint;
		if(blocksForCheckpoint < 0)
			blocksForCheckpoint = 0;

		nFree -= (blocksForCheckpoint * dev.subField1.nChunksPerBlock);

		if (nFree < 0)
			nFree = 0;

		return nFree;

	}

	static int yaffs_freeVerificationFailures;

	static void yaffs_VerifyFreeChunks(yaffs_Device dev)
	{
		int counted = yaffs_CountFreeChunks(dev);

		int difference = dev.subField3.nFreeChunks - counted;

		if (difference != 0) {
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,
					("Freechunks verification failure %d %d %d" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(dev.subField3.nFreeChunks), PrimitiveWrapperFactory.get(counted), PrimitiveWrapperFactory.get(difference));
			yaffs_freeVerificationFailures++;
		}
	}

	/*---------------------------------------- YAFFS test code ----------------------*/

//	#define yaffs_CheckStruct(structure,syze, name) \
//	if(sizeof(structure) != syze) \
//	{ \
//	yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,(("%s should be %d but is %d\n" + ydirectenv.TENDSTR),\
//	name,syze,sizeof(structure))); \
//	return Guts_H.YAFFS_FAIL; \
//	}

	static boolean yaffs_CheckStruct(int structureSize, int syze, String name)
	{
		if(/*sizeof(structure)*/ structureSize != syze)
		{ 
			yportenv.T(yportenv.YAFFS_TRACE_ALWAYS,("%s should be %d but is %d\n" + ydirectenv.TENDSTR),
					PrimitiveWrapperFactory.get(name),PrimitiveWrapperFactory.get(syze),/*sizeof(structure)*/ PrimitiveWrapperFactory.get(structureSize)); 
			return false; 
		}
		return true;
	}


	static boolean yaffs_CheckStructures()
	{
		return 
		/*      yaffs_CheckStruct(yaffs_Tags,8,"yaffs_Tags") */
		/*      yaffs_CheckStruct(yaffs_TagsUnion,8,"yaffs_TagsUnion") */
		/*      yaffs_CheckStruct(yaffs_Spare,16,"yaffs_Spare") */
//		#ifndef CONFIG_YAFFS_TNODE_LIST_DEBUG
		(yaffs_CheckStruct(yaffs_Tnode.SERIALIZED_LENGTH, 2 * Guts_H.YAFFS_NTNODES_LEVEL0, "yaffs_Tnode") &&
//				#endif
				yaffs_CheckStruct(yaffs_ObjectHeader.SERIALIZED_LENGTH, 512, "yaffs_ObjectHeader"));
	}
}
