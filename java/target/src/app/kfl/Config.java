package kfl;

/**
*	Mast Konfigurationsdaten.
*
*/

public class Config {

//
//	offsets of data in flash
//
	private static final int OFF_MSCNT = 0;
	private static final int OFF_LANG = 1;

	private static final int MS_LEN = 32;
	private static final int OFF_MAXCNT = 0;
	private static final int OFF_DWNCNT = 2;
	private static final int OFF_UPCNT = 3;


	public static int getCnt() {

		int i = Flash.read(Flash.MS_DATA+OFF_MSCNT);
		if (i<0 || i>15) i = 0;
		return i;
	}

	public static void setCnt(int cnt) {

		if (cnt<0 || cnt>15) return;
		Flash.write(Flash.MS_DATA+OFF_MSCNT, cnt);
	}

	public static int getLang() {

		int i = Flash.read(Flash.MS_DATA+OFF_LANG);
		if (i<0 || i>1) i = 0;
		return i;
	}

	public static void setLang(int i) {

		if (i<0 || i>1) i = 0;
		Flash.write(Flash.MS_DATA+OFF_LANG, i);
	}
/**
*	ms is 1 based
*/
	public static int getMSmaxCnt(int ms) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_MAXCNT;
		return Flash.read16(i);
	}

	public static void setMSmaxCnt(int ms, int val) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_MAXCNT;
		Flash.write16(i, val);
	}

	public static int getMSdwnCnt(int ms) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_DWNCNT;
		return Flash.read(i);
	}

	public static void setMSdwnCnt(int ms, int val) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_DWNCNT;
		Flash.write(i, val);
	}

	public static int getMSupCnt(int ms) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_UPCNT;
		return Flash.read(i);
	}

	public static void setMSupCnt(int ms, int val) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_UPCNT;
		Flash.write(i, val);
	}
}
