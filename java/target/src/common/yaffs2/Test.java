package yaffs2;

public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		yaffs2.utils.Globals.startUp(
			new yaffs2.platform.jop.Simulation_yaffscfg2k_C(),
			new yaffs2.platform.jop.PortConfiguration(),
			null);
		
	}

}
