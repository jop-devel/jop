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

package ejip.nfs;


import util.Timer;
import joprt.RtThread;
import util.Serial;
import util.Timer;
import com.jopdesign.sys.Const;
import ejip.*;
import ejip.examples.Pinger;
import ejip.nfs.datastructs.Access3Res;
import ejip.nfs.datastructs.Commit3Res;
import ejip.nfs.datastructs.CreateRes;
import ejip.nfs.datastructs.Diropargs3;
import ejip.nfs.datastructs.FSInfo3Res;
import ejip.nfs.datastructs.FSStat3Res;
import ejip.nfs.datastructs.Fattr3;
import ejip.nfs.datastructs.GetAttr3Res;
import ejip.nfs.datastructs.Link3Res;
import ejip.nfs.datastructs.Lookup3Res;
import ejip.nfs.datastructs.Nfstime3;
import ejip.nfs.datastructs.PathConf3Res;
import ejip.nfs.datastructs.Read3Res;
import ejip.nfs.datastructs.ReadDir3Res;
import ejip.nfs.datastructs.ReadDirPlus3Res;
import ejip.nfs.datastructs.Remove3Res;
import ejip.nfs.datastructs.Rename3Res;
import ejip.nfs.datastructs.Sattr3;
import ejip.nfs.datastructs.ServicePort;
import ejip.nfs.datastructs.SetAttr3Res;
import ejip.nfs.datastructs.Specdata3;
import ejip.nfs.datastructs.Write3Res;

public class Nfs {
	private int nfsPort;
	private int prog;
	private int vers;
	private StringBuffer messageBuffer = new StringBuffer();
	private NfsClient nc;
	private int uid = 0;
	private int gid = 0;
	
	public Nfs(int prog, int vers, NfsClient nc) {
		this.prog = prog;
		this.vers = vers;
		this.nc = nc;
	}
	
	public void setNfsPort(int port) {
		nfsPort = port;
	}
	
	public void setUID(int uid) {
		this.uid = uid;
	}
	
	public void setGID(int gid) {
		this.gid = gid;
	}
	
	public int getNfsPort() {
		return nfsPort;
	}

