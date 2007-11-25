package yaffs2;

public class TestWithNAND {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting FS...");
		
		// TODO Auto-generated method stub
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.jop.yaffscfg2k(),
				new yaffs2.platform.jop.PortConfiguration(),
				null);
		
		yaffs2.port.Dtest_C.small_overwrite_test("/",1);

		System.out.println("Finished test.");
	}

}
