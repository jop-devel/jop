package wcet;


public class StartLift {


	public static void main(String[] args) {

		// just because it's not so simple to
		// have the main method in a deeper package
		// blame MS on this Makefile
		wcet.lift.Lift.main(null);
	}
			
}
