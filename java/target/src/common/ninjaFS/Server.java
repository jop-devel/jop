/* *************************************************************************** *

	Copyright 2009 Georg Merzdovnik, Gerald Wodni

	This file is part of ninjaFS.

	ninjaFS is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 3 of the License, or
	(at your option) any later version.

	ninjaFS is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

* *************************************************************************** */

package ninjaFS;

import com.jopdesign.sys.Const;
import joprt.RtThread;
import ejip123.*;
import ejip123.Ip;
import ejip123.Packet;
import ejip123.PacketPool;
import ejip123.util.Dbg;
import ejip123.util.DbgUdp;
import ejip123.util.Serial;
import ejip123.Udp;
import ejip123.UdpHandler;
import util.Timer;

import ninjaFS.Filesystem.FileSystem;
import ninjaFS.Rpc.Call;
import ninjaFS.Rpc.Handler;
import ninjaFS.Rpc.Reply;
import ninjaFS.Rpc.*;

public class Server implements UdpHandler
{
	final static boolean TWO_SERIAL = false;

	private static final int portNumber = 111;
	private static final int maxReceivedPacketSize = 1500;
	
	private boolean serverRunning = false;

	private Packet p;
	private int idx;
	private int destIp, srcIp, srcPort;


	private Server(){
	}

	public void request( Packet receivedPacket, int offset ) 
	{

		StringBuffer receiveBuffer = new StringBuffer( receivedPacket.len() );
		int bufferLength = receivedPacket.getData( offset, receiveBuffer );
		String buffer = receiveBuffer.toString();

		byte[] receivedData = buffer.getBytes();
		
        Call rpcCall = new Call();
		rpcCall.read( receivedData );

		Handler handler = Handler.create( rpcCall );
		Reply reply = handler.read();

	
		StringBuffer portBuffer = new StringBuffer( 22 );
		receivedPacket.getData( 0, portBuffer );
		int destinationPort = ((int)portBuffer.charAt(20) << 8) | ((int)portBuffer.charAt(21))  ;

		destIp = Ip.Ip(192, 168, 1, 1);
		srcIp = Ip.Ip(192, 168, 1, 2);
		srcPort = 111;
		Dbg.wr( rpcCall.toString() + "\nHandler:" + reply.getData().length  + "----" );
		Dbg.lf();


		if( reply != null )
		{
			idx = Udp.OFFSET<<2;
			p = PacketPool.getFreshPacket();
			p.setLen( reply.getWriteOffset() + idx );
			
			byte[] Data = reply.getData();

			for( int i = 0; i< reply.getWriteOffset() ; i++ )
			{ 
				System.out.println( "[" + i + "]=" + Data[i] + "; " );
				p.setByte(  Data[i] & 0xFF, idx + i );
			}
			
            Udp.send(p, srcIp, destIp, srcPort, destinationPort, true);
		}

		String data;
		try
		{
			data = new String( receivedData, 0, bufferLength );
		}
		catch( Exception e )
		{
			data = "null";
		}

		if( data.startsWith("exit") )
			serverRunning = false;

		receivedPacket.free();
	}

	public void run()
	{
		FileSystem fsystem = new FileSystem();	
		Dbg.wr( "NFS ready" );
		Dbg.wr( "NFS really ready" );
		Dbg.lf();
		Udp.init(1);
		if( Udp.addHandler( portNumber, (UdpHandler) this ) )
		{
			Dbg.wr("Port ready!");
			Dbg.lf();
		}
		else
			Dbg.wr("Port in use!");

		Dbg.lf();

		for(;;)
		{
		}
	}

	public static void main(String[] args)
	{
		PacketPool.init(10, 1500); // inits 10 packet buffers with 1500B each

		Serial ser;
		if (TWO_SERIAL) {
			Dbg.initSer(); // serial debug output		
			ser = new Serial(10, 1000, Const.IO_UART_BG_MODEM_BASE); // simulator
		} else {
			DbgUdp.init(); // sends debug output over the network to 192.168.2.1:10000 (see init method)		
			ser = new Serial(10, 1000, Const.IO_UART1_BASE); // one byte every ~400us at 19200 baud
		}



		Router.init(3); // initializes a routing table with 3 routes
		LinkLayer slip = Slip.init(9, 10000, ser, Ip.Ip(192, 168, 1, 2), 1500);
		Ip.init(6, 50000); // ip (and therefore icmp and tcp) loop thread: period 50ms
		Router.addRoute(new Route(Ip.Ip(192, 168, 2, 0), Ip.Ip(255, 255, 255, 0), slip));
		Router.setDefaultInterface(slip); // where should packets go which are not matched by a route?

		Dbg.wr( "Starting Mission" );
		Dbg.lf();
		RtThread.startMission();
		Router.print();
		Dbg.wr( "Starting Server" );
		Dbg.lf();

		Server server = new Server();
		server.run();
	}
}
