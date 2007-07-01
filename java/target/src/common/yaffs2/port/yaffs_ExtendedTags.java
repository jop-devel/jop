package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_ExtendedTags
{
	
	/**unsigned*/ public int validMarker0;
		public boolean  chunkUsed;	/*  Status of the chunk: used or unused */
		/**unsigned*/ public int objectId;	/* If 0 then this is not part of an object (unused) */
		/**unsigned*/ public int chunkId;	/* If 0 then this is a header, else a data chunk */
		/**unsigned*/ public int byteCount;	/* Only valid for data chunks */

		/* The following stuff only has meaning when we read */
		/**yaffs_ECCResult*/ public int eccResult;
		/**unsigned*/ public boolean blockBad;	

		/* YAFFS 1 stuff */
		/**unsigned*/ public boolean chunkDeleted;	/* The chunk is marked deleted */
		/**unsigned*/ public int serialNumber;	/* Yaffs1 2-bit serial number */

		/* YAFFS2 stuff */
		/**unsigned*/ public int sequenceNumber;	/* The sequence number of this block */

		/* Extra info if this is an object header (YAFFS2 only) */

		/**unsigned*/ public boolean extraHeaderInfoAvailable;	/* There is extra info available if this is not zero */
		/**unsigned*/ public int extraParentObjectId;	/* The parent object */
		/**unsigned*/ public boolean extraIsShrinkHeader;	/* Is it a shrink header? */
		/**unsigned*/ public boolean extraShadows;		/* Does this shadow another object? */

		/**yaffs_ObjectType*/ public int extraObjectType;	/* What object type? */

		/**unsigned*/ public int extraFileLength;		/* Length if it is a file */
		/**unsigned*/ public int extraEquivalentObjectId;	/* Equivalent object Id if it is a hard link */

		/*boolean*/ public int validMarker1;
		
		public static final int SERIALIZED_LENGTH = 72;
		
		public void writeTagsToByteArray(byte[] array, int index)
		{
			Utils.writeIntToByteArray(array, index+4, validMarker0);
			Utils.writeBooleanAsIntToByteArray(array, index+4, chunkUsed);
			Utils.writeIntToByteArray(array, index+8, objectId);
			Utils.writeIntToByteArray(array, index+12, chunkId);
			Utils.writeIntToByteArray(array, index+16, byteCount);
			
			Utils.writeIntToByteArray(array, index+20, eccResult);
			Utils.writeBooleanAsIntToByteArray(array, index+24, blockBad);
			
			Utils.writeBooleanAsIntToByteArray(array, index+28, chunkDeleted);
			Utils.writeIntToByteArray(array, index+32, serialNumber);
			
			Utils.writeIntToByteArray(array, index+36, sequenceNumber);
			
			Utils.writeBooleanAsIntToByteArray(array, index+40, extraHeaderInfoAvailable);
			Utils.writeIntToByteArray(array, index+44, extraParentObjectId);
			Utils.writeBooleanAsIntToByteArray(array, index+48, extraIsShrinkHeader);
			Utils.writeBooleanAsIntToByteArray(array, index+52, extraShadows);
			
			Utils.writeIntToByteArray(array, index+56, extraObjectType);
			
			Utils.writeIntToByteArray(array, index+60, extraFileLength);
			Utils.writeIntToByteArray(array, index+64, extraEquivalentObjectId);
			
			Utils.writeIntToByteArray(array, index+68, validMarker1);
		}

		public void readTagsFromByteArray(byte[] array, int index)
		{
			validMarker0 = Utils.getIntFromByteArray(array, index+4);
			chunkUsed = Utils.getBooleanAsIntFromByteArray(array, index+4);
			objectId = Utils.getIntFromByteArray(array, index+8);
			chunkId = Utils.getIntFromByteArray(array, index+12);
			byteCount = Utils.getIntFromByteArray(array, index+16);
			
			eccResult = Utils.getIntFromByteArray(array, index+20);
			blockBad = Utils.getBooleanAsIntFromByteArray(array, index+24);
			
			chunkDeleted = Utils.getBooleanAsIntFromByteArray(array, index+28);
			serialNumber = Utils.getIntFromByteArray(array, index+32);
			
			sequenceNumber = Utils.getIntFromByteArray(array, index+36);
			
			extraHeaderInfoAvailable = Utils.getBooleanAsIntFromByteArray(array, index+40);
			extraParentObjectId = Utils.getIntFromByteArray(array, index+44);
			extraIsShrinkHeader = Utils.getBooleanAsIntFromByteArray(array, index+48);
			extraShadows = Utils.getBooleanAsIntFromByteArray(array, index+52);
			
			extraObjectType = Utils.getIntFromByteArray(array, index+56);
			
			extraFileLength = Utils.getIntFromByteArray(array, index+60);
			extraEquivalentObjectId = Utils.getIntFromByteArray(array, index+64);
			
			validMarker1 = Utils.getIntFromByteArray(array, index+68);
		}
}
