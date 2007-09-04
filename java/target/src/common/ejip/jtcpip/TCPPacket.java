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
 * Encapsulating methods to handle a Payload as a TCP Packet. All methods are
 * static and get a {@link Payload} as a parameter. There are get and set
 * methods for all fields of the TCP header.
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 939 $ $Date: 2007/09/04 00:56:05 $
 */
public class TCPPacket
{
	/** Position of the FIN Flag in the 4th word of the IP header */
	public static final int FIN_MASK = 0x10000;

	/** Position of the SYN Flag in the 4th word of the IP header */
	public static final int SYN_MASK = 0x20000;

	/** Position of the RST Flag in the 4th word of the IP header */
	public static final int RST_MASK = 0x40000;

	/** Position of the PSH Flag in the 4th word of the IP header */
	public static final int PSH_MASK = 0x80000;

	/** Position of the ACK Flag in the 4th word of the IP header */
	public static final int ACK_MASK = 0x100000;

	/** Position of the URG Flag in the 4th word of the IP header */
	public static final int URG_MASK = 0x200000;

	/**
	 * Get the sending port. The value is read from the TCP header.
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
	 * Set the sending port. The value is set in the TCP header.
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
	 * Get the receiving port. The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The destination port
	 */
	public static short getDestPort(Payload pay)
	{
		return (short) (pay.payload[0] & 0xFFFF);
	}

	/**
	 * Set the receiving port. The value is set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param port
	 *            The destination port
	 */
	public static void setDestPort(Payload pay, short port)
	{
		int i = pay.payload[0] & 0xFFFF0000;
		pay.payload[0] = i | (port & 0xFFFF);
	}

	/**
	 * Get the Sequence number. If the SYN flag is present then this is the
	 * initial sequence number and the first data byte is the sequence number
	 * plus 1. Otherwise if the SYN flag is not present then the first data byte
	 * is the sequence number. The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Sequence number
	 */
	public static int getSeqNr(Payload pay)
	{
		return pay.payload[1];
	}

	/**
	 * Set the Sequence number. If the SYN flag is present then this is the
	 * initial sequence number and the first data byte is the sequence number
	 * plus 1. Otherwise if the SYN flag is not present then the first data byte
	 * is the sequence number. The value is set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param seqNr
	 *            The Sequence number
	 */
	public static void setSeqNr(Payload pay, int seqNr)
	{
		pay.payload[1] = seqNr;
	}

	/**
	 * Get the Acknowledgement number. If the ACK flag is set then the value of
	 * this field is the sequence number the sender expects next. The value is
	 * read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Acknowledgement number
	 */
	public static int getAckNr(Payload pay)
	{
		return pay.payload[2];
	}

	/**
	 * Set the Acknowledgement number. If the ACK flag is set then the value of
	 * this field is the sequence number the sender expects next. The value is
	 * set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param ackNr
	 *            The Acknowledgement number
	 */
	public static void setAckNr(Payload pay, int ackNr)
	{
		pay.payload[2] = ackNr;
	}

	/**
	 * Get the Data offset. This is the size of the TCP header in 32-bit words.
	 * (min. 5, max. 15) The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Data offset
	 */
	public static byte getDataOffset(Payload pay)
	{
		return (byte) ((pay.payload[3] >>> 28) & 0x0F);
	}

	/**
	 * Set the Data offset. This is the size of the TCP header in 32-bit words.
	 * (min. 5, max. 15) The value is set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param ofs
	 *            The Data offset
	 */
	public static void setDataOffset(Payload pay, byte ofs)
	{
		int i = pay.payload[3] & 0x0FFFFFFF;
		pay.payload[3] = i | ((ofs & 0xF) << 28);
	}

	// TODO: Reserved field

	/**
	 * Check whether the FIN Flag is set. No more data from sender. The value is
	 * read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the FIN Flag is set
	 */
	public static boolean isFINFlagSet(Payload pay)
	{
		return (pay.payload[3] & FIN_MASK) != 0;
	}

	/**
	 * Set the FIN Flag. No more data from sender. The value is set in the TCP
	 * header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setFINFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] | FIN_MASK;
	}

	/**
	 * Clear the FIN Flag. No more data from sender. The value is set in the TCP
	 * header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearFINFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] & ~FIN_MASK;
	}

	/**
	 * Check whether the SYN Flag is set. Synchronize sequence numbers. The
	 * value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the SYN Flag is set
	 */
	public static boolean isSYNFlagSet(Payload pay)
	{
		return (pay.payload[3] & SYN_MASK) != 0;
	}

