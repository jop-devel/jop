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

import joprt.RtThread;
import util.Dbg;
import ejip.jtcpip.util.Debug;
import ejip.jtcpip.util.NumFunctions;

/**
 * Represents the TCP transport layer. Contains methods for connection
 * establishment and disposal (to the upper (application) layer), and TCP Packet
 * sending/receiving (to the lower (network) layer).
 * 
 * @see ejip.jtcpip.IP
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 999 $ $Date: 2007/01/24 19:37:07 $
 */
public class TCP
{
	/** Initial window size */
	static short initialWindow = StackParameters.TCP_INITIAL_WINDOW_SIZE;

	
	
//*************************** FIRST SOME COMMON METHODS **********************
	/**
	 * Get an initial randomly generated sequence number.
	 * 
	 * @return the new initial sequence number
	 */
	synchronized private static int getSeqStart()
	{
		return NumFunctions.rand.nextInt();
	}

	/**
	 * Creates a basic TCP packet by allocating a Payload. All header bits are
	 * set to 0, except the data offset which will be set correctly. If no
	 * packet can be allocated, null is returned.
	 * Option length is assumed to be 0.
	 * 
	 * @return The Payload of the new Packet (or null)
	 */
	protected static Payload createEmptyPayload()
	{
		return createEmptyPayload(0);
	}

	/**
	 * Creates a basic TCP packet by allocating a Payload. All header bits are
	 * set to 0, except the data offset which will be set correctly. If no
	 * packet can be allocated, null is returned.
	 * NOTE: Not used at the moment (only by {@link #createEmptyPayload()}.
	 * 
	 * @param optionLength
	 *            Length of the option data (in units of 4 bytes)
	 * @return The Payload of the new Packet (or null)
	 */
	protected static Payload createEmptyPayload(int optionLength)
	{
		Payload pay = Payload.newPayload();
		if (pay == null)
			return null;
		// set whole tcp header to 0
		for (int i = 0; i < 5 + optionLength; i++)
			pay.payload[i] = 0;
		TCPPacket.setDataOffset(pay, (byte) (5 + optionLength));
		pay.length = (5 + optionLength) * 4;
		return pay;
	}

	/**
	 * Create an empty TCP segment from a given {@link Payload}. All header
	 * bits are set to 0, except the data offset which will be set correctly.
	 * Option length is assumed to be 0.
	 * 
	 * @param pay
	 *            The Payload
	 */
	private static void emptyPayload(Payload pay)
	{
		emptyPayload(pay, 0);
	}

	/**
	 * Create an empty TCP segment from a given {@link Payload}. All header
	 * bits are set to 0, except the data offset which will be set correctly.
	 * NOTE: Not used at the moment (only by {@link #emptyPayload(Payload)}.
	 *  
	 * @param pay
	 *            The Payload
	 * @param optionLength
	 *            Length of the option data (in units of 4 bytes)
	 */
	private static void emptyPayload(Payload pay, int optionLength)
	{
		// set whole tcp header to 0
		for (int i = 0; i < 5 + optionLength; i++)
			pay.payload[i] = 0;
		TCPPacket.setDataOffset(pay, (byte) (5 + optionLength));
		pay.reassembledBitMap.clearBitmap();
		pay.length = (5 + optionLength) * 4;
	}

	
	
	/**
	 * Calculates the segment length of a given Payload, counting also the Syn
	 * or Fin flag
	 * 
	 * @param pay
	 *            the given Payload
	 * @return positive integer with the length in octets
	 */
	protected static int calculateSegmentLength(Payload pay)
	{
		int length = TCPPacket.getDataLength(pay);
		if (TCPPacket.isFINFlagSet(pay))
			length++;
		if (TCPPacket.isSYNFlagSet(pay))
			length++;
		return length;
	}

	/**
	 * Attaches some data from the send buffer to the given <code>Payload</code>.
	 * This is done until the maximum segment size is reached, the<code>Payload</code>
	 * or the sender Window is full or the user's OutputStream is empty.
	 * Also the new Payload length will be set corectly.
	 * 
	 * @param conn
	 *            the <code>TCPConnection</code>
	 * @param pay
	 *            the <code>Payload</code>
	 */
	private static void attachDataToPayload(TCPConnection conn, Payload pay)
	{
		if (conn.oStream.isNoMoreDataToRead())
			return;
	
		int nrToCopy = Math.min(StackParameters.PAYLOAD_MAX_DATA_SIZE - TCPPacket.getDataOffset(pay) * 4, conn.maxSndSegSize);
		int openSendWindow = getRemainingSendWindow(conn);
//		assert openSendWindow >= 0; //Overflow? no, no not possible i hope
		if (openSendWindow ==0)
			return;
		
		nrToCopy = Math.min(nrToCopy, openSendWindow);
	
		int dataCount;
		int readInt = 0;
		for (dataCount = TCPPacket.getDataOffset(pay) * 4; dataCount < nrToCopy + TCPPacket.getDataOffset(pay) * 4; dataCount++) 
			// copy the buffer to the payload until payload is full!
		{
			readInt = conn.oStream.read();
			if (readInt == -1)
				break;
			pay.payload[dataCount / 4] = pay.payload[dataCount / 4] << 8 | (readInt & 0xFF);
		}
	
		// pad the last integer with zeros
		if ((dataCount) % 4 == 1)
			pay.payload[dataCount / 4] = (pay.payload[dataCount / 4] << 24) & 0xFF000000;
		if ((dataCount) % 4 == 2)
			pay.payload[dataCount / 4] = (pay.payload[dataCount / 4] << 16) & 0xFFFF0000;
		if ((dataCount) % 4 == 3)
			pay.payload[dataCount / 4] = (pay.payload[dataCount / 4] << 8) & 0xFFFFFF00;
		pay.length = dataCount;
	}

	/**
	 * Reads out the data stored in a <code>Payload</code> and stores it in the connection's 
	 * <code>TCPinputStream</code>.
	 * 
	 * @param conn
	 *            the <code>TCPConnection</code> containing the stream
	 * @param pay
	 *            the Payload
	 * @param offset
	 *            the offset at which to start reading within the payload
	 * @return how many bytes were read out including SYN and FIN flags!!
	 */
	private static int readOutPayloadData(TCPConnection conn, Payload pay, int offset)
	{
		int bytesWritten = 0;
		if (TCPPacket.getDataLength(pay) > 0)
		{
			byte data;
			for (int i = TCPPacket.getDataOffset(pay) * 4 + offset; i < pay.length; i++)
			{
				data = (byte) ((pay.payload[i / 4] >>> (3 - i % 4) * 8) & 0xFF);
				if (conn.iStream.write(data) < 0)
					break;
				bytesWritten++;
			}
			//FIXME does nothing for SingleThread Version
			conn.iStream.wakeUpRead();
		}
		if (Debug.enabled)
			if (bytesWritten == TCPPacket.getDataLength(pay))
				Debug.println("All data was read out from packet", Debug.DBG_TCP);
	
		//assert bytesWritten <= TCPPacket.getDataLength(pay);
	
		// count fin after all data in the packet was acked
		if (bytesWritten == TCPPacket.getDataLength(pay) && TCPPacket.isFINFlagSet(pay))
		{
			bytesWritten++;
		}
		// syn is counted before data is recieved
		if (TCPPacket.isSYNFlagSet(pay) && offset == 0)
		{
			bytesWritten++;
		}
		if (Debug.enabled)
			Debug.println("Stored bytes", Debug.DBG_TCP);
		return bytesWritten;
	}



//************************* METHODS FOR SEGMENT RECEIVING ***********************

//--------------------------- first the heping methods --------------------------

