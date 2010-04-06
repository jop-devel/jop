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

public class PathConf3Res extends ResultType {
	private PostOpAttr objAttributes = new PostOpAttr();
	private int linkmax;
	private int nameMax;
	private boolean noTrunc;
	private boolean chownRestricted;
	private boolean caseInsensitive;
	private boolean casePreserving;
	
	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public int getLinkmax() {
		return linkmax;
	}

	public int getNameMax() {
		return nameMax;
	}

	public boolean isNoTrunc() {
		return noTrunc;
	}

	public boolean isChownRestricted() {
		return chownRestricted;
	}

	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public boolean isCasePreserving() {
		return casePreserving;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		this.error = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (this.error == NfsConst.NFS3_OK) {
			linkmax = Xdr.getNextInt(sb);
			nameMax = Xdr.getNextInt(sb);
			noTrunc = Xdr.getNextInt(sb) == NfsConst.TRUE;
			chownRestricted = Xdr.getNextInt(sb) == NfsConst.TRUE;
			caseInsensitive = Xdr.getNextInt(sb) == NfsConst.TRUE;
			casePreserving = Xdr.getNextInt(sb) == NfsConst.TRUE;
			return true;
		} else {
			return false;
		}
	}
}
