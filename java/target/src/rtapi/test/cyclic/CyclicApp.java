package test.cyclic;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Terminal;

public class CyclicApp {
	

		public static Terminal term;

		public static void main(String args[]) {

			term = Terminal.getTerminal();
			JopSystem js = new JopSystem();

			CyclicSafelet s = new CyclicSafelet();
			term.writeln("Safelet created");

			js.startCycle(s);

			term.writeln("Application finished");
		}


}
