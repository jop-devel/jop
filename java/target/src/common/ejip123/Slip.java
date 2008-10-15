package ejip123;

import joprt.RtThread;
import ejip123.util.Dbg;
import ejip123.util.Serial;

/**
 Slip driver. Acting as LinkLayer in the stack between {@link Serial} and {@link Ip}. Used to tunnel IP packets over a
 serial line according to rfc1055.
 */
public class Slip extends LinkLayer{

private static final int END = 0xc0;
private static final int ESC = 0xdb;
private static final int ESC_END = 0xdc;
private static final int ESC_ESC = 0xdd;

//private static int simSendErr, simRcvErr;

// todo change buffers to byte arrays
/** receive buffer. */
private static int[] rbuf = null;
/** send buffer. */
private static int[] sbuf = null;
/** bytes received. */
private static int cnt = 0;
/** mark escape sequence. */
private static boolean esc = false;

/** bytes to be sent. 0 means txFree */
private static int scnt = 0;
/** already sent bytes. */
private static int sent = 0;

/** The one and only reference to this object. */
private static Slip single = null;

private static Serial ser = null;

private static int timer = 0;

private Slip(){
}

/**
 Initialises the slip layer.

 @param prio         The priority of the thread.
 @param us           The period of the thread.
 @param serPort      The serial port to use.
 @param ipAddr       The ip address of this interface.
 @param maxTransUnit The maximum transfer unit (mtu) in bytes of the local network segment. Used to set up receive and
 transmit buffers (defaults to 1500B if <= 0).
 @return the singleton reference. */
public static LinkLayer init(int prio, int us, Serial serPort, int ipAddr, int maxTransUnit){

	if(single != null)
		return single;	// already called init()

	single = new Slip();
	if(maxTransUnit <= 0)
		maxTransUnit = 1500;
	single.setMtu(maxTransUnit);
	rbuf = new int[maxTransUnit];
	sbuf = new int[maxTransUnit];
	cnt = 0;
	esc = false;
	scnt = 0;
	sent = 0;

	ser = serPort;

	single.setIp(ipAddr);
	/** DONE whats the correct netmask for slip tunnels?
	 {@link #isLocalBroadcast(int)} */
	single.setNetmask(Ip.Ip(255, 255, 255, 255));
	// Slip timeout (for windows slip reply) depends on
	// period (=100*period) !

	new RtThread(prio, us){
		public void run(){
			for(; ;){
				waitForNextPeriod();
				single.loop();
			}
		}
	};

	return single;
}

/**
 Normally we can check for (local) broadcasts with the interface ip and netmask. On a point-to-point connection every
 packet is directed in the view of a network layer, but could be seen as a broadcast in the view of the link layer too.
 Since the linklayer will just send any packet we put into the pool for it, we are interested in the view of the network
 layer only. The network layer checks for broadcasts with {@link #isLocalBroadcast(int)} and it should never think that a
 packet travelling over slip is meant as a broadcast, except for limited broadcasts. Therefore we will override the
 method.

 @param dstAddr The destination address.
 @return True if dstAddr is the limited broadcast address. */
public boolean isLocalBroadcast(int dstAddr){
	return dstAddr == 0xffffffff;

}

/** main loop. */
protected void loop(){

	recv_packet();

	if(timer > 100 && cnt > 0){		// flush buffer on timeout (100*thread period)
/*
		Dbg.wr("slip timeout, flushing buffer cnt=");
		Dbg.intVal(cnt);
		Dbg.lf();
*/
//		for(int i = 0; i < cnt; ++i){
//			int val = rbuf[i];
//			if(val == '\r'){
//				Dbg.wr('r');
//			} else{
//				Dbg.wr(val);
//			}
//		}
		cnt = 0;
		timer = 0;
		// send anything back for windoz slip version
		if(ser.txFreeCnt() > 0){
			ser.wr('C');
			ser.wr('L');
			ser.wr('I');
			ser.wr('E');
			ser.wr('N');
			ser.wr('T');
			ser.wr('S');
			ser.wr('E');
			ser.wr('R');
			ser.wr('V');
			ser.wr('E');
			ser.wr('R');
			ser.wr('\r');
		}
	}

	if(scnt != 0){
		int i1 = ser.txFreeCnt();
		if(i1 > 2){
			snd(i1);
		}
	}
	if(scnt == 0){				// transmit buffer is free
		//
		// get a ready to send packet with source from this driver.
		//
		Packet p = PacketPool.getTxPacket(single);
		if(p != null){
			send(p);				// send one packet
		}
	}
}

/** get a Packet buffer and copy from receive buffer. */
private static void read(){

	Packet p = PacketPool.getFreshRcvPacket(single);
	if(p == null){
		Dbg.wr('!');
		cnt = 0; // drop it
		return;
	}

	int[] pb = p.buf;

	// copy buffer
	int k = 0;
	for(int i = 0; i < cnt; i += 4){
		for(int j = 0; j < 4; ++j){
			k <<= 8;
			k |= rbuf[i + j];
		}
		pb[i>>>2] = k;
	}

	p.setLen(cnt);

	cnt = 0;

/*
++simRcvErr;
if (simRcvErr%5==1) {
p.setStatus(PacketPool.FREE);
Dbg.wr(" rcv dropped");
Dbg.lf();
return;
}
*/
	p.setStatus(Packet.RCV);		// inform upper layer
}

/* copy packet to send buffer. */
private static void send(Packet p){

	int[] pb = p.buf;

//	Dbg.wr('s');
//	Dbg.intVal(p.len());

/*
++simSendErr;
if (simSendErr%7==3) {
p.setStatus(PacketPool.FREE);
Dbg.wr(" send dropped");
Dbg.lf();
return;
}
*/
	scnt = p.len();
	sent = 0;
	for(int i = 0; i < scnt; i += 4){
		int k = pb[i>>>2];
		sbuf[i] = k>>>24;
		sbuf[i + 1] = (k>>>16)&0xff;
		sbuf[i + 2] = (k>>>8)&0xff;
		sbuf[i + 3] = k&0xff;
	}
	if(p.status() == Packet.CON_RDY){
		p.setStatus(Packet.CON_ONFLY);		// mark on the fly
	} else{
		p.free();		// mark packet free
	}
}

/* copy from send buffer to serial buffer. */
private static void snd(int free){

	if(sent == 0){
		ser.wr(END);
		--free;
	}

	int i;
	for(i = sent; free > 1 && i < scnt; ++i){

		int c = sbuf[i];
		if(c == END){
			ser.wr(ESC);
			ser.wr(ESC_END);
			free -= 2;
		} else if(c == ESC){
			ser.wr(ESC);
			ser.wr(ESC_ESC);
			free -= 2;
		} else{
			ser.wr(c);
			--free;
		}
	}
	sent = i;

	if(sent == scnt && free > 0){
		ser.wr(END);
		scnt = 0;
		sent = 0;
	}
}

/**
 Reads from the serial buffer into the receive buffer. If more bytes are received, than the receive buffer can hold, the
 packet will be truncated.
 */
private static void recv_packet(){
	int rx = ser.rxCnt();
	if(rx <= 0){
		timer++;
		return;
	}

	timer = 0;
	while(rx > 0){
		int c;
		if(!esc){
			rx--;
			c = ser.rd();
		} else{
			c = ESC;
			esc = false;
		}
		switch(c){
			/* if it's an END character then we're done with the packet */
			case END:
				/* a minor optimization: if there is no data in the packet, ignore it.
				This is meant to avoid bothering IP with all the empty packets generated
				by the duplicate END characters which are in turn sent to try to detect line noise. */
				if(cnt >= 20){
					// 0 padding of the last word
					int max = single.getMtu() - cnt;
					if(max > 3)
						max = 3;
					switch(max){
						case 3:
							rbuf[cnt + 2] = 0;
						case 2:
							rbuf[cnt + 1] = 0;
						case 1:
							rbuf[cnt] = 0;
					}
					read();
					return;
				}
				cnt = 0;
				break;

			case ESC:
				/* if it's an ESC character, get another character and then figure out what to store in the packet based on that.*/
				if(rx <= 0){
					esc = true;
					return;
				}
				c = ser.rd();
				rx--;

				/* if "c" is not one of these two, then we have a protocol violation.
				  The best bet seems to be to leave the byte alone and just stuff it into the packet. */
				switch(c){
					case ESC_END:
						c = END;
						break;
					case ESC_ESC:
						c = ESC;
						break;
				}

			default:
				/* here we fall into the default handler and let it store the character for us*/
				if(cnt < single.getMtu())
					rbuf[cnt++] = c;
		}
	}
}

/*
private static void print(int len){

	synchronized(PacketPool.print){
		if(len > cnt)
			len = cnt;
		Dbg.wr('\n');
		Dbg.wr("dumping SLIP packet content inside the first ");
		Dbg.intVal(len);
		Dbg.wr('/');
		Dbg.intVal(cnt);
		Dbg.wr("bytes\n");
		int max = len + 14;
		for(int i = 0; i < max; i++){
			int rem = i&0xf; // rem = i % 16
			if(rem == 0){
				Dbg.wr('\n');
				if(i < 0x100)
					Dbg.wr('0');
				if(i < 0x1000)
					Dbg.wr('0');
				Dbg.hexVal(i);
				Dbg.wr(' ');
			}
			if(rem == 8)
				Dbg.wr(' ');

			if(i < 14){
				Dbg.hexVal(0);
			} else{
				Dbg.hexVal(rbuf[(i - 14)]);
			}
		}
	}

}
*/
}
