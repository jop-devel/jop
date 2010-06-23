package wcet.dsvmfp.model.smo.kernel;
/**
 * @author ms & rup
 */
// Fixed Point math access in JOP
public class KVH {

	// All decimal numbers are FP
	private static int[][] data;// data

	private static int m;    // number of rows (observations)
	private static int n;    // number of columns (dimensions)
	private static int i;   // row index counter
	private static int j;   // column index counter

	private static int r;   // temp FP
	private static int s;   // temp FP

	public static int kernelDot(int i1, int i2) {
		//return Native.kernelDot(int i1, int i2);
		return -1;
	}

	public static int kernelDotArray(int i1) {
		//return Native.kernelDotArray(int i1);
		return -1;
	}

	public static void setData(int[][] data) {
		KVH.data = data;
	}

	public static void setM(int m) {
		KVH.m = m;
	}

	public static void setN(int n) {
		KVH.n = n;
	}
}