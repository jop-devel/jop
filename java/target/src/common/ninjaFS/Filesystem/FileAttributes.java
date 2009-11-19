/* *************************************************************************** *

	Copyright 2009 Georg Merzdovnik, Gerald Wodni

	This file is part of ninjaFS.

	ninjaFS is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 3 of the License, or
	(at your option) any later version.

	ninjaFS is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

* *************************************************************************** */

package ninjaFS.Filesystem;

import ninjaFS.Rpc.TimeValue;


public class FileAttributes {
	
	public FileAttributes( long filetype,			
						   long mode,
						   long nlink,
						   long uid,
						   long gid,
						   long size,
						   long blocksize,
						   long rdev,
						   long blocks,
						   long fsid,
						   long fileid,
						   TimeValue atime,
						   TimeValue mtime,
						   TimeValue ctime){
		
		this.filetype = filetype;			
		this.mode = mode;
		this.nlink = nlink;
		this.uid = uid;
		this.gid = gid;
		this.size = size;
		this.blocksize = blocksize; 
		this.rdev = rdev;
		this.blocks = blocks;
		this.fsid = fsid;
		this.fileid = fileid;
		this.atime = atime;
		this.mtime = mtime;
		this.ctime = ctime;
		
		
	}
	
	// type of the file
	public long filetype;
	
	public long mode;
	// number of hard links to the file
    public long nlink;
    // user identification number of the files owner
    public long uid;
    // group identification number of the files group
    public long gid;
    // file size in bytes
    public long size;
    // size of bytes of a files block
    public long blocksize;
    // device number of the file if it is type NFCHR or NFBLK;
    public long rdev;
    // number of blocks a file takes up on disc
    public long blocks;
    // filesystem identifier for the filesystem
    public long fsid;
    // Uniquely identifies file in filesystem
    public long fileid;
    // time value when the file was last accesed
    public TimeValue atime;
    // last modified
    public TimeValue mtime;
    // Status of the File was last changed
    public TimeValue ctime;
    
}
