package yaffs2.utils.debug.pc;

import java.io.BufferedInputStream;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.port.emulation.port_fileem2k_C;
import yaffs2.utils.debug.communication.DebugDevice;
import yaffs2.utils.debug.communication.DirectInterfaceServerStub;

public class DirectInterfaceHost
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		SerialInterface serialInterface = new SerialInterface("COM1");
		
		final yaffs_Device dev = DebugDevice.getDebugDevice();

//		MS: uncomment it to avoid syntx errors.
//		Shall we really include a reference to joptraget in pc?
//		I don't think that makes sense.
		
//		new DirectInterfaceServerStub(dev, port_fileem2k_C.instance, 
//				new BufferedInputStream(serialInterface.getInputStream()),
//			serialInterface.getOutputStream(), "PC").receive(true, new byte[dev.subField1.nDataBytesPerChunk], 0, new byte[yaffs_Spare.SERIALIZED_LENGTH], 0);
	}

}