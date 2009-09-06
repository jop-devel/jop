package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class ReadLink3Res {
	private PostOpAttr	symlinkAttributes = new PostOpAttr();
	private StringBuffer data = new StringBuffer();
	
	public PostOpAttr getSymlinkAttributes() {
		return symlinkAttributes;
	}

	public StringBuffer getData() {
		return data;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		status = Xdr.getNextInt(sb);
		symlinkAttributes.loadFields(sb);
		if (status == NfsConst.TRUE) {
			Xdr.getNextStringBuffer(sb,data);
			return true;
		} else {
			return false;
		}
	}
	
}
