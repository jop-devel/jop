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
import ejip.LinkLayer;
import ejip.Packet;
import ejip.Udp;

/**
 * Contains the state of the BG. Will substitute the class Status.
 * 
 * @author martin
 * 
 */
public class State extends ejip.UdpHandler implements Runnable {

	// State of the system - exchanged periodically
	int bgid;
	int date;
	int time;
	int strnr;
	int zugnr;
	volatile int pos;	// set in Gps.checkStrMelnr()
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
	
	// Alarme vom BG zur Zentrale
	final static int AFLAG_MLR =		0x00000001; // Melderaum ueberfahren
	final static int AFLAG_RICHTUNG =	0x00000002;	// Falsche Richtung
	final static int AFLAG_FERL =		0x00000004;	// Keine Fahrerlaubnis
	final static int AFLAG_ES221 =		0x00000008;	// ES221 Mode
	final static int AFLAG_NOTQUIT =	0x00000010;	// NOTHALT Quit
	final static int AFLAG_ZIEL =		0x00000020;	// Ziel Erreicht
	final static int AFLAG_ANK =		0x00000040;	// Ankunft
	final static int AFLAG_VERL =		0x00000080;	// Verlassen
	
	// Meldungen von der Zentrale zum BG
	final static int CFLAG_ABM =	0x00000001;		// Abmelden
	final static int CFLAG_FWR =	0x00000002;		// Fahrtwiderruf
	final static int CFLAG_NOT =	0x00000004;		// Nothalt
	final static int CFLAG_ANMOK =	0x00000008;		// Anmelden OK


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
	 * Get at least on message all 17 seconds.
	 */
	final static int ZLB_TIMEOUT = 17;
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

	/**
	 * some network statistics
	 */
	private int[] stat;

	public State(LinkLayer link) {

		ipLink = link;
		setTimestamp(2001, 1, 1, 0, 0, 0, 0);

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
		arr[off + 4] = (pos << 16) + start;
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

	public void setTimestamp(int year, int month, int day, int hour,
			int minute, int second, int milli) {

		date = (year << 16) + (month << 8) + day;
		time = (hour << (32 - 6)) + (minute << (32 - 12))
				+ (second << (32 - 18)) + (milli << 32 - 28);
	}

	public void setTimestamp(int date, int time) {

		this.date = date;
		this.time = time;
		// increment millis
		this.time += (1 << 32 - 28);
	}

	boolean send() {

		// get an IP packet
		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (p == null) { // got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return false;
		}

		setUDPData(p.buf, Udp.DATA);
		p.len = (Udp.DATA + 12) << 2;
		
		Dbg.wr("BG:  ");
		printMsg(p);

		// and send it
		Udp.build(p, destIp, BG_SND_PORT);
		return true;
	}

	/**
	 * Get one UDP status packet from the ZLB.
	 * Just store it zlbMsg for further handling in run().
	 */
	public void request(Packet p) {

		System.out.println("ZLB pkt");
		// just store the received package
		p.setStatus(Packet.ALLOC);
		synchronized(this) {
			if (zlbMsg==null) {
				zlbMsg = p;
			} else {
				// this should not happen
				p.setStatus(Packet.FREE);
			}
		}
	}
	
	/**
	 * Handle a message received from the ZLB.
	 * zlbMsg point to the received package
	 * 
	 * TODO: check timestamp
	 */
	private void handleMsg() {
		
		Packet p;
		synchronized (this) {
			p = zlbMsg;			
			zlbMsg = null;
		}
		
		if (!checkPkt(p)) {
			p.setStatus(Packet.FREE);
			return;
		}
		int date = p.buf[Udp.DATA + 1];
		int time = p.buf[Udp.DATA + 2];

		if (!contactZLB) {
			lastMsgTimestamp = time;
			contactZLB = true;
		}
		if (time - lastMsgTimestamp < 0) {
			// it's a too old packet
			p.setStatus(Packet.FREE);
			return;
		}
		// reset ZLB timeout
		zlbTimer = Timer.getTimeoutSec(ZLB_TIMEOUT);
		Status.connOk = true;

		// just now - use the ZLB time for our message timing
		setTimestamp(date, time);
		
		int[] buf = p.buf;
		// extract data
		int strPos = buf[Udp.DATA + 3];
		int str = strPos >> 20;
		int zugnr = strPos & 0xfffff;
		
		int cmd = buf[Udp.DATA+8];
		if (cmd!=cmdAck) {
			// TODO check a cmd change
			
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
			// update ack with cmd
			cmdAck = cmd;
			
			// FWR
			if ((cmd & CFLAG_FWR)!=0) {
				if (!fwrQuitPending) {
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
					// we cannot ack it, reset the flag
					cmdAck &= ~CFLAG_NOT;
				}
			} else {
				// reset pending when NOT flag was reset by ZLB
				nothaltQuitPending = false;
			}
		}
		
		// Alarm flag quits
		int alarmAck = buf[Udp.DATA+7];
		synchronized (this) {
			if (alarmAck==alarmFlags) {
				// we can reset some of the flags here
				if ((alarmAck & AFLAG_ANK)!=0) {
					alarmFlags &= ~AFLAG_ANK;
					ankVerl = 0;
				}
				if ((alarmAck & AFLAG_VERL)!=0) {
					alarmFlags &= ~AFLAG_VERL;
					ankVerl = 0;
				}
			}
		}

		// TODO: any sanity checks? action on change of start/end?
		synchronized (this) {
			start = buf[Udp.DATA+4]&0xffff;
			end = buf[Udp.DATA+5]>>>16;
			startNF = buf[Udp.DATA+5]&0xffff;
		}
		if (start!=0 && (Logic.state==Logic.ANM_OK || Logic.state==Logic.ZIEL)) {
			// this is now a FERL event
			synchronized (Status.dirMutex) {
				// let Logik.check() update the direction
				Status.direction = Gps.DIR_UNKNOWN;
			}
			Logic.state = Logic.ERLAUBNIS;			
		}


		Dbg.wr("ZLB: ");
		printMsg(p);
		p.setStatus(Packet.FREE);
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
			Status.commErr = 2;
			Dbg.wr("wl");
			Dbg.intVal(p.len);
			Dbg.wr('\n');
			stat[4]++;
			return false;
		}
		if (p.buf[Udp.DATA + 0] != bgid) {
			Status.commErr = 4;
			Dbg.wr('w');
			Dbg.wr('i');
			Dbg.intVal(p.buf[Udp.DATA+0]);
			System.out.println("wrong bgid");
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
		Dbg.wr("seconds=");
		Dbg.intVal((p.buf[Udp.DATA+2]>>14) & 0x3f);
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
			Status.commErr = 1;
			Dbg.wr("connection lost");
		}
	}

