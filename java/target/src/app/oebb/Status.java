package oebb;

/**
*	Status.java: Shared data between Logic and Comm
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*
*/

public class Status {

	// values for state
	static final int DL_CHECK = -1;
	static final int INIT = 0;
	static final int GPS_OK = 1;
	static final int CONNECT = 2;
	static final int FDL_CONN = 3;
	static final int ANM_OK = 4;
	static final int ANGABE = 5;
	static final int ERLAUBNIS = 6;
	static final int WIDERRUF = 7;
	static final int WIDERRUF_OK = 8;
	static final int ABGEMELDET = 9;
	static final int NOTHALT = 10;
	static final int NOTHALT_OK = 11;
	static final int ZIEL = 12;
	static final int INFO = 13;
	static final int LERN = 14;
	static final int ES221 = 15;
	static final int ALARM = 16;

	
	static final int ES_RDY = 17;
	static final int ES_VERSCHUB = 18;
	static final int DEAKT = 19;
	
	volatile static int state;
	
	static final int ZUG_NORMAL = 1;
	static final int ZUG_NF = 2;
	
	/**
	 * wenn esMode is true we work local without
	 * Internet connection.
	 */
	volatile static boolean esMode;


/**
 * Streckennummer vom Benutzer anfordern.
 */
	volatile static boolean selectStr;
/**
*	some error in communication
*/
	volatile static int commErr;
/**
*	Set after CONN/CONN_RPL
*/
	volatile static boolean connOk;
/**
*	Set after CONN/CONN_RPL when download will follow.
*/
	volatile static boolean download;

	volatile static int dlType;
	volatile static int dlPercent;

/**
*	Set after ANM/ANM_RPL and ANMOK
*/
	volatile static boolean anmOk;

/**
*	Set after lern msg got a repley
*/
	volatile static boolean lernOk;

/**
*	Set after ankunft msg got a repley
*/
	volatile static boolean ankunftOk;

/**
*	Set after verlassen msg got a repley
*/
	volatile static boolean verlassenOk;

/**
*	display menu
*/
	volatile static boolean dispMenu;

/**
*	is Lernbetrieb allowed
*/
	volatile static boolean isMaster;

/**
*	aktuelle Strecke
*/
	volatile static int strNr;

/**
*	Zugnummer, wird aber nur für Fahrtwiederruf Text
*	verwendet.
*/
	volatile static int zugNr;

/**
*	aktueller Melderaum
*/
	volatile static int melNr;
/**
*	zuletzt gesendeter Melderaum
*/
	volatile static int melNrSent;
	
	/**
	*	send a communication alarm
	*/
	volatile static boolean doCommAlarm;
/**
*	Startmelderaum
*/
	volatile static int melNrStart;
/**
*	Zielmelderaum
*/
	volatile static int melNrZiel;
	
/**
 *  Angabe Melderaum
 */
	volatile static int angabe;
/**
*	send FERL_QUIT
*/
	volatile static boolean sendFerlQuit;
	
	static Object dirMutex;
	volatile static int direction;

/**
*	Verschub
*/
	volatile static int von, bis;


/**
 *	Fahrttype
 */
	volatile static int art;
}
