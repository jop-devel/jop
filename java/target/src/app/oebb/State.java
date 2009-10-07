/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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

/**
 * 
 */
package oebb;

import util.Dbg;
import util.Timer;
import ejip.Ejip;
import ejip.LinkLayer;
import ejip.Net;
import ejip.Packet;
import ejip.Udp;

/**
 * Contains the state of the BG. Will substitute the class Status.
 * 
 * @author martin
 * 
 */
public class State implements ejip.UdpHandler, Runnable {

	// State of the system - exchanged periodically
	int bgid;
	int date;
	int time;
	int strnr;
	int zugnr;
	private volatile int pos;	// set in Gps.checkStrMelnr()
	int start;
	int end;
	int startNF;
	int ankVerl;
	volatile int type;	// changed at anmelden, on Verschub
	int alarmFlags;
	int cmdAck;
	int versionStrecke;
	// set in Gps.process()
	volatile int gpsLat;
	volatile int gpsLong;

	final static int TYPE_UNKNOWN = 0;
	final static int TYPE_ZUG = 1;		// fix in menu
	final static int TYPE_NF = 2;		// fix in menu
	final static int TYPE_VERSCH = 3;
	final static int TYPE_LERN = 4;
	final static int TYPE_ES221 = 5;
	final static int TYPE_UPDATE = 6;
	final static int TYPE_INFO = 7;
	
	final static int BG_SND_PORT = 2004;
	final static int ZLB_RCV_PORT = 2005;
	
	int destIp;
	
	
	// Alarm and flags
	public static final int ALARM_UEBERF = 1;
	public static final int ALARM_RICHTUNG = 2;
	public static final int ALARM_FAEHRT = 3;
	public static final int ALARM_ES221 = 4;	// flag not used anymore
	public static final int FLAG_ANK = 5;
	public static final int FLAG_VERL = 6;
	public static final int FLAG_ZIEL = 7;
	public static final int ALARM_MLR = 8;
	public static final int FLAG_LERN = 9;
	
	private final static int AFLAG_ANK = 1<<(FLAG_ANK-1);
	private final static int AFLAG_VERL = 1<<(FLAG_VERL-1);
	private final static int AFLAG_ZIEL = 1<<(FLAG_ZIEL-1);
	private final static int AFLAG_LERN = 1<<(FLAG_LERN-1);
	
	private final static int ALARM_MSK = (1<<(ALARM_UEBERF-1)) |
	(1<<(ALARM_RICHTUNG-1)) | (1<<(ALARM_FAEHRT-1)) | (1<<(ALARM_ES221-1)) | (1<<(ALARM_MLR-1));
	
	// Meldungen von der Zentrale zum BG
	final static int CFLAG_ABM =	0x00000001;		// Abmelden
	final static int CFLAG_FWR =	0x00000002;		// Fahrtwiderruf
	final static int CFLAG_NOT =	0x00000004;		// Nothalt
	final static int CFLAG_ANMOK =	0x00000008;		// Anmelden OK
	final static int CFLAG_ZLB_INT= 0x00000010;		// used only internally by ZLB
	final static int CFLAG_IGNORE =	0x00000020;		// ignore message 
	final static int CFLAG_DOWNLOAD = 0x00000040;	// downloadind SW or Strecke 

	/**
	 * The alarm was ack by the TFZ. Set the flags to zero when
	 * FDL has also acked the alarm.
	 */
	private boolean alarmQuit;


	// local states
	/**
	 * Send the status all 5 seconds.
	 */
	final static int SEND_PERIOD = 5;
	/**
	 * Send timer
	 */
	int sendTimer;
	/**
	 * A recieved packet from the ZLB.
	 */
	Packet zlbMsg;
	/**
	 * Did we have a contact with the ZLB?
	 */
	boolean contactZLB;
	/**
	 * Timestamp of last ZLB message.
	 */
	int lastMsgTimestamp;
	/**
	 * Date of last message.
	 */
	int lastMsgDate;
	/**
	 * Get at least on message all 22 seconds.
	 */
	final static int ZLB_TIMEOUT = 22;
	/**
	 * Watch ZLB timer
	 */
	int zlbTimer;
	private LinkLayer ipLink;
	
	/**
	 * FWR was acknowledged by TFZF
	 */
	boolean fwrQuitPending;

	/**
	 * NOTHALT was acknowledged by TFZF
	 */
	boolean nothaltQuitPending;

	// save some fields and use static
	/**
	 * Ignore flag is set
	 */
	static boolean ignore;
	
