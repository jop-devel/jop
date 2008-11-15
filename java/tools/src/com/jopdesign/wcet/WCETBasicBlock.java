/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)

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
import java.util.HashSet;
import java.util.Iterator;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.StoreInstruction;

/**
 * Basic block of byte codes. It is aggregated in WCETMethodBlock.
 *
 * @author rup,ms
 */

// History:
// 2006-06-22 rup: Extracted as from WCETAnalyser on wish from ms


class WCETBasicBlock {

   // parent
  WCETMethodBlock wcmb;

  WCETMethodBlock invowcmb = null;

  static ArrayList bbl = new ArrayList(); // bb links
  static int[] bbe; // execution times
  static int wcetid;
  static int bcetid;
  static WCETBasicBlock[] bba; //S on 0 and T on end

  // loopcontroller vars
  boolean innerloop = false; // used both for invo block and lc blocks when applicable
  boolean onlyInvokeInInnerLoop = false; //rup: only activate cache hits when an invocation is alone in an inner loop
  ArrayList loopchains; // chains of BB that loop back to the lc

  // id of the bb
  int bid = -1;

  int line = -1;

  int loopid = -1;
  boolean loopdriver = false;
  boolean loopcontroller = false;
  WCETBasicBlock loopdriverwcbb = null; // is set for loopcontrollers
  boolean loopreturn = false;
  boolean leq = false;

  // loop target
  int loop = -1;
  int looptargetid = -1;


  // the reason why we are doing this...
  int wcetHit;
  int wcetMiss;
  int blockcyc;

  // additional cycles from a cache miss on invoke
  int cacheInvokeMiss = -1;
  // additional cycles from a cache
  int cacheReturnMiss = -1;

  // false if we encounter WCETNOTAVAILABLE bytecodes while counting
  boolean valid;

  // start pos
  final int start;

  final Integer key;

  InstructionHandle stih;

  // end pos which will change as splitting happens
  int end;

  // end instruction handle
  InstructionHandle endih;

  // previous bb
  WCETBasicBlock prevbb;

  // sucessor block
  WCETBasicBlock sucbb;

  // target block
  WCETBasicBlock tarbb;


  // invard links from other BBs called targeters
  HashMap inbbs;

  // invoke info after toCodeString has been called
  String invokeStr;

  //Strings of method ids
  String bbinvo;

  // Walking
  int sc = -1; // positive if controlled
  int scsid = -1; //id of source controller
  int sctid = -1; //id of target controller

  int tc = -1;
  int tcsid = -1;
  int tctid = -1;



  // T or S

  boolean s = false;
  boolean t = false;
  public final static int SNODE = 1;
  public final static int BNODE = 2;
  public final static int INODE = 3;
  public final static int TNODE = 4;
  public int nodetype = BNODE;
  WCETBasicBlock(WCETMethodBlock wcmb, int nodetype){
    this.nodetype = nodetype;
    this.wcmb = wcmb;
    valid = true;
    wcetHit =0;
    wcetMiss =0;
    start = 0;
    key = new Integer(-1);
    inbbs = new HashMap();
  }

  WCETBasicBlock(InstructionHandle stih, InstructionHandle endih, WCETMethodBlock wcmb, int nodetype) {
    this.wcmb = wcmb;
    this.nodetype = nodetype;
    valid = false;
    wcetHit = -1;
    wcetMiss = -1;
    inbbs = new HashMap();
    start = stih.getPosition();
    key = new Integer(start);
    end = endih.getPosition();
    this.stih = stih;
    this.endih = endih;
  }

