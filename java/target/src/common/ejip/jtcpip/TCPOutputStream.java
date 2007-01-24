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

import java.io.IOException;
import java.io.OutputStream;

import ejip.jtcpip.util.Debug;

/**
 * Output stream passed to the user if he opens a TCP connection over CLDC
 * Also the data for retransmission is stored in the stream's buffer. Because
 * of this the buffer can be full also if all written data was read out using
 * {@link #read()}. With {@link #ackData(int)} this retransmission data can 
 * be released.
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 989 $ $Date: 2007/01/24 19:37:07 $
 */
public class TCPOutputStream extends OutputStream
{
	/**
	 * Size of the circular buffer in bytes. BEWARE: DON'T SET THIS BIGGER THAN
	 * MAX_INT!
	 */
	private int BUFSIZE;

	/**
	 * The circular buffer
	 */
	private byte[] buffer;

	/**
	 * Points at the next element to be written
	 */
	private int writePtr;

	/**
	 * points at the next element to be read
	 */
	private int readPtr;

	/**
	 * points at the next that was not already acked (conn.sndUnack si the
	 * sequence number for this byte)
	 */
	private int ackWaitPtr;

	/**
	 * Whether the buffer is blocked (if {@link #writePtr} == {@link #readPtr})
	 */
	private boolean isBufferBlocked;

	/**
	 * Whether the buffer is full (if {@link #writePtr} == {@link #ackWaitPtr})
	 */
	private boolean isBufferFull;

	/**
	 * Whether the stream is closed
	 */
	private boolean closed;

	
	
	/**
	 * Exception that gets thrown when trying to write although the stream has
	 * been closed
	 */
	private static IOException streamClosedException;
			

	/**
	 * Exception that gets thrown when trying to write although the buffer is
	 * full
	 */
	private static IOException bufferFullException;
			

	
	public static void init(){
		streamClosedException = new IOException(
			"TCPOutputStream: Stream closed");
		bufferFullException = new IOException(
		"TCPOutputStream: Buffer is full");
	}
	
	/**
	 * Returns the number of free bytes in the buffer.
	 * 
	 * @return int containing the number of free bytes in the buffer
	 */
	protected int getFreeBufferSpace()
	{
		if (isBufferFull)
			return 0;
		else if (writePtr == ackWaitPtr)
			return BUFSIZE;
		else if (writePtr > ackWaitPtr)
			return BUFSIZE - (writePtr - ackWaitPtr);
		else
			return ackWaitPtr - writePtr;
	}

	/**
	 * Constructor which takes the size of the circular buffer in bytes
	 * 
	 * @param size
	 *            size of the circular buffer
	 */
	protected TCPOutputStream(int size)
	{
		closed = false;
		BUFSIZE = size;
		writePtr = 0;
		readPtr = 0;
		ackWaitPtr = 0;
		isBufferFull = false;
		isBufferBlocked = false;
		buffer = new byte[BUFSIZE];
	}

	/**
	 * Writes the 8 least significant bits of b into the stream. If the streams
	 * buffer is full or the stream is closed a IOException is thrown.
	 * 
	 * @param b
	 *            byte to be written. (is passed as integer from which the 24
	 *            most significant bits are discarded)
	 * @throws IOException
	 *             thrown if there is no more buffer space or the stream is
	 *             closed
	 */
	synchronized public void write(int b) throws IOException
	{
		if (closed)
			throw streamClosedException;
		if (isBufferFull || isBufferBlocked)
			throw bufferFullException;
		buffer[writePtr] = (byte) (b & 0xFF);
		writePtr = ++writePtr % BUFSIZE;
		if (writePtr == readPtr)
			isBufferBlocked = true; // don't write over read Pointer
		if (writePtr == ackWaitPtr)
			isBufferFull = true;
	}

	/**
	 * Reads a byte out from the buffer but returns it as an integer, so just
	 * the last 8 least significant bits should be observed. If the buffer is
	 * empty the function will return -1!
	 * 
	 * @return The read byte in the LSByte of the int, or -1 if empty
	 */
	synchronized protected int read()
	{
		if (isNoMoreDataToRead())
			return -1;
		byte out = buffer[readPtr];
		readPtr = ++readPtr % BUFSIZE;
		isBufferBlocked = false;
		return out & 0xFF;
	}

