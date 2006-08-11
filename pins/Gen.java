
import java.io.*;
import java.util.*;

public class Gen {

	public static void main(String[] args) {

		int i, pos;
		String line;

		try {
			BufferedReader in = new BufferedReader(new FileReader(args[0]));

			while ((line = in.readLine()) != null) {

				pos = line.indexOf('\t');
				String name = line.substring(0, pos);
				String pin = line.substring(pos+1, line.length());
				name = name.substring(name.indexOf('.')+1, name.length());

				if (name.equals("fl_ncs2")) {
					name = "fl_csb";
				} else {
					for (i=0; i<name.length(); ++i) {
						try {
							Integer.valueOf(name.substring(i, name.length()));
						} catch (Exception x) {
							continue;
						}
						break;
					}
					if (i<name.length()) {
						//
						// Quartus pin/bus naming is absolute shit!
						//
// only for pinout from Leo or... ???
//						if (name.substring(0, i).equals("io_l") || name.substring(0, i).equals("io_r")) {
//							name = name.substring(0, i) + "_" +
//								name.substring(i, name.length());
//						} else {
							name = name.substring(0, i) + "\\[" +
								name.substring(i, name.length()) + "\\]";
//						}
					}
				}
/*
cmp add_assignment "cyciotest" "" "fl_a\[9\]" "IO_STANDARD" "LVCMOS";
cmp add_assignment "cyciotest" "" "fl_a\[9\]" "LOCATION" "Pin_6";
cmp add_assignment "cyciotest" "" "fl_a\[9\]" "SIGNALPROBE_ENABLE" "Off";
*/
System.out.println("cmp add_assignment \"jop\" \"\" \""+name+"\" \"IO_STANDARD\" \"LVCMOS\";");
System.out.println("cmp add_assignment \"jop\" \"\" \""+name+"\" \"LOCATION\" \"Pin_"+pin+"\";");
System.out.println("cmp add_assignment \"jop\" \"\" \""+name+"\" \"SIGNALPROBE_ENABLE\" \"Off\";");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
}
