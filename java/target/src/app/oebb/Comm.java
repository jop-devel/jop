package oebb;

/**
*	Comm.java: Communication for OEBB project.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*
*/

import util.*;
import ejip.*;
import joprt.*;

public class Comm extends RtThread {

	// UDP destination ports
	public static final int BG_REQUEST = 2000;
	public static final int FDL_REPLY = 2001;
	public static final int FDL_REQUEST = 2002;
	public static final int BG_REPLY = 2003;

	// header
	public static final int UDP_DATA = Udp.DATA;
	public static final int OFF_CMD = UDP_DATA;
	public static final int OFF_ID = UDP_DATA+1;
	public static final int OFF_BGID = UDP_DATA+2;
	public static final int OFF_DATA = UDP_DATA+3;

	// local linux pc
	// public static final int DST_IP = (192<<24) + (168<<16) + (0<<8) + 1;
	// local slip pc
	// public static final int DST_IP = (192<<24) + (168<<16) + (1<<8) + 1;
	// local Windoz pc
	// public static final int DST_IP = (192<<24) + (168<<16) + (0<<8) + 5;
	// linux pc (chello)
	// public static final int DST_IP = (80<<24) + (110<<16) + (200<<8) + 231;

	// walter (dial up)
	// public static final int DST_IP = (212<<24) + (152<<16) + (141<<8) + 241;
	// Sagner PC
	// public static final int DST_IP = (10<<24) + (19<<16) + (36<<8) + 71;
	// Wieselburg
	// public static final int DST_IP = (10<<24) + (49<<16) + (70<<8) + 243;

	public static int dst_ip;

/**
*	ping periode in n x 100 ms
*/
	private static final int PING_PERIOD = 200;		// two times msg. timeout (2*5 s)

	private static int ptim;
	private static boolean pingOut;

/**
*	some statistics
*/
	private static int[] stat;



private static int rtim;
private static boolean scheduleReset;

/**
*	The one and only reference to this object.
*/
	private static Comm single;

	private static int bgid;
	private static LinkLayer ipLink;

/**
*	helper class for outstanding commands.
*/
	private static class MsgOut {

		final static int MAX_OUTSTANDING = 4;
		final static int MAX_DATA = 5;		// maximum length of data
//		final static int TIMEOUT = 5000000;	// 5 seconds
		final static int TIMEOUT = 10000000;	// 10 seconds
		final static int RETRY = 3;			// maximum send count

		int cmd;		// waiting for repley on this command
		int id;			// timestamp of sending
		int[] data;
		int len;		// length of data
		int cnt;		// send count (for retrys)

		private static boolean init;
		private static MsgOut[] list;
		private static Object monitor;

		private MsgOut() {
			cmd = 0;
			id = 0;
			data = new int[MAX_DATA];
			len = 0;
			int cnt = 0;	// 0 means it is a free packet
		}

		static void init() {

			if (init) return;
			init = true;
			monitor = new Object();

			list = new MsgOut[MAX_OUTSTANDING];
			for (int i=0; i<MAX_OUTSTANDING; ++i) {
				list[i] = new MsgOut();
			}
		}

		static MsgOut getFree(int cmd) {

			if (!init) init();

			for (int i=0; i<MAX_OUTSTANDING; ++i) {
				synchronized (monitor) {
					if (list[i].cmd == 0) {
						list[i].cmd = cmd;		// mark it allocated
						return list[i];
					}
				}
			}
Dbg.wr("no free MsgOut\n");
			return null;
		}

		void setFree() {
			synchronized (monitor) {
				cnt = 0;
				cmd = 0;
			}
		}

		static void setAllFree() {
			synchronized (monitor) {
				for (int i=0; i<MAX_OUTSTANDING; ++i) {
					MsgOut m = list[i];
					m.cnt = 0;
					m.cmd = 0;
				}
			}
		}
		
