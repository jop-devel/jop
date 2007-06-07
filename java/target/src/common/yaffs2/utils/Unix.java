package yaffs2.utils;

import java.util.Arrays;

import yaffs2.port.*;

import static yaffs2.utils.emulation.Utils.*; // XXX remove dependencies

/**
 * Note that these methods are neither semantically nor syntactically equivalent to their 
 * archetypes.
 * Rather, they should fit to the porting style.
 * 
 */
public class Unix
{
//	public static void memset(ArrayPointer s, byte c, int n)
//	{
//		Arrays.fill(s.array, s.index, s.index+n-1, c);
//		// XXX use this instead of the hand-written code?
////		for (int i = 0; i < n; i++)
////			s.set(i, c);
//	}
	
//	public static void memset(byte[] s, byte c, int n)
//	{
//		for (int i = 0; i < n; i++)
//			s[i] = c;
//	}
	
	public static void memset(byte[] s, int sIndex, byte c, int n)
	{
		for (int i = sIndex; i < sIndex + n; i++)
			s[i] = c;
	}
	
	public static void memset(yaffs_Tnode s)
	{
		for (int i = 0; i < s.internal.length; i++)
			s.internal[i] = null;
		
		memset((PartiallySerializableObject)s,(byte)0);
	}
	
	public static void memset(list_head s)
	{
		s.next = null;
		s.prev = null;
	}
	
	public static void memset(yaffsfs_Handle s)
	{
		s.inUse = false;
		s.readOnly = false;
		s.append = false;
		s.exclusive = false;
		s.position = 0;
		s.obj = null;
	}
	
	public static void memset(yaffsfs_DirectorySearchContext s)
	{
		s.magic = 0;
		memset(s.de);
		memset(s.name, 0, (byte)0, s.name.length);
		s.nameIndex = 0;
		s.dirObj = null;
		s.nextReturn = null;
		s.offset = 0;
		memset(s.others);
	}
	
	public static void memset(yaffs_dirent s)
	{
		s.d_ino = 0;
		s.d_off = 0;
//		s.d_reclen = 0;
		memset(s.d_name, 0, (byte)0, s.d_name.length);
		s.d_nameIndex = 0;
		s.d_dont_use = null;
	}
	
	public static void memset(yaffs_ExtendedTags s)
	{
		s.validMarker0 = 0;
		s.chunkUsed = false;
		s.objectId = 0;
		s.chunkId = 0;
		s.byteCount = 0;

		s.eccResult = 0;
		s.blockBad = false;

		s.chunkDeleted = false;
		s.serialNumber = 0;

		s.sequenceNumber = 0;

		s.extraHeaderInfoAvailable = false;
		s.extraParentObjectId = 0;
		s.extraIsShrinkHeader = false;
		s.extraShadows = false;

		s.extraObjectType = 0;

		s.extraFileLength = 0;
		s.extraEquivalentObjectId = 0;

		s.validMarker1 = 0;
	}
	
	public static void memset(yaffs_Object s)
	{
		s.deleted = false;		/* This should only apply to unlinked files. */
		s.softDeleted = false;	/* it has also been soft deleted */
		s.unlinked = false;	/* An unlinked file. The file should be in the unlinked directory.*/
		s.fake = false;		/* A fake object has no presence on NAND. */
		s.renameAllowed = false;	/* Some objects are not allowed to be renamed. */
		s.unlinkAllowed = false;
		s.dirty = false;		/* the object needs to be written to flash */
		s.valid = false;		/* When the file system is being loaded up, this 
					 * object might be created before the data
					 * is available (ie. file data records appear before the header).
					 */
		s.lazyLoaded = false;	/* This object has been lazy loaded and is missing some detail */

		s.deferedFree = false;	/* For Linux kernel. Object is removed from NAND, but is
					 * still in the inode cache. Free of object is defered.
					 * until the inode is released.
					 */

		s.serial = 0;		/* serial number of chunk in NAND. Cached here */
		s.sum = 0;		/* sum of the name to speed searching */

		s.myDev = null;	/* The device I'm on */

		memset(s.hashLink);
		memset(s.hardLinks);

		s.parent = null; 
		memset(s.siblings);

		s.chunkId = 0;		

		s.nDataChunks = 0;

		s.objectId = 0;

		s.yst_mode = 0;

	//#ifdef CONFIG_YAFFS_SHORT_NAMES_IN_RAM
		for (int i = 0; i < s.shortName.length; i++)
			s.shortName[i] = 0;
	//#endif

		s.inUse = 0;

		s.yst_uid = 0;
		s.yst_gid = 0;
		s.yst_atime = 0;
		s.yst_mtime = 0;
		s.yst_ctime = 0;

		s.yst_rdev = 0;

		s.variantType = 0;

		memset(s.variant);
	}
	
	public static void memset(yaffs_ObjectVariant s)
	{
		memset(s.fileVariant);
		memset(s.directoryVariant);
		memset(s.symLinkVariant);
		memset(s.hardLinkVariant);
	}
	
