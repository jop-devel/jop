package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class ReadDirPlus3Res extends ReadDir3Res {
	private DirListPlus3 reply = new DirListPlus3(maxEntries);

	public ReadDirPlus3Res(int maxEntries) {
		super(maxEntries);
	}


	protected class DirListPlus3 {
		private EntryPlus3 entries = new EntryPlus3();
		private boolean eof;

		public EntryPlus3 getEntries() {
			return entries;
		}

		public boolean isEof() {
			return eof;
		}

		public DirListPlus3(int maxEntries) {
			EntryPlus3 e;

			e = entries;
			for (int i = 0; i < maxEntries - 1; i++) {
				e.nextEntry = new EntryPlus3();
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

	protected class EntryPlus3 {
		private long fileID;
		private StringBuffer name = new StringBuffer();
		private long cookie;
		private PostOpAttr nameAttributes = new PostOpAttr();
		private PostOpFH3 nameHandle = new PostOpFH3();
		private EntryPlus3 nextEntry;

		public long getFileID() {
			return fileID;
		}

		public StringBuffer getName() {
			return name;
		}

		public long getCookie() {
			return cookie;
		}

		public PostOpAttr getNameAttributes() {
			return nameAttributes;
		}

		public PostOpFH3 getNameHandle() {
			return nameHandle;
		}

		public EntryPlus3 getNextEntry() {
			return nextEntry;
		}

		public void loadFields(StringBuffer sb) {
			fileID = Xdr.getNextLong(sb);
			Xdr.getNextStringBuffer(sb, name);
			cookie = Xdr.getNextLong(sb);
			nameAttributes.loadFields(sb);
			nameHandle.loadFields(sb);
			if (Xdr.getNextInt(sb) == NfsConst.NFS3_OK) { // value follows
				if (this.nextEntry != null) { // otherwise we're out of possible
												// entries in the datastructure
					nextEntry.loadFields(sb);
				} else {
					System.out
							.println("ReadDirPlus3Res.loadFields(): reached MAX_ENTRIES, but there are still more files to list");
				}
			}
		}
	}

}
