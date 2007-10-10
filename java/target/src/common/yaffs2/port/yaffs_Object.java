package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_Object implements list_head_or_yaffs_Object
{
	public class yaffs_Object_Sub
	{
		public boolean deleted;		/* This should only apply to unlinked files. */
		public boolean softDeleted;	/* it has also been soft deleted */
		public boolean unlinked;	/* An unlinked file. The file should be in the unlinked directory.*/
		public boolean fake;		/* A fake object has no presence on NAND. */		
	}
	
	public yaffs_Object_Sub sub = new yaffs_Object_Sub();
	
	//struct yaffs_ObjectStruct {
		public boolean renameAllowed;	/* Some objects are not allowed to be renamed. */
		public boolean unlinkAllowed;
		public boolean dirty;		/* the object needs to be written to flash */
		public boolean valid;		/* When the file system is being loaded up, this 
					 * object might be created before the data
					 * is available (ie. file data records appear before the header).
					 */
		public boolean lazyLoaded;	/* This object has been lazy loaded and is missing some detail */

		public boolean deferedFree;	/* For Linux kernel. Object is removed from NAND, but is
					 * still in the inode cache. Free of object is defered.
					 * until the inode is released.
					 */

		/**__u8*/ public byte serial;		/* serial number of chunk in NAND. Cached here */
		/**__u16*/ public short sum;		/* sum of the name to speed searching */

		public yaffs_Device myDev;	/* The device I'm on */

		public list_head hashLink = new list_head(this);	/* list of objects in this hash bucket */

		public list_head hardLinks = new list_head(this);	/* all the equivalent hard linked objects */

		/* directory structure stuff */
		/* also used for linking up the free list */
		public yaffs_Object parent; 
		public list_head siblings = new list_head(this);

		/* Where's my object header in NAND? */
		public int chunkId;		

		public int nDataChunks;	/* Number of data chunks attached to the file. */

		/**__u32*/ public int objectId;		/* the object id value */

		/**__u32*/ public int yst_mode;

	//#ifdef CONFIG_YAFFS_SHORT_NAMES_IN_RAM
		public byte[] shortName = new byte[Guts_H.YAFFS_SHORT_NAME_LENGTH + 1];
		public final static int shortNameIndex = 0;
	//#endif

	//#ifndef __KERNEL__
		/**__u32*/ public int inUse;
	//#endif

	/*#ifdef CONFIG_YAFFS_WINCE
		__u32 win_ctime[2];
		__u32 win_mtime[2];
		__u32 win_atime[2];
	#else*/
		/**__u32*/ public int yst_uid;
		/**__u32*/ public int yst_gid;
		/**__u32*/ public int yst_atime;
		/**__u32*/ public int yst_mtime;
		/**__u32*/ public int yst_ctime;
	/*#endif*/

		/**__u32*/ public int yst_rdev;

	/*#ifdef __KERNEL__
		struct inode *myInode;

	#endif*/

		/**yaffs_ObjectType*/ public int variantType;

		public yaffs_ObjectVariant variant = new yaffs_ObjectVariant();

	//};

	//typedef struct yaffs_ObjectStruct yaffs_Object;
}
