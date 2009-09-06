package ejip.nfs.datastructs;

public class WccData {
	public PreOpAttr before = new PreOpAttr();
	public PostOpAttr after = new PostOpAttr();
	
//	public void appendToStringBuffer(StringBuffer sb) {
//		before.appendToStringBuffer(sb);
//		after.appendToStringBuffer(sb);
//	}
	
	public void loadFields(StringBuffer sb) {
			before.getPreOpAttr(sb);
			after.loadFields(sb);
	}
	
	public String toString() {
		return "== before:\n" + before.toString() + "\n" +
			"== after:\n" + after.toString();
	}
}
