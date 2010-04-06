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
package ejip.examples;

import joprt.RtThread;
import ejip.Ejip;
import ejip.Slip;
import util.Timer;
import ejip.nfs.Callbackable;
import ejip.nfs.NfsClient;
import ejip.nfs.NfsConst;
import ejip.nfs.datastructs.*;


public class NfsExample {
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_MOUNT = 6; 
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_NFS = 7;
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_NLM = 8;

	public static final int ACTION_CALL_NFS_NULL = 11;
	public static final int ACTION_CALL_NFS_LOOKUP = 12;
	
	public static final int MAX_EXPORTS = 3;
	public static final int MAX_GROUPS = 1;
	public static final int MAX_MOUNTS = 4;
	public static final int MAX_FLAVORS = 4;

	int destIP;
	int ownIP;
	int mountPort;
	int nfsPort;
	
	static StringBuffer hostname = new StringBuffer("jopClient");
	StringBuffer messageBuffer = new StringBuffer();
	
	static NfsClient nc;
	static NfsExample nex;
	
	/**
	 * List of Directories
	 */
	static StringBuffer[] exportsList = new StringBuffer[] {new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()};
	/**
	 * groups of corresponding export
	 */
	static StringBuffer[][] groups = new StringBuffer[][] {{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()},
							{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()},
							{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()},
							{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()}};
	/**
	 * List of Mounts
	 * index 0: hostname
	 * index 1: directory
	 */
	public static MountRes3 mountRes = new MountRes3(MAX_FLAVORS);
	public static int[] flavors = new int[MAX_FLAVORS];
	public static Exports exports = new Exports(MAX_EXPORTS, MAX_GROUPS);
//	public static MountList mountList = new MountList(MAX_MOUNTS);
	
	public static GetAttr3Res getAttrRes = new GetAttr3Res(); 
	public static SetAttr3Res setAttrRes = new SetAttr3Res(); 
	public static Lookup3Res lookupRes = new Lookup3Res();
	public static CreateRes createRes = new CreateRes();
	public static Write3Res writeRes = new Write3Res();
	
	public static Diropargs3 what = new Diropargs3();
	public static Sattr3 sattr = new Sattr3();
	public static Nfstime3 guard = new Nfstime3();
	public static Sattr3 setObjAttributes = new Sattr3();
	public static Diropargs3 where = new Diropargs3();
	public static StringBuffer createVerf = new StringBuffer(NfsConst.NFS3_CREATEVERFSIZE);
	public static Remove3Res removeRes = new Remove3Res();
	public static ServicePort port = new ServicePort();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		nex = new NfsExample();
		StateHandler sh = new StateHandler();
		//NfsClient(int myIP, int destinationIP, int portmapperPort, StringBuffer myHostname, Callbackable caller)
		nc = new NfsClient(Ejip.makeIp(192, 168, 10, 2), Ejip.makeIp(192, 168, 10, 1), 111, hostname, sh);
		
		//measure();
		//first we need the mount port
		nc.mount.requestMountPort(port);
	}
	
	public static void measure() {
		nc.mount.requestMountPort(port);
	}
	
	static class StateHandler implements Callbackable {
		int nextState = 0;
		int i = 0;
		StringBuffer logFileName = new StringBuffer("messages");
		
		public void callback(int error) {
			long offset, size;
			StringBuffer data;
	/*	
		}
	}
	
			static class SttateHandler  {
				int nextState = 0;
				int i = 0;
				StringBuffer logFileName = new StringBuffer("messages");
				
				public void callback(ResultType message) {  //*/
			if (error != RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_OK) {
				nextState = 100;
			
				switch (error) {
				case RpcDecodeMessageResult.RPC_DECODE_MSG_RESULT_STATUS_DENIED:
					System.out.println("\nACCESS DENIED");
					break;
				default:
					System.out.println("\nAn Error occured: " + error);
				}
			}
			switch (nextState) {
			case 0:
				System.out.println("+ mount Port: "+nc.mount.getMountPort());
				nextState = 1;
				//next we need the nfs port
				nc.nfs.requestNfsPort(port);
				break;
			case 1:
				System.out.println("+ nfs Port: " + nc.nfs.getNfsPort());
				System.out.println("Getting Exports");
				nextState = 2;
				nc.mount.getExports(exports);
				break;
			case 2: 
				//take first export and mount it
				nextState = 3;
				if (exports.getExports()[0].getExDir().length() > 0) {
					System.out.print("MOUNTING: " + exports.getExports()[0].getExDir() + " ... ");
					nc.mount.mount(exports.getExports()[0].getExDir(), mountRes);
				} else {
					System.out.println("No export has been found!");
					nextState = 100;
				}
				break;
			case 3:
				//lookup the logfile 
				if (mountRes.getError() == NfsConst.MNT3_OK) {
					System.out.println("OK");
					nextState = 4;
				} else {
					System.out.println("FAILED");
					nextState = 100;
				}
				nc.nfs.setUID(1000);
				nc.nfs.setGID(1000);
				System.out.print("LOOKING FOR LOGFILE: " + logFileName + " ... ");
				what.setDir(mountRes.getFHandle());
				what.setName(logFileName);
				nc.nfs.lookup(what, lookupRes);
				break;
			case 4:
				//when logfile exists, write first mark to it, else create logfile
				if (lookupRes.getError() == NfsConst.MNT3_OK) {
					System.out.println("FOUND");
					nextState = 7;
					size = lookupRes.getObjAttributes().getAttributes().getSize();
					if (size > 0) {
						offset = size + 1;
					} else {
						offset = 0;
					}
					System.out.print("WRITING TO LOG FILE ...");
					
					data = new StringBuffer(Timer.us() + " " + hostname + " -- MARK --\n");
					
					nc.nfs.write(lookupRes.getObject(), offset, 29, 0, data, writeRes);
				} else {
					System.out.println("NOT FOUND (OR CALL FAILED)");
					nextState = 5;
					System.out.print("CREATING LOG FILE ...");
					setObjAttributes.setMode(0x1B0); //rw-rw----
					nc.nfs.create(what, 0, setObjAttributes, createVerf, createRes);
				}
				break;
			case 5:
				//logfile now exists, write first mark to it
				if (createRes.getError() == NfsConst.MNT3_OK) {
					System.out.println("OK");
					data = new StringBuffer(Timer.us() + " " + hostname + " -- MARK --\n");
					
					nc.nfs.write(lookupRes.getObject(), 1, 29, 0, data, writeRes);
					System.out.print("WRITING TO LOG FILE ...");
					nextState = 7;
				} else {
					System.out.println("FAILED\nCouldn't create file.");
					nextState = 100;
				}
				break;
			case 6:

				break;
			case 7:
				System.out.println("WRITE RESULT:");
				if (createRes.getError() == NfsConst.MNT3_OK) {
					System.out.println("OK");
				} else {
					System.out.println("FAILED");
				}
				nextState = 8;
				break;
			case 8:
				nextState = 9;
				nc.nfs.lookup(what, lookupRes);
				break;
			case 9:
				nc.nfs.write(lookupRes.getObject(), lookupRes.getObjAttributes().getAttributes().getSize() + 1, 100, 0, new StringBuffer(Timer.us() + " " + hostname + " received packet\n"), writeRes);
				System.out.print("WRITING INCOMING PACKET TO LOG FILE ...");
				nextState = 7;
				break;
			case 100://trap
				System.out.println("Exit!");
				break;
			}
		}
	}
}
