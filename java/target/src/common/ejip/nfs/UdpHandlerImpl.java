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

import ejip.Ip;
import ejip.Packet;
import ejip.Udp;
import ejip.UdpHandler;
import ejip.nfs.datastructs.RpcDecodeMessageResult;
import ejip.nfs.datastructs.ServicePort;

/**
 * memberclass to receive packets
 *
 */
class UdpHandlerImpl implements UdpHandler {	
	StringBuffer decodeMessageBuffer = new StringBuffer();
	NfsClient nc;	
	
	public UdpHandlerImpl(NfsClient nc) {
		this.nc = nc;
	}
	
	public void request(Packet p) {
		RpcDecodeMessageResult result;

		Ip.getData(p, Udp.DATA, decodeMessageBuffer);
		nc.ejip.returnPacket(p);
		result = Rpc.decodeMessage(decodeMessageBuffer);
		//TODO handle invalid RPC messages
		if (decodeMessageBuffer != null) {
			if (result.error == 0) { //else something went wrong
				for (int i = 0; i < nc.waitList.length; i++) {
					if (nc.waitList[i].xid == result.xid) {
						nc.waitList[i].xid = 0;
//							System.out.println("incoming: (index " + i + ")");
						chooseHandler(decodeMessageBuffer, nc.waitList[i], result.error);
					}//else ignore
				}
			} else { //received unexpected packet
				//System.out.println("received unexpected packet");
				chooseHandler(null, null, result.error);
			}
		} else {
			chooseHandler(null, null, RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_MSG_EMPTY);
			//System.out.println("received empty rpc message"); 
		}
	}	
	
	/**
	 * checks for lost packets 
	 */
	public void loop() {
		for (int i=0; i < nc.waitList.length; i++) {
			if (nc.waitList[i].xid != 0) {
				if (((int)System.currentTimeMillis() - nc.waitList[i].tstamp) > NfsClient.PACKET_TIMEOUT) {
					//System.out.println("Timeout: lost packet: " + nc.waitList[i].xid + "!");
					nc.waitList[i].xid = 0;
					chooseHandler(null, nc.waitList[i], RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_OK);
				}
			}
		}
	}
	
	protected void chooseHandler(StringBuffer mb, NfsClient.WaitList list, byte error) {
		if ((mb != null) && (list != null) && (list.action != NfsConst.NFS3PROC3_NULL)) {//also matches != NfsConst.MOUNTPROC3_NULL
			list.dataStruct.loadFields(mb);
			if (list.service != 0) {
				nc.handlePortmapStates( ((ServicePort) list.dataStruct).getPort(), list.service);
			}
		}
		
//		if (list != null) {
//			nc.caller.callback(0);
//		} else {
			nc.caller.callback(error);
//		}
	}
	
}