	/**
	 * Receive a packet from the lower (network) Layer.
	 * This method checks if a TCP segment is valid, and if it can processed by a
	 * TCPConnection in the pool. If so the method processes it depending 
	 * on the state. 
	 * If the segment is not acceptable a reset response will be created.
	 * 
	 * @param pay
	 *            The packet content
	 * @see ejip.jtcpip.IP#handlePayload
	 */
	synchronized public static void receivePayload(Payload pay)
	{
		if (Debug.enabled)
			Debug.println("------------------NEW SEGMENT---------------------", Debug.DBG_TCP);
		if (!TCPPacket.isChecksumValid(pay))
		{
			if (Debug.enabled)
				Debug.println("Checksum not valid", Debug.DBG_TCP);
			Payload.freePayload(pay);
			return;
		}
		TCPConnection conn = TCPConnection.getConnection(TCPPacket.getDestPort(pay), IPPacket
				.getSrcAddr(pay), TCPPacket.getSourcePort(pay));
		if (conn == null)
		{
			if (Debug.enabled)
				Debug.println("No matching connection found", Debug.DBG_TCP);
			if (!TCPPacket.isRSTFlagSet(pay))
				sendBackReset(pay);
			else
				Payload.freePayload(pay);
			return;
		}
		if (Debug.enabled)
			Debug.println("Matching connection found", Debug.DBG_TCP);
		pay.conn = conn;
		conn.timeLastRemoteActivity = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
		switch (conn.getState())
		{
			case TCPConnection.STATE_LISTEN:
				establishConnectionPassive1(conn, pay);
				break;

			case TCPConnection.STATE_SYN_SENT:
				establishConnectionActive(conn, pay);
				break;

			case TCPConnection.STATE_SYN_RCVD:
				establishConnectionPassive2(conn, pay);
				break;

			case TCPConnection.STATE_ESTABLISHED:
				handleEstablishedState(conn, pay);
				break;

			case TCPConnection.STATE_FIN_WAIT_1:
				closeConnectionActive1(conn, pay);
				break;

			case TCPConnection.STATE_FIN_WAIT_2:
				closeConnectionActive2(conn, pay);
				break;

			case TCPConnection.STATE_CLOSE_WAIT:
				closeConnectionPassive1(conn, pay);
				break;

			case TCPConnection.STATE_CLOSING:
				closeConnectionActive3(conn, pay);
				break;

			case TCPConnection.STATE_LAST_ACK:
				closeConnectionPassive2(conn, pay);
				break;

			case TCPConnection.STATE_TIME_WAIT:
				closeConnectionActive4(conn, pay);
				break;

			default:
		}
	}

	/**
	 * Checks if a given sequence number seqNr lies in the reciever window set
	 * in the passed <code>TCPConnection</code> conn. 
	 * Used by isPacketInRecieveSpace()
	 * 
	 * @param conn
	 *            the TCPConnection
	 * @param seqNr
	 * 	          the sequence number to check
	 * @return true if seqNr lies in the window, else false
	 */
	private static boolean isSeqNrInWindow(TCPConnection conn, int seqNr)
	{
		int window = conn.rcvWindow & 0xFFFF; // window must be positive
		return NumFunctions.isBetweenOrEqualSmaller(conn.rcvNext, conn.rcvNext + window, seqNr);
	}

	/**
	 * Checks if a given acknowledge number ackNr lies in the sender window set
	 * in the passed <code>TCPConnection</code> conn.
	 * 
	 * @param conn
	 *            the TCPConnection
	 * @param ackNr
	 *            acknowledge number to check
	 * @return true if ackNr lies in the window, else false
	 */
	private static boolean isAckNrInWindow(TCPConnection conn, int ackNr)
	{
		return NumFunctions.isBetweenOrEqualBigger(conn.sndUnack, conn.sndNext, ackNr);
	}

	/**
	 * Used to check if even a part of a given Segment lies in the reciever
	 * window space.
	 * 
	 * @param conn
	 *            The <code>TCPConnection</code> wich should process this Segment
	 * @param seqNr
	 *            The sequence number of the segment wich should be checked
	 * @param segLength
	 *            The segment length including the FIN or SYN flag
	 * @return true if the segment lies in the window, else false
	 */
	private static boolean isSegmentInReceiverWindow(TCPConnection conn, int seqNr, int segLength)
	{
		//assert segLength >= 0; // should not be longer than short

		if (conn.rcvWindow == 0)
			if (segLength > 0)
				return false;
			else
				return (seqNr == conn.rcvNext);
		else if (segLength == 0)
			return isSeqNrInWindow(conn, seqNr);
		else
			return isSeqNrInWindow(conn, seqNr) || isSeqNrInWindow(conn, seqNr + segLength - 1);
	}

	/**
	 * Checks if a segment pay is acceptable checking the sequence number. 
	 * It returns true if a part of the recieved segment is in the reciever
	 * window, else it returns false. (see RFC 793 pg.69)
	 * 
	 * @param conn
	 *            the connection from which the bounds shall be taken
	 * @param pay
	 *            <code>Payload</code> containing the segment
	 *            
	 * @return true if accptable, else false
	 */
	private static boolean isSeqAcceptable(TCPConnection conn, Payload pay)
	{
		if (!isSegmentInReceiverWindow(conn, TCPPacket.getSeqNr(pay), calculateSegmentLength(pay)))
		{
			if (Debug.enabled)
			{
				Debug.println("ERROR: Segment not in window", Debug.DBG_TCP);
//				Debug.println("Values: rcvNext: " + Debug.intToHexString(conn.rcvNext) + " seqNr: "
//					+ Debug.intToHexString(TCPPacket.getSeqNr(pay)) + " segment length: "
//					+ calculateSegmentLength(pay) + " window: " + (int) (conn.rcvWindow & 0xFFFF),
//					Debug.DBG_TCP);
			}
			// sending a ack so that reciever knows wich numbers we are using
			if (TCPPacket.isRSTFlagSet(pay))
			{
				Payload.freePayload(pay);
				return false;
			}
			emptyPayload(pay);
			sendEmptyPacket(conn, pay); // send back packet with correct seq and
										// ack
			return false;
		}
		return true;
	}

	/**
	 * Returns the size of the remaining send window. That's the window into
	 * which we can send data to the remote host.
	 * 
	 * @param conn
	 *            the <code>TCPConnection</code> from which the window boundaries 
	 *            shall be taken.
	 * @return the remaining send window size
	 */
	private static int getRemainingSendWindow(TCPConnection conn)
	{
		return NumFunctions.calcDiffWithOverflow(conn.sndUnack + ((int) conn.sndWindow & 0xFFFF),
				conn.sndNext);
	}

