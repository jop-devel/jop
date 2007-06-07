package yaffs2.utils;

import yaffs2.port.yramdisk_Block;

public class yramdisk_BlockPointer {
	
	public yramdisk_BlockPointer()
	{
		
	}
	
	public yramdisk_BlockPointer(yramdisk_Block value) {
		dereferenced = value;
	}
	
	public yramdisk_Block dereferenced;

}