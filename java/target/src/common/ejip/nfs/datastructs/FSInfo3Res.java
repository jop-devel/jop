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

public class FSInfo3Res extends ResultType {
	PostOpAttr objAttributes = new PostOpAttr();
	private int rtmax;
	private int rtpref;
	private int rtmult;
	private int wtmax;
	private int wtpref;
	private int wtmult;
	private int dtpref;
	private long maxfilesize;
	private Nfstime3 timeDelta = new Nfstime3();
	/**
	 * one of FSF3_LINK, FSF3_SYMLINK, FSF3_HOMOGENOUS, FSF3_CANSETTIME
	 */
	private int properties;

	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public int getRtmax() {
		return rtmax;
	}

	public int getRtpref() {
		return rtpref;
	}

	public int getRtmult() {
		return rtmult;
	}

	public int getWtmax() {
		return wtmax;
	}

	public int getWtpref() {
		return wtpref;
	}

	public int getWtmult() {
		return wtmult;
	}

	public int getDtpref() {
		return dtpref;
	}

	public long getMaxfilesize() {
		return maxfilesize;
	}

	public Nfstime3 getTimeDelta() {
		return timeDelta;
	}

	public int getProperties() {
		return properties;
	}

	public boolean loadFields(StringBuffer sb) {
		this.error = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (this.error == NfsConst.NFS3_OK) {
			rtmax = Xdr.getNextInt(sb);
			rtpref = Xdr.getNextInt(sb);
			rtmult = Xdr.getNextInt(sb);
			wtmax = Xdr.getNextInt(sb);
			wtpref = Xdr.getNextInt(sb);
			wtmult = Xdr.getNextInt(sb);
			dtpref = Xdr.getNextInt(sb);
			maxfilesize = Xdr.getNextLong(sb);
			timeDelta.loadFields(sb);
			properties = Xdr.getNextInt(sb);
			return true;
		} else {
			return false;
		}
	}
}