	/**
	 * Do the reset as we got back a message after ABM
	 */
	static boolean forceReset;
	// reset not yet used - we have a reset function in Logic.
//	/**
//	 * RESET timer
//	 */
//	private static int rtim;
//	/**
//	 * reset pending
//	 */
//	private static boolean scheduleReset;
	/**
	 * some network statistics
	 */
	private int[] stat;

	// TODO: to many fields - we use statics here...
	private static Ejip ejip;
	private static Net net;
	
	public State(Ejip ejipRef, Net netRef, LinkLayer link) {
		ejip = ejipRef;
		net = netRef;

		ipLink = link;
//		setTimestamp(2001, 1, 1, 0, 0, 0, 1);

		sendTimer = Timer.getTimeoutSec(SEND_PERIOD);
		stat = new int[5];

		/*
		 * start = 1; end = 2; startNF = 3; ankVerl = 4; type = 5; alarmFlags =
		 * 2; cmdAck = 4; versionStrecke = 789; gpsLat = 6; gpsLong = 7;
		 */
	}

	public void setUDPData(int[] arr, int off) {

		arr[off] = bgid;
		arr[off + 1] = date;
		arr[off + 2] = time;
		arr[off + 3] = (strnr << 20) + zugnr;
		arr[off + 4] = (getPos() << 16) + start;
		arr[off + 5] = (end << 16) + startNF;
		int gpsFix = Gps.fix;
		if (gpsFix == -1)
			gpsFix = 0;
		// TODO: BgPpp.getConnType();
		arr[off + 6] = (ankVerl << 16) + (type << 13) + (gpsFix << 11);
		arr[off + 7] = alarmFlags;
		arr[off + 8] = cmdAck;
		arr[off + 9] = (((Main.VER_MAJ << 8) + Main.VER_MIN) << 16)
				+ versionStrecke;
		arr[off + 10] = gpsLat;
		arr[off + 11] = gpsLong;
	}
	
	static int getDate() {
		int d = Gps.getDate();
		return (((d%100) + 2000) << 16) + ((d/100%100) << 8) + d/10000%100;
		
	}
	
	static int getTime() {
		int t = Gps.getTime();
		return (t/10000000%100 << (32 - 6)) + (t/100000%100 << (32 - 12))
			+ (t/1000%100 << (32 - 18)) + (t%1000 << 32 - 28);		
	}

	static int cnt;
	
	boolean send() {

		// get an IP packet
		Packet p = ejip.getFreePacket(ipLink);
		if (p == null) { // got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return false;
		}

		date = getDate();
		time = getTime();

		setUDPData(p.buf, Udp.DATA);
		p.len = (Udp.DATA + 12) << 2;
		
		Dbg.wr("BG:  ");
		printMsg(p);

		// and send it
		net.getUdp().build(p, destIp, BG_SND_PORT);
		return true;
	}

	/**
	 * Get one UDP status packet from the ZLB.
	 * Just store it zlbMsg for further handling in run().
	 */
	public void request(Packet p) {

		System.out.println("ZLB pkt");
		// just store the received package
		synchronized(this) {
			if (zlbMsg==null) {
				zlbMsg = p;
			} else {
				// this should not happen
				ejip.returnPacket(p);
			}
		}
	}
	
