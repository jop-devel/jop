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

/**
 * @author bayer
 *
 */
public class Rpc {
	
	/**
	 * adds the header bytes to the message buffer
	 * 
	 * @param xid - the xid to set
	 * @param authType - authorization type (AUTH_NULL or AUTH_SYS)
	 */
	public static void setupHeader(StringBuffer rpcMessageBuffer, int xid, byte authType, int uid, int gid, int prog, int vers, int proc, StringBuffer hostname) {
		rpcMessageBuffer.setLength(0); //setlength doesnt work?
		Xdr.append(rpcMessageBuffer,xid);
		Xdr.append(rpcMessageBuffer,0); //message type = call
		Xdr.append(rpcMessageBuffer,RpcConst.RPC_RPCVERS);
		Xdr.append(rpcMessageBuffer,prog);
		Xdr.append(rpcMessageBuffer,vers);
		Xdr.append(rpcMessageBuffer,proc);
		switch (authType) {
		case RpcConst.AUTH_NULL:
			Xdr.append(rpcMessageBuffer,0);
			Xdr.append(rpcMessageBuffer,0);
			Xdr.append(rpcMessageBuffer,0);
			Xdr.append(rpcMessageBuffer,0);
			break;
		case RpcConst.AUTH_SYS:
			Xdr.append(rpcMessageBuffer,RpcConst.AUTH_SYS);
			Xdr.append(rpcMessageBuffer,(hostname.length()) + 4 - (hostname.length()%4) + 6 * 4);
			Xdr.append(rpcMessageBuffer,262);
			Xdr.append(rpcMessageBuffer,hostname);
			Xdr.append(rpcMessageBuffer,uid);
			Xdr.append(rpcMessageBuffer,gid);
			Xdr.append(rpcMessageBuffer,1);
			Xdr.append(rpcMessageBuffer,0);
			Xdr.append(rpcMessageBuffer,RpcConst.AUTH_NULL);
			Xdr.append(rpcMessageBuffer,0);
			break;
		}
	}
	
	/**
	 * decodes the rpc message state
	 * 
	 * @return the xid of the message received on success, 0 otherwise
	 */
	public static int decodeMessage(StringBuffer decodeMessageBuffer) {
		int  xid;
		xid = Xdr.getNextInt(decodeMessageBuffer);
		//location = findXid(xid);
		if ((Xdr.getNextInt(decodeMessageBuffer) == RpcConst.TYPE_REPLY) ) { //message type = reply (message word  1)
			
			switch (Xdr.getNextInt(decodeMessageBuffer)) {	//reply status (word index 2)
			case RpcConst.RPC_MSG_RPLY_STAT_ACCEPTED:
			
				switch (Xdr.getNextInt(decodeMessageBuffer)) { //verf union (word index 3)
				case RpcConst.AUTH_NULL: //the only auth flavor accepted so far	
					break;
				default:
					System.out.println("RPC: Unsupported Authentication Flavour!");
					return 0;
				}
		
				if (Xdr.getNextInt(decodeMessageBuffer) != 0) {//auth body (index 4)
					System.out.println("RPC: Authentication body not 0!");
					return 0;
				}

				switch (Xdr.getNextInt(decodeMessageBuffer)) { //accept state (index 5)
				case RpcConst.RPC_MSG_ACCEPT_STAT_SUCCESS: 
					return xid;
//					return pos;
				case RpcConst.RPC_MSG_ACCEPT_STAT_PROG_UNAVAIL:
					System.out.println("RPC: program not available");
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_PROG_MISMATCH:
					System.out.println("RPC: program version mismatch");
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_PROC_UNAVAIL:
					System.out.println("RPC: procedure unavailable");
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_GARBAGE_ARGS:
					System.out.println("RPC: garbage arguments");
					break;
				}
				break;
			case RpcConst.RPC_MSG_RPLY_STAT_DENIED:
				System.out.println("RPC: Reply status: denied");
				break;
			}
			
		} else {
			System.out.println("RPC: Either xid doesnt match, or msg type is not reply");
		}
		return 0;
	}

	/**
	 * ask portmapper for port of a program
	 * 
	 * @param rpcMessageBuffer	a StringBuffer to write the data to 
	 * @param program	the remote program number (according to the RPC program number assignment)
	 * @param version	the version of the remote program
	 * @param xid	an arbitrary xid
	 * @return	the xid just sent on success, 0 otherwise
	 */
	public static void queryPortmapper(StringBuffer rpcMessageBuffer, int program, int version, int xid, StringBuffer hostname) {
			setupHeader(rpcMessageBuffer, xid, RpcConst.AUTH_SYS, 0, 0, RpcConst.PMAP_PROG, RpcConst.PMAP_VERS, NfsConst.PMAPPROC_GETPORT, hostname);
			//append program (portmap) specific part:
			Xdr.append(rpcMessageBuffer,program);
			Xdr.append(rpcMessageBuffer,version);
			Xdr.append(rpcMessageBuffer,RpcConst.IPPROTO_UDP);
			Xdr.append(rpcMessageBuffer,0); //port is ignored

	}
	
}