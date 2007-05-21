package lego;

/**
 * Does not crash.
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class GCTest
{
	public static void main(String[] args)
	{
		while (true)
		{
			String x = "20934utgiouhsidgui349pther8hgdfuiovhxfugd8gf934tz8ehf9adsuhudahg97q34t9";
			String y = "wz45ugji9eidfjsghuoi34htuerohgiufadbgzfdabzgegfdpjgsdfoigjeiojgiouojioejf"; 
			StringBuffer z = new StringBuffer(x);
			z.append(y); // System.out.println(x + y);
		}
	}
}
