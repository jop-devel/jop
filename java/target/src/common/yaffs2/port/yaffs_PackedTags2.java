package yaffs2.port;

import yaffs2.utils.*;

public class yaffs_PackedTags2 extends SerializableObject 
{
	public yaffs_PackedTags2()
	{
		super(SERIALIZED_LENGTH);
	}

//	typedef struct {
	yaffs_PackedTags2TagsPart t = new yaffs_PackedTags2TagsPart(this, 0);
	yaffs_ECCOther ecc = new yaffs_ECCOther(this, yaffs_PackedTags2TagsPart.SERIALIZED_LENGTH);


	public static final int SERIALIZED_LENGTH = yaffs_PackedTags2TagsPart.SERIALIZED_LENGTH + yaffs_ECCOther.SERIALIZED_LENGTH; // no alignment needed

	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}

//	} yaffs_PackedTags2;
}