	/**
	 * Request to send a message immediately.
	 */
	public void requestSend() {
		sendTimer = Timer.getTimeoutSec(0);
	}

	public void sendZiel() {
		// TODO Auto-generated method stub
		
	}

	public boolean isVerschub() {
		
		return type==TYPE_VERSCH;
	}
	
	public boolean anmOk() {
		return (cmdAck & CFLAG_ANMOK) != 0;
	}

	/**
	 * Set alarm flag
	 * @param alarmType
	 */
	public void setAlarm(int alarmType) {
		// TODO Auto-generated method stub
//		if (alarmType==Cmd.ALARM_UEBERF) {
//			Display.write("", "ZIEL ÜBERFAHREN", "");
//		} else if (alarmType==Cmd.ALARM_FAEHRT) {
//			Display.write("KEINE", "FAHRERLAUBNIS", "");
//		} else if (alarmType==Cmd.ALARM_RICHTUNG) {
//			Display.write("Falsche", "Richtung", "");
//		} else {
//			Display.write("Alarm", "Nummer", alarmType, "");
//		}
		
		if (alarmType==0) {
			// reset the alarm
			// or better mark it as reset alarm pending
			// we should not loose a alarm message due to a
			// too fast quite from the TFZF
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
	 * Got ack for ANK from ZLB so flag is reset
	 * @return
	 */
	public boolean ankuftAck() {
		return (alarmFlags & AFLAG_ANK) == 0;
	}

	/**
	 * Got ack for VERL from ZLB so flag is reset
	 * @return
	 */
	public boolean verlassenAck() {
		return (alarmFlags & AFLAG_VERL) == 0;
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
		// TODO Auto-generated method stub
		
	}

	public void setESAlarm() {
		// TODO Auto-generated method stub
		
	}

}
