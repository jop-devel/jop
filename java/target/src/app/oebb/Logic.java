package oebb;

/**
*	Logic.java: Logic for OEBB project.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*
*/

import util.*;
import joprt.*;
import ejip.*;

public class Logic extends RtThread {

	private LinkLayer ipLink;

	private int[] buf;
	// length is one display line without status character
	private static final int BUF_LEN = 19;

	private String[] mnuTxt;
	private static final int MNU_CNT = 5;
	
	private String bsyIndication;

	/**
	*	Next state after alarm or error quit.
	*/
	private int stateAfterQuit;

	Logic(int prio, int period, LinkLayer ipl) {
		super(prio, period);
		ipLink = ipl;
		init();
	}

	private void init() {

		Status.state = Status.INIT;
		buf = new int[BUF_LEN];

		Status.melNr = 0;
		Status.melNrSent = 0;
		Status.strNr = 0;
		Status.melNrStart = 0;
		Status.melNrZiel = 0;
		Status.sendFerlQuit = false;

		Status.dispMenu = false;
		mnuTxt = new String[MNU_CNT];
		mnuTxt[0] = "Ankunft/Verlassen";
		mnuTxt[1] = "Infobetrieb";
		mnuTxt[2] = "Lernbetrieb";
		mnuTxt[3] = "Umschalten -> ES221";
		mnuTxt[4] = "Neustart";
		bsyIndication = "+*";

		stateAfterQuit = Status.state;
	}

	public void run() {

		for (;;) {

			if (Status.download) {
				download();
			} else if (Status.dispMenu) {
				menu();
			} else if (Status.state==Status.INIT) {
				connect();
			} else if (Status.state==Status.FDL_CONN) {
				anmelden();
			} else if (Status.state==Status.ANM_OK) {
				Display.write("Anmeldung OK", "", "");
			} else if (Status.state==Status.COMM_ERR) {
				error();
			} else if (Status.state==Status.ANGABE) {
				Display.write(0, "Angabe");
				Display.write(20, "");
				Flash.setText(Status.melNrZiel, buf);
				Display.write(40, buf);
			} else if (Status.state==Status.ERLAUBNIS) {
				erlaubnis();
			} else if (Status.state==Status.WIDERRUF) {
				widerruf();
			} else if (Status.state==Status.NOTHALT) {
				nothalt();
			} else if (Status.state==Status.ABGEMELDET) {
				abmelden();
			} else if (Status.state==Status.ZIEL) {
				ziel();
			} else if (Status.state==Status.INFO) {
				info();
			} else if (Status.state==Status.LERN) {
				lern();
			} else if (Status.state==Status.ES221) {
				es221();
			} else if (Status.state==Status.ALARM) {
				alarm();
			} else {
				Display.write(0, "STATE          ");
				Display.intVal(40, Status.state);
			}

			loop();
		}
	}


	/**
	*	update display status, wait period and
	*	check for ATC condition.
	*/
	private boolean loop() {

		waitForNextPeriod();

		//
		// set status in display
		//
		Display.setInetOk(ipLink.getIpAddress()!=0);
		Display.setGpsOk(Gps.fix>0);
		Display.setDgpsOk(Gps.fix==2);

		//
		//	do the LED and Beep thing
		//
		Led.loop();

		if (Status.commErr != 0) {
			Status.state = Status.COMM_ERR;
			// some async message should be displayed (like comm err, Nothalt...)
			return false;
		}

		if (Keyboard.peek()==Keyboard.B) {
			Keyboard.rd();
			Status.dispMenu = true;
			return false;
		}

		if (!check()) return false;


// Dbg.intVal(com.jopdesign.sys.Native.getSP());
		return true;
	}

