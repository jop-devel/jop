package yaffs2.port.emulation;


public class yramdisk_BlockPointer {
	
	public yramdisk_BlockPointer()
	{
		
	}
	
	public yramdisk_BlockPointer(yramdisk_Block value) {
		dereferenced = value;
	}
	
	public yramdisk_Block dereferenced;

}