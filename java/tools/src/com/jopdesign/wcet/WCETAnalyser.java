package com.jopdesign.wcet;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Visitor;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.verifier.structurals.*;

import com.jopdesign.build.AppInfo;
import com.jopdesign.build.TransitiveHull;
import com.jopdesign.tools.JopInstr;

import lpsolve.*;

/**
 * The class is for wcet analysis. The class hierarchy is such 
 * that WCETAnalyzer creates one WCETMethodBlock for each
 * method. The WCETMethodBlock assists WCETAnalyzer in creating the
 * WCETBasicBlock objects for each basic block. Then WCETBasicBlock can be used
 * together with WCETInstruction to calculate the WCET/BCET value for that particular
 * basic block.
 * 
 * Options in the Makefile for the wcet target: You can set "latex" to true, 
 * and WCA will generate "&" characters between columns and "\\" as row terminator. 
 * In Latex do this post-processing: replace ">" with "$>$ and "_" with "\_".
 * A directed graph of the basic blocks can be generated in dot  
 * format by setting the "dot" property to true.
 * 
 * It can generate LPSolve compliant code which can be used to calculate
 * WCET of each method. Enable the "ls" switch in the Makefile.
 *  
 * @author rup, ms
 * @see Section 7.4 and Appendix D in MS thesis
 * @see http://www.graphviz.org
 * @see http://lpsolve.sourceforge.net/5.5/
 */

// History:
// 2006-04-01 rup: Initial version aimed at directed graph of basic blocks
// 2006-04-07 rup: Moved to become a non-Jopizer dependent piece of code
// 2006-04-20 rup: Show both cachehit and cachemiss entries
// 2006-04-27 rup: Show latex tables and load/store info for locals
// 2006-05-04  ms: Split cache miss column 
// 2006-05-07 rup: Output dot graphs 
// 2006-05-25 rup: "Annotations" and lp_solvable wcet output
// 2006-05-30 rup: Exact call graph permutation to allow cache simulation

/**
 * The thing that controls the WCETClassBlock etc.
 */
public class WCETAnalyser{
  
  WCETMethodBlock wcmbapp = null;
  boolean global = true; // controls names of blocks B1 or B1_M1 if true
  String dotf = null;
  // dot property: it will generate dot graphs if true
  public static boolean jline;
  
  // The app method or main if not provided
  public static String appmethod;
  
  public static int idtmp = 0; // counter to make unique ids
  
  public final static String nativeClass = "com.jopdesign.sys.Native";

  PrintWriter out;
  
  PrintWriter dotout;

  /**
   * Loaded classes, type is JavaClass
   */
  List clazzes = new LinkedList();

  org.apache.bcel.util.ClassPath classpath; // = ClassPath.SYSTEM_CLASS_PATH;

  /**
   * The class that contains the main method.
   */
  static String mainClass;
  
  public StringBuffer wcasb = new StringBuffer();
  
  //signaure -> methodbcel
  HashMap mmap;
  
  // methodbcel -> WCETMethodBlock 
  HashMap mtowcmb = new HashMap();  
  
  //method name to id
  HashMap midmap;
  
  //id to wcmb
  HashMap idmmap;
  
  // methodsignature -> wcmb
  HashMap msigtowcmb;
  
  HashMap javaFilePathMap;
  
  ArrayList javaFiles;
  
  public ArrayList wcmbs; // all the wcmbs
  
  static String outFile;
  
  public boolean init = true;
  public boolean analyze = false;

  public WCETAnalyser() {
    
    wcmbs = new ArrayList();
    msigtowcmb = new HashMap();
    classpath = new org.apache.bcel.util.ClassPath(".");
    mmap = new HashMap();
    midmap = new HashMap();
    javaFiles = new ArrayList();
    javaFilePathMap = new HashMap();
  }

