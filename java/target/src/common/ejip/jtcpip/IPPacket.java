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

/**
 * The IP Packet Class encapsulating methods to handle a Payload as an IP
 * Packet. All methods are static and get a {@link Payload} as a parameter.
 * There are get and set methods for all fields of the IP header.
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 849 $ $Date: 2007/01/24 19:37:07 $
 */
public class IPPacket
{
	/** Position of the Reserved Flag in the 2nd word of the IP header */
	private static final int RESERVED_MASK = 0x8000;

	/** Position of the Don't Fragment Flag in the 2nd word of the IP header */
	private static final int DF_MASK = 0x4000;

	/** Position of the More Fragments Flag in the 2nd word of the IP header */
	private static final int MF_MASK = 0x2000;

	/**
	 * Get the IP Version. Should be 4 for IPv4
	 * 
	 * @param pay
	 *            The Payload
	 * @return The IP Version
	 */
	public static byte getVersion(Payload pay)
	{
		return (byte) (pay.ipHeader[0] >>> 28);
	}

	/**
	 * Set the IP Version Should be 4 for IPv4
	 * 
	 * @param pay
	 *            The Payload
	 * @param ver
	 *            The IP Version to set
	 */
	public static void setVersion(Payload pay, byte ver)
	{
		int i = pay.ipHeader[0] & 0x0FFFFFFF;
		pay.ipHeader[0] = i | ((ver & 0xF) << 28);
	}

	/**
	 * Get the Internet Header Length. This is the size of the IP header in
	 * 32-bit words. (min. 5, max. 15)
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Internet Header Length
	 */
	public static byte getIHL(Payload pay)
	{
		return (byte) ((pay.ipHeader[0] >>> 24) & 0x0F);
	}

	/**
	 * Set the Internet Header Length. This is the size of the IP header in
	 * 32-bit words. (min. 5, max. 15)
	 * 
	 * @param pay
	 *            The Payload
	 * @param len
	 *            The Internet Header Length
	 */
	public static void setIHL(Payload pay, byte len)
	{
		int i = pay.ipHeader[0] & 0xF0FFFFFF;
		pay.ipHeader[0] = i | ((len & 0xF) << 24);
	}

	/**
	 * Get the Type of Service.
	 * <ul>
	 * <li> bits 0-2: precedence
	 * <li> bit 3: 0 = Normal Delay, 1 = Low Delay
	 * <li> bit 4: 0 = Normal Throughput, 1 = High Throughput
	 * <li> bit 5: 0 = Normal Reliability, 1 = High Reliability
	 * <li> bits 6-7: Reserved for future use
	 * </ul>
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Type of Service
	 */
	public static byte getToS(Payload pay)
	{
		return (byte) ((pay.ipHeader[0] >>> 16) & 0xFF);
	}

	/**
	 * Set the Type of Service.
	 * <ul>
	 * <li> bits 0-2: precedence
	 * <li> bit 3: 0 = Normal Delay, 1 = Low Delay
	 * <li> bit 4: 0 = Normal Throughput, 1 = High Throughput
	 * <li> bit 5: 0 = Normal Reliability, 1 = High Reliability
	 * <li> bits 6-7: Reserved for future use
	 * </ul>
	 * 
	 * @param pay
	 *            The Payload
	 * @param tos
	 *            The Type of Service
	 */
	public static void setToS(Payload pay, byte tos)
	{
		int i = pay.ipHeader[0] & 0xFF00FFFF;
		pay.ipHeader[0] = i | ((tos & 0xFF) << 16);
	}

	/**
	 * Get the Total Length of the Packet. Entire size of the datagram,
	 * including header and data, in bytes. (min. 20, max. 65535)
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Total Length
	 */
	public static short getLength(Payload pay)
	{
		return (short) (pay.ipHeader[0] & 0xFFFF);
	}

	/**
	 * Set the Total Length of the Packet. Entire size of the datagram,
	 * including header and data, in bytes. (min. 20, max. 65535)
	 * 
	 * @param pay
	 *            The Payload
	 * @param len
	 *            The Total Length
	 */
	public static void setLength(Payload pay, short len)
	{
		int i = pay.ipHeader[0] & 0xFFFF0000;
		pay.ipHeader[0] = i | (len & 0xFFFF);
	}

