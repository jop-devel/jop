package yaffs2.port.emulation;

import java.io.RandomAccessFile;

import yaffs2.utils.emulation.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;
import yaffs2.port.yaffs_stat;
import yaffs2.port.yaffsfs_C;
import yaffs2.port.yaffsfs_H;
import yaffs2.utils.Constants;
import yaffs2.utils.EmulationUtils;
import yaffs2.utils.Unix;
import yaffs2.utils.Utils;

public class Dtest_C extends yaffs2.port.Dtest_C_Additional {
	static void copy_in_a_file(String yaffsName, String inName)
	{
		RandomAccessFile inh;
		int outh;
		/*unsigned char buffer[100];*/
		byte[] buffer = new byte[100]; final int bufferIndex = 0;
		int ni,no;
		inh = FileEmulationUnix.open(inName,yaffsfs_H.O_RDONLY);
		outh = yaffsfs_C.yaffs_open(Utils.StringToByteArray(yaffsName), 0, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		while((ni = FileEmulationUnix.read(inh,buffer,bufferIndex,100)) > 0)
		{
			no = yaffsfs_C.yaffs_write(outh,buffer,bufferIndex,ni);
			if(ni != no)
			{
				Unix.printf("problem writing yaffs file\n");
			}
			
		}
		
		yaffsfs_C.yaffs_close(outh);
		FileEmulationUnix.close(inh);
	}
	
	static void make_pattern_file(byte[] fn,int fnIndex,int size)
	{
		int outh;
		int marker;
		int i;
		outh = yaffsfs_C.yaffs_open(fn, fnIndex, yaffsfs_H.O_CREAT | yaffsfs_H.O_RDWR | yaffsfs_H.O_TRUNC, yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		yaffsfs_C.yaffs_lseek(outh,size-1,yaffsfs_H.SEEK_SET);
		yaffsfs_C.yaffs_write(outh,new byte[] {'A'},0,1);
		
		for(i = 0; i < size; i+=256)
		{
			marker = ~i;
			yaffsfs_C.yaffs_lseek(outh,i,yaffsfs_H.SEEK_SET);
			yaffsfs_C.yaffs_write(outh,FileEmulationUnix.IntToByteArray(marker),0,Constants.SIZEOF_INT);
		}
		yaffsfs_C.yaffs_close(outh);
		
	}
	
	static void scan_pattern_test(byte[] path, int pathIndex, int fsize, int niterations)
	{
		int i;
		int j;
		byte[][] fn = new byte[3][100]; final int fnIndex = 0;
		boolean result;
		
		Unix.sprintf(fn[0],fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("f0"));
		Unix.sprintf(fn[1],fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("f1"));
		Unix.sprintf(fn[2],fnIndex,"%a/%s",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex),PrimitiveWrapperFactory.get("f2"));
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < niterations; i++)
		{
			Unix.printf("\n*****************\nIteration %d\n",PrimitiveWrapperFactory.get(i));
			yaffsfs_C.yaffs_mount(path, pathIndex);
			Unix.printf("\nmount: Directory look-up of %a\n",PrimitiveWrapperFactory.get(path),PrimitiveWrapperFactory.get(pathIndex));
			dumpDir(path, pathIndex);
			for(j = 0; j < 3; j++)
			{
				result = dump_file_data(fn[j],fnIndex);
				result = check_pattern_file(fn[j],fnIndex);
				make_pattern_file(fn[j],fnIndex,fsize); 
				result = dump_file_data(fn[j],fnIndex);
				result = check_pattern_file(fn[j],fnIndex);
			}
			yaffsfs_C.yaffs_unmount(path, pathIndex);
		}
	}

	
	public static int long_test(/*int argc, char *argv[]*/)
	{

		int f;
		int r;

		byte[] buffer = new byte[20]; final int bufferIndex = 0;
		byte[] str = new byte[100]; final int strIndex = 0;

		int h;
		
		int temp_mode;
		yaffs_stat ystat = new yaffs_stat();
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
	
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/"), 0);
//		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/boot"), 0);
//		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/data"), 0);
//		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/flash"), 0);
//		yaffsfs_C.yaffs_mount(Utils.StringToByteArray("/ram"), 0);
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"), 0);
//		Unix.printf("\nDirectory look-up of /boot\n");
//		dumpDir(Utils.StringToByteArray("/boot"), 0);
//		Unix.printf("\nDirectory look-up of /data\n");
//		dumpDir(Utils.StringToByteArray("/data"), 0);
//		Unix.printf("\nDirectory look-up of /flash\n");
//		dumpDir(Utils.StringToByteArray("/flash"), 0);

		//leave_unlinked_file("/flash",20000,0);
		//leave_unlinked_file("/data",20000,0);
		
//		leave_unlinked_file(Utils.StringToByteArray("/ram"),0,20,0);
		

		f = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/flashboot/b1"), 0, yaffsfs_H.O_RDONLY,0);
		
		Unix.printf("open /b1 readonly, f=%d\n",PrimitiveWrapperFactory.get(f));
		
		f = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/b1"), 0, yaffsfs_H.O_CREAT,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);
		
		Unix.printf("open /b1 yaffsfs_H.O_CREAT, f=%d\n",PrimitiveWrapperFactory.get(f));
		
		
		r = yaffsfs_C.yaffs_write(f,Utils.StringToByteArray("hello"),0,1);
		Unix.printf("write %d attempted to write to a read-only file\n",PrimitiveWrapperFactory.get(r));
		
		r = yaffsfs_C.yaffs_close(f);
		
		Unix.printf("close %d\n",PrimitiveWrapperFactory.get(r));

		f = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/b1"), 0, yaffsfs_H.O_RDWR,0);
		
		Unix.printf("open /b1 yaffsfs_H.O_RDWR,f=%d\n",PrimitiveWrapperFactory.get(f));
		
		
		r = yaffsfs_C.yaffs_write(f,Utils.StringToByteArray("hello"),0,2);
		Unix.printf("write %d attempted to write to a writeable file\n",PrimitiveWrapperFactory.get(r));
		r = yaffsfs_C.yaffs_write(f,Utils.StringToByteArray("world"),0,3);
		Unix.printf("write %d attempted to write to a writeable file\n",PrimitiveWrapperFactory.get(r));
		
		r= yaffsfs_C.yaffs_lseek(f,0,yaffsfs_H.SEEK_END);
		Unix.printf("seek end %d\n",PrimitiveWrapperFactory.get(r));
		Unix.memset(buffer,bufferIndex,(byte)0,20);
		r = yaffsfs_C.yaffs_read(f,buffer,bufferIndex,10);
		Unix.printf("read %d \"%s\"\n",PrimitiveWrapperFactory.get(r),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(buffer,0)));
		r= yaffsfs_C.yaffs_lseek(f,0,yaffsfs_H.SEEK_SET);
		Unix.printf("seek set %d\n",PrimitiveWrapperFactory.get(r));
		Unix.memset(buffer,bufferIndex,(byte)0,20);
		r = yaffsfs_C.yaffs_read(f,buffer,bufferIndex,10);
		Unix.printf("read %d \"%s\"\n",PrimitiveWrapperFactory.get(r),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(buffer,0)));
		Unix.memset(buffer,bufferIndex,(byte)0,20);
		r = yaffsfs_C.yaffs_read(f,buffer,bufferIndex,10);
		Unix.printf("read %d \"%s\"\n",PrimitiveWrapperFactory.get(r),PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(buffer,0)));

		// Check values reading at end.
		// A read past end of file should return 0 for 0 bytes read.
			
		r= yaffsfs_C.yaffs_lseek(f,0,yaffsfs_H.SEEK_END);
		r = yaffsfs_C.yaffs_read(f,buffer,bufferIndex,10);
		Unix.printf("read at end returned  %d\n",PrimitiveWrapperFactory.get(r)); 
		r= yaffsfs_C.yaffs_lseek(f,500,yaffsfs_H.SEEK_END);
		r = yaffsfs_C.yaffs_read(f,buffer,bufferIndex,10);
		Unix.printf("read past end returned  %d\n",PrimitiveWrapperFactory.get(r));       
		
		r = yaffsfs_C.yaffs_close(f);
		
		Unix.printf("close %d\n",PrimitiveWrapperFactory.get(r));
		
		copy_in_a_file("/yyfile","xxx");
		
		// Create a file with a long name
		
		copy_in_a_file("/file with a long name","xxx");
		
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);

		// Check stat
		r = yaffsfs_C.yaffs_stat(Utils.StringToByteArray("/file with a long name"),0,ystat);
		
		// Check rename
		
		r = yaffsfs_C.yaffs_rename(Utils.StringToByteArray("/file with a long name"),0,Utils.StringToByteArray("/r1"),0);
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		
		// Check unlink
		r = yaffsfs_C.yaffs_unlink(Utils.StringToByteArray("/r1"),0);
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);

		// Check mkdir
		
		r = yaffsfs_C.yaffs_mkdir(Utils.StringToByteArray("/directory1"),0,0);
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		Unix.printf("\nDirectory look-up of /directory1\n");
		dumpDir(Utils.StringToByteArray("/directory1"),0);

		// add a file to the directory                  
		copy_in_a_file("/directory1/file with a long name","xxx");
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		Unix.printf("\nDirectory look-up of /directory1\n");
		dumpDir(Utils.StringToByteArray("/directory1"),0);
		
		//  Attempt to delete directory (should fail)
		
		r = yaffsfs_C.yaffs_rmdir(Utils.StringToByteArray("/directory1"),0);
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		Unix.printf("\nDirectory look-up of /directory1\n");
		dumpDir(Utils.StringToByteArray("/directory1"),0);
		
		// Delete file first, then rmdir should work
		r = yaffsfs_C.yaffs_unlink(Utils.StringToByteArray("/directory1/file with a long name"),0);
		r = yaffsfs_C.yaffs_rmdir(Utils.StringToByteArray("/directory1"),0);
		
		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		Unix.printf("\nDirectory look-up of /directory1\n");
		dumpDir(Utils.StringToByteArray("/directory1"),0);

