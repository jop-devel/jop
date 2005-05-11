/**
* really deletes log.
*/
import BBSys;
import Msg;

public class ConvertLog {

	private class CmdException extends Exception {};

	private static final int LOG_LEN = 8192;
	private static final int LOG_SIZE = 64;
	private static final int LOG_START = 0x20000-LOG_LEN;

	private int[] mem;
	private int nr;

	Msg m = new Msg();

	public ConvertLog() {

		nr = 0;					// Zentrale
		mem = new int[LOG_LEN];
		for (int i=0; i<LOG_LEN; ++i) mem[i] = 0xff;
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

	public void doit() {

		int i, j;

/*
		try {
			cmd(BBSys.CMD_FL_PAGE, LOG_START>>7);

			for (i=0; i<LOG_LEN; ++i) {
				mem[i] = cmd(BBSys.CMD_FL_READ, 0);
			}
		} catch (CmdException e) {
			System.exit(-1);
		}

		for (i=0; i<LOG_LEN; i+=LOG_SIZE) {
			for (j=0; j<LOG_SIZE/2; ++j) {
				mem[i/2+j] = mem[i+j];
			}
		}
		for (i=LOG_LEN/2; i<LOG_LEN; ++i) mem[i] = 0;
*/

		int startPage = LOG_START>>7;

		int pages = LOG_LEN/128;
		for (i=0; i<pages; ++i) {

System.out.println(i);
			try {
				cmd(BBSys.CMD_FL_PAGE, startPage+i);
				for (j=0; j<128; ++j) {
					cmd(BBSys.CMD_FL_DATA, mem[i*128+j]);
				}
				cmd(BBSys.CMD_FL_PROG, 0);
			} catch (CmdException e) {
				try { Thread.sleep(1000); } catch (Exception x) {}
				--i;
			}
		}

	}

	public static void main (String[] args) {

		ConvertLog l = new ConvertLog();
		l.doit();
	}
}
