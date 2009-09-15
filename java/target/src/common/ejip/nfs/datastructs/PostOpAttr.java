package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class PostOpAttr {
	Fattr3 attributes = new Fattr3();

	public Fattr3 getAttributes() {
		return attributes;
	}

	public void loadFields(StringBuffer sb) {

		if (Xdr.getNextInt(sb) == NfsConst.TRUE) {
			attributes.loadFields(sb);
		}

	}

	public String toString() {
		return attributes.toString();
	}
}