//	#if 0
//		fill_disk_and_delete("/boot",20,20);
//				
//		Unix.printf("\nDirectory look-up of /boot\n");
//		dumpDir("/boot");
//	#endif

		yaffsfs_C.yaffs_symlink(Utils.StringToByteArray("yyfile"),0,Utils.StringToByteArray("/slink"),0);
		
		yaffsfs_C.yaffs_readlink(Utils.StringToByteArray("/slink"),0,str,strIndex,100);
		Unix.printf("symlink alias is %s\n",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(str,0)));
		

		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		Unix.printf("\nDirectory look-up of / (using stat instead of lstat)\n");
		dumpDirFollow(Utils.StringToByteArray("/"),0);
		Unix.printf("\nDirectory look-up of /directory1\n");
		dumpDir(Utils.StringToByteArray("/directory1"),0);

		h = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/slink"),0,yaffsfs_H.O_RDWR,0);
		
		Unix.printf("file length is %d\n",PrimitiveWrapperFactory.get((int)yaffsfs_C.yaffs_lseek(h,0,yaffsfs_H.SEEK_END)));
		
		yaffsfs_C.yaffs_close(h);
		
		yaffsfs_C.yaffs_unlink(Utils.StringToByteArray("/slink"),0);

		
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		
		// Check chmod
		
		yaffsfs_C.yaffs_stat(Utils.StringToByteArray("/yyfile"),0,ystat);
		temp_mode = ystat.st_mode;
		
		yaffsfs_C.yaffs_chmod(Utils.StringToByteArray("/yyfile"),0,0x55555);
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		
		yaffsfs_C.yaffs_chmod(Utils.StringToByteArray("/yyfile"),0,temp_mode);
		Unix.printf("\nDirectory look-up of /\n");
		dumpDir(Utils.StringToByteArray("/"),0);
		
		// Permission checks...
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,0, yaffsfs_H.O_WRONLY,0);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,0, yaffsfs_H.O_RDONLY,0);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,0, yaffsfs_H.O_RDWR,0);

		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IREAD, yaffsfs_H.O_WRONLY,0);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IREAD, yaffsfs_H.O_RDONLY,1);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IREAD, yaffsfs_H.O_RDWR,0);

		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IWRITE, yaffsfs_H.O_WRONLY,1);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IWRITE, yaffsfs_H.O_RDONLY,0);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IWRITE, yaffsfs_H.O_RDWR,0);
		
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE, yaffsfs_H.O_WRONLY,1);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE, yaffsfs_H.O_RDONLY,1);
		PermissionsCheck(Utils.StringToByteArray("/yyfile"),0,yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE, yaffsfs_H.O_RDWR,1);

		yaffsfs_C.yaffs_chmod(Utils.StringToByteArray("/yyfile"),0,temp_mode);
		
		//create a zero-length file and unlink it (test for scan bug)
		
		h = yaffsfs_C.yaffs_open(Utils.StringToByteArray("/zlf"),0,yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC | yaffsfs_H.O_RDWR,0);
		yaffsfs_C.yaffs_close(h);
		
		yaffsfs_C.yaffs_unlink(Utils.StringToByteArray("/zlf"),0);
		
		
		yaffsfs_C.yaffs_DumpDevStruct(Utils.StringToByteArray("/"),0);
		
		fill_disk_and_delete(Utils.StringToByteArray("/"),0,20,20);
		
		yaffsfs_C.yaffs_DumpDevStruct(Utils.StringToByteArray("/"),0);
		
		fill_files(Utils.StringToByteArray("/"),0,1,10000,0);
		fill_files(Utils.StringToByteArray("/"),0,1,10000,5000);
		fill_files(Utils.StringToByteArray("/"),0,2,10000,0);
		fill_files(Utils.StringToByteArray("/"),0,2,10000,5000);
		
