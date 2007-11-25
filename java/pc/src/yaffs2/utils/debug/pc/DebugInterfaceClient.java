package yaffs2.utils.debug.pc;

import java.net.*;

import yaffs2.platform.emulation.PortConfiguration;
import yaffs2.port.Dtest_C;

public class DebugInterfaceClient
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 7777);
		
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.emulation.PCOnlySimulation_yaffscfg2k_C(
						socket.getInputStream(), socket.getOutputStream()), 
				new PortConfiguration(), 
				new yaffs2.platform.emulation.DebugConfiguration());				

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		Dtest_C.small_overwrite_test("/",1);
	}

}
