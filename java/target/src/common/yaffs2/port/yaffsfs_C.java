// XXX some = 0 initializations are wrong
// XXX some type issues - long - int -
// CHECK needs verification

package yaffs2.port;

import yaffs2.utils.*;

import static yaffs2.utils.Unix.*;

import static yaffs2.port.Guts_H.*;
import static yaffs2.port.CFG_H.*;
import static yaffs2.port.yportenv.*;
import static yaffs2.port.ydirectenv.*;
import static yaffs2.port.yaffsfs_H.*;
import static yaffs2.port.yaffs_guts_C.*;
import static yaffs2.port.devextras.*;

public class yaffsfs_C implements yaffs_Device.removeObjectCallbackInterface
{
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
	 
//	#include "yaffsfs.h"
//	#include "yaffs_guts.h"
//	#include "yaffscfg.h"
//	#include <string.h> // for memset
//	#include "yportenv.h"

	
	// PORT
	private static yaffsfs_C callbackInstance = new yaffsfs_C(); 
	
	static final int YAFFSFS_MAX_SYMLINK_DEREFERENCES = 5;

//	#ifndef NULL
//	#define NULL ((void *)0)
//	#endif


	static final String yaffsfs_c_version="$Id: yaffsfs_C.java,v 1.1 2007/06/07 14:37:29 peter.hilber Exp $";

//	 configurationList is the list of devices that are supported
	static yaffsfs_DeviceConfiguration[] yaffsfs_configurationList;


	/* Some forward references */
//	static yaffs_Object *yaffsfs_FindObject(yaffs_Object *relativeDirectory, const char *path, int symDepth);
//	static void yaffsfs_RemoveObjectCallback(yaffs_Object *obj);


//	 Handle management.
//	 

//	typedef struct
//	{
//		__u8  inUse:1;		// this handle is in use
//		__u8  readOnly:1;	// this handle is read only
//		__u8  append:1;		// append only
//		__u8  exclusive:1;	// exclusive
//		__u32 position;		// current position in file
//		yaffs_Object *obj;	// the object
//	}yaffsfs_Handle;


	static yaffsfs_Handle[] yaffsfs_handle;
	
	static {
		 yaffsfs_handle = new yaffsfs_Handle[YAFFSFS_N_HANDLES];
		 for (int i = 0; i < yaffsfs_handle.length; i++)
			 yaffsfs_handle[i] = new yaffsfs_Handle();
	}

//	 yaffsfs_InitHandle
//	/ Inilitalise handles on start-up.
	//
	static int yaffsfs_InitHandles()
	{
		int i;
		for(i = 0; i < YAFFSFS_N_HANDLES; i++)
		{
			yaffsfs_handle[i].inUse = false;
			yaffsfs_handle[i].obj = null;
		}
		return 0;
	}

	static yaffsfs_Handle yaffsfs_GetHandlePointer(int h)
	{
		if(h < 0 || h >= YAFFSFS_N_HANDLES)
		{
			return null;
		}
		
		return yaffsfs_handle[h];
	}

	static yaffs_Object yaffsfs_GetHandleObject(int handle)
	{
		yaffsfs_Handle h = yaffsfs_GetHandlePointer(handle);

		if(h != null && h.inUse)
		{
			return h.obj;
		}
		
		return null;
	}


//	yaffsfs_GetHandle
//	 Grab a handle (when opening a file)
	//

	static int yaffsfs_GetHandle()
	{
		int i;
		yaffsfs_Handle h;
		
		for(i = 0; i < YAFFSFS_N_HANDLES; i++)
		{
			h = yaffsfs_GetHandlePointer(i);
			if(!(h != null))
			{
				// todo bug: should never happen
			}
			if(!h.inUse)
			{
				memset(h/*,0,sizeof(yaffsfs_Handle)*/ );
				h.inUse=true;
				return i;
			}
		}
		return -1;
	}

//	 yaffs_PutHandle
//	 Let go of a handle (when closing a file)
	//
	static int yaffsfs_PutHandle(int handle)
	{
		yaffsfs_Handle h = yaffsfs_GetHandlePointer(handle);
		
		if(h != null)
		{
			h.inUse = false;
			h.obj = null;
		}
		return 0;
	}



//	 Stuff to search for a directory from a path


