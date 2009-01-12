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

import ejip_old.Net;
import ejip_old.Packet;
import ejip.jtcpip.util.Debug;
import ejip.jtcpip.util.NumFunctions;

/**
 * Represents the network layer. Contains methods for Packet sending and
 * receiving (including reassembly). Received Packets are then handed to the
 * upper (transport) layer.
 * 
 * @see ejip2.jtcpip.TCP
 * @see ejip2.jtcpip.UDP
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 994 $ $Date: 2009/01/12 23:00:13 $
 */
public class IP {
	/** Default Time To Live Value */
	private static final byte TTL = (byte) 128;

	/** Protocol Constant for IP Header: ICMP */
	public static final byte PROT_ICMP = 1;

	/** Protocol Constant for IP Header: TCP */
	public static final byte PROT_TCP = 6;

	/** Protocol Constant for IP Header: UDP */
	public static final byte PROT_UDP = 17;

	/**
	 * Packet identification for IP Header. Incremented after each send. (From a
	 * security point of view, this is questionable) TODO
	 */
	private static short packetID = 0;

	/** Exception that gets thrown when there is an error in the IP Address */

	private static JtcpipException ipException;

	public static void init() {
		ipException = new JtcpipException("Invalid IP Address String");
	}

	/**
	 * Stores the content of a fragment into a reassembling payload
	 * 
	 * @param rsmblPay
	 * @param fragmtPay
	 */
	private static void rsmblStore(Payload rsmblPay, Payload fragmtPay) {
		int pLen = IPPacket.getLength(fragmtPay) - IPPacket.getIHL(fragmtPay)
				* 4;
		int ofs = IPPacket.getFragOfs(fragmtPay) * 2;
		int maxIndex = NumFunctions.divRoundUp(pLen, 4);

		if (ofs + maxIndex > rsmblPay.payload.length) {
			// if (Debug.enabled)
			// Debug
			// .println(
			// "Reassembled Payload would be greater than max. Payload size!
			// Offset: "
			// + ofs + " max. index: " + maxIndex
			// + " Payload.length: "
			// + rsmblPay.payload.length, Debug.DBG_IP);

			ICMP.sendDestUnreach(rsmblPay, 4); // fragmentation needed and DF
			// set;
			return;
		}

		/*
		 * To prevent data overwriting (as for the first call rsmblPay ==
		 * fragmtPay) we copy the data back to front
		 */

		switch (pLen % 4) {// copy the last bytes but don't overwrite the
		// previous content
		case 1:
			fragmtPay.payload[maxIndex] &= 0xFF000000;
			rsmblPay.payload[ofs + maxIndex] &= 0x00FFFFFF;
			rsmblPay.payload[ofs + maxIndex] |= fragmtPay.payload[maxIndex];
			break;

		case 2:
			fragmtPay.payload[maxIndex] &= 0xFFFF0000;
			rsmblPay.payload[ofs + maxIndex] &= 0x0000FFFF;
			rsmblPay.payload[ofs + maxIndex] |= fragmtPay.payload[maxIndex];
			break;

		case 3:
			fragmtPay.payload[maxIndex] &= 0xFFFFFF00;
			rsmblPay.payload[ofs + maxIndex] &= 0x000000FF;
			rsmblPay.payload[ofs + maxIndex] |= fragmtPay.payload[maxIndex];
			break;
		}

		for (int i = pLen / 4 - 1; i >= 0; i--)
			rsmblPay.payload[ofs + i] = fragmtPay.payload[i];

		// if the offset == 0 the first fragment is stored and we can send an
		// ICMP message in case of a time out
		rsmblPay.setOffset(IPPacket.getFragOfs(fragmtPay));

		rsmblPay.reassembledBitMap.setBits(IPPacket.getFragOfs(fragmtPay),
				pLen / 8);

		if (!IPPacket.isMFSet(fragmtPay)) {// last fragment => calculate bytes
			// of unfragmented packet
			rsmblPay.length = pLen + IPPacket.getFragOfs(fragmtPay) * 8;

			// Only the very last datablock that doesn't fall on an eight byte
			// boundary _may_ mark a block in the bitmap (else we wait for a
			// fragment
			// that holds the complete datablock)
			if (pLen % 8 > 0)
				rsmblPay.reassembledBitMap.setBit(IPPacket
						.getFragOfs(fragmtPay)
						+ (pLen / 8));
		}

		if (Debug.enabled && (Debug.dbgFlagsToDisplay & Debug.DBG_IP) > 0)
			rsmblPay.reassembledBitMap.print();
	}

