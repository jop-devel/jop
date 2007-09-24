package yaffs2.port.emulation;

public class yramdisk_Device {
//	typedef struct
//	{
//		yramdisk_Block **block;
//		int nBlocks;
//	} yramdisk_Device;
	
	public void set_nBlocks(int n)
	{
		nBlocks = n;
	}
	
	public int get_nBlocks()
	{
		return nBlocks;
	}
	
	public void set_block()
	{
		block = new yramdisk_Block[nBlocks];
	}
	
	//yramdisk_BlockPointer block;
	public yramdisk_Block[] block;
	public int nBlocks;
}