	/**
	 * Get the Identification. Used for uniquely identifying individual
	 * fragments of a fragmented IP Packet.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Identification
	 */
	public static short getID(Payload pay)
	{
		return (short) (pay.ipHeader[1] >>> 16);
	}

	/**
	 * Set the Identification. Used for uniquely identifying individual
	 * fragments of a fragmented IP Packet.
	 * 
	 * @param pay
	 *            The Payload
	 * @param id
	 *            The Identification
	 */
	public static void setID(Payload pay, short id)
	{
		int i = pay.ipHeader[1] & 0x0000FFFF;
		pay.ipHeader[1] = i | ((id & 0xFFFF) << 16);
	}

	/**
	 * Check whether the Reserved Flag is set. This Flag must be zero. <b>Note:</b>
	 * because this is a benign IP Stack, setting this flag is not possible.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the Reserved Flag is set
	 */
	public static boolean isReservedSet(Payload pay)
	{
		return (pay.ipHeader[1] & RESERVED_MASK) != 0;
	}

	/**
	 * Clear the Reserved Flag. This Flag must be zero. <b>Note:</b> because
	 * this is a benign IP Stack, setting this flag is not possible.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearReserved(Payload pay)
	{
		pay.ipHeader[1] = pay.ipHeader[1] & ~RESERVED_MASK;
	}

	/**
	 * Check whether the Don't Fragment Flag is set. A Packet with DF set must
	 * not be fragmented. If it would have to be, it is dropped instead.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the Don't Fragment Flag is set
	 */
	public static boolean isDFSet(Payload pay)
	{
		return (pay.ipHeader[1] & DF_MASK) != 0;
	}

	/**
	 * Set the Don't Fragment Flag. A Packet with DF set must not be fragmented.
	 * If it would have to be, it is dropped instead.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setDF(Payload pay)
	{
		pay.ipHeader[1] = pay.ipHeader[1] | DF_MASK;
	}

	/**
	 * Clear the Don't Fragment Flag. A Packet with DF set must not be
	 * fragmented. If it would have to be, it is dropped instead.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearDF(Payload pay)
	{
		pay.ipHeader[1] = pay.ipHeader[1] & ~DF_MASK;
	}

	/**
	 * Check whether the More Fragments Flag is set. In a fragmented IP Packet,
	 * all split Packets except the last one have this flag set.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the More Fragments Flag is set
	 */
	public static boolean isMFSet(Payload pay)
	{
		return (pay.ipHeader[1] & MF_MASK) != 0;
	}

	/**
	 * Set the More Fragments Flag. In a fragmented IP Packet, all split Packets
	 * except the last one have this flag set.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setMF(Payload pay)
	{
		pay.ipHeader[1] = pay.ipHeader[1] | MF_MASK;
	}

	/**
	 * Clear the More Fragments Flag. In a fragmented IP Packet, all split
	 * Packets except the last one have this flag set.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearMF(Payload pay)
	{
		pay.ipHeader[1] = pay.ipHeader[1] & ~MF_MASK;
	}

	/**
	 * Get the Fragment Offset. The place of a particular fragment in the
	 * original IP datagram, measured in units of 8-byte blocks.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Fragment Offset
	 */
	public static short getFragOfs(Payload pay)
	{
		return (short) (pay.ipHeader[1] & 0x1FFF);
	}

	/**
	 * Set the Fragment Offset. The place of a particular fragment in the
	 * original IP datagram, measured in units of 8-byte blocks.
	 * 
	 * @param pay
	 *            The Payload
	 * @param ofs
	 *            The Fragment Offset
	 */
	public static void setFragOfs(Payload pay, short ofs)
	{
		int i = pay.ipHeader[1] & 0xFFFFE000;
		pay.ipHeader[1] = i | (ofs & 0x1FFF);
	}

	/**
	 * Get the Time To Live. Each packet switch (or router) that a datagram
	 * crosses decrements the TTL field by one. When the TTL field hits zero,
	 * the Packet is no longer forwarded by a packet switch and is discarded.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Time To Live
	 */
	public static byte getTTL(Payload pay)
	{
		return (byte) (pay.ipHeader[2] >>> 24);
	}

