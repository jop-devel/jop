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
	 * Strecken muessen mindestens 5km voneinander
	 * entfernt sein sonst ist eine Benutzereingabe erforderlich.
	 */
	private static final int MIN_DIST = 1000;
	
	private static void findStr() {


		int cnt = Flash.getCnt();
System.out.print(cnt);
System.out.println(" Strecken");
		int nr;
		int min = 999999999;
		int dist;
		int foundCnt = 0;
		int foundIdx = -1;

		//
		//	We have to enter it manually
		//
		if (Status.selectStr) return;
		
//Dbg.wr("find Strecke\n");
		for (int i=0; i<cnt; ++i) {
			nr = Flash.getStrNr(i);
			if (nr==Logic.DL_STRNR) {
				continue;
			}
			Flash.loadStr(nr);
			Flash.loadStrNames(nr, 0, 0);
// we changed ES mode to be like Hilsbetrieb
//			if (Flash.getIp()==0) {
//				Flash.esStr();
//			}

Dbg.intVal(nr);

			// find one 'exact'
			if (Gps.getMelnr(nr, lat, lon, -1)!=-1) {
				foundIdx = i;
				++foundCnt;
Dbg.wr("in melnr ");
Dbg.lf();
			} else {
				// check if another is nearby
				dist = getDistStr(nr);
Dbg.wr("distance: ");
Dbg.intVal(dist);
Dbg.wr("\n");
				if (dist<MIN_DIST) {
Dbg.wr("++foundCnt");
Dbg.lf();
					++foundCnt;
				}				
			}
		}
		
System.out.print("foundIdx=");
Dbg.intVal(foundIdx);
System.out.print("foundCnt=");
Dbg.intVal(foundCnt);
System.out.println();

		if (foundIdx!=-1) {
			if (foundCnt==1) {
				nr = Flash.getStrNr(foundIdx);
				Flash.loadStr(nr);
				Flash.loadStrNames(nr, 0, 0);

//				if (Flash.getIp()==0) {
//					Flash.esStr();
//				}
Dbg.wr("found: ");
Dbg.intVal(nr);
Dbg.wr("\n");
				Main.state.strnr = nr;
				
			} else if (foundCnt>1 ) {
				Status.selectStr = true;
//				System.out.println("Strecke nicht eindeutig");
				return;
				
			}
			
		} else {
			Main.state.strnr = 0;
Dbg.wr("nothing found\n");		
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
