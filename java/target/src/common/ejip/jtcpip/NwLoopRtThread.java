/* 
 * Copyright  (c) 2006-2007 Graz University of Technology. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The names "Graz University of Technology" and "IAIK of Graz University of
 *    Technology" must not be used to endorse or promote products derived from
 *    this software without prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY  OF SUCH DAMAGE.
 */

package ejip.jtcpip;

import util.Dbg;
import joprt.RtThread;
import ejip_old.Arp;
import ejip_old.LinkLayer;
import ejip_old.Net;
import ejip_old.Packet;
import ejip.jtcpip.util.Debug;

/**
 * Network loop class. Creates a thread for a given Net and LinkLayer instance
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 984 $ $Date: 2009/01/12 23:00:13 $
 */
public class NwLoopRtThread extends RtThread {
	public static TCPConnection conn = null;

	public static Payload pay = null;

	/** Holds the instance for the singelton pattern */
	private static NwLoopRtThread nwLoop;

	/**
	 * Holds a references to the LinkLayer instances to call the
	 * LinkLayer.loop()
	 */
	private LinkLayer linkLayer;

	/** counts the loop cycles up to StackParameters.NW_LOOP_CYCLES */
	private int cycleCnt = 0;

	/**
	 * if the cycleCnt % tcpModulus == 0 the next TCP connection is tested to
	 * send new data of the oStream
	 */
	private int tcpModulus = StackParameters.NW_LOOP_CYCLES
			/ StackParameters.TCP_CONNECTION_POOL_SIZE;

	/**
	 * Creates the network loop instance
	 * 
	 * @param linkLayer
	 *            Instances to the link layers
	 * @param msDelay
	 *            Delay in milli seconds between two calls
	 */
	private NwLoopRtThread(LinkLayer linkLayer, int msDelay) {
		super(10, 10000);
		this.linkLayer = linkLayer;
	}

	public void run() {
		for (;;) {
		//	Dbg.wr("NWLOOP\n");

			waitForNextPeriod();
			if (cycleCnt % tcpModulus == 0) {
				int conToTest = cycleCnt / tcpModulus;
				if (TCPConnection.pool[conToTest] != null
						&& TCPConnection.pool[conToTest].getState() != TCPConnection.STATE_CLOSED) {
					if (!TCPConnection.pool[conToTest].pollConnection())
						TCPConnection.retryToSendData = conToTest;
				}
			} else {
				if (TCPConnection.retryToSendData > -1) {
					if (TCPConnection.pool[TCPConnection.retryToSendData]
							.pollConnection())
						TCPConnection.retryToSendData = -1;
				}
			}

			int cnt = Payload.waitingPayloadCount();

			if (cnt > 0) {
				byte status;

				for (int i = 0; i < StackParameters.PAYLOAD_POOL_SIZE; i++) {
					status = Payload.pool[i].getStatus();
					if (status > Payload.PAYLOAD_WND_RX) {
						cnt--;
						switch (status) {
						case Payload.PAYLOAD_FRAGMT:
						case Payload.PAYLOAD_SND_RD:
							if (preparedSend(Payload.pool[i])) {
								// Everything ok -> free payload
								Payload.freePayload(Payload.pool[i]);
							}
							break;

						case Payload.PAYLOAD_ARP_WT:
							if (Arp.inCache(IPPacket
									.getDestAddr(Payload.pool[i]))) {
								if (Payload.pool[i].getOffset() == 0)
									Payload.pool[i].setStatus(
											Payload.PAYLOAD_SND_RD, 0);
								else
									Payload.pool[i].setStatus(
											Payload.PAYLOAD_FRAGMT,
											Payload.pool[i].getOffset());
							}

							if (Payload.pool[i].isTimeout()) {
								if (Debug.enabled)
									Debug.println("ARP timeout!",
											Debug.DBG_OTHER);
								Payload.freePayload(Payload.pool[i]);
								// No one to inform (ICMP) -> free payload
							}
							break;

						case Payload.PAYLOAD_RESMBL:
							if (Payload.pool[i].isTimeout()) {
								if (Payload.pool[i].getOffset() == 0)
									// The first fragment has been received
									// -> So we can inform the remote host
									ICMP.sendTimeExceeded(Payload.pool[i]);
								else
									Payload.freePayload(Payload.pool[i]);
								// Just free the payload
							}
							break;

						default:
							if (Debug.enabled)
								Debug.println("Unknown Payload status: ",
										Debug.DBG_OTHER);
						}
					}

					if (cnt <= 0)
						break;
				}

			}

			Packet p = Packet.getPacket(Packet.RCV, Packet.ALLOC);
			if (p != null) {
				IP.receivePacket(p);
				Dbg.wr("Ip.receive returned\n");
			}
			if ((conn != null) && (pay != null)) {
				TCP.sendPackets(conn, pay);
				conn = null;
				pay = null;
			}

			cycleCnt++;
			if (cycleCnt >= StackParameters.NW_LOOP_CYCLES)
				cycleCnt = 0;

		}
		
	}

