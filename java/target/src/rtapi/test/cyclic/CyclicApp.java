package test.cyclic;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Terminal;

public class CyclicApp {
	

		public static Terminal term;

		public static void main(String args[]) {

			term = Terminal.getTerminal();

			CyclicSafelet s = new CyclicSafelet();
			term.writeln("Safelet created");

			JopSystem.startCycle(s);

			term.writeln("Main method finished");
		}


}