		static void setAllFreeErr() {
			synchronized (monitor) {
				for (int i=0; i<MAX_OUTSTANDING; ++i) {
					MsgOut m = list[i];
					m.cnt = 0;
					m.cmd = 0;
				}
			}
			// force a reconnect in Ppp-Modem
			ipLink.reconnect();
			Status.connOk = false;
			Status.commErr = 1;
		}

		/**
		*	get a Packet and send or resend the message.
		*/
		boolean send() {

			if (cnt>=RETRY) {
Dbg.wr("give up ");
Dbg.intVal(cmd);
Dbg.lf();
				setAllFreeErr();				// give up!
				return false;
			}

			// get an IP packet
			Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
			if (p == null) {								// got no free buffer!
				Dbg.wr('!');
				Dbg.wr('b');
				return false;
			}

			++cnt;
	
			p.buf[OFF_CMD] = cmd;
			id = Timer.us();				// id is timer value
			p.buf[OFF_ID] = id;
			p.buf[OFF_BGID] = bgid;			// bg263 serial number

if (len>MAX_DATA) {
Dbg.wr("big problem\n");
synchronized (monitor) { for(;;); }
}

			for (int i=0; i<len; ++i) {
				p.buf[OFF_DATA+i] = data[i];
			}
			p.len = (OFF_DATA+len)<<2;		// in bytes

Dbg.wr("send cmd ");
Dbg.intVal(p.buf[Udp.DATA]);
/*
for (int i=Udp.DATA; i<(p.len>>2); ++i) {
	Dbg.intVal(p.buf[i]);
}
*/
Dbg.lf();
			// and send it
stat[0]++;
stat[1] += p.len;
			Udp.build(p, dst_ip, BG_REQUEST);
			return true;
		}

		/**
		*	resend a packet.
		*/
		static void checkList() {

			if (!init) init();

			for (int i=0; i<MAX_OUTSTANDING; ++i) {
				MsgOut m = list[i];
				synchronized (monitor) {
					// find a waiting message
					if (m.cmd!=0 && m.cnt!=0) {
						int tim = Timer.us()-m.id;
						if (tim > TIMEOUT) {
							m.send();
							return;				// only one resend to not
												// fill up the ip buffer too
												// much
						}
					}
				}
			}
		}

		/**
		*	find the message to the reply and
		*	remove it from the list.
		*	Is there a race condition with checkList?
		*		checkList get's called from Comm-Thread
		*		inList from Udp/Net-Thread
		*/
		static boolean inList(Packet p) {

			if (!init) init();

			int cmd = p.buf[OFF_CMD]-1;
			int id = p.buf[OFF_ID];

			for (int i=0; i<MAX_OUTSTANDING; ++i) {
				MsgOut m = list[i];
				synchronized (monitor) {
					// find the waiting message
					if (m.cmd==cmd && m.cnt!=0) {
						if (id==m.id) {
							m.setFree();
							return true;
						}
					}
				}
			}

Dbg.wr("not found inList ");
Dbg.intVal(cmd);
Dbg.lf();

			p.setStatus(Packet.FREE);
			return false;
		}
	}
	// end MsgOut




/**
*	private because it's a singleton Thread.
*/
	private Comm(int prio, int us) {
		super(prio, us);
	}


