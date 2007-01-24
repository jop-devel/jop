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

import java.io.InputStream;

import util.Dbg;

/**
 * TCPInputStream
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 989 $ $Date: 2007/01/24 19:37:07 $
 */
public class TCPInputStream extends InputStream
{

	/**
	 * Size of the circular buffer in 4 bytes
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
	 * Points at the next element to be read
	 */
	private int readPtr;

	/**
	 * Whether the buffer is full (if {@link #readPtr} == {@link #writePtr})
	 */
	private boolean isBufferFull;

	/**
	 * Whether the stream is closed
	 */
	private boolean closed;

	/**
	 * Constructor which takes the size of the circular buffer in bytes
	 * 
	 * @param size
	 *            size of the circular buffer
	 */
	protected TCPInputStream(int size)
	{
		closed = false;
		BUFSIZE = size;
		writePtr = 0;
		readPtr = 0;
		isBufferFull = false;
		buffer = new byte[BUFSIZE];
	}

	/**
	 * Returns the number of free bytes in the buffer
	 * 
	 * @return int containing the number of free bytes in the buffer
	 */
	protected int getFreeBufferSpace()
	{
		if (isBufferFull)
			return 0;
		else if (writePtr == readPtr)
			return BUFSIZE;
		else if (writePtr > readPtr)
			return BUFSIZE - (writePtr - readPtr);
		else
			return readPtr - writePtr;
	}

	/**
	 * Writes the 8 least significant bits of b into the stream. Returns 0 if
	 * all was ok, -1 if the buffer is full and -2 if the stream is closed.
	 * IMPORTANT: If some bytes are written they chould not be read out before
	 * wakeUpRead() was invoked!
	 * 
	 * @param b
	 *            Byte to be written. (is passed as integer from which the 24
	 *            most significant bits are discarded)
	 * 
	 * @return 0 if all was ok, -1 if the buffer is full and -2 if the stream is
	 *         closed
	 * 
	 */
	synchronized protected int write(int b)
	{
		//System.out.println(b);
		if (closed)
			return -2;
		if (isBufferFull)
			return -1;
		buffer[writePtr] = (byte) (b & 0xFF);
		writePtr = ++writePtr % BUFSIZE;
		if (writePtr == readPtr)
			isBufferFull = true;
		return 0;
	}

	/**
	 * Reads a byte from the buffer. If the stream is closed, -1 is returned
	 * instead.
	 * 
	 * @return The read byte in the LSByte of the int, or -1 if closed
	 */
	synchronized public int read()
	{
		
		
		if (isBufferEmpty() || (closed == true))
			return -1;
//		while (isBufferEmpty()) {
//			//wait(); // block
//			return -1;
//		}

		byte out = buffer[readPtr];
		readPtr = ++readPtr % BUFSIZE;
		isBufferFull = false;
		return out & 0xFF;
	
	}

	/**
	 * Checks if the buffer of the stream is empty
	 * 
	 * @return true if empty, else false
	 */
	protected boolean isBufferEmpty()
	{
		return (writePtr == readPtr && !isBufferFull);
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
	 * Closes the stream.
	 * After closing all data can be read out from the stream, after a that
	 * -1 will be returned.
	 */
	synchronized public void close()
	{
		closed = true;
		//notifyAll();
	}

	/**
	 * Re-opens the stream. 
	 * ATTENTION: if a user keeps the handle after reopening the stream 
	 * is re activated
	 */
	synchronized protected void reOpen()
	{
		if (!closed)
			return;
		closed = false;
		writePtr = 0;
		readPtr = 0;
		isBufferFull = false;
	}

	/**
	 * Clones the instance.
	 * 
	 * @return the cloned stream
	 */
	synchronized protected TCPInputStream cloneInstance()
	{
		TCPInputStream clone = new TCPInputStream(BUFSIZE);

		for (int i = 0; i < buffer.length; i++)
			clone.buffer[i] = buffer[i];

		clone.writePtr = writePtr;
		clone.readPtr = readPtr;
		clone.isBufferFull = isBufferFull;
		clone.closed = closed;

		return clone;
	}

	/**
	 * wakes <code>read()</code> method up if it is sleeping
	 *
	 */
	synchronized protected void wakeUpRead()
	{
		//Dbg.wr("wake up\n");
		//notifyAll();
	}

}
