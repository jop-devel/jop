package ejip.nfs.datastructs;

import ejip.nfs.Xdr;

public class Diropargs3 {
	/**
	 * a file handle
	 */
	private StringBuffer dir = new StringBuffer();
	private StringBuffer name = new StringBuffer();
	
	public void setDir(StringBuffer dir) {
		this.dir = dir;
	}

	public void setName(StringBuffer name) {
		this.name = name;
	}

	public void appendToStringBuffer(StringBuffer sb) {
		Xdr.append(sb, dir);
		Xdr.append(sb, name);
	}
}
