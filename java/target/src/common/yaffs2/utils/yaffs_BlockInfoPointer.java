package yaffs2.utils;

import yaffs2.port.yaffs_BlockInfo;

public class yaffs_BlockInfoPointer
{
	public yaffs_BlockInfoPointer()
	{
	}
	
	public yaffs_BlockInfoPointer(yaffs_BlockInfo value)
	{
		dereferenced = value;
	}
	
	public yaffs_BlockInfo dereferenced;
}
