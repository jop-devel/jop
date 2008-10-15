package ejip123;

import ejip123.util.Dbg;

/** Utility functions. Used in several ejip classes and may be of use outside the stack too. */
public class Util{
private Util(){
}

static void copyStr(StringBuffer src, StringBuffer dst){
	dst.setLength(0);
	int i = src.length();
	for(int j = 0; j < i; ++j)
		dst.append(src.charAt(j));
}

/**
 Checks if the characters stored in two CharSequences are equal.

 @param s CharSequence s.
 @param t CharSequence t.
 @return True, if both CharSequences have the same length and every character stored in s equal the one in t at the same
 position. */
public static boolean CharSequenceStartsWith(CharSequence s, CharSequence t){
	int len = t.length();
	if(s.length() < len)
		return false;
	for(int i = 0; i < len; i++){
		if(s.charAt(i) != t.charAt(i))
			return false;
	}
	return true;
}

/**
 Appends a IPv4 address in dotted-decimal notation to a StringBuffer.

 @param buf The StringBuffer.
 @param ip  The IP address.
 @return The number of characters appended to the StringBuffer. */
public static int appendIp(StringBuffer buf, int ip){
	int i = 0;
	for(int octCnt = 0; octCnt < 4; octCnt++){
		int oct = (ip>>((3 - octCnt)<<3))&(0xff);
		char v3 = (char)((oct % 10) + '0');
		oct /= 10;
		char v2 = (char)((oct % 10) + '0');
		oct /= 10;
		char v1 = (char)((oct % 10) + '0');
		if(v1 > '0'){
			buf.append(v1);
			i++;
		}
		if(v2 > '0'){
			buf.append(v2);
			i++;
		}
		buf.append(v3);
		i++;

		if(octCnt <= 2){
			buf.append('.');
			i++;
		}
	}
	return i;
}

public static int wrIp(int ip){
	int i = 0;
	for(int octCnt = 0; octCnt < 4; octCnt++){
		int oct = (ip>>((3-octCnt)<<3))&(0xff);
		int v3 = (oct % 10) + '0';
		oct /= 10;
		int v2 = (oct % 10) + '0';
		oct /= 10;
		int v1 = (oct % 10) + '0';
		if(v1>'0'){
			Dbg.wr(v1);
			i++;
		}
		if(v2>'0'){
			Dbg.wr(v2);
			i++;
		}
		Dbg.wr(v3);
		i++;

		if(octCnt <= 2){
			Dbg.wr('.');
			i++;
		}
	}
	Dbg.wr(' ');
	return i;
}
}
