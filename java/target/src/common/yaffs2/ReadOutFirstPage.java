package yaffs2;

import yaffs2.platform.jop.InternalNANDYaffs1NANDInterface;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.debug.communication.DebugDevice;

public class ReadOutFirstPage
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		InternalNANDYaffs1NANDInterface.instance.initialiseNAND(
				DebugDevice.getDebugDevice());

		byte[] data = new byte[DebugDevice.getDebugDevice().subField1.nDataBytesPerChunk];
		yaffs_Spare spare = new yaffs_Spare();

		InternalNANDYaffs1NANDInterface.instance.readChunkFromNAND(
				DebugDevice.getDebugDevice(),
				0,
				data,
				0,
				spare);

		System.out.println("Data:");
		for (int i = 0; i < data.length; i++)
		{
			System.out.println((int)data[i] & 0xff);
		}
		System.out.println("Spare:");
		for (int i = 0; i < spare.serialized.length; i++)
		{
			System.out.println((int)spare.serialized[i] & 0xff);
		}
	}

}
