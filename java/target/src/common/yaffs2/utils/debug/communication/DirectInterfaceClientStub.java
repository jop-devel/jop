package yaffs2.utils.debug.communication;

import java.io.InputStream;
import java.io.OutputStream;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.Yaffs1NANDInterface;

public class DirectInterfaceClientStub extends Transceiver implements Yaffs1NANDInterface
{
	yaffs_Device dev;
	
	public DirectInterfaceClientStub(yaffs_Device dev, InputStream rx, OutputStream tx)
	{
		super(rx, tx);
	}
	
	public yaffs_Device deviceIdToDevice(int deviceId)
	{
		return dev; 
	}
	
	public void processInput(int command, yaffs_Device dev, int chunkInNAND, int blockInNAND, byte[] data, int dataIndex, yaffs_Spare spare)
	{
		// ignored
	}
	
	public boolean readChunkFromNAND(yaffs_Device dev, int chunkInNAND,
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		send(CMD_READCHUNKFROMNAND, dev, chunkInNAND, -1, data, dataIndex, spare);
		receive(false, data, dataIndex, spare);
		
		return true;
	}

	public boolean writeChunkToNAND(yaffs_Device dev, int chunkInNAND,
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		send(CMD_WRITECHUNKTONAND, dev, chunkInNAND, -1, data, dataIndex, spare);
		return true;
	}

	public boolean eraseBlockInNAND(yaffs_Device dev, int blockInNAND)
	{
		send(CMD_ERASEBLOCKINNAND, dev, -1, blockInNAND, null, 0, null);
		return true;
	}

	public boolean initialiseNAND(yaffs_Device dev)
	{
		send(CMD_INITIALISENAND, dev, -1, -1, null, 0, null); 
		return true;
	}

}
