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
		int status;

		status = Xdr.getNextInt(sb);
		dirAttributes.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
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
					System.out.println("ReadDir3Res.loadFields(): reached MAX_ENTRIES, but there are still more files to list");
				}
			}
		}
	}
}
