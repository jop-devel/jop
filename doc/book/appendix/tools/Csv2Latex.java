
/**
*	Csv2Latex.java
*
*/


import java.io.*;
import java.util.*;

public class Csv2Latex {



	Csv2Latex(String fn) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(fn));
			String s;
			
			for (int nr=0; (s=in.readLine()) != null; ++nr) {
				int pos;
				System.out.print("\\instr");
				for (int i=0; i<6; ++i) {
					System.out.print("{");
					String sub;
					if ((pos = s.indexOf(';'))!=-1) {
						sub = s.substring(0, pos);
						s = s.substring(pos+1);
					} else {
						sub = s;
						s = "";
					}
					String pr = "";
					for (int j=0; j<sub.length(); ++j) {
						char c = sub.charAt(j);
						if (c=='_') pr += "\\";
//						if (c=='<' || c=='>') pr += "$";
						pr += c;
//						if (c=='<' || c=='>') pr += "$";
					}
					System.out.print(pr+"}");
				}
				System.out.println();
				if (nr%2==1) {
					System.out.println("\\clearpage");				
				}
			}
			

		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}

	public static void main(String args[]) {

		Csv2Latex js = null;
		if (args.length==1) {
			js = new Csv2Latex(args[0]);
		} else {
			System.out.println("usage: java Csv2Latex file.csv");
			System.exit(-1);
		}

	}
}