  public static void main(String[] args) {
    WCETAnalyser wca = new WCETAnalyser();
    HashSet clsArgs = new HashSet();
    outFile = null;     // wcet/P3+Wcet.txt
    //the tables can be easier to use in latex using this property
    jline = System.getProperty("jline", "false").equals("true");
    appmethod = System.getProperty("appmethod");
    if(appmethod==null){
      System.out.println("appmethod property not set");
      System.exit(-1);
    }
      
    String srcPath = "nodir";
    try {
      if (args.length == 0) {
        System.err
            .println("WCETAnalyser arguments: [-cp classpath] [-o file] class [class]*");
      } else {
        for (int i = 0; i < args.length; i++) {
          if (args[i].equals("-cp")) {
            i++;
            wca.classpath = new org.apache.bcel.util.ClassPath(args[i]);
            continue;
          }
          if (args[i].equals("-o")) {
            i++;
            outFile = args[i];
            continue;
          }
          if (args[i].equals("-sp")) {
            i++;
            srcPath = args[i];
            continue;
          }
          clsArgs.add(args[i]);
          mainClass = args[i].replace('/', '.');
        }
        
        StringTokenizer st = new StringTokenizer(srcPath,";");
        while(st.hasMoreTokens()){
          String srcDir = st.nextToken();//"java/target/src/common";
          File sDir = new File(srcDir);
          if(sDir.isDirectory()){
//System.out.println("srcDir="+srcDir);          
            wca.visitAllFiles(sDir);
          }
        }
//        Iterator ito = wca.javaFilePathMap.values().iterator();
//        while(ito.hasNext()){
//System.out.println(ito.next());          
//        }

//System.out.println("CLASSPATH=" + wca.classpath + "\tmain class="
//            + mainClass);

        wca.out = new PrintWriter(new FileOutputStream(outFile));
        String ds = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\Makefile";
        wca.dotout = new PrintWriter(new FileOutputStream(ds));
        wca.dotout.print("doteps:\n");
        
        wca.load(clsArgs);
        wca.global = false;
        wca.iterate(new SetWCETAnalysis(wca));
        wca.init = false;
        wca.analyze = true;
        wca.iterate(new SetWCETAnalysis(wca));
        
        //wca.out.println("*************APPLICATION WCET="+wca.wcmbapp.wcet+"********************");
        StringBuffer wcasbtemp = new StringBuffer();
        if(wca.analyze){
          wca.global = true;
          wcasbtemp.append(wca.wcmbapp.toLS(true,true, null));
          //wca.out.println(wca.wcmbapp.toLS(true,true, null));
          wcasbtemp.append(wca.toDot());
          if(wca.wcmbapp.wcetlp>=0)
            wcasbtemp.insert(0, "*************APPLICATION WCET="+wca.wcmbapp.wcetlp+"********************\n");
          else
            wcasbtemp.insert(0, "*************APPLICATION WCET=UNBOUNDED (CHECK LOOP BOUNDS I.E.: @WCA loop=XYZ)********************\n");
          wca.out.println(wcasbtemp.toString());
          wca.dotout.print("\tdot -Tps "+wca.dotf+" > "+wca.dotf.substring(0,wca.dotf.length()-4)+".eps\n");
        }
        wca.out.println("*************END APPLICATION WCET*******************");
        wca.out.println(wca.wcasb.toString());
        
        
        //instruction info
        wca.out.println("*****************************************************");
        wca.out.println(WCETInstruction.toWCAString());
        wca.out.println("Note: Remember to keep WCETAnalyzer updated");
        wca.out.println("each time a bytecode implementation is changed.");
        wca.out.close();
        wca.dotout.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  //Java Dev. Almanac
  public void visitAllFiles(File dir) {
    if (dir.isDirectory()) {
        String[] children = dir.list();
        for (int i=0; i<children.length; i++) {
            visitAllFiles(new File(dir, children[i]));
        }
    } else {
      String filePath = dir.getAbsolutePath();
      String fileName = dir.getName();
      if(fileName.endsWith(".java")){
//System.out.println(fileName);
//System.out.println(filePath);
//        String prevPath = (String)javaFilePathMap.get(fileName);
//        if(prevPath != null && !prevPath.equals(filePath)){
//          System.out.println(fileName +" is referring to "+prevPath+" and to "+filePath+". Exiting.");
//          System.exit(1);          
//        }
//        else{
//          javaFilePathMap.put(fileName,filePath);
//        }
        javaFiles.add(filePath);
      }
    }
}

  /**
   * Load all classes and the super classes from the argument list.
   * 
   * @throws IOException
   */
  private void load(Set clsArgs) throws IOException {
    JavaClass[] jcl = new JavaClass[clsArgs.size()];
    Iterator i = clsArgs.iterator();
    for (int nr = 0; i.hasNext(); ++nr) {
      String clname = (String) i.next();
      InputStream is = classpath.getInputStream(clname);
      jcl[nr] = new ClassParser(is, clname).parse();
    }
    TransitiveHull hull = new TransitiveHull(classpath, jcl);
    hull.start();
    System.out.println(Arrays.asList(hull.getClassNames()));
    JavaClass[] jc = hull.getClasses();
    // clazzes contains now the closure of the application
    int mid = 0;
    for (int j = 0; j < jc.length; ++j) {
      // The class Native is NOT used in a JOP application
      if (!jc[j].getClassName().equals(nativeClass)) {
        // ClassInfo cli = new ClassInfo(jc[j]);
//System.out.println("added classname:"+jc[j].getClassName()+" filename:"+jc[j].getFileName()+ " sourcefilename:"+jc[j].getSourceFileName()+" packagename:"+jc[j].getPackageName());        
        clazzes.add(jc[j]);
      }
      //package name and associated sourcefile 
      String pacSrc = jc[j].getPackageName()+"."+jc[j].getSourceFileName();
      boolean fileMatch = false;
      for(int k=0;k<javaFiles.size();k++){
        String orig = (String)javaFiles.get(k);
        String pn = orig;
        pn = pn.replace('/','.');
        pn = pn.replace('\\','.');
//System.out.println("Trying to match:"+pn+ " with: "+pacSrc);        
        int match = pn.lastIndexOf(pacSrc);
        if(match != -1){
          String key = jc[j].getClassName();
//System.out.println("Match! Key :"+key);          
          javaFilePathMap.put(key, orig);
          fileMatch = true;
          break;
        }
      }
      if(!fileMatch){
        System.out.println("No filematch for "+jc[j].getClassName() + " and pacSrc="+pacSrc);
        System.exit(-1);
      }
      Method[] m = jc[j].getMethods();
      for(int ii=0;ii<m.length;ii++){
        String msig = jc[j].getClassName() + "." + m[ii].getName()+m[ii].getSignature();
//System.out.println("m to be put:"+msig);//TODO mig everywhere          
//System.out.println("r: "+m[ii].getReturnType().getSignature());//TODO mig everywhere
        mmap.put(msig,m[ii]);
        midmap.put(new Integer(mid),msig);
        mid++;
      }
    }
  }

  private void iterate(Visitor v) {
    Iterator it = clazzes.iterator();
    while (it.hasNext()) {
      JavaClass clz = (JavaClass) it.next();
      new DescendingVisitor(clz, v).visit();
    }
  }
  
  /**
   * Get a method object from the String id. It is used to find
   * the length of a method when it is invoked from some method.
   * The methodid is created like this: c.getClassName() + "." + m.getName()+m.getSignature();
   * @param methodid
   * @return the method object
   */
  public Method getMethod(String methodid){
    Method m = (Method)mmap.get(methodid);
    return m;
  }
  
  public WCETMethodBlock getWCMB(Method method){
    WCETMethodBlock wcmb = (WCETMethodBlock)mtowcmb.get(method);
//System.out.println("getWCMB:"+wcmb);    
    return wcmb;
  }

  /**
   * Return the opcode for the methodId (applicable to Native methods).
   * @param methodid
   * @return opcode which can be used to call WCETIstruction.getcycles
   */
  public int getNativeOpcode(String methodid){
    int opcode = JopInstr.getNative(methodid);
    if(opcode==-1){
      System.out.println("Did not find native");
      System.exit(-1);
    }
    return opcode;
  }
  
  public String toDot(){
    StringBuffer sb = new StringBuffer();
    sb.append("\n/* App Dot Graph */\n");
    sb.append("digraph G {\n");
    sb.append("size = \"7.27,10.69\"\n");
    
    
    ArrayList appWCMB = new ArrayList();
    appWCMB.add(wcmbapp);
    while(appWCMB.size() > 0){
      WCETMethodBlock wcmb = (WCETMethodBlock)appWCMB.get(0);
      wcmb.link();
      sb.append("subgraph cluster"+wcmb.mid+" {\n");
      sb.append("color = black;\n");
      sb.append(wcmb.toDot(true)+"\n");
      sb.append("label = \""+wcmb.cname+"."+wcmb.name+"\";\n");
      
      sb.append("}\n");
      WCETBasicBlock[] wcbba = wcmb.getBBSArray();
      for(int j=0;j<wcbba.length;j++){
        if(wcbba[j].invowcmb != null){
          sb.append(wcbba[j].toDotFlowEdge(wcbba[j].invowcmb.S));
          sb.append(" [label=\""+wcbba[j].toDotFlowLabel(wcbba[j].invowcmb.S)+"\"];\n");
          sb.append(wcbba[j].invowcmb.T.toDotFlowEdge(wcbba[j]));
          sb.append(" [label=\""+wcbba[j].invowcmb.T.toDotFlowLabel(wcbba[j])+"\"];\n");
          appWCMB.add(wcbba[j].invowcmb);
        }
      }
      appWCMB.remove(0);
    }
    sb.append("}\n");
   
    try {
      dotf = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\App.dot";
      dotf = dotf.replace('<','_');
      dotf = dotf.replace('>','_');
      dotf = dotf.replace('\\','/');
      PrintWriter dotout = new PrintWriter(new FileOutputStream(dotf));
      dotout.write(sb.toString());
      dotout.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
    
    return "";
  }
}

/**
 * It has a HashMap of WCETBasicBlocks. The class have methods that are called
 * from the WCETAnalyzers controlFlowGraph method. It creates the the directed
 * graph of wcbbs.
 */
class WCETMethodBlock {
  
  // list of BBs that are loopcontrollers
  ArrayList loopcontrollers = new ArrayList();
  
  int wcet = -1; // wcet count
  
  final int mid; // a unique id across the app
  
  // Basic Blocks
  TreeMap bbs;

  JavaClass jc;

  Method methodbcel;

  MethodGen mg;

  ControlFlowGraph cfg;
  
  ConstantPoolGen cpg;

  String tostr;

  String signature;

  String name;

  String cname;
  
  WCETAnalyser wca;
  
  String lpf = null;
  
  String dotf = null;
  
  String[] codeLines;

  // directed graph of the basic blocks
  int dg[][];
  
  // method size in 32 bit words
  public int n = -1;
  
  public int wcetlp;
  
  static HashMap wcetvars;
  
  public WCETBasicBlock S;
  
  public WCETBasicBlock T;
  
  public boolean leaf = true; // if no invokes out from anly bb
  
  // create a bb covering the whole method
  // from here on we split it when necessary
  public void init(InstructionHandle stih, InstructionHandle endih) {
    WCETBasicBlock wcbb = new WCETBasicBlock(stih, endih, this, WCETBasicBlock.BNODE);
    S.sucbb = wcbb;
    bbs.put(new Integer(wcbb.getStart()), wcbb);
  }

  /**
   * Instanciated from from <code>SetClassInfo</code>.
   */
  public WCETMethodBlock(Method method, JavaClass jc, WCETAnalyser wca) {
//System.out.println("WCMB CONSTR putting: "+jc.getClassName()+"."+method.getName());
//if(method.getName().equals("printLn"))
//  System.out.println("HELLO");
    wca.mtowcmb.put(method,this);
    this.wca = wca;
    mid = wca.idtmp++;
    
    bbs = new TreeMap();
    this.methodbcel = method;
    name = methodbcel.getName();
    cname = jc.getClassName();
    this.jc = jc;
//System.out.println("sourcefilename: "+ jc.getSourceFileName());

    //method length in words
    if (!method.isAbstract()) {
      n = (method.getCode().getCode().length + 3) / 4;
      String methodId = method.getName() + method.getSignature();
      String classId = jc.getClassName();
      String srcFile = jc.getSourceFileName();
      String filePath = (String)wca.javaFilePathMap.get(classId);
      if(filePath==null){
        codeLines = new String[0];
        System.out.println("Did not find file:"+srcFile+" class:"+ classId+" package:"+jc.getPackageName());
        System.exit(-1);
      }
      try {
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        String str;
        ArrayList al = new ArrayList();
        while ((str = in.readLine()) != null) {
            al.add(str);
        }
        codeLines = (String[])al.toArray(new String[0]);
        in.close();
      } catch (IOException e) {
      }
     
    } else {
      n = -1;
    }
  }

  /**
   * Control flow analysis for one nonabstract-method.
   */
  public void controlFlowGraph() {
    cpg = new ConstantPoolGen(jc.getConstantPool());

    // Some methods overridden (see bottom of this file)
    InstConstraintVisitor icv = new AnInstConstraintVisitor();

    icv.setConstantPoolGen(cpg);

    ExecutionVisitor ev = new ExecutionVisitor();
    ev.setConstantPoolGen(cpg);

    mg = new MethodGen(methodbcel, jc.getClassName(), cpg);
    mg.getInstructionList().setPositions(true);
// String tostr = mg.toString();
//String signature = mg.getSignature();
//String name = mg.getName();
//String cname = mg.getClassName();

    icv.setMethodGen(mg);
    if (!(mg.isAbstract() || mg.isNative())) { // IF mg HAS CODE

      S = new WCETBasicBlock(this,WCETBasicBlock.SNODE);  
      bbs.put(new Integer(Integer.MIN_VALUE), S);
      T = new WCETBasicBlock(this,WCETBasicBlock.TNODE);
     
      // pass 0: Create basic blocks
      InstructionHandle ih = mg.getInstructionList().getStart();
      // wcet startup: create the first full covering bb
      InstructionHandle ihend = mg.getInstructionList().getEnd();
      init(ih, ihend);

      do {
        // create new bb (a)for branch target and (b) for sucessor
        Instruction ins = ih.getInstruction();
        
        if(ih.getInstruction() instanceof InvokeInstruction){
          createBasicBlock(ih);
          createBasicBlock(ih.getNext());
        } else if (ih.getInstruction() instanceof BranchInstruction) {
          InstructionHandle ihtar = ((BranchInstruction) ih.getInstruction())
              .getTarget();
          InstructionHandle ihnext = ih.getNext();
          createBasicBlock(ihtar);
          if (ihnext != null) {
            createBasicBlock(ihnext);
          }
        }
      } while ((ih = ih.getNext()) != null);

      // Pass 1: Set the id of each block
      int bid = 0;
      // it is sorted on the (final) start pos of each block
      for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
        WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
            (Integer) iter.next());
        wbb.calculateWcet();
        wbb.setBid(bid);

        bid++;

        if(wbb.nodetype != WCETBasicBlock.SNODE && wbb.nodetype != WCETBasicBlock.TNODE){
          ih = wbb.getEndih();
          WCETBasicBlock wbbthis = getCoveringBB(ih);
  
          if(ih.getInstruction() instanceof BranchInstruction) {
            // target
            InstructionHandle ihtar = ((BranchInstruction) ih.getInstruction())
                .getTarget();
            WCETBasicBlock wbbtar = getCoveringBB(ihtar);
            // target wbb
            wbbthis.setTarbb(wbbtar);
            // targeter in target
            wbbtar.addTargeter(wbbthis);
  
            // next when the instruction is an if
            // TODO: What about TABLESWITCH and LOOKUPSWITCH
            if (ih.getInstruction() instanceof IfInstruction) {
              InstructionHandle ihnext = ih.getNext();
              if (ihnext != null) {
                WCETBasicBlock wbbnxt = getCoveringBB(ihnext);
                // nextwbb
                wbbthis.setSucbb(wbbnxt);
              }
            }
          } 
          else if(ih.getInstruction() instanceof ReturnInstruction){
            // TODO: set T node here
  if(T==null)
    System.out.println("T=null");
  if(wbbthis==null)
    System.out.println("wbbthis=null");          
  
            wbbthis.sucbb = T;
            T.addTargeter(wbbthis);
          }
          else { // set the successor
            InstructionHandle ihnext = ih.getNext();
  
            if (ihnext != null) {
              WCETBasicBlock wbbnxt = getCoveringBB(ihnext);
              // nextwbb
              wbbthis.setSucbb(wbbnxt);
            }
          }
        }
      }      
      
      bbs.put(new Integer(Integer.MAX_VALUE), T);

      T.bid = bid;

      TreeMap newbbs = new TreeMap();
      
      for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
        WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
            (Integer) iter.next());
        newbbs.put(new Integer(wbb.bid),wbb);
//System.out.println("CFG putting "+wbb.bid+" in newbbs. Nodetype:"+wbb.nodetype);
      }
      bbs = newbbs;
      //bbs.put(new Integer(T.bid),T);
    }
  }
  
