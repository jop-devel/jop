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

public class ReadDir3Res extends ResultType {
	protected static int maxEntries;

	private PostOpAttr dirAttributes = new PostOpAttr();
	private StringBuffer cookieVerf = new StringBuffer(
			NfsConst.NFS3_COOKIEVERFSIZE);
	private DirList3 reply = new DirList3(maxEntries);

	public PostOpAttr getDirAttributes() {
		return dirAttributes;
	}

	public StringBuffer getCookieVerf() {
		return cookieVerf;
	}

	public DirList3 getReply() {
		return reply;
	}

	public ReadDir3Res(int maxEntries) {
		ReadDir3Res.maxEntries = maxEntries;
	}

	public boolean loadFields(StringBuffer sb) {
		this.error = Xdr.getNextInt(sb);
		dirAttributes.loadFields(sb);
		if (this.error == NfsConst.NFS3_OK) {
			Xdr.getBytes(sb, cookieVerf, NfsConst.NFS3_COOKIEVERFSIZE);
			reply.loadFields(sb);
			return true;
		} else {
			return false;
		}
	}

	protected class DirList3 {
		private Entry3 entries = new Entry3();
		private boolean eof;

		public Entry3 getEntries() {
			return entries;
		}

		public boolean isEof() {
			return eof;
		}

		public DirList3(int maxEntries) {
			Entry3 e;
			e = entries;
			for (int i = 0; i < maxEntries - 1; i++) {
				e.nextEntry = new Entry3();
				e = e.nextEntry;
			}
			e.nextEntry = null;
		}

		public void loadFields(StringBuffer sb) {
			if (Xdr.getNextInt(sb) == NfsConst.NFS3_OK) { // value follows
				entries.loadFields(sb);
			}
			eof = Xdr.getNextInt(sb) == 1;
		}
	}

	protected class Entry3 {
		private long fileID;
		private StringBuffer name = new StringBuffer();
		private long cookie;
		private Entry3 nextEntry;

		public long getFileID() {
			return fileID;
		}

		public StringBuffer getName() {
			return name;
		}

		public long getCookie() {
			return cookie;
		}

		public Entry3 getNextEntry() {
			return nextEntry;
		}

		public void loadFields(StringBuffer sb) {
			fileID = Xdr.getNextLong(sb);
			Xdr.getNextStringBuffer(sb, name);
			cookie = Xdr.getNextLong(sb);
			if (Xdr.getNextInt(sb) == NfsConst.NFS3_OK) { // value follows
				if (this.nextEntry != null) { // otherwise we're out of possible
												// entries in the datastructure
					nextEntry.loadFields(sb);
				} else {
					//System.out.println("ReadDir3Res.loadFields(): reached MAX_ENTRIES, but there are still more files to list");
				}
			}
		}
	}
}