	public static void memset(yaffs_FileStructure s)
	{
		s.fileSize = 0;
		s.scannedFileSize = 0;
		s.shrinkSize = 0;
		s.topLevel = 0;
		s.top = null;
	}
	
	public static void memset(yaffs_DirectoryStructure s)
	{
		memset(s.children);
	}
	
	public static void memset(yaffs_SymLinkStructure s)
	{
		s.alias = null;
		s.aliasIndex = 0;
	}
	
	public static void memset(yaffs_HardLinkStructure s)
	{
		s.equivalentObject = null;
		s.equivalentObjectId = 0;
	}
	
//	public static void memset(yaffs_Device s)
//	{
//		throw new NotImplementedException();
//	}
	
//	public static void memset(byte[] s, byte c)
//	{
//		for (int i = 0; i < s.length; i++)
//			s[i] = c;
//	}
	
//	public static void memset(yaffs_Spare s, byte c)
//	{
//		s.tagByte0 = c;
//		s.tagByte1 = c;
//		s.tagByte2 = c;
//		s.tagByte3 = c;
//		s.pageStatus = c;	/* set to 0 to delete the chunk */
//		s.blockStatus = c;
//		s.tagByte4 = c;
//		s.tagByte5 = c;
//		memset(s.ecc1, c);
//		s.tagByte6 = c;
//		s.tagByte7 = c;
//		memset(s.ecc2, c);
//	}
	
	public static void memset(SerializableObject s, byte c)
	{
		// N.B. fill() toIndex parameter is exclusive
		Arrays.fill(s.serialized, s.offset, s.offset+s.getSerializedLength(), c);
	}
	
	public static void memset(SerializableObject[] s, byte c)
	{
		for (int i = 0; i < s.length; i++)
			memset(s[i], c);
	}
	
	public static void memcpy(yaffs_Tnode dest, yaffs_Tnode src)
	{
		for (int i = 0; i < dest.internal.length; i++)
			dest.internal[i] = src.internal[i];
		
		memcpy((PartiallySerializableObject)dest,(PartiallySerializableObject)src);
	}
	
//	/**
//	 * Copies dest.length bytes.
//	 * @param dest
//	 * @param src
//	 */
//	public static void memcpy(byte[] dest, byte[] src)
//	{
//		for (int i = 0; i < dest.length; i++)
//			dest[i] = src[i];
//	}
//	
//	public static void memcpy(byte[] dest, ArrayPointer src, int num)
//	{
//		for (int i = 0; i < num; i++)
//			dest[i] = src.get(i);
//	}
//	
//	public static void memcpy(ArrayPointer dest, ArrayPointer src, int num)
//	{
//		// XXX use this instead of the hand-written code?
//		System.arraycopy(src.array, src.index, dest.array, dest.index, num);
////		for (int i = 0; i < num; i++)
////			dest.set(i, src.get(i));
//	}
	
	public static void memcpy(byte[] dest, int destIndex, byte[] src, int srcIndex, int num)
	{
		System.arraycopy(src, srcIndex, dest, destIndex, num);
	}
			
	/**
	 * As long as a class only contains other SerializableObjects and Primitives,
	 * this works.
	 *
	 */
	public static void memcpy(SerializableObject dest, SerializableObject src)
	{
		assert dest.getClass().equals(src.getClass());
		
		System.arraycopy(src.serialized, src.offset, 
				dest.serialized, dest.offset, dest.getSerializedLength());
	}
	
//	public static void memcpy(yaffs_NANDSpare dest, yaffs_NANDSpare src) {
//		dest.eccres1 = src.eccres1;
//		dest.eccres2 = src.eccres2;
//		memcpy(dest.spare, src.spare);
//	}
	
