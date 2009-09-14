/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Daniel Reichhard (daniel.reichhard@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ejip.nfs;

public class NfsConst {

	public static final int FALSE = 0;
	public static final int TRUE = 1;
	
	public static final int IPPROTO_TCP = 6;
	public static final int IPPROTO_UDP = 17;
	
	//sizes
	public static final int NFS3_FHSIZE = 64;
	public static final int NFS3_COOKIEVERFSIZE = 8;
	public static final int NFS3_CREATEVERFSIZE = 8;
	public static final int NFS3_WRITEVERFSIZE = 8;
	
	//portmapper procedures
	public static final int PMAPPROC_NULL = 0;
	public static final int PMAPPROC_SET = 1;
	public static final int PMAPPROC_UNSET = 2;
	public static final int PMAPPROC_GETPORT = 3;
	public static final int PMAPPROC_DUMP = 4;
	public static final int PMAPPROC_CALLIT = 5;
	
	//Name of the Nfs-Client
	public static final String HOSTNAME = "JOP_NFS_Client";
	
	//NFS program constants
    public static final int NFS_PROGRAM = 100003;
    public static final int NFS_VERSION = 3;

    //MOUNT program constants
    public static final int MOUNT_PROGRAM = 100005;
    public static final int MOUNT_VERSION = 3;
    
    //NETWORK LOCK MANAGER program constants
    public static final int NLM_PROGRAM	= 100021;
    public static final int NLM_VERSION 	= 4;

    //NFS procedures
    //Do nothing
    public static final int NFS3PROC3_NULL        = 0;
    public static final int NFS3PROC3_GETATTR     = 1;
    public static final int NFS3PROC3_SETATTR     = 2;
    public static final int NFS3PROC3_LOOKUP      = 3;
    public static final int NFS3PROC3_ACCESS      = 4;
    public static final int NFS3PROC3_READLINK    = 5;
    public static final int NFS3PROC3_READ        = 6;
    public static final int NFS3PROC3_WRITE       = 7;
    public static final int NFS3PROC3_CREATE      = 8;
    public static final int NFS3PROC3_MKDIR       = 9;
    public static final int NFS3PROC3_SYMLINK     = 10;
    public static final int NFS3PROC3_MKNOD       = 11;
    public static final int NFS3PROC3_REMOVE      = 12;
    public static final int NFS3PROC3_RMDIR       = 13;
    public static final int NFS3PROC3_RENAME      = 14;
    public static final int NFS3PROC3_LINK        = 15;
    public static final int NFS3PROC3_READDIR     = 16;
    public static final int NFS3PROC3_READDIRPLUS = 17;
    public static final int NFS3PROC3_FSSTAT      = 18;
    public static final int NFS3PROC3_FSINFO      = 19;
    public static final int NFS3PROC3_PATHCONF    = 20;
    public static final int NFS3PROC3_COMMIT      = 21;
    
    //NFS return values
    public static final int NFS3_OK             = 0;
    public static final int NFS3ERR_PERM        = 1;
    public static final int NFS3ERR_NOENT       = 2;
    public static final int NFS3ERR_IO          = 5;
    public static final int NFS3ERR_NXIO        = 6;
    public static final int NFS3ERR_ACCES       = 13;
    public static final int NFS3ERR_EXIST       = 17;
    public static final int NFS3ERR_XDEV        = 18;
    public static final int NFS3ERR_NODEV       = 19;
    public static final int NFS3ERR_NOTDIR      = 20;
    public static final int NFS3ERR_ISDIR       = 21;
    public static final int NFS3ERR_INVAL       = 22;
    public static final int NFS3ERR_FBIG        = 27;
    public static final int NFS3ERR_NOSPC       = 28;
    public static final int NFS3ERR_ROFS        = 30;
    public static final int NFS3ERR_MLINK       = 31;
    public static final int NFS3ERR_NAMETOOLONG = 63;
    public static final int NFS3ERR_NOTEMPTY    = 66;
    public static final int NFS3ERR_DQUOT       = 69;
    public static final int NFS3ERR_STALE       = 70;
    public static final int NFS3ERR_REMOTE      = 71;
    public static final int NFS3ERR_BADHANDLE   = 10001;
    public static final int NFS3ERR_NOT_SYNC    = 10002;
    public static final int NFS3ERR_BAD_COOKIE  = 10003;
    public static final int NFS3ERR_NOTSUPP     = 10004;
    public static final int NFS3ERR_TOOSMALL    = 10005;
    public static final int NFS3ERR_SERVERFAULT = 10006;
    public static final int NFS3ERR_BADTYPE     = 10007;
    public static final int NFS3ERR_JUKEBOX     = 10008;
    
    //MOUNT procedures    
    public static final int MOUNTPROC3_NULL		= 0;
    public static final int MOUNTPROC3_MNT		= 1;
    public static final int MOUNTPROC3_DUMP		= 2;
    public static final int MOUNTPROC3_UMNT		= 3;
    public static final int MOUNTPROC3_UMNTALL	= 4;
    public static final int MOUNTPROC3_EXPORT	= 5;    
    
    //mount faults
    public static final int MNT3_OK = 0;                 /* no error */
    public static final int MNT3ERR_PERM = 1;            /* Not owner */
    public static final int MNT3ERR_NOENT = 2;           /* No such file or directory */
    public static final int MNT3ERR_IO = 5;              /* I/O error */
    public static final int MNT3ERR_ACCES = 13;          /* Permission denied */
    public static final int MNT3ERR_NOTDIR = 20;         /* Not a directory */
    public static final int MNT3ERR_INVAL = 22;          /* Invalid argument */
    public static final int MNT3ERR_NAMETOOLONG = 63;    /* Filename too long */
    public static final int MNT3ERR_NOTSUPP = 10004;     /* Operation not supported */
    public static final int MNT3ERR_SERVERFAULT = 10006;  /* A failure on the server */
    
    //ftype3 file types
    public static final byte NF3REG = 1;
    public static final	byte NF3DIR = 2;
    public static final byte NF3BLK = 3;
    public static final byte NF3CHR = 4;
    public static final byte NF3LNK = 5;
    public static final byte NF3SOCK = 6;
    public static final byte NF3FIFO = 7;
    
    //time_how values
    public static final int DONT_CHANGE = 0;
    public static final int SET_TO_SERVER_TIME = 1;
    public static final int SET_TO_CLIENT_TIME = 2;
    
    //valid access types for procedure ACCESS
    /**
     * Read data from file or read a directory.
     */
    public static final int ACCESS3_READ = 0x1;
    /**
     * Look up a name in a directory (no meaning for non-directory objects).
     */
    public static final int ACCESS3_LOOKUP = 0x2;
    /**
     * Rewrite existing file data or modify existing directory entries.
     */
    public static final int ACCESS3_MODIFY = 0x4;
    /**
     * Write new data or add directory entries.
     */
    public static final int ACCESS3_EXTEND = 0x8;
    /**
     * Delete an existing directory entry.
     */
    public static final int ACCESS3_DELETE = 0x10;
    /**
     * Execute file (no meaning for a directory).
     */
    public static final int ACCESS3_EXECUTE = 0x20;
    
    //values for FSINFO field "properties"
    public static final int FSF3_LINK = 0x1;
    public static final int FSF3_SYMLINK = 0x2;
    public static final int FSF3_HOMOGENOUS = 0x8;
    public static final int FSF3_CANSETTIME = 0x10;
    
    public static final byte STABLEHOW_UNSTABLE = 0;
    public static final byte STABLEHOW_DATA_SYNC = 1;
    public static final byte STABLEHOW_FILE_SYNC = 2;
}