//		leave_unlinked_file(Utils.StringToByteArray("/data"),0,20000,0);
//		leave_unlinked_file(Utils.StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(Utils.StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(Utils.StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(Utils.StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(Utils.StringToByteArray("/data"),0,20000,5000);
		
		yaffsfs_C.yaffs_DumpDevStruct(Utils.StringToByteArray("/"),0);
//		yaffs_DumpDevStruct(Utils.StringToByteArray("/data"),0);
		
			
			
		return 0;

	}

	
	public static void checkpoint_upgrade_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[50]; final int aIndex = 0;
		byte[] b = new byte[50]; final int bIndex = 0;
		byte[] c = new byte[50]; final int cIndex = 0;
		byte[] d = new byte[50]; final int dIndex = 0;
		
		int i;
		int j;
		int h;
		
		Unix.sprintf(a,aIndex,"%s/a",PrimitiveWrapperFactory.get(mountpt));

		Unix.printf("Create start condition\n");
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		((yaffs2.platform.emulation.yaffscfg2k_C)yaffs2.utils.Globals.configuration).SetCheckpointReservedBlocks(0);
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		yaffsfs_C.yaffs_mkdir(a,aIndex,0);
		Unix.sprintf(b,bIndex,"%s/zz",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
		Unix.sprintf(c,cIndex,"%s/xx",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
		make_file2(b,bIndex,c,cIndex,2000000);
		Unix.sprintf(d,dIndex,"%s/aa",PrimitiveWrapperFactory.get(EmulationUtils.byteArrayToString(a, aIndex)));
		make_file2(d,dIndex,null,0,500000000);
		dump_directory_tree(Utils.StringToByteArray(mountpt),0);
		
		Unix.printf("Umount/mount attempt full\n");
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		
		((yaffs2.platform.emulation.yaffscfg2k_C)yaffs2.utils.Globals.configuration).SetCheckpointReservedBlocks(10);
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		Unix.printf("unlink small file\n");
		yaffsfs_C.yaffs_unlink(c,cIndex);
		dump_directory_tree(Utils.StringToByteArray(mountpt),0);
			
		Unix.printf("Umount/mount attempt\n");
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		
		for(j = 0; j < 500; j++){
			Unix.printf("***** touch %d\n",PrimitiveWrapperFactory.get(j));
			dump_directory_tree(Utils.StringToByteArray(mountpt),0);
			yaffs_touch(b,bIndex);
			yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
			yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		}

		for(j = 0; j < 500; j++){
			Unix.printf("***** touch %d\n",PrimitiveWrapperFactory.get(j));
			dump_directory_tree(Utils.StringToByteArray(mountpt),0);
			yaffs_touch(b,bIndex);
			yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountpt),0);
			yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountpt),0);
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.emulation.yaffscfg2k_C(),
				new yaffs2.platform.emulation.PortConfiguration(),
				new yaffs2.platform.emulation.DebugConfiguration());
		
		small_overwrite_test("/",1);
		
