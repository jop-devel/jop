package oebb;

/**
*	Constants for Communication.
*/

public class Cmd {

	public static final int FIRST_CMD = 1;

	public static final int PING = 1;
	public static final int PING_RPL = 2;
	public static final int DGPS = 3;
	public static final int DGPS_RPL = 4;
	public static final int CONN = 5;
	public static final int CONN_RPL = 6;
	public static final int SWVER = 7;
	public static final int SWVER_RPL = 8;
	public static final int RESET = 9;
	public static final int RESET_RPL = 10;
	public static final int DLSTAT = 11;
	public static final int DLSTAT_RPL = 12;
	public static final int ANM = 13;
	public static final int ANM_RPL = 14;
	public static final int ANMOK = 15;
	public static final int ANMOK_RPL = 16;
	public static final int FLAN = 17;
	public static final int FLAN_RPL = 18;
	public static final int FERL = 19;
	public static final int FERL_RPL = 20;
	public static final int MLR = 21;
	public static final int MLR_RPL = 22;
	public static final int ANZ = 23;
	public static final int ANZ_RPL = 24;
	public static final int ABM = 25;
	public static final int ABM_RPL = 26;
	public static final int FWR = 27;
	public static final int FWR_RPL = 28;
	public static final int ANK = 29;
	public static final int ANK_RPL = 30;
	public static final int VERL = 31;
	public static final int VERL_RPL = 32;
	public static final int NOT = 33;
	public static final int NOT_RPL = 34;
	public static final int ALARM = 35;
	public static final int ALARM_RPL = 36;
	public static final int LERN = 37;
	public static final int LERN_RPL = 38;
	public static final int FWR_QUIT = 39;
	public static final int FWR_QUIT_RPL = 40;
	public static final int NOT_QUIT = 41;
	public static final int NOT_QUIT_RPL = 42;
	public static final int VERSCHUB = 43;
	public static final int VERSCHUB_RPL = 44;
	public static final int FERL_QUIT = 45;
	public static final int FERL_QUIT_RPL = 46;
	public static final int ZIEL = 47;
	public static final int ZIEL_RPL = 48;

	public static final int LAST_CMD = 48;

	public static final int ALARM_UEBERF = 1;
	public static final int ALARM_RICHTUNG = 2;
}
