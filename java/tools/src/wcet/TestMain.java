/**
 * 
 */
package wcet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Elena Axamitova
 * @version 0.1 30.06.2007
 */
public class TestMain {

    public static void main(String[] args){
	try {
	    ClassReader reader = new ClassReader(new FileInputStream("D:\\Studium\\DA\\new\\jop\\java\\target\\src\\jdk_base\\java\\lang\\String.class"));
	    ClassNode clNode = new ClassNode();
	    reader.accept(clNode, ClassReader.SKIP_FRAMES);
	    List list = clNode.methods;
	    for(Iterator iterator = list.iterator();iterator.hasNext();){
		MethodNode node = (MethodNode)iterator.next();
		if(node.name.equals("charAt"))
		    System.out.println("HIT");
		System.out.println(node.name+" "+node.desc+" ");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
