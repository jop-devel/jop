package yaffs2.port;

import static yaffs2.utils.Utils.getBooleanAsIntFromByteArray;
import static yaffs2.utils.Utils.getIntFromByteArray;
import static yaffs2.utils.Utils.writeBooleanAsIntToByteArray;
import static yaffs2.utils.Utils.writeIntToByteArray;

// XXX make it a SerializableObject
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
		/**unsigned*/ public int sequenceNumber;	/* The sequence number of this block */	// XXX changed long=>int

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
			writeIntToByteArray(array, index+4, validMarker0);
			writeBooleanAsIntToByteArray(array, index+4, chunkUsed);
			writeIntToByteArray(array, index+8, objectId);
			writeIntToByteArray(array, index+12, chunkId);
			writeIntToByteArray(array, index+16, byteCount);
			
			writeIntToByteArray(array, index+20, eccResult);
			writeBooleanAsIntToByteArray(array, index+24, blockBad);
			
			writeBooleanAsIntToByteArray(array, index+28, chunkDeleted);
			writeIntToByteArray(array, index+32, serialNumber);
			
			writeIntToByteArray(array, index+36, sequenceNumber);
			
			writeBooleanAsIntToByteArray(array, index+40, extraHeaderInfoAvailable);
			writeIntToByteArray(array, index+44, extraParentObjectId);
			writeBooleanAsIntToByteArray(array, index+48, extraIsShrinkHeader);
			writeBooleanAsIntToByteArray(array, index+52, extraShadows);
			
			writeIntToByteArray(array, index+56, extraObjectType);
			
			writeIntToByteArray(array, index+60, extraFileLength);
			writeIntToByteArray(array, index+64, extraEquivalentObjectId);
			
			writeIntToByteArray(array, index+68, validMarker1);
		}

		public void readTagsFromByteArray(byte[] array, int index)
		{
			validMarker0 = getIntFromByteArray(array, index+4);
			chunkUsed = getBooleanAsIntFromByteArray(array, index+4);
			objectId = getIntFromByteArray(array, index+8);
			chunkId = getIntFromByteArray(array, index+12);
			byteCount = getIntFromByteArray(array, index+16);
			
			eccResult = getIntFromByteArray(array, index+20);
			blockBad = getBooleanAsIntFromByteArray(array, index+24);
			
			chunkDeleted = getBooleanAsIntFromByteArray(array, index+28);
			serialNumber = getIntFromByteArray(array, index+32);
			
			sequenceNumber = getIntFromByteArray(array, index+36);
			
			extraHeaderInfoAvailable = getBooleanAsIntFromByteArray(array, index+40);
			extraParentObjectId = getIntFromByteArray(array, index+44);
			extraIsShrinkHeader = getBooleanAsIntFromByteArray(array, index+48);
			extraShadows = getBooleanAsIntFromByteArray(array, index+52);
			
			extraObjectType = getIntFromByteArray(array, index+56);
			
			extraFileLength = getIntFromByteArray(array, index+60);
			extraEquivalentObjectId = getIntFromByteArray(array, index+64);
			
			validMarker1 = getIntFromByteArray(array, index+68);
		}
}