	/**
	 * Handles the reassembling of a IP packet fragment
	 * 
	 * @param pay
	 */
	private static void reassemblePayload(Payload pay) {
		for (int i = 0; i < StackParameters.PAYLOAD_POOL_SIZE; i++) {
			if (Payload.pool[i] != null
					&& Payload.pool[i].getStatus() == Payload.PAYLOAD_RESMBL
					&& (IPPacket.getID(pay) == IPPacket.getID(Payload.pool[i]))) { // just
				// received
				// a
				// packet
				// with
				// the
				// same
				// ID
				rsmblStore(Payload.pool[i], pay);
				Payload.freePayload(pay);

				// if (Debug.enabled)
				// if (Payload.pool[i].length != -1)
				// Debug.println("Seeing if all "
				// + NumFunctions.divRoundUp(
				// Payload.pool[i].length, 8)
				// + " bits are set", Debug.DBG_IP);

				if (Payload.pool[i].length != -1 // length is -1 until we got
						// the last fragment.
						&& Payload.pool[i].reassembledBitMap
								.allSet(NumFunctions.divRoundUp(
										Payload.pool[i].length, 8))) {
					Payload.pool[i].setStatus(Payload.PAYLOAD_USED, 0);
					//handlePayload(Payload.pool[i]);
					byte prot = IPPacket.getProtocol(Payload.pool[i]);

					switch (prot) {

					case PROT_TCP:
						TCP.receivePayload(Payload.pool[i]);
						break;

					case PROT_UDP:
						UDP.receivePayload(Payload.pool[i]);
						break;

					case PROT_ICMP:
						ICMP.receivePayload(Payload.pool[i]);
						break;
					}
				
				
				}

				return;
			}
		}

		// No matching payload found => this is the first part of a fragmented
		// payload

		pay.length = -1; // the payload length is invalid until we got the
		// last fragment
		pay.setStatus(Payload.PAYLOAD_RESMBL,
				StackParameters.REASSEMBLE_TIMEOUT);
		pay.setOffset(StackParameters.PAYLOAD_MAX_DATA_SIZE); // No fragments
		// stored yet
		rsmblStore(pay, pay); // move the payload to the correct position
	}

	/**
	 * Copies the data stored in a <code>Payload</code> object to a
	 * <code>Packet</code> object. Also sets the protocol field in the
	 * Ethernet header to IP.
	 * 
	 * @param pay
	 *            Payload to copy from
	 * @param p
	 *            Packet to write to
	 * 
	 * @return Zero if the full payload fitted into the packet else the number
	 *         of already copied bytes
	 */
	protected static int payloadToPacket(Payload pay, Packet p) {
		return payloadToPacket(pay, p, 0);
	}

