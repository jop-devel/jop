package yaffs2.port;

import static yaffs2.utils.Utils.*;
import static yaffs2.utils.Constants.*;
import static yaffs2.assertions.Assert.*;

import static yaffs2.port.yaffs_ObjectHeader.*;
import static yaffs2.port.Guts_H.*;
import static yaffs2.port.yaffs_BlockInfo.*;
import static yaffs2.port.yaffs_Spare.*;
import static yaffs2.port.yaffs_ExtendedTags.*;
import static yaffs2.port.yaffs_Tags.*;
import static yaffs2.port.yaffs_ChunkCache.*;
import static yaffs2.port.CFG_H.*;
import static yaffs2.port.yaffsfs_DeviceConfiguration.*;
import static yaffs2.port.ECC_C.*;
import static yaffs2.port.yaffs_ECCOther.*;
import static yaffs2.port.ECC_H.*;

public class yaffs_Tnode_internal extends yaffs_Tnode
{
	yaffs_Tnode[] internal = new yaffs_Tnode[YAFFS_NTNODES_INTERNAL];
}
