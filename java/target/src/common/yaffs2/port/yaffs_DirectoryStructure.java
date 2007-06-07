package yaffs2.port;

public class yaffs_DirectoryStructure
{
	//typedef struct {
	public list_head children = new list_head(this);	/* list of child links */
	//} yaffs_DirectoryStructure;
}
