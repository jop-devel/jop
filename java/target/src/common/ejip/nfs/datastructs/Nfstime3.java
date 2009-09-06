package ejip.nfs.datastructs;

import ejip.nfs.Xdr;

public class Nfstime3 {
	private int seconds;
	private int nseconds;
	
	//TODO: write nfstime3.writeFields(StringBuffer sb)
	
	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getNseconds() {
		return nseconds;
	}

	public void setNseconds(int nseconds) {
		this.nseconds = nseconds;
	}

	public void loadFields(StringBuffer sb) {
		seconds = Xdr.getNextInt(sb);
		nseconds = Xdr.getNextInt(sb);
	}
	
	public String toString() {
		return "Seconds:\t" + seconds + "\n" + 
			"NSeconds:\t" + nseconds;
	}
}
