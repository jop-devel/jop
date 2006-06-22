package jvm;

public class Except extends TestCase {
	
	public String getName() {
		return "Except";
	}
	
	public boolean test() {

		boolean ok = false;
		boolean thr = true;
		
		try {
//			if (thr) {
				throw new Exception("Message");				
//			}
		} catch (Exception e) {
			ok = true;
			e.printStackTrace();
		}

		return ok;
	}

	
}
