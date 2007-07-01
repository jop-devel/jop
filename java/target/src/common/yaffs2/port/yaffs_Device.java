package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_Device
{
	/*----------------- Device ---------------------------------*/

	public class yaffs_Device_Sub1
	{
		public list_head devList;
		public String name;
		public int nDataBytesPerChunk;
		public int nChunksPerBlock;
		public int nBytesPerSpare;
		public int startBlock;
		public int endBlock;
		public int nReservedBlocks;
		public int checkpointStartBlock;
		public int checkpointEndBlock;
		public int nCheckpointReservedBlocks;
		public int nShortOpCaches;
		public boolean useHeaderFileSize;
		public boolean useNANDECC;
		public int genericDevice;
		public Object superBlock;
		public writeChunkToNANDInterface writeChunkToNAND;
		public readChunkFromNANDInterface readChunkFromNAND;
		public eraseBlockInNANDInterface eraseBlockInNAND;
		public initialiseNANDInterface initialiseNAND;
		public writeChunkWithTagsToNANDInterface writeChunkWithTagsToNAND;
		public readChunkWithTagsFromNANDInterface readChunkWithTagsFromNAND;
		public markNANDBlockBadInterface markNANDBlockBad;
		public queryNANDBlockInterface queryNANDBlock;
		public boolean isYaffs2;
		removeObjectCallbackInterface removeObjectCallback;
		public markSuperBlockDirtyInterface markSuperBlockDirty;
		public boolean wideTnodesDisabled;
		public int chunkGroupBits;
		public int chunkGroupSize;
		
	}
	public class yaffs_Device_Sub2
	{
		public int tnodeWidth;
		public int tnodeMask;
		public int crumbMask;
		public int crumbShift;
		public int crumbsPerChunk;
		public int chunkShift;
		public int chunkMask;
		public boolean isMounted;
		public boolean isCheckpointed;
		public int internalStartBlock;
		public int internalEndBlock;
		public int blockOffset;
		public int chunkOffset;
		public int checkpointPageSequence;
		public int checkpointByteCount;
		public int checkpointByteOffset;
		public byte[] checkpointBuffer;
		int checkpointBufferIndex;
		public boolean checkpointOpenForWrite;
		public int blocksInCheckpoint;
		public int checkpointCurrentChunk;
		public int checkpointCurrentBlock;
		public int checkpointNextBlock;
		public int[] checkpointBlockList;
		public int checkpointMaxBlocks;
		public yaffs_BlockInfo[] blockInfo;
		public byte[] chunkBits;
		public int chunkBitsIndex;
		public boolean blockInfoAlt;
		public boolean chunkBitsAlt;
	}
	public class yaffs_Device_Sub3
	{
		public int chunkBitmapStride;
		public int nErasedBlocks;
		public int allocationBlock;
		public int allocationPage;
		public int allocationBlockFinder;
		public int nTnodesCreated;
		public yaffs_Tnode freeTnodes;
		public int nFreeTnodes;
		public yaffs_TnodeList allocatedTnodeList;
		public boolean isDoingGC;
		public int nObjectsCreated;
		public yaffs_Object freeObjects;
		public int nFreeObjects;
		public yaffs_ObjectList allocatedObjectList;
		public yaffs_ObjectBucket[] objectBucket;
		public int nFreeChunks;
		public int currentDirtyChecker;
		public int[] gcCleanupList;
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
	}
	// PORT
	public yaffs_Device()
	{
		tempBuffer = new yaffs_TempBuffer[Guts_H.YAFFS_N_TEMP_BUFFERS];
		for (int i = 0; i < tempBuffer.length; i++)
			tempBuffer[i] = new yaffs_TempBuffer();
		
		subField3.objectBucket = new yaffs_ObjectBucket[Guts_H.YAFFS_NOBJECT_BUCKETS];
		for (int i = 0; i < subField3.objectBucket.length; i++)
			subField3.objectBucket[i] = new yaffs_ObjectBucket();
	}
	
	public yaffs_Device_Sub1 subField1 = new yaffs_Device_Sub1();
	public yaffs_Device_Sub2 subField2 = new yaffs_Device_Sub2();
	public yaffs_Device_Sub3 subField3 = new yaffs_Device_Sub3();
	
	public interface writeChunkToNANDInterface {
	    	public boolean writeChunkToNAND (yaffs_Device dev,
					 int chunkInNAND, byte[] data, int dataIndex,
					 yaffs_Spare spare);
	    }
	    public interface readChunkFromNANDInterface {
			public boolean readChunkFromNAND (yaffs_Device dev,
					  int chunkInNAND, byte[] data, int dataIndex,
					  yaffs_Spare spare);
		}
		public interface eraseBlockInNANDInterface { 
			public boolean eraseBlockInNAND (yaffs_Device dev,
					 int blockInNAND);
		}
		public interface initialiseNANDInterface {
			public boolean initialiseNAND (yaffs_Device dev);
		}
		// #ifdef CONFIG_YAFFS_YAFFS2
		public interface writeChunkWithTagsToNANDInterface {
			public boolean writeChunkWithTagsToNAND (yaffs_Device dev,
						 int chunkInNAND, byte[] data, int dataIndex, 
						 yaffs_ExtendedTags tags);
		}
		public interface readChunkWithTagsFromNANDInterface {
			public boolean readChunkWithTagsFromNAND (yaffs_Device dev,
						  int chunkInNAND, byte[] data, int dataIndex,
						  yaffs_ExtendedTags tags);
		}
		public interface markNANDBlockBadInterface { 
			public boolean markNANDBlockBad (yaffs_Device dev, int blockNo);
		}
		public interface queryNANDBlockInterface {
			public boolean queryNANDBlock  (yaffs_Device dev, int blockNo,
				       /*yaffs_BlockState*/ IntegerPointer state, IntegerPointer sequenceNumber);
		}
		/* The removeObjectCallback function must be supplied by OS flavours that 
		 * need it. The Linux kernel does not use this, but yaffs direct does use
		 * it to implement the faster readdir
		 */
		interface removeObjectCallbackInterface {
			void yaffsfs_RemoveObjectCallback (yaffs_Object obj);
		}
		/* Callback to mark the superblock dirsty */
		public interface markSuperBlockDirtyInterface {
			public boolean markSuperBlockDirty (Object superblock);
		}
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
