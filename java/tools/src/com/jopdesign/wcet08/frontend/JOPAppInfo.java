/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.frontend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.util.ClassPath;
import com.jopdesign.build.AppInfo;
import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.InsertSynchronized;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.build.ReplaceIinc;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Config.MissingConfigurationError;
import com.jopdesign.wcet08.graphutils.Pair;
import com.jopdesign.wcet08.report.InvokeDot;

/**
 * AppInfo subclass for the WCET analysis.
 * Provides a TypeGraph.
 * 
 * @author Benedikt Huber, benedikt.huber@gmail.com
 */
public class JOPAppInfo extends AppInfo {
	private static final long serialVersionUID = 2L;
	/**
	 * Raised when we cannot find / fail to load a referenced method.
	 */
	public class MethodNotFoundException extends Exception {
		private static final long serialVersionUID = 1L;
		public MethodNotFoundException(String message) {
			super(message);			
		}
	}	
	private TypeGraph typeGraph;
	
	public JOPAppInfo() {
		super(ClassInfo.getTemplate());
	}
	/**
	 * load all classes reachable from the given class
	 * @param topClass 
	 * @throws IOException
	 */
	public void loadClasses(String topClass) throws IOException {
		this.classpath = new ClassPath(Config.instance().getClassPath());
		this.srcPath = Config.instance().getSourcePath();
		addClass(topClass);
		load();
		iterate(new ReplaceIinc(this));
		iterate(new InsertSynchronized(this));
		this.typeGraph = new TypeGraph(this);
	}
	/**
	 * @return A mapping from the name of a loaded class to {@link ClassInfo}.
	 */
	public Map<String, ? extends ClassInfo> getCliMap() {
		return cliMap;
	}
	/**
	 * @return The typegraph of all loaded classes
	 */
	public TypeGraph getTypeGraph() {
		return typeGraph;
	}
	/**
	 * @param className Name of the class to lookup
	 * @return the class info, or null if the class could'nt be found
	 */
	public ClassInfo getClassInfo(String className) {
		return this.cliMap.get(className);
	}

	/**
	 * Find the given method
	 * @param className The fully qualified name of the class the method is located in
	 * @param methodSig The name of the method to be searched. 
	 * 				    Signature is optional if the method name is unique.
	 * @return The method searched for, or null if it couldn't be found
	 * @throws MethodNotFoundException if the method couldn't be found or is ambigous
	 */
	public MethodInfo searchMethod(String className, String methodSig) throws MethodNotFoundException {
		ClassInfo cli = cliMap.get(className);
		if(cli == null) throw new MethodNotFoundException("The class "+className+" couldn't be found");
		return searchMethod(cli,methodSig);
	}
	private MethodInfo searchMethod(ClassInfo cli, String methodSig) throws MethodNotFoundException {
		MethodInfo mi = null;
		if(methodSig.indexOf("(") > 0) {
			mi = cli.getMethodInfo(methodSig);
		} else {
			for(MethodInfo candidate : cli.getMethods()) {
				if(methodSig.equals(candidate.getMethod().getName())) {
					if(mi == null) {
						mi = candidate;
					} else {
						throw new MethodNotFoundException("The method name "+methodSig+" is ambigous."+
														  "Both "+mi.methodId+" and "+candidate.methodId+" match");
					}
				}
			}			
		}
		if(mi == null) {
			throw new MethodNotFoundException("The method "+cli.toString()+"."+methodSig+" could not be found");
		}
		return mi;
	}
	/**
	 * Return the receiver name and method name of a 
	 * method referenced by the given invoke instruction
	 * @param invokerCi the classinfo of the method which contains the {@link InvokeInstruciton}
	 * @param instr the invoke instruction
	 * @return A pair of class info and method name
	 */
	public Pair<ClassInfo, String> getReferenced(ClassInfo invokerCi, InvokeInstruction instr) {
		ConstantPoolGen cpg = new ConstantPoolGen(invokerCi.clazz.getConstantPool());
		String classname = instr.getClassName(cpg );
		String methodname = instr.getMethodName(cpg) + instr.getSignature(cpg);
		ClassInfo refCi = getClassInfo(classname);
		if(refCi == null) throw new AssertionError("Failed class lookup (invoke target): "+classname);
		return new Pair<ClassInfo,String>(refCi,methodname);
	}
	public Pair<ClassInfo, String> getReferenced(MethodInfo method, InvokeInstruction instr) {
		return getReferenced(method.getCli(),instr);
	}

	public MethodInfo findStaticImplementation(ClassInfo receiver, String methodRef) {
		MethodInfo staticImpl = receiver.getMethodInfo(methodRef);
		if(staticImpl == null) {
			ClassInfo superRec = receiver;
			while(staticImpl == null && superRec != null) {
				staticImpl = superRec.getMethodInfo(methodRef);
				if(superRec.clazz.getSuperClass() == null) superRec = null;
				else superRec = superRec.superClass;
			}
		}		
		return staticImpl;
	}

