package kfl.ctrl;

/**
*	Send reset msg.
*/
import BBSys;
import Msg;

public class Reset {

	public static void main (String[] args) {

		if (args.length != 1) {
			System.out.println("usage: java Reset nr");
			System.exit(-1);
		}

		Msg m = new Msg();
		m.exchg(Integer.parseInt(args[0]), BBSys.CMD_RESET, 0);
	}
}
