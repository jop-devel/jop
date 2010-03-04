/*
 * Copyright (c) Daniel Reichhard, daniel.reichhard@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Daniel Reichhard
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Sattr3 {


	/**
	 * protection mode bits:
	 * <pre>
	 * 0x00800 Set user ID on execution.
	 * 0x00400 Set group ID on execution.
	 * 0x00200 Save swapped text (not defined in POSIX).
	 * 0x00100 Read permission for owner.
	 * 0x00080 Write permission for owner.
	 * 0x00040 Execute permission for owner on a file. Or lookup
	 *        (search) permission for owner in directory.
	 * 0x00020 Read permission for group.
	 * 0x00010 Write permission for group.
	 * 0x00008 Execute permission for group on a file. Or lookup
	 *        (search) permission for group in directory.
	 * 0x00004 Read permission for others.
	 * 0x00002 Write permission for others.
	 * 0x00001 Execute permission for others on a file. Or lookup
	 *        (search) permission for others in directory.
	 * </pre>
	 */
	protected int		mode; 
	protected int		modeset;
	/**
	 * user ID of the file owner
	 */
	protected int		uid; 
	protected int		uidset;
	/**
	 * group ID of the file group
	 */
	protected int		gid;
	protected int		gidset;
	/**
	 * size in bytes
	 */
	protected long	size;
	protected int		sizeset;
	/**
	 * last access time
	 */
	protected Nfstime3   atime = new Nfstime3(); 
	protected int		atimeset;
	/**
	 * last modification time
	 */
	protected Nfstime3   mtime = new Nfstime3();
	protected int		mtimeset;
    
	/**
	 * sets the given mode
	 * @param mode	possible values:
	 * <pre>
	 * 0x00800 Set user ID on execution.
	 * 0x00400 Set group ID on execution.
	 * 0x00200 Save swapped text (not defined in POSIX).
	 * 0x00100 Read permission for owner.
	 * 0x00080 Write permission for owner.
	 * 0x00040 Execute permission for owner on a file. Or lookup
	 *        (search) permission for owner in directory.
	 * 0x00020 Read permission for group.
	 * 0x00010 Write permission for group.
	 * 0x00008 Execute permission for group on a file. Or lookup
	 *        (search) permission for group in directory.
	 * 0x00004 Read permission for others.
	 * 0x00002 Write permission for others.
	 * 0x00001 Execute permission for others on a file. Or lookup
	 *        (search) permission for others in directory.
	 * </pre>
	 */
	public void setMode(int mode) {
		this.mode = mode;
		this.modeset = NfsConst.TRUE;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
		this.uidset = NfsConst.TRUE;
	}
	
	public void setGid(int gid) {
		this.gid = gid;
		this.gidset = NfsConst.TRUE;
	}
	
	public void setSize(long size) {
		this.size = size;
		this.sizeset = NfsConst.TRUE;
	}
	
	public void setAtime(Nfstime3 atime, int time_how) {
		if (time_how == NfsConst.SET_TO_CLIENT_TIME ) {
			this.atime = atime;
		}
		if (time_how == NfsConst.DONT_CHANGE | time_how == NfsConst.SET_TO_CLIENT_TIME | time_how == NfsConst.SET_TO_SERVER_TIME) {
			this.atimeset = time_how;
		}
	}
	
	public void setMtime(Nfstime3 mtime, int time_how) {
		if (time_how == NfsConst.SET_TO_CLIENT_TIME ) {
			this.mtime = mtime;
		} 
		if (time_how == NfsConst.DONT_CHANGE | time_how == NfsConst.SET_TO_CLIENT_TIME | time_how == NfsConst.SET_TO_SERVER_TIME) {
			this.mtimeset = time_how;
		}
	}
	
	/**
	 * append NFS representation of this objects fields to a StringBuffer<br/>
	 * @param sb	the StringBuffer to append to
	 */
	public void appendToStringBuffer(StringBuffer sb) {
		Xdr.append(sb, modeset);
		if (modeset == NfsConst.TRUE) {
			Xdr.append(sb, mode);
		}
		Xdr.append(sb, uidset);
		if (uidset == NfsConst.TRUE) {
			Xdr.append(sb, uid);
		}
		Xdr.append(sb, gidset);
		if (gidset == NfsConst.TRUE) {
			Xdr.append(sb,gid);
		}
		Xdr.append(sb, sizeset);
		if (sizeset == NfsConst.TRUE) {
			Xdr.append(sb,size);
		} 
		Xdr.append(sb, atimeset);
		if (atimeset == NfsConst.SET_TO_CLIENT_TIME) {
			Xdr.append(sb, atime.getSeconds());
			Xdr.append(sb, atime.getNseconds());
		}
		Xdr.append(sb,mtimeset);
		if (mtimeset == NfsConst.SET_TO_CLIENT_TIME) {
			Xdr.append(sb, mtime.getSeconds());
			Xdr.append(sb, mtime.getNseconds());
		}
    }
	
	public void resetFields() {
		mode = 0; 
		modeset = NfsConst.FALSE;
		uid = 0; 
		uidset = NfsConst.FALSE;
		gid = 0;
		gidset = NfsConst.FALSE;
		size = 0;
		sizeset = NfsConst.FALSE;
		atime.setSeconds(0);
		atime.setNseconds(0); 
		atimeset = NfsConst.FALSE;
		mtime.setSeconds(0);
		mtime.setNseconds(0);
		mtimeset = NfsConst.FALSE;
	}
    
   /* public void loadFromStringBuffer(StringBuffer sb) {
    	mode = Xdr.getNextInt(sb);
    	uid = Xdr.getNextInt(sb);
    	gid = Xdr.getNextInt(sb);
    	size = Xdr.getNextLong(sb);
    	atime.seconds = Xdr.getNextInt(sb);
    	atime.nseconds = Xdr.getNextInt(sb);
    	mtime.seconds = Xdr.getNextInt(sb);
    	mtime.nseconds = Xdr.getNextInt(sb);
    }*/
}
