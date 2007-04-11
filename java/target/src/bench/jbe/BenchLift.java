package jbe;

import jbe.lift.LiftControl;
import jbe.lift.TalIo;

public class BenchLift extends BenchMark {

	public BenchLift() {

		// initialization
		ctrl = new LiftControl();
		io = new TalIo();
	}

	public int test(int cnt) {

		int i;

		for (i=0; i<cnt; ++i) {
			loop();
		}

		return i;
	}

	public String getName() {

		return "Lift";
	}
	
	private static LiftControl ctrl;
	private static TalIo io;

	static void loop() {
		ctrl.setVals();
		ctrl.getVals();
		ctrl.loop(io);
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchLift();

		Execute.perform(bm);
	}
			
}
