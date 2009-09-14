/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Daniel Reichhard (daniel.reichhard@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ejip.nfs;

import joprt.RtThread;
import com.jopdesign.sys.Const;
import ejip.Ejip;
import ejip.Ip;
import ejip.LinkLayer;
import ejip.Net;
import ejip.Packet;
import ejip.Slip;
import ejip.Udp;
import ejip.UdpHandler;
//import ejip.nfs.Nfs;
import ejip.nfs.NfsConst;
import ejip.nfs.Rpc;
import ejip.nfs.Xdr;
import ejip.nfs.Mount;
import ejip.nfs.datastructs.*;
import util.Serial;
import util.Timer;

public class NfsClient {
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_MOUNT = 0; 
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_NFS = 1;
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_NLM = 2;
	
	public static final int ACTION_CALL_MOUNT_NULL = 5;
	public static final int ACTION_CALL_GET_EXPORTS = 6;
	public static final int ACTION_CALL_DUMP = 7;

	public static final int ACTION_CALL_NFS_NULL = 11;
	public static final int ACTION_CALL_NFS_LOOKUP = 12;
	
	public static final int MAX_EXPORTS = 5, MAX_MOUNTS = 5;
	public static final int MAX_GROUPS = 4; //multiplies with MAX_EXPORTS!
	
	public static final int MAX_PACKETS = 8;
	static final int MAX_PACKET_SIZE = 1500;
	
	public static final int MAX_MSG_SIZE = MAX_PACKET_SIZE;

	public static final int PACKET_TIMEOUT = 10000;
	

	
	public UdpHandlerImpl uClient = new UdpHandlerImpl(this);
	public Mount mount = new Mount(NfsConst.MOUNT_PROGRAM, NfsConst.MOUNT_VERSION, this);
	
	public int xid = 0;
	public int prog = NfsConst.NFS_PROGRAM;
	public int vers = NfsConst.NFS_VERSION;
	public int proc = 0;
	public int destPort = 111; //portmapper
	public int destIP = 0;
	public int ownIP = 0;
	public int nfsPort = 0;
	public StringBuffer hostname;
	public Ejip ejip	= new Ejip(MAX_PACKETS,MAX_PACKET_SIZE);
	public Net net		= new Net(ejip);
	public Serial ser	= new Serial(Const.IO_UART_BG_MODEM_BASE); //simulator
	public LinkLayer ipLink;
	public Packet p;
	
	public StringBuffer messageBuffer = new StringBuffer();
	
	public Callbackable caller;
	
	public int hid = 0;
	
	class WaitList {
		public int xid;
		public int action;
		public int tstamp;
		public int service; //portmap, mount, nfs...
		public ResultType dataStruct;
	}
	
	WaitList[] waitList = new WaitList[MAX_PACKETS];
	
	protected GetAttr3Res getAttrRes;

	/**
	 * constructor
	 * @param myIP the IP address of the JOP Client
	 * @param destinationIP the IP of the RPC server
	 * @param myHostName the Hostname of the JOP Client
	 * @param caller an instance implementing the Runnable interface
	 */
	public NfsClient(int myIP, int destinationIP, StringBuffer myHostname, Callbackable caller) {
		new RtThread(4, 100000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					receive();
				}
			}
		};
		for (int i = 0; i < waitList.length; i++) {
			waitList[i] = new WaitList();
		}
		destIP 	= destinationIP;
		ownIP 	= myIP;
		hostname = myHostname;
		ipLink 	= new Slip(ejip, ser, ownIP);
		this.caller = caller;
		RtThread.startMission();
	}
	
	protected int newHandle(int action, int service, ResultType dataStruct) {
		if (hid == 0) {
			int ts;
			ts = Timer.us();
			for (int i = 0; i<2; i++) {
				ts = ts * ts;
				ts <<= 8;
				ts >>>= 16;
			}	
			hid = ts;
		} else {
			hid ++;
		}
		for (int i = 0; i < waitList.length; i++) {
			if (waitList[i].xid == 0) {
				waitList[i].xid = hid;
				waitList[i].action = action;
				waitList[i].service = service;
				if (dataStruct != null) {
					waitList[i].dataStruct = dataStruct;
				}
				break;
			}
		}
		return hid;
	}
	
	/**
	 * used to look up the incoming xid in the list of outstanding messages
	 * 
	 * @param xid the xid to search for
	 * @return the position of the xid in the list of outstanding messages on success, -1 otherwise
	 */
	protected int findXid(int xid) {
		for (int i=0; i<MAX_PACKETS; i++) {
			if (waitList[i].xid == xid) {
				System.out.println("diff: " + ((int)System.currentTimeMillis() - waitList[i].tstamp));
				waitList[i].xid = 0;
				return i;
			}
		}
		return -1;		
	}
	
	protected void receive(){
		for (int i=0; i<50; ++i) {
			ser.loop();
			// timeout in slip depends on loop time!
			ipLink.run();
			ser.loop();
			net.run();
		}
		Timer.wd();
	}
	
	/**
	 * send the message
	 * 
	 * @return the xid of the message sent
	 */
	protected int sendBuffer(StringBuffer rpcMessageBuffer, int destPort) {
		int xid;
		
		xid = Xdr.getIntAt(rpcMessageBuffer,0);
		
		p = ejip.getFreePacket(ipLink);
		if (p==null) {
			System.out.println("no free packet");
			return 0;
		} else {
			net.getUdp().addHandler(10111, uClient);
			Ip.setData(p, Udp.DATA, rpcMessageBuffer);
			
			for (int i=0; i < (MAX_PACKETS - 1); i++)	{
				if (waitList[i].xid == xid) {
//					System.out.println("Put new packet "+xid+" in slot "+i);
					waitList[i].tstamp = (int)System.currentTimeMillis();
					i = MAX_PACKETS;
				} else {
					if (i == (MAX_PACKETS - 1)) {
						System.out.println("ERROR - no more free space in Rpc message buffer \"waitlist\"!");
					}
				}
			}
			net.getUdp().build(p, destIP, destPort);	
		}
		return xid;
	}
	
	protected void handleNfsStates(StringBuffer msgBuffer, WaitList list) {
		switch (list.action) {
	
		case NfsConst.NFS3PROC3_NULL:
			System.out.println("nfs null call is back");
			//nothing to be done
			break;
		default:
			list.dataStruct.loadFields(msgBuffer);
		}
	}
	
	protected void handlePortmapStates(StringBuffer msgBuffer, int action) {
		
		switch (action) {
		
		case NfsConst.MOUNT_PROGRAM:
			this.mount.mountPort = Nfs.decodePort(msgBuffer); //Xdr.getNextInt(msgBuffer);
			net.getUdp().addHandler(this.mount.mountPort+10000, uClient);
			break;
		
		case NfsConst.NFS_PROGRAM:
			nfsPort = Nfs.decodePort(msgBuffer);
			net.getUdp().addHandler(nfsPort+10000, uClient);
			break;
			
		
/*		
 * 			case ACTION_QUERY_PORT_MAPPER_FOR_NLM:
 *			Nfs.nlmPort = Xdr.getNextInt(nfsMessageBuffer);
 *			rpcInst.installHandler(Nfs.nlmPort);
 *			System.out.println("got port (nlm): " + Nfs.nlmPort);
 *			break;
 */
		}
	}

	public void getNfsPort() {
		Rpc.queryPortmapper(messageBuffer, NfsConst.NFS_PROGRAM, NfsConst.NFS_VERSION, newHandle(NfsConst.NFS_PROGRAM, RpcConst.PMAP_PROG, null), hostname);
		sendBuffer(messageBuffer, destPort);
	}
	
	public void nullCall() {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_NULL, NfsConst.NFS_PROGRAM, null), RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.NFS3PROC3_NULL, hostname); 
		// no data to append
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * @param fHandle	filehandle (as returned from lookup, create, mkdir, symlink, mknod, readdirplus or mount)
	 * @param  
	 */
	public void getAttr(StringBuffer fHandle, GetAttr3Res getAttrRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_GETATTR, NfsConst.NFS_PROGRAM, getAttrRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_GETATTR, hostname); 
		Xdr.append(messageBuffer, fHandle);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * Set file attributes sattr on the file fHandle
	 * @param fHandle	the handle of the file to modify
	 * @param sattr	the new file attributes
	 * @param guard	if set, the attributes won't be set if the actual ctime on the server is newer than guard
	 * @param setAttrRes the object that receives the result of the call
	 */
	public void setAttr(StringBuffer fHandle, Sattr3 sattr, Nfstime3 guard, SetAttr3Res setAttrRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_SETATTR, NfsConst.NFS_PROGRAM, setAttrRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_SETATTR, hostname);
		Xdr.append(messageBuffer, fHandle);
		sattr.appendToStringBuffer(messageBuffer);
		if (guard != null) {
			Xdr.append(messageBuffer, NfsConst.TRUE);
			Xdr.append(messageBuffer, guard.getSeconds());
			Xdr.append(messageBuffer, guard.getNseconds());
		} else {
			Xdr.append(messageBuffer, NfsConst.FALSE);
		}
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * lookup filename
	 * @param what	handle of directory and filename to look up
	 * @param object	output: 
	 * @param obj_attributes	output:
	 * @param dir_attributes	output:
	 */
	public void lookup(Diropargs3 what, Lookup3Res lookupRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_LOOKUP, NfsConst.NFS_PROGRAM, lookupRes), RpcConst.AUTH_NULL, 0, 0, prog, vers, NfsConst.NFS3PROC3_LOOKUP, hostname);
		what.appendToStringBuffer(messageBuffer);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * @param objectHandle
	 * @param access
	 *            a combination of ACCESS3_READ, ACCESS3_LOOKUP, ACCESS3_MODIFY,
	 *            ACCESS3_EXTEND, ACCESS3_DELETE, ACCESS3_EXECUTE
	 * @param accessRes
	 */
	public void access(StringBuffer objectHandle, int access, Access3Res accessRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_ACCESS, NfsConst.NFS_PROGRAM, accessRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_ACCESS, hostname);
		Xdr.append(messageBuffer, objectHandle);
		Xdr.append(messageBuffer, access);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void read(StringBuffer file, long offset, int count, Fattr3 postOpAttr, int countReturn, boolean eof, StringBuffer data, Read3Res readRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_READ, NfsConst.NFS_PROGRAM, readRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_READ, hostname);
		Xdr.append(messageBuffer, file);
		Xdr.append(messageBuffer, offset);
		Xdr.append(messageBuffer, count);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * Write data to file
	 * @param fileHandle The file handle for the file to which data is to be written. This must identify a file system object of type, NF3REG
	 * @param offset The position within the file at which the write is to begin. An offset of 0 means to write data starting at the beginning of the file.
	 * @param count The number of bytes of data to be written. If count is 0, the WRITE will succeed and return a count of 0, barring errors due to permissions checking. The size of data must be less than or equal to the value of the wtmax field in the FSINFO reply structure for the file system that contains file. If greater, the server may write only wtmax bytes, resulting in a short write.
	 * @param stable One of FILE_SYNC, DATA_SYNC or UNSTABLE. <br/>FILE_SYNC: server must commit the data plus file system metadata to stable storage before returning results.<br/>DATA_SYNC: server must commit data to stable storage and enough of the metadata to retrieve the data before returning.<br/>UNSTABLE: server can return in any state.
	 * @param data The data to be written to the file.
	 */
	public void write(StringBuffer fileHandle, long offset, int count, int stable, StringBuffer data, Write3Res writeRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_WRITE, NfsConst.NFS_PROGRAM, writeRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_WRITE, hostname);
		Xdr.append(messageBuffer, fileHandle);
		Xdr.append(messageBuffer, offset);
		Xdr.append(messageBuffer, count);
		Xdr.append(messageBuffer, stable);
		Xdr.append(messageBuffer, data);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * 
	 * @param where
	 * @param obj_attributes if present, the file will be created in guarded mode.
	 * @param verf if present, and <em>obj_attributes</em> is null, the file will be created in exclusive mode. if both <em>obj_attributes</em> and <em>verf</em> are <pre>null</pre>, the file will be created in unchecked mode. 
	 */
	public void create(Diropargs3 where, int mode, Sattr3 objAttributes, StringBuffer verf, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_CREATE, NfsConst.NFS_PROGRAM, createRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_CREATE, hostname);
		where.appendToStringBuffer(messageBuffer);
		Xdr.append(messageBuffer, mode);
		switch (mode) {
		case 0: //fall through
		case 1:
			objAttributes.appendToStringBuffer(messageBuffer);
			break;
		case 2:
			if (verf != null) {
				if (verf.length() != NfsConst.NFS3_CREATEVERFSIZE) {
					verf.setLength(NfsConst.NFS3_CREATEVERFSIZE);
				}
				Xdr.appendRaw(messageBuffer, verf);
			}
			break;
		}
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void mkDir(Diropargs3 where, Sattr3 attributes, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_MKDIR, NfsConst.NFS_PROGRAM, createRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_MKDIR, hostname);
		where.appendToStringBuffer(messageBuffer);
		attributes.appendToStringBuffer(messageBuffer);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void symLink(Diropargs3 where, Sattr3 symlinkAttributes, StringBuffer symlinkData, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_SYMLINK, NfsConst.NFS_PROGRAM, createRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_SYMLINK, hostname);
		where.appendToStringBuffer(messageBuffer);
		symlinkAttributes.appendToStringBuffer(messageBuffer);
		Xdr.append(messageBuffer, symlinkData);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	/**
	 * @param where
	 * @param type
	 *            one of the following values: NF3CHR, NF3BLK, NF3SOCK, NF3FIFO
	 * @param devAttributes
	 * @param spec
	 * @param pipeAttributes
	 * @param createRes
	 */
	public void mkNod(Diropargs3 where, int type, Sattr3 devAttributes, Specdata3 spec, Sattr3 pipeAttributes, CreateRes createRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_MKNOD, NfsConst.NFS_PROGRAM, createRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_MKNOD, hostname);
		where.appendToStringBuffer(messageBuffer);
		Xdr.append(messageBuffer, type);
		switch (type) {
		case NfsConst.NF3CHR: // fall through
		case NfsConst.NF3BLK:
			devAttributes.appendToStringBuffer(messageBuffer);
			spec.appendToStringBuffer(messageBuffer);
			break;
		case NfsConst.NF3SOCK: // fall through
		case NfsConst.NF3FIFO:
			pipeAttributes.appendToStringBuffer(messageBuffer);
			break;
		default:
			break;
		}
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void remove(Diropargs3 object, Remove3Res removeRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_REMOVE, NfsConst.NFS_PROGRAM, removeRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_REMOVE, hostname);
		object.appendToStringBuffer(messageBuffer);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void rmDir(Diropargs3 object, Remove3Res rmDirRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_RMDIR, NfsConst.NFS_PROGRAM, rmDirRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_RMDIR, hostname);
		object.appendToStringBuffer(messageBuffer);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void rename(Diropargs3 from, Diropargs3 to, Rename3Res renameRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_RENAME, NfsConst.NFS_PROGRAM, renameRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_RENAME, hostname);
		from.appendToStringBuffer(messageBuffer);
		to.appendToStringBuffer(messageBuffer);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void link(StringBuffer fileHandle, Diropargs3 link, Link3Res linkRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_LINK, NfsConst.NFS_PROGRAM, linkRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_LINK, hostname);
		Xdr.append(messageBuffer, fileHandle);
		link.appendToStringBuffer(messageBuffer);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void readDir(StringBuffer dirHandle, long cookie, StringBuffer cookieverf, int count, ReadDir3Res readDirRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_READDIR, NfsConst.NFS_PROGRAM, readDirRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_READDIR, hostname);
		Xdr.append(messageBuffer, dirHandle);
		Xdr.append(messageBuffer, cookie);
		if (cookieverf != null) {
			if (cookieverf.length() != NfsConst.NFS3_COOKIEVERFSIZE) {
				cookieverf.setLength(NfsConst.NFS3_COOKIEVERFSIZE);
			}
			Xdr.append(messageBuffer, cookieverf);
		}
		Xdr.append(messageBuffer, count);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void readDirPlus(StringBuffer dirHandle, long cookie, StringBuffer cookieverf, int dirCount, int maxCount, ReadDirPlus3Res readDirPlusRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_READDIRPLUS, NfsConst.NFS_PROGRAM, readDirPlusRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_READDIRPLUS, hostname);
		Xdr.append(messageBuffer, dirHandle);
		Xdr.append(messageBuffer, cookie);
		if (cookieverf != null) {
			if (cookieverf.length() != NfsConst.NFS3_COOKIEVERFSIZE) {
				cookieverf.setLength(NfsConst.NFS3_COOKIEVERFSIZE);
			}
			Xdr.append(messageBuffer, cookieverf);
		}
		Xdr.append(messageBuffer, dirCount);
		Xdr.append(messageBuffer, maxCount);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void fsStat(StringBuffer fsroot, FSStat3Res fsStatRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_FSSTAT, NfsConst.NFS_PROGRAM, fsStatRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_FSSTAT, hostname);
		Xdr.append(messageBuffer, fsroot);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void fsInfo(StringBuffer fsroot, FSInfo3Res fsInfoRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_FSINFO, NfsConst.NFS_PROGRAM, fsInfoRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_FSINFO, hostname);
		Xdr.append(messageBuffer, fsroot);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void pathConf(StringBuffer object, PathConf3Res pathConfRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_PATHCONF, NfsConst.NFS_PROGRAM, pathConfRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_PATHCONF, hostname);
		Xdr.append(messageBuffer, object);
		sendBuffer(messageBuffer, nfsPort);
	}
	
	public void commit(StringBuffer fileHandle, long offset, int count, Commit3Res commitRes) {
		Rpc.setupHeader(messageBuffer, newHandle(NfsConst.NFS3PROC3_COMMIT, NfsConst.NFS_PROGRAM, commitRes), RpcConst.AUTH_SYS, 1000, 1000, prog, vers, NfsConst.NFS3PROC3_COMMIT, hostname);
		Xdr.append(messageBuffer, fileHandle);
		Xdr.append(messageBuffer, offset);
		Xdr.append(messageBuffer, count);
		sendBuffer(messageBuffer, nfsPort);
	}
	
}
