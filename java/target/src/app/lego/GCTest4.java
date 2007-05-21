package lego;


/**
 * Does not crash.
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class GCTest4
{
	public static final int INTERVAL = 200;
	
	public static void main(String[] args)
	{
		StringBuffer s = new StringBuffer(200);
		while (true)
		{
			s.setLength(0);
			s.append("fgdfgdffgasdflösadlfösdf");
			s.append("sdfsdfgffsadfsalfdadsflpdf");
			//s.append(100);
			System.out.print(s.toString() + 100);
		}
	}
}
