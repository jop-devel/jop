package yaffs2.platform.emulation;

import static yaffs2.port.ydirectenv.TENDSTR;
import static yaffs2.port.ydirectenv.TSTR;
import static yaffs2.port.yportenv.*;
import static yaffs2.utils.Utils.__FILE__;
import static yaffs2.utils.Utils.__LINE__;

public class PortConfiguration implements yaffs2.utils.PortConfiguration
{

	public void YBUG()
	{
		////T(YAFFS_TRACE_BUG,(TSTR("==>> yaffs bug: " __FILE__ " %d" TENDSTR),__LINE__))
		T(YAFFS_TRACE_BUG,TSTR("==>> yaffs bug: " + __FILE__() + " %d" + TENDSTR),__LINE__());
	}

}
