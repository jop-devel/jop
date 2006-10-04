package jvm;
import java.lang.*;

public class SystemCopy extends TestCase
{
	public String getName () 
	{
		return "SystemCopy";
	}
	public boolean test ()
	{
		boolean bOk = true;
		bOk = bOk && testCopy ();
		return bOk;
	}
	private boolean compare (Object[] a,Object[] b)
	{
		for (int i=0;i<a.length&&i<b.length;++i)
			if ( a[i] != b[i] )
				return false;
		return true;
	}
	private boolean compare (int[] a,int[] b)
	{
		for (int i=0;i<a.length&&i<b.length;++i)
			if ( a[i] != b[i] )
				return false;
		return true;
	}
	private boolean testCopy ()
	{
		Object[] oSrc1;
		Object[] oSrc = { "ABC" };
		Object[] oDest = { null };
		oSrc = new Object[]  { "ABC", "BDE" };
		oSrc1 = new Object[] { oSrc[0], oSrc[1] };
		oDest = new Object[] { null, null };
		System.arraycopy (oSrc,0,oDest,0,2);
		if ( !compare (oSrc,oDest) )
			return false;
		if ( !compare (oSrc,oSrc1) )
			return false;
		
		int[] nSrc =  { 1,2,3,4,5 };
		int[] nDest = { 1,1,2,4,5 };
		System.arraycopy (nSrc,0,nSrc,1,2);
		if ( !compare (nSrc,nDest) )
			return false;
		
		nSrc = new int []  { 1,2,3,4,5 };
		nDest = new int [] { 1,2,4,5,5 };
		System.arraycopy (nSrc,3,nSrc,2,2);
		if ( !compare (nSrc,nDest) )
			return false;
		
		String s = "aa";
		Object o = null;
		foo(s, o);
		return true;
	}
	
	void foo(Object a, Object b) {
		a = b;
	}
	
}