	/**
	 * Tries to send a payload that is in the PAYLOAD_SND_RD state
	 * 
	 * @param pay
	 * @return true if the complete payload has been sent
	 */
	protected static boolean preparedSend(Payload pay) {
		if (Debug.enabled)
			Debug.println("prepared send", Debug.DBG_OTHER);

		// TODO for Dhcp
		// int firstHopDest = (LinkLayer.isSameSubnet(IPPacket.getDestAddr(pay))
		// ? IPPacket
		// .getDestAddr(pay)
		// : Net.linkLayer.gateway);
		int firstHopDest = IPPacket.getDestAddr(pay);
		// System.out.println(IPPacket.getDestAddr(pay));

		// FIXME: For LinkLayers that do not require ARP (e.g. Slip, Loopback)
		// this has to be modified
		boolean inCache = (Arp.inCache(firstHopDest)
				|| (firstHopDest == Net.linkLayer.getIpAddress())
		// loopback to same addr
		|| (firstHopDest >= 2130706433 && firstHopDest <= 2147483646));
		// loopback to 127.x.x.x

		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, Net.linkLayer);

		if (p == null)
			return false;

		if (!inCache)
			pay.setStatus(Payload.PAYLOAD_ARP_WT, StackParameters.ARP_TIMEOUT);
		int offset = 0;
		if (inCache)
			offset = pay.getOffset();
		else
			offset = Math.max(pay.length / 4 - 1, 0);

		// int offset = inCache ? pay.getOffset() : Math
		// .max(pay.length / 4 - 1, 0);
		// // If ARP request use just the minimum of Payload

		offset = IP.payloadToPacket(pay, p, offset);

		if (firstHopDest == Net.linkLayer.getIpAddress() // loopback to same
				// addr
				|| (firstHopDest >= 2130706433 && firstHopDest <= 2147483646)) // loopback
			// to
			// 127.x.x.x
			p.setStatus(Packet.RCV);
		else {
			if (!inCache && IPPacket.getDestAddr(pay) != firstHopDest) { // Request
				// ARP
				// resolution
				// for
				// gateway

				// IP destination address (without gateway) is
				// at position 4 for IP packets and at 6 for ARP packets
				int addrPos = p.llh[6] == 0x0806 ? 6 : 4;
				p.buf[addrPos] = firstHopDest;
			}
			p.setStatus(Packet.SND_DGRAM);
		}

		if (offset > 0)
			pay.setStatus(Payload.PAYLOAD_FRAGMT, offset);

		return inCache && offset == 0;
	}

	/**
	 * Creates and start the network loop thread
	 * 
	 * @param linkLayer
	 * @return The instance of the network loop
	 */
	public static NwLoopRtThread createInstance(LinkLayer linkLayer) {
		if (nwLoop == null) {
			nwLoop = new NwLoopRtThread(linkLayer,
					StackParameters.NW_LOOP_TIMEOUT);
			// nwLoop.start();
		}

		return nwLoop;
	}

	/**
	 * Returns the instance of the network loop
	 * 
	 * @return NwLoopThread
	 */
	public static NwLoopRtThread getInstance() {
		return nwLoop;
	}
}