	static boolean yaffsfs_Match(byte a, byte b)
	{
		// case sensitive
		return (a == b);
	}

//	 yaffsfs_FindDevice
//	 yaffsfs_FindRoot
//	 Scan the configuration list to find the root.
//	 Curveballs: Should match paths that end in '/' too
//	 Curveball2 Might have "/x/ and "/x/y". Need to return the longest match
	static yaffs_Device yaffsfs_FindDevice(byte[] path, int pathIndex, /*char ***/ ArrayPointer restOfPath)
	{
		yaffsfs_DeviceConfiguration[] cfg = yaffsfs_configurationList;
		int cfgIndex = 0;
		byte[] leftOver;
		int leftOverIndex;
		byte[] p;
		int pIndex;
		yaffs_Device retval = null;
		int thisMatchLength;
		int longestMatch = -1;
		
		// Check all configs, choose the one that:
		// 1) Actually matches a prefix (ie /a amd /abc will not match
		// 2) Matches the longest.
		while(cfg != null && cfg[cfgIndex].prefix != null && cfg[cfgIndex].dev != null)
		{
			leftOver = path; leftOverIndex = pathIndex;
			p = cfg[cfgIndex].prefix; pIndex = cfg[cfgIndex].prefixIndex;
			thisMatchLength = 0;
			
			while(p[pIndex] != 0 &&  //unmatched part of prefix 
			      strcmp(p,pIndex,new byte[]{'/',0},0) != 0 && // the rest of the prefix is not / (to catch / at end)
			      leftOver[leftOverIndex] != 0 && 
			      yaffsfs_Match(p[pIndex],leftOver[leftOverIndex]))
			{
				pIndex++;
				leftOverIndex++;
				thisMatchLength++;
			}
			if((!(p[pIndex] != 0) || strcmp(p,pIndex,new byte[]{'/',0},0) == 0) &&      // end of prefix
			   (!(leftOver[leftOverIndex] != 0) || leftOver[leftOverIndex] == '/') && // no more in this path name part
			   (thisMatchLength > longestMatch))
			{
				// Matched prefix
				restOfPath.array = /*(char *)*/ leftOver;
				restOfPath.index = leftOverIndex;
				retval = cfg[cfgIndex].dev;
				longestMatch = thisMatchLength;
			}
			cfgIndex++;
		}
		return retval;
	}

	static yaffs_Object yaffsfs_FindRoot(byte[] path, int pathIndex, /*char ***/ ArrayPointer restOfPath)
	{

		yaffs_Device dev;
		
		dev= yaffsfs_FindDevice(path,pathIndex,restOfPath);
		if(dev != null && dev.isMounted)
		{
			return dev.rootDir;
		}
		return null;
	}

	static yaffs_Object yaffsfs_FollowLink(yaffs_Object obj,int symDepth)
	{

		while(obj != null && obj.variantType == YAFFS_OBJECT_TYPE_SYMLINK)
		{
			byte[] alias = obj.variant.symLinkVariant().alias;
			int aliasIndex = obj.variant.symLinkVariant().aliasIndex;
							
			if(alias[aliasIndex] == '/')
			{
				// Starts with a /, need to scan from root up
				obj = yaffsfs_FindObject(null,alias,aliasIndex,symDepth++);
			}
			else
			{
				// Relative to here, so use the parent of the symlink as a start
				obj = yaffsfs_FindObject(obj.parent,alias,aliasIndex,symDepth++);
			}
		}
		return obj;
	}


//	 yaffsfs_FindDirectory
//	 Parse a path to determine the directory and the name within the directory.
	//
//	 eg. "/data/xx/ff" -. puts name="ff" and returns the directory "/data/xx"
	static yaffs_Object yaffsfs_DoFindDirectory(yaffs_Object startDir, byte[] path,
			int pathIndex, /*char ***/ ArrayPointer name,int symDepth)
	{
		yaffs_Object dir;
		byte[] restOfPath; int restOfPathIndex;
		byte[] str = new byte[YAFFS_MAX_NAME_LENGTH+1];
		final int strIndex = 0;
		int i;
		
		if(symDepth > YAFFSFS_MAX_SYMLINK_DEREFERENCES)
		{
			return null;
		}
		
		if(startDir != null)
		{
			dir = startDir;
			restOfPath = /*(char *)*/ path;
			restOfPathIndex = pathIndex;
		}
		else
		{
			ArrayPointer restOfPathPointer = new ArrayPointer();
			dir = yaffsfs_FindRoot(path,pathIndex,restOfPathPointer);
			restOfPath = restOfPathPointer.array; restOfPathIndex = restOfPathPointer.index;
		}
		
		while(dir != null)
		{	
			// parse off /.
			// curve ball: also throw away surplus '/' 
			// eg. "/ram/x////ff" gets treated the same as "/ram/x/ff"
			while(restOfPath[restOfPathIndex] == '/')
			{
				restOfPathIndex++; // get rid of '/'
			}
			
			name.array = restOfPath;
			name.index = restOfPathIndex;
			i = 0;
			
			while(restOfPath[restOfPathIndex] != 0 && restOfPath[restOfPathIndex] != '/')
			{
				if (i < YAFFS_MAX_NAME_LENGTH)
				{
					str[strIndex+i] = restOfPath[restOfPathIndex];
					str[strIndex+i+1] = '\0';
					i++;
				}
				restOfPathIndex++;
			}
			
			if(!(restOfPath[restOfPathIndex] != 0))
			{
				// got to the end of the string
				return dir;
			}
			else
			{
				if(strcmp(str,strIndex,new byte[]{'.',0},0) == 0)
				{
					// Do nothing
				}
				else if(strcmp(str,strIndex,new byte[]{'.','.',0},0) == 0)
				{
					dir = dir.parent;
				}
				else
				{
					dir = yaffs_FindObjectByName(dir,str,strIndex);
					
					while(dir != null && dir.variantType == YAFFS_OBJECT_TYPE_SYMLINK)
					{
					
						dir = yaffsfs_FollowLink(dir,symDepth);
			
					}
					
					if(dir != null && dir.variantType != YAFFS_OBJECT_TYPE_DIRECTORY)
					{
						dir = null;
					}
				}
			}
		}
		// directory did not exist.
		return null;
	}

