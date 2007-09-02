package yaffs2;

import yaffs2.platform.jop.InternalNANDYaffs1NANDInterface;
import yaffs2.utils.debug.communication.DebugDevice;

public class EraseNAND
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		InternalNANDYaffs1NANDInterface.instance.initialiseNAND(
				DebugDevice.getDebugDevice());
		
		for (int i = 0; i < 32; i++)
			InternalNANDYaffs1NANDInterface.instance.eraseBlockInNAND(
					DebugDevice.getDebugDevice(),
					i);
		
		System.out.println("Done erasing.");
	}

}