	public void requestNfsPort(ServicePort port) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.PMAPPROC_GETPORT, port, NfsConst.NFS_PROGRAM), RpcConst.AUTH_SYS, uid, gid, RpcConst.PMAP_PROG, RpcConst.PMAP_VERS, NfsConst.PMAPPROC_GETPORT, nc.hostname);
		//append program (portmap) specific part:
		Xdr.append(messageBuffer, NfsConst.NFS_PROGRAM);
		Xdr.append(messageBuffer, NfsConst.NFS_VERSION);
		Xdr.append(messageBuffer,RpcConst.IPPROTO_UDP);
		Xdr.append(messageBuffer,0); //port is ignored

		nc.sendBuffer(messageBuffer, nc.portmapperPort);
	}
	
	public void nullCall() {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_NULL, null), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.NFS3PROC3_NULL, nc.hostname); 
		// no data to append
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * @param fHandle	filehandle (as returned from lookup, create, mkdir, symlink, mknod, readdirplus or mount)
	 * @param  
	 */
	public void getAttr(StringBuffer fHandle, GetAttr3Res getAttrRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_GETATTR, getAttrRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_GETATTR, nc.hostname); 
		Xdr.append(messageBuffer, fHandle);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * Set file attributes sattr on the file fHandle
	 * @param fHandle	the handle of the file to modify
	 * @param sattr	the new file attributes
	 * @param guard	if set, the attributes won't be set if the actual ctime on the server is newer than guard
	 * @param setAttrRes the object that receives the result of the call
	 */
	public void setAttr(StringBuffer fHandle, Sattr3 sattr, Nfstime3 guard, SetAttr3Res setAttrRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_SETATTR, setAttrRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_SETATTR, nc.hostname);
		Xdr.append(messageBuffer, fHandle);
		sattr.appendToStringBuffer(messageBuffer);
		if (guard != null) {
			Xdr.append(messageBuffer, NfsConst.TRUE);
			Xdr.append(messageBuffer, guard.getSeconds());
			Xdr.append(messageBuffer, guard.getNseconds());
		} else {
			Xdr.append(messageBuffer, NfsConst.FALSE);
		}
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * lookup filename
	 * @param what	handle of directory and filename to look up
	 * @param lookupRes a Lookup3Res to write the result to
	 */
	public void lookup(Diropargs3 what, Lookup3Res lookupRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_LOOKUP, lookupRes), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.NFS3PROC3_LOOKUP, nc.hostname);
		what.appendToStringBuffer(messageBuffer);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * @param objectHandle
	 * @param access
	 *            a combination of ACCESS3_READ, ACCESS3_LOOKUP, ACCESS3_MODIFY,
	 *            ACCESS3_EXTEND, ACCESS3_DELETE, ACCESS3_EXECUTE
	 * @param accessRes
	 */
	public void access(StringBuffer objectHandle, int access, Access3Res accessRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_ACCESS, accessRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_ACCESS, nc.hostname);
		Xdr.append(messageBuffer, objectHandle);
		Xdr.append(messageBuffer, access);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void read(StringBuffer file, long offset, int count, Fattr3 postOpAttr, int countReturn, boolean eof, StringBuffer data, Read3Res readRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_READ, readRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_READ, nc.hostname);
		Xdr.append(messageBuffer, file);
		Xdr.append(messageBuffer, offset);
		Xdr.append(messageBuffer, count);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * Write data to file
	 * @param fileHandle The file handle for the file to which data is to be written. This must identify a file system object of type, NF3REG
	 * @param offset The position within the file at which the write is to begin. An offset of 0 means to write data starting at the beginning of the file.
	 * @param count The number of bytes of data to be written. If count is 0, the WRITE will succeed and return a count of 0, barring errors due to permissions checking. The size of data must be less than or equal to the value of the wtmax field in the FSINFO reply structure for the file system that contains file. If greater, the server may write only wtmax bytes, resulting in a short write.
	 * @param stable One of FILE_SYNC, DATA_SYNC or UNSTABLE. <br/>FILE_SYNC: server must commit the data plus file system metadata to stable storage before returning results.<br/>DATA_SYNC: server must commit data to stable storage and enough of the metadata to retrieve the data before returning.<br/>UNSTABLE: server can return in any state.
	 * @param data The data to be written to the file.
	 */
	public void write(StringBuffer fileHandle, long offset, int count, int stable, StringBuffer data, Write3Res writeRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_WRITE, writeRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_WRITE, nc.hostname);
		Xdr.append(messageBuffer, fileHandle);
		Xdr.append(messageBuffer, offset);
		Xdr.append(messageBuffer, count);
		Xdr.append(messageBuffer, stable);
		Xdr.append(messageBuffer, data);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * 
	 * @param where
	 * @param mode
	 * @param objAttributes 
	 */
	public void create(Diropargs3 where, int mode, Sattr3 objAttributes, StringBuffer verf, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_CREATE, createRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_CREATE, nc.hostname);
		where.appendToStringBuffer(messageBuffer);
		Xdr.append(messageBuffer, mode);
		switch (mode) {
		case 0: //fall through
			System.out.println("falling");
		case 1:
			objAttributes.appendToStringBuffer(messageBuffer);
			break;
		case 2:
			if (verf != null) {
				if (verf.length() != NfsConst.NFS3_CREATEVERFSIZE) {
					verf.setLength(NfsConst.NFS3_CREATEVERFSIZE);
				}
				Xdr.appendRaw(messageBuffer, verf);
			}
			break;
		}
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void mkDir(Diropargs3 where, Sattr3 attributes, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_MKDIR, createRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_MKDIR, nc.hostname);
		where.appendToStringBuffer(messageBuffer);
		attributes.appendToStringBuffer(messageBuffer);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void symLink(Diropargs3 where, Sattr3 symlinkAttributes, StringBuffer symlinkData, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_SYMLINK, createRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_SYMLINK, nc.hostname);
		where.appendToStringBuffer(messageBuffer);
		symlinkAttributes.appendToStringBuffer(messageBuffer);
		Xdr.append(messageBuffer, symlinkData);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * @param where
	 * @param type
	 *            one of the following values: NF3CHR, NF3BLK, NF3SOCK, NF3FIFO
	 * @param devAttributes
	 * @param spec
	 * @param pipeAttributes
	 * @param createRes
	 */
	public void mkNod(Diropargs3 where, int type, Sattr3 devAttributes, Specdata3 spec, Sattr3 pipeAttributes, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_MKNOD, createRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_MKNOD, nc.hostname);
		where.appendToStringBuffer(messageBuffer);
		Xdr.append(messageBuffer, type);
		switch (type) {
		case NfsConst.NF3CHR: // fall through
		case NfsConst.NF3BLK:
			devAttributes.appendToStringBuffer(messageBuffer);
			spec.appendToStringBuffer(messageBuffer);
			break;
		case NfsConst.NF3SOCK: // fall through
		case NfsConst.NF3FIFO:
			pipeAttributes.appendToStringBuffer(messageBuffer);
			break;
		default:
			break;
		}
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void remove(Diropargs3 object, Remove3Res removeRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_REMOVE, removeRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_REMOVE, nc.hostname);
		object.appendToStringBuffer(messageBuffer);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void rmDir(Diropargs3 object, Remove3Res rmDirRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_RMDIR, rmDirRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_RMDIR, nc.hostname);
		object.appendToStringBuffer(messageBuffer);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void rename(Diropargs3 from, Diropargs3 to, Rename3Res renameRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_RENAME, renameRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_RENAME, nc.hostname);
		from.appendToStringBuffer(messageBuffer);
		to.appendToStringBuffer(messageBuffer);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void link(StringBuffer fileHandle, Diropargs3 link, Link3Res linkRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_LINK, linkRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_LINK, nc.hostname);
		Xdr.append(messageBuffer, fileHandle);
		link.appendToStringBuffer(messageBuffer);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void readDir(StringBuffer dirHandle, long cookie, StringBuffer cookieverf, int count, ReadDir3Res readDirRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_READDIR, readDirRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_READDIR, nc.hostname);
		Xdr.append(messageBuffer, dirHandle);
		Xdr.append(messageBuffer, cookie);
		if (cookieverf != null) {
			if (cookieverf.length() != NfsConst.NFS3_COOKIEVERFSIZE) {
				cookieverf.setLength(NfsConst.NFS3_COOKIEVERFSIZE);
			}
			Xdr.append(messageBuffer, cookieverf);
		}
		Xdr.append(messageBuffer, count);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void readDirPlus(StringBuffer dirHandle, long cookie, StringBuffer cookieverf, int dirCount, int maxCount, ReadDirPlus3Res readDirPlusRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_READDIRPLUS, readDirPlusRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_READDIRPLUS, nc.hostname);
		Xdr.append(messageBuffer, dirHandle);
		Xdr.append(messageBuffer, cookie);
		if (cookieverf != null) {
			if (cookieverf.length() != NfsConst.NFS3_COOKIEVERFSIZE) {
				cookieverf.setLength(NfsConst.NFS3_COOKIEVERFSIZE);
			}
			Xdr.append(messageBuffer, cookieverf);
		}
		Xdr.append(messageBuffer, dirCount);
		Xdr.append(messageBuffer, maxCount);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void fsStat(StringBuffer fsroot, FSStat3Res fsStatRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_FSSTAT, fsStatRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_FSSTAT, nc.hostname);
		Xdr.append(messageBuffer, fsroot);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void fsInfo(StringBuffer fsroot, FSInfo3Res fsInfoRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_FSINFO, fsInfoRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_FSINFO, nc.hostname);
		Xdr.append(messageBuffer, fsroot);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void pathConf(StringBuffer object, PathConf3Res pathConfRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_PATHCONF, pathConfRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_PATHCONF, nc.hostname);
		Xdr.append(messageBuffer, object);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
	
	public void commit(StringBuffer fileHandle, long offset, int count, Commit3Res commitRes) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.NFS3PROC3_COMMIT, commitRes), RpcConst.AUTH_SYS, uid, gid, prog, vers, NfsConst.NFS3PROC3_COMMIT, nc.hostname);
		Xdr.append(messageBuffer, fileHandle);
		Xdr.append(messageBuffer, offset);
		Xdr.append(messageBuffer, count);
		nc.sendBuffer(messageBuffer, nfsPort);
	}
}
