/**
*	BG id write.
*
*	write current time in sector 3
*
*	Bgid overview (in 64 KB sectors):
*
*		0x30000 :	Applet for TAL, bgid in oebb
*/
import java.io.*;
import java.util.*;

public class Bgid extends Flash {

	public Bgid() {
		super("");
	}


	void setInt(int val) {

		mem[len++] = (byte) (val>>>24);
		mem[len++] = (byte) (val>>>16);
		mem[len++] = (byte) (val>>>8);
		mem[len++] = (byte) val;
	}

	public static void main (String[] args) {

		if (args.length > 1) {
			System.out.println("usage: java Bgid [host]");
			System.exit(-1);
		}

		if (args.length==1) Tftp.setAddr(args[0]);

		Bgid fl = new Bgid();
		fl.start = START_APPL;
		fl.len = 0;
		int val = (int) (System.currentTimeMillis()/1000);
System.out.println("bgid: "+val);
		fl.setInt(val);
		fl.setInt(~val);
		fl.program();
	}
}
