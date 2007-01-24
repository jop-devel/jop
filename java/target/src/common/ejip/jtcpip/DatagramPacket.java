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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.Datagram;

/**
 * Container for a UDP datagram. The DatagamPacket acts as a container for
 * incoming and outgoing UDP datagrams. It offers functions to read and write
 * data to its buffer and the remote address as truncated connector string (“IP
 * address:port”). The buffer that is used has to be provided at the
 * instantiation. This allows reuse of a buffer on systems with limited memory
 * (or systems without a Garbage Collector).
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 994 $ $Date: 2007/01/24 19:37:07 $
 */
public class DatagramPacket implements Datagram
{
	/**
	 * Remote address in the form IPaddress:port[options] where options is a set
	 * of ;x=y
	 */
	private String remoteAddr = "";

	/** Reference to a byte array */
	private byte[] buffer;

	/** fixed offset within buffer given at constructor call */
	private int initialOffset;

	/** the maximum useable space in the buffer given at constructor call */
	private int maxLength;

	/** pointer for read operations */
	private int readPtr;

	/** pointer for write operations */
	private int writePtr;

	/** actual length in the buffer */
	private int length;

	/**
	 * Constructs a DatagramPacket for receiving packets of length length. The
	 * length argument must be less than or equal to buffer.length
	 * 
	 * @param buffer
	 *            buffer for holding the incoming datagram
	 * @param length
	 *            the number of bytes to read
	 */
	public DatagramPacket(byte[] buffer, int length)
	{
		this(buffer, 0, length);
	}

	/**
	 * Constructs a DatagramPacket for receiving packets of length length. The
	 * length argument must be less than or equal to buffer.length
	 * 
	 * @param buffer
	 *            buffer for holding the incoming datagram
	 * @param offset
	 *            the offset for the buffer
	 * @param length
	 *            the number of bytes to read
	 */
	public DatagramPacket(byte[] buffer, int offset, int length)
	{
		if (buffer == null)
			throw new NullPointerException();
		if (offset < 0 || length < 0 || offset + length > buffer.length)
			throw new IndexOutOfBoundsException();

		this.buffer = buffer;
		initialOffset = offset;
		maxLength = offset + length;
		reset();
	}

	/**
	 * Constructs a datagram packet for sending packets of length length to the
	 * specified port number on the specified host.
	 * 
	 * The length argument must be less than or equal to buffer.length
	 * 
	 * @param buffer
	 *            the packet data
	 * @param length
	 *            the packet length
	 * @param remoteAddr
	 *            the destination address ([[protocol:]//]ip_addr:port[;opt=value...])
	 */
	public DatagramPacket(byte[] buffer, int length, String remoteAddr)
	{
		this(buffer, 0, length, remoteAddr);
	}

	/**
	 * Constructs a datagram packet for sending packets of length length to the
	 * specified port number on the specified host.
	 * 
	 * The length argument must be less than or equal to buffer.length
	 * 
	 * @param buffer
	 *            the packet data
	 * @param offset
	 *            the packet data offset
	 * @param length
	 *            the packet length
	 * @param remoteAddr
	 *            the destination ([//]ip_addr:port[;opt=value...])
	 */
	public DatagramPacket(byte[] buffer, int offset, int length, String remoteAddr)
	{
		this(buffer, offset, length);

		this.remoteAddr = remoteAddr;
		this.length = length; // Set the length of data to send
	}

	/**
	 * @see javax.microedition.io.Datagram#getAddress()
	 */
	public String getAddress()
	{
		return remoteAddr;
	}

	/**
	 * @see javax.microedition.io.Datagram#getData()
	 */
	synchronized public byte[] getData()
	{
		byte[] data = new byte[length];

		for (int i = 0; i < length; i++)
			data[i] = buffer[initialOffset + i];

		return data;
	}

	/**
	 * @see javax.microedition.io.Datagram#getLength()
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.io.Datagram#getOffset()
	 */
	public int getOffset()
	{
		return readPtr;
	}

	/**
	 * @see javax.microedition.io.Datagram#reset()
	 */
	synchronized public void reset()
	{
		length = 0;
		readPtr = initialOffset;
		writePtr = initialOffset;
	}

	/**
	 * @see javax.microedition.io.Datagram#setAddress(java.lang.String)
	 */
	public void setAddress(String addr) throws IOException
	{
		remoteAddr = addr;
	}

	/**
	 * @see javax.microedition.io.Datagram#setAddress(javax.microedition.io.Datagram)
	 */
	public void setAddress(Datagram reference)
	{
		try
		{
			if (reference != null)
				setAddress(reference.getAddress());
		} catch (IOException e)
		{
		}
	}

