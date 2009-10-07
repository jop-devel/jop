package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class PostOpFH3 {
	StringBuffer handle = new StringBuffer();

	public void loadFields(StringBuffer sb) {
		if (Xdr.getNextInt(sb) == NfsConst.TRUE) {
			Xdr.getNextStringBuffer(sb, handle);
		}
	}
}
