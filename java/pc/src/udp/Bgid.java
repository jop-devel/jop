package udp;
/**
*	BG id write.
*
*	write current time in sector 3
*
*	Bgid overview (in 64 KB sectors):
*
*		0x30000 :	Applet for TAL, bgid in oebb
*/

public class Bgid extends Flash {

	public Bgid(Tftp t) {
		super(t, "");
	}


	void setInt(int pos, int val) {

		pos *= 4;		// pos counts in 32 bit words
		mem[pos++] = (byte) (val>>>24);
		mem[pos++] = (byte) (val>>>16);
		mem[pos++] = (byte) (val>>>8);
		mem[pos++] = (byte) val;
		if (pos>len) len = pos;
	}

	public static void main (String[] args) {

		if (args.length != 1) {
			System.out.println("usage: java Bgid host");
			System.exit(-1);
		}

		Bgid fl = new Bgid(new Tftp(args[0]));
		fl.start = START_CONFIG;
		fl.len = 0;
		int val = (int) (System.currentTimeMillis()/1000);
System.out.println("id: "+val);
		fl.setInt(CONFIG_ID, val);
		fl.setInt(CONFIG_NOTID, ~val);
		fl.setInt(CONFIG_CORE, 2);
		fl.setInt(CONFIG_IO, 0);
		fl.setInt(CONFIG_APP, 0);
		fl.setInt(CONFIG_LEN, 300);
		fl.setInt(CONFIG_CHECK, 0);
		fl.setInt(CONFIG_IP_ADDR, 0);
		fl.setInt(CONFIG_IP_MASK, 0);
		fl.setInt(CONFIG_IP_GW, 0);
		fl.setInt(CONFIG_TAL_TYPE, 0);
		fl.setInt(CONFIG_TAL_PARAM, 0);
		fl.program();
	}
}
