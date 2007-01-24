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

import ejip.jtcpip.util.Bitmap;
import ejip.jtcpip.util.Debug;
import ejip.jtcpip.util.NumFunctions;

/**
 * Provides a buffer for a certain number of Packets. The number of packets and
 * the size of the individual buffers can be set through constants.
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 950 $ $Date: 2007/01/24 19:37:07 $
 */
public class Payload {
	/** Maximum Data length in words */
	// TODO:
	// private final static int MAXW =
	// NumFunctions.divRoundUp(StackParameters.PAYLOAD_MAX_DATA_SIZE, 4);
	private final static int MAXW = StackParameters.PAYLOAD_MAX_DATA_SIZE;

	/** Maximum IP Header length */
	private final static int MAXIPH = 60;

	/** Maximum IP Header length in words */
	private final static int MAXIPHW = MAXIPH / 4;

	/** Free <code>Payload</code> */
	protected final static byte PAYLOAD_FREE = 0x01;

	/** Used <code>Payload</code> */
	public final static byte PAYLOAD_USED = 0x02;

	/** <code>Payload</code> is a future part of a TCP connections */
	public final static byte PAYLOAD_WND_RX = 0x03;

	/** <code>Payload</code> marked able to send */
	protected final static byte PAYLOAD_SND_RD = 0x04;

	/** <code>Payload</code> is waiting for an ARP response */
	protected final static byte PAYLOAD_ARP_WT = 0x05;

	/**
	 * <code>Payload</code> is split into fragments and not all fragments have
	 * been sent
	 */
	protected final static byte PAYLOAD_FRAGMT = 0x06;

	/**
	 * <code>Payload</code> is split into fragments and not all fragments have
	 * been received
	 */
	protected final static byte PAYLOAD_RESMBL = 0x07;

	/** Exception that gets thrown when passes data is too large */
	private static JtcpipException payloadException;

	/** The <code>Payload</code> pool */
	public static Payload[] pool;

	/**
	 * Holds the number of <code>Payload</code>s that are currently in a
	 * waiting state
	 */
	private static byte numWaitingPayloads = 0;

	/**
	 * Stores the current status of one <code>Payload</code> <b>Note:</b> As
	 * the number of waiting payloads must be correct the setter methode has to
	 * be used!
	 */
	public byte status;

	/**
	 * The <code>Payload</code> data (excluding the first 20 bytes of the IP
	 * Header)
	 */
	public int[] payload;

	/** The IP Header (only the first 20 bytes) */
	public int[] ipHeader;

	/**
	 * Length of <code>Payload</code> data. Length (connection layer, without
	 * IP header) stored in {@link Payload#payload} in bytes.
	 */
	public int length;

	/**
	 * Containing TCP Connection (only needed if the payload is in WND_RX state)
	 */
	protected TCPConnection conn = null;

	/**
	 * If the payload is waiting (for e.g. ARP response, acknowledge) the
	 * timeout is used to free the payload if the response is not coming.
	 */
	private long timeout = 0;

	/**
	 * Holds the offset of the next fragment to be sent if Payload is in the
	 * PAYLOAD_FRAGMT state. This field is also used by the IP.rsmblStore()
	 * function to mark the smallest offset yet received (used if a timeout
	 * occures to send an ICMP message if the first fragment was stored)
	 */
	private int nextFragOffset = 0;

	/** Used to mark all received fragments of an incoming payload */
	protected Bitmap reassembledBitMap;

	public static void init() {
		payloadException = new JtcpipException(
				"The buffer exceeds the payload size");
		pool = new Payload[StackParameters.PAYLOAD_POOL_SIZE];

	}