	/**
	 * Copies the data (up to the size of a Ethernet packet) stored in a
	 * <code>Payload</code> object to a <code>Packet</code> object. Also
	 * sets the protocol field in the Ethernet header to IP.
	 * 
	 * @param pay
	 *            Payload to copy from
	 * @param p
	 *            Packet to write to
	 * @param offset
	 *            in 4-byte-blocks (i.e. already sent blocks)
	 * @return The offset where the data for the next Packet starts (if
	 *         fragmentation needed, else 0)
	 */
	protected static int payloadToPacket(Payload pay, Packet p, int offset) {
		byte headerLength = IPPacket.getIHL(pay);
		boolean isFragmt = (pay.length + headerLength * 4) > p.buf.length * 4
				&& pay.getStatus() != Payload.PAYLOAD_ARP_WT;

		if (isFragmt && IPPacket.isDFSet(pay)) {
			ICMP.sendDestUnreach(pay, 4); // Code 4: fragmentation needed and
			// DF set
			return 0;
		}

		/*
		 * if (isFragmt && (offset % 2 != 0)) //Should not be needed anymore {
		 * if (Debug.enabled) Debug.println("ALERT! Fragment not on boundary",
		 * Debug.DBG_IP); offset--; // Fragmentation Offset not on an 8-byte
		 * boundary -> overlap }
		 */

		int dataInPayload = headerLength * 4 + pay.length - offset * 4; // Size
		// of
		// the
		// (rest
		// of
		// the)
		// payload

		p.len = Math.min(p.buf.length * 4, dataInPayload);
		p.llh[6] = 0x0800; // IP code for ethernet header

		if (isFragmt) // IP Fragmentation!
		{
			if (p.len == dataInPayload) // Last fragment of the Payload
				IPPacket.clearMF(pay);
			else {
				p.len -= (p.len - headerLength * 4) % 8; // Fragmentation
				// offset - 8 byte
				// boundary!
				IPPacket.setMF(pay);
			}

			IPPacket.setLength(pay, (short) p.len);
			IPPacket.setFragOfs(pay, (short) (offset / 2));
		}

		IPPacket.setChecksum(pay);

		// copy the IP header into the Ethernet packet
		for (byte i = 0; i < headerLength; i++)
			p.buf[i] = pay.ipHeader[i];

		int i = 0;

		int lastPayloadIndex = NumFunctions.divRoundUp(pay.length, 4);
		int limit = Math.min(NumFunctions.divRoundUp(p.len, 4) - headerLength,
				lastPayloadIndex - offset);

		for (i = 0; i < limit; i++)
			p.buf[i + headerLength] = pay.payload[offset + i];

		// TODO: maybe padding if too short
		if (p.len == dataInPayload)
			return 0;
		return offset + 1;

		// return p.len == dataInPayload ? 0 : offset + i; // if the size of the
		// (rest of the) payload
		// == the packet length
		// return 0 else the
		// offset
	}

