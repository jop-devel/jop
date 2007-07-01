package yaffs2.port;

public class yaffs_stat
{
//	struct yaffs_stat{
	    int		      st_dev;      /* device */
	    int           st_ino;      /* inode */
	    /*mode_t*/ int        st_mode;     /* protection */	// XXX at least it is compiled as int under cygwin
	    int           st_nlink;    /* number of hard links */
	    int           st_uid;      /* user ID of owner */
	    int           st_gid;      /* group ID of owner */
	    /*unsigned*/ int      st_rdev;     /* device type (if inode device) */
	    /*off_t*/ int         st_size;     /* total size, in bytes */
	    /*unsigned long*/ int st_blksize;  /* blocksize for filesystem I/O */
	    /*unsigned long*/ int st_blocks;   /* number of blocks allocated */
	    /*unsigned long*/ int yst_atime;    /* time of last access */
	    /*unsigned long*/ int yst_mtime;    /* time of last modification */
	    /*unsigned long*/ int yst_ctime;    /* time of last change */
//	};
}
