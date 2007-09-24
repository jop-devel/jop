package yaffs2.utils.debug.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import yaffs2.utils.UnexpectedException;
import yaffs2.utils.DebugUtils;

public abstract class Transceiver {
	static final int CMD_READCHUNKFROMNAND = 1;

	static final int CMD_WRITECHUNKTONAND = 2;

	static final int CMD_ERASEBLOCKINNAND = 3;

	static final int CMD_INITIALISENAND = 4;

	static final int REPLY_READCHUNKFROMNAND = 5;

	static final int REPLY_DONE = 6;

	static final byte[] START_DELIMITER = { 0x1 };

	static final byte[] END_DELIMITER = { 0x2 };

	protected InputStream rx;

	protected OutputStream tx;
	
	protected String nodeName;

	protected int rxSequenceNumber = -1;

	protected int txSequenceNumber = -1;
	
	
	public Transceiver(InputStream rx, OutputStream tx, String nodeName) {
		this.rx = rx;
		this.tx = tx;
		this.nodeName = nodeName;
	}

	public void send(int command, int deviceGenericDevice,
			int devicenDataBytesPerChunk, int chunkInNAND, int blockInNAND,
			byte[] data, int dataIndex, byte[] spare, int spareIndex) {
		try {
			
			tx.write(START_DELIMITER);

			tx.write(DebugUtils.intToByteArray(++txSequenceNumber));

			tx.write(DebugUtils.intToByteArray(command));
			tx.write(DebugUtils.intToByteArray(chunkInNAND));
			tx.write(DebugUtils.intToByteArray(blockInNAND));
			tx.write(DebugUtils.intToByteArray(data != null ? 1 : 0));
			if (data != null)
				tx.write(data, dataIndex, devicenDataBytesPerChunk);
			tx.write(DebugUtils.intToByteArray(spare != null ? 1 : 0));
			if (spare != null)
				tx.write(spare, spareIndex,
						DebugSettings.SPARE_SERIALIZED_LENGTH);

			tx.write(END_DELIMITER);

			
			tx.flush();

		} catch (IOException e) {
			throw new UnexpectedException();
		}
	}

	protected abstract void processInput(int command, int deviceGenericDevice,
			int devicenDataBytesPerChunk, int chunkInNAND, int blockInNAND,
			byte[] data, int dataIndex, byte[] spare, int spareIndex);

//	private char[] log = new char[200];
	
	public void receive(boolean loop, byte[] data, int dataIndex, byte[] spare,
			int spareIndex) {
		try {
			int ch;
			
			int logIndex = 0;

			do {
				while ((ch = rx.read()) != -1 && ch != START_DELIMITER[0]) {
//					if (logIndex < 200)
//						log[logIndex++] = (char) ch;
					System.out.write(ch);
				}

				if (ch == -1)
					break;

				{
					int lastSequenceNumber = rxSequenceNumber;


					rxSequenceNumber = DebugUtils.readIntFromInputStream(rx);

					if (!(lastSequenceNumber == -1)
							&& lastSequenceNumber + 1 != rxSequenceNumber)
						throw new UnexpectedException(
								"Sequence number mismatch!");

					int command = DebugUtils.readIntFromInputStream(rx);
					int chunkInNAND = DebugUtils.readIntFromInputStream(rx);
					int blockInNAND = DebugUtils.readIntFromInputStream(rx);

					boolean dataPresent = DebugUtils.readIntFromInputStream(rx) != 0;
					if (dataPresent)
						rx.read(data, dataIndex,
								DebugSettings.NDATABYTESPERCHUNK);

					boolean sparePresent = DebugUtils
							.readIntFromInputStream(rx) != 0;
					if (sparePresent)
						rx.read(spare, spareIndex,
								DebugSettings.SPARE_SERIALIZED_LENGTH);

					int endDelim = rx.read();

					if (endDelim != END_DELIMITER[0])
						throw new UnexpectedException(
								"Failed to receive message!");

					processInput(command, DebugSettings.GENERIC_DEVICE,
							DebugSettings.NDATABYTESPERCHUNK, chunkInNAND,
							blockInNAND, dataPresent ? data : null, dataIndex,
							sparePresent ? spare : null, spareIndex);
					
				}
			} while (loop);
		} catch (IOException e) {
			throw new UnexpectedException();
		}
	}
}