	/**
	 * Takes a newly received Packet from the network driver. The content is
	 * then stored in a <code>Payload</code> object. If the Packet is valid
	 * (checksum, ehternet header...) it will be delivered to to the next layer
	 * (via {@link IP#handlePayload}). If a packet is fragmented, it will be
	 * reassembled here. <b>Note:</b> After the method returns, the passed
	 * Packet will be marked as FREE!
	 * 
	 * @see #handlePayload
	 * @param p
	 *            Packet to process
	 */
	public static void receivePacket(Packet p) {

		if (Debug.enabled)
			Debug.println("Received a packet", Debug.DBG_IP);
		if (p.len < 20) {
			if (Debug.enabled)
				Debug.println("Packet is shorter than 20 bytes, dropping!!",
						Debug.DBG_IP);
			p.setStatus(Packet.FREE);
			return;
		}

		if (p.llh[6] != 0x0800) {
			if (Debug.enabled)
				Debug.println("PACKET IS NOT AN IP PACKET!!", Debug.DBG_IP);
			p.setStatus(Packet.FREE);
			return;
		}

		Payload pay = Payload.newPayload();
		if (pay == null) {
			if (Debug.enabled)
				Debug.println("No more Payloads available - packet dropped",
						Debug.DBG_IP);
			p.setStatus(Packet.FREE);
			return;
		}

		for (int i = 0; i < 5; i++)
			// copy first 20 byte of header to ipHeader (therefore we are not
			// able do read out IPPacket.getIHL(pay))
			pay.ipHeader[i] = p.buf[i];

		// accept only packets for our ip or eth broadcasts
		if (IPPacket.getDestAddr(pay) != Net.linkLayer.getIpAddress()
				&& !(p.llh[0] == 0xFFFF && p.llh[1] == 0xFFFF && p.llh[2] == 0xFFFF)) {
			// System.out.println(IPPacket.getDestAddr(pay));
			// System.out.println(Net.linkLayer.ip);
			if (Debug.enabled)
				Debug.println("Packet is not for us... dropping", Debug.DBG_IP);
			p.setStatus(Packet.FREE);
			Payload.freePayload(pay);
			return;
		}

		int headerLength = (int) (IPPacket.getIHL(pay) & 0xFF);
		if (headerLength > 5) // if header is longer than 20 byte copy the
			// rest
			for (int i = 5; i < headerLength; i++)
				pay.ipHeader[i] = p.buf[i];

		// if (Debug.enabled)
		// Debug.println("Captured packet length: ", Debug.DBG_IP);

		// check if the packet length is acceptable, drop the packet if wrong
		int payloadLengtFromIPHeader = ((int) (IPPacket.getLength(pay) & 0xFFFF) - headerLength * 4);
		if (payloadLengtFromIPHeader > p.len - headerLength * 4
				|| payloadLengtFromIPHeader > StackParameters.PAYLOAD_MAX_DATA_SIZE) {
			if (Debug.enabled)
				Debug.println("Packet is too long, dropping", Debug.DBG_IP);
			p.setStatus(Packet.FREE);
			Payload.freePayload(pay);
			return;
		}
		for (int i = 0; i < (NumFunctions.divRoundUp(p.len, 4) - headerLength); i++) // copy
		// payload
		{
			pay.payload[i] = p.buf[i + headerLength];
			// if (Debug.enabled)
			// Debug.print(Debug.intToHexString(pay.payload[i]), Debug.DBG_IP);
		}

		// Take the min of the length specified in the IP header and the real
		// captured data as Payload length
		pay.length = payloadLengtFromIPHeader;
		p.setStatus(Packet.FREE);

		if (!IPPacket.isValidPacket(pay)) {
			if (Debug.enabled)
				Debug.println("Checksum not valid; dropping packet",
						Debug.DBG_IP);
			Payload.freePayload(pay);
			return;
		}

		if (IPPacket.isMFSet(pay) || IPPacket.getFragOfs(pay) != 0) {
			if (Debug.enabled)
				Debug.println("Valid Packet, FRAGMENTED", Debug.DBG_IP);
			reassemblePayload(pay);
		} else { // not fragmented
			if (Debug.enabled)
				Debug.println("Valid Packet, not fragmented", Debug.DBG_IP);
			// TODO: inlined
			 //handlePayload(pay);
		
			byte prot = IPPacket.getProtocol(pay);

			switch (prot) {

			case PROT_TCP:
				TCP.receivePayload(pay);
				break;

			case PROT_UDP:
				UDP.receivePayload(pay);
				break;

			case PROT_ICMP:
				ICMP.receivePayload(pay);
				break;
			}
		}
		/*
		 * if (Debug.enabled){ Debug.println("version " +
		 * IPPacket.getVersion(pay)); Debug.println("ihl " +
		 * IPPacket.getIHL(pay)); Debug.println("tos " +
		 * Debug.IntegerToHexString(IPPacket.getToS(pay)));
		 * Debug.println("length: " + IPPacket.getLength(pay));
		 * Debug.println("id: " + IPPacket.getID(pay)); Debug.println("res: " +
		 * IPPacket.isReservedSet(pay)); Debug.println("fragm: " +
		 * IPPacket.isDFSet(pay)); Debug.println("more frag: " +
		 * IPPacket.isMFSet(pay)); Debug.println("frag ofs: " +
		 * IPPacket.getFragOfs(pay)); Debug.println("ttl: " +
		 * IPPacket.getTTL(pay)); Debug.println("protocol: " +
		 * Debug.IntegerToHexString(IPPacket.getProtocol(pay)));
		 * Debug.println("src: " +
		 * Debug.IntegerToHexString(IPPacket.getSrcAddr(pay)));
		 * Debug.println("dest addr: " +
		 * Debug.IntegerToHexString(IPPacket.getDestAddr(pay))); }
		 */
	}

	/**
	 * Prepares the IP header. In order to send a packet
	 * 
	 * @param pay
	 *            The packet payload
	 * @param destIP
	 *            The destination IP
	 * @param protocol
	 *            The transport protocol id
	 */
	private static void prepareIPPacket(Payload pay, int destIP, byte protocol) {
		IPPacket.setIHL(pay, (byte) 0x5);
		IPPacket.setToS(pay, (byte) 0x0);
		IPPacket
				.setLength(pay, (short) (pay.length + IPPacket.getIHL(pay) * 4));
		IPPacket.setID(pay, packetID++);
		IPPacket.clearDF(pay);
		IPPacket.clearMF(pay);
		IPPacket.clearReserved(pay);
		IPPacket.setFragOfs(pay, (short) 0x0);
		IPPacket.setTTL(pay, (byte) TTL);
		IPPacket.setProtocol(pay, protocol);
		if ((destIP >= 2130706432) && (destIP <= 2147483647)) // 127.0.0.0/8
		{
			IPPacket.setDestAddr(pay, Net.linkLayer.getIpAddress()); // Swapping
			// addresses for
			// loopback
			IPPacket.setSrcAddr(pay, destIP);
		} else {
			IPPacket.setDestAddr(pay, destIP);
			IPPacket.setSrcAddr(pay, Net.linkLayer.getIpAddress());
		}

		switch (IPPacket.getProtocol(pay)) {
		case PROT_TCP:
			TCPPacket.setChecksum(pay);
			break;
		case PROT_UDP:
			UDPPacket.setChecksum(pay);
			break;
		case PROT_ICMP:
			ICMPPacket.setChecksum(pay);
			break;
		}

	}

