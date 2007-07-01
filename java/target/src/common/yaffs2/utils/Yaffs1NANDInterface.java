package yaffs2.utils;

import yaffs2.port.yaffs_Device.eraseBlockInNANDInterface;
import yaffs2.port.yaffs_Device.initialiseNANDInterface;
import yaffs2.port.yaffs_Device.readChunkFromNANDInterface;
import yaffs2.port.yaffs_Device.writeChunkToNANDInterface;

public interface Yaffs1NANDInterface extends readChunkFromNANDInterface,
	eraseBlockInNANDInterface, initialiseNANDInterface, writeChunkToNANDInterface
{
}
