/* String.java -- immutable character sequences; the object of string literals
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
   Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.lang;


public final class String {

  final char[] value;

  public String()
  {
    value = "".value;
  }

  public String(String str)
  {
    value = str.value;
  }

  public String(StringBuffer str)
  {
	int count = str.length();
    value = new char[count];
for (int i=0; i<count; ++i) value[i] = str.value[i];
  }

  public int length()
  {
    return value.length;
  }

  public char charAt(int index)
  {
/*
    if (index < 0 || index >= count)
      throw new StringIndexOutOfBoundsException(index);
*/
    return value[index];
  }

  public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
  {
//    if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > count)
//      throw new StringIndexOutOfBoundsException();
/*
    if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > value.length)
return;
		// wait for array bound exception!
*/
/*
    System.arraycopy(value, srcBegin + offset,
                     dst, dstBegin, srcEnd - srcBegin);
*/
	for (int i=0; i<srcEnd-srcBegin; ++i) dst[dstBegin+i] = value[srcBegin+i];
  }

//
//	for CoffeinMarkEmbedded
//
  public int indexOf(String str, int fromIndex)
  {
    if (fromIndex < 0)
      fromIndex = 0;
    int limit = value.length - str.value.length;
    for ( ; fromIndex <= limit; fromIndex++)
      if (regionMatches(fromIndex, str, 0, str.value.length))
        return fromIndex;
    return -1;
  }

  public boolean regionMatches(int toffset, String other, int ooffset, int len)
  {
    if (toffset < 0 || ooffset < 0 || toffset + len > value.length
        || ooffset + len > other.value.length)
      return false;
    while (--len >= 0)
      {
        char c1 = value[toffset++];
        char c2 = other.value[ooffset++];
        if (c1 != c2)
          return false;
      }
    return true;
  }

}