	public static void init(int id, int prio, int period, LinkLayer ipl) {

		if (single != null) return;			// allready called init()

		bgid = id;

		Status.connOk = false;
		Status.download = false;
		Status.anmOk = false;
		Status.commErr = 0;

		ptim = 0;
		pingOut = false;
		rtim = 0;
		scheduleReset = false;

		ipLink = ipl;
		dst_ip = 0;						// as default

		MsgOut.init();

		//
		//	some statistics in HTML server
		//
		stat = new int[5];
		for (int i=0; i<stat.length; ++i) stat[i]=0;
		Html.setValArray(stat);

		//
		//	start my own thread
		//
		single = new Comm(prio, period);

		//
		// generate UDP handlers as inner classes and add to as UDP server.
		//
		UdpHandler uh;
		uh = new UdpHandler() {
			public void request(Packet p) {
				rpl(p);
			}
		};
		Udp.addHandler(FDL_REPLY, uh);
		uh = new UdpHandler() {
			public void request(Packet p) {
				rqt(p);
			}
		};
		Udp.addHandler(FDL_REQUEST, uh);
	}


/**
*	Do the ping!
*/
	public void run() {

		int i, j;
		int val;

		for (;;) {
			waitForNextPeriod();

			// resend outstanding packets
			MsgOut.checkList();

			if (ipLink.getIpAddress()==0) {
				continue;
			}

			if (scheduleReset) {
				Dbg.wr('$');
				++rtim;
				if (rtim==20) {
					Main.reset = true;
				}
			}


			if (Status.connOk) {
				++ptim;
				if (ptim==PING_PERIOD) {
					if (pingOut) {					// last ping not replayed
						timeout();
					}
					ptim = 0;
					ping();
				}
			}

		}
	}

	public static void startConnection(int dstIp, StringBuffer[] connStr) {

		dst_ip = dstIp;
		ipLink.startConnection(connStr[0], connStr[1], connStr[2], connStr[3]);
	}

	public static void connect() {

		MsgOut m = MsgOut.getFree(Cmd.CONN);
		if (m!=null) {
			m.data[0] = (Main.VER_MAJ<<16)+Main.VER_MIN;	// SW Ver.
			m.data[1] = Flash.getVer();
			m.len = 2;
			Status.connOk = false;
			Status.download = false;
			m.send();
		}
Dbg.wr('#');
	}

	public static void anmelden(int art, int zugnr, int strnr, int melnr) {

		MsgOut m = MsgOut.getFree(Cmd.ANM);
		if (m!=null) {
			m.data[0] = art;			// art 1
			m.data[1] = zugnr;
			m.data[2] = strnr;
			m.data[3] = melnr;
			m.len = 4;
			m.send();
		}
	}

	public static void verschub(int strnr, int melnr) {

		MsgOut m = MsgOut.getFree(Cmd.VERSCHUB);
		if (m!=null) {
			m.data[0] = strnr;
			m.data[1] = melnr;
			m.len = 2;
			m.send();
		}
	}

	public static void lern(int strnr, int melnr, int lat, int lon) {

		MsgOut m = MsgOut.getFree(Cmd.LERN);
		if (m!=null) {
			m.data[0] = strnr;
			m.data[1] = melnr;
			m.data[2] = lat;
			m.data[3] = lon;
			m.len = 4;
			m.send();
		}
	}

	public static void alarm(int strnr, int melnr, int alarmType) {

		MsgOut m = MsgOut.getFree(Cmd.ALARM);
		if (m!=null) {
			m.data[0] = strnr;
			m.data[1] = melnr;
			m.data[2] = alarmType;
			m.len = 3;
			m.send();
		}
	}
	public static void gpsStatus(int gpsInfo, int lat, int lon) {

		MsgOut m = MsgOut.getFree(Cmd.GPS_INFO);
		if (m!=null) {
			m.data[0] = Status.strNr;
			m.data[1] = Status.melNr;
			m.data[2] = gpsInfo;
			m.data[3] = lat;
			m.data[4] = lon;
			m.len = 5;
			m.send();
		}
	}

	public static void charlyStatus(int cmd, int val) {

		MsgOut m = MsgOut.getFree(cmd);
		if (m!=null) {
			m.data[0] = Status.strNr;
			m.data[1] = Status.melNr;
			m.data[2] = val;
			m.len = 3;
			m.send();
		}
	}

	public static void melnrCmd(int cmd, int strnr, int melnr) {

		MsgOut m = MsgOut.getFree(cmd);
		if (m!=null) {
			m.data[0] = strnr;
			m.data[1] = melnr;
			m.len = 2;
			m.send();
		}
	}

