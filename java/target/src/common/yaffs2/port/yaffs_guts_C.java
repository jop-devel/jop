package yaffs2.port;

import static yaffs2.utils.Utils.*;
import static yaffs2.utils.Constants.*;
import yaffs2.utils.*;
import static yaffs2.utils.Unix.*;
import static yaffs2.port.devextras.*;

import static yaffs2.port.yaffs_checkptrw_C.*;
import static yaffs2.port.Guts_H.*;
import static yaffs2.port.yportenv.*;
import static yaffs2.port.ydirectenv.*;
import static yaffs2.port.yaffs_nand_C.*;
import static yaffs2.port.yaffs_tagsvalidity_C.*;

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
		"$Id: yaffs_guts_C.java,v 1.2 2007/06/20 00:45:16 alexander.dejaco Exp $";

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
		if(dev.chunkShift != 0){
			/* Easy-peasy power of 2 case */
			chunk.dereferenced  = /*(__u32)*/(addr >>> dev.chunkShift);
			offset.dereferenced = /*(__u32)*/(addr & dev.chunkMask);
		}
		else if(dev.crumbsPerChunk != 0)
		{
			/* Case where we're using "crumbs" */
			offset.dereferenced = /*(__u32)*/(addr & dev.crumbMask);
			addr >>>= dev.crumbShift;
			chunk.dereferenced = (/*(__u32)*/addr)/dev.crumbsPerChunk;
			offset.dereferenced += ((addr - (chunk.dereferenced * dev.crumbsPerChunk)) << dev.crumbShift);
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

		while(intAsUnsignedInt(x)>1){
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
		for (i = 0; i < YAFFS_N_TEMP_BUFFERS; i++) {
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

		T(YAFFS_TRACE_BUFFERS,
				TSTR("Out of temp buffers at line %d, other held by lines:"),
				lineNo);
		for (i = 0; i < YAFFS_N_TEMP_BUFFERS; i++) {
			T(YAFFS_TRACE_BUFFERS, TSTR(" %d "), dev.tempBuffer[i].line);
		}
		T(YAFFS_TRACE_BUFFERS, (TSTR(" " + TENDSTR)));

		/*
		 * If we got here then we have to allocate an unmanaged one
		 * This is not good.
		 */

		dev.unmanagedTempAllocations++;
		return YMALLOC(dev.nDataBytesPerChunk);

	}

	static void yaffs_ReleaseTempBuffer(yaffs_Device dev, byte[] buffer,
			int lineNo)
	{
		int i;
		for (i = 0; i < YAFFS_N_TEMP_BUFFERS; i++) {
			if (dev.tempBuffer[i].buffer == buffer) {
				dev.tempBuffer[i].line = 0;
				return;
			}
		}

		if (buffer != null) {
			/* assume it is an unmanaged one. */
			T(YAFFS_TRACE_BUFFERS,
					TSTR("Releasing unmanaged temp buffer in line %d" + TENDSTR),
					lineNo);
			YFREE(buffer);
			dev.unmanagedTempDeallocations++;
		}

	}

	/*
	 * Determine if we have a managed buffer.
	 */
	static boolean yaffs_IsManagedTempBuffer(yaffs_Device dev, byte[] buffer)
	{
		int i;
		for (i = 0; i < YAFFS_N_TEMP_BUFFERS; i++) {
			if (dev.tempBuffer[i].buffer == buffer)
				return true;

		}

		for (i = 0; i < dev.nShortOpCaches; i++) {
			if( dev.srCache[i].data == buffer )
				return true;

		}

		if (buffer == dev.checkpointBuffer)
			return true;

		T(YAFFS_TRACE_ALWAYS,
				TSTR("yaffs: unmaged buffer detected.\n" + TENDSTR));
		return false;
	}

	/*
	 * Chunk bitmap manipulations
	 */

	static /*Y_INLINE*/ /*byte[]*/ ArrayPointer yaffs_BlockBits(yaffs_Device dev, int blk)
	{
		if (blk < dev.internalStartBlock || blk > dev.internalEndBlock) {
			T(YAFFS_TRACE_ERROR,
					TSTR("**>> yaffs: BlockBits block %d is not valid" + TENDSTR),
					blk);
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		return new ArrayPointer(dev.chunkBits, (dev.chunkBitmapStride * (blk - dev.internalStartBlock)));
	}

	static /*Y_INLINE*/ void yaffs_ClearChunkBits(yaffs_Device dev, int blk)
	{
		ArrayPointer blkBits = yaffs_BlockBits(dev, blk);

		memset(blkBits.array, blkBits.index, (byte)0, dev.chunkBitmapStride);
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
		for (i = 0; i < dev.chunkBitmapStride; i++) {
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
		return (n % YAFFS_NOBJECT_BUCKETS);
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
			if (byteAsUnsignedByte(buffer[bufferIndex+i]) != 0xFF)
				return false;
			i++;
		}
		return true;
	}

	static boolean yaffs_CheckChunkErased(yaffs_Device dev,
			int chunkInNAND)
	{

		boolean retval = YAFFS_OK;
		byte[] data = yaffs_GetTempBuffer(dev, __LINE__());
		final int dataIndex = 0;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		boolean result;

		result = yaffs_ReadChunkWithTagsFromNAND(dev, chunkInNAND, data, dataIndex, tags);

		if(tags.eccResult > YAFFS_ECC_RESULT_NO_ERROR)
			retval = YAFFS_FAIL;


		if (!yaffs_CheckFF(data, dataIndex, dev.nDataBytesPerChunk) || tags.chunkUsed) {
			T(YAFFS_TRACE_NANDACCESS,
					TSTR("Chunk %d not erased" + TENDSTR), chunkInNAND);
			retval = YAFFS_FAIL;
		}

		yaffs_ReleaseTempBuffer(dev, data, __LINE__());

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
					yaffs_DeleteChunk(dev, chunk, true, __LINE__());
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
						T(YAFFS_TRACE_ERROR,
								TSTR
								("**>> yaffs chunk %d was not erased"
										+ TENDSTR), chunk);
					} else {
						writeOk =
							yaffs_WriteChunkWithTagsToNAND(dev, chunk,
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
			T(YAFFS_TRACE_ERROR,
					TSTR("**>> yaffs write required %d attempts" + TENDSTR),
					attempts);
			dev.nRetriedWrites += (attempts - 1);
		}

		return chunk;
	}


	/*
	 * Block retiring for handling a broken block.
	 */

	static void yaffs_RetireBlock(yaffs_Device dev, int blockInNAND)
	{
		yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev, blockInNAND);

		yaffs_InvalidateCheckpoint(dev);

		yaffs_MarkBlockBad(dev, blockInNAND);

		bi.setBlockState(YAFFS_BLOCK_STATE_DEAD);
		bi.setGcPrioritise(false);
		bi.setNeedsRetiring(false);

		dev.nRetiredBlocks++;
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
				T(YAFFS_TRACE_ALWAYS, TSTR("yaffs: Block struck out" + TENDSTR));

			}

		}
	}

	static void yaffs_ReportOddballBlocks(yaffs_Device dev)
	{
		int i;

		for(i = dev.internalStartBlock; i <= dev.internalEndBlock && ((yaffs2.utils.Globals.yaffs_traceMask & YAFFS_TRACE_BAD_BLOCKS) != 0); i++){
			yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev,i);
			if(bi.needsRetiring() || bi.gcPrioritise())
				T(YAFFS_TRACE_BAD_BLOCKS, TSTR("yaffs block %d%s%s" + TENDSTR),
						i,
						bi.needsRetiring() ? " needs retiring" : "", // XXX hope no gc
								bi.gcPrioritise() ?  " gc prioritised" : "");

		}
	}

	static void yaffs_HandleWriteChunkError(yaffs_Device dev, int chunkInNAND, boolean erasedOk)
	{

		int blockInNAND = chunkInNAND / dev.nChunksPerBlock;
		yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev, blockInNAND);

		yaffs_HandleChunkError(dev,bi);


		if(erasedOk ) {
			/* Was an actual write failure, so mark the block for retirement  */
			bi.setNeedsRetiring(true);
			T(YAFFS_TRACE_ERROR | YAFFS_TRACE_BAD_BLOCKS,
					TSTR("**>> Block %d needs retiring" + TENDSTR), blockInNAND);


		}

		/* Delete the chunk */
		yaffs_DeleteChunk(dev, chunkInNAND, true, __LINE__());
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
			while (bname[bnameIndex] != 0 && (i <= YAFFS_MAX_NAME_LENGTH)) {

				/*#ifdef CONFIG_YAFFS_CASE_INSENSITIVE
			sum += yaffs_toupper(*bname) * i;
#else*/
				sum += byteAsUnsignedByte(bname[bnameIndex]) * i;
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
		if ((name != null) && yaffs_strlen(name, nameIndex) <= YAFFS_SHORT_NAME_LENGTH) {
			yaffs_strcpy(obj.shortName, 0, name, nameIndex);
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
			return YAFFS_OK;

		/* Calculate the tnode size in bytes for variable width tnode support.
		 * Must be a multiple of 32-bits  */
		tnodeSize = (dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8;

		/* make these things */

		newTnodes = /*YMALLOC(nTnodes * tnodeSize)*/ YMALLOC_TNODE(nTnodes); 
		//mem = (__u8 *)newTnodes;

		if (newTnodes == null) {
			T(YAFFS_TRACE_ERROR,
					(TSTR("yaffs: Could not allocate Tnodes" + TENDSTR)));
			return YAFFS_FAIL;
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
		curr.internal[0] = dev.freeTnodes; 
		dev.freeTnodes = /*(yaffs_Tnode *)mem*/ newTnodes[0]; // XXX array or not?

		/*#endif*/


		dev.nFreeTnodes += nTnodes;
		dev.nTnodesCreated += nTnodes;

		/* Now add this bunch of tnodes to a list for freeing up.
		 * NB If we can't add this to the management list it isn't fatal
		 * but it just means we can't free this bunch of tnodes later.
		 */

		tnl = /*YMALLOC(sizeof(yaffs_TnodeList))*/ new yaffs_TnodeList();
		if (tnl == null) {
			T(YAFFS_TRACE_ERROR,
					(TSTR
							("yaffs: Could not add tnodes to management list" + TENDSTR)));

		} else {
			tnl.tnodes = newTnodes;	// XXX check
			tnl.next = dev.allocatedTnodeList;
			dev.allocatedTnodeList = tnl;
		}

		T(YAFFS_TRACE_ALLOCATE, (TSTR("yaffs: Tnodes added" + TENDSTR)));

		return YAFFS_OK;
	}


	/* GetTnode gets us a clean tnode. Tries to make allocate more if we run out */

	static yaffs_Tnode yaffs_GetTnodeRaw(yaffs_Device dev)
	{
		yaffs_Tnode tn = null;

		/* If there are none left make more */
		if (dev.freeTnodes == null) {
			yaffs_CreateTnodes(dev, YAFFS_ALLOCATION_NTNODES);
		}

		if (dev.freeTnodes != null) {
			tn = dev.freeTnodes;
//			#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
//			if (tn->internal[YAFFS_NTNODES_INTERNAL] != (void *)1) {
//			/* Hoosterman, this thing looks like it isn't in the list */
//			T(YAFFS_TRACE_ALWAYS,
//			(TSTR("yaffs: Tnode list bug 1" TENDSTR)));
//			}
//			#endif
			dev.freeTnodes = dev.freeTnodes.internal[0]; // XXX check
			dev.nFreeTnodes--;
		}

		return tn;
	}

	static yaffs_Tnode yaffs_GetTnode(yaffs_Device dev)
	{
		yaffs_Tnode tn = yaffs_GetTnodeRaw(dev);

		if(tn != null)
			memset(tn/*, 0, (dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8*/);

		return tn;	
	}

	/* FreeTnode frees up a tnode and puts it back on the free list */
	static void yaffs_FreeTnode(yaffs_Device dev, yaffs_Tnode tn)
	{
		if (tn != null) {
//			#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
//			if (tn->internal[YAFFS_NTNODES_INTERNAL] != 0) {
//			/* Hoosterman, this thing looks like it is already in the list */
//			T(YAFFS_TRACE_ALWAYS,
//			(TSTR("yaffs: Tnode list bug 2" TENDSTR)));
//			}
//			tn->internal[YAFFS_NTNODES_INTERNAL] = (void *)1;
//			#endif
			tn.internal[0] = dev.freeTnodes;
			dev.freeTnodes = tn;
			dev.nFreeTnodes++;
		}
	}

//	XXX does this work? only needed for aborted checkpoint loading, and deinitialising
	static void yaffs_DeinitialiseTnodes(yaffs_Device dev)
	{
		/* Free the list of allocated tnodes */
		yaffs_TnodeList tmp;

		while (dev.allocatedTnodeList != null) {
			tmp = dev.allocatedTnodeList.next;

			// XXX has it a chance to work?
			// XXX or try another strategy?
			/*YFREE(dev->allocatedTnodeList->tnodes)*/ dev.allocatedTnodeList.tnodes = null;
			/*YFREE(dev->allocatedTnodeList)*/ dev.allocatedTnodeList = null;
			dev.allocatedTnodeList = tmp;

		}

		dev.freeTnodes = null;
		dev.nFreeTnodes = 0;
	}

	static void yaffs_InitialiseTnodes(yaffs_Device dev)
	{
		dev.allocatedTnodeList = null;
		dev.freeTnodes = null;
		dev.nFreeTnodes = 0;
		dev.nTnodesCreated = 0;

	}


	static void yaffs_PutLevel0Tnode(yaffs_Device dev, yaffs_Tnode tn, int pos, int val)
	{
		/*__u32 map = (__u32 *)tn*/;
		int bitInMap;
		int bitInWord;
		int wordInMap;
		int mask;

		pos &= YAFFS_TNODES_LEVEL0_MASK;
		val >>>= dev.chunkGroupBits;

		bitInMap = pos * dev.tnodeWidth;
		wordInMap = bitInMap /32;
		bitInWord = bitInMap & (32 -1);

		mask = dev.tnodeMask << bitInWord;

		tn.andLevel0AsInt(wordInMap, ~mask);
		tn.orLevel0AsInt(wordInMap, (mask & (val << bitInWord)));

		if(dev.tnodeWidth > (32-bitInWord)) {
			bitInWord = (32 - bitInWord);
			wordInMap++;;
			mask = dev.tnodeMask >>> (/*dev->tnodeWidth -*/ bitInWord);
		tn.andLevel0AsInt(wordInMap, ~mask);
		tn.orLevel0AsInt(wordInMap, mask & (val >>> bitInWord));
		}
		
		// FIXME
		T(YAFFS_TRACE_TNODE, "PutLevel0Tnode: pos %d val %d map[wordInMap]: %d\n", pos, val, tn.level0AsInt(wordInMap));
	}

	static int yaffs_GetChunkGroupBase(yaffs_Device dev, yaffs_Tnode tn, int pos)
	{
		/*__u32 *map = (__u32 *)tn;*/
		int bitInMap;
		int bitInWord;
		int wordInMap;
		int val;

		pos &= YAFFS_TNODES_LEVEL0_MASK;

		bitInMap = pos * dev.tnodeWidth;
		wordInMap = bitInMap /32;
		bitInWord = bitInMap & (32 -1);

		val = tn.level0AsInt(wordInMap) >>> bitInWord;

		if(dev.tnodeWidth > (32-bitInWord)) {
			bitInWord = (32 - bitInWord);
			wordInMap++;;
			val |= (tn.level0AsInt(wordInMap) << bitInWord);
		}

		val &= dev.tnodeMask;
		val <<= dev.chunkGroupBits;

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
		if (level < 0 || level > YAFFS_TNODES_MAX_LEVEL) {
			return null;
		}

		if (chunkId > YAFFS_MAX_CHUNK_ID) {
			return null;
		}

		/* First check we're tall enough (ie enough topLevel) */

		i = chunkId >>> YAFFS_TNODES_LEVEL0_BITS;
			requiredTallness = 0;
			while (i != 0) {
				i >>>= YAFFS_TNODES_INTERNAL_BITS;
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
				( YAFFS_TNODES_LEVEL0_BITS + 
						(level - 1) *
						YAFFS_TNODES_INTERNAL_BITS)
				) &
				YAFFS_TNODES_INTERNAL_MASK];
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
		if (fStruct.topLevel < 0 || fStruct.topLevel > YAFFS_TNODES_MAX_LEVEL) {
			return null;
		}

		if (intAsUnsignedInt(chunkId) > YAFFS_MAX_CHUNK_ID) { 
			return null;
		}

		/* First check we're tall enough (ie enough topLevel) */

		x = chunkId >>> YAFFS_TNODES_LEVEL0_BITS;
		requiredTallness = 0;
		while (x != 0) {
			x >>>= YAFFS_TNODES_INTERNAL_BITS;
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
					T(YAFFS_TRACE_ERROR,
							(TSTR("yaffs: no more tnodes" + TENDSTR)));
				}
			}

			// FIXME
			if (requiredTallness > 0) 
				T(YAFFS_TRACE_TALLNESS, "Required tallness: %d\n", requiredTallness); 
			fStruct.topLevel = requiredTallness;
		}

		/* Traverse down to level 0, adding anything we need */

		l = fStruct.topLevel;
		tn = fStruct.top;

		if(l > 0) {
			while (l > 0 && tn != null) {
				x = (chunkId >>>
				( YAFFS_TNODES_LEVEL0_BITS +
						(l - 1) * YAFFS_TNODES_INTERNAL_BITS)) &
						YAFFS_TNODES_INTERNAL_MASK;


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
				memcpy(tn,passedTn/*,(dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8*/); // XXX only copy level0?
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

		for (j = 0; theChunk != 0 && j < dev.chunkGroupSize; j++) {
			if (yaffs_CheckChunkBit
					(dev, theChunk / dev.nChunksPerBlock,
							theChunk % dev.nChunksPerBlock)) {
				yaffs_ReadChunkWithTagsFromNAND(dev, theChunk, null, 0,
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

				for (i = YAFFS_NTNODES_INTERNAL - 1; allDone && i >= 0;
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
												YAFFS_TNODES_INTERNAL_BITS)
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

				for (i = YAFFS_NTNODES_LEVEL0 - 1; i >= 0 && (hitLimit == 0);
				i--) {
					theChunk = yaffs_GetChunkGroupBase(dev,tn,i);
					if (theChunk != 0) {

						chunkInInode =
							(chunkOffset <<
									YAFFS_TNODES_LEVEL0_BITS) + i;

						foundChunk =
							yaffs_FindChunkInGroup(dev,
									theChunk,
									tags,
									in.objectId,
									chunkInInode);

						if (foundChunk > 0) {
							yaffs_DeleteChunk(dev,
									foundChunk, true,
									__LINE__());
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

		T(YAFFS_TRACE_DELETION, TSTR("soft delete chunk %d" + TENDSTR), chunk);

		theBlock = yaffs_GetBlockInfo(dev, chunk / dev.nChunksPerBlock);
		if (theBlock != null) {
			theBlock.setSoftDeletions(theBlock.softDeletions()+1);
			dev.nFreeChunks++;
		}
	}

	/* SoftDeleteWorker scans backwards through the tnode tree and soft deletes all the chunks in the file.
	 * All soft deleting does is increment the block's softdelete count and pulls the chunk out
	 * of the tnode.
	 * Thus, essentially this is the same as DeleteWorker except that the chunks are soft deleted.
	 */

	static boolean yaffs_SoftDeleteWorker(yaffs_Object in, yaffs_Tnode tn,
			int level, int chunkOffset)
	{
		int i;
		int theChunk;
		boolean allDone = true;
		yaffs_Device dev = in.myDev;

		if (tn != null) {
			if (level > 0) {

				for (i = YAFFS_NTNODES_INTERNAL - 1; (allDone) && i >= 0;
				i--) {
					if (tn.internal[i] != null) {
						allDone =
							yaffs_SoftDeleteWorker(in,
									tn.
									internal[i],
									level - 1,
									(chunkOffset
											<<
											YAFFS_TNODES_INTERNAL_BITS)
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

				for (i = YAFFS_NTNODES_LEVEL0 - 1; i >= 0; i--) {
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
		if (obj.deleted &&
				obj.variantType == YAFFS_OBJECT_TYPE_FILE && !obj.softDeleted) {
			if (obj.nDataChunks <= 0) {
				/* Empty file with no duplicate object headers, just delete it immediately */
				yaffs_FreeTnode(obj.myDev,
						obj.variant.fileVariant().top);
				obj.variant.fileVariant().top = null;
				T(YAFFS_TRACE_TRACING,
						TSTR("yaffs: Deleting empty file %d" + TENDSTR),
						obj.objectId);
				yaffs_DoGenericObjectDeletion(obj);
			} else {
				yaffs_SoftDeleteWorker(obj,
						obj.variant.fileVariant().top,
						obj.variant.fileVariant().
						topLevel, 0);
				obj.softDeleted = true;
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

			for (i = 0; i < YAFFS_NTNODES_INTERNAL; i++) {
				assert (level > 0 ? 
						getIntFromByteArray(tn.serialized, i*4) == 0 : 
							tn.internal[i] == null); 

				if (tn.internal[i] != null && level > 0) {
					tn.internal[i] =	// XXX recursive call
						yaffs_PruneWorker(dev, tn.internal[i],
								level - 1,
								(i == 0) ? del0 : true);
				}

				if ((tn.internal[i] != null) || // PORT union
						(getIntFromByteArray(tn.serialized, tn.offset+i*4) != 0)) {
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
				for (i = 1; i < YAFFS_NTNODES_INTERNAL; i++) {
					if (tn.internal[i] != null) {
						hasData++;
					}
				}

				if (hasData == 0) {
					fStruct.top = tn.internal[0];
					fStruct.topLevel--;
					// FIXME
					T(YAFFS_TRACE_TOPLEVEL, "Reducing topLevel: %d\n", fStruct.topLevel);
					yaffs_FreeTnode(dev, tn);
				} else {
					done = true;
				}
			}
		}

		return YAFFS_OK;
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
			return YAFFS_OK;

		/* make these things */
		newObjects = YMALLOC_OBJECT(nObjects/* * sizeof(yaffs_Object)*/ );

		if (!(newObjects != null)) {
			T(YAFFS_TRACE_ALLOCATE,
					(TSTR("yaffs: Could not allocate more objects" + TENDSTR)));
			return YAFFS_FAIL;
		}

		/* Hook them into the free list */
		for (i = 0; i < nObjects - 1; i++) {
			newObjects[i].siblings.next =
				/*(list_head)*/ (newObjects[i + 1]);	// XXX check
		}

		newObjects[nObjects - 1].siblings.next = /*(void *)*/ dev.freeObjects; 
		dev.freeObjects = newObjects[0];	// XXX check
		dev.nFreeObjects += nObjects;
		dev.nObjectsCreated += nObjects;

		/* Now add this bunch of Objects to a list for freeing up. */

		list = /*YMALLOC(sizeof(yaffs_ObjectList))*/ new yaffs_ObjectList();
		if (!(list != null)) {
			T(YAFFS_TRACE_ALLOCATE,
					(TSTR("Could not add objects to management list" + TENDSTR)));
		} else {
			list.objects = newObjects;
			list.next = dev.allocatedObjectList;
			dev.allocatedObjectList = list;
		}

		return YAFFS_OK;
	}


	/* AllocateEmptyObject gets us a clean Object. Tries to make allocate more if we run out */
	static yaffs_Object yaffs_AllocateEmptyObject(yaffs_Device dev)
	{
		yaffs_Object tn = null;

		/* If there are none left make more */
		if (!(dev.freeObjects != null)) {
			yaffs_CreateFreeObjects(dev, YAFFS_ALLOCATION_NOBJECTS);
		}

		if (dev.freeObjects != null) {
			tn = dev.freeObjects;	// XXX check
			dev.freeObjects =
				(yaffs_Object) (dev.freeObjects.siblings.next);
			dev.nFreeObjects--;

			/* Now sweeten it up... */

			memset(tn/*, 0, sizeof(yaffs_Object)*/ );
			tn.myDev = dev;
			tn.chunkId = -1;
			tn.variantType = YAFFS_OBJECT_TYPE_UNKNOWN;
			INIT_LIST_HEAD((tn.hardLinks));
			INIT_LIST_HEAD((tn.hashLink));
			INIT_LIST_HEAD(tn.siblings);

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
			yaffs_CreateNewObject(dev, number, YAFFS_OBJECT_TYPE_DIRECTORY);
		if (obj != null) {
			obj.fake = true;		/* it is fake so it has no NAND presence... */
			obj.renameAllowed = false;	/* ... and we're not allowed to rename it... */
			obj.unlinkAllowed = false;	/* ... or unlink it */
			obj.deleted = false;
			obj.unlinked = false;
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
		if (!list_empty(tn.hashLink)) {
			list_del_init(tn.hashLink);
			bucket = yaffs_HashFunction(tn.objectId);
			dev.objectBucket[bucket].count--;
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
		tn.siblings.next = /*(list_head)*/ (dev.freeObjects);
		dev.freeObjects = tn;
		dev.nFreeObjects++;
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

		while (dev.allocatedObjectList != null) {
			tmp = dev.allocatedObjectList.next;
			YFREE(dev.allocatedObjectList.objects);
			YFREE(dev.allocatedObjectList);

			dev.allocatedObjectList = tmp;
		}

		dev.freeObjects = null;
		dev.nFreeObjects = 0;
	}

	static void yaffs_InitialiseObjects(yaffs_Device dev)
	{
		int i;

		dev.allocatedObjectList = null;
		dev.freeObjects = null;
		dev.nFreeObjects = 0;

		for (i = 0; i < YAFFS_NOBJECT_BUCKETS; i++) {
			INIT_LIST_HEAD(dev.objectBucket[i].list);
			dev.objectBucket[i].count = 0;
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
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x %= YAFFS_NOBJECT_BUCKETS;
			if (dev.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count < lowest) {
				lowest = dev.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count;
				l = _STATIC_LOCAL_yaffs_FindNiceObjectBucket_x;
			}

		}

		/* If we didn't find an empty list, then try
		 * looking a bit further for a short one
		 */

		for (i = 0; i < 10 && lowest > 3; i++) {
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x++;
			_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x %= YAFFS_NOBJECT_BUCKETS;
			if (dev.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count < lowest) {
				lowest = dev.objectBucket[_STATIC_LOCAL_yaffs_FindNiceObjectBucket_x].count;
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
			n += YAFFS_NOBJECT_BUCKETS;
			if (true || dev.objectBucket[bucket].count > 0) {
//				list_for_each(i, &dev->objectBucket[bucket].list) {
				for (i = dev.objectBucket[bucket].list.next(); i != dev.objectBucket[bucket].list;
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

		list_add(in.hashLink, dev.objectBucket[bucket].list);
		dev.objectBucket[bucket].count++;

	}

	static yaffs_Object yaffs_FindObjectByNumber(yaffs_Device dev, /*__u32*/ int number)
	{
		int bucket = yaffs_HashFunction(number);
		list_head i;
		yaffs_Object in;

//		list_for_each(i, &dev.objectBucket[bucket].list) {
		for (i = dev.objectBucket[bucket].list.next(); i != dev.objectBucket[bucket].list;
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
			theObject.fake = false;
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
				theObject.yst_ctime = Y_CURRENT_TIME();
//			#endif
			switch (type) {	// XXX either create the corresponding variant here, or create all possible variants on object creation
				case YAFFS_OBJECT_TYPE_FILE:
					theObject.variant.fileVariant().fileSize = 0;
					theObject.variant.fileVariant().scannedFileSize = 0;
					theObject.variant.fileVariant().shrinkSize = 0xFFFFFFFF;	/* max __u32 */
					theObject.variant.fileVariant().topLevel = 0;
					theObject.variant.fileVariant().top =
						yaffs_GetTnode(dev);
					break;
				case YAFFS_OBJECT_TYPE_DIRECTORY:
					INIT_LIST_HEAD(theObject.variant.directoryVariant().
							children);
					break;
				case YAFFS_OBJECT_TYPE_SYMLINK:
				case YAFFS_OBJECT_TYPE_HARDLINK:
				case YAFFS_OBJECT_TYPE_SPECIAL:
					/* No action required */
					break;
				case YAFFS_OBJECT_TYPE_UNKNOWN:
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
			newStr = YMALLOC((yaffs_strlen(str, strIndex) + 1)/* * sizeof(YCHAR)*/);
			yaffs_strcpy(newStr, 0, str, strIndex);
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
			in.yst_atime = in.yst_mtime = in.yst_ctime = Y_CURRENT_TIME();

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
				case YAFFS_OBJECT_TYPE_SYMLINK:
					in.variant.symLinkVariant().alias =
						yaffs_CloneString(aliasString, aliasStringIndex);
					in.variant.symLinkVariant().aliasIndex = 0;
					break;
				case YAFFS_OBJECT_TYPE_HARDLINK:
					in.variant.hardLinkVariant().equivalentObject =
						equivalentObject;
					in.variant.hardLinkVariant().equivalentObjectId =
						equivalentObject.objectId;
					list_add(in.hardLinks, equivalentObject.hardLinks);
					break;
				case YAFFS_OBJECT_TYPE_FILE:	
				case YAFFS_OBJECT_TYPE_DIRECTORY:
				case YAFFS_OBJECT_TYPE_SPECIAL:
				case YAFFS_OBJECT_TYPE_UNKNOWN:
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
		return yaffs_MknodObject(YAFFS_OBJECT_TYPE_FILE, parent, name, nameIndex, mode,
				uid, gid, null, null, 0, 0);
	}

	static yaffs_Object yaffs_MknodDirectory(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid)
	{
		return yaffs_MknodObject(YAFFS_OBJECT_TYPE_DIRECTORY, parent, name, nameIndex,
				mode, uid, gid, null, null, 0, 0);
	}

	static yaffs_Object yaffs_MknodSpecial(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid, /*__u32*/ int rdev)
	{
		return yaffs_MknodObject(YAFFS_OBJECT_TYPE_SPECIAL, parent, name, nameIndex, 
				mode, uid, gid, null, null, 0, rdev);
	}

	static yaffs_Object yaffs_MknodSymLink(yaffs_Object  parent, byte[] name, int nameIndex,
			/*__u32*/ int mode, /*__u32*/ int uid, /*__u32*/ int gid,
			byte[] alias, int aliasIndex)
	{
		return yaffs_MknodObject(YAFFS_OBJECT_TYPE_SYMLINK, parent, name, nameIndex,
				mode, uid, gid, null, alias, aliasIndex, 0);
	}

	/* yaffs_Link returns the object id of the equivalent object.*/
	static yaffs_Object yaffs_Link(yaffs_Object  parent, byte[] name, int nameIndex,
			yaffs_Object  equivalentObject)
	{
		/* Get the real object in case we were fed a hard link as an equivalent object */
		equivalentObject = yaffs_GetEquivalentObject(equivalentObject);

		if (yaffs_MknodObject
				(YAFFS_OBJECT_TYPE_HARDLINK, parent, name, nameIndex, 0, 0, 0,
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

		if (newDir.variantType != YAFFS_OBJECT_TYPE_DIRECTORY) {
			T(YAFFS_TRACE_ALWAYS,
					TSTR
					("tragendy: yaffs_ChangeObjectName: newDir is not a directory"
							+ TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		/* TODO: Do we need this different handling for YAFFS2 and YAFFS1?? */
		if (obj.myDev.isYaffs2) {
			unlinkOp = (newDir == obj.myDev.unlinkedDir);
		} else {
			unlinkOp = (newDir == obj.myDev.unlinkedDir
					&& obj.variantType == YAFFS_OBJECT_TYPE_FILE);
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
				newDir.variantType == YAFFS_OBJECT_TYPE_DIRECTORY) {
			yaffs_SetObjectName(obj, newName, newNameIndex);
			obj.dirty = true;

			yaffs_AddObjectToDirectory(newDir, obj);

			if (unlinkOp)
				obj.unlinked = true;

			/* If it is a deletion then we mark it as a shrink for gc purposes. */
			if (yaffs_UpdateObjectHeader(obj, newName, newNameIndex, false, deleteOp, shadows)>= 0)
				return YAFFS_OK;
		}

		return YAFFS_FAIL;
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
		if (obj.variantType == YAFFS_OBJECT_TYPE_SYMLINK &&
				yaffs_strlen(newName, newNameIndex) > YAFFS_MAX_ALIAS_LENGTH)
			/* ENAMETOOLONG */
			return YAFFS_FAIL;
		else if (obj.variantType != YAFFS_OBJECT_TYPE_SYMLINK &&
				yaffs_strlen(newName, newNameIndex) > YAFFS_MAX_NAME_LENGTH)
			/* ENAMETOOLONG */
			return YAFFS_FAIL;

		if (obj != null && obj.renameAllowed) {

			/* Now do the handling for an existing target, if there is one */

			existingTarget = yaffs_FindObjectByName(newDir, newName, newNameIndex);
			if (existingTarget != null &&
					existingTarget.variantType == YAFFS_OBJECT_TYPE_DIRECTORY &&
					!list_empty(existingTarget.variant.directoryVariant().children)) {
				/* There is a target that is a non-empty directory, so we fail */
				return YAFFS_FAIL;	/* EEXIST or ENOTEMPTY */
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
		return YAFFS_FAIL;
	}

	/*------------------------- Block Management and Page Allocation ----------------*/

	static boolean yaffs_InitialiseBlocks(yaffs_Device dev)	// XXX only done once?
	{
		int nBlocks = dev.internalEndBlock - dev.internalStartBlock + 1;

		dev.allocationBlock = -1;	/* force it to get a new one */

		/* Todo we're assuming the malloc will pass. */
		dev.blockInfo = /*YMALLOC(nBlocks * sizeof(yaffs_BlockInfo))*/ YMALLOC_BLOCKINFO(nBlocks);
		if(!(dev.blockInfo != null)){
			// XXX makes no sense in Java?
			throw new NotImplementedException();
//			dev.blockInfo = YMALLOC_ALT(nBlocks * sizeof(yaffs_BlockInfo));
//			dev.blockInfoAlt = 1;
		}
		else
			dev.blockInfoAlt = false;

		/* Set up dynamic blockinfo stuff. */
		dev.chunkBitmapStride = (dev.nChunksPerBlock + 7) / 8; /* round up bytes */
		dev.chunkBits = YMALLOC(dev.chunkBitmapStride * nBlocks);
		dev.chunkBitsIndex = 0;
		if(!(dev.chunkBits != null)){
			throw new NotImplementedException();
//			dev.chunkBits = YMALLOC_ALT(dev.chunkBitmapStride * nBlocks);
//			dev.chunkBitsAlt = 1;
		}
		else
			dev.chunkBitsAlt = false;

		if ((dev.blockInfo != null) && (dev.chunkBits != null)) {
			// PORT already done on object creation
			// XXX not if not using pool
			memset(dev.blockInfo, (byte)0/*, nBlocks * sizeof(yaffs_BlockInfo)*/);
			memset(dev.chunkBits, dev.chunkBitsIndex, (byte)0, dev.chunkBitmapStride * nBlocks);
			return YAFFS_OK;
		}

		return YAFFS_FAIL;

	}

	static void yaffs_DeinitialiseBlocks(yaffs_Device dev) // XXX
	{
		if(dev.blockInfoAlt)
			YFREE_ALT(dev.blockInfo);
		else
			YFREE(dev.blockInfo);
		dev.blockInfoAlt = false;

		dev.blockInfo = null;

		if(dev.chunkBitsAlt)
			YFREE_ALT(dev.chunkBits);
		else
			YFREE(dev.chunkBits);
		dev.chunkBitsAlt = false;
		dev.chunkBits = null;
	}

	static boolean yaffs_BlockNotDisqualifiedFromGC(yaffs_Device dev,
			yaffs_BlockInfo bi)
	{
		int i;
		/*__u32*/ long seq;
		yaffs_BlockInfo b;

		if (!dev.isYaffs2)
			return true;	/* disqualification only applies to yaffs2. */

		if (!bi.hasShrinkHeader()) 
			return true;	/* can gc */

		/* Find the oldest dirty sequence number if we don't know it and save it
		 * so we don't have to keep recomputing it.
		 */
		if (!(dev.oldestDirtySequence != 0)) {
			seq = dev.sequenceNumber;

			for (i = dev.internalStartBlock; i <= dev.internalEndBlock;
			i++) {
				b = yaffs_GetBlockInfo(dev, i);
				if (b.blockState() == YAFFS_BLOCK_STATE_FULL &&
						(b.pagesInUse() - b.softDeletions()) <
						dev.nChunksPerBlock && intAsUnsignedInt(b.sequenceNumber()) < seq) {
					seq = intAsUnsignedInt(b.sequenceNumber());
				}
			}
			dev.oldestDirtySequence = seq;
		}

		/* Can't do gc of this block if there are any blocks older than this one that have
		 * discarded pages.
		 */
		return (intAsUnsignedInt(bi.sequenceNumber()) <= dev.oldestDirtySequence);

	}

	/* FindDiretiestBlock is used to select the dirtiest block (or close enough)
	 * for garbage collection.
	 */

	static int _STATIC_LOCAL_yaffs_FindBlockForGarbageCollection_nonAggressiveSkip = 0;
	static int yaffs_FindBlockForGarbageCollection(yaffs_Device dev,
			boolean aggressive)
	{

		int b = dev.currentDirtyChecker;

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
			for(i = dev.internalStartBlock; i < dev.internalEndBlock && !prioritised; i++){

				bi = yaffs_GetBlockInfo(dev, i);
				if(bi.gcPrioritise()) {
					pendingPrioritisedExist = true;
					if(bi.blockState() == YAFFS_BLOCK_STATE_FULL &&
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
				(aggressive) ? dev.nChunksPerBlock : YAFFS_PASSIVE_GC_CHUNKS + 1;

		if (aggressive) {
			iterations =
				dev.internalEndBlock - dev.internalStartBlock + 1;
		} else {
			iterations =
				dev.internalEndBlock - dev.internalStartBlock + 1;
			iterations = iterations / 16;
			if (iterations > 200) {
				iterations = 200;
			}
		}

		for (i = 0; i <= iterations && pagesInUse > 0 && !prioritised; i++) {
			b++;
			if (b < dev.internalStartBlock || b > dev.internalEndBlock) {
				b = dev.internalStartBlock;
			}

			if (b < dev.internalStartBlock || b > dev.internalEndBlock) {
				T(YAFFS_TRACE_ERROR,
						TSTR("**>> Block %d is not valid" + TENDSTR), b);
				yaffs2.utils.Globals.portConfiguration.YBUG();
			}

			bi = yaffs_GetBlockInfo(dev, b);

//			#if 0
//			if (bi.blockState == YAFFS_BLOCK_STATE_CHECKPOINT) {
//			dirtiest = b;
//			pagesInUse = 0;
//			}
//			else 
//			#endif

			if (bi.blockState() == YAFFS_BLOCK_STATE_FULL &&
					(bi.pagesInUse() - bi.softDeletions()) < pagesInUse &&
					(yaffs_BlockNotDisqualifiedFromGC(dev, bi))) {
				dirtiest = b;
				pagesInUse = (bi.pagesInUse() - bi.softDeletions());
			}
		}

		dev.currentDirtyChecker = b;

		if (dirtiest > 0) {
			T(YAFFS_TRACE_GC,
					TSTR("GC Selected block %d with %d free, prioritised:%b" + TENDSTR), dirtiest,
					dev.nChunksPerBlock - pagesInUse,prioritised);
		}

		dev.oldestDirtySequence = 0;

		if (dirtiest > 0) {
			_STATIC_LOCAL_yaffs_FindBlockForGarbageCollection_nonAggressiveSkip = 4;
		}

		return dirtiest;
	}

	static void yaffs_BlockBecameDirty(yaffs_Device dev, int blockNo)
	{
		yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev, blockNo);

		boolean erasedOk = false;

		/* If the block is still healthy erase it and mark as clean.
		 * If the block has had a data failure, then retire it.
		 */

		T(YAFFS_TRACE_GC | YAFFS_TRACE_ERASE,
				TSTR("yaffs_BlockBecameDirty block %d state %d %s"+ TENDSTR),
				blockNo, bi.blockState(), (bi.needsRetiring()) ? "needs retiring" : "");

		bi.setBlockState(YAFFS_BLOCK_STATE_DIRTY);

		if (!bi.needsRetiring()) {
			yaffs_InvalidateCheckpoint(dev);
			erasedOk = yaffs_EraseBlockInNAND(dev, blockNo);
			if (!erasedOk) {
				dev.nErasureFailures++;
				T(YAFFS_TRACE_ERROR | YAFFS_TRACE_BAD_BLOCKS,
						TSTR("**>> Erasure failed %d" + TENDSTR), blockNo);
			}
		}

		if (erasedOk && ((yaffs2.utils.Globals.yaffs_traceMask & YAFFS_TRACE_ERASE) != 0)) {
			int i;
			for (i = 0; i < dev.nChunksPerBlock; i++) {
				if (!yaffs_CheckChunkErased
						(dev, blockNo * dev.nChunksPerBlock + i)) {
					T(YAFFS_TRACE_ERROR,
							TSTR
							(">>Block %d erasure supposedly OK, but chunk %d not erased"
									+ TENDSTR), blockNo, i);
				}
			}
		}

		if (erasedOk) {
			/* Clean it up... */
			bi.setBlockState(YAFFS_BLOCK_STATE_EMPTY);
			dev.nErasedBlocks++;
			bi.setPagesInUse(0);
			bi.setSoftDeletions(0);
			bi.setHasShrinkHeader(false);
			bi.setSkipErasedCheck(true);  /* This is clean, so no need to check */
			bi.setGcPrioritise(false);
			yaffs_ClearChunkBits(dev, blockNo);

			T(YAFFS_TRACE_ERASE,
					TSTR("Erased block %d" + TENDSTR), blockNo);
		} else {
			dev.nFreeChunks -= dev.nChunksPerBlock;	/* We lost a block of free space */

			yaffs_RetireBlock(dev, blockNo);
			T(YAFFS_TRACE_ERROR | YAFFS_TRACE_BAD_BLOCKS,
					TSTR("**>> Block %d retired" + TENDSTR), blockNo);
		}
	}

	static int yaffs_FindBlockForAllocation(yaffs_Device dev)
	{
		int i;

		yaffs_BlockInfo bi;

		if (dev.nErasedBlocks < 1) {
			/* Hoosterman we've got a problem.
			 * Can't get space to gc
			 */
			T(YAFFS_TRACE_ERROR,
					(TSTR("yaffs tragedy: no more eraased blocks" + TENDSTR)));

			return -1;
		}

		/* Find an empty block. */

		for (i = dev.internalStartBlock; i <= dev.internalEndBlock; i++) {
			dev.allocationBlockFinder++;
			if (dev.allocationBlockFinder < dev.internalStartBlock
					|| dev.allocationBlockFinder > dev.internalEndBlock) {
				dev.allocationBlockFinder = dev.internalStartBlock;
			}

			bi = yaffs_GetBlockInfo(dev, dev.allocationBlockFinder);

			if (bi.blockState() == YAFFS_BLOCK_STATE_EMPTY) {
				bi.setBlockState(YAFFS_BLOCK_STATE_ALLOCATING);
				dev.sequenceNumber++;
				bi.setSequenceNumber((int)dev.sequenceNumber);
				dev.nErasedBlocks--;
				T(YAFFS_TRACE_ALLOCATE,
						TSTR("Allocated block %d, seq  %l, %d left" + TENDSTR),
						dev.allocationBlockFinder, dev.sequenceNumber,
						dev.nErasedBlocks);
				return dev.allocationBlockFinder;
			}
		}

		T(YAFFS_TRACE_ALWAYS,
				TSTR
				("yaffs tragedy: no more eraased blocks, but there should have been %d"
						+ TENDSTR), dev.nErasedBlocks);

		return -1;
	}


//	Check if there's space to allocate...
//	Thinks.... do we need top make this ths same as yaffs_GetFreeChunks()?
	static boolean yaffs_CheckSpaceForAllocation(yaffs_Device dev)
	{
		int reservedChunks;
		int reservedBlocks = dev.nReservedBlocks;
		int checkpointBlocks;

		checkpointBlocks =  dev.nCheckpointReservedBlocks - dev.blocksInCheckpoint;
		if(checkpointBlocks < 0)
			checkpointBlocks = 0;

		reservedChunks = ((reservedBlocks + checkpointBlocks) * dev.nChunksPerBlock);

		return (dev.nFreeChunks > reservedChunks);
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

		if (dev.allocationBlock < 0) {
			/* Get next block to allocate off */
			dev.allocationBlock = yaffs_FindBlockForAllocation(dev);
			dev.allocationPage = 0;
		}

		if (!useReserve && !yaffs_CheckSpaceForAllocation(dev)) {
			/* Not enough space to allocate unless we're allowed to use the reserve. */
			return -1;
		}

		if (dev.nErasedBlocks < dev.nReservedBlocks
				&& dev.allocationPage == 0) {
			T(YAFFS_TRACE_ALLOCATE, (TSTR("Allocating reserve" + TENDSTR)));
		}

		/* Next page please.... */
		if (dev.allocationBlock >= 0) {
			bi = yaffs_GetBlockInfo(dev, dev.allocationBlock);

			retVal = (dev.allocationBlock * dev.nChunksPerBlock) +
			dev.allocationPage;
			bi.setPagesInUse(bi.pagesInUse()+ 1);
			yaffs_SetChunkBit(dev, dev.allocationBlock,
					dev.allocationPage);

			dev.allocationPage++;

			dev.nFreeChunks--;

			/* If the block is full set the state to full */
			if (dev.allocationPage >= dev.nChunksPerBlock) {
				bi.setBlockState(YAFFS_BLOCK_STATE_FULL);
				dev.allocationBlock = -1;
			}

			if(blockUsedPtr != null)
				blockUsedPtr.dereferenced = bi;

			return retVal;
		}

		T(YAFFS_TRACE_ERROR,
				TSTR("!!!!!!!!! Allocator out !!!!!!!!!!!!!!!!!" + TENDSTR));

		return -1;
	}

	static int yaffs_GetErasedChunks(yaffs_Device dev)
	{
		int n;

		n = dev.nErasedBlocks * dev.nChunksPerBlock;

		if (dev.allocationBlock > 0) {
			n += (dev.nChunksPerBlock - dev.allocationPage);
		}

		return n;

	}

	static boolean yaffs_GarbageCollectBlock(yaffs_Device dev, int block)
	{
		int oldChunk;
		int newChunk;
		int chunkInBlock;
		boolean markNAND;
		boolean retVal = YAFFS_OK;
		int cleanups = 0;
		int i;
		boolean isCheckpointBlock;

		int chunksBefore = yaffs_GetErasedChunks(dev);
		int chunksAfter;

		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();

		yaffs_BlockInfo bi = yaffs_GetBlockInfo(dev, block);

		yaffs_Object object;

		isCheckpointBlock = (bi.blockState() == YAFFS_BLOCK_STATE_CHECKPOINT);

		bi.setBlockState(YAFFS_BLOCK_STATE_COLLECTING);

		T(YAFFS_TRACE_TRACING,
				TSTR("Collecting block %d, in use %d, shrink %b, " + TENDSTR), block,
				bi.pagesInUse(), bi.hasShrinkHeader());

		/*yaffs_VerifyFreeChunks(dev); */

		bi.setHasShrinkHeader(false);	/* clear the flag so that the block can erase */

		/* Take off the number of soft deleted entries because
		 * they're going to get really deleted during GC.
		 */
		dev.nFreeChunks -= bi.softDeletions();

		dev.isDoingGC = true;

		if (isCheckpointBlock ||
				!yaffs_StillSomeChunkBits(dev, block)) {
			T(YAFFS_TRACE_TRACING,
					TSTR
					("Collecting block %d that has no chunks in use" + TENDSTR),
					block);
			yaffs_BlockBecameDirty(dev, block);
		} else {

			byte[] buffer = yaffs_GetTempBuffer(dev, __LINE__());

			for (chunkInBlock = 0, oldChunk = block * dev.nChunksPerBlock;
			chunkInBlock < dev.nChunksPerBlock
			&& yaffs_StillSomeChunkBits(dev, block);
			chunkInBlock++, oldChunk++) {
				if (yaffs_CheckChunkBit(dev, block, chunkInBlock)) {

					/* This page is in use and might need to be copied off */

					markNAND = true;

					yaffs_InitialiseTags(tags);

					yaffs_ReadChunkWithTagsFromNAND(dev, oldChunk,
							buffer, 0, tags);

					object =
						yaffs_FindObjectByNumber(dev,
								tags.objectId);

					T(YAFFS_TRACE_GC_DETAIL,
							TSTR
							("Collecting page %d, %d %d %d " + TENDSTR),
							chunkInBlock, tags.objectId, tags.chunkId,
							tags.byteCount);

					if (!(object != null)) {
						T(YAFFS_TRACE_ERROR,
								TSTR
								("page %d in gc has no object "
										+ TENDSTR), oldChunk);
					}

					if (object != null && object.deleted
							&& tags.chunkId != 0) {
						/* Data chunk in a deleted file, throw it away
						 * It's a soft deleted data chunk,
						 * No need to copy this, just forget about it and 
						 * fix up the object.
						 */

						object.nDataChunks--;

						if (object.nDataChunks <= 0) {
							/* remeber to clean up the object */
							dev.gcCleanupList[cleanups] =
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

						dev.nGCCopies++;

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
							retVal = YAFFS_FAIL;
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

					yaffs_DeleteChunk(dev, oldChunk, markNAND, __LINE__());

				}
			}

			yaffs_ReleaseTempBuffer(dev, buffer, __LINE__());


			/* Do any required cleanups */
			for (i = 0; i < cleanups; i++) {
				/* Time to delete the file too */
				object =
					yaffs_FindObjectByNumber(dev,
							dev.gcCleanupList[i]);
				if (object != null) {
					yaffs_FreeTnode(dev,
							object.variant.fileVariant().
							top);
					object.variant.fileVariant().top = null;
					T(YAFFS_TRACE_GC,
							TSTR
							("yaffs: About to finally delete object %d"
									+ TENDSTR), object.objectId);
					yaffs_DoGenericObjectDeletion(object);
					object.myDev.nDeletedFiles--;
				}

			}

		}

		if (chunksBefore >= (chunksAfter = yaffs_GetErasedChunks(dev))) {
			T(YAFFS_TRACE_GC,
					TSTR
					("gc did not increase free chunks before %d after %d"
							+ TENDSTR), chunksBefore, chunksAfter);
		}

		dev.isDoingGC = false;

		return YAFFS_OK;
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
		boolean gcOk = YAFFS_OK;
		int maxTries = 0;

		int checkpointBlockAdjust;

		if (dev.isDoingGC) {
			/* Bail out so we don't get recursive gc */
			return YAFFS_OK;
		}

		/* This loop should pass the first time.
		 * We'll only see looping here if the erase of the collected block fails.
		 */

		do {
			maxTries++;

			checkpointBlockAdjust = (dev.nCheckpointReservedBlocks - dev.blocksInCheckpoint);
			if(checkpointBlockAdjust < 0)
				checkpointBlockAdjust = 0;

			if (dev.nErasedBlocks < (dev.nReservedBlocks + checkpointBlockAdjust)) {
				/* We need a block soon...*/
				aggressive = true;
			} else {
				/* We're in no hurry */
				aggressive = false;
			}

			block = yaffs_FindBlockForGarbageCollection(dev, aggressive);

			if (block > 0) {
				dev.garbageCollections++;
				if (!aggressive) {
					dev.passiveGarbageCollections++;
				}

				T(YAFFS_TRACE_GC,
						TSTR
						("yaffs: GC erasedBlocks %d aggressive %b" + TENDSTR),
						dev.nErasedBlocks, aggressive);

				gcOk = yaffs_GarbageCollectBlock(dev, block);
			}

			if (dev.nErasedBlocks < (dev.nReservedBlocks) && block > 0) {
				T(YAFFS_TRACE_GC,
						TSTR
						("yaffs: GC !!!no reclaim!!! erasedBlocks %d after try %d block %d"
								+ TENDSTR), dev.nErasedBlocks, maxTries, block);
			}
		} while ((dev.nErasedBlocks < dev.nReservedBlocks) && (block > 0)
				&& (maxTries < 2));

		return aggressive ? gcOk : YAFFS_OK;
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
			/*T(("No level 0 found for %d\n", chunkInInode)); */
		}

		if (retVal == -1) {
			/* T(("Could not find %d to delete\n",chunkInInode)); */
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

//	if (in.variantType != YAFFS_OBJECT_TYPE_FILE) {
//	/* T(("Object not a file\n")); */
//	return YAFFS_FAIL;
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
//	/* T(("No level 0 found for %d\n", chunk)); */
//	}
//	}

//	return failed ? YAFFS_FAIL : YAFFS_OK;
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

		if (in.variantType != YAFFS_OBJECT_TYPE_FILE) {
			/* Just ignore an attempt at putting a chunk into a non-file during scanning
			 * If it is not during Scanning then something went wrong!
			 */
			if (!(inScan != 0)) {
				T(YAFFS_TRACE_ERROR,
						TSTR
						("yaffs tragedy:attempt to put data chunk into a non-file"
								+ TENDSTR));
				yaffs2.utils.Globals.portConfiguration.YBUG();
			}

			yaffs_DeleteChunk(dev, chunkInNAND, true, __LINE__());
			return YAFFS_OK;
		}

		tn = yaffs_AddOrFindLevel0Tnode(dev, 
				in.variant.fileVariant(),
				chunkInInode,
				null);
		if (!(tn != null)) {
			return YAFFS_FAIL;
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
					yaffs_ReadChunkWithTagsFromNAND(dev,
							chunkInNAND,
							null, 0, newTags);

					/* Do a proper find */
					existingChunk =
						yaffs_FindChunkInFile(in, chunkInInode,
								existingTags);
				}

				if (existingChunk <= 0) {
					/*Hoosterman - how did this happen? */

					T(YAFFS_TRACE_ERROR,
							TSTR
							("yaffs tragedy: existing chunk < 0 in scan"
									+ TENDSTR));

				}

				/* NB The deleted flags should be false, otherwise the chunks will 
				 * not be loaded during a scan
				 */

				newSerial = newTags.serialNumber;
				existingSerial = existingTags.serialNumber;

				if ((inScan > 0) &&
						(in.myDev.isYaffs2 ||
								existingChunk <= 0 ||
								((existingSerial + 1) & 3) == newSerial)) {
					/* Forward scanning.                            
					 * Use new
					 * Delete the old one and drop through to update the tnode
					 */
					yaffs_DeleteChunk(dev, existingChunk, true,
							__LINE__());
				} else {
					/* Backward scanning or we want to use the existing one
					 * Use existing.
					 * Delete the new one and return early so that the tnode isn't changed
					 */
					yaffs_DeleteChunk(dev, chunkInNAND, true,
							__LINE__());
					return YAFFS_OK;
				}
			}

		}

		if (existingChunk == 0) {
			in.nDataChunks++;
		}

		yaffs_PutLevel0Tnode(dev,tn,chunkInInode,chunkInNAND);

		return YAFFS_OK;
	}

	static boolean yaffs_ReadChunkDataFromObject(yaffs_Object  in, int chunkInInode,
			byte[] buffer, int bufferIndex)
	{
		int chunkInNAND = yaffs_FindChunkInFile(in, chunkInInode, null);

		if (chunkInNAND >= 0) {
			return yaffs_ReadChunkWithTagsFromNAND(in.myDev, chunkInNAND,
					buffer,bufferIndex,null);
		} else {
			T(YAFFS_TRACE_NANDACCESS,
					TSTR("Chunk %d not found zero instead" + TENDSTR),
					chunkInNAND);
			/* get sane (zero) data if you read a hole */
			memset(buffer, bufferIndex, (byte)0, in.myDev.nDataBytesPerChunk);	
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
		block = chunkId / dev.nChunksPerBlock;
		page = chunkId % dev.nChunksPerBlock;

		bi = yaffs_GetBlockInfo(dev, block);

		T(YAFFS_TRACE_DELETION,
				TSTR("line %d delete of chunk %d" + TENDSTR), lyn, chunkId);

		if (markNAND &&
				bi.blockState() != YAFFS_BLOCK_STATE_COLLECTING && !dev.isYaffs2) {

			yaffs_InitialiseTags(tags);

			tags.chunkDeleted = true;

			yaffs_WriteChunkWithTagsToNAND(dev, chunkId, null, 0, tags);
			yaffs_HandleUpdateChunk(dev, chunkId, tags);
		} else {
			dev.nUnmarkedDeletions++;
		}

		/* Pull out of the management area.
		 * If the whole block became dirty, this will kick off an erasure.
		 */
		if (bi.blockState() == YAFFS_BLOCK_STATE_ALLOCATING ||
				bi.blockState() == YAFFS_BLOCK_STATE_FULL ||
				bi.blockState() == YAFFS_BLOCK_STATE_NEEDS_SCANNING ||
				bi.blockState() == YAFFS_BLOCK_STATE_COLLECTING) {
			dev.nFreeChunks++;

			yaffs_ClearChunkBit(dev, block, page);

			bi.setPagesInUse(bi.pagesInUse()-1);

			if (bi.pagesInUse() == 0 &&
					!bi.hasShrinkHeader() &&
					bi.blockState() != YAFFS_BLOCK_STATE_ALLOCATING &&
					bi.blockState() != YAFFS_BLOCK_STATE_NEEDS_SCANNING) {
				yaffs_BlockBecameDirty(dev, block);
			}

		} else {
			/* T(("Bad news deleting chunk %d\n",chunkId)); */
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
		yaffs_InitialiseTags(newTags);

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
				yaffs_DeleteChunk(dev, prevChunkId, true, __LINE__());

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
		byte[] oldName = new byte[YAFFS_MAX_NAME_LENGTH + 1]; final int oldNameIndex = 0;

		yaffs_ObjectHeader oh = null;

		if (!in.fake || force) {

			yaffs_CheckGarbageCollection(dev);

			buffer = yaffs_GetTempBuffer(in.myDev, __LINE__());
			oh = /*(yaffs_ObjectHeader *) buffer*/ new yaffs_ObjectHeader(buffer,0);

			prevChunkId = in.chunkId;

			if (prevChunkId >= 0) {
				result = yaffs_ReadChunkWithTagsFromNAND(dev, prevChunkId,
						buffer, bufferIndex, null);
				memcpy(oldName, oldNameIndex, oh.name(), oh.nameIndex(), (yaffs_ObjectHeader.SIZEOF_name));
			}

			memset(buffer, bufferIndex, (byte)0xFF, dev.nDataBytesPerChunk);

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
				memset(oh.name(), oh.nameIndex(), (byte)0, yaffs_ObjectHeader.SIZEOF_name);
				yaffs_strncpy(oh.name(), oh.nameIndex(), name, nameIndex, YAFFS_MAX_NAME_LENGTH);
			} else if (prevChunkId != 0) {
				memcpy(oh.name(), oh.nameIndex(), oldName, oldNameIndex, yaffs_ObjectHeader.SIZEOF_name);
			} else {
				memset(oh.name(), oh.nameIndex(), (byte)0, yaffs_ObjectHeader.SIZEOF_name);
			}

			oh.setIsShrink(isShrink);

			switch (in.variantType) {
				case YAFFS_OBJECT_TYPE_UNKNOWN:
					/* Should not happen */
					break;
				case YAFFS_OBJECT_TYPE_FILE:
					oh.setFileSize(
							(oh.parentObjectId() == YAFFS_OBJECTID_DELETED
									|| oh.parentObjectId() ==
										YAFFS_OBJECTID_UNLINKED) ? 0 : in.variant.
												fileVariant().fileSize);
					break;
				case YAFFS_OBJECT_TYPE_HARDLINK:
					oh.setEquivalentObjectId(
							in.variant.hardLinkVariant().equivalentObjectId);
					break;
				case YAFFS_OBJECT_TYPE_SPECIAL:
					/* Do nothing */
					break;
				case YAFFS_OBJECT_TYPE_DIRECTORY:
					/* Do nothing */
					break;
				case YAFFS_OBJECT_TYPE_SYMLINK:
					yaffs_strncpy(oh.alias(), oh.aliasIndex(),
							in.variant.symLinkVariant().alias,
							in.variant.symLinkVariant().aliasIndex,
							YAFFS_MAX_ALIAS_LENGTH);
					oh.alias()[oh.aliasIndex()+YAFFS_MAX_ALIAS_LENGTH] = 0;
					break;
			}

			/* Tags */
			yaffs_InitialiseTags(newTags);
			in.serial++;
			newTags.chunkId = 0;
			newTags.objectId = in.objectId;
			newTags.serialNumber = byteAsUnsignedByte(in.serial);

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
							__LINE__());
				}

				if(!yaffs_ObjectHasCachedWriteData(in))
					in.dirty = false;

				/* If this was a shrink, then mark the block that the chunk lives on */
				if (isShrink) {
					bi = yaffs_GetBlockInfo(in.myDev,
							newChunkId /in.myDev.	nChunksPerBlock);
					bi.setHasShrinkHeader(true);
				}

			}

			retVal = newChunkId;

		}

		if (buffer != null)
			yaffs_ReleaseTempBuffer(dev, buffer, __LINE__());

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
		int nCaches = obj.myDev.nShortOpCaches;

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
		int nCaches = obj.myDev.nShortOpCaches;

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
				T(YAFFS_TRACE_ERROR,
						(TSTR("yaffs tragedy: no space during cache write" + TENDSTR)));

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
		int nCaches = dev.nShortOpCaches;
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

		if (dev.nShortOpCaches > 0) {
			for (i = 0; i < dev.nShortOpCaches; i++) {
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

		if (dev.nShortOpCaches > 0) {
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

				for (i = 0; i < dev.nShortOpCaches; i++) {
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
		if (dev.nShortOpCaches > 0) {
			for (i = 0; i < dev.nShortOpCaches; i++) {
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

		if (dev.nShortOpCaches > 0) {
			if (dev.srLastUse < 0 || dev.srLastUse > 100000000) {
				/* Reset the cache usages */
				int i;
				for (i = 1; i < dev.nShortOpCaches; i++) {
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
		if (object.myDev.nShortOpCaches > 0) {
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

		if (dev.nShortOpCaches > 0) {
			/* Invalidate it. */
			for (i = 0; i < dev.nShortOpCaches; i++) {
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
		cp.setMagic(YAFFS_MAGIC);
		cp.setVersion(YAFFS_CHECKPOINT_VERSION);
		cp.setHead((head != 0) ? 1 : 0);

		return (yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,
				/*sizeof(cp)*/ yaffs_CheckpointValidity.SERIALIZED_LENGTH) 
				== /*sizeof(cp)*/ yaffs_CheckpointValidity.SERIALIZED_LENGTH) ? true : false;
	}

	static boolean yaffs_ReadCheckpointValidityMarker(yaffs_Device dev, int head)
	{
		yaffs_CheckpointValidity cp = new yaffs_CheckpointValidity();
		boolean ok;

		ok = (yaffs_CheckpointRead(dev,cp.serialized,cp.offset,
				yaffs_CheckpointValidity.SERIALIZED_LENGTH) == yaffs_CheckpointValidity.SERIALIZED_LENGTH);

		if(ok)
			ok = (cp.structType() == yaffs_CheckpointValidity.SERIALIZED_LENGTH) &&
			(cp.magic() == YAFFS_MAGIC) &&
			(cp.version() == YAFFS_CHECKPOINT_VERSION) &&
			(cp.head() == ((head != 0) ? 1 : 0));
		return (ok) ? true : false;
	}

	static void yaffs_DeviceToCheckpointDevice(yaffs_CheckpointDevice cp, 
			yaffs_Device dev)
	{
		cp.setNErasedBlocks(dev.nErasedBlocks);
		cp.setAllocationBlock(dev.allocationBlock);
		cp.setAllocationPage(dev.allocationPage);
		cp.setNFreeChunks(dev.nFreeChunks);

		cp.setNDeletedFiles(dev.nDeletedFiles);
		cp.setNUnlinkedFiles(dev.nUnlinkedFiles);
		cp.setNBackgroundDeletions(dev.nBackgroundDeletions);
		cp.setSequenceNumber((int)dev.sequenceNumber);
		cp.setOldestDirtySequence((int)dev.oldestDirtySequence);

	}

	static void yaffs_CheckpointDeviceToDevice(yaffs_Device dev,
			yaffs_CheckpointDevice cp)
	{
		dev.nErasedBlocks = cp.nErasedBlocks();
		dev.allocationBlock = cp.allocationBlock();
		dev.allocationPage = cp.allocationPage();
		dev.nFreeChunks = cp.nFreeChunks();

		dev.nDeletedFiles = cp.nDeletedFiles();
		dev.nUnlinkedFiles = cp.nUnlinkedFiles();
		dev.nBackgroundDeletions = cp.nBackgroundDeletions();
		dev.sequenceNumber = intAsUnsignedInt(cp.sequenceNumber());
		dev.oldestDirtySequence = intAsUnsignedInt(cp.oldestDirtySequence());
	}


	static boolean yaffs_WriteCheckpointDevice(yaffs_Device dev)
	{
		yaffs_CheckpointDevice cp = new yaffs_CheckpointDevice();
		/**__u32*/ int nBytes;
		/**__u32*/ int nBlocks = (dev.internalEndBlock - dev.internalStartBlock + 1);

		boolean ok;

		/* Write device runtime values*/
		yaffs_DeviceToCheckpointDevice(cp,dev);
		cp.setStructType(yaffs_CheckpointDevice.SERIALIZED_LENGTH);

		ok = (yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,yaffs_CheckpointDevice.SERIALIZED_LENGTH) == yaffs_CheckpointDevice.SERIALIZED_LENGTH);

		/* Write block info */
		if(ok) {
			nBytes = nBlocks * yaffs_BlockInfo.SERIALIZED_LENGTH;
			// PORT We want to write the data for the whole array.
			// The data for all yaffs_BlockInfo array members is stored in the same array.
			ok = (yaffs_CheckpointWrite(dev,dev.blockInfo[0].serialized,
					dev.blockInfo[0].offset,nBytes) == nBytes);
		}

		/* Write chunk bits */		
		if(ok) {
			nBytes = nBlocks * dev.chunkBitmapStride;
			ok = (yaffs_CheckpointWrite(dev,dev.chunkBits,dev.chunkBitsIndex,nBytes) == nBytes);
		}
		return	 ok ? true : false;

	}

	static boolean yaffs_ReadCheckpointDevice(yaffs_Device dev)
	{
		yaffs_CheckpointDevice cp = new yaffs_CheckpointDevice();
		/**__u32*/ int nBytes;
		/**__u32*/ int nBlocks = (dev.internalEndBlock - dev.internalStartBlock + 1);

		boolean ok;	

		ok = (yaffs_CheckpointRead(dev,cp.serialized,cp.offset,yaffs_CheckpointDevice.SERIALIZED_LENGTH) == yaffs_CheckpointDevice.SERIALIZED_LENGTH);
		if(!(ok))
			return false;

		if(cp.structType() != yaffs_CheckpointDevice.SERIALIZED_LENGTH)
			return false;


		yaffs_CheckpointDeviceToDevice(dev,cp);

		nBytes = nBlocks * yaffs_BlockInfo.SERIALIZED_LENGTH;

		ok = (yaffs_CheckpointRead(dev,dev.blockInfo[0].serialized,dev.blockInfo[0].offset,
				nBytes) == nBytes);

		if(!(ok))
			return false;
		nBytes = nBlocks * dev.chunkBitmapStride;

		ok = (yaffs_CheckpointRead(dev,dev.chunkBits,dev.chunkBitsIndex,nBytes) == nBytes);

		return ok ? true : false;
	}

	static void yaffs_ObjectToCheckpointObject(yaffs_CheckpointObject cp,
			yaffs_Object obj)
	{

		cp.setObjectId(obj.objectId);
		cp.setParentId((obj.parent != null) ? obj.parent.objectId : 0);
		cp.setChunkId(obj.chunkId);
		cp.setVariantType(obj.variantType);			
		cp.setDeleted(obj.deleted);
		cp.setSoftDeleted(obj.softDeleted);
		cp.setUnlinked(obj.unlinked);
		cp.setFake(obj.fake);
		cp.setRenameAllowed(obj.renameAllowed);
		cp.setUnlinkAllowed(obj.unlinkAllowed);
		cp.setSerial(obj.serial);
		cp.setNDataChunks(obj.nDataChunks);

		if(obj.variantType == YAFFS_OBJECT_TYPE_FILE)
			cp.setFileSizeOrEquivalentObjectId(obj.variant.fileVariant().fileSize);
		else if(obj.variantType == YAFFS_OBJECT_TYPE_HARDLINK)
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
					YAFFS_OBJECT_TYPE_DIRECTORY);
		else
			parent = null;

		if(parent != null)
			yaffs_AddObjectToDirectory(parent, obj);

		obj.chunkId = cp.chunkId();
		obj.variantType = cp.variantType();			
		obj.deleted = cp.deleted();
		obj.softDeleted = cp.softDeleted();
		obj.unlinked = cp.unlinked();
		obj.fake = cp.fake();
		obj.renameAllowed = cp.renameAllowed();
		obj.unlinkAllowed = cp.unlinkAllowed();
		obj.serial = cp.serial();
		obj.nDataChunks = cp.nDataChunks();

		if(obj.variantType == YAFFS_OBJECT_TYPE_FILE)
			obj.variant.fileVariant().fileSize = cp.fileSizeOrEquivalentObjectId();
		else if(obj.variantType == YAFFS_OBJECT_TYPE_HARDLINK)
			obj.variant.hardLinkVariant().equivalentObjectId = cp.fileSizeOrEquivalentObjectId();

		if(obj.objectId >= YAFFS_NOBJECT_BUCKETS)
			obj.lazyLoaded = true;
	}



	// XXX what is written here
	// - only leafs (16 bit page address)
	// - or pointers, too?
	static boolean yaffs_CheckpointTnodeWorker(yaffs_Object  in, yaffs_Tnode tn,
			/**__u32*/ int level, int chunkOffset)
	{
		int i;
		yaffs_Device dev = in.myDev;
		boolean ok = true;
		int nTnodeBytes = (dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8;

		if (tn != null) {
			if (level > 0) {

				for (i = 0; i < YAFFS_NTNODES_INTERNAL && ok; i++){
					if (tn.internal[i] != null) {
						ok = yaffs_CheckpointTnodeWorker(in,
								tn.internal[i],
								level - 1,
								(chunkOffset<<YAFFS_TNODES_INTERNAL_BITS) + i);
					}
				}
			} else if (level == 0) {
				/*__u32*/ byte[] baseOffset = new byte[4]; final int baseOffsetIndex = 0;
				writeIntToByteArray(baseOffset, baseOffsetIndex, 
						chunkOffset <<  YAFFS_TNODES_LEVEL0_BITS);	// XXX haaaaaa?
				/* printf("write tnode at %d\n",baseOffset); */
				ok = (yaffs_CheckpointWrite(dev,baseOffset,baseOffsetIndex,4) == 4);
				if(ok)
					ok = (yaffs_CheckpointWrite(dev,tn.serialized,tn.offset,nTnodeBytes) == nTnodeBytes);
			}
		}

		return ok;

	}

	static boolean yaffs_WriteCheckpointTnodes(yaffs_Object obj)
	{
		/**__u32*/ byte[] endMarker = new byte[SIZEOF_INT]; final int endMarkerIndex = 0;
		writeIntToByteArray(endMarker, endMarkerIndex, ~0);
		boolean ok = true;

		if(obj.variantType == YAFFS_OBJECT_TYPE_FILE){
			ok = yaffs_CheckpointTnodeWorker(obj,
					obj.variant.fileVariant().top,
					obj.variant.fileVariant().topLevel,
					0);
			if(ok)
				ok = (yaffs_CheckpointWrite(obj.myDev,endMarker,endMarkerIndex,SIZEOF_INT) == 
					SIZEOF_INT);
		}

		return ok ? true : false;
	}

	static boolean yaffs_ReadCheckpointTnodes(yaffs_Object obj)
	{
		/**__u32*/ byte[] baseChunk = new byte[SIZEOF_INT];
		final int baseChunkIndex = 0;
		boolean ok = true;
		yaffs_Device dev = obj.myDev;
		yaffs_FileStructure fileStructPtr = obj.variant.fileVariant();
		yaffs_Tnode tn;

		ok = (yaffs_CheckpointRead(dev,baseChunk,baseChunkIndex,SIZEOF_INT) == SIZEOF_INT);

		while(ok && (~getIntFromByteArray(baseChunk, baseChunkIndex)) != 0){
			/* Read level 0 tnode */

			/* printf("read  tnode at %d\n",baseChunk); */
			tn = yaffs_GetTnodeRaw(dev);
			if(tn != null)
				ok = (yaffs_CheckpointRead(dev,tn.serialized,tn.offset,
						(dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8) ==
							(dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8);
			else
				ok = false;

			if(tn != null && ok){
				ok = yaffs_AddOrFindLevel0Tnode(dev,
						fileStructPtr,
						getIntFromByteArray(baseChunk, baseChunkIndex),
						tn) != null ? true : false;
			}

			if(ok)
				ok = (yaffs_CheckpointRead(dev,baseChunk,baseChunkIndex,
						SIZEOF_INT) == 
							SIZEOF_INT);

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

		for(i = 0; ok &&  i <  YAFFS_NOBJECT_BUCKETS; i++){
//			list_for_each(lh, &dev.objectBucket[i].list) {
			for (lh = dev.objectBucket[i].list.next(); lh != dev.objectBucket[i].list;
			lh = lh.next()) {
				if (lh != null) {
					obj = /*list_entry(lh, yaffs_Object, hashLink)*/ (yaffs_Object)lh.list_entry;
					if (!obj.deferedFree) {
						memset(cp.serialized,0,(byte)0,cp.getSerializedLength());
						yaffs_ObjectToCheckpointObject(cp,obj);
						cp.setStructType(yaffs_CheckpointObject.SERIALIZED_LENGTH);

						T(YAFFS_TRACE_CHECKPOINT,
								TSTR("Checkpoint write object %d parent %d type %d chunk %d obj addr %x" + TENDSTR),
								cp.objectId(),cp.parentId(),cp.variantType(),cp.chunkId(),/*(unsigned)*/yaffs2.utils.Utils.hashCode(obj));

						ok = (yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,
								yaffs_CheckpointObject.SERIALIZED_LENGTH)) == yaffs_CheckpointObject.SERIALIZED_LENGTH;

						if(ok && obj.variantType == YAFFS_OBJECT_TYPE_FILE){
							ok = yaffs_WriteCheckpointTnodes(obj);
						}
					}
				}
			}
		}

		/* Dump end of list */
		memset(cp,(byte)0xFF/*,sizeof(yaffs_CheckpointObject)*/);
		cp.setStructType(yaffs_CheckpointObject.SERIALIZED_LENGTH);

		if(ok)
			ok = (yaffs_CheckpointWrite(dev,cp.serialized,cp.offset,
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
			ok = (yaffs_CheckpointRead(dev,cp.serialized,cp.offset,
					yaffs_CheckpointObject.SERIALIZED_LENGTH) == yaffs_CheckpointObject.SERIALIZED_LENGTH);
			if(cp.structType() != yaffs_CheckpointObject.SERIALIZED_LENGTH) {
				/* printf("structure parsing failed\n"); */
				ok = false;
			}

			if(ok && cp.objectId() == ~0)
				done = true;
			else if(ok){
				obj = yaffs_FindOrCreateObjectByNumber(dev,cp.objectId(), cp.variantType());
				T(YAFFS_TRACE_CHECKPOINT,TSTR("Checkpoint read object %d parent %d type %d chunk %d obj addr %x" + TENDSTR),
						cp.objectId(),cp.parentId(),cp.variantType(),cp.chunkId(),/*(unsigned)*/yaffs2.utils.Utils.hashCode(obj));
				if(obj != null) {
					yaffs_CheckpointObjectToObject(obj,cp);
					if(obj.variantType == YAFFS_OBJECT_TYPE_FILE) {
						ok = yaffs_ReadCheckpointTnodes(obj);
					} else if(obj.variantType == YAFFS_OBJECT_TYPE_HARDLINK) {
						obj.hardLinks.next =	// XXX what does this do?
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

		ok = yaffs_CheckpointOpen(dev,true) ? true : false;

		if(ok)
			ok = yaffs_WriteCheckpointValidityMarker(dev,1);
		if(ok)
			ok = yaffs_WriteCheckpointDevice(dev) ? true : false;
		if(ok)
			ok = yaffs_WriteCheckpointObjects(dev) ? true : false;
		if(ok)
			ok = yaffs_WriteCheckpointValidityMarker(dev,0);

		if(!yaffs_CheckpointClose(dev))
			ok = false;

		if(ok)
			dev.isCheckpointed = true;
		else 
			dev.isCheckpointed = false;

		return dev.isCheckpointed;
	}

	static boolean yaffs_ReadCheckpointData(yaffs_Device dev)
	{
		boolean ok;

		ok = yaffs_CheckpointOpen(dev,false); /* open for read */

		if(ok)
			ok = yaffs_ReadCheckpointValidityMarker(dev,1);
		if(ok)
			ok = yaffs_ReadCheckpointDevice(dev);
		if(ok)
			ok = yaffs_ReadCheckpointObjects(dev);
		if(ok)
			ok = yaffs_ReadCheckpointValidityMarker(dev,0);



		if(!yaffs_CheckpointClose(dev))
			ok = false;

		if(ok)
			dev.isCheckpointed = true;
		else 
			dev.isCheckpointed = false;

		return ok ? true : false;

	}

	static void yaffs_InvalidateCheckpoint(yaffs_Device dev)
	{
		if(dev.isCheckpointed || 
				dev.blocksInCheckpoint > 0){
			dev.isCheckpointed = false;
			yaffs_CheckpointInvalidateStream(dev);
			if(dev.superBlock != null && dev.markSuperBlockDirty != null)
				dev.markSuperBlockDirty.markSuperBlockDirty(dev.superBlock);
		}
	}


	static boolean yaffs_CheckpointSave(yaffs_Device dev)
	{
		yaffs_ReportOddballBlocks(dev);
		T(YAFFS_TRACE_CHECKPOINT,TSTR("save entry: isCheckpointed %b"+ TENDSTR),dev.isCheckpointed);

		if(!dev.isCheckpointed)
			yaffs_WriteCheckpointData(dev);

		T(YAFFS_TRACE_CHECKPOINT,TSTR("save exit: isCheckpointed %b"+ TENDSTR),dev.isCheckpointed);

		return dev.isCheckpointed;
	}

	static boolean yaffs_CheckpointRestore(yaffs_Device dev)
	{
		boolean retval;
		T(YAFFS_TRACE_CHECKPOINT,TSTR("restore entry: isCheckpointed %b"+ TENDSTR),dev.isCheckpointed);

		retval = yaffs_ReadCheckpointData(dev);

		T(YAFFS_TRACE_CHECKPOINT,TSTR("restore exit: isCheckpointed %b"+ TENDSTR),dev.isCheckpointed);

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
			if ((start + n) < dev.nDataBytesPerChunk) {
				nToCopy = n;
			} else {
				nToCopy = dev.nDataBytesPerChunk - start;
			}

			cache = yaffs_FindChunkCache(in, chunk);

			/* If the chunk is already in the cache or it is less than a whole chunk
			 * then use the cache (if there is caching)
			 * else bypass the cache.
			 */
			if (cache != null || nToCopy != dev.nDataBytesPerChunk) {
				if (dev.nShortOpCaches > 0) {

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
					memcpy(buffer, bufferIndex, cache.data, cache.dataIndex+start, nToCopy);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_LockYAFFS(TRUE);
//					#endif
					cache.locked = false;
				} else {
					/* Read into the local buffer then copy..*/

					/*__u8 **/ byte[] localBuffer =
						yaffs_GetTempBuffer(dev, __LINE__());
					final int localBufferIndex = 0;
					yaffs_ReadChunkDataFromObject(in, chunk,
							localBuffer, localBufferIndex);
//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_UnlockYAFFS(TRUE);
//					#endif
					memcpy(buffer, bufferIndex, localBuffer, localBufferIndex+start, nToCopy);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_LockYAFFS(TRUE);
//					#endif
					yaffs_ReleaseTempBuffer(dev, localBuffer,
							__LINE__());
				}

			} else {
//				#ifdef CONFIG_YAFFS_WINCE
//				__u8 *localBuffer = yaffs_GetTempBuffer(dev, __LINE__);

//				/* Under WinCE can't do direct transfer. Need to use a local buffer.
//				* This is because we otherwise screw up WinCE's memory mapper
//				*/
//				yaffs_ReadChunkDataFromObject(in, chunk, localBuffer);

//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_UnlockYAFFS(TRUE);
//				#endif
//				memcpy(buffer, localBuffer, dev.nDataBytesPerChunk);

//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_LockYAFFS(TRUE);
//				yaffs_ReleaseTempBuffer(dev, localBuffer, __LINE__);
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

			if ((start + n) < dev.nDataBytesPerChunk) {
				nToCopy = n;

				/* Now folks, to calculate how many bytes to write back....
				 * If we're overwriting and not writing to then end of file then
				 * we need to write back as much as was there before.
				 */

				nBytesRead =
					in.variant.fileVariant().fileSize -
					((chunk - 1) * dev.nDataBytesPerChunk);

				if (nBytesRead > dev.nDataBytesPerChunk) {
					nBytesRead = dev.nDataBytesPerChunk;
				}

				nToWriteBack =
					(nBytesRead >
					(start + n)) ? nBytesRead : (start + n);

			} else {
				nToCopy = dev.nDataBytesPerChunk - start;
				nToWriteBack = dev.nDataBytesPerChunk;
			}

			if (nToCopy != dev.nDataBytesPerChunk) {
				/* An incomplete start or end chunk (or maybe both start and end chunk) */
				if (dev.nShortOpCaches > 0) {
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

						memcpy(cache.data, cache.dataIndex+start, buffer, bufferIndex,
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
						yaffs_GetTempBuffer(dev, __LINE__());
					final int localBufferIndex = 0;

					yaffs_ReadChunkDataFromObject(in, chunk,
							localBuffer, localBufferIndex);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_UnlockYAFFS(TRUE);
//					#endif

					memcpy(localBuffer, start, buffer, bufferIndex, nToCopy);

//					#ifdef CONFIG_YAFFS_WINCE
//					yfsd_LockYAFFS(TRUE);
//					#endif
					chunkWritten =
						yaffs_WriteChunkDataToObject(in, chunk,
								localBuffer, localBufferIndex,
								nToWriteBack,
								false);

					yaffs_ReleaseTempBuffer(dev, localBuffer,
							__LINE__());

				}

			} else {

//				#ifdef CONFIG_YAFFS_WINCE
//				/* Under WinCE can't do direct transfer. Need to use a local buffer.
//				* This is because we otherwise screw up WinCE's memory mapper
//				*/
//				__u8 *localBuffer = yaffs_GetTempBuffer(dev, __LINE__);
//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_UnlockYAFFS(TRUE);
//				#endif
//				memcpy(localBuffer, buffer, dev.nDataBytesPerChunk);
//				#ifdef CONFIG_YAFFS_WINCE
//				yfsd_LockYAFFS(TRUE);
//				#endif
//				chunkWritten =
//				yaffs_WriteChunkDataToObject(in, chunk, localBuffer,
//				dev.nDataBytesPerChunk,
//				0);
//				yaffs_ReleaseTempBuffer(dev, localBuffer, __LINE__);
//				#else
				/* A full chunk. Write directly from the supplied buffer. */
				chunkWritten =
					yaffs_WriteChunkDataToObject(in, chunk, buffer, bufferIndex,
							dev.nDataBytesPerChunk,
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

		int lastDel = 1 + (oldFileSize - 1) / dev.nDataBytesPerChunk;

		int startDel = 1 + (newSize + dev.nDataBytesPerChunk - 1) /
		dev.nDataBytesPerChunk;
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
						(dev.internalStartBlock * dev.nChunksPerBlock)
						|| chunkId >=
							((dev.internalEndBlock +
									1) * dev.nChunksPerBlock)) {
					T(YAFFS_TRACE_ALWAYS,
							TSTR("Found daft chunkId %d for %d" + TENDSTR),
							chunkId, i);
				} else {
					in.nDataChunks--;
					yaffs_DeleteChunk(dev, chunkId, true, __LINE__());
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

		if (in.variantType != YAFFS_OBJECT_TYPE_FILE) {
			return yaffs_GetFileSize(in);
		}

		if (newSize == oldFileSize) {
			return oldFileSize;
		}

		if (newSize < oldFileSize) {

			yaffs_PruneResizedChunks(in, newSize);

			if (newSizeOfPartialChunk != 0) {
				int lastChunk = 1 + newFullChunks;

				/*__u8 **/ byte[] localBuffer = yaffs_GetTempBuffer(dev, __LINE__());
				final int localBufferIndex = 0;

				/* Got to read and rewrite the last chunk with its new size and zero pad */
				yaffs_ReadChunkDataFromObject(in, lastChunk,
						localBuffer, localBufferIndex);

				memset(localBuffer, localBufferIndex+newSizeOfPartialChunk, (byte)0,
						dev.nDataBytesPerChunk - newSizeOfPartialChunk);

				yaffs_WriteChunkDataToObject(in, lastChunk, localBuffer, localBufferIndex,
						newSizeOfPartialChunk, true);

				yaffs_ReleaseTempBuffer(dev, localBuffer, __LINE__());
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
		if (in.parent.objectId != YAFFS_OBJECTID_UNLINKED &&
				in.parent.objectId != YAFFS_OBJECTID_DELETED) {
			yaffs_UpdateObjectHeader(in, null, 0, false,
					(newSize < oldFileSize) ? true : false, 0);
		}

		return newSize;
	}

	static /*loff_t*/ int yaffs_GetFileSize(yaffs_Object  obj)
	{
		obj = yaffs_GetEquivalentObject(obj);

		switch (obj.variantType) {
			case YAFFS_OBJECT_TYPE_FILE:
				return obj.variant.fileVariant().fileSize;
			case YAFFS_OBJECT_TYPE_SYMLINK:
				return yaffs_strlen(obj.variant.symLinkVariant().alias,
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

				in.yst_mtime = Y_CURRENT_TIME();

//				#endif
			}

			retVal =
				(yaffs_UpdateObjectHeader(in, null, 0, false, false, 0) >=
					0) ? YAFFS_OK : YAFFS_FAIL;
		} else {
			retVal = YAFFS_OK;
		}

		return retVal;

	}

	static boolean yaffs_DoGenericObjectDeletion(yaffs_Object  in)
	{

		/* First off, invalidate the file's data in the cache, without flushing. */
		yaffs_InvalidateWholeChunkCache(in);

		if (in.myDev.isYaffs2 && (in.parent != in.myDev.deletedDir)) {
			/* Move to the unlinked directory so we have a record that it was deleted. */
			yaffs_ChangeObjectName(in, in.myDev.deletedDir, null, 0, false, 0);

		}

		yaffs_RemoveObjectFromDirectory(in);
		yaffs_DeleteChunk(in.myDev, in.chunkId, true, __LINE__());
		in.chunkId = -1;

		yaffs_FreeObject(in);
		return YAFFS_OK;

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
				T(YAFFS_TRACE_TRACING,
						TSTR("yaffs: immediate deletion of file %d" + TENDSTR),
						in.objectId);
				in.deleted = true;
				in.myDev.nDeletedFiles++;
				if (false && in.myDev.isYaffs2) {
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
		boolean retVal = YAFFS_OK;

		if (in.nDataChunks > 0) {
			/* Use soft deletion if there is data in the file */
			if (!in.unlinked) {
				retVal = yaffs_UnlinkFile(in);
			}
			if (retVal == YAFFS_OK && in.unlinked && !in.deleted) {
				in.deleted = true;
				in.myDev.nDeletedFiles++;
				yaffs_SoftDeleteFile(in);
			}
			return in.deleted ? YAFFS_OK : YAFFS_FAIL;
		} else {
			/* The file has no data chunks so we toss it immediately */
			yaffs_FreeTnode(in.myDev, in.variant.fileVariant().top);
			in.variant.fileVariant().top = null;
			yaffs_DoGenericObjectDeletion(in);

			return YAFFS_OK;
		}
	}

	static boolean yaffs_DeleteDirectory(yaffs_Object  in)
	{
		/* First check that the directory is empty. */
		if (list_empty(in.variant.directoryVariant().children)) {
			return yaffs_DoGenericObjectDeletion(in);
		}

		return YAFFS_FAIL;

	}

	static boolean yaffs_DeleteSymLink(yaffs_Object  in)
	{
		YFREE(in.variant.symLinkVariant().alias);

		return yaffs_DoGenericObjectDeletion(in);
	}

	static boolean yaffs_DeleteHardLink(yaffs_Object  in)
	{
		/* remove this hardlink from the list assocaited with the equivalent
		 * object
		 */
		list_del(in.hardLinks);
		return yaffs_DoGenericObjectDeletion(in);
	}

	static void yaffs_DestroyObject(yaffs_Object  obj)
	{
		switch (obj.variantType) {
			case YAFFS_OBJECT_TYPE_FILE:
				yaffs_DeleteFile(obj);
				break;
			case YAFFS_OBJECT_TYPE_DIRECTORY:
				yaffs_DeleteDirectory(obj);
				break;
			case YAFFS_OBJECT_TYPE_SYMLINK:
				yaffs_DeleteSymLink(obj);
				break;
			case YAFFS_OBJECT_TYPE_HARDLINK:
				yaffs_DeleteHardLink(obj);
				break;
			case YAFFS_OBJECT_TYPE_SPECIAL:
				yaffs_DoGenericObjectDeletion(obj);
				break;
			case YAFFS_OBJECT_TYPE_UNKNOWN:
				break;		/* should not happen. */
		}
	}

	static boolean yaffs_UnlinkWorker(yaffs_Object  obj)
	{

		if (obj.variantType == YAFFS_OBJECT_TYPE_HARDLINK) {
			return yaffs_DeleteHardLink(obj);
		} else if (!list_empty(obj.hardLinks)) {
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
			/*YCHAR*/ byte[] name = new byte[YAFFS_MAX_NAME_LENGTH + 1];
			final int nameIndex = 0;

			hl = /*list_entry(obj.hardLinks.next, yaffs_Object, hardLinks)*/ (yaffs_Object)obj.parent;

			list_del_init(hl.hardLinks);
			list_del_init(hl.siblings);

			yaffs_GetObjectName(hl, name, nameIndex, YAFFS_MAX_NAME_LENGTH + 1);

			retVal = yaffs_ChangeObjectName(obj, hl.parent, name, nameIndex, false, 0);

			if (retVal == YAFFS_OK) {
				retVal = yaffs_DoGenericObjectDeletion(hl);
			}
			return retVal;

		} else {
			switch (obj.variantType) {
				case YAFFS_OBJECT_TYPE_FILE:
					return yaffs_UnlinkFile(obj);
//					break;
				case YAFFS_OBJECT_TYPE_DIRECTORY:
					return yaffs_DeleteDirectory(obj);
//					break;
				case YAFFS_OBJECT_TYPE_SYMLINK:
					return yaffs_DeleteSymLink(obj);
//					break;
				case YAFFS_OBJECT_TYPE_SPECIAL:
					return yaffs_DoGenericObjectDeletion(obj);
//					break;
				case YAFFS_OBJECT_TYPE_HARDLINK:
				case YAFFS_OBJECT_TYPE_UNKNOWN:
				default:
					return YAFFS_FAIL;
			}
		}
	}


	static boolean yaffs_UnlinkObject( yaffs_Object obj)
	{

		if (obj != null && obj.unlinkAllowed) {
			return yaffs_UnlinkWorker(obj);
		}

		return YAFFS_FAIL;

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
					YAFFS_OBJECT_TYPE_FILE);
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
				list_add(hl.hardLinks, in.hardLinks);
			} else {
				/* Todo Need to report/handle this better.
				 * Got a problem... hardlink to a non-existant object
				 */
				hl.variant.hardLinkVariant().equivalentObject = null;
				INIT_LIST_HEAD(hl.hardLinks);

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

	static boolean yaffs_Scan(yaffs_Device dev)
	{
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		int blk;
		int blockIterator;
		int startIterator;
		int endIterator;
		int nBlocksToScan = 0;
		boolean result;

		int chunk;
		int c;
		int deleted;
		/*yaffs_BlockState*/ int state;
		yaffs_Object hardList = null;
		yaffs_Object hl;
		yaffs_BlockInfo bi;
		long sequenceNumber;
		yaffs_ObjectHeader oh;
		yaffs_Object in;
		yaffs_Object parent;
		int nBlocks = dev.internalEndBlock - dev.internalStartBlock + 1;

		/*__u8 **/ byte[] chunkData;
		final int chunkDataIndex;

		yaffs_BlockIndex[] blockIndex = null;

		if (dev.isYaffs2) {
			T(YAFFS_TRACE_SCAN,
					TSTR("yaffs_Scan is not for YAFFS2!" + TENDSTR));
			return YAFFS_FAIL;
		}

		//TODO  Throw all the yaffs2 stuuf out of yaffs_Scan since it is only for yaffs1 format.

		T(YAFFS_TRACE_SCAN,
				TSTR("yaffs_Scan starts  intstartblk %d intendblk %d..." + TENDSTR),
				dev.internalStartBlock, dev.internalEndBlock);

		chunkData = yaffs_GetTempBuffer(dev, __LINE__());
		chunkDataIndex = 0;

		dev.sequenceNumber = YAFFS_LOWEST_SEQUENCE_NUMBER;

		if (dev.isYaffs2) {
			blockIndex = YMALLOC_BLOCKINDEX(nBlocks/* * sizeof(yaffs_BlockIndex)*/ );
		}

		/* Scan all the blocks to determine their state */
		for (blk = dev.internalStartBlock; blk <= dev.internalEndBlock; blk++) {
			bi = yaffs_GetBlockInfo(dev, blk);
			yaffs_ClearChunkBits(dev, blk);
			bi.setPagesInUse(0);
			bi.setSoftDeletions(0);

			IntegerPointer statePointer = new IntegerPointer();
			IntegerPointer sequenceNumberPointer = new IntegerPointer();
			yaffs_QueryInitialBlockState(dev, blk, statePointer, sequenceNumberPointer);
			state = statePointer.dereferenced;
			sequenceNumber = sequenceNumberPointer.dereferenced;

			bi.setBlockState(state);
			bi.setSequenceNumber((int)sequenceNumber);

			T(YAFFS_TRACE_SCAN_DEBUG,
					TSTR("Block scanning block %d state %d seq %l" + TENDSTR), blk,
					state, sequenceNumber);

			if (state == YAFFS_BLOCK_STATE_DEAD) {
				T(YAFFS_TRACE_BAD_BLOCKS,
						TSTR("block %d is bad" + TENDSTR), blk);
			} else if (state == YAFFS_BLOCK_STATE_EMPTY) {
				T(YAFFS_TRACE_SCAN_DEBUG,
						(TSTR("Block empty " + TENDSTR)));
				dev.nErasedBlocks++;
				dev.nFreeChunks += dev.nChunksPerBlock;
			} else if (state == YAFFS_BLOCK_STATE_NEEDS_SCANNING) {

				/* Determine the highest sequence number */
				if (dev.isYaffs2 &&
						sequenceNumber >= YAFFS_LOWEST_SEQUENCE_NUMBER &&
						sequenceNumber < YAFFS_HIGHEST_SEQUENCE_NUMBER) {

					blockIndex[nBlocksToScan].seq = (int)sequenceNumber;
					blockIndex[nBlocksToScan].block = blk;

					nBlocksToScan++;

					if (sequenceNumber >= dev.sequenceNumber) {
						dev.sequenceNumber = sequenceNumber;
					}
				} else if (dev.isYaffs2) {
					/* TODO: Nasty sequence number! */
					T(YAFFS_TRACE_SCAN,
							TSTR
							("Block scanning block %d has bad sequence number %l"
									+ TENDSTR), blk, sequenceNumber);

				}
			}
		}

		/* Sort the blocks
		 * Dungy old bubble sort for now...
		 */
		if (dev.isYaffs2) {
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
		if (dev.isYaffs2) {
			startIterator = 0;
			endIterator = nBlocksToScan - 1;
			T(YAFFS_TRACE_SCAN_DEBUG,
					TSTR("%d blocks to be scanned" + TENDSTR), nBlocksToScan);
		} else {
			startIterator = dev.internalStartBlock;
			endIterator = dev.internalEndBlock;
		}

		/* For each block.... */
		for (blockIterator = startIterator; blockIterator <= endIterator;
		blockIterator++) {

			if (dev.isYaffs2) {
				/* get the block to scan in the correct order */
				blk = blockIndex[blockIterator].block;
			} else {
				blk = blockIterator;
			}

			bi = yaffs_GetBlockInfo(dev, blk);
			state = bi.blockState();

			deleted = 0;

			/* For each chunk in each block that needs scanning....*/
			for (c = 0; c < dev.nChunksPerBlock &&
			state == YAFFS_BLOCK_STATE_NEEDS_SCANNING; c++) {
				/* Read the tags and decide what to do */
				chunk = blk * dev.nChunksPerBlock + c;

				result = yaffs_ReadChunkWithTagsFromNAND(dev, chunk, null, 0,
						tags);

				/* Let's have a good look at this chunk... */

				if (!dev.isYaffs2 && tags.chunkDeleted) {
					/* YAFFS1 only...
					 * A deleted chunk
					 */
					deleted++;
					dev.nFreeChunks++;
					/*T((" %d %d deleted\n",blk,c)); */
				} else if (!tags.chunkUsed) {
					/* An unassigned chunk in the block
					 * This means that either the block is empty or 
					 * this is the one being allocated from
					 */

					if (c == 0) {
						/* We're looking at the first chunk in the block so the block is unused */
						state = YAFFS_BLOCK_STATE_EMPTY;
						dev.nErasedBlocks++;
					} else {
						/* this is the block being allocated from */
						T(YAFFS_TRACE_SCAN,
								TSTR
								(" Allocating from %d %d" + TENDSTR),
								blk, c);
						state = YAFFS_BLOCK_STATE_ALLOCATING;
						dev.allocationBlock = blk;
						dev.allocationPage = c;
						dev.allocationBlockFinder = blk;	
						/* Set it to here to encourage the allocator to go forth from here. */

						/* Yaffs2 sanity check:
						 * This should be the one with the highest sequence number
						 */
						if (dev.isYaffs2
								&& (dev.sequenceNumber !=
									intAsUnsignedInt(bi.sequenceNumber()))) {
							T(YAFFS_TRACE_ALWAYS,
									TSTR
									("yaffs: Allocation block %d was not highest sequence id:" +
											" block seq = %l, dev seq = %l"
											+ TENDSTR), blk,intAsUnsignedInt(bi.sequenceNumber()),dev.sequenceNumber);
						}
					}

					dev.nFreeChunks += (dev.nChunksPerBlock - c);
				} else if (tags.chunkId > 0) {
					/* chunkId > 0 so it is a data chunk... */
					/*unsigned int*/ int endpos;

					yaffs_SetChunkBit(dev, blk, c);
					bi.setPagesInUse(bi.pagesInUse()+1);

					in = yaffs_FindOrCreateObjectByNumber(dev,
							tags.objectId,
							YAFFS_OBJECT_TYPE_FILE);
					/* PutChunkIntoFile checks for a clash (two data chunks with
					 * the same chunkId).
					 */
					yaffs_PutChunkIntoFile(in, tags.chunkId, chunk,
							1);
					endpos =
						(tags.chunkId - 1) * dev.nDataBytesPerChunk +
						tags.byteCount;
					if (in.variantType == YAFFS_OBJECT_TYPE_FILE
							&& in.variant.fileVariant().scannedFileSize <
							endpos) {
						in.variant.fileVariant().
						scannedFileSize = endpos;
						if (!dev.useHeaderFileSize) {
							in.variant.fileVariant().
							fileSize =
								in.variant.fileVariant().
								scannedFileSize;
						}

					}
					/* T((" %d %d data %d %d\n",blk,c,tags.objectId,tags.chunkId));   */
				} else {
					/* chunkId == 0, so it is an ObjectHeader.
					 * Thus, we read in the object header and make the object
					 */
					yaffs_SetChunkBit(dev, blk, c);
					bi.setPagesInUse(bi.pagesInUse()+1);

					result = yaffs_ReadChunkWithTagsFromNAND(dev, chunk,
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

						/*unsigned*/ int existingSerial = byteAsUnsignedByte(in.serial);
						/*unsigned*/ int newSerial = tags.serialNumber;

						if (dev.isYaffs2 ||
								((existingSerial + 1) & 3) ==
									newSerial) {
							/* Use new one - destroy the exisiting one */
							yaffs_DeleteChunk(dev,
									in.chunkId,
									true, __LINE__());
							in.valid = false;
						} else {
							/* Use existing - destroy this one. */
							yaffs_DeleteChunk(dev, chunk, true,
									__LINE__());
						}
					}

					if (!in.valid &&
							(tags.objectId == YAFFS_OBJECTID_ROOT ||
									tags.objectId == YAFFS_OBJECTID_LOSTNFOUND)) {
						/* We only load some info, don't fiddle with directory structure */
						in.valid = true;
						in.variantType = oh.type();

						in.yst_mode = oh.yst_mode();
//						#ifdef CONFIG_YAFFS_WINCE
//						in.win_atime[0] = oh.win_atime[0];
//						in.win_ctime[0] = oh.win_ctime[0];
//						in.win_mtime[0] = oh.win_mtime[0];
//						in.win_atime[1] = oh.win_atime[1];
//						in.win_ctime[1] = oh.win_ctime[1];
//						in.win_mtime[1] = oh.win_mtime[1];
//						#else
						in.yst_uid = oh.yst_uid();
						in.yst_gid = oh.yst_gid();
						in.yst_atime = oh.yst_atime();
						in.yst_mtime = oh.yst_mtime();
						in.yst_ctime = oh.yst_ctime();
						in.yst_rdev = oh.yst_rdev();
//						#endif
						in.chunkId = chunk;

					} else if (!in.valid) {
						/* we need to load this info */

						in.valid = true;
						in.variantType = oh.type();

						in.yst_mode = oh.yst_mode();
//						#ifdef CONFIG_YAFFS_WINCE
//						in.win_atime[0] = oh.win_atime[0];
//						in.win_ctime[0] = oh.win_ctime[0];
//						in.win_mtime[0] = oh.win_mtime[0];
//						in.win_atime[1] = oh.win_atime[1];
//						in.win_ctime[1] = oh.win_ctime[1];
//						in.win_mtime[1] = oh.win_mtime[1];
//						#else
						in.yst_uid = oh.yst_uid();
						in.yst_gid = oh.yst_gid();
						in.yst_atime = oh.yst_atime();
						in.yst_mtime = oh.yst_mtime();
						in.yst_ctime = oh.yst_ctime();
						in.yst_rdev = oh.yst_rdev();
//						#endif
						in.chunkId = chunk;

						yaffs_SetObjectName(in, oh.name(), oh.nameIndex());
						in.dirty = false;

						/* directory stuff...
						 * hook up to parent
						 */

						parent =
							yaffs_FindOrCreateObjectByNumber
							(dev, oh.parentObjectId(),
									YAFFS_OBJECT_TYPE_DIRECTORY);
						if (parent.variantType ==
							YAFFS_OBJECT_TYPE_UNKNOWN) {
							/* Set up as a directory */
							parent.variantType =
								YAFFS_OBJECT_TYPE_DIRECTORY;
							INIT_LIST_HEAD(parent.variant.
									directoryVariant().
									children);
						} else if (parent.variantType !=
							YAFFS_OBJECT_TYPE_DIRECTORY)
						{
							/* Hoosterman, another problem....
							 * We're trying to use a non-directory as a directory
							 */

							T(YAFFS_TRACE_ERROR,
									TSTR
									("yaffs tragedy: attempting to use non-directory as" +
											" a directory in scan. Put in lost+found."
											+ TENDSTR));
							parent = dev.lostNFoundDir;
						}

						yaffs_AddObjectToDirectory(parent, in);

						if (false && (parent == dev.deletedDir ||
								parent == dev.unlinkedDir)) {
							in.deleted = true;	/* If it is unlinked at start up then it wants deleting */
							dev.nDeletedFiles++;
						}
						/* Note re hardlinks.
						 * Since we might scan a hardlink before its equivalent object is scanned
						 * we put them all in a list.
						 * After scanning is complete, we should have all the objects, so we run through this
						 * list and fix up all the chains.              
						 */

						switch (in.variantType) {
							case YAFFS_OBJECT_TYPE_UNKNOWN:	
								/* Todo got a problem */
								break;
							case YAFFS_OBJECT_TYPE_FILE:
								if (dev.isYaffs2
										&& oh.isShrink()) {
									/* Prune back the shrunken chunks */
									yaffs_PruneResizedChunks
									(in, oh.fileSize());
									/* Mark the block as having a shrinkHeader */
									bi.setHasShrinkHeader(true);
								}

								if (dev.useHeaderFileSize)

									in.variant.fileVariant().
									fileSize =
										oh.fileSize();

								break;
							case YAFFS_OBJECT_TYPE_HARDLINK:
								in.variant.hardLinkVariant().
								equivalentObjectId =
									oh.equivalentObjectId();
								in.hardLinks.next =
									/*(list_head)*/ 
									hardList;
								hardList = in;
								break;
							case YAFFS_OBJECT_TYPE_DIRECTORY:
								/* Do nothing */
								break;
							case YAFFS_OBJECT_TYPE_SPECIAL:
								/* Do nothing */
								break;
							case YAFFS_OBJECT_TYPE_SYMLINK:
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
				}
			}

			if (state == YAFFS_BLOCK_STATE_NEEDS_SCANNING) {
				/* If we got this far while scanning, then the block is fully allocated.*/
				state = YAFFS_BLOCK_STATE_FULL;
			}

			bi.setBlockState(state);

			/* Now let's see if it was dirty */
			if (bi.pagesInUse() == 0 &&
					!bi.hasShrinkHeader() &&
					bi.blockState() == YAFFS_BLOCK_STATE_FULL) {
				yaffs_BlockBecameDirty(dev, blk);
			}

		}

		if (blockIndex != null) {
			YFREE(blockIndex);
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

		yaffs_ReleaseTempBuffer(dev, chunkData, __LINE__());

		T(YAFFS_TRACE_SCAN, (TSTR("yaffs_Scan ends" + TENDSTR)));

		return YAFFS_OK;
	}

	static void yaffs_CheckObjectDetailsLoaded(yaffs_Object in)
	{
		/*__u8 **/ byte[] chunkData; final int chunkDataIndex;
		yaffs_ObjectHeader oh;
		yaffs_Device dev = in.myDev;
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		boolean result;

//		#if 0
//		T(YAFFS_TRACE_SCAN,(TSTR("details for object %d %s loaded" + TENDSTR),
//		in.objectId,
//		in.lazyLoaded ? "not yet" : "already"));
//		#endif

		if(in.lazyLoaded){
			in.lazyLoaded = false;
			chunkData = yaffs_GetTempBuffer(dev, __LINE__());
			chunkDataIndex = 0;

			result = yaffs_ReadChunkWithTagsFromNAND(dev,in.chunkId,chunkData,chunkDataIndex,tags);
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

			if(in.variantType == YAFFS_OBJECT_TYPE_SYMLINK)
			{
				in.variant.symLinkVariant().alias =
					yaffs_CloneString(oh.alias(),oh.aliasIndex());
				in.variant.symLinkVariant().aliasIndex = 0;
			}

			yaffs_ReleaseTempBuffer(dev,chunkData, __LINE__());
		}
	}

	static boolean yaffs_ScanBackwards(yaffs_Device dev)
	{
		yaffs_ExtendedTags tags = new yaffs_ExtendedTags();
		int blk;
		int blockIterator;
		int startIterator;
		int endIterator;
		int nBlocksToScan = 0;

		int chunk;
		boolean result;
		int c;
		boolean deleted;
		/*yaffs_BlockState*/ int state;
		yaffs_Object hardList = null;
		yaffs_BlockInfo bi;
		long sequenceNumber;
		yaffs_ObjectHeader oh;
		yaffs_Object in;
		yaffs_Object parent;
		int nBlocks = dev.internalEndBlock - dev.internalStartBlock + 1;
		boolean itsUnlinked;
		/*__u8 **/ byte[] chunkData;
		final int chunkDataIndex;

		int fileSize;
		boolean isShrink;
		boolean foundChunksInBlock;
		int equivalentObjectId;


		yaffs_BlockIndex blockIndex[] = null;
		boolean altBlockIndex = false;

		if (!dev.isYaffs2) {
			T(YAFFS_TRACE_SCAN,
					(TSTR("yaffs_ScanBackwards is only for YAFFS2!" + TENDSTR)));
			return YAFFS_FAIL;
		}

		T(YAFFS_TRACE_SCAN,
				TSTR
				("yaffs_ScanBackwards starts  intstartblk %d intendblk %d..."
						+ TENDSTR), dev.internalStartBlock, dev.internalEndBlock);


		dev.sequenceNumber = YAFFS_LOWEST_SEQUENCE_NUMBER;

		blockIndex = YMALLOC_BLOCKINDEX(nBlocks/* * sizeof(yaffs_BlockIndex)*/);

		if(!(blockIndex != null)) {
			blockIndex = YMALLOC_ALT_BLOCKINDEX(nBlocks/* * sizeof(yaffs_BlockIndex)*/);
			altBlockIndex = true;
		}

		if(!(blockIndex != null)) {
			T(YAFFS_TRACE_SCAN,
					(TSTR("yaffs_Scan() could not allocate block index!" + TENDSTR)));
			return YAFFS_FAIL;
		}

		chunkData = yaffs_GetTempBuffer(dev, __LINE__());
		chunkDataIndex = 0;

		/* Scan all the blocks to determine their state */
		for (blk = dev.internalStartBlock; blk <= dev.internalEndBlock; blk++) {
			bi = yaffs_GetBlockInfo(dev, blk);
			yaffs_ClearChunkBits(dev, blk);
			bi.setPagesInUse(0);
			bi.setSoftDeletions(0);

			IntegerPointer statePointer = new IntegerPointer();
			IntegerPointer sequenceNumberPointer = new IntegerPointer();
			yaffs_QueryInitialBlockState(dev, blk, statePointer, sequenceNumberPointer);
			state = statePointer.dereferenced;
			sequenceNumber = sequenceNumberPointer.dereferenced;

			bi.setBlockState(state);
			bi.setSequenceNumber((int)sequenceNumber);

			if(bi.sequenceNumber() == YAFFS_SEQUENCE_CHECKPOINT_DATA)
				bi.setBlockState(state = YAFFS_BLOCK_STATE_CHECKPOINT);

			T(YAFFS_TRACE_SCAN_DEBUG,
					TSTR("Block scanning block %d state %d seq %d" + TENDSTR), blk,
					state, sequenceNumber);


			if(state == YAFFS_BLOCK_STATE_CHECKPOINT){
				/* todo .. fix free space ? */

			} else if (state == YAFFS_BLOCK_STATE_DEAD) {
				T(YAFFS_TRACE_BAD_BLOCKS,
						TSTR("block %d is bad" + TENDSTR), blk);
			} else if (state == YAFFS_BLOCK_STATE_EMPTY) {
				T(YAFFS_TRACE_SCAN_DEBUG,
						TSTR("Block empty " + TENDSTR));
				dev.nErasedBlocks++;
				dev.nFreeChunks += dev.nChunksPerBlock;
			} else if (state == YAFFS_BLOCK_STATE_NEEDS_SCANNING) {

				/* Determine the highest sequence number */
				if (dev.isYaffs2 &&
						sequenceNumber >= YAFFS_LOWEST_SEQUENCE_NUMBER &&
						sequenceNumber < YAFFS_HIGHEST_SEQUENCE_NUMBER) {

					blockIndex[nBlocksToScan].seq = (int)sequenceNumber;
					blockIndex[nBlocksToScan].block = blk;

					nBlocksToScan++;

					if (sequenceNumber >= dev.sequenceNumber) {
						dev.sequenceNumber = sequenceNumber;
					}
				} else if (dev.isYaffs2) {
					/* TODO: Nasty sequence number! */
					T(YAFFS_TRACE_SCAN,
							TSTR
							("Block scanning block %d has bad sequence number %d"
									+ TENDSTR), blk, sequenceNumber);

				}
			}
		}

		T(YAFFS_TRACE_SCAN,
				TSTR("%d blocks to be sorted..." + TENDSTR), nBlocksToScan);



		YYIELD();

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

		YYIELD();

		T(YAFFS_TRACE_SCAN, TSTR("...done" + TENDSTR));

		/* Now scan the blocks looking at the data. */
		startIterator = 0;
		endIterator = nBlocksToScan - 1;
		T(YAFFS_TRACE_SCAN_DEBUG,
				TSTR("%d blocks to be scanned" + TENDSTR), nBlocksToScan);

		/* For each block.... backwards */
		for (blockIterator = endIterator; blockIterator >= startIterator;
		blockIterator--) {
			/* Cooperative multitasking! This loop can run for so
			   long that watchdog timers expire. */
			YYIELD();

			/* get the block to scan in the correct order */
			blk = blockIndex[blockIterator].block;

			bi = yaffs_GetBlockInfo(dev, blk);
			state = bi.blockState();

			deleted = false;

			/* For each chunk in each block that needs scanning.... */
			foundChunksInBlock = false;
			for (c = dev.nChunksPerBlock - 1; c >= 0 &&
			(state == YAFFS_BLOCK_STATE_NEEDS_SCANNING ||
					state == YAFFS_BLOCK_STATE_ALLOCATING); c--) {
				/* Scan backwards... 
				 * Read the tags and decide what to do
				 */
				chunk = blk * dev.nChunksPerBlock + c;

				result = yaffs_ReadChunkWithTagsFromNAND(dev, chunk, null, 0,
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
						state = YAFFS_BLOCK_STATE_EMPTY;
						dev.nErasedBlocks++;
					} else {
						if (state == YAFFS_BLOCK_STATE_NEEDS_SCANNING ||
								state == YAFFS_BLOCK_STATE_ALLOCATING) {
							if(dev.sequenceNumber == intAsUnsignedInt(bi.sequenceNumber())) {
								/* this is the block being allocated from */

								T(YAFFS_TRACE_SCAN,
										TSTR
										(" Allocating from %d %d"
												+ TENDSTR), blk, c);

								state = YAFFS_BLOCK_STATE_ALLOCATING;
								dev.allocationBlock = blk;
								dev.allocationPage = c;
								dev.allocationBlockFinder = blk;	
							}
							else {
								/* This is a partially written block that is not
								 * the current allocation block. This block must have
								 * had a write failure, so set up for retirement.
								 */

								bi.setNeedsRetiring(true);
								bi.setGcPrioritise(true);

								T(YAFFS_TRACE_ALWAYS,
										TSTR("Partially written block %d being set for retirement" + TENDSTR),
										blk);
							}

						}

					}

					dev.nFreeChunks++;

				} else if (tags.chunkId > 0) {
					/* chunkId > 0 so it is a data chunk... */
					/*unsigned int*/ int endpos;
					/*__u32*/ int chunkBase =
						(tags.chunkId - 1) * dev.nDataBytesPerChunk;

					foundChunksInBlock = true;


					yaffs_SetChunkBit(dev, blk, c);
					bi.setPagesInUse(bi.pagesInUse()+1);

					in = yaffs_FindOrCreateObjectByNumber(dev,
							tags.
							objectId,
							YAFFS_OBJECT_TYPE_FILE);
					if (in.variantType == YAFFS_OBJECT_TYPE_FILE
							&& chunkBase <
							intAsUnsignedInt(in.variant.fileVariant().shrinkSize)) {
						/* This has not been invalidated by a resize */
						yaffs_PutChunkIntoFile(in, tags.chunkId,
								chunk, -1);

						/* File size is calculated by looking at the data chunks if we have not 
						 * seen an object header yet. Stop this practice once we find an object header.
						 */
						endpos =
							(tags.chunkId -
									1) * dev.nDataBytesPerChunk +
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
						yaffs_DeleteChunk(dev, chunk, true, __LINE__());

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
									(tags.objectId == YAFFS_OBJECTID_ROOT ||
											tags.objectId == YAFFS_OBJECTID_LOSTNFOUND))
					) {

						/* If we don't have  valid info then we need to read the chunk
						 * TODO In future we can probably defer reading the chunk and 
						 * living with invalid data until needed.
						 */

						result = yaffs_ReadChunkWithTagsFromNAND(dev,
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
						T(YAFFS_TRACE_ERROR,
								TSTR
								("yaffs tragedy: Could not make object for object  %d  " +
										"at chunk %d during scan"
										+ TENDSTR), tags.objectId, chunk);

					}

					if (in.valid) {
						/* We have already filled this one.
						 * We have a duplicate that will be discarded, but 
						 * we first have to suck out resize info if it is a file.
						 */

						if ((in.variantType == YAFFS_OBJECT_TYPE_FILE) && 
								((oh != null && 
										oh.type() == YAFFS_OBJECT_TYPE_FILE)||
										(tags.extraHeaderInfoAvailable  &&
												tags.extraObjectType == YAFFS_OBJECT_TYPE_FILE))
						) {
							/*__u32*/ int thisSize =
								(oh != null) ? oh.fileSize() : tags.
										extraFileLength;
								/*__u32*/ int parentObjectId =
									(oh != null) ? oh.
											parentObjectId() : tags.
											extraParentObjectId;
//											XXX allowed in C?
											// PORT changed the name of the inner variable 
											/*unsigned*/ boolean isShrinkInner =	
												(oh != null) ? oh.isShrink() : tags.
														extraIsShrinkHeader;

												/* If it is deleted (unlinked at start also means deleted)
												 * we treat the file size as being zeroed at this point.
												 */
												if (parentObjectId ==
													YAFFS_OBJECTID_DELETED
													|| parentObjectId ==
														YAFFS_OBJECTID_UNLINKED) {
													thisSize = 0;
													isShrinkInner = true;
												}

												if (isShrinkInner &&
														intAsUnsignedInt(in.variant.fileVariant().
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
						yaffs_DeleteChunk(dev, chunk, true, __LINE__());

					}

					if (!in.valid &&
							(tags.objectId == YAFFS_OBJECTID_ROOT ||
									tags.objectId ==
										YAFFS_OBJECTID_LOSTNFOUND)) {
						/* We only load some info, don't fiddle with directory structure */
						in.valid = true;

						if(oh != null) {
							in.variantType = oh.type();

							in.yst_mode = oh.yst_mode();
//							#ifdef CONFIG_YAFFS_WINCE
//							in.win_atime[0] = oh.win_atime[0];
//							in.win_ctime[0] = oh.win_ctime[0];
//							in.win_mtime[0] = oh.win_mtime[0];
//							in.win_atime[1] = oh.win_atime[1];
//							in.win_ctime[1] = oh.win_ctime[1];
//							in.win_mtime[1] = oh.win_mtime[1];
//							#else
							in.yst_uid = oh.yst_uid();
							in.yst_gid = oh.yst_gid();
							in.yst_atime = oh.yst_atime();
							in.yst_mtime = oh.yst_mtime();
							in.yst_ctime = oh.yst_ctime();
							in.yst_rdev = oh.yst_rdev();

//							#endif
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
//							#ifdef CONFIG_YAFFS_WINCE
//							in.win_atime[0] = oh.win_atime[0];
//							in.win_ctime[0] = oh.win_ctime[0];
//							in.win_mtime[0] = oh.win_mtime[0];
//							in.win_atime[1] = oh.win_atime[1];
//							in.win_ctime[1] = oh.win_ctime[1];
//							in.win_mtime[1] = oh.win_mtime[1];
//							#else
							in.yst_uid = oh.yst_uid();
							in.yst_gid = oh.yst_gid();
							in.yst_atime = oh.yst_atime();
							in.yst_mtime = oh.yst_mtime();
							in.yst_ctime = oh.yst_ctime();
							in.yst_rdev = oh.yst_rdev();
//							#endif

							if (oh.shadowsObject() > 0) 
								yaffs_HandleShadowedObject(dev,
										oh.
										shadowsObject(),
										true);


							yaffs_SetObjectName(in, oh.name(), oh.nameIndex());
							parent =
								yaffs_FindOrCreateObjectByNumber
								(dev, oh.parentObjectId(),
										YAFFS_OBJECT_TYPE_DIRECTORY);

							fileSize = oh.fileSize();
							isShrink = oh.isShrink();
							equivalentObjectId = oh.equivalentObjectId();

						}
						else {
							in.variantType = tags.extraObjectType;
							parent =
								yaffs_FindOrCreateObjectByNumber
								(dev, tags.extraParentObjectId,
										YAFFS_OBJECT_TYPE_DIRECTORY);
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
							YAFFS_OBJECT_TYPE_UNKNOWN) {
							/* Set up as a directory */
							parent.variantType =
								YAFFS_OBJECT_TYPE_DIRECTORY;
							INIT_LIST_HEAD(parent.variant.
									directoryVariant().
									children);
						} else if (parent.variantType !=
							YAFFS_OBJECT_TYPE_DIRECTORY)
						{
							/* Hoosterman, another problem....
							 * We're trying to use a non-directory as a directory
							 */

							T(YAFFS_TRACE_ERROR,
									TSTR
									("yaffs tragedy: attempting to use non-directory as" +
											" a directory in scan. Put in lost+found."
											+ TENDSTR));
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
							case YAFFS_OBJECT_TYPE_UNKNOWN:	
								/* Todo got a problem */
								break;
							case YAFFS_OBJECT_TYPE_FILE:

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
										intAsUnsignedInt(in.variant.fileVariant().shrinkSize) > fileSize) {
									in.variant.fileVariant().shrinkSize = fileSize;
								}

								break;
							case YAFFS_OBJECT_TYPE_HARDLINK:
								if(!itsUnlinked) {
									in.variant.hardLinkVariant().equivalentObjectId =
										equivalentObjectId;
									in.hardLinks.next =
										/*(list_head)*/ hardList;
									hardList = in;
								}
								break;
							case YAFFS_OBJECT_TYPE_DIRECTORY:
								/* Do nothing */
								break;
							case YAFFS_OBJECT_TYPE_SPECIAL:
								/* Do nothing */
								break;
							case YAFFS_OBJECT_TYPE_SYMLINK:
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
			}

			if (state == YAFFS_BLOCK_STATE_NEEDS_SCANNING) {
				/* If we got this far while scanning, then the block is fully allocated. */
				state = YAFFS_BLOCK_STATE_FULL;
			}

			bi.setBlockState(state);

			/* Now let's see if it was dirty */
			if (bi.pagesInUse() == 0 &&
					!bi.hasShrinkHeader() &&
					bi.blockState() == YAFFS_BLOCK_STATE_FULL) {
				yaffs_BlockBecameDirty(dev, blk);
			}

		}

		if (altBlockIndex) 
			YFREE_ALT(blockIndex);
		else
			YFREE(blockIndex);

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

		yaffs_ReleaseTempBuffer(dev, chunkData, __LINE__());

		T(YAFFS_TRACE_SCAN, (TSTR("yaffs_ScanBackwards ends" + TENDSTR)));

		return YAFFS_OK;
	}

	/*------------------------------  Directory Functions ----------------------------- */

	static void yaffs_RemoveObjectFromDirectory(yaffs_Object  obj)
	{
		yaffs_Device dev = obj.myDev;

		if(dev != null && dev.removeObjectCallback != null)
			dev.removeObjectCallback.yaffsfs_RemoveObjectCallback(obj);

		list_del_init(obj.siblings);
		obj.parent = null;
	}


	static void yaffs_AddObjectToDirectory(yaffs_Object  directory,
			yaffs_Object  obj)
	{

		if (!(directory != null)) {
			T(YAFFS_TRACE_ALWAYS,
					TSTR
					("tragedy: Trying to add an object to a null pointer directory"
							+ TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		if (directory.variantType != YAFFS_OBJECT_TYPE_DIRECTORY) {
			T(YAFFS_TRACE_ALWAYS,
					TSTR
					("tragedy: Trying to add an object to a non-directory"
							+ TENDSTR));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}

		if (obj.siblings.prev == null) {
			/* Not initialised */
			INIT_LIST_HEAD(obj.siblings);

		} else if (!list_empty(obj.siblings)) {
			/* If it is holed up somewhere else, un hook it */
			yaffs_RemoveObjectFromDirectory(obj);
		}
		/* Now add it */
		list_add(obj.siblings, directory.variant.directoryVariant().children);
		obj.parent = directory;

		if (directory == obj.myDev.unlinkedDir
				|| directory == obj.myDev.deletedDir) {
			obj.unlinked = true;
			obj.myDev.nUnlinkedFiles++;
			obj.renameAllowed = false;
		}
	}

	static yaffs_Object yaffs_FindObjectByName(yaffs_Object  directory,
			/*const YCHAR **/ byte[] name, int nameIndex)
	{
		short sum;

		list_head i;
		/*YCHAR buffer[YAFFS_MAX_NAME_LENGTH + 1]*/ byte[] buffer = new byte[YAFFS_MAX_NAME_LENGTH + 1];
		final int bufferIndex = 0;

		yaffs_Object l;

		if (!(name != null)) {
			return null;
		}

		if (!(directory != null)) {
			T(YAFFS_TRACE_ALWAYS,
					(TSTR
							("tragedy: yaffs_FindObjectByName: null pointer directory"
									+ TENDSTR)));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		if (directory.variantType != YAFFS_OBJECT_TYPE_DIRECTORY) {
			T(YAFFS_TRACE_ALWAYS,
					(TSTR
							("tragedy: yaffs_FindObjectByName: non-directory" + TENDSTR)));
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
				if (l.objectId == YAFFS_OBJECTID_LOSTNFOUND) {
					if (yaffs_strcmp(name, nameIndex, YAFFS_LOSTNFOUND_NAME, 0) == 0) {
						return l;
					}
				} else if (yaffs_SumCompare(l.sum, sum) || l.chunkId <= 0)	
				{
					/* LostnFound cunk called Objxxx
					 * Do a real check
					 */
					yaffs_GetObjectName(l, buffer, bufferIndex,
							YAFFS_MAX_NAME_LENGTH);
					if (yaffs_strcmp(name, nameIndex, buffer, bufferIndex) == 0) {
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
//	T(YAFFS_TRACE_ALWAYS,
//	(TSTR
//	("tragedy: yaffs_FindObjectByName: null pointer directory"
//	+ TENDSTR)));
//	YBUG();
//	}
//	if (theDir.variantType != YAFFS_OBJECT_TYPE_DIRECTORY) {
//	T(YAFFS_TRACE_ALWAYS,
//	(TSTR
//	("tragedy: yaffs_FindObjectByName: non-directory" + TENDSTR)));
//	YBUG();
//	}

//	list_for_each(i, &theDir.variant.directoryVariant.children) {
//	if (i) {
//	l = list_entry(i, yaffs_Object, siblings);
//	if (l && !fn(l)) {
//	return YAFFS_FAIL;
//	}
//	}
//	}

//	return YAFFS_OK;

//	}
//	#endif

	/* GetEquivalentObject dereferences any hard links to get to the
	 * actual object.
	 */

	static yaffs_Object yaffs_GetEquivalentObject(yaffs_Object  obj)
	{
		if (obj != null && obj.variantType == YAFFS_OBJECT_TYPE_HARDLINK) {
			/* We want the object id of the equivalent object, not this one */
			obj = obj.variant.hardLinkVariant().equivalentObject;
		}
		return obj;

	}

	static int yaffs_GetObjectName(yaffs_Object  obj, /*YCHAR **/ byte[] name, 
			int nameIndex,int buffSize)
	{
		memset(name, nameIndex, (byte)0, buffSize/* * sizeof(YCHAR)*/ );

		yaffs_CheckObjectDetailsLoaded(obj);

		if (obj.objectId == YAFFS_OBJECTID_LOSTNFOUND) {
			yaffs_strncpy(name, nameIndex, YAFFS_LOSTNFOUND_NAME, 0, buffSize - 1);
		} else if (obj.chunkId <= 0) {
			/*YCHAR locName[20]*/ byte[] locName = new byte[20];
			final int locNameIndex = 0;
			/* make up a name */
			yaffs_sprintf(locName, locNameIndex,
					"%a%d", YAFFS_LOSTNFOUND_PREFIX, 0,
					obj.objectId);
			yaffs_strncpy(name, nameIndex, locName, locNameIndex, buffSize - 1);

		}
//		#ifdef CONFIG_YAFFS_SHORT_NAMES_IN_RAM
		else if (obj.shortName[obj.shortNameIndex] != 0) {
			yaffs_strcpy(name, nameIndex, obj.shortName, obj.shortNameIndex);
		}
//		#endif
		else {
			boolean result;
			/*__u8 **/ byte[] buffer = yaffs_GetTempBuffer(obj.myDev, __LINE__());
			final int bufferIndex = 0;

			yaffs_ObjectHeader oh = /*(yaffs_ObjectHeader) buffer*/ 
				new yaffs_ObjectHeader(buffer, bufferIndex);

			memset(buffer, bufferIndex, (byte)0, obj.myDev.nDataBytesPerChunk);

			if (obj.chunkId >= 0) {
				result = yaffs_ReadChunkWithTagsFromNAND(obj.myDev,
						obj.chunkId, buffer, bufferIndex,
						null);
			}
			yaffs_strncpy(name, nameIndex, oh.name(), oh.nameIndex(), buffSize - 1);

			yaffs_ReleaseTempBuffer(obj.myDev, buffer, __LINE__());
		}

		return yaffs_strlen(name, nameIndex);
	}

	static int yaffs_GetObjectFileLength(yaffs_Object  obj)
	{

		/* Dereference any hard linking */
		obj = yaffs_GetEquivalentObject(obj);

		if (obj.variantType == YAFFS_OBJECT_TYPE_FILE) {
			return obj.variant.fileVariant().fileSize;
		}
		if (obj.variantType == YAFFS_OBJECT_TYPE_SYMLINK) {
			return yaffs_strlen(obj.variant.symLinkVariant().alias,
					obj.variant.symLinkVariant().aliasIndex);
		} else {
			/* Only a directory should drop through to here */
			return obj.myDev.nDataBytesPerChunk;
		}
	}

	static int yaffs_GetObjectLinkCount(yaffs_Object  obj)
	{
		int count = 0;
		list_head i;

		if (!obj.unlinked) {
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
			case YAFFS_OBJECT_TYPE_FILE:
				return DT_REG;
//				break;
			case YAFFS_OBJECT_TYPE_DIRECTORY:
				return DT_DIR;
//				break;
			case YAFFS_OBJECT_TYPE_SYMLINK:
				return DT_LNK;
//				break;
			case YAFFS_OBJECT_TYPE_HARDLINK:
				return DT_REG;
//				break;
			case YAFFS_OBJECT_TYPE_SPECIAL:
				if (S_ISFIFO(obj.yst_mode))
					return DT_FIFO;
				if (S_ISCHR(obj.yst_mode))
					return DT_CHR;
				if (S_ISBLK(obj.yst_mode))
					return DT_BLK;
				if (S_ISSOCK(obj.yst_mode))
					return DT_SOCK;
			default:
				return DT_REG;
//			break;
		}
	}

	static /*YCHAR **/ byte[] yaffs_GetSymlinkAlias(yaffs_Object  obj)
	{
		obj = yaffs_GetEquivalentObject(obj);
		if (obj.variantType == YAFFS_OBJECT_TYPE_SYMLINK) {
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

		if ((valid & ATTR_MODE) != 0)
			obj.yst_mode = attr.ia_mode;
		if ((valid & ATTR_UID) != 0)
			obj.yst_uid = attr.ia_uid;
		if ((valid & ATTR_GID) != 0)
			obj.yst_gid = attr.ia_gid;

		if ((valid & ATTR_ATIME) != 0)
			obj.yst_atime = Y_TIME_CONVERT(attr.ia_atime);
		if ((valid & ATTR_CTIME) != 0)
			obj.yst_ctime = Y_TIME_CONVERT(attr.ia_ctime);
		if ((valid & ATTR_MTIME) != 0)
			obj.yst_mtime = Y_TIME_CONVERT(attr.ia_mtime);

		if ((valid & ATTR_SIZE) != 0)
			yaffs_ResizeFile(obj, attr.ia_size);

		yaffs_UpdateObjectHeader(obj, null, 0, true, false, 0);

		return YAFFS_OK;

	}
	static boolean yaffs_GetAttributes(yaffs_Object  obj, iattr attr)
	{
		/*unsigned int*/ int valid = 0;

		attr.ia_mode = obj.yst_mode;
		valid |= ATTR_MODE;
		attr.ia_uid = obj.yst_uid;
		valid |= ATTR_UID;
		attr.ia_gid = obj.yst_gid;
		valid |= ATTR_GID;

		// XXX either remove all Y_TIME_CONVERT(), or...
		/*Y_TIME_CONVERT(*/ attr.ia_atime/*)*/ = obj.yst_atime;
		valid |= ATTR_ATIME;
		/*Y_TIME_CONVERT(*/ attr.ia_ctime/*)*/ = obj.yst_ctime;
		valid |= ATTR_CTIME;
		/*Y_TIME_CONVERT(*/ attr.ia_mtime/*)*/ = obj.yst_mtime;
		valid |= ATTR_MTIME;

		attr.ia_size = yaffs_GetFileSize(obj);
		valid |= ATTR_SIZE;

		attr.ia_valid = valid;

		return YAFFS_OK;

	}

//	#endif

//	#if 0
//	int yaffs_DumpObject(yaffs_Object  obj)
//	{
//	YCHAR name[257];

//	yaffs_GetObjectName(obj, name, 256);

//	T(YAFFS_TRACE_ALWAYS,
//	(TSTR
//	("Object %d, inode %d \"%s\"\n dirty %d valid %d serial %d sum %d"
//	" chunk %d type %d size %d\n"
//	+ TENDSTR), obj.objectId, yaffs_GetObjectInode(obj), name,
//	obj.dirty, obj.valid, obj.serial, obj.sum, obj.chunkId,
//	yaffs_GetObjectType(obj), yaffs_GetObjectFileLength(obj)));

//	return YAFFS_OK;
//	}
//	#endif

	/*---------------------------- Initialisation code -------------------------------------- */

	static boolean yaffs_CheckDevFunctions(yaffs_Device dev)
	{

		/* Common functions, gotta have */
		if (!(dev.eraseBlockInNAND != null) || !(dev.initialiseNAND != null))
			return false;

//		#ifdef CONFIG_YAFFS_YAFFS2

		/* Can use the "with tags" style interface for yaffs1 or yaffs2 */
		if (dev.writeChunkWithTagsToNAND != null &&
				dev.readChunkWithTagsFromNAND != null &&
				!(dev.writeChunkToNAND != null) &&
				!(dev.readChunkFromNAND != null) &&
				dev.markNANDBlockBad != null && dev.queryNANDBlock != null)
			return true;
//		#endif

		/* Can use the "spare" style interface for yaffs1 */
		if (!dev.isYaffs2 &&
				!(dev.writeChunkWithTagsToNAND != null) &&
				!(dev.readChunkWithTagsFromNAND != null) &&
				dev.writeChunkToNAND != null &&
				dev.readChunkFromNAND != null &&
				!(dev.markNANDBlockBad != null) && !(dev.queryNANDBlock != null))
			return true;

		return false;		/* bad */
	}


	static void yaffs_CreateInitialDirectories(yaffs_Device dev)
	{
		/* Initialise the unlinked, deleted, root and lost and found directories */

		dev.lostNFoundDir = dev.rootDir =  null;
		dev.unlinkedDir = dev.deletedDir = null;

		dev.unlinkedDir =
			yaffs_CreateFakeDirectory(dev, YAFFS_OBJECTID_UNLINKED, S_IFDIR);
		dev.deletedDir =
			yaffs_CreateFakeDirectory(dev, YAFFS_OBJECTID_DELETED, S_IFDIR);

		dev.rootDir =
			yaffs_CreateFakeDirectory(dev, YAFFS_OBJECTID_ROOT,
					YAFFS_ROOT_MODE | S_IFDIR);
		dev.lostNFoundDir =
			yaffs_CreateFakeDirectory(dev, YAFFS_OBJECTID_LOSTNFOUND,
					YAFFS_LOSTNFOUND_MODE | S_IFDIR);
		yaffs_AddObjectToDirectory(dev.rootDir, dev.lostNFoundDir);
	}

	static boolean yaffs_GutsInitialise(yaffs_Device dev)
	{
		/*unsigned*/ int x;
		int bits;

		T(YAFFS_TRACE_TRACING, (TSTR("yaffs: yaffs_GutsInitialise()" + TENDSTR)));

		/* Check stuff that must be set */

		if (!(dev != null)) {
			T(YAFFS_TRACE_ALWAYS, (TSTR("yaffs: Need a device" + TENDSTR)));
			return YAFFS_FAIL;
		}

		dev.internalStartBlock = dev.startBlock;
		dev.internalEndBlock = dev.endBlock;
		dev.blockOffset = 0;
		dev.chunkOffset = 0;
		dev.nFreeChunks = 0;

		if (dev.startBlock == 0) {
			dev.internalStartBlock = dev.startBlock + 1;
			dev.internalEndBlock = dev.endBlock + 1;
			dev.blockOffset = 1;
			dev.chunkOffset = dev.nChunksPerBlock;
		}

		/* Check geometry parameters. */

		if ((dev.isYaffs2 && dev.nDataBytesPerChunk < 1024) || 
				(!dev.isYaffs2 && dev.nDataBytesPerChunk != 512) || 
				dev.nChunksPerBlock < 2 || 
				dev.nReservedBlocks < 2 || 
				dev.internalStartBlock <= 0 || 
				dev.internalEndBlock <= 0 || 
				dev.internalEndBlock <= (dev.internalStartBlock + dev.nReservedBlocks + 2)	// otherwise it is too small
		) {
			T(YAFFS_TRACE_ALWAYS,
					TSTR
					("yaffs: NAND geometry problems: chunk size %d, type is yaffs%s "
							+ TENDSTR), dev.nDataBytesPerChunk, dev.isYaffs2 ? "2" : "");
			return YAFFS_FAIL;
		}

		if (yaffs_InitialiseNAND(dev) != YAFFS_OK) {
			T(YAFFS_TRACE_ALWAYS,
					(TSTR("yaffs: InitialiseNAND failed" + TENDSTR)));
			return YAFFS_FAIL;
		}

		/* Got the right mix of functions? */
		if (!yaffs_CheckDevFunctions(dev)) {
			/* Function missing */
			T(YAFFS_TRACE_ALWAYS,
					(TSTR
							("yaffs: device function(s) missing or wrong\n" + TENDSTR)));

			return YAFFS_FAIL;
		}

		/* This is really a compilation check. */
		if (!yaffs_CheckStructures()) {
			T(YAFFS_TRACE_ALWAYS,
					(TSTR("yaffs_CheckStructures failed\n" + TENDSTR)));
			return YAFFS_FAIL;
		}

		if (dev.isMounted) {
			T(YAFFS_TRACE_ALWAYS,
					(TSTR("yaffs: device already mounted\n" + TENDSTR)));
			return YAFFS_FAIL;
		}

		/* Finished with most checks. One or two more checks happen later on too. */

		dev.isMounted = true;



		/* OK now calculate a few things for the device */

		/*
		 *  Calculate all the chunk size manipulation numbers: 
		 */
		/* Start off assuming it is a power of 2 */
		dev.chunkShift = ShiftDiv(dev.nDataBytesPerChunk);
		dev.chunkMask = (1<<dev.chunkShift) - 1;

		if(dev.nDataBytesPerChunk == (dev.chunkMask + 1)){
			/* Yes it is a power of 2, disable crumbs */
			dev.crumbMask = 0;
			dev.crumbShift = 0;
			dev.crumbsPerChunk = 0;
		} else {
			/* Not a power of 2, use crumbs instead */
			dev.crumbShift = ShiftDiv(yaffs_PackedTags2TagsPart.SERIALIZED_LENGTH);
			dev.crumbMask = (1<<dev.crumbShift)-1;
			dev.crumbsPerChunk = dev.nDataBytesPerChunk/(1 << dev.crumbShift);
			dev.chunkShift = 0;
			dev.chunkMask = 0;
		}


		/*
		 * Calculate chunkGroupBits.
		 * We need to find the next power of 2 > than internalEndBlock
		 */

		x = dev.nChunksPerBlock * (dev.internalEndBlock + 1);	// XXX understand, I'm too tired now

		bits = ShiftsGE(x);

		/* Set up tnode width if wide tnodes are enabled. */
		if(!dev.wideTnodesDisabled){
			/* bits must be even so that we end up with 32-bit words */
			if((bits & 1) != 0)
				bits++;
			if(bits < 16)
				dev.tnodeWidth = 16;
			else
				dev.tnodeWidth = bits;
		}
		else
			dev.tnodeWidth = 16;

		dev.tnodeMask = (1<<dev.tnodeWidth)-1;

		/* Level0 Tnodes are 16 bits or wider (if wide tnodes are enabled),
		 * so if the bitwidth of the
		 * chunk range we're using is greater than 16 we need
		 * to figure out chunk shift and chunkGroupSize
		 */

		if (bits <= dev.tnodeWidth)
			dev.chunkGroupBits = 0;
		else
			dev.chunkGroupBits = bits - dev.tnodeWidth;


		dev.chunkGroupSize = 1 << dev.chunkGroupBits;

		if (dev.nChunksPerBlock < dev.chunkGroupSize) {
			/* We have a problem because the soft delete won't work if
			 * the chunk group size > chunks per block.
			 * This can be remedied by using larger "virtual blocks".
			 */
			T(YAFFS_TRACE_ALWAYS,
					(TSTR("yaffs: chunk group too large\n" + TENDSTR)));

			return YAFFS_FAIL;
		}

		/* OK, we've finished verifying the device, lets continue with initialisation */

		/* More device initialisation */
		dev.garbageCollections = 0;
		dev.passiveGarbageCollections = 0;
		dev.currentDirtyChecker = 0;
		dev.bufferedBlock = -1;
		dev.doingBufferedBlockRewrite = 0;
		dev.nDeletedFiles = 0;
		dev.nBackgroundDeletions = 0;
		dev.nUnlinkedFiles = 0;
		dev.eccFixed = 0;
		dev.eccUnfixed = 0;
		dev.tagsEccFixed = 0;
		dev.tagsEccUnfixed = 0;
		dev.nErasureFailures = 0;
		dev.nErasedBlocks = 0;
		dev.isDoingGC = false;
		dev.hasPendingPrioritisedGCs = true; /* Assume the worst for now, will get fixed on first GC */

		/* Initialise temporary buffers and caches. */
		{
			int i;
			for (i = 0; i < YAFFS_N_TEMP_BUFFERS; i++) {
				dev.tempBuffer[i].line = 0;	/* not in use */
				dev.tempBuffer[i].buffer =
					YMALLOC_DMA(dev.nDataBytesPerChunk);
			}
		}

		if (dev.nShortOpCaches > 0) {
			int i;

			if (dev.nShortOpCaches > YAFFS_MAX_SHORT_OP_CACHES) {
				dev.nShortOpCaches = YAFFS_MAX_SHORT_OP_CACHES;
			}

			dev.srCache =
				YMALLOC_CHUNKCACHE(dev.nShortOpCaches/* * sizeof(yaffs_ChunkCache)*/);

			for (i = 0; i < dev.nShortOpCaches; i++) {
				dev.srCache[i].object = null;
				dev.srCache[i].lastUse = 0;
				dev.srCache[i].dirty = false;
				dev.srCache[i].data = YMALLOC_DMA(dev.nDataBytesPerChunk);
				dev.srCache[i].dataIndex = 0;
			}
			dev.srLastUse = 0;
		}

		dev.cacheHits = 0;

		dev.gcCleanupList = YMALLOC_INT(dev.nChunksPerBlock /*sizeof(__u32)*/);

		if (dev.isYaffs2) {
			dev.useHeaderFileSize = true;
		}

		yaffs_InitialiseBlocks(dev);
		yaffs_InitialiseTnodes(dev);
		yaffs_InitialiseObjects(dev);

		yaffs_CreateInitialDirectories(dev);


		/* Now scan the flash. */
		if (dev.isYaffs2) {
			if(yaffs_CheckpointRestore(dev)) {
				T(YAFFS_TRACE_ALWAYS,
						(TSTR("yaffs: restored from checkpoint" + TENDSTR)));
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
		dev.nPageReads = 0;
		dev.nPageWrites = 0;
		dev.nBlockErasures = 0;
		dev.nGCCopies = 0;
		dev.nRetriedWrites = 0;

		dev.nRetiredBlocks = 0;

		yaffs_VerifyFreeChunks(dev);

		T(YAFFS_TRACE_TRACING,
				(TSTR("yaffs: yaffs_GutsInitialise() done.\n" + TENDSTR)));
		return YAFFS_OK;

	}

	static void yaffs_Deinitialise(yaffs_Device dev)
	{
		if (dev.isMounted) {
			int i;

			yaffs_DeinitialiseBlocks(dev);
			yaffs_DeinitialiseTnodes(dev);
			yaffs_DeinitialiseObjects(dev);
			if (dev.nShortOpCaches > 0) {

				for (i = 0; i < dev.nShortOpCaches; i++) {
					YFREE(dev.srCache[i].data);
				}

				YFREE(dev.srCache);
			}

			YFREE(dev.gcCleanupList);

			for (i = 0; i < YAFFS_N_TEMP_BUFFERS; i++) {
				YFREE(dev.tempBuffer[i].buffer);
			}

			dev.isMounted = false;
		}

	}

	static int yaffs_CountFreeChunks(yaffs_Device dev)
	{
		int nFree;
		int b;

		yaffs_BlockInfo blk;

		for (nFree = 0, b = dev.internalStartBlock; b <= dev.internalEndBlock;
		b++) {
			blk = yaffs_GetBlockInfo(dev, b);

			switch (blk.blockState()) {
				case YAFFS_BLOCK_STATE_EMPTY:
				case YAFFS_BLOCK_STATE_ALLOCATING:
				case YAFFS_BLOCK_STATE_COLLECTING:
				case YAFFS_BLOCK_STATE_FULL:
					nFree +=
						(dev.nChunksPerBlock - blk.pagesInUse() +
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
		nFree = dev.nFreeChunks;
//		#else
//		nFree = yaffs_CountFreeChunks(dev);
//		#endif

		nFree += dev.nDeletedFiles;

		/* Now count the number of dirty chunks in the cache and subtract those */

		{
			int i;
			for (nDirtyCacheChunks = 0, i = 0; i < dev.nShortOpCaches; i++) {
				if (dev.srCache[i].dirty)
					nDirtyCacheChunks++;
			}
		}

		nFree -= nDirtyCacheChunks;

		nFree -= ((dev.nReservedBlocks + 1) * dev.nChunksPerBlock);

		/* Now we figure out how much to reserve for the checkpoint and report that... */
		blocksForCheckpoint = dev.nCheckpointReservedBlocks - dev.blocksInCheckpoint;
		if(blocksForCheckpoint < 0)
			blocksForCheckpoint = 0;

		nFree -= (blocksForCheckpoint * dev.nChunksPerBlock);

		if (nFree < 0)
			nFree = 0;

		return nFree;

	}

	static int yaffs_freeVerificationFailures;

	static void yaffs_VerifyFreeChunks(yaffs_Device dev)
	{
		int counted = yaffs_CountFreeChunks(dev);

		int difference = dev.nFreeChunks - counted;

		if (difference != 0) {
			T(YAFFS_TRACE_ALWAYS,
					TSTR("Freechunks verification failure %d %d %d" + TENDSTR),
					dev.nFreeChunks, counted, difference);
			yaffs_freeVerificationFailures++;
		}
	}

	/*---------------------------------------- YAFFS test code ----------------------*/

//	#define yaffs_CheckStruct(structure,syze, name) \
//	if(sizeof(structure) != syze) \
//	{ \
//	T(YAFFS_TRACE_ALWAYS,(TSTR("%s should be %d but is %d\n" + TENDSTR),\
//	name,syze,sizeof(structure))); \
//	return YAFFS_FAIL; \
//	}

	static boolean yaffs_CheckStruct(int structureSize, int syze, String name)
	{
		if(/*sizeof(structure)*/ structureSize != syze)
		{ 
			T(YAFFS_TRACE_ALWAYS,TSTR("%s should be %d but is %d\n" + TENDSTR),
					name,syze,/*sizeof(structure)*/ structureSize); 
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
		(yaffs_CheckStruct(yaffs_Tnode.SERIALIZED_LENGTH, 2 * YAFFS_NTNODES_LEVEL0, "yaffs_Tnode") &&
//				#endif
				yaffs_CheckStruct(yaffs_ObjectHeader.SERIALIZED_LENGTH, 512, "yaffs_ObjectHeader"));
	}
}
