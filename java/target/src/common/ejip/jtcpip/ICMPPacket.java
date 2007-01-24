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
 * Interprets a Payload as ICMP packet. See RFC792
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 938 $ $Date: 2007/01/24 19:37:07 $
 */
public class ICMPPacket
{
	/**
	 * Returns the ICMP type
	 * 
	 * @param pay
	 * @return the type of an ICMP packet
	 */
	protected static byte getType(Payload pay)
	{
		return (byte) (pay.payload[0] >> 24);
	}

	/**
	 * Sets the ICMP type
	 * 
	 * @param pay
	 * @param type
	 */
	protected static void setType(Payload pay, byte type)
	{
		pay.payload[0] &= 0x00FFFFFF;
		pay.payload[0] |= type << 24;
	}

	/**
	 * Returns the code of an ICMP packet. The code is used to specify a certain
	 * ICMP type (e.g. Type 3 = 'destination unreachable' and Code 1 = 'host
	 * unreachable' or Code 3 = 'port unreachable'
	 * 
	 * @param pay
	 * @return the code of an ICMP packet
	 */
	protected static byte getCode(Payload pay)
	{
		return (byte) (pay.payload[0] >> 16);
	}

	/**
	 * Sets the ICMP type
	 * 
	 * @param pay
	 * @param code
	 */
	protected static void setCode(Payload pay, int code)
	{
		pay.payload[0] &= 0xFF00FFFF;
		pay.payload[0] |= (code & 0xFF) << 16;
	}

	/**
	 * Get the Header Checksum. The checksum is calculated over the ICMP header
	 * and the data. The value is read from the ICMP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Header Checksum
	 */
	public static short getChecksum(Payload pay)
	{
		return (short) (pay.payload[0] & 0xFFFF);
	}

	/**
	 * Set the Header Checksum to the correct value. The checksum is calculated
	 * over the UDP header (+IP Pseudoheader) and the data (with the checksum
	 * field set to zero). The calculation is done in
	 * {@link UDPPacket#calculateChecksum}. The value is set in the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setChecksum(Payload pay)
	{
		pay.payload[0] = pay.payload[0] & 0xFFFF0000;
		pay.payload[0] = pay.payload[0] | (calculateChecksum(pay) & 0xFFFF);
	}

	/**
	 * Calculate the correct Checksum. The checksum is calculated over the ICMP
	 * header and the data (with the checksum field assumed to be zero).
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Checksum
	 */
	public static short calculateChecksum(Payload pay)
	{
		// compute over ICMP Header and Payload, except the last bytes which
		// don't
		// fall on an int boundary
		int packetLength = pay.length;
		int cnt = packetLength / 4;

		int i = 0;
		int sum = 0;
		int ofs = 0;

		while (cnt != 0)
		{
			i = pay.payload[ofs];

			sum += i & 0xFFFF;
			sum += i >>> 16;
			++ofs;
			--cnt;
		}

		int modulo = packetLength % 4;
		if (modulo != 0)
		{
			i = pay.payload[ofs];
			// compute over the last int of the payload
			switch (modulo)
			{
				case 1:
					sum += (i >>> 16) & 0xFF00;
					break;
	
				case 2:
					sum += (i >>> 16) & 0xFFFF;
					break;
	
				case 3:
					sum += i & 0xFF00;
					sum += (i >>> 16) & 0xFFFF;
			}
		}

		while ((sum >> 16) != 0)
			sum = (sum & 0xffff) + (sum >> 16);
		sum = (~sum) & 0xffff;

		return (short) sum;
	}

	/**
	 * Capsulates the original IP Datagram as part of some ICMP messages
	 * <b>Note:</b> The function trucenates the original IP datagram
	 * 
	 * @param pay
	 */
	protected static void ipToICMPPacket(Payload pay)
	{
		int ofs = IPPacket.getIHL(pay);

		for (int i = 0; i < 2; i++)
		{
			pay.payload[ofs + i + 2] = pay.payload[i]; // Two bytes of ICMP
														// header...
			pay.payload[i] = 0; // Clear the ICMP header fields
		}

		for (int i = 0; i < ofs; i++)
			pay.payload[i + 2] = pay.ipHeader[i];

		pay.length = (ofs + 4) * 4;
	}

	/**
	 * Restores the original IP Datagram as sent as part of some ICMP messages
	 * <b>Note:</b> The function destroys the ICMP Datagram
	 * 
	 * @param pay
	 */
	protected static void icmpToIPPacket(Payload pay)
	{
		byte i;
		for (i = 2; i < pay.length / 4 - 2; i++)
			pay.ipHeader[i - 2] = pay.payload[i];

		for (byte j = i; j < pay.length / 4; j++)
			pay.payload[j - i] = pay.payload[j];

		pay.length -= 8;
	}
}