	public static void simpleCmd(int cmd) {

		MsgOut m = MsgOut.getFree(cmd);
		if (m!=null) {
			m.len = 0;
			m.send();
		}
	}

/* not used for now
	private static Packet head(int cmd) {

		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (p == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return null;
		}
	
		p.buf[OFF_CMD] = cmd;
		p.buf[OFF_ID] = Timer.us();		// id is timer value
		p.buf[OFF_BGID] = bgid;			// bg263 serial number

		return p;
	}
*/












/***************************************************************/
	private static void ping() {

		MsgOut m = MsgOut.getFree(Cmd.PING);
		if (m!=null) {
			m.data[0] = 0xcafe;		// magic number
			m.len = 1;
			m.send();
			pingOut = true;
		}
Dbg.wr('.');
	}




	public static void mel(int strnr, int melnr) {

		MsgOut m = MsgOut.getFree(Cmd.MLR);
		if (m!=null) {
			m.data[0] = strnr;
			m.data[1] = melnr;
			m.len = 2;
			m.send();
		}
/*
Object o = new Object();
synchronized(o) {
Dbg.wr('\n');
for (int i=0; i<1024; ++i) {
Dbg.intVal(com.jopdesign.sys.Native.rdMem(i));
}
}
*/
	}


	/**
	*	request from FDL.
	*/
	// private static void rqt(Packet p) {
	public static void rqt(Packet p) {

		if (!checkPkt(p)) return;
		int nr = p.buf[Udp.DATA];
Dbg.wr("got cmd ");
Dbg.intVal(p.buf[Udp.DATA]);
/*
for (int i=Udp.DATA; i<(p.len>>2); ++i) {
	Dbg.intVal(p.buf[i]);
}
*/
Dbg.lf();

		if (nr==Cmd.PING) {
			sendPingReply(p);
		} else if (nr==Cmd.DGPS) {
			Gps.dgps(p);
			sendDgpsReply(p);
		} else if (nr==Cmd.SWVER) {
			sendSwverReply(p);
		} else if (nr==Cmd.RESET) {
			sendSimpleReply(p);
			scheduleReset = true;
		} else if (nr==Cmd.ANMOK) {
			Status.anmOk = true;
			sendReply(p);
		} else if (nr==Cmd.FLAN) {
			Status.melNrZiel = p.buf[OFF_DATA+1];
			Status.state = Status.ANGABE;
			sendReply(p);
		} else if (nr==Cmd.FERL) {
			Status.melNrStart = Status.melNr;
			Status.melNrZiel = p.buf[OFF_DATA+1];
			Status.state = Status.ERLAUBNIS;
			Status.sendFerlQuit = true;
			sendReply(p);
		} else if (nr==Cmd.FWR) {
			Status.melNrZiel = Status.melNr;
			Status.state = Status.WIDERRUF;
			sendReply(p);
		} else if (nr==Cmd.ABM) {
			Status.state = Status.ABGEMELDET;
			Status.anmOk = false;
			sendReply(p);
		} else if (nr==Cmd.NOT) {
			Status.state = Status.NOTHALT;
			sendReply(p);
		} else if (nr==Cmd.DLSTAT) {
			Status.dlType = p.buf[OFF_DATA];
			Status.dlPercent = p.buf[OFF_DATA+1];
			sendSimpleReply(p);
		} else {
			Dbg.wr("unknown cmd\n");
		}
		p.setStatus(Packet.FREE);
	}


