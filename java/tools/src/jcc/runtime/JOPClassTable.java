package runtime;

import java.util.*;
import jcc.Util;
import components.*;
import vm.*;


public class JOPClassTable extends JOPHashtable { 

    JOPNameTable nameTable;

    JOPClassTable(JOPNameTable nameTable) { 
	super(32, JOPClassName.class); 
	this.nameTable = nameTable;
    }

    public int getClassKey(String name) { 
	return getClassKey(new JOPClassName(name));
    }
    
    public int getClassKey(JOPClassName cn) {
	int key;
	if (cn.getDepth() > 6) { 
	    key = getKey(cn) | (7 << 13);
	} else if (cn.getBaseType() != 0) { 
	    key = (cn.getDepth() << 13) + (int)cn.getBaseType();
        } else { 
            JOPClassName entry = 
                cn.getDepth() == 0 
		? cn : new JOPClassName(cn.getPackageName(), cn.getBaseName(), 0);
            key = getKey(entry) | (cn.getDepth() << 13);
        }
        return key;
   }
   
   public void addArrayClass(String name) {
       JOPClassName cn = new  JOPClassName(name);
       addArrayClass(cn);
   }

   private void addArrayClass(JOPClassName cn) {
       for(;;) {
           addEntry(cn);
           if (cn.getDepth() <= (cn.isPrimitiveBaseType() ? 1 : 0)) {
                break;
           }
           cn = cn.deltaDepth(-1);
       }
   }

    public String getKeyClass(int value) { 
        return ((JOPClassName)getObject(value)).toString();
    }

    public void addNewEntryCallback(int bucket, Object neww) {
        JOPClassName cn = (JOPClassName)neww;
        /* Make sure that the package name and basic name have keys */
        String packageName = cn.getPackageName();
        if (packageName != null) { 
            nameTable.getKey(packageName);
        }
        nameTable.getKey(cn.getFullBaseName());

        if (cn.isArrayClass() && cn.getDepth() <= 6) { 
            setKey(cn, getClassKey(cn));
            return;
        } 

        int newKey = 256 + bucket; // default value
        for (JOPClassName next = (JOPClassName)getNext(cn); 
	         next != null; 
                 next = (JOPClassName)getNext(next)) {
            int value = getKey(next);
            int depth = value >> 13;
            if (value == -1) { 
                System.out.println("Strangeness in addNewEntryCallback");
            } else if (depth == 0 || depth == 7) { 
                newKey = (value & 0x1FFF) + getSize();
                break;
            }
        }
        setKey(cn, newKey);
        if (cn.isArrayClass()) { 
            addArrayClass(cn.deltaDepth(-1));
        }
    }

    Object tableChain(CCodeWriter out, int bucket, Object[] list) {
        return (list.length == 0) ? null : list[0];
    }


    void tableEntry(CCodeWriter out, int bucket, Object token) { 
        JOPClassName name = (JOPClassName)token;
        EVMClass cc = name.getEVMClass();
        if (cc != null) { 
            out.print("&" + cc.getNativeName() + "_Classblock");
        } else { 
            out.print("&" + Util.convertToClassName(name.toString()) + 
                      "_Classblock");
        }
        out.print(" /* " + name.toString() + " */");
    }

    public NameAndTypeKey getNameAndTypeKey(ClassMemberInfo cmi) { 
        return getNameAndTypeKey(cmi.name.string, cmi.type.string);
    }

    public NameAndTypeKey getNameAndTypeKey(String name, String type) {
        int nameKey = getNameKey(name);
        int typeKey;
        if (type.charAt(0) == '(') { 
            typeKey = getNameKey(encodeMethodSignature(type));
        } else { 
            typeKey = getFieldSignatureKey(type);
        }
        return new NameAndTypeKey(nameKey, typeKey);
    }
    
    public int getNameKey(String name) { 
        return nameTable.getNameKey(name);
    }

    
    public int getClassKey(UnicodeConstant string) { 
        return getClassKey(string.string);
    }

