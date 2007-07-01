package yaffs2.utils.debug.communication;

import java.io.InputStream;
import java.io.OutputStream;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.UnexpectedException;
import yaffs2.utils.Yaffs1NANDInterface;


public class DirectInterfaceServerStub extends Transceiver
{
	yaffs2.utils.Yaffs1NANDInterface implementation;
	yaffs_Device dev;
	
	public DirectInterfaceServerStub(yaffs_Device dev, Yaffs1NANDInterface implementation,
			InputStream rx, OutputStream tx)
	{
		super(rx, tx);
		this.implementation = implementation;
		this.dev = dev;
	}
	
	public yaffs_Device deviceIdToDevice(int deviceId)
	{
		return dev; 
	}
	
	/* (non-Javadoc)
	 * @see yaffs2.utils.debug.communication.ServerStub#translate(int, int, int, int, byte[], yaffs2.port.yaffs_Spare)
	 */
	protected void processInput(int command, yaffs_Device dev, int chunkInNAND, int blockInNAND, 
			byte[] data, int dataIndex,	yaffs_Spare spare)
	{
		switch (command)
		{
			case CMD_ERASEBLOCKINNAND:
				implementation.eraseBlockInNAND(dev, blockInNAND);
				break;
			case CMD_INITIALISENAND:
				implementation.initialiseNAND(dev);
				break;
			case CMD_READCHUNKFROMNAND:
				implementation.readChunkFromNAND(dev, chunkInNAND, data, dataIndex, spare);
				send(REPLY_READCHUNKFROMNAND, dev, chunkInNAND, blockInNAND, data, dataIndex, spare);
				break;
			case CMD_WRITECHUNKTONAND:
				implementation.writeChunkToNAND(dev, chunkInNAND, data, dataIndex, spare);
				break;
			default:
				throw new UnexpectedException();
		}
	}
}
