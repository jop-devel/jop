/*
 * Created on 10.08.2005
 *
 */
package oebb;

import util.Dbg;
import joprt.SwEvent;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Strecke extends SwEvent {
	
	static Strecke find;
	
	static boolean idle;
	static int lat, lon;

	/**
	 * @param priority
	 * @param minTime
	 */
	public Strecke(int priority, int minTime) {
		super(priority, minTime);
		find = this;
		idle = true;
	}

	public void handle() {
		System.out.println("Strecke fired!");
		findStr();
		idle = true;
	}
	
	/**
	 * Strecken muessen mindestens 1km voneinander
	 * entfernt sein sonst ist eine Benutzereingabe erforderlich.
	 */
	private static final int MIN_DIST = 1000;
	
	private static void findStr() {


		int cnt = Flash.getCnt();
System.out.print(cnt);
System.out.println(" Strecken");
		int nr;
		int min = 999999999;
		int dist, strNr = 0;
		boolean foundOne = false;
		int foundIdx = 0;

		//
		//	We have to enter it manually
		//
		if (Status.selectStr) return;
		
//Dbg.wr("find Strecke\n");
		for (int i=0; i<cnt; ++i) {
			nr = Flash.getStrNr(i);
			Flash.loadStr(nr);
//Dbg.intVal(nr);
			dist = getDistStr(nr);
//Dbg.intVal(dist);
//Dbg.wr("\n");
			if (dist<min) {
				min = dist;
				strNr = nr;
			}
			if (dist<MIN_DIST) {
				if (foundOne) {
					Status.selectStr = true;
//System.out.println("Strecke nicht eindeutig");
					return;
				} else {
					foundIdx = i;
					foundOne = true;
				}
			}
		}
		
//System.out.print("minimum Distance=");
//Dbg.intVal(min);
//System.out.println();


		if (foundOne) {
			nr = Flash.getStrNr(foundIdx);
			Flash.loadStr(nr);
			
			if (Gps.getMelnr(strNr, lat, lon)!=-1) {
				
//Dbg.wr("found: ");
//Dbg.intVal(strNr);
//Dbg.wr("\n");
				Status.strNr = strNr;
			}
		} else {
			Status.strNr = 0;
//Dbg.wr("nothing found\n");
		}
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
			if (p==null) break;
			if (p.lat!=0 && p.lon!=0) {
				int i = Gps.dist(p.lat-lat, p.lon-lon);
				if (i<diff) {
					diff = i;
					melnr = nr;
				}
			}
			nr = Flash.getNext(nr);
		}
		return diff;
	}

}
