
public class Crc {

	static int check(int val) {

		int reg = -1;
		int poly = 0x07;

		for (int i=0; i<32; ++i) {
			reg <<= 1;
			if (val<0) reg |= 1;
			val <<=1;
			if ((reg & 0x100) != 0) reg ^= 0x07;
		}
		reg &= 0xff;

		return reg;
	}

	public static void main(String[] args) {

		int msg = 0x12345600;

		msg |= check(msg);

		System.out.println(Integer.toHexString(check(0)));
		System.out.println(Integer.toHexString(check(0x100)));
		System.out.println(Integer.toHexString(check(0x200)));
		System.out.println(Integer.toHexString(check(0x300)));
		System.out.println(Integer.toHexString(check(0xffffffff)));
		System.out.println(Integer.toHexString(msg));

		System.out.println(Integer.toHexString(check(msg)));
		System.out.println(Integer.toHexString(check(msg+15)));
		System.out.println(Integer.toHexString(check(msg+8)));

/*
		int cnt = 0;
		for (int i=1; i!=0; ++i) {
			int j = check(msg ^ i);
			if (j==0) {
				++cnt;
if ((cnt&0xff) == 0)
				System.out.println(i+" "+cnt);
			}
		}

		System.out.println("count: "+cnt);
*/

	}
}
