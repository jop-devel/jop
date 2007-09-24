package yaffs2.port.emulation;



public class yflash_Block
{
//	typedef struct
//	{
		yflash_Page[] page; // The pages in the block
		
		yflash_Block()
		{
			page = new yflash_Page[yaffs_fileem2k_H.PAGES_PER_BLOCK];
			for (int i = 0; i < page.length; i++)
			{
				page[i] = new yflash_Page();
			}
		}
		
//	} yflash_Block;
}