	/**
	 * Handle a message received from the ZLB.
	 * zlbMsg point to the received package
	 * 
	 */
	private void handleMsg() {
		
		Packet p;
		synchronized (this) {
			p = zlbMsg;			
			zlbMsg = null;
		}
		
		if (!checkPkt(p)) {
			ejip.returnPacket(p);
			return;
		}
		int date = p.buf[Udp.DATA + 1];
		int time = p.buf[Udp.DATA + 2];

		if (!contactZLB) {
			lastMsgDate = date;
			lastMsgTimestamp = time;
			contactZLB = true;
		} else {
			// check date and time of the message
			if (date>lastMsgDate || (date==lastMsgDate && time>lastMsgTimestamp)) {
				lastMsgDate = date;			
				lastMsgTimestamp = time;
			} else {
				Main.logger.print("Msg. too old");
				// it's a too old packet
				ejip.returnPacket(p);
				return;
			}			
		}

		
		int[] buf = p.buf;
		// extract data

		// fist set cmd as it is used in isDownloading()
		int cmd = buf[Udp.DATA+8];

		int strZugnr = buf[Udp.DATA + 3];
		// use data from ZLB if not yet set - synchronization if restarted
		// but only if we are not in download check!
		if (Logic.state!=Logic.DL_CHECK && !isDownloading() && !stickyDl &&
				(strZugnr & 0xfffff)!=0) {
			if (strnr==0) {
				// that one will probably never happen
				strnr = strZugnr >> 20;
			}
			if (zugnr==0) {
				zugnr = strZugnr & 0xfffff;
			}
			if (type==TYPE_UNKNOWN) {
				type = (buf[Udp.DATA+6]&0xffff)>>>13;
			}			
		}
		

		// first set ignore flag, then connection state
		ignore = (cmd & CFLAG_IGNORE)!=0;
		// reset ZLB timeout
		zlbTimer = Timer.getTimeoutSec(ZLB_TIMEOUT);
		Status.connOk = true;

		// ack just this flag, but ignore the rest
		// work-around: also Ack the download flag
		if (ignore) {
			cmdAck |= CFLAG_IGNORE;
			// set the download flag
			cmdAck &= ~CFLAG_DOWNLOAD;
			cmdAck |= cmd & CFLAG_DOWNLOAD;
			Dbg.wr("ZLB ignored");
			Dbg.lf();
			ejip.returnPacket(p);
			return;
		}


		if (cmd!=cmdAck) {
			// send the ack
			requestSend();
		}

		
		// type
		int val = (buf[Udp.DATA+6]&0xffff)>>>13;
		// all state update synchronized
		synchronized (this) {
			// update when Verschub
			if (val==TYPE_VERSCH) {
				type = val;
			}
			// update ack with cmd as default action
			cmdAck = cmd;
			// but keep some when not acked from TFZF or Logic
	
			// Abmelden
			if ((cmd & CFLAG_ABM)!=0) {
				Logic.state = Logic.ABGEMELDET;
			}
			// Hack for Charlys Abmelden issue:
			// don't reset the ABM flag when reset is pending
			if ((cmd & CFLAG_ABM)==0 && Logic.state==Logic.ABGEMELDET) {
				cmdAck |= CFLAG_ABM;
				forceReset = true;
			}
			

			// Angemeldet
			if ((cmd & CFLAG_ANMOK)!=0) {
				Events.anmeldenOk = true;
			}

			// FWR
			if ((cmd & CFLAG_FWR)!=0) {
				if (!fwrQuitPending) {
					Logic.state = Logic.WIDERRUF;
					// we cannot ack it, reset the flag
					cmdAck &= ~CFLAG_FWR;
				}
			} else {
				// reset pending when FWR flag was reset by ZLB
				fwrQuitPending = false;
			}

			// NOTHALT
			if ((cmd & CFLAG_NOT)!=0) {
				if (!nothaltQuitPending) {
					Logic.state = Logic.NOTHALT;
					// we cannot ack it, reset the flag
					cmdAck &= ~CFLAG_NOT;
				}
			} else {
				// reset pending when NOT flag was reset by ZLB
				nothaltQuitPending = false;
			}
		}
		
		// Alarm and flag quits
		int alarmAck = buf[Udp.DATA+7];
		synchronized (this) {
			// we can reset some of the flags here
			// check them individual as Charly does not
			// always ack the whole flag field - but he should
			if ((alarmAck & AFLAG_ANK)!=0) {
				alarmFlags &= ~AFLAG_ANK;
				ankVerl = 0;
			}
			if ((alarmAck & AFLAG_VERL)!=0) {
				alarmFlags &= ~AFLAG_VERL;
				ankVerl = 0;
			}
			if ((alarmAck & AFLAG_ZIEL)!=0) {
				alarmFlags &= ~AFLAG_ZIEL;
			}
			if ((alarmAck & AFLAG_LERN)!=0) {
				alarmFlags &= ~AFLAG_LERN;
				Status.lernOk = true;
			}
			if ((alarmAck&ALARM_MSK)==(alarmFlags&ALARM_MSK)) {
				// Alarm has been reset by FDL and seen by ZLB
				// we can reset it in the flags
				if (alarmQuit) {
					alarmFlags &= ~ALARM_MSK;
					alarmQuit = false;
				}
			}
		}

		boolean ferlChanged = false;
		val = buf[Udp.DATA+5]>>>16;
		if (val!=end) ferlChanged = true;
		val = buf[Udp.DATA+5]&0xffff;
		if (val!=startNF) ferlChanged = true;
		val = buf[Udp.DATA+4]&0xffff;
		if (val!=start) ferlChanged = true;
		
		// logging
		if (ferlChanged) {
			Main.logger.printSmall("Ferl changed, Logic.state=", Logic.state);
			Main.logger.printSmall("From ", buf[Udp.DATA+4]&0xffff);
			Main.logger.printSmall("To " , buf[Udp.DATA+5]>>>16);
		}
		
		
		if (val!=0 && ferlChanged && (Logic.state==Logic.ANM_OK || Logic.state==Logic.ZIEL
				|| Logic.state==Logic.NOTHALT_OK || Logic.state==Logic.ERLAUBNIS)) {
			Main.logger.printSmall("new ferl accepted with state check, from=", buf[Udp.DATA+4]&0xffff);			
		}
		// For a test accept FERL in any Logic.state
		if (val!=0 && ferlChanged) {
			// this is now a FERL event and we accept the change
			synchronized (this) {
				start = buf[Udp.DATA+4]&0xffff;
				end = buf[Udp.DATA+5]>>>16;
				startNF = buf[Udp.DATA+5]&0xffff;
			}
			Main.logger.printSmall("new ferl accepted, from=", start);
			synchronized (Status.dirMutex) {
				// let Logik.check() update the direction
				Status.direction = Gps.DIR_UNKNOWN;
			}
			Dbg.wr("set FERL");
			Logic.state = Logic.ERLAUBNIS;			
		}


		Dbg.wr("ZLB: ");
		printMsg(p);
		ejip.returnPacket(p);
	}

