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

import ejip.jtcpip.util.Debug;
import ejip.jtcpip.util.NumFunctions;

/**
 * Represents a TCP Connection.
 * Contains the Connection State and input/output
 * buffers. The size of the pool of available connections and the sizes of the
 * buffers can be set through constants.
 * 
 * @author Tobias Kellner
 * @author Ulrich Feichter
 * @author Christof Rath
 * @version $Rev: 989 $ $Date: 2007/09/04 00:56:05 $
 */
public class TCPConnection
{
	
	public boolean wakeup = false;
	/** Connection state: Closed */
	public final static byte STATE_CLOSED = 0;

	/** Connection state: Listen */
	public final static byte STATE_LISTEN = 1;

	/** Connection state: SYN Sent */
	public final static byte STATE_SYN_SENT = 2;

	/** Connection state: SYN Received */
	public final static byte STATE_SYN_RCVD = 3;

	/** Connection state: Established */
	public final static byte STATE_ESTABLISHED = 4;

	/** Connection state: FIN Wait 1 */
	public final static byte STATE_FIN_WAIT_1 = 5;

	/** Connection state: FIN Wait 2 */
	public final static byte STATE_FIN_WAIT_2 = 6;

	/** Connection state: Close Wait */
	public final static byte STATE_CLOSE_WAIT = 7;

	/** Connection state: Closing */
	public final static byte STATE_CLOSING = 8;

	/** Connection state: Last ACK */
	public final static byte STATE_LAST_ACK = 9;

	/** Connection state: Time Wait */
	public final static byte STATE_TIME_WAIT = 10;

	/** Free <code>TCPConnection</code> */
	public final static byte TCP_CONN_FREE = 0x01;

	/** Used <code>TCPConnection</code> */
	public final static byte TCP_CONN_USED = 0x02;

	/** The <code>TCPConnection</code> pool */
	protected static TCPConnection[] pool ;
	public static void init(){
		pool = new TCPConnection[StackParameters.TCP_CONNECTION_POOL_SIZE];

	}
	
	/**
	 * If > -1 try every NwLoop cycle the get a payload and send a portion of
	 * pool[retryToSendData]
	 */
	protected static int retryToSendData = -1;

	/**
	 * Stores the status of a certain connection.
	 * <b>Note:</b> state != status
	 * state is the TCP state as in RFC status: Connection in use or free
	 */
	protected byte status;

	/**
	 * The default recieve buffer.
	 * <p>
	 * If the default buffer is used we limit the required memory but if one
	 * closes the connection and someone else opens the connection again both
	 * parties have read/write access to the same stream!!
	 */
	protected TCPInputStream defaultInputStream;

	/**
	 * The default send buffer.
	 * <p>
	 * If the default buffer is used we limit the required memory but if one
	 * closes the connection and someone else opens the connection again both
	 * parties have read/write access to the same stream!!
	 */
	protected TCPOutputStream defaultOutputStream;

	/**
	 * The actual recieve buffer. Might be the default stream, or an connection
	 * own stream
	 */
	public TCPInputStream iStream;

	/**
	 * The actual send buffer. Might be the default stream, or an connection own
	 * stream
	 */
	public TCPOutputStream oStream;

	/** ip of the remote host */
	public int remoteIP;

	/** port of the remote host */
	public short remotePort;

	/** port of the local host */
	public short localPort;

	/** state of the connection */
	private byte state;

	/** previous state */
	private byte prevState;

	/**
	 * last acknoledged sequence number. beginnign of the unacknowledged
	 * sequence numbers
	 */
	protected int sndUnack;

	/** time when the seq number in sndUnack was first acknowledged */
	protected int sndUnackTime;

	/** next sequence number to send */
	protected int sndNext;

	/** send window */
	protected short sndWindow;

	/**
	 * sequence number of the incoming segment with whom a window update was
	 * done (SND.WL1)
	 */
	protected int sndWndLastUpdateSeq;

	/**
	 * acknowledge number of the incoming segment with whom a window update was
	 * done (SND.WL2)
	 */
	protected int sndWndLastUpdateAck;

	/** initial sequence number (ISS) */
	protected int initialSeqNr;

	/** initial remote sequence number (IRS) */
	protected int initialRemoteSeqNr;

	/** The next sequence number we expect to receive */
	protected int rcvNext;

