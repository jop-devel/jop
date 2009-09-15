package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Access3Res extends ResultType {
	private PostOpAttr objAttributes = new PostOpAttr();
	private int access;
	
	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public int getAccess() {
		return access;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		status = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			access = Xdr.getNextInt(sb);
			return true;
		} else {
			return false;
		}
	}
}