	/**
	 * @deprecated
	 * 
	 * Sends a Packet by handing it to the network driver. Takes a payload, a
	 * destination ip and a protocol number (e.g. <code>IP.PROT_TCP</code>).
	 * <b>Note:</b> After the method returns, the passed Payload will be marked
	 * as free!
	 * 
	 * @param pay
	 *            The packet payload
	 * @param destIP
	 *            The destination IP
	 * @param protocol
	 *            The transport protocol id
	 * 
	 * @return Whether send was successful
	 */
	public static boolean sendPayload(Payload pay, int destIP, byte protocol) {
		prepareIPPacket(pay, destIP, protocol);

		if (pay.length > StackParameters.PACKET_MTU_SIZE) {
			// TODO: Implement fragmentation here? Would this make sense?
			if (Debug.enabled)
				Debug.println("Fragmentation not implemented here!",
						Debug.DBG_IP);
			return false;
		}

		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, Net.linkLayer);
		if (p == null)
			return false; // TODO: Retransmit

		payloadToPacket(pay, p);
		Payload.freePayload(pay);
		p.setStatus(Packet.SND_DGRAM);

		return true;
	}

	/**
	 * Prepares and marks a Payload for sending
	 * 
	 * @param pay
	 *            The packet payload
	 * @param destIP
	 *            The destination IP
	 * @param protocol
	 *            The transport protocol id
	 */
	public synchronized static void asyncSendPayload(Payload pay, int destIP,
			byte protocol) {
		if (Debug.enabled)
			Debug.println("asyncSendPayload", Debug.DBG_IP);

		prepareIPPacket(pay, destIP, protocol);
		if (Debug.enabled)
			Debug.println("asyncSendPayload2", Debug.DBG_IP);

		pay.setStatus(Payload.PAYLOAD_SND_RD, 0);
		if (Debug.enabled)
			Debug.println("asyncSendPayload3", Debug.DBG_IP);

	}

	/**
	 * Converts an IP address of the format [xx]x.[xx]x.[xx]x.[xx]x to an
	 * integer. Throws an exception if the format of the String is faulty.
	 * 
	 * @param ipAddr
	 *            The IP address String
	 * @return The IP address as an int
	 * @throws JtcpipException
	 *             Invalid IP Address String
	 */
	public static int ipStringToInt(String ipAddr) throws JtcpipException {
		byte dots = 0;
		short ipOctet = 0;
		int ipInt = 0;
		for (int i = 0; i <= ipAddr.length(); i++) {
			if (i == ipAddr.length() || ipAddr.charAt(i) == '.') {
				if (i < ipAddr.length() && ++dots == 4) {
					// if (Debug.enabled)
					// Debug.println("Too many dots in " + ipAddr,
					// Debug.DBG_IP);
					throw ipException;
				}

				if (ipOctet < 0 || ipOctet > 255) {
					// if (Debug.enabled)
					// Debug.println("Wrong IP values in " + ipAddr,
					// Debug.DBG_IP);
					throw ipException;
				}

				ipInt = (ipInt << 8) | (ipOctet & 0xFF);
				ipOctet = 0;
			} else if (ipAddr.charAt(i) >= '0' && ipAddr.charAt(i) <= '9')
				ipOctet = (short) (ipOctet * 10 + (ipAddr.charAt(i) - '0'));
			else {
				if (Debug.enabled)
					Debug.println("Wrong char in IP address ", Debug.DBG_IP);
				throw ipException;
			}
		}

		if (dots != 3) {
			if (Debug.enabled)
				Debug.println("IP address too short ", Debug.DBG_IP);
			throw ipException;
		}
		return ipInt;
	}

	/**
	 * @param ipAddr
	 * @return the String representation of an IP address
	 */
	public static String ipIntToString(int ipAddr) {
		return "" + ((ipAddr >>> 24) & 0xFF) + "." + ((ipAddr >>> 16) & 0xFF)
				+ "." + ((ipAddr >>> 8) & 0xFF) + "." + ((ipAddr) & 0xFF);
	}

}