	/** recieve window */
	protected short rcvWindow;

	/** timestamp from the last packet received from the remote side */
	protected int timeLastRemoteActivity;

	/**
	 * maximum segment size wich will be set from the remote host in the MSS
	 * option or set by the Constant in StackParameters
	 */
	protected int maxSndSegSize;

	/**
	 * Send the remaining data and then close the connection
	 */
	protected boolean flushAndClose;

	/**
	 * is true if a syn is to send. the sequencenumber of the syn is then stored
	 * in synToSendSeq
	 * 
	 */
	protected boolean synToSend;

	/**
	 * sequence number of the syn (needed for retransmitt and acknowledging)
	 * this is the sequence number which must be acknowledged so that the syn is
	 * acknowledged
	 */
	protected int synToSendSeq;

	/**
	 * is true if a fin is to send. The sequencenumber of the fin is then stored
	 * in finToSendSeq
	 */
	protected boolean finToSend;

	/**
	 * sequence number of the fin (needed for retransmitt and acknowledging)
	 * this is the sequence number which must be acknowledged so that the fin is
	 * acknowledged
	 */
	protected int finToSendSeq;
	
	/**
	 * in this variable the retransmissions of the same sequence number are counted.
	 * if then a acknowledge comes the counter is set to zero
	 */
	protected int numRetransmissions;

	/**
	 * connection number (just for debugging use!) is the index of the connection in pool
	 */
	protected int connNum;

	
	/**
	 * Create a <code>TCPConnection</code>. This constructor is private,
	 * because <code>TCPConnection</code>s can only be got through
	 * {@link TCPConnection#newConnection}. Initializes some internal
	 * variables. Creates a {@link TCPInputStream} and a {@link TCPOutputStream}.
	 * 
	 * @param port
	 */
	private TCPConnection(short port)
	{
		state = STATE_CLOSED;
		status = TCP_CONN_FREE;
		flushAndClose = false;
	
		defaultInputStream = new TCPInputStream(StackParameters.TCP_CONNECTION_RCV_BUFFER_SIZE);
		defaultOutputStream = new TCPOutputStream(StackParameters.TCP_CONNECTION_SND_BUFFER_SIZE);
	
		iStream = defaultInputStream;
		oStream = defaultOutputStream;
	
		cleanUpConnection(this);
		localPort = port;
	}

	/**
	 * Returns the name of a given state.
	 * 
	 * @param state
	 *            the state
	 * @return the state name
	 */
	protected static String getStateName(byte state)
	{
		switch (state)
		{
			case STATE_LISTEN:
				return "listen";

			case STATE_SYN_SENT:
				return "syn sent";

			case STATE_SYN_RCVD:
				return "syn received";

			case STATE_ESTABLISHED:
				return "established";

			case STATE_FIN_WAIT_1:
				return "fin wait 1";

			case STATE_FIN_WAIT_2:
				return "fin wait 2";

			case STATE_CLOSE_WAIT:
				return "close wait";

			case STATE_CLOSING:
				return "closing";

			case STATE_LAST_ACK:
				return "last ack";

			case STATE_TIME_WAIT:
				return "time wait";

			case STATE_CLOSED:
				return "closed";

			default:
				return "UNKNOWN!";
		}
	}

	/**
	 * Returns the previous state of the connection.
	 * 
	 * @return the previous state of the connection
	 */
	protected byte getPreviousState()
	{
		return prevState;
	}

	/**
	 * Returns the current state of the connection.
	 * 
	 * @return the current state of the connection
	 */
	public byte getState()
	{
		return state;
	}

	/**
	 * Sets the state of the connection.
	 * 
	 * @param state
	 *            the state to set
	 */
	public synchronized void  setState(byte state)
	{
		prevState = this.state;
		this.state = state;
	}

	

	/**
	 * Returns true if a given Payload is part of this connection.
	 * 
	 * @param pay
	 * @return boolean
	 */
	protected boolean isIncomingPayPartOfConn(Payload pay)
	{
		return (IPPacket.getSrcAddr(pay) == remoteIP && TCPPacket.getSourcePort(pay) == remotePort && TCPPacket
				.getDestPort(pay) == localPort);
	}