	/**
	 * @see javax.microedition.io.Datagram#setData(byte[], int, int)
	 */
	synchronized public void setData(byte[] buffer, int offset, int len)
	{
		try
		{
			reset();
			write(buffer, offset, len);
		} catch (IOException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * @see javax.microedition.io.Datagram#setLength(int)
	 */
	public void setLength(int len)
	{
		if (len > (maxLength - initialOffset))
			throw new IllegalArgumentException();

		length = len;
	}

	/**
	 * @see java.io.DataInput#readBoolean()
	 */
	public boolean readBoolean() throws IOException
	{
		return readByte() != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readByte()
	 */
	public byte readByte() throws IOException
	{
		if (readPtr >= (initialOffset + length))
			throw new EOFException();

		return buffer[readPtr++];
	}

	/**
	 * @see java.io.DataInput#readChar()
	 */
	public char readChar() throws IOException
	{
		return (char) readByte();
	}

	/**
	 * @see java.io.DataInput#readFully(byte[])
	 */
	synchronized public void readFully(byte[] b) throws IOException
	{
		readFully(b, 0, length);
	}

	/**
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public void readFully(byte[] b, int off, int len) throws IOException
	{
		if (b == null)
			throw new NullPointerException();

		if (off < 0 || len < 0 || off + len > b.length || len > buffer.length)
			throw new IndexOutOfBoundsException();

		for (int i = 0; i < len; i++)
		{
			if (i == length)
				throw new EOFException();

			b[off + i] = buffer[i];
		}

	}

	/**
	 * @see java.io.DataInput#readInt()
	 */
	public int readInt() throws IOException
	{
		return (readUnsignedShort() << 16) | readUnsignedShort();
	}

	/**
	 * @see java.io.DataInput#readLong()
	 */
	public long readLong() throws IOException
	{
		return ((long) readInt() & 0xFFFFFFFF << 32) | ((long) readInt() & 0xFFFFFFFF);
	}

	/**
	 * @see java.io.DataInput#readShort()
	 */
	public short readShort() throws IOException
	{
		return (short) ((readUnsignedByte() << 8) | readUnsignedByte());
	}

	/**
	 * @see java.io.DataInput#readUTF()
	 */
	public String readUTF() throws IOException
	{
		return DataInputStream.readUTF(this);
	}

	/**
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public int readUnsignedByte() throws IOException
	{
		return readByte() & 0xFF;
	}

	/**
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public int readUnsignedShort() throws IOException
	{
		return readShort() & 0xFFFF;
	}

	/**
	 * @see java.io.DataInput#skipBytes(int)
	 */
	public int skipBytes(int n) throws IOException
	{
		//if n < 0 the readPtr must be at least initialOffset
		//if n > 0 the readPtr must not be greater than maxLength
		int res = Math.max(initialOffset - readPtr, Math.min(n, maxLength - readPtr));
		
		readPtr += res;

		return res;
	}

	/**
	 * @see java.io.DataOutput#write(int)
	 */
	synchronized public void write(int b) throws IOException
	{
		if (writePtr >= maxLength)
			throw new IndexOutOfBoundsException();

		buffer[writePtr++] = (byte) b;

		length++;
	}

	/**
	 * @see java.io.DataOutput#write(byte[])
	 */
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}

	/**
	 * @see java.io.DataOutput#write(byte[], int, int)
	 */
	synchronized public void write(byte[] b, int off, int len) throws IOException
	{
		if (b == null)
			throw new NullPointerException();
		if (off < 0 || len < 0)
			throw new IllegalArgumentException();

		for (int i = off; i < len; i++)
			write(b[i]);
	}

	/**
	 * @see java.io.DataOutput#writeBoolean(boolean)
	 */
	public void writeBoolean(boolean v) throws IOException
	{
		writeByte(v ? 1 : 0);
	}

	/**
	 * @see java.io.DataOutput#writeByte(int)
	 */
	public void writeByte(int v) throws IOException
	{
		write(v);
	}

	/**
	 * @see java.io.DataOutput#writeChar(int)
	 */
	public void writeChar(int v) throws IOException
	{
		writeByte(v);
	}

	/**
	 * @see java.io.DataOutput#writeChars(java.lang.String)
	 */
	public void writeChars(String s) throws IOException
	{
		write(s.getBytes());
	}

	/**
	 * @see java.io.DataOutput#writeInt(int)
	 */
	public void writeInt(int v) throws IOException
	{
		writeShort((v >>> 16) & 0xFFFF);
		writeShort((v >>> 0) & 0xFFFF);
	}

	/**
	 * @see java.io.DataOutput#writeLong(long)
	 */
	public void writeLong(long v) throws IOException
	{
		writeInt((int) (v >>> 32));
		writeInt((int) (v >>> 0));
	}

	/**
	 * @see java.io.DataOutput#writeShort(int)
	 */
	public void writeShort(int v) throws IOException
	{
		writeByte((v >>> 8) & 0xFF);
		writeByte((v >>> 0) & 0xFF);
	}



	/**
	 * @return the buffer
	 */
	protected byte[] getBuffer()
	{
		return buffer;
	}

	public void writeUTF(String value) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
