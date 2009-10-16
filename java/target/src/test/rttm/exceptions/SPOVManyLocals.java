package rttm.exceptions;

public class SPOVManyLocals {

	private static void recursive()
	{
		int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, 
		w, x, y, z;
		a = b = c = d = e = f = g = h = i = j = k = l = m = n = o = p = 
		q = r = s = t = u = v = w = x = y = z = 0;
		
		recursive();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		recursive();
	}

}
