package yaffs2.port;


import yaffs2.utils.*;

import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class Dtest_C {
	/*
	 * YAFFS: Yet Another Flash File System. A NAND-flash specific file system.
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License version 2 as
	 * published by the Free Software Foundation.
	 */

	/*
	* Test code for the "direct" interface. 
	*/
	
	//void dumpDir(const char *dname);

	static byte[] xx = new byte[600];//char xx[600];
	static final int xxIndex = 0;


	static void dump_directory_tree_worker(byte[] dname, int dnameIndex, int recursive)
	{
		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();
		byte[] str = new byte[1000]; final int strIndex = 0;
				
		d = yaffsfs_C.yaffs_opendir(dname, dnameIndex);
		
		if(!(d != null))
		{
			Unix.printf("opendir failed\n");
		}
		else
		{
			while((de = yaffsfs_C.yaffs_readdir(d)) != null)
			{
				Unix.sprintf(str, strIndex,"%a/%a",PrimitiveWrapperFactory.get(dname),PrimitiveWrapperFactory.get(dnameIndex),PrimitiveWrapperFactory.get(de.d_name),PrimitiveWrapperFactory.get(de.d_nameIndex));
				
				yaffsfs_C.yaffs_lstat(str, strIndex,s);
				
				Unix.printf("%a inode %d obj %x length %d mode %X ",PrimitiveWrapperFactory.get(str),PrimitiveWrapperFactory.get(strIndex),PrimitiveWrapperFactory.get(s.st_ino),
						PrimitiveWrapperFactory.get(Utils.hashCode(de.d_dont_use)),PrimitiveWrapperFactory.get((int)s.st_size),PrimitiveWrapperFactory.get(s.st_mode));
				switch(s.st_mode & yaffsfs_H.S_IFMT)
				{
					case yaffsfs_H.S_IFREG: Unix.printf("data file"); break;
					case Unix.S_IFDIR: Unix.printf("directory"); break;
					case yaffsfs_H.S_IFLNK: Unix.printf("symlink -->");
								  if(yaffsfs_C.yaffs_readlink(str, strIndex,str, strIndex,100) < 0)
									Unix.printf("no alias");
								  else
									Unix.printf("\"%a\"",PrimitiveWrapperFactory.get(str), PrimitiveWrapperFactory.get(strIndex));    
								  break;
					default: Unix.printf("unknown"); break;
				}
				
				Unix.printf("\n");

				if((s.st_mode & yaffsfs_H.S_IFMT) == Unix.S_IFDIR && recursive != 0)
					dump_directory_tree_worker(str, strIndex,1);
								
			}
			
			yaffsfs_C.yaffs_closedir(d);
		}

	}

	public static void dump_directory_tree(byte[] dname, int dnameIndex)
	{
		dump_directory_tree_worker(dname, dnameIndex,1);
		Unix.printf("\n");
		Unix.printf("Free space in %a is %d\n\n",PrimitiveWrapperFactory.get(dname),PrimitiveWrapperFactory.get(dnameIndex),PrimitiveWrapperFactory.get((int)yaffsfs_C.yaffs_freespace(dname, dnameIndex)));
	}



	static boolean early_exit;

	static byte[] _STATIC_LOCAL_small_overwrite_test_xx = new byte[8000];
	
	public static void small_overwrite_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		
		int i;
		int j;

		int h0;
		int h1;
		int len0;
		int len1;
		int nread;
		
		Unix.sprintf(a,aIndex,"%s/a",PrimitiveWrapperFactory.get(mountpt));

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		
		
		for(i = 0; i < nmounts; i++){
			
//			static char xx[8000];
			
			Unix.printf("############### Iteration %d   Start\n",PrimitiveWrapperFactory.get(i));
			if(true)
				yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt), 0);

			dump_directory_tree(Utils.StringToByteArray(mountpt), 0);
			
			yaffsfs_C.yaffs_mkdir(a, aIndex,0);
			
			Unix.sprintf(_STATIC_LOCAL_small_overwrite_test_xx,0,"%a/0",PrimitiveWrapperFactory.get(a),PrimitiveWrapperFactory.get(aIndex));
			h0 = yaffsfs_C.yaffs_open(_STATIC_LOCAL_small_overwrite_test_xx,0, yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			Unix.sprintf(_STATIC_LOCAL_small_overwrite_test_xx,0,"%a/1",PrimitiveWrapperFactory.get(a),PrimitiveWrapperFactory.get(aIndex));
			h1 = yaffsfs_C.yaffs_open(_STATIC_LOCAL_small_overwrite_test_xx,0, yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
			for(j = 0; j < 1000000; j+=1000){
				yaffsfs_C.yaffs_truncate(h0,j);
				yaffsfs_C.yaffs_lseek(h0,j,yaffsfs_H.SEEK_SET);
				yaffsfs_C.yaffs_write(h0,_STATIC_LOCAL_small_overwrite_test_xx,0,7000);
				yaffsfs_C.yaffs_write(h1,_STATIC_LOCAL_small_overwrite_test_xx,0,7000);
				
				if(early_exit)
					System.exit(0);
			}
			
			yaffsfs_C.yaffs_close(h0);
			
			Unix.printf("########### %d\n",PrimitiveWrapperFactory.get(i));
			dump_directory_tree(Utils.StringToByteArray(mountpt), 0);

			if(true)
				yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt), 0);
		}
	}



}
