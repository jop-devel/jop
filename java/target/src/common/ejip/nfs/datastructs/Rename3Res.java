package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Rename3Res extends ResultType {
	private WccData fromDirWcc = new WccData();
	private WccData toDirWcc = new WccData();
	
	public WccData getFromDirWcc() {
		return fromDirWcc;
	}

	public WccData getToDirWcc() {
		return toDirWcc;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;

		status = Xdr.getNextInt(sb);
		fromDirWcc.loadFields(sb);
		toDirWcc.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			return true;
		} else {
			return false;
		}
	}
}
