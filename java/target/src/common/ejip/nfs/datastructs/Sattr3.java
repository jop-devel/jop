package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Sattr3 {


	/**
	 * protection mode bits:
	 * <pre>
	 * 0x00800 Set user ID on execution.
	 * 0x00400 Set group ID on execution.
	 * 0x00200 Save swapped text (not defined in POSIX).
	 * 0x00100 Read permission for owner.
	 * 0x00080 Write permission for owner.
	 * 0x00040 Execute permission for owner on a file. Or lookup
	 *        (search) permission for owner in directory.
	 * 0x00020 Read permission for group.
	 * 0x00010 Write permission for group.
	 * 0x00008 Execute permission for group on a file. Or lookup
	 *        (search) permission for group in directory.
	 * 0x00004 Read permission for others.
	 * 0x00002 Write permission for others.
	 * 0x00001 Execute permission for others on a file. Or lookup
	 *        (search) permission for others in directory.
	 * </pre>
	 */
	protected int		mode; 
	protected int		modeset;
	/**
	 * user ID of the file owner
	 */
	protected int		uid; 
	protected int		uidset;
	/**
	 * group ID of the file group
	 */
	protected int		gid;
	protected int		gidset;
	/**
	 * size in bytes
	 */
	protected long	size;
	protected int		sizeset;
	/**
	 * last access time
	 */
	protected Nfstime3   atime = new Nfstime3(); 
	protected int		atimeset;
	/**
	 * last modification time
	 */
	protected Nfstime3   mtime = new Nfstime3();
	protected int		mtimeset;
    
	/**
	 * sets the given mode
	 * @param mode	possible values:
	 * <pre>
	 * 0x00800 Set user ID on execution.
	 * 0x00400 Set group ID on execution.
	 * 0x00200 Save swapped text (not defined in POSIX).
	 * 0x00100 Read permission for owner.
	 * 0x00080 Write permission for owner.
	 * 0x00040 Execute permission for owner on a file. Or lookup
	 *        (search) permission for owner in directory.
	 * 0x00020 Read permission for group.
	 * 0x00010 Write permission for group.
	 * 0x00008 Execute permission for group on a file. Or lookup
	 *        (search) permission for group in directory.
	 * 0x00004 Read permission for others.
	 * 0x00002 Write permission for others.
	 * 0x00001 Execute permission for others on a file. Or lookup
	 *        (search) permission for others in directory.
	 * </pre>
	 */
	public void setMode(int mode) {
		this.mode = mode;
		this.modeset = 1;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
		this.uidset = 1;
	}
	
	public void setGid(int gid) {
		this.gid = gid;
		this.gidset = 1;
	}
	
	public void setSize(long size) {
		this.size = size;
		this.sizeset = 1;
	}
	
	public void setAtime(Nfstime3 atime, int time_how) {
		if (time_how == NfsConst.SET_TO_CLIENT_TIME ) {
			this.atime = atime;
		}
		if (time_how == NfsConst.DONT_CHANGE | time_how == NfsConst.SET_TO_CLIENT_TIME | time_how == NfsConst.SET_TO_SERVER_TIME) {
			this.atimeset = time_how;
		}
	}
	
	public void setMtime(Nfstime3 mtime, int time_how) {
		if (time_how == NfsConst.SET_TO_CLIENT_TIME ) {
			this.mtime = mtime;
		} 
		if (time_how == NfsConst.DONT_CHANGE | time_how == NfsConst.SET_TO_CLIENT_TIME | time_how == NfsConst.SET_TO_SERVER_TIME) {
			this.mtimeset = time_how;
		}
	}
	
	/**
	 * append NFS representation of this objects fields to a StringBuffer<br/>
	 * @param sb	the StringBuffer to append to
	 */
	public void appendToStringBuffer(StringBuffer sb) {
		Xdr.append(sb, modeset);
		if (modeset == NfsConst.TRUE) {
			Xdr.append(sb,mode);
		}
		Xdr.append(sb, uidset);
		if (uidset == NfsConst.TRUE) {
			Xdr.append(sb, uid);
		}
		Xdr.append(sb, gidset);
		if (gidset == NfsConst.TRUE) {
			Xdr.append(sb,gid);
		}
		Xdr.append(sb, sizeset);
		if (sizeset == NfsConst.TRUE) {
			Xdr.append(sb,size);
		} 
		Xdr.append(sb, atimeset);
		if (atimeset == NfsConst.SET_TO_CLIENT_TIME) {
			Xdr.append(sb, atime.getSeconds());
			Xdr.append(sb, atime.getNseconds());
		}
		Xdr.append(sb,mtimeset);
		if (mtimeset == NfsConst.SET_TO_CLIENT_TIME) {
			Xdr.append(sb, mtime.getSeconds());
			Xdr.append(sb, mtime.getNseconds());
		}
    }
	
	public void resetFields() {
		mode = 0; 
		modeset = 0;
		uid = 0; 
		uidset = 0;
		gid = 0;
		gidset = 0;
		size = 0;
		sizeset = 0;
		atime.setSeconds(0);
		atime.setNseconds(0); 
		atimeset = 0;
		mtime.setSeconds(0);
		mtime.setNseconds(0);
		mtimeset = 0;
	}
    
   /* public void loadFromStringBuffer(StringBuffer sb) {
    	mode = Xdr.getNextInt(sb);
    	uid = Xdr.getNextInt(sb);
    	gid = Xdr.getNextInt(sb);
    	size = Xdr.getNextLong(sb);
    	atime.seconds = Xdr.getNextInt(sb);
    	atime.nseconds = Xdr.getNextInt(sb);
    	mtime.seconds = Xdr.getNextInt(sb);
    	mtime.nseconds = Xdr.getNextInt(sb);
    }*/
}
