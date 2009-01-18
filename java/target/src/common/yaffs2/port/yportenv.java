package yaffs2.port;

import yaffs2.utils.Unix;
import yaffs2.utils.factory.PrimitiveWrapper;

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
	
	// PORT added for port debugging
	public static final int PORT_TRACE_TNODE =		0x00010000;
	public static final int PORT_TRACE_NANDSIM =		0x00020000;
	public static final int PORT_TRACE_TALLNESS =		0x00040000;
	public static final int PORT_TRACE_TOPLEVEL =		0x00080000;
	public static final int PORT_TRACE_CHECKSUMS =		0x00100000;
	
	public static final int YAFFS_TRACE_ALWAYS =		0x40000000;
	public static final int YAFFS_TRACE_BUG =			0x80000000;
	
	public static final boolean LOGGING = true;

	public static void T(int mask, String p)
	{
//		printfBuffer[0] = arg0; 
//		printfBuffer[1] = arg1;
//		printfBuffer[2] = arg2;
//		printfBuffer[3] = arg3;
//		printfBuffer[4] = arg4;
//		printfBuffer[5] = arg5;
//		printfBuffer[6] = arg6;
//		printfBuffer[7] = arg7;
//		printfBuffer[8] = arg8;

		if (LOGGING) {
			if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
				Unix.printf(p);			
		}
	}
	
	public static void T(int mask, String p, PrimitiveWrapper arg0)
	{
		if (LOGGING) {
			Unix.xprintfArgs[0] = arg0; 
//			printfBuffer[1] = arg1;
//			printfBuffer[2] = arg2;
//			printfBuffer[3] = arg3;
//			printfBuffer[4] = arg4;
//			printfBuffer[5] = arg5;
//			printfBuffer[6] = arg6;
//			printfBuffer[7] = arg7;
//			printfBuffer[8] = arg8;

			if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
				Unix.printf(p);
		}
	}
	
	public static void T(int mask, String p, PrimitiveWrapper arg0, PrimitiveWrapper arg1)
	{
		if (LOGGING) {
			Unix.xprintfArgs[0] = arg0; 
			Unix.xprintfArgs[1] = arg1;
//			printfBuffer[2] = arg2;
//			printfBuffer[3] = arg3;
//			printfBuffer[4] = arg4;
//			printfBuffer[5] = arg5;
//			printfBuffer[6] = arg6;
//			printfBuffer[7] = arg7;
//			printfBuffer[8] = arg8;

			if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
				Unix.printf(p);
		}
	}

	public static void T(int mask, String p, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2)
	{
		if (LOGGING) {
			Unix.xprintfArgs[0] = arg0; 
			Unix.xprintfArgs[1] = arg1;
			Unix.xprintfArgs[2] = arg2;
//			printfBuffer[3] = arg3;
//			printfBuffer[4] = arg4;
//			printfBuffer[5] = arg5;
//			printfBuffer[6] = arg6;
//			printfBuffer[7] = arg7;
//			printfBuffer[8] = arg8;

			if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
				Unix.printf(p);
		}
	}
	
	public static void T(int mask, String p, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3)
	{
		if (LOGGING) {
			Unix.xprintfArgs[0] = arg0; 
			Unix.xprintfArgs[1] = arg1;
			Unix.xprintfArgs[2] = arg2;
			Unix.xprintfArgs[3] = arg3;
//			printfBuffer[4] = arg4;
//			printfBuffer[5] = arg5;
//			printfBuffer[6] = arg6;
//			printfBuffer[7] = arg7;
//			printfBuffer[8] = arg8;

			if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
				Unix.printf(p);
		}
	}
	
	public static void T(int mask, String p, 
			PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3,
			PrimitiveWrapper arg4, PrimitiveWrapper arg5, PrimitiveWrapper arg6, PrimitiveWrapper arg7,
			PrimitiveWrapper arg8)
	{		
		if (LOGGING) {
			Unix.xprintfArgs[0] = arg0; 
			Unix.xprintfArgs[1] = arg1;
			Unix.xprintfArgs[2] = arg2;
			Unix.xprintfArgs[3] = arg3;
			Unix.xprintfArgs[4] = arg4;
			Unix.xprintfArgs[5] = arg5;
			Unix.xprintfArgs[6] = arg6;
			Unix.xprintfArgs[7] = arg7;
			Unix.xprintfArgs[8] = arg8;

			if(((mask) & (yaffs2.utils.Globals.yaffs_traceMask | YAFFS_TRACE_ERROR)) != 0)
				Unix.printf(p);
		}
	}

	//	static void YBUG()
//	{
//		////T(YAFFS_TRACE_BUG,(("==>> yaffs bug: " __FILE__ " %d" TENDSTR),__LINE__))
//		T(YAFFS_TRACE_BUG,("==>> yaffs bug: " + __FILE__() + " %d" + TENDSTR),__LINE__());
//	}
}
