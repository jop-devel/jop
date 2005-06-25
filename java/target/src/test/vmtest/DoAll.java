/*
 * Created on 20.06.2005
 *
 */
package vmtest;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class DoAll {
	
	public static void main(String[] args) {
	
		util.Dbg.initSerWait();
		Clinit.main(args);
		Array.main(args);
		MultiArray.main(args);
		jbe.DoAll.main(args);
	}

}
