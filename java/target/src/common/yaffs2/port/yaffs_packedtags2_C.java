package yaffs2.port;

import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class yaffs_packedtags2_C
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

//	#include "yaffs_packedtags2.h"
//	#include "yportenv.h"
//	#include "yaffs_tagsvalidity.h"

	/* This code packs a set of extended tags into a binary structure for
	 * NAND storage
	 */

	/* Some of the information is "extra" struff which can be packed in to
	 * speed scanning
	 * This is defined by having the EXTRA_HEADER_INFO_FLAG set.
	 */

	/* Extra flags applied to chunkId */

	static final int EXTRA_HEADER_INFO_FLAG =  0x80000000; // PORT ok
	static final int EXTRA_SHRINK_FLAG =	0x40000000;
	static final int EXTRA_SHADOWS_FLAG	= 0x20000000;
	static final int EXTRA_SPARE_FLAGS =	0x10000000;

	static final int ALL_EXTRA_FLAGS =		0xF0000000; // PORT ok

	/* Also, the top 4 bits of the object Id are set to the object type. */
	static final int EXTRA_OBJECT_TYPE_SHIFT = 28;
	static final int EXTRA_OBJECT_TYPE_MASK = ((0x0F) << EXTRA_OBJECT_TYPE_SHIFT);

	static void yaffs_DumpPackedTags2(yaffs_PackedTags2 pt)
	{
		yportenv.T(yportenv.YAFFS_TRACE_MTD,
		  ("packed tags obj %d chunk %d byte %d seq %d" + ydirectenv.TENDSTR),
		  PrimitiveWrapperFactory.get(pt.t.objectId()), PrimitiveWrapperFactory.get(pt.t.chunkId()), PrimitiveWrapperFactory.get(pt.t.byteCount()),
		  PrimitiveWrapperFactory.get(pt.t.sequenceNumber()));
	}

	static void yaffs_DumpTags2(yaffs_ExtendedTags t)
	{
		yportenv.T(yportenv.YAFFS_TRACE_MTD,
		   ("ext.tags eccres %d blkbad %b chused %b obj %d chunk%d byte " +
		    "%d del %b ser %d seq %d" +
		    ydirectenv.TENDSTR), PrimitiveWrapperFactory.get(t.eccResult), PrimitiveWrapperFactory.get(t.blockBad), PrimitiveWrapperFactory.get(t.chunkUsed), PrimitiveWrapperFactory.get(t.objectId),
		    PrimitiveWrapperFactory.get(t.chunkId), PrimitiveWrapperFactory.get(t.byteCount), PrimitiveWrapperFactory.get(t.chunkDeleted), PrimitiveWrapperFactory.get(t.serialNumber),
		    PrimitiveWrapperFactory.get(t.sequenceNumber));

	}

	public static void yaffs_PackTags2(yaffs_PackedTags2 pt, yaffs_ExtendedTags t)
	{
		pt.t.setChunkId(t.chunkId);
		pt.t.setSequenceNumber(t.sequenceNumber);
		pt.t.setByteCount(t.byteCount);
		pt.t.setObjectId(t.objectId);

		if (t.chunkId == 0 && t.extraHeaderInfoAvailable) {
			/* Store the extra header info instead */
			/* We save the parent object in the chunkId */
			pt.t.setChunkId(EXTRA_HEADER_INFO_FLAG
				| t.extraParentObjectId);
			if (t.extraIsShrinkHeader) {
				pt.t.orChunkId(EXTRA_SHRINK_FLAG);
			}
			if (t.extraShadows) {
				pt.t.orChunkId(EXTRA_SHADOWS_FLAG);
			}

			pt.t.andObjectId(~EXTRA_OBJECT_TYPE_MASK);
			pt.t.orObjectId(
			    (t.extraObjectType << EXTRA_OBJECT_TYPE_SHIFT));

			if (t.extraObjectType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK) {
				pt.t.setByteCount(t.extraEquivalentObjectId);
			} else if (t.extraObjectType == Guts_H.YAFFS_OBJECT_TYPE_FILE) {
				pt.t.setByteCount(t.extraFileLength);
			} else {
				pt.t.setByteCount(0);
			}
		}

		yaffs_DumpPackedTags2(pt);
		yaffs_DumpTags2(t);

//	#ifndef YAFFS_IGNORE_TAGS_ECC
		{
			ECC_C.yaffs_ECCCalculateOther(/*(unsigned char *)&*/ pt.t/*,
					sizeof(yaffs_PackedTags2TagsPart)*/ ,
						pt.ecc);
		}
//	#endif
	}

	public static void yaffs_UnpackTags2(yaffs_ExtendedTags t, yaffs_PackedTags2 pt)
	{

		Unix.memset(t/*, 0, sizeof(yaffs_ExtendedTags)*/);

		yaffs_tagsvalidity_C.yaffs_InitialiseTags(t);

		if (pt.t.sequenceNumber() != 0xFFFFFFFF) {
			/* Page is in use */
//	#ifdef YAFFS_IGNORE_TAGS_ECC
//			{
//				t.eccResult = YAFFS_ECC_RESULT_NO_ERROR;
//			}
//	#else
			{
				yaffs_ECCOther ecc = new yaffs_ECCOther();
				int result;
				ECC_C.yaffs_ECCCalculateOther(/*(unsigned char *)&*/ pt.t,
						/*sizeof
						(yaffs_PackedTags2TagsPart),*/
							ecc);
				result =
					ECC_C.yaffs_ECCCorrectOther(/*(unsigned char *)&*/ pt.t,
				    		/*sizeof
							  (yaffs_PackedTags2TagsPart),*/ 
							  pt.ecc, ecc);
				switch(result){
					case 0: 
						t.eccResult = Guts_H.YAFFS_ECC_RESULT_NO_ERROR; 
						break;
					case 1: 
						t.eccResult = Guts_H.YAFFS_ECC_RESULT_FIXED;
						break;
					case -1:
						t.eccResult = Guts_H.YAFFS_ECC_RESULT_UNFIXED;
						break;
					default:
						t.eccResult = Guts_H.YAFFS_ECC_RESULT_UNKNOWN;
				}
			}
//	#endif
			t.blockBad = false;
			t.chunkUsed = true;
			t.objectId = pt.t.objectId();
			t.chunkId = pt.t.chunkId();
			t.byteCount = pt.t.byteCount();
			t.chunkDeleted = false;
			t.serialNumber = 0;
			t.sequenceNumber = pt.t.sequenceNumber();

			/* Do extra header info stuff */

			if ((pt.t.chunkId() & EXTRA_HEADER_INFO_FLAG) != 0) {
				t.chunkId = 0;
				t.byteCount = 0;

				t.extraHeaderInfoAvailable = true;
				t.extraParentObjectId =
				    pt.t.chunkId() & (~(ALL_EXTRA_FLAGS));
				t.extraIsShrinkHeader =
				    (pt.t.chunkId() & EXTRA_SHRINK_FLAG) != 0;
				t.extraShadows =
				    (pt.t.chunkId() & EXTRA_SHADOWS_FLAG) != 0;
				t.extraObjectType =
				    pt.t.objectId() >>> EXTRA_OBJECT_TYPE_SHIFT;
				t.objectId &= ~EXTRA_OBJECT_TYPE_MASK;

				if (t.extraObjectType == Guts_H.YAFFS_OBJECT_TYPE_HARDLINK) {
					t.extraEquivalentObjectId = pt.t.byteCount();
				} else {
					t.extraFileLength = pt.t.byteCount();
				}
			}
		}

		yaffs_DumpPackedTags2(pt);
		yaffs_DumpTags2(t);

	}
}
