package yaffs2.port;

public class yaffsfs_Handle
{
//	typedef struct
//	{
	/*__u8*/ public boolean  inUse/*:1*/ ;		// this handle is in use
	/*__u8*/ public boolean  readOnly/*:1*/ ;	// this handle is read only
	/*__u8*/ public boolean  append/*:1*/ ;		// append only
	/*__u8*/ public boolean  exclusive/*:1*/ ;	// exclusive
	/*__u32*/ public int position;		// current position in file
	public yaffs_Object obj;	// the object
//	}yaffsfs_Handle;
}
