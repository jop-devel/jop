package yaffs2.utils.debug.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.UnexpectedException;
import yaffs2.utils.DebugUtils;

public abstract class Transceiver
{
	static final int CMD_READCHUNKFROMNAND = 1;
	static final int CMD_WRITECHUNKTONAND = 2;
	static final int CMD_ERASEBLOCKINNAND = 3;
	static final int CMD_INITIALISENAND = 4;
	
	static final int REPLY_READCHUNKFROMNAND = 5;	
	
	
	static final byte[] START_DELIMITER = { 0x1 };
	static final byte[] END_DELIMITER = { 0x2 };
		
	protected InputStream rx;
	protected OutputStream tx;
	protected int sequenceNumber = -1;

	public Transceiver(InputStream rx, OutputStream tx)
	{
		this.rx = rx;
		this.tx = tx;
	}
	
	public void send(int command, yaffs_Device dev, int chunkInNAND, int blockInNAND, 
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		try
		{
			tx.write(START_DELIMITER);
			
			tx.write(DebugUtils.intToByteArray(sequenceNumber++));
			
			tx.write(DebugUtils.intToByteArray(command));
			tx.write(DebugUtils.intToByteArray(dev.subField1.genericDevice));
			tx.write(DebugUtils.intToByteArray(chunkInNAND));
			tx.write(DebugUtils.intToByteArray(blockInNAND));
			tx.write(data, dataIndex, dev.subField1.nDataBytesPerChunk);
			tx.write(spare.serialized, spare.offset, spare.SERIALIZED_LENGTH);
			
			tx.write(END_DELIMITER);
		}
		catch (IOException e)
		{
			throw new UnexpectedException();
		}
	}
	
	public abstract yaffs_Device deviceIdToDevice(int deviceId);

	protected abstract void processInput(int command, yaffs_Device dev, int chunkInNAND, int blockInNAND, 
			byte[] data, int dataIndex, yaffs_Spare spare);
	
	public void receive(boolean loop, byte[] data, int dataIndex, yaffs_Spare spare)
	{
		try
		{
			int ch; 

			while (loop)
			{
				while ((ch = rx.read()) != -1 && ch != START_DELIMITER[0])
				{
					System.out.write(ch);
				}
				
				if (ch == -1)
					break;
				
				{
					int lastSequenceNumber = sequenceNumber;
					sequenceNumber = DebugUtils.readIntFromInputStream(rx);
					
					if (!(lastSequenceNumber == -1) && lastSequenceNumber +1 != sequenceNumber)
						throw new UnexpectedException("Sequence number mismatch!");
					
					int command = DebugUtils.readIntFromInputStream(rx);
					yaffs_Device dev = deviceIdToDevice(DebugUtils.readIntFromInputStream(rx));
					int chunkInNAND = DebugUtils.readIntFromInputStream(rx);
					int blockInNAND = DebugUtils.readIntFromInputStream(rx);
					rx.read(data, dataIndex, dev.subField1.nDataBytesPerChunk);

					rx.read(spare.serialized, spare.offset, spare.SERIALIZED_LENGTH);
					
					int endDelim = rx.read();
					
					if (endDelim != END_DELIMITER[0])
						throw new UnexpectedException("Failed to receive message!");
					
					processInput(command, (dev), chunkInNAND, blockInNAND, 
							data, dataIndex, spare);
				}
			}
		}
		catch (IOException e)
		{
			throw new UnexpectedException();
		}
	}
}
