package yaffs2.platform.jop;

import yaffs2.port.*;
import yaffs2.port.yaffs_Device;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.Yaffs1NANDInterface;

import com.jopdesign.sys.Native;

// XXX change to implement Yaffs1NANDInterface, add constructor for initialization
public class InternalNANDYaffs1NANDInterface implements Yaffs1NANDInterface 
{
	public static InternalNANDYaffs1NANDInterface instance = new InternalNANDYaffs1NANDInterface();
	
	static final int IO_NAND = 0x100000;
	static final int CLE = 1;	// command latch enable
	static final int ALE = 2;	// address latch enable
	static final int COMMAND_ERASE = 0x60;
	static final int COMMAND_ERASE_CONFIRM = 0xD0;
	static final int COMMAND_PROGRAM = 0x80;
	static final int COMMAND_PRORGAM_CONFIRM = 0x10;
	static final int COMMAND_READA = 00;
	static final int COMMAND_READB = 01;
	static final int COMMAND_READC = 50;
	
	static final int nPagesPerBlock = 32;
	
	/**
	 * 
	 * @return true if a Error is signalled by the error bit
	 */
	static boolean errorOccurred()
	{
//		 07: read status register command, 100000 address, 1 = A0 = CLE
		Native.wrMem(0x70, 0x100001);
//		 S0=error bit S6=controller inactive S7=wr protection S8=nReady/Busy Signal
		return ((Native.rdMem(0x100000)&0x01) == 0x01) ? true : false;
	}
	
	/**
	 * 
	 *  waits until the nand is ready
	 */
	static int waitForNandReady(String msg)
	{
		int i = 0;
		int j;
		while(((j = Native.rdMem(0x100000)) & 0x100) != 0x100)	// wait if rdy signal is 0
		{i++;}
		System.out.print(msg);System.out.println(i);
		//Dbg.wr(msg);Dbg.hexVal(i);Dbg.wr("\n");
		return j;
	}
	
	/**
	 * 
	 *  writes data to nand at the address given
	 *  returns true if error occurred
	 */
	public boolean writeChunkToNAND(yaffs_Device dev, int chunkInNAND, 
			byte[] data, int dataIndex, yaffs_Spare spare)
	{
		if (data != null) {
			int column = 0, pointer = 0, addr0, addr1;

			// (chunkInNAND % (PAGES_PER_BLOCK)) address in block
			addr0 = (chunkInNAND % 32) & 0x1f;

			// (chunkInNAND / (PAGES_PER_BLOCK) block address
			addr0 = (addr0 & 0x1f) | (((chunkInNAND / 32) << 5) & 0xe0);
			addr1 = ((chunkInNAND / 32) >>> 3) & 0xff;

			Native.wrMem(pointer, IO_NAND + CLE); // pointer op.
			Native.wrMem(COMMAND_PROGRAM, IO_NAND + CLE); // page program
															// command
			Native.wrMem(column, IO_NAND + ALE); // column address 0-7
			Native.wrMem(addr0, IO_NAND + ALE); // page address 9-16
			Native.wrMem(addr1, IO_NAND + ALE); // page address 17-24
			for (int i = 0; i < dev.subField1.nDataBytesPerChunk; i++)
				Native.wrMem(data[i], IO_NAND + 0); // input data
			Native.wrMem(COMMAND_PRORGAM_CONFIRM, IO_NAND + CLE); // confirm
																	// code,
			waitForNandReady("data written.");
			
			if (errorOccurred())
				return Guts_H.YAFFS_FAIL;
		}
		
		if (spare != null)
		{
				// waitForNandReady("data written."); shall we wait before writing?
				int column = 0, pointer = 80, addr0, addr1;

				addr0 = 0;
				addr1 = 0;

				Native.wrMem(pointer, IO_NAND + CLE); // pointer op.
				Native.wrMem(COMMAND_PROGRAM, IO_NAND + CLE); // page program command
				Native.wrMem(column, IO_NAND + ALE); // column address 0-7
				Native.wrMem(addr0, IO_NAND + ALE); // page address 9-16
				Native.wrMem(addr1, IO_NAND + ALE); // page address 17-24
				for (int i = 0; i < spare.SERIALIZED_LENGTH; i++)
					Native.wrMem(spare.serialized[i], IO_NAND + 0); // input data
				Native.wrMem(COMMAND_PRORGAM_CONFIRM, IO_NAND + CLE); // confirm
																		// code,
				waitForNandReady("data written.");
				
				if (errorOccurred())
					return Guts_H.YAFFS_FAIL;
		}
		
		return Guts_H.YAFFS_OK;
	}
	