	/**
	 * Look for a TCPConnection with matching source IP and port and destination
	 * port. Returns null if no matching connection was found.
	 * 
	 * @param dstPort
	 *            The destination port to look for
	 * @param srcIP
	 *            The source IP to look for
	 * @param srcPort
	 *            The source port to look for
	 * @return <code>TCPConnection</code> object if available or null if no
	 *         connection was found
	 */
	protected static TCPConnection getConnection(short dstPort, int srcIP, int srcPort)
	{
		for (int i = 0; i < StackParameters.TCP_CONNECTION_POOL_SIZE; i++)
		{
			if ((pool[i] != null) && (pool[i].status == TCP_CONN_USED)
					&& (pool[i].localPort == dstPort))
			{
				// local port matches destination port
				if (pool[i].state == STATE_LISTEN)
				{
					// found matching listening connection - but check if there
					// is a matching one first
					for (int j = i + 1; j < StackParameters.TCP_CONNECTION_POOL_SIZE; j++)
						if ((pool[j] != null) && (pool[j].status == TCP_CONN_USED)
								&& (pool[j].localPort == dstPort))
							if ((pool[j].remoteIP == srcIP) && (pool[j].remotePort == srcPort))
								// found a better match
								return pool[j];
					// didn't find an exact match - return listening connection
					return pool[i];
				}
				if ((pool[i].remoteIP == srcIP) && (pool[i].remotePort == srcPort))
					// found an exact match
					return pool[i];
			}
		}
		// found no match
		return null;
	}

	/**
	 * Create a new <code>TCPConnection</code>. Tries to create a new
	 * <code>TCPConnection</code>. If the connection pool is full, or 
	 * there is already a listening connection at the specified port, 
	 * null is returned.
	 * 
	 * @param port
	 * @return The new <code>TCPConnection</code> or null if unsuccessful
	 */
	public static TCPConnection newConnection(short port)
	{
		for (byte i = 0; i < StackParameters.TCP_CONNECTION_POOL_SIZE; i++)
		{
			if (pool[i] == null)
			{
				pool[i] = new TCPConnection(port);
				pool[i].connNum = i;
			}

			if (pool[i].status == TCP_CONN_USED)
			{
				if (pool[i].localPort == port && pool[i].state == STATE_LISTEN)
					return null;
				continue;
			}

			// Found the first free connection

			// Now test if the rest of the connections do not listen on the same
			// port
			for (int j = i + 1; j < StackParameters.TCP_CONNECTION_POOL_SIZE; j++)
				if ((pool[j] != null) && (pool[j].status == TCP_CONN_USED)
						&& (pool[j].localPort == port) && (pool[j].state == STATE_LISTEN))
					return null;

			// The connection at position i is free and no one else is
			// using the same port
			pool[i].status = TCP_CONN_USED;
			cleanUpConnection(pool[i]);
			pool[i].localPort = port;
			return pool[i];
		}
		return null;
	}

	/**
	 * Relinquish a <code>TCPConnection</code>. The connection is marked as
	 * free. 
	 * ATTENTION: User might have some references which will be wrong after
	 *            invoking. Use free to avoid such conflicts
	 * 
	 * @see #abort()
	 * @param conn
	 *            The connection to delete
	 */
	protected static void deleteConnection(TCPConnection conn)
	{
		if (conn == null)
			return;
		conn.oStream.close();
		conn.iStream.close();

		/*
		 * if the connection is released the default streams get in place again
		 * (just in case the connection had its own streams)
		 */
		conn.oStream = conn.defaultOutputStream;
		conn.iStream = conn.defaultInputStream;

		conn.setState(STATE_CLOSED);
		conn.status = TCP_CONN_FREE;
	}

	/**
	 * Prepares a connection to remove possible artefacts of an old connection.
	 * 
	 * @param conn
	 */
	private static void cleanUpConnection(TCPConnection conn)
	{
		conn.remoteIP = 0;
		conn.remotePort = 0;
		conn.localPort = 0;
		conn.state = STATE_CLOSED;
		conn.sndUnack = 0;
		conn.sndNext = 0;
		conn.sndWindow = 0;
		conn.initialSeqNr = 0;
		conn.initialRemoteSeqNr = 0;
		conn.rcvNext = 0;
		conn.rcvWindow = StackParameters.TCP_INITIAL_WINDOW_SIZE;
		conn.maxSndSegSize = StackParameters.TCP_INITIAL_SND_MAX_SEGMENT_SIZE;
		conn.flushAndClose = false;
		conn.oStream.reOpen();
		conn.iStream.reOpen();
		conn.finToSend = false;
		conn.finToSendSeq = 0;
		conn.synToSend = false;
		conn.synToSendSeq = 0;
		conn.timeLastRemoteActivity = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
		conn.numRetransmissions = 0;
	}

