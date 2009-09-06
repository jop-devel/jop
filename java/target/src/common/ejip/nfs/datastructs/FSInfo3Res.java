package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class FSInfo3Res extends ResultType {
	PostOpAttr objAttributes = new PostOpAttr();
	private int rtmax;
	private int rtpref;
	private int rtmult;
	private int wtmax;
	private int wtpref;
	private int wtmult;
	private int dtpref;
	private long maxfilesize;
	private Nfstime3 timeDelta = new Nfstime3();
	/**
	 * one of FSF3_LINK, FSF3_SYMLINK, FSF3_HOMOGENOUS, FSF3_CANSETTIME
	 */
	private int properties;

	public PostOpAttr getObjAttributes() {
		return objAttributes;
	}

	public int getRtmax() {
		return rtmax;
	}

	public int getRtpref() {
		return rtpref;
	}

	public int getRtmult() {
		return rtmult;
	}

	public int getWtmax() {
		return wtmax;
	}

	public int getWtpref() {
		return wtpref;
	}

	public int getWtmult() {
		return wtmult;
	}

	public int getDtpref() {
		return dtpref;
	}

	public long getMaxfilesize() {
		return maxfilesize;
	}

	public Nfstime3 getTimeDelta() {
		return timeDelta;
	}

	public int getProperties() {
		return properties;
	}

	public boolean loadFields(StringBuffer sb) {
		int status;

		status = Xdr.getNextInt(sb);
		objAttributes.loadFields(sb);
		if (status == NfsConst.NFS3_OK) {
			rtmax = Xdr.getNextInt(sb);
			rtpref = Xdr.getNextInt(sb);
			rtmult = Xdr.getNextInt(sb);
			wtmax = Xdr.getNextInt(sb);
			wtpref = Xdr.getNextInt(sb);
			wtmult = Xdr.getNextInt(sb);
			dtpref = Xdr.getNextInt(sb);
			maxfilesize = Xdr.getNextLong(sb);
			timeDelta.loadFields(sb);
			properties = Xdr.getNextInt(sb);
			return true;
		} else {
			return false;
		}
	}
}