	public boolean eraseBlockInNAND(yaffs_Device dev, int blockNumber)
	{		
		int addr0 = (blockNumber & 0x07) << 5; // only A14-A26 matters: the block address
		int addr1 = (blockNumber >>> 3) & 0xff;
		
		System.out.print("blockNumber: ");
		System.out.println(blockNumber);
		System.out.print("addr0: ");
		System.out.println(Integer.toHexString(addr0));
		System.out.print("addr1: ");
		System.out.println(Integer.toHexString(addr1));
		
		Native.wrMem(COMMAND_ERASE, IO_NAND+CLE);
		Native.wrMem(addr0, IO_NAND+ALE);	// address 9-16
		Native.wrMem(addr1, IO_NAND+ALE);	// address 17-24
		Native.wrMem(COMMAND_ERASE_CONFIRM, IO_NAND+CLE);
		waitForNandReady("block erased.");	

		return !errorOccurred();
	}
	
	
	/*public byte[] readFromNAND(int chunkInNAND) */
	public boolean readChunkFromNAND(yaffs_Device dev, int chunkInNAND, byte[] data, int dataIndex, yaffs_Spare spare)
	{
		int column = 0, pointer = 0, addr0, addr1;
		if (data != null) {
			// (chunkInNAND % (PAGES_PER_BLOCK)) address in block
			addr0 = (chunkInNAND % 32) & 0x1f;
			// (chunkInNAND / (PAGES_PER_BLOCK) block address
			addr0 = (addr0 & 0x1f) | (((chunkInNAND / 32) << 5) & 0xe0);
			addr1 = ((chunkInNAND / 32) >>> 3) & 0xff;

			Native.wrMem(pointer, IO_NAND + CLE); // Read A command
			Native.wrMem(column, IO_NAND + ALE); // column address 0-7
			Native.wrMem(addr0, IO_NAND + ALE); // page address 9-16
			Native.wrMem(addr1, IO_NAND + ALE); // page address 17-24

			// TODO conversion
			data[0] = (byte) (waitForNandReady("data read.") & 0xff);

			for (int i = 1; i < dev.subField1.nDataBytesPerChunk - 1; i++)
				data[i] = (byte) (Native.rdMem(IO_NAND) & 0xff);
			
			if (errorOccurred())
				return Guts_H.YAFFS_FAIL;
		}
		
		if (spare != null)
		{
			pointer = 80;
			addr0 = (chunkInNAND % 32) & 0x1f;
			addr0 = (addr0 & 0x1f) | (((chunkInNAND / 32) << 5) & 0xe0);
			addr1 = ((chunkInNAND / 32) >>> 3) & 0xff;
			Native.wrMem(pointer, IO_NAND + CLE); // Read A command
			Native.wrMem(column, IO_NAND + ALE); // column address 0-7
			Native.wrMem(addr0, IO_NAND + ALE); // page address 9-16
			Native.wrMem(addr1, IO_NAND + ALE); // page address 17-24

			data[0] = (byte) (waitForNandReady("data read.") & 0xff);
			for (int i = 1; i < spare.SERIALIZED_LENGTH - 1; i++)
				data[i] = (byte) (Native.rdMem(IO_NAND) & 0xff);
			
			if (errorOccurred())
				return Guts_H.YAFFS_FAIL;
		}
		
		return Guts_H.YAFFS_OK;
	}
	
	public boolean initialiseNAND(yaffs_Device dev)
	{
		//CheckInit();
		
		return Guts_H.YAFFS_OK;
	}
}