package ejip123.examples;

import com.jopdesign.sys.Const;
import ejip123.*;
import joprt.RtThread;
import ejip123.util.Dbg;
import ejip123.util.DbgUdp;
import ejip123.util.Serial;
import util.Timer;

/** This examples shows how TCP clients can open a connection and use it. */
public class TcpClientTest{
private static TcpHandler th = null;
private static TcpConnection con = null;
private static int cnt = 0;
private static StringBuffer text = new StringBuffer("total useful text #000\n");
private static final Object lock = new Object();

private TcpClientTest(){
}

public static void main(String[] args) throws InterruptedException{

	PacketPool.init(10, 1500);

	DbgUdp.init();

//	Serial ser = new Serial(10, 1000, Const.IO_UART_BG_MODEM_BASE); // simulator
	Serial ser = new Serial(10, 1000, Const.IO_UART1_BASE);
	LinkLayer slip = Slip.init(9, 1000, ser, Ip.Ip(192, 168, 2, 2), 1500);
	Ip.init(5, 1000);
	Router.init(3);
	Router.addRoute(new Route(Ip.Ip(192, 168, 2, 0), Ip.Ip(255, 255, 255, 0), slip));
	Router.setDefaultInterface(slip);

	Tcp.init(1, 4);
	th = new TcpTestHandler();

	RtThread.startMission();

	Router.print();
	forever();
}

private static void forever(){
	text.setLength(19);
	for(; ;){
		synchronized(lock){
			if(con == null)
				Tcp.open(Ip.Ip(192, 168, 2, 1), 4321, 1234, th);
			else{
				Packet p = PacketPool.getFreshPacket();
				if(p != null){
					text.append(cnt % 1000);
					text.append('\n');
					p.setData(Tcp.OFFSET<<2, text);
					if(con.send(p, true)){
						cnt++;
						text.setLength(19);
					}
				}
			}
		}

		for(int i = 0; i < 5; ++i){
			RtThread.sleepMs(500);
			Timer.wd();
		}
	}
}

private static class TcpTestHandler implements TcpHandler{

	/** handle one request on the registered port. */
	public boolean request(TcpConnection con, Packet p, int off){
		Dbg.wr("request\n");
		p.print(0);
		return true;
	}

	public boolean isBusy(TcpConnection newCon){
		return con == null;
	}

	/** Connection is established. Transfer can start. */
	public void established(TcpConnection newCon){
		synchronized(lock){
			if(con != null){
				newCon.close();
			} else{
				Dbg.wr("established\n");
				con = newCon;
			}
		}
	}

	public void closed(TcpConnection closedCon){
		synchronized(lock){
			if(closedCon == con){
				Dbg.wr("closed\n");
				con = null;
				cnt = 0;
			}
		}
	}

	public void reset(TcpConnection closedCon){
		synchronized(lock){
			if(closedCon == con){
				Dbg.wr("reset\n");
				con = null;
				cnt = 0;
			}
		}
	}
}
}