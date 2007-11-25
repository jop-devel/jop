package yaffs2.port;

import yaffs2.utils.Constants;
import yaffs2.utils.EmulationUtils;
import yaffs2.utils.Unix;
import yaffs2.utils.Utils;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class Dtest_C_Additional extends Dtest_C {
	static void make_a_file(byte[] yaffsName,int yaffsNameIndex,byte bval,int sizeOfFile)
	{
		int outh;
		int i;
		//unsigned char buffer[100];
		byte[] buffer = new byte[100]; final int bufferIndex = 0;

		outh = yaffsfs_C.yaffs_open(yaffsName, yaffsNameIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		Unix.memset(buffer,bufferIndex,bval,100);
		
		do{
			i = sizeOfFile;
			if(i > 100) i = 100;
			sizeOfFile -= i;
			
			yaffsfs_C.yaffs_write(outh,buffer,bufferIndex,i);
			
		} while (sizeOfFile > 0);
		
			
		yaffsfs_C.yaffs_close(outh);

	}



	protected static boolean check_pattern_file(byte[] fn, int fnIndex)
	{
		int h;
		byte[] marker = new byte[Constants.SIZEOF_INT];
		int i;
		int size;
		boolean ok = true;
		
		h = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_RDWR,0);
		size = yaffsfs_C.yaffs_lseek(h,0,yaffsfs_H.SEEK_END);
			
		for(i = 0; i < size; i+=256)
		{
			yaffsfs_C.yaffs_lseek(h,i,yaffsfs_H.SEEK_SET);
			yaffsfs_C.yaffs_read(h,marker,0,Constants.SIZEOF_INT);
			ok = (Utils.getIntFromByteArray(marker,0) == ~i);
			if(!ok)
			{
			   Unix.printf("pattern check failed on file %a, size %d at position %d. Got %x instead of %x\n",
					   PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex),PrimitiveWrapperFactory.get(size),PrimitiveWrapperFactory.get(i),PrimitiveWrapperFactory.get(Utils.getIntFromByteArray(marker,0)),PrimitiveWrapperFactory.get(~i));
			}
		}
		yaffsfs_C.yaffs_close(h);
		return ok;
	}





	protected static boolean dump_file_data(byte[] fn, int fnIndex)
	{
		int h;
		int marker;
		int i = 0;
		int size;
		boolean ok = true;
		/*unsigned*/ byte[] b = new byte[1];
		
		h = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_RDWR,0);
					
		Unix.printf("%a\n",PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex));
		while(yaffsfs_C.yaffs_read(h,b,0,1)> 0)
		{
			Unix.printf("%02y",PrimitiveWrapperFactory.get(b));
			i++;
			if(i > 32) 
			{
			   Unix.printf("\n");
			   i = 0;;
			 }
		}
		Unix.printf("\n");
		yaffsfs_C.yaffs_close(h);
		return ok;
	}



	static void dump_file(byte[] fn, int fnIndex)
	{
		int i;
		int size;
		int h;
		
		h = yaffsfs_C.yaffs_open(fn,fnIndex,yaffsfs_H.O_RDONLY,0);
		if(h < 0)
		{
			Unix.printf("*****\nDump file %a does not exist\n",PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex));
		}
		else
		{
			size = yaffsfs_C.yaffs_lseek(h,0,yaffsfs_H.SEEK_SET);
			Unix.printf("*****\nDump file %a size %d\n",PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex),PrimitiveWrapperFactory.get(size));
			for(i = 0; i < size; i++)
			{
				
			}
		}
	}

	static void create_file_of_size(byte[] fn,int fnIndex,int syze)
	{
		int h;
		int n;
		
		//char xx[200];
		byte[] xx = new byte[200]; final int xxIndex = 0; 
		
		int iterations = (syze + Unix.strlen(fn,fnIndex) -1)/ Unix.strlen(fn,fnIndex);	// BUG FOUND
		//int iterations = (syze + yaffs2.utils.FileNameLength.fnLength(fn, fnIndex) -1)/ yaffs2.utils.FileNameLength.fnLength(fn, fnIndex);
		
		h = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
		while (iterations > 0)
		{
			Unix.sprintf(xx,xxIndex,"%a %8d",PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex),PrimitiveWrapperFactory.get(iterations));
			yaffsfs_C.yaffs_write(h,xx,xxIndex,Unix.strlen(xx, xxIndex));
			iterations--;
		}
		yaffsfs_C.yaffs_close (h);
	}

	static void verify_file_of_size(byte[] fn,int fnIndex,int syze)
	{
		int h;
		int n;
		
		byte[] xx = new byte[200]; final int xxIndex = 0;
		byte[] yy = new byte[200]; final int yyIndex = 0;
		int l;
		
		int iterations = (syze + Unix.strlen(fn,fnIndex) -1)/ Unix.strlen(fn,fnIndex);
		
		h = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_RDONLY, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
		while (iterations > 0)
		{
			Unix.sprintf(xx,xxIndex,"%a %8d",PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex),PrimitiveWrapperFactory.get(iterations));
			l = Unix.strlen(xx,xxIndex);
			
			yaffsfs_C.yaffs_read(h,yy,xxIndex,l);
			yy[l] = 0;
			
			if(Unix.strcmp(xx,xxIndex,yy,yyIndex) != 0){
				Unix.printf("=====>>>>> verification of file %a failed near position %d\n",PrimitiveWrapperFactory.get(fn),PrimitiveWrapperFactory.get(fnIndex),PrimitiveWrapperFactory.get(yaffsfs_C.yaffs_lseek(h,0,yaffsfs_H.SEEK_CUR)));
			}
			iterations--;
		}
		yaffsfs_C.yaffs_close (h);
	}

	static void create_resized_file_of_size(/*const char **/ byte[] fn,int fnIndex,
			int syze1,int reSyze, int syze2)
	{
		int h;
		int n;
		
		
		int iterations;
		
		h = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
		iterations = (syze1 + Unix.strlen(fn,fnIndex) -1)/ Unix.strlen(fn,fnIndex);
		while (iterations > 0)
		{
			yaffsfs_C.yaffs_write(h,fn,fnIndex,Unix.strlen(fn,fnIndex));
			iterations--;
		}
		
		yaffsfs_C.yaffs_truncate(h,reSyze);
		
		yaffsfs_C.yaffs_lseek(h,0,yaffsfs_H.SEEK_SET);
		iterations = (syze2 + Unix.strlen(fn,fnIndex) -1)/ Unix.strlen(fn,fnIndex);
		while (iterations > 0)
		{
			yaffsfs_C.yaffs_write(h,fn,fnIndex,Unix.strlen(fn,fnIndex));
			iterations--;
		}
		
		yaffsfs_C.yaffs_close (h);
	}


	static void do_some_file_stuff(byte[] path, int pathIndex)
	{

		byte[] fn = new byte[100]; final int fnIndex = 0;

		Unix.sprintf(fn,fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("f1"));
		create_file_of_size(fn,fnIndex,10000);

		Unix.sprintf(fn,fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("fdel"));
		create_file_of_size(fn,fnIndex,10000);
		yaffsfs_C.yaffs_unlink(fn,fnIndex);

		Unix.sprintf(fn,fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("f2"));
		
		create_resized_file_of_size(fn,fnIndex,10000,3000,4000);
	}

	static void yaffs_backward_scan_test(byte[] path, int pathIndex)
	{
		byte[] fn = new byte[100]; final int fnIndex = 0;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();	
		
		yaffsfs_C.yaffs_mount(path,pathIndex);
		
		do_some_file_stuff(path,pathIndex);
		
		Unix.sprintf(fn,fnIndex,"%a/ddd",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex));
		
		yaffsfs_C.yaffs_mkdir(fn,fnIndex,0);
		
		do_some_file_stuff(fn,fnIndex);
		
		yaffsfs_C.yaffs_unmount(path,pathIndex);
		
		yaffsfs_C.yaffs_mount(path,pathIndex);
	}

	static byte[] xxzz = new byte[2000]; static int xxzzIndex = 0;


	static void yaffs_device_flush_test(byte[] path, int pathIndex)
	{
		byte[] fn = new byte[100]; int fnIndex = 0;
		int h;
		int i;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();	
		
		yaffsfs_C.yaffs_mount(path, pathIndex);
		
		do_some_file_stuff(path, pathIndex);
		
		// Open and add some data to a few files
		for(i = 0; i < 10; i++) {
		
			Unix.sprintf(fn,fnIndex,"%a/ff%d",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get(i));

			h = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IWRITE | yaffsfs_H.S_IREAD);
			yaffsfs_C.yaffs_write(h,xxzz,xxzzIndex,2000);
			yaffsfs_C.yaffs_write(h,xxzz,xxzzIndex,2000);
		}
		yaffsfs_C.yaffs_unmount(path,pathIndex);
		
		yaffsfs_C.yaffs_mount(path,pathIndex);
	}



	static void short_scan_test(byte[] path, int pathIndex, int fsize, int niterations)
	{
		int i;
		byte[] fn = new byte[100]; final int fnIndex = 0;
		
		Unix.sprintf(fn,fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("f1"));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		for(i = 0; i < niterations; i++)
		{
			Unix.printf("\n*****************\nIteration %d\n",PrimitiveWrapperFactory.get(i));
			yaffsfs_C.yaffs_mount(path,pathIndex);
			Unix.printf("\nmount: Directory look-up of %a\n",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex));
			dumpDir(path, pathIndex);
			make_a_file(fn, fnIndex,(byte)1,fsize);
			yaffsfs_C.yaffs_unmount(path, pathIndex);
		}
	}




	static void fill_disk(byte[] path, int pathIndex, int nfiles)
	{
		int h;
		int n;
		int result;
		int f;
		
		byte[] str = new byte[50]; final int strIndex = 0;
		
		for(n = 0; n < nfiles; n++)
		{
			Unix.sprintf(str,strIndex,"%a/%d",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get(n));
			
			h = yaffsfs_C.yaffs_open(str, strIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
			Unix.printf("writing file %a handle %d ",PrimitiveWrapperFactory.get(str),PrimitiveWrapperFactory.get(strIndex),PrimitiveWrapperFactory.get(h));
			
			while ((result = yaffsfs_C.yaffs_write(h,xx,xxIndex,600)) == 600)
			{
				f = yaffsfs_C.yaffs_freespace(path, pathIndex);
			}
			result = yaffsfs_C.yaffs_close(h);
			Unix.printf(" close %d\n",PrimitiveWrapperFactory.get(result));
		}
	}

	protected static void fill_disk_and_delete(byte[] path, int pathIndex, int nfiles, int ncycles)
	{
		int i,j;
		byte[] str = new byte[50]; final int strIndex = 0;
		int result;
		
		for(i = 0; i < ncycles; i++)
		{
			Unix.printf("@@@@@@@@@@@@@@ cycle %d\n",PrimitiveWrapperFactory.get(i));
			fill_disk(path,pathIndex,nfiles);
			
			for(j = 0; j < nfiles; j++)
			{
				Unix.sprintf(str,strIndex,"%a/%d",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get(j));
				result = yaffsfs_C.yaffs_unlink(str,strIndex);
				Unix.printf("unlinking file %a, result %d\n",PrimitiveWrapperFactory.get(str),PrimitiveWrapperFactory.get(strIndex),PrimitiveWrapperFactory.get(result));
			}
		}
	}


	protected static void fill_files(byte[] path, int pathIndex, int flags, int maxIterations, int siz)
	{
		int i;
		int j;
		byte[] str = new byte[50]; final int strIndex = 0;
		int h;
		
		i = 0;
		
		do{
			Unix.sprintf(str,strIndex,"%a/%d",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get(i));
			h = yaffsfs_C.yaffs_open(str, strIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			yaffsfs_C.yaffs_close(h);

			if(h >= 0)
			{
				for(j = 0; j < siz; j++)
				{
					yaffsfs_C.yaffs_write(h,str,strIndex,1);
				}
			}
			
			if((flags & 1) != 0)
			{
				yaffsfs_C.yaffs_unlink(str, strIndex);
			}
			i++;
		} while(h >= 0 && i < maxIterations);
		
		if((flags & 2) != 0)
		{
			i = 0;
			do{
				Unix.sprintf(str, strIndex, "%a/%d", PrimitiveWrapperFactory.get(path), PrimitiveWrapperFactory.get(pathIndex), PrimitiveWrapperFactory.get(i));
				Unix.printf("unlink %a\n",PrimitiveWrapperFactory.get(str),PrimitiveWrapperFactory.get(strIndex));
				i++;
			} while(yaffsfs_C.yaffs_unlink(str,strIndex) >= 0);
		}
	}

	static void leave_unlinked_file(byte[] path, int pathIndex,int maxIterations,int siz)
	{
		int i;
		byte[] str = new byte[50]; final int strIndex = 0;
		int h;
		
		i = 0;
		
		do{
			Unix.sprintf(str, strIndex,"%a/%d",PrimitiveWrapperFactory.get(path), PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get(i));
			Unix.printf("create %a\n",PrimitiveWrapperFactory.get(str), PrimitiveWrapperFactory.get(strIndex));
			h = yaffsfs_C.yaffs_open(str, strIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			if(h >= 0)
			{
				yaffsfs_C.yaffs_unlink(str, strIndex);
			}
			i++;
		} while(h < 0 && i < maxIterations);
		
		if(h >= 0)
		{
			for(i = 0; i < siz; i++)
			{
				yaffsfs_C.yaffs_write(h,str, strIndex,1);
			}
		}
		
		Unix.printf("Leaving file %a open\n",PrimitiveWrapperFactory.get(str), PrimitiveWrapperFactory.get(strIndex));

	}

	protected static void dumpDirFollow(byte[] dname, int dnameIndex)
	{
		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();
		byte[] str = new byte[100]; final int strIndex = 0;
				
		d = yaffsfs_C.yaffs_opendir(dname, dnameIndex);
		
		if(!(d != null))
		{
			Unix.printf("opendir failed\n");
		}
		else
		{
			while((de = yaffsfs_C.yaffs_readdir(d)) != null)
			{
				Unix.sprintf(str, strIndex,"%a/%a",PrimitiveWrapperFactory.get(dname), PrimitiveWrapperFactory.get(dnameIndex),PrimitiveWrapperFactory.get(de.d_name),PrimitiveWrapperFactory.get(de.d_nameIndex));
				
				yaffsfs_C.yaffs_stat(str, strIndex, s);
				
				Unix.printf("%a length %d mode %X ",PrimitiveWrapperFactory.get(de.d_name),PrimitiveWrapperFactory.get(de.d_nameIndex),PrimitiveWrapperFactory.get((int)s.st_size),PrimitiveWrapperFactory.get(s.st_mode));
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
			}
			
			yaffsfs_C.yaffs_closedir(d);
		}
		Unix.printf("\n");
		
		Unix.printf("Free space in %a is %d\n\n",PrimitiveWrapperFactory.get(dname),PrimitiveWrapperFactory.get(dnameIndex),PrimitiveWrapperFactory.get((int)yaffsfs_C.yaffs_freespace(dname, dnameIndex)));

	}



	protected static void dumpDir(byte[] dname, int dnameIndex)
	{	dump_directory_tree_worker(dname, dnameIndex,0);
		Unix.printf("\n");
		Unix.printf("Free space in %a is %d\n\n",PrimitiveWrapperFactory.get(dname), PrimitiveWrapperFactory.get(dnameIndex),PrimitiveWrapperFactory.get((int)yaffsfs_C.yaffs_freespace(dname, dnameIndex)));
	}


	protected static void PermissionsCheck(byte[] path, int pathIndex, /*mode_t*/ int tmode, int tflags,int expectedResult)
	{
		int fd;
		
		if(yaffsfs_C.yaffs_chmod(path, pathIndex,tmode)< 0) Unix.printf("chmod failed\n");
		
		fd = yaffsfs_C.yaffs_open(path, pathIndex,tflags,0);
		
		if((fd >= 0) != (expectedResult > 0))
		{
			Unix.printf("Permissions check %x %x %d failed\n",PrimitiveWrapperFactory.get(tmode),PrimitiveWrapperFactory.get(tflags),PrimitiveWrapperFactory.get(expectedResult));
		}
		else
		{
			Unix.printf("Permissions check %x %x %d OK\n",PrimitiveWrapperFactory.get(tmode),PrimitiveWrapperFactory.get(tflags),PrimitiveWrapperFactory.get(expectedResult));
		}
		
		
		yaffsfs_C.yaffs_close(fd);
		
		
	}


	

	public static int huge_directory_test_on_path(String path)
	{

		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();

		int f;
		int i;
		int r;
		int total = 0;
		int lastTotal = 0;
		byte[] buffer = new byte[20]; final int bufferIndex = 0;
		
		byte[] str = new byte[100]; final int strIndex = 0;
		byte[] name = new byte[100]; final int nameIndex = 0;
		byte[] name2 = new byte[100]; final int name2Index = 0;
		
		int h;
		int temp_mode;
		yaffs_stat ystat;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(path),0);
		
		// Create a large number of files
		
		for(i = 0; i < 2000; i++)
		{
		  Unix.sprintf(str,strIndex,"%s/%d",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(i));
		  
		   f = yaffsfs_C.yaffs_open(str,strIndex,yaffsfs_H.O_CREAT,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		   yaffsfs_C.yaffs_close(f);
		}
		
		
		
		d = yaffsfs_C.yaffs_opendir(Utils.StringToByteArray(path),0);
		i = 0;
		if (d != null) {
		while((de = yaffsfs_C.yaffs_readdir(d)) != null) {
		if (total >lastTotal+100*9*1024||(i & 1023)==0){
		Unix.printf("files = %d, total = %d\n",PrimitiveWrapperFactory.get(i), PrimitiveWrapperFactory.get(total));
		lastTotal = total;
		}
			i++;
			Unix.sprintf(str,strIndex,"%s/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(de.d_name,de.d_nameIndex)));
			yaffsfs_C.yaffs_lstat(str,strIndex,s);
			switch(s.st_mode & yaffsfs_H.S_IFMT){
			case yaffsfs_H.S_IFREG:
		//Unix.printf("data file");
		total += s.st_size;
		break;
		}
		}
		
		yaffsfs_C.yaffs_closedir(d);
		}
		
		return 0;
	}

//	static int yaffs_scan_test(const char *path)
//	{
//	}
//
//
	public static void rename_over_test(String mountpt)
	{
		int i;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		
		Unix.sprintf(a,aIndex,"%s/a",PrimitiveWrapperFactory.get(mountpt));
		Unix.sprintf(b,bIndex,"%s/b",PrimitiveWrapperFactory.get(mountpt));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		i = yaffsfs_C.yaffs_open(a,aIndex,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, 0); 
		yaffsfs_C.yaffs_close(i);
		i = yaffsfs_C.yaffs_open(b,bIndex,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, 0);
		yaffsfs_C.yaffs_close(i);
		yaffsfs_C.yaffs_rename(a,aIndex,b,bIndex); // rename over
		yaffsfs_C.yaffs_rename(b,bIndex,a,aIndex); // rename back again (not renaimng over)
		yaffsfs_C.yaffs_rename(a,aIndex,b,bIndex); // rename back again (not renaimng over)
		
		
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		
	}
	
	public static int resize_stress_test(String path)
	{
	   int a,b,i,j;
	   int x;
	   int r;
	   byte[] aname = new byte[100]; final int anameIndex = 0;
	   byte[] bname = new byte[100]; final int bnameIndex = 0;
	   
	   byte[] abuffer = new byte[1000]; final int abufferIndex = 0;
	   byte[] bbuffer = new byte[1000]; final int bbufferIndex = 0;
	   
	   yaffs2.utils.Globals.configuration.yaffs_StartUp();
	   
	   yaffsfs_C.yaffs_mount(Utils.StringToByteArray(path),0);
	   
	   Unix.sprintf(aname,anameIndex,"%s%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get("/a"));
	   Unix.sprintf(bname,bnameIndex,"%s%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get("/b"));
	   
	   Unix.memset(abuffer,abufferIndex,(byte)'a',1000);
	   Unix.memset(bbuffer,bbufferIndex,(byte)'b',1000);
	   
	   a = yaffsfs_C.yaffs_open(aname, anameIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
	   b = yaffsfs_C.yaffs_open(bname, bnameIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
	   
	   Unix.printf(" %s %d %s %d\n",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(aname,0)),PrimitiveWrapperFactory.get(a),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(bname,0)),PrimitiveWrapperFactory.get(b));
	  
	   x = 0;
	   
	   for(j = 0; j < 100; j++)
	   {
			yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);

			
			for(i = 0; i <20000; i++)
			{
			   //r =        yaffsfs_C.yaffs_lseek(b,i,yaffsfs_H.SEEK_SET);
				//r = yaffsfs_C.yaffs_write(b,bbuffer,1000);
				
				if((x & 0x16) != 0)
				{
					// shrink
					int syz = yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
					
					syz -= 500;
					if(syz < 0) syz = 0;
					yaffsfs_C.yaffs_truncate(a,syz);
					
				}
				else
				{
					//expand
					r = yaffsfs_C.yaffs_lseek(a,i * 500,yaffsfs_H.SEEK_SET);
					r = yaffsfs_C.yaffs_write(a,abuffer,abufferIndex,1000);
				}
				x++;
				
			}
	   }
	   
	   return 0;
	   
	}

	
	public static int resize_stress_test_no_grow_complex(String path,int iters)
	{
	   int a,b,i,j;
	   int x;
	   int r;
	   byte[] aname = new byte[100]; final int anameIndex = 0;
	   byte[] bname = new byte[100]; final int bnameIndex = 0;
	   
	   byte[] abuffer = new byte[1000]; final int abufferIndex = 0;
	   byte[] bbuffer = new byte[1000]; final int bbufferIndex = 0;
	   
	   yaffs2.utils.Globals.configuration.yaffs_StartUp();
	   
	   yaffsfs_C.yaffs_mount(Utils.StringToByteArray(path),0);
	   
	   Unix.sprintf(aname,anameIndex,"%s%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get("/a"));
	   Unix.sprintf(bname,bnameIndex,"%s%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get("/b"));
	   
	   Unix.memset(abuffer,abufferIndex,(byte)'a',1000);
	   Unix.memset(bbuffer,bbufferIndex,(byte)'b',1000);
	   
	   a = yaffsfs_C.yaffs_open(aname, anameIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
	   b = yaffsfs_C.yaffs_open(bname, bnameIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
	   
	   Unix.printf(" %s %d %s %d\n",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(aname,anameIndex)),PrimitiveWrapperFactory.get(a),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(bname,bnameIndex)),PrimitiveWrapperFactory.get(b));
	  
	   x = 0;
	   
	   for(j = 0; j < iters; j++)
	   {
			yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);

			
			for(i = 0; i <20000; i++)
			{
			   //r =        yaffsfs_C.yaffs_lseek(b,i,yaffsfs_H.SEEK_SET);
				//r = yaffsfs_C.yaffs_write(b,bbuffer,1000);
				
				if(!(x%20 != 0))
				{
					// shrink
					int syz = yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
					
					while(syz > 4000)
					{
					
						syz -= 2050;
						if(syz < 0) syz = 0;
						yaffsfs_C.yaffs_truncate(a,syz);
						syz = yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
						Unix.printf("shrink to %d\n",PrimitiveWrapperFactory.get(syz));
					}
					
					
				}
				else
				{
					//expand
					r = yaffsfs_C.yaffs_lseek(a,500,yaffsfs_H.SEEK_END);
					r = yaffsfs_C.yaffs_write(a,abuffer,abufferIndex,1000);
				}
				x++;
				
						
			}
			Unix.printf("file size is %d\n",PrimitiveWrapperFactory.get(yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END)));

	   }
	   
	   return 0;
	   
	}
//
//	static int resize_stress_test_no_grow(const char *path,int iters)
//	{
//	   int a,b,i,j;
//	   int x;
//	   int r;
//	   char aname[100];
//	   char bname[100];
//	   
//	   char abuffer[1000];
//	   char bbuffer[1000];
//	   
//	   yaffs_StartUp();
//	   
//	   yaffsfs_C.yaffs_mount(path);
//	   
//	   Unix.sprintf(aname,"%s%s",path,"/a");
//	   Unix.sprintf(bname,"%s%s",path,"/b");
//	   
//	   Unix.memset(abuffer,'a',1000);
//	   Unix.memset(bbuffer,'b',1000);
//	   
//	   a = yaffsfs_C.yaffs_open(aname, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
//	   b = yaffsfs_C.yaffs_open(bname, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
//	   
//	   Unix.printf(" %s %d %s %d\n",aname,a,bname,b);
//	  
//	   x = 0;
//	   
//	   for(j = 0; j < iters; j++)
//	   {
//			yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
//
//			
//			for(i = 0; i <20000; i++)
//			{
//			   //r =        yaffsfs_C.yaffs_lseek(b,i,yaffsfs_H.SEEK_SET);
//				//r = yaffsfs_C.yaffs_write(b,bbuffer,1000);
//				
//				if(!(x%20))
//				{
//					// shrink
//					int syz = yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
//					
//					while(syz > 4000)
//					{
//					
//						syz -= 2050;
//						if(syz < 0) syz = 0;
//						yaffsfs_C.yaffs_truncate(a,syz);
//						syz = yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
//						Unix.printf("shrink to %d\n",syz);
//					}
//					
//					
//				}
//				else
//				{
//					//expand
//					r = yaffsfs_C.yaffs_lseek(a,-500,yaffsfs_H.SEEK_END);
//					r = yaffsfs_C.yaffs_write(a,abuffer,1000);
//				}
//				x++;
//				
//						
//			}
//			Unix.printf("file size is %d\n",yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END));
//
//	   }
//	   
//	   return 0;
//	   
//	}
//
//	static int directory_rename_test(void)
//	{
//		int r;
//		yaffs_StartUp();
//		
//		yaffsfs_C.yaffs_mount("/ram");
//		yaffsfs_C.yaffs_mkdir("/ram/a",0);
//		yaffsfs_C.yaffs_mkdir("/ram/a/b",0);
//		yaffsfs_C.yaffs_mkdir("/ram/c",0);
//		
//		Unix.printf("\nDirectory look-up of /ram\n");
//		dumpDir("/ram");
//		dumpDir("/ram/a");
//		dumpDir("/ram/a/b");
//
//		Unix.printf("Do rename (should fail)\n");
//			
//		r = yaffs_rename("/ram/a","/ram/a/b/d");
//		Unix.printf("\nDirectory look-up of /ram\n");
//		dumpDir("/ram");
//		dumpDir("/ram/a");
//		dumpDir("/ram/a/b");
//
//		Unix.printf("Do rename (should not fail)\n");
//			
//		r = yaffs_rename("/ram/c","/ram/a/b/d");
//		Unix.printf("\nDirectory look-up of /ram\n");
//		dumpDir("/ram");
//		dumpDir("/ram/a");
//		dumpDir("/ram/a/b");
//		
//		
//		return 1;
//		
//	}
//
	public static int cache_read_test()
	{
		int a,b,c;
		int i;
		int sizeOfFiles = 500000;
		byte[] buffer = new byte[100]; final int bufferIndex = 0;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/"), 0);
		
		make_a_file(Utils.StringToByteArray("/a"), 0, (byte)'a',sizeOfFiles);
		make_a_file(Utils.StringToByteArray("/b"), 0, (byte)'b',sizeOfFiles);

		a = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/a"),0,yaffsfs_H.O_RDONLY,0);
		b = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/b"),0,yaffsfs_H.O_RDONLY,0);
		c = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/c"),0, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);

		do{
			i = sizeOfFiles;
			if (i > 100) i = 100;
			sizeOfFiles  -= i;
			yaffsfs_C.yaffs_read(a,buffer,bufferIndex,i);
			yaffsfs_C.yaffs_read(b,buffer,bufferIndex,i);
			yaffsfs_C.yaffs_write(c,buffer,bufferIndex,i);
		} while(sizeOfFiles > 0);
		
		
		return 1;
		
	}
	
//	PORT Not worth translating.
//
//	static int cache_bypass_bug_test(void)
//	{
//		// This test reporoduces a bug whereby YAFFS caching *was* buypassed
//		// resulting in erroneous reads after writes.
//		// This bug has been fixed.
//		
//		int a;
//		int i;
//		char buffer1[1000];
//		char buffer2[1000];
//		
//		Unix.memset(buffer1,0,sizeof(buffer1));
//		Unix.memset(buffer2,0,sizeof(buffer2));
//			
//		yaffs_StartUp();
//		
//		yaffsfs_C.yaffs_mount("/boot");
//		
//		// Create a file of 2000 bytes.
//		make_a_file("/boot/a",'X',2000);
//
//		a = yaffsfs_C.yaffs_open("/boot/a",yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
//		
//		// Write a short sequence to the file.
//		// This will go into the cache.
//		yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_SET);
//		yaffsfs_C.yaffs_write(a,"abcdefghijklmnopqrstuvwxyz",20); 
//
//		// Read a short sequence from the file.
//		// This will come from the cache.
//		yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_SET);
//		yaffsfs_C.yaffs_read(a,buffer1,30); 
//
//		// Read a page size sequence from the file.
//		yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_SET);
//		yaffsfs_C.yaffs_read(a,buffer2,512); 
//		
//		Unix.printf("buffer 1 %s\n",buffer1);
//		Unix.printf("buffer 2 %s\n",buffer2);
//		
//		if(strncmp(buffer1,buffer2,20))
//		{
//			Unix.printf("Cache bypass bug detected!!!!!\n");
//		}
//		
//		
//		return 1;
//	}
//

	public static int free_space_check()
	{
		int f;
		
			yaffs2.utils.Globals.configuration.yaffs_StartUp();
			yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/"),0);
		    fill_disk(Utils.StringToByteArray("/"),0,2);
		    f = yaffsfs_C.yaffs_freespace(Utils.StringToByteArray("/"),0);
		    
		    Unix.printf("%d free when disk full\n",PrimitiveWrapperFactory.get(f));           
		    return 1;
	}

	public static int truncate_test()
	{
		int a;
		int r;
		int i;
		int l;

		byte[] y = new byte[10]; final int yIndex = 0;
		byte[] tmp = new byte[1];

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/"),0);

		yaffsfs_C.yaffs_unlink(Utils.StringToByteArray("/trunctest"),0);
		
		a = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/trunctest"),0, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR,  yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		yaffsfs_C.yaffs_write(a,Utils.StringToByteArray("abcdefghijklmnopqrstuvwzyz"),0,26);
		
		yaffsfs_C.yaffs_truncate(a,3);
		l= yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_END);
		
		Unix.printf("truncated length is %d\n",PrimitiveWrapperFactory.get(l));

		yaffsfs_C.yaffs_lseek(a,5,yaffsfs_H.SEEK_SET);
		yaffsfs_C.yaffs_write(a,Utils.StringToByteArray("1"),0,1);

		yaffsfs_C.yaffs_lseek(a,0,yaffsfs_H.SEEK_SET);
		
		r = yaffsfs_C.yaffs_read(a,y,yIndex,10);

		Unix.printf("read %d bytes:",PrimitiveWrapperFactory.get(r));

		for(i = 0; i < r; i++)
			{
			tmp[0] = y[i];
			Unix.printf("[%02X]",PrimitiveWrapperFactory.get(tmp));
			}

		Unix.printf("\n");

		return 0;

	}





	static void fill_disk_test(String mountpt)
	{
		int i;
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < 5; i++)
		{
			yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
			fill_disk_and_delete(Utils.StringToByteArray(mountpt),0,100,i+1);
			yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		}
		
	}



	public static void lookup_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		

		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();
		byte[] str = new byte[100]; final int strIndex = 0;

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
					
		d = yaffsfs_C.yaffs_opendir(Utils.StringToByteArray(mountpt),0);
		
		if(!(d != null))
		{
			Unix.printf("opendir failed\n");
		}
		else
		{
			
			for(i = 0; (de = yaffsfs_C.yaffs_readdir(d)) != null; i++)
			{
				Unix.printf("unlinking %s\n",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(de.d_name,de.d_nameIndex)));
				yaffsfs_C.yaffs_unlink(de.d_name,de.d_nameIndex);
			}
			
			Unix.printf("%d files deleted\n",PrimitiveWrapperFactory.get(i));
		}
		
		
		for(i = 0; i < 2000; i++){
		Unix.sprintf(a,aIndex,"%s/%d",PrimitiveWrapperFactory.get(mountpt),PrimitiveWrapperFactory.get(i));
			h =  yaffsfs_C.yaffs_open(a,aIndex,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, 0);
			yaffsfs_C.yaffs_close(h);
		}

		yaffsfs_C.yaffs_rewinddir(d);
		for(i = 0; (de = yaffsfs_C.yaffs_readdir(d)) != null; i++)
		{
			Unix.printf("%d  %s\n",PrimitiveWrapperFactory.get(i),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(de.d_name,de.d_nameIndex)));
		}	
		
		Unix.printf("%d files listed\n\n\n",PrimitiveWrapperFactory.get(i));
		
		yaffsfs_C.yaffs_rewinddir(d);
		yaffsfs_C.yaffs_readdir(d);
		yaffsfs_C.yaffs_readdir(d);
		yaffsfs_C.yaffs_readdir(d);
		
		for(i = 0; i < 2000; i++){
			Unix.sprintf(a,aIndex,"%s/%d",PrimitiveWrapperFactory.get(mountpt),PrimitiveWrapperFactory.get(i));
			yaffsfs_C.yaffs_unlink(a,aIndex);
		}
		
			
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		
	}

	public static void link_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		byte[] c = new byte[100]; final int cIndex = 0;
		
		int  f0;
		int f1;
		int f2;
		int f3;
		Unix.sprintf(a,aIndex,"%s/aaa",PrimitiveWrapperFactory.get(mountpt));
		Unix.sprintf(b,bIndex,"%s/bbb",PrimitiveWrapperFactory.get(mountpt));
		Unix.sprintf(c,cIndex,"%s/ccc",PrimitiveWrapperFactory.get(mountpt));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		
		h = yaffsfs_C.yaffs_open(a, aIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		for(i = 0; i < 100; i++)
			yaffsfs_C.yaffs_write(h,a,aIndex,100);
		
		yaffsfs_C.yaffs_close(h);
		
		yaffsfs_C.yaffs_unlink(b,bIndex);
		yaffsfs_C.yaffs_unlink(c,cIndex);
		yaffsfs_C.yaffs_link(a,aIndex,b,bIndex);
		yaffsfs_C.yaffs_link(a,aIndex,c,cIndex);
		yaffsfs_C.yaffs_unlink(b,bIndex);
		yaffsfs_C.yaffs_unlink(c,cIndex);
		yaffsfs_C.yaffs_unlink(a,aIndex);
		
		
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		Unix.printf("link test done\n");	
		
	}

	public static void freespace_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		
		int  f0;
		int f1;
		int f2;
		int f3;
		Unix.sprintf(a,aIndex,"%s/aaa",PrimitiveWrapperFactory.get(mountpt));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		f0 = yaffsfs_C.yaffs_freespace(Utils.StringToByteArray(mountpt),0);
		
		h = yaffsfs_C.yaffs_open(a, aIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		for(i = 0; i < 100; i++)
			yaffsfs_C.yaffs_write(h,a,aIndex,100);
		
		yaffsfs_C.yaffs_close(h);
		
		f1 = yaffsfs_C.yaffs_freespace(Utils.StringToByteArray(mountpt),0);
		
		yaffsfs_C.yaffs_unlink(a,aIndex);
		
		f2 = yaffsfs_C.yaffs_freespace(Utils.StringToByteArray(mountpt),0);
		
			
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		f3 = yaffsfs_C.yaffs_freespace(Utils.StringToByteArray(mountpt),0);
		
		Unix.printf("%d\n%d\n%d\n%d\n",PrimitiveWrapperFactory.get(f0), PrimitiveWrapperFactory.get(f1),PrimitiveWrapperFactory.get(f2),PrimitiveWrapperFactory.get(f3));
		
		
	}

	public static void simple_rw_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] tmp;
		
		//int x;
		byte[] x;
		int result;

		Unix.sprintf(a,aIndex,"%s/aaa",PrimitiveWrapperFactory.get(mountpt));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		yaffsfs_C.yaffs_unlink(a,aIndex);
		
		h = yaffsfs_C.yaffs_open(a,aIndex,yaffsfs_H.O_CREAT| yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		for(i = 100000;i < 200000; i++){
			tmp = Utils.StringToByteArray(Integer.toString(i));				// TODO test it
			result = yaffsfs_C.yaffs_write(h,tmp,0,tmp.length);/*(h,&i,sizeof(i))*/

			if(result != 7)
			{
				Unix.printf("write error\n");
				System.exit(1);
			}
		}
		
		//yaffsfs_C.yaffs_close(h);
		
		// h = yaffsfs_C.yaffs_open(a,yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		
		yaffsfs_C.yaffs_lseek(h,0,yaffsfs_H.SEEK_SET);
		
		for(i = 100000; i < 200000; i++){
			x = Utils.StringToByteArray(Integer.toString(i));
			result = yaffsfs_C.yaffs_read(h,x,0,x.length);
			
			if(result != 7 || Unix.strcmp(x,0,Utils.StringToByteArray(Integer.toString(i)),0) != 0){
				Unix.printf("read error %d %x %s\n",PrimitiveWrapperFactory.get(i),PrimitiveWrapperFactory.get(result),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(x,0)));
			}
		}
		
		Unix.printf("Simple rw test passed\n");
		
		
		
	}


	public static void scan_deleted_files_test(String mountpt)
	{
		byte[] fn = new byte[100]; final int fnIndex = 0;
		byte[] sub = new byte[100]; final int subIndex = 0;
		
		//const char *p;
		byte[] p; int pIndex;
		
		int i;
		int j;
		int k;
		int h;
		
		Unix.sprintf(sub,subIndex,"%s/sdir",PrimitiveWrapperFactory.get(mountpt));
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(j = 0; j < 10; j++)
		{
			Unix.printf("\n\n>>>>>>> Run %d <<<<<<<<<<<<<\n\n",PrimitiveWrapperFactory.get(j));
			yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
			yaffsfs_C.yaffs_mkdir(sub,subIndex,0);
			
			// TODO verify
			p = ((j & 0) != 0) ? Utils.StringToByteArray(mountpt): sub;
		
			for(i = 0; i < 100; i++)
			{
			  Unix.sprintf(fn,fnIndex,"%s/%d",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(p, 0)),PrimitiveWrapperFactory.get(i));  
			  
			  if((i & 1) != 0)
			  {
				  h = yaffsfs_C.yaffs_open(fn,fnIndex,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
				  for(k = 0; k < 1000; k++)
					  yaffsfs_C.yaffs_write(h,fn,fnIndex,100);
				  yaffsfs_C.yaffs_close(h);
			  }
			  else
			    	yaffsfs_C.yaffs_mkdir(fn,fnIndex,0);
			}
			
			for(i = 0; i < 10; i++)
			{
			  Unix.sprintf(fn,fnIndex,"%s/%d",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(p, 0)),PrimitiveWrapperFactory.get(i));  
			  if((i & 1) != 0) 
			  	yaffsfs_C.yaffs_unlink(fn,fnIndex);
			  else
				  yaffsfs_C.yaffs_rmdir(fn,fnIndex);
			  
			}
					
			yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		}
		
		
		

	}


	static void write_10k(int h)
	{
	   int i;
	   byte[] s = Utils.StringToByteArray("0123456789");
	   for(i = 0; i < 1000; i++)
	     yaffsfs_C.yaffs_write(h,s,0,10);

	}
	static void write_200k_file(byte[] fn, int fnIndex, byte[] fdel, int fdelIndex, byte[] fdel1, int fdel1Index)
	{
	   int h1;
	   int i;
	   int offs;
	   
	   h1 = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
	   
	   for(i = 0; i < 100000; i+= 10000)
	   {
	   	write_10k(h1);
	   }
	   
	   offs = yaffsfs_C.yaffs_lseek(h1,0,yaffsfs_H.SEEK_CUR);
	   if( offs != 100000)
	   {
	   	Unix.printf("Could not write file\n");
	   }
	   
	   yaffsfs_C.yaffs_unlink(fdel,fdelIndex);
	   for(i = 0; i < 100000; i+= 10000)
	   {
	   	write_10k(h1);
	   }
	   
	   offs = yaffsfs_C.yaffs_lseek(h1,0,yaffsfs_H.SEEK_CUR);
	   if( offs != 200000)
	   {
	   	Unix.printf("Could not write file\n");
	   }
	   
	   yaffsfs_C.yaffs_close(h1);
	   yaffsfs_C.yaffs_unlink(fdel1,fdelIndex);
	   
	}


	static void verify_200k_file(byte[] fn, int fnIndex)
	{
	   int h1;
	   int i;
	   byte[] x = new byte[11]; final int xIndex = 0;
	   byte[] s=Utils.StringToByteArray("0123456789"); final int sIndex = 0;
	   int errCount = 0;
	   
	   h1 = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_RDONLY, 0);
	   
	   for(i = 0; i < 200000 && errCount < 10; i+= 10)
	   {
	   	yaffsfs_C.yaffs_read(h1,x,xxIndex,10);
		if(Unix.strncmp(x,xIndex,s,sIndex,10) != 0)
		{
			Unix.printf("File %s verification failed at %d\n",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(fn, fnIndex)),PrimitiveWrapperFactory.get(i));
			errCount++;
		}
	   }
	   if(errCount >= 10)
	   	Unix.printf("Too many errors... aborted\n");
	      
	   yaffsfs_C.yaffs_close(h1);	   
		
	}

	
	public static void check_resize_gc_bug(String mountpt)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		byte[] tmp = new byte[30]; final int tmpIndex = 0;
		
		int i;
		
		Unix.sprintf(a,aIndex,"%s/a",PrimitiveWrapperFactory.get(mountpt));
		Unix.sprintf(b,bIndex,"%s/b",PrimitiveWrapperFactory.get(mountpt));
		Unix.sprintf(c,cIndex,"%s/c",PrimitiveWrapperFactory.get(mountpt));
		Unix.memset(tmp,tmpIndex,(byte)0,30);
	
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		yaffsfs_C.yaffs_unlink(a,aIndex);
		yaffsfs_C.yaffs_unlink(b,bIndex);
		
		for(i = 0; i < 50; i++)
		{  
		   Unix.printf("A\n");write_200k_file/*(a,"",c)*/(a,aIndex,tmp,tmpIndex,c,cIndex);		// TODO verify
		   Unix.printf("B\n");verify_200k_file(a,aIndex);
		   Unix.printf("C\n");write_200k_file(b,bIndex,a,aIndex,c,cIndex);
		   Unix.printf("D\n");verify_200k_file(b,bIndex);
		   yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		   yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		   Unix.printf("E\n");verify_200k_file(a,aIndex);
		   Unix.printf("F\n");verify_200k_file(b,bIndex);
		}
			
	}


	public static void multi_mount_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		byte[] xx = new byte[1000]; final int xxIndex = 0;
		
		int i;
		int j;
		
		Unix.sprintf(a,aIndex,"%s/a",PrimitiveWrapperFactory.get(mountpt));

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < nmounts; i++){
			int h0;
			int h1;
			int len0;
			int len1;
			
			//static char xx[1000];
			
			Unix.printf("############### Iteration %d   Start\n",PrimitiveWrapperFactory.get(i));
			if(true || i == 0 || i == 5) 
				yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);

			dump_directory_tree(Utils.StringToByteArray(mountpt),0);
			
			
			yaffsfs_C.yaffs_mkdir(a,aIndex,0);
			
			Unix.sprintf(xx,xxIndex,"%s/0",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
			h0 = yaffsfs_C.yaffs_open(xx, xxIndex,yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
			Unix.sprintf(xx,xxIndex,"%s/1",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
			h1 = yaffsfs_C.yaffs_open(xx, xxIndex, yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
			for(j = 0; j < 200; j++){
			   yaffsfs_C.yaffs_write(h0,xx,xxIndex,1000);
			   yaffsfs_C.yaffs_write(h1,xx,xxIndex,1000);
			}
			
			len0 = yaffsfs_C.yaffs_lseek(h0,0,yaffsfs_H.SEEK_END);
			len1 = yaffsfs_C.yaffs_lseek(h1,0,yaffsfs_H.SEEK_END);
			
			yaffsfs_C.yaffs_lseek(h0,0,yaffsfs_H.SEEK_SET);
			yaffsfs_C.yaffs_lseek(h1,0,yaffsfs_H.SEEK_SET);

			for(j = 0; j < 200; j++){
			   yaffsfs_C.yaffs_read(h0,xx,xxIndex,1000);
			   yaffsfs_C.yaffs_read(h1,xx,xxIndex,1000);
			}
			
			
			yaffsfs_C.yaffs_truncate(h0,0);
			yaffsfs_C.yaffs_close(h0);
			yaffsfs_C.yaffs_close(h1);
			
			Unix.printf("########### %d\n",PrimitiveWrapperFactory.get(i));
			dump_directory_tree(Utils.StringToByteArray(mountpt),0);

			if(true || i == 4 || i == nmounts -1)
				yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		}
	}


	public static void small_mount_test(String mountpt,int nmounts)
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
		
		byte[] xx = new byte[1000]; final int xxIndex = 0;

		for(i = 0; i < nmounts; i++){
			
			//static char xx[1000];
			
			
			Unix.printf("############### Iteration %d   Start\n",PrimitiveWrapperFactory.get(i));
			if(true || i == 0 || i == 5) 
				yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);

			dump_directory_tree(Utils.StringToByteArray(mountpt),0);
			
			yaffsfs_C.yaffs_mkdir(a,aIndex,0);
			
			Unix.sprintf(xx,xxIndex,"%s/0",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
			if(i ==0){
			
				h0 = yaffsfs_C.yaffs_open(xx, xxIndex, yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
				for(j = 0; j < 130; j++)
					yaffsfs_C.yaffs_write(h0,xx,xxIndex,1000);
				yaffsfs_C.yaffs_close(h0);
			}
			
			h0 = yaffsfs_C.yaffs_open(xx,xxIndex,yaffsfs_H.O_RDONLY,0);
			
			Unix.sprintf(xx,xxIndex,"%s/1",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
			h1 = yaffsfs_C.yaffs_open(xx, xxIndex, yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
			
			while((nread = yaffsfs_C.yaffs_read(h0,xx,xxIndex,1000)) > 0)
				yaffsfs_C.yaffs_write(h1,xx,xxIndex,nread);
			
			
			len0 = yaffsfs_C.yaffs_lseek(h0,0,yaffsfs_H.SEEK_END);
			len1 = yaffsfs_C.yaffs_lseek(h1,0,yaffsfs_H.SEEK_END);
			
			yaffsfs_C.yaffs_lseek(h0,0,yaffsfs_H.SEEK_SET);
			yaffsfs_C.yaffs_lseek(h1,0,yaffsfs_H.SEEK_SET);

			for(j = 0; j < 200; j++){
			   yaffsfs_C.yaffs_read(h0,xx,xxIndex,1000);
			   yaffsfs_C.yaffs_read(h1,xx,xxIndex,1000);
			}
			
			yaffsfs_C.yaffs_close(h0);
			yaffsfs_C.yaffs_close(h1);
			
			Unix.printf("########### %d\n",PrimitiveWrapperFactory.get(i));
			dump_directory_tree(Utils.StringToByteArray(mountpt),0);

			if(true || i == 4 || i == nmounts -1)
				yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		}
	}
	
	protected static void yaffs_touch(byte[] fn, int fnIndex)
	{
		yaffsfs_C.yaffs_chmod(fn, fnIndex, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
	}
	
// TODO needs testing
	public static void checkpoint_fill_test(String mountpt, int nmounts)
	{

		byte[] a = new byte[50]; final int aIndex = 0;
		byte[] b = new byte[50]; final int bIndex = 0;
		byte[] c = new byte[50]; final int cIndex = 0;
		
		byte[] tmp = Utils.StringToByteArray("test Data");
		
		Unix.memcpy(c, cIndex, tmp, 0, tmp.length);
		
		
		
		int i;
		int j;
		int h;
		
		Unix.sprintf(a,aIndex,"%s/a",PrimitiveWrapperFactory.get(mountpt));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < nmounts; i++){
			Unix.printf("############### Iteration %d   Start\n",PrimitiveWrapperFactory.get(i));
			yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt), 0);
			dump_directory_tree(Utils.StringToByteArray(mountpt), 0);
			yaffsfs_C.yaffs_mkdir(a,aIndex,0);
			
			Unix.sprintf(b,bIndex,"%a/zz",PrimitiveWrapperFactory.get(a), PrimitiveWrapperFactory.get(aIndex));
			
			h = yaffsfs_C.yaffs_open(b,bIndex,yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR,yaffsfs_H.S_IREAD |yaffsfs_H.S_IWRITE);
			
			while(yaffsfs_C.yaffs_write(h,c,cIndex,50) == 50){}
			
			yaffsfs_C.yaffs_close(h);
			
			for(j = 0; j < 2; j++){
				Unix.printf("touch %d\n",PrimitiveWrapperFactory.get(j));
				yaffs_touch(b,bIndex);
				yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt), 0);
				yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt), 0);
			}

			dump_directory_tree(Utils.StringToByteArray(mountpt), 0);
			yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt), 0);
		}
	}

	protected static void make_file2(byte[] name1, int name1Index, byte[] name2, int name2Index, int syz)
	{

		byte[] xx = new byte[2500]; final int xxIndex = 0;
		byte[] tmp = Utils.StringToByteArray("abcdefghijklmnopqrstuvwxyz"); 
		Unix.memcpy(xx,xxIndex,tmp,0,tmp.length);
		int i;
		int h1=-1,h2=-1;
		int n = 1;


		if(name1 != null)
			h1 = yaffsfs_C.yaffs_open(name1,name1Index,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		if(name2 != null)
			h2 = yaffsfs_C.yaffs_open(name2,name2Index,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		while(syz > 0 && n > 0){
			i = (syz > 2500) ? 2500 : syz;
			n = yaffsfs_C.yaffs_write(h1,xx,xxIndex,i);
			n = yaffsfs_C.yaffs_write(h2,xx,xxIndex,i);
			syz -= 500;
		}
		yaffsfs_C.yaffs_close(h1);
		yaffsfs_C.yaffs_close(h2);
		
	}
//
//
//	extern void SetCheckpointReservedBlocks(int n);

	
	public static void huge_array_test(String mountpt,int n)
	{

		byte[] a = new byte[50]; final int aIndex = 0;

		
		int i;
		int j;
		int h;
		
		int fnum;
		
		Unix.sprintf(a,aIndex,"mount point %s",PrimitiveWrapperFactory.get(mountpt));
		

		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();

		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		while(n>0){
			n--;
			fnum = 0;
			Unix.printf("\n\n START run\n\n");
			while(yaffsfs_C.yaffs_freespace(Utils.StringToByteArray(mountpt),0) > 25000000){
				Unix.sprintf(a,aIndex,"%s/file%d",PrimitiveWrapperFactory.get(mountpt),PrimitiveWrapperFactory.get(fnum));
				fnum++;
				Unix.printf("create file %s\n",PrimitiveWrapperFactory.get(a));
				create_file_of_size(a,aIndex,10000000);
				Unix.printf("verifying file %s\n",PrimitiveWrapperFactory.get(a));
				verify_file_of_size(a,aIndex,10000000);
			}
			
			Unix.printf("\n\n verification/deletion\n\n");
			
			for(i = 0; i < fnum; i++){
				Unix.sprintf(a,aIndex,"%s/file%d",PrimitiveWrapperFactory.get(mountpt),PrimitiveWrapperFactory.get(i));
				Unix.printf("verifying file %s\n",PrimitiveWrapperFactory.get(a));
				verify_file_of_size(a,aIndex,10000000);
				Unix.printf("deleting file %s\n",PrimitiveWrapperFactory.get(a));
				yaffsfs_C.yaffs_unlink(a,aIndex);
			}
			Unix.printf("\n\n done \n\n");
				
			   
		}
	}

}
