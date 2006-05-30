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
public class WCETAnalyser {
  // latex table string
  public static String las;
  public static String lae;
  // dot property: it will generate dot graphs if true
  public static boolean dot;
  public static boolean jline;
  public static boolean ls;
  
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
  
  HashMap mmap;
  
  HashMap javaFilePathMap;
  
  ArrayList javaFiles;
  
  static String outFile;

  public WCETAnalyser() {
    
    // TODO: Debugging from Eclipse creates a different classpath?
    classpath = new org.apache.bcel.util.ClassPath(".");
    mmap = new HashMap();
    javaFiles = new ArrayList();
    javaFilePathMap = new HashMap();
    
  }

  public static void main(String[] args) {
    WCETAnalyser wca = new WCETAnalyser();
    HashSet clsArgs = new HashSet();
    outFile = null;     // wcet/P3+Wcet.txt
    //the tables can be easier to use in latex using this property
    boolean latex = System.getProperty("latex", "false").equals("true");
    //dot graphs code generation
    dot = System.getProperty("dot", "false").equals("true");
    jline = System.getProperty("jline", "false").equals("true");
    ls = System.getProperty("ls", "false").equals("true");
    if(latex){
      las = " & ";
      lae = " \\\\";
    }
    else {
      las = "";
      lae = "";
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
        String ds = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\dotall.bat";
        wca.dotout = new PrintWriter(new FileOutputStream(ds));
        
        wca.load(clsArgs);

        wca.iterate(new SetWCETAnalysis(wca));
        
        //instruction info
        wca.out.println("************************************************");
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
}

/**
 * It has a HashMap of WCETBasicBlocks. The class have methods that are called
 * from the WCETAnalyzers controlFlowGraph method. It creates the the directed
 * graph of wcbbs.
 */
class WCETMethodBlock {
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
  int n = -1;
  
  int wcetlp;
  
  HashMap wcetvars;
  
  public WCETBasicBlock S;
  
  public WCETBasicBlock T;

  // create a bb covering the whole method
  // from here on we split it when necessary
  public void init(InstructionHandle stih, InstructionHandle endih) {
    WCETBasicBlock wcbb = new WCETBasicBlock(stih, endih, this);
    bbs.put(new Integer(wcbb.getStart()), wcbb);
  }

  /**
   * Instanciated from from <code>SetClassInfo</code>.
   */
  public WCETMethodBlock(Method method, JavaClass jc, WCETAnalyser wca) {
    this.wca = wca;
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
      T = new WCETBasicBlock(this,WCETBasicBlock.TNODE);
      // pass 0: Create basic blocks
      InstructionHandle ih = mg.getInstructionList().getStart();
      // wcet startup: create the first full covering bb
      InstructionHandle ihend = mg.getInstructionList().getEnd();
      init(ih, ihend);

      do {
        // create new bb (a)for branch target and (b) for sucessor
        Instruction ins = ih.getInstruction();
        if (ih.getInstruction() instanceof BranchInstruction) {
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
      int id = 0;
      // it is sorted on the (final) start pos of each block
      for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
        WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
            (Integer) iter.next());
        wbb.calculateWcet();
        wbb.setId(id);
        id++;

        ih = wbb.getEndih();
        WCETBasicBlock wbbthis = getCoveringBB(ih);

        if (ih.getInstruction() instanceof BranchInstruction) {
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
      T.id = id;
      TreeMap newbbs = new TreeMap();
      for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
        WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
            (Integer) iter.next());
        newbbs.put(new Integer(wbb.id),wbb);
      }
      bbs = newbbs;
      bbs.put(new Integer(T.id),T);
    }
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
      // save the new bb in the hash map
      if (bbs.put(new Integer(stih.getPosition()), wcbb) != null) {
        System.err.println("The starting pos should be unique.");
        System.exit(-1);
      }
      res = true;
    }
    return res;
  }

  /**
   * It sorts the basic blocks and creates the directed graph.
   */
  public void directedGraph() {
    // now create the directed graph
    dg = new int[bbs.size()][bbs.size()];
    WCETBasicBlock.bba = new WCETBasicBlock[bbs.size()+1];//TODO
    LineNumberTable lnt = methodbcel.getLineNumberTable();
    WCETBasicBlock pbb = null;
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      if(pbb!=null)
        wcbb.prevbb = pbb;
      pbb = wcbb;
      if(wcbb.nodetype!=WCETBasicBlock.TNODE)
        wcbb.line = lnt.getSourceLine(wcbb.endih.getPosition());
      else 
        wcbb.line = -1;
      WCETBasicBlock tarwcbb = wcbb.getTarbb();
      int id = wcbb.getId();
      WCETBasicBlock.bba[id] = wcbb;
      if (tarwcbb != null) {
        int tarbbid = tarwcbb.getId();
        tarwcbb.addTargeter(wcbb);
        dg[id][tarbbid]++;
      }
      WCETBasicBlock sucbb = wcbb.getSucbb();
      if (sucbb != null && sucbb.nodetype != WCETBasicBlock.TNODE) {
        int sucid = sucbb.getId();
        sucbb.addTargeter(wcbb);
        dg[id][sucid]++;
      }
    }
    
    HashSet lines = new HashSet();
    // find loopdrivers/loopcontrollers
//System.out.println("\nmethod:"+method.getClass().getName()+"."+method.getName());    
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
//System.out.println("outer loop wcbb.id:"+wcbb.id);      
      // identify loop controller candidate
      if(((wcbb.sucbb != null && wcbb.tarbb != null)
          && (!wcbb.loopdriver || !wcbb.loopcontroller))
          && !lines.contains(new Integer(wcbb.line))){
        HashMap wcaA = WU.wcaA(codeLines[wcbb.line-1]);
        if(wcaA != null){
          if(wcaA.get("loop") != null){ // wcbb is now loopdriver
//System.out.println("loopdriver id:"+wcbb.id);            
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
            wcbb.loop = Integer.parseInt((String)wcaA.get("loop"));
            wcbbhit.loopcontroller = true;
            wcbbhit.loopid = wcbb.id;
            wcbbhit.loop = Integer.parseInt((String)wcaA.get("loop"));
            lines.add(new Integer(wcbbhit.line));
          }
        }
      }
          
      if(wcbb.loopcontroller){
        HashMap tinbbs = wcbb.getInbbs();
        if(wcbb.id > 0 && tinbbs.size()!=2){
//          System.out.println("error in loopcontrol:"+wcbb.id);
//          System.out.println("tinbbs.size:"+tinbbs.size());
//          System.exit(-1);
        }
      }
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
    if(wca.las.length()>0)
      top.append("  ");    
    top.append(WU.prepad(""+wca.las,4));

    for (int i = 0; i < dg.length; i++) {
      if(i<dg.length-1)
        top.append(WU.postpad("B" + i+wca.las,4));
      else
        top.append(WU.postpad("B" + i+wca.lae,4));
    }
    top.append("\n");

    for (int i = 0; i < top.length() - 3+wca.las.length(); i++) {
      sb.append("=");
    }
    sb.append("\n" + top.toString());