	/**
	 * Get the maximum number of octets that can be acknowledged.
	 * 
	 * @return the number of bytes that can be ACKed
	 */
	protected int getNumAckableBytes()
	{
		return BUFSIZE - getFreeBufferSpace();
	}

	/**
	 * If some data was acknowledged this method will free num_bytes of memory
	 * of the already sent data. <b>Note:</b> only the data bytes are count!
	 * dont include flags!
	 * 
	 * @param num_bytes
	 *            number of DATA-bytes
	 */
	synchronized protected void ackData(int num_bytes)
	{
		if (num_bytes == 0)
			return;
//		if (Debug.enabled)
//			Debug.println("num ackable bytes:  + getNumAckableBytes()", Debug.DBG_TCP);
	
													// - no assert, report error
		// int oldAckWaitPtr = ackWaitPtr;
		ackWaitPtr = (ackWaitPtr + num_bytes) % BUFSIZE;
		// set readPtr to AckWaitPtr if newer data ACKed than readPtr points at
		// (retransmission)
		/*
		 * if (NumFunctions.relGt(ackWaitPtr, readPtr, oldAckWaitPtr) == 1 ||
		 * 	oldAckWaitPtr == ackWaitPtr)
		 * {
		 * 	if (Debug.enabled)
		 *		Debug.println("Advancing read pointer because of ACKed data! ackableBytes: "
		 *			+ getNumAckableBytes() + ", acked bytes " + num_bytes, Debug.DBG_TCP);
		 * 	//readPtr = ackWaitPtr;
		 * 	//what if this happens during retransmission and we get new data at readpointer?
		 * }
		 */
		isBufferFull = false;
	}

	/**
	 * Checks if there is more data available for read out. The data for
	 * retransmit is not counted <b>Note:</b> if also data for retransmission
	 * should be counted use is buffer empty!
	 * 
	 * @return true if empty, else false
	 */
	protected boolean isNoMoreDataToRead()
	{
		return (writePtr == readPtr && !isBufferBlocked); // because the read 
					 // pointer bumped into the write pointer, not vice versa
	}

	/**
	 * Checks if the buffer of the stream is empty the data for retransmit is
	 * not counted
	 * 
	 * @return true if empty, else false
	 */
	protected boolean isBufferEmpty()
	{
		return (writePtr == ackWaitPtr && !isBufferFull);
	}

	/**
	 * Checks if the stream's buffer is full
	 * 
	 * @return true if full, else false
	 */
	protected boolean isBufferFull()
	{
		return isBufferFull;
	}

	/**
	 * closes the stream. ATENTION: dont't invoke protected methods on a closed
	 * stream!
	 */
	synchronized public void close()
	{
		closed = true;
		//notifyAll();
	}

	/**
	 * re opens the stream ATTENTION: if a user keeps the handle after reopening
	 * the stream is re activated
	 */
	protected void reOpen()
	{
		if (!closed)
			return;
		closed = false;
		writePtr = 0;
		readPtr = 0;
		ackWaitPtr = 0;
		isBufferFull = false;
		isBufferBlocked = false;
	}

	/**
	 * if a retransmission is needed, this method sets the read Pointer so that
	 * in the following read() calls the not acknowledged data will be read out.
	 * ATTENTION: dont forget setting the sequence numbers in the Connection to the
	 * 		right values! (sndNext = sndUnack)
	 */
	synchronized protected void setPtrForRetransmit()
	{
		readPtr = ackWaitPtr;

		if (isBufferFull)
			isBufferBlocked = true;
	}

	/**
	 * Clones the instance.
	 * 
	 * @return the cloned stream
	 */
	synchronized protected TCPOutputStream cloneInstance()
	{
		TCPOutputStream clone = new TCPOutputStream(BUFSIZE);

		for (int i = 0; i < buffer.length; i++)
			clone.buffer[i] = buffer[i];

		clone.writePtr = writePtr;
		clone.readPtr = readPtr;
		clone.ackWaitPtr = ackWaitPtr;
		clone.isBufferFull = isBufferFull;
		clone.isBufferBlocked = isBufferBlocked;
		clone.closed = closed;

		return clone;
	}
}
