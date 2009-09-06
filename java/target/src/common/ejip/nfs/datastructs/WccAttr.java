package ejip.nfs.datastructs;

import ejip.nfs.Xdr;

public class WccAttr {
	/**
	 * file size in bytes
	 */
	public long	size;
	/**
	 * last modification time
	 */
	public Nfstime3   mtime = new Nfstime3();
	/**
	 * last attribute change time
	 */
	public Nfstime3	 ctime = new Nfstime3();
	
	public void getWccAttr(StringBuffer sb) {
		size = Xdr.getNextLong(sb);
		mtime.loadFields(sb);
		ctime.loadFields(sb);
	}
	
	public void appendToStringBuffer(StringBuffer sb) {
		Xdr.append(sb, size);
		Xdr.append(sb, mtime.getSeconds());
		Xdr.append(sb, mtime.getNseconds());
		Xdr.append(sb, ctime.getSeconds());
		Xdr.append(sb, ctime.getNseconds());
	}
	
	public String toString() {
		return "Size:\t" + size + 
			"\nmtime:\n" + 
			mtime.toString() + 
			"\nctime:\n" +
			ctime.toString(); 
	}
}
