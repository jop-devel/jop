/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2006, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.wcet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
/**
 * Utility class for WCA.
 * @author rup,ms
 */
public class WU{

  /**
   * Parse WCA annotation
   * @param wcaA Java source line possibly with a @WCA comment
   * @return key,value String pairs
   */
  public static HashMap wcaA(String wcaA){
    HashMap wcaAH = null;
    int ai = wcaA.indexOf("@WCA");
    if(ai!=-1){
      wcaAH = new HashMap();
      String c = wcaA.substring(ai+"@WCA".length());
      StringTokenizer st = new StringTokenizer(c.trim());
      while(st.hasMoreTokens()){
        StringTokenizer stv = new StringTokenizer(st.nextToken(),"=");
        String key = stv.nextToken();
        if(key.indexOf('<')!=-1)
          key = key.substring(0,key.indexOf('<'));
        if(!key.equals("loop")){
          System.out.println("WCA only understands \"loop\" token at this time, not:"+key);
          System.exit(-1);
        }
        String val = stv.nextToken();
        wcaAH.put(key,val);
      }
    }
    return wcaAH;
  }

  /**
   * Inserts spaces in front of a string.
   * @param len the desired total length
   * @param val the string
   * @return the prepadded string
   */
  public static String prepad(String val, int len){
    StringBuffer sb = new StringBuffer();
    for(int i=len;i>val.length();i--){
      sb.append(" ");
    }
    sb.append(val);
    return sb.toString();
  }

  /**
   * Inserts spaces behind a string.
   * @param len the desired total length
   * @param val the string
   * @return the prepadded string
   */
  public static String postpad(String val, int len){
    StringBuffer sb = new StringBuffer();
    sb.append(val);
    for(int i=len;i>val.length();i--){
        sb.append(" ");
    }
    return sb.toString();
  }

  /**
   * Return n repetitions of a string, which is usually a single character.
   * @param val the string
   * @param n the repetitions
   * @return the repeated string
   */
  public static String repeat(String val, int n){
    StringBuffer sb = new StringBuffer();
    for(int i=0;i<n;i++){
        sb.append(val);
    }
    return sb.toString();
  }
  public static String printChains(ArrayList links){
    StringBuffer sb = new StringBuffer();
    for (int i=0;i<links.size();i++){
      sb.append("links["+i+"]"+printChain((ArrayList)links.get(i))+"\n");
    }
    return sb.toString();
  }
  /**
   * Print link info
   * @param link WCETBasicBlock
   * @return
   */
  public static String printChain(ArrayList link){
    StringBuffer sb = new StringBuffer();
    sb.append("chain[size="+link.size()+"]:");
    for (int j=0;j<link.size();j++){
      WCETBasicBlock wcbb = (WCETBasicBlock)link.get(j);
      sb.append(wcbb.getIDS());
      if(j<link.size()-1)
        sb.append(" -> ");

    }
    return sb.toString();
  }
}
