package jvm;

public class StackManipulation extends TestCase {
	
	public String getName() {
		return "StackManipulation";
	}
	
	public boolean test() {

		boolean ok = true;
		
		// dup_x2
		char s[] = new char[2];
		s[0] = s[1] = 'x';
		ok = ok && (s[0]=='x' && s[1]=='x');
		
		long l[] = new long[2];
		long lx;
		
		l[0] = 123;
		
		// dup2_x2
		lx = l[0] = 2;
		ok = ok && (l[0]==2 && l[1]==0 && lx==2);
		
		// dup2_x2
		l[0] = l[1] = 1;
		ok = ok && (l[0]==1 && l[1]==1);

		return ok;
	}

}
