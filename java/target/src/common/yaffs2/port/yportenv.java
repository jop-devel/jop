package yaffs2.port;

import static yaffs2.utils.Utils.*;
import static yaffs2.port.ydirectenv.*;

public class yportenv
{
	/*
	 * YAFFS: Yet another Flash File System . A NAND-flash specific file system. 
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU Lesser General Public License version 2.1 as
	 * published by the Free Software Foundation.
	 *
	 * Note: Only YAFFS headers are LGPL, YAFFS C code is covered by GPL.
	 */


	//#elif defined CONFIG_YAFFS_DIRECT

	/* Direct interface */
	//#include "ydirectenv.h"

	//#endif

//	extern unsigned yaffs_traceMask;

	public static final int YAFFS_TRACE_ERROR	=	0x00000001;
	public static final int YAFFS_TRACE_OS =			0x00000002;
	public static final int YAFFS_TRACE_ALLOCATE =		0x00000004;
	public static final int YAFFS_TRACE_SCAN =	0x00000008;
	public static final int YAFFS_TRACE_BAD_BLOCKS =		0x00000010;
	public static final int YAFFS_TRACE_ERASE =		0x00000020;
	public static final int YAFFS_TRACE_GC =			0x00000040;
	public static final int YAFFS_TRACE_WRITE =		0x00000080;
	public static final int YAFFS_TRACE_TRACING =		0x00000100;
	public static final int YAFFS_TRACE_DELETION =		0x00000200;
	public static final int YAFFS_TRACE_BUFFERS =		0x00000400;
	public static final int YAFFS_TRACE_NANDACCESS =		0x00000800;
	public static final int YAFFS_TRACE_GC_DETAIL =		0x00001000;
	public static final int YAFFS_TRACE_SCAN_DEBUG =		0x00002000;
	public static final int YAFFS_TRACE_MTD =			0x00004000;
	public static final int YAFFS_TRACE_CHECKPOINT =		0x00008000;
	public static final int YAFFS_TRACE_ALWAYS =		0x40000000;
	public static final int YAFFS_TRACE_BUG =			0x80000000;

	/**
	 * XXX enable ability to disable
	 * @param mask
	 * @param p
	 * @param args
	 */
	public static void T(int mask, String p, Object... args)
	{
		if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
			TOUT(p, args);
	}
	
//	static void YBUG()
//	{
//		////T(YAFFS_TRACE_BUG,(TSTR("==>> yaffs bug: " __FILE__ " %d" TENDSTR),__LINE__))
//		T(YAFFS_TRACE_BUG,TSTR("==>> yaffs bug: " + __FILE__() + " %d" + TENDSTR),__LINE__());
//	}
}
