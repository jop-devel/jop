package yaffs2;

import yaffs2.platform.jop.InternalNANDYaffs1NANDInterface;
import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.debug.communication.DebugDevice;
import yaffs2.utils.debug.communication.DirectInterfaceServerStub;

public class JOPDirectInterfaceHost
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		yaffs_Device dev = DebugDevice.createDebugDevice();
		
		new DirectInterfaceServerStub(dev, 
			new InternalNANDYaffs1NANDInterface(),
			System.in,
			System.out)
		.receive(true, new byte[dev.subField1.nDataBytesPerChunk], 
				0, new yaffs_Spare());			
	}

}
