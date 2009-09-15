package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class PathConf3Res extends ResultType {
	private PostOpAttr objAttributes = new PostOpAttr();
	private int linkmax;
	private int nameMax;
	private boolean noTrunc;
	private boolean chownRestricted;
	private boolean caseInsensitive;
	private boolean casePreserving;
	
	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public int getLinkmax() {
		return linkmax;
	}

	public int getNameMax() {
		return nameMax;
	}

	public boolean isNoTrunc() {
		return noTrunc;
	}

	public boolean isChownRestricted() {
		return chownRestricted;
	}

	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public boolean isCasePreserving() {
		return casePreserving;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		status = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			linkmax = Xdr.getNextInt(sb);
			nameMax = Xdr.getNextInt(sb);
			noTrunc = Xdr.getNextInt(sb) == NfsConst.TRUE;
			chownRestricted = Xdr.getNextInt(sb) == NfsConst.TRUE;
			caseInsensitive = Xdr.getNextInt(sb) == NfsConst.TRUE;
			casePreserving = Xdr.getNextInt(sb) == NfsConst.TRUE;
			return true;
		} else {
			return false;
		}
	}
}
