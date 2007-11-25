package yaffs2.utils;

import yaffs2.port.*;
import yaffs2.utils.factory.PrimitiveWrapper;

/**
 * Note that these methods are neither semantically nor syntactically equivalent to their 
 * archetypes.
 * Rather, they should fit to the porting style.
 * 
 */
public class Unix
{

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
		s.sub.deleted = false;		/* This should only apply to unlinked files. */
		s.sub.softDeleted = false;	/* it has also been soft deleted */
		s.sub.unlinked = false;	/* An unlinked file. The file should be in the unlinked directory.*/
		s.sub.fake = false;		/* A fake object has no presence on NAND. */
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

	public static void memset(SerializableObject s, byte c)
	{
		memset(s.serialized, s.offset, c, s.getSerializedLength());
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
//		assert dest.getClass().equals(src.getClass());

		System.arraycopy(src.serialized, src.offset, 
				dest.serialized, dest.offset, dest.getSerializedLength());
	}

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
//		assert s1.getClass().equals(s2.getClass());
//		assert s1.getSerializedLength() == s2.getSerializedLength();

		return memcmp(s1.serialized, s1.offset, s2.serialized, s2.offset, s1.getSerializedLength());
	}

	public static int strcmp(byte[] s1, int s1Index, byte[] s2, int s2Index)
	{
		int i = 0;
		while (s1[s1Index+i] == s2[s2Index+i] && s1[s1Index+i] != 0)
			i++;
		return s1[s1Index+i]-s2[s2Index+i];
	}

	public static int strncmp(byte[] s1, int s1Index, byte[] s2, int s2Index, int n)
	{
		int i = 0;
		while (s1[s1Index+i] == s2[s2Index+i] && s1[s1Index+i] != 0)
		{
			if (i == n-1)
				return 0;
			i++;
		}
		return s1[s1Index+i]-s2[s2Index+i];
	}

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
		// this is not a bug imo, this should only be called on zero-terminated strings
		while (s[sIndex+i] != 0)
			++i;

		return i;
	}

	public static PrimitiveWrapper[] xprintfArgs = new PrimitiveWrapper[9];	
	
	// PORT
	static byte[] _STATIC_LOCAL_printf_buffer = new byte[200];
	static final int _STATIC_LOCAL_printf_bufferIndex = 0;

	// XXX only for emulation
	static int debugLines = 0; 

	public static void printf(String format, PrimitiveWrapper arg0)
	{
		xprintfArgs[0] = arg0;
//		xprintfArgs[1] = arg1;
////		xprintfArgs[2] = arg2;
//////		xprintfArgs[3] = arg3;
////////		xprintfArgs[4] = arg4;
//////////		xprintfArgs[5] = arg5;
		
		printf(format);
	}

	
	public static void printf(String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
//		xprintfArgs[2] = arg2;
////		xprintfArgs[3] = arg3;
//////		xprintfArgs[4] = arg4;
////////		xprintfArgs[5] = arg5;
		
		printf(format);
	}

	public static void printf(String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
//		xprintfArgs[3] = arg3;
////		xprintfArgs[4] = arg4;
//////		xprintfArgs[5] = arg5;
		
		printf(format);
	}
	
	public static void printf(String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
		xprintfArgs[3] = arg3;
//		xprintfArgs[4] = arg4;
////		xprintfArgs[5] = arg5;
		
		printf(format);
	}
	
	public static void printf(String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3, PrimitiveWrapper arg4)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
		xprintfArgs[3] = arg3;
		xprintfArgs[4] = arg4;