  public void link (){
    // set up the  loop controllers
    //   WCMB: Arraylist of <WCBB> loopcontrollers
    //     WCBB: ArrayList of <ArrayList> of loopchains
    //       loopchain: ArrayList <WCBB> of WCBB in chain
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      if(wcbb.loopcontroller){
        wcbb.createLoopChains();
        loopcontrollers.add(wcbb);
      }
    }
    
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      
      // hook the called method to the outgoing node
      if(wcbb.nodetype == WCETBasicBlock.INODE){

//if(wca.getMethod(wcbb.bbinvo)==null){
//  System.out.println("wca.getMethod(wcbb.bbinvo) == null");  
//}
//else
//  System.out.println(wca.getMethod(wcbb.bbinvo).getName());
//if(wca.getWCMB(wca.getMethod(wcbb.bbinvo))==null){
//  System.out.println("wca.getWCMB(wca.getMethod(wcbb.bbinvo)) == null");
//}
        wcbb.invowcmb = wca.getWCMB(wca.getMethod(wcbb.bbinvo));
        if(wcbb.invowcmb==null)
          System.out.println("Could not resolve "+wcbb.bbinvo+" for linking in "+wcbb.getIDS());
        
        leaf = false;
        
        // backtrack
/*        ArrayList wcbbs = new ArrayList();
        wcbbs.add(wcbb);
        WCETBasicBlock lcwcbb = null;
        while(wcbbs.size()>0){
          WCETBasicBlock curwcbb = (WCETBasicBlock)wcbbs.get(0);
          WCETBasicBlock[] tarbb = curwcbb.getInBBSArray();
          for (int i=0;i<tarbb.length;i++){
            if(tarbb[i].loopcontroller){ 
              lcwcbb = tarbb[i];
              wcbbs.clear();
              break;
            }
            if(tarbb[i].nodetype != WCETBasicBlock.SNODE || tarbb[i].nodetype != WCETBasicBlock.TNODE){
              wcbbs.add(tarbb[i]);
            }
          }
        }*/
        
      }
    }
/*//TODO: discuss with ms    
// if there are any path that leads back to the INODE that has only one loopcontroller    
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      if(wcbb.nodetype == WCETBasicBlock.INODE && wca.global){
        if(wcbb.invowcmb.leaf){ 
          
System.out.println("invowcmb from "+cname+"."+name+"("+wcbb.getIDS()+")"+":"+wcbb.invowcmb.cname+"."+wcbb.invowcmb.name+" is a leaf");          
          //  we have a candidate  
          //  dismiss if there is an invoblock in an inner loop 
          ArrayList okl = new ArrayList();  //  ok
          ArrayList pl = new ArrayList(); //  pending
          ArrayList link = new ArrayList();
          link.add(wcbb);
          pl.add(link);
          while(pl.size()>0){
            link = (ArrayList)pl.get(0);
System.out.println("\na:"+ WU.printChain(link));          
            WCETBasicBlock wcbbinvo = (WCETBasicBlock)link.get(0);
            WCETBasicBlock wcbblast = (WCETBasicBlock)link.get(link.size()-1);
            
            //first check the chain
            if(wcbbnext == wcbbinvo){ 
              System.out.println("removing  pl(0). pll.size:"+pl.size());                
                              okl.add(pl.remove(0)); // the max 1 candidate chain saved
              System.out.println("removed  pl(0). pll.size:"+pl.size());
              if(pl.size()>0)
                System.out.println("pl(0):"+WU.printChain((ArrayList)pl.get(0)));
                            } 
                            else if(link.contains(wcbbnext)){
                              pl.remove(0);
                            }
                            else
                              link.add(wcbbnext);
          }}}            
            
            //then advance it
            
            if(wcbblast.sucbb != null && wcbblast.tarbb != null){
              ArrayList linkclone = (ArrayList)link.clone();
              pl.add(linkclone);
            }
            WCETBasicBlock wcbbnext = null;
            
            if(wcbblast.sucbb != null){
              wcbbnext = wcbblast.sucbb;             
            }else if(wcbblast.tarbb != null){
              wcbbnext = wcbblast.tarbb;
            }else{ // T node
System.out.println("T:"+ WU.printChain(link));              
              pl.remove(0);
            }
System.out.println("b:"+ WU.printChain(link));  
System.out.println("wcbbnext:"+wcbbnext.getIDS());
System.out.println("wcbbinvo:"+wcbbinvo.getIDS());
            if(wcbbnext != null){
              if(wcbbnext == wcbbinvo){ 
System.out.println("removing  pl(0). pll.size:"+pl.size());                
                okl.add(pl.remove(0)); // the max 1 candidate chain saved
System.out.println("removed  pl(0). pll.size:"+pl.size());
if(pl.size()>0)
  System.out.println("pl(0):"+WU.printChain((ArrayList)pl.get(0)));
              } 
              else if(link.contains(wcbbnext)){
                pl.remove(0);
              }
              else
                link.add(wcbbnext);
              
//              else if(wcbbnext.loopcontroller){ // check that it is the first loopcontroller
//                  boolean onlylc = true;
//                  for (int i=1;i<link.size();i++){
//                    if(((WCETBasicBlock)link.get(i)).loopcontroller){
//                      onlylc = false;
//                      break;
//                    }
//                  }
//                  if(onlylc){
//                    link.add(wcbbnext);
//                    wcbbinvo.innerlc = wcbbnext.bid;
//                  }
//                  else
//                    pl.remove(0);
//              } else if(wcbbnext.nodetype == WCETBasicBlock.INODE){
//                if(wcbbnext.invowcmb == wcbbinvo.invowcmb) 
//                  link.add(wcbbnext); // ok to invoke the same method (one cache miss is false, however)
//                else
//                  pl.remove(0);
//              }
//              else{
//                link.add(wcbbnext);
//              }         
            }
          }
          
          System.out.println("#ok chains:"+okl.size());
          for (int i=0;i<okl.size();i++){
            link = (ArrayList)okl.get(i);
            System.out.println(WU.printChain(link));
          }
          
          while(okl.size()>0){
            // conservative: require that it is the only invo block for all loops
            // ok to have it multiple times (just a more conservative estimate)
            link = (ArrayList)okl.get(0);
            WCETBasicBlock wcbbinvo = (WCETBasicBlock)link.get(0);
            wcbbinvo.innerinode = true;
            for (int i=1;i<link.size()-1;i++){
              WCETBasicBlock wcbbtest = (WCETBasicBlock)link.get(i);
              if(wcbbtest.nodetype == WCETBasicBlock.INODE && wcbbtest != wcbbinvo)
                wcbbinvo.innerinode = false;
            }           
          }
        } else{
          wcbb.innerinode = false; 
        }
      }
    }
    */
  }

  public ControlFlowGraph getControlFlowGraph() {
    return cfg;
  }

  public MethodGen getMethodGen() {
    return mg;
  }
  
  /**
   * Find a local variable based on an entry in the LocalVariableTable attribute.
   * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#5956
   * @param index
   * @param pc
   * @return local variable type and name or "NA"
   */
  public String getLocalVarName(int index, int pc){
//System.out.println("getLocalVarName: index:"+index+" pc:"+pc+" info:"+mg.getClassName()+"."+mg.getName());
    LocalVariableTable lvt = methodbcel.getLocalVariableTable();
    String lvName = "";
    boolean match = false;
    if(lvt!=null){
      LocalVariable[] lva = lvt.getLocalVariableTable(); 

//System.out.println("lva.length:"+lva.length);   
     for(int i=0;i<lva.length;i++){
       LocalVariable lv = lva[i]; 
//System.out.println("lv["+i+"]: index:"+lv.getIndex()+" name:"+lv.getName()+" pcstart:"+pc+" length:"+lv.getLength());     
       if(lv.getIndex()==index){
//System.out.println("index match");       
         if(pc>=lv.getStartPC()){
//System.out.println("startpc match");
           if(pc<=lv.getStartPC()+lv.getLength()){
//System.out.println("endpc match");            
             lvName = lv.getSignature() +":"+lv.getName();
             if(match){
               System.out.println("Only one match pr. local variable table is possible");
               System.exit(-1);
             }
             match = true;
             //break; //safety check when commented out, but slower
           }
         }
       }
    }
    }
    return lvName;
  }

  /**
   * Get the bb that currently covers the bytecode at the position. The design
   * is such that some bb will always cover a bytecode. It may the the same bb
   * that is returned if the branch points back (direct loop).
   * 
   * @param pos
   * @return covering bb
   */
  public WCETBasicBlock getCoveringBB(InstructionHandle ih) {
    // if cov bb starts on pos then done otherwise run throug all keys
    WCETBasicBlock covbb = (WCETBasicBlock) bbs.get(new Integer(ih
        .getPosition()));
    if (covbb == null) {
      Iterator it = bbs.keySet().iterator();
      // find the cov. bb
      int bbpos = 0;
      while (it.hasNext()) {
        int apos = ((Integer) it.next()).intValue();
        if (apos < ih.getPosition() && apos > bbpos) {
          bbpos = apos;
        }
      }
      covbb = (WCETBasicBlock) bbs.get(new Integer(bbpos));
    }
    return covbb;
  }

  /**
   * Create a new wcbb if none of the existing wcbbs are starting on that
   * position. Call this method twice when you encounter a branch type byte
   * code.
   * 
   * @param start
   *          the position to check for
   * @return true if a wcbb was created
   */
  public boolean createBasicBlock(InstructionHandle stih) {
    boolean res;
    // get the covering bb
    WCETBasicBlock covwcbb = getCoveringBB(stih);
    if (covwcbb.getStart() == stih.getPosition()) {
      // already a bb on start pos
      res = false;
    } else // create one by splitting the covering bb
    {
      WCETBasicBlock wcbb = covwcbb.split(stih);
      if((stih.getInstruction() instanceof InvokeInstruction) &&
          !(((InvokeInstruction)stih.getInstruction()).getClassName(getCpg())).equals(wca.nativeClass)){
//System.out.println("inode:"+((InvokeInstruction)stih.getInstruction()).getClassName(getCpg()));       
        wcbb.nodetype = WCETBasicBlock.INODE; 
        leaf = false;
      }
      // save the new bb in the hash map
      if (bbs.put(new Integer(stih.getPosition()), wcbb) != null) {
        System.err.println("The starting pos should be unique.");
        System.exit(-1);
      }
      res = true;
    }
    return res;
  }
  
  public void createBasicBlock(int type){
    if(type == WCETBasicBlock.SNODE){
      S = new WCETBasicBlock(this,WCETBasicBlock.SNODE);  
      bbs.put(new Integer(Integer.MIN_VALUE), S);
    }
    if(type == WCETBasicBlock.TNODE){
      T = new WCETBasicBlock(this,WCETBasicBlock.TNODE);
      bbs.put(new Integer(Integer.MAX_VALUE), T);
    }
  }

  /**
   * It sorts the basic blocks and creates the directed graph.
   */
  public void directedGraph() {
    // now create the directed graph
    dg = new int[bbs.size()][bbs.size()];
    WCETBasicBlock.bba = new WCETBasicBlock[bbs.size()+2];//TODO
    LineNumberTable lnt = methodbcel.getLineNumberTable();
    WCETBasicBlock pbb = null;
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      if(pbb!=null)
        wcbb.prevbb = pbb;
      pbb = wcbb;
      if(wcbb.nodetype!=WCETBasicBlock.SNODE && wcbb.nodetype!=WCETBasicBlock.TNODE)
        wcbb.line = lnt.getSourceLine(wcbb.endih.getPosition());
      else 
        wcbb.line = -1;
      WCETBasicBlock tarwcbb = wcbb.getTarbb();
      int bid = wcbb.getBid();
//System.out.println(bid + ":"+WCETBasicBlock.bba.length);      
      WCETBasicBlock.bba[bid] = wcbb;
      if (tarwcbb != null) {
        int tarbbid = tarwcbb.getBid();
        tarwcbb.addTargeter(wcbb);
        dg[bid][tarbbid]++;
      }
      WCETBasicBlock sucbb = wcbb.getSucbb();
      if (sucbb != null){// && sucbb.nodetype != WCETBasicBlock.TNODE) {
        int sucid = sucbb.getBid();
        sucbb.addTargeter(wcbb);
        dg[bid][sucid]++;
      }
    }
    
    HashSet lines = new HashSet();
    // find loopdrivers/loopcontrollers
