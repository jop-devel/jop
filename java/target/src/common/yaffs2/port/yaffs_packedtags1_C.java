package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_packedtags1_C
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

//	#include "yaffs_packedtags1.h"
//	#include "yportenv.h"

	public static void yaffs_PackTags1(yaffs_PackedTags1 pt, yaffs_ExtendedTags t)
	{
		pt.setChunkId(t.chunkId);
		pt.setSerialNumber((byte)t.serialNumber);
		pt.setByteCount(t.byteCount);
		pt.setObjectId(t.objectId);
		pt.setEcc(0);
		pt.setDeleted(t.chunkDeleted);
		pt.setUnusedStuff(false);
		pt.setShouldBeFF(0xFFFFFFFF);

	}

	static final byte[] allFF =
     { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff };
	public static void yaffs_UnpackTags1(yaffs_ExtendedTags t, yaffs_PackedTags1 pt)
	{
//		static const __u8 allFF[] =
//		    { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
//	0xff };

		if (Unix.memcmp(allFF, 0, pt.serialized, pt.offset, /*sizeof(yaffs_PackedTags1)*/ yaffs_PackedTags1.SERIALIZED_LENGTH) != 0) {
			t.blockBad = false;
			if (pt.shouldBeFF() != 0xFFFFFFFF) {
				t.blockBad = true;
			}
			t.chunkUsed = true;
			t.objectId = pt.objectId();
			t.chunkId = pt.chunkId();
			t.byteCount = pt.byteCount();
			t.eccResult = Guts_H.YAFFS_ECC_RESULT_NO_ERROR;
			t.chunkDeleted = pt.deleted();
			t.serialNumber = Utils.byteAsUnsignedByte(pt.serialNumber());
		} else {
			Unix.memset(t/*, 0 , sizeof(yaffs_ExtendedTags)*/ );

		}
	}
}
