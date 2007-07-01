package yaffs2.utils.factory;

import yaffs2.port.yaffs_Object;

public class yaffs_ObjectFactory extends
		PooledObjectFactory
{
	protected PooledObject createInstance()
	{
		return new yaffs_Object();
	}
}
