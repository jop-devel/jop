package yaffs2.port;

public class yaffs_SuperBlockHeader
{
	/* The static layout of bllock usage etc is stored in the super block header */
	//typedef struct {
	        int StructType;
		int version;
		int checkpointStartBlock;
		int checkpointEndBlock;
		int startBlock;
		int endBlock;
		int[] rfu = new int[100];
	//} yaffs_SuperBlockHeader;
}
