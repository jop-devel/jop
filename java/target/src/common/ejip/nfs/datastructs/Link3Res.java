package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Link3Res extends ResultType{
	private PostOpAttr fileAttributes = new PostOpAttr();
	private WccData linkDirWcc = new WccData();
	
	public PostOpAttr getFileAttributes() {
		return fileAttributes;
	}

	public WccData getLinkDirWcc() {
		return linkDirWcc;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;
		
		status = Xdr.getNextInt(sb);
		fileAttributes.loadFields(sb);
		linkDirWcc.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			return true;
		} else {
			return false;
		}
	}
}