	static yaffs_Object yaffsfs_FindDirectory(yaffs_Object relativeDirectory, byte[] path, 
			int pathIndex,/*char ***/ ArrayPointer name,int symDepth)
	{
		return yaffsfs_DoFindDirectory(relativeDirectory,path,pathIndex,name,symDepth);
	}

//	 yaffsfs_FindObject turns a path for an existing object into the object
//	 
	static yaffs_Object yaffsfs_FindObject(yaffs_Object relativeDirectory, byte[] path, int pathIndex, int symDepth)
	{
		yaffs_Object dir;
		byte[] name; int nameIndex;
		
		ArrayPointer namePointer = new ArrayPointer();
		dir = yaffsfs_FindDirectory(relativeDirectory,path,pathIndex,namePointer,symDepth);
		name = namePointer.array; nameIndex = namePointer.index; 
		
		if(dir != null && name[nameIndex] != 0)
		{
			return yaffs_FindObjectByName(dir,name,nameIndex);
		}
		
		return dir;
	}



	static int yaffs_open(byte[] path, int pathIndex, int oflag, int mode)
	{
		yaffs_Object obj = null;
		yaffs_Object dir = null;
		byte[] name; int nameIndex;
		int handle = -1;
		yaffsfs_Handle h = null;
		boolean alreadyOpen = false;
		boolean alreadyExclusive = false;
		boolean openDenied = false;
		int symDepth = 0;
		boolean errorReported = false;
		
		int i;
		
		
		// todo sanity check oflag (eg. can't have O_TRUNC without WRONLY or RDWR
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		handle = yaffsfs_GetHandle();
		
		if(handle >= 0)
		{

			h = yaffsfs_GetHandlePointer(handle);
		
		
			// try to find the exisiting object
			obj = yaffsfs_FindObject(null,path,pathIndex,0);
			
			if(obj != null && obj.variantType == YAFFS_OBJECT_TYPE_SYMLINK)
			{
			
				obj = yaffsfs_FollowLink(obj,symDepth++);
			}

			if(obj != null)
			{
				// Check if the object is already in use
				alreadyOpen = alreadyExclusive = false;
				
//				for(i = 0; i <= YAFFSFS_N_HANDLES; i++) // PORT a yaffs bug imho
				for(i = 0; i < YAFFSFS_N_HANDLES; i++)
				{
					
					if(i != handle &&
					   yaffsfs_handle[i].inUse &&
					    obj == yaffsfs_handle[i].obj)
					 {
					 	alreadyOpen = true;
						if(yaffsfs_handle[i].exclusive)
						{
							alreadyExclusive = true;
						}
					 }
				}

				if(((oflag & O_EXCL) != 0 && alreadyOpen) || alreadyExclusive)
				{
					openDenied = true;
				}
				
				// Open should fail if O_CREAT and O_EXCL are specified
				if((oflag & O_EXCL) != 0 && (oflag & O_CREAT) != 0)
				{
					openDenied = true;
					yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EEXIST);
					errorReported = true;
				}
				
				// Check file permissions
				if( (oflag & (O_RDWR | O_WRONLY)) == 0 &&     // ie O_RDONLY
				   !((obj.yst_mode & S_IREAD) != 0))
				{
					openDenied = true;
				}

				if( (oflag & O_RDWR) != 0 && 
				   !((obj.yst_mode & S_IREAD) != 0))
				{
					openDenied = true;
				}

				if( (oflag & (O_RDWR | O_WRONLY)) != 0 && 
				   !((obj.yst_mode & S_IWRITE) != 0))
				{
					openDenied = true;
				}
				
			}
			
			else if((oflag & O_CREAT) != 0)
			{
				// Let's see if we can create this fil
				ArrayPointer namePointer = new ArrayPointer();
				dir = yaffsfs_FindDirectory(null,path,pathIndex,namePointer,0);
				name = namePointer.array; nameIndex = namePointer.index;
				
				if(dir != null)
				{
					obj = yaffs_MknodFile(dir,name,nameIndex,mode,0,0);	
				}
				else
				{
					yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOTDIR);
				}
			}
			
