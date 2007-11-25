package yaffs2;

import yaffs2.port.Dtest_C;

public class JOPDirectInterfaceClient {
	public static void main(String[] args)
	{
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.jop.Simulation_yaffscfg2k_C(),
				new yaffs2.platform.jop.PortConfiguration(), 
				null);				

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		Dtest_C.small_overwrite_test("/",1);
	}
}
