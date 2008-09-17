package oebb;

/**
*	Gps.java: 
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*
*/

import util.*;
import ejip.*;
import joprt.*;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Gps extends RtThread {

	static boolean wait;
/**
*	Status of GPS fix:
*	-1 ... no GGA from GPS
*	0 ... no fix
*	1 ... GPS fix
*	2 ... DGPS fix
*/
	public static int fix;

// TODO Geschw. in Knoten >=2 ... faehrt (= 1m/s).
// Meldr.ueberw und Richtung wird im Stillstand unterdrueckt.

/**
*	Calculated speed in km/h
*
*	-1 if no GPS fix;
*/
	public static int speedCalc;		// my calculated value
	public static int speed;			// value from GPS
	
/**
 *	Minimum speed in km/h for direction detection and
 *	Stillstand detection.
 */
	public static final int MIN_SPEED = 4;
/**
 *	Direction:
 *		forward is from 'left to right'
 *		back is from 'right to left'
 *			seen from the Strecke definition
 *
 */
	public static int direction;
	public static final int DIR_UNKNOWN = 0;
	public static final int DIR_FORWARD = 1;
	public static final int DIR_BACK = -1;
	// minum Distance change for direction test
	public static final int MIN_DIR_DIST = 1;

	/** delay of fix for correct subtraction */
	private static int last_fix;

	/** timestamp of last value (with fix!=0) */
	private static int ts;
	/** last known 'good' coordinates */
	public static int last_lat;
	public static int last_lon;
	/** old coordinates for direction processing */
	public static int old_lat;
	public static int old_lon;
	public static int[] text;

	// this one is in us - for history reasons
	private static final int FIX_TIMEOUT = 3000000;
	
	// this timeout is in Seconds
	private static final int MELNR_TIMEOUT = 120;
	private static int melNrTimeout;
	
	public static boolean changeToBereit;
	
	/*
	 * values for info mode
	 */
	public static int nearestPoint;
	public static int nearestPointDistance;

	/**
	*	start averaging.
	*/
	private static boolean average;
	private final static int MAX_AVG = 120;
	static int avgCnt;
	private static int[] avgLat, avgLon;

/**
*	The one and only reference to this object.
*/
	private static Gps single;
	private static Serial ser;

	private static final int BUF_LEN = 80;
	private static int[] rxBuf;
	private static int rxCnt;
	
/**
 *	GPS GGA line for the logbook 
 */
	static StringBuffer lastGGA;
/**
 *	GPS RMC line for the logbook 
 */
	static StringBuffer lastRMC;
	
	/**
	 * Time as integer in format hhmmss
	 */
	private static int gpsTime;
	/**
	 * us timestamp of last GGA message
	 */
	private static int gpsTimestamp;
	/**
	 * Date as integer in format ddmmyy
	 */
	private static int gpsDate;
	
	private static Object timMutex = new Object();
	
	private static SysDevice sys;

/**
*	private because it's a singleton Thread.
*/
	private Gps(int priority, int us) {
		super(priority, us);
	}


	public static void init(int priority, int period, Serial serPort) {

		if (single != null) return;			// allready called init()

		wait = true;
		rxBuf = new int[BUF_LEN];
		rxCnt = 0;
		fix = -1;
		last_fix = 0;
		speedCalc = -1;
		speed = -1;
		direction = DIR_UNKNOWN;
		average = false;
		changeToBereit = false;
		avgCnt = 0;
		avgLat = new int[MAX_AVG];
		avgLon = new int[MAX_AVG];
		text = new int[19];
		lastGGA = new StringBuffer(BUF_LEN);
		lastRMC = new StringBuffer(BUF_LEN);
		
		// start serial buffer thread
		
		ser = serPort;
		
		sys = IOFactory.getFactory().getSysDevice();

		//
		//	start my own thread
		//
		single = new Gps(priority, period);
	}


/**
*	Main loop for GPS processing.
*/
	public void run() {

		int i, j;
		int val;
		
		// wait till all download stuff is done and
		// Logic starts GPS recognition
		while (wait) {
			waitForNextPeriod();
		}

		for (;;) {
			waitForNextPeriod();

			// too long no data from GPS
			if (fix>0 && Native.rd(Const.IO_US_CNT)-ts>FIX_TIMEOUT) {
Dbg.wr('*');
				last_fix = 0;
				fix = 0;
				speedCalc = -1;
				speed = -1;
				direction = DIR_UNKNOWN;
			}

			i = ser.rxCnt();
			for (j=0; j<i; ++j) {
				val = ser.rd();
// System.out.print((char) val);
				rxBuf[rxCnt] = val;
				++rxCnt;
// TODO set fix to 0 when no data
				// we have one message
				if (val == '\n') {
					if (checkGGA()) {
						if (checkSum()) {
							process();
							if (fix>0) {
								// find Strecke and Melderaum
								checkStrMelnr();
							}
						} else {
//Dbg.wr("GPS wrong checksum\n");
						} 
					} else if (checkRMC()) {
						if (checkSum()) {
							processRMC();
						} else {
//Dbg.wr("GPS wrong checksum\n");
						} 
					}
					rxCnt = 0;	// free buffer
				} else if (rxCnt >= BUF_LEN) {
					rxCnt = 0;					// drop it if too long
				}
			}
		}
	}


	private static void checkStrMelnr() {

		State state = Main.state;
		
		if (Main.state.strnr<=0) {
			if (Strecke.idle) {
				Strecke.idle = false;
				// set coordinates
				Strecke.lat = last_lat;
				Strecke.lon = last_lon;
				Strecke.find.fire();
			}
		} else {
			int melnr = getMelnr(state.strnr, last_lat, last_lon, state.getPos());
			
			Status.doCommAlarm = Flash.isCommAlarm(melnr, last_lat, last_lon);
//			if (Status.doCommAlarm) {
//				Dbg.wr("do communication Alarm");
//			} else {
//				Dbg.wr("no communication Alarm");
//			}

			if (melnr != state.getPos()) {
Dbg.wr("Melderaum: ");
Dbg.intVal(melnr);
Dbg.wr("\n");
				//
				// keep last melNr if no new melnr found
				//
				if (melnr!=-1) {
					// change only if previous unknown or
					// we're moving
					if (state.getPos()<=0 || speed>MIN_SPEED) {
						if (Main.logic.state!=Logic.LERN) {
							state.setPos(melnr);
							state.requestSend();
Dbg.wr("Melderaum: ");
Dbg.intVal(melnr);
Dbg.wr(" nun aktiv\n");							
						}
						// enable Alarm checking again
						// is disabled again!!!
						// Status.checkMove = true;

					}
				}
			} else {
				// check direction only if no melNr change
				// and we have a valid melNr
				if (state.getPos()>0) {
					checkDir();
				}
			}
			//
			//	check the timeout for a change to 'Bereit'
			//
			if (state.getPos()>=0) {
				if (melnr==-1) {
					if (Timer.secTimeout(melNrTimeout)) {
						changeToBereit = true;
					}
				} else {
					melNrTimeout = Timer.getSec() + MELNR_TIMEOUT;
					changeToBereit = false;
				}				
			}
		}
	}

	private static boolean checkGGA() {

		if (rxBuf[3] != 'G') return false;
		if (rxBuf[4] != 'G') return false;
		if (rxBuf[5] != 'A') return false;
		return true;
	}


	private static boolean checkRMC() {

		if (rxBuf[3] != 'R') return false;
		if (rxBuf[4] != 'M') return false;
		if (rxBuf[5] != 'C') return false;
		return true;
	}
	

	private static boolean checkSum() {

		int i, j, sum;

		sum = 0;

		if (rxCnt<4) return false;	// too short

		// message starts after '$' and goes till '*'
		for (i=1; i<rxCnt-5; ++i) {
			sum ^= rxBuf[i];
		}
		i = sum >>> 4;
		j = sum & 0x0f;
		if (i<=9) { i += '0'; } else { i += 'A'-10; }
		if (j<=9) { j += '0'; } else { j += 'A'-10; }
		if (rxBuf[rxCnt-4]!=i || rxBuf[rxCnt-3]!=j) {
			return false;
		}

		return true;
	}
	
	/**
	*	Get speed from RMC message.
	*
	*	speed is in knots.
	*	1 nautical mile = 1.15 miles = 1852 meters = 6067 feet 
	*	knots = nautic miles / hour
	*/
	private static void processRMC() {

		int i, knt, val;

		synchronized (lastRMC) {
			lastRMC.setLength(0);
			for (i=0; i<rxCnt; ++i) {
				lastRMC.append((char) rxBuf[i]);
			}
		}

		// find date position
		val = 9;	// number of commas
		for (i=0; i<BUF_LEN; ++i) {
			if (rxBuf[i]==',') {
				--val;
			}
			if (val==0) {
				break;
			}
		}
		val = i+1;	// start of date
		if (val<BUF_LEN-10) {
			// get date
			for (i=val; i<val+6; ++i) {
				rxBuf[i] -= '0'; 
			}
			i = rxBuf[val] * 100000;
			i += rxBuf[val+1] * 10000;
			i += rxBuf[val+2] * 1000;
			i += rxBuf[val+3] * 100;
			i += rxBuf[val+4] * 10;
			i += rxBuf[val+5];
			synchronized (timMutex) {
				gpsDate = i;
			}			
		}

		knt = 0;
		for (i=0; i<5; ++i) {
			val = rxBuf[41+i];
			if (val=='.') continue;
			if (val==',') break;
			knt *= 10;
			knt += val-'0';
		}
		
		// in 1/10 knots
		// k/10*1.85 = x km/h
		// = k*0.185 = k*47/256
		speed = (knt*47)>>8;
/*
Dbg.wr("knots=");
Dbg.intVal(knt);
Dbg.wr("km/h=");
Dbg.intVal(gpsSpeed);
Dbg.lf();
*/
	}

	/**
	*	Get GPS coordinates from GGA message.
	*
	*	Internal format: AUT is N 46 - 50, E 9 - 18
	*		use only 2 digits of grad and ignore N/S and E/W
	*		in 0.0001 minutes
	*
	*	= lat: 0.1853 m, long: 0.124 m (in AUT)
	*/
	private static void process() {

		
		int i, j, lat, lon;

		int tmpTS = sys.uscntTimer;

		synchronized (lastGGA) {
			lastGGA.setLength(0);
			for (i=0; i<rxCnt; ++i) {
				lastGGA.append((char) rxBuf[i]);
			}
		}

		// text is for info and lern mode
		j = 0;
		for (i=14; i<23; ++i) {
			text[j++]=rxBuf[i];
		}
		text[j++] = ' ';
		for (i=27; i<36; ++i) {
			text[j++]=rxBuf[i];
		}

		// get time
		for (i=7; i<=12; ++i) {
			rxBuf[i] -= '0'; 
		}

		i = rxBuf[7] * 100000;
		i += rxBuf[8] * 10000;
		i += rxBuf[9] * 1000;
		i += rxBuf[10] * 100;
		i += rxBuf[11] * 10;
		i += rxBuf[12];
		synchronized (timMutex) {
			gpsTime = i;
			gpsTimestamp = tmpTS;			
		}

		for (i=14; i<40; ++i) {
			rxBuf[i] -= '0'; 
		}
		
		lat = rxBuf[14] * 6000000;
		lat += rxBuf[15] * 600000;
		lat += rxBuf[16] * 100000;
		lat += rxBuf[17] * 10000;
		lat += rxBuf[19] * 1000;
		lat += rxBuf[20] * 100;
		lat += rxBuf[21] * 10;
		lat += rxBuf[22];

		lon = rxBuf[27] * 6000000;
		lon += rxBuf[28] * 600000;
		lon += rxBuf[29] * 100000;
		lon += rxBuf[30] * 10000;
		lon += rxBuf[32] * 1000;
		lon += rxBuf[33] * 100;
		lon += rxBuf[34] * 10;
		lon += rxBuf[35];

		i = rxBuf[39];
		// Garmin sends sometimes a value of 6!
		if (i<0 || i>2) i = 0;

		int last_ts = ts;
		int lat_diff = lat-last_lat;
		int lon_diff = lon-last_lon;
		
		old_lat = last_lat;			// remember for direction check
		old_lon = last_lon;

		if (i!=0) {
			ts = Native.rd(Const.IO_US_CNT);
			last_lat = lat;
			last_lon = lon;
			if (average && avgCnt<MAX_AVG) {
				avgLat[avgCnt] = lat;
				avgLon[avgCnt] = lon;
				++avgCnt;
			}
		}

//		if (i!=last_fix) {
//			if (Status.connOk) {
//				Comm.gpsStatus(i, last_lat, last_lon);
//			} 
//		}

		if (Main.logic.state!=Logic.LERN) {
			Main.state.gpsLat = last_lat;
			Main.state.gpsLong = last_lon;			
		}

		// delay fix one message
		if (last_fix!=0) {
			last_fix = i;
			fix = i;
		} else {
			last_fix = i;
			fix = 0;
		}
		

		// time diff in seconds
		i = (ts-last_ts+500000)/1000000;

		if (fix>0) {

			// calculate speed
			if (i!=0) {
				j = dist(lat_diff, lon_diff);
				// x[m/s]*3.6 = x[km/h]
				// 3.6 = 922/256
				speedCalc = (j/i*922)>>8;
			}

		}
/*
Dbg.wr("GPS: ");
Dbg.intVal(lat);
Dbg.intVal(lon);
Dbg.intVal(rxBuf[39]);
Dbg.intVal(speed);
Dbg.wr("m/s \n");
*/
	}


	/**
	*	calculate distance in meter.
	*/
	static int dist(int lat_diff, int lon_diff) {

		// to simplifiy rounding
		if (lat_diff<0) lat_diff = -lat_diff;
		if (lon_diff<0) lon_diff = -lon_diff;
		// convert to meter
		lat_diff = (lat_diff*47+128)>>8;	// * 0.184
		lon_diff = (lon_diff+4)>>3;			// * 0.125
		// diff is now in meter

		int val;

		// quick hack to avoid overflow
		if (lat_diff>32000 || lon_diff>32000) {
			lat_diff >>>= 10;
			lon_diff >>>= 10;
			// diff is now in about km
			val = IMath.sqrt(lat_diff*lat_diff+lon_diff*lon_diff);
			// back to m
			val <<= 10;
		} else {
			val = IMath.sqrt(lat_diff*lat_diff+lon_diff*lon_diff);
		}
		return val;
	}


	private static int findNearestPoint(int strNr) {

		if (strNr<=0) return -1;

		int nr = Flash.getFirst(strNr);

		int melnr = -1;
		int diff = 999999999;

		//
		// find nearest melnr
		//
		while (nr!=-1) {
			Flash.Point p = Flash.getPoint(nr);
			if (p==null) return -1;
			if (p.lat!=0 && p.lon!=0) {
				int i = dist(p.lat-last_lat, p.lon-last_lon);
				if (i<diff) {
					diff = i;
					melnr = nr;
				}
			}
			nr = Flash.getNext(nr);
		}
		return melnr;
	}

	/**
	 * Find the melnr with the GPS coordinates. If two meln are found
	 * keep the last one. If nothing found return -1;
	 * @param strNr
	 * @param lat
	 * @param lon
	 * @param lastMelnr
	 * @return
	 */
	static int getMelnr(int strNr, int lat, int lon, int lastMelnr) {

		int ret = -1;			// default not found
		int b = findNearestPoint(strNr);
		nearestPoint = b;
		if (b==-1) return -1;	// not even one point found

		int a = Flash.getPrev(b);
		int c = Flash.getNext(b);

		if (a==-1) {
			a = b;
			b = c;
			c = Flash.getNext(c);
		} else if (c==-1) {
			c = b;
			b = a;
			a = Flash.getPrev(a);
		}
		// we don't care about Strecken with only two points
		// useless definition	
		if (a==-1 || c==-1) return -1;

		Flash.Point pa = Flash.getPoint(a);
		Flash.Point pb = Flash.getPoint(b);
		Flash.Point pc = Flash.getPoint(c);
		
		if (pa==null || pb==null || pc==null) return -1;
		
		int xa = dist(pa.lat-lat, pa.lon-lon);
		int xb = dist(pb.lat-lat, pb.lon-lon);
		int xc = dist(pc.lat-lat, pc.lon-lon);

		int ab = dist(pa.lat-pb.lat, pa.lon-pb.lon);
		int bc = dist(pb.lat-pc.lat, pb.lon-pc.lon);

		nearestPointDistance = xb;
		//
		// return 'left' melnr
		//
		if (xa<=ab && xb<=ab) {
			if (xb<=bc && xc<=bc) {
				ret = lastMelnr;	// point fits for both -> return last known good point
			} else {
				ret = a;
			}
		} else if (xb<=bc && xc<=bc) {
			ret = b;
		}

		return ret;
	}

	static void checkDir() {
		
		if (speed<MIN_SPEED) {
			direction = DIR_UNKNOWN;
			return;
		} 
		Flash.Point p = Flash.getPoint(Main.state.getPos());
		if (p==null) return;
		
		int dold = dist(p.lat-old_lat, p.lon-old_lon);
		int dnew = dist(p.lat-last_lat, p.lon-last_lon);
		if (dnew > dold + MIN_DIR_DIST) {
			direction = DIR_FORWARD;
		} else if (dold > dnew + MIN_DIR_DIST) {
			direction = DIR_BACK;
		} else {
			direction = DIR_UNKNOWN;
		}
	}

	/**
	*	start collecting values for averaging.
	*/
	public static void startAvg() {

		avgCnt = 0;
		average = true;
	}

	/**
	*	stop collecting values for averaging.
	*/
	public static void stopAvg() {

		average = false;
	}

	public static int getLatAvg() {

		return avg(avgLat);
	}

	public static int getLonAvg() {

		return avg(avgLon);
	}


	private static int avg(int[] vals) {

		int cnt = avgCnt;
		if (cnt==0) return 0;
		int first = vals[0];
		int val = 0;
		for (int i=1; i<cnt; ++i) {
			val += vals[i]-first;		// take only the difference
		}
		val /= cnt;
		return first+val;
	}

/**
*	get dgps data from Comm.
*/
	public static void dgps(Packet p) {

		int len = p.len - Udp.DATA*4;

		if (len > ser.txFreeCnt()) {
Dbg.wr("DGPS droped\r\n");
			return;										// just drop it
		}

Dbg.wr("DGPS ok\r\n");


		int i;
		int[] buf = p.buf;

		// 12 is offset of DGPS data, first three fields
		// are bgid, date, time as 32 bit words
		for (i=12; i<len; ++i) {
//Dbg.hexVal((buf[Udp.DATA+(i>>2)]>>(24-(i&3)*8)) & 0xff);
			ser.wr((buf[Udp.DATA+(i>>2)]>>(24-(i&3)*8)) & 0xff);
		}
//Dbg.lf();
	}


	/**
	 * @return date in ddmmyy
	 */
	public static int getDate() {
	
		int i;
		synchronized (timMutex) {
			i = gpsDate;
		}
		return i;
	}


	/**
	 * TODO: time and date format could be in the message format time
	 * 
	 * @return time in hhmmssmmm
	 */
	public static int getTime() {
		
		int off = sys.uscntTimer;
		int i;
		synchronized (timMutex) {
			off -= gpsTimestamp;
			i = gpsTime;
		}
		off /= 1000; // in ms
		return i*1000 + off;
	}

	/**
	 * GPS has a fix.
	 * @return
	 */
	public static boolean ok() {
		return fix>0;
	}

/* how to copy a char buffer to a packet buffer
	would be nice method for Packet

	static int readBuffer(int[] udpBuf, int pos) {

		int i, j, k;

		if (rxBuf[4]=='G') {
			Dbg.wr('\n');
			for (i=0; i<rxCnt; ++i) {
				Dbg.wr(rxBuf[i]);
			}
		}

		j = 0;
		k = pos;
		for (i=0; i<rxCnt; ++i) {
			j <<= 8;
			j += rxBuf[i];
			if ((i&3)==3) {
				udpBuf[k] = j;
				++k;
			}
		}
		int cnt = i & 3;
		if (cnt!=0) {
			for (; cnt<4; ++cnt) {
				j <<= 8;
			}
			udpBuf[k] = j;
		}
		return i;
	}
*/
}
