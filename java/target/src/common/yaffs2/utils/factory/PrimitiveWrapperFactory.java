package yaffs2.utils.factory;

public class PrimitiveWrapperFactory extends PooledObjectFactory
{
	protected PrimitiveWrapperFactory()
	{
	}
	
	protected static PrimitiveWrapperFactory instance = new PrimitiveWrapperFactory(); 
	
	// XXX refactor -> search and replace
	public static PrimitiveWrapper get(int primitive)
	{
		PrimitiveWrapper result = (PrimitiveWrapper)instance.get();
		result._int = primitive;
		return result;
	}
	
	public static PrimitiveWrapper get(String primitive)
	{
		PrimitiveWrapper result = (PrimitiveWrapper)instance.get();
		result._String = primitive;
		return result;
	}
	
	public static PrimitiveWrapper get(boolean primitive)
	{
		PrimitiveWrapper result = (PrimitiveWrapper)instance.get();
		result._boolean = primitive;
		return result;
	}
	
	public static PrimitiveWrapper get(byte[] array)
	{
		PrimitiveWrapper result = (PrimitiveWrapper)instance.get();
		result._byteArray = array;
		return result;
	}	
	
	
	protected PooledObject createInstance()
	{
		return new PrimitiveWrapper();
	}

}
