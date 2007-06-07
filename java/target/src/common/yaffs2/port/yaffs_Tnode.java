package yaffs2.port;

import static yaffs2.port.Guts_H.*;
import static yaffs2.utils.Utils.*;
import yaffs2.utils.*;

/**
 *
 * Not really a union, but when the space is allocated, it is XXX not possible(?) 
 * to determine the type. Well, maybe it is possible.
 * XXX PORT Works only when dev.tnodeWidth = 16.  
 */
public class yaffs_Tnode extends PartiallySerializableObject
{
	/*--------------------------- Tnode -------------------------- */

	//union yaffs_Tnode_union {
	/*#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
		union yaffs_Tnode_union *internal[YAFFS_NTNODES_INTERNAL + 1];
	#else*/
	public yaffs_Tnode[] internal = new yaffs_Tnode[YAFFS_NTNODES_INTERNAL];
	/*#endif*/
	/*	__u16 level0[YAFFS_NTNODES_LEVEL0]; */

	static final int SERIALIZED_LENGTH = YAFFS_NTNODES_LEVEL0*2; 
	// XXX PORT should be (dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8


	int level0AsInt(int index)
	{
		return getIntFromByteArray(serialized, index*4);
	}
	void setLevel0AsInt(int index, int value)
	{
		writeIntToByteArray(serialized, index*4, value);
	}
	void andLevel0AsInt(int index, int value)
	{
		setLevel0AsInt(index, level0AsInt(index) & value);
	}
	void orLevel0AsInt(int index, int value)
	{
		setLevel0AsInt(index, level0AsInt(index) | value);
	}

	//};

	//typedef union yaffs_Tnode_union yaffs_Tnode;

	public yaffs_Tnode()
	{
		super(SERIALIZED_LENGTH);
	}
	
	@Override
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
