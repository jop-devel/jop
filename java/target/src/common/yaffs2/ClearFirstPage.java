package yaffs2;

import yaffs2.platform.jop.InternalNANDYaffs1NANDInterface;
import yaffs2.port.yaffs_Spare;
import yaffs2.utils.debug.communication.DebugDevice;

public class ClearFirstPage
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

		InternalNANDYaffs1NANDInterface.instance.writeChunkToNAND(
				DebugDevice.getDebugDevice(),
				0,
				data,
				0,
				spare);
	}
}
