package yaffs2.port;

public class yaffsfs_H
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
	 * Header file for using yaffs in an application via
	 * a direct interface.
	 */


//	#ifndef __YAFFSFS_H__
//	#define __YAFFSFS_H__
//
//	#include "yaffscfg.h"
//	#include "yportenv.h"


//	typedef long off_t;
//	typedef long dev_t;
//	typedef unsigned long mode_t;


//	#ifndef NAME_MAX
	static final int NAME_MAX = 	256;
//	#endif

//	#ifndef O_RDONLY
	public static final int O_RDONLY = 	00;
//	#endif

//	#ifndef O_WRONLY
	public static final int O_WRONLY = 	01;
//	#endif

//	#ifndef O_RDWR
	public static final int O_RDWR = 		02;
//	#endif

//	#ifndef O_CREAT		
	public static final int O_CREAT =  	0100;	// XXX check values
//	#endif

//	#ifndef O_EXCL
	public static final int O_EXCL = 		0200;
//	#endif

//	#ifndef O_TRUNC
	public static final int O_TRUNC = 		01000;
//	#endif

//	#ifndef O_APPEND
	public static final int O_APPEND = 	02000;
//	#endif

//	#ifndef SEEK_SET
	public static final int SEEK_SET = 	0;
//	#endif

//	#ifndef SEEK_CUR
	public static final int SEEK_CUR = 	1;
//	#endif

//	#ifndef SEEK_END
	public static final int SEEK_END = 	2;
//	#endif

//	#ifndef EBUSY
	public static final int EBUSY = 	16;
//	#endif

//	#ifndef ENODEV
	public static final int ENODEV = 	19;
//	#endif

//	#ifndef EINVAL
	public static final int EINVAL = 	22;
//	#endif

//	#ifndef EBADF
	public static final int EBADF = 	9;
//	#endif

//	#ifndef EACCESS
	public static final int EACCESS = 	13;
//	#endif

//	#ifndef EXDEV	
	public static final int EXDEV = 	18;
//	#endif

//	#ifndef ENOENT
	public static final int ENOENT = 	2;
//	#endif

//	#ifndef ENOSPC
	public static final int ENOSPC = 	28;
//	#endif

//	#ifndef ENOTEMPTY
	public static final int ENOTEMPTY =  39;
//	#endif

//	#ifndef ENOMEM
	public static final int ENOMEM =  12;
//	#endif

//	#ifndef EEXIST
	public static final int EEXIST =  17;
//	#endif

//	#ifndef ENOTDIR
	public static final int ENOTDIR =  20;
//	#endif

//	#ifndef EISDIR
	public static final int EISDIR =  21;
//	#endif


//	 Mode flags

//	#ifndef S_IFMT
	public static final int S_IFMT = 		0170000;
//	#endif

//	#ifndef S_IFLNK
	public static final int S_IFLNK = 		0120000;
//	#endif

//	#ifndef S_IFDIR
//	public static final int S_IFDIR = 		0040000; 
//	#endif

//	#ifndef S_IFREG
	public static final int S_IFREG = 		0100000;
//	#endif

//	#ifndef S_IREAD 
	public static final int S_IREAD = 		0000400;
//	#endif

//	#ifndef S_IWRITE
	public static final int S_IWRITE =		0000200;
//	#endif




//	struct yaffs_dirent{
//	    long d_ino;                 /* inode number */
//	    off_t d_off;                /* offset to this dirent */
//	    unsigned short d_reclen;    /* length of this d_name */
//	    char d_name [NAME_MAX+1];   /* file name (null-terminated) */
//	    unsigned d_dont_use;	/* debug pointer, not for public consumption */
//	};
//
//	typedef struct yaffs_dirent yaffs_dirent;


//	typedef struct __opaque yaffs_DIR;



//	struct yaffs_stat{
//	    int		      st_dev;      /* device */
//	    int           st_ino;      /* inode */
//	    mode_t        st_mode;     /* protection */
//	    int           st_nlink;    /* number of hard links */
//	    int           st_uid;      /* user ID of owner */
//	    int           st_gid;      /* group ID of owner */
//	    unsigned      st_rdev;     /* device type (if inode device) */
//	    off_t         st_size;     /* total size, in bytes */
//	    unsigned long st_blksize;  /* blocksize for filesystem I/O */
//	    unsigned long st_blocks;   /* number of blocks allocated */
//	    unsigned long yst_atime;    /* time of last access */
//	    unsigned long yst_mtime;    /* time of last modification */
//	    unsigned long yst_ctime;    /* time of last change */
//	};

//	int yaffs_open(const char *path, int oflag, int mode) ;
//	int yaffs_read(int fd, void *buf, unsigned int nbyte) ;
//	int yaffs_write(int fd, const void *buf, unsigned int nbyte) ;
//	int yaffs_close(int fd) ;
//	off_t yaffs_lseek(int fd, off_t offset, int whence) ;
//	int yaffs_truncate(int fd, off_t newSize);
//
//	int yaffs_unlink(const char *path) ;
//	int yaffs_rename(const char *oldPath, const char *newPath) ;
//
//	int yaffs_stat(const char *path, struct yaffs_stat *buf) ;
//	int yaffs_lstat(const char *path, struct yaffs_stat *buf) ;
//	int yaffs_fstat(int fd, struct yaffs_stat *buf) ;
//
//	int yaffs_chmod(const char *path, mode_t mode); 
//	int yaffs_fchmod(int fd, mode_t mode); 
//
//	int yaffs_mkdir(const char *path, mode_t mode) ;
//	int yaffs_rmdir(const char *path) ;
//
//	yaffs_DIR *yaffs_opendir(const char *dirname) ;
//	struct yaffs_dirent *yaffs_readdir(yaffs_DIR *dirp) ;
//	void yaffs_rewinddir(yaffs_DIR *dirp) ;
//	int yaffs_closedir(yaffs_DIR *dirp) ;
//
//	int yaffs_mount(const char *path) ;
//	int yaffs_unmount(const char *path) ;
//
//	int yaffs_symlink(const char *oldpath, const char *newpath); 
//	int yaffs_readlink(const char *path, char *buf, int bufsiz); 
//
//	int yaffs_link(const char *oldpath, const char *newpath); 
//	int yaffs_mknod(const char *pathname, mode_t mode, dev_t dev);
//
//	loff_t yaffs_freespace(const char *path);
//
//	void yaffs_initialise(yaffsfs_DeviceConfiguration *configList);
//
//	int yaffs_StartUp(void);

//	#endif
}