    public int getFieldSignatureKey(String string) { 
        int length = string.length();
        char firstChar = string.charAt(0);
        if (length == 1) { 
            if (firstChar >= 'A' && firstChar <= 'Z') { 
                return string.charAt(0) & 0x7F;
            }
        } else if (firstChar == '[') { 
            return getClassKey(string);
        } else if (firstChar == 'L' && string.charAt(length - 1) == ';') { 
            return getClassKey(string.substring(1, string.length() - 1));
        }
        throw new NullPointerException("Unknown signature: " + string);
    }

    
    public String encodeMethodSignature(String string) { 
        int index = 1;          // skip over the opening parenthesis
        int argCount = 0;
        StringBuffer sb = new StringBuffer().append('\0');
        while(string.charAt(index) != ')') {
            index = encodeMethodSignature(string, index, sb);
            argCount++;
        }
        index++;                // skip over the close parenthesis 
        sb.setCharAt(0, (char)argCount);

        // Add the return value to the string
        encodeMethodSignature(string, index, sb);

        return sb.toString();
    }
        
    public String decodeMethodSignature(String string) { 
        int index = 0;
        int argCount = string.charAt(index++);
        StringBuffer result = new StringBuffer().append('(');
        for (int i = 0; i < argCount; i++) { 
            index = decodeMethodSignature(string, index, result);
        }
        result.append(')');
        decodeMethodSignature(string, index, result);
        return result.toString();
    } 

    private int 
    encodeMethodSignature(String string, int index, StringBuffer result) {
        int end;
        switch(string.charAt(index)) { 
            default:
                // A primitive type
                result.append(string.charAt(index));
                return index + 1;

            case 'L': { 
                // A class
                end = string.indexOf(';', index) + 1;
                break;
            }

            case '[': { 
                // An array
                for (end = index + 1; string.charAt(end) == '['; end++);
                if (string.charAt(end) == 'L') { 
                    // Array of classes
                    end = string.indexOf(';', end) + 1;             
                } else { 
                    // Array of primitives
                    end++;
                }
                break;
            }
        }
        int key = getFieldSignatureKey(string.substring(index, end));
        char hiChar = (char)(key >> 8);
        char loChar = (char)(key & 0xFF);
        if (hiChar >= 'A' && hiChar <= 'Z') { 
            result.append('L');
        } 
        result.append(hiChar).append(loChar);
        // Return the end
        return end;
    }

    private int
    decodeMethodSignature(String string, int index, StringBuffer result) 
    { 
        int baseKey;
        int depth;
        char firstChar = string.charAt(index++);
        if (firstChar >= 'A' && firstChar <= 'Z' && firstChar != 'L') { 
            baseKey = firstChar;
            depth = 0;
        } else { 
            if (firstChar == 'L') { 
                firstChar = string.charAt(index++);
            }
            char secondChar = string.charAt(index++);
            int key = (firstChar << 8) + secondChar;
            depth = key >> 13;
            baseKey = key & 0x1FFF;
        }
        if (depth != 7) { 
            for (int i = 0; i < depth; i++) result.append('[');
        } 
        if (depth == 7) { 
            result.append(getKeyClass(baseKey));
        } else if (baseKey >= 'A' && baseKey <= 'Z') { 
            result.append((char)baseKey);
        } else { 
            result.append('L').append(getKeyClass(baseKey)).append(';');
        }
        return index;
    }

    long hash(Object x) { 
        return ((JOPClassName)x).hashCode() & 0xFFFFFFFFL;
    }

    public String toString() { 
        return "<ClassTable " + hashCode() + ">";
    }

    static class NameAndTypeKey { 
        int nameKey;

        int typeKey;

        NameAndTypeKey(int n, int t) { nameKey = n; typeKey = t; }

        public String toString() { 
            return "NameAndTypeKey(0x" + 
                Integer.toHexString(0x10000 + nameKey).substring(1) + ", 0x" +
                Integer.toHexString(0x10000 + typeKey).substring(1) + ")";
        }
    }

}
