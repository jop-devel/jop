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

import ejip_old.CS8900;
import ejip_old.Net;
import ejip.jtcpip.util.Debug;
import ejip.jtcpip.util.NumFunctions;

/**
 * Implements a basic DHCP client
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 975 $ $Date: 2009/01/12 23:00:13 $
 */
public class DHCPClient
{
	/**
	 * UDP connection to the DHCP server to the server port 67 from the server
	 * port 68
	 */
	private static UDPConnection conn;

	/** buffer for the datagram */
	private static byte[] buffer;

	/** datagram for the communication */
	private static Datagram dg;

	/** transaction id */
	private static int transID;

	/** Timeout when we have to renew the lease */
	private static long renewalTimeout;

	/** IP address lease time */
	private static int leaseTimeSec;

	/** Server identifier (ip address) */
	private static int serverID;

	/** Time when we got the lease (required for the lease timeout) */
	private static long ackTime;

	/** time in seconds between two trys */
	public static int retryTimeout = 15;

	
	public static void init(){
		buffer = new byte[StackParameters.UDP_DATA_SIZE];

		conn = UDPConnection.newConnection(68, 0xFFFFFFFF, 67);
		dg = new DatagramPacket(buffer, buffer.length, "//255.255.255.255:67");
	}

	/**
	 * forbids instantiation
	 */
	private DHCPClient()
	{
	}

	/**
	 * Creates a new transaction ID and resets the datagram
	 */
	private static void start()
	{
		transID = NumFunctions.rand.nextInt();
		renewalTimeout = 0;
		leaseTimeSec = 0;
		serverID = 0;
		ackTime = 0;

		Net.linkLayer.gateway = 0;
		Net.linkLayer.netmask = 0;
	}

	/**
	 * Sends a DHCP discover datagram.
	 * 
	 * @return the datagram with the DHCP server answer
	 * @throws IOException
	 */
	private static Datagram discover() throws IOException
	{
		dg.reset();

		dg.write(1); // OP: request
		dg.write(1); // hw_type: ethernet
		dg.write(6); // hw_addr_len: 6 for ethernet
		dg.write(0); // hops

		dg.writeInt(transID); // transaction ID

		dg.writeShort(0); // secs: time elapsed since start of DHCP request
		dg.writeShort(0); // flags: MSB set rest must be zero

		dg.writeInt(0); // client ip address

		dg.writeInt(0); // your (client) ip address

		dg.writeInt(0); // server ip address

		dg.writeInt(0); // relay agent ip address

		dg.write(CS8900.eth[0]); // client hardware address
		dg.write(CS8900.eth[1]); // s.a.a.
		dg.write(CS8900.eth[2]); // s.a.a.
		dg.write(CS8900.eth[3]); // s.a.a.

		dg.write(CS8900.eth[4]); // s.a.a.
		dg.write(CS8900.eth[5]); // s.a.a.
		dg.writeShort(0); // s.a.a.

		dg.writeInt(0); // s.a.a.

		dg.writeInt(0); // s.a.a.

		dg.writeLong(0); // server host name
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);