	/**
	*	Check for error conditions.
	*/
	private boolean check() {

		//
		//	check for MelNr range
		//
		// TODO check direction
		//
		if (Status.state==Status.ERLAUBNIS) {
			if (Status.melNrZiel > Status.melNrStart) {		// going from left to rigth.
				if (Status.melNr<Status.melNrStart || Status.melNr>Status.melNrZiel) {
					Status.state = Status.ALARM;
					stateAfterQuit = Status.ANM_OK;
					return false;
				}
			} else {										// going from right to left
				if (Status.melNr>Status.melNrStart || Status.melNr<Status.melNrZiel) {
					Status.state = Status.ALARM;
					stateAfterQuit = Status.ANM_OK;
					return false;
				}
			}
		}
		if (Status.von>0) {		// Verschub
			if (Status.melNr>Status.bis || Status.melNr<Status.von) {
				Status.state = Status.ALARM;
				Status.von = 0;	// clear Verschub
				Status.bis = 0;	// clear Verschub
				stateAfterQuit = Status.FDL_CONN;
				return false;
			}
		}
		//
		// send MelNr change
		//
		if (Status.melNrSent!=Status.melNr && Status.state>=Status.FDL_CONN && Status.state!=Status.COMM_ERR) {
			Comm.mel(Status.strNr, Status.melNr);
			Status.melNrSent = Status.melNr;

			//
			//	change start to disallow going back
			//
			Status.melNrStart = Status.melNr;
		}
		// TODO Ziel erreicht
		if (Status.state==Status.ERLAUBNIS && Status.melNr == Status.melNrZiel) {
			Status.state = Status.ZIEL;
		}

		return true;
	}

	/**
	*	menu handling
	*/
	private void menu() {

		int cnt, val, tim;
		cnt = 0;
		tim = Timer.getTimeoutSec(10);

		while (loop()) {

			Display.write("Betriebsfunktion:", mnuTxt[cnt],"");

			if (Timer.timeout(tim)) {
				Status.dispMenu = false;
				return;
			}
			val = Keyboard.rd();
			if (val==-1) {
				continue;
			}
			tim = Timer.getTimeoutSec(10);

			if (val==Keyboard.C) {
				Status.dispMenu = false;
				return;
			}
			if (val==Keyboard.DOWN) {
				if (cnt<MNU_CNT-1) ++cnt;
			}
			if (val==Keyboard.UP) {
				if (cnt>0) --cnt;
			}
			if (val==Keyboard.E) {
				Status.dispMenu = false;
				if (cnt==0) {
					ankverl();
					return;
				} else if (cnt==1) {
					Status.state = Status.INFO;
				} else if (cnt==2) {
					Status.state = Status.LERN;
				} else if (cnt==3) {
					Status.state = Status.ES221;
				} else if (cnt==4) {
					Display.write("Neustart", "", "");
					restart();
				}

				return;
			}
		}
	}

	private void connect() {

		int tim;
		Display.initMsg();

		tim = Timer.getTimeoutSec(10);
		// wait for GPS data
		while (loop()) {
			if (Gps.fix!=-1) {
				break;
			}
			if (Timer.timeout(tim)) {
				error("GPS gestört!", "Quittieren (E)?");
				return;
			}
		}

		tim = Timer.getTimeoutSec(120);
		// wait for GPS melnr found
		while (loop()) {
			if (Status.melNr>0) {
				break;
			}
			if (Timer.timeout(tim)) {
				// beep();
				if (Gps.fix==0) {
					Display.write("Keine Satelliten!", "","");
				} else {
					Display.write("Betriebsbereit", "","");
				}
				tim = Timer.getTimeoutSec(120);
			}
		}

		Display.write("", "Verbindungsaufbau", "");

		// Isn't Flash.java a strange point for communication start!
		Flash.startComm();

		// wait for ip link established
		while (loop()) {
			if (ipLink.getIpAddress()!=0) {
				Status.state = Status.IP_LINK;
				break;
			}
		}
		// Status.state change in loop()
		if (Status.state!=Status.IP_LINK || Status.dispMenu) return;

		Display.write("Verbinden zu", "", "");
		Display.ipVal(40, Comm.dst_ip);
		// send a connect
		Comm.connect();
		// and wait for a reply
		while (loop()) {
			if (Status.connOk) {
				Status.state = Status.FDL_CONN;
				break;
			}
		}
	}

