package yaffs2.port.emulation;

public class yramdisk_Block
{
//	typedef struct
//	{
//		yramdisk_Page page[32]; // The pages in the block
//		
//	} yramdisk_Block;
	
	public yramdisk_Page[] page;
	
	public yramdisk_Block()
	{
		page = new yramdisk_Page[32];
		for (int i = 0; i < page.length; i++)
			page[i] = new yramdisk_Page();
	}
}
