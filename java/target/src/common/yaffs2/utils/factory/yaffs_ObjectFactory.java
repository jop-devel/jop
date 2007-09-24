package yaffs2.utils.factory;

//import yaffs2.port.yaffs_Object;
import yaffs2.utils.NotImplementedException;

public class yaffs_ObjectFactory extends
		PooledObjectFactory
{
	protected PooledObject createInstance()
	{
		// TODO pooling not yet implemented
		throw new NotImplementedException();
//		return new yaffs_Object();
	}
}
