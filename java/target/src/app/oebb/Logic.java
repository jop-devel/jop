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
	
	private boolean checkMelnr;
	/**
	 *	Richtungsüberwachung
	 */
	private boolean checkDirection;
	/**
	 *	Bewegungsueberwachung bei keiner Fahrerlaubnis
	 */
	private boolean checkMove;
	
	
	private int alarmType;

	private int[] buf;
	// length is one display line without status character
	private static final int BUF_LEN = 19;

	private String[] mnuBereit;
	private static final int MNU_BEREIT_CNT = 2;
	private String[] mnuTxt;
	private static final int MNU_CNT = 6;
	private String[] mnuESTxt;
	private static final int MNU_ES_CNT = 3;
	
	private String bsyIndication;
	private StringBuffer tmpStr;
	
	static final int DL_STRNR = 999;
	static final int DL_TIMEOUT = 60;

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

		mnuBereit = new String[MNU_BEREIT_CNT];
		mnuBereit[0] = "Deaktivieren";
		mnuBereit[1] = "Neustart";
		mnuTxt = new String[MNU_CNT];
		mnuTxt[0] = "Ankunft";
		mnuTxt[1] = "Verlassen";
		mnuTxt[2] = "Infobetrieb";
		mnuTxt[3] = "Lernbetrieb";
		mnuTxt[4] = "Umschalten -> ES221";
		mnuTxt[5] = "Neustart";
		mnuESTxt = new String[MNU_ES_CNT];
		mnuESTxt[0] = "Haltepunkt";
		mnuESTxt[1] = "Verschub";
		mnuESTxt[2] = "Neustart";
		bsyIndication = "+*";
		tmpStr = new StringBuffer(200);
		
		Status.dirMutex = new Object();
		
		initVals();
	}
	
	private void initVals() {

System.out.println("Logic.initVals()");
		Status.state = Status.INIT;
		Status.state = Status.DL_CHECK;

		Status.selectStr = false;
		Status.melNr = 0;
		Status.melNrSent = 0;
		Status.strNr = 0;
		Status.zugNr = 0;
		Status.melNrStart = 0;
		Status.melNrZiel = 0;
		Status.angabe = 0;
		Status.von = -1;
		Status.direction = Gps.DIR_UNKNOWN;
		Status.sendFerlQuit = false;

		Status.dispMenu = false;

		stateAfterQuit = Status.state;
		connSent = false;
		checkMelnr = false;
		checkMove = false;
		checkDirection = false;
		Status.esMode = false;
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
			} else {

				switch (Status.state) {

					case Status.DL_CHECK:
						queryDownload();
						break;
					case Status.INIT:
						waitForGps();
						break;
					case Status.GPS_OK:
						bereit();
						break;
					case Status.CONNECT:
						startConn();
						break;
					case Status.FDL_CONN:
						anmelden();
						break;
					case Status.ANM_OK:
						if (Status.esMode) {
							// we get into ANM_OK after an alarm the removes
							// our Fahrerlaubnis
							Status.state = Status.ES_RDY;
						} else {
							Display.write("Anmeldung OK", "", "");
						}
						break;
					case Status.ANGABE:
						// if we will remove this in the future, make shure
						// loadStrNames is invoked in ERLAUBNIS
						Flash.loadStrNames(Status.strNr, Status.melNr, Status.angabe);
						Flash.Point p = Flash.getPoint(Status.angabe);
						Display.write("Angabe", p.stationLine1, p.stationLine2);
						break;
					case Status.ERLAUBNIS:
						erlaubnis();
						break;
					case Status.WIDERRUF:
						widerruf();
						break;
					case Status.NOTHALT:
						nothalt();
						break;
					case Status.NOTHALT_OK:
						Display.write("Nothalt OK", "", "");
						break;
					case Status.ABGEMELDET:
						abmelden();
						break;
					case Status.ZIEL:
						ziel();
						break;
					case Status.INFO:
						info();
						break;
					case Status.LERN:
						lern();
						break;
					case Status.ES221:
						es221();
						break;
					case Status.ALARM:
						alarm();
						break;
					case Status.ES_RDY:
						esRdy();
						break;
					case Status.ES_VERSCHUB:
						esVerschub();
						break;
					case Status.DEAKT:
						deakt();
						break;

					default:
						Display.write(0, "STATE          ");
						Display.intVal(40, Status.state);

				}
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

		updateStates();
		if (!check()) return false;


// Dbg.intVal(com.jopdesign.sys.Native.getSP());
		return true;
	}

	/**
	 * 'calculate' what to check from the state we are in.
	 *
	 */
	private void updateStates() {
		
		if (Status.melNr>0) {
			Flash.Point p = Flash.getPoint(Status.melNr);
			checkDirection = p.checkDirection;
			// Stillstand: Enable checkMove
			if (Gps.speed<Gps.MIN_SPEED && Status.state!=Status.ERLAUBNIS) {
				checkMove = p.checkMove;
			}	
		}
		
		if (Status.state!=Status.ERLAUBNIS) {
			checkDirection = false;
		}
		
		
	}
	/**
	*	Check for error conditions.
	*/
	private boolean check() {

		//
		//	check for MelNr range and direction
		//		direction check also in ZIEL
		//
		if (checkMelnr && Status.von<=0 && Status.state!=Status.ALARM
				&& Status.melNr!=-1) {

			synchronized (Status.dirMutex) {
				if (Status.direction==Gps.DIR_UNKNOWN) {
					if (Status.melNrZiel > Status.melNrStart) {		// going from left to rigth.
						Status.direction = Gps.DIR_FORWARD;
					} else if (Status.melNrZiel < Status.melNrStart) {		// going from left to rigth.
						Status.direction = Gps.DIR_BACK;
					}
				}
			}

			if (Status.direction==Gps.DIR_FORWARD) {		// going from left to rigth.
				// check Melderaum
				if (Status.melNr<Status.melNrStart || Status.melNr>Status.melNrZiel) {
					stateAfterQuit = Status.state;
					Status.state = Status.ALARM;
					alarmType = Cmd.ALARM_UEBERF;
					Status.melNrStart = Status.melNr;
					Status.melNrZiel = Status.melNr;
					return false;
				}
				// check direction
				if (checkDirection && Gps.direction==Gps.DIR_BACK &&
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
					stateAfterQuit = Status.state;
					Status.state = Status.ALARM;
					alarmType = Cmd.ALARM_UEBERF;
					Status.melNrStart = Status.melNr;
					Status.melNrZiel = Status.melNr;
					return false;
				}
				// check direction
				if (checkDirection && Gps.direction==Gps.DIR_FORWARD &&
					!(Status.art==Status.ZUG_NF && Status.melNr==Status.melNrZiel)) {
						stateAfterQuit = Status.state;
// FERL bleibt
//						stateAfterQuit = Status.ANM_OK;
						Status.state = Status.ALARM;
						alarmType = Cmd.ALARM_RICHTUNG;
						return false;					
				}
			}
		}
		// every state different form Erlaubnis/Verschub.. check moving
		// except Verschub, Info, Lern, NOTHALT (?...?)
		// Status.von > 0 means Verschub
		if (Status.von<=0 && Status.state!=Status.ALARM &&
			Status.state!=Status.NOTHALT && Status.state!=Status.NOTHALT_OK &&
			Status.state!=Status.INFO && Status.state!=Status.LERN &&
			Status.state!=Status.ES_VERSCHUB &&
			checkMove && Gps.speed>Gps.MIN_SPEED) {
			stateAfterQuit = Status.state;
			Status.state = Status.ALARM;
			alarmType = Cmd.ALARM_FAEHRT;
			checkMove = false;		// disable further Alarms
			return false;
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
		

		//
		//	Ziel erreicht
		//
		if (Status.state == Status.ERLAUBNIS && 
			Status.melNr == Status.melNrZiel &&
			Status.art == Status.ZUG_NORMAL) {

			Status.state = Status.ZIEL;
			if (Status.connOk) {
				Comm.melnrCmd(Cmd.ANZ, Status.strNr, Status.melNr);		
			}

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
		boolean bereit = false;
		
		if (Status.state==Status.DL_CHECK ||
			Status.state==Status.INIT  ||
			Status.state==Status.GPS_OK  ||
			Status.state==Status.CONNECT) {
				bereit = true;
			}


// System.out.println("Menue");
		while (loop()) {

			if (Status.esMode) {
				Display.write("Betriebsfunktion:", mnuESTxt[cnt],"");
			} else if (bereit) {
				Display.write("Betriebsfunktion:", mnuBereit[cnt],"");				
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
				} else if (bereit) {
					if (cnt==MNU_BEREIT_CNT) cnt=0;	
				} else {
					if (cnt==MNU_CNT) cnt=0;			
				}
			}
			if (val==Keyboard.E) {
				Status.dispMenu = false;
				if ((Status.state==Status.NOTHALT) && cnt!=4) {
					Display.write("", "Nicht möglich", "(Quitt. [E])");
					waitEnter();
					return;
				}
			
				if (Status.esMode) {
					if (cnt==0) {
						Status.state = Status.ES_RDY;
					} else if (cnt==1) {
						Status.state = Status.ES_VERSCHUB;
					} else if (cnt==2) {
						Display.write("Neustart", "", "");
						restart();
					}
				} else if (bereit) {
					if (cnt==0) {
						Status.state = Status.DEAKT;
					} else if (cnt==1) {
						Display.write("Neustart", "", "");
						restart();
					}
				} else {
					if (cnt==0) {
						ankunft();
						return;
					} else if (cnt==1) {
						verlassen();
						return;
					} else if (cnt==2) {
						Status.state = Status.INFO;
					} else if (cnt==3) {
						if (!Status.isMaster) {
							Display.write("", "Nicht erlaubt", "");
							waitEnter();
							return;
						}
						Status.state = Status.LERN;
					} else if (cnt==4) {
						Status.state = Status.ES221;
					} else if (cnt==5) {
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
	 *
	 */
	private void waitForGps() {

System.out.println("waitGps");
		int tim;
		
		Gps.wait = false;
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
				Display.write("GPS gestört!", "", "(Quitt. [E])");
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
			if (Gps.fix!=0) {
				break;
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
		
		Status.state = Status.GPS_OK;

	}

	/**
	 * GPS is ready, we are waiting for a valid Strecke
	 * and a valid melNr.
	 */
	private void bereit() {
		
		// wait for GPS melnr found
		for (;;) {
			if (Status.melNr>0) {
				// now we can check for a change of Melnr
				checkMelnr = true;
				Status.melNrStart = Status.melNr;
				Status.melNrZiel = Status.melNr;
				break;
			}
			if (Status.strNr>0) {
				Display.write("Betriebsbereit", "Strecke ", Status.strNr, "");
			} else {
				Display.write("Betriebsbereit", "","");				
			}
			if (Status.strNr<=0 && Status.selectStr) {
				enterStrNr();
			}
			if (!loop()) return;
		}
		
		if (Flash.getIp()!=0) {
			// that's a ZLB Strecke
			Status.state = Status.CONNECT;
		} else {
			Status.esMode = true;
			Status.state = Status.ES_RDY;
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
						Flash.loadStr(nr);
						if (Flash.getIp()==0) {
							Flash.esStr();
						}
						break;
					}
				}
				if (i==cnt) {
					Display.write("Streckennummer","ungültig","(Quitt. [E])");
					Led.shortBeep();
					waitEnterOnly();
					nr = -1;
				}
			}
		}
		Status.strNr = nr;
	}

	private void queryDownload() {

		int tim = Timer.getSec()+DL_TIMEOUT;
System.out.println("Check download server");
		
		// Isn't Flash.java a strange point for communication start!
		if (!Flash.startComm(DL_STRNR)) {
			System.out.println("kein Download Server");
			Status.state = Status.INIT;
			return;
		}
		Display.write("Verbindungsaufbau", "Download check", "(Bitte warten)");
		
		// wait for ip link established
		for (;;) {
			if (ipLink.getIpAddress()!=0) {
				break;
			}
			if (Timer.secTimeout(tim)) {
System.out.println("Link timeout");
				break;
			}
			loop();
		}
		
		Display.write("Download check", "", "");
		Display.ipVal(40, Comm.dst_ip);

		// CONN is sent in check()
		// Here we wait for the reply
		for (;;) {
			if (Status.connOk) {
				if (Status.download) {
					// go the normal way to download
					Status.state = Status.FDL_CONN;
					return;
				} else {
					break;
				}
			}
			if (Timer.secTimeout(tim)) {
System.out.println("Download server connect timeout");
				break;
			}
			loop();
		}
		// we fall through when there is no download
		// or a connection timeout
		// disconnect and wait for GPS
		Status.connOk = false;
		connSent = false;
		Status.commErr = 0;  // ignore it
		ipLink.disconnect();
		Comm.dst_ip = 0;
		Status.state = Status.INIT;
	}

	
	
	private void startConn() {

		Display.write("Verbindungsaufbau", "", "");
		
		// Isn't Flash.java a strange point for communication start!
		Flash.startComm(Status.strNr);
		
		// wait for ip link established
		while (loop()) {
			int cnt = ipLink.getConnCount();
			if (cnt!=0) {
				Display.write(20, "Versuch ", cnt);			
			}
			if (ipLink.getIpAddress()!=0) {
				break;
			}
		}
		
		Display.write("Verbinden zu", "", "");
		Display.ipVal(40, Comm.dst_ip);
		// CONN is sent in check()
		// Here we wait for the reply
		while (loop()) {
			if (Status.connOk) {
				Status.state = Status.FDL_CONN;
				break;
			}
		}
	}

	/**
	 * returns false if cancelled by pressing 'C'
	 *
	 */
	private boolean verschub() {

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
			return true;
		}
		// Status.state change in loop()
		if (Status.state!=Status.FDL_CONN || Status.dispMenu) return true;

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
			int val = Keyboard.rd();
			if (val==Keyboard.C) {
				Status.von=0;
				Status.bis=0;
				return false;
			} else if (val==Keyboard.B) {
				Keyboard.unread(val);
				loop();
				return true;
			}

			// Status.state change in loop()
			if (Status.state!=Status.FDL_CONN || Status.dispMenu) return true;
		}
		return true;
	}

	private void anmelden() {

		int nr, val, tim;

// System.out.println("Anmelden");
		// load default strings for Verschub
		Flash.loadStrNames(Status.strNr, 0, 0);

		boolean askVerschub = verschub();
		if (Status.state!=Status.FDL_CONN || Status.dispMenu) return;
		val = 0;
		Status.art = 0;
		nr = 0;
		tim = Timer.getTimeoutSec(5);

		// Strecke is known!!!

		while (loop()) {

			if (askVerschub && Timer.timeout(tim)) {
				askVerschub = verschub();
				// Status.state change in verschub()
				if (Status.state!=Status.FDL_CONN || Status.dispMenu) return;
				tim = Timer.getTimeoutSec(5);
			}

			Flash.Point p = Flash.getPoint(Status.melNr);
			if (p==null || !(p.anmelden)) {
				Display.write("Anmelden bei", "dieser Position", "nicht möglich");				
			} else {
				Display.write("Anmelden", "Strecke ", Status.strNr, "Zug: 1, Nf: 2");

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

			// check for going back to 'Bereit'
			if (Gps.changeToBereit) {
				reset();
			}
		}
		// Status.state change in loop()
		if (Status.state!=Status.FDL_CONN || Status.dispMenu) return;

		Status.zugNr = nr;
		Status.art = val;

		if (val==1) {
			Display.write("StreckenNr: ", Status.strNr, "ZugNr: ",Status.zugNr ,
					"(Anmelden mit [E])");
		} else {
			Display.write("StreckenNr: ", Status.strNr, "Nebenfahrt: N", Status.zugNr,
					"(Anmelden mit [E])");
		}


		while (loop()) {
			val = Keyboard.rd();
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				continue;
			}
			if (val==Keyboard.E) {
				Display.write("Anmelden", "", "(bitte warten)");
				if (Status.state!=Status.FDL_CONN) return;
				// send anmelden
				Comm.anmelden(Status.art, nr, Status.strNr, Status.melNr);
				// and wait for a reply of ANM
				// and a ANMOK
				while (loop()) {
					if (Status.anmOk) {
						Status.state = Status.ANM_OK;
						Status.melNrStart = Status.melNr;
						Status.melNrZiel = Status.melNr;
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
		// In ES mode or when checking the download
		// server we just ignore the communication error
		if (Status.esMode || Status.state == Status.DL_CHECK) {
			Status.commErr = 0;
System.out.println("comm err ignored");
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
			Display.write("FDL Rechner", "antwortet nicht", "(FDL verständigen)");
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

Dbg.wr("Alarm ");
Dbg.intVal(alarmType);
Dbg.lf();
		if (alarmType==Cmd.ALARM_UEBERF) {
			Display.write("", "ZIEL ÜBERFAHREN", "");
		} else if (alarmType==Cmd.ALARM_FAEHRT) {
			Display.write("KEINE", "FAHRERLAUBNIS", "");
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
		if (Status.connOk) {
			Comm.alarm(Status.strNr, Status.melNr, alarmType);			
		}
		for (;;) {
			// only NOTHAL overwrites an Alarm
			if (Status.state == Status.NOTHALT) return;
			// wait for Enter to quit Alarm
			if (waitEnter()) break;
		}
		Led.alarmOff();
		if (Status.connOk) {
			Comm.alarm(Status.strNr, Status.melNr, 0);			
		}
		//
		//	update state with stateAfterQuit only if
		//	state did not change since alarm
		//
		if (Status.state==Status.ALARM) {
			Status.state = stateAfterQuit;
			if (Status.state==Status.ZIEL) {
				Status.state = Status.ANM_OK;
			}
		}
	}

	private void erlaubnis() {

// Dbg.wr("Erlaubnis\n");
		checkMove = false;
		Flash.Point p = Flash.getPoint(Status.melNrZiel);
		Display.write("Fahrerlaubnis", p.stationLine1, p.stationLine2);

/* 2005-08-19: wird nicht mehr verwendet
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
*/
	}

	private void ziel() {

		Flash.Point p = Flash.getPoint(Status.melNr);
		if (Status.esMode) {
			Display.write("Ziel erreicht:", p.stationLine1, "(HP-Ausw. Pfeilt.)");
			setGpsData();
			tmpStr.append("Ziel erreicht: ");
			tmpStr.append(Status.melNr);
			tmpStr.append("\n");
			Flash.log(tmpStr);
			if (waitAnyKey()) {
				Status.state = Status.ES_RDY;				
			}
		} else {
			Display.write("Ziel erreicht:", p.stationLine1, p.stationLine2);
		}
	}


	private void widerruf() {

// System.out.println("Widerruf");
		Display.write("Fahrtwiderruf", "ZugNr: ", Status.zugNr,
					"(Quitt. mit [E])");
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
		Display.write("", "NOTHALT", "");
		Led.alarm();
		// wait for Enter
		for(;;) {
			loop();
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
		Display.write("Abmelden", "", "(bitte warten)");
		restart();
	}

	private void restart() {

		if (Status.esMode) {
			setGpsData();
			tmpStr.append("Neustart: ");
			tmpStr.append(Status.melNr);
			tmpStr.append("\n");
			Flash.log(tmpStr);
		}
		reset();
	}
	
	private void deakt() {
		
		Display.write("Deaktiviert", "", "Aktivieren mit [E]");
		setGpsData();
		tmpStr.append("Deaktiviert: ");
		tmpStr.append(Status.melNr);
		tmpStr.append("\n");
		Flash.log(tmpStr);
		initVals();
		waitEnterOnly();
		reset();
		
	}
	
	/**
	 * reset() is without Logging in ES mode
	 *
	 */
	private void reset() {
		// wait some time to send outstanding messages
		// and reply
		int tim = Timer.getTimeoutSec(4);
		while (!Timer.timeout(tim)) {
			loop();
		}
		// wait for reset
		Main.reset = true;
		for (;;) {
			loop();
		}		
	}

	private void ankunft() {
		
		int tim;

// System.out.println("AnkVerl");
		if (Status.melNr<=0 || Status.melNrStart<=0 || Status.melNrZiel<=0 ||
				Status.direction == Gps.DIR_UNKNOWN) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}
		Flash.Point p = Flash.getPoint(Status.melNr);
		if (p==null) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}

		if (Status.direction == Gps.DIR_FORWARD) {		// going from left to rigth.
			while (p!=null && !p.station) {
				p = p.getPrev();
			}
		} else {
			while (p!=null && !p.station) {
				p = p.getNext();
			}
		}
		if (p==null) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}
		Display.write("Ankunftsmeldung bei", p.stationLine1, "(Absenden mit [E])");

		if (!waitEnter()) return;

		Display.write("Ankunftsmeldung OK", p.stationLine1, "");

		tim = Timer.getTimeoutSec(5);
		Status.ankunftOk = false;
		Comm.melnrCmd(Cmd.ANK, Status.strNr, p.melnr);
		// and wait for a reply from FDL
		while (loop()) {
			if (Status.ankunftOk && Timer.timeout(tim)) {
				return;
			}
		}
	}

	private void verlassen() {
		
		int tim;

// System.out.println("AnkVerl");
		if (Status.melNr<=0 || Status.melNrStart<=0 || Status.melNrZiel<=0 ||
				Status.direction == Gps.DIR_UNKNOWN) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}
		Flash.Point p = Flash.getPoint(Status.melNr);
		if (p==null || !p.verlassen) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}

		if (Status.direction == Gps.DIR_FORWARD) {		// going from left to rigth.
			while (p!=null && !p.station) {
				p = p.getPrev();
			}
		} else {
			while (p!=null && !p.station) {
				p = p.getNext();
			}
		}
		if (p==null) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}
		Display.write("Verl.meldung nach", p.stationLine1, "(Absenden mit [E])");

		if (!waitEnter()) return;

		Display.write("Verl.meldung OK", p.stationLine1, "");


		tim = Timer.getTimeoutSec(5);
		Status.verlassenOk = false;
		Comm.melnrCmd(Cmd.VERL, Status.strNr, p.melnr);
		// and wait for a reply from FDL
		while (loop()) {
			if (Status.verlassenOk && Timer.timeout(tim)) {
				return;
			}
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
		checkMelnr = false;
		if (Status.connOk) {
			Comm.charlyStatus(Cmd.INFO_MODE, 1);
		}
		// wait for Enter or 'C'
		while (loop()) {
			i = Keyboard.rd();
			if (i==Keyboard.E || i==Keyboard.C) {
				Comm.charlyStatus(Cmd.INFO_MODE, 0);
				reset();
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
		checkMelnr = false;

		Status.strNr = getNumber(8, 3);
		if (Status.strNr == -1) return;
		Flash.loadStr(Status.strNr);

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
				reset();
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
		// we will keep the connection up
		Comm.alarm(Status.strNr, Status.melNr, Cmd.ALARM_ES221);
		
System.out.println("esInit");
		Status.esMode = true;
		// question: when shall be do the esStr()?
		// is it necessary to do esStr() on ES Strecken befor findStr()?
		synchronized(this) {
			Flash.esStr();			
			Status.melNr = -1;
			// disable till Gps finds the new Melnr for the
			// changed Strecke
			checkMelnr = false;
		}
		
		int melnr = Flash.getFirst(Status.strNr);
		Flash.Point p = Flash.getPoint(melnr);
		if (p==null) {
			Display.write("Keine ES-mode", "Streckendaten", "");
			while (loop()) {
				;
			}
			return;
		}
		Status.state = Status.ES_RDY;
		
		for (;;) {
			if (Status.melNr>0) {
				// now we can check for a change of Melnr
				checkMelnr = true;
				Status.melNrStart = Status.melNr;
				Status.melNrZiel = Status.melNr;
				break;
			}
			loop();
		}
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
		

		int val = 0;
		int i = 0;
		
System.out.println("ES Rdy");
		int melnr = Flash.getFirst(Status.strNr);
		Flash.Point p = Flash.getPoint(melnr);

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

			// check for going back to 'Bereit'
			if (Gps.changeToBereit) {
				reset();
			}

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
		checkMelnr = false;
		while (loop()) {
			Status.melNrStart = Status.melNr;
			Status.melNrZiel = Status.melNr;
		}
		checkMelnr = true;

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
	*	wait for any key press (Ziel in ES-mode)
	*/
	private boolean waitAnyKey() {

		int val;
		int state = Status.state;
		while (loop()) {
			val = Keyboard.rd();
			if (val!=-1) {
				return true;
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
	*	return false if 'C'
	*/
	private boolean waitEnter(int timeout) {

		int val;
		int tim;

		tim = Timer.getTimeoutSec(5);
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
			// timout expired
			if (Timer.timeout(tim)) {
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