    for (int i = 0; i < dg.length; i++) {
      sb.append(WU.postpad("B" + i,3));
      sb.append(wca.las);

      for (int j = 0; j < dg.length; j++) {
        if (dg[i][j] == 0)
          sb.append(" ."); // a space does not clutter it as much as a zero
        else
          sb.append(" " + dg[i][j]);

        if(j<dg.length-1)
          sb.append(WU.postpad(""+wca.las,2));
        else
          sb.append(WU.postpad(""+wca.lae,2));
      }
      sb.append("\n");
    }
    sb.append(WU.repeat("=",top.length() - 3+wca.las.length()));
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
      if(wcbb.nodetype!=WCETBasicBlock.TNODE)
        sb.append(wcbb.toCodeString());
    }
    sb.append("=========================================================================\n");
    sb.append("Info: n="+n+" b="+WCETInstruction.calculateB(n)+" a="+WCETInstruction.a+" r="+WCETInstruction.r+" w="+WCETInstruction.w+"\n");
    sb.append("\n"); 
    if(wca.ls){
      sb.append(toLS());
      WCETBasicBlock.linkbb(WCETBasicBlock.bba[0]);
      WCETBasicBlock.bbe();
      sb.append("\n"+toLinkBBS());
    }
    if(wca.dot)
      sb.append(toDot());
    
    
    return sb.toString();
  }

  public String toDot() {
    StringBuffer sb = new StringBuffer();
    // dot graph
    // use: dot -Tps graph.dot -o graph.ps
    boolean labels = true;

    sb.append("\n/*"+ jc.getClassName() + "." + methodbcel.getName()
        + methodbcel.getSignature()+"*/\n");
    sb.append("digraph G {\n");
    sb.append("size = \"10,7.5\"\n");

    for (int i = 0; i < dg.length; i++) {
      for (int j = 0; j < dg.length; j++) {
        if(dg[i][j]>0){
          sb.append("\tB"+i+" -> "+"B"+j);
          if(labels){
            //sb.append(" [label=\""+dg[i][j]+"\"");
            String edge = "f"+i+"_"+j;

            if(wcetvars.get(edge)!=null){
              int edgeval = Integer.parseInt((String)wcetvars.get(edge));
              if(edgeval>0)
                sb.append(" [label=\"f"+i+"_"+j+"="+edgeval+"\"");
              else
                sb.append(" [style=dashed,label=\"f"+i+"_"+j+"="+edgeval+"\"");
            }
            else
              sb.append(" [label=\"f"+i+"_"+j+"=?\"");
            
              //sb.append(",labelfloat=true");
            sb.append("]");
          }
          sb.append(";\n");
        }
      }
    }
    
    //T
    HashMap tinbbs = T.getInbbs();
    for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
      Integer tkeyInt = (Integer) titer.next();
      WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
      sb.append("\tB"+w.id+" -> T");
      if(labels){
        String edge = "f"+w.id+"_t";
        if(wcetvars.get(edge)!=null){
          int edgeval = Integer.parseInt((String)wcetvars.get(edge));
          if(edgeval>0)
            sb.append(" [label=\"f"+w.id+"_t ="+edgeval+"\"");
          else
            sb.append(" [style=dashed,label=\"f"+w.id+"_t ="+edgeval+"\"");
        }
        else
          sb.append(" [label=\"f"+w.id+"_t = ?\"");
        
          //sb.append(",labelfloat=true");
        sb.append("]");
      }
      sb.append(";\n");

    } 


    
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      int id = wcbb.getId();
      sb.append("\tB"+id+" [label=\"B"+id+"\\n"+wcbb.wcetHit+"\"];\n");
    }
    sb.append("\tT [label=\"T\"];\n");
    sb.append("}\n");
    
    try {
      dotf = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\"+jc.getClassName()+"."+methodbcel.getName()+".dot";
      dotf = dotf.replace('<','_');
      dotf = dotf.replace('>','_');
      PrintWriter dotout = new PrintWriter(new FileOutputStream(dotf));
      dotout.write(sb.toString());
      dotout.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
    
    return sb.toString();    

  }
  //TODO: loop follows loop controller?
  public String toLS(){
    StringBuffer ls = new StringBuffer();
    ls.append("/***WCET calculation source***/\n");
    ls.append("/* WCA WCET objective: "+jc.getClassName() + "." + methodbcel.getName()+ " */\n");
    ls.append("max: ");
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      ls.append("t"+wcbb.id);
      
//      HashMap tinbbs = wcbb.getInbbs();
//      if(wcbb.id==0){
//        ls.append(wcbb.blockcycmiss+" e0 ");
//      }
//      else{
//        for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
//          Integer tkeyInt = (Integer) titer.next();
//          WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
//          ls.append(wcbb.blockcycmiss+" e"+w.id+"_"+wcbb.id);
//          if(titer.hasNext())
//            ls.append(" ");
//        }    
//      }
      if(iter.hasNext())
        ls.append(" ");
    }
    ls.append(";\n");
    ls.append("/* WCA flow constraints */\n");
    ls.append("S: 1 = fs_0; // flow\n");
    WCETBasicBlock wcbb = null;
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      wcbb = (WCETBasicBlock) bbs.get(keyInt);
      HashMap tinbbs = wcbb.getInbbs();
      if(tinbbs.size()>0 || wcbb.id ==0){
        ls.append("B"+wcbb.id+": ");
        if(wcbb.id==0){
          ls.append("fs_0");
          if(tinbbs.size()>0){
            ls.append(" + ");
          }
        }
        for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
          Integer tkeyInt = (Integer) titer.next();
          WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
          ls.append("f"+w.id+"_"+wcbb.id);
          
          if(titer.hasNext())
            ls.append(" + ");
          
        } 
        ls.append(" = ");
        if(wcbb.sucbb != null){
          if(wcbb.sucbb.nodetype != WCETBasicBlock.TNODE){  
            ls.append("f"+wcbb.id+"_"+wcbb.sucbb.id);
          }
          else{
            ls.append("f"+wcbb.id+"_t");
          }
        }
        if(wcbb.sucbb != null && wcbb.tarbb!=null)
          ls.append(" + ");
        if(wcbb.tarbb!=null)
          ls.append("f"+wcbb.id+"_"+wcbb.tarbb.id);
        if(wcbb.sucbb == null && wcbb.tarbb == null)
          ls.append("1");
        ls.append(";\n");  
      }
    }

    HashMap tinbbs = T.getInbbs();
    for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
      Integer tkeyInt = (Integer) titer.next();
      WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
      ls.append("f"+w.id+"_t");
      if(titer.hasNext())
        ls.append(" + ");
    } 
    ls.append(" = 1;\n");
    
    ls.append("/* WCA loops */\n");

    //loops
    //  TODO: targeter can be part of a loop... and  + drivers
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      wcbb = (WCETBasicBlock) bbs.get(keyInt);
      if(wcbb.loopcontroller){
        if(wcbb.id==0){
          ls.append("lc"+wcbb.id+": f"+ wcbb.id+"_"+wcbb.sucbb.id+" <= "+wcbb.loop+" f"+"s"+"_"+wcbb.id+";\n");
        }else {
          ls.append("lc"+wcbb.id+": f"+ wcbb.id+"_"+wcbb.sucbb.id+" <= "+wcbb.loop+" f"+(wcbb.loopid-1)+"_"+wcbb.loopid+";\n");
          wcbb.sc = wcbb.loop;
          wcbb.scsid = wcbb.loopid-1;
          wcbb.sctid = wcbb.loopid;
        }
      }
    }

    ls.append("/* WCA flow to cycle count */\n");
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      wcbb = (WCETBasicBlock) bbs.get(keyInt);
      ls.append("t"+wcbb.id+" = ");
      
      tinbbs = wcbb.getInbbs();
      if(tinbbs.size()>0 || wcbb.id==0){
        if(wcbb.id==0){
          ls.append(wcbb.blockcycmiss+" fs_0");
          if(tinbbs.size()>0)
            ls.append(" + ");
        }
        for (Iterator titer = tinbbs.keySet().iterator(); titer.hasNext();) {
          Integer tkeyInt = (Integer) titer.next();
          WCETBasicBlock w = (WCETBasicBlock) tinbbs.get(tkeyInt);
          ls.append(wcbb.blockcycmiss+" f"+w.id+"_"+wcbb.id);
          if(titer.hasNext())
            ls.append(" + ");
        }
      }
        ls.append(";\n");
    }
    
    
    try {
      lpf = new File(WCETAnalyser.outFile).getParentFile().getAbsolutePath()+"\\"+jc.getClassName()+"."+methodbcel.getName()+".lp";
      lpf = lpf.replace('<','_');
      lpf = lpf.replace('>','_');
//System.out.println("about to write:"+lpf);
      PrintWriter lsout = new PrintWriter(new FileOutputStream(lpf));
      lsout.write(ls.toString());
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
            wcetvars.put(st.nextToken(),st.nextToken());
          }
        }
        in.close();
      } catch (IOException e) {
      }
    } catch (LpSolveException e) {
      System.out.println("LP not solvable for: "+jc.getClassName()+"."+methodbcel.getName());
      //e.printStackTrace();
    } 
    
    return ls.toString();
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

  // parent
  WCETMethodBlock wcmb;
  
  static ArrayList bbl = new ArrayList(); // bb links
  static int[] bbe; // execution times
  static int wcetid;
  static int bcetid;
  static WCETBasicBlock[] bba;
  
  // id of the bb
  int id = -1;
  
  int line = -1;
  
  int loopid = -1;
  boolean loopdriver = false;
  boolean loopcontroller = false;
  boolean loopreturn = false;
  
  // loop target
  int loop = -1;
  int looptargetid = -1;
  

  // the reason why we are doing this...
  int wcetHit;
  int wcetMiss;
  int blockcychit;
  int blockcycmiss;


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
  ArrayList bbinvo = new ArrayList();

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
  public final static int TNODE = 2;
  public final static int BBNODE = 3;
  public int nodetype = BBNODE;
  WCETBasicBlock(WCETMethodBlock wcmb, int nodetype){
    this.nodetype = nodetype;
    valid = true;
    wcetHit =0;
    wcetMiss =0;
    start = 0;
    key = new Integer(-1);
    inbbs = new HashMap();
  }
  
  WCETBasicBlock(InstructionHandle stih, InstructionHandle endih, WCETMethodBlock wcmb) {
    this.wcmb = wcmb;
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
    int[] l = new int[200]; // -1 marks termination
    int MAXLINK = 1000;  //
    
    l[0] = b.id;
//System.out.println("l[0]:"+b.id);    
    l[1] = -1;
    al.add(l);
    
    while(al.size()>0){
      l = (int[])al.get(0);
      int len = 0;
      for (int i=0;i<l.length;i++){
        if(l[i]!=-1)
          len++;
        else{
//System.out.println("l[len-1] "+l[len-1]);          
          b = bba[l[len-1]];
          break;
        }
      }
//System.out.println("len:"+len);   
//for (int i=0;i<len;i++){
//  System.out.println(l[i]);
//}
      
      if(l.length<len+2){
        int newl[] = new int[l.length+10];
        System.arraycopy(l,0,newl,0,l.length);
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
            if(l[i-1] == b.id && l[i] == b.sucbb.id)
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
            if(l[i-1] == b.id && l[i] == b.tarbb.id)
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
        newl[len] = b.tarbb.id;
        newl[len+1] = -1;
        if(b.tarbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(newl);
        else
          al.add(newl);
        
        l[len] = b.sucbb.id;
        l[len+1] = -1;
        if(b.sucbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(al.remove(0));
      } else if(b.sucbb != null && !svio){
        l[len] = b.sucbb.id;
        l[len+1] = -1;
        if(b.sucbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(al.remove(0));
      } else if(b.tarbb != null && !tvio){
        l[len] = b.tarbb.id;
        l[len+1] = -1;
        if(b.tarbb.nodetype == WCETBasicBlock.TNODE)
          bbl.add(al.remove(0));
      } else
        al.remove(l);
    }
    
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
        if(l[j]==-1)
          break;
//System.out.println("i = "+i);
//System.out.println("j = "+j);
//System.out.println("l[j] = "+l[j]);
        if(bba.length>l[j] && bba[l[j]]!=null)//TODO
          bbe[i] += bba[l[j]].getBlockCycles();
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
    WCETBasicBlock spbb = new WCETBasicBlock(newstih, endih, wcmb);
    end = newstih.getPrev().getPosition();
    endih = newstih.getPrev();
    return spbb;
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

  /**
   * True if the
   * 
   * @return
   */
  public boolean getValid() {
    return valid;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    // block (6)
    String s = "B" + id;
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

    InstructionHandle ih = stih;
    blockcychit = 0;
    blockcycmiss = 0;

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
            sb.append(WU.postpad(wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+"Annotated Src. line :"+srcLine+": "+wcmb.codeLines[srcLine-1].trim()+wcmb.wca.lae,62)+"\n");
          }else
            sb.append(WU.postpad(wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+wcmb.wca.las+"  Src. line "+srcLine+": "+wcmb.codeLines[srcLine-1].trim()+wcmb.wca.lae,62)+"\n");
        }
        prevLine = srcLine; 
      }

      // block (len 6)
      if (ih == stih) {
        String tStr = "<-[";
        for (Iterator iter = inbbs.keySet().iterator(); iter.hasNext();) {
          Integer keyInt = (Integer) iter.next();
          WCETBasicBlock wcbb = (WCETBasicBlock) inbbs.get(keyInt);
          tStr += "B"+wcbb.getId()+" ";
        }
        tStr += "]";

sb.append(WU.postpad("B" + id+tStr,6)); // see the BBs that point to this BB
//        sb.append(WU.postpad("B" + id,6));
      } else {
        sb.append("      ");
      }
      
      sb.append(wcmb.wca.las);
      
      // addr (len 6)
      sb.append(WU.postpad(ih.getPosition() + ":",6));

      if(!WCETInstruction.wcetAvailable(ih.getInstruction().getOpcode()))
        sb.append("*");
      else 
        sb.append(" ");
      
      sb.append(wcmb.wca.las);
      
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
      
      sb.append(wcmb.wca.las);
      
      String invoStr = "";
      
      //invoke instructions
      if(ih.getInstruction() instanceof InvokeInstruction){
        int wcetihMiss = -1;
        int wcetihHit = -1;
       
        String methodid = ((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())
        +"."
        +((InvokeInstruction)ih.getInstruction()).getMethodName(wcmb.getCpg())
        +((InvokeInstruction)ih.getInstruction()).getSignature(wcmb.getCpg());
        String retsig = ((InvokeInstruction)ih.getInstruction()).getReturnType(wcmb.getCpg()).getSignature(); 

        //signature Java Type, Z boolean, B byte, C char, S short, I int
        //J long, F float, D double, L fully-qualified-class, [ type type[] 
        bbinvo.add(methodid);
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
            wcetihMiss = cycles;
            wcetihHit = cycles;
            blockcycmiss += wcetihMiss;
            blockcychit += wcetihHit;
            sb.append(WU.prepad(Integer.toString(wcetihHit),10));
            sb.append("   ");
            sb.append("                ");
            invoStr = methodid;
          }
        }
        else if(m!=null && !m.isAbstract()){
          int n = -1;
          if(m.getCode()!= null){
            n = (m.getCode().getCode().length + 3) / 4;
          }else{
            n=0;
          }
          int invokehit = WCETInstruction.getCyclesFromHandle(ih,false,n);
          int invokemiss = WCETInstruction.getCyclesFromHandle(ih,true,n);
          
          //now the return 
          int rethit = -1;
          int retmiss = -1; 
          //TODO: Check these with ms
          if(retsig.equals("V")){
            rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.RETURN,false, n);
            retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.RETURN,true, n);
          } 
          else if(retsig.equals("I") || retsig.equals("Z")|| retsig.equals("B")|| retsig.equals("C")|| retsig.equals("S")){
            rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.IRETURN,false, n);
            retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.IRETURN,true, n);
          } 
          else if(retsig.equals("J")){
            rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.LRETURN,false, n);
            retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.LRETURN,true, n);
          } 
          else if(retsig.equals("D")){
            rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.DRETURN,false, n);
            retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.DRETURN,true, n);
          } 
          else if(retsig.equals("F")){
            rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.FRETURN,false, n);
            retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.FRETURN,true, n);
          } 
          else if(retsig.startsWith("[") || retsig.startsWith("L")){
            rethit = WCETInstruction.getCycles(org.apache.bcel.Constants.ARETURN,false, n);
            retmiss = WCETInstruction.getCycles(org.apache.bcel.Constants.ARETURN,true, n);
          }else{
            System.out.println("Did not recognize "+retsig+" as return type");
            System.exit(-1);
          }
          wcetihMiss = invokemiss;//+retmiss;
          blockcycmiss += wcetihMiss;
          wcetihHit = invokehit;//+rethit;
          blockcychit += wcetihHit;
          if((((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())).equals(wcmb.wca.nativeClass)){
//            sb.append(WU.prepad("*"+Integer.toString(wcetihHit)+"/"+Integer.toString(wcetihMiss),10));
            sb.append(WU.prepad("*to check",10));
          } else {
//            sb.append(WU.prepad(Integer.toString(wcetihHit)+"/"+Integer.toString(wcetihMiss),10));
            sb.append(WU.prepad(invokehit+"",10));
            sb.append(WU.prepad(wcmb.wca.las+(invokemiss-invokehit)+"",8));
            sb.append(WU.prepad(wcmb.wca.las+(retmiss-rethit)+"",8));
          }

          sb.append("   ");
          invoStr = methodid+", invoke(n="+n+"):"+invokehit+"/"+invokemiss;//+" return(n="+wcmb.getN()+"):"+rethit+"/"+retmiss;
          if((((InvokeInstruction)ih.getInstruction()).getClassName(wcmb.getCpg())).equals(wcmb.wca.nativeClass)){
            invoStr += ", no hit/miss cycle count for Native invokes (yet)";
          } 
        }
        else{
          sb.append("*");
        }

      }else{ // non-invoke functions
        int wcetihMiss;
        int wcetihHit;
        if(ih.getInstruction() instanceof ReturnInstruction){
          // ms suggestion?
          // MS: no not this way, use the hit cycles count and
          // add the miss cycles to the invoke instruction
          wcetihMiss = 0;
          // wcetihHit = 0;
          wcetihHit = WCETInstruction.getCyclesFromHandle(ih, false, wcmb.getN());
          sb.append(WU.prepad(Integer.toString(wcetihHit),10));
        } else{
          wcetihMiss = WCETInstruction.getCyclesFromHandle(ih, true, wcmb.getN());
          wcetihHit = WCETInstruction.getCyclesFromHandle(ih, false, wcmb.getN());
          sb.append(WU.prepad(Integer.toString(wcetihHit),10));
        }
        blockcycmiss += wcetihMiss;
        blockcychit += wcetihHit;

        sb.append(wcmb.wca.las+"   ");
        sb.append(wcmb.wca.las+"                ");
      }

      sb.append(wcmb.wca.las);
      
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
        sb.append("sum(B"+id+"):");
        if(ih.getInstruction() instanceof ReturnInstruction){
          sb.append(WU.prepad(blockcychit+"/NA",7));
//          sb.append(" *do add return cycles and do not have size of caller (yet)");
        }
        else{
          sb.append(WU.prepad(blockcychit+"/"+blockcycmiss,7));
        }
      }
      
      sb.append(wcmb.wca.lae+"\n");
    } while (ih != endih && (ih = ih.getNext()) != null);
    
    if(blockcycmiss<blockcychit)
      blockcycmiss = blockcychit;

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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  //TODO: ms implements CacheSimul
  public int getBlockCycles() {
    boolean hit = false;
    for (int i=0;i<bbinvo.size();i++){
      hit = CacheSimul.get(wcmb.methodbcel);
    }
    if(hit)
      return blockcychit;
    else
      return blockcycmiss;
  }

}

