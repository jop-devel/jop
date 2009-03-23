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

import javax.microedition.io.Datagram;

import ejip.jtcpip.util.Debug;
import ejip.jtcpip.util.NumFunctions;
import ejip.jtcpip.util.StringFunctions;

/**
 * Handles a UDP connection (port and IO-stream combination) If the remoteIP ==
 * 0 the first source IP address and port will be set as remoteIP and remotePort
 * and we can't sent befor receive.
 * 
 * If remoteIP == 255.255.255.255 we accept UDP packets from everyone and can
 * send broadcasts
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 984 $ $Date: 2007/09/11 00:16:50 $
 */
public class UDPConnection
{
	/** Free <code>UDPConnection</code> */
	protected final static byte UDP_CONN_FREE = 0x01;

	/** Used <code>UDPConnection</code> */
	protected final static byte UDP_CONN_USED = 0x02;

	/** The <code>UDPConnection</code> pool */
	protected static UDPConnection[] pool ;
	
	/** Stores the status of a certain connection */
	protected byte status;

	/** port of the local host */
	private int localPort;

	/**
	 * If incomingDatagram != null there is a blocked receive() call. Fill the
	 * DatagramPacket not the stream!
	 */
	private Datagram incomingDatagram = null;

	
	
	public static void init(){
		
		pool = new UDPConnection[StackParameters.UDP_CONNECTION_POOL_SIZE];

	}
	/**
	 * Look for a UDPConnection with matching source IP and port and destination
	 * port. Returns null if no matching connection was found.
	 * 
	 * @param pay
	 *            The payload where we seek an connection for
	 * @return <code>UDPConnection</code> object if available or null if no
	 *         connection was found
	 */
	protected static UDPConnection getConnection(Payload pay)
	{
		for (int i = 0; i < StackParameters.UDP_CONNECTION_POOL_SIZE; i++)
			if ((pool[i] != null) && pool[i].isIncomingPayPartOfConn(pay))
				return pool[i];

		return null;
	}

	/**
	 * Look for a UDPConnection with matching source IP and port and destination
	 * port. Returns null if no matching connection was found.
	 * 
	 * @param destPort
	 *            The destination port to look for
	 * @param srcIP
	 *            The source IP to look for
	 * @param srcPort
	 *            The source port to look for
	 * @return <code>UDPConnection</code> object if available or null if no
	 *         connection was found
	 */
	protected static UDPConnection getConnection(short destPort, int srcIP, short srcPort)
	{

		for (int i = 0; i < StackParameters.UDP_CONNECTION_POOL_SIZE; i++)
			if (pool[i] != null && pool[i].localPort == destPort)
			{
				/*
				 * if (pool[i].remoteIP == 0) { pool[i].remoteIP = srcIP;
				 * pool[i].remotePort = srcPort; }
				 * 
				 * 
				 * //if remoteIP == 255.255.255.255 accept every payload if
				 * (pool[i].remoteIP == 0xFFFFFFFF || (pool[i].remoteIP == srcIP &&
				 * pool[i].remotePort == srcPort))
				 */return pool[i];
			}

		return null;
	}

	/**
	 * Create a new <code>UDPConnection</code>. Tries to create a new
	 * UDPConnection. If the connection pool is full, or there is already a
	 * listening connection at the specified port, null is returned.
	 * 
	 * @param localPort
	 * @return The new <code>UDPConnection</code> or null if unsuccessful
	 */
	public static UDPConnection newConnection(int localPort)
	{
		if (localPort < 0 || localPort > 0xFFFF)
			return null;

		for (byte i = 0; i < StackParameters.UDP_CONNECTION_POOL_SIZE; i++)
		{
			if (pool[i] == null)
				pool[i] = new UDPConnection(localPort);

			if (pool[i].status == UDP_CONN_USED)
			{
				if (pool[i].localPort == localPort)
					return null;

				continue;
			}

			// Found the first free connection

			// Now test if the rest of the connections do not use the same port
			for (int j = i + 1; j < StackParameters.UDP_CONNECTION_POOL_SIZE; j++)
				if ((pool[j] != null) && (pool[j].status == UDP_CONN_USED)
						&& (pool[j].localPort == localPort))
					return null;

			// The connection at position i is free and no one else is
			// using the same port
			pool[i].status = UDP_CONN_USED;
			cleanUpConnection(pool[i]);
			pool[i].localPort = localPort;

			return pool[i];
		}

		return null;
	}

	/**
	 * Create a new <code>UDPConnection</code>. Tries to create a new
	 * UDPConnection. If the connection pool is full, or there is already a
	 * listening connection at the specified port, null is returned.
	 * 
	 * @param localPort
	 * @param remoteIP
	 * @param remotePort
	 * 
	 * @return The new <code>UDPConnection</code> or null if unsuccessful
	 */
	public static UDPConnection newConnection(int localPort, int remoteIP, int remotePort)
	{
		if (localPort < 0 || localPort > 0xFFFF || remotePort < 0 || remotePort > 0xFFFF)
			return null;

		UDPConnection conn = newConnection(localPort);
		/*
		 * if (conn != null) { conn.remoteIP = remoteIP; conn.remotePort =
		 * remotePort; }
		 */
		return conn;
	}

