package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Lookup3Res extends ResultType {

	/**
	 * The file handle of the object corresponding to what.name.
	 */
	private StringBuffer object = new StringBuffer();
	/**
	 * The attributes of the object corresponding to what.name.
	 */
	private PostOpAttr objAttributes = new PostOpAttr();
	/**
	 * The post-operation attributes of the directory, what.dir.
	 */
	private PostOpAttr dirAttributes = new PostOpAttr();

	public StringBuffer getObject() {
		return object;
	}

	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public PostOpAttr getDirAttributes() {
		return dirAttributes;
	}

	public boolean loadFields(StringBuffer sb) {
		boolean retval;
		this.error = Xdr.getNextInt(sb);
		if (this.error == NfsConst.NFS3_OK) {
			Xdr.getNextStringBuffer(sb, object);
			objAttributes.loadFields(sb);
			retval = true;
		} else {
			retval = false;
		}
		dirAttributes.loadFields(sb);
		return retval;
	}

	public String toString() {
		return "object:\t" + object.toString() + "\n"
				+ objAttributes.toString() + "\n" + dirAttributes.toString();
	}
}
