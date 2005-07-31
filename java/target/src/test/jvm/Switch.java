package jvm;

public class Switch extends TestCase {
	
	public String getName() {
		return "Switch";
	}
	
	public boolean test() {

		boolean ok = true;
		
		ok = ok && (sw(2)==20);
		ok = ok && (sw(3)==3);
		ok = ok && (sw(4)==4);
		ok = ok && (sw(5)==5);
		ok = ok && (sw(6)==60);

		ok = ok && (lsw(0)==0);
		ok = ok && (lsw(1)==1);
		ok = ok && (lsw(4)==40);
		ok = ok && (lsw(5)==5);
		ok = ok && (lsw(6)==60);
		ok = ok && (lsw(7)==7);

		return ok;
	}

	public static int sw(int i) {

		int x = 999;
		switch (i) {
			case 3:
				x = 3;
				break;
			case 4:
				x = 4;
				break;
			case 5:
				x = 5;
				break;
			default:
				x = i*10;
		}
		
		return x;
	}

	public static int lsw(int i) {

		int x = 0;
		switch (i) {
			case 1:
				x = 1;
				break;
			case 7:
				x = 7;
				break;
			case 5:
				x = 5;
				break;
			default:
				x = i*10;
		}
		return x;
	}
}
