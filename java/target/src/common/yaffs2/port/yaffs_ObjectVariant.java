package yaffs2.port;

public class yaffs_ObjectVariant
{
	// XXX memory hog
	// XXX initialize: either create all possible variants or initialize variant in yaffs_CreateNewObject()
	//typedef union {

//	Object variant;

	public yaffs_FileStructure fileVariant = new yaffs_FileStructure();
	public yaffs_DirectoryStructure directoryVariant = new yaffs_DirectoryStructure();
	public yaffs_SymLinkStructure symLinkVariant = new yaffs_SymLinkStructure();
	public yaffs_HardLinkStructure hardLinkVariant = new yaffs_HardLinkStructure();

	yaffs_FileStructure fileVariant()
	{
		return fileVariant;
	}
	yaffs_DirectoryStructure directoryVariant()
	{
		return directoryVariant;
	}
	yaffs_SymLinkStructure symLinkVariant()
	{
		return symLinkVariant;
	}
	yaffs_HardLinkStructure hardLinkVariant()
	{
		return hardLinkVariant;
	}
	//} yaffs_ObjectVariant;

}
