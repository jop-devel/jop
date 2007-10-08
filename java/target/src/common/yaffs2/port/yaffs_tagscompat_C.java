package yaffs2.port;

import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class yaffs_tagscompat_C {
	
//	static {
//		System.out.println("clinit tasgcomp_C");
//	}
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
//
//	#include "yaffs_guts.h"
//	#include "yaffs_tagscompat.h"
//	#include "yaffs_ecc.h"
//
//	static void yaffs_HandleReadDataError(yaffs_Device * dev, int chunkInNAND);
//	#ifdef NOTYET
//	static void yaffs_CheckWrittenBlock(yaffs_Device * dev, int chunkInNAND);
//	static void yaffs_HandleWriteChunkOk(yaffs_Device * dev, int chunkInNAND,
//					     const __u8 * data,
//					     const yaffs_Spare * spare);
//	static void yaffs_HandleUpdateChunk(yaffs_Device * dev, int chunkInNAND,
//					    const yaffs_Spare * spare);
//	static void yaffs_HandleWriteChunkError(yaffs_Device * dev, int chunkInNAND);
//	#endif

	static int yaffs_CountBits(/*__u8*/ byte x)
	{
		int retVal;
		retVal = yaffs_tagscompat_C_bitsTable.yaffs_countBitsTable[x & 0xff];
		return retVal;
	}

	/********** Tags ECC calculations  *********/

	static void yaffs_CalcECC(/*const __u8*/byte[] data, int dataIndex, yaffs_Spare spare)
	{
		ECC_C.yaffs_ECCCalculate(data, dataIndex, spare.ecc1(), spare.ecc1Index());
		ECC_C.yaffs_ECCCalculate(data, dataIndex + 256, spare.ecc2(), spare.ecc2Index());
	}

	static void yaffs_CalcTagsECC(yaffs_Tags tags)
	{
		/* Calculate an ecc */

		
		byte[] b = tags.serialized; int bIndex = tags.offset;
		int i, j;
		/*unsigned*/ int ecc = 0;
		/*unsigned*/ int bit = 0;

		/*tags.ecc = 0;*/ tags.setEcc(0);

		for (i = 0; i < 8; i++) {
			for (j = 1; (j & 0xff) != 0; j <<= 1) {
				bit++;
				if ((b[bIndex+i] & j) != 0) {
					ecc ^= bit;
				}
			}
		}

		tags.setEcc(ecc);

	}

	static int yaffs_CheckECCOnTags(yaffs_Tags tags)
	{
		/*unsigned char*/int ecc = tags.getEcc();

		yaffs_CalcTagsECC(tags);

		ecc ^= tags.getEcc();

		if ((ecc != 0) && (Utils.intAsUnsignedInt(ecc) <= 64)) {
			/* TODO: Handle the failure better. Retire? */
			byte[] b = tags.serialized; int bIndex = tags.offset;

			ecc--;

			b[bIndex + (/*ecc / 8*/ ecc >>> 3)] ^= (1 << (ecc & 7));

			/* Now recvalc the ecc */
			yaffs_CalcTagsECC(tags);

			return 1;	/* recovered error */
		} else if (ecc != 0) {
			/* Wierd ecc failure value */
			/* TODO Need to do somethiong here */
			return -1;	/* unrecovered error */
		}

		return 0;
	}

	/********** Tags **********/

	static void yaffs_LoadTagsIntoSpare(yaffs_Spare sparePtr,
					    yaffs_Tags tagsPtr)
	{
		yaffs_Tags tu = tagsPtr;

		yaffs_CalcTagsECC(tagsPtr);

		sparePtr.setTagByte0(tu.serialized[tu.offset+0]);
		sparePtr.setTagByte1(tu.serialized[tu.offset+1]);
		sparePtr.setTagByte2(tu.serialized[tu.offset+2]);
		sparePtr.setTagByte3(tu.serialized[tu.offset+3]);
		sparePtr.setTagByte4(tu.serialized[tu.offset+4]);
		sparePtr.setTagByte5(tu.serialized[tu.offset+5]);
		sparePtr.setTagByte6(tu.serialized[tu.offset+6]);
		sparePtr.setTagByte7(tu.serialized[tu.offset+7]);
	}

	static void yaffs_GetTagsFromSpare(yaffs_Device dev, yaffs_Spare sparePtr,
					   yaffs_Tags tagsPtr)
	{
		yaffs_Tags tu = tagsPtr;
		int result;

		tu.serialized[tu.offset+0] = sparePtr.tagByte0();
		tu.serialized[tu.offset+1] = sparePtr.tagByte1();
		tu.serialized[tu.offset+2] = sparePtr.tagByte2();
		tu.serialized[tu.offset+3] = sparePtr.tagByte3();
		tu.serialized[tu.offset+4] = sparePtr.tagByte4();
		tu.serialized[tu.offset+5] = sparePtr.tagByte5();
		tu.serialized[tu.offset+6] = sparePtr.tagByte6();
		tu.serialized[tu.offset+7] = sparePtr.tagByte7();

		result = yaffs_CheckECCOnTags(tagsPtr);
		if (result > 0) {
			dev.subField3.tagsEccFixed++;
		} else if (result < 0) {
			dev.tagsEccUnfixed++;
		}
	}

	static void yaffs_SpareInitialise(yaffs_Spare spare)
	{
		Unix.memset(spare, (byte)0xFF/*, sizeof(yaffs_Spare)*/);
	}

	static boolean yaffs_WriteChunkToNAND(yaffs_Device dev,
					  int chunkInNAND, /*const __u8 **/byte[] data, int dataIndex,
					  yaffs_Spare spare)
	{
		if (chunkInNAND < dev.subField1.startBlock * dev.subField1.nChunksPerBlock) {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
			  ("**>> yaffs chunk %d is not valid" + ydirectenv.TENDSTR),
			  PrimitiveWrapperFactory.get(chunkInNAND));
			return Guts_H.YAFFS_FAIL;
		}

		dev.subField3.nPageWrites++;
		return dev.subField1.writeChunkToNAND.writeChunkToNAND(dev, chunkInNAND, data, dataIndex, spare);
	}

	static boolean yaffs_ReadChunkFromNAND(yaffs_Device dev,
					   int chunkInNAND,
					   /*__u8 **/byte[] data, int dataIndex,
					   yaffs_Spare spare,
					   /*yaffs_ECCResult*/IntegerPointer eccResult,
					   boolean doErrorCorrection)
	{
		boolean retVal;
		yaffs_Spare localSpare = new yaffs_Spare();		

		dev.subField3.nPageReads++;

		if ((spare == null) && (data != null)) {
			/* If we don't have a real spare, then we use a local one. */
			/* Need this for the calculation of the ecc */
			spare = localSpare;
		}

		if (!dev.subField1.useNANDECC) {
			retVal = dev.subField1.readChunkFromNAND.readChunkFromNAND(dev, chunkInNAND, data, dataIndex, spare);
			if ((data != null) && doErrorCorrection) {
				/* Do ECC correction */
				/* Todo handle any errors */
				int eccResult1, eccResult2;
				/*__u8 calcEcc[3];*/ byte[] calcEcc = new byte[3]; 
				int calcEccIndex = 0;

				ECC_C.yaffs_ECCCalculate(data, dataIndex, calcEcc, calcEccIndex);
				eccResult1 =
					ECC_C.yaffs_ECCCorrect(data, dataIndex, spare.ecc1(), spare.ecc1Index(), 
				    		calcEcc, calcEccIndex);
				ECC_C.yaffs_ECCCalculate(data, dataIndex+256, calcEcc, calcEccIndex);
				eccResult2 =
					ECC_C.yaffs_ECCCorrect(data, dataIndex+256, 
				    		spare.ecc2(), spare.ecc2Index(), 
				    		calcEcc, calcEccIndex);

				if (eccResult1 > 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>yaffs ecc error fix performed on chunk %d:0" + 
							   ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
					dev.subField3.eccFixed++;
				} else if (eccResult1 < 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>yaffs ecc error unfixed on chunk %d:0" +
							   ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
					dev.subField3.eccUnfixed++;
				}

				if (eccResult2 > 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>yaffs ecc error fix performed on chunk %d:1" +
							   ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
					dev.subField3.eccFixed++;
				} else if (eccResult2 < 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>yaffs ecc error unfixed on chunk %d:1" +
							   ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
					dev.subField3.eccUnfixed++;
				}

				if ((eccResult1 != 0) || (eccResult2 != 0)) {
					/* We had a data problem on this page */
					yaffs_HandleReadDataError(dev, chunkInNAND);
				}

				if ((eccResult1 < 0) || (eccResult2 < 0))
					eccResult.dereferenced = Guts_H.YAFFS_ECC_RESULT_UNFIXED;
				else if ((eccResult1 > 0) || (eccResult2 > 0))
					eccResult.dereferenced = Guts_H.YAFFS_ECC_RESULT_FIXED;
				else
					eccResult.dereferenced = Guts_H.YAFFS_ECC_RESULT_NO_ERROR;
			}
		} else {
			/* Must allocate enough memory for spare+2*sizeof(int) */
			/* for ecc results from device. */
			yaffs_NANDSpare nspare = new yaffs_NANDSpare();
			retVal =
			    dev.subField1.readChunkFromNAND.readChunkFromNAND(dev, chunkInNAND, data, dataIndex,
						   /*(yaffs_Spare *) & nspare*/nspare.spare);
			Unix.memcpy(spare, nspare.spare/*, sizeof(yaffs_Spare)*/);
			if ((data != null) && doErrorCorrection) {
				if (nspare.eccres1 > 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>mtd ecc error fix performed on chunk %d:0" +
					    ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
				} else if (nspare.eccres1 < 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>mtd ecc error unfixed on chunk %d:0" +
					    ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
				}

				if (nspare.eccres2 > 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>mtd ecc error fix performed on chunk %d:1" +
					    ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
				} else if (nspare.eccres2 < 0) {
					yportenv.T(yportenv.YAFFS_TRACE_ERROR,
					   ("**>>mtd ecc error unfixed on chunk %d:1" +
					    ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(chunkInNAND));
				}

				if ((nspare.eccres1 != 0) || (nspare.eccres2 != 0)) {
					/* We had a data problem on this page */
					yaffs_HandleReadDataError(dev, chunkInNAND);
				}

				if ((nspare.eccres1 < 0) || (nspare.eccres2 < 0))
					eccResult.dereferenced = Guts_H.YAFFS_ECC_RESULT_UNFIXED;
				else if ((nspare.eccres1 > 0) || (nspare.eccres2 > 0))
					eccResult.dereferenced = Guts_H.YAFFS_ECC_RESULT_FIXED;
				else
					eccResult.dereferenced = Guts_H.YAFFS_ECC_RESULT_NO_ERROR;

			}
		}
		return retVal;
	}

//	#ifdef NOTYET
//	static int yaffs_CheckChunkErased(struct yaffs_DeviceStruct *dev,
//					  int chunkInNAND)
//	{
//
//		static int init = 0;
//		static __u8 cmpbuf[YAFFS_BYTES_PER_CHUNK];
//		static __u8 data[YAFFS_BYTES_PER_CHUNK];
//		/* Might as well always allocate the larger size for */
//		/* dev->useNANDECC == true; */
//		static __u8 spare[sizeof(struct yaffs_NANDSpare)];
//
//		dev->readChunkFromNAND(dev, chunkInNAND, data, (yaffs_Spare *) spare);
//
//		if (!init) {
//			memset(cmpbuf, 0xff, YAFFS_BYTES_PER_CHUNK);
//			init = 1;
//		}
//
//		if (memcmp(cmpbuf, data, YAFFS_BYTES_PER_CHUNK))
//			return Guts_H.YAFFS_FAIL;
//		if (memcmp(cmpbuf, spare, 16))
//			return Guts_H.YAFFS_FAIL;
//
//		return YAFFS_OK;
//
//	}
//	#endif

	/*
	 * Functions for robustisizing
	 */

	static void yaffs_HandleReadDataError(yaffs_Device dev, int chunkInNAND)
	{
		int blockInNAND = chunkInNAND / dev.subField1.nChunksPerBlock;

		/* Mark the block for retirement */
		Guts_H.yaffs_GetBlockInfo(dev, blockInNAND).setNeedsRetiring(true);
		yportenv.T(yportenv.YAFFS_TRACE_ERROR | yportenv.YAFFS_TRACE_BAD_BLOCKS,
		  ("**>>Block %d marked for retirement" + ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(blockInNAND));

		/* TODO:
		 * Just do a garbage collection on the affected block
		 * then retire the block
		 * NB recursion
		 */
	}

//	#ifdef NOTYET
//	static void yaffs_CheckWrittenBlock(yaffs_Device * dev, int chunkInNAND)
//	{
//	}
//
//	static void yaffs_HandleWriteChunkOk(yaffs_Device * dev, int chunkInNAND,
//					     const __u8 * data,
//					     const yaffs_Spare * spare)
//	{
//	}
//
//	static void yaffs_HandleUpdateChunk(yaffs_Device * dev, int chunkInNAND,
//					    const yaffs_Spare * spare)
//	{
//	}
//
//	static void yaffs_HandleWriteChunkError(yaffs_Device * dev, int chunkInNAND)
//	{
//		int blockInNAND = chunkInNAND / dev->nChunksPerBlock;
//
//		/* Mark the block for retirement */
//		yaffs_GetBlockInfo(dev, blockInNAND)->needsRetiring = 1;
//		/* Delete the chunk */
//		yaffs_DeleteChunk(dev, chunkInNAND, 1, __LINE__);
//	}
//
//	static int yaffs_VerifyCompare(const __u8 * d0, const __u8 * d1,
//				       const yaffs_Spare * s0, const yaffs_Spare * s1)
//	{
//
//		if (memcmp(d0, d1, YAFFS_BYTES_PER_CHUNK) != 0 ||
//		    s0->tagByte0 != s1->tagByte0 ||
//		    s0->tagByte1 != s1->tagByte1 ||
//		    s0->tagByte2 != s1->tagByte2 ||
//		    s0->tagByte3 != s1->tagByte3 ||
//		    s0->tagByte4 != s1->tagByte4 ||
//		    s0->tagByte5 != s1->tagByte5 ||
//		    s0->tagByte6 != s1->tagByte6 ||
//		    s0->tagByte7 != s1->tagByte7 ||
//		    s0->ecc1[0] != s1->ecc1[0] ||
//		    s0->ecc1[1] != s1->ecc1[1] ||
//		    s0->ecc1[2] != s1->ecc1[2] ||
//		    s0->ecc2[0] != s1->ecc2[0] ||
//		    s0->ecc2[1] != s1->ecc2[1] || s0->ecc2[2] != s1->ecc2[2]) {
//			return 0;
//		}
//
//		return 1;
//	}
//	#endif				/* NOTYET */

	static boolean yaffs_TagsCompatabilityWriteChunkWithTagsToNAND(yaffs_Device dev,
							    int chunkInNAND,
							    /*const __u8 **/byte[] data, int dataIndex,
							    /*const*/ yaffs_ExtendedTags
							    eTags)
	{
		yaffs_Spare spare = new yaffs_Spare();
		yaffs_Tags tags = new yaffs_Tags();

		yaffs_SpareInitialise(spare);

		if (eTags.chunkDeleted) {
			spare.setPageStatus((byte)0);
		} else {
			tags.setObjectID(eTags.objectId);
			tags.setChunkId(eTags.chunkId);
			tags.setByteCount(eTags.byteCount);
			tags.setSerialNumber(eTags.serialNumber);

			if ((!dev.subField1.useNANDECC) && (data != null)) {
				yaffs_CalcECC(data, dataIndex, spare);
			}
			yaffs_LoadTagsIntoSpare(spare, tags);

		}

		return yaffs_WriteChunkToNAND(dev, chunkInNAND, data, dataIndex, spare);
	}
	
	static yaffs_Spare _STATIC_LOCAL_yaffs_TagsCompatabilityReadChunkWithTagsFromNAND_spareFF = new yaffs_Spare();
	static boolean _STATIC_LOCAL_yaffs_TagsCompatabilityReadChunkWithTagsFromNAND_init;

	static boolean yaffs_TagsCompatabilityReadChunkWithTagsFromNAND(yaffs_Device dev,
							     int chunkInNAND,
							     /*__u8 **/byte[] data, int dataIndex,
							     yaffs_ExtendedTags eTags)
	{

		yaffs_Spare spare = new yaffs_Spare();
		yaffs_Tags tags = new yaffs_Tags();
		int eccResult;

		/*static yaffs_Spare spareFF;*/
		/*static int init;*/

		if (!_STATIC_LOCAL_yaffs_TagsCompatabilityReadChunkWithTagsFromNAND_init) {
			Unix.memset(_STATIC_LOCAL_yaffs_TagsCompatabilityReadChunkWithTagsFromNAND_spareFF, (byte)0xFF/*, sizeof(spareFF)*/);
			_STATIC_LOCAL_yaffs_TagsCompatabilityReadChunkWithTagsFromNAND_init = true;
		}

		IntegerPointer eccResultPointer = new IntegerPointer();
		
		if (yaffs_ReadChunkFromNAND
		    (dev, chunkInNAND, data, dataIndex, spare, eccResultPointer, true)) {
			eccResult = eccResultPointer.dereferenced;
			/* eTags may be NULL */
			if (eTags != null) {

				boolean deleted =
				    (yaffs_CountBits(spare.pageStatus()) < 7);

				eTags.chunkDeleted = deleted;
				eTags.eccResult = eccResult;
				eTags.blockBad = false;	/* We're reading it */
				/* therefore it is not a bad block */

				eTags.chunkUsed = Unix.memcmp(_STATIC_LOCAL_yaffs_TagsCompatabilityReadChunkWithTagsFromNAND_spareFF, spare) != 0; 

				if (eTags.chunkUsed) {
					yaffs_GetTagsFromSpare(dev, spare, tags);

					eTags.objectId = tags.getObjectId();
					eTags.chunkId = tags.getChunkId();
					eTags.byteCount = tags.getByteCount();
					eTags.serialNumber = tags.getSerialNumber();
				}
			}

			return Guts_H.YAFFS_OK;
		} else {
//			eccResult = eccResultPointer.dereferenced;
			return Guts_H.YAFFS_FAIL;
		}
	}

	static boolean yaffs_TagsCompatabilityMarkNANDBlockBad(yaffs_Device dev,
						    int blockInNAND)
	{

		yaffs_Spare spare = new yaffs_Spare();

		Unix.memset(spare, (byte)0xFF/*, sizeof(yaffs_Spare)*/);

		spare.setBlockStatus((byte)'Y');

		yaffs_WriteChunkToNAND(dev, blockInNAND * dev.subField1.nChunksPerBlock, null, 0,
				       spare);
		yaffs_WriteChunkToNAND(dev, blockInNAND * dev.subField1.nChunksPerBlock + 1,
				       null, 0, spare);

		return Guts_H.YAFFS_OK;

	}
	
	static yaffs_Spare _STATIC_LOCAL_yaffs_TagsCompatabilityQueryNANDBlock_spareFF = new yaffs_Spare();
	static boolean _STATIC_LOCAL_yaffs_TagsCompatabilityQueryNANDBlock_init;
	
	static boolean yaffs_TagsCompatabilityQueryNANDBlock(yaffs_Device dev,
			  int blockNo, /*yaffs_BlockState **/ IntegerPointer
			  state,
			  IntegerPointer sequenceNumber)
	{

		yaffs_Spare spare0 = new yaffs_Spare();
		yaffs_Spare spare1 = new yaffs_Spare();
//		static yaffs_Spare spareFF;
//		static int init;
//		int dummy;

		if (!_STATIC_LOCAL_yaffs_TagsCompatabilityQueryNANDBlock_init) {
			Unix.memset(_STATIC_LOCAL_yaffs_TagsCompatabilityQueryNANDBlock_spareFF, (byte)0xFF/*, sizeof(spareFF)*/);
			_STATIC_LOCAL_yaffs_TagsCompatabilityQueryNANDBlock_init = true;
		}

		sequenceNumber.dereferenced = 0;
		
		IntegerPointer dummyPointer = new IntegerPointer();

		yaffs_ReadChunkFromNAND(dev, blockNo * dev.subField1.nChunksPerBlock, null, 0,
				spare0, dummyPointer, true);
		yaffs_ReadChunkFromNAND(dev, blockNo * dev.subField1.nChunksPerBlock + 1, null, 0,
				spare1, dummyPointer, true);
		
//		dummy = dummyPointer.dereferenced;

		if (yaffs_CountBits((byte)(spare0.blockStatus() & spare1.blockStatus())) < 7)
			state.dereferenced = Guts_H.YAFFS_BLOCK_STATE_DEAD;
		/*else if (memcmp(&spareFF, &spare0, sizeof(spareFF)) == 0)*/
		else if (Unix.memcmp(_STATIC_LOCAL_yaffs_TagsCompatabilityQueryNANDBlock_spareFF, spare0) == 0)
			state.dereferenced = Guts_H.YAFFS_BLOCK_STATE_EMPTY;
		else
			state.dereferenced = Guts_H.YAFFS_BLOCK_STATE_NEEDS_SCANNING;
		
		return Guts_H.YAFFS_OK;
	}
}
