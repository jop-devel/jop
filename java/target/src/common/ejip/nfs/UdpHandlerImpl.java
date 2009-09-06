package ejip.nfs;

import ejip.Ip;
import ejip.Packet;
import ejip.Udp;
import ejip.UdpHandler;

/**
 * memberclass to receive packets
 *
 */
class UdpHandlerImpl extends UdpHandler {	
	StringBuffer decodeMessageBuffer = new StringBuffer();
	NfsClient nc;	
	
	public UdpHandlerImpl(NfsClient nc) {
		this.nc = nc;
	}
	
	public void request(Packet p) {
		int xid;

		synchronized(decodeMessageBuffer) {
			Ip.getData(p, Udp.DATA, decodeMessageBuffer);
			nc.ejip.returnPacket(p);
			xid = Rpc.decodeMessage(decodeMessageBuffer);
			if (decodeMessageBuffer != null) {
				if (xid != 0) { //else something went wrong
					for (int i = 0; i < nc.waitList.length; i++) {
						if (nc.waitList[i].xid == xid) {
							nc.waitList[i].xid = 0;
//							System.out.println("incoming: (index " + i + ")");
							chooseHandler(decodeMessageBuffer, nc.waitList[i]);
						}//else ignore
					}
				}
			} else {
				System.out.println("received empty rpc message"); 
			}
		}
	}	
	
	/**
	 * checks for lost packets 
	 */
	public void loop() {
		for (int i=0; i < nc.waitList.length; i++) {
			if (nc.waitList[i].xid != 0) {
				if (((int)System.currentTimeMillis() - nc.waitList[i].tstamp) > NfsClient.PACKET_TIMEOUT) {
					System.out.println("Timeout: lost packet: " + nc.waitList[i].xid + "!");
					nc.waitList[i].xid = 0;
					chooseHandler(null, nc.waitList[i]);
				}
			}
		}
	}
	
	protected void chooseHandler(StringBuffer mb, NfsClient.WaitList list) {
		switch (list.service) { 
		case RpcConst.PMAP_PROG:
			nc.handlePortmapStates(mb, list.action);
			break;
		case NfsConst.MOUNT_PROGRAM:
			nc.mount.handleStates(mb, list.action);
			break;
		case NfsConst.NFS_PROGRAM:
			nc.handleNfsStates(mb, list);
			break;
		}
		nc.caller.callback(list.dataStruct);
	}
	
}
