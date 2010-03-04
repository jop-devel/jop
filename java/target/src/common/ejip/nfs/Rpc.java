/*
 * Copyright (c) Daniel Reichhard, daniel.reichhard@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Daniel Reichhard
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package ejip.nfs;

import ejip.nfs.datastructs.RpcDecodeMessageResult;

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
	public static RpcDecodeMessageResult decodeMessage(StringBuffer decodeMessageBuffer) {
		int  xid;
		RpcDecodeMessageResult result = new RpcDecodeMessageResult();
		
		result.xid = Xdr.getNextInt(decodeMessageBuffer);
		//location = findXid(xid);
		if ((Xdr.getNextInt(decodeMessageBuffer) == RpcConst.TYPE_REPLY) ) { //message type = reply (message word  1)
			
			switch (Xdr.getNextInt(decodeMessageBuffer)) {	//reply status (word index 2)
			case RpcConst.RPC_MSG_RPLY_STAT_ACCEPTED:
			
				switch (Xdr.getNextInt(decodeMessageBuffer)) { //verf union (word index 3)
				case RpcConst.AUTH_NULL: //the only auth flavor accepted so far	
					break;
				default:
					//System.out.println("RPC: Unsupported Authentication Flavour!");
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_UNSUPPORTED_AUTH_FLAVOR;
					return result;
				}
		
				if (Xdr.getNextInt(decodeMessageBuffer) != 0) {//auth body (index 4)
					//System.out.println("RPC: Authentication body not 0!");
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_AUTH_BODY_NOT_0;
					return result;
				}

				switch (Xdr.getNextInt(decodeMessageBuffer)) { //accept state (index 5)
				case RpcConst.RPC_MSG_ACCEPT_STAT_SUCCESS: 
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_OK;
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_PROG_UNAVAIL:
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_PROG_UNAVAIL;
					//System.out.println("RPC: program not available");
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_PROG_MISMATCH:
					//System.out.println("RPC: program version mismatch");
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_PROG_VERSION_MISMATCH;
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_PROC_UNAVAIL:
					//System.out.println("RPC: procedure unavailable");
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_PROC_UNAVAIL;
					break;
				case RpcConst.RPC_MSG_ACCEPT_STAT_GARBAGE_ARGS:
					//System.out.println("RPC: garbage arguments");
					result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_GARBAGE_ARGS;
					break;
				}
				break;
			case RpcConst.RPC_MSG_RPLY_STAT_DENIED:
				//System.out.println("RPC: Reply status: denied");
				result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_STATUS_DENIED;
				break;
			}
			
		} else {
			result.error = RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_MSG_TYPE_NOT_REPLY;
			//System.out.println("RPC: Either xid doesnt match, or msg type is not reply");
		}
		return result;
	}

}
