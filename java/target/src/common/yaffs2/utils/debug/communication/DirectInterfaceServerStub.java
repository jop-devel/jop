package yaffs2.utils.debug.communication;

import java.io.InputStream;
import java.io.OutputStream;

import yaffs2.utils.UnexpectedException;
import yaffs2.utils.Yaffs1NANDInterfacePrimitives;


public class DirectInterfaceServerStub extends Transceiver
{
	yaffs2.utils.Yaffs1NANDInterfacePrimitives implementation;
	
	public DirectInterfaceServerStub(Yaffs1NANDInterfacePrimitives implementation,
			InputStream rx, OutputStream tx, String nodeName)
	{
		super(rx, tx, nodeName);
		this.implementation = implementation;
	}
	
	/* (non-Javadoc)
	 * @see yaffs2.utils.debug.communication.ServerStub#translate(int, int, int, int, byte[], yaffs2.port.yaffs_Spare)
	 */
	protected void processInput(int command, int deviceGenericDevice, 
			int devicenDataBytesPerChunk, int chunkInNAND, int blockInNAND, 
			byte[] data, int dataIndex,	byte[] spare, int spareIndex)
	{
		switch (command)
		{
			case CMD_ERASEBLOCKINNAND:
				implementation.eraseBlockInNAND(deviceGenericDevice, devicenDataBytesPerChunk, blockInNAND);
				send(REPLY_DONE, deviceGenericDevice, devicenDataBytesPerChunk,
						0, 0, null, 0, null, 0);
				break;
			case CMD_INITIALISENAND:
				implementation.initialiseNAND(deviceGenericDevice, devicenDataBytesPerChunk);
				send(REPLY_DONE, deviceGenericDevice, devicenDataBytesPerChunk,
						0, 0, null, 0, null, 0);
				break;
			case CMD_READCHUNKFROMNAND:
				implementation.readChunkFromNAND(deviceGenericDevice, devicenDataBytesPerChunk, 
						chunkInNAND, data, dataIndex, spare, spareIndex);
				send(REPLY_READCHUNKFROMNAND, deviceGenericDevice, devicenDataBytesPerChunk, 
						chunkInNAND, blockInNAND, data, dataIndex, 
						spare, spareIndex);
//				System.out.println("Reply complete."); // XXX
				break;
			case CMD_WRITECHUNKTONAND:
				implementation.writeChunkToNAND(deviceGenericDevice, devicenDataBytesPerChunk, 
						chunkInNAND, data, dataIndex, spare, spareIndex);
				send(REPLY_DONE, deviceGenericDevice, devicenDataBytesPerChunk,
						0, 0, null, 0, null, 0);
				break;
			default:
				throw new UnexpectedException();
		}
	}
}
