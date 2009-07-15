package wcet.dsvmfp.util;
/**
 * 
 * @author rup.inf
 * GPL
 */
public class DsvmUtilFP {
  /**
   * Coyy some integers from one array to another.
   * @param src the source array
   * @param srcpos start in src array
   * @param dest the destination array
   * @param destpos the destination position
   * @param length the number of integers to copy
   */
	public static void arrayCopy(int[] src, int srcpos, int[] dest,
			int destpos, int length) {
		for (int i = 0; i < length; i++)
			dest[destpos + i] = src[srcpos + i];
	}
	
	public static void pb(int b) {
		int mask = 0x01;
		for (int i = 31; i >= 0; i--) {
			int res = (b >>> i) & mask;
			if ((i + 1) % 8 == 0 && i < 31)
				System.out.print("_");
			System.out.print(res);
		}
		System.out.println("");
	}

	public static byte[] intToByte(int[] intArray) {
		byte[] byteArray = new byte[intArray.length * 4];
		for (int j = 0; j < intArray.length; j++) {
			for (int i = 0, shift = 24; i < 4; i++, shift -= 8)
				byteArray[4*j+i] = (byte) (0xFF & (intArray[j] >>> shift));
		}
		return byteArray;
	}

	public static int[] byteToInt(byte[] byteArray, int byteLength, int byteOffset) {
		int[] intArray = new int[byteLength/4];
		for (int j = 0; j < intArray.length; j++) {
			for (int i = 0, shift = 24; i < 4; i++, shift -= 8)
				intArray[j] += (0xFF << shift) & (byteArray[j*4+byteOffset*4+i] << shift);
		}
		return intArray;
	}

}
