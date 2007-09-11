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

package ejip.jtcpip.unused;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

import com.sun.cldc.io.ConnectionBaseInterface;

import ejip.jtcpip.DatagramPacket;
import ejip.jtcpip.JtcpipException;
import ejip.jtcpip.StackParameters;
import ejip.jtcpip.UDPConnection;
import ejip.jtcpip.util.StringFunctions;

/**
 * Application interface to the UDP layer. The DatagramSocket holds a reference
 * to a corresponding UDPConnection, provides some functions to create new
 * Datagrams and the functions to send and receive them.
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 994 $ $Date: 2007/09/11 00:16:51 $
 */
public class DatagramSocket implements DatagramConnection, ConnectionBaseInterface
{
	/** Reference to the UDPConnection */
	private UDPConnection conn;

	/** Default remote host */
	private String defaultRemoteHost;

	/**
	 * Dummy constructor - see {@link Connector#open(String)}.
	 * 
	 * @param addr
	 *            Format: [[protocol:]//][ip address]:port[;opt1=val1...]
	 * @throws IOException
	 * @see #openPrim(String, int, boolean)
	 */
	public DatagramSocket(String addr) throws IOException
	{
		openPrim(addr, 0, false);
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#getMaximumLength()
	 */
	public int getMaximumLength() throws IOException
	{
		return StackParameters.UDP_DATA_SIZE;
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#getNominalLength()
	 */
	public int getNominalLength() throws IOException
	{
		return getMaximumLength();
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#newDatagram(int)
	 */
	public Datagram newDatagram(int size) throws IOException
	{
		return newDatagram(size, defaultRemoteHost);
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#newDatagram(int,
	 *      java.lang.String)
	 */
	public Datagram newDatagram(int size, String addr) throws IOException
	{
		if (size < 0 || size > getMaximumLength())
			throw new IllegalArgumentException();

		byte[] b = new byte[size];

		return newDatagram(b, size, addr);
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#newDatagram(byte[], int)
	 */
	public Datagram newDatagram(byte[] buf, int size) throws IOException
	{
		return new DatagramPacket(buf, size, defaultRemoteHost);
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#newDatagram(byte[], int,
	 *      java.lang.String)
	 */
	public Datagram newDatagram(byte[] buf, int size, String addr) throws IOException
	{
		if (size < 0 || size > getMaximumLength())
			throw new IllegalArgumentException();

		Datagram dg = new DatagramPacket(buf, size);
		if (addr != null)
			dg.setAddress(addr);

		return dg;
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#receive(javax.microedition.io.Datagram)
	 */
	public void receive(Datagram dgram) throws IOException
	{
		if (conn != null)
			conn.receive(dgram);
	}

	/**
	 * @see javax.microedition.io.DatagramConnection#send(javax.microedition.io.Datagram)
	 */
	public void send(Datagram dgram) throws IOException
	{
		if (conn != null)
			conn.send(dgram);
	}

	/**
	 * @see javax.microedition.io.Connection#close()
	 */
	public void close() throws IOException
	{
		if (conn != null)
		{
			conn.close();
			conn = null;
		}
	}

	/**
	 * Opens the datagram socket.
	 * 
	 * <pre>
	 *  URL format:
	 *  [//][host]:port[;option1=value1[;option2=value2[...]]]
	 * </pre>
	 * 
	 * If the host is given a connection will be established to the given
	 * host:port the local port will be random. Else a listening connection will
	 * be opened on the given port
	 * 
	 * <p>
	 * Examples: (Note that 'datagram:' will be removed by
	 * {@link Connector#open(String)})
	 * <p>
	 * A datagram connection for accepting datagrams<br>
	 * datagram://:1234
	 * <p>
	 * A datagram connection for sending to a server:<br>
	 * datagram://123.456.789.12:1234
	 * <p>
	 * 
	 * <b>Note: </b> Even in "client mode" is the actual destination always set
	 * in the DatagramPacket! The given remote address is used as default
	 * destination.
	 * 
	 * <p>
	 * Note that the port number in "server mode" (unspecified host name) is
	 * that of the receiving port. The port number in "client mode" (host name
	 * specified) is that of the target port. The reply-to port in both cases is
	 * never unspecified. In "server mode", the same port number is used for
	 * both receiving and sending. In "client mode", the reply-to port is always
	 * dynamically allocated.
	 * 
	 * @param name
	 *            The URL for the connection
	 * @param mode
	 *            The access mode
	 * @param timeouts
	 *            A flag to indicate that the caller wants timeout exceptions
	 * @return A new Connection object
	 */
	public Connection openPrim(String name, int mode, boolean timeouts) throws IOException
	{
		int addr = StringFunctions.getAddrFromConnectorStr(name);
		int port;

		if (addr == 0)
			port = StringFunctions.getPortFromConnectorStr(name);
		else
		{
			port = UDPConnection.newLocalPort();
			defaultRemoteHost = name;
		}

		conn = UDPConnection.newConnection(port);

		if (conn == null)
			throw new JtcpipException("No connection available!");

		return this;
	}

}
