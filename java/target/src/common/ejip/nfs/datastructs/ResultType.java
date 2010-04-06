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

public abstract class ResultType implements Loadable {
	protected int error;
	//TODO: error zuweisung in allen klassen nachziehen
	public abstract boolean loadFields(StringBuffer sb);
	
	public int getError() {
		return error;
	}

	public String getErrorDescription() {
		switch (error) {
		case NfsConst.NFS3_OK:
			return "Call completed Successfully.";
		case NfsConst.NFS3ERR_PERM:
			return "Caller is not the owner, or a privileged user.";
		case NfsConst.NFS3ERR_NOENT:
			return "No such file or directory.";
		case NfsConst.NFS3ERR_IO:
			return "I/O error";
		case NfsConst.NFS3ERR_NXIO:
			return "I/O error: No such device or address.";
		case NfsConst.NFS3ERR_ACCES:
			return "Permission denied.";
		case NfsConst.NFS3ERR_EXIST:
			return "File exists.";
		case NfsConst.NFS3ERR_XDEV:
			return "Attempt to do a cross-device hard link.";
		case NfsConst.NFS3ERR_NODEV:
			return "No such device.";
		case NfsConst.NFS3ERR_NOTDIR:
			return "Not a directory.";
		case NfsConst.NFS3ERR_ISDIR:
			return "Is a directory.";
		case NfsConst.NFS3ERR_INVAL:
			return "Invalid or unsupported argument.";
		case NfsConst.NFS3ERR_FBIG:
			return "File too large.";
		case NfsConst.NFS3ERR_NOSPC:
			return "No space left on device.";
		case NfsConst.NFS3ERR_ROFS:
			return "Read-only file system.";
		case NfsConst.NFS3ERR_MLINK:
			return "Too many hard links.";
		case NfsConst.NFS3ERR_NAMETOOLONG:
			return "Filename too long.";
		case NfsConst.NFS3ERR_NOTEMPTY:
			return "Cannot remove non-empty directory.";
		case NfsConst.NFS3ERR_DQUOT:
			return "Quota hard limit exceeded.";
		case NfsConst.NFS3ERR_STALE:
			return "Invalid file handle.";
		case NfsConst.NFS3ERR_REMOTE:
			return "Too many levels of remote in path.";
		case NfsConst.NFS3ERR_BADHANDLE:
			return "Illegal NFS file handle.";
		case NfsConst.NFS3ERR_NOT_SYNC:
			return "Update synchronization mismatch was detected.";
		case NfsConst.NFS3ERR_BAD_COOKIE:
			return "Cookie is stale";
		case NfsConst.NFS3ERR_NOTSUPP:
			return "Operation is not supported.";
		case NfsConst.NFS3ERR_TOOSMALL:
			return "Buffer or request is too small.";
		case NfsConst.NFS3ERR_SERVERFAULT:
			return "An unknown error occurred on the server.";
		case NfsConst.NFS3ERR_BADTYPE:
			return "An attempt was made to create an object of a type not supported by the server.";
		case NfsConst.NFS3ERR_JUKEBOX:
			return "Could not complete request in a timely fashion.";
		default:
			return "Something is rotten in the state of Denmark.";
		}
	}
}