	/**
	 * Set the Time To Live. Each packet switch (or router) that a datagram
	 * crosses decrements the TTL field by one. When the TTL field hits zero,
	 * the Packet is no longer forwarded by a packet switch and is discarded.
	 * 
	 * @param pay
	 *            The Payload
	 * @param ttl
	 *            The Time To Live
	 */
	public static void setTTL(Payload pay, byte ttl)
	{
		int i = pay.ipHeader[2] & 0x00FFFFFF;
		pay.ipHeader[2] = i | ((ttl & 0xFF) << 24);
	}

	/**
	 * Get the Protocol. The protocol used in the data part of the datagram.
	 * 
	 * @see IP#PROT_ICMP
	 * @see IP#PROT_TCP
	 * @see IP#PROT_UDP
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Protocol
	 */
	public static byte getProtocol(Payload pay)
	{
		return (byte) ((pay.ipHeader[2] >>> 16) & 0xFF);
	}

	/**
	 * Set the Protocol. The protocol used in the data part of the datagram.
	 * 
	 * @see IP#PROT_ICMP
	 * @see IP#PROT_TCP
	 * @see IP#PROT_UDP
	 * 
	 * @param pay
	 *            The Payload
	 * @param proto
	 *            The Protocol
	 */
	public static void setProtocol(Payload pay, byte proto)
	{
		int i = pay.ipHeader[2] & 0xFF00FFFF;
		pay.ipHeader[2] = i | ((proto & 0xFF) << 16);
	}

	/**
	 * Get the Header Checksum. The checksum is calculated over the IP Header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Header Checksum
	 */
	public static short getChecksum(Payload pay)
	{
		return (short) (pay.ipHeader[2] & 0xFFFF);
	}

	/**
	 * Set the Header Checksum to the correct value. The checksum is calculated
	 * over the IP Header (with the checksum field assumed to be zero). The
	 * calculation is done in {@link IPPacket#calculateChecksum}.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setChecksum(Payload pay)
	{
		pay.ipHeader[2] = pay.ipHeader[2] & 0xFFFF0000;
		pay.ipHeader[2] = pay.ipHeader[2] | (calculateChecksum(pay) & 0xFFFF);
	}

	/**
	 * Get the Source address. The IP address the Packet originated from.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Source address as 32-bit value
	 */
	public static int getSrcAddr(Payload pay)
	{
		return pay.ipHeader[3];
	}

	/**
	 * Set the Source address. The IP address the Packet originated from.
	 * 
	 * @param pay
	 *            The Payload
	 * @param addr
	 *            The Source address as 32-bit value
	 */
	public static void setSrcAddr(Payload pay, int addr)
	{
		pay.ipHeader[3] = addr;
	}

	/**
	 * Get the Destination address. The IP address the Packet is headed to.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Destination address as 32-bit value
	 */
	public static int getDestAddr(Payload pay)
	{
		return pay.ipHeader[4];
	}

	/**
	 * Set the Destination address. The IP address the Packet is headed to.
	 * 
	 * @param pay
	 *            The Payload
	 * @param addr
	 *            The Destination address as 32-bit value
	 */
	public static void setDestAddr(Payload pay, int addr)
	{
		pay.ipHeader[4] = addr;
	}

	/**
	 * Calculate the correct Checksum. The checksum is calculated over the IP
	 * Header (with the checksum field assumed to be zero).
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Checksum
	 */
	public static short calculateChecksum(Payload pay)
	{
		int i;
		int ofs = 0;
		int sum = 0;
		byte cnt = getIHL(pay);
		while (cnt != 0)
		{
			i = pay.ipHeader[ofs];
			sum += i & 0xffff;
			sum += i >>> 16;
			++ofs;
			--cnt;
		}

		while ((sum >> 16) != 0)
			sum = (sum & 0xffff) + (sum >> 16);
		sum = (~sum) & 0xffff;

		return (short) sum;
	}

	/**
	 * Check whether the checksum is valid.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the checksum is valid.
	 */
	public static boolean isChecksumValid(Payload pay)
	{
		return calculateChecksum(pay) == 0; // Checksum with checksum field
		// correctly set should equal 0
	}

	/**
	 * Check whether the Packet is valid. Checks the checksum and whether the
	 * Reserved Flag is cleared.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the Packet is valid.
	 */
	public static boolean isValidPacket(Payload pay)
	{
		return (!isReservedSet(pay) && isChecksumValid(pay));
	}
}
