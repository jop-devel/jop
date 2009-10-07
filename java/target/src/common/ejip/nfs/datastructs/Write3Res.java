package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Write3Res extends ResultType {
	private WccData fileWcc = new WccData();
	private int count;
	private int committed;
	private StringBuffer verf = new StringBuffer(NfsConst.NFS3_WRITEVERFSIZE);
	
	public WccData getFileWcc() {
		return fileWcc;
	}

	public int getCount() {
		return count;
	}

	public int getCommitted() {
		return committed;
	}

	public StringBuffer getVerf() {
		return verf;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		status = Xdr.getNextInt(sb);
		fileWcc.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			count = Xdr.getNextInt(sb);
			committed = Xdr.getNextInt(sb);
			Xdr.getBytes(sb, verf, NfsConst.NFS3_WRITEVERFSIZE);
			return true;
		} else {
			return false;
		}
	}
	
}
