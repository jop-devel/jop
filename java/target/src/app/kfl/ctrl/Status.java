/*
 * Status.java
 *
 * 
 */
import Station;

public class Status {

	private List st;

	public Status() {

		st = Station.find();
	}

	public void doit() {
		
		for (Iterator i = st.iterator(); i.hasNext(); ) {
			((Station) i.next()).status();
		}
	}

	public static void main(String args[]) {
		Status c = new Status();
		for (int i=1; true; ++i) {
			System.out.print(i+" ");
			c.doit();
			System.out.print(" \r");
		}
	}
}
