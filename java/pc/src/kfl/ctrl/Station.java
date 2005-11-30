package kfl.ctrl;

/*
 * Station.java
 *
 * 
 */
import BBSys;
import Msg;
import Temp;


public class Station {
	int nr;
	int cnt;
	int err;
	int minTime;
	int maxTime;
	long sumTime;

	static Msg m = new Msg();
	static List stl = null;

	private Station(int nr) {
		this.nr = nr;
		cnt = err = maxTime = 0;
		minTime = 9999;
		sumTime = 0;
	}

	public static Msg getMsg() {

		return m;
	}

	public static List find() {

		stl = new LinkedList();
		int val;

		for (int i=0; i<8; ++i) {
			val = m.exchg(i, BBSys.CMD_STATUS, 0);
			if (val<0) {
				m.clear();
			} else {
				System.out.print("Station "+i+" found");
				Station s = new Station(i);
				stl.add(s);
				val = s.version();
				if (val<0) {
					System.out.println(", version ???");
				} else {
					System.out.println(", version "+(val>>6)+"."+(val&0x3f));
				}
			}
		}
		return stl;
	}

	public static String up() {
		return cmdAll(BBSys.CMD_UP);
	}
	public static String down() {
		return cmdAll(BBSys.CMD_DOWN);
	}
	public static String stop() {
		return cmdAll(BBSys.CMD_STOP);
	}
	public static String resTime() {
		return cmdAll(BBSys.CMD_RESTIM);
	}
	public static String resCnt() {
		return cmdAll(BBSys.CMD_SETCNT);
	}
	public static String reset() {
		return cmdAll(BBSys.CMD_RESET);
	}

	public static String cmdAll(int val) {

		String ret = "";
		int ok;

		for (Iterator i = stl.iterator(); i.hasNext(); ) {
			Station s = (Station) i.next();
			ok = s.cmd(val);
			if (ok<0) {							// second try
				ok = s.cmd(val);
			}
			ret += "Station "+s.nr()+" "+(ok<0 ? "error "+ok : "OK")+"\n";
		}

		return ret;
	}

	public static String info() {

		String ret = "";

		for (Iterator i = stl.iterator(); i.hasNext(); ) {
			Station s = (Station) i.next();
			ret += "Station "+s.nr()+": "+s.cnt+" Befehle, "+s.err+" Fehler\n";
			ret += "\tStatus: "+s.cmd(BBSys.CMD_STATUS)+" Impulse: "+((s.cmd(BBSys.CMD_CNT)<<20)>>20);
			ret += " serviceCnt: "+s.cmd(BBSys.CMD_SERVICECNT);
			ret += " max. Time: ";
			int t = s.cmd(BBSys.CMD_TIME);
			ret += (t/29)+"."+((t-t/29*29)*10/29)+" ms\n";

			Temp.init();
			t = s.cmd(BBSys.CMD_TEMP);
			if (t<0) {
				t = -99;
			} else {
				t = Temp.calc((t<<3)+17000);
			}
			ret += "Temp: "+t+" C ";

			ret += " dbg: ";
			for (int nr=0; nr<3; ++nr) {
				int val = s.cmd(BBSys.CMD_DBG_DATA, nr);
				if (val<0) break;
				ret += val+" ";
			}
			ret += "\n";
		}
		return ret;
	}

	private int cmd(int val) {

		return cmd(val, 0);
	}

	private int cmd(int cmd, int val) {

		int ret = m.exchg(nr, cmd, val);
		++cnt;
		if (ret<0) ++err;
		return ret;
	}

	public int version() {
		return cmd(BBSys.CMD_VERSION);
	}
	public int inp() {
		return cmd(BBSys.CMD_INP);
	}
	public int opto() {
		return cmd(BBSys.CMD_OPTO);
	}
	public int cnt() {
		return cmd(BBSys.CMD_CNT)<<20>>20;
	}
	public int stat() {
		return cmd(BBSys.CMD_STATUS);
	}
	public int iadc(int nr) {
		return cmd(BBSys.CMD_DBG_DATA, nr);
	}

	public int nr() {
		return nr;
	}

	public void status() {

		++cnt;

		long l = System.currentTimeMillis();
		int val = m.exchg(nr, BBSys.CMD_STATUS, 0);
		l = System.currentTimeMillis()-l;

		if (val<0) {
			++err;
			System.out.println();
			System.out.println("Error "+val+" with station "+nr);
		} else {
			if (l<minTime) {
				minTime = (int) l;
			} else if (l>maxTime) {
				maxTime = (int) l;
			}
			sumTime += l;
			System.out.print(nr+": "+l+"/"+(sumTime/(cnt-err))+"ms err="+err+" ");
		}
	}

	public static void main(String[] args) {

		find();
		System.out.print(info());
	}
}
