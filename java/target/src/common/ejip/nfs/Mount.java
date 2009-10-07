package ejip.nfs;

public class Mount {
	public int mountPort;
	public int prog;
	public int vers;
	private int xid;
	private StringBuffer messageBuffer = new StringBuffer();
	private NfsClient nc;
	
	StringBuffer exportsList[];
	StringBuffer groups[][];
	/**
	 * List of Mounts
	 * index 0: hostname
	 * index 1: directory
	 */
	StringBuffer mountList[][];
	StringBuffer fHandle;
	int flavors[];
	
	public Mount(int prog, int vers, NfsClient nc) {
		this.prog = prog;
		this.vers = vers;
		this.nc = nc;
	}
	
	public void getMountPort() {
		Rpc.queryPortmapper(messageBuffer, NfsConst.MOUNT_PROGRAM, NfsConst.MOUNT_VERSION, nc.newHandle(NfsConst.MOUNT_PROGRAM, RpcConst.PMAP_PROG, null), nc.hostname);
		nc.sendBuffer(messageBuffer, nc.destPort);
	}
	
	public void nullCall() {
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_NULL, NfsConst.MOUNT_PROGRAM, null), RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.MOUNTPROC3_NULL, nc.hostname); // no data to append
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void getExports(StringBuffer[] eList, StringBuffer[][] grps) {
		exportsList = eList;
		groups = grps;
		Rpc.setupHeader(messageBuffer, nc.newHandle(NfsConst.MOUNTPROC3_EXPORT, NfsConst.MOUNT_PROGRAM, null), RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.MOUNTPROC3_EXPORT, nc.hostname); // no data to append
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void dump(StringBuffer[][] mntList) {
		mountList = mntList;
		xid = nc.newHandle(NfsConst.MOUNTPROC3_DUMP, NfsConst.MOUNT_PROGRAM, null);
		Rpc.setupHeader(messageBuffer, xid, RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.MOUNTPROC3_DUMP, nc.hostname); 
//		Nfs.Mount.dump(messageBuffer, RpcConst.AUTH_SYS, xid);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void mount(StringBuffer directory, StringBuffer fileHandle, int[] flavs) {
		fHandle = fileHandle;
		flavors = flavs;
		xid = nc.newHandle(NfsConst.MOUNTPROC3_MNT, NfsConst.MOUNT_PROGRAM, null);
		Rpc.setupHeader(messageBuffer, xid, RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.MOUNTPROC3_MNT, nc.hostname);
		Xdr.append(messageBuffer, directory);
		nc.sendBuffer(messageBuffer, mountPort);
		System.out.println("mount");
	}
	
	public void unmountAll() {
		xid = nc.newHandle(NfsConst.MOUNTPROC3_UMNTALL, NfsConst.MOUNT_PROGRAM, null);
		Rpc.setupHeader(messageBuffer, xid, RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.MOUNTPROC3_UMNTALL, nc.hostname);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	public void unmount(StringBuffer path) {
		xid = nc.newHandle(NfsConst.MOUNTPROC3_UMNT, NfsConst.MOUNT_PROGRAM, null);
		Rpc.setupHeader(messageBuffer, xid, RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.MOUNTPROC3_UMNT, nc.hostname);
		Xdr.append(messageBuffer, path);
		nc.sendBuffer(messageBuffer, mountPort);
	}
	
	void handleStates(StringBuffer msgBuffer, int action) {
		
		switch (action) {
	
		case NfsConst.MOUNTPROC3_NULL:
			System.out.println("mount null call is back");
			//nothing to be done
			break;
		
		case NfsConst.MOUNTPROC3_EXPORT:
			Nfs.decodeExports(msgBuffer, exportsList, groups);
			break;
		
		case NfsConst.MOUNTPROC3_DUMP:
			Nfs.decodeMountlist(msgBuffer, mountList);
			break;
			
		case NfsConst.MOUNTPROC3_MNT:
			System.out.println("received mount response");
			Nfs.decodeMountCallOK(msgBuffer, fHandle, flavors);
			break;
			
		case NfsConst.MOUNTPROC3_UMNTALL:
			//nothing to decode
			break;
		
		case NfsConst.MOUNTPROC3_UMNT:
			//nothing to decode
			break;
			
		default:
			System.out.println("FUNCTION NOT IMPLEMENTED: " + action);
		}
	}
}
