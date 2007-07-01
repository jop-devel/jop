package yaffs2;

public class TestWithNAND {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.jop.yaffscfg2k(),
				new yaffs2.platform.jop.PortConfiguration(),
				null);
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
	}

}
