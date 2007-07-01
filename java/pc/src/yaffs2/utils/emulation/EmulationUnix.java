package yaffs2.utils.emulation;

import yaffs2.utils.UnexpectedException;
import yaffs2.utils.Unix;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

public class EmulationUnix
{
	public static void printf(String format, Object[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			Object o = args[i];
			
			if (o instanceof Integer)
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get(((Integer)args[i]).intValue());
			else if (o instanceof String)
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get(((String)args[i]));
			else if (o instanceof Boolean)
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get(((Boolean)args[i]).booleanValue());
			else if (o instanceof byte[])
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get((byte[])args[i]);
			else 
				throw new UnexpectedException();
		}
		
		Unix.printf(format);
	}
	
	public static void sprintf(byte[] s, int sIndex, String format, Object[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			Object o = args[i];
			
			if (o instanceof Integer)
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get(((Integer)args[i]).intValue());
			else if (o instanceof String)
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get(((String)args[i]));
			else if (o instanceof Boolean)
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get(((Boolean)args[i]).booleanValue());
			else if (o instanceof byte[])
				Unix.xprintfArgs[i] = PrimitiveWrapperFactory.get((byte[])args[i]);
			else 
				throw new UnexpectedException();
		}
		
		Unix.sprintf(s, sIndex, format);
	}
	
	
}
