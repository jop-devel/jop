package yaffs2.port;

public class yaffs_dirent
{
//	struct yaffs_dirent{
	public int d_ino;                 /* inode number */
	/**off_t*/ public int d_off;                /* offset to this dirent */
//	/**unsigned short*/ public short d_reclen;    /* length of this d_name */ // PORT ???
	public byte[] d_name = new byte[yaffsfs_H.NAME_MAX+1];   /* file name (null-terminated) */
	public int d_nameIndex = 0;
	/** unsigned PORT it is a pointer */ public Object d_dont_use;	/* debug pointer, not for public consumption */
//	};

//	typedef struct yaffs_dirent yaffs_dirent;
}
