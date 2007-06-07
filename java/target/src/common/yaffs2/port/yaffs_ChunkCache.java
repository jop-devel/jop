package yaffs2.port;

/* ChunkCache is used for short read/write operations.*/
public class yaffs_ChunkCache
{
	yaffs_Object object;
	int chunkId;
	int lastUse;
	boolean dirty;
	int nBytes;		/* Only valid if the cache is dirty */
	boolean locked;		/* Can't push out or flush while locked. */
//	#ifdef CONFIG_YAFFS_YAFFS2
	/*__u8 **/ byte[] data; // PORT It is initialized in yaffs_GutsInitialise()
	int dataIndex;	
//	#else
//	byte[] data = new byte[Guts_H.YAFFS_BYTES_PER_CHUNK];
//	#endif
}
