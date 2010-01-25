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

public class Fattr3 {

	/**
	 * type of file: NF3REG, NF3DIR, NF3BLK, NF3CHR, NF3LNK, NF3SOCK, NF3FIFO
	 */
	private int type;
	private int mode;
	private int nlink; // number of hard links to the file
	private int uid;
	private int gid;
	private long size;
	private long used; // number of bytes of disk space used
	private Specdata3 rdev = new Specdata3();
	private long fsid; // file system identifier for the file system
	private long fileid; // ID for the file within its file system (=inumber)
	private Nfstime3 atime = new Nfstime3();
	private Nfstime3 mtime = new Nfstime3();
	private Nfstime3 ctime = new Nfstime3(); // last attribute change time
	
	public int getType() {
		return type;
	}


	public int getMode() {
		return mode;
	}


	public int getNlink() {
		return nlink;
	}


	public int getUid() {
		return uid;
	}


	public int getGid() {
		return gid;
	}


	public long getSize() {
		return size;
	}


	public long getUsed() {
		return used;
	}


	public Specdata3 getRdev() {
		return rdev;
	}


	public long getFsid() {
		return fsid;
	}


	public long getFileid() {
		return fileid;
	}


	public Nfstime3 getAtime() {
		return atime;
	}


	public Nfstime3 getMtime() {
		return mtime;
	}


	public Nfstime3 getCtime() {
		return ctime;
	}
	

	public String toString() {
		return "Type:\t" + type + "\n" +
			"Mode:\t" + mode + "\n" +
			"NLink:\t" + nlink + "\n" +
			"UID:\t" + uid + "\n" +
			"GID:\t" + gid + "\n" +
			"Size:\t" + size + "\n" +
			"Used:\t" + used + "\n" +
			"RDev:\n" + rdev.toString() + "\n" +
			"FSID:\t" + fsid + "\n" +
			"FileID:\t" + fileid + "\n" +
			"aTime:\n" + atime.toString() + "\n" +
			"mTime:\n" + mtime.toString() + "\n" +
			"cTime:\n" + ctime.toString();
	}
		

	public void appendToStringBuffer(StringBuffer sb) {
		Xdr.append(sb, type);
		Xdr.append(sb, mode);
		Xdr.append(sb, nlink);
		Xdr.append(sb, uid);
		Xdr.append(sb, gid);
		Xdr.append(sb, size);
		Xdr.append(sb, used);
		Xdr.append(sb, rdev.specdata1);
		Xdr.append(sb, rdev.specdata2);
		Xdr.append(sb, fsid);
		Xdr.append(sb, fileid);
		// TODO: write nfstime3.writeFields(StringBuffer sb)
		Xdr.append(sb, atime.getSeconds());
		Xdr.append(sb, atime.getNseconds());
		Xdr.append(sb, mtime.getSeconds());
		Xdr.append(sb, mtime.getNseconds());
		Xdr.append(sb, ctime.getSeconds());
		Xdr.append(sb, ctime.getNseconds());
	}

	public void loadFields(StringBuffer sb) {
		type = Xdr.getNextInt(sb);
		mode = Xdr.getNextInt(sb);
		nlink = Xdr.getNextInt(sb);
		uid = Xdr.getNextInt(sb);
		gid = Xdr.getNextInt(sb);
		size = Xdr.getNextLong(sb);
		used = Xdr.getNextLong(sb);
		rdev.loadFields(sb);
		fsid = Xdr.getNextLong(sb);
		fileid = Xdr.getNextLong(sb);
		atime.loadFields(sb);
		mtime.loadFields(sb);
		ctime.loadFields(sb);
	}
}
