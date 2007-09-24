package yaffs2.utils.debug.communication;

import java.io.InputStream;
import java.io.OutputStream;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.UnexpectedException;
import yaffs2.utils.Yaffs1NANDInterface;

public class DirectInterfaceClientStub extends Transceiver implements Yaffs1NANDInterface
{
	yaffs_Device dev;
	
	public DirectInterfaceClientStub(yaffs_Device dev, InputStream rx, OutputStream tx,
			String nodeName)
	{
		super(rx, tx, nodeName);
		this.dev = dev;
	}
	
	public yaffs_Device deviceIdToDevice(int deviceId)
	{
		return dev; 
	}
	
	protected void processInput(int command,
			int deviceGenericDevice, 
			int devicenDataBytesPerChunk, int chunkInNAND, int blockInNAND, 
			byte[] data, int dataIndex, byte[] spare, int spareIndex)
	{
		// ignored
		switch (command)
		{
			case REPLY_DONE:
			case REPLY_READCHUNKFROMNAND:
				break;
			default:
				throw new UnexpectedException();
		}
	}
	
	public boolean readChunkFromNAND(yaffs_Device dev, int chunkInNAND,
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		send(CMD_READCHUNKFROMNAND, dev.subField1.genericDevice,
				dev.subField1.nDataBytesPerChunk,
				chunkInNAND, -1, data, dataIndex, spare == null ? null : spare.serialized, 
				spare == null ? 0 : spare.offset);
		receive(false, data, dataIndex, spare.serialized, 
				spare == null ? 0 : spare.offset);
		
		return true;
	}

	public boolean writeChunkToNAND(yaffs_Device dev, int chunkInNAND,
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		send(CMD_WRITECHUNKTONAND, dev.subField1.genericDevice,
				dev.subField1.nDataBytesPerChunk, chunkInNAND, -1, data, dataIndex, 
				spare == null ? null : spare.serialized, spare == null ? 0 : spare.offset);
		receive(false, null, 0, null, 0);
		return true;
	}

	public boolean eraseBlockInNAND(yaffs_Device dev, int blockInNAND)
	{
		send(CMD_ERASEBLOCKINNAND, dev.subField1.genericDevice,
				dev.subField1.nDataBytesPerChunk, -1, blockInNAND, null, 0, null, 0);
		receive(false, null, 0, null, 0);
		return true;
	}

	public boolean initialiseNAND(yaffs_Device dev)
	{
		send(CMD_INITIALISENAND, dev.subField1.genericDevice,
				dev.subField1.nDataBytesPerChunk, -1, -1, null, 0, null, 0);
		receive(false, null, 0, null, 0);
		return true;
	}

}
