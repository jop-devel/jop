package yaffs2.utils.debug.pc;

import java.net.*;

import yaffs2.port.port_fileem2k_C;
import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.debug.communication.DebugDevice;
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
		
		yaffs_Device dev = DebugDevice.createDebugDevice();
		
		new DirectInterfaceServerStub(dev, port_fileem2k_C.instance, 
				socket.getInputStream(),
				socket.getOutputStream()).
				receive(true, new byte[dev.subField1.nDataBytesPerChunk], 0, new yaffs_Spare());
	}

}
