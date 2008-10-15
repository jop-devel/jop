package ejip123;

import ejip123.util.Dbg;

/**
 An instance of an TCP connection. Contains the TCP state machine and all other vital parts of a TCP conneciton. see RFC
 793.
 */
public class TcpConnection{
// TCP connection states
private final static int FREE = -1;
private final static int CLOSED = 0;
private final static int LISTEN = 1;
private final static int SYN_RCVD = 2;
private final static int SYN_SENT = 3;
private final static int ESTABLISHED = 4;
//private final static int CLOSE_WAIT = 5;
private final static int LAST_ACK = 6;
private final static int FIN_WAIT_1 = 7;
private final static int FIN_WAIT_2 = 8;
private final static int CLOSING = 9;
private final static int TIME_WAIT = 10;
// TCP flags TODO public because of the old html server
//static final int FL_URG = 0x20;
public static final int FL_ACK = 0x10;
public static final int FL_PSH = 0x8;
public static final int FL_RST = 0x4;
public static final int FL_SYN = 0x2;
public static final int FL_FIN = 0x1;

private int state = FREE;
private int localIp = 0;
private int remoteIp = 0;
private int localPort = 0;
private int remotePort = 0;
private TcpHandler th = null;

/** The next expected sequence number. */
private int rcvNxt = 0;
/** The last sent sequence number, offset of first byte in data stream. */
private int seqNum = 0;
/** The outstanding packet. We only allow one packet on the fly per connection. */
private Packet outstanding = null;
private int timeout = 0;
private int closing = 0;
/** round trip time. */
private int rtt = 500;
private int sndMss = 536;
private int rcvMss = 536;

TcpConnection(){
}

void loop(int cur){
	synchronized(this){
		if(state == FREE)
			return;
/*
	if(state != ESTABLISHED){
		Dbg.wr("\nCon ");
		Dbg.wr(this.toString());
		Dbg.wr(" loop() state=");
		Dbg.intVal(state);
	}
*/
		if(state == CLOSED){
			th.closed(this);
			free();
		}
		if(closing == 1 && outstanding == null){
			Packet p = PacketPool.getFreshPacket();
			if(p != null){
				if(send(p, FL_FIN|FL_ACK, true, 0)){
					//Dbg.wr(" LOOP: sent FIN");
					state = FIN_WAIT_1;
					closing = 2;
				}
			}
		}
		if(outstanding != null){
			if(timeout - cur < 0){
//			Dbg.wr("\nCon ");
//			Dbg.wr(this.toString());
//			Dbg.wr(" loop() state=");
//			Dbg.intVal(state);
//				Dbg.wr("\ntransmission timed out... \ncurrent rtt=");
//				Dbg.intVal(rtt);
				if(rtt < 0){
					if(rtt < -60000){
						// give up
//						Dbg.wr("giving up...\n");
						Packet os = outstanding;
						outstanding = null;
						sendRst(os, 0);
//						Dbg.wr("rst3");

						free();
					} else{
//						Dbg.wr("resending... ");
//						Dbg.intVal(outstanding.buf[6]);
//						Dbg.lf();
						outstanding.testSetStatus(Packet.CON_ONFLY, Packet.CON_RDY);
						// double the round trip time with every timeout
						rtt = rtt<<1;
						timeout = cur - rtt;
					}
				} else{
//					Dbg.wr("first resending... ");
//					Dbg.intVal(outstanding.buf[6]);
//					Dbg.lf();
//					outstanding.print(0);
					outstanding.testSetStatus(Packet.CON_ONFLY, Packet.CON_RDY);
					rtt = (-rtt)<<1;
					timeout = cur - rtt;
				}
			}
		} else if(state == TIME_WAIT && timeout - cur < 0){
//		Dbg.intVal(cur);
			free();
		}
	}
}

private void sendRst(Packet p, int flags){
	synchronized(this){
		p.setLen(0);
//		Dbg.wr("\nCon ");
//		Dbg.wr(this.toString());
//		Dbg.wr(" reset ");
		send(p, FL_RST|flags, true, 0);
		free();
	}
}

public boolean send(Packet p, boolean push){
	synchronized(this){
		if(state != ESTABLISHED){
			p.free();
			return false;
		}
		return send(p, (push ? FL_PSH : 0)|FL_ACK, false, 0);
	}
}

public void close(){
	synchronized(this){
		if(closing == 0){
			switch(state){
				case CLOSED:
				case LISTEN:
				case SYN_SENT:
					free();
					break;
				case SYN_RCVD:
				case ESTABLISHED:
					closing = 1;
					break;
				default:
					break;
			}
		}
	}
}

boolean open(int remIp, int locIp, int remPort, int locPort, TcpHandler handler){
	synchronized(this){
		localIp = locIp;
		remoteIp = remIp;
		localPort = locPort;
		remotePort = remPort;
		th = handler;

		Packet p = PacketPool.getFreshPacket();
		if(p != null){
			seqNum = (int)System.currentTimeMillis(); // TODO secure enough?
			if(send(p, FL_SYN, true, 0)){
				state = SYN_SENT;
				return true;
			}
		}
		return false;
	}
}

void newIncoming(int remIp, int locIp, int remPort, int locPort, Packet p, int off){
	synchronized(this){
		th = Tcp.getHandler(locPort);
		// no handler found
		if(th == null || th.isBusy(this)){
			// tcp does not generate port unreachable but sends resets.
			// this is done in handleState, if the connection remains in closed state.
			//Icmp.sendPortUnreachable(p.buf, off);
			//Dbg.wr('T');
			//Dbg.intVal(locPort);
			state = CLOSED;
		} else{
			state = LISTEN;
		}
		localIp = locIp;
		remoteIp = remIp;
		localPort = locPort;
		remotePort = remPort;

		// the advertised receive buffer should be limited either by mtu size of the local net or by the size of the packet buffers.
		// both calculations use minimal TCP and IP header lengths and are therefore too optimistic.
		// if the sender decides to transmit a packet with mss and use IP or TCP options, we would need to drop the packet.
		rcvMss = Math.min(Tcp.getMaxPayload(remIp), PacketPool.PACKET_SIZE() - (Tcp.OFFSET<<2));

		handleState(p, off);
	}
}

/** Frees the connection and returns it to the pool. */
private void free(){
	synchronized(this){
//		Dbg.wr("\nCon ");
//		Dbg.wr(this.toString());
//		Dbg.wr(" freed");

		state = FREE;
		outstanding = null;
		rtt = 500;
		closing = 0;
		th = null;
	}
}

boolean matches(int remIp, int locIp, int remPort, int locPort){
	synchronized(this){
		return remIp == remoteIp && locIp == localIp && remPort == remotePort && locPort == localPort;
	}
}

/**
 Processes an incoming packet (if it belongs to this connection).

 @param off     Offset where the TCP header starts
 @param remIp   The remote IP address.
 @param locIp   The local IP address.
 @param remPort The remote port.
 @param locPort The local port.
 @param p       The received packet.
 @return True, if the packet got processed. False, if it does not belong to this connection. */
boolean processPacket(int remIp, int locIp, int remPort, int locPort, Packet p, int off){
	synchronized(this){
		if(remIp == remoteIp && locIp == localIp && remPort == remotePort && locPort == localPort){
			handleState(p, off);
			return true;
		} else
			return false;
	}
}

/**
 Handles the TCP state machine.

 @param p   incoming packet
 @param off Offset in 32b-words where the TCP header starts. */
private void handleState(Packet p, int off){
	synchronized(this){

		int[] buf = p.buf;
		int i = buf[off + 3]>>>16;
		int flags = i&0xff;
		int hlen = i>>>12;
		int datOff = off + hlen;
		int datLen = p.len() - (datOff<<2);

		if(state == CLOSED){
			//Dbg.wr("TC closed!\n");
			if((flags&FL_RST) != 0){
				p.free();
				free();
			} else{
				if((flags&FL_ACK) != 0){
					seqNum = buf[off + 2];
					sendRst(p, 0);
				} else{
					seqNum = 0;
					rcvNxt = buf[off + 1] + datLen + (((flags&(FL_SYN|FL_RST)) != 0) ? 1 : 0);
// not used because of stack mem! sendRst(p, FL_ACK);
					p.setLen(0);
					send(p, FL_RST|FL_ACK, true, 0);
					free();
				}
			}
			return;
		}

//	Dbg.wr("\nCon ");
//	Dbg.wr(this.toString());
//	Dbg.wr(" TCP state: ");
//	Dbg.intVal(state);

		// check received sequence number for all packets, if we can
		if(buf[off + 1] != rcvNxt && state != LISTEN && state != SYN_SENT){
			//Dbg.intVal(buf[off + 1]);
			//Dbg.wr("is not the correct SEQuence number ");
			//Dbg.intVal(rcvNxt);
			//Dbg.wr("- dropping\n");
			p.free();
			return;
		}
		if((flags&FL_RST) != 0){
			if(state >= ESTABLISHED){
				th.reset(this);
			}
			p.free();
			free();
			return;
		}

		//Dbg.wr("datlen ");
		//Dbg.intVal(datLen);
		//Dbg.wr("off ");
		//Dbg.intVal(off);

		// process ack
		if((flags&FL_ACK) != 0){
//			Dbg.intVal(buf[off + 2]);
			if(buf[off + 2] != seqNum){
//				Dbg.wr("is not the expected ACKnowledge number ");
//				Dbg.intVal(seqNum);
				//Dbg.wr("- dropping\n");
				if(state < ESTABLISHED){
					/*  If the connection is in any non-synchronized state (LISTEN, SYN-SENT,
						SYN-RECEIVED), and the incoming segment acknowledges something not
						yet sent (the segment carries an unacceptable ACK) a reset is sent. */
					seqNum = buf[off + 2];
					sendRst(p, 0);

/*
					if((flags&FL_ACK) != 0){
						seqNum = buf[off + 2];
						sendRst(p, 0);
					} else{
						seqNum = 0;
						rcvNxt = buf[off + 1] + datLen;
						sendRst(p, FL_ACK);
					}
*/
				} else{
					/*  If the connection is in a synchronized state (ESTABLISHED, FIN-WAIT-1,
						FIN-WAIT-2, CLOSE-WAIT, CLOSING, LAST-ACK, TIME-WAIT),
						any unacceptable segment (out of window sequence number or unacceptable
						acknowledgment number) must elicit only an empty acknowledgment segment */
					send(p, FL_ACK, true, 0);
				}
				return;
			}
//			Dbg.wr("correct ACK\n");
			if(outstanding != null){
				//Dbg.wr("correct ACK received... ");
				if(rtt >= 0){
					//Dbg.wr("rtt old=");
					//Dbg.intVal(rtt);
//					int currtt = (int)System.currentTimeMillis() - (timeout - (rtt * 4));
//					rtt = (rtt + currtt) / 2;
					rtt = (rtt + ((int)System.currentTimeMillis() - (timeout - (rtt<<2))))>>1; // timeout = now() + 4*rtt
					//Dbg.wr("rtt cur=");
					//Dbg.intVal(rttCur);
					//Dbg.wr("rtt new=");
					//Dbg.intVal(rtt);
				} else
					rtt = -rtt;
				Packet os = outstanding;
				outstanding = null;
				os.free();
				if(flags == FL_ACK && datLen == 0 && state == ESTABLISHED){
					// just an ack for sent user data
					//Dbg.wr("just an ACK ");
					p.free();
					return;
				}
			} else if(state == SYN_SENT && (flags&FL_RST) != 0){
				th.reset(this);
				p.free();
				free();
				return;
			}
		}

		// we handle only one packet at a time so we have to drop it.
		if(outstanding == null){
		 Label:
			if(state == LISTEN){
				if((flags&FL_SYN) == 0)
					break Label;
				readOptions(buf, off, datOff);
//			Dbg.wr("sndMss=");
//			Dbg.intVal(sndMss);
				//Dbg.wr("LISTEN: new connection SYNACKing ");
				rcvNxt = buf[off + 1] + 1;
				seqNum = (int)System.currentTimeMillis();	// TODO? secure enough?
				if(send(p, FL_SYN|FL_ACK, true, 0))
					state = SYN_RCVD;
				return;
			} else if(state == SYN_SENT){
				if((flags&FL_SYN) == 0)
					break Label;

				readOptions(buf, off, datOff);
//			Dbg.wr("sndMss=");
//			Dbg.intVal(sndMss);
				rcvNxt = buf[off + 1] + 1;
				if(send(p, FL_ACK, true, 0)){
					if((flags&FL_ACK) != 0){
						state = ESTABLISHED;
						th.established(this);
					} else{
						state = SYN_RCVD;
					}
					//Dbg.wr("SYN_SENT: sent ack on syn ");
				}
				return;
			} else if(state == SYN_RCVD){
				if((flags&FL_ACK) != 0){
					//Dbg.wr("SYN_RCVD: SYN acked ");
					state = ESTABLISHED;
					th.established(this);
				}
			} else if(state == ESTABLISHED){
				if((flags&FL_FIN) != 0){
					//Dbg.wr("ESTABLISHED: got FIN ");
					if(send(p, FL_FIN|FL_ACK, true, 1)){
						state = LAST_ACK;
					}
					return;
				}
				// acknowledging before delivering
				p.setStatus(Packet.APP);
				if(th.request(this, p, datOff)){
					if(p.freeIfApp()){
						send(p, FL_ACK, true, datLen);
					} else{
						Packet p_ack = PacketPool.getFreshPacket();
						if(p_ack != null)
							send(p_ack, FL_ACK, true, datLen);
					}
					return;
				} else{
					break Label;
				}
//			} else if(state == CLOSE_WAIT){// ESTABLISHED changes directly to LAST_ACK
				// we do not need this state as we are not interested in half open connections
				//Dbg.wr("CLOSE_WAIT, shouldn't happen!");
			} else if(state == LAST_ACK){
				if(outstanding == null){
					//Dbg.wr("we received the last ACK ");
					state = CLOSED;
				}
			} else if(state == FIN_WAIT_1){
				if((flags&(FL_FIN|FL_ACK)) == (FL_FIN|FL_ACK)){
					if(send(p, FL_ACK, true, 1)){
//					Dbg.wr(" changing to time_wait, timeout=");
						timeout = (int)System.currentTimeMillis() + (rtt<<1);
//					Dbg.intVal(timeout);
//					Dbg.wr("cur=");
//					Dbg.intVal((int)System.currentTimeMillis());
						state = TIME_WAIT;
					}
				} else if((flags&FL_ACK) != 0){
					state = FIN_WAIT_2;
					p.free();
				} else if((flags&FL_FIN) != 0){
					if(send(p, FL_ACK, true, 1))
						state = CLOSING;
				} else
					break Label;
				return;
			} else if(state == FIN_WAIT_2){
				if((flags&(FL_FIN)) != 0){
					if(send(p, FL_ACK, true, 1)){
//					Dbg.wr("FIN_WAIT_2 changing to time_wait, timeout=");
						timeout = (int)System.currentTimeMillis() + (rtt<<1);
//					Dbg.intVal(timeout);
//					Dbg.wr("cur=");
//					Dbg.intVal((int)System.currentTimeMillis());
						state = TIME_WAIT;
					}
					return;
				}
			} else if(state == CLOSING){
				if((flags&FL_ACK) != 0){
//				Dbg.wr(" changing to time_wait, timeout=");
					timeout = (int)System.currentTimeMillis() + (rtt<<1);
//				Dbg.intVal(timeout);
//				Dbg.wr("cur=");
//				Dbg.intVal((int)System.currentTimeMillis());
					state = TIME_WAIT;
				}
			} else if(state == TIME_WAIT){
			} else{
			}
		}
		p.free();
	}
}

private void readOptions(int[] buf, int off, int datOff){
	synchronized(this){
		off += 5;
//	Dbg.wr(" options: ");
		int kind = -1;
		int len = 0;
		int mss = 0;
		for(int i = off; i < datOff; i++){
			int tmp = buf[i];
			for(int j = 3; j >= 0; j--){
				int cur = (tmp>>>(j<<3))&0xFF;
//			Dbg.hexVal(cur);
				switch(kind){
					case -1:
						if(cur == 0){
							return;
						} else if(cur == 1){
							continue;
						} else{
							// begin of a new option
							kind = cur;
						}
						break;
					case 2: // mss
						if(len == 0){
							len = 2;
						} else if(mss == 0){
							mss = cur<<8;
						} else{
							int max = mss + cur;
//						Dbg.wr("remote mss=");
//						Dbg.intVal(max);
							sndMss = Math.min(max, Tcp.getMaxPayload(remoteIp));
							mss = 0;
							kind = -1;
							len = 0;
						}
						break;
					default:
						break;
				}
			}
		}
	}
}

/* @param rcvNxtChange how much should the rcvNxt value rise. Will be undone, if the packet could not be sent. */
private boolean send(Packet p, int fl, boolean controlOnly, int rcvNxtChange){
	synchronized(this){
		int buf[] = p.buf;
		int hdrOff = Ip.OFFSET;
		buf[hdrOff] = (localPort<<16) + remotePort;
		buf[hdrOff + 1] = seqNum;
		if((fl&FL_ACK) != 0){
			rcvNxt += rcvNxtChange;
			buf[hdrOff + 2] = rcvNxt;
		} else{
			buf[hdrOff + 2] = 0;
		}

		int dataOff = Tcp.OFFSET;
		if((fl&FL_SYN) != 0){
			// hlen = 24, mss option
			buf[hdrOff + 3] = 0x60000000 + (fl<<16) + rcvMss; // 1 word of options, receive window = mss
			buf[hdrOff + 5] = 0x02040000 + rcvMss; // mss option
			p.setLen((hdrOff + 6)<<2);
		} else{
			buf[hdrOff + 3] = 0x50000000 + (fl<<16) + rcvMss; // hlen = 20, no options
			if(controlOnly){
				p.setLen(dataOff<<2);
			} else if(p.len() <= (dataOff<<2) || outstanding != null){
				p.free();
				return false;
			} else{
				// ensure last word to be 0-padded
				int loff = p.len();
				int last = loff&0x3;
				loff = (loff)>>2; // word offset
				if(last == 1){
					buf[loff] = buf[loff]&0xff000000;
				} else if(last == 2){
					buf[loff] = buf[loff]&0xffff0000;
				} else if(last == 3){
					buf[loff] = buf[loff]&0xffffff00;
				}
			}
		}
		int datLen = p.len() - (dataOff<<2);
		if(datLen > sndMss){
			Dbg.wr("TCP segment too big!\n");
			p.free();
			return false;
		}

		// inject pseudo header
		buf[hdrOff - 1] = remoteIp;
		buf[hdrOff - 2] = localIp;
		buf[hdrOff - 3] = (Tcp.PROTOCOL<<16)|(p.len() - (hdrOff<<2)); // set protocol and tcp length

		buf[hdrOff + 4] = 0; // reset checksum and urgent pointer
		buf[hdrOff + 4] = Ip.chkSum(buf, hdrOff - 3, p.len() - ((hdrOff - 3)<<2))<<16;

		// packets with data or having SYN or FIN set, need to be retransmitted. those will get freed, when acked.
		// everything else should get freed by the link layer.
		boolean retransmit = datLen > 0 || (fl&(FL_SYN|FL_FIN)) != 0;
		if(retransmit){
//		Dbg.wr("mark for retransmit, ");
//		Dbg.intVal( datLen);
			p.setStatus(Packet.CON_PREP);
		}

		boolean ret = Ip.send(p, localIp, remoteIp, Tcp.PROTOCOL);
		if(ret){
//			Dbg.wr("sent ");
//			Dbg.intVal(seqNum);
//			Dbg.lf();
			if((fl&(FL_SYN|FL_FIN)) != 0){
				seqNum++;
			} else{
				seqNum += datLen;
			}

			if(retransmit){
				outstanding = p;
				timeout = ((int)(System.currentTimeMillis()) + (rtt<<2)); // timeout = now() + 4*rtt
			}
		} else
			rcvNxt -= rcvNxtChange;
		return ret;
	}
}

boolean isUsed(){
	synchronized(this){
		return state != FREE;
	}
}

boolean hasOutStanding(){
	synchronized(this){
		return outstanding != null;
	}
}

}