	/**
	 * Get a new <code>Payload</code>. Tries to find an unused
	 * <code>Payload</code>, allocates and returns it. If none is found, null
	 * is returned.
	 * 
	 * @return The <code>Payload</code> (or null)
	 */
	public static synchronized Payload newPayload() {
		/**
		 * If we can't find a free payload we look for a payload that holds a
		 * future part of a TCP connection (Payload marked with the
		 * PAYLOAD_WND_RX status).
		 */
		int futureIndex = -1;
		int futureSeqNr = 0;

		for (int i = 0; i < StackParameters.PAYLOAD_POOL_SIZE; i++) {
			if (pool[i] == null)
				pool[i] = new Payload();

			if (pool[i].status != PAYLOAD_FREE) {
				if (pool[i].status == PAYLOAD_WND_RX
						&& NumFunctions.unsignedGt(TCPPacket.getSeqNr(pool[i]),
								futureSeqNr) > 0) {
					futureIndex = i;
					futureSeqNr = TCPPacket.getSeqNr(pool[i]);
				}
				continue;
			}

			pool[i].setStatus(PAYLOAD_USED, 0);
			clearPayload(pool[i]);
			return pool[i];
		}

		if (futureIndex > -1) {
			freePayload(pool[futureIndex]);
			pool[futureIndex].setStatus(PAYLOAD_USED, 0);
			clearPayload(pool[futureIndex]);
			return pool[futureIndex];
		}

		if (Debug.enabled)
			Debug.println("Payload: No more Payload", Debug.DBG_OTHER);
		return null;
	}

	/**
	 * Clear all data from a Payload.
	 * 
	 * @param pay
	 *            the Payload
	 */
	private static void clearPayload(Payload pay) {
		if (pay == null)
			return;

		pay.reassembledBitMap.clearBitmap();
		pay.conn = null;
		pay.length = 0;
	}

	/**
	 * Free a <code>Payload</code>. The <code>Payload</code> is marked as
	 * unused, so it can be allocated again.
	 * 
	 * @param pay
	 *            The <code>Payload</code> to be freed
	 */
	public static void freePayload(Payload pay) {
		if (pay == null)
			return;

		pay.setStatus(PAYLOAD_FREE, 0);
		pay.length = 0; // redundant
	}

	/**
	 * Create a <code>Payload</code>. This constructor is private, because
	 * <code>Payload</code>s can only be got through
	 * {@link Payload#newPayload}. Initializes both (payload and ip header)
	 * arrays.
	 */
	private Payload() {
		payload = new int[MAXW];
		ipHeader = new int[MAXIPHW];
		status = PAYLOAD_FREE;

		reassembledBitMap = new Bitmap(NumFunctions.divRoundUp(
				StackParameters.PAYLOAD_MAX_DATA_SIZE, 8));
	}

	/**
	 * Fills the payload buffer starting at offset with a given area of a byte
	 * array
	 * 
	 * @param offset
	 *            within the payload as multiple of four octets
	 * @param buffer
	 * @param firstByte
	 *            to copy within the buffer
	 * @param count
	 * 
	 * @throws JtcpipException
	 *             Data too large
	 */
	public void setData(int offset, byte[] buffer, int firstByte, int count)
			throws JtcpipException {
		length = offset * 4 + count;
		if (length > StackParameters.PAYLOAD_MAX_DATA_SIZE)
			throw payloadException;

		for (int i = 0; i < count; i++) {
			switch (i % 4) {
			case 0:
				payload[offset] = buffer[firstByte + i] << 24;
				break;
			case 1:
				payload[offset] |= (buffer[firstByte + i] & 0xFF) << 16;
				break;
			case 2:
				payload[offset] |= (buffer[firstByte + i] & 0xFF) << 8;
				break;
			case 3:
				payload[offset] |= buffer[firstByte + i] & 0xFF;
				offset++;
			}
		}
	}

	/**
	 * Sets the status field.
	 * 
	 * @param newStatus
	 * @param msTimeout
	 *            timeout in milli seconds
	 */
	protected synchronized void setStatus(byte newStatus, int msTimeout) {
		if (Debug.enabled) {
			Debug.println("Status change", Debug.DBG_OTHER);
			// Byte byt = new Byte(status);
			// Debug.print(byt.toString(),Debug.DBG_OTHER);
			// Debug.print(" -> ",Debug.DBG_OTHER);
			// Byte byt1 = new Byte(newStatus);
			// Debug.print(byt1.toString(), Debug.DBG_OTHER);
			// Debug.println(" T: ", Debug.DBG_OTHER);
			//			
		}
		if (status <= PAYLOAD_WND_RX && newStatus > PAYLOAD_WND_RX)
			numWaitingPayloads++;

		if (status > PAYLOAD_WND_RX && newStatus <= PAYLOAD_WND_RX)
			numWaitingPayloads--;

		status = newStatus;

		if (status == PAYLOAD_FRAGMT) {
			timeout = 0;
			nextFragOffset = msTimeout;
		} else {
			if (msTimeout > 0) {
				timeout = System.currentTimeMillis() + msTimeout;
			} else {
				timeout = 0;
			}
		}
	}

