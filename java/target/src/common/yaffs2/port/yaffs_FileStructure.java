package yaffs2.port;

public class yaffs_FileStructure
{
	//typedef struct {
	/** __u32 */ public int fileSize;
	/** __u32 */ public int scannedFileSize;
	/** __u32 */ public int shrinkSize;
	public int topLevel;
	public yaffs_Tnode top;
	//} yaffs_FileStructure;


}
