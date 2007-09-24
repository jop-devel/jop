package yaffs2;

import yaffs2.platform.jop.InternalNANDYaffs1NANDInterfacePrimitives;
import yaffs2.utils.debug.communication.DebugSettings;
import yaffs2.utils.debug.communication.DirectInterfaceServerStub;

public class JOPDirectInterfaceHost
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new DirectInterfaceServerStub( 
			InternalNANDYaffs1NANDInterfacePrimitives.instance,
			System.in,
			System.out, "JOP")
		.receive(true, new byte[DebugSettings.NDATABYTESPERCHUNK], 
				0, new byte[DebugSettings.SPARE_SERIALIZED_LENGTH], 0);			
	}

}
