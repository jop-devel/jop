package yaffs2.platform.jop;

import yaffs2.port.*;
import yaffs2.utils.*;
import yaffs2.utils.factory.PrimitiveWrapperFactory;

import java.io.PrintStream;

public class PortConfiguration implements yaffs2.utils.PortConfiguration
{
	public PrintStream logStream()
	{
		return System.out;
	}

	public void YBUG()
	{
		////T(YAFFS_TRACE_BUG,(("==>> yaffs bug: " __FILE__ " %d" TENDSTR),__LINE__))
		yportenv.T(yportenv.YAFFS_TRACE_BUG,("==>> yaffs bug: %s %d" + ydirectenv.TENDSTR),PrimitiveWrapperFactory.get(Utils.__FILE__()), PrimitiveWrapperFactory.get(Utils.__LINE__()));
	}

}
