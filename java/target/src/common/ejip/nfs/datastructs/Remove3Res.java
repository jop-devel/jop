package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Remove3Res extends ResultType {
	private WccData dirWcc = new WccData();

	public WccData getDirWcc() {
		return dirWcc;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;

		status = Xdr.getNextInt(sb);
		dirWcc.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			return true;
		} else {
			return false;
		}
	}
}