			if(obj != null && !openDenied)
			{
				h.obj = obj;
				h.inUse = true;
		    	h.readOnly = (oflag & (O_WRONLY | O_RDWR)) != 0 ? false : true;
				h.append =  (oflag & O_APPEND) != 0 ? true : false;
				h.exclusive = (oflag & O_EXCL) != 0 ? true : false;
				h.position = 0;
				
				obj.inUse++;
				if((oflag & O_TRUNC) != 0 && !h.readOnly)
				{
					//todo truncate
					yaffs_ResizeFile(obj,0);
				}
				
			}
			else
			{
				yaffsfs_PutHandle(handle);
				if(!errorReported)
				{
					yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EACCESS);
					errorReported = true;
				}
				handle = -1;
			}
			
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return handle;		
	}

	static int yaffs_close(int fd)
	{
		yaffsfs_Handle h = null;
		int retVal = 0;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();

		h = yaffsfs_GetHandlePointer(fd);
		
		if(h != null && h.inUse)
		{
			// clean up
			yaffs_FlushFile(h.obj,true);
			h.obj.inUse--;
			if(h.obj.inUse <= 0 && h.obj.unlinked)
			{
				yaffs_DeleteFile(h.obj);
			}
			yaffsfs_PutHandle(fd);
			retVal = 0;
		}
		else
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
			retVal = -1;
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
	}

	static int yaffs_read(int fd, /*void **/ byte[] buf, int bufIndex, 
			/*unsigned int*/ int nbyte)
	{
		yaffsfs_Handle h = null;
		yaffs_Object obj = null;
		int pos = 0;
		int nRead = -1;
		int maxRead;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		h = yaffsfs_GetHandlePointer(fd);
		obj = yaffsfs_GetHandleObject(fd);
		
		if(!(h != null) || !(obj != null))
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
		}
		else if(h != null && obj != null)
		{
			pos=  h.position;
			if(yaffs_GetObjectFileLength(obj) > pos)
			{
				maxRead = yaffs_GetObjectFileLength(obj) - pos;
			}
			else
			{
				maxRead = 0;
			}

			if(nbyte > maxRead)
			{
				nbyte = maxRead;
			}

			
			if(nbyte > 0)
			{
				nRead = yaffs_ReadDataFromFile(obj,buf,bufIndex,pos,nbyte);
				if(nRead >= 0)
				{
					h.position = pos + nRead;
				}
				else
				{
					//todo error
				}
			}
			else
			{
				nRead = 0;
			}
			
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		
		return (nRead >= 0) ? nRead : -1;
			
	}

	static int yaffs_write(int fd, /*const void **/ byte[] buf, int bufIndex,
			/*unsigned int*/ int nbyte)
	{
		yaffsfs_Handle h = null;
		yaffs_Object obj = null;
		int pos = 0;
		int nWritten = -1;
		boolean writeThrough = false;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		h = yaffsfs_GetHandlePointer(fd);
		obj = yaffsfs_GetHandleObject(fd);
		
		if(!(h != null) || !(obj != null))
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
		}
		else if( h != null && obj != null && h.readOnly)
		{
			// todo error
		}
		else if( h != null && obj != null)
		{
			if(h.append)
			{
				pos =  yaffs_GetObjectFileLength(obj);
			}
			else
			{
				pos = h.position;
			}
			
			nWritten = yaffs_WriteDataToFile(obj,buf,bufIndex,pos,nbyte,writeThrough);
			
			if(nWritten >= 0)
			{
				h.position = pos + nWritten;
			}
			else
			{
				//todo error
			}
			
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		
		return (nWritten >= 0) ? nWritten : -1;

	}

	static int yaffs_truncate(int fd, /*off_t*/ int newSize)
	{
		yaffsfs_Handle h = null;
		yaffs_Object obj = null;
		int result = 0;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		h = yaffsfs_GetHandlePointer(fd);
		obj = yaffsfs_GetHandleObject(fd);
		
		if(!(h != null) || !(obj != null))
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
		}
		else
		{
			// resize the file
			result = yaffs_ResizeFile(obj,newSize);
		}	
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		
		return (result != 0) ? 0 : -1;

	}

	/*off_t*/static int yaffs_lseek(int fd, /*off_t*/ int offset, int whence) 
	{
		yaffsfs_Handle h = null;
		yaffs_Object obj = null;
		int pos = -1;
		int fSize = -1;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		h = yaffsfs_GetHandlePointer(fd);
		obj = yaffsfs_GetHandleObject(fd);
		
		if(!(h != null) || !(obj != null))
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
		}
		else if(whence == SEEK_SET)
		{
			if(offset >= 0)
			{
				pos = offset;
			}
		}
		else if(whence == SEEK_CUR)
		{
			if( (h.position + offset) >= 0)
			{
				pos = (h.position + offset);
			}
		}
		else if(whence == SEEK_END)
		{
			fSize = yaffs_GetObjectFileLength(obj);
			if(fSize >= 0 && (fSize + offset) >= 0)
			{
				pos = fSize + offset;
			}
		}
		
		if(pos >= 0)
		{
			h.position = pos;
		}
		else
		{
			// todo error
		}

		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return pos;
	}


	static int yaffsfs_DoUnlink(byte[] path, int pathIndex, boolean isDirectory) 
	{
		yaffs_Object dir = null;
		yaffs_Object obj = null;
		byte[] name; int nameIndex;
		boolean result = YAFFS_FAIL;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();

		obj = yaffsfs_FindObject(null,path,pathIndex,0);
		ArrayPointer namePointer = new ArrayPointer();
		dir = yaffsfs_FindDirectory(null,path,pathIndex,namePointer,0);
		name = namePointer.array; nameIndex = namePointer.index;
		
		if(!(dir != null))
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOTDIR);
		}
		else if(!(obj != null))
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOENT);
		}
		else if(!isDirectory && obj.variantType == YAFFS_OBJECT_TYPE_DIRECTORY)
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EISDIR);
		}
		else if(isDirectory && obj.variantType != YAFFS_OBJECT_TYPE_DIRECTORY)
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOTDIR);
		}
		else
		{
			result = yaffs_Unlink(dir,name,nameIndex);
			
			if(result == YAFFS_FAIL && isDirectory)
			{
				yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOTEMPTY);
			}
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		// todo error
		
		return (result == YAFFS_FAIL) ? -1 : 0;
	}
	static int yaffs_rmdir(byte[] path, int pathIndex) 
	{
		return yaffsfs_DoUnlink(path,pathIndex,true);
	}

	static int yaffs_unlink(byte[] path, int pathIndex) 
	{
		return yaffsfs_DoUnlink(path,pathIndex,false);
	}

	static int yaffs_rename(byte[] oldPath, int oldPathIndex, byte[] newPath, int newPathIndex)
	{
		yaffs_Object olddir = null;
		yaffs_Object newdir = null;
		yaffs_Object obj = null;
		byte[] oldname; int oldnameIndex;
		byte[] newname; int newnameIndex;
		boolean result= YAFFS_FAIL;
		boolean renameAllowed = true;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		ArrayPointer oldnamePointer = new ArrayPointer();
		olddir = yaffsfs_FindDirectory(null,oldPath,oldPathIndex,oldnamePointer,0);
		oldname = oldnamePointer.array; oldnameIndex = oldnamePointer.index;
		
		ArrayPointer newnamePointer = new ArrayPointer();
		newdir = yaffsfs_FindDirectory(null,newPath,newPathIndex,newnamePointer,0);
		newname = newnamePointer.array; newnameIndex = newnamePointer.index;
		
		obj = yaffsfs_FindObject(null,oldPath,oldPathIndex,0);
		
		if(!(olddir != null) || !(newdir != null) || !(obj != null))
		{
			// bad file
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);	
			renameAllowed = false;	
		}
		else if(olddir.myDev != newdir.myDev)
		{
			// oops must be on same device
			// todo error
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EXDEV);
			renameAllowed = false;	
		}
		else if(obj != null && obj.variantType == YAFFS_OBJECT_TYPE_DIRECTORY)
		{
			// It is a directory, check that it is not being renamed to 
			// being its own decendent.
			// Do this by tracing from the new directory back to the root, checking for obj
			
			yaffs_Object xx = newdir;
			
			while( renameAllowed && xx != null)
			{
				if(xx == obj)
				{
					renameAllowed = false;
				}
				xx = xx.parent;
			}
			if(!renameAllowed) yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EACCESS);
		}
		
		if(renameAllowed)
		{
			result = yaffs_RenameObject(olddir,oldname,oldnameIndex,newdir,newname,newnameIndex);
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return (result == YAFFS_FAIL) ? -1 : 0;	
	}


	static int yaffsfs_DoStat(yaffs_Object obj, yaffs_stat buf)
	{
		int retVal = -1;

		if(obj != null)
		{
			obj = yaffs_GetEquivalentObject(obj);
		}

		if(obj != null && buf != null)
		{
	    	buf.st_dev = (int)obj.myDev.genericDevice;
	    	buf.st_ino = obj.objectId;
	    	buf.st_mode = obj.yst_mode & ~S_IFMT; // clear out file type bits
		
			if(obj.variantType == YAFFS_OBJECT_TYPE_DIRECTORY) 
			{
				buf.st_mode |= S_IFDIR;
			}
			else if(obj.variantType == YAFFS_OBJECT_TYPE_SYMLINK) 
			{
				buf.st_mode |= S_IFLNK;
			}
			else if(obj.variantType == YAFFS_OBJECT_TYPE_FILE)
			{
				buf.st_mode |= S_IFREG;
			}
			
	    	buf.st_nlink = yaffs_GetObjectLinkCount(obj);
	    	buf.st_uid = 0;    
	    	buf.st_gid = 0;;     
	    	buf.st_rdev = obj.yst_rdev;
	    	buf.st_size = yaffs_GetObjectFileLength(obj);
			buf.st_blksize = obj.myDev.nDataBytesPerChunk;
	    	buf.st_blocks = (buf.st_size + buf.st_blksize -1)/buf.st_blksize;
	    	buf.yst_atime = obj.yst_atime; 
	    	buf.yst_ctime = obj.yst_ctime; 
	    	buf.yst_mtime = obj.yst_mtime; 
			retVal = 0;
		}
		return retVal;
	}

	// XXX what does L mean? -i dont see no L- the one in the method name	
	static int yaffsfs_DoStatOrLStat(byte[] path, int pathIndex, yaffs_stat buf, 
			boolean doLStat)
	{
		yaffs_Object obj;
		
		int retVal = -1;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		obj = yaffsfs_FindObject(null,path,pathIndex,0);
		
		if(!doLStat && obj != null)
		{
			obj = yaffsfs_FollowLink(obj,0);
		}
		
		if(obj != null)
		{
			retVal = yaffsfs_DoStat(obj,buf);
		}
		else
		{
			// todo error not found
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOENT);
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
		
	}

	static int yaffs_stat(byte[] path, int pathIndex, yaffs_stat buf)
	{
		return yaffsfs_DoStatOrLStat(path,pathIndex,buf,false);
	}

	static int yaffs_lstat(byte[] path, int pathIndex, yaffs_stat buf)
	{
		return yaffsfs_DoStatOrLStat(path,pathIndex,buf,true);
	}

	static int yaffs_fstat(int fd, yaffs_stat buf)
	{
		yaffs_Object obj;
		
		int retVal = -1;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		obj = yaffsfs_GetHandleObject(fd);
		
		if(obj != null)
		{
			retVal = yaffsfs_DoStat(obj,buf);
		}
		else
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
	}

	static int yaffsfs_DoChMod(yaffs_Object obj,/*mode_t*/ int mode)
	{
		boolean result = false;

		if(obj != null)
		{
			obj = yaffs_GetEquivalentObject(obj);
		}
		
		if(obj != null)
		{
			obj.yst_mode = mode;
			obj.dirty = true;
			result = yaffs_FlushFile(obj,false);
		}
		
		return result == YAFFS_OK ? 0 : -1;
	}


	static int yaffs_chmod(byte[] path, int pathIndex, /*mode_t*/int mode)
	{
		yaffs_Object obj;
		
		int retVal = -1;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		obj = yaffsfs_FindObject(null,path,pathIndex,0);
		
		if(obj != null)
		{
			retVal = yaffsfs_DoChMod(obj,mode);
		}
		else
		{
			// todo error not found
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOENT);
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
		
	}


	static int yaffs_fchmod(int fd, /*mode_t*/ int mode)
	{
		yaffs_Object obj;
		
		int retVal = -1;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		obj = yaffsfs_GetHandleObject(fd);
		
		if(obj != null)
		{
			retVal = yaffsfs_DoChMod(obj,mode);
		}
		else
		{
			// bad handle
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);		
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
	}


	static int yaffs_mkdir(byte[] path, int pathIndex, /*mode_t*/ int mode)
	{
		yaffs_Object parent = null;
		yaffs_Object dir = null;
		byte[] name; int nameIndex;
		int retVal= -1;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		ArrayPointer namePointer = new ArrayPointer();
		parent = yaffsfs_FindDirectory(null,path,pathIndex,namePointer,0);
		name = namePointer.array; nameIndex = namePointer.index;
		
		if(parent != null)
			dir = yaffs_MknodDirectory(parent,name,nameIndex,mode,0,0);
		if(dir != null)
		{
			retVal = 0;
		}
		else
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOSPC); // just assume no space for now
			retVal = -1;
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
	}

	static int yaffs_mount(byte[] path, int pathIndex)
	{
		int retVal=-1;
		boolean result=YAFFS_FAIL;
		yaffs_Device dev=null;
//		byte[] dummy; int dummyIndex;
		
		T(YAFFS_TRACE_ALWAYS,"yaffs: Mounting %a\n",path,pathIndex);
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		ArrayPointer dummyPointer = new ArrayPointer();
		dev = yaffsfs_FindDevice(path,pathIndex,dummyPointer);
//		dummy = dummyPointer.array; dummyIndex = dummyPointer.index;
		
		if(dev != null)
		{
			if(!dev.isMounted)
			{
				result = yaffs_GutsInitialise(dev);
				if(result == YAFFS_FAIL)
				{
					// todo error - mount failed
					yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOMEM);
				}
				retVal = result ? 0 : -1;
				
			}
			else
			{
				//todo error - already mounted.
				yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBUSY);
			}
		}
		else
		{
			// todo error - no device
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENODEV);
		}
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		return retVal;
		
	}

	static int yaffs_unmount(byte[] path, int pathIndex)
	{
		int retVal=-1;
		yaffs_Device dev=null;
//		byte[] dummy; int dummyIndex;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		ArrayPointer dummyPointer = new ArrayPointer();
		dev = yaffsfs_FindDevice(path,pathIndex,dummyPointer);
//		dummy = dummyPointer.array;	dummyIndex = dummyPointer.index;
		
		if(dev != null)
		{
			if(dev.isMounted)
			{
				int i;
				boolean inUse = false;
				
				yaffs_FlushEntireDeviceCache(dev);
				yaffs_CheckpointSave(dev);
				
				for(i = 0; i < YAFFSFS_N_HANDLES && !inUse; i++)
				{
					if(yaffsfs_handle[i].inUse && yaffsfs_handle[i].obj.myDev == dev)
					{
						inUse = true; // the device is in use, can't unmount
					}
				}
				
				if(!inUse)
				{
					yaffs_Deinitialise(dev);
						
					retVal = 0;
				}
				else
				{
					// todo error can't unmount as files are open
					yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBUSY);
				}
				
			}
			else
			{
				//todo error - not mounted.
				yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EINVAL);
				
			}
		}
		else
		{
			// todo error - no device
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENODEV);
		}	
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		return retVal;
		
	}

	// XXX type
	static /*loff_t*/ int yaffs_freespace(byte[] path, int pathIndex)
	{
		/*loff_t*/ int retVal=-1;
		yaffs_Device dev=null;
//		byte[] dummy; int dummyIndex;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		ArrayPointer dummyPointer = new ArrayPointer();
		dev = yaffsfs_FindDevice(path,pathIndex,dummyPointer);
//		dummy = dummyPointer.array;	dummyIndex = dummyPointer.index;
		
		if(dev != null && dev.isMounted)
		{
			retVal = yaffs_GetNumberOfFreeChunks(dev);
			retVal = retVal * dev.nDataBytesPerChunk;		
			
		}
		else
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EINVAL);
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		return retVal;	
	}



	public static void yaffs_initialise(yaffsfs_DeviceConfiguration[] cfgList)	
	{
		
		yaffsfs_DeviceConfiguration[] cfg;
		int cfgIndex;
		
		yaffsfs_configurationList = cfgList;
		
		yaffsfs_InitHandles();
		
		cfg = yaffsfs_configurationList;
		cfgIndex = 0;
		
		while(cfg != null && cfg[cfgIndex].prefix != null && cfg[cfgIndex].dev != null)
		{
			cfg[cfgIndex].dev.isMounted = false;
			cfg[cfgIndex].dev.removeObjectCallback = callbackInstance;
			cfgIndex++;
		}
		
		
	}


	//
