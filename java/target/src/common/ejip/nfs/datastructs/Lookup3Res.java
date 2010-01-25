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

public class Lookup3Res extends ResultType {

	/**
	 * The file handle of the object corresponding to what.name.
	 */
	private StringBuffer object = new StringBuffer();
	/**
	 * The attributes of the object corresponding to what.name.
	 */
	private PostOpAttr objAttributes = new PostOpAttr();
	/**
	 * The post-operation attributes of the directory, what.dir.
	 */
	private PostOpAttr dirAttributes = new PostOpAttr();

	public StringBuffer getObject() {
		return object;
	}

	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public PostOpAttr getDirAttributes() {
		return dirAttributes;
	}

	public boolean loadFields(StringBuffer sb) {
		boolean retval;
		this.error = Xdr.getNextInt(sb);
		if (this.error == NfsConst.NFS3_OK) {
			Xdr.getNextStringBuffer(sb, object);
			objAttributes.loadFields(sb);
			retval = true;
		} else {
			retval = false;
		}
		dirAttributes.loadFields(sb);
 		return retval;
	}

	public String toString() {
		return "object:\t" + object.toString() + "\n"
				+ objAttributes.toString() + "\n" + dirAttributes.toString();
	}
}
