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
import com.jopdesign.sys.Native;

public class Gps extends RtThread {

/**
*	Status of GPS fix:
*	-1 ... no GGA from GPS
*	0 ... no fix
*	1 ... GPS fix
*	2 ... DGPS fix
*/
	public static int fix;

/**
*	Calculated speed in m/s
*
*	-1 if no GPS fix;
*/
	public static int speed;

	/** delay of fix for correct subtraction */
	private static int last_fix;

	/** timestamp of last value (with fix!=0) */
	private static int ts;
	/** last known 'good' coordinates */
	public static int last_lat;
	public static int last_lon;

	private static final int FIX_TIMEOUT = 3000000;

	/**
	*	start averaging.
	*/
	private static boolean average;
	private final static int MAX_AVG = 120;
	static int avgCnt;
	private static int[] avgLat, avgLon;

/**
*	period for thread in us.
*/
	// TODO find a schedule whith correct priorities
	private static final int PERIOD = 100000;
/**
*	The one and only reference to this object.
*/
	private static Gps single;

	private static final int BUF_LEN = 80;
	private static int[] rxBuf;
	private static int rxCnt;

/**
*	private because it's a singleton Thread.
*/
	private Gps(int priority, int us) {
		super(priority, us);
	}


	public static void init(int priority) {

		if (single != null) return;			// allready called init()

		rxBuf = new int[BUF_LEN];
		rxCnt = 0;
		fix = -1;
		last_fix = 0;
		speed = -1;
		average = false;
		avgCnt = 0;
		avgLat = new int[MAX_AVG];
		avgLon = new int[MAX_AVG];

		Serial2.init();						// start serial buffer thread

		//
		//	start my own thread
		//
		single = new Gps(priority, PERIOD);
	}


/**
*	Echo GPS data to Dbg.
*/
	public void run() {

		int i, j;
		int val;

		for (;;) {
			waitForNextPeriod();

			// too long no data from GPS
			if (fix>0 && Native.rd(Native.IO_US_CNT)-ts>FIX_TIMEOUT) {
Dbg.wr('*');
				last_fix = 0;
				fix = 0;
				speed = -1;
			}

			i = Serial2.rxCnt();
			for (j=0; j<i; ++j) {
				val = Serial2.rd();
// Dbg.wr(val);
				rxBuf[rxCnt] = val;
				++rxCnt;
// TODO set fix to 0 when no data
				// we have one message
				if (val == '\n') {
					if (checkGGA()) {
						if (checkSum()) {
							process();
						} else {
Dbg.wr("GPS wrong checksum\n");
						} 
					}
					rxCnt = 0;	// free buffer
				} else if (rxCnt >= BUF_LEN) {
					rxCnt = 0;					// drop it if too long
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

		if (i!=0) {
			ts = Native.rd(Native.IO_US_CNT);
			last_lat = lat;
			last_lon = lon;
			if (average && avgCnt<MAX_AVG) {
				avgLat[avgCnt] = lat;
				avgLon[avgCnt] = lon;
				++avgCnt;
			}
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
				speed = j/i;
			}

			// find Strecke and Melderaum
			if (Status.strNr<=0) {
				findStr();
			} else {
				int melnr = getMelnr();
				if (melnr != Status.melNr) {
Dbg.wr("Melderaum: ");
Dbg.intVal(melnr);
Dbg.wr("\n");
					Status.melNr = melnr;
				}
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


	private static void findStr() {

		int cnt = Flash.getCnt();
		int min = 999999999;
		int dist, strNr = 0;

Dbg.wr("find Strecke\n");
		for (int i=0; i<cnt; ++i) {
			int nr = Flash.getStrNr(i);
Dbg.intVal(nr);
			dist = getDistStr(nr);
Dbg.intVal(dist);
Dbg.wr("\n");
			if (dist<min) {
				min = dist;
				strNr = nr;
			}
		}
Dbg.wr("found: ");
Dbg.intVal(strNr);
Dbg.wr("\n");
		Status.strNr = strNr;
	}


	/**
	*	calculate distance in meter.
	*/
	private static int dist(int lat_diff, int lon_diff) {

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

	/**
	*	find nearest point in strNr and return distance in m.
	*/

	private static int getDistStr(int strNr) {

		if (strNr<=0) return -1;

		int nr = Flash.getFirst(strNr);

		int melnr = nr;
		int diff = 999999999;

		while (nr!=-1) {
			Flash.Point p = Flash.getPoint(nr);
			if (p.lat!=0 && p.lon!=0) {
				int i = dist(p.lat-last_lat, p.lon-last_lon);
				if (i<diff) {
					diff = i;
					melnr = nr;
				}
			}
			nr = Flash.getNext(nr);
		}
		return diff;
	}

	private static int findNearestPoint() {

		if (Status.strNr<=0) return -1;

		int nr = Flash.getFirst(Status.strNr);

		int melnr = -1;
		int diff = 999999999;

		//
		// find nearest melnr
		//
		while (nr!=-1) {
			Flash.Point p = Flash.getPoint(nr);
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

	private static int getMelnr() {

		int b = findNearestPoint();
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
		
		int xa = dist(pa.lat-last_lat, pa.lon-last_lon);
		int xb = dist(pb.lat-last_lat, pb.lon-last_lon);
		int xc = dist(pc.lat-last_lat, pc.lon-last_lon);

		int ab = dist(pa.lat-pb.lat, pa.lon-pb.lon);
		int bc = dist(pb.lat-pc.lat, pb.lon-pc.lon);

		//
		// return 'left' melnr
		//
		if (xa<ab && xb<ab) {
			return a;
		}
		if (xb<bc && xc<bc) {
			return b;
		}

		// we have not found a melnr
		return -1;
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

		int len = p.len - Comm.OFF_DATA*4;

		if (len > Serial2.txFreeCnt()) {
Dbg.wr('d');
			return;										// just drop it
		}

Dbg.wr('D');

		int i;
		int[] buf = p.buf;

		for (i=0; i<len; ++i) {
			Serial2.wr((buf[Comm.OFF_DATA+(i>>2)]>>(24-(i&3)*8)) & 0xff);
		}
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