/**
 * It has wcet info on byte code instruction granlularity. Should we consider
 * making a class that wraps the microcodes into objects?
 */
class WCETInstruction {
  // indicate that wcet is not available for this bytecode
  public static final int WCETNOTAVAILABLE = -1;

  // bytecode load
  public static final int a = 2;

  // mem read: 2 for mem and 3 for pipe
  public static final int r = 5;

  // mem write
  public static final int w = 6;
  
  //Native bytecodes (see jvm.asm)
  private static final int JOPSYS_RD = 209;   
  private static final int JOPSYS_WR = 210;
  private static final int JOPSYS_RDMEM = 211;
  private static final int JOPSYS_WRMEM = 212;
  private static final int JOPSYS_RDINT = 213;
  private static final int JOPSYS_WRINT = 214;
  private static final int JOPSYS_GETSP = 215;
  private static final int JOPSYS_SETSP = 216;
  private static final int JOPSYS_GETVP = 217;
  private static final int JOPSYS_SETVP = 218;
  private static final int JOPSYS_INT2EXT = 219;
  private static final int JOPSYS_EXT2INT = 220;
  private static final int JOPSYS_NOP = 221;
  
  private static String ILLEGAL_OPCODE = "ILLEGAL_OPCODE";

  /**
   * Names of opcodes.
   */
  protected static final String[] OPCODE_NAMES = { "nop", "aconst_null",
      "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4",
      "iconst_5", "lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2",
      "dconst_0", "dconst_1", "bipush", "sipush", "ldc", "ldc_w", "ldc2_w",
      "iload", "lload", "fload", "dload", "aload", "iload_0", "iload_1",
      "iload_2", "iload_3", "lload_0", "lload_1", "lload_2", "lload_3",
      "fload_0", "fload_1", "fload_2", "fload_3", "dload_0", "dload_1",
      "dload_2", "dload_3", "aload_0", "aload_1", "aload_2", "aload_3",
      "iaload", "laload", "faload", "daload", "aaload", "baload", "caload",
      "saload", "istore", "lstore", "fstore", "dstore", "astore", "istore_0",
      "istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1", "lstore_2",
      "lstore_3", "fstore_0", "fstore_1", "fstore_2", "fstore_3", "dstore_0",
      "dstore_1", "dstore_2", "dstore_3", "astore_0", "astore_1", "astore_2",
      "astore_3", "iastore", "lastore", "fastore", "dastore", "aastore",
      "bastore", "castore", "sastore", "pop", "pop2", "dup", "dup_x1",
      "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd", "fadd",
      "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul", "fmul", "dmul",
      "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg",
      "lneg", "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr",
      "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f",
      "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f",
      "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ifeq",
      "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne",
      "if_icmplt", "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq",
      "if_acmpne", "goto", "jsr", "ret", "tableswitch", "lookupswitch",
      "ireturn", "lreturn", "freturn", "dreturn", "areturn", "return",
      "getstatic", "putstatic", "getfield", "putfield", "invokevirtual",
      "invokespecial", "invokestatic", "invokeinterface", ILLEGAL_OPCODE,
      "new", "newarray", "anewarray", "arraylength", "athrow", "checkcast",
      "instanceof", "monitorenter", "monitorexit", "wide", "multianewarray",
      "ifnull", "ifnonnull", "goto_w", "jsr_w", "breakpoint", ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
      ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE };

  // TODO: make those missing (the rup/ms speciffic ones, but are they
  // reachable?)

  /**
   * Same as getWCET, but using the handle.
   * 
   * @param ih
   * @param pmiss true if the cache is missed and false if there is a cache hit
   * @return wcet or WCETNOTAVAILABLE (-1)
   */
  static int getCyclesFromHandle(InstructionHandle ih, boolean pmiss, int n) {
    Instruction ins = ih.getInstruction();
    int opcode = ins.getOpcode();
     
    return getCycles(opcode, pmiss, n);
  }

  /**
   * Get the name using the opcode. Used when WCA toWCAString().
   * 
   * @param opcode
   * @return name or "ILLEGAL_OPCODE"
   */
  static String getNameFromOpcode(int opcode) {
    return OPCODE_NAMES[opcode];
  }

  /**
   * See the WCET values
   * @return table body of opcodes with info
   */

  static String toWCAString() {
    StringBuffer sb = new StringBuffer();
    
    sb.append("Table of WCETInstruction cycles\n");
    sb.append("=============================================================\n");
    sb.append("Instruction               Hit cycles  Miss cycles  Mich. info\n");
    sb.append("                            n=0/1000     n=0/1000\n");
    sb.append("-------------------------------------------------------------\n");
 
    for (int op = 0; op <= 255; op++) {
      // name (25)
      String str = new String("[" + op + "] " + getNameFromOpcode(op));
      sb.append(WU.postpad(str,25));

      //hit n={0,1000}
      String hitstr = getCycles(op, false, 0)+"/"+getCycles(op, false, 1000); 
      hitstr = WU.prepad(hitstr,12);
      
      //miss n={0,1000}
      String missstr = getCycles(op, true, 0)+"/"+getCycles(op, true, 1000);
      missstr = WU.prepad(missstr,12);
      
      sb.append(hitstr+missstr + "\n");
    }
    sb.append("=============================================================\n");
    sb.append("Info: b(n=1000)="+calculateB(1000)+" a="+a+" r="+r+" w="+w+"\n");
    sb.append("Signatures: V void, Z boolean, B byte, C char, S short, I int, J long, F float, D double, L class, [ array\n");
    return sb.toString();
  }