  public static void linkbb(WCETBasicBlock S){
//System.out.println("About to link:"+S.wcmb.name);
//for (int i=0;i<bba.length;i++){
//  System.out.println("bba["+i+"]"+bba[i].id);
//}
    WCETBasicBlock b = S;
    ArrayList al = new ArrayList(); // not finished paths

    // [l.length-1] : last element (T node)
    //  Integer.MIN_VALUE terminates a method sequence
    // first element is unique int id of method: mmap->midmap
    int[] l = new int[200];
    int MAXLINK = 1000;  //

    l[0] = b.bid;
//System.out.println("l[0]:"+b.id);
    l[1] = Integer.MIN_VALUE;
    al.add(l);
    int len = 0;
    while(al.size()>0){
      if((l = (int[])al.get(0)) != l){
        len = 0;
        for (int i=0;true;i++){
          if(l[i]!=Integer.MIN_VALUE)
            len++;
          else{
  //System.out.println("l[len-1] "+l[len-1]);
            b = bba[l[len-1]];
            break;
          }
        }
      }
//System.out.println("len:"+len);
//for (int i=0;i<len;i++){
//  System.out.println(l[i]);
//}

      if(l.length<=len+1){ // make room for invokes
        int newl[] = new int[l.length+10];
        System.arraycopy(l,0,newl,0,len);
        l = newl;
      }

      if(l.length>MAXLINK){
        System.out.println("MAXLINK in "+b.wcmb.name+": probably UNBOUNDED and need a @WCA loop annotation");
        al.remove(l);
        break;
      }

      int hit = 0;
      int chit = 0;
      boolean svio = false;
      if(b.sucbb != null){
        if(b.sc != -1){  // constraint on sucbb
          for (int i=1;i<len;i++){
            if(l[i-1] == b.bid && l[i] == b.sucbb.bid)
              hit++;
            if(l[i-1] == b.scsid && l[i] == b.sctid)
              chit++;
          }
          if(hit>chit*b.sc)
            svio = true;
        }
      }

      hit = 0;
      chit = 0;
      boolean tvio = false;
      if(b.tarbb != null){
        if(b.tc != -1){
          for (int i=0;i<len;i++){
            if(l[i-1] == b.bid && l[i] == b.tarbb.bid)
              hit++;
            if(l[i-1] == b.tcsid && l[i] == b.tctid)
              chit++;
          }
          if(hit>chit*b.tc)
            tvio = true;
        }
      }

      // both paths advancing
      if((b.sucbb != null && !svio) && (b.tarbb != null && !tvio)){
        int newl[] = new int[l.length];
        System.arraycopy(l,0,newl,0,l.length);
        newl[len] = b.tarbb.bid;
        newl[len+1] = Integer.MIN_VALUE;
        if(b.tarbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(newl);
        else
          al.add(newl);

        l[len] = b.sucbb.bid;
        l[len+1] = Integer.MIN_VALUE;
        if(b.sucbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(al.remove(0));
      } else if(b.sucbb != null && !svio){
        l[len] = b.sucbb.bid;
        l[len+1] = Integer.MIN_VALUE;
        if(b.sucbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(al.remove(0));
      } else if(b.tarbb != null && !tvio){
        l[len] = b.tarbb.bid;
        l[len+1] = Integer.MIN_VALUE;
        if(b.tarbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(al.remove(0));
      } else
        al.remove(l);
    }

    // append methodid to invoking blocks as -id
//    for (int i=0;i<bbl.size();i++){
//      l = (int[])bbl.get(i);
//      int j = 0;
//      for (;j<l.length;j++){
//        if(l[j]==Integer.MIN_VALUE){
//          break;
//        }
//        else{
//          int inv = bba[l[j]].bbinvo.size();
//          int[] newl = new int[l.length+inv];
//          System.arraycopy(l,0,newl,0,j);
//          System.arraycopy(l,j,newl,j+inv,l.length-j);
//          for(int m = 0;m<inv;m++){
//            String mids = (String)bba[l[j]].bbinvo.get(m);
//            int mid = ((Integer)wcmb.wca.midmap.get(mids)).intValue();
//            newl[j+m] = -mid;
//          }
//        }
//      }
//    }
//
//
//
//    if(true){
//      for (int i=0;i<bbl.size();i++){
//        l = (int[])bbl.get(i);
//        int j = 0;
//        for (;j<l.length;j++){
//          if(l[j]==-1)
//            break;
//        }
//        System.out.println("bbl["+i+"]"+b.wcmb.cname+":"+j);
//      }
//    }
  }

  public static void bbe(){
    bbe = new int[bbl.size()];
//System.out.println("bbe size:"+bbe.length);
//System.out.println("bba[l[j]]:"+bba.length);
//for (int i=0;i<bba.length;i++){
//  System.out.println("bba["+i+"].id"+bba[i].id);
//
//}
    int wcetmax = Integer.MIN_VALUE;
    int bcetmin = Integer.MAX_VALUE;
    for (int i=0;i<bbl.size();i++){
      int[] l = (int[])bbl.get(i);
//System.out.println("bbl["+i+"]"+"l.length="+l.length);
      for (int j=0;j<l.length;j++){
        if(l[j]==Integer.MIN_VALUE)
          break;
//System.out.println("i = "+i);
//System.out.println("j = "+j);
//System.out.println("l[j] = "+l[j]);
        if(l[j]<0){ // another method w. neg. entry
//          int mid = -m;
//          String mids = (String)bba[l[j]].bbinvo.get(mid);
//          int mid = ((Integer)wcmb.wca.midmap.get(mids)).intValue();
//          newl[j+m] = -mid;
//        } else if(bba.length>l[j] && bba[l[j]]!=null){//TODO
          bbe[i] += bba[l[j]].getBlockCycles();
        }

//System.out.println("bba[l["+i+"]].getBlockCycles()"+bba[l[i]].getBlockCycles());
//System.out.println("bbe["+i+"]="+bbe[i]);
        if(bbe[i]>wcetmax){
          wcetid = i;
          wcetmax = bbe[i];
        }
        if(bbe[i]<bcetmin){
          bcetid = i;
          bcetmin = bbe[i];
        }
      }
    }
//System.out.println("wcetid:"+wcetid);

//System.out.println("wcetmax:"+wcetmax);
//System.out.println("bcetid:"+bcetid);
//System.out.println("bcetmax:"+bcetmin);
//System.exit(-1);

  }


  /**
   * Add wbb that points to this wbb.
   *
   * @param wbbtargeter
   *          a wbb that points to this wbb.
   * @return true if it was already added
   */
  boolean addTargeter(WCETBasicBlock wbbtargeter) {
    WCETBasicBlock wbbold = (WCETBasicBlock) inbbs.put(wbbtargeter.getKey(),
        wbbtargeter);
    if (wbbold == null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Will create a new BB by splitting the old.
   *
   * @param stih
   *          the first instruction of the new block
   * @return the new BB
   */
  WCETBasicBlock split(InstructionHandle newstih) {
    WCETBasicBlock spbb = new WCETBasicBlock(newstih, endih, wcmb, WCETBasicBlock.BNODE);
    end = newstih.getPrev().getPosition();
    endih = newstih.getPrev();
    return spbb;
  }

  /**
   * <code>loopchains</code> now contains the chains the define the loop.
   */
  public void createLoopChains(){
//    System.out.println("entering createloopchains for:"+getIDS()+","+wcmb.cname+"."+wcmb.name);
    if(!loopcontroller){
      System.out.println("not a loop controler");
      System.exit(-1);
    }
    else{
//      System.out.println("loopcontroller:"+getIDS());
//      System.out.println("loopdriver:"+loopdriverwcbb.getIDS());
    }

    innerloop = true;
    loopchains = new ArrayList();

//if(innerloop)
//  return;
    ArrayList chains = new ArrayList();
    ArrayList chain = new ArrayList();
    chains.add(chain);
    chain.add(this);
    chain.add(sucbb);
    // loop until exausted all possibilities
    while(chains.size()>0){
      chain = (ArrayList)chains.get(0);
      WCETBasicBlock wcbblast = (WCETBasicBlock)chain.get(chain.size()-1);
      if(wcbblast.loopcontroller)
        innerloop = false;
      if(wcbblast.sucbb != null){
        if(wcbblast.sucbb == this){
          loopchains.add(chain);
        }
        else if(!wcbblast.loopcontroller){
          ArrayList newchain = (ArrayList)chain.clone();
          newchain.add(wcbblast.sucbb);
          chains.add(newchain);
        }
      }
      if(wcbblast.tarbb != null){
        if(wcbblast.tarbb == this)
          loopchains.add(chain);
        else {
          ArrayList newchain = (ArrayList)chain.clone();
          newchain.add(wcbblast.tarbb);
          chains.add(newchain);
        }
      }
      chains.remove(0);
    }
    // mark invocation blocks as innerloop
    if(innerloop){
//System.out.println("if innerloop");
//System.out.println("loopchains:\n"+WU.printChains(loopchains));
      HashSet invowcmb = new HashSet();
      ArrayList invoblocks = new ArrayList();
      // loop all chains
      for (int i=0;i<loopchains.size();i++){
        chain = (ArrayList)loopchains.get(i);
        for (int j=0;j<chain.size();j++){
          WCETBasicBlock wcbb = (WCETBasicBlock)chain.get(j);
          if(wcbb.nodetype == WCETBasicBlock.INODE){
            invowcmb.add(wcbb.invowcmb);
            invoblocks.add(wcbb);
          }
        }
      }
//System.out.println("invowcmb.size():"+invowcmb.size());
      if(invowcmb.size()==1){
//System.out.println("invoblocks.size():"+invoblocks.size());
        for (int i=0;i<invoblocks.size();i++){
          WCETBasicBlock invowcbb = (WCETBasicBlock)invoblocks.get(i);
          invowcbb.innerloop = true;
          invowcbb.loopdriverwcbb = loopdriverwcbb;
          invowcbb.loop = loop;
          if(invoblocks.size() == 1)
            invowcbb.onlyInvokeInInnerLoop = true;
        }
      }
    }
  }

  /**
   * Returns the cycle count for cache hit. Remember to check validWcet() for validity.
   *
   * @return wcet count
   */
  public int getWcetHit() {
    return wcetHit;
  }

  /**
   * Calculte wcetHit and wcetMiss for the Basic block.
   */
  public void calculateWcet() {
    InstructionHandle ih = stih;
    wcetHit = 0;
    wcetMiss = 0;
    valid = true;
    if(nodetype != WCETBasicBlock.SNODE && nodetype != WCETBasicBlock.TNODE){
      do {
        int wcetHitTmp = WCETInstruction.getCyclesFromHandle(ih, false, wcmb.getN());
        int wcetMissTmp = WCETInstruction.getCyclesFromHandle(ih, true, wcmb.getN());
        if (wcetHitTmp != WCETInstruction.WCETNOTAVAILABLE) {
          wcetHit += wcetHitTmp;
          wcetMiss += wcetMissTmp;
        } else {
          valid = false;
        }
      } while (ih != endih && (ih = ih.getNext()) != null); // null will never
                                                            // happen, but need
                                                            // the getNext
    }
  }

  /**
   * True if the
   *
   * @return
   */
  public boolean getValid() {
    return valid;
  }

  // convert to block name: in flow = out flow, S & T default = 1
  public String toLSFlow(){
    StringBuffer ls = new StringBuffer();
    if(nodetype == WCETBasicBlock.SNODE)
      ls.append(getIDS()+": 1 = f"+getIDS()+"_"+sucbb.getIDS()+"; // S flow\n");
    else if(nodetype == WCETBasicBlock.BNODE || nodetype == WCETBasicBlock.INODE){
      HashMap tinbbs = getInbbs();

      ls.append(getIDS()+": ");
      for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
        Integer tkeyInt = (Integer) titer.next();
        WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
        ls.append("f"+w.getIDS()+"_"+getIDS());

        if(titer.hasNext())
          ls.append(" + ");
      }
      //TODO: Solve the real issue with no in-flow
      if(tinbbs.size() == 0)
        ls.append(" 0 ");

      ls.append(" = ");
      if(sucbb != null){
        //if(wcbb.sucbb.nodetype != WCETBasicBlock.TNODE){
          ls.append("f"+getIDS()+"_"+sucbb.getIDS());
        //}
      }
      // same target can happen if there is an empty branch; if(true);
      boolean sametarget = ((sucbb != null && tarbb!=null)&&(sucbb == tarbb));
      if((sucbb != null && tarbb!=null)&&!sametarget)
        ls.append(" + ");
      if(tarbb!=null && !sametarget)
        ls.append("f"+getIDS()+"_"+tarbb.getIDS());

      ls.append(";\n");
      }
    else if(nodetype == WCETBasicBlock.TNODE){
      HashMap tinbbs = getInbbs();

      ls.append(getIDS()+": ");
      for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
        Integer tkeyInt = (Integer) titer.next();
        WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
        ls.append("f"+w.getIDS()+"_"+getIDS());

        if(titer.hasNext())
          ls.append(" + ");
      }
      ls.append(" = 1");
    }
    else{
      System.out.println("Unknown nodetype");
      System.exit(-1);
    }
    return ls.toString();
  }

  // flow connect external invo BB to S
  public String toLSS(WCETBasicBlock wcbb){
    StringBuffer ls = new StringBuffer();
    if(nodetype == WCETBasicBlock.SNODE){
      if(wcbb == null) {
        ls.append(getIDS()+": 1 = f"+getIDS()+"_"+sucbb.getIDS()+"; // S flow\n");
        ls.append("t"+getIDS()+" = 0; // force to zero\n");
      }else{ // connect the two cache paths
        ls.append(getIDS()+": fch"+ wcbb.getIDS()+"_"+ getIDS() + "+ fcm"+ wcbb.getIDS()+"_"+ getIDS()+" = f"+getIDS()+"_"+sucbb.getIDS()+"; // S flow\n");
        if (wcbb.getCacheInvokeMiss() == 0)
          ls.append("t"+getIDS()+" = 0; // S cache miss time\n");
        else
          ls.append("t"+getIDS()+" = "+wcbb.getCacheInvokeMiss()+" fcm"+ wcbb.getIDS()+"_"+ getIDS()+"; // S cache miss time\n");
      }
    }
    else{
      System.out.println("Not S type");
      System.exit(-1);
    }
    return ls.toString();
  }

  // flow connect external BB to T
  public String toLST(WCETBasicBlock wcbb){
    StringBuffer ls = new StringBuffer();
    if(nodetype == WCETBasicBlock.TNODE){
      HashMap tinbbs = getInbbs();
      ls.append(getIDS()+": ");
      for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
        Integer tkeyInt = (Integer) titer.next();
        WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
        ls.append("f"+w.getIDS()+"_"+getIDS());

        if(titer.hasNext())
          ls.append(" + ");
      }
      if(wcbb == null) {
        ls.append(" = 1; // T flow\n");
		ls.append("t"+getIDS()+" = 0; // force to zero\n");
      }else{
        ls.append(" = f"+getIDS()+"_"+wcbb.getIDS()+";// T interconnect flow\n");
        // hit on return if it is a leaf
        if(wcmb.leaf) {
          ls.append("t"+getIDS()+" = 0; // T cache hit (leaf)\n");
        }
        else{
          if (wcbb.getCacheReturnMiss() == 0)
            ls.append("t"+getIDS()+" = 0;\n");
          else
            ls.append("t"+getIDS()+" = "+wcbb.getCacheReturnMiss()+" f"+getIDS()+"_"+wcbb.getIDS()+"; // T cache miss (not leaf)\n");
        }
      }

    }
    else{
      System.out.println("Not TNODE");
      System.exit(-1);
    }
    return ls.toString();
  }

  // hook INODE's outgoing link to invo S and T
  // or hook INODE's loopdriver to S and T
  public String toLSInvo(){
    //System.exit(-1);
    StringBuffer ls = new StringBuffer();
    // hook the called method to the outgoing node
    if(nodetype == WCETBasicBlock.INODE){
//System.out.println(getIDS()+" bbinvo="+bbinvo);
//if(invowcmb== null)
//  System.out.println("invowcmb== null");
//if(invowcmb.S== null)
//  System.out.println("invowcmb== null");

      ls.append("/* Connecting(invoking) to "+bbinvo+" id:"+invowcmb.S.getIDS()+"*/\n");
      // to invo S
      String invodriver = getIDS()+"_"+sucbb.getIDS();
      String invoS = getIDS()+"_"+invowcmb.S.getIDS();
      String invoT = invowcmb.T.getIDS()+"_"+getIDS();
      ls.append(getIDS()+"_S: fcm"+ invoS+" + fch"+ invoS+" = f"+invodriver+"; //cache S paths\n");
      //ls.append(getIDS()+"ch: fch"+ invoS+" = f"+invodriver+"; //cache hit S path\n");
      ls.append(getIDS()+ "_T: f" + invodriver+" = f" +invoT+"; // invo T return path \n");

      // flow constrain the cache paths
      //if(innerloop){
	  if(innerloop && onlyInvokeInInnerLoop){
        //ls.append("fcm"+ invoS + " <= f"+loopdriverwcbb.getLoopdriverprevwcbb().getIDS()+"_"+loopdriverwcbb.getIDS()+"; // cache misses driven by loopdriver\n");
        ls.append("fcm"+ invoS + " = f"+loopdriverwcbb.getIDS()+"_"+loopdriverwcbb.getSucbb().getIDS()+"; // innerloop && onlyInvokeInInnerLoop: cache misses driven by loopdriver\n");
      } else { // cache misses
        ls.append("fch"+ invoS + " = 0; // !(innerloop && onlyInvokeInInnerLoop): no cache hits\n");
      }

      ls.append("/* Done with "+bbinvo+"*/\n");
     } else{
       System.out.println("Not INODE type");
       System.exit(-1);
     }
     return ls.toString();
  }

  // loop controller code
  public String toLSLoop(){
    StringBuffer ls = new StringBuffer();
    //TODO: fix
    if(loopcontroller){
      if(leq)
        ls.append("LC_"+getIDS()+": f"+ getIDS()+"_"+sucbb.getIDS()+" <= "+loop+" f"+getLoopdriverwcbb().getLoopdriverprevwcbb().getIDS()+"_"+getLoopdriverwcbb().getIDS()+";\n");
      else
        ls.append("LC_"+getIDS()+": f"+ getIDS()+"_"+sucbb.getIDS()+" = "+loop+" f"+getLoopdriverwcbb().getLoopdriverprevwcbb().getIDS()+"_"+getLoopdriverwcbb().getIDS()+";\n");
    }else{
      System.out.println("BB is not loop controller");
      System.exit(-1);
    }
    return ls.toString();
  }

  public String toLSCycles(){
    StringBuffer ls = new StringBuffer();
    ls.append("t"+getIDS()+" = ");
    HashMap tinbbs = getInbbs();
    for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
      Integer tkeyInt = (Integer) titer.next();
      WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
      ls.append(blockcyc+" f"+w.getIDS()+"_"+getIDS());
      if(titer.hasNext())
        ls.append(" + ");
    }
    ls.append(";\n");
    return ls.toString();
  }

  // return objective string
  public String toLSObj(){
    return "t"+getIDS();
  }

  public String toDotFlowEdge(WCETBasicBlock bb){
    return getIDS()+"->"+bb.getIDS();
  }

  public String toDotFlowLabel(WCETBasicBlock bb){
    return "f"+getIDS()+"_"+bb.getIDS();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    // block (6)
    String s = "B" + bid;
    if (!getValid())
      s+="*";

    sb.append(WU.postpad(s,8));

    // Cyc. hit   Cyc. miss
    sb.append(WU.prepad(Integer.toString(wcetHit),6));
    sb.append("   ");
    sb.append(WU.prepad(Integer.toString(wcetMiss),9));

    return sb.toString();
  }

  /**
   * Outputs in the format similar to Table 1 in the DATE paper.
   *
   * @return the formatted string
   */
  public String toCodeString() {
    StringBuffer sb = new StringBuffer();
    if(nodetype == WCETBasicBlock.SNODE){
      sb.append(WU.postpad(getIDS()+"'S'\n",6)); // see the BBs that point to this BB
    } else if(nodetype == WCETBasicBlock.TNODE){
      String tStr = "<-[";
      for (Iterator iter = inbbs.keySet().iterator(); iter.hasNext();) {
        Integer keyInt = (Integer) iter.next();
        WCETBasicBlock wcbb = (WCETBasicBlock) inbbs.get(keyInt);
        tStr += wcbb.getIDS();
        if(iter.hasNext())
          tStr += " ";
      }
      tStr += "]";
      sb.append(WU.postpad(getIDS()+"'T'"+tStr+"\n",6)); // see the BBs that point to this BB
    }
    else{
      InstructionHandle ih = stih;
      blockcyc = 0;

      LineNumberTable lnt = wcmb.mi.getMethod().getLineNumberTable();
      int prevLine = -1;
      int srcLine = -1;
      do {
        if(wcmb.wca.jline){
          srcLine = lnt.getSourceLine(ih.getPosition());

          if(srcLine>prevLine){
            //"Annotation" example
            int ai = wcmb.codeLines[srcLine-1].trim().indexOf("@WCA");
            String c = "";
            if(ai!=-1){
              c = wcmb.codeLines[srcLine-1].trim().substring(ai);
              sb.append(WU.postpad("Annotated Src. line :"+srcLine+": "+wcmb.codeLines[srcLine-1].trim(),62)+"\n");
            }else
              sb.append(WU.postpad("  Src. line "+srcLine+": "+wcmb.codeLines[srcLine-1].trim(),62)+"\n");
          }
          prevLine = srcLine;
        }

        // block (len 6)
        if (ih == stih) {
          String tStr = "<-[";
          for (Iterator iter = inbbs.keySet().iterator(); iter.hasNext();) {
            Integer keyInt = (Integer) iter.next();
            WCETBasicBlock wcbb = (WCETBasicBlock) inbbs.get(keyInt);
            tStr += wcbb.getIDS();
            if(iter.hasNext())
              tStr += " ";
          }
          tStr += "]";
          String lcStr = "";
          if(loopcontroller){
            lcStr += "lc(ld="+loopid+")";
            if(loopdriver)
              lcStr += ",";
          }
          if(loopdriver)
            lcStr += "ld";
  if(innerloop)
    lcStr += "(il)"; //means on inner loop

  if(innerloop && onlyInvokeInInnerLoop)
    lcStr += "(oi)";  //means only invoke


  if(nodetype == BNODE)
    sb.append(WU.postpad(getIDS()+"'B'{"+lcStr+"}"+tStr,6)); // see the BBs that point to this BB
  else if(nodetype == INODE)
    sb.append(WU.postpad(getIDS()+"'I'{"+lcStr+"}"+tStr,6)); // see the BBs that point to this BB
  sb.append("\n");

  //        sb.append(WU.postpad("B" + id,6));
        }
        sb.append("      ");

        // addr (len 6)
        sb.append(WU.postpad(ih.getPosition() + ":",6));

        if(!WCETInstruction.wcetAvailable(ih.getInstruction().getOpcode()))
          sb.append("*");
        else
          sb.append(" ");

        // bytecode (len 22)
        StringBuffer ihs = new StringBuffer(ih.getInstruction().getName());
        // MS get rid of instruction number
        // + "["  + ih.getInstruction().getOpcode() + "]");

        if (ih.getInstruction() instanceof BranchInstruction) {
          // target
          InstructionHandle ihtar = ((BranchInstruction) ih.getInstruction())
              .getTarget();
          int tarpos = ihtar.getPosition();
          ihs.append(" " + tarpos);
        }

        sb.append(WU.postpad(ihs.toString(),20));

        String invoStr = "";

        //invoke instructions
        if(ih.getInstruction() instanceof InvokeInstruction){
if(nodetype!=WCETBasicBlock.INODE){
  bbinvo = ((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())
  +"."
  +((InvokeInstruction)ih.getInstruction()).getMethodName(wcmb.getCpg())
  +((InvokeInstruction)ih.getInstruction()).getSignature(wcmb.getCpg());
}
          String retsig = ((InvokeInstruction)ih.getInstruction()).getReturnType(wcmb.getCpg()).getSignature();
////
          //signature Java Type, Z boolean, B byte, C char, S short, I int
          //J long, F float, D double, L fully-qualified-class, [ type type[]
//          bbinvo = methodid;
//System.out.println("extra bbinvo:"+bbinvo);
          Method m = wcmb.wca.getMethod(bbinvo).getMethod();
//System.out.println("bbinvo:"+bbinvo);
//System.out.println("name:"+wcmb.name);
//System.out.println("cname:"+wcmb.cname);
          if(bbinvo.startsWith(WCETAnalyser.nativeClass)){
            int opcode = wcmb.wca.getNativeOpcode(m.getName());//methodid);
            if(opcode == -1){
              sb.append(WU.prepad("*to check",10));
              invoStr = bbinvo + " did not find mapping";
            }else
            {
              int cycles = WCETInstruction.getCycles(opcode,false,0);
              // no difference as cache is not involved
              blockcyc += cycles;
              sb.append(WU.prepad(Integer.toString(cycles),10));
              sb.append("   ");
              sb.append("                ");
              invoStr = bbinvo;
            }
          }
          else if(m!=null && !m.isAbstract()){
            int invon = -1;
            if(m.getCode()!= null){
              invon = (m.getCode().getCode().length + 3) / 4;
            }else{
              invon=0;
            }
            int invokehit = WCETInstruction.getCyclesFromHandle(ih,false,invon);
            int invokemiss = WCETInstruction.getCyclesFromHandle(ih,true,invon);

            //now the return
            int rethit = -1;
            int retmiss = -1;
            //TODO: Check these with ms
            if(retsig.equals("V")){
              rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.RETURN,false, wcmb.n);
              retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.RETURN,true, wcmb.n);
            }
            else if(retsig.equals("I") || retsig.equals("Z")|| retsig.equals("B")|| retsig.equals("C")|| retsig.equals("S")){
              rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.IRETURN,false, wcmb.n);
              retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.IRETURN,true, wcmb.n);
            }
            else if(retsig.equals("J")){
              rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.LRETURN,false, wcmb.n);
              retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.LRETURN,true, wcmb.n);
            }
            else if(retsig.equals("D")){
              rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.DRETURN,false, wcmb.n);
              retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.DRETURN,true, wcmb.n);
            }
            else if(retsig.equals("F")){
              rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.FRETURN,false, wcmb.n);
              retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.FRETURN,true, wcmb.n);
            }
            else if(retsig.startsWith("[") || retsig.startsWith("L")){
              rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.ARETURN,false, wcmb.n);
              retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.ARETURN,true, wcmb.n);
            }else{
              System.out.println("Did not recognize "+retsig+" as return type");
              System.exit(-1);
            }
            cacheInvokeMiss = (invokemiss-invokehit);
            cacheReturnMiss = (retmiss-rethit);
            // that's the invoke instruction
            blockcyc = invokehit;
            // cache influence now as always miss up
            // we hve solved it with extra blocks
