/**
 *  test/Schleife.java
 *
 *  Nils Hagge
 *
 *   2003-10-15 Erstelldatum
 */

package test;

import util.*;
 
public class Schleife
{
  private static void init()
  {
    Dbg.initSer();
  }
  
  public static void print(String s)
  {
    Dbg.wr(s);
  }
  
  public static void print(char c)
  {
    Dbg.wr(c);
  }
  
  public static void print(int i)
  {
    if(i < 0)
    {
      print("-");
      i = -i;
    }
    int ziffer[] = new int[10];
    int anzahl = 0;
    do
      ziffer[anzahl++] = i % 10;
    while((i /= 10) != 0);
    while(anzahl-- != 0)
      print((char)('0' + ziffer[anzahl]));
  }
	
  public static void main(String a[])
  {
    init();
    for(int i = 0; i < 20; i++)
    {
      print(" ");
      print(i);
    }
    print("\n");
    for(;;); // Ende
  } 
}