	public static int memcmp(byte[] s1, int s1Index, byte[] s2, int s2Index, int n) {
		for (int i = 0; i < n; i++) {
			int compare;
			compare = s1[s1Index+i] - s2[s2Index+i];
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}
	
	public static int memcmp(SerializableObject s1, SerializableObject s2)
	{
		assert s1.getClass().equals(s2.getClass());
		assert s1.getSerializedLength() == s2.getSerializedLength();
		
		return memcmp(s1.serialized, s1.offset, s2.serialized, s2.offset, s1.getSerializedLength());
	}
	
	public static int strcmp(byte[] s1, int s1Index, byte[] s2, int s2Index)
	{
		// XXX could make it quicker
		int i = 0;
		while (s1[s1Index+i] == s2[s2Index+i] && s1[s1Index+i] != 0)
			i++;
		return s1[s1Index+i]-s2[s2Index+i];
	}
	
	// XXX speed functions up
	public static void strcpy(byte[] a, int aIndex, byte[] b, int bIndex)
	{
		int i = 0;
		do
		{
			a[aIndex+i] = b[bIndex+i];
		} while (b[bIndex+(i++)] != 0);
	}

	public static void strncpy(byte[] a, int aIndex, byte[] b, int bIndex, int c)
	{
		boolean nullEncountered = false;
		for (int i = 0; i < c; i++)
		{
			if (!nullEncountered)
			{
				nullEncountered = b[bIndex+i] == 0;
				a[aIndex+i] = b[bIndex+i];
			}
			else
			{
				a[aIndex+i] = 0;
			}				
		}
	}
	
	public static int strlen(byte[] s, int sIndex)
	{
		int i = 0;
		while (s[sIndex+i] != 0)
			++i;
		
		return i;
	}
	
	// PORT
	static byte[] _STATIC_LOCAL_printf_buffer = new byte[200];
	static final int _STATIC_LOCAL_printf_bufferIndex = 0;
		
	// XXX only for emulation
	static int debugLines = 0; 
	
	public static void printf(String format, Object... args)
	{
		sprintf(_STATIC_LOCAL_printf_buffer, _STATIC_LOCAL_printf_bufferIndex,
				format, args);
		
		String buffer = yaffs2.utils.emulation.Utils.byteArrayToString(
				_STATIC_LOCAL_printf_buffer, _STATIC_LOCAL_printf_bufferIndex);
				
		System.out.print(buffer);
		
		for (int i = 0; i < buffer.length(); i++)
		{
			if (buffer.charAt(i) == '\n')
				debugLines++;
		}
		int foo = 0; // FIXME use this statement to break when trace line #debugLines is written 
	}
	
	// XXX only for emulation

	/**
	 * @return Upon successful completion, the sprintf() function shall return the number of bytes written to s, excluding the terminating null byte. 
	 */
	// XXX overflow still possible in format parsing code
	public static int sprintf(byte[] s, int sIndex, String format, Object... args)
	{
		int formatIndex = 0;
		int argsIndex = 0;
		final int formatLength = format.length();
		final int sLength = s.length; 
		
		int sOffset = sIndex;
		
		char c;
		
		while (formatIndex < formatLength && sOffset < sLength)
		{
			if ((c = format.charAt(formatIndex)) == '%')
			{
				char formatChar = format.charAt(++formatIndex);
				boolean padWithZeroes = false;	// XXX
				int width = 0;  // XXX
				
				if (formatChar == '0')	// pad with leading zeros
				{
					padWithZeroes = true;
					formatChar = format.charAt(++formatIndex);
				}
				
				// PORT bad bad HACK
				if (formatChar >= '1' && formatChar <= '9')		
				{
					width = formatChar - '0';
					formatChar = format.charAt(++formatIndex);
				}
				
				// XXX make sure of no overflows && sOffset reaches sLength
				switch (formatChar)
				{
//					case 'c':	// single byte
//						sOffset = StringToByteArraySafe(
//								((Byte)args[argsIndex]).toString(), s, sOffset);
//						break;
					case 'd':	// integer
					case 'i':
						sOffset = StringToByteArraySafe(
								((Integer)args[argsIndex]).toString(), s, sOffset);
						break;
					case 'l':	// long
						sOffset = StringToByteArraySafe(
								((Long)args[argsIndex]).toString(), s, sOffset);
						break;
					case 'x':	// hex
					case 'X':
						sOffset = StringToByteArraySafe(
								Integer.toHexString((Integer)args[argsIndex]),
								s, sOffset); 
						break;
					case 'b':	// boolean
						s[sOffset++] = (Boolean)args[argsIndex] ? (byte)'1' : (byte)'0'; 
						break;
					case 's':	// char string
						sOffset = StringToByteArraySafe((String)args[argsIndex], 
								s, sOffset);
						break;
					case 'a':	// PORT byte[] array, int offset
						byte[] array = (byte [])args[argsIndex];
						int offset = (Integer)args[argsIndex+1];
						int len = strlen(array, offset);
						// trim to avoid buffer overflow
						len = Math.min(len, sLength-sOffset);
								
						System.arraycopy(array, offset, s, sOffset, len);
						sOffset += len;
						
						argsIndex++; // 2 args consumed
						break;
					case '%':
						s[sOffset++] = (byte)'%';
						argsIndex--; // no arg consumed
						break;
					default:
						assert false;
				}
				
				argsIndex++;
			}
			else 
				s[sOffset++] = (byte)c;
			
			formatIndex++;
		}
		
		assert sOffset < sLength-1;
		
		if (!(sOffset <= sLength-1))
			sOffset = sLength-1;
		
		s[sOffset] = 0;
		
		return sOffset-sIndex;
	}
	
	public static final int _IFMT = 0170000;	/* type of file */
	public static final int _IFCHR	= 0020000;	/* character special */
	public static final int _IFBLK	= 0060000;	/* block special */
	public static final int _IFSOCK = 0140000;	/* socket */
	public static final int _IFIFO	= 0010000;	/* fifo */
	
	public static boolean S_ISCHR(int m)
	{
		return (((m)&_IFMT) == _IFCHR);
	}
	
	public static boolean S_ISBLK(int m)
	{
		return (((m)&_IFMT) == _IFBLK);
	}
	
	public static boolean S_ISFIFO(int m)
	{
		return (((m)&_IFMT) == _IFIFO);
	}
	
	public static boolean S_ISSOCK(int m)
	{
		return (((m)&_IFMT) == _IFSOCK);
	}
	
	public static final int S_IFDIR = 0040000;	
	
}