	/**
	 * @return a random unused port number
	 */
	protected static short newLocalPort()
	{
		int randPort = NumFunctions.rand.nextInt() & 0xFFFF;
	
		// FIXME: I don't like recursive calls
		for (int i = 0; i < StackParameters.TCP_CONNECTION_POOL_SIZE; i++)
			if (pool[i] != null && pool[i].status == TCP_CONN_USED && pool[i].localPort == randPort)
				return newLocalPort();
	
		return (short) randPort;
	}

	/**
	 * Checks if there is some data to retransmit. If there is it changes the
	 * parameters of the connection so that the next transmission is done with
	 * the old data.
	 * 
	 * @return false if there was no answer after MAX_TRY_TO_RETRANSMIT_TIME
	 *         milliseconds else true
	 */
	protected boolean checkAndprepareRetransmission()
	{
		if (checkforRetransmission())
		{
			if(getState() == STATE_SYN_SENT)
				if(numRetransmissions >= StackParameters.TCP_MAX_TIMES_SYN_RETRANSMIT)
					return false;
			
			if (Debug.enabled)
			{
//				Debug.println("sndUnackTime  + sndUnackTime +  current time	+ (int) (System.currentTimeMillis() & 0xFFFFFFFF)", Debug.DBG_TCP);
//				Debug.println("sndNext alt =  + sndNext", Debug.DBG_TCP);
				Debug.println("Starting retransmission", Debug.DBG_TCP);
			}
			sndNext = sndUnack; // maybe some other things to set...
			if(!oStream.isBufferEmpty())
				oStream.setPtrForRetransmit();
			numRetransmissions++;
			
			// so that is not startet soon after
			sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
			if (!NumFunctions.isBetweenOrEqualSmaller(timeLastRemoteActivity, timeLastRemoteActivity
					+ StackParameters.TCP_MAX_TRY_TO_RETRANSMIT_TIME, (int) (System
					.currentTimeMillis() & 0xFFFFFFFF)))
				return false; // no answer from other side since
			// MAX_TRY_TO_RETRANSMIT_TIME milliseconds ->
			// close
		}
		return true;
	}

	/**
	 * Checks if retransmission is needed.
	 * 
	 * @return true if retransission is needed, else false
	 */
	private boolean checkforRetransmission()
	{
		if (sndNext == sndUnack)
			return false;
		int timeout = StackParameters.TCP_RETRANSMISSION_TIMEOUT;
		int current_time = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
		if (getState() == STATE_SYN_SENT)
		{
			timeout *= StackParameters.TCP_SYN_RETRANSMIT_TIMEOUT_MULTIPLYER;
		}
		
		return (!NumFunctions.isBetweenOrEqualSmaller(sndUnackTime, sndUnackTime
				+ timeout, current_time));
	}

