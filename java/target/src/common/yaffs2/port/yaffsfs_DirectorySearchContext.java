package yaffs2.port;

public class yaffsfs_DirectorySearchContext implements yaffs_DIR {
	
//	typedef struct
//	{
		/**__u32*/ public int magic;
		public yaffs_dirent de = new yaffs_dirent();		/* directory entry being used by this dsc */
		public byte[] name = new byte[yaffsfs_H.NAME_MAX+1];		/* name of directory being searched */
		public int nameIndex = 0;
		public yaffs_Object dirObj;		/* ptr to directory being searched */
		public yaffs_Object nextReturn;	/* obj to be returned by next readddir */
		public int offset;
		public list_head others = new list_head(this);	
//	} yaffsfs_DirectorySearchContext;
	
}