//System.out.println("\nmethod:"+method.getClass().getName()+"."+method.getName());    
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
System.out.println("first wcbb:"+wcbb.getIDS());    
if(wcbb.line >=0)
  System.out.println("codeline:"+codeLines[wcbb.line-1]);
//System.out.println("outer loop wcbb.id:"+wcbb.id);      
      // identify loop controller candidate
      if(((wcbb.sucbb != null && wcbb.tarbb != null)
          && (!wcbb.loopdriver || !wcbb.loopcontroller))
          && !lines.contains(new Integer(wcbb.line))){
        HashMap wcaA = WU.wcaA(codeLines[wcbb.line-1]);
        if(wcaA != null){
          if(wcaA.get("loop") != null){ // wcbb is now loopdriver
//System.out.println("loopdriver id:"+wcbb.id);           
System.out.println("LOOPDRIVER:"+wcbb.getIDS());            
            // find loopcontroller
            boolean set = false;
            WCETBasicBlock wcbbhit = wcbb;
            for (Iterator lciter = bbs.keySet().iterator(); lciter.hasNext();) {
              Integer lckeyInt = (Integer) lciter.next();
              WCETBasicBlock lcwcbb = (WCETBasicBlock) bbs.get(lckeyInt);
              if(set){
                if(lcwcbb.sucbb != null && lcwcbb.tarbb != null){
                  if(lcwcbb.line == wcbbhit.line){
//System.out.println("hit candidate:"+lcwcbb.id);                    
                    wcbbhit = lcwcbb;
                  } 
                }
              }
              if(lcwcbb==wcbb){
//System.out.println("set on id:"+lcwcbb.id);                
                set = true;
              }                
            }
//System.out.println("loop controller hit:"+wcbbhit.id);            
            wcbb.loopdriver = true;
            wcbb.loopcontroller = false;
            wcbb.loop = Integer.parseInt((String)wcaA.get("loop"));
            wcbbhit.loopcontroller = true;
            wcbbhit.loopdriver = false;
            wcbbhit.loopid = wcbb.bid;
            wcbbhit.loop = Integer.parseInt((String)wcaA.get("loop"));
            wcbbhit.loopdriverwcbb = wcbb;
            lines.add(new Integer(wcbbhit.line));
//            if(wcaA.get("innerloop") != null){
//              if(((String)wcaA.get("innerloop")).equals("true")){
//System.out.println(wcbb.getIDS() +" is an inner loop controller");                
//                wcbb.innerloop = true;
//              }
//            }
          }
        }
      }
          
