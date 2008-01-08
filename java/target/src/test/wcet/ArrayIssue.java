package wcet;

import com.jopdesign.sys.*;

/**
 * WCET timing issue - measurement differs from WCET analysis
 * @author martin
 *
 */
public class ArrayIssue {

	int[][] block = { {99, 104}, {109, 113} };

	public static void main(String[] args) {
		ArrayIssue ai = new ArrayIssue();
		ai.fdct(ai.block);
	}
	
	void fdct(int[][] block) {
	     int t1, diff;
	     t1 = Native.rd(Const.IO_CNT);
	     t1 = Native.rd(Const.IO_CNT)-t1;
	     diff = t1;

	     JVMHelp.wr('*');
	     
	     int[] ia = new int[1];
	     
	     t1 = Native.rd(Const.IO_CNT);
//	     int tmp0 = block[0][0];
//	     block[0][0] = 0;
	     ia[0] = 0;

//	     int x[] = block[0];
//	     int i = x[0];
	     
	     t1 = Native.rd(Const.IO_CNT)-t1;
	     JVMHelp.wr('-');
	     System.out.println(t1-diff);
	     
	}
}