	/**
	 * Process a valid FIN flag for all states.
	 * 
	 * @param conn the connection
	 * @return whether sendAck has to be set
	 */
	private static boolean processFIN(TCPConnection conn)
	{
		boolean setSendAck = false;
		
		// Signal user "connection closing"
		switch (conn.getState())
		{
			case TCPConnection.STATE_SYN_RCVD:
				// fall through
			case TCPConnection.STATE_ESTABLISHED:
				conn.setState(TCPConnection.STATE_CLOSE_WAIT);
				if (Debug.enabled)
					Debug.println("Entering state: CLOSE WAIT", Debug.DBG_TCP);
				conn.oStream.close();
				conn.iStream.close(); // neverthless user can read out all data that was stored until now
				if (conn.flushAndClose)
				{
					// user called close() -> make sure we enter sendPackets
					setSendAck = true;
				}
				break;
	
			case TCPConnection.STATE_FIN_WAIT_1:
				if (conn.sndNext == conn.sndUnack && conn.oStream.isNoMoreDataToRead()) // FIN ACKed
				{ // never reached
					conn.setState(TCPConnection.STATE_TIME_WAIT);
					if (Debug.enabled)
						Debug.println("Entering state: TIME WAIT", Debug.DBG_TCP);
				}
				else
				{
					conn.setState(TCPConnection.STATE_CLOSING);
					if (Debug.enabled)
						Debug.println("Entering state: CLOSING", Debug.DBG_TCP);
				}
				break;
	
			case TCPConnection.STATE_FIN_WAIT_2:
				conn.setState(TCPConnection.STATE_TIME_WAIT);
				if (Debug.enabled)
					Debug.println("Entering state: TIME WAIT", Debug.DBG_TCP);
				break;
		}
		return setSendAck;
	}

	/**
	 * Processes data for established state starting checking the urg bit (RFC,
	 * pg 73) {@link #isSeqAcceptable} must be checked before!
	 * Here also Segments who lie in the window but are not the next awaited
	 * segment are stored for later. If now the next awaited segment is received
	 * also the previously stored "future segments" are processed.
	 * If the FIN flag is set in the <code>Payload</code> it will be processed
	 * calling <code>processFIN()</code>
	 * TODO: Send collective acks
	 * 
	 * @param conn
	 * 			<code>TCPConnection</code> for which the segment was sent.
	 * @param pay
	 *			<code>Payload</code> containing the data
	 */
	private static void receivePayload(TCPConnection conn, Payload pay)
	{
		if (TCPPacket.isURGFlagSet(pay))
		{
			// TODO: switch to urgent mode!
		}
	
		boolean sendAck = false; // to not get acked acks
		int bytesRead = 0;
		int expSeqPay = conn.rcvNext;
	
		// We assume that the sequence number was checked correctly before with
		// isSeqAcceptable - so the packet lies within the receiver window
	
		if (!Payload.isSeqNrInPayload(expSeqPay, pay))
		{
			// Store segment for later use
			if (Debug.enabled)
				Debug.println("Storing segment for later", Debug.DBG_TCP);
			pay.setStatus(Payload.PAYLOAD_WND_RX, 0);
			pay = null;
		}
		else
		{
			int seqPay;
			int offset;
			int currentBytesRead;
			Payload lastPay = null;
			do
			{
				// lastPay = null the first time around
				Payload.freePayload(lastPay);
				seqPay = TCPPacket.getSeqNr(pay);
				offset = NumFunctions.calcDiffWithOverflow(expSeqPay, seqPay);
			//	assert (offset >= 0); // Happens only when there was an overflow,
									  // so that means offset > MAX_INT.
									  // Can't happen because segment size = short
				
				currentBytesRead = readOutPayloadData(conn, pay, offset);
				bytesRead += currentBytesRead;
				expSeqPay += currentBytesRead;
				
				//Check if everything could be read out.
				//calculateSegmentLength returns the whole Payload length,
				//so we have to substract the offset, and additionally
				//substract 1 if the SYN flag was set and the offset
				//was > 0, because calculateSegmentLength counted it.
				
				int tmp = offset;
				if(offset > 0 && TCPPacket.isSYNFlagSet(pay))
					tmp++;
				
				if (currentBytesRead != (calculateSegmentLength(pay) - tmp))
				{
					if (Debug.enabled)
						Debug.println("Emergency: Out of rcv buffer - setting window to 0", Debug.DBG_TCP);
					conn.rcvWindow = 0;
					sendAck = true;
					lastPay = pay;
					break;
				}
				//here we can be sure that the whole payload was read out, so also the FIN can
				//be processed
	
				//check FIN flag
				if (TCPPacket.isFINFlagSet(pay))
					if (processFIN(conn))
						sendAck = true;
	
				lastPay = pay;
				pay = Payload.findPayload(conn, expSeqPay);
				
			} while (pay != null);
			
			pay = lastPay; // pay is now the last payload of all processed ones
			
			if (bytesRead > 0)
			{
				conn.rcvNext += bytesRead; // just buffered bytes will be
											// acknowledged
				sendAck = true; // TODO: We should receive a whole Window before
								// acking
								// -> don't set ACK every time
			}
		}
	
		if ((conn.oStream.isNoMoreDataToRead() || getRemainingSendWindow(conn) == 0) && !sendAck)
			// dont send a packet if nothing is to ack and no data is available
		{
			if (Debug.enabled)
				Debug.println("Packet must not be acked, no new data to send => dont send anything", Debug.DBG_TCP);
			if (pay != null)
				Payload.freePayload(pay);
			return;
		}
	
		if (pay == null)
			pay = createEmptyPayload();
		else
			emptyPayload(pay);
	
		
		//FIXME not thread safe
		NwLoopThread.conn = conn;
		NwLoopThread.pay = pay;
		//sendPackets(conn, pay);
	}

	/**
	 * Is used in {@link #handleAck(TCPConnection, Payload)} (Hendl-Eck :) to update
	 * the window information sent by the remote host. 
	 * @param conn
	 * 			The connection
	 * @param pay
	 * 			The segment
	 */
	private static void handleSenderWindow(TCPConnection conn, Payload pay)
	{
		// check if segment is valid for window update (see RFC)
		if (conn.sndWndLastUpdateSeq < TCPPacket.getSeqNr(pay) ||
				(conn.sndWndLastUpdateSeq == TCPPacket.getSeqNr(pay) || 
						conn.sndWndLastUpdateAck <= TCPPacket.getAckNr(pay)))
		{
			conn.sndWindow = TCPPacket.getWindow(pay);
			// if now sndNext is bigger than sndUnack + snd.Window set it
			// smaller
			if (NumFunctions.isBetweenOrEqualSmaller(conn.sndUnack, conn.sndNext, conn.sndUnack
					+ (int) (conn.sndWindow & 0xFFFF)))
			{
				if (Debug.enabled)
					Debug.println("remote window was reduced, starting retransmission", Debug.DBG_TCP);
				// simply start retransmission, easier do handle :)
				// TODO: set sndNext = sndUnack + conn.sndWindow and the pointer in iStream to the
				// right values
				conn.sndNext = TCPPacket.getAckNr(pay);
				if(!conn.oStream.isBufferEmpty())
					conn.oStream.setPtrForRetransmit();
			}
		}
	}

