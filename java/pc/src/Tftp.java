import java.io.*;
import java.net.*;
import java.util.*;

/**
*	send UPD requests to jop and receive UPD packets for debug output.
*/

public class Tftp {

	public static final int PORT = 69;

	private static final int RRQ = 1;
	private static final int WRQ = 2;
	private static final int DAT = 3;
	private static final int ACK = 4;
	private static final int ERR = 5;

	private static String addrString = "192.168.1.2";


/**
*	test Main read internal memory of JOP.
*/
	public static void main(String[] args) throws IOException {

		int i;

		if (args.length!=0) setAddr(args[0]);

		byte[] buf = new byte[65536];
//		for (;;) {
			int rcv_len = read((byte) 'i', (byte) '0', buf);

			System.out.println();
			for (i=0; i<rcv_len; i+=4) {
				int j = (buf[i]<<24) +
					((buf[i+1]<<16)&0xff0000) +
					((buf[i+2]<<8)&0xff00) +
					(buf[i+3]&0xff);
				System.out.println((i/4)+" "+j);
			}
			System.out.println();
//		}
	}

	public static void setAddr(String addr) {
		addrString = addr;
	}

	public static int read(byte fn, byte sector, byte[] buf) throws IOException {

		// get a datagram socket
		DatagramSocket socket = new DatagramSocket();

		byte[] sndBuf = new byte[512+4];
		byte[] rcvBuf = new byte[512+4];

		InetAddress address = InetAddress.getByName(addrString);

		sndBuf[0] = 0;
		sndBuf[1] = RRQ;
		sndBuf[2] = fn;
		sndBuf[3] = sector;
		sndBuf[4] = sndBuf[5] = 0;
		DatagramPacket send = new DatagramPacket(sndBuf, 6, address, PORT);
		socket.send(send);

		socket.setSoTimeout(10000);

		int expBlock = 1;

		int len = 0;

		// get response
		for (;;) {

			// this is neccessary! I don't know why I have to construct a new packet for every reveive.
			DatagramPacket rcv = new DatagramPacket(rcvBuf, rcvBuf.length);

			try {
				socket.receive(rcv);

				// display response
				byte[] resp = rcv.getData();
				if (resp[1]==DAT) {

					sndBuf[0] = 0;
					sndBuf[1] = ACK;
					sndBuf[2] = (byte) (expBlock >>> 8);
					sndBuf[3] = (byte) expBlock;

					send = new DatagramPacket(sndBuf, 4, address, PORT);
					socket.send(send);

					int block = ((((int) resp[2])&0xff)<<8) +
								(((int) resp[3])&0xff);

System.out.print("got "+block+"\r");
					if (block == expBlock) {
						for (int i=0; i<rcv.getLength()-4; ++i) {
							buf[(block-1)*512+i] = resp[i+4];
						}
						if (rcv.getLength()<512+4) {
							len = (block-1)*512+rcv.getLength()-4;
							break;
						}

						expBlock += 1;
					}

				} else {
					len = 0;
					break;
				}
			} catch (Exception e) {

System.out.println();
				System.out.println(e);
				// retry
				if (expBlock==1) {
System.out.println("retry RRQ");
					sndBuf[0] = 0;
					sndBuf[1] = RRQ;
					sndBuf[2] = fn;
					sndBuf[3] = sector;
				} else {
System.out.println("retry ACK "+(expBlock-1));
					sndBuf[0] = 0;
					sndBuf[1] = ACK;
					sndBuf[2] = (byte) ((expBlock-1) >>> 8);
					sndBuf[3] = (byte) (expBlock-1);
				}

				send = new DatagramPacket(sndBuf, 4, address, PORT);
				socket.send(send);
			}

		}
	
		socket.close();
		return len;
	}

	public static boolean write(byte fn, byte sector, byte[] buf, int len) throws IOException {

		// get a datagram socket
		DatagramSocket socket = new DatagramSocket();

		byte[] sndBuf = new byte[512+4];
		byte[] rcvBuf = new byte[512+4];

		InetAddress address = InetAddress.getByName(addrString);

		sndBuf[0] = 0;
		sndBuf[1] = WRQ;
		sndBuf[2] = fn;
		sndBuf[3] = sector;
		sndBuf[4] = sndBuf[5] = 0;
		DatagramPacket send = new DatagramPacket(sndBuf, 6, address, PORT);
		socket.send(send);

		socket.setSoTimeout(5000);

		int expBlock = 0;
		boolean ret = false;

		// get response
		for (;;) {

			// this is neccessary! I don't know why I have to construct a new packet for every reveive.
			DatagramPacket rcv = new DatagramPacket(rcvBuf, rcvBuf.length);

			try {
				socket.receive(rcv);

				// display response
				byte[] resp = rcv.getData();

				if (resp[1]==ACK) {

					int block = ((((int) resp[2])&0xff)<<8) +
								(((int) resp[3])&0xff);

					if (block != expBlock) {
						ret = false;
						break;
					}

					++expBlock;

System.out.print("send "+expBlock+"\r");

				} else {
					ret = false;
					break;
				}
			} catch (Exception e) {

System.out.println();
				System.out.println(e);
				// retry
				if (expBlock==0) {
System.out.println("retry WRQ");
				} else {
System.out.println("retry DAT "+expBlock);
				}
			}

			if (expBlock==0) {

				sndBuf[0] = 0;
				sndBuf[1] = WRQ;
				sndBuf[2] = fn;
				sndBuf[3] = sector;
				sndBuf[4] = sndBuf[5] = 0;
				send = new DatagramPacket(sndBuf, 6, address, PORT);
				socket.send(send);

			} else {

				sndBuf[0] = 0;
				sndBuf[1] = DAT;
				sndBuf[2] = (byte) (expBlock >>> 8);
				sndBuf[3] = (byte) expBlock;

				int start = (expBlock-1)*512;
				int k=4;
				if (start>len) {						// last acked packet was less than 512
					ret = true;
					break;
				}
				for (int i=start; i<len && k<4+512; ++i) {
					sndBuf[k] = buf[i];
					++k;
				}

				send = new DatagramPacket(sndBuf, k, address, PORT);
				socket.send(send);
				if (k==4) {						// last packet is empty
					ret = true;
					break;
				}
			}
		}
	
		socket.close();
		return ret;
	}
}
