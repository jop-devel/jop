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
	private boolean connSent;
	
	private int alarmType;

	private int[] buf;
	// length is one display line without status character
	private static final int BUF_LEN = 19;

	private String[] mnuTxt;
	private static final int MNU_CNT = 5;
	private String[] mnuESTxt;
	private static final int MNU_ES_CNT = 4;
	
	private String bsyIndication;
	private StringBuffer tmpStr;
	

	/**
	*	Next state after alarm quit.
	*/
	private int stateAfterQuit;

	Logic(int prio, int period, LinkLayer ipl) {
		super(prio, period);
		ipLink = ipl;
		init();
	}

	private void init() {

		buf = new int[BUF_LEN];

		mnuTxt = new String[MNU_CNT];
		mnuTxt[0] = "Ankunft/Verlassen";
		mnuTxt[1] = "Infobetrieb";
		mnuTxt[2] = "Lernbetrieb";
		mnuTxt[3] = "Umschalten -> ES221";
		mnuTxt[4] = "Neustart";
		mnuESTxt = new String[MNU_ES_CNT];
		mnuESTxt[0] = "Haltepunkt";
		mnuESTxt[1] = "Verschub";
		mnuESTxt[2] = "Umschalten -> ZLB";
		mnuESTxt[3] = "Neustart";
		bsyIndication = "+*";
		tmpStr = new StringBuffer(200);
		
		initVals();
	}
	
	private void initVals() {

		Status.state = Status.INIT;

		Status.selectStr = false;
		Status.melNr = 0;
		Status.melNrSent = 0;
		Status.strNr = 0;
		Status.melNrStart = 0;
		Status.melNrZiel = 0;
		Status.von = -1;
		Status.direction = Gps.DIR_UNKNOWN;
		Status.sendFerlQuit = false;
		Status.checkMove = true;

		Status.dispMenu = false;

		stateAfterQuit = Status.state;
		connSent = false;
		Status.esMode = true;
	}

	public void run() {

		int old_state = Status.state;
		
		for (;;) {

			//
			// Beep on every change
			//
			if (Status.state != old_state) {
				Led.shortBeep();
				old_state = Status.state;
			}
			
			if (Status.commErr!=0) {
				commError();
			} else if (Status.download) {
				download();
			} else if (Status.dispMenu) {
				menu();
			} else if (Status.state==Status.INIT) {
				connect();
			} else if (Status.state==Status.FDL_CONN) {
				anmelden();
			} else if (Status.state==Status.ANM_OK) {
				if (Status.esMode) {
					// we get into ANM_OK after an alarm the removes
					// our Fahrerlaubnis
					Status.state = Status.ES_RDY;
				} else {
					Display.write("Anmeldung OK", "", "");
				}
			} else if (Status.state==Status.ANGABE) {
				Flash.loadStrNames(Status.strNr, Status.melNrStart, Status.melNrZiel);
				Flash.Point p = Flash.getPoint(Status.melNrZiel);
				Display.write("Angabe", p.stationLine1, p.stationLine2);
			} else if (Status.state==Status.ERLAUBNIS) {
				erlaubnis();
			} else if (Status.state==Status.WIDERRUF) {
				widerruf();
			} else if (Status.state==Status.NOTHALT) {
				nothalt();
			} else if (Status.state==Status.NOTHALT_OK) {
				Display.write("Nothalt OK", "", "");
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
			} else if (Status.state==Status.ES_RDY) {
				esRdy();
			} else if (Status.state==Status.ES_VERSCHUB) {
				esVerschub();
			} else if (Status.state==Status.ES2ZLB) {
				es2zlb();
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
		
		//
		//	update Timer
		//
		Timer.loop();

		if (Status.commErr != 0) {
			// some async message should be displayed (like comm err, Nothalt...)
			return false;
		}

		if (Keyboard.peek()==Keyboard.B) {
			if (!Status.dispMenu) {
				Keyboard.rd();
				Status.dispMenu = true;
				return false;
			}
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
		//	check for MelNr range and direction
		//		direction check also in ZIEL
		//
		if (Status.state==Status.ERLAUBNIS || Status.state==Status.ZIEL) {

			if (Status.direction==Gps.DIR_UNKNOWN) {
				if (Status.melNrZiel > Status.melNrStart) {		// going from left to rigth.
					Status.direction = Gps.DIR_FORWARD;
				} else if (Status.melNrZiel < Status.melNrStart) {		// going from left to rigth.
					Status.direction = Gps.DIR_BACK;
				}
			}

			if (Status.direction==Gps.DIR_FORWARD) {		// going from left to rigth.
				// check Melderaum
				if (Status.melNr<Status.melNrStart || Status.melNr>Status.melNrZiel) {
					Status.state = Status.ALARM;
					alarmType = Cmd.ALARM_UEBERF;
					stateAfterQuit = Status.ANM_OK;
					return false;
				}
				// check direction
				if (Gps.direction==Gps.DIR_BACK &&
					!(Status.art==Status.ZUG_NF && Status.melNr==Status.melNrZiel)) {
						stateAfterQuit = Status.state;
// FERL bleibt
//						stateAfterQuit = Status.ANM_OK;
						Status.state = Status.ALARM;
						alarmType = Cmd.ALARM_RICHTUNG;
						return false;					
				}
			} else {										// going from right to left
				// check Melderaum
				if (Status.melNr>Status.melNrStart || Status.melNr<Status.melNrZiel) {
					Status.state = Status.ALARM;
					alarmType = Cmd.ALARM_UEBERF;
					stateAfterQuit = Status.ANM_OK;
					return false;
				}
				// check direction
				if (Gps.direction==Gps.DIR_FORWARD &&
					!(Status.art==Status.ZUG_NF && Status.melNr==Status.melNrZiel)) {
						stateAfterQuit = Status.state;
// FERL bleibt
//						stateAfterQuit = Status.ANM_OK;
						Status.state = Status.ALARM;
						alarmType = Cmd.ALARM_RICHTUNG;
						return false;					
				}
			}
		// every state different form Erlaubnis/Verschub.. check moving
		// except Verschub, Info, Lern, NOTHALT (?...?)
		} else {
			// Status.von > 0 means Verschub
			if (Status.von<=0 && Status.state!=Status.ALARM &&
				Status.state!=Status.NOTHALT && Status.state!=Status.NOTHALT_OK &&
				Status.state!=Status.INFO && Status.state!=Status.LERN &&
				Status.state!=Status.ES_VERSCHUB &&
				Status.checkMove && Gps.speed>Gps.MIN_SPEED) {
				stateAfterQuit = Status.state;
				Status.state = Status.ALARM;
				alarmType = Cmd.ALARM_FAEHRT;
				Status.checkMove = false;		// disable further Alarms
				return false;
			}
		}
		// Stillstand: Enable checkMove
		if (Gps.speed<Gps.MIN_SPEED) {
			Status.checkMove = true;
		}
		if (Status.von>0 && Status.state!=Status.ALARM) {		// Verschub
			if (Status.melNr>Status.bis || Status.melNr<Status.von) {
				Status.state = Status.ALARM;
				alarmType = Cmd.ALARM_UEBERF;
				Status.von = 0;	// clear Verschub
				Status.bis = 0;	// clear Verschub
				stateAfterQuit = Status.FDL_CONN;
				return false;
			}
		}
		if (!Status.connOk) {
			// perhaps retry a connect
			if (ipLink.getIpAddress()!=0 && Comm.dst_ip!=0 && !connSent) {
				// send a connect
				Comm.connect();
				connSent = true;
			}
		} else {
			//
			// send MelNr change
			//
			if (Status.melNrSent!=Status.melNr) {
				Comm.mel(Status.strNr, Status.melNr);
				Status.melNrSent = Status.melNr;

				//
				//	change start to disallow going back
				//
				Status.melNrStart = Status.melNr;
			}
		}
		

		if (Status.state==Status.ERLAUBNIS && 
			Status.melNr == Status.melNrZiel &&
			Status.art==Status.ZUG_NORMAL) {
			// disable till Stillstand
			Status.checkMove = false;
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

// System.out.println("Menue");
		while (loop()) {

			if (Status.esMode) {
				Display.write("Betriebsfunktion:", mnuESTxt[cnt],"");
			} else {
				Display.write("Betriebsfunktion:", mnuTxt[cnt],"");
			}

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
			if (val==Keyboard.B) {
				++cnt;
				if (Status.esMode) {
					if (cnt==MNU_ES_CNT) cnt=0;								
				} else {
					if (cnt==MNU_CNT) cnt=0;			
				}
			}
			if (val==Keyboard.E) {
				Status.dispMenu = false;
				if ((Status.state==Status.NOTHALT || Status.state==Status.NOTHALT_OK) && cnt!=4) {
					Display.write("", "Nicht möglich", "");
					waitEnter();
					return;
				}
			
				if (Status.esMode) {
					if (cnt==0) {
						Status.state = Status.ES_RDY;
					} else if (cnt==1) {
						Status.state = Status.ES_VERSCHUB;
					} else if (cnt==2) {
						Status.state = Status.ES2ZLB;
					} else if (cnt==3) {
						Display.write("Neustart", "", "");
						restart();
					}
				} else {
					if (cnt==0) {
						ankverl();
						return;
					} else if (cnt==1) {
						Status.state = Status.INFO;
					} else if (cnt==2) {
						if (!Status.isMaster) {
							Display.write("", "Nicht erlaubt", "");
							waitEnter();
							return;
						}
						Status.state = Status.LERN;
					} else if (cnt==3) {
						Status.state = Status.ES221;
					} else if (cnt==4) {
						Display.write("Neustart", "", "");
						restart();
					}			
				}

				return;
			}
		}
	}

	/**
	 * Wait for GPS to be ready.
	 * Wait to find a Strecke.
	 *
	 */
	private void connect() {

System.out.println("Connect");
		int tim;
		Display.initMsg();
		Led.shortBeep();
		
		boolean first = true;

		tim = Timer.getSec()+10;
		// wait for GPS data
		for (;;) {
			if (Gps.fix!=-1) {
				break;
			}
			if (Timer.secTimeout(tim) && first) {
				Display.write("GPS gestört!", "Quittieren (E)?", "");
				Led.alarm();
				waitEnterOnly();
				Led.alarmOff();
				first = false;
			}
			loop();
		}

		tim = Timer.getSec()+120;
		first = true;
		// wait for GPS melnr found
		for (;;) {
			if (Status.melNr>0) {
				break;
			}
			if (Gps.fix!=0) {
				Display.write("Betriebsbereit", "","");
			}
			if (Status.strNr<=0 && Status.selectStr) {
				enterStrNr();
			}
			if (Timer.secTimeout(tim) && first) {
				if (Gps.fix==0) {
					Display.write("Keine Satelliten!", "","");
					Led.shortBeep();
					first = false;
				}
			}
			if (!loop()) return;
		}

		if (Status.esMode) {
			// Str. names are already loaded in ES mode!
			Status.state = Status.ES_RDY;
		} else {
			startConn();			
		}
	}

	/**
	 * Strecke nicht eindeutig => eingeben
	 */
	private void enterStrNr() {
		
		int nr=-1;
		
		while (nr<=0) {
			Display.write("Strecke eingeben","Strecke:","");
			Led.shortBeep();
			nr = getNumber(8, 3);
			if (nr>0) {
				int cnt = Flash.getCnt();
				int i;
				for (i=0; i<cnt; ++i) {
					if (nr==Flash.getStrNr(i)) {
						break;
					}
				}
				if (i==cnt) {
					Display.write("Streckennummer","ungültig","");
					Led.shortBeep();
					waitEnterOnly();
					nr = -1;
				}
			}
		}
		Status.strNr = nr;
	}

	private void startConn() {

		Display.write("Verbindungsaufbau", "", "");
		
		// Isn't Flash.java a strange point for communication start!
		Flash.startComm();
		
		// wait for ip link established
		for (;;) {
			Display.write(20, "Versuch ", ipLink.getConnCount());
			if (ipLink.getIpAddress()!=0) {
				break;
			}
			if (!loop()) return;
		}
		
		Display.write("Verbinden zu", "", "");
		Display.ipVal(40, Comm.dst_ip);
/* called in check()
		// send a connect
		Comm.connect();
*/
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
		// wait for replay
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

		int nr, val, tim;

// System.out.println("Anmelden");
		// load default strings for Verschub
		Flash.loadStrNames(Status.strNr, 0, 0);

		verschub();
		val = 0;
		Status.art = 0;
		nr = 0;
		tim = Timer.getTimeoutSec(5);

		// Strecke is known!!!

		while (loop()) {

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

		Status.art = val;
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
				Comm.anmelden(Status.art, nr, Status.strNr, Status.melNr);
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

// System.out.println("Zugnummer");
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
	
	private void commError() {

// System.out.println("Comm Error");
		// In ES mode we just ignore the communication error
		if (Status.esMode) {
			Status.commErr = 0;
			return;
		}
		int nr = Status.commErr;
		Led.shortBeep();
		Led.startBlinking();
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

		Status.connOk = false;
		connSent = false;
		// clear error
		Status.commErr = 0;
		// clear melNrSent, has to be resend
		Status.melNrSent = 0;

		for (;;) {
			loop();
			//
			//	we got another communication error during wait for Enter
			//		just reset our vars and let check()/Comm do the reconnect
			//
			if (Status.commErr!=0) {
				connSent = false;
				// clear error
				Status.commErr = 0;
				// clear melNrSent, has to be resend
				Status.melNrSent = 0;
			}
			if (Keyboard.rd()==Keyboard.E) break;
			if (Status.connOk) break;
		}
		Led.stopBlinking();
		// keep Status even if not connected
/*
		Status.state = Status.INIT;
		Status.strNr = 0;
		Status.melNr = 0;
		Status.melNrSent = 0;
*/
	}

	/**
	*	Handle the alarms.
	*/
	private void alarm() {

//		Dbg.wr("Alarm ");
//		Dbg.intVal(alarmType);
//		Dbg.lf();
		if (alarmType==Cmd.ALARM_UEBERF) {
			Display.write("Melderaum", "überfahren", "");
		} else if (alarmType==Cmd.ALARM_FAEHRT) {
			Display.write("Keine", "Fahrerlaubnis!", "");
		} else if (alarmType==Cmd.ALARM_RICHTUNG) {
			Display.write("Falsche", "Richtung", "");
		} else {
			Display.write("Alarm", "Nummer", alarmType, "");
		}
		if (Status.esMode) {
			setGpsData();
			tmpStr.append("Alarm: ");
			tmpStr.append(alarmType);
			tmpStr.append("\n");
			Flash.log(tmpStr);

		}

		Led.alarm();
		Comm.alarm(Status.strNr, Status.melNr, alarmType);
		for (;;) {
			// only NOTHAL overwrites an Alarm
			if (Status.state == Status.NOTHALT) return;
			// wait for Enter to quit Alarm
			if (waitEnter()) break;
		}
		Led.alarmOff();
		Comm.alarm(Status.strNr, Status.melNr, 0);
		//
		//	update state with stateAfterQuit only if
		//	state did not change since alarm
		//
		if (Status.state==Status.ALARM) {
			Status.state = stateAfterQuit;
		}
	}

	private void erlaubnis() {

// Dbg.wr("Erlaubnis\n");
		Flash.Point p = Flash.getPoint(Status.melNrZiel);
		Display.write("Fahrerlaubnis", p.stationLine1, p.stationLine2);
		if (Status.sendFerlQuit) {
			Led.startBlinking();

			int state = Status.state;
			boolean warned = false;
			while (loop()) {
				int val = Keyboard.rd();
				if (val==Keyboard.E) {
					break;
				}
				// some State has changed!
				// dont wait anymore
				if (state!=Status.state) {
					Led.stopBeeping();
					Led.stopBlinking();
					return;
				}

				if (!warned && Gps.speed>Gps.MIN_SPEED) {
					Display.write("Fahrerlaubnis", "mit Enter bestätigen", "");
					Led.startBeeping();
					warned = true;
				}
			}


			Comm.simpleCmd(Cmd.FERL_QUIT);
			Status.sendFerlQuit = false;
			Led.stopBeeping();
			Led.stopBlinking();
		}
	}

	private void ziel() {

// Dbg.wr("Ziel\n");
		Flash.Point p = Flash.getPoint(Status.melNr);
		Display.write("Ziel erreicht:", p.stationLine1, p.stationLine2);
		Comm.melnrCmd(Cmd.ANZ, Status.strNr, Status.melNr);
		Led.startBlinking();
		if (Status.esMode) {
			setGpsData();
			tmpStr.append("Ziel erreicht: ");
			tmpStr.append(Status.melNr);
			tmpStr.append("\n");
			Flash.log(tmpStr);

		}
		if (!waitEnter()) {
			Led.stopBlinking();
			return;
		}
		Status.state = Status.ANM_OK;
		Led.stopBlinking();
	}


	private void widerruf() {

// System.out.println("Widerruf");
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

// System.out.println("Nothalt");
		Display.write("", "NOTHALT!", "");
		Led.alarm();
		// wait for Enter
		while (loop()) {
			if (Keyboard.rd()==Keyboard.E) {
				Comm.simpleCmd(Cmd.NOT_QUIT);
				Led.alarmOff();
				Status.state = Status.NOTHALT_OK;
				return;
			}
		}
	}

	private void abmelden() {

// System.out.println("Abmelden");
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
		
		int tim;

// System.out.println("AnkVerl");
		if (Status.melNr<=0 || Status.melNrStart<=0 || Status.melNrZiel<=0 ||
				Status.direction == Gps.DIR_UNKNOWN) {
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
			Display.write("Ankunftsmeldung bei", p.stationLine1, p.stationLine2);

			if (!waitEnter()) return;

			Display.write("Ankunftsmeldung OK", p.stationLine1, p.stationLine2);

			tim = Timer.getTimeoutSec(5);
			Status.ankunftOk = false;
			Comm.melnrCmd(Cmd.ANK, Status.strNr, p.melnr);
			// and wait for a reply from FDL
			while (loop()) {
				if (Status.ankunftOk && Timer.timeout(tim)) {
					return;
				}
			}
			return;
		} else if (p.verlassen) {
			if (Status.direction == Gps.DIR_FORWARD) {		// going from left to rigth.
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
			Display.write("Verlmeldung nach", p.stationLine1, p.stationLine2);

			if (!waitEnter()) return;

			Display.write("Verlmeldung OK", p.stationLine1, p.stationLine2);


			tim = Timer.getTimeoutSec(5);
			Status.verlassenOk = false;
			Comm.melnrCmd(Cmd.VERL, Status.strNr, p.melnr);
			// and wait for a reply from FDL
			while (loop()) {
				if (Status.verlassenOk && Timer.timeout(tim)) {
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

// System.out.println("Download");
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

// System.out.println("Infobtrieb");
		Display.write("Infobetrieb", "", "");
		Comm.charlyStatus(Cmd.INFO_MODE, 1);
		// wait for Enter or 'C'
		while (loop()) {
			i = Keyboard.rd();
			if (i==Keyboard.E || i==Keyboard.C) {
				Comm.charlyStatus(Cmd.INFO_MODE, 0);
				Status.state = Status.INIT;
				Status.strNr = 0;
				Status.melNr = 0;
				Status.melNrSent = 0;
				return;
			}
			
			
			tmpStr.setLength(0);
			tmpStr.append(Gps.nearestPointDistance);
			tmpStr.append("m zu ");
			tmpStr.append(Gps.nearestPoint);
			tmpStr.append(' ');
			tmpStr.append(Gps.speed);
			tmpStr.append(" km/h");
			
			Display.write(0, tmpStr);
			
			tmpStr.setLength(0);
			i = Gps.speedCalc;
			if (i<0) i = 0;
			tmpStr.append(Gps.speedCalc);
			tmpStr.append(" km/h");
			if (i<1000) tmpStr.append(' ');
			if (i<100) tmpStr.append(' ');
			tmpStr.append("MNr: ");
			Display.write(20, tmpStr, Status.melNr);
			Display.write(40, Gps.text);
		}
	}

	private void lern() {

		int i, val;

// System.out.println("Lern");
		Display.write("Lerne", "Strecke","");

		Status.strNr = getNumber(8, 3);
		if (Status.strNr == -1) return;

		int melnr = Flash.getFirst(Status.strNr);
		if (melnr==-1) {
			Display.write("Strecke", "nicht gefunden", "");
			waitEnterAndInit();
			return;
		}

		Flash.loadStrNames(Status.strNr, 0, 0);

		startConn();
		// Conn changes to FLD_CONN
		Status.state = Status.LERN;
		Comm.charlyStatus(Cmd.ST_LERN, 1);

		while (loop()) {

			Display.write(0, "Lernbetrieb ", melnr);
			Flash.Point p = Flash.getPoint(melnr);
			Display.write(20, p.stationLine1);

			Display.write(40, Gps.text);

			val = Keyboard.rd();
			if (val==-1) {
				continue;
			}

			if (val==Keyboard.C) {
				Comm.charlyStatus(Cmd.ST_LERN, 0);
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

// System.out.println("Measure");
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
	
	/**
	 * Menu command to change to ES mode
	 *
	 */
	private void es221() {

		Display.write("Wechsel zu", "ES221 Betrieb", "");
		Status.esMode = true;
		// force a new Str. load
		Status.strNr = 0;
		Status.melNr = 0;
		Status.melNrSent = 0;
		Flash.forceReload();
		Status.state = Status.ES_RDY;
		int tim = Timer.getTimeoutSec(2);
		while (loop()) {
			if (Timer.timeout(tim)) {
				return;
			}
		}
	}

	/**
	 * ES mode: wait for entering a destination
	 *
	 */
	private void esRdy() {
		
		Status.melNrStart = Status.melNr;
		Status.melNrZiel = Status.melNr;

		int val = 0;
		int i = 0;
		int melnr = Flash.getFirst(Status.strNr);
		Flash.Point p = Flash.getPoint(melnr);
		if (p==null) {
			Display.write("Keine ES-mode", "Streckendaten", "");
			while (loop()) {
				;
			}
			return;
		}
		
// System.out.println("ES Menue");

		for (;;) {
			i = Flash.getNext(melnr);
			i = Flash.getNext(i);
			if (i!=-1) {
				melnr = i;
				if (melnr>=Status.melNr) {
					p = Flash.getPoint(melnr);
					break;
				}
			} else {
				i = 0;
				break;
			}
		}

		while (loop()) {

			Display.write("Haltepunkt ausw.:", p.stationLine1, p.stationLine2);

			val = Keyboard.rd();
			if (val==-1) {
				continue;
			}
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				return;
			}

			// display only the left point text

			if (val==Keyboard.UP) {
				i = Flash.getNext(melnr);
				i = Flash.getNext(i);
				if (i!=-1) {
					melnr = i;
					p = Flash.getPoint(melnr);
				}
			}
			if (val==Keyboard.DOWN) {
				i = Flash.getPrev(melnr);
				i = Flash.getPrev(i);
				if (i!=-1) {
					melnr = i;
					p = Flash.getPoint(melnr);
				}
			}
			if (val==Keyboard.E) {
				
				// The left point is the station
				// smaller melnr is start
				Status.melNrStart = Status.melNr;
				Status.melNrZiel = melnr;
				Status.state = Status.ERLAUBNIS;
				Status.art = Status.ZUG_NORMAL;
				Status.direction = Gps.DIR_UNKNOWN;

				setGpsData();
				tmpStr.append("Fahrt von ");
				tmpStr.append(Status.melNrStart);
				tmpStr.append(" nach ");
				tmpStr.append(Status.melNrZiel);
				tmpStr.append("\n");
				Flash.log(tmpStr);


				return;
			}
		}

	}

	/**
	 * 
	 */
	private void setGpsData() {
		tmpStr.setLength(0);
		synchronized (Gps.lastGGA) {
			tmpStr.append(Gps.lastGGA);
		}
		synchronized (Gps.lastRMC) {
			tmpStr.append(Gps.lastRMC);
		}
		tmpStr.append("Strecke ");
		tmpStr.append(Status.strNr);
		tmpStr.append(" - ");
	}

	private void esVerschub() {
		Display.write("ES Verschub", "", "");

		setGpsData();
		tmpStr.append("Verschub: ");
		tmpStr.append(Status.melNr);
		tmpStr.append("\n");
		Flash.log(tmpStr);

		Status.state = Status.ES_VERSCHUB;
		Status.melNrStart = Status.melNr;
		Status.melNrZiel = Status.melNr;
		while (loop()) {
			;
		}

	}
	
	/**
	 * Menu command to change to ES mode
	 *
	 */
	private void es2zlb() {

		Display.write("Wechsel zu", "ZLB Betrieb", "");
		// hoffentlich reicht das!!
		initVals();
		Status.esMode = false;
		// force a new Str. load
		Flash.forceReload();
		int tim = Timer.getTimeoutSec(2);
		while (loop()) {
			if (Timer.timeout(tim)) {
				return;
			}
		}
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