	/**
	 * Handles the acknowledge flag and number for the established state. 
	 * It may also be used in FIN WAIT 1, FIN_WAIT_2, CLOSE and CLOSE_WAIT.
	 * Window updates by the remote side are also handled here.
	 * 
	 * @param conn
	 * 			the connection
	 * @param pay
	 * 			the payload
	 * @return true if no error occured (and packet can be further processed)
	 */
	private static boolean handleAck(TCPConnection conn, Payload pay)
	{
		// Hendl-Eck :)
		if (!TCPPacket.isACKFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: ACK not set, dropping", Debug.DBG_TCP);
			Payload.freePayload(pay);
			return false;
		}
		if (conn.sndUnack == TCPPacket.getAckNr(pay))
		{
			if (Debug.enabled)
				Debug.println("acked an other time", Debug.DBG_TCP);
			conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
			handleSenderWindow(conn, pay);
		}
		else if (isAckNrInWindow(conn, TCPPacket.getAckNr(pay))) // is ack awaited?
		{
			// check if send window can be updated (RFC793 pg.72)
			int oldSndUnack = conn.sndUnack;
			conn.sndUnack = TCPPacket.getAckNr(pay);
			conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
			conn.numRetransmissions = 0; // if we were in a retransmission, now it was acked
			int difference = NumFunctions.calcDiffWithOverflow(conn.sndUnack, oldSndUnack);
		//	assert difference > 0; // unack time whould be wrong if equal 0
			if (Debug.enabled)
				Debug.println("Some data was acked",Debug.DBG_TCP);
			
			// check if a syn or a fin were sent before to dont ack this
			// sequence numbers in the oStream
			if (conn.synToSend)
				if (NumFunctions.isBetweenOrEqualBigger(oldSndUnack, conn.sndUnack, conn.synToSendSeq))
				{
					difference--;
					conn.synToSend = false;
				}
			if (conn.finToSend)
				if (NumFunctions.isBetweenOrEqualBigger(oldSndUnack, conn.sndUnack, conn.finToSendSeq))
				{
					difference--;
					conn.finToSend = false;
				}
			conn.oStream.ackData(difference);
			handleSenderWindow(conn, pay);
			return true;
		}
		else if (NumFunctions.isBetween(conn.sndUnack, conn.sndNext, TCPPacket.getAckNr(pay))
				// ACK Nr between unack + next -> error
				|| (NumFunctions.unsignedGt(NumFunctions.calcDiffWithOverflow(conn.sndUnack, TCPPacket
						.getAckNr(pay)), NumFunctions.calcDiffWithOverflow(TCPPacket.getAckNr(pay),
						conn.sndNext)) == 1) // ACK Nr closer to next -> error
		)
		// TCPPacket.getAckNr(pay) > sndUnack
		{
			if (Debug.enabled)
				Debug.println("ERROR: Unexpected Ack", Debug.DBG_TCP);
			emptyPayload(pay);
			sendEmptyPacket(conn, pay);
			return false;
		}
	
	
		return true;
	}

	/**
	 * Searches for the MSS option in the tcp header. If MSS is set correctly
	 * the value will be stored in <code>conn.maxSndSegSize</code>
	 * 
	 * @param conn
	 * 			<code>TCPConnection</code> to which the Segment was adressed
	 * @param pay
	 * 			<code>Payload</code> to check
	 */
	private static void checkAndHandleMSS(TCPConnection conn, Payload pay)
	{
		if (TCPPacket.getDataOffset(pay) <= 5)
			return;
		int mss = -1;
		int i = 20;

		while (i < (TCPPacket.getDataOffset(pay) * 4))
		{
			byte processedByte = (byte) ((pay.payload[i / 4] >>> (3 - i % 4) * 8) & 0xFF);
			switch (processedByte)
			{
				case 0x00: // End of option list
					return;
				case 0x01: // NOP
					i++;
					continue;
				case 0x02: // MSS
					if ((byte) ((pay.payload[(i + 1) / 4] >>> ((3 - (i + 1) % 4) * 8)) & 0xFF) == 0x04)
					{
						mss = 0;
						mss = (pay.payload[(i + 2) / 4] >>> ((3 - (i + 2) % 4) * 8)) & 0xFF;
						mss = (mss << 8) | (pay.payload[(i + 3) / 4] >>> ((3 - (i + 3) % 4) * 8))
								& 0xFF;
						break;
					}
					else
						return; // error in tcp header
				default: // read out the length of the other options and
						 // increment i
					i += (pay.payload[(i + 1) / 4] >>> ((3 - (i + 1) % 4) * 8)) & 0xFF;
					continue;
			}
			break;
		}
		if (mss != -1)
		{
			conn.maxSndSegSize = mss;
			if (Debug.enabled)
				Debug.println("MSS received", Debug.DBG_TCP);
		}
	}
	
	/**
	 * Used in <code>establishConnectionActive1()</code> to check if the acknowledge is
	 * acceptable. It checks if the recieved Ack number is between the initial
	 * sequence number (ISS) and the next sequence number to be sent (SND.NXT).
	 * It takes care of java's always signed interpreted datatypes and overflows.
	 * 
	 * @param conn
	 *            <code>TCPConnection</code> from which the boundaries are taken
	 * @param ackNr
	 *            Acknowledge number to be checked
	 * @return whether the ACK is acceptable
	 */
	private static boolean isFirstAckAcceptable(TCPConnection conn, int ackNr)
	{
		return NumFunctions.isBetweenOrEqualBigger(conn.initialSeqNr, conn.sndNext, ackNr);
	}

	
	
//	------------------------------ the receive methods -----------------------------
	/**
	 * Method which is invoked when a segment arrives in state LISTEN. It looks
	 * for a Packet with the SYN flag set, and if such a Packet arrives a
	 * SYN-ACK is sent back and the state is switched then to SYN_RECIEVED. The
	 * behaviour on other setted flags is implementet according RFC 793
	 * 
	 * @param conn
	 *            <code>TCPConnection</code> for who the packet was sent
	 * @param pay
	 *            <code>Payload</code> which contains the segment
	 */
	private static void establishConnectionPassive1(TCPConnection conn, Payload pay)
	{
		// STATE_LISTEN
		if (TCPPacket.isRSTFlagSet(pay))
		{
			Payload.freePayload(pay);
			return;
		}
		if (TCPPacket.isACKFlagSet(pay))
		{
			sendBackReset(pay);
			return;
		}
		if (!TCPPacket.isSYNFlagSet(pay))
		{
			Payload.freePayload(pay);
			return;
		}

		int seqStart = getSeqStart();

		// TODO: check priority security and so on... not so important (see RFC)
		conn.remoteIP = IPPacket.getSrcAddr(pay);
		conn.remotePort = TCPPacket.getSourcePort(pay);
		conn.rcvNext = TCPPacket.getSeqNr(pay) + 1;
		conn.initialRemoteSeqNr = TCPPacket.getSeqNr(pay);
		conn.sndNext = seqStart;
		conn.sndUnack = seqStart;
		conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
		conn.rcvWindow = initialWindow;
		conn.sndWindow = TCPPacket.getWindow(pay);
		checkAndHandleMSS(conn, pay);

		emptyPayload(pay);
		TCPPacket.setSYNFlag(pay);

		conn.synToSend = true;
		conn.synToSendSeq = conn.sndNext + 1;

		TCPPacket.setMMS(pay);

		sendEmptyPacket(conn, pay);
		if (Debug.enabled)
			Debug.println("Entering state: SYN_RCVD", Debug.DBG_TCP);
		conn.setState(TCPConnection.STATE_SYN_RCVD);
	}

