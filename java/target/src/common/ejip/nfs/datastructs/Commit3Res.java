package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Commit3Res extends ResultType {
	private WccData fileWcc = new WccData();
	private StringBuffer verf = new StringBuffer(NfsConst.NFS3_WRITEVERFSIZE);

	public WccData getFileWcc() {
		return fileWcc;
	}

	public StringBuffer getVerf() {
		return verf;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;

		status = Xdr.getNextInt(sb);
		fileWcc.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			Xdr.getBytes(sb, verf, NfsConst.NFS3_WRITEVERFSIZE);
			return true;
		} else {
			return false;
		}
	}
}
