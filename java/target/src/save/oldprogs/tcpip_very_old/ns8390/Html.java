
public class Html {

	public static int[] prot;
	public static int[] head;
	public static int[] tail;

	public static int[] text;
	public static int[] temp;

	public static int[] msgtxt;
	public static int[] form1;
	public static int[] form2;

	public static int[] val;
	private static int hits;

	public static int[] msg;

	public static void init() {

		init1();
		init2();
		init3();
		init4();
		init5();
		init6();

		Temp.init();
		val = new int[6];
		msg = new int[20];
		for (int i=0; i<20; ++i) msg[i] = ' ';
		Display.init();
		hits = 0;
	}

	private static void init1() {
		int[] s1 = {'H', 'T', 'T', 'P', '/', '1', '.', '0', ' ', '2', '0', '0', ' ', 'O', 'K', '\r', '\n', '\r', '\n'};
		int[] s2 = {' ', '<', 'h', 't', 'm', 'l', '>', '<', 'b', 'o', 'd', 'y', '>'};
		prot = s1;
		head = s2;
	}

	private static void init2() {
		int[] s1 = {'<', '/', 'b', 'o', 'd', 'y', '>', '<', '/', 'h', 't', 'm', 'l', '>', '\r', '\n', '\r', '\n'};
		tail = s1;
		init22();
	}

	private static void init22() {
		int[] s2 = {'<', 'h', '2', '>', 'H', 'e', 'l', 'l', 'o', ' ', 'f', 'r', 'o', 'm', ' ', 'J', 'O', 'P', '!',
				'<', '/', 'h', '2', '>'};
		text = s2;
	}

	private static void init3() {
		int[] s1 = {'T', 'h', 'e', ' ', 't', 'e', 'm', 'p', 'e', 'r', 'a', 't', 'u', 'r', 'e', ' ', 'i', 's', ' '};
		temp = s1;
	}

	private static void init4() {
		int[] s1 = {'<', 'p', '>', 'l', 'a', 's', 't', ' ', 'm', 's', 'g', ':', ' '};
		msgtxt = s1;
	}
	private static void init5() {
		int[] s1 = {'<', 'p', '>', '<', 'f', 'o', 'r', 'm', '>', '<', 'i', 'n', 'p', 'u', 't', ' ',
			't', 'y', 'p', 'e', '=', 't', 'e', 'x', 't', ' ', 'n', 'a', 'm', 'e', '=', 'm', 's', 'g', '>'};
		form1 = s1;
	}
	private static void init6() {
		int[] s1 = {'<', 'i', 'n', 'p', 'u', 't', ' ',
			't', 'y', 'p', 'e', '=', 's', 'u', 'b', 'm', 'i', 't', '>', '<', '/', 'f', 'o', 'r', 'm', '>', };
		form2 = s1;
	}

	private static int append(int[] buf, int pos, int[] str) {

		int ret;
		for (ret=0; ret<str.length; ++ret) {
			buf[pos+ret] = str[ret];
		}
		return ret;
	}

	private static int[] getTemp() {

		int i = Temp.calc(46000-JopSys.rd(BBSys.IO_ADC));

		if (i<0) {
			i = -i;
			val[0] = '-';
		} else {
			val[0] = ' ';
		}
		val[1] = '0'+i/10;
		val[2] = '0'+i%10;
		val[3] = ' ';
		val[4] = 'C';
		val[5] = '.';

		return val;
	}

	private static void setMsg(int[] buf, int pos) {

		int i, j;
		Display.line2();
		for (i=0; i<20; ++i) {
			Display.data(' ');
			msg[i] = ' ';
		}
		Display.line2();
		for (i=0; i<20; ++i) {
			j = buf[pos+i];
			if (j==' ') break;
			if (j=='+') j=' ';
			msg[i] = j;
			Display.data(j);
		}
	}

	public static int setText(int[] buf, int req, int pos) {

		Display.line1();
		Display.intVal(++hits);
Eth.wrSer('\n');
for (int i=0; i<100; ++i) Eth.wrSer(buf[req+i]);
Eth.wrSer('\n');
		if (buf[req+5]=='?') {
			setMsg(buf, req+10);
		}

		int ret = 0;

		ret += append(buf, pos+ret, prot);
		ret += append(buf, pos+ret, head);
		ret += append(buf, pos+ret, text);
		ret += append(buf, pos+ret, tail);
		ret += append(buf, pos+ret, temp);
		ret += append(buf, pos+ret, getTemp());

		ret += append(buf, pos+ret, msgtxt);
		ret += append(buf, pos+ret, msg);
		ret += append(buf, pos+ret, form1);
		ret += append(buf, pos+ret, form2);

		return ret;
	}
}
