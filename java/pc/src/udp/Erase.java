package udp;
/**
*	Flash ean ia TFTP.
*
*	For new jopcore board with 512 KB flash.
*
*/

public class Erase {

	private static final int FLASH_SIZE = 0x80000;
	private static final int SECTOR_SIZE = 0x10000;
	private static final int SECTOR_MASK = 0xf0000;
	private static final int SECTOR_SHIFT = 16;

	private int start;

	private byte[] mem;
	private int len;
	private Tftp tftp;

	public Erase(Tftp tftp) {

		this.tftp = tftp;
		mem = new byte[FLASH_SIZE];
		for (int i=0; i<FLASH_SIZE; ++i) mem[i] = (byte) 0xff;
		len = FLASH_SIZE;
		start = 0;
	}

	public void program() {

		int i, j;

		byte[] buf = new byte[SECTOR_SIZE];
		byte[] inbuf = new byte[SECTOR_SIZE];


		for (i=0; i<len; i+=SECTOR_SIZE) {

			int slen = SECTOR_SIZE;
			if (len-i<slen) slen = len-i;

			System.out.println("programming");
			for (j=0; j<slen ; ++j) {
				buf[j] = mem[i+j];
			}
			byte s = (byte) ('0'+((i+start)>>SECTOR_SHIFT));

			try {
				if (!tftp.write((byte) 'f', (byte) s, buf, slen)) {
					System.out.println();
					System.out.println("programming error");
					System.exit(-1);
				}
				System.out.println();
				System.out.println("compare");
				if (tftp.read((byte) 'f', s, inbuf)!=SECTOR_SIZE) {
					System.out.println();
					System.out.println("read error");
					System.exit(-1);
				}
			} catch (Exception e) {
				System.out.println(e);
				System.exit(-1);
			}

			for (j=0; j<slen; ++j) {
				if (buf[j] != inbuf[j]) {
					System.out.println("wrong data: "+(i+j)+" "+buf[j]+" "+inbuf[j]);
					System.exit(-1);
				}
			}
		}
		System.out.println("programming ok");
	}

	public static void main (String[] args) {

		if (args.length!=1) {
			System.out.println("usage: Erase host");
			System.exit(-1);
		}
		Erase fl = new Erase(new Tftp(args[0]));
		fl.program();
	}

}