//            blockcyc += cacheInvokeMiss;
//            blockcyc += cacheReturnMiss;
            if((((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())).equals(wcmb.wca.nativeClass)){
  //            sb.append(WU.prepad("*"+Integer.toString(wcetihHit)+"/"+Integer.toString(wcetihMiss),10));
              sb.append(WU.prepad("*to check",10));
            } else {
  //            sb.append(WU.prepad(Integer.toString(wcetihHit)+"/"+Integer.toString(wcetihMiss),10));
              sb.append(WU.prepad(invokehit+"",10));
              sb.append(WU.prepad(cacheInvokeMiss+"",8));
              sb.append(WU.prepad(cacheReturnMiss+"",8));
            }

            sb.append("   ");
            invoStr = bbinvo+", invoke(n="+invon+"):"+invokehit+"/"+invokemiss+" return(n="+wcmb.getN()+"):"+rethit+"/"+retmiss;
            if((((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())).equals(wcmb.wca.nativeClass)){
              invoStr = bbinvo;
            }
          }
          else{
            sb.append("*");
          }

        }else{ // non-invoke functions
          int wcetih;
          if(ih.getInstruction() instanceof ReturnInstruction){
            wcetih = WCETInstruction.getCyclesFromHandle(ih, false, wcmb.getN());
            sb.append(WU.prepad(Integer.toString(wcetih),10));
          } else{
            wcetih = WCETInstruction.getCyclesFromHandle(ih, false, wcmb.getN());
            sb.append(WU.prepad(Integer.toString(wcetih),10));
          }
          blockcyc += wcetih;

          sb.append("   ");
          sb.append("                ");
        }

        // invoke info or ""
        sb.append(invoStr);

        //field info
        if(ih.getInstruction() instanceof FieldInstruction){
          String fieStrType = ((FieldInstruction)ih.getInstruction()).getFieldType(wcmb.getCpg()).toString();
          sb.append(fieStrType+" ");
          if(ih.getInstruction() instanceof FieldOrMethod){
            String fieStrClass = ((FieldOrMethod)ih.getInstruction()).getClassName(wcmb.getCpg());
            sb.append(fieStrClass+".");
          }
          String fieStrName = ((FieldInstruction)ih.getInstruction()).getFieldName(wcmb.mi.getConstantPoolGen());
          sb.append(fieStrName);
        }

       //fetch local variable name and type from class file
        // MS: disable output of local type
        if(ih.getInstruction() instanceof LocalVariableInstruction){
          if(ih.getInstruction() instanceof StoreInstruction){
            StoreInstruction si = (StoreInstruction)ih.getInstruction();
            //add instruction len to pos to peek into localvariable table
            String siStr = wcmb.getLocalVarName(si.getIndex(),ih.getPosition()+ih.getInstruction().getLength());
//            if(siStr.length()>0)
//              sb.append("->"+siStr+" ");
          } else{ //load or iinc
            LocalVariableInstruction lvi = (LocalVariableInstruction)ih.getInstruction();
            String lvStr = wcmb.getLocalVarName(lvi.getIndex(),ih.getPosition());
//            if(lvStr.length()>0)
//              sb.append(lvStr+" ");
          }
        }

        if(ih.getInstruction() instanceof ArrayInstruction){
          String aType = ((ArrayInstruction)ih.getInstruction()).getType(wcmb.getCpg()).getSignature();
          sb.append(aType+" ");
        }

        //block sum if end
        if(ih == endih){
          sb.append("sum(B"+bid+"):");
  //        if(ih.getInstruction() instanceof ReturnInstruction){
            sb.append(WU.prepad(""+blockcyc,7));
  //          sb.append(" *do add return cycles and do not have size of caller (yet)");
  //        }
  //        else{
  //          sb.append(WU.prepad(blockcychit+"/"+blockcycmiss,7));
  //        }
        }

        sb.append("\n");
      } while (ih != endih && (ih = ih.getNext()) != null);
    }

    return sb.toString();
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public int getBid() {
    return bid;
  }

  public String getIDS(){

    StringBuffer sbIDS = new StringBuffer();

    if(wcmb.wca.global){
      sbIDS.append("M"+wcmb.mid+"_");
    }

    if(nodetype == SNODE)
      sbIDS.append("S");
    if(nodetype == BNODE)
      sbIDS.append("B"+bid);
    if(nodetype == INODE)
      sbIDS.append("I"+bid);
    if(nodetype == TNODE)
      sbIDS.append("T");


    if(wcmb.wca.global){
      sbIDS.append("_E"+wcmb.E);
    }

    return sbIDS.toString();
  }

  public void setBid(int bid) {
    this.bid = bid;
  }

  public InstructionHandle getStih() {
    return stih;
  }

  public InstructionHandle getEndih() {
    return endih;
  }

  public void setEndih(InstructionHandle endih) {
    this.endih = endih;
  }

  public WCETBasicBlock getTarbb() {
    return tarbb;
  }

  public WCETBasicBlock getSucbb() {
    return sucbb;
  }

  public void setTarbb(WCETBasicBlock tarbb) {
    this.tarbb = tarbb;
  }

  public void setSucbb(WCETBasicBlock sucbb) {
    this.sucbb = sucbb;
  }

  public Integer getKey() {
    return key;
  }

  public int getWcetMiss() {
    return wcetMiss;
  }

  public String getInvokeStr() {
    return invokeStr;
  }

  public HashMap getInbbs() {
    return inbbs;
  }

  //  array of inward basic blocks
  public WCETBasicBlock[] getInBBSArray(){
    WCETBasicBlock[]  awcbb = new WCETBasicBlock[inbbs.size()];
    int i=0;
    for (Iterator iter = getInbbs().keySet().iterator(); iter.hasNext();) {
      WCETBasicBlock wbb = (WCETBasicBlock) getInbbs().get((Integer) iter.next());
      awcbb[i] = wbb;
      i++;
    }
    return awcbb;
  }

  public int getBlockCycles() {
//    boolean hit = false;
//    for (int i=0;i<bbinvo.size();i++){
//      hit = CacheSimul.get(wcmb.methodbcel);
//    }
      return blockcyc;
  }

  public WCETBasicBlock getLoopdriverwcbb() {
    return loopdriverwcbb;
  }

  // called on the loop driver to get the bb that points to it
  // check the rule
  public WCETBasicBlock getLoopdriverprevwcbb(){
    WCETBasicBlock[] wcbbprev = getInBBSArray();
    int bidin = Integer.MAX_VALUE;
    for (int i=0;i<wcbbprev.length;i++){
      if(bidin > wcbbprev[i].bid)
        bidin = wcbbprev[i].bid;
    }

    return (WCETBasicBlock)wcmb.getBbs(bidin);
  }

  public int getCacheInvokeMiss() {
    if(nodetype != INODE){
      System.out.println("not INODE");
      System.exit(-1);
    }
    return cacheInvokeMiss;
  }

  public int getCacheReturnMiss() {
    if(nodetype != INODE){
      System.out.println("not INODE");
      System.exit(-1);
    }
    return cacheReturnMiss;
  }

}