	/**
	 * Some sanity checks on the received packet.
	 * @param p
	 * @return
	 */
	private boolean checkPkt(Packet p) {

		stat[2]++;
		stat[3] += p.len;
		if (p.len != ((Udp.DATA+9)*4)) {		// fix length
			// Status.commErr = Logic.COMM_SHORT;
			Dbg.wr("wrong length");
			Dbg.intVal(p.len);
			Dbg.lf();
			Main.logger.printHex("wrong length", p.len);
			stat[4]++;
			return false;
		}
		if (p.buf[Udp.DATA + 0] != bgid) {
			// Status.commErr = Logic.COMM_WRBGID;
			Dbg.wr("wrong bgid");
			Dbg.intVal(p.buf[Udp.DATA+0]);
			Dbg.lf();
			Main.logger.printHex("wrong bgid", p.buf[Udp.DATA+0]);
			return false;
		}
		return true;
	}
	
	/**
	 * Debug output of a message.
	 * @param p
	 */
	private void printMsg(Packet p) {
		int[] buf = p.buf;
		int i;
		Dbg.wr("date=");
		i = p.buf[Udp.DATA+1];
		Dbg.intVal(i>>>16);
		Dbg.intVal((i>>8) & 0xff);
		Dbg.intVal(i & 0xff);
		Dbg.wr("time=");
		i = p.buf[Udp.DATA+2];
		Dbg.intVal((i>>26)&0x3f);
		Dbg.intVal((i>>20)&0x3f);
		Dbg.intVal((i>>14)&0x3f);
		Dbg.intVal(i&0x3ff);
		Dbg.wr("strnr=");
		Dbg.intVal(buf[Udp.DATA+3]>>>20);
		Dbg.wr("zugnr=");
		Dbg.intVal(buf[Udp.DATA+3]&0xfffff);
		Dbg.wr("pos=");
		Dbg.intVal(buf[Udp.DATA+4]>>>16);
		Dbg.wr("start=");
		Dbg.intVal(buf[Udp.DATA+4]&0xffff);
		Dbg.wr("end=");
		Dbg.intVal(buf[Udp.DATA+5]>>>16);
		Dbg.wr("NFstart=");
		Dbg.intVal(buf[Udp.DATA+5]&0xffff);
		Dbg.wr("ankVerl=");
		Dbg.intVal(buf[Udp.DATA+6]>>>16);
		Dbg.wr("type=");
		Dbg.intVal((buf[Udp.DATA+6]&0xffff)>>>13);
		Dbg.wr("alarm=");
		Dbg.hexVal(buf[Udp.DATA+7]);
		Dbg.wr("cmd=");
		Dbg.hexVal(buf[Udp.DATA+8]);
		
		Dbg.lf();
	}

