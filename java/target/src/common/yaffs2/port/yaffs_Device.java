package yaffs2.port;

import yaffs2.utils.*;
import static yaffs2.port.Guts_H.*;

public class yaffs_Device
{
	/*----------------- Device ---------------------------------*/

	// PORT
	public yaffs_Device()
	{
		tempBuffer = new yaffs_TempBuffer[YAFFS_N_TEMP_BUFFERS];
		for (int i = 0; i < tempBuffer.length; i++)
			tempBuffer[i] = new yaffs_TempBuffer();
		
		objectBucket = new yaffs_ObjectBucket[YAFFS_NOBJECT_BUCKETS];
		for (int i = 0; i < objectBucket.length; i++)
			objectBucket[i] = new yaffs_ObjectBucket();
	}
	
	//struct yaffs_DeviceStruct {
		public list_head devList;
		public String name;

		/* Entry parameters set up way early. Yaffs sets up the rest.*/
		public int nDataBytesPerChunk;	/* Should be a power of 2 >= 512 */
		public int nChunksPerBlock;	/* does not need to be a power of 2 */
		public int nBytesPerSpare;	/* spare area size */
		public int startBlock;		/* Start block we're allowed to use */
		public int endBlock;		/* End block we're allowed to use */
		public int nReservedBlocks;	/* We want this tuneable so that we can reduce */
					/* reserved blocks on NOR and RAM. */
		
		/* Stuff used by the partitioned checkpointing mechanism */
		public int checkpointStartBlock;
		public int checkpointEndBlock;
		
		/* Stuff used by the shared space checkpointing mechanism */
		/* If this value is zero, then this mechanism is disabled */
		
		public int nCheckpointReservedBlocks; /* Blocks to reserve for checkpoint data */

		


		public int nShortOpCaches;	/* If <= 0, then short op caching is disabled, else
					 * the number of short op caches (don't use too many)
					 */

		public boolean useHeaderFileSize;	/* Flag to determine if we should use file sizes from the header */

		public boolean useNANDECC;		/* Flag to decide whether or not to use NANDECC */

		public int genericDevice;	/* Pointer to device context
					 * On an mtd this holds the mtd pointer.
					 */
		// PORT It's not really a pointer.
		public Object superBlock; // XXX only needed to mark superblock as "dirsty"?
	        
		/* NAND access functions (Must be set before calling YAFFS)*/

	    public interface writeChunkToNANDInterface {
	    	public boolean writeChunkToNAND (yaffs_Device dev,
					 int chunkInNAND, byte[] data, int dataIndex,
					 yaffs_Spare spare);
	    }
	    public writeChunkToNANDInterface writeChunkToNAND;
	
		public interface readChunkFromNANDInterface {
			public boolean readChunkFromNAND (yaffs_Device dev,
					  int chunkInNAND, byte[] data, int dataIndex,
					  yaffs_Spare spare);
		}
		public readChunkFromNANDInterface readChunkFromNAND;
		
		public interface eraseBlockInNANDInterface { 
			public boolean eraseBlockInNAND (yaffs_Device dev,
					 int blockInNAND);
		}
		public eraseBlockInNANDInterface eraseBlockInNAND;
		
		public interface initialiseNANDInterface {
			public boolean initialiseNAND (yaffs_Device dev);
		}
		public initialiseNANDInterface initialiseNAND;

	// #ifdef CONFIG_YAFFS_YAFFS2
		public interface writeChunkWithTagsToNANDInterface {
			public boolean writeChunkWithTagsToNAND (yaffs_Device dev,
						 int chunkInNAND, byte[] data, int dataIndex, 
						 yaffs_ExtendedTags tags);
		}
		public writeChunkWithTagsToNANDInterface writeChunkWithTagsToNAND;
		
		public interface readChunkWithTagsFromNANDInterface {
			public boolean readChunkWithTagsFromNAND (yaffs_Device dev,
						  int chunkInNAND, byte[] data, int dataIndex,
						  yaffs_ExtendedTags tags);
		}
		public readChunkWithTagsFromNANDInterface readChunkWithTagsFromNAND;
		
		public interface markNANDBlockBadInterface { 
			public boolean markNANDBlockBad (yaffs_Device dev, int blockNo);
		}
		public markNANDBlockBadInterface markNANDBlockBad;
		
		public interface queryNANDBlockInterface {
			public boolean queryNANDBlock  (yaffs_Device dev, int blockNo,
				       /*yaffs_BlockState*/ IntegerPointer state, IntegerPointer sequenceNumber);
		}
		public queryNANDBlockInterface queryNANDBlock;
		
	// #endif

		public boolean isYaffs2;
		
		/* The removeObjectCallback function must be supplied by OS flavours that 
		 * need it. The Linux kernel does not use this, but yaffs direct does use
		 * it to implement the faster readdir
		 */
		interface removeObjectCallbackInterface {
			void yaffsfs_RemoveObjectCallback (yaffs_Object obj);
		}
		removeObjectCallbackInterface removeObjectCallback; 
		
		/* Callback to mark the superblock dirsty */
		public interface markSuperBlockDirtyInterface {
			public boolean markSuperBlockDirty (Object superblock);
		}
		public markSuperBlockDirtyInterface markSuperBlockDirty; 
		
		public boolean wideTnodesDisabled; /* Set to disable wide tnodes */// XXX as of now, we cant support wideTnodes
		

		/* End of stuff that must be set before initialisation. */

		/* Runtime parameters. Set up by YAFFS. */

