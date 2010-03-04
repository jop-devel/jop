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

import ejip.nfs.datastructs.MountRes3;
import ejip.nfs.datastructs.MountList;
import ejip.nfs.datastructs.Exports;
import ejip.nfs.datastructs.ServicePort;

public class Mount {
	private int mountPort;
	private int prog;
	private int vers;
	private StringBuffer messageBuffer = new StringBuffer();
	private NfsClient nc;
	private int uid = 0;
	private int gid = 0;
	
	
	public Mount(int prog, int vers, NfsClient nc) {
		this.prog = prog;
		this.vers = vers;
		this.nc = nc;
	}
	
	public void setMountPort(int port) {
		mountPort = port;
	}
	
	public void setUID(int uid) {
		this.uid = uid;
	}
	
	public void setGID(int gid) {
		this.gid = gid;
	}

	public int getMountPort() {
		return mountPort;
	}
	
	public void requestMountPort(ServicePort port) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.PMAPPROC_GETPORT, port, NfsConst.MOUNT_PROGRAM), RpcConst.AUTH_SYS, uid, gid, RpcConst.PMAP_PROG, RpcConst.PMAP_VERS, NfsConst.PMAPPROC_GETPORT, nc.hostname);
		//append program (portmap) specific part:
		Xdr.append(messageBuffer, NfsConst.MOUNT_PROGRAM);
		Xdr.append(messageBuffer, NfsConst.MOUNT_VERSION);
		Xdr.append(messageBuffer,RpcConst.IPPROTO_UDP);
		Xdr.append(messageBuffer,0); //port is ignored

		nc.sendBuffer(messageBuffer, nc.portmapperPort);
	}
	
	public void nullCall() {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_NULL, null), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.MOUNTPROC3_NULL, nc.hostname); // no data to append
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void getExports(Exports exports) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_EXPORT, exports), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.MOUNTPROC3_EXPORT, nc.hostname); // no data to append
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void dump(MountList mountList) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_DUMP, mountList), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.MOUNTPROC3_DUMP, nc.hostname); 
//		Nfs.Mount.dump(messageBuffer, RpcConst.AUTH_SYS, xid);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void mount(StringBuffer directory, MountRes3 mountres) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_MNT, mountres), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.MOUNTPROC3_MNT, nc.hostname);
		Xdr.append(messageBuffer, directory);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void unmountAll() {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_UMNTALL, null), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.MOUNTPROC3_UMNTALL, nc.hostname);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void unmount(StringBuffer path) {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_UMNT, null), RpcConst.AUTH_NULL, uid, gid, prog, vers, NfsConst.MOUNTPROC3_UMNT, nc.hostname);
		Xdr.append(messageBuffer, path);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
}
