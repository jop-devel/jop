package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Read3Res extends ResultType {
	private PostOpAttr fileAttributes = new PostOpAttr();
	private int count;
	private boolean eof;
	private StringBuffer data = new StringBuffer();
	
	public PostOpAttr getFileAttributes() {
		return fileAttributes;
	}

	public int getCount() {
		return count;
	}

	public boolean isEof() {
		return eof;
	}

	public StringBuffer getData() {
		return data;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		status = Xdr.getNextInt(sb);
		fileAttributes.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			count = Xdr.getNextInt(sb);
			eof = Xdr.getNextInt(sb) == 1;
			Xdr.getNextStringBuffer(sb,data);
			return true;
		} else {
			return false;
		}
	}
}
