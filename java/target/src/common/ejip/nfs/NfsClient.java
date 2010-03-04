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
	
	public static final int MAX_EXPORTS = 5;
	public static final int MAX_MOUNTS = 5;
	public static final int MAX_GROUPS = 4; //multiplies with MAX_EXPORTS!
	
	public static final int MAX_PACKETS = 8;
	static final int MAX_PACKET_SIZE = 1500;
	
	public static final int MAX_MSG_SIZE = MAX_PACKET_SIZE;

	public static final int PACKET_TIMEOUT = 10000;
	

	public UdpHandlerImpl uClient = new UdpHandlerImpl(this);
	public Mount mount = new Mount(NfsConst.MOUNT_PROGRAM, NfsConst.MOUNT_VERSION, this);
	public Nfs nfs = new Nfs(NfsConst.NFS_PROGRAM, NfsConst.NFS_VERSION, this);
	
	public int xid = 0;
	public int prog = NfsConst.NFS_PROGRAM;
	public int vers = NfsConst.NFS_VERSION;
	public int proc = 0;
	public int portmapperPort = 111; //portmapper
	public int destIP = 0;
	public int ownIP = 0;
	public StringBuffer hostname;
	public Ejip ejip	= new Ejip(MAX_PACKETS, MAX_PACKET_SIZE);
	public Net net		= new Net(ejip);
	public Serial ser	= new Serial(Const.IO_UART_BG_MODEM_BASE); 
	public LinkLayer ipLink;
	public Packet p;
	
	public StringBuffer messageBuffer = new StringBuffer();
	
	public Callbackable caller;
	
	public int hid = 0;
	
	class WaitList {
		public int xid;
		public int action;
		public int tstamp;
		public int service;
		public ResultType dataStruct;
	}
	
	WaitList[] waitList = new WaitList[MAX_PACKETS];
	
	protected GetAttr3Res getAttrRes;

	/**
	 * constructor
	 * @param myIP the IP address of the JOP Client
	 * @param destinationIP the IP of the RPC server
	 * @param portmapperPort the port on which the Portmapper is listening
	 * @param caller an instance implementing the Runnable interface
	 * @param myHostName the Hostname of the JOP Client
	 */
	public NfsClient(int myIP, int destinationIP, int portmapperPort, StringBuffer myHostname, Callbackable caller) {
			//periode: 1000000 / ((115200 / 10 * 8) / 8) * 50 = 4340
			new RtThread(10, 4400) { //TODO: fragen warum so große periode auch funktioniert
				public void run() {
					for (;;) {
						waitForNextPeriod();
						ser.loop();
					}
				}
			};

			new RtThread(9, 10000) {
				public void run() {
					for (;;) {
						waitForNextPeriod();
						ipLink.run();
						Timer.wd();
					}
				}
			};

			new RtThread(7, 10000) {
				public void run() {
					for (;;) {
						waitForNextPeriod();
						net.run();
					}
				}
			};
		for (int i = 0; i < waitList.length; i++) {
			waitList[i] = new WaitList();
		}
		destIP 	= destinationIP;
		ownIP 	= myIP;
		hostname = myHostname;
		this.portmapperPort = portmapperPort;
		ipLink 	= new Slip(ejip, ser, ownIP);
		this.caller = caller;
		RtThread.startMission();
	}
	
	protected int newHandle(int action, ResultType dataStruct) {
		return newHandle(action, dataStruct, 0);
	}
	
	protected int newHandle(int action, ResultType dataStruct, int service) {
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
				waitList[i].service = service;
				waitList[i].action = action;
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
				//System.out.println("diff: " + ((int)System.currentTimeMillis() - waitList[i].tstamp));
				waitList[i].xid = 0;
				return i;
			}
		}
		return -1;		
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
			//System.out.println("no free packet");
			return 0;
		} else {
			net.getUdp().addHandler(10111, uClient);
			Ip.setData(p, Udp.DATA, rpcMessageBuffer);
			
			for (int i=0; i < (MAX_PACKETS - 1); i++)	{ //@WCA loop=8
				if (waitList[i].xid == xid) {
//					System.out.println("Put new packet "+xid+" in slot "+i);
					waitList[i].tstamp = (int)System.currentTimeMillis();
					i = MAX_PACKETS;
				} else {
					if (i == (MAX_PACKETS - 1)) {
						//System.out.println("ERROR - no more free space in Rpc message buffer \"waitlist\"!");
					}
				}
			}
			net.getUdp().build(p, destIP, destPort);	
		}
		return xid;
	}
	
	protected void handlePortmapStates(int port, int service) {
		
		switch (service) {
		
		case NfsConst.MOUNT_PROGRAM:
			this.mount.setMountPort(port); 
			break;
		
		case NfsConst.NFS_PROGRAM:
			this.nfs.setNfsPort(port);
			break;
/*		
 * 			case ACTION_QUERY_PORT_MAPPER_FOR_NLM:
 *			break;
 */
		}
		
		net.getUdp().addHandler(port+10000, uClient);
		
	}
	
}
