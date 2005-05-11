/**
*	Load new firmware into flash via BB commands.
*/
import BBSys;
import Msg;

public class Log {

	private class CmdException extends Exception {};

	private static final int LOG_LEN = 8192;
	private static final int LOG_SIZE = 32;
	private static final int LOG_START = 0x20000-LOG_LEN;

	private String[] actTxt = {
		"null",
		"AUF / GESTARTET",
		"AB  / GESTARTET",
		"AUF / OBEN ERREICHT",
		"AB  / UNTEN ERREICHT",
		"FEHLER",
		"GESTOPPT",
		"NOTAUS GEDRÜCKT",
		"8",
		"9",
		"10",
	};
	private String[] errTxt = {
		"",
		"KOMMUNIKATIONSFEHLER",
		"MAXIMALE DIFFERENZ ERREICHT",
		"FEHLER BEI MAST",
		"KEIN MAST ANTWORTET",
		"Text nicht belegt",
		"ANZAHL MS FALSCH",
		"SENSOR OBEN UND UNTEN GLEICHZEITIG",
		"KEINE IMPULSE",
		"KEINE ABFRAGEN",
		"SENSOR OBEN DEFEKT",
		"SENSOR UNTEN DEFEKT",
		"SPANNUNG L1 FEHLT",
		"SPANNUNG L2 FEHLT",
		"SPANNUNG L3 FEHLT",
		"ANTWORTET NICHT",
		"KEIN STROM L1",
		"KEIN STROM L2",
		"KEIN STROM L3",
		"STROM ÜBERLAST L1",
		"STROM ÜBERLAST L2",
		"STROM ÜBERLAST L3",
		"ZU WENIG STROM L1",
		"ZU WENIG STROM L2",
		"ZU WENIG STROM L3",
		"STROM IN L1",
		"STROM IN L2",
		"STROM IN L3",
		"xx",
		"yy",
		"zz",


	};
	private int[] mem;
	private int nr;

	Msg m = new Msg();

	private class Entry {

		int nr;
		int year;
		int month;
		int day;
		int s;
		int h;
		int m;
		int action;
		int errnr;
		int msnr;
		int zstemp;
		int minnr;
		int minval;
		int maxnr;
		int maxval;
	}

	public Log() {

		nr = 0;					// Zentrale
		mem = new int[LOG_LEN];
		for (int i=0; i<LOG_LEN; ++i) mem[i] = 0;
	}

	private int cmd(int cmd, int data) throws CmdException {

		int ret;

		ret = m.exchg(nr, cmd, data);
		if (ret < 0) {
			System.out.println();
			System.out.println("Error "+ret+" with station "+nr);
			System.out.println("\rcmd: "+cmd+" data: "+data);
			throw new CmdException();
		}
		return ret;
	}

	private String ldz(int val, int len) {

		String s = ""+val;
		for (int i=s.length(); i<len; ++i) {
			s = "0"+s;
		}
		return s;
	}

	public void read() {

		int lastNr = 0;
		int maxNr = 0;
		try {
			BufferedReader in = new BufferedReader(new FileReader("lastnr"));
			lastNr = Integer.parseInt(in.readLine());
		} catch (Exception inex) {
		}

		TreeMap tm = new TreeMap();

		try {
			cmd(BBSys.CMD_FL_PAGE, LOG_START>>7);

			for (int i=0; i<LOG_LEN; i+=LOG_SIZE) {

System.out.print(i/LOG_SIZE+"\r");
				for (int j=0; j<LOG_SIZE; ++j) {
					mem[j] = cmd(BBSys.CMD_FL_READ, 0);
//System.out.print(mem[j]+" ");
				}
//System.out.println();

				Entry ent = new Entry();

				ent.nr = mem[0]<<24;
				ent.nr |= mem[1]<<16;
				ent.nr |= mem[2]<<8;
				ent.nr |= mem[3];

				ent.year = mem[4]<<8;
				ent.year |= mem[5];
				ent.month = mem[6];
				ent.day = mem[7];
	
				ent.s = mem[8]<<8;
				ent.s |= mem[9];
				ent.h = ent.s/3600;
				ent.m = ent.s%3600/60;
				ent.s %= 60;

				ent.action = mem[10];
				ent.errnr = mem[11];
				ent.msnr = mem[12];
				ent.zstemp = (int) ((byte) mem[13]);

				ent.minnr = ent.maxnr = 0;
				ent.minval = 127;
				ent.maxval = -127;
				for (int k=0; k<3; ++k) {

					int val = (int) ((byte) mem[16+k]);
					if (val<ent.minval) {
						ent.minval = val;
						ent.minnr = k+1;
					}
					if (val>ent.maxval) {
						ent.maxval = val;
						ent.maxnr = k+1;
					}
				}

				if (ent.nr>maxNr) maxNr = ent.nr;

				if (ent.nr!=0 && ent.nr!=-1 && ent.nr>lastNr) {
					tm.put(new Integer(ent.nr), ent);
				}
			}
		} catch (CmdException e) {
			System.exit(-1);
		}

		Calendar now = Calendar.getInstance();
		String fname = "log"+
			ldz(now.get(Calendar.YEAR), 4) +
			ldz(now.get(Calendar.MONTH)+1, 2) +
			ldz(now.get(Calendar.DAY_OF_MONTH), 2) +
			".txt";

		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			System.exit(-1);
		}

		for (Iterator i = tm.entrySet().iterator(); i.hasNext(); ) {
			Entry ent = (Entry) ((Map.Entry) i.next()).getValue();
			out.print(ent.nr+"\t"+ent.day+"."+ent.month+"."+ent.year+" "+ent.h+":"+ent.m+":"+ent.s+"\t");
			out.print(ent.zstemp+"\t"+ent.minnr+"\t"+ent.minval+"\t"+ent.maxnr+"\t"+ent.maxval+"\t");
			out.print((ent.action&0x80)!=0 ? "A\t" : "M\t");
			ent.action &= 0x7f;
			try {
				out.print("\""+actTxt[ent.action]+"\""+"\t\""+errTxt[ent.errnr]);
				if (ent.msnr!=0) out.print(" MAST "+ent.msnr);
				out.print("\"");
			} catch (Exception arr) {
				out.print("Fehler: "+ent.action+" "+ent.errnr);
			} 

			out.println();
		}

		out.close();


		try {
			out = new PrintWriter(new FileWriter("lastnr"));
			out.println(maxNr);
			out.close();
		} catch (IOException lastnrout) {
		}
	}

	public static void main (String[] args) {

		Log l = new Log();
		l.read();
	}
}