	/**
	 * Set the SYN Flag. Synchronize sequence numbers. The value is set in the
	 * TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setSYNFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] | SYN_MASK;
	}

	/**
	 * Clear the SYN Flag. Synchronize sequence numbers. The value is set in the
	 * TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearSYNFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] & ~SYN_MASK;
	}

	/**
	 * Check whether the RST Flag is set. Reset the connection. The value is
	 * read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the RST Flag is set
	 */
	public static boolean isRSTFlagSet(Payload pay)
	{
		return (pay.payload[3] & RST_MASK) != 0;
	}

	/**
	 * Set the RST Flag. Reset the connection. The value is set in the TCP
	 * header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setRSTFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] | RST_MASK;
	}

	/**
	 * Clear the RST Flag. Reset the connection. The value is set in the TCP
	 * header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearRSTFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] & ~RST_MASK;
	}

	/**
	 * Check whether the PSH Flag is set. Push function. The value is read from
	 * the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the PSH Flag is set
	 */
	public static boolean isPSHFlagSet(Payload pay)
	{
		return (pay.payload[3] & PSH_MASK) != 0;
	}

	/**
	 * Set the PSH Flag. Push function. The value is set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setPSHFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] | PSH_MASK;
	}

	/**
	 * Clear the PSH Flag. Push function. The value is set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearPSHFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] & ~PSH_MASK;
	}

	/**
	 * Check whether the ACK Flag is set. Acknowledgement field is significant.
	 * The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the ACK Flag is set
	 */
	public static boolean isACKFlagSet(Payload pay)
	{
		return (pay.payload[3] & ACK_MASK) != 0;
	}

	/**
	 * Set the ACK Flag. Acknowledgement field is significant. The value is set
	 * in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setACKFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] | ACK_MASK;
	}

	/**
	 * Clear the ACK Flag. Acknowledgement field is significant. The value is
	 * set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearACKFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] & ~ACK_MASK;
	}

	/**
	 * Check whether the URG Flag is set. Urgent pointer field is significant.
	 * The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return Whether the Flag is set
	 */
	public static boolean isURGFlagSet(Payload pay)
	{
		return (pay.payload[3] & URG_MASK) != 0;
	}

	/**
	 * Set the URG Flag. Urgent pointer field is significant. The value is set
	 * in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setURGFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] | URG_MASK;
	}

	/**
	 * Clear the URG Flag. Urgent pointer field is significant. The value is set
	 * in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void clearURGFlag(Payload pay)
	{
		pay.payload[3] = pay.payload[3] & ~URG_MASK;
	}

	/**
	 * Get the Window size. The number of bytes the sender is willing to receive
	 * starting from the acknowledgement field value. The value is read from the
	 * TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Window
	 */
	public static short getWindow(Payload pay)
	{
		return (short) (pay.payload[3] & 0xFFFF);
	}

	/**
	 * Set the Window size. The number of bytes the sender is willing to receive
	 * starting from the acknowledgement field value. The value is set in the
	 * TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param wnd
	 *            The Window
	 */
	public static void setWindow(Payload pay, short wnd)
	{
		int i = pay.payload[3] & 0xFFFF0000;
		pay.payload[3] = i | (wnd & 0xFFFF);
	}

	/**
	 * Get the Header Checksum. The checksum is calculated over the TCP header
	 * (+IP Pseudoheader) and the data. The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Header Checksum
	 */
	public static short getChecksum(Payload pay)
	{
		return (short) ((pay.payload[4] >>> 16) & 0xFFFF);
	}

	/**
	 * Set the Header Checksum to the correct value. The checksum is calculated
	 * over the TCP header (+IP Pseudoheader) and the data. (with the checksum
	 * field set to zero). The calculation is done in
	 * {@link TCPPacket#calculateChecksum(Payload)}. The value is set in the
	 * TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setChecksum(Payload pay)
	{
		pay.payload[4] = pay.payload[4] & 0x0000FFFF;
		pay.payload[4] = pay.payload[4] | (calculateChecksum(pay) << 16);
		
	}

	/**
	 * Get the Urgent pointer. An offset from the sequence number indicating the
	 * last urgent data byte. The value is read from the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Urgent pointer
	 */
	public static short getURGPointer(Payload pay)
	{
		return (byte) (pay.payload[4] & 0xFFFF);
	}

	/**
	 * Set the Urgent pointer. An offset from the sequence number indicating the
	 * last urgent data byte. The value is set in the TCP header.
	 * 
	 * @param pay
	 *            The Payload
	 * @param urgP
	 *            The Urgent pointer
	 */
	public static void setURGPointer(Payload pay, short urgP)
	{
		int i = pay.payload[4] & 0xFFFF0000;
		pay.payload[4] = i | urgP & 0xFFFF;
	}

	/**
	 * Calculate the correct Checksum. The checksum is calculated over the TCP
	 * header (+IP Pseudoheader) and the data (with the checksum field assumed
	 * to be zero).
	 * 
	 * @param pay
	 *            The Payload
	 * @return The Checksum
	 */
	public static short calculateChecksum(Payload pay)
	{
		return calculateChecksum(pay, pay.length);
	}