//		xprintfArgs[5] = arg5;
		
		printf(format);
	}

	
	public static void printf(String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3, PrimitiveWrapper arg4, PrimitiveWrapper arg5)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
		xprintfArgs[3] = arg3;
		xprintfArgs[4] = arg4;
		xprintfArgs[5] = arg5;
		
		printf(format);
	}


	public static void sprintf(byte[] s, int sIndex, String format, PrimitiveWrapper arg0)
	{
		xprintfArgs[0] = arg0;
		
		sprintf(s, sIndex, format);
	}
	
	public static void sprintf(byte[] s, int sIndex, String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		
		sprintf(s, sIndex, format);
	}
	
	public static void sprintf(byte[] s, int sIndex, String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
		
		sprintf(s, sIndex, format);
	}

	public static void sprintf(byte[] s, int sIndex, String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
		xprintfArgs[3] = arg3;
		
		sprintf(s, sIndex, format);
	}
	
	public static void sprintf(byte[] s, int sIndex, String format, PrimitiveWrapper arg0, PrimitiveWrapper arg1, PrimitiveWrapper arg2, PrimitiveWrapper arg3, PrimitiveWrapper arg4)
	{
		xprintfArgs[0] = arg0;
		xprintfArgs[1] = arg1;
		xprintfArgs[2] = arg2;
		xprintfArgs[3] = arg3;
		xprintfArgs[4] = arg4;
		
		sprintf(s, sIndex, format);
	}

	
	/**
	 * Args must be written to printfBuffer.
	 * @param format
	 * @param args
	 */
	public static void printf(String format)
	{
		int len = sprintf(_STATIC_LOCAL_printf_buffer, _STATIC_LOCAL_printf_bufferIndex, format);
		
		try
		{
		Globals.logStream.write(_STATIC_LOCAL_printf_buffer,
				_STATIC_LOCAL_printf_bufferIndex, len); 
		}
		catch(Exception e)
		{
			throw new UnexpectedException();
		}
		
//		String buffer = yaffs2.utils.emulation.Utils.byteArrayToString(
//		_STATIC_LOCAL_printf_buffer, _STATIC_LOCAL_printf_bufferIndex);

//		Globals.logStream.print(buffer);

		for (int i = 0; i < len; i++)
		{
			if (_STATIC_LOCAL_printf_buffer[_STATIC_LOCAL_printf_bufferIndex+i] == '\n')
				debugLines++;
		}
		int foo = 0; // use this statement to break when trace line #debugLines is written 
	}

	// XXX only for emulation

	/**
	 * @return Upon successful completion, the sprintf() function shall return the number of bytes written to s, excluding the terminating null byte. 
	 */
	// XXX overflow still possible in format parsing code
	
	/**
	 * Args must be written to printfBuffer.
	 */
	public static int sprintf(byte[] s, int sIndex, String format)
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
				boolean padWithZeroes = false;
				int width = 0;

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
					case 'd':	// integer
					case 'i':
					{
						int val = xprintfArgs[argsIndex]._int;

						if (val < 0)
						{
							s[sOffset++] = '-';
						}
						
						int buf;

						buf = Math.abs(val);
						int digits = 1;
						while (buf >= 10)
						{
							digits++;
							buf /= 10;
						}

						for (int i= 0; i < width-digits; i++)	
						{
							s[sOffset++] = padWithZeroes ? (byte)'0' : (byte)' ';
						}

						buf = Math.abs(val);
						for (int i = 0; i < digits; i++)
						{							
							s[sOffset + (digits-1-i)] = (byte)('0' + (buf % 10));
							buf /= 10;
						}
						sOffset += digits;

						break;
					}
					case 'x':	// hex // XXX
					case 'X':
					{
						int val = xprintfArgs[argsIndex]._int;

						int buf;


						buf = val;
						int digits = 1;
						while (Math.abs(buf) >= 16)
						{
							digits++;
							buf /= 16;
						}

						for (int i= 0; i < width-digits; i++)	
						{
							s[sOffset++] = padWithZeroes ? (byte)'0' : (byte)' ';
						}

						buf = val;
						for (int i = 0; i < digits; i++)
						{							
							int charValue = buf % 16;
							s[sOffset + (digits-1-i)] = (byte)((charValue >= 10 ? 'a'-10 : '0') + charValue);
							buf /= 16;
						}
						sOffset += digits;

						break;
					}
					case 'b':	// boolean
					{
						s[sOffset++] = xprintfArgs[argsIndex]._boolean ? (byte)'1' : (byte)'0'; 
						break;
					}
					case 's':	// char string
					{
						String str = xprintfArgs[argsIndex]._String;

						int len = str.length();
						for (int i = 0; i < len; i++)
							s[sOffset++] = (byte)str.charAt(i);
						break;
					}
					case 'a':	// PORT byte[] array, int offset
					{
						byte[] array = xprintfArgs[argsIndex]._byteArray;
						int offset = xprintfArgs[argsIndex+1]._int;
						int len = strlen(array, offset);
						// trim to avoid buffer overflow
						len = Math.min(len, sLength-sOffset);

						System.arraycopy(array, offset, s, sOffset, len);
						sOffset += len;

						argsIndex++; // 2 args consumed
						break;
					}
					case '%':
					{
						s[sOffset++] = (byte)'%';
						argsIndex--; // no arg consumed
						break;
					}
					default:
//						assert false;
				}

				argsIndex++;
			}
			else 
				s[sOffset++] = (byte)c;

			formatIndex++;
		}

//		assert sOffset < sLength-1;

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
