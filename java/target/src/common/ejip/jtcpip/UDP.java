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
import ejip.jtcpip.util.Debug;

/**
 * Represents the UDP transport layer. Contains methods for packet sending and
 * receiving to the upper (application) layer and to the lower (network) layer.
 * 
 * @see ejip2.jtcpip.IP
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 984 $ $Date: 2009/01/12 23:00:13 $
 */
public class UDP
{
	/**
	 * Send a UDP Packet with the destination IP given as a String.
	 * 
	 * @param ip
	 *            String containing the destination ip in dotted-decimal format
	 * @param port
	 *            Destination port
	 * @param data
	 *            Data to send
	 * @return Whether sending was successful
	 * @throws JtcpipException
	 *             IP String is faulty
	 */
	public static boolean sendPacket(String ip, short port, byte[] data) throws JtcpipException
	{
		return sendPacket(IP.ipStringToInt(ip), port, data);
	}

	/**
	 * Send a UDP Packet with the destination IP given as four bytes.
	 * 
	 * @param a
	 *            First byte of the destination IP
	 * @param b
	 *            Second byte of the destination IP
	 * @param c
	 *            Third byte of the destination IP
	 * @param d
	 *            Fourth byte of the destination IP String containing the
	 *            destination ip in dotted-decimal format
	 * @param port
	 *            Destination port
	 * @param data
	 *            Data to send
	 * @return Whether sending was successful
	 * 
	 * @throws JtcpipException
	 */
	public static boolean sendPacket(byte a, byte b, byte c, byte d, short port, byte[] data)
			throws JtcpipException
	{
		return sendPacket(((a & 0xFF) << 24) + ((b & 0xFF) << 16) + ((c & 0xFF) << 8) + (d & 0xFF),
				port, data);
	}

	/**
	 * Send a UDP Packet with the destination IP given as an int.
	 * 
	 * @param remoteIP
	 *            Int containing the destination ip
	 * @param remotePort
	 *            Destination port
	 * @param data
	 *            Data to send
	 * @return Whether sending was successful
	 * 
	 * @throws JtcpipException
	 */
	public static boolean sendPacket(int remoteIP, short remotePort, byte[] data)
			throws JtcpipException
	{
		Payload pay = preparePayload(remotePort, (short) 0);
		if (pay == null)
			return false;

		UDPPacket.setData(pay, data);
		UDPPacket.setLength(pay, (short) pay.length); // Sets the length in
														// the UDP header

		if (Debug.enabled)
			Debug.println("Trying to send", Debug.DBG_UDP);
		IP.asyncSendPayload(pay, remoteIP, IP.PROT_UDP);
		return true;
	}

	/**
	 * Gets and prepares a UDP Packet with the destination IP given as an int.
	 * 
	 * @param remotePort
	 *            Destination port
	 * @param localPort
	 * 
	 * @return the payload or null
	 */
	protected static Payload preparePayload(short remotePort, short localPort)
	{
		Payload pay = Payload.newPayload();

		if (pay != null)
		{
			UDPPacket.setDestPort(pay, remotePort);
			UDPPacket.setSourcePort(pay, localPort);
		}

		return pay;
	}

	/**
	 * Receive a packet from the lower (network) Layer.
	 * 
	 * @param pay
	 *            The packet content
	 * @see ejip2.jtcpip.IP#handlePayload
	 */
	public static void receivePayload(Payload pay)
	{
		if (Debug.enabled)
			Debug.println("UDP packed received! ID: ",
				Debug.DBG_UDP);

		if (!UDPPacket.isChecksumValid(pay))
		{
			if (Debug.enabled)
				Debug.println("Checksum not valid", Debug.DBG_UDP);
			Payload.freePayload(pay);
			return;
		}

		UDPConnection conn = UDPConnection.getConnection(pay);

		if (conn != null)
		{
			conn.receivePayload(pay);
			Payload.freePayload(pay);
		}
		else
		{
			if (IPPacket.getDestAddr(pay) == Net.linkLayer.getIpAddress())
			{
				if (Debug.enabled)
					Debug.println("No matching connection found", Debug.DBG_UDP);
				ICMP.sendDestUnreach(pay, 3); // Code 3: port unreachable (see RFC792)
			}
			else // its a broadcast...
				Payload.freePayload(pay); 
		}
	}
}
