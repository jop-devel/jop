package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class GetAttr3Res extends ResultType {
	private Fattr3 objAttributes = new Fattr3();
	
	public Fattr3 getObjAttributes() {
		return objAttributes;
	}

	public boolean loadFields(StringBuffer sb) {
		if (Xdr.getNextInt(sb) == NfsConst.NFS3_OK) {
			objAttributes.loadFields(sb);
			return true;
		}
		return false;
	}
	
	public String toString() {
		return objAttributes.toString();
	}
	
}


