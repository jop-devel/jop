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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.sun.cldc.io.ConnectionBaseInterface;

import ejip.jtcpip.util.StringFunctions;

/**
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 903 $ $Date: 2007/01/24 19:37:07 $
 */
public class Socket implements StreamConnection, ConnectionBaseInterface
{
	/** Reference to the TCPConnection */
	private TCPConnection conn;

	/** Data Input Stream for the connection */
	private DataInputStream dis;

	/** Data Output Stream for the connection */
	private DataOutputStream dos;

	/** Input Stream for the connection */
	private TCPInputStream is;

	/** Output Stream for the connection */
	private TCPOutputStream os;

	/** static exception to limit memory consumption */
	private static JtcpipException socketClosedException ;
	public static void init(){
		socketClosedException= new JtcpipException("socket closed");

		
	}
	/**
	 * Dummy constructor - see {@link Connector#open(String)}.
	 * 
	 * @param addr
	 *            Format: [//][remote ip addr]:port[;opt1=val1...]
	 * @throws IOException
	 * @see #openPrim(String, int, boolean)
	 */
	public Socket(String addr) throws IOException
	{
		openPrim(addr, 0, false);
	}

	/**
	 * @see javax.microedition.io.InputConnection#openDataInputStream()
	 */
	public DataInputStream openDataInputStream() throws IOException
	{
		if (conn == null)
			throw socketClosedException;

		if (dis == null)
			dis = new DataInputStream(is);

		return dis;
	}

	/**
	 * @see javax.microedition.io.InputConnection#openInputStream()
	 */
	public InputStream openInputStream() throws IOException
	{
		if (conn == null)
			throw socketClosedException;

		return is;
	}

	/**
	 * @see javax.microedition.io.Connection#close()
	 */
	public void close() throws IOException
	{
		if (conn == null)
			return;

		conn.iStream.close();
		conn.close();

		TCPConnection.deleteConnection(conn);

		conn = null;
	}

	/**
	 * @see javax.microedition.io.OutputConnection#openDataOutputStream()
	 */
	public DataOutputStream openDataOutputStream() throws IOException
	{
		if (conn == null)
			throw socketClosedException;

		if (dos == null)
			dos = new DataOutputStream(os);

		return dos;
	}

	/**
	 * @see javax.microedition.io.OutputConnection#openOutputStream()
	 */
	public OutputStream openOutputStream() throws IOException
	{
		if (conn == null)
			throw socketClosedException;

		return os;
	}

	/**
	 * Opens the socket.
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
	 * @param name
	 *            The URL for the connection
	 * @param mode
	 *            The access mode (ignored)
	 * @param timeouts
	 *            A flag to indicate that the caller wants timeout exceptions
	 *            (ignored)
	 * @return A new Connection object
	 */
	public Connection openPrim(String name, int mode, boolean timeouts) throws IOException
	{
		int port = StringFunctions.getPortFromConnectorStr(name);
		int addr = StringFunctions.getAddrFromConnectorStr(name);

		if (addr == 0) // listening connection
;
			//FIXME
			//conn = TCP.listen((short) port);
		else
			conn = TCP.connect(addr, (short) port);

		if (conn == null)
			throw new JtcpipException("No connection left");

		// conn.waitForConnection();

		if (StackParameters.SOCKET_USES_PRIVATE_STREAMS)
		{
			if (is == null)
			{
				is = conn.iStream.cloneInstance();
				os = conn.oStream.cloneInstance();
			}
			else
			{
				is.reOpen();
				os.reOpen();
			}

			conn.setOwnStreams(is, os);
		}
		else
		{
			is = conn.iStream;
			os = conn.oStream;
		}

		return this;
	}

}