	/**
	 * Periodically invoked to send a status message or handle
	 * an incoming message. Check if ZLB and
	 * connection is still alive.
	 */
	public void run() {

		// a hack for a lost bgid in the Flash
		if ((bgid==-1 || bgid==0) && Gps.ok()) {
			bgid = (getDate()<<16) + (getTime()>>>16);
			Dbg.wr("bgid is -1/0 => set a new one");
			Dbg.lf();
			Main.tftpHandler.programBgid(bgid);
			bgid = Flash.getId();
			Main.logger.printHex("bgid is -1/0 set to", bgid);
			// enough done this round
			return;
		}
		
		if (Timer.timeout(sendTimer)) {
			sendTimer = Timer.getTimeoutSec(SEND_PERIOD);
			if (ipLink.getIpAddress()!=0 && destIp!=0) {
				send();				
			}
		} else {
			if (zlbMsg!=null) {
				handleMsg();
			}
		}
		if (!Status.connOk) {
			// reset ZLB timeout
			zlbTimer = Timer.getTimeoutSec(ZLB_TIMEOUT);
		} else if (Timer.timeout(zlbTimer)) {
			// trigger a reconnect and display error
			// force a reconnect in Ppp-Modem
			ipLink.reconnect();
			Status.connOk = false;
			Status.commErr = Logic.COMM_FDLERR;
			Main.logger.print("connection lost");
			Dbg.wr("connection lost");
		}
		
		// not yet used
//		if (scheduleReset) {
//			Dbg.wr('$');
//			++rtim;
//			if (rtim==20) {
//				Main.reset = true;
//			}
//		}

	}

	/**
	 * Request to send a message immediately.
	 */
	public void requestSend() {
		sendTimer = Timer.getTimeoutSec(0);
	}

	public void sendZiel() {
		synchronized (this) {
			alarmFlags |= AFLAG_ZIEL;			
		}
		requestSend();
		
	}

	public boolean isVerschub() {
		
		return type==TYPE_VERSCH;
	}
	
	/**
	 * Set alarm flag
	 * @param alarmType
	 */
	public void setAlarm(int alarmType) {
		
		if (alarmType==0) {
			alarmQuit = true;
		} else {
			alarmFlags |= 1<<(alarmType-1);			
		}

		requestSend();
		
	}

	/**
	 * Quit FWR flag after Enter from TFZF
	 */
	public void fwrQuit() {
		fwrQuitPending = true;
		requestSend();
	}

	/**
	 * Quit NOT flag after Enter from TFZF
	 */
	public void nothaltQuit() {
		nothaltQuitPending = true;
		requestSend();
	}

	/**
	 * Flag Ankunft at position melnr
	 * @param melnr
	 */
	public void ankunft(int melnr) {
		synchronized(this) {
			ankVerl = melnr;
			alarmFlags |= AFLAG_ANK;
			// reset a pending verl
			if ((alarmFlags & AFLAG_VERL)!=0) {
				alarmFlags &= ~AFLAG_VERL;
			}
		}
		requestSend();
	}

	/**
	 * Flag Verlassen at position melnr
	 * @param melnr
	 */
	public void verlassen(int melnr) {
		synchronized(this) {
			ankVerl = melnr;
			alarmFlags |= AFLAG_VERL;
			// reset a pending verl
			if ((alarmFlags & AFLAG_ANK)!=0) {
				alarmFlags &= ~AFLAG_ANK;
			}
		}
		requestSend();
	}
	
	/**
	 * Got ack for ANK from ZLB
	 * @return
	 */
	public boolean ankuftAck() {
		return (alarmFlags & AFLAG_ANK) == 0;
	}

	/**
	 * Got ack for VERL from ZLB
	 * @return
	 */
	public boolean verlassenAck() {
		return (alarmFlags & AFLAG_VERL) == 0;
	}
	
	public boolean isDownloading() {
		return (cmdAck & CFLAG_DOWNLOAD) != 0;
	}
	
	boolean stickyDl;
	
	public boolean isDownloadSticky() {
		if ((cmdAck & CFLAG_DOWNLOAD)!=0) {
			stickyDl = true;
		}
		return stickyDl;
	}

	public void setInfo() {
		type = TYPE_INFO;
	}

	public void resetInfo() {
		type = TYPE_UNKNOWN;
	}

	public void setLern() {
		type = TYPE_LERN;
	}

	public void resetLern() {
		type = TYPE_UNKNOWN;		
	}

	public void lern(int melnr, int latAvg, int lonAvg) {
		
		synchronized (this) {
			pos = melnr;
			gpsLat = latAvg;
			gpsLong = lonAvg;
			alarmFlags |= AFLAG_LERN;
		}
		
		// TODO Auto-generated method stub
		
	}

	// flag not used anymore
	public void setESAlarm() {
		type = TYPE_ES221;
		// setAlarm(ALARM_ES221);
	}

	void setPos(int pos) {
		int oldPos = this.pos;
		this.pos = pos;
		// trigger actions on position change
		if (pos != oldPos) {
			requestSend();
			Main.logic.posChanged();
		}
	}

	int getPos() {
		return pos;
	}

	public void loop() {
		// do nothing
	}

}