//	 Directory search stuff.

	//
//	 Directory search context
	//
//	 NB this is an opaque structure.


//	typedef struct
//	{
//		__u32 magic;
//		yaffs_dirent de;		/* directory entry being used by this dsc */
//		char name[NAME_MAX+1];		/* name of directory being searched */
//		yaffs_Object *dirObj;		/* ptr to directory being searched */
//		yaffs_Object *nextReturn;	/* obj to be returned by next readddir */
//		int offset;
//		struct list_head others;	
//	} yaffsfs_DirectorySearchContext;



	static list_head search_contexts = new list_head(null);


	static void yaffsfs_SetDirRewound(yaffsfs_DirectorySearchContext dsc)
	{
		if(dsc != null &&
		   dsc.dirObj != null &&
		   dsc.dirObj.variantType == YAFFS_OBJECT_TYPE_DIRECTORY){
		   
		   dsc.offset = 0;
		   
		   if(list_empty(dsc.dirObj.variant.directoryVariant.children)){
		   	dsc.nextReturn = null;
		   } else {
		      	dsc.nextReturn = /*list_entry(dsc.dirObj.variant.directoryVariant.children.next,
							yaffs_Object,siblings);*/
		      		(yaffs_Object)dsc.dirObj.variant.directoryVariant.children.next().list_entry;
		   }
		} else {
				/* Hey someone isn't playing nice! */
		}
	}

	static void yaffsfs_DirAdvance(yaffsfs_DirectorySearchContext dsc)
	{
		if(dsc != null &&
		   dsc.dirObj != null &&
		   dsc.dirObj.variantType == YAFFS_OBJECT_TYPE_DIRECTORY){
		   
		   if( dsc.nextReturn == null ||
		       list_empty(dsc.dirObj.variant.directoryVariant.children)){
		   	dsc.nextReturn = null;
		   } else {
			   list_head next = dsc.nextReturn.siblings.next();
	   
			   if( next == dsc.dirObj.variant.directoryVariant.children)
		   		dsc.nextReturn = null; /* end of list */
		   	   else 
			   	dsc.nextReturn = /*list_entry(next,yaffs_Object,siblings)*/(yaffs_Object)next.list_entry;
		   }
		} else {
			/* Hey someone isn't playing nice! */
		}
	}

	public void yaffsfs_RemoveObjectCallback(yaffs_Object obj)
	{

		list_head i;
		yaffsfs_DirectorySearchContext dsc;
		
		/* if search contexts not initilised then skip */
		if(!(search_contexts.next != null))
			return;
			
		/* Iteratethrough the directory search contexts.
		 * If any are the one being removed, then advance the dsc to
		 * the next one to prevent a hanging ptr.
		 */
		 /*list_for_each(i, &search_contexts) {*/
		 for (i = search_contexts.next();i != search_contexts;i = i.next()) {
			 if (i != null) {
			   dsc = /*list_entry(i, yaffsfs_DirectorySearchContext,others);*/
				   (yaffsfs_DirectorySearchContext)i.list_entry;
			   if(dsc.nextReturn == obj)
				   yaffsfs_DirAdvance(dsc);
			   }
			 }
					
	}

	static yaffs_DIR yaffs_opendir(byte[] dirname, int dirnameIndex)
	{
		yaffs_DIR dir = null;
	 	yaffs_Object obj = null;
		yaffsfs_DirectorySearchContext dsc = null;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		obj = yaffsfs_FindObject(null,dirname,dirnameIndex,0);
		
		if(obj != null && obj.variantType == YAFFS_OBJECT_TYPE_DIRECTORY)
		{
			
			dsc = /*YMALLOC(sizeof(yaffsfs_DirectorySearchContext))*/ new yaffsfs_DirectorySearchContext();
			dir = (yaffs_DIR)dsc;
			if(dsc != null)
			{
//				memset(dsc,0,sizeof(yaffsfs_DirectorySearchContext)); // TODO CHECK: do as serializable?
				memset(dsc);
				dsc.magic = YAFFS_MAGIC;
				dsc.dirObj = obj;
				strncpy(dsc.name,dsc.nameIndex,dirname,dirnameIndex,NAME_MAX);
				INIT_LIST_HEAD(dsc.others);
				
				if(!(search_contexts.next != null))
					INIT_LIST_HEAD(/*&*/search_contexts);
					
				list_add(/*&*/dsc.others,/*&*/search_contexts);	
				yaffsfs_SetDirRewound(dsc);		}
		
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return dir;
	}

	static yaffs_dirent yaffs_readdir(yaffs_DIR dirp)
	{
		yaffsfs_DirectorySearchContext dsc = (yaffsfs_DirectorySearchContext)dirp;
		yaffs_dirent retVal = null;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		if(dsc != null && dsc.magic == YAFFS_MAGIC){
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(0);
			if(dsc.nextReturn != null){
				dsc.de.d_ino = yaffs_GetEquivalentObject(dsc.nextReturn).objectId;
				dsc.de.d_dont_use = /*(unsigned)*/dsc.nextReturn;
				dsc.de.d_off = dsc.offset++;
				yaffs_GetObjectName(dsc.nextReturn,dsc.de.d_name,dsc.de.d_nameIndex,NAME_MAX);
				if(strlen(dsc.de.d_name, dsc.de.d_nameIndex) == 0)
				{
					// this should not happen!
					strcpy(dsc.de.d_name,dsc.de.d_nameIndex,new byte[]{'z','z',0},0);
				}
//				dsc.de.d_reclen = sizeof(struct yaffs_dirent); // PORT ??? 
				retVal = dsc.de;
				yaffsfs_DirAdvance(dsc);
			} else
				retVal = null;
		}
		else
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EBADF);
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
		
	}


	static void yaffs_rewinddir(yaffs_DIR dirp)
	{
		yaffsfs_DirectorySearchContext dsc = (yaffsfs_DirectorySearchContext)dirp;
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		yaffsfs_SetDirRewound(dsc);

		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
	}


	static int yaffs_closedir(yaffs_DIR dirp)
	{
		yaffsfs_DirectorySearchContext dsc = (yaffsfs_DirectorySearchContext)dirp;
			
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		dsc.magic = 0;
		list_del(dsc.others); /* unhook from list */
		YFREE(dsc);
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		return 0;
	}