	/**
	 * Method which is invoken when a segment arrives in state SYN_RECIEVED It
	 * looks for the acknowledge of the previous sent SYN-ACK. If such a
	 * acknowledge arrives the 3-way-handshake is finished and the state is
	 * switched to ESTABLISHED. In this method also the blocking listen method
	 * will be woken up if the connection was established or a error occured.
	 * If some data was sent with the segment it is processed and new data
	 * is sent if available.
	 * The behaviour on other setted flags and theb check of Syn and Ack 
	 * numbers is implementet according RFC 793.
	 * 
	 * @param conn
	 *            <code>TCPConnection</code> for who the packet was sent
	 * @param pay
	 *            <code>Payload</code> which contains the segment
	 */
	private static void establishConnectionPassive2(TCPConnection conn, Payload pay)
	{
		// SYN RECIEVED STATE
		if (!isSeqAcceptable(conn, pay))
			return;

		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (conn.getPreviousState() == TCPConnection.STATE_SYN_SENT)
			{
				if (Debug.enabled)
					Debug.println("ERROR: RST set, closing connection", Debug.DBG_TCP);
				conn.abort();
			}
			else if (conn.getPreviousState() == TCPConnection.STATE_LISTEN)
			{
				if (Debug.enabled)
					Debug.println("ERROR: RST set, returning to state LISTEN", Debug.DBG_TCP);
				conn.setState(TCPConnection.STATE_LISTEN);
			}
			Payload.freePayload(pay);
			return;
		}

		
//		if (TCPPacket.isSYNFlagSet(pay))
//		{
//			if (Debug.enabled)
//				Debug.println("ERROR: SYN Flag set! sending back a reset", Debug.DBG_TCP);
//			sendBackReset(pay);
//			conn.abort();
//			if (conn.getPreviousState() == TCPConnection.STATE_LISTEN)
//				// Wake up listening connections
//				synchronized (conn)
//				{
//					conn.notifyAll();
//				}
//			return;
//		}
		if (!TCPPacket.isACKFlagSet(pay))
		{
			Payload.freePayload(pay);
			return;
		}
		if (!isAckNrInWindow(conn, TCPPacket.getAckNr(pay)))
		{
			sendBackReset(pay);
			return;
		}

		handleAck(conn, pay);
		conn.sndWindow = TCPPacket.getWindow(pay);
		conn.sndWndLastUpdateSeq = TCPPacket.getSeqNr(pay);
		conn.sndWndLastUpdateAck = TCPPacket.getAckNr(pay);

		if (Debug.enabled)
			Debug.println("Entering state: ESTABLISHED", Debug.DBG_TCP);
		conn.setState(TCPConnection.STATE_ESTABLISHED);

		//FIXME only one connection for now
// Wake up listening connections
//		synchronized (conn)
//		{
//			conn.notifyAll();
//		}

