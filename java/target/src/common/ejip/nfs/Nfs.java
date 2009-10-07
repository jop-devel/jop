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


import util.Timer;
import joprt.RtThread;
import util.Serial;
import util.Timer;
import com.jopdesign.sys.Const;
import ejip.*;
import ejip.examples.Pinger;

public class Nfs extends Rpc {


	public static int decodePort(StringBuffer msgBuffer) {
		return Xdr.getNextInt(msgBuffer);
	}
	
	/**
	 * @param program		the program number
	 * @param version		the version of the program to call
	 * @param procedure		the procedure to call
	 * @param port			the target port (use queryPortMapper to find)
	 * @param data			procedure specific call data - in XDR format
	 * @param auth			the authentication type (AUTH_NULL, AUTH_SYS)
	 * @param xid			an arbitrary number
	 * @return				XID of the call (serves as handle, same as input XID)
	 */
	static void call(StringBuffer messageBuffer, int program, int version, int procedure, StringBuffer data, byte auth, int xid) {
		synchronized(messageBuffer) {
			//append program specific data:
			if (data != null) {
				messageBuffer.append(data);
			}
		}
	} 
	
	

	public static void decodeExports(StringBuffer msgBuffer, StringBuffer[] exportsList, StringBuffer[][] groups) {
		int i = 0, j = 0;
		while (Xdr.getNextInt(msgBuffer) != 0) {
			if (i < exportsList.length) {
				 Xdr.getNextStringBuffer(msgBuffer, exportsList[i]);
				System.out.println(exportsList[i]);
				j = 0;
				while (Xdr.getNextInt(msgBuffer) != 0) {
					if (j < groups[i].length) {
						 Xdr.getNextStringBuffer(msgBuffer, groups[i][j++]);
					}
				}
				i++;				
			}
		}
	}
	
	public static void decodeMountlist(StringBuffer msgBuffer, StringBuffer[][] mountList) {
		int i = 0;
		while (Xdr.getNextInt(msgBuffer) != 0) {
			if (i < mountList.length) {
				 Xdr.getNextStringBuffer(msgBuffer, mountList[i][0]);
				 Xdr.getNextStringBuffer(msgBuffer, mountList[i++][1]);
			}
		}
	}
	
	public static void decodeMountCallOK(StringBuffer msgBuffer, StringBuffer fHandle, int[] flavors) {
		int amountOfFlavors;
		if (Xdr.getNextInt(msgBuffer) == NfsConst.MNT3_OK) {
			Xdr.getNextStringBuffer(msgBuffer, fHandle);
			amountOfFlavors = Xdr.getNextInt(msgBuffer);
			for (int i = 0; i < amountOfFlavors; i++) {
				if (i < flavors.length) {
					flavors[i] = Xdr.getNextInt(msgBuffer);
				}
			}
		}
	}
	
	public static class Mount {
	
		public static void nullCall(StringBuffer messageBuffer) {
			//messageBuffer.append(null); 
//			mountCall(messageBuffer, NfsConst.MOUNTPROC3_NULL, null, RpcConst.AUTH_NULL, xid);
		}
		
		public static void getExports(StringBuffer messageBuffer) {
//			mountCall(messageBuffer, NfsConst.MOUNTPROC3_EXPORT, null, RpcConst.AUTH_SYS, xid);
		}
		
		public static void mount(StringBuffer messageBuffer, int auth, int xid, StringBuffer path) {
//			Xdr.append(messageBuffer, path);
		}
//		
//		public static void unmount(StringBuffer path) {
//			StringBuffer workingBuffer = new StringBuffer();
//			workingBuffer.setLength(0);
//			Xdr.append(workingBuffer, path);
//			mountCall(NfsConst.MOUNTPROC3_UMNT, workingBuffer, RpcConst.AUTH_SYS, Nfs.newHandle(ACTION_CALL_UMOUNT));
//		}
//		
//		public static void umountAll() {
//			mountCall(NfsConst.MOUNTPROC3_UMNTALL, null, RpcConst.AUTH_SYS, Nfs.newHandle(ACTION_CALL_UMOUNTALL));
//		}
//		
		public static void dump(StringBuffer messageBuffer, int auth, int xid) {
//			mountCall(messageBuffer, null, RpcConst.AUTH_SYS, xid));
		}
//		
//		public static boolean mountCall(StringBuffer messageBuffer, int procedure, StringBuffer data, byte auth, int xid) {
//			call(messageBuffer, NfsConst.MOUNT_PROGRAM, NfsConst.MOUNT_VERSION, procedure, data, auth, xid);
//			return true;
//		}
	}

}