	/**
	*	reply from FDL.
	*/
	// private static void rpl(Packet p) {
	public static void rpl(Packet p) {

		if (!checkPkt(p)) return;
		if (!MsgOut.inList(p)) return;

		int nr = p.buf[Udp.DATA];
Dbg.wr("got reply ");
Dbg.intVal(nr);
Dbg.lf();
		if (nr==Cmd.PING_RPL) {
			rcvPingReply(p);
		} else if (nr==Cmd.CONN_RPL) {
			// first set dl status and then connOk!
			if (p.buf[OFF_DATA]!=0) Status.download = true;
			Status.dlType = -1;
			Status.dlPercent = -1;
			Status.connOk = true;

		} else if (nr==Cmd.LERN_RPL) {
			Status.lernOk = true;
		} else if (nr==Cmd.VERSCHUB_RPL) {
			Status.von = p.buf[OFF_DATA+1];
			Status.bis = p.buf[OFF_DATA+2];
		} else if (nr==Cmd.ANM_RPL || nr==Cmd.MLR_RPL) {
			;
		} else if (nr==Cmd.ANK_RPL) {
			Status.ankunftOk = true;
		} else if (nr==Cmd.VERL_RPL) {
			Status.verlassenOk = true;
		} else {
			// just ignore it 
		}
		p.setStatus(Packet.FREE);
	}

	private static boolean checkPkt(Packet p) {

stat[2]++;
stat[3] += p.len;
		if (p.len < ((OFF_DATA)<<2)) {		// minimum length
			Status.commErr = 2;
			Dbg.wr('w');
			Dbg.wr('l');
			Dbg.intVal(p.len);
			Dbg.wr('\n');
			p.setStatus(Packet.FREE);		// mark packet free
stat[4]++;
			return false;
		}
		if (p.buf[OFF_CMD]<Cmd.FIRST_CMD || p.buf[OFF_CMD]>Cmd.LAST_CMD) {
//			Status.commErr = 3;
			Dbg.wr('w');
			Dbg.wr('c');
			Dbg.intVal(p.buf[OFF_CMD]);
			Dbg.wr('\n');
			p.setStatus(Packet.FREE);		// mark packet free
stat[4]++;
			return false;
		}
		if (p.buf[OFF_BGID] != bgid) {		// check bgid
			Status.commErr = 4;
			Dbg.wr('w');
			Dbg.wr('i');
			Dbg.intVal(p.buf[OFF_BGID]);
			Dbg.wr('\n');
			p.setStatus(Packet.FREE);		// mark packet free
stat[4]++;
			return false;
		}

		return true;
	}


	/**
	*	a simple reply (without additional data)
	*/
	static void sendReply(Packet p) {

		// get a free packet and set source to LinkLayer
		Packet np = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (np == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}
		np.len = (OFF_DATA)<<2;
		np.buf[OFF_CMD] = p.buf[OFF_CMD]+1;	// reply
		np.buf[OFF_ID] = p.buf[OFF_ID];		// copy id
		np.buf[OFF_BGID] = bgid;			// bg263 serial number
stat[0]++;
stat[1] += np.len;
		Udp.build(np, dst_ip, BG_REPLY);
Dbg.wr("send reply ");
Dbg.intVal(np.buf[OFF_CMD]);
Dbg.lf();


p.setStatus(Packet.FREE);		// mark packet free
// we could reuse p now with changed Udp.java
	}

	public static void sendPingReply(Packet p) {

		// get a free packet and set source to LinkLayer
		Packet np = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (np == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}
		np.len = p.len;
		// TODO I don't like this packet copy
		// Udp swaps fields, TcpIp swaps ip fields and sends it back
		// when p.len != 0.
		// should change this
		for (int i=0; i<p.len>>2; ++i) {
			np.buf[i] = p.buf[i];
		}
		np.buf[OFF_CMD] = Cmd.PING_RPL;		// reply
		np.buf[OFF_BGID] = bgid;			// bg263 serial number
stat[0]++;
stat[1] += np.len;
		Udp.build(np, dst_ip, BG_REPLY);
Dbg.wr("send reply ");
Dbg.intVal(np.buf[OFF_CMD]);
Dbg.lf();

p.setStatus(Packet.FREE);		// mark packet free
// we could reuse p now with changed Udp.java
	}