		dg.writeLong(0); // boot file name
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);

		// Options (1 byte option type, 1 byte option length in bytes, n bytes
		// option value)
		dg.writeInt(0x63825363); // Magic cookie

		dg.write(53); // DHCP Msg type
		dg.write(1); // only one byte of data
		dg.write(1); // DHCP discover

		dg.write(61); // Client identifier
		dg.write(7); // 7 bytes of data
		dg.write(1); // 1 for Ethernet addr
		dg.write(CS8900.eth[0]); // client hardware address
		dg.write(CS8900.eth[1]); // s.a.a.
		dg.write(CS8900.eth[2]); // s.a.a.
		dg.write(CS8900.eth[3]); // s.a.a.
		dg.write(CS8900.eth[4]); // s.a.a.
		dg.write(CS8900.eth[5]); // s.a.a.

		dg.write(12); // Host name
		dg.write(6); // 6 bytes of data
		dg.writeChar('j'); // jcpip
		dg.writeChar('t');
		dg.writeChar('c');
		dg.writeChar('p');
		dg.writeChar('i');
		dg.writeChar('p');

		dg.write(55); // Parameter Request List
		dg.write(2); // 2 bytes of data
		dg.write(1); // Subnetmask
		dg.write(3); // Gateway

		dg.write(0xFF); // End of Options

		conn.send(dg);
		conn.receive(dg);

		if (dg.getLength() != 0)
		{
			dg.skipBytes(28); //jump to chaddr (client hardware address)
			//and check if the received datagram is for our MAC address
			boolean isOurs = true;
			for (int i = 0; i < 6; i++)
				if (dg.readUnsignedByte() != CS8900.eth[i])
				{
					isOurs = false;
					break;
				}
			
			if (!isOurs)
				dg.reset();
			else dg.skipBytes(-34);
		}
		
		return dg;
	}

	/**
	 * Sends a DHCP request for a given IP address
	 * 
	 * @param ipAddr
	 * @return DHCP server answer
	 * @throws IOException
	 */
	private static Datagram request(int ipAddr) throws IOException
	{
		dg.reset();

		dg.write(1); // OP: request
		dg.write(1); // hw_type: ethernet
		dg.write(6); // hw_addr_len: 6 for ethernet
		dg.write(0); // hops

		dg.writeInt(transID); // transaction ID

		dg.writeShort(0); // secs: time elapsed since start of DHCP request
		// but must be the same as at discover!!!!
		dg.writeShort(0x0000); // flags: MSB = uni/broadcast; rest must be zero

		dg.writeInt(ipAddr); // client ip address

		dg.writeInt(0); // your (client) ip address

		dg.writeInt(0); // server ip address

		dg.writeInt(0); // relay agent ip address

		dg.write(CS8900.eth[0]); // client hardware address
		dg.write(CS8900.eth[1]); // s.a.a.
		dg.write(CS8900.eth[2]); // s.a.a.
		dg.write(CS8900.eth[3]); // s.a.a.

		dg.write(CS8900.eth[4]); // s.a.a.
		dg.write(CS8900.eth[5]); // s.a.a.
		dg.writeShort(0); // s.a.a.

		dg.writeInt(0); // s.a.a.

		dg.writeInt(0); // s.a.a.

		dg.writeLong(0); // server host name
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);

		dg.writeLong(0); // boot file name
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);
		dg.writeLong(0);

		// Options (1 byte option type, 1 byte option length in bytes, n bytes
		// option value)
		dg.writeInt(0x63825363); // Magic cookie

		dg.write(53); // DHCP Msg type
		dg.write(1); // only one byte of data
		dg.write(3); // DHCP request

		dg.write(61); // Client identifier
		dg.write(7); // 7 bytes of data
		dg.write(1); // 1 for Ethernet addr
		dg.write(CS8900.eth[0]); // client hardware address
		dg.write(CS8900.eth[1]); // s.a.a.
		dg.write(CS8900.eth[2]); // s.a.a.
		dg.write(CS8900.eth[3]); // s.a.a.
		dg.write(CS8900.eth[4]); // s.a.a.
		dg.write(CS8900.eth[5]); // s.a.a.

		dg.write(12); // Host name
		dg.write(6); // 6 bytes of data
		dg.writeChar('j'); // jcpip
		dg.writeChar('t');
		dg.writeChar('c');
		dg.writeChar('p');
		dg.writeChar('i');
		dg.writeChar('p');

		dg.write(55); // Parameter Request List
		dg.write(3); // 2 bytes of data
		dg.write(1); // Subnetmask
		dg.write(3); // Gateway
		dg.write(51); // Lease time

		dg.write(0xFF); // End of Options

		conn.send(dg);
		conn.receive(dg);
		
		if (dg.getLength() != 0)
		{
			dg.skipBytes(28); //jump to chaddr (client hardware address)
			//and check if the received datagram is for our MAC address
			boolean isOurs = true;
			for (int i = 0; i < 6; i++)
				if (dg.readUnsignedByte() != CS8900.eth[i])
				{
					isOurs = false;
					break;
				}
			
			if (!isOurs)
				dg.reset();
			else dg.skipBytes(-34);
		}
		
		return dg;
	}

	/**
	 * Sets the IP address, the Subnet mask and the Gateway
	 * 
	 * @return true if the DHCP ACK has been received
	 */
	public static boolean setNetParams()
	{
		try
		{
			int offeredIP = 0;

			start();
			discover();

			if (dg.getLength() == 0)
			{
				return false;
			}

			dg.setAddress("//255.255.255.255:67"); // send again a broadcast
			dg.skipBytes(16); // jump to yiaddr
			offeredIP = dg.readInt();
			dg.skipBytes(8); // jump to chaddr (client MAC)

			request(offeredIP);

			if (dg.getLength() == 0)
			{
				return false;
			}

			return processOptions();
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks if the renewalTimeout has been reached and send a DHCP request in
	 * case
	 * 
	 * @return true if everything went right
	 */
	public static boolean renewIfNecessary()
	{
		if (renewalTimeout > System.currentTimeMillis())
			return true;

		try
		{
			start();
			request(Net.linkLayer.ip);

			if (dg.getLength() == 0)
			{
				return false;
			}

			return processOptions();
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * @return true if the processed datagram was a DHCP ACK packet
	 * @throws IOException
	 */
	private static boolean processOptions() throws IOException
	{
		int offeredIP;
		dg.skipBytes(16); // jump to yiaddr
		offeredIP = dg.readInt(); // read yiaddr
		Net.linkLayer.ip = offeredIP;

		dg.skipBytes(220); // jump to options (after magic cookie)

		int opt; // DHCP option
		int cnt; // count of option value bytes
		int value = 0; // option value;
		boolean result = false;

		while ((opt = dg.readUnsignedByte()) != 0xFF)
		{
			cnt = dg.readUnsignedByte();

			switch (cnt)
			{
				case 1:
					value = dg.readUnsignedByte();
					break;
				case 2:
					value = dg.readUnsignedShort();
					break;
				case 4:
					value = dg.readInt();
					break;
				default:
					for (int i = 0; i < cnt; i++)
						dg.readByte();
			}

//			if (Debug.enabled)
//				Debug.println("DHCP opt: " + opt + " cnt: " + cnt + " val: " + value, Debug.DBG_OTHER);
			if (Debug.enabled)
			Debug.println("DHCP opt", Debug.DBG_OTHER);

			// interpret the options
			switch (opt)
			{
				case 53: // DHCP Msg type
					result = (value == 5); // 5: DHCP ACK
					if (result)
						ackTime = System.currentTimeMillis();
					break;

				case 58: // renewal time
					renewalTimeout = value;
					break;

				case 51: // address lease time
					leaseTimeSec = value;
					break;

				case 54: // server identifier
					serverID = value;
					break;

				case 1: // subnet mask
					Net.linkLayer.netmask = value;
					break;

				case 3: // gateway
					Net.linkLayer.gateway = value;
					break;
			}
		}

		if (renewalTimeout > 0) // there has been a 'renewal time'-option
			renewalTimeout = ackTime + renewalTimeout * 1000; // as the
																// ackTime is in
																// seconds
		else if (leaseTimeSec > 0) // at least there has been an 'address lease
									// time'-option
			renewalTimeout = ackTime + leaseTimeSec * 500; // according to RFC
															// if not given by
															// the server it
															// should be 0.5 of
															// the leas time
		else
			renewalTimeout = ackTime + 3600000; // send renewal every hour

		return result;
	}

	/**
	 * @return the address lease time in hours
	 */
	public static int getLeaseTime()
	{
		return leaseTimeSec / 3600;
	}

	/**
	 * @return the renewal time in hours
	 */
	public static int getRenewalTime()
	{
		return (int) (renewalTimeout - ackTime) / 3600000;
	}

	/**
	 * @return the server IP address
	 */
	public static int getServerIP()
	{
		return serverID;
	}
}
