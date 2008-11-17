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

	// values for state
	static final int DL_CHECK = -1;
	static final int ANM_WAIT = 19;
	static final int DEAKT = 18;
	static final int ES_VERSCHUB = 17;
	static final int ES_RDY = 16;
	static final int ALARM = 15;
	static final int ES221 = 14;
	static final int LERN = 13;
	static final int INFO = 12;
	static final int ZIEL = 11;
	static final int NOTHALT_OK = 10;
	static final int NOTHALT = 9;
	static final int ABGEMELDET = 8;
	static final int WIDERRUF_OK = 7;
	static final int WIDERRUF = 6;
	static final int ERLAUBNIS = 5;
	static final int ANM_OK = 4;
	static final int FDL_CONN = 3;
	static final int CONNECT = 2;
	static final int GPS_OK = 1;
	static final int INIT = 0;

	/**
	 * State of the Logic
	 */
	volatile static int state;
	
	/**
	 * We are in a manual mode
	 */
	volatile boolean hilfsbtr;

	/**
	*	Next state after alarm quit.
	*/
	private int stateAfterQuit;

	private LinkLayer ipLink;
	private boolean connSent;
	
	private boolean checkMelnr;
	/**
	 *	Richtungsueberwachung
	 */
	private boolean checkDirection;
	/**
	 *	Bewegungsueberwachung bei keiner Fahrerlaubnis
	 */
	private boolean checkMove;
	
	/**
	 * Verschub is active and not canceled by pressing 'C'. 
	 */
	private boolean isVerschub;
	
	private int alarmType;
	
	private boolean alarmZielQuit;
	private boolean alarmFaehrtQuit;
	private boolean alarmRichtungQuit;
	private boolean alarmMlrQuit;

	private int[] buf;
	// length is one display line without status character
	private static final int BUF_LEN = 19;

	private String[] mnuBereit;
	private static final int MNU_BEREIT_CNT = 2;
	private String[] mnuTxt;
	private static final int MNU_CNT = 8;
	private String[] mnuESTxt;
	private static final int MNU_ES_CNT = 3;
	
	private String bsyIndication;
	private StringBuffer tmpStr;
	
	static final int DL_STRNR = 999;
	static final int DL_TIMEOUT = 60;
	static final int CHECK_TIMEOUT = 3;
	
	static boolean dlChecked;


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
		mnuTxt[6] = "Hilfsbetrieb";
		mnuTxt[7] = "Fahrerlaubnis";
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
		Logic.state = Logic.INIT;
		dlChecked = false;

		Status.selectStr = false;
		Status.doCommAlarm = false;
		Status.direction = Gps.DIR_UNKNOWN;

		Status.dispMenu = false;
		
		hilfsbtr = false;

		stateAfterQuit = Logic.state;
		connSent = false;
		checkMelnr = false;
		checkMove = false;
		checkDirection = false;
		Status.esMode = false;
	}

	public void run() {

		int old_state = Logic.state;
		
		for (;;) {

			//
			// Beep on every change
			//
			if (Logic.state != old_state) {
				Led.shortBeep();
				old_state = Logic.state;
			}
			
			if (Status.commErr!=0) {
				commError();
			} else if (Status.dispMenu) {
				menu();
			} else if (Main.state.isDownloading() && Logic.state!=ES221
					&& Logic.state!=ES_RDY && !Status.esMode) {
				download();
			} else {

				switch (Logic.state) {

					case Logic.DL_CHECK:
						queryDownload();
						break;
					case Logic.INIT:
						waitForGps();
						break;
					case Logic.GPS_OK:
						bereit();
						break;
					case Logic.CONNECT:
						startConn();
						break;
					case Logic.FDL_CONN:
						anmelden();
						break;
					case Logic.ANM_WAIT:
						anmeldenWait();
						break;
					case Logic.ANM_OK:
						if (Status.esMode) {
							// we get into ANM_OK after an alarm the removes
							// our Fahrerlaubnis
							Logic.state = Logic.ES_RDY;
						} else {
							Display.write("Anmeldung OK", "", "");
						}
						break;
					case Logic.ERLAUBNIS:
						// load the names with the known direction
						// TODO: with additional points we can avoid this
						Flash.loadStrNames(Main.state.strnr, Main.state.start, Main.state.end);
						erlaubnis();
						break;
					case Logic.WIDERRUF:
						widerruf();
						break;
					case Logic.NOTHALT:
						nothalt();
						break;
					case Logic.NOTHALT_OK:
						Display.write("Nothalt OK", "", "");
						break;
					case Logic.ABGEMELDET:
						abmelden();
						break;
					case Logic.ZIEL:
						ziel();
						break;
					case Logic.INFO:
						info();
						break;
					case Logic.LERN:
						lern();
						break;
					case Logic.ES221:
						es221();
						break;
					case Logic.ALARM:
						alarm();
						break;
					case Logic.ES_RDY:
						esRdy();
						break;
					case Logic.ES_VERSCHUB:
						esVerschub();
						break;
					case Logic.DEAKT:
						deakt();
						break;

					default:
						Display.write(0, "STATE          ");
						Display.intVal(40, Logic.state);

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
		Display.setInetOk(ipLink.getIpAddress()!=0 && Status.connOk);
		Display.setHbOk(hilfsbtr);
		Display.setGpsOk(Gps.fix>0);
		Display.setDgpsOk(Gps.fix==2);
		
		// reset Hilfsbetrieb when connection is ok
		// and ignore flag is not set
		if (Status.connOk && !State.ignore) {
			// TODO some synchronization?
			hilfsbtr = false;
		}

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
	 * Send a postion change event from State.
	 * Reset alarm quit fields.
	 */
	void posChanged() {
		alarmFaehrtQuit = false;
		alarmRichtungQuit = false;
		alarmZielQuit = false;
		alarmMlrQuit = false;
	}

	/**
	 * 'calculate' what to check from the state we are in.
	 *
	 */
	private void updateStates() {
		
		if (Main.state.getPos()>0) {
			Flash.Point p = Flash.getPoint(Main.state.getPos());
			if (p!=null) {
				checkDirection = p.checkDirection;
				// Stillstand: Enable checkMove
				if (Gps.speed<Gps.MIN_SPEED && Logic.state!=Logic.ERLAUBNIS) {
					checkMove = p.checkMove;
				}					
			}
		}
		
		if (Logic.state!=Logic.ERLAUBNIS) {
			checkDirection = false;
		}
		
		
	}
	/**
	*	Check for error conditions.
	*/
	private boolean check() {
		
		State state = Main.state;
		int pos, start, end;
		// TODO: use the following locals for the checks
		synchronized (state) {
			pos = state.getPos();
			start = state.start;
			end = state.end;
		}
		

		//
		//	check for MelNr range and direction
		//		direction check also in ZIEL
		//
		if (checkMelnr && !isVerschub && Logic.state!=Logic.ALARM
				&& pos!=-1) {

			synchronized (Status.dirMutex) {
				if (Status.direction==Gps.DIR_UNKNOWN) {
					if (Main.state.end > Main.state.start) {		// going from left to right.
						Status.direction = Gps.DIR_FORWARD;
					} else if (Main.state.end < Main.state.start) {		// going from left to right.
						Status.direction = Gps.DIR_BACK;
					}
				}
			}

			if (Status.direction==Gps.DIR_FORWARD) {		// going from left to rigth.
				// check direction with melnr
				if (checkDirection && pos<Main.state.start && !alarmRichtungQuit) {				
					// FERL bleibt
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_RICHTUNG;
					return false;						
				}
				// check melnr in the other direction
				if (pos<Main.state.start && Main.state.start!=0 && !alarmMlrQuit) {
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_MLR;
					return false;
				}
				// check Melderaum Ziel
//				if (Main.state.pos<Main.state.start || Main.state.pos>Main.state.end) {
				// change 13.12.2006 - Ziel only in the direction
				if (pos>Main.state.end && Main.state.end!=0 && !alarmZielQuit) {
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_UEBERF;
					return false;
				}
				// check direction
				if (checkDirection && Gps.direction==Gps.DIR_BACK &&
					!(state.type==State.TYPE_NF && pos==Main.state.end) &&!alarmRichtungQuit) {

					// FERL bleibt
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_RICHTUNG;
					return false;					
				}
			} else {										// going from right to left
				// check direction with melnr
				if (checkDirection && pos>Main.state.start && !alarmRichtungQuit) {				
					// FERL bleibt
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_RICHTUNG;
					return false;						
				}
				// check melnr in the other direction
				if (pos>Main.state.start && Main.state.start!=0 && !alarmMlrQuit) {
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_MLR;
					return false;
				}
				// check Melderaum
				// change 13.12.2006 - Ziel only in the direction
//				if (Main.state.pos>Main.state.start || Main.state.pos<Main.state.end) {
				if (pos<Main.state.end && Main.state.end!=0 && !alarmZielQuit) {
					stateAfterQuit = Logic.state;
					Logic.state = Logic.ALARM;
					alarmType = State.ALARM_UEBERF;
					return false;
				}
				// check direction
				if (checkDirection && Gps.direction==Gps.DIR_FORWARD &&
					!(state.type==State.TYPE_NF && pos==Main.state.end) && !alarmRichtungQuit) {
						stateAfterQuit = Logic.state;
// FERL bleibt
//						stateAfterQuit = Status.ANM_OK;
						Logic.state = Logic.ALARM;
						alarmType = State.ALARM_RICHTUNG;
						return false;					
				}
			}
		}
		// every state different form Erlaubnis/Verschub.. check moving
		// except Verschub, Info, Lern, NOTHALT (?...?)
		if (!isVerschub && Logic.state!=Logic.ALARM &&
			Logic.state!=Logic.NOTHALT && Logic.state!=Logic.NOTHALT_OK &&
			Logic.state!=Logic.INFO && Logic.state!=Logic.LERN &&
			Logic.state!=Logic.ES_VERSCHUB &&
			checkMove && Gps.speed>Gps.MIN_SPEED && !alarmFaehrtQuit &&
			// the following should not be necessary, but for sure
			start==end) {
			
			stateAfterQuit = Logic.state;
			Logic.state = Logic.ALARM;
			alarmType = State.ALARM_FAEHRT;
			checkMove = false;		// disable further Alarms
			return false;
		}
		// check Verschub
		if (isVerschub && Logic.state!=Logic.ALARM && !alarmMlrQuit) {
			if (pos>end || pos<start) {
				Logic.state = Logic.ALARM;
				alarmType = State.ALARM_MLR;
				isVerschub = false;	// clear Verschub
				stateAfterQuit = Logic.FDL_CONN;
				return false;
			}
		}		

		//
		//	Ziel erreicht
		//
		if (Logic.state == Logic.ERLAUBNIS && 
			pos == Main.state.end &&
			(state.type==State.TYPE_ZUG || state.type==State.TYPE_ES221)) {

			Logic.state = Logic.ZIEL;
			if (Status.connOk) {
				Main.state.sendZiel();
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
		boolean download = Main.state.isDownloadSticky();
		
		if (Logic.state==Logic.DL_CHECK ||
			Logic.state==Logic.INIT  ||
			Logic.state==Logic.GPS_OK) {
//			Logic.state==Logic.CONNECT) {
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
				if ((Logic.state==Logic.NOTHALT) && cnt!=4) {
					Display.write("", "Nicht möglich", "(Quitt. [E])");
					waitEnter();
					return;
				}
			
				if (Status.esMode) {
					if (cnt==0) {
						Logic.state = Logic.ES_RDY;
					} else if (cnt==1) {
						Logic.state = Logic.ES_VERSCHUB;
					} else if (cnt==2) {
						if (Main.state.isDownloading()) {
							Display.write("Download", "Nicht erlaubt", "");
							waitEnter();
							return;
						}
						Display.write("Neustart", "", "");
						restart();
					}
				} else if (bereit) {
					if (download) {
						Display.write("Download", "Nicht erlaubt", "");
						waitEnter();
						return;
					}
					if (cnt==0) {
						Logic.state = Logic.DEAKT;
					} else if (cnt==1) {
						Display.write("Neustart", "", "");
						restart();
					}
				} else {
					// only ES mode is allowed
					if (download && cnt!=4) {
						Display.write("Download", "Nicht erlaubt", "");
						waitEnter();
						return;
					}

					if (cnt==0) {
						ankunft();
						return;
					} else if (cnt==1) {
						verlassen();
						return;
					} else if (cnt==2) {
						Logic.state = Logic.INFO;
					} else if (cnt==3) {
						if (!Status.isMaster) {
							Display.write("", "Nicht erlaubt", "");
							waitEnter();
							return;
						}
						Logic.state = Logic.LERN;
					} else if (cnt==4) {
						Logic.state = Logic.ES221;
					} else if (cnt==5) {
						Display.write("Neustart", "", "");
						restart();
					} else if (cnt==6) {
						// Hilfsbetrieb
						if (Status.connOk || Gps.speed>Gps.MIN_SPEED) {
							Display.write("", "Nicht möglich", "");
							waitEnter();							
						} else {
							Display.write("Wechsel zu", "Hilfsbetrieb?", "(Quitt. [E])");
							if (waitEnter()) {
								hilfsbtr = true;
							}							
						}
					} else if (cnt==7) {
						// HB Ferl
						if (!hilfsbtr || Main.state.zugnr==0) {
							Display.write("", "Nicht möglich", "");
							waitEnter();
						} else {
							hbFerl();							
						}
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
		
		Logic.state = Logic.GPS_OK;

	}

	/**
	 * GPS is ready, we are waiting for a valid Strecke
	 * and a valid melNr.
	 */
	private void bereit() {
		
		int tim = Timer.getSec()+CHECK_TIMEOUT;

		// wait for GPS melnr found
		for (;;) {
			if (Main.state.getPos()>0) {
				// now we can check for a change of Melnr
				checkMelnr = true;
				break;
			}
			if (Main.state.strnr>0) {
				Display.write("Betriebsbereit", "Strecke ", Main.state.strnr, "");
			} else {
				Display.write("Betriebsbereit", "","");
				// check for a download server after some seconds
				// when no Strecke found
				if (Timer.secTimeout(tim) && !dlChecked) {
					Logic.state = Logic.DL_CHECK;
					return;
				}
			}
			if (Main.state.strnr<=0 && Status.selectStr) {
				enterStrNr();
			}
			if (!loop()) return;
		}
		
		if (Flash.getIp()!=0) {
			// that's a ZLB Strecke
			Logic.state = Logic.CONNECT;
		} else {
			Status.esMode = true;
			Logic.state = Logic.ES_RDY;
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
						Flash.loadStrNames(nr, 0, 0);

//						if (Flash.getIp()==0) {
//							Flash.esStr();
//						}
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
		Main.state.strnr = nr;
	}

	private void queryDownload() {

		// just check it once from GPS_OK,
		// but goes back to INIT
		dlChecked = true;
		
		int tim = Timer.getSec()+DL_TIMEOUT;
System.out.println("Check download server");
		
		// Isn't Flash.java a strange point for communication start!
		if (!Flash.startComm(DL_STRNR)) {
			System.out.println("kein Download Server");
			Logic.state = Logic.INIT;
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
		Display.ipVal(40, Main.state.destIp);

		// CONN is sent in check()
		// Here we wait for the reply
		for (;;) {
			if (Status.connOk) {
				if (Main.state.isDownloadSticky()) {
					// go the normal way to download
					Logic.state = Logic.FDL_CONN;
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
		Main.state.destIp = 0;
		Logic.state = Logic.INIT;
	}

	
	/**
	 * Start the connection. In Hilsbetrieb the connection start is
	 * 'faked'.
	 */
	private void startConn() {

		Display.write("Verbindungsaufbau", "", "");
		Main.logger.print("start connection");
		
		// Isn't Flash.java a strange point for communication start!
		Flash.startComm(Main.state.strnr);
		
		// wait for ip link established
		while (loop()) {
			int cnt = ipLink.getConnCount();
			if (cnt!=0) {
				Display.write(20, "Versuch ", cnt);			
			}
			if (ipLink.getIpAddress()!=0) {
				break;
			}
			if (hilfsbtr) {
				break;
			}
		}
		
		if (Status.dispMenu) return;
		
		Display.write("Verbinden zu", "", "");
		Display.ipVal(40, Main.state.destIp);
		// CONN is sent in check()
		// Here we wait for the reply
		while (loop()) {
			if (Status.connOk || hilfsbtr) {
				Logic.state = Logic.FDL_CONN;
				break;
			}
		}
	}

	/**
	 * Check Verschub is allowed and display the message.
	 * 
	 * returns true to continue the check.
	 * returns false if canceled by pressing 'C' =>
	 * 		enter zug data
	 *
	 */
	private boolean verschub() {

		State state = Main.state;
		
		int from, to;
		
		synchronized (state) {
			isVerschub = state.isVerschub();
			from = state.start;
			to = state.end;
		}
		
		if (!isVerschub) return true;
		
		// Status.state change in loop()
		if (Logic.state!=Logic.FDL_CONN || Status.dispMenu) return true;

		Display.write("Verschub erlaubt", "", "");

		while (loop()) {
			
			Flash.Point p = Flash.getPoint(from);
			if (p!=null) {
				Display.write(20, p.verschubVon);
			}
			p = Flash.getPoint(to);
			if (p!=null) {
				Display.write(40, p.verschubBis);
			}
			
			// check again
			synchronized (state) {
				isVerschub = state.isVerschub();
				from = state.start;
				to = state.end;
			}
			if (!isVerschub) break;

			int val = Keyboard.rd();
			if (val==Keyboard.C) {
				isVerschub = false;
				return false;
			} else if (val==Keyboard.B) {
				Keyboard.unread(val);
				loop();
				return true;
			}

			// Status.state change in loop()
			if (Logic.state!=Logic.FDL_CONN || Status.dispMenu) return true;
		}
		return true;
	}

	private void anmelden() {

		int nr, val, tim;
		State state = Main.state;

// System.out.println("Anmelden");
		// load default strings for Verschub
		Flash.loadStrNames(Main.state.strnr, 0, 0);

		boolean askVerschub = verschub();
		if (Logic.state!=Logic.FDL_CONN || Status.dispMenu) return;
		val = 0;
		state.type = State.TYPE_UNKNOWN;
		nr = 0;
		tim = Timer.getTimeoutSec(5);

		// Strecke is known!!!

		while (loop()) {

			// stop loop when downloading
			if (state.isDownloadSticky()) {
				return;
			}
			// did we reset and get the data from the ZLB?
			if (state.zugnr!=0 && state.type!=0) {
				if (state.start!=state.end) {
					Logic.state = Logic.ERLAUBNIS;										
				} else {
					Logic.state = Logic.ANM_OK;					
				}
				return;
			}

			if (askVerschub && Timer.timeout(tim)) {
				askVerschub = verschub();
				// Status.state change in verschub()
				if (Logic.state!=Logic.FDL_CONN || Status.dispMenu) return;
				tim = Timer.getTimeoutSec(5);
			}

			Flash.Point p = Flash.getPoint(Main.state.getPos());
			if (p==null || !(p.anmelden)) {
				Display.write("Anmelden bei", "dieser Position", "nicht möglich");				
			} else {
				Display.write("Anmelden", "Strecke ", state.strnr, "Zug: 1, Nf: 2");

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
		if (Logic.state!=Logic.FDL_CONN || Status.dispMenu) return;
		// also return on a comm error
		if (!loop()) return;

		int type = val;

		if (val==1) {
			Display.write("StreckenNr: ", state.strnr, "ZugNr: ",nr ,
					"(Anmelden mit [E])");
		} else {
			Display.write("StreckenNr: ", state.strnr, "Nebenfahrt: N", nr,
					"(Anmelden mit [E])");
		}

		while (loop()) {
			// stop loop when downloading
			if (state.isDownloadSticky()) {
				return;
			}
			val = Keyboard.rd();
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				continue;
			}
			if (val==Keyboard.E) {
				state.zugnr = nr;
				state.type = type;
				Logic.state = Logic.ANM_WAIT;
//				if (Logic.state!=Logic.FDL_CONN) return;
				return;
			} else if (val==Keyboard.C) {
				return;
			}
		}
	}
	
	private void anmeldenWait() {
		
		while (loop()) {
			Display.write("Anmelden", "(bitte warten)", "(Fdl verständigen)");
			// wait for Anmelden OK or we already got a FERL
			if (Events.anmeldenOk || Main.state.start!=0 || hilfsbtr) {
				Logic.state = Logic.ANM_OK;
				Events.anmeldenOk = false;
				if (Main.state.start!=Main.state.end) {
					// we also got a FERL
					Logic.state = Logic.ERLAUBNIS;
				}
				break;
			} else if (Logic.state == Logic.ABGEMELDET) {
				abmelden();
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
	
	final static int COMM_NOERR = 0;
	final static int COMM_FDLERR = 1;
	final static int COMM_SHORT = 2;
	final static int COMM_WRCMD = 3;
	final static int COMM_WRBGID = 4;
	
	private void commError() {

		// In ES mode or when checking the download
		// server we just ignore the communication error
		// 19.4.2008 also ZLB not responsing ignored!
		if (Status.esMode || Logic.state == Logic.DL_CHECK || Status.commErr==COMM_FDLERR) {
			Status.commErr = 0;
System.out.println("comm err ignored");
			return;
		}
		if (Status.doCommAlarm) {
			int nr = Status.commErr;
			Led.shortBeep();
			Led.startBlinking();
			if (nr==COMM_SHORT) {
				Display.write("FDL Msg Fehler", "Paket zu kurz", "");
			} else if (nr==COMM_WRBGID) {
				Display.write("FDL Msg Fehler", "falsches CMD", "");
			} else if (nr==COMM_WRBGID) {
				Display.write("FDL Msg Fehler", "falsche bgid", "");
			} else {
				Display.write("FDL Rechner", "antwortet nicht", "(FDL verständigen)");
			}
			// Display.intVal(40, Status.commErr);			
		}

		Status.connOk = false;
		connSent = false;
		// clear error
		Status.commErr = 0;

		if (Status.doCommAlarm) {
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
				}
				if (Keyboard.rd()==Keyboard.E) break;
				if (Status.connOk) break;
			}
			Led.stopBlinking();			
		}

		// keep Status even if not connected
/*
		Status.state = Status.INIT;
		Main.state.strnr = 0;
		Main.state.pos = 0;
		Main.state.posSent = 0;
*/
	}

	/**
	*	Handle the alarms.
	*/
	private void alarm() {

Dbg.wr("Alarm ");
Dbg.intVal(alarmType);
Dbg.lf();
		if (alarmType==State.ALARM_UEBERF) {
			Display.write("", "ZIEL ÜBERFAHREN", "");
		} else if (alarmType==State.ALARM_FAEHRT) {
			Display.write("KEINE", "FAHRERLAUBNIS", "");
		} else if (alarmType==State.ALARM_RICHTUNG) {
			Display.write("Falsche", "Richtung", "");
		} else if (alarmType==State.ALARM_MLR) {
			Display.write("Falscher", "Melderaum", "");
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
			Main.state.setAlarm(alarmType);
		}
		for (;;) {
			// only NOTHAL overwrites an Alarm
			if (Logic.state == Logic.NOTHALT) return;
			// wait for Enter to quit Alarm
			if (waitEnter()) break;
		}
		if (alarmType==State.ALARM_UEBERF) {
			alarmZielQuit = true;
		} else if (alarmType==State.ALARM_FAEHRT) {
			alarmFaehrtQuit = true;
		} else if (alarmType==State.ALARM_RICHTUNG) {
			alarmRichtungQuit = true;
		} else if (alarmType==State.ALARM_MLR) {
			alarmMlrQuit = true;
		} else {
			Display.write("Alarm", "Nummer", alarmType, "");
		}
		Led.alarmOff();
		if (Status.connOk) {
			// reset alarm - even is not quit?
			Main.state.setAlarm(0);
		}
		//
		//	update state with stateAfterQuit only if
		//	state did not change since alarm
		//
		if (Logic.state==Logic.ALARM) {
			Logic.state = stateAfterQuit;
			if (Logic.state==Logic.ZIEL) {
				Logic.state = Logic.ANM_OK;
			}
		}
	}

	private void erlaubnis() {

		checkMove = false;
		Flash.Point p = Flash.getPoint(Main.state.end);
		if (p==null) {
			Display.write("Falsche", "Fahrerlaubnis", "");				
		} else {
			Display.write("Fahrerlaubnis", p.stationLine1, p.stationLine2);			
		}

	}

	private void ziel() {

		Flash.Point p = Flash.getPoint(Main.state.getPos());
		if (p==null) return;
		if (Status.esMode) {
			Display.write("Ziel erreicht:", p.stationLine1, "(HP-Ausw. Pfeilt.)");
			setGpsData();
			tmpStr.append("Ziel erreicht: ");
			tmpStr.append(Main.state.getPos());
			tmpStr.append("\n");
			Flash.log(tmpStr);
			if (waitAnyKey()) {
				Logic.state = Logic.ES_RDY;				
			}
		} else {
			Display.write("Ziel erreicht:", p.stationLine1, p.stationLine2);
		}
	}


	private void widerruf() {

// System.out.println("Widerruf");
		Display.write("Fahrtwiderruf", "ZugNr: ", Main.state.zugnr,
					"(Quitt. mit [E])");
		Led.startBlinking();
		// wait for Enter
		while (loop()) {
			if (Keyboard.rd()==Keyboard.E) {
				Main.state.fwrQuit();
				Display.write("Fahrtwiderruf OK", "", "");
				Logic.state = Logic.ANM_OK;
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
				Main.state.nothaltQuit();
				Led.alarmOff();
				Logic.state = Logic.NOTHALT_OK;
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
			tmpStr.append(Main.state.getPos());
			tmpStr.append("\n");
			Flash.log(tmpStr);
		}
		reset();
	}
	
	private void deakt() {
		
		Display.write("Deaktiviert", "", "Aktivieren mit [E]");
		setGpsData();
		tmpStr.append("Deaktiviert: ");
		tmpStr.append(Main.state.getPos());
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
		int tim = Timer.getTimeoutSec(10);
		while (!Timer.timeout(tim)) {
			loop();
			if (State.forceReset) {
				break;
			}
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
		if (Main.state.getPos()<=0 || Main.state.start<=0 || Main.state.end<=0 ||
				Status.direction == Gps.DIR_UNKNOWN) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}
		Flash.Point p = Flash.getPoint(Main.state.getPos());
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

		// minimum display time
		tim = Timer.getTimeoutSec(5);
		Main.state.ankunft(p.melnr);
		// and wait for a reply from FDL
		while (loop()) {
			if (Main.state.ankuftAck() && Timer.timeout(tim)) {
				return;
			}
		}
	}

	private void verlassen() {
		
		int tim;

// System.out.println("AnkVerl");
		if (Main.state.getPos()<=0 || Main.state.start<=0 || Main.state.end<=0 ||
				Status.direction == Gps.DIR_UNKNOWN) {
			Display.write("Bei dieser Position", "keine Meldung", "möglich!");
			waitEnter(5);
			return;
		}
		Flash.Point p = Flash.getPoint(Main.state.getPos());
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

		// minimum display time
		tim = Timer.getTimeoutSec(5);
		Main.state.verlassen(p.melnr);
		// and wait for a reply from FDL
		while (loop()) {
			if (Main.state.verlassenAck() && Timer.timeout(tim)) {
				return;
			}
		}
	}

	private void download() {

		int dlType = -1;
		int percent = -1;
		int cnt = 0;

// System.out.println("Download");
		Display.write("Übertragung", "ES221 möglich", "");
		while (loop()) {
			;
		}
/* not type no percentage
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

*/
	}
	
	private void info() {

		int i, j;

// System.out.println("Infobtrieb");
		Display.write("Infobetrieb", "", "");
		checkMelnr = false;
		Main.state.setInfo();
		// wait for Enter or 'C'
		while (loop()) {
			i = Keyboard.rd();
			if (i==Keyboard.E || i==Keyboard.C) {
				Main.state.resetInfo();
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
			Display.write(20, tmpStr, Main.state.getPos());
			Display.write(40, Gps.text);
		}
	}

	private void lern() {

		int i, val;

// System.out.println("Lern");
		Display.write("Lerne", "Strecke","");
		checkMelnr = false;

		val = Main.state.strnr;
		
		Main.state.strnr = getNumber(8, 3);
		if (Main.state.strnr == -1) return;
		
		if (val!=Main.state.strnr || !Status.connOk) {
			Flash.loadStr(Main.state.strnr);
			Flash.loadStrNames(Main.state.strnr, 0, 0);

			startConn();			
		}
		int melnr = Flash.getFirst(Main.state.strnr);
		if (melnr==-1) {
			Display.write("Strecke", "nicht gefunden", "");
			waitEnterAndInit();
			return;
		}
		// Conn changes to FLD_CONN
		Logic.state = Logic.LERN;
		Main.state.setLern();

		while (loop()) {

			Display.write(0, "Lernbetrieb ", melnr);
			Flash.Point p = Flash.getPoint(melnr);
			if (p==null) {
				Display.write(20, "Falscher Punkt");				
			} else {
				Display.write(20, p.stationLine1);				
			}

			Display.write(40, Gps.text);

			val = Keyboard.rd();
			if (val==-1) {
				continue;
			}

			if (val==Keyboard.C) {
				Main.state.resetLern();
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
				Main.state.lern(melnr, Gps.getLatAvg(), Gps.getLonAvg());
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
		Main.state.setESAlarm();
		
System.out.println("esInit");
		Status.esMode = true;
		// question: when shall be do the esStr()?
		// is it necessary to do esStr() on ES Strecken befor findStr()?
		synchronized(this) {
//			Flash.esStr();			
			Main.state.setPos(-1);
			// disable till Gps finds the new Melnr for the
			// changed Strecke
			checkMelnr = false;
		}
		
		int melnr = Flash.getFirst(Main.state.strnr);
		Flash.Point p = Flash.getPoint(melnr);
		if (p==null) {
			Display.write("Keine ES-mode", "Streckendaten", "");
			while (loop()) {
				;
			}
			return;
		}
		Logic.state = Logic.ES_RDY;
		
		for (;;) {
			if (Main.state.getPos()>0) {
				// now we can check for a change of Melnr
				checkMelnr = true;
				Main.state.start = Main.state.getPos();
				Main.state.end = Main.state.getPos();
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
		int melnr = Flash.getFirst(Main.state.strnr);
		Flash.Point p = Flash.getPoint(melnr);

		for (;;) {
			if (p.station && melnr>=Main.state.getPos()) {
				break;
			}
			i = Flash.getNext(melnr);
			if (i!=-1) {
				melnr = i;
			} else {
				break;
			}
			p = Flash.getPoint(melnr);
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

			// TODO: the following is almost a copy from HB select
			// just use different flags
			boolean found = false;

			if (val==Keyboard.UP) {
				while (!found) {
					i = Flash.getNext(melnr);
					if (i!=-1) {
						melnr = i;
						p = Flash.getPoint(melnr);
						if (p.station) {
							found = true;
						}
					} else {
						break;
					}
				}
			}
			if (val==Keyboard.DOWN) {
				while (!found) {
					i = Flash.getPrev(melnr);
					if (i!=-1) {
						melnr = i;
						p = Flash.getPoint(melnr);
						if (p.station) {
							found = true;
						}
					} else {
						break;
					}
				}
			}


// that's the old ES select with ES strecke change
//			// display only the left point text
//
//			if (val==Keyboard.UP) {
//				i = Flash.getNext(melnr);
//				i = Flash.getNext(i);
//				if (i!=-1) {
//					melnr = i;
//					p = Flash.getPoint(melnr);
//				}
//			}
//			if (val==Keyboard.DOWN) {
//				i = Flash.getPrev(melnr);
//				i = Flash.getPrev(i);
//				if (i!=-1) {
//					melnr = i;
//					p = Flash.getPoint(melnr);
//				}
//			}
			if (val==Keyboard.E) {
				
				// The left point is the station
				// smaller melnr is start
				Main.state.start = Main.state.getPos();
				Main.state.end = melnr;
				Logic.state = Logic.ERLAUBNIS;
				Main.state.type = State.TYPE_ES221;
				Status.direction = Gps.DIR_UNKNOWN;

				setGpsData();
				tmpStr.append("Fahrt von ");
				tmpStr.append(Main.state.start);
				tmpStr.append(" nach ");
				tmpStr.append(Main.state.end);
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
		tmpStr.append(Main.state.strnr);
		tmpStr.append(" - ");
	}

	private void esVerschub() {
		Display.write("ES Verschub", "", "");

		setGpsData();
		tmpStr.append("Verschub: ");
		tmpStr.append(Main.state.getPos());
		tmpStr.append("\n");
		Flash.log(tmpStr);

		Logic.state = Logic.ES_VERSCHUB;
		checkMelnr = false;
		while (loop()) {
			Main.state.start = Main.state.getPos();
			Main.state.end = Main.state.getPos();
		}
		checkMelnr = true;

	}
	
	/**
	 * HB mode ferl
	 *
	 */
	private void hbFerl() {
		

		int val = 0;
		int i = 0;
		
System.out.println("HB Ferl");
		int melnr = Main.state.getPos();
		Flash.Point p = Flash.getPoint(melnr);
		if (p==null) {
System.out.println("np Problem");
			return;
		}

		while (loop()) {

			// check for going back to 'Bereit'
			if (Gps.changeToBereit) {
				reset();
			}

			Flash.loadStrNames(Main.state.strnr, Main.state.getPos(), melnr);

			Display.write("Haltepunkt ausw.:", p.stationLine1, p.stationLine2);

			val = Keyboard.rd();
			if (val==-1) {
				continue;
			}
			if (val==Keyboard.B) {
				Keyboard.unread(val);
				return;
			}

			boolean found = false;
			// select the correct destination when it depends on
			// the direction
			boolean right = melnr > Main.state.getPos();

			if (val==Keyboard.UP) {
				if (melnr==Main.state.getPos()) {
					right = true;
				}
				while (!found) {
					i = Flash.getNext(melnr);
					if (i!=-1) {
						melnr = i;
						p = Flash.getPoint(melnr);
						if (p.station || (right && p.hbRight) || (!right && p.hbLeft)) {
							found = true;
						}
					} else {
						break;
					}
				}
			}
			if (val==Keyboard.DOWN) {
				if (melnr==Main.state.getPos()) {
					right = false;
				}
				while (!found) {
					i = Flash.getPrev(melnr);
					if (i!=-1) {
						melnr = i;
						p = Flash.getPoint(melnr);
						if (p.station || (right && p.hbRight) || (!right && p.hbLeft)) {
							found = true;
						}
					} else {
						break;
					}
				}
			}
			if (val==Keyboard.E) {
				
				Main.state.start = Main.state.getPos();
				Main.state.end = melnr;
				Logic.state = Logic.ERLAUBNIS;
				Main.state.type = State.TYPE_ZUG;
				Status.direction = Gps.DIR_UNKNOWN;

				setGpsData();
				tmpStr.append("HB Fahrt von ");
				tmpStr.append(Main.state.start);
				tmpStr.append(" nach ");
				tmpStr.append(Main.state.end);
				tmpStr.append("\n");
				Flash.log(tmpStr);


				return;
			}
			
			if (val==Keyboard.C) {
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
				Logic.state = Logic.INIT;
				Main.state.strnr = 0;
				Main.state.setPos(0);
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
		int state = Logic.state;
		while (loop()) {
			val = Keyboard.rd();
			if (val!=-1) {
				return true;
			}
			// some State has changed!
			// dont wait anymore
			if (state!=Logic.state) {
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
		int state = Logic.state;
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
			if (state!=Logic.state) {
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
		int state = Logic.state;
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
			if (state!=Logic.state) {
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
