package yaffs2.utils.emulation;

import yaffs2.port.emulation.yramdisk_Block;
import yaffs2.port.emulation.yramdisk_Page;

public abstract class RamEmulationUnix
{
	public static void memset(yramdisk_Block s, byte c)
	{
		for (int i = 0; i < s.page.length; i++)
			memset(s.page[i], c);
	}
	
	public static void memset(yramdisk_Page s, byte c)
	{
		yaffs2.utils.Unix.memset(s.data, 0, c, s.data.length);
	}
}