// PASSED:
		
		//cache_read_test();
		//scan_pattern_test(Utils.StringToByteArray("/"),0,10000,10);
		//yaffs_backward_scan_test(Utils.StringToByteArray("/"),0);
		//scan_pattern_test(Utils.StringToByteArray("/"),0,10000,100);
		//yaffs_device_flush_test(Utils.StringToByteArray("/"),0);
		//short_scan_test(Utils.StringToByteArray("/"),0,40000,10);
		//short_scan_test(Utils.StringToByteArray("/"),0,40000,20);
		//short_scan_test(Utils.StringToByteArray("/"),0,40000,100);
		//small_overwrite_test("/",100);
		//small_overwrite_test("/",10);
		//checkpoint_fill_test("/",10);
		//long_test();		
		//resize_stress_test("/");
		//resize_stress_test_no_grow_complex("/",1);
		//resize_stress_test_no_grow_complex("/",20);
		//huge_array_test("/",1);
		//huge_array_test("/",10);
		//checkpoint_upgrade_test("/",1);
		//checkpoint_upgrade_test("/",10);
		//small_mount_test("/",1);
		//small_mount_test("/",1000);
		//multi_mount_test("/",10);
		//rename_over_test("/");
		//truncate_test();
		//huge_directory_test_on_path("/");
		//scan_deleted_files_test("/");
		//check_resize_gc_bug("/");
		//lookup_test("/");
		//simple_rw_test("/");
		//free_space_check();
		//freespace_test("/");
		//link_test("/");
		//fill_disk_test("/");
		
// MAY NEED RETESTING:
		
		//small_overwrite_test("/",1000);		
		//(should be fine, takes about half a day on my windows)		
		
// TO DO:
		  
// NOT DONE:	

		// cache_bypass_bug_test();
		// (i suppose its not worth translating)		 
		 
		 //return 0;
	}


}