	private void verschub() {

		int tim;

		Status.von = -1;

		Comm.verschub(Status.strNr, Status.melNr);
		while (loop()) {
			if (Status.von != -1) {
				break;
			}
		}
		if ((Status.von)==0 && (Status.bis)==0) {
			return;
		}
		// Status.state change in loop()
		if (Status.state!=Status.FDL_CONN || Status.dispMenu) return;

		tim = Timer.getTimeoutSec(5);
		Display.write("Verschub erlaubt", "", "");

		while (loop()) {

			Flash.Point p = Flash.getPoint(Status.von);
			if (p!=null) {
				Display.write(20, p.verschubVon);
			}
			p = Flash.getPoint(Status.bis);
			if (p!=null) {
				Display.write(40, p.verschubBis);
			}
			if (Status.von==0 && Status.bis==0) {
				break;
			}
			if (Timer.timeout(tim)) {
				Comm.verschub(Status.strNr, Status.melNr);
				tim = Timer.getTimeoutSec(5);
			}
		}
	}

	private void anmelden() {

		int art, nr, val, tim;

		verschub();
		val = 0;
		art = 0;
		nr = 0;
		tim = Timer.getTimeoutSec(5);

		// Strecke is known!!!
		// TODO querry Strecke if not clear which one

/*
		if (Status.strNr<=0) {
			nr = getNumber(8, 3);		// TODO: check Strecke
			if (nr == -1) { 			// start communication
				return;					// change anmelden() with known Strecke...
			} else {
				Status.strNr = nr;
			}
		} else {
		}
		if (Status.strNr == -1) return;
*/

		while (loop()) {

/*
			Display.write("Anmelden", "Strecke", "Z: 1, Nf: 2");
			Display.intVal(28, Status.strNr);
*/
			Display.write("Anmelden", "Strecke ", Status.strNr, "Z: 1, Nf: 2");

			if (Timer.timeout(tim)) {
				verschub();
				// Status.state change in verschub()
				if (Status.state!=Status.FDL_CONN || Status.dispMenu) return;
				tim = Timer.getTimeoutSec(5);
			}
			val = Keyboard.rd();
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				return;
			} else if (val==Keyboard.C) {
				return;
			}
			val = Keyboard.num(val);
			if (val>=1 && val <=2) {
				nr = zugnummer(val);
				if (nr == -1) return;
				break;
			}
		}
		// Status.state change in loop()
		if (Status.state!=Status.FDL_CONN || Status.dispMenu) return;

		art = val;
/*
		if (val==1) {
			Display.write("StreckenNr:", "ZugNr:","");
			Display.intVal(12, Status.strNr);
			Display.intVal(27, nr);
		} else {
			Display.write("StreckenNr:", "Nebenfahrt: N","");
			Display.intVal(12, Status.strNr);
			Display.intVal(33, nr);
		}
*/
		if (val==1) {
			Display.write("StreckenNr: ", Status.strNr, "ZugNr: ",nr ,"");
		} else {
			Display.write("StreckenNr: ", Status.strNr, "Nebenfahrt: N", nr,"");
		}

		//
		//	set IP address from Strecke
		//
		Flash.loadStr(Status.strNr);

