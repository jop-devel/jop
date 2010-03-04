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

public class FSStat3Res extends ResultType {

	private PostOpAttr objAttributes = new PostOpAttr();
	private long tbytes;
	private long fbytes;
	private long abytes;
	private long tfiles;
	private long ffiles;
	private long afiles;
	private int invarsec;
	
	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public long getTbytes() {
		return tbytes;
	}

	public long getFbytes() {
		return fbytes;
	}

	public long getAbytes() {
		return abytes;
	}

	public long getTfiles() {
		return tfiles;
	}

	public long getFfiles() {
		return ffiles;
	}

	public long getAfiles() {
		return afiles;
	}

	public int getInvarsec() {
		return invarsec;
	}

	public boolean loadFields(StringBuffer sb) {
		this.error = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (this.error == NfsConst.NFS3_OK) {
			tbytes = Xdr.getNextLong(sb);
			fbytes = Xdr.getNextLong(sb);
			abytes = Xdr.getNextLong(sb);
			tfiles = Xdr.getNextLong(sb);
			ffiles = Xdr.getNextLong(sb);
			afiles = Xdr.getNextLong(sb);
			invarsec = Xdr.getNextInt(sb);
			return true;
		} else {
			return false;
		}
	}
}
