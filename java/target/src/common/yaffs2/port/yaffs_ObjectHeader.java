package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_ObjectHeader extends SerializableObject
{
	/* -------------------------- Object structure -------------------------------*/
	/* This is the object structure as stored on NAND */

	public yaffs_ObjectHeader(byte[] array, int offset)
	{
		super(array, offset);
	}
	
	static final int SERIALIZED_LENGTH = 4+4+2+(Guts_H.YAFFS_MAX_NAME_LENGTH+1)+/*alignment*/2+
	+4+(5*4)+4+4+
	(Guts_H.YAFFS_MAX_ALIAS_LENGTH+1)+4+(10*4)+4+4; 

	/*yaffs_ObjectType*/ int type()
	{
		return Utils.getIntFromByteArray(serialized, offset+0);
	}
	void setType(/*yaffs_ObjectType*/ int value)
	{
		Utils.writeIntToByteArray(serialized, offset+0, value);
	}

	/* Apply to everything  */
	int parentObjectId()
	{
		return Utils.getIntFromByteArray(serialized, offset+4);
	}
	void setParentObjectId(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+4, value);
	}

//	short sum__NoLongerUsed()	/* checksum of name. No longer used */
//	{
//		throw new NotImplementedException();
//	}
	
	static final int SIZEOF_name = Guts_H.YAFFS_MAX_NAME_LENGTH + 1;
	byte[] name() // = new byte[YAFFS_MAX_NAME_LENGTH + 1];
	{
		return serialized;
	}
	int nameIndex()
	{
		return offset+10;
	}

	/* Thes following apply to directories, files, symlinks - not hard links */
	int yst_mode()		/* protection */
	{
		return Utils.getIntFromByteArray(serialized, offset+268);
	}
	void setYst_mode(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+268, value);
	}

	/*#ifdef CONFIG_YAFFS_WINCE
		__u32 notForWinCE[5];
	#else*/
	int yst_uid()
	{
		return Utils.getIntFromByteArray(serialized, offset+272);
	}
	void setYst_uid(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+272, value);
	}

	int yst_gid()
	{
		return Utils.getIntFromByteArray(serialized, offset+276);
	}
	void setYst_gid(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+276, value);
	}

	int yst_atime()
	{
		return Utils.getIntFromByteArray(serialized, offset+280);
	}
	void setYst_atime(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+280, value);
	}

	int yst_mtime()
	{
		return Utils.getIntFromByteArray(serialized, offset+284);
	}
	void setYst_mtime(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+284, value);
	}

	int yst_ctime()
	{
		return Utils.getIntFromByteArray(serialized, offset+288);
	}
	void setYst_ctime(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+288, value);
	}
	
	/*#endif*/

	/* File size  applies to files only */
	int fileSize()
	{
		return Utils.getIntFromByteArray(serialized, offset+292);
	}
	void setFileSize(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+292, value);
	}

	/* Equivalent object id applies to hard links only. */
	int equivalentObjectId()
	{
		return Utils.getIntFromByteArray(serialized, offset+296);
	}
	void setEquivalentObjectId(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+296, value);
	}

	/* Alias is for symlinks only. */
	byte[] alias() // = new byte[YAFFS_MAX_ALIAS_LENGTH + 1];
	{
		return serialized;
	}
	int aliasIndex()
	{
		return offset+300;
	}

	int yst_rdev()		/* device stuff for block and char devices (major/min) */
	{
		return Utils.getIntFromByteArray(serialized, offset+460);
	}
	void setYst_rdev(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+460, value);
	}


	/*#ifdef CONFIG_YAFFS_WINCE
		__u32 win_ctime[2];
		__u32 win_atime[2];
		__u32 win_mtime[2];
		__u32 roomToGrow[4];
	#else*/
	IntArrayPointer roomToGrow() // = new int[10];
	{
		return new IntArrayPointer(serialized, offset+464);
	}
	/*#endif*/

	// XXX != 0, not > 0 // ???
	int shadowsObject() 	/* This object header shadows the specified object if > 0 */
	{
		return Utils.getIntFromByteArray(serialized, offset+504);
	}
	void setShadowsObject(int value)
	{
		Utils.writeIntToByteArray(serialized, offset+504, value);
	}

	/* isShrink applies to object headers written when we shrink the file (ie resize) */
	/*__u32*/ boolean isShrink()
	{
		return Utils.getBooleanAsIntFromByteArray(serialized, offset+508);
	}
	void setIsShrink(boolean value)
	{
		Utils.writeBooleanAsIntToByteArray(serialized, offset+508, value);
	}
	
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