	/**
	 * Is continously called by <code>NWLoopThread</code> after a defined timeout. 
	 * Here all the tcp-timeouts are processed, new user data is sent and
	 * retransmission is initiaded when needed.
	 * 
	 * @return true if nothing to send or if we got a payload to send a portion,
	 * 		false if no <code>Payload</code> was available
	 */
	protected synchronized boolean pollConnection()
	{
//		if (Debug.enabled)
//			Debug.println("------- Polling TCP conn ----------", Debug.DBG_TCP);
//			Debug.println("------- Polling TCP conn " + connNum + " (state: " + getStateName(state)
//				+ ") ----------", Debug.DBG_TCP);
		if (state == STATE_LISTEN || state == STATE_CLOSED)
			return true;

		boolean sendSomething = false;
		// check for zero window and open it if possible
		if (rcvWindow == 0)
		{
			if (iStream.getFreeBufferSpace() >= StackParameters.TCP_INITIAL_WINDOW_SIZE)
			{
				if (Debug.enabled)
					Debug.println("Opening window for conn", Debug.DBG_TCP);
				rcvWindow = StackParameters.TCP_INITIAL_WINDOW_SIZE;
				sndUnackTime = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
				sendSomething = true;
			}
		}

		switch (state)
		{				
			case STATE_LAST_ACK:
				// close connection if no response came for
				// MAX_TRY_TO_RETRANSMIT_TIME because ack might be lost
				if (!NumFunctions.isBetweenOrEqualBigger(timeLastRemoteActivity, timeLastRemoteActivity
						+ StackParameters.TCP_MAX_TRY_TO_RETRANSMIT_TIME, (int) (System
						.currentTimeMillis() & 0xFFFFFFFF)))
				{
					if (Debug.enabled)
						Debug.println("Closing connection", Debug.DBG_TCP);
					deleteConnection(this);
				}
				return true;
				
			case STATE_TIME_WAIT:
				// close connection if no response came for TCP_TIME_WAIT_TIME
				if (!NumFunctions.isBetweenOrEqualBigger(timeLastRemoteActivity, timeLastRemoteActivity
						+ StackParameters.TCP_TIME_WAIT_TIME,
						(int) (System.currentTimeMillis() & 0xFFFFFFFF)))
				{
					if (Debug.enabled)
						Debug.println("Closing connection in TIME_WAIT state after timeout", Debug.DBG_TCP);
					deleteConnection(this);
				}
				return true;
				
			case STATE_SYN_SENT:
				if (flushAndClose)
				{
					// user called close()
					if (Debug.enabled)
						Debug.println("Entering state: CLOSED", Debug.DBG_TCP);
					deleteConnection(this);
					return true;
				}
				if(!checkforRetransmission())
					return true;
				break;
				
			case STATE_LISTEN:
				if (flushAndClose)
				{
					// user called close()
					if (Debug.enabled)
						Debug.println("Entering state: CLOSED", Debug.DBG_TCP);
					deleteConnection(this);
					return true;
				}
				break;	
		}
		
		// send data if neccesary
		if (oStream.isNoMoreDataToRead() && !checkforRetransmission() && !sendSomething
				&& !flushAndClose)
			return true;

		Payload pay = TCP.createEmptyPayload();
		if (pay == null)
		{
			if (Debug.enabled)
				Debug.println("no more Payload... ", Debug.DBG_TCP);
			return false;
		}
		TCP.sendPackets(this, pay);	
		return true;
	}

	
	/**
	 * Set own streams for a connection. The default streams get back in place
	 * in {@link #deleteConnection(TCPConnection)}
	 * ATENTION: do it before reading and writing something from the streams!
	 * 			Or clone the streams before setting new ones.
	 * 			Else the stack will get a bit confused...
	 * 
	 * @param in
	 * 		The new input stream
	 * @param out
	 * 		The new output stream
	 */
	protected void setOwnStreams(TCPInputStream in, TCPOutputStream out)
	{
		iStream = in;
		oStream = out;
	}

	/**
	 * Aborts the connection.
	 * That means that all the user's streams are closed and the state turns
	 * to Closed. This is needed that the user can take note that something 
	 * went wrong and finally he can call <code>close()</code> so that the
	 * <code>TCPConnection</code> object is released.
	 * 
	 * @see #deleteConnection
	 * @see #close()
	 * 
	 */
	protected synchronized void abort()
	{
		oStream.close();
		iStream.close();
		setState(STATE_CLOSED);
		// Wake up listening connections
		// TODO 
		//notifyAll();
	}

	/**
	 * Close the connection.
	 * Closes the output stream and later a FIN will be send. After this call
	 * both streams are closed, so no new data can be sent and received.
	 * 
	 */
	public void close()
	{
		if (Debug.enabled)
			Debug.println("close call from user on conn", Debug.DBG_TCP);
		
		if (getState() == STATE_CLOSED)
		{
			deleteConnection(this);
			return;
		}
		
		oStream.close();
		iStream.close();
		
		/*
		 * following is not possible on jop because no class loader
		 * try {
		 * 	synchronized(Class.forName("ejip.jtcpip.TCP"))
		 * 	{
		 * 		flushAndClose = true;
		 * 	}
		 * }
		 * catch (ClassNotFoundException e) { e.printStackTrace(); }
		 */
		TCP.flushAndClose(this); // just to use tcps monitior
	
		pollConnection();
	
		/*
		 * Payload pay = TCP.createEmptyPacket(0); if(pay == null) return;
		 * TCP.sendPackets(this, pay);
		 */
	}
}
