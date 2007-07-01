package yaffs2.port;

import yaffs2.utils.*;

/**
 *
 * XXX Not really a union, but i found no better way.
 * XXX PORT Works only when dev.tnodeWidth = 16.  
 */
public class yaffs_Tnode extends PartiallySerializableObject
{
	/*--------------------------- Tnode -------------------------- */

	//union yaffs_Tnode_union {
	/*#ifdef CONFIG_YAFFS_TNODE_LIST_DEBUG
		union yaffs_Tnode_union *internal[YAFFS_NTNODES_INTERNAL + 1];
	#else*/
	public yaffs_Tnode[] internal = new yaffs_Tnode[Guts_H.YAFFS_NTNODES_INTERNAL];
	/*#endif*/
	/*	__u16 level0[YAFFS_NTNODES_LEVEL0]; */

	static final int SERIALIZED_LENGTH = Guts_H.YAFFS_NTNODES_LEVEL0*2; 
	// XXX PORT should be (dev.tnodeWidth * YAFFS_NTNODES_LEVEL0)/8


	int level0AsInt(int index)
	{
		return Utils.getIntFromByteArray(serialized, index*4);
	}
	void setLevel0AsInt(int index, int value)
	{
		Utils.writeIntToByteArray(serialized, index*4, value);
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
	
	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