	/**
	 * Find possible implementations of the given method in the given class
	 * <p>
	 * For all candidates, check whether they implement the method.
	 * All subclasses of the receiver class are candidates. If the method isn't implemented
	 * in the receiver, the lowest superclass implementing the method is a candidate too.
	 * </p>
	 * @param receiver The class info of the receiver
	 * @param methodname The method name
	 * @return list of method infos that might be invoked
	 */
	public List<MethodInfo> findImplementations(ClassInfo receiver, String methodname) {
		Vector<MethodInfo> impls = new Vector<MethodInfo>(3);
		tryAddImpl(impls,findStaticImplementation(receiver, methodname));
		for(ClassInfo subty : this.typeGraph.getStrictSubtypes(receiver)) {
			MethodInfo subtyImpl = subty.getMethodInfo(methodname);
			tryAddImpl(impls,subtyImpl);
		}
		return impls;
	}
	/* helper to avoid code dupl */
	private void tryAddImpl(List<MethodInfo> ms, MethodInfo m) {
		if(m != null) {
			if(! m.getMethod().isAbstract() && ! m.getMethod().isInterface()) {
				ms.add(m);
			}
		}		
	}

	/**
	 * check whether we need to deal with the given statement in a special way,
	 * because it is translated to a JOP specific microcode sequence
     *
	 * @param instr the instruction to check
	 * @return true, if this is translated to a JOP specific bytecode
	 */
	public boolean isSpecialInvoke(ClassInfo ci, Instruction i) {		
		if(! (i instanceof INVOKESTATIC)) return false;
		ConstantPoolGen cpg = new ConstantPoolGen(ci.clazz.getConstantPool());
		String classname = ((INVOKESTATIC) i).getClassName(cpg);
		return (classname.equals("com.jopdesign.sys.Native"));		
	}
	public boolean isSpecialInvoke(MethodInfo methodInfo, Instruction i) {
		return isSpecialInvoke(methodInfo.getCli(),i);
	}

	/**
	 * Get the (actual) opcode of a statement, as executed on JOP
	 * FIXME: handle java-implemented bytecodes [1]
	 * @param instr the BCEL instructions
	 * @return
	 */
	public int getJOpCode(ClassInfo ci, Instruction instr) {
		if(isSpecialInvoke(ci,instr)) {
			ConstantPoolGen cpg = new ConstantPoolGen(ci.clazz.getConstantPool());
			String methodName = ((INVOKESTATIC) instr).getMethodName(cpg);			
			return JopInstr.getNative(methodName);
		} else {
			return instr.getOpcode();
		}
	}

	/*
	 * DEMO
	 * ~~~~
	 */

	public static String USAGE = 
		"Usage: java [-Dconfig=file://<config.props>] "+ 
		JOPAppInfo.class.getCanonicalName()+
		" [-outdir outdir] [-cp classpath] package.rootclass.rootmethod";

	/* small demo using the class loader */	
	public static void main(String[] argv) {

		try {
			String[] argvrest = Config.load(System.getProperty("config"), argv);
			Config config = Config.instance();
			config.setProjectName("typegraph");
			if(argvrest.length == 1) config.setTarget(argvrest[0]);
			config.initializeReport();
			config.checkPresent(Config.CLASSPATH_PROPERTY);
			config.checkPresent(Config.ROOT_CLASS_NAME);
			config.checkPresent(Config.ROOT_METHOD_NAME);
			config.checkPresent(Config.REPORTDIR_PROPERTY);
			config.checkPresent(Config.PROGRAM_DOT);
		} catch(MissingConfigurationError e) {
			System.err.println(e);
			System.err.println(USAGE);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		JOPAppInfo cl = new JOPAppInfo();
		try {
			Config config = Config.instance();
			System.out.println("Classloader Demo: "+config.getRootClassName() + "." + config.getRootMethodName());
			String rootClass = config.getRootClassName();
			String rootPkg = rootClass.substring(0,rootClass.lastIndexOf("."));
			cl.loadClasses(rootClass);
			ClassInfo ci = cl.getClassInfo(config.getRootClassName());
			System.out.println("Source file: "+ci.clazz.getSourceFileName());
			System.out.println("Root class: "+ci.clazz.toString());
			{ 
				System.out.println("Writing type graph to "+config.getOutFile("typegraph.png"));
				File dotFile = config.getOutFile("typegraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				cl.getTypeGraph().exportDOT(dotWriter,rootPkg);			
				dotWriter.close();			
				InvokeDot.invokeDot(dotFile, config.getOutFile("typegraph.png"));
			}
			CallGraph cg = CallGraph.buildCallGraph(cl, config.getRootClassName(), config.getRootMethodName());			
			{
				System.out.println("Writing call graph to "+config.getOutFile("callgraph.png"));
				File dotFile = config.getOutFile("callgraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				cg.exportDOT(dotWriter);			
				dotWriter.close();			
				InvokeDot.invokeDot(dotFile, config.getOutFile("callgraph.png"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MethodNotFoundException e) {
			e.printStackTrace();
		}
	}
}

