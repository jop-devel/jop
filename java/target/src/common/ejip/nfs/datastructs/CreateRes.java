package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class CreateRes extends ResultType {
	private PostOpFH3 obj = new PostOpFH3();
	private PostOpAttr objAttributes = new PostOpAttr();
	private WccData dirWcc = new WccData();

	public PostOpFH3 getObj() {
		return obj;
	}

	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public WccData getDirWcc() {
		return dirWcc;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		boolean retval;

		status = Xdr.getNextInt(sb);
		if (status == NfsConst.NFS3_OK) {
			obj.loadFields(sb);
			objAttributes.loadFields(sb);
			retval = true;
		} else {
			retval = false;
		}
		dirWcc.loadFields(sb);
		return retval;
	}
}