		receivePayload(conn, pay);
	}

	/**
	 * Is invoked if a segment arrives in SYN_SENT state. Here the tree way
	 * handshake is finished if a valid SYN-ACK for the previous sent SYN
	 * arrives. If so a acknowledge will be sent, and the state is switched to
	 * ESTABLISHED. If the segments contains data it will be stored, and also
	 * userdata will be sent piggybacked with the acknowledge.
	 * The behaviour on other setted flags and the check of Syn and
	 * Ack numbers is implementet according RFC 793
	 * 
	 * @param conn
	 *            <code>TCPConnection</code> for who the packet was sent
	 * @param pay
	 *            <code>Payload</code> which contains the segment
	 */
	private static void establishConnectionActive(TCPConnection conn, Payload pay)
	{
		// SYN SENT STATE
		if (!TCPPacket.isACKFlagSet(pay))
		{
			sendBackReset(pay);
			return;
		}
		else
		// Ack set
		{
			if (!isFirstAckAcceptable(conn, TCPPacket.getAckNr(pay)))
			{
				if (Debug.enabled)
					Debug.println("ERROR: Ack is not acceptable", Debug.DBG_TCP);
				if (TCPPacket.isRSTFlagSet(pay))
				{
					if (Debug.enabled)
						Debug.println("ERROR: RST set, closing connection", Debug.DBG_TCP);
					conn.abort();
					Payload.freePayload(pay);
					return;
				}
				sendBackReset(pay);
				return;
			}

		}
		conn.synToSend = false; // now the syn is acked
		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: Connection reset by other side, closing connection", Debug.DBG_TCP);
			conn.abort();
			Payload.freePayload(pay);
			return;
		}
		// TODO: check security and precedence
		if (!TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN not set, dropping packet", Debug.DBG_TCP);
			Payload.freePayload(pay);
			return;
		}
		checkAndHandleMSS(conn, pay);
		conn.rcvNext = TCPPacket.getSeqNr(pay);
		conn.sndWindow = TCPPacket.getWindow(pay);
		conn.sndWndLastUpdateSeq = TCPPacket.getSeqNr(pay);
		conn.sndWndLastUpdateAck = TCPPacket.getAckNr(pay);
		conn.sndUnack = TCPPacket.getAckNr(pay);
		conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);

		if (Debug.enabled)
			Debug.println("Entering state: ESTABLISHED", Debug.DBG_TCP);
		conn.setState(TCPConnection.STATE_ESTABLISHED);

		receivePayload(conn, pay);
	}

	/**
	 * Method which is invoken when a segment arrives in state ESTABLISHED. After some
	 * initial parameter checks on the received segment <code>receiveAndSendData()</code>
	 * will do the most of the work
	 * 
	 * @param conn
	 *            <code>TCPConnection</code> for who the packet was sent
	 * @param pay
	 *            <code>Payload</code> which contains the segment
	 */
	private static void handleEstablishedState(TCPConnection conn, Payload pay)
	{
		// STATE_ESTABLISHED
		if (!isSeqAcceptable(conn, pay))
			return;

		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: RESET Flag set!, closing connection", Debug.DBG_TCP);
			// inform user
			conn.abort();
			Payload.freePayload(pay);
			return;
		}

		if (TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN Flag set! closing connection", Debug.DBG_TCP);
			// inform user
			conn.abort();
			Payload.freePayload(pay);
			return;
		}

		if (!handleAck(conn, pay))
			return;

		receivePayload(conn, pay);
	}

	/**
	 * Receive a segment in STATE_FIN_WAIT_1. Method is invoked after user sent a
	 * CLOSE call, and the other sends something after our FIN Packet. If data comes
	 * it will be processed. If finally our FIN is acknowledged the the state will
	 * be switched to STATE_FIN_WAIT_2.
	 * In this state we will not send more data, just retransmissions if needed 
	 * 
	 * @param conn
	 *            The connection
	 * @param pay
	 *            The payload
	 */
	private static void closeConnectionActive1(TCPConnection conn, Payload pay)
	{
		// STATE_FIN_WAIT_1
		if (!isSeqAcceptable(conn, pay))
			return;

		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: RESET Flag set!", Debug.DBG_TCP);
			// inform user
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}

		if (TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN Flag set!", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}

		if (!handleAck(conn, pay))
			return;

		if (conn.sndNext == conn.sndUnack && conn.oStream.isNoMoreDataToRead()) // FIN ACKed
		{
			if (Debug.enabled)
				Debug.println("Entering state: FIN WAIT 2", Debug.DBG_TCP);
			conn.setState(TCPConnection.STATE_FIN_WAIT_2);
		}
		receivePayload(conn, pay);
	}

	/**
	 * Handles the active closing of a connection in state FIN_WAIT_2. Does just
	 * the same as in ESTABLISHED. Since user called close the output stream is 
	 * closed, so no new data can't be sent, also retransmissions are not done because
	 * the other side has just acknowledged our FIN which is (hopefully) the last sequence
	 * number of the data sent by us. 
	 * We are just waiting for the remote FIN
	 * 
	 * @param conn
	 *            The Connection
	 * @param pay
	 *            The Payload
	 */
	private static void closeConnectionActive2(TCPConnection conn, Payload pay)
	{
		// STATE_FIN_WAIT_2
		// do the same as in ESTABLISHED
		handleEstablishedState(conn, pay);
	}

	/**
	 * Handles a segment received in state CLOSING.
	 * Closing is reached if the remote 
	 * side sent a FIN before acking our FIN. Here we wait for the ACK of our FIN and
	 * switch to TIME WAIT if we received it.
	 * 
	 * @param conn
	 *            The Connection
	 * @param pay
	 *            The Payload
	 */
	private static void closeConnectionActive3(TCPConnection conn, Payload pay)
	{
		// STATE_CLOSING
		if (!isSeqAcceptable(conn, pay))
			return;

		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("RESET Flag set, closing the connection", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}
		if (TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN Flag set!", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}
		if (!TCPPacket.isACKFlagSet(pay))
		{
			Payload.freePayload(pay);
			return;

		}
		if (conn.sndNext == conn.sndUnack && conn.oStream.isNoMoreDataToRead()) // FIN ACKed
		{
			if (Debug.enabled)
				Debug.println("Entering state: TIME WAIT", Debug.DBG_TCP);
			conn.setState(TCPConnection.STATE_TIME_WAIT);
		}
		receivePayload(conn, pay);
	}

	/**
	 * Handles a segment received in state TIME WAIT.
	 * This just means that we dropp every segment, except the RST or 
	 * SYN flag are set:
	 * if this is the case the connection will be closed immediately.
	 * 
	 * @param conn
	 *            The Connection
	 * @param pay
	 *            The Payload
	 */
	private static void closeConnectionActive4(TCPConnection conn, Payload pay)
	{
		// STATE_TIME_WAIT
		if (!isSeqAcceptable(conn, pay))
			return;

		else if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("RESET Flag set, closing the connection", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
		}
		else if (TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN Flag set!", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
		}
		Payload.freePayload(pay);
		return;
	}

	/**
	 * Method which is invoken when a segment arrives in state CLOSE_WAIT. Here
	 * Data is just acknowledged but not stored! The state CLOSE_WAIT can be
	 * left just with an user input
	 * 
	 * The behaviour on setted flags and the check of Syn and Ack numbers is
	 * implementet according RFC 793
	 * 
	 * @param conn
	 *            TCPConnection for who the packet was sent
	 * @param pay
	 *            Payload which contains the segment
	 */
	private static void closeConnectionPassive1(TCPConnection conn, Payload pay)
	{
		// STATE_CLOSE_WAIT
		if (!isSeqAcceptable(conn, pay))
			return;

		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: RST Flag set! sending back a reset", Debug.DBG_TCP);
			sendBackReset(pay);
			conn.abort();
			return;
		}
		if (TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN Flag set! sending back a reset", Debug.DBG_TCP);
			sendBackReset(pay);
			conn.abort();
			return;
		}

		if (!handleAck(conn, pay))
			return;

		//send FIN if user called close()
		if (conn.flushAndClose) 
		{
			emptyPayload(pay);
			sendEmptyPacket(conn, pay);
			return;
		}
		
		if (Debug.enabled)
			Debug.println("Still waiting... no more data to read: ", Debug.DBG_TCP);
		// urg must be ignored as written in rfc
		// payload must also be ignored
		// fin doesnt mather
		Payload.freePayload(pay);
	}

	/**
	 * It is invoken if a segment arrives in the state LAST_ACK Here we wait for
	 * the last acknowledge for our previous sent FIN-ACK. If this acknowledge
	 * arrives the connection is closed. Data will not be processed any more.
	 * 
	 * The behaviour on setted flags and the check of Syn and Ack numbers is
	 * implementet according RFC 793
	 * 
	 * @param conn
	 *            TCPConnection for who the packet was sent
	 * @param pay
	 *            Payload which contains the segment
	 */
	private static void closeConnectionPassive2(TCPConnection conn, Payload pay)
	{
		// STATE_LAST_ACK
		if (!isSeqAcceptable(conn, pay))
			return;

		if (TCPPacket.isRSTFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("RESET Flag set, closing the connection", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}
		if (TCPPacket.isSYNFlagSet(pay))
		{
			if (Debug.enabled)
				Debug.println("ERROR: SYN Flag set!", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}
		if (!TCPPacket.isACKFlagSet(pay))
		{
			Payload.freePayload(pay);
			return;

		}
		// Ack is the right ack => connection gets closed normally
		if (conn.sndNext == TCPPacket.getAckNr(pay))
		{
			if (Debug.enabled)
				Debug.println("Entering state: CLOSED", Debug.DBG_TCP);
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return;
		}
		Payload.freePayload(pay);
		return;
	}

	
	
//********************************* SENDING METHODES ************************
	/**
	 * Reads the actual state out from the given connection and returns if we
	 * are allowed to send data in this state 
	 * @param conn 
	 * 			The connection
	 * @return true if data sending is allowed, else false
	 */
	private static boolean isDataSendingAllowedInCurrentState(TCPConnection conn)
	{
		switch (conn.getState())
		{
			case TCPConnection.STATE_ESTABLISHED:
			case TCPConnection.STATE_SYN_RCVD:
			case TCPConnection.STATE_CLOSING: //needed for retransmission
			case TCPConnection.STATE_FIN_WAIT_1: //needed for retransmission
				return true;
			default:
				return false;
		}
	}
		
	/**
	 * If the maximum retranmission timeout expired we send a reset and close the
	 * connection. Depending on the state we can close the connection immediately
	 * or we have to wait for the users <code>close()</code> call to provide
	 * that no objects used by the user will be deleted.
	 * 
	 * @param conn
	 * 			The connection
	 * @param pay
	 * 			The payload
	 */
	private static void handleExpiredRetransmissionTimeout(TCPConnection conn, Payload pay)
	{
		if (Debug.enabled)
			Debug.println("No response from the other side, closing connection!", Debug.DBG_TCP);
		TCPPacket.setRSTFlag(pay);
		sendEmptyPacket(conn, pay);
		switch (conn.getState())
		{
			case TCPConnection.STATE_SYN_SENT:
			case TCPConnection.STATE_ESTABLISHED:
			case TCPConnection.STATE_CLOSE_WAIT:
				// user didn't close yet -> abort
				conn.abort();
				break;
				
			case TCPConnection.STATE_FIN_WAIT_1:
			case TCPConnection.STATE_FIN_WAIT_2:
			case TCPConnection.STATE_CLOSING:
			case TCPConnection.STATE_TIME_WAIT:
			case TCPConnection.STATE_LAST_ACK:
				// user closed already -> delete
				TCPConnection.deleteConnection(conn);
				break;
	
			case TCPConnection.STATE_SYN_RCVD:
				conn.abort();
				if (conn.getPreviousState() == TCPConnection.STATE_LISTEN)
					// Wake up listening connections
					// FIXME
					synchronized (conn)
					{
					//	conn.notifyAll();
					}
				break;
				
			default:
				// listen / closed -> can't happen
		//		assert(false);
		}
	}

	/**
	 * Sends one or more TCP Packets by handing them to the lower (network) layer. 
	 * Segment will be sent until the Window of the reciever is closed, the send 
	 * buffer is empty or we run out of payloads.
	 * 
	 * @see #sendPayload(TCPConnection, Payload)
	 * 
	 * @param conn
	 *            The corresponding TCP Connection
	 * @param pay
	 *            The payload of the first Packet
	 */
	synchronized protected static void sendPackets(TCPConnection conn, Payload pay)
	{
		//sendPayload(conn, pay);
		//inlined--------------------------------------------------------
		if (!conn.checkAndprepareRetransmission())
		{
			handleExpiredRetransmissionTimeout(conn, pay);
			return;
		}

		if(isDataSendingAllowedInCurrentState(conn))
			attachDataToPayload(conn, pay);
		
		if (conn.flushAndClose)
		{
			// User called TCPConnection.close()
			switch (conn.getState())
			{
				// handles SYN_RCVD too
				case TCPConnection.STATE_ESTABLISHED:
					if (conn.oStream.isNoMoreDataToRead())
					{
						conn.finToSend = true;
						conn.finToSendSeq = conn.sndNext + TCP.calculateSegmentLength(pay) + 1;
						if (Debug.enabled)
							Debug.println("Entering state: FIN WAIT 1", Debug.DBG_TCP);
						conn.setState(TCPConnection.STATE_FIN_WAIT_1);
						conn.flushAndClose = false;
					}
					break;

				case TCPConnection.STATE_CLOSE_WAIT:
					conn.finToSend = true;
					conn.finToSendSeq = conn.sndNext + TCP.calculateSegmentLength(pay) + 1;
					if (Debug.enabled)
						Debug.println("Entering state: LAST ACK", Debug.DBG_TCP);
					conn.setState(TCPConnection.STATE_LAST_ACK);
					conn.flushAndClose = false;
					break;

				default:
					//do nothing
			}
		}
		// attach Syn and Fin flag to the payload if needed
		if (conn.synToSend)
		{
			if (NumFunctions.isBetweenOrEqualBigger(conn.sndNext, conn.sndNext
					+ TCPPacket.getDataLength(pay) + 1, conn.synToSendSeq))
			{
				if (Debug.enabled)
					if (TCPPacket.isSYNFlagSet(pay))
						Debug.println("SYN set twice!", Debug.DBG_TCP);
				TCPPacket.setSYNFlag(pay);
			}
		}
		if (conn.finToSend)
		{
			if (NumFunctions.isBetweenOrEqualBigger(conn.sndNext, conn.sndNext
					+ TCPPacket.getDataLength(pay) + 1, conn.finToSendSeq))
			{
				if (Debug.enabled)
					if (TCPPacket.isFINFlagSet(pay))
						Debug.println("FIN set twice!", Debug.DBG_TCP);
				TCPPacket.setFINFlag(pay);
			}
		}
		
		
		if (conn.sndNext == conn.sndUnack) // send new data after everything was acknowledged
			conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);

		if (conn.getState() == TCPConnection.STATE_SYN_SENT)
		{  //to provide that the MSS is also sent if we do a retransmission
			TCPPacket.setMMS(pay);
			sendEmptyPacket(conn, pay, false);
		}
		else
			sendEmptyPacket(conn, pay, true);

		
		//inlined-----------------------end----------------------------
		
		
		
		
		
		
		if(!isDataSendingAllowedInCurrentState(conn))
			return;
			
		pay = null;
		if (getRemainingSendWindow(conn) > 0 && !conn.oStream.isNoMoreDataToRead())
			pay = TCP.createEmptyPayload();
		while (pay != null)
		{
			if (Debug.enabled)
				Debug.println("looping in send packets", Debug.DBG_TCP);
			if (!sendPayload(conn, pay))
				break;
			
			if (getRemainingSendWindow(conn) > 0 && !conn.oStream.isNoMoreDataToRead())
				pay = TCP.createEmptyPayload(0);
			else
				pay = null;
		}
	}

	/**
	 * Sends a single TCP Segment, if needet adds some data to the segment and handles
	 * a previous close call from the user by sending a fin. If retransmission
	 * id needed this will be handled in 
	 * <code>TCPConnection.checkAndprepareRetransmission()</code> who is called.
	 * 
	 * @param conn
	 *            The corresponding TCP Connection
	 * @param pay
	 *            The payload of the Packet
	 * 
	 * @return returns true if sucessful, false if an error occurs (e.g.
	 *         retransmission timeout expired)
	 */
	private static boolean sendPayload(TCPConnection conn, Payload pay)
	{
		if (!conn.checkAndprepareRetransmission())
		{
			handleExpiredRetransmissionTimeout(conn, pay);
			return false;
		}

		if(isDataSendingAllowedInCurrentState(conn))
			attachDataToPayload(conn, pay);
		
		if (conn.flushAndClose)
		{
			// User called TCPConnection.close()
			switch (conn.getState())
			{
				// handles SYN_RCVD too
				case TCPConnection.STATE_ESTABLISHED:
					if (conn.oStream.isNoMoreDataToRead())
					{
						conn.finToSend = true;
						conn.finToSendSeq = conn.sndNext + TCP.calculateSegmentLength(pay) + 1;
						if (Debug.enabled)
							Debug.println("Entering state: FIN WAIT 1", Debug.DBG_TCP);
						conn.setState(TCPConnection.STATE_FIN_WAIT_1);
						conn.flushAndClose = false;
					}
					break;

				case TCPConnection.STATE_CLOSE_WAIT:
					conn.finToSend = true;
					conn.finToSendSeq = conn.sndNext + TCP.calculateSegmentLength(pay) + 1;
					if (Debug.enabled)
						Debug.println("Entering state: LAST ACK", Debug.DBG_TCP);
					conn.setState(TCPConnection.STATE_LAST_ACK);
					conn.flushAndClose = false;
					break;

				default:
					//do nothing
			}
		}
		// attach Syn and Fin flag to the payload if needed
		if (conn.synToSend)
		{
			if (NumFunctions.isBetweenOrEqualBigger(conn.sndNext, conn.sndNext
					+ TCPPacket.getDataLength(pay) + 1, conn.synToSendSeq))
			{
				if (Debug.enabled)
					if (TCPPacket.isSYNFlagSet(pay))
						Debug.println("SYN set twice!", Debug.DBG_TCP);
				TCPPacket.setSYNFlag(pay);
			}
		}
		if (conn.finToSend)
		{
			if (NumFunctions.isBetweenOrEqualBigger(conn.sndNext, conn.sndNext
					+ TCPPacket.getDataLength(pay) + 1, conn.finToSendSeq))
			{
				if (Debug.enabled)
					if (TCPPacket.isFINFlagSet(pay))
						Debug.println("FIN set twice!", Debug.DBG_TCP);
				TCPPacket.setFINFlag(pay);
			}
		}
		
		
		if (conn.sndNext == conn.sndUnack) // send new data after everything was acknowledged
			conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);

		if (conn.getState() == TCPConnection.STATE_SYN_SENT)
		{  //to provide that the MSS is also sent if we do a retransmission
			TCPPacket.setMMS(pay);
			sendEmptyPacket(conn, pay, false);
		}
		else
			sendEmptyPacket(conn, pay, true);

		return true;
	}

	/**
	 * Sends a TCP Packet by handing it to the lower (network) layer.
	 * Just calls <code>sendEmptyPacket(conn, pay, true)</code>
	 *
	 * @see #sendEmptyPacket(TCPConnection, Payload, boolean)
	 * 
	 * @param conn
	 *            The corresponding TCP Connection
	 * @param pay
	 *            The payload of the Packet
	 */
	private static void sendEmptyPacket(TCPConnection conn, Payload pay)
	{
		sendEmptyPacket(conn, pay, true);
	}

	/**
	 * Sends a TCP Packet by handing it to the lower (network) layer.
	 * No data will be added by this method, but data which is already
	 * in the <code>Payload</code> will not be touched. 
	 * It also sets the sequence number field to <code>conn.sndNext</code>
	 * and if not otherwise requested also the acknowledge flag will be 
	 * set and the acknowledge number will get the value from
	 * <code>conn.rcvNext</code>.
	 * 
	 * @param conn
	 *            The corresponding TCP Connection
	 * @param pay
	 *            The payload of the Packet
	 * @param sendAck
	 *            If set, the Ack flag will be set and the acknowledge number
	 *            will be set correctly.
	 */
	private static void sendEmptyPacket(TCPConnection conn, Payload pay, boolean sendAck)
	{
		TCPPacket.setWindow(pay, (short) (conn.rcvWindow & 0xFFFF));
		if (Debug.enabled)
			Debug.println("setting Port: Src",Debug.DBG_TCP);
		TCPPacket.setSourcePort(pay, conn.localPort);
		TCPPacket.setDestPort(pay, conn.remotePort);
		TCPPacket.setSeqNr(pay, conn.sndNext);
//		if (Debug.enabled)
//		{
//			Debug.println("sequence number: " + conn.sndNext, Debug.DBG_TCP);
//			Debug.println("data length from header: " + TCPPacket.getDataLength(pay), Debug.DBG_TCP);
//		}
		conn.sndNext += calculateSegmentLength(pay);
		if (sendAck)
			TCPPacket.setACKFlag(pay);
		TCPPacket.setAckNr(pay, conn.rcvNext);
		IP.asyncSendPayload(pay, conn.remoteIP, IP.PROT_TCP);
	}

	/**
	 * Takes the given segment form a reset response and sends it.
	 * 
	 * @param pay
	 * 			The <code>Payload</code> containing the segment.
	 */
	private static void sendBackReset(Payload pay)
	{
		if (Debug.enabled)
			Debug.println("sending reset", Debug.DBG_TCP);
		short srcPort = TCPPacket.getSourcePort(pay);
		short dstPort = TCPPacket.getDestPort(pay);
		int srcAddr = IPPacket.getSrcAddr(pay);
		if (TCPPacket.isACKFlagSet(pay))
		{
			int rcvAck = TCPPacket.getAckNr(pay);
			emptyPayload(pay);
			TCPPacket.setSeqNr(pay, rcvAck);
		}
		else
		{
			int rcvSeq = TCPPacket.getSeqNr(pay);
			int rcvLength = calculateSegmentLength(pay);
			emptyPayload(pay, 0);
			TCPPacket.setAckNr(pay, rcvSeq + rcvLength);
			TCPPacket.setACKFlag(pay);
		}
		TCPPacket.setRSTFlag(pay);
		TCPPacket.setDestPort(pay, srcPort);
		TCPPacket.setSourcePort(pay, dstPort);
		IP.asyncSendPayload(pay, srcAddr, IP.PROT_TCP);
	}

	
	
	
