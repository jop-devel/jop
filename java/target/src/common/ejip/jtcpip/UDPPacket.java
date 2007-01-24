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
 * Provides methods to manipulate a Payload instance as if it is a UDP packet
 * 
 * @see ejip2.jtcpip.Payload
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 938 $ $Date: 2007/01/24 19:37:07 $
 */
public class UDPPacket
{
	/**
	 * Get the sending port. The value is read from the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The source port
	 */
	public static short getSourcePort(Payload pay)
	{
		return (short) ((pay.payload[0] >>> 16) & 0xFFFF);
	}

	/**
	 * Set the sending port. The value is set in the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param port
	 *            The source port
	 */
	public static void setSourcePort(Payload pay, short port)
	{
		int i = pay.payload[0] & 0x0000FFFF;
		pay.payload[0] = i | (port << 16);
	}

	/**
	 * Get the receiving port. The value is read from the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The destination port
	 */
	public static short getDestPort(Payload pay)
	{
		return (short) (pay.payload[0] & 0x0000FFFF);
	}

	/**
	 * Set the receiving port. The value is set in the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param port
	 *            The destination port
	 */
	public static void setDestPort(Payload pay, short port)
	{
		int i = pay.payload[0] & 0xFFFF0000;
		pay.payload[0] = i | port;
	}

	/**
	 * Get the length in bytes of the entire datagram. This includes header and
	 * data. (min. 8 - max. 65527) The value is read from the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The UDP Packet lenght
	 */
	public static short getLength(Payload pay)
	{
		return (short) ((pay.payload[1] >>> 16) & 0xFFFF);
	}

	/**
	 * Set the length in bytes of the entire datagram. This includes header and
	 * data. (min. 8 - max. 65527) The value is set in the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param len
	 *            The UDP Packet lenght
	 */
	public static void setLength(Payload pay, short len)
	{
		int i = pay.payload[1] & 0x0000FFFF;
		pay.payload[1] = i | (len << 16);
	}

	/**
	 * Get the Header Checksum. The checksum is calculated over the UDP header
	 * (+IP Pseudoheader) and the data. The value is read from the UDP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Header Checksum
	 */
	public static short getChecksum(Payload pay)
	{
		return (short) ((pay.payload[1] >>> 16) & 0xFFFF);
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
		pay.payload[1] = pay.payload[1] & 0xFFFF0000;
		pay.payload[1] = pay.payload[1] | (calculateChecksum(pay) & 0xFFFF);
	}

	/**
	 * Calculate the correct Checksum. The checksum is calculated over the UDP
	 * header (+IP Pseudoheader) and the data (with the checksum field assumed
	 * to be zero).
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Checksum
	 */
	public static short calculateChecksum(Payload pay)
	{
		// compute over UDP Header and Payload, except the last bytes which
		// don't
		// fall on an int boundary
		int udpPacketLength = pay.length;
		int cnt = udpPacketLength / 4;

		int i = 0;
		int sum = 0;
		int ofs = 0;

		while (cnt != 0)
		{
			try
			{
				i = pay.payload[ofs];
			} catch (Exception e)
			{
				System.out.println("Offset: " + ofs);
				e.printStackTrace();
				System.exit(1);
			}

			sum += i & 0xFFFF;
			sum += i >>> 16;
			++ofs;
			--cnt;
		}

		int modulo = udpPacketLength % 4;
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
		
		// compute over the pseudo header
		sum += (IPPacket.getDestAddr(pay) & 0xFFFF) + (IPPacket.getDestAddr(pay) >>> 16);
		sum += (IPPacket.getSrcAddr(pay) & 0xFFFF) + (IPPacket.getSrcAddr(pay) >>> 16);
		sum += IPPacket.getProtocol(pay) & 0x00FF;
		sum += udpPacketLength & 0xFFFF;

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
		return calculateChecksum(pay) == 0;
	}

	/**
	 * Get the Data offset. This is the size of the UDP header in 32-bit words.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Data offset
	 */
	public static byte getDataOffset(Payload pay)
	{
		return 2;
	}

	/**
	 * Returns the length of the payload in octets. This is the size of the IP
	 * packet minus the size of the IP and the UDP headers.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The number of payload octets
	 */
	public static int getDataLength(Payload pay)
	{
		return pay.length - getDataOffset(pay) * 4;
	}

	/**
	 * Fills the payload buffer with a given byte array.
	 * 
	 * @param pay
	 *            The Payload to write to
	 * @param buffer
	 *            The data to write
	 * @throws JtcpipException
	 *             Data too large
	 */
	public static void setData(Payload pay, byte[] buffer) throws JtcpipException
	{
		if (buffer != null)
			setData(pay, buffer, 0, buffer.length);
		else
			setData(pay, buffer, 0, 0);
	}

	/**
	 * Fills the payload buffer with a given area of a byte array.
	 * 
	 * @param pay
	 *            The Payload to write to
	 * @param buffer
	 *            The data to write
	 * @param firstByte
	 *            The start of the data to write
	 * @param count
	 *            The length of the data to write
	 * @throws JtcpipException
	 *             Data too large
	 */
	public static void setData(Payload pay, byte[] buffer, int firstByte, int count)
			throws JtcpipException
	{
		pay.setData(getDataOffset(pay), buffer, firstByte, count);
	}

}
