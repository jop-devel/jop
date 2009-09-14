package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class SetAttr3Res extends ResultType {
	private WccData objWcc = new WccData();

	public WccData getObjWcc() {
		return objWcc;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		status = Xdr.getNextInt(sb);
		objWcc.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			return true;
		}
		return false;
	}

	public String toString() {
		return "objWcc:\n" + objWcc.toString();
	}
}
