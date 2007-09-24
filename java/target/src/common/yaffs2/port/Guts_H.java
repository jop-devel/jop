package yaffs2.port;

import yaffs2.utils.factory.PrimitiveWrapperFactory;
public abstract class Guts_H
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

	public static final boolean YAFFS_OK = /*1*/ true;
	public static final boolean YAFFS_FAIL = /*0*/ false;

	/* Give us a  Y=0x59, 
	 * Give us an A=0x41, 
	 * Give us an FF=0xFF 
	 * Give us an S=0x53
	 * And what have we got... 
	 */
	static final int YAFFS_MAGIC	= 0x5941FF53;

	static final int YAFFS_NTNODES_LEVEL0 =	  	16;
	static final int YAFFS_TNODES_LEVEL0_BITS =	4;
	static final int YAFFS_TNODES_LEVEL0_MASK =	0xf;

	static final int YAFFS_NTNODES_INTERNAL = 		(YAFFS_NTNODES_LEVEL0 / 2);
	static final int YAFFS_TNODES_INTERNAL_BITS =	(YAFFS_TNODES_LEVEL0_BITS - 1);
	static final int YAFFS_TNODES_INTERNAL_MASK	= 0x7;
	static final int YAFFS_TNODES_MAX_LEVEL	=	6;

	//#ifndef CONFIG_YAFFS_NO_YAFFS1
	static final int YAFFS_BYTES_PER_SPARE =		16;
	static final int YAFFS_BYTES_PER_CHUNK =		512;
	static final int YAFFS_CHUNK_SIZE_SHIFT =	9;
	static final int YAFFS_CHUNKS_PER_BLOCK =		32;
	static final int YAFFS_BYTES_PER_BLOCK =		(YAFFS_CHUNKS_PER_BLOCK*YAFFS_BYTES_PER_CHUNK);
	//#endif

	static final int YAFFS_MIN_YAFFS2_CHUNK_SIZE =	1024;
	static final int YAFFS_MIN_YAFFS2_SPARE_SIZE	= 32;

	static final int YAFFS_MAX_CHUNK_ID =		0x000FFFFF;

	static final int YAFFS_UNUSED_OBJECT_ID =		0x0003FFFF;

	static final int YAFFS_ALLOCATION_NOBJECTS =	100;
	static final int YAFFS_ALLOCATION_NTNODES =	100;
	static final int YAFFS_ALLOCATION_NLINKS	=	100;

	static final int YAFFS_NOBJECT_BUCKETS =		256;


	static final int YAFFS_OBJECT_SPACE =		0x40000;

	static final int YAFFS_NCHECKPOINT_OBJECTS =	5000;

	static final int YAFFS_CHECKPOINT_VERSION =	2;

	/*#ifdef CONFIG_YAFFS_UNICODE
	#define YAFFS_MAX_NAME_LENGTH		127
	#define YAFFS_MAX_ALIAS_LENGTH		79
	#else*/
	static final int YAFFS_MAX_NAME_LENGTH =		255;
	static final int YAFFS_MAX_ALIAS_LENGTH =		159;
	/*#endif*/

	static final int YAFFS_SHORT_NAME_LENGTH	=	15;

	/* Some special object ids for pseudo objects */
	static final int YAFFS_OBJECTID_ROOT	=	1;
	static final int YAFFS_OBJECTID_LOSTNFOUND =	2;
	static final int YAFFS_OBJECTID_UNLINKED	=	3;
	static final int YAFFS_OBJECTID_DELETED =		4;

	/* Sseudo object ids for checkpointing */
	static final int YAFFS_OBJECTID_SB_HEADER =	0x10;
	static final int YAFFS_OBJECTID_CHECKPOINT_DATA =	0x20;
	static final int YAFFS_SEQUENCE_CHECKPOINT_DATA = 0x21;

	/* */

	static final int YAFFS_MAX_SHORT_OP_CACHES =	20;

	static final int YAFFS_N_TEMP_BUFFERS =		4;

	/* Sequence numbers are used in YAFFS2 to determine block allocation order.
	 * The range is limited slightly to help distinguish bad numbers from good.
	 * This also allows us to perhaps in the future use special numbers for
	 * special purposes.
	 * EFFFFF00 allows the allocation of 8 blocks per second (~1Mbytes) for 15 years, 
	 * and is a larger number than the lifetime of a 2GB device.
	 */
	static final long YAFFS_LOWEST_SEQUENCE_NUMBER =	0x00001000l;
	static final long YAFFS_HIGHEST_SEQUENCE_NUMBER =	0xEFFFFF00l;






	/* Stuff used for extended tags in YAFFS2 */

	//typedef enum {
	public static final int YAFFS_ECC_RESULT_UNKNOWN = 0;
	public static final int YAFFS_ECC_RESULT_NO_ERROR = 1;
	public static final int YAFFS_ECC_RESULT_FIXED = 2;
	public static final int YAFFS_ECC_RESULT_UNFIXED = 3;
	//} yaffs_ECCResult;

	//typedef enum {
	static final int YAFFS_OBJECT_TYPE_UNKNOWN = 0;
	static final int YAFFS_OBJECT_TYPE_FILE = 1;
	static final int YAFFS_OBJECT_TYPE_SYMLINK = 2;
	static final int YAFFS_OBJECT_TYPE_DIRECTORY = 3;
	static final int YAFFS_OBJECT_TYPE_HARDLINK = 4;
	static final int YAFFS_OBJECT_TYPE_SPECIAL = 5;
	//} yaffs_ObjectType;




	/*Special structure for passing through to mtd */
	/*struct yaffs_NANDSpare {
		yaffs_Spare spare;
		int eccres1;
		int eccres2;
	};*/

	/* Block data in RAM */

	//typedef enum {
		public static final int YAFFS_BLOCK_STATE_UNKNOWN = 0;

		public static final int YAFFS_BLOCK_STATE_SCANNING = 1;
		public static final int YAFFS_BLOCK_STATE_NEEDS_SCANNING = 2;
		/* The block might have something on it (ie it is allocating or full, perhaps empty)
		 * but it needs to be scanned to determine its true state.
		 * This state is only valid during yaffs_Scan.
		 * NB We tolerate empty because the pre-scanner might be incapable of deciding
		 * However, if this state is returned on a YAFFS2 device, then we expect a sequence number
		 */

		public static final int YAFFS_BLOCK_STATE_EMPTY = 3;
		/* This block is empty */

		public static final int YAFFS_BLOCK_STATE_ALLOCATING = 4;
		/* This block is partially allocated. 
		 * At least one page holds valid data.
		 * This is the one currently being used for page
		 * allocation. Should never be more than one of these
		 */

		public static final int YAFFS_BLOCK_STATE_FULL = 5;	
		/* All the pages in this block have been allocated.
		 */

		public static final int YAFFS_BLOCK_STATE_DIRTY = 6;
		/* All pages have been allocated and deleted. 
		 * Erase me, reuse me.
		 */

		public static final int YAFFS_BLOCK_STATE_CHECKPOINT = 7;	
		/* This block is assigned to holding checkpoint data.
		 */

		public static final int YAFFS_BLOCK_STATE_COLLECTING = 8;	
		/* This block is being garbage collected */

		public static final int YAFFS_BLOCK_STATE_DEAD	= 9;
		/* This block has failed and is not in use */
	//} yaffs_BlockState;


	/*------------------------  Object -----------------------------*/
	/* An object can be one of:
	 * - a directory (no data, has children links
	 * - a regular file (data.... not prunes :->).
	 * - a symlink [symbolic link] (the alias).
	 * - a hard link
	 */




	/* Function to manipulate block info */
	static /*Y_INLINE*/ yaffs_BlockInfo yaffs_GetBlockInfo(yaffs_Device dev, int blk)
	{
		if (blk < dev.subField2.internalStartBlock || blk > dev.subField2.internalEndBlock) {
			yportenv.T(yportenv.YAFFS_TRACE_ERROR,
			   ("**>> yaffs: getBlockInfo block %d is not valid" + ydirectenv.TENDSTR),
			   PrimitiveWrapperFactory.get(blk));
			yaffs2.utils.Globals.portConfiguration.YBUG();
		}
		return dev.subField2.blockInfo[blk - dev.subField2.internalStartBlock];
	}

	/*----------------------- YAFFS Functions -----------------------*/