		public int chunkGroupBits;	/* 0 for devices <= 32MB. else log2(nchunks) - 16 */
		public int chunkGroupSize;	/* == 2^^chunkGroupBits */
		
		/* Stuff to support wide tnodes */
		public int tnodeWidth;
		public int tnodeMask; // XXX hope it works as a int
		
		/* Stuff to support various file offses to chunk/offset translations */
		/* "Crumbs" for nDataBytesPerChunk not being a power of 2 */
		public int crumbMask;	// XXX changed long->
		public int crumbShift;
		public int crumbsPerChunk;
		
		/* Straight shifting for nDataBytesPerChunk being a power of 2 */
		public int chunkShift;
		public int chunkMask;	// XXX changed long->
		

//	#ifdef __KERNEL__
//
//		struct semaphore sem;	/* Semaphore for waiting on erasure.*/
//		struct semaphore grossLock;	/* Gross locking semaphore */
//		__u8 *spareBuffer;	/* For mtdif2 use. Don't know the size of the buffer 
//					 * at compile time so we have to allocate it.
//					 */
//		void (*putSuperFunc) (struct super_block * sb);
//	#endif

		public boolean isMounted;
		
		public boolean isCheckpointed;


		/* Stuff to support block offsetting to support start block zero */
		public int internalStartBlock;
		public int internalEndBlock;
		public int blockOffset;
		public int chunkOffset;
		

		/* Runtime checkpointing stuff */
		public int checkpointPageSequence;   /* running sequence number of checkpoint pages */
		public int checkpointByteCount;
		public int checkpointByteOffset;
		public byte[] checkpointBuffer; int checkpointBufferIndex;
		public boolean checkpointOpenForWrite;
		public int blocksInCheckpoint;
		public int checkpointCurrentChunk;
		public int checkpointCurrentBlock;
		public int checkpointNextBlock;
		public int[] checkpointBlockList;
		public int checkpointMaxBlocks;
		
		/* Block Info */
		public yaffs_BlockInfo[] blockInfo;
		public byte[] chunkBits;	/* bitmap of chunks in use */
		public int chunkBitsIndex;
		public boolean blockInfoAlt;	/* was allocated using alternative strategy */
		public boolean chunkBitsAlt;	/* was allocated using alternative strategy */
		public int chunkBitmapStride;	/* Number of bytes of chunkBits per block. 
					 * Must be consistent with nChunksPerBlock.
					 */

		public int nErasedBlocks;
		public int allocationBlock;	/* Current block being allocated off */
		public int allocationPage;
		public int allocationBlockFinder;	/* Used to search for next allocation block */

		/* Runtime state */
		public int nTnodesCreated;
		public yaffs_Tnode freeTnodes;	// XXX array?
		public int nFreeTnodes;
		public yaffs_TnodeList allocatedTnodeList;

		public boolean isDoingGC;

		public int nObjectsCreated;
		public yaffs_Object freeObjects;
		public int nFreeObjects;

		public yaffs_ObjectList allocatedObjectList;

		public yaffs_ObjectBucket[] objectBucket; // PORT initialized in constructor

		public int nFreeChunks;

		public int currentDirtyChecker;	/* Used to find current dirtiest block */

		public int[] gcCleanupList;	/* objects to delete at the end of a GC. */

		/* Statistcs */
		public int nPageWrites;
		public int nPageReads;
		public int nBlockErasures;
		public int nErasureFailures;
		public int nGCCopies;
		public int garbageCollections;
		public int passiveGarbageCollections;
		public int nRetriedWrites;
		public int nRetiredBlocks;
		public int eccFixed;
		public int eccUnfixed;
		public int tagsEccFixed;
		public int tagsEccUnfixed;
		public int nDeletions;
		public int nUnmarkedDeletions;
		
		public boolean hasPendingPrioritisedGCs; /* We think this device might have pending prioritised gcs */

		/* Special directories */
		public yaffs_Object rootDir;
		public yaffs_Object lostNFoundDir;

		/* Buffer areas for storing data to recover from write failures TODO
		 *      __u8            bufferedData[YAFFS_CHUNKS_PER_BLOCK][YAFFS_BYTES_PER_CHUNK];
		 *      yaffs_Spare bufferedSpare[YAFFS_CHUNKS_PER_BLOCK];
		 */
		
		public int bufferedBlock;	/* Which block is buffered here? */
		public int doingBufferedBlockRewrite;

		public yaffs_ChunkCache[] srCache;
		public int srLastUse;

		public int cacheHits;

		/* Stuff for background deletion and unlinked files.*/
		public yaffs_Object unlinkedDir;	/* Directory where unlinked and deleted files live. */
		public yaffs_Object deletedDir;	/* Directory where deleted objects are sent to disappear. */
		public yaffs_Object unlinkedDeletion;	/* Current file being background deleted.*/
		public int nDeletedFiles;		/* Count of files awaiting deletion;*/
		public int nUnlinkedFiles;		/* Count of unlinked files. */
		public int nBackgroundDeletions;	/* Count of background deletions. */


		public yaffs_TempBuffer[] tempBuffer; // PORT initialized in constructor 
		
		public int maxTemp;
		public int unmanagedTempAllocations;
		public int unmanagedTempDeallocations;

		/* yaffs2 runtime stuff */
		public long sequenceNumber;	/* Sequence number of currently allocating block */
		public long oldestDirtySequence;

	//};

	//typedef struct yaffs_DeviceStruct yaffs_Device;
}
