package yaffs2.port;

import static yaffs2.port.yaffs_fileem2k_H.*;

public class yflash_Block
{
//	typedef struct
//	{
		yflash_Page[] page; // The pages in the block
		
		yflash_Block()
		{
			page = new yflash_Page[PAGES_PER_BLOCK];
			for (int i = 0; i < page.length; i++)
			{
				page[i] = new yflash_Page();
			}
		}
		
//	} yflash_Block;
}
