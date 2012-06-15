package csp.test;

import csp.CRC32;

public class crc_test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CRC32 crc32 = new CRC32();

		System.out.println(crc32.getValue());

		int[] data = new int[5];

		data[0] = 0xff;
		data[1] = 255;
		data[2] = 255;
		data[3] = 255;
		data[4] = 255;

		//crc32.update(0x80);
		crc32.update(data,0,data.length);

		System.out.println(crc32.getValue());
	}

}
