package yaffs2.utils;

import java.io.PrintStream;

import yaffs2.port.yaffscfg2k_C;

public class Globals
{
	/**
	 * Call this method to start up the file system. 
	 * @param configuration Should contain settings equivalent to those in direct/yaffscfg2k.c. 
	 * @param debugConfiguration May be null.
	 * @param portConfiguration Additional settings. 
	 */
	public static void startUp(yaffscfg2k_C configuration, 
			PortConfiguration portConfiguration, 
			DebugConfiguration debugConfiguration)
	{
		Globals.configuration = configuration;
		Globals.yaffs_traceMask = configuration.yaffs_traceMask();
		
		Globals.portConfiguration = portConfiguration;
		Globals.logStream = portConfiguration.logStream();
		
		Globals.debugConfiguration = debugConfiguration;
	}

	public static yaffscfg2k_C configuration;
	/**unsigned*/ public static int yaffs_traceMask;
	
	public static PortConfiguration portConfiguration;
	public static PrintStream logStream; 
	
	public static DebugConfiguration debugConfiguration;
	
	
}