	/**
	 * Returns the current status
	 * 
	 * @return byte
	 */
	protected synchronized byte getStatus() {
		return status;
	}

	/**
	 * Returns true if a timeout occured for this <code>Payload</code>
	 * 
	 * @return boolean
	 */
	protected boolean isTimeout() {
		return timeout > 0 ? System.currentTimeMillis() > timeout : false;
	}

	/**
	 * Returns the offset for the next fragment. Required for fragmented
	 * payloads as the offset is stored
	 * 
	 * @return The offset for the next fragment
	 */
	protected int getOffset() {
		return (status == PAYLOAD_FRAGMT || status == PAYLOAD_RESMBL) ? nextFragOffset
				: 0;
	}

	/**
	 * Sets the offset to the minimum of all received fragments of a Payload. If
	 * the first fragment (Offset == 0) has arrived and the reassambling has
	 * timed out we send an ICMP message
	 * 
	 * @param curOffset
	 *            Offset of the current fragment
	 */
	protected void setOffset(int curOffset) {
		if (curOffset < nextFragOffset)
			nextFragOffset = curOffset;
	}

	/**
	 * Returns the number of <code>Payload</code>s with a status >
	 * PAYLOAD_USED
	 * 
	 * @return the number of payloads that are to be processed
	 */
	public synchronized static short waitingPayloadCount() {
		return numWaitingPayloads;
	}

	/**
	 * Checks if a payload contains a certain seqNr.
	 * 
	 * @param seqNr
	 *            the sequence number to look for
	 * @param pay
	 *            the Payload to check
	 * @return whether the Payload contains the seqNr
	 */
	public static boolean isSeqNrInPayload(int seqNr, Payload pay) {
		if (Debug.enabled)
			Debug.println("seqNextNrinPayload ", Debug.DBG_TCP);// + seqNr + "
		// seq payload"
		// +
		// TCPPacket.getSeqNr(pay),
		// Debug.DBG_TCP);

		return NumFunctions.isBetweenOrEqualSmaller(TCPPacket.getSeqNr(pay),
				TCPPacket.getSeqNr(pay) + TCP.calculateSegmentLength(pay),
				seqNr);
	}

	/**
	 * Look for a payload which contains a certain seqNr.
	 * 
	 * @param conn
	 *            The connection containing the payload
	 * @param seqNr
	 *            The sequence number we look for
	 * @return the <code>Payload</code> or <code>null</code> if none was
	 *         found
	 */
	public synchronized static Payload findPayload(TCPConnection conn, int seqNr) {
		Payload pay;
		for (int i = 0; i < StackParameters.PACKET_POOL_SIZE; i++) {
			pay = pool[i];
			if (pay == null)
				continue;
			if (pay.status != PAYLOAD_WND_RX)
				continue;
			if (pay.conn != conn)
				continue;
			// inline----------------------(isSeqNrInPayload(seqNr, pay)
			// if (isSeqNrInPayload(seqNr, pay))

			boolean isSeqNumberInPl = false;

			int seqLength = TCPPacket.getDataLength(pay);
			if (TCPPacket.isFINFlagSet(pay))
				seqLength++;
			if (TCPPacket.isSYNFlagSet(pay))
				seqLength++;

			int smallerVal = TCPPacket.getSeqNr(pay);
			int testVal = seqNr;
			int biggerVal = TCPPacket.getSeqNr(pay) + seqLength;

			if (smallerVal == testVal)
				isSeqNumberInPl = true;
			if (biggerVal < smallerVal)
				isSeqNumberInPl = ((testVal >= smallerVal) && (testVal <= Integer.MAX_VALUE))
						|| ((testVal >= Integer.MIN_VALUE) && (testVal < biggerVal));
			else
				isSeqNumberInPl = (testVal >= smallerVal)
						&& (testVal < biggerVal);

			// inline----------------------(isSeqNrInPayload(seqNr, pay)---end

			if (isSeqNumberInPl) {
				// So they won't get freed
				pay.status = PAYLOAD_USED;
				return pay;
			}
		}
		return null;
	}
}
