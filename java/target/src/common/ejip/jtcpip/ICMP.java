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

import ejip.jtcpip.util.Debug;

/**
 * Class to handle ICMP packets.
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 984 $ $Date: 2007/01/24 19:37:07 $
 */
class ICMP
{
	/** 0 */
	protected final static byte TYPE_ECHO_REPLY = 0;

	/** 3: Destination unreachable */
	protected final static byte TYPE_DEST_UNREACH = 3;

	/** 4: Source quench */
	protected final static byte TYPE_SRC_QUENCH = 4;

	/** 8 */
	protected final static byte TYPE_ECHO = 8;

	/** 11: Time exceeded */
	protected final static byte TYPE_TIME_EXCEEDED = 11;

	/**
	 * Answers to an incoming ICMP ECHO.
	 * 
	 * @param pay
	 */
	private static void answerToEcho(Payload pay)
	{
//		if (Debug.enabled)
//			Debug.println("ECHO REPLY to: " + IP.ipIntToString(IPPacket.getSrcAddr(pay)), Debug.DBG_ICMP);
		if (Debug.enabled)
			Debug.println("ECHO REPLY", Debug.DBG_ICMP);
		
		ICMPPacket.setType(pay, TYPE_ECHO_REPLY);

		IP.asyncSendPayload(pay, IPPacket.getSrcAddr(pay), IP.PROT_ICMP);
	}

	/**
	 * Answers ICMP DESTINATION UNREACHABLE.
	 * 
	 * @param pay
	 * @param code
	 */
	protected static void sendDestUnreach(Payload pay, int code)
	{
//		if (Debug.enabled)
//			Debug.println("DEST UNREACHABLE to: " + IP.ipIntToString(IPPacket.getSrcAddr(pay)), Debug.DBG_ICMP);
		if (Debug.enabled)
			Debug.println("DEST UNREACHABLE", Debug.DBG_ICMP);

		ICMPPacket.ipToICMPPacket(pay);
		ICMPPacket.setType(pay, ICMP.TYPE_DEST_UNREACH);
		ICMPPacket.setCode(pay, code);

		IP.asyncSendPayload(pay, IPPacket.getSrcAddr(pay), IP.PROT_ICMP);
	}

	/**
	 * Answers ICMP SOURCE QUENCHE.
	 * 
	 * TODO: call this function if we have to close the TCP window 
	 * 
	 * @param pay
	 */
	protected static void sendSrcQuenche(Payload pay)
	{
//		if (Debug.enabled)
//			Debug.println("SOURCHE QUENCHE to: " + IP.ipIntToString(IPPacket.getSrcAddr(pay)), Debug.DBG_ICMP);
		if (Debug.enabled)
			Debug.println("SOURCHE QUENCHE ", Debug.DBG_ICMP);

		ICMPPacket.ipToICMPPacket(pay);
		ICMPPacket.setType(pay, ICMP.TYPE_SRC_QUENCH);

		IP.asyncSendPayload(pay, IPPacket.getSrcAddr(pay), IP.PROT_ICMP);
	}

	/**
	 * Answers ICMP TIME EXCEEDED.
	 * 
	 * @param pay
	 */
	protected static void sendTimeExceeded(Payload pay)
	{
//		if (Debug.enabled)
//			Debug.println("TIME EXCEEDED to: " + IP.ipIntToString(IPPacket.getSrcAddr(pay)), Debug.DBG_ICMP);
		if (Debug.enabled)
			Debug.println("TIME EXCEEDED  ", Debug.DBG_ICMP);

		ICMPPacket.ipToICMPPacket(pay);
		ICMPPacket.setType(pay, ICMP.TYPE_TIME_EXCEEDED);
		ICMPPacket.setCode(pay, 1); // 1: fragment reassembly time exceeded.

		IP.asyncSendPayload(pay, IPPacket.getSrcAddr(pay), IP.PROT_ICMP);
	}

	/**
	 * By now we just print (if Debug.enabled == true) the information, that an IP packet
	 * hasn't reached its destination.
	 * 
	 * TODO: do something if one of our IP packets has not reached its destination
	 * 
	 * @param pay
	 */
	private static void handleDestUnreach(Payload pay)
	{
		byte code = ICMPPacket.getCode(pay);
		ICMPPacket.icmpToIPPacket(pay);

		if (Debug.enabled)
			switch (code)
			{
				case 0: // net unreachable;
					Debug.println("The network of the IP address ",Debug.DBG_ICMP);
//							+ IP.ipIntToString(IPPacket.getDestAddr(pay)) + " is unreachable",
//							Debug.DBG_ICMP);
					break;
	
				case 1: // host unreachable;
					Debug.println("The host with the IP address ",Debug.DBG_ICMP);
//							+ IP.ipIntToString(IPPacket.getDestAddr(pay)) + " is unreachable",
//							Debug.DBG_ICMP);
					break;
	
				case 2: // protocol unreachable;
					Debug.println("The protocol " ,Debug.DBG_ICMP);
//							+ IPPacket.getProtocol(pay) + "is unreachable",
//							Debug.DBG_ICMP);
					break;
	
				case 3: // port unreachable;
					Debug.println("The port IP address ",Debug.DBG_ICMP);
//					+ IP.ipIntToString(IPPacket.getDestAddr(pay))
//							+ ":" + UDPPacket.getDestPort(pay) + " is unreachable", Debug.DBG_ICMP);
					break;
	
				case 4: // fragmentation needed and DF set;
					Debug.println("Fragmentation is needed but the Don't Fragment flag is set",
							Debug.DBG_ICMP);
					break;
	
				case 5: // source route failed.
					Debug.println("Source route failed", Debug.DBG_ICMP);
					break;
			}
	}

	/**
	 * Handles an incoming ICMP packet.
	 * 
	 * @param pay
	 */
	protected static void receivePayload(Payload pay)
	{
		if (Debug.enabled)
		Debug.println("ICMP received",Debug.DBG_ICMP);
			//	Debug.println("Type: " + ICMPPacket.getType(pay) + " Code: " + ICMPPacket.getCode(pay), Debug.DBG_ICMP);

		switch (ICMPPacket.getType(pay))
		{
			case TYPE_ECHO:
				answerToEcho(pay);
				break;

			case TYPE_DEST_UNREACH:
				handleDestUnreach(pay);
				Payload.freePayload(pay);
				break;

			default:
				if (Debug.enabled)
					Debug.println("Datagram unhandled...", Debug.DBG_ICMP);
				Payload.freePayload(pay);
		}
	}
}
