/*
 * UpDown.java
 *
 * 
 */
import BBSys;
import Msg;

public class UpDown {

	private Msg m;
	private List st = new LinkedList();


	public UpDown() {

		m = new Msg();

		for (int i=0; i<8; ++i) {
			int val = m.exchg(i, BBSys.CMD_STATUS, 0);
			if (val<0) {
				m.clear();
			} else {
				System.out.println("Station "+i+" found");
				st.add(new Integer(i));
			}
		}
	}

	public void doit(int cmd) {
		
		for (Iterator i = st.iterator(); i.hasNext(); ) {

			int nr = ((Integer) i.next()).intValue();
			int val = m.exchg(nr, cmd, 0);
			if (val<0) {
				System.out.println("Error "+val+" with station "+nr);
			}
		}
		try { Thread.sleep(1000); } catch (Exception e) {};
	}

	public static void main(String args[]) {
		UpDown c = new UpDown();
		for (;;) {
			c.doit(BBSys.CMD_UP);
			c.doit(BBSys.CMD_STOP);
			c.doit(BBSys.CMD_DOWN);
			c.doit(BBSys.CMD_STOP);
		}
	}


}
