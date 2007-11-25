package yaffs2.utils.debug.pc;

import yaffs2.platform.emulation.PortConfiguration;
import yaffs2.port.Dtest_C;

public class DirectInterfaceClient
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.emulation.Simulation_yaffscfg2k_C("COM1"), 
				new PortConfiguration(), 
				new yaffs2.platform.emulation.DebugConfiguration());

		// done in dtest already
//		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		Dtest_C.small_overwrite_test("/",1);		
	}

}
