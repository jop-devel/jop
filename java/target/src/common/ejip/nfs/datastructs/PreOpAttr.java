package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class PreOpAttr {
	WccAttr attributes = new WccAttr();
	
	public void getPreOpAttr(StringBuffer sb) {
		if (Xdr.getNextInt(sb) == NfsConst.TRUE) { //value follows
			attributes.getWccAttr(sb);
		}
	}
	
	public String toString() {
		return attributes.toString();
	}
}
