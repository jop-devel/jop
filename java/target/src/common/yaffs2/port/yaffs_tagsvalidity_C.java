package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_tagsvalidity_C {
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

//	#include "yaffs_tagsvalidity.h"
	
	public static void yaffs_InitialiseTags(yaffs_ExtendedTags tags)
	{
		Unix.memset(tags);
		tags.validMarker0 = 0xAAAAAAAA;
		tags.validMarker1 = 0x55555555;
	}

	static boolean yaffs_ValidateTags(yaffs_ExtendedTags tags)
	{
		return ((tags.validMarker0 == 0xAAAAAAAA) &&
			(tags.validMarker1 == 0x55555555));
	}

}