  /**
   * Returns the wcet count for the instruction.
   * 
   * @see table D.1 in ms thesis
   * @param opcode
   * @param pmiss true if cacle is misses and false if a cache hit
   * @return wcet cycle count or -1 if wcet not available
   */
  static int getCycles(int opcode, boolean pmiss, int n) {
    int wcet = 0;
    int b = -1;

    // cache miss
    if(pmiss){
      b=calculateB(n);
    } else // cache hit
    {
      b=0;
    }
    
    switch (opcode) {
    // NOP = 0
    case org.apache.bcel.Constants.NOP:
      wcet = 1;
      break;
    // ACONST_NULL = 1
    case org.apache.bcel.Constants.ACONST_NULL:
      wcet = 1;
      break;
    // ICONST_M1 = 2
    case org.apache.bcel.Constants.ICONST_M1:
      wcet = 1;
      break;
    // ICONST_0 = 3
    case org.apache.bcel.Constants.ICONST_0:
      wcet = 1;
      break;
    // ICONST_1 = 4
    case org.apache.bcel.Constants.ICONST_1:
      wcet = 1;
      break;
    // ICONST_2 = 5
    case org.apache.bcel.Constants.ICONST_2:
      wcet = 1;
      break;
    // ICONST_3 = 6
    case org.apache.bcel.Constants.ICONST_3:
      wcet = 1;
      break;
    // ICONST_4 = 7
    case org.apache.bcel.Constants.ICONST_4:
      wcet = 1;
      break;
    // ICONST_5 = 8
    case org.apache.bcel.Constants.ICONST_5:
      wcet = 1;
      break;
    // LCONST_0 = 9
    case org.apache.bcel.Constants.LCONST_0:
      wcet = 2;
      break;
    // LCONST_1 = 10
    case org.apache.bcel.Constants.LCONST_1:
      wcet = 2;
      break;
    // FCONST_0 = 11
    case org.apache.bcel.Constants.FCONST_0:
      wcet = -1;
      break;
    // FCONST_1 = 12
    case org.apache.bcel.Constants.FCONST_1:
      wcet = -1;
      break;
    // FCONST_2 = 13
    case org.apache.bcel.Constants.FCONST_2:
      wcet = -1;
      break;
    // DCONST_0 = 14
    case org.apache.bcel.Constants.DCONST_0:
      wcet = -1;
      break;
    // DCONST_1 = 15
    case org.apache.bcel.Constants.DCONST_1:
      wcet = -1;
      break;
    // BIPUSH = 16
    case org.apache.bcel.Constants.BIPUSH:
      wcet = 2;
      break;
    // SIPUSH = 17
    case org.apache.bcel.Constants.SIPUSH:
      wcet = 3;
      break;
    // LDC = 18
    case org.apache.bcel.Constants.LDC:
      wcet = 3 + r;
      break;
    // LDC_W = 19
    case org.apache.bcel.Constants.LDC_W:
      wcet = 4 + r;
      break;
    // LDC2_W = 20
    case org.apache.bcel.Constants.LDC2_W:
      wcet = 8 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // ILOAD = 21
    case org.apache.bcel.Constants.ILOAD:
      wcet = 2;
      break;
    // LLOAD = 22
    case org.apache.bcel.Constants.LLOAD:
      wcet = 11;
      break;
    // FLOAD = 23
    case org.apache.bcel.Constants.FLOAD:
      wcet = 2;
      break;
    // DLOAD = 24
    case org.apache.bcel.Constants.DLOAD:
      wcet = 11;
      break;
    // ALOAD = 25
    case org.apache.bcel.Constants.ALOAD:
      wcet = 2;
      break;
    // ILOAD_0 = 26
    case org.apache.bcel.Constants.ILOAD_0:
      wcet = 1;
      break;
    // ILOAD_1 = 27
    case org.apache.bcel.Constants.ILOAD_1:
      wcet = 1;
      break;
    // ILOAD_2 = 28
    case org.apache.bcel.Constants.ILOAD_2:
      wcet = 1;
      break;
    // ILOAD_3 = 29
    case org.apache.bcel.Constants.ILOAD_3:
      wcet = 1;
      break;
    // LLOAD_0 = 30
    case org.apache.bcel.Constants.LLOAD_0:
      wcet = 2;
      break;
    // LLOAD_1 = 31
    case org.apache.bcel.Constants.LLOAD_1:
      wcet = 2;
      break;
    // LLOAD_2 = 32
    case org.apache.bcel.Constants.LLOAD_2:
      wcet = 2;
      break;
    // LLOAD_3 = 33
    case org.apache.bcel.Constants.LLOAD_3:
      wcet = 11;
      break;
    // FLOAD_0 = 34
    case org.apache.bcel.Constants.FLOAD_0:
      wcet = 1;
      break;
    // FLOAD_1 = 35
    case org.apache.bcel.Constants.FLOAD_1:
      wcet = 1;
      break;
    // FLOAD_2 = 36
    case org.apache.bcel.Constants.FLOAD_2:
      wcet = 1;
      break;
    // FLOAD_3 = 37
    case org.apache.bcel.Constants.FLOAD_3:
      wcet = 1;
      break;
    // DLOAD_0 = 38
    case org.apache.bcel.Constants.DLOAD_0:
      wcet = 2;
      break;
    // DLOAD_1 = 39
    case org.apache.bcel.Constants.DLOAD_1:
      wcet = 2;
      break;
    // DLOAD_2 = 40
    case org.apache.bcel.Constants.DLOAD_2:
      wcet = 2;
      break;
    // DLOAD_3 = 41
    case org.apache.bcel.Constants.DLOAD_3:
      wcet = 11;
      break;
    // ALOAD_0 = 42
    case org.apache.bcel.Constants.ALOAD_0:
      wcet = 1;
      break;
    // ALOAD_1 = 43
    case org.apache.bcel.Constants.ALOAD_1:
      wcet = 1;
      break;
    // ALOAD_2 = 44
    case org.apache.bcel.Constants.ALOAD_2:
      wcet = 1;
      break;
    // ALOAD_3 = 45
    case org.apache.bcel.Constants.ALOAD_3:
      wcet = 1;
      break;
    // IALOAD = 46
    case org.apache.bcel.Constants.IALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // LALOAD = 47
    case org.apache.bcel.Constants.LALOAD:
      wcet = -1;
      break;
    // FALOAD = 48
    case org.apache.bcel.Constants.FALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // DALOAD = 49
    case org.apache.bcel.Constants.DALOAD:
      wcet = -1;
      break;
    // AALOAD = 50
    case org.apache.bcel.Constants.AALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // BALOAD = 51
    case org.apache.bcel.Constants.BALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // CALOAD = 52
    case org.apache.bcel.Constants.CALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // SALOAD = 53
    case org.apache.bcel.Constants.SALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // ISTORE = 54
    case org.apache.bcel.Constants.ISTORE:
      wcet = 2;
      break;
    // LSTORE = 55
    case org.apache.bcel.Constants.LSTORE:
      wcet = 11;
      break;
    // FSTORE = 56
    case org.apache.bcel.Constants.FSTORE:
      wcet = 2;
      break;
    // DSTORE = 57
    case org.apache.bcel.Constants.DSTORE:
      wcet = 11;
      break;
    // ASTORE = 58
    case org.apache.bcel.Constants.ASTORE:
      wcet = 2;
      break;
    // ISTORE_0 = 59
    case org.apache.bcel.Constants.ISTORE_0:
      wcet = 1;
      break;
    // ISTORE_1 = 60
    case org.apache.bcel.Constants.ISTORE_1:
      wcet = 1;
      break;
    // ISTORE_2 = 61
    case org.apache.bcel.Constants.ISTORE_2:
      wcet = 1;
      break;
    // ISTORE_3 = 62
    case org.apache.bcel.Constants.ISTORE_3:
      wcet = 1;
      break;
    // LSTORE_0 = 63
    case org.apache.bcel.Constants.LSTORE_0:
      wcet = 2;
      break;
    // LSTORE_1 = 64
    case org.apache.bcel.Constants.LSTORE_1:
      wcet = 2;
      break;
    // LSTORE_2 = 65
    case org.apache.bcel.Constants.LSTORE_2:
      wcet = 2;
      break;
    // LSTORE_3 = 66
    case org.apache.bcel.Constants.LSTORE_3:
      wcet = 11;
      break;
    // FSTORE_0 = 67
    case org.apache.bcel.Constants.FSTORE_0:
      wcet = 1;
      break;
    // FSTORE_1 = 68
    case org.apache.bcel.Constants.FSTORE_1:
      wcet = 1;
      break;
    // FSTORE_2 = 69
    case org.apache.bcel.Constants.FSTORE_2:
      wcet = 1;
      break;
    // FSTORE_3 = 70
    case org.apache.bcel.Constants.FSTORE_3:
      wcet = 1;
      break;
    // DSTORE_0 = 71
    case org.apache.bcel.Constants.DSTORE_0:
      wcet = 2;
      break;
    // DSTORE_1 = 72
    case org.apache.bcel.Constants.DSTORE_1:
      wcet = 2;
      break;
    // DSTORE_2 = 73
    case org.apache.bcel.Constants.DSTORE_2:
      wcet = 2;
      break;
    // DSTORE_3 = 74
    case org.apache.bcel.Constants.DSTORE_3:
      wcet = 11;
      break;
    // ASTORE_0 = 75
    case org.apache.bcel.Constants.ASTORE_0:
      wcet = 1;
      break;
    // ASTORE_1 = 76
    case org.apache.bcel.Constants.ASTORE_1:
      wcet = 1;
      break;
    // ASTORE_2 = 77
    case org.apache.bcel.Constants.ASTORE_2:
      wcet = 1;
      break;
    // ASTORE_3 = 78
    case org.apache.bcel.Constants.ASTORE_3:
      wcet = 1;
      break;
    // IASTORE = 79
    case org.apache.bcel.Constants.IASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // LASTORE = 80
    case org.apache.bcel.Constants.LASTORE:
      wcet = -1;
      break;
    // FASTORE = 81
    case org.apache.bcel.Constants.FASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // DASTORE = 82
    case org.apache.bcel.Constants.DASTORE:
      wcet = -1;
      break;
    // AASTORE = 83
    case org.apache.bcel.Constants.AASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // BASTORE = 84
    case org.apache.bcel.Constants.BASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // CASTORE = 85
    case org.apache.bcel.Constants.CASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // SASTORE = 86
    case org.apache.bcel.Constants.SASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // POP = 87
    case org.apache.bcel.Constants.POP:
      wcet = 1;
      break;
    // POP2 = 88
    case org.apache.bcel.Constants.POP2:
      wcet = 2;
      break;
    // DUP = 89
    case org.apache.bcel.Constants.DUP:
      wcet = 1;
      break;
    // DUP_X1 = 90
    case org.apache.bcel.Constants.DUP_X1:
      wcet = 5;
      break;
    // DUP_X2 = 91
    case org.apache.bcel.Constants.DUP_X2:
      wcet = -1;
      break;
    // DUP2 = 92
    case org.apache.bcel.Constants.DUP2:
      wcet = 6;
      break;
    // DUP2_X1 = 93
    case org.apache.bcel.Constants.DUP2_X1:
      wcet = -1;
      break;
    // DUP2_X2 = 94
    case org.apache.bcel.Constants.DUP2_X2:
      wcet = -1;
      break;
    // SWAP = 95
    case org.apache.bcel.Constants.SWAP:
      wcet = -1;
      break;
    // IADD = 96
    case org.apache.bcel.Constants.IADD:
      wcet = 1;
      break;
    // LADD = 97
    case org.apache.bcel.Constants.LADD:
      wcet = -1;
      break;
    // FADD = 98
    case org.apache.bcel.Constants.FADD:
      wcet = -1;
      break;
    // DADD = 99
    case org.apache.bcel.Constants.DADD:
      wcet = -1;
      break;
    // ISUB = 100
    case org.apache.bcel.Constants.ISUB:
      wcet = 1;
      break;
    // LSUB = 101
    case org.apache.bcel.Constants.LSUB:
      wcet = -1;
      break;
    // FSUB = 102
    case org.apache.bcel.Constants.FSUB:
      wcet = -1;
      break;
    // DSUB = 103
    case org.apache.bcel.Constants.DSUB:
      wcet = -1;
      break;
    // IMUL = 104
    case org.apache.bcel.Constants.IMUL:
      wcet = 35;
      break;
    // LMUL = 105
    case org.apache.bcel.Constants.LMUL:
      wcet = -1;
      break;
    // FMUL = 106
    case org.apache.bcel.Constants.FMUL:
      wcet = -1;
      break;
    // DMUL = 107
    case org.apache.bcel.Constants.DMUL:
      wcet = -1;
      break;
    // IDIV = 108
    case org.apache.bcel.Constants.IDIV:
      wcet = -1;
      break;
    // LDIV = 109
    case org.apache.bcel.Constants.LDIV:
      wcet = -1;
      break;
    // FDIV = 110
    case org.apache.bcel.Constants.FDIV:
      wcet = -1;
      break;
    // DDIV = 111
    case org.apache.bcel.Constants.DDIV:
      wcet = -1;
      break;
    // IREM = 112
    case org.apache.bcel.Constants.IREM:
      wcet = -1;
      break;
    // LREM = 113
    case org.apache.bcel.Constants.LREM:
      wcet = -1;
      break;
    // FREM = 114
    case org.apache.bcel.Constants.FREM:
      wcet = -1;
      break;
    // DREM = 115
    case org.apache.bcel.Constants.DREM:
      wcet = -1;
      break;
    // INEG = 116
    case org.apache.bcel.Constants.INEG:
      wcet = 4;
      break;
    // LNEG = 117
    case org.apache.bcel.Constants.LNEG:
      wcet = -1;
      break;
    // FNEG = 118
    case org.apache.bcel.Constants.FNEG:
      wcet = -1;
      break;
    // DNEG = 119
    case org.apache.bcel.Constants.DNEG:
      wcet = -1;
      break;
    // ISHL = 120
    case org.apache.bcel.Constants.ISHL:
      wcet = 1;
      break;
    // LSHL = 121
    case org.apache.bcel.Constants.LSHL:
      wcet = -1;
      break;
    // ISHR = 122
    case org.apache.bcel.Constants.ISHR:
      wcet = 1;
      break;
    // LSHR = 123
    case org.apache.bcel.Constants.LSHR:
      wcet = -1;
      break;
    // IUSHR = 124
    case org.apache.bcel.Constants.IUSHR:
      wcet = 1;
      break;
    // LUSHR = 125
    case org.apache.bcel.Constants.LUSHR:
      wcet = -1;
      break;
    // IAND = 126
    case org.apache.bcel.Constants.IAND:
      wcet = 1;
      break;
    // LAND = 127
    case org.apache.bcel.Constants.LAND:
      wcet = -1;
      break;
    // IOR = 128
    case org.apache.bcel.Constants.IOR:
      wcet = 1;
      break;
    // LOR = 129
    case org.apache.bcel.Constants.LOR:
      wcet = -1;
      break;
    // IXOR = 130
    case org.apache.bcel.Constants.IXOR:
      wcet = 1;
      break;
    // LXOR = 131
    case org.apache.bcel.Constants.LXOR:
      wcet = -1;
      break;
    // IINC = 132
    case org.apache.bcel.Constants.IINC:
      wcet = 8;
      break;
    // I2L = 133
    case org.apache.bcel.Constants.I2L:
      wcet = -1;
      break;
    // I2F = 134
    case org.apache.bcel.Constants.I2F:
      wcet = -1;
      break;
    // I2D = 135
    case org.apache.bcel.Constants.I2D:
      wcet = -1;
      break;
    // L2I = 136
    case org.apache.bcel.Constants.L2I:
      wcet = 3;
      break;
    // L2F = 137
    case org.apache.bcel.Constants.L2F:
      wcet = -1;
      break;
    // L2D = 138
    case org.apache.bcel.Constants.L2D:
      wcet = -1;
      break;
    // F2I = 139
    case org.apache.bcel.Constants.F2I:
      wcet = -1;
      break;
    // F2L = 140
    case org.apache.bcel.Constants.F2L:
      wcet = -1;
      break;
    // F2D = 141
    case org.apache.bcel.Constants.F2D:
      wcet = -1;
      break;
    // D2I = 142
    case org.apache.bcel.Constants.D2I:
      wcet = -1;
      break;
    // D2L = 143
    case org.apache.bcel.Constants.D2L:
      wcet = -1;
      break;
    // D2F = 144
    case org.apache.bcel.Constants.D2F:
      wcet = -1;
      break;
    // I2B = 145
    case org.apache.bcel.Constants.I2B:
      wcet = -1;
      break;
    // INT2BYTE = 145 // Old notion
    // case org.apache.bcel.Constants.INT2BYTE : wcet = -1; break;
    // I2C = 146
    case org.apache.bcel.Constants.I2C:
      wcet = 2;
      break;
    // INT2CHAR = 146 // Old notion
    // case org.apache.bcel.Constants.INT2CHAR : wcet = -1; break;
    // I2S = 147
    case org.apache.bcel.Constants.I2S:
      wcet = -1;
      break;
    // INT2SHORT = 147 // Old notion
    // case org.apache.bcel.Constants.INT2SHORT : wcet = -1; break;
    // LCMP = 148
    case org.apache.bcel.Constants.LCMP:
      wcet = -1;
      break;
    // FCMPL = 149
    case org.apache.bcel.Constants.FCMPL:
      wcet = -1;
      break;
    // FCMPG = 150
    case org.apache.bcel.Constants.FCMPG:
      wcet = -1;
      break;
    // DCMPL = 151
    case org.apache.bcel.Constants.DCMPL:
      wcet = -1;
      break;
    // DCMPG = 152
    case org.apache.bcel.Constants.DCMPG:
      wcet = -1;
      break;
    // IFEQ = 153
    case org.apache.bcel.Constants.IFEQ:
      wcet = 4;
      break;
    // IFNE = 154
    case org.apache.bcel.Constants.IFNE:
      wcet = 4;
      break;
    // IFLT = 155
    case org.apache.bcel.Constants.IFLT:
      wcet = 4;
      break;
    // IFGE = 156
    case org.apache.bcel.Constants.IFGE:
      wcet = 4;
      break;
    // IFGT = 157
    case org.apache.bcel.Constants.IFGT:
      wcet = 4;
      break;
    // IFLE = 158
    case org.apache.bcel.Constants.IFLE:
      wcet = 4;
      break;
    // IF_ICMPEQ = 159
    case org.apache.bcel.Constants.IF_ICMPEQ:
      wcet = 4;
      break;
    // IF_ICMPNE = 160
    case org.apache.bcel.Constants.IF_ICMPNE:
      wcet = 4;
      break;
    // IF_ICMPLT = 161
    case org.apache.bcel.Constants.IF_ICMPLT:
      wcet = 4;
      break;
    // IF_ICMPGE = 162
    case org.apache.bcel.Constants.IF_ICMPGE:
      wcet = 4;
      break;
    // IF_ICMPGT = 163
    case org.apache.bcel.Constants.IF_ICMPGT:
      wcet = 4;
      break;
    // IF_ICMPLE = 164
    case org.apache.bcel.Constants.IF_ICMPLE:
      wcet = 4;
      break;
    // IF_ACMPEQ = 165
    case org.apache.bcel.Constants.IF_ACMPEQ:
      wcet = 4;
      break;
    // IF_ACMPNE = 166
    case org.apache.bcel.Constants.IF_ACMPNE:
      wcet = 4;
      break;
    // GOTO = 167
    case org.apache.bcel.Constants.GOTO:
      wcet = 4;
      break;
    // JSR = 168
    case org.apache.bcel.Constants.JSR:
      wcet = -1;
      break;
    // RET = 169
    case org.apache.bcel.Constants.RET:
      wcet = -1; // TODO: Should this be 1?
      break;
    // TABLESWITCH = 170
    case org.apache.bcel.Constants.TABLESWITCH:
      wcet = -1;
      break;
    // LOOKUPSWITCH = 171
    case org.apache.bcel.Constants.LOOKUPSWITCH:
      wcet = -1;
      break;
    // IRETURN = 172
    case org.apache.bcel.Constants.IRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 8) {
        wcet += b - 8;
      } else {
        wcet += 0;
      }
      break;
    // LRETURN = 173
    case org.apache.bcel.Constants.LRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 9) {
        wcet += b - 9;
      } else {
        wcet += 0;
      }
      break;
    // FRETURN = 174
    case org.apache.bcel.Constants.FRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 8) {
        wcet += b - 8;
      } else {
        wcet += 0;
      }
      break;
    // DRETURN = 175
    case org.apache.bcel.Constants.DRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 9) {
        wcet += b - 9;
      } else {
        wcet += 0;
      }
      break;
    // ARETURN = 176
    case org.apache.bcel.Constants.ARETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 8) {
        wcet += b - 8;
      } else {
        wcet += 0;
      }
      break;
    // RETURN = 177
    case org.apache.bcel.Constants.RETURN:
      wcet = 13;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 7) {
        wcet += b - 7;
      } else {
        wcet += 0;
      }
      break;
    // GETSTATIC = 178
    case org.apache.bcel.Constants.GETSTATIC:
      wcet = 4 + 2 * r;
      break;
    // PUTSTATIC = 179
    case org.apache.bcel.Constants.PUTSTATIC:
      wcet = 5 + r + w;
      break;
    // GETFIELD = 180
    case org.apache.bcel.Constants.GETFIELD:
      wcet = 10 + 2 * r;
      break;
    // PUTFIELD = 181
    case org.apache.bcel.Constants.PUTFIELD:
      wcet = 13 + r + w;
      break;
    // INVOKEVIRTUAL = 182
    case org.apache.bcel.Constants.INVOKEVIRTUAL:
      wcet = 78 + 2 * r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // INVOKESPECIAL = 183
    case org.apache.bcel.Constants.INVOKESPECIAL:
      wcet = 58 + r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // INVOKENONVIRTUAL = 183
    // case org.apache.bcel.Constants.INVOKENONVIRTUAL : wcet = -1; break;
    // INVOKESTATIC = 184
    case org.apache.bcel.Constants.INVOKESTATIC:
      wcet = 58 + r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // INVOKEINTERFACE = 185
    case org.apache.bcel.Constants.INVOKEINTERFACE:
      wcet = 84 + 4 * r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // NEW = 187
    case org.apache.bcel.Constants.NEW:
      wcet = -1;
      break;
    // NEWARRAY = 188
    case org.apache.bcel.Constants.NEWARRAY:
      wcet = 12 + w; // TODO: Time to clear array not included
      break;
    // ANEWARRAY = 189
    case org.apache.bcel.Constants.ANEWARRAY:
      wcet = -1;
      break;
    // ARRAYLENGTH = 190
    case org.apache.bcel.Constants.ARRAYLENGTH:
      wcet = 2 + r;
      break;
    // ATHROW = 191
    case org.apache.bcel.Constants.ATHROW:
      wcet = -1;
      break;
    // CHECKCAST = 192
    case org.apache.bcel.Constants.CHECKCAST:
      wcet = -1;
      break;
    // INSTANCEOF = 193
    case org.apache.bcel.Constants.INSTANCEOF:
      wcet = -1;
      break;
    // MONITORENTER = 194
    case org.apache.bcel.Constants.MONITORENTER:
      wcet = 9;
      break;
    // MONITOREXIT = 195
    case org.apache.bcel.Constants.MONITOREXIT:
      wcet = 10;
      wcet = 11; // TODO: Which one to keep?
      break;
    // WIDE = 196
    case org.apache.bcel.Constants.WIDE:
      wcet = -1;
      break;
    // MULTIANEWARRAY = 197
    case org.apache.bcel.Constants.MULTIANEWARRAY:
      wcet = -1;
      break;
    // IFNULL = 198
    case org.apache.bcel.Constants.IFNULL:
      wcet = 4;
      break;
    // IFNONNULL = 199
    case org.apache.bcel.Constants.IFNONNULL:
      wcet = 4;
      break;
    // GOTO_W = 200
    case org.apache.bcel.Constants.GOTO_W:
      wcet = -1;
      break;
    // JSR_W = 201
    case org.apache.bcel.Constants.JSR_W:
      wcet = -1;
      break;
    // JOPSYS_RD = 209   
    case JOPSYS_RD:
      wcet = 3;
      break;
    // JOPSYS_WR = 210
    case JOPSYS_WR:
      wcet = 3;
      break;
    // JOPSYS_RDMEM = 211
    case JOPSYS_RDMEM:
      wcet = r;
      break;
    // JOPSYS_WRMEM = 212
    case JOPSYS_WRMEM:
      wcet = w+1;
      break;
    // JOPSYS_RDINT = 213
    case JOPSYS_RDINT:
      wcet = 8;
      break;
    // JOPSYS_WRINT = 214
    case JOPSYS_WRINT:
      wcet = 8;
      break;
    // JOPSYS_GETSP = 215
    case JOPSYS_GETSP:
      wcet = 3;
      break;
    // JOPSYS_SETSP = 216
    case JOPSYS_SETSP:
      wcet = 4;
      break;
    // JOPSYS_GETVP = 217
    case JOPSYS_GETVP:
      wcet = 1;
      break;
    // JOPSYS_SETVP = 218
    case JOPSYS_SETVP:
      wcet = 2;
      break;
    // JOPSYS_INT2EXT = 219
    case JOPSYS_INT2EXT:
      wcet = 12;
      if(w>=12){
        wcet+=n*(19+w-8);
      }else
      {
        wcet+=n*(19+4);
      }
      break;
    // JOPSYS_EXT2INT = 220
    case JOPSYS_EXT2INT:
      wcet = 12;
      if(w>=14){
        wcet+=n*(19+w-10);
      }else
      {
        wcet+=n*(19+4);
      }
      break;
      // JOPSYS_NOP = 221
    case JOPSYS_NOP:
      wcet = 1;
      break;
      
    default:
      wcet = -1;
      break;
    }
    // TODO: Add the JOP speciffic codes?
    return wcet;
  }

  /**
   * Check to see if there is a valid WCET count for the instruction.
   * 
   * @param opcode
   * @return true if there is a valid wcet value
   */
  static boolean wcetAvailable(int opcode) {
    if (getCycles(opcode, false, 0) == WCETNOTAVAILABLE)
      return false;
    else
      return true;
  }

  /**
   * Method load time on invoke or return if there is a cache miss (see pMiss).
   * 
   * @see ms thesis p 232
   */
  public static int calculateB(int n) {
    int b = -1;
    if (n == -1) {
      System.err.println("n not set!");
      System.exit(-1);
    } else {
      b = 2 + (n + 1) * a;
    }
    return b;
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

}