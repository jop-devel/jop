package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class FSStat3Res extends ResultType {

	private PostOpAttr objAttributes = new PostOpAttr();
	private long tbytes;
	private long fbytes;
	private long abytes;
	private long tfiles;
	private long ffiles;
	private long afiles;
	private int invarsec;
	
	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public long getTbytes() {
		return tbytes;
	}

	public long getFbytes() {
		return fbytes;
	}

	public long getAbytes() {
		return abytes;
	}

	public long getTfiles() {
		return tfiles;
	}

	public long getFfiles() {
		return ffiles;
	}

	public long getAfiles() {
		return afiles;
	}

	public int getInvarsec() {
		return invarsec;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;

		status = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			tbytes = Xdr.getNextLong(sb);
			fbytes = Xdr.getNextLong(sb);
			abytes = Xdr.getNextLong(sb);
			tfiles = Xdr.getNextLong(sb);
			ffiles = Xdr.getNextLong(sb);
			afiles = Xdr.getNextLong(sb);
			invarsec = Xdr.getNextInt(sb);
			return true;
		} else {
			return false;
		}
	}
}