//	 end of directory stuff


	static int yaffs_symlink(byte[] oldpath, int oldpathIndex, byte[] newpath, int newpathIndex)
	{
		yaffs_Object parent = null;
		yaffs_Object obj;
		byte[] name; int nameIndex;
		int retVal= -1;
		int mode = 0; // ignore for now
		
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		ArrayPointer namePointer = new ArrayPointer();
		parent = yaffsfs_FindDirectory(null,newpath,newpathIndex,namePointer,0);
		name = namePointer.array; nameIndex = namePointer.index;
		
		obj = yaffs_MknodSymLink(parent,name,nameIndex,mode,0,0,oldpath,oldpathIndex);
		if(obj != null)
		{
			retVal = 0;
		}
		else
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOSPC); // just assume no space for now
			retVal = -1;
		}
		
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
		
	}

	static int yaffs_readlink(byte[] path, int pathIndex, byte[] buf, int bufIndex, int bufsiz)
	{
		yaffs_Object obj = null;
		int retVal;

			
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		obj = yaffsfs_FindObject(null,path,pathIndex,0);
		
		if(!(obj != null))
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOENT);
			retVal = -1;
		}
		else if(obj.variantType != YAFFS_OBJECT_TYPE_SYMLINK)
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EINVAL);
			retVal = -1;
		}
		else
		{
			byte[] alias = obj.variant.symLinkVariant.alias;
			int aliasIndex = obj.variant.symLinkVariant.aliasIndex;
			memset(buf,bufIndex,(byte)0,bufsiz);
			strncpy(buf,bufIndex,alias,aliasIndex,bufsiz-1);
			retVal = 0;
		}
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		return retVal;
	}

	static int yaffs_link(byte[] oldpath, int oldpathIndex, byte[] newpath, int newpathIndex)
	{
		// Creates a link called newpath to existing oldpath
		yaffs_Object obj = null;
		yaffs_Object target = null;
		int retVal = 0;

			
		yaffs2.utils.Globals.configuration.yaffsfs_Lock();
		
		obj = yaffsfs_FindObject(null,oldpath,oldpathIndex,0);
		target = yaffsfs_FindObject(null,newpath,newpathIndex,0);
		
		if(!(obj != null))
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOENT);
			retVal = -1;
		}
		else if(target != null)
		{
			yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EEXIST);
			retVal = -1;
		}
		else	
		{
			yaffs_Object newdir = null;
			yaffs_Object link = null;
			
			byte[] newname; int newnameIndex;
			
			ArrayPointer newnamePointer = new ArrayPointer();
			newdir = yaffsfs_FindDirectory(null,newpath,newpathIndex,newnamePointer,0);
			newname = newnamePointer.array; newnameIndex = newnamePointer.index; 
			
			if(!(newdir != null))
			{
				yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOTDIR);
				retVal = -1;
			}
			else if(newdir.myDev != obj.myDev)
			{
				yaffs2.utils.Globals.configuration.yaffsfs_SetError(-EXDEV);
				retVal = -1;
			}
			if(newdir != null && strlen(newname,newnameIndex) > 0)
			{
				link = yaffs_Link(newdir,newname,newnameIndex,obj);
				if(link != null)
					retVal = 0;
				else
				{
					yaffs2.utils.Globals.configuration.yaffsfs_SetError(-ENOSPC);
					retVal = -1;
				}

			}
		}
		yaffs2.utils.Globals.configuration.yaffsfs_Unlock();
		
		return retVal;
	}

//	static int yaffs_mknod(const char *pathname, mode_t mode, dev_t dev);

	static int yaffs_DumpDevStruct(byte[] path, int pathIndex)
	{
//		byte[] rest; int restIndex;
		
		ArrayPointer restPointer = new ArrayPointer();
		yaffs_Object obj = yaffsfs_FindRoot(path,pathIndex,restPointer);
//		rest = restPointer.array; restIndex = restPointer.index;
		
		if(obj != null)
		{
			yaffs_Device dev = obj.myDev;
			
			printf("\n" +
				   "nPageWrites.......... %d\n" +
				   "nPageReads........... %d\n" +
				   "nBlockErasures....... %d\n" +
				   "nGCCopies............ %d\n" +
				   "garbageCollections... %d\n" +
				   "passiveGarbageColl'ns %d\n" +
				   "\n",
					dev.nPageWrites,
					dev.nPageReads,
					dev.nBlockErasures,
					dev.nGCCopies,
					dev.garbageCollections,
					dev.passiveGarbageCollections
			);
			
		}
		return 0;
	}
}