	/**
	 * Calculate the correct Checksum. The checksum is calculated over the TCP
	 * header (+IP Pseudoheader) and the data (with the checksum field assumed
	 * to be zero).
	 * 
	 * @param pay
	 *            The Payload
	 * @param tcpPacketLength
	 *            The lenght of the packet in case of fragmentation
	 * @return The Checksum
	 */
	public static short calculateChecksum(Payload pay, int tcpPacketLength)
	{
		// compute over TCP Header and Payload, except the last bytes which
		// don't
		// fall on an int boundary
		int cnt = tcpPacketLength / 4;
		int i;
		int ofs = 0;
		int sum = 0;
		
		while (cnt != 0)
		{
			i = pay.payload[ofs];
			// if (Debug.enabled)
			// 	Debug.println("Checksum: " + Debug.intToHexString(i) + " (ofs: "
			// 		+ ofs + ")", Debug.DBG_TCP);
			sum += i & 0xFFFF;
			sum += i >>> 16;
			++ofs;
			--cnt;
		}

		int modulo = tcpPacketLength % 4;
		if (modulo != 0)
		{
			i = pay.payload[ofs];
			// compute over the last int of the payload
			switch (modulo)
			{
				case 1:
					sum += (i >>> 16) & 0xFF00;
					// if (Debug.enabled)
					// 	Debug.println("Checksum: " + Debug.intToHexString(sum +=
					// 		(i >>> 16) & 0xFF00) + " (ofs: " + ofs + ")",
					// 		Debug.DBG_TCP);
					break;

				case 2:
					sum += (i >>> 16) & 0xFFFF;
					// if (Debug.enabled)
					// 	Debug.println("Checksum: " + Debug.intToHexString(sum +=
					// 		(i >>> 16) & 0xFFFF) + " (ofs: " + ofs + ")",
					// 		Debug.DBG_TCP);
					break;

				case 3:
					sum += i & 0xFF00;
					sum += (i >>> 16) & 0xFFFF;
					// if (Debug.enabled)
					// 	Debug.println("Checksum: " + Debug.intToHexString(sum +=
					// 		i & 0xFF00) + " " + Debug.intToHexString(sum += (i >>>
					// 		16) & 0xFFFF) + " (ofs: " + ofs + ")", Debug.DBG_TCP);
			}
		}
		// compute over the pseudo header
		sum += (IPPacket.getDestAddr(pay) & 0xFFFF) + (IPPacket.getDestAddr(pay) >>> 16);
		sum += (IPPacket.getSrcAddr(pay) & 0xFFFF) + (IPPacket.getSrcAddr(pay) >>> 16);
		sum += IPPacket.getProtocol(pay) & 0x00FF;
		sum += tcpPacketLength & 0xFFFF;
		while ((sum >> 16) != 0)
			sum = (sum & 0xffff) + (sum >> 16);
		sum = (~sum) & 0xffff;

		// if (Debug.enabled)
		// 	Debug.println("Checksum: " + Debug.intToHexString(sum),
		// 		Debug.DBG_TCP);
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
	 * Appends the option header to set the Maximum Segmentation Size (MSS)
	 * according to the maximum size of payload buffer.
	 * <p>
	 * <b>Note:</b> This option has to be transmitted only during the initial
	 * connection request! The TCP payload data already written to the Payload
	 * pay will be overwritten! => do it before storing data
	 * 
	 * @param pay
	 *            The Payload
	 */
	public static void setMMS(Payload pay)
	{
		byte dofs = getDataOffset(pay);

		// add the option header (first byte = 2: MSS option, second byte = 4:
		// number
		// of bytes for this option, the other two bytes for the actual size)
		pay.payload[dofs] = 0x02040000 | (StackParameters.TCP_RCV_MAX_SEGMENT_SIZE & 0xFFFF);

		// and change the data offset
		setDataOffset(pay, (byte) (dofs + 1));
		pay.length += 4;
	}

	/**
	 * Returns the length of the payload in octets. This is the size of the IP
	 * packet minus the size of the IP and the TCP headers.
	 * 
	 * @param pay
	 *            The Payload
	 * @return The number of payload octets
	 */
	public static int getDataLength(Payload pay)
	{
		return pay.length - ((pay.payload[3] >>> 28) & 0x0F) * 4;
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
		// Could throw an IndexOutOfBounds exception here
		setData(pay, buffer, 0, (short) buffer.length);
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
	public static void setData(Payload pay, byte[] buffer, int firstByte, short count)
			throws JtcpipException
	{
		pay.setData(getDataOffset(pay), buffer, firstByte, count);
	}
}
