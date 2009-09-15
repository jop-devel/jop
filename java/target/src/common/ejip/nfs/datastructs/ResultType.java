package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;

public abstract class ResultType implements Loadable {
	protected int error;
	//TODO: error zuweisung in allen klassen nachziehen
	public abstract boolean loadFields(StringBuffer sb);
	
	public int getError() {
		return error;
	}

	public String getErrorDescription() {
		switch (error) {
		case NfsConst.NFS3_OK:
			return "Call completed Successfully.";
		case NfsConst.NFS3ERR_PERM:
			return "Caller is not the owner, or a privileged user.";
		case NfsConst.NFS3ERR_NOENT:
			return "No such file or directory.";
		case NfsConst.NFS3ERR_IO:
			return "I/O error";
		case NfsConst.NFS3ERR_NXIO:
			return "I/O error: No such device or address.";
		case NfsConst.NFS3ERR_ACCES:
			return "Permission denied.";
		case NfsConst.NFS3ERR_EXIST:
			return "File exists.";
		case NfsConst.NFS3ERR_XDEV:
			return "Attempt to do a cross-device hard link.";
		case NfsConst.NFS3ERR_NODEV:
			return "No such device.";
		case NfsConst.NFS3ERR_NOTDIR:
			return "Not a directory.";
		case NfsConst.NFS3ERR_ISDIR:
			return "Is a directory.";
		case NfsConst.NFS3ERR_INVAL:
			return "Invalid or unsupported argument.";
		case NfsConst.NFS3ERR_FBIG:
			return "File too large.";
		case NfsConst.NFS3ERR_NOSPC:
			return "No space left on device.";
		case NfsConst.NFS3ERR_ROFS:
			return "Read-only file system.";
		case NfsConst.NFS3ERR_MLINK:
			return "Too many hard links.";
		case NfsConst.NFS3ERR_NAMETOOLONG:
			return "Filename too long.";
		case NfsConst.NFS3ERR_NOTEMPTY:
			return "Cannot remove non-empty directory.";
		case NfsConst.NFS3ERR_DQUOT:
			return "Quota hard limit exceeded.";
		case NfsConst.NFS3ERR_STALE:
			return "Invalid file handle.";
		case NfsConst.NFS3ERR_REMOTE:
			return "Too many levels of remote in path.";
		case NfsConst.NFS3ERR_BADHANDLE:
			return "Illegal NFS file handle.";
		case NfsConst.NFS3ERR_NOT_SYNC:
			return "Update synchronization mismatch was detected.";
		case NfsConst.NFS3ERR_BAD_COOKIE:
			return "Cookie is stale";
		case NfsConst.NFS3ERR_NOTSUPP:
			return "Operation is not supported.";
		case NfsConst.NFS3ERR_TOOSMALL:
			return "Buffer or request is too small.";
		case NfsConst.NFS3ERR_SERVERFAULT:
			return "An unknown error occurred on the server.";
		case NfsConst.NFS3ERR_BADTYPE:
			return "An attempt was made to create an object of a type not supported by the server.";
		case NfsConst.NFS3ERR_JUKEBOX:
			return "Could not complete request in a timely fashion.";
		default:
			return "Something is rotten in the state of Denmark.";
		}
	}
}
