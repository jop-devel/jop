package yaffs2.port;

public class yaffs_stat
{
//	struct yaffs_stat{
		public int		      st_dev;      /* device */
	    public int           st_ino;      /* inode */
	    /*mode_t*/ public int        st_mode;     /* protection */	// XXX at least it is compiled as int under cygwin
	    public int           st_nlink;    /* number of hard links */
	    public int           st_uid;      /* user ID of owner */
	    public int           st_gid;      /* group ID of owner */
	    /*unsigned*/ public int      st_rdev;     /* device type (if inode device) */
	    /*off_t*/ public int         st_size;     /* total size, in bytes */
	    /*unsigned long*/ public int st_blksize;  /* blocksize for filesystem I/O */
	    /*unsigned long*/ public int st_blocks;   /* number of blocks allocated */
	    /*unsigned long*/ public int yst_atime;    /* time of last access */
	    /*unsigned long*/ public int yst_mtime;    /* time of last modification */
	    /*unsigned long*/ public int yst_ctime;    /* time of last change */
//	};
}
