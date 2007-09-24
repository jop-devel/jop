package yaffs2.port;

import yaffs2.utils.*;

public class ydirectenv
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

	/*
	 * ydirectenv.h: Environment wrappers for YAFFS direct.
	 */

//	 Direct interface

	// #include "devextras.h"

	// #define YYIELD()  do {} while(0)
	static void YYIELD()
	{
		if (YYIELDCallback != null)
			YYIELDCallback.YYIELD();
	}
	
	/**
	 * PORT Set this to get some callbacks while YAFFS is scanning a device.
	 */
	public static YYIELDInterface YYIELDCallback;

	// PORT Removed.
	// #define _Y(x) x

	static void yaffs_strcpy(byte[] a, int aIndex, byte[] b, int bIndex)
	{
		Unix.strcpy(a, aIndex, b, bIndex);
	}
	static void yaffs_strncpy(byte[] a, int aIndex, byte[] b, int bIndex, int c)
	{
		Unix.strncpy(a, aIndex, b, bIndex, c);
	}
	static int yaffs_strlen(byte[] s, int sIndex)
	{
		return Unix.strlen(s, sIndex);
	}
//	static void yaffs_sprintf(byte[] s, int sIndex, String format, PrimitiveWrapper... args)
//	{
//		Unix.sprintf(s, sIndex, format, args);
//	}
	//#define yaffs_toupper(a)     toupper(a)
	
	
	static byte[] YMALLOC(int x)
	{
		return new byte[x];
	}
	static int[] YMALLOC_INT(int x)
	{
		return new int[x];
	}
	public static void YFREE(byte[] x)
	{		
		// XXX cant do anything
	}
	public static void YFREE(int[] x)
	{
		// XXX cant do anything
	}
	public static void YFREE(Object x)
	{
		// XXX cant do anything
	}
	
	public static void YFREE_ALT(Object x)
	{
		// XXX get rid of the calls
	}

	static byte[] YMALLOC_DMA(int x)
	{
		return YMALLOC(x);
	}
	static yaffs_BlockIndex[] YMALLOC_BLOCKINDEX(int x)
	{
		yaffs_BlockIndex[] result = new yaffs_BlockIndex[x];
		for (int i = 0; i < x; i++)
			result[i] = new yaffs_BlockIndex();
		
		return result;
	}
	static yaffs_Object[] YMALLOC_OBJECT(int x)
	{
		yaffs_Object[] result = new yaffs_Object[x];
		for (int i = 0; i < x; i++)
			result [i] = new yaffs_Object();
		
		return result;
	}
	/**
	 * One byte array backing all instances will be allocated as contiguous block for checkpointing.
	 * @param x
	 * @return
	 */
	static yaffs_BlockInfo[] YMALLOC_BLOCKINFO(int x)
	{
		yaffs_BlockInfo[] result = new yaffs_BlockInfo[x];
		// PORT contiguous block for checkpointing 
		byte[] backingBuffer = new byte[x*yaffs_BlockInfo.SERIALIZED_LENGTH];
		for (int i = 0; i < x; i++)
			result[i] = new yaffs_BlockInfo(backingBuffer, i*yaffs_BlockInfo.SERIALIZED_LENGTH);
		
		return result;
	}
	
	static yaffs_ChunkCache[] YMALLOC_CHUNKCACHE(int x)
	{
		yaffs_ChunkCache[] result = new yaffs_ChunkCache[x];
		for (int i = 0; i < x; i++)
			result [i] = new yaffs_ChunkCache();
		
		return result;
	}
	
	static yaffs_Tnode[] YMALLOC_TNODE(int x)
	{
		yaffs_Tnode[] result = new yaffs_Tnode[x];
		for (int i = 0; i < x; i++)
			result[i] = new yaffs_Tnode();
		
		return result;
	}
	
	static yaffs_BlockIndex[] YMALLOC_ALT_BLOCKINDEX(int x)
	{
		throw new NotImplementedException(); // XXX remove callers
	}
	
//	#define YINFO(s) YPRINTF(( __FILE__ " %d %s\n",__LINE__,s))
//	#define YALERT(s) YINFO(s)


	public static final String TENDSTR = "\n";
	
//	public static final String (String x)
//	{
//		return x;
//	}

//	static void TOUT(String format, Object... args)
//	{
//		printf(format, args); 
//	}

//	static final String YAFFS_LOSTNFOUND_NAME =		"lost+found";
	static final byte[] YAFFS_LOSTNFOUND_NAME =		
    {
		'l','o','s','t','+','f','o','u','n','d','\0'			
     };
	
//	static final String YAFFS_LOSTNFOUND_PREFIX	=	"obj";
	static final byte[] YAFFS_LOSTNFOUND_PREFIX	= 
		     {
				'o','b','j','\0'
		     };

	// #define YPRINTF(x) printf x

	// #include "yaffscfg.h"

	static int Y_CURRENT_TIME()
	{
		return yaffs2.utils.Globals.configuration.yaffsfs_CurrentTime();
	}
	
	// PORT removed
	// #define Y_TIME_CONVERT(x) x

	static final int YAFFS_ROOT_MODE =				0666;
	static final int YAFFS_LOSTNFOUND_MODE =		0666;

	static boolean yaffs_SumCompare(int x, int y)
	{
		return ((x) == (y));
	}

	static int yaffs_strcmp(byte[] a, int aIndex, byte[] b, int bIndex)
	{
		return Unix.strcmp(a,aIndex,b,bIndex);
	}

}