	/**
	 * Prepares a connection to remove possible artefacts of an old connection.
	 * 
	 * @param conn
	 */
	private static void cleanUpConnection(UDPConnection conn)
	{
		/*
		 * conn.remoteIP = 0; conn.remotePort = 0;
		 */conn.localPort = 0;
	}

	/**
	 * Create a <code>UDPConnection</code>. This constructor is private,
	 * because <code>UDPConnection</code>s can only be got through
	 * {@link #newConnection(int)}. Initializes some internal variables.
	 * 
	 * @param port
	 */
	private UDPConnection(int port)
	{
		status = UDP_CONN_FREE;

		// iStream = new
		// UDPInputStream(StackParameters.UDP_CONNECTION_RCV_BUFFER_SIZE);
		// oStream = new
		// UDPOutputStream(StackParameters.UDP_CONNECTION_RCV_BUFFER_SIZE);

		cleanUpConnection(this);
		localPort = port;
	}

	/**
	 * Returns true if a given Payload is part of this connection
	 * 
	 * @param pay
	 * @return boolean
	 */
	protected boolean isIncomingPayPartOfConn(Payload pay)
	{
		return UDPPacket.getDestPort(pay) == localPort;
	}

	/**
	 * Actively closes the UDP Connection.
	 */
	public void close()
	{
		status = UDP_CONN_FREE;
		cleanUpConnection(this);
	}

	/**
	 * Sends one datagram
	 * 
	 * @param p
	 * @throws IOException
	 */
	public void send(Datagram p) throws IOException
	{
		Payload pay = null;

		int dgRemPort = StringFunctions.getPortFromConnectorStr(p.getAddress());
		int dgRemAddr = dgRemPort > -1 ? StringFunctions.getAddrFromConnectorStr(p.getAddress()) : 0;

		while (true)
		{
			pay = UDP.preparePayload((short) dgRemPort, (short) localPort);

			if (pay != null)
				break;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		UDPPacket.setData(pay, ((DatagramPacket) p).getBuffer(), 0, p.getLength());
		UDPPacket.setLength(pay, (short) pay.length); // Sets the length in
														// the UDP header

		if (Debug.enabled)
			Debug.println("Trying to send a UDP connection portion", Debug.DBG_UDP);

		IP.asyncSendPayload(pay, dgRemAddr, IP.PROT_UDP);
	}

	/**
	 * Adds the content of the payload to the UDPInputStream
	 * 
	 * @param pay
	 */
	synchronized protected void receivePayload(Payload pay)
	{
		if (incomingDatagram != null)
		{
			try
			{
				int i = 0;

				for (i = UDPPacket.getDataOffset(pay); i < pay.length / 4; i++)
					incomingDatagram.writeInt(pay.payload[i]);

				switch (pay.length % 4)
				{
					case 1:
						incomingDatagram.write(pay.payload[i] >> 24);
						break;
					case 2:
						incomingDatagram.writeShort(pay.payload[i] >> 16);
						break;
					case 3:
						incomingDatagram.writeShort(pay.payload[i] >> 16);
						incomingDatagram.write(pay.payload[i] >> 8);
						break;
				}

				incomingDatagram.setAddress(IP.ipIntToString(IPPacket.getSrcAddr(pay)) + ":"
						+ UDPPacket.getSourcePort(pay));
				incomingDatagram = null;

				//TODO
				//notifyAll();
			} catch (IOException e)
			{
			}
		}
	}

	/**
	 * Receives a datagram packet from this socket. When this method returns,
	 * the DatagramPacket's buffer is filled with the data received. The
	 * datagram packet also contains the sender's IP address, and the port
	 * number on the sender's machine.
	 * <p>
	 * This method blocks until a datagram is received. The length field of the
	 * datagram packet object contains the length of the received message. If
	 * the message is longer than the packet's length, the message is truncated.
	 * 
	 * @param datagram
	 *            the DatagramPacket into which to place the incoming data.
	 */
	synchronized public void receive(Datagram datagram)
	{
		incomingDatagram = datagram;
		incomingDatagram.reset();
//TODO
	//	wait();
		//wait(StackParameters.UDP_RECV_TIMEOUT);
	}

	/**
	 * @return a random unused port number
	 */
	public static short newLocalPort()
	{
		int randPort = NumFunctions.rand.nextInt() & 0xFFFF;

		for (int i = 0; i < StackParameters.UDP_CONNECTION_POOL_SIZE; i++)
			if (pool[i] != null && pool[i].status == UDP_CONN_USED && pool[i].localPort == randPort)
				return newLocalPort();

		return (short) randPort;
	}
}