//      if(wcbb.loopcontroller){
//        HashMap tinbbs = wcbb.getInbbs();
//        if(wcbb.bid > 0 && tinbbs.size()!=2){
////          System.out.println("error in loopcontrol:"+wcbb.id);
////          System.out.println("tinbbs.size:"+tinbbs.size());
////          System.exit(-1);
//        }
//      }
    }
  }

  /**
   * Converts the WCETMethodBasicBlock to a String.
   * 
   * @return string representation of the MehtodBasicBlock
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
     
    sb.append("******************************************************************************\n");
        sb.append("WCET info for:"+jc.getClassName() + "." + methodbcel.getName()
        + methodbcel.getSignature()+"\n\n");

    // directed graph
    sb.append("Directed graph of basic blocks(row->column):\n");
    StringBuffer top = new StringBuffer();
    top.append(WU.prepad("",4));

    for (int i = 0; i < dg.length; i++) {
      if(i<dg.length-1)
        top.append(WU.postpad(getBbs(i).getIDS(),4));
      else
        top.append(WU.postpad(getBbs(i).getIDS(),4));
    }
    top.append("\n");

    for (int i = 0; i < top.length() - 3; i++) {
      sb.append("=");
    }
    sb.append("\n" + top.toString());

    for (int i = 0; i < dg.length; i++) {
      sb.append(WU.postpad(getBbs(i).getIDS(),3));

      for (int j = 0; j < dg.length; j++) {
        if (dg[i][j] == 0)
          sb.append(" ."); // a space does not clutter it as much as a zero
        else
          sb.append(" " + dg[i][j]);

        if(j<dg.length-1)
          sb.append(WU.postpad("",2));
        else
          sb.append(WU.postpad("",2));
      }
      sb.append("\n");
    }
    sb.append(WU.repeat("=",top.length() - 3));
    sb.append("\n");
    

    // bytecode listing
    sb.append("\nTable of basic blocks' and instructions\n");
    sb.append("=========================================================================\n");
    sb.append("Block Addr.  Bytecode                Cycles    Cache miss     Misc. info\n");
    sb.append("             [opcode]                        invoke  return\n");
    sb.append("-------------------------------------------------------------------------\n");
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      sb.append(wcbb.toCodeString());
    }
    sb.append("=========================================================================\n");
    sb.append("Info: n="+n+" b="+WCETInstruction.calculateB(n)+" a="+WCETInstruction.a+" r="+WCETInstruction.r+" w="+WCETInstruction.w+"\n");
    sb.append("\n"); 
//    if(wca.ls){
//      sb.append(toLS(true, true, ""));
//      WCETBasicBlock.linkbb(WCETBasicBlock.bba[0]);
//      WCETBasicBlock.bbe();
//      sb.append("\n"+toLinkBBS());
//    }
    sb.append(toLS(false,true, null));
    
    sb.append(toDot(false));
   
    return sb.toString();
  }

  public String toDot(boolean global) {
    // global if true then dot is appwide   
    StringBuffer sb = new StringBuffer();
    // dot graph
    // use: dot -Tps graph.dot -o graph.ps
    boolean labels = true;

    sb.append("\n/*"+ jc.getClassName() + "." + methodbcel.getName()
        + methodbcel.getSignature()+"*/\n");
    if(!global){
      sb.append("digraph G {\n");
      sb.append("size = \"10,7.5\"\n");
    }

    for (int i = 0; i < dg.length; i++) {
      for (int j = 0; j < dg.length; j++) {
        if(dg[i][j]>0){
       
          sb.append("\t"+getBbs(i).toDotFlowEdge(getBbs(j)));
          if(labels){
            //sb.append(" [label=\""+dg[i][j]+"\"");
            String edge = getBbs(i).toDotFlowLabel(getBbs(j));

            if(wcetvars.get(edge)!=null){
              int edgeval = Integer.parseInt((String)wcetvars.get(edge));
              if(edgeval>0)
                sb.append(" [label=\""+getBbs(i).toDotFlowLabel(getBbs(j))+"="+edgeval+"\"");
              else
                sb.append(" [style=dashed,label=\""+getBbs(i).toDotFlowLabel(getBbs(j))+"="+edgeval+"\"");
            }
            else
              sb.append(" [label=\""+getBbs(i).toDotFlowLabel(getBbs(j))+"=?\"");
            
              //sb.append(",labelfloat=true");
            sb.append("]");
          }
          sb.append(";\n");
        }
      }
    }
   
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      int id = wcbb.getBid();
      if(wcbb.nodetype != WCETBasicBlock.SNODE && wcbb.nodetype != WCETBasicBlock.TNODE)
        sb.append("\t"+wcbb.getIDS()+" [label=\""+wcbb.getIDS()+"\\n"+wcbb.wcetHit+"\"];\n");
      else
        sb.append("\t"+wcbb.getIDS()+";\n");
    }
    if(!global){
      sb.append("}\n");
    }
    
    if(!global){
      try {
        dotf = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\"+jc.getClassName()+"."+methodbcel.getName()+".dot";
        dotf = dotf.replace('<','_');
        dotf = dotf.replace('>','_');
        dotf = dotf.replace('\\','/');
        PrintWriter dotout = new PrintWriter(new FileOutputStream(dotf));
        dotout.write(sb.toString());
        dotout.close();
      } catch (FileNotFoundException e1) {
        e1.printStackTrace();
      }
    }
    
    return sb.toString();    

  }
  //TODO: loop follows loop controller?
  /**
   * @param global follow the invokes
   * @param term terminate with s=1, t=1
   * @param invowcbb the invoking wcbb or null
   */
  public String toLS(boolean global, boolean term, WCETBasicBlock invowcbb){
    
    StringBuffer ls = new StringBuffer();
    StringBuffer lsinvo = new StringBuffer();
    StringBuffer lsobj = new StringBuffer();

    ls.append("/* WCA flow constraints */\n");

    lsobj.append(toLSO());
    
    WCETBasicBlock wcbb = null;
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      wcbb = (WCETBasicBlock) bbs.get(keyInt);
      //S
      if(wcbb.nodetype==WCETBasicBlock.SNODE)
        if(invowcbb != null)
          ls.append(wcbb.toLSS(invowcbb));
        else 
          ls.append(wcbb.toLSS(null));
      else if(wcbb.nodetype==WCETBasicBlock.BNODE || wcbb.nodetype==WCETBasicBlock.INODE){
        ls.append(wcbb.toLSFlow());
        if(wcbb.loopcontroller)
          ls.append(wcbb.toLSLoop());
      } else if(wcbb.nodetype==WCETBasicBlock.TNODE){
        if(invowcbb != null)
          ls.append(wcbb.toLST(invowcbb));
        else
          ls.append(wcbb.toLST(null));
      }
      
      if(wcbb.nodetype==WCETBasicBlock.INODE && global){
        ls.append(wcbb.toLSInvo());
        lsinvo.append(wcbb.invowcmb.toLS(global,false, wcbb));
        lsobj.append(" "+wcbb.invowcmb.toLSO());
      }
    }
      
    ls.append("/* WCA flow to cycle count */\n");
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      wcbb = (WCETBasicBlock) bbs.get(keyInt);
      if(wcbb.nodetype==WCETBasicBlock.BNODE || wcbb.nodetype==WCETBasicBlock.INODE){
        ls.append(wcbb.toLSCycles());
      }
    }
    
    ls.append("/* Invocation(s) */\n");
    ls.append(lsinvo.toString());
    
    if(term){ // once
      //String lso = obs.toString();
      StringBuffer lso = new StringBuffer();
      lso.append("/***WCET calculation source***/\n");
      lso.append("/* WCA WCET objective: "+jc.getClassName() + "." + methodbcel.getName()+ " */\n");
      lso.append("max: "+lsobj.toString()+";\n");
      ls.insert(0, lso.toString());

      try {
        lpf = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\"+jc.getClassName()+"."+methodbcel.getName()+".lp";
        lpf = lpf.replace('<','_');
        lpf = lpf.replace('>','_');
  //System.out.println("about to write:"+lpf);
        PrintWriter lsout = new PrintWriter(new FileOutputStream(lpf));
        lsout.write(ls.toString());
//System.out.println("LS to be solved:"+ls.toString());        
        lsout.close();
      } catch (FileNotFoundException e1) {
        e1.printStackTrace();
      }
  
      try {
        wcetvars = new HashMap();
        LpSolve problem = LpSolve.readLp(lpf, LpSolve.NORMAL, jc.getClassName()+"."+methodbcel.getName());
        problem.setOutputfile(lpf+".output.txt");
        problem.solve();
        problem.setOutputfile(lpf+".solved.txt");
        problem.printObjective();
        problem.printSolution(1);
        wcetlp = (int)problem.getObjective();
        try {
          BufferedReader in = new BufferedReader(new FileReader(lpf+".solved.txt"));
          String str;
          while ((str = in.readLine()) != null) {
            ls.append(str+"\n");
            StringTokenizer st = new StringTokenizer(str);
            if(st.countTokens()==2){
              String st1 = st.nextToken();
              String st2 = st.nextToken();
              wcetvars.put(st1,st2);
//System.out.println("putting:"+st1+","+st2);              
            }
          }
          in.close();
        } catch (IOException e) {
        }
      } catch (LpSolveException e) {
        System.out.println("LP not solvable for: "+jc.getClassName()+"."+methodbcel.getName());
        //e.printStackTrace();
      } 
    }
    
    return ls.toString();
  }
  
  // tS tB1 etc.
  public String toLSO(){
    StringBuffer lso = new StringBuffer();
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      lso.append(wcbb.toLSObj());
      
      if(iter.hasNext())
        lso.append(" ");
    }
    return lso.toString();
  }
  
  public String toLinkBBS(){
    StringBuffer lsb = new StringBuffer();
    int l[] = (int[])WCETBasicBlock.bbl.get(WCETBasicBlock.bcetid);
    lsb.append("BBs bcet link:");
    for (int i=0;i<l.length;i++){
      if(l[i]!=-1){
        lsb.append(l[i]+"->");
      } else
        break;
    }
    lsb.append("T\n");
    lsb.append("BBs bcet:"+WCETBasicBlock.bbe[WCETBasicBlock.bcetid]+"\n");

    l = (int[])WCETBasicBlock.bbl.get(WCETBasicBlock.wcetid);
    lsb.append("BBs wcet link:");
    for (int i=0;i<l.length;i++){
      if(l[i]!=-1){
      lsb.append(l[i]+"->");
      } else
        break;
    }
    lsb.append("T\n");
    lsb.append("BBs wcet:"+WCETBasicBlock.bbe[WCETBasicBlock.wcetid]+"\n");
    //lsb.append("BBs bcet:"+WCETBasicBlock.bbe[WCETBasicBlock.bcetid]+"\n");
    return lsb.toString();
  }

  public TreeMap getBbs() {
    return bbs;
  }
  
  public WCETBasicBlock[] getBBSArray(){
    WCETBasicBlock[]  awcbb = new WCETBasicBlock[bbs.size()];
    int i=0;
    for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
      WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get((Integer) iter.next());
      awcbb[i] = wbb;
      i++;
    }
    return awcbb;
  }
  
  public WCETBasicBlock getBbs(int bid){
    WCETBasicBlock wbb = null;
    for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
      wbb = (WCETBasicBlock) getBbs().get((Integer) iter.next());
      if(wbb.bid == bid){
        break;
      }
      else
        wbb = null;
    }
    return wbb;
  }

  public int getN() {
    return n;
  }

  public ConstantPoolGen getCpg() {
    return cpg;
  }
  
}