		while (loop()) {
			val = Keyboard.rd();
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				continue;
			}
			if (val==Keyboard.E) {
				Display.write("Anmeldung", "", "");
				if (Status.state!=Status.FDL_CONN) return;
				// send anmelden
				Comm.anmelden(art, nr, Status.strNr, Status.melNr);
				// and wait for a reply of ANM
				// and a ANMOK
				while (loop()) {
					if (Status.anmOk) {
						Status.state = Status.ANM_OK;
						break;
					} else if (Status.state == Status.ABGEMELDET) {
						abmelden();
					}
				}

				return;
			} else if (val==Keyboard.C) {
				return;
			}
		}
	}

	private int zugnummer(int val) {

		if (val==1) {
			Display.write("", "ZugNr:","");
			return getNumber(7, 5);
		} else if (val==2) {
			Display.write("", "Nebenfahrt:","");
			return getNumber(12, 5);
		} else {
			return 0;	// no number fuer Verschub
		}
	}
	
	private void error(String l1, String l2) {

		Display.write(l1, l2, "");
		waitEnterOnly();
		Status.state = Status.INIT;
		Status.strNr = 0;
		Status.melNr = 0;
		Status.melNrSent = 0;
	}

	private void error() {

		int nr = Status.commErr;
		Led.shortBeep();
		if (nr==2) {
			Display.write("FDL Msg Fehler", "Paket zu kurz", "");
		} else if (nr==3) {
			Display.write("FDL Msg Fehler", "falsches CMD", "");
		} else if (nr==4) {
			Display.write("FDL Msg Fehler", "falsche bgid", "");
		} else {
			Display.write("FDL Rechner", "antwortet nicht", "FDL verständigen!");
		}
		// Display.intVal(40, Status.commErr);

		// clear error
		Status.commErr = 0;
		// clear melNrSent, has to be resend
		Status.melNrSent = 0;

		waitEnterOnly();
		Status.state = Status.INIT;
		Status.strNr = 0;
		Status.melNr = 0;
		Status.melNrSent = 0;
	}

	/**
	*	Now only one Alarm defined.
	*/
	private void alarm() {

		Display.write("Melderaum", "überfahren", "");
		Led.alarm();
		Comm.alarm(Status.strNr, Status.melNr, Cmd.ALARM_UEBERF);
		// wait for Enter
		if (!waitEnter()) return;
		Display.write("??? OK", "", "");
		Led.alarmOff();
		Status.state = stateAfterQuit;
	}

	private void erlaubnis() {

		Display.write(0, "Fahrerlaubnis");
		Display.write(20, "");
		Flash.setText(Status.melNrZiel, buf);
		Display.write(40, buf);
		if (Status.sendFerlQuit) {
			Led.startBlinking();
			if (!waitEnter()) return;
			Comm.simpleCmd(Cmd.FERL_QUIT);
			Status.sendFerlQuit = false;
			Led.stopBlinking();
		}
	}

	private void ziel() {

		Display.write(0, "Ziel erreicht:");
		Display.write(20, "");
		Flash.setText(Status.melNr, buf);
		Display.write(40, buf);
		Comm.melnrCmd(Cmd.ANZ, Status.strNr, Status.melNr);
		Led.startBlinking();
		if (!waitEnter()) {
			Led.stopBlinking();
			return;
		}
		Status.state = Status.ANM_OK;
		Led.stopBlinking();
	}


	private void widerruf() {

		Display.write("Fahrtwiderruf", "", "");
		Status.melNrZiel = 0;
		Led.startBlinking();
		// wait for Enter
		while (loop()) {
			if (Keyboard.rd()==Keyboard.E) {
				Comm.simpleCmd(Cmd.FWR_QUIT);
				Display.write("Fahrtwiderruf OK", "", "");
				Status.state = Status.ANM_OK;
				Led.stopBlinking();
				return;
			}
		}
	}

	private void nothalt() {

		Display.write("", "NOTHALT!", "");
		Led.alarm();
		// wait for Enter
		while (loop()) {
			if (Keyboard.rd()==Keyboard.E) {
				Comm.simpleCmd(Cmd.NOT_QUIT);
				Display.write("Nothalt OK", "", "");
				Led.alarmOff();
				Status.state = Status.ANM_OK;
				break;
			}
		}
		while (loop()) {
			if (Status.dispMenu) {
				return;				// accept Menu request
			}
		}
	}

	private void abmelden() {

		Display.write("Abgemeldet", "", "");
		restart();
	}

	private void restart() {

		// wait some time to send reply
		int tim = Timer.getTimeoutSec(2);
		while (!Timer.timeout(tim)) {
			loop();
		}

		// wait for reset
		Main.reset = true;
		for (;;) {
			loop();
		}
	}

	private void ankverl() {

		if (Status.melNr<=0 || Status.melNrStart<=0 || Status.melNrZiel<=0) {
			Display.write("Keine Meldung", "möglich!", "");
			waitEnter();
			return;
		}
		Flash.Point p = Flash.getPoint(Status.melNr);
		if (p==null || !(p.ankunft || p.verlassen)) {
			Display.write("Keine Meldung", "möglich!", "");
			waitEnter();
			return;
		}

		if (p.ankunft) {
			Display.write("Ankunftsmeldung bei", "", "");
			Flash.setText(p.melnr, buf);
			Display.write(40, buf);

			if (!waitEnter()) return;

			Display.write("Ankunftsmeldung OK", "", "");
			Flash.setText(p.melnr, buf);
			Display.write(40, buf);

			Status.ankunftOk = false;
			Comm.melnrCmd(Cmd.ANK, Status.strNr, p.melnr);
			// and wait for a reply from FDL
			while (loop()) {
				if (Status.ankunftOk) {
					return;
				}
			}
			return;
		} else if (p.verlassen) {
			if (Status.melNrZiel > Status.melNrStart) {		// going from left to rigth.
				while (p!=null && !p.ankunft) {
					p = p.getPrev();
				}
			} else {
				while (p!=null && !p.ankunft) {
					p = p.getNext();
				}
			}
			if (p==null) {
				Display.write("Keine Meldung", "möglich!", "");
				waitEnter();
				return;
			}
			Display.write("Verlmeldung nach", "", "");

			Flash.setText(p.melnr, buf);
			Display.write(40, buf);

			if (!waitEnter()) return;

			Display.write("Verlmeldung OK", "", "");
			Flash.setText(p.melnr, buf);
			Display.write(40, buf);


			Status.verlassenOk = false;
			Comm.melnrCmd(Cmd.VERL, Status.strNr, p.melnr);
			// and wait for a reply from FDL
			while (loop()) {
				if (Status.verlassenOk) {
					return;
				}
			}
			return;
		}
	}

	private void download() {

		int dlType = -1;
		int percent = -1;
		int cnt = 0;

		Display.write("Übertragung", "", "");
		for (;;) {
			loop();			// there is no exit from download state!
			if (Status.dlType!=dlType) {
				dlType = Status.dlType;
				if (dlType==0) {
					Display.write("Übertragung", "  Streckendaten", "");
				} else {
					Display.write("Übertragung", "  Programm", "");
				}
			}
			if (percent != Status.dlPercent) {
				percent = Status.dlPercent;
				cnt = (cnt+1) & 0x01;
				Display.write(20, bsyIndication.charAt(cnt));
				if (percent==101) {
					Display.write(40, "fertig");
				} else if (percent>0 && percent<101) {
					int i = percent*19/100;
					for (int j=0; j<i; ++j) {
						Display.write(40+j, '#');
					}
				}
			}
		}
	}

	private void info() {

		int i, j;

		Display.write("Infobetrieb", "", "");
		// wait for Enter
		while (loop()) {
			if (Keyboard.rd()==Keyboard.E) {
				Status.state = Status.INIT;
				Status.strNr = 0;
				Status.melNr = 0;
				Status.melNrSent = 0;
				return;
			}
			i = Gps.speed*36;
			if (i>=0) {
				Display.intVal(20, i/10);
				j = 21;
				if (i>99) j = 22;
				if (i>999) j=23;
				Display.write(j, '.');
				Display.intVal(j+1, i%10);
				Display.write(j+2, " km/h   ");
			}
			Display.write(30, "MNr:   ");
			Display.intVal(34, Status.melNr);
			Display.intVal(40, Gps.last_lat);
			Display.intVal(50, Gps.last_lon);
		}
	}

	private void lern() {

		int i, val;

		Display.write("Lerne", "Strecke","");

		Status.strNr = getNumber(8, 3);
		if (Status.strNr == -1) return;


		int melnr = Flash.getFirst(Status.strNr);
		if (melnr==-1) {
			Display.write("Strecke", "nicht gefunden", "");
			waitEnterAndInit();
			return;
		}


		while (loop()) {

/*
			Display.write("Lernbetrieb", "", "");
			Display.intVal(12, melnr);
*/
			Display.write("Lernbetrieb ", melnr, "", "");
			Flash.setText(melnr, buf);
// buf should ba a string
for (i=0; i<19; ++i) Display.write(20+i, buf[i]);
// Display.wr(20, buf);

			Display.intVal(40, Gps.last_lat);
			Display.intVal(50, Gps.last_lon);

			val = Keyboard.rd();
			if (val==-1) {
				continue;
			}

			if (val==Keyboard.C) {
				Status.state = Status.INIT;
				Status.strNr = 0;
				Status.melNr = 0;
				Status.melNrSent = 0;
				return;
			}
			if (val==Keyboard.DOWN) {
				i = Flash.getPrev(melnr);
				if (i!=-1) melnr = i;
			}
			if (val==Keyboard.UP) {
				i = Flash.getNext(melnr);
				if (i!=-1) melnr = i;
			}
			if (val==Keyboard.E) {
				measure(melnr);
			}
		}
	}

	private void measure(int melnr) {

/*
		Display.write("Mittelung", "", "");
		Display.intVal(10, melnr);
*/
		Display.write("Mittelung ", melnr, "", "");
		Gps.startAvg();

		while (loop()) {

			Display.intVal(16, Gps.avgCnt);
			Display.intVal(20, Gps.getLatAvg());
			Display.intVal(30, Gps.getLonAvg());
			Display.intVal(40, Gps.last_lat);
			Display.intVal(50, Gps.last_lon);

			int val = Keyboard.rd();
			if (val==-1) {
				continue;
			}

			if (val==Keyboard.C) {
				return;
			}
			if (val==Keyboard.E) {

				Gps.stopAvg();
				Display.write("Wert wird", "gesendet", "");
				Status.lernOk = false;
				Comm.lern(Status.strNr, melnr, Gps.getLatAvg(), Gps.getLonAvg());
				// and wait for a reply from FDL
				while (loop()) {
					if (Status.lernOk) {
						return;
					}
				}
			}
		}
	}

	private void es221() {

		Display.write("", "---", "");
		waitEnterAndInit();
	}

	/**
	*	Wait for Enter and set state to INIT.
	*/
	private void waitEnterAndInit() {

		while (loop()) {
			if (Keyboard.rd()==Keyboard.E) {
				Status.state = Status.INIT;
				Status.strNr = 0;
				Status.melNr = 0;
				Status.melNrSent = 0;
				return;
			}
		}
	}

	/**
	*	wait for Enter only, no unread on 'B'.
	*	no break out with loop()
	*/
	private void waitEnterOnly() {

		for (;;) {
			loop();
			if (Keyboard.rd()==Keyboard.E) return;
		}
	}

	/**
	*	return false if 'C'
	*/
	private boolean waitEnter() {

		int val;
		int state = Status.state;
		while (loop()) {
			val = Keyboard.rd();
			if (val==Keyboard.E) {
				return true;
			} else if (val==Keyboard.B) {
				Keyboard.unread(val);
				return false;
			} else if (val==Keyboard.C) {
				return false;
			}
			// some State has changed!
			// dont wait anymore
			if (state!=Status.state) {
				return false;
			}
		}
		return false;
	}

	/**
	*	read a number with display update at pos.
	*	'E' is Enter
	*	'C' is Backspace
	*	'B' cancels input (call menu)
	*/
	private int getNumber(int pos, int size) {

		int cnt;
		if (size>BUF_LEN) size = BUF_LEN;
		for (cnt=0; cnt<size; ++cnt) {
			buf[cnt] = 0;
		}
		cnt = 0;

		while (loop()) {
			Display.write(20+pos+cnt, '_');
			int val = Keyboard.rd();
			if (val==Keyboard.C) {
				Display.write(20+pos+cnt, ' ');
				if (cnt>0) {
					--cnt;
				} else {
					return -1;
				}
			}
			if (val==Keyboard.E || cnt==size) {
				val = 0;
				Display.write(20+pos+cnt, ' ');
				for (int i=0; i<cnt; ++i) {
					val *= 10;
					val += buf[i];
				}
				return val;
			}
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				continue;
			}
			val = Keyboard.num(val);
			if (val!=-1) {
				buf[cnt] = val;
				Display.write(20+pos+cnt, '0'+val);
				++cnt;
			}
		}
		return -1;
	}
}
