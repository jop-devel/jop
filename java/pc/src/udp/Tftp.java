/*
  This file is part of JOP, the Java Optimized Processor (http://www.jopdesign.com/)

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package udp;
import java.io.*;
import java.net.*;

/**
*	TFTP client for Flash programming and read of internal memory (stack).
*/

public class Tftp {

	public static final int PORT = 69;

	private static final int RRQ = 1;
	private static final int WRQ = 2;
	private static final int DAT = 3;
	private static final int ACK = 4;
	private static final int ERR = 5;

	private String addrString;
	private int retryCnt = 0;
	private static final int MAX_CNT = 3;
	
	private boolean verbose;


	/**
	 * 
	 */
	public Tftp(String addr) {
		addrString = addr;
		verbose = true;
	}

/**
*	test Main read internal memory of JOP.
*/
	public static void main(String[] args) throws IOException {

		int i;
		String file = "i0";

		if (args.length==0) {
			System.out.println("usage: Tftp ip-address [file]");
			System.exit(-1);
		}
		if (args.length==2) file = args[1];
		Tftp t = new Tftp(args[0]);

		int[] buf = new int[65536/4];
		int rcv_len = t.read(file, buf);
		System.out.println(rcv_len+" words received");

		System.out.println();
		for (i=0; i<rcv_len; ++i) {
			System.out.println(i+" "+buf[i]);
		}
		System.out.println();
	}

	public int read(String fn, int[] buf) {
		try {
			byte[] byteBuf = new byte[buf.length*4];
			byte f = (byte) fn.charAt(0);
			byte s = (byte) fn.charAt(1);
			int len = read(f, s, byteBuf);
			for (int i=0; i<len; i+=4) {
				buf[i/4] = (byteBuf[i]<<24) +
					((byteBuf[i+1]<<16)&0xff0000) +
					((byteBuf[i+2]<<8)&0xff00) +
					(byteBuf[i+3]&0xff);
			}
			return len/4;
		} catch (Exception e) {
			return 0;
		}
	}

	public int read(byte fn, byte sector, byte[] buf) throws IOException {

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
		retryCnt = 0;

		// get response
		for (;;) {

			// this is neccessary! I don't know why I have to construct a new packet for every reveive.
			DatagramPacket rcv = new DatagramPacket(rcvBuf, rcvBuf.length);

			try {
				socket.receive(rcv);

				// display response
				byte[] resp = rcv.getData();
				retryCnt = 0;
				if (resp[1]==DAT) {

					sndBuf[0] = 0;
					sndBuf[1] = ACK;
					sndBuf[2] = (byte) (expBlock >>> 8);
					sndBuf[3] = (byte) expBlock;

					send = new DatagramPacket(sndBuf, 4, address, PORT);
					socket.send(send);

					int block = ((((int) resp[2])&0xff)<<8) +
								(((int) resp[3])&0xff);

					if (verbose) System.out.print("got "+block+"\r");
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

				++retryCnt;
				if (retryCnt > MAX_CNT) return 0;
				if (verbose) System.out.println();
				System.out.println(e);
				// retry
				if (expBlock==1) {
					if (verbose) System.out.println("retry RRQ");
					sndBuf[0] = 0;
					sndBuf[1] = RRQ;
					sndBuf[2] = fn;
					sndBuf[3] = sector;
				} else {
					if (verbose) System.out.println("retry ACK "+(expBlock-1));
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

	public boolean write(byte fn, byte sector, byte[] buf, int len) throws IOException {

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

					if (verbose) System.out.print("send "+expBlock+"\r");

				} else {
					ret = false;
					break;
				}
			} catch (Exception e) {

				if (verbose) System.out.println();
				System.out.println(e);
				// retry
				if (expBlock==0) {
					if (verbose) System.out.println("retry WRQ");
				} else {
					if (verbose) System.out.println("retry DAT "+expBlock);
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
	/**
	 * @return
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param b
	 */
	public void setVerbose(boolean b) {
		verbose = b;
	}

}
