import BBSys;
import Msg;

/**
*	Not Stop fuer BB (drei Masten).
*/

public class Stop {

	public static void main(String[] args) {

		Msg m = new Msg();
		for (int i=0; i<10; ++i) {		// try ten times
			for (int j=1; j<4; ++j) {
				m.exchg(j, BBSys.CMD_STOP, 0);
			}
		}
	}
}
