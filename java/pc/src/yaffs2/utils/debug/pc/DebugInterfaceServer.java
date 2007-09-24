package yaffs2.utils.debug.pc;

import java.net.*;

import yaffs2.port.yaffs_Device;
import yaffs2.port.emulation.port_fileem2k_C;
import yaffs2.utils.Yaffs1NANDInterfacePrimitivesWrapper;
import yaffs2.utils.debug.communication.DebugDevice;
import yaffs2.utils.debug.communication.DebugSettings;
import yaffs2.utils.debug.communication.DirectInterfaceServerStub;

public class DebugInterfaceServer
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		ServerSocket serverSocket = new ServerSocket(7777, 0, InetAddress.getByName("127.0.0.1"));	
		Socket socket = serverSocket.accept();
		
		yaffs_Device dev = DebugDevice.getDebugDevice();
		byte[] spare = new byte[DebugSettings.SPARE_SERIALIZED_LENGTH];
		
		new DirectInterfaceServerStub(new Yaffs1NANDInterfacePrimitivesWrapper(
				dev, port_fileem2k_C.instance), 
				socket.getInputStream(),
				socket.getOutputStream(), "PC").
				receive(true, new byte[DebugSettings.NDATABYTESPERCHUNK], 0, spare, 0);
	}

}