/**
 * Basic block of byte codes
 */
class WCETBasicBlock {

  //public String ids; // id like "S", "T" or "B1"
  
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
  ArrayList loopchains; // chains of BB that loop back to the lc
  
  // id of the bb
  int bid = -1;
  
  int line = -1;
  
  int loopid = -1;
  boolean loopdriver = false;
  boolean loopcontroller = false;
  WCETBasicBlock loopdriverwcbb = null; // is set for loopcontrollers 
  boolean loopreturn = false;
  
  // loop target
  int loop = -1;
  int looptargetid = -1;
  

  // the reason why we are doing this...
  int wcetHit;
  int wcetMiss;
  int blockcyc;


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
    System.out.println("entering createloopchains");
    if(!loopcontroller){
      System.out.println("not a loop controler");
      System.exit(-1);
    }
    else{
      System.out.println("loopcontroller:"+getIDS());
      System.out.println("loopdriver:"+loopdriverwcbb.getIDS());
    }
    
      
    
    innerloop = true;
    loopchains = new ArrayList();
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
System.out.println("if innerloop");      
System.out.println("loopchains:\n"+WU.printChains(loopchains));
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
System.out.println("invowcmb.size():"+invowcmb.size());      
      if(invowcmb.size()==1){
System.out.println("invoblocks.size():"+invoblocks.size());        
        for (int i=0;i<invoblocks.size();i++){
          WCETBasicBlock invowcbb = (WCETBasicBlock)invoblocks.get(i);
          invowcbb.innerloop = true;
          invowcbb.loopdriverwcbb = loopdriverwcbb;
          invowcbb.loop = loop;
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
      ls.append(" = ");
      if(sucbb != null){
        //if(wcbb.sucbb.nodetype != WCETBasicBlock.TNODE){  
          ls.append("f"+getIDS()+"_"+sucbb.getIDS());
        //}
      }
      if(sucbb != null && tarbb!=null)
        ls.append(" + ");
      if(tarbb!=null)
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
      if(wcbb == null)
        ls.append(getIDS()+": 1 = f"+getIDS()+"_"+sucbb.getIDS()+"; // S flow\n");
      else{ // connect the two cache paths
        ls.append(getIDS()+": fch"+ wcbb.getIDS()+"_"+ getIDS() + "+ fcm"+ wcbb.getIDS()+"_"+ getIDS()+" = f"+getIDS()+"_"+sucbb.getIDS()+"; // S flow\n");
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
      if(wcbb == null)
        ls.append(" = 1; // T flow\n");
      else
        ls.append(" = f"+getIDS()+"_"+wcbb.getIDS()+";// T interconnect flow\n");
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

      ls.append("/* Invoking "+bbinvo+" id:"+invowcmb.S.getIDS()+"*/\n");
      // to invo S
      String invodriver = getIDS()+"_"+sucbb.getIDS(); 
      String invoS = getIDS()+"_"+invowcmb.S.getIDS();
      String invoT = invowcmb.T.getIDS()+"_"+getIDS();
      ls.append(getIDS()+"_S: fcm"+ invoS+" + fch"+ invoS+" = f"+invodriver+"; //cache S paths\n");
      //ls.append(getIDS()+"ch: fch"+ invoS+" = f"+invodriver+"; //cache hit S path\n");
      ls.append(getIDS()+ "_T: f" + invodriver+" = f" +invoT+"; // invo T return path \n");
      
      // flow constrain the cache paths
      if(innerloop){
        ls.append("fcm"+ invoS + " = f"+loopdriverwcbb.getIDS()+"_"+loopdriverwcbb.sucbb.getIDS()+"; // cache misses driven by loopdriver\n");
      } else { // cache misses
        ls.append("fch"+ invoS + " = 0; // no cache hits (because not innerloop)\n");
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
    if(loopcontroller){
      if(wcmb.wca.global)
        ls.append("LC_"+getIDS()+": f"+ getIDS()+"_"+sucbb.getIDS()+" <= "+loop+" fB"+(loopid-1)+"_M"+wcmb.mid+"_B"+loopid+"_M"+wcmb.mid+";\n");
      else
        ls.append("LC_"+getIDS()+": f"+ getIDS()+"_"+sucbb.getIDS()+" <= "+loop+" fB"+(loopid-1)+"_B"+loopid+";\n");
//      wcbb.sc = wcbb.loop;
//      wcbb.scsid = wcbb.loopid-1;
//      wcbb.sctid = wcbb.loopid;
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
      sb.append(WU.postpad(getIDS()+"\n",6)); // see the BBs that point to this BB
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
      sb.append(WU.postpad(getIDS()+tStr+"\n",6)); // see the BBs that point to this BB
    }
    else{
      InstructionHandle ih = stih;
      blockcyc = 0;
  
      LineNumberTable lnt = wcmb.methodbcel.getLineNumberTable();
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
  
  sb.append(WU.postpad(getIDS()+tStr,6)); // see the BBs that point to this BB
  //        sb.append(WU.postpad("B" + id,6));
        } else {
          sb.append("      ");
        }
        
        // addr (len 6)
        sb.append(WU.postpad(ih.getPosition() + ":",6));
  
        if(!WCETInstruction.wcetAvailable(ih.getInstruction().getOpcode()))
          sb.append("*");
        else 
          sb.append(" ");
        
        // bytecode (len 22)
        StringBuffer ihs = new StringBuffer(ih.getInstruction().getName() + "["
            + ih.getInstruction().getOpcode() + "]");
  
        if (ih.getInstruction() instanceof BranchInstruction) {
          // target
          InstructionHandle ihtar = ((BranchInstruction) ih.getInstruction())
              .getTarget();
          int tarpos = ihtar.getPosition();
          ihs.append("->" + tarpos + ":");
        }
  
        sb.append(WU.postpad(ihs.toString(),20));
        
        String invoStr = "";
        
        //invoke instructions
        if(ih.getInstruction() instanceof InvokeInstruction){
          String methodid = ((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())
          +"."
          +((InvokeInstruction)ih.getInstruction()).getMethodName(wcmb.getCpg())
          +((InvokeInstruction)ih.getInstruction()).getSignature(wcmb.getCpg());
          String retsig = ((InvokeInstruction)ih.getInstruction()).getReturnType(wcmb.getCpg()).getSignature(); 
  
          //signature Java Type, Z boolean, B byte, C char, S short, I int
          //J long, F float, D double, L fully-qualified-class, [ type type[] 
          bbinvo = methodid;
          Method m = wcmb.wca.getMethod(methodid);
          if(methodid.startsWith("com.jopdesign.sys.Native")){
            int opcode = wcmb.wca.getNativeOpcode(m.getName());//methodid);
            if(opcode == -1){
              sb.append(WU.prepad("*to check",10));
              invoStr = methodid + " did not find mapping";
            }else
            {
              int cycles = WCETInstruction.getCycles(opcode,false,0);
              // no difference as cache is not involved
              blockcyc += cycles;
              sb.append(WU.prepad(Integer.toString(cycles),10));
              sb.append("   ");
              sb.append("                ");
              invoStr = methodid;
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
            int cacheInvokeMiss = (invokemiss-invokehit);
            int cacheReturnMiss = (retmiss-rethit);
            // that's the invoke instruction
            blockcyc += invokehit;
            // cache influence now as always miss up
            // we hve solved it with extra blocks
            blockcyc += cacheInvokeMiss;
            blockcyc += cacheReturnMiss;
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
            invoStr = methodid+", invoke(n="+invon+"):"+invokehit+"/"+invokemiss+" return(n="+wcmb.getN()+"):"+rethit+"/"+retmiss;
            if((((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())).equals(wcmb.wca.nativeClass)){
              invoStr = methodid;
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
  
        // misc.
        
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
          String fieStrName = ((FieldInstruction)ih.getInstruction()).getFieldName(wcmb.cpg);
          sb.append(fieStrName);
        }
  
       //fetch local variable name and type from class file
        if(ih.getInstruction() instanceof LocalVariableInstruction){
          if(ih.getInstruction() instanceof StoreInstruction){
            StoreInstruction si = (StoreInstruction)ih.getInstruction();
            //add instruction len to pos to peek into localvariable table
            String siStr = wcmb.getLocalVarName(si.getIndex(),ih.getPosition()+ih.getInstruction().getLength());
            if(siStr.length()>0)
              sb.append("->"+siStr+" ");
          } else{ //load or iinc        
            LocalVariableInstruction lvi = (LocalVariableInstruction)ih.getInstruction();
            String lvStr = wcmb.getLocalVarName(lvi.getIndex(),ih.getPosition());
            if(lvStr.length()>0)
              sb.append(lvStr+" ");
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
    
    if(nodetype == SNODE)
      sbIDS.append("S");
    if(nodetype == BNODE)
      sbIDS.append("B"+bid);
    if(nodetype == INODE)
      sbIDS.append("I"+bid);
    if(nodetype == TNODE)
      sbIDS.append("T");
    
    if(wcmb.wca.global)
      sbIDS.append("_M"+wcmb.mid);
    
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

}

// BCEL overrides

/**
 * Extends org.apache.bcel.verifier.structurals. Frame just to get access to the
 * _this field, which the the operandWalker method in MethodInfo uses.
 */
class FrameFrame extends Frame {

  public FrameFrame(int maxLocals, int maxStack) {
    super(maxLocals, maxStack);
  }

  public FrameFrame(LocalVariables locals, OperandStack stack) {
    super(locals, stack);
  }

  void setThis(UninitializedObjectType uot) {
    _this = uot;
  }

  UninitializedObjectType getThis() {
    return _this;
  }
}

/**
 * BCEL throws an exception for the util.Dbg class because it overloads a field.
 * The choice (as described in the BCEL method comment for around line 2551 in
 * org.apache.bcel.verifier.structurals.InstConstraintVisitor) is to comment out
 * this check and recompile BCEL jar. Insted of recompiling BCEL the choice is
 * to override the methods, which is ok? as we are not using BCEL for bytecode
 * verification purposes (using Sun Javac).
 */
// TODO: Can this extension be avoided (ie. why did BCEL not like the
// overloaded Dbg field).
class AnInstConstraintVisitor extends InstConstraintVisitor {
  public void visitAALOAD(AALOAD o) {
  }

  public void visitAASTORE(AASTORE o) {
  }

  public void visitACONST_NULL(ACONST_NULL o) {
  }

  public void visitALOAD(ALOAD o) {
  }

  public void visitANEWARRAY(ANEWARRAY o) {
  }

  public void visitARETURN(ARETURN o) {
  }

  public void visitARRAYLENGTH(ARRAYLENGTH o) {
  }

  public void visitASTORE(ASTORE o) {
  }

  public void visitATHROW(ATHROW o) {
  }

  public void visitBALOAD(BALOAD o) {
  }

  public void visitBASTORE(BASTORE o) {
  }

  public void visitBIPUSH(BIPUSH o) {
  }

  public void visitBREAKPOINT(BREAKPOINT o) {
  }

  public void visitCALOAD(CALOAD o) {
  }

  public void visitCASTORE(CASTORE o) {
  }

  public void visitCHECKCAST(CHECKCAST o) {
  }

  public void visitCPInstruction(CPInstruction o) {
  }

  public void visitD2F(D2F o) {
  }

  public void visitD2I(D2I o) {
  }

  public void visitD2L(D2L o) {
  }

  public void visitDADD(DADD o) {
  }

  public void visitDALOAD(DALOAD o) {
  }

  public void visitDASTORE(DASTORE o) {
  }

  public void visitDCMPG(DCMPG o) {
  }

  public void visitDCMPL(DCMPL o) {
  }

  public void visitDCONST(DCONST o) {
  }

  public void visitDDIV(DDIV o) {
  }

  public void visitDLOAD(DLOAD o) {
  }

  public void visitDMUL(DMUL o) {
  }

  public void visitDNEG(DNEG o) {
  }

  public void visitDREM(DREM o) {
  }

  public void visitDRETURN(DRETURN o) {
  }

  public void visitDSTORE(DSTORE o) {
  }

  public void visitDSUB(DSUB o) {
  }

  public void visitDUP_X1(DUP_X1 o) {
  }

  public void visitDUP_X2(DUP_X2 o) {
  }

  public void visitDUP(DUP o) {
  }

  public void visitDUP2_X1(DUP2_X1 o) {
  }

  public void visitDUP2_X2(DUP2_X2 o) {
  }

  public void visitDUP2(DUP2 o) {
  }

  public void visitF2D(F2D o) {
  }

  public void visitF2I(F2I o) {
  }

  public void visitF2L(F2L o) {
  }

  public void visitFADD(FADD o) {
  }

  public void visitFALOAD(FALOAD o) {
  }

  public void visitFASTORE(FASTORE o) {
  }

  public void visitFCMPG(FCMPG o) {
  }

  public void visitFCMPL(FCMPL o) {
  }

  public void visitFCONST(FCONST o) {
  }

  public void visitFDIV(FDIV o) {
  }

  public void visitFieldInstruction(FieldInstruction o) {
  }

  public void visitFLOAD(FLOAD o) {
  }

  public void visitFMUL(FMUL o) {
  }

  public void visitFNEG(FNEG o) {
  }

  public void visitFREM(FREM o) {
  }

  public void visitFRETURN(FRETURN o) {
  }

  public void visitFSTORE(FSTORE o) {
  }

  public void visitFSUB(FSUB o) {
  }

  public void visitGETFIELD(GETFIELD o) {
  }

  public void visitGETSTATIC(GETSTATIC o) {
  }

  public void visitGOTO_W(GOTO_W o) {
  }

  public void visitGOTO(GOTO o) {
  }

  public void visitI2B(I2B o) {
  }

  public void visitI2C(I2C o) {
  }

  public void visitI2D(I2D o) {
  }

  public void visitI2F(I2F o) {
  }

  public void visitI2L(I2L o) {
  }

  public void visitI2S(I2S o) {
  }

  public void visitIADD(IADD o) {
  }

  public void visitIALOAD(IALOAD o) {
  }

  public void visitIAND(IAND o) {
  }

  public void visitIASTORE(IASTORE o) {
  }

  public void visitICONST(ICONST o) {
  }

  public void visitIDIV(IDIV o) {
  }

  public void visitIF_ACMPEQ(IF_ACMPEQ o) {
  }

  public void visitIF_ACMPNE(IF_ACMPNE o) {
  }

  public void visitIF_ICMPEQ(IF_ICMPEQ o) {
  }

  public void visitIF_ICMPGE(IF_ICMPGE o) {
  }

  public void visitIF_ICMPGT(IF_ICMPGT o) {
  }

  public void visitIF_ICMPLE(IF_ICMPLE o) {
  }

  public void visitIF_ICMPLT(IF_ICMPLT o) {
  }

  public void visitIF_ICMPNE(IF_ICMPNE o) {
  }

  public void visitIFEQ(IFEQ o) {
  }

  public void visitIFGE(IFGE o) {
  }

  public void visitIFGT(IFGT o) {
  }

  public void visitIFLE(IFLE o) {
  }

  public void visitIFLT(IFLT o) {
  }

  public void visitIFNE(IFNE o) {
  }

  public void visitIFNONNULL(IFNONNULL o) {
  }

  public void visitIFNULL(IFNULL o) {
  }

  public void visitIINC(IINC o) {
  }

  public void visitILOAD(ILOAD o) {
  }

  public void visitIMPDEP1(IMPDEP1 o) {
  }

  public void visitIMPDEP2(IMPDEP2 o) {
  }

  public void visitIMUL(IMUL o) {
  }

  public void visitINEG(INEG o) {
  }

  public void visitINSTANCEOF(INSTANCEOF o) {
  }

  public void visitInvokeInstruction(InvokeInstruction o) {
  }

  public void visitINVOKEINTERFACE(INVOKEINTERFACE o) {
  }

  public void visitINVOKESPECIAL(INVOKESPECIAL o) {
  }

  public void visitINVOKESTATIC(INVOKESTATIC o) {
  }

  public void visitINVOKEVIRTUAL(INVOKEVIRTUAL o) {
  }

  public void visitIOR(IOR o) {
  }

  public void visitIREM(IREM o) {
  }

  public void visitIRETURN(IRETURN o) {
  }

  public void visitISHL(ISHL o) {
  }

  public void visitISHR(ISHR o) {
  }

  public void visitISTORE(ISTORE o) {
  }

  public void visitISUB(ISUB o) {
  }

  public void visitIUSHR(IUSHR o) {
  }

  public void visitIXOR(IXOR o) {
  }

  public void visitJSR_W(JSR_W o) {
  }

  public void visitJSR(JSR o) {
  }

  public void visitL2D(L2D o) {
  }

  public void visitL2F(L2F o) {
  }

  public void visitL2I(L2I o) {
  }

  public void visitLADD(LADD o) {
  }

  public void visitLALOAD(LALOAD o) {
  }

  public void visitLAND(LAND o) {
  }

  public void visitLASTORE(LASTORE o) {
  }

  public void visitLCMP(LCMP o) {
  }

  public void visitLCONST(LCONST o) {
  }

  public void visitLDC_W(LDC_W o) {
  }

  public void visitLDC(LDC o) {
  }

  public void visitLDC2_W(LDC2_W o) {
  }

  public void visitLDIV(LDIV o) {
  }

  public void visitLLOAD(LLOAD o) {
  }

  public void visitLMUL(LMUL o) {
  }

  public void visitLNEG(LNEG o) {
  }

  public void visitLoadClass(LoadClass o) {
  }

  public void visitLoadInstruction(LoadInstruction o) {
  }

  public void visitLocalVariableInstruction(LocalVariableInstruction o) {
  }

  public void visitLOOKUPSWITCH(LOOKUPSWITCH o) {
  }

  public void visitLOR(LOR o) {
  }

  public void visitLREM(LREM o) {
  }

  public void visitLRETURN(LRETURN o) {
  }

  public void visitLSHL(LSHL o) {
  }

  public void visitLSHR(LSHR o) {
  }

  public void visitLSTORE(LSTORE o) {
  }

  public void visitLSUB(LSUB o) {
  }

  public void visitLUSHR(LUSHR o) {
  }

  public void visitLXOR(LXOR o) {
  }

  public void visitMONITORENTER(MONITORENTER o) {
  }

  public void visitMONITOREXIT(MONITOREXIT o) {
  }

  public void visitMULTIANEWARRAY(MULTIANEWARRAY o) {
  }

  public void visitNEW(NEW o) {
  }

  public void visitNEWARRAY(NEWARRAY o) {
  }

  public void visitNOP(NOP o) {
  }

  public void visitPOP(POP o) {
  }

  public void visitPOP2(POP2 o) {
  }

  public void visitPUTFIELD(PUTFIELD o) {
  }

  public void visitPUTSTATIC(PUTSTATIC o) {
  }

  public void visitRET(RET o) {
  }

  public void visitRETURN(RETURN o) {
  }

  public void visitReturnInstruction(ReturnInstruction o) {
  }

  public void visitSALOAD(SALOAD o) {
  }

  public void visitSASTORE(SASTORE o) {
  }

  public void visitSIPUSH(SIPUSH o) {
  }

  public void visitStackConsumer(StackConsumer o) {
  }

  public void visitStackInstruction(StackInstruction o) {
  }

  public void visitStackProducer(StackProducer o) {
  }

  public void visitStoreInstruction(StoreInstruction o) {
  }

  public void visitSWAP(SWAP o) {
  }

  public void visitTABLESWITCH(TABLESWITCH o) {
  }
}

// We may need this later in controlFlowMethod to simulate the basic blocks
// // TODO: Do we even need the controlflowgraph for this?
// cfg = new ControlFlowGraph(mg);
// // InstructionContext as key for inFrame
// HashMap inFrames = new HashMap();
// HashMap outFrames = new HashMap();
//
// // Build the initial frame situation for this method.
// FrameFrame fStart = new FrameFrame(mg.getMaxLocals(), mg.getMaxStack());
// if (!mg.isStatic()) {
// if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)) {
// fStart.setThis(new UninitializedObjectType(new ObjectType(jc
// .getClassName())));
// fStart.getLocals().set(0, fStart.getThis());
// } else {
// fStart.setThis(null);
// fStart.getLocals().set(0, new ObjectType(jc.getClassName()));
// }
// }
// Type[] argtypes = mg.getArgumentTypes();
// int twoslotoffset = 0;
// for (int j = 0; j < argtypes.length; j++) {
// if (argtypes[j] == Type.SHORT || argtypes[j] == Type.BYTE
// || argtypes[j] == Type.CHAR || argtypes[j] == Type.BOOLEAN) {
// argtypes[j] = Type.INT;
// }
// fStart.getLocals().set(twoslotoffset + j + (mg.isStatic() ? 0 : 1),
// argtypes[j]);
// if (argtypes[j].getSize() == 2) {
// twoslotoffset++;
// fStart.getLocals().set(twoslotoffset + j + (mg.isStatic() ? 0 : 1),
// Type.UNKNOWN);
// }
// }
//
// // if (method.getName().equalsIgnoreCase("sort")) {
// // System.out.println(method.getCode().toString());
// //
// // }
//
// InstructionContext start = cfg.contextOf(mg.getInstructionList()
// .getStart());
// // don't need to compare for first frame
// inFrames.put(start, fStart);
//
// boolean fbool = start.execute(fStart, new ArrayList(), icv, ev);
// Frame fout = start.getOutFrame(new ArrayList());
// outFrames.put(start, fout);
// start.setTag(start.getTag() + 1);
// // int posnow = start.getInstruction().getPosition();
//
// Vector ics = new Vector(); // Type: InstructionContext
// Vector ecs = new Vector(); // Type: ArrayList (of
// // InstructionContext)
//
// ics.add(start);
// ecs.add(new ArrayList());
// int loopcnt = 1;
// // LOOP!
// while (!ics.isEmpty()) {
// loopcnt++;
// InstructionContext u;
// ArrayList ec;
// u = (InstructionContext) ics.get(0);
// // TODO: Would it be better to call wcet here instead of in the TAG
// // count
// // loop?
// // System.out.println(u.toString());
// ec = (ArrayList) ecs.get(0);
// ics.remove(0);
// ecs.remove(0);
// ArrayList oldchain = (ArrayList) (ec.clone());
// ArrayList newchain = (ArrayList) (ec.clone());
// newchain.add(u);
//
// if ((u.getInstruction().getInstruction()) instanceof RET) {
// // We can only follow _one_ successor, the one after the
// // JSR that was recently executed.
// RET ret = (RET) (u.getInstruction().getInstruction());
// ReturnaddressType t = (ReturnaddressType) u.getOutFrame(oldchain)
// .getLocals().get(ret.getIndex());
// InstructionContext theSuccessor = cfg.contextOf(t.getTarget());
//
// // Sanity check
// InstructionContext lastJSR = null;
// int skip_jsr = 0;
// for (int ss = oldchain.size() - 1; ss >= 0; ss--) {
// if (skip_jsr < 0) {
// throw new AssertionViolatedException(
// "More RET than JSR in execution chain?!");
// }
// if (((InstructionContext) oldchain.get(ss)).getInstruction()
// .getInstruction() instanceof JsrInstruction) {
// if (skip_jsr == 0) {
// lastJSR = (InstructionContext) oldchain.get(ss);
// break;
// } else {
// skip_jsr--;
// }
// }
// if (((InstructionContext) oldchain.get(ss)).getInstruction()
// .getInstruction() instanceof RET) {
// skip_jsr++;
// }
// }
// if (lastJSR == null) {
// throw new AssertionViolatedException(
// "RET without a JSR before in ExecutionChain?! EC: '" + oldchain
// + "'.");
// }
// JsrInstruction jsr = (JsrInstruction) (lastJSR.getInstruction()
// .getInstruction());
// if (theSuccessor != (cfg.contextOf(jsr.physicalSuccessor()))) {
// throw new AssertionViolatedException("RET '" + u.getInstruction()
// + "' info inconsistent: jump back to '" + theSuccessor
// + "' or '" + cfg.contextOf(jsr.physicalSuccessor()) + "'?");
// }
//
// if (theSuccessor.execute(u.getOutFrame(oldchain), newchain, icv, ev)) {
// ics.add(theSuccessor);
// ecs.add((ArrayList) newchain.clone());
// }
// // inFrames.put(theSuccessor,u.getOutFrame(oldchain));
// theSuccessor.setTag(theSuccessor.getTag() + 1);
// // osa[theSuccessor.getInstruction().getPosition()].add(fStart
// // .getStack().getClone());
// // lva[theSuccessor.getInstruction().getPosition()].add(fStart
// // .getLocals().getClone());
// Frame prevf = (Frame) inFrames.put(theSuccessor, u
// .getOutFrame(oldchain));
// Frame newf = theSuccessor.getOutFrame(newchain);
// Frame prevof = (Frame) outFrames.put(theSuccessor, newf);
//
// } else {// "not a ret"
// // Normal successors. Add them to the queue of successors.
// // TODO: Does u get executed?
// InstructionContext[] succs = u.getSuccessors();
//
// // System.out.println("suss#:" + succs.length);
// for (int s = 0; s < succs.length; s++) {
// InstructionContext v = succs[s];
// // System.out.println(v.toString());
// if (v.execute(u.getOutFrame(oldchain), newchain, icv, ev)) {
// ics.add(v);
// ecs.add((ArrayList) newchain.clone());
// }
// v.setTag(v.getTag() + 1);
// Frame prevf = (Frame) inFrames.put(v, u.getOutFrame(oldchain));
// Frame newf = v.getOutFrame(newchain);
// Frame prevof = (Frame) outFrames.put(v, newf);
// }
// }// end "not a ret"
//
// // Exception Handlers. Add them to the queue of successors.
// // [subroutines are never protected; mandated by JustIce]
// ExceptionHandler[] exc_hds = u.getExceptionHandlers();
// for (int s = 0; s < exc_hds.length; s++) {
// InstructionContext v = cfg.contextOf(exc_hds[s].getHandlerStart());
// Frame f = new Frame(u.getOutFrame(oldchain).getLocals(),
// new OperandStack(u.getOutFrame(oldchain).getStack().maxStack(),
// (exc_hds[s].getExceptionType() == null ? Type.THROWABLE
// : exc_hds[s].getExceptionType())));
//
// if (v.execute(f, new ArrayList(), icv, ev)) {
// ics.add(v);
// ecs.add(new ArrayList());
// }
// v.setTag(v.getTag() + 1);
// Frame prevf = (Frame) inFrames.put(v, f);
// Frame newf = v.getOutFrame(new ArrayList());
// Frame prevof = (Frame) outFrames.put(v, newf);
//
// }
// }// while (!ics.isEmpty()) END
//
// // Check that all instruction have been simulated
// do {
// InstructionContext ic = cfg.contextOf(ih);
// if (ic.getTag() == 0) {
// System.err
// .println("Instruction " + ic.toString() + " not simulated.");
// System.exit(-1);
// }
// // TODO: Can it handle direct loops back
// if (ih.getInstruction() instanceof BranchInstruction) {
// int target = ((BranchInstruction) ih.getInstruction()).getTarget()
// .getPosition();
// InstructionHandle btarget = ((BranchInstruction) ih.getInstruction())
// .getTarget();
// // check and possibly create a new bb starting at the target
// createBasicBlock(btarget);
// InstructionHandle ihnext = ih.getNext();
// // check if the next instruction was the target and if not see if a
// // new bb is to be created
// // TODO: Could it be true?
// if (!ihnext.equals(btarget)) {
// createBasicBlock(ihnext);
// }
//
// }
//
// } while ((ih = ih.getNext()) != null);

// Some utilitilities
class WU{
  
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