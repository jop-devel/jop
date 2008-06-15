package oebb;

/**
*	Status.java: Shared data between Logic and Comm
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*   
*   TODO: should be moved to Logic.
*
*/

public class Status {

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
*	Set after lern msg got a repley
*/
	volatile static boolean lernOk;


/**
*	display menu
*/
	volatile static boolean dispMenu;

/**
*	is Lernbetrieb allowed
*/
	volatile static boolean isMaster;



	
	/**
	*	send a communication alarm
	*/
	volatile static boolean doCommAlarm;
	
	
	static Object dirMutex;
	volatile static int direction;

}
