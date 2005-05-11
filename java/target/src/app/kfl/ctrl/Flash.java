/**
*	Load new firmware into flash via BB commands.
*
*	three file types:
*
*		.ttf	ACEX config start at 0
*		.bin	Java files start at 0x18000
*		.txt	Text for Zentrale start at 0x1c400
*/
import BBSys;
import Msg;

public class Flash {

	private class CmdException extends Exception {};

	private static final int MAX_MEM = 32768*3;		// 96kB (acex 1k50)
	private static final int MAX_JAVA = 4096*4;		// max. 8192 word (-1 for address)
	private static final int MAX_TEXT = 1024*7;		// max. 7kB

	private static final int TEXT_START = 0x1c400;

	private int startPage;

	private int nr;
	private String fname;

	private byte[] mem;
	private int len;

	Msg m = new Msg();

	public Flash(int nr, String fname) {

		this.nr = nr;
		this.fname = fname;
		mem = new byte[MAX_MEM];
		for (int i=0; i<MAX_MEM; ++i) mem[i] = 0;
	}

	private static final int  ae = 0xe1;
	private static final int  oe = 0xef;
	private static final int  ue = 0xf5;
	private static final int  Ae = 0x80;
	private static final int  Oe = 0x86;
	private static final int  Ue = 0x8a;

	private void chkUml(byte[] buf) {

		for (int i=0; i<buf.length; ++i) {
			switch (buf[i]) {
				case (byte) 'ä':
					buf[i] = (byte) ae;
					break;
				case (byte) 'ö':
					buf[i] = (byte) oe;
					break;
				case (byte) 'ü':
					buf[i] = (byte) ue;
					break;
				case (byte) 'Ä':
					buf[i] = (byte) Ae;
					break;
				case (byte) 'Ö':
					buf[i] = (byte) Oe;
					break;
				case (byte) 'Ü':
					buf[i] = (byte) Ue;
					break;
			}
		}
	}

	public void read() {

		String line;
		StringTokenizer st;
		int pos;
		boolean isJava = false;
		boolean isTxt = false;
		int byteCnt = 0;

		len = 0;
		try {
			File f = new File(fname);
			String s = f.getName();
			pos = s.indexOf('.');
			if (pos==-1) {
				System.out.println("wrong filename");
				System.exit(-1);
			}
			s = s.substring(pos);
			if (s.equals(".bin")) {
				isJava = true;
				startPage = 0x18000>>7;
			} else if (s.equals(".txt")) {
				isTxt = true;
				startPage = TEXT_START>>7;
				len = 32;						// index 0 no text
			} else if (s.equals(".ttf")) {
				startPage = 0;
			} else {
				System.out.println("wrong file type: '"+s+"'");
				System.exit(-1);
			}

			BufferedReader in = new BufferedReader(new FileReader(f));

			while ((line = in.readLine()) != null) {

				if (isTxt) {

					int txtLen = line.length();
					byte[] str = line.getBytes();
					chkUml(str);
					if (txtLen > 20) txtLen = 20;
					mem[len] = (byte) txtLen;
					for (int i=0; i<txtLen; ++i) {
						mem[len+1+i] = str[i];
					}
					len += 32;
					if (len>MAX_TEXT) {
						System.out.println("textfile to long");
						System.exit(-1);
					}

				} else {
					if ((pos = line.indexOf('/'))!=-1) {
						st = new StringTokenizer(line.substring(0, pos), " \t\n\r\f,");
					} else {
						st = new StringTokenizer(line, " \t\n\r\f,");
					}
					while (st.hasMoreTokens()) {
						int val = Integer.parseInt(st.nextToken());
	
						if (isJava) {
							mem[len++] = (byte) (val>>>24);
							mem[len++] = (byte) (val>>>16);
							mem[len++] = (byte) (val>>>8);
							mem[len++] = (byte) val;
						} else {
							mem[len++] = (byte) val;
						}
	
						if (len==MAX_MEM) {
							System.out.println("too many words");
							System.exit(-1);
						}
						if (isJava && len==MAX_JAVA) {
							System.out.println("too many words: change jvmflash.asm");
							System.exit(-1);
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		System.out.println(len+" bytes to program");
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

	public void program() {

		int data;

		try {
			cmd(BBSys.CMD_STATUS, 0);
		} catch (CmdException e) {
			System.out.println("Station "+nr+ " not found");
			System.exit(-1);
		}
		try {
			if (nr!=0) cmd(BBSys.CMD_SET_STATE, BBSys.MS_DBG);
		} catch (CmdException x) {
		}


		int pages = (len+127)/128;
		for (int i=0; i<pages; ++i) {

			try {
				cmd(BBSys.CMD_FL_PAGE, startPage+i);
				for (int j=0; j<128; ++j) {
					System.out.print((i*128+j)+"          \r");
					data = mem[i*128+j] & 0xff;
					cmd(BBSys.CMD_FL_DATA, data);
				}
				cmd(BBSys.CMD_FL_PROG, 0);
			} catch (CmdException e) {
				try { Thread.sleep(1000); } catch (Exception x) {}
				--i;
			}
		}

		System.out.println("programmed");

		try {
			cmd(BBSys.CMD_FL_PAGE, startPage);		// compare
			for (int i=0; i<len; ++i) {
				System.out.print(i+"\r");
				byte val = (byte) cmd(BBSys.CMD_FL_READ, 0);
				if (val != mem[i]) {
					System.out.println("wrong data");
					System.exit(-1);
				}
			}
		} catch (CmdException e) {}

		System.out.println("program OK");

	}

	public static void main (String[] args) {

		if (args.length != 2) {
			System.out.println("usage: java Flash nr file");
			System.exit(-1);
		}

		Flash fl = new Flash(Integer.parseInt(args[0]), args[1]);
		fl.read();
		fl.program();
	}
}