	public static void sendDgpsReply(Packet p) {

		// get a free packet and set source to LinkLayer
		Packet np = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (np == null) {					// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}

		np.len = (OFF_DATA+3)<<2;
		np.buf[OFF_CMD] = Cmd.DGPS_RPL;		// reply
		np.buf[OFF_ID] = p.buf[OFF_ID];		// copy id
		np.buf[OFF_BGID] = bgid;			// bg263 serial number
		np.buf[OFF_DATA] = Gps.last_lat;
		np.buf[OFF_DATA+1] = Gps.last_lon;
		np.buf[OFF_DATA+2] = Status.melNr;
stat[0]++;
stat[1] += np.len;
		Udp.build(np, dst_ip, BG_REPLY);
p.setStatus(Packet.FREE);		// mark packet free
// we could reuse p now with changed Udp.java
	}

	public static void sendSwverReply(Packet p) {

		// get a free packet and set source to LinkLayer
		Packet np = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (np == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}
		np.len = (OFF_DATA+1)<<2;
		np.buf[OFF_CMD] = Cmd.SWVER_RPL;	// reply
		np.buf[OFF_ID] = p.buf[OFF_ID];		// copy id
		np.buf[OFF_BGID] = bgid;			// bg263 serial number
		np.buf[OFF_DATA] = (Main.VER_MAJ<<16)+Main.VER_MIN;
stat[0]++;
stat[1] += np.len;
		Udp.build(np, dst_ip, BG_REPLY);
Dbg.wr("send reply ");
Dbg.intVal(np.buf[OFF_CMD]);
Dbg.lf();
p.setStatus(Packet.FREE);		// mark packet free
// we could reuse p now with changed Udp.java
	}

	/**
	*	Send a simple reply without any user data.
	*	Just increment CMD from original packet to get the RPL number.
	*/
	public static void sendSimpleReply(Packet p) {

		// get a free packet and set source to LinkLayer
		Packet np = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (np == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}
		np.len = (OFF_DATA)<<2;
		np.buf[OFF_CMD] = p.buf[OFF_CMD]+1;	// reply
		np.buf[OFF_ID] = p.buf[OFF_ID];		// copy id
		np.buf[OFF_BGID] = bgid;			// bg263 serial number
stat[0]++;
stat[1] += np.len;
		Udp.build(np, dst_ip, BG_REPLY);
Dbg.wr("send reply ");
Dbg.intVal(np.buf[OFF_CMD]);
Dbg.lf();
p.setStatus(Packet.FREE);		// mark packet free
// we could reuse p now with changed Udp.java
	}

	public static void sendAnmOkReply(Packet p) {

		// get a free packet and set source to LinkLayer
		Packet np = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (np == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}
		np.len = (OFF_DATA)<<2;
		np.buf[OFF_CMD] = Cmd.ANMOK_RPL;	// reply
		np.buf[OFF_ID] = p.buf[OFF_ID];		// copy id
		np.buf[OFF_BGID] = bgid;			// bg263 serial number
stat[0]++;
stat[1] += np.len;
		Udp.build(np, dst_ip, BG_REPLY);

Dbg.wr("send reply ");
Dbg.intVal(np.buf[OFF_CMD]);
Dbg.lf();
		Status.anmOk = true;
p.setStatus(Packet.FREE);		// mark packet free
// we could reuse p now with changed Udp.java
	}
/**
*	received reply to ping.
*/
	public static void rcvPingReply(Packet p) {

		if (p.len != ((OFF_DATA+1)<<2)) {
			Dbg.wr('x');
			return;
		}

		int tim = Timer.us()-p.buf[Udp.DATA+1];
		tim /= 1000;		// in ms

		if (p.buf[OFF_DATA] != 0xcafe) {		// check magic
			Dbg.wr('m');
			return;
		}
		Dbg.wr("r ");
		Dbg.intVal(tim);
		Dbg.wr("ms ");
		pingOut = false;

		// upper level frees buffer
	}


	static void timeout() {

		Dbg.wr('.');
		Dbg.wr('t');
		Dbg.wr(' ');
		pingOut = false;
	}

}