//	public static int yaffs_GutsInitialise(yaffs_Device dev);
//	public static void yaffs_Deinitialise(yaffs_Device dev);
//
//	public static int yaffs_GetNumberOfFreeChunks(yaffs_Device dev);
//
//	public static int yaffs_RenameObject(yaffs_Object oldDir, byte[] oldName,
//			       yaffs_Object newDir, byte[] newName);
//
//	public static int yaffs_Unlink(yaffs_Object dir, byte[] name);
//	public static int yaffs_DeleteFile(yaffs_Object obj);
//
//	int yaffs_GetObjectName(yaffs_Object * obj, YCHAR * name, int buffSize);
//	int yaffs_GetObjectFileLength(yaffs_Object * obj);
//	int yaffs_GetObjectInode(yaffs_Object * obj);
//	unsigned yaffs_GetObjectType(yaffs_Object * obj);
//	int yaffs_GetObjectLinkCount(yaffs_Object * obj);
//
//	int yaffs_SetAttributes(yaffs_Object * obj, struct iattr *attr);
//	int yaffs_GetAttributes(yaffs_Object * obj, struct iattr *attr);
//
//	/* File operations */
//	int yaffs_ReadDataFromFile(yaffs_Object * obj, __u8 * buffer, loff_t offset,
//				   int nBytes);
//	int yaffs_WriteDataToFile(yaffs_Object * obj, const __u8 * buffer, loff_t offset,
//				  int nBytes, int writeThrough);
//	int yaffs_ResizeFile(yaffs_Object * obj, loff_t newSize);
//
//	yaffs_Object *yaffs_MknodFile(yaffs_Object * parent, const YCHAR * name,
//				      __u32 mode, __u32 uid, __u32 gid);
//	int yaffs_FlushFile(yaffs_Object * obj, int updateTime);
//
//	/* Flushing and checkpointing */
//	void yaffs_FlushEntireDeviceCache(yaffs_Device *dev);
//
//	int yaffs_CheckpointSave(yaffs_Device *dev);
//	int yaffs_CheckpointRestore(yaffs_Device *dev);
//
//	/* Directory operations */
//	yaffs_Object *yaffs_MknodDirectory(yaffs_Object * parent, const YCHAR * name,
//					   __u32 mode, __u32 uid, __u32 gid);
//	yaffs_Object *yaffs_FindObjectByName(yaffs_Object * theDir, const YCHAR * name);
//	int yaffs_ApplyToDirectoryChildren(yaffs_Object * theDir,
//					   int (*fn) (yaffs_Object *));
//
//	yaffs_Object *yaffs_FindObjectByNumber(yaffs_Device * dev, __u32 number);
//
//	/* Link operations */
//	yaffs_Object *yaffs_Link(yaffs_Object * parent, const YCHAR * name,
//				 yaffs_Object * equivalentObject);
//
//	yaffs_Object *yaffs_GetEquivalentObject(yaffs_Object * obj);
//
//	/* Symlink operations */
//	yaffs_Object *yaffs_MknodSymLink(yaffs_Object * parent, const YCHAR * name,
//					 __u32 mode, __u32 uid, __u32 gid,
//					 const YCHAR * alias);
//	YCHAR *yaffs_GetSymlinkAlias(yaffs_Object * obj);
//
//	/* Special inodes (fifos, sockets and devices) */
//	yaffs_Object *yaffs_MknodSpecial(yaffs_Object * parent, const YCHAR * name,
//					 __u32 mode, __u32 uid, __u32 gid, __u32 rdev);
//
//	/* Special directories */
//	yaffs_Object *yaffs_Root(yaffs_Device * dev);
//	yaffs_Object *yaffs_LostNFound(yaffs_Device * dev);
//
//	#ifdef CONFIG_YAFFS_WINCE
//	/* CONFIG_YAFFS_WINCE special stuff */
//	void yfsd_WinFileTimeNow(__u32 target[2]);
//	#endif
//
//	#ifdef __KERNEL__
//
//	void yaffs_HandleDeferedFree(yaffs_Object * obj);
//	#endif
//
//	/* Debug dump  */
//	int yaffs_DumpObject(yaffs_Object * obj);
//
//	void yaffs_GutsTest(yaffs_Device * dev);
//
//	/* A few useful functions */
//	void yaffs_InitialiseTags(yaffs_ExtendedTags * tags);
//	void yaffs_DeleteChunk(yaffs_Device * dev, int chunkId, int markNAND, int lyn);
//	int yaffs_CheckFF(__u8 * buffer, int nBytes);
//	void yaffs_HandleChunkError(yaffs_Device *dev, yaffs_BlockInfo *bi);

}