//*************************** METHODES FOR USER CALLS ************************
	/**
	 * Sets the flushAndClose field of a given connection. Is mainly used to
	 * synchronize on TCPs monitor
	 * 
	 * @param conn
	 * 			The connection object
	 */
	synchronized protected static void flushAndClose(TCPConnection conn)
	{
		conn.flushAndClose = true;
	}

	/**
	 * Establish a new TCP Connection with a specific source port. This method
	 * is private for now, so applications can't choose the source port
	 * themselves. Should this change, the source port check needs to be
	 * relocated.
	 * 
	 * @param ip
	 *            Int containing the remote ip
	 * @param port
	 *            Remote port
	 * @param srcPort
	 *            Local Port
	 * @return The TCP Connection Object (or null in case of failure)
	 */
	private static TCPConnection connect(int ip, short port, short srcPort)
	{
		TCPConnection conn = TCPConnection.newConnection(srcPort);
		if (conn == null)
			return null;
	
		int seqStart = getSeqStart();
	
		conn.remoteIP = ip;
		conn.remotePort = port;
		conn.rcvWindow = initialWindow;
		conn.initialSeqNr = seqStart;
		conn.sndNext = seqStart;
	
		Payload pay;
		pay = createEmptyPayload();
		if (pay == null)
		{
			TCPConnection.deleteConnection(conn);
			Payload.freePayload(pay);
			return null;
		}
	
		TCPPacket.setSYNFlag(pay);
		TCPPacket.setMMS(pay);
	
		conn.synToSend = true;
		conn.synToSendSeq = conn.sndNext + 1;
		conn.sndUnack = seqStart;
		conn.sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
		
		sendEmptyPacket(conn, pay, false);
		
		if (Debug.enabled)
			Debug.println("Entering state: Syn Sent", Debug.DBG_TCP);
		conn.setState(TCPConnection.STATE_SYN_SENT);
		return conn;
	}

	/**
	 * Establish a new TCP Connection with the destination IP given as an int.
	 * This method tries to find an unused source port by looking at all
	 * established TCP Connections.
	 * 
	 * @param ip
	 *            Int containing the destination ip
	 * @param port
	 *            Destination port
	 * @return The TCP Connection Object (or null in case of failure)
	 */
	public static TCPConnection connect(int ip, short port)
	{
		if (Debug.enabled)
			Debug.println("Connect to " , Debug.DBG_TCP);
	
		// Get an unused src port
		return connect(ip, port, TCPConnection.newLocalPort());
	}

	/**
	 * Establish a new TCP Connection with the destination IP given as a String.
	 * 
	 * @param ip
	 *            String containing the destination ip in dotted-decimal format
	 * @param port
	 *            Destination port
	 * 
	 * @return The TCP Connection Object (or null in case of failure)
	 * @throws JtcpipException
	 *             IP String is faulty
	 */
	public static TCPConnection connect(String ip, short port) throws JtcpipException
	{
		return connect(IP.ipStringToInt(ip), port);
	}

	/**
	 * Establish a new TCP Connection with the destination ip given as four
	 * bytes.
	 * 
	 * @param a
	 *            First byte of the destination IP
	 * @param b
	 *            Second byte of the destination IP
	 * @param c
	 *            Third byte of the destination IP
	 * @param d
	 *            Fourth byte of the destination IP
	 * @param port
	 *            Destination port
	 * @return The TCP Connection Object (or null in case of failure)
	 */
	public static TCPConnection connect(byte a, byte b, byte c, byte d, short port)
	{
		return connect(((a & 0xFF) << 24) + ((b & 0xFF) << 16) + ((c & 0xFF) << 8) + (d & 0xFF),
				port);
	}

	/**
	 * Opens a listening connection on the specified port and returns the new
	 * connection. 
	 * This method is blocking so it will return when the connection
	 * was established.
	 * 
	 * @param port
	 *            The port to listen on
	 * @return the new connection
	 */
	public static TCPConnection listen(short port, RtThread listenThread)
	{
		TCPConnection conn = TCPConnection.newConnection(port);
		if (conn == null)
			return null;
		conn.setState(TCPConnection.STATE_LISTEN);
		if (Debug.enabled)
			Debug.println("Listening on port " , Debug.DBG_TCP);
		// FIXME for jop
//		synchronized (conn)
//		{
//			while (conn.getState() == TCPConnection.STATE_LISTEN)
//				try
//				{
//				//	conn.wait();
//				} catch (InterruptedException e)
//				{
//				}
//		}
		
		while (conn.getState() != TCPConnection.STATE_ESTABLISHED){
			listenThread.waitForNextPeriod();
		}//Busy wait
			return conn;
		
		//TCPConnection.deleteConnection(conn);
		//return null;
	}
	
} // class TCP
