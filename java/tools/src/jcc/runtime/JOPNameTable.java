package runtime;

import java.util.Hashtable;
import java.io.*;

class JOPNameTable extends JOPHashtable { 

    static final String NameStringPrefix = "UString_Item";

    java.util.BitSet seenSizes   = new java.util.BitSet();

    // #define UTF_TABLE_SIZE 256
    // #define CLASS_TABLE_SIZE 32
    // #define INTERN_TABLE_SIZE 32

    JOPNameTable() { 
        super(256, String.class); 
    }

    long hash(Object x) { 
        String str = (String)x;
        return stringHash(str);
    }

    public void chainPrefix(CCodeWriter out) { 
        out.println("/* STRING TABLE */\n\n");
    } 

    public void chainSuffix(CCodeWriter out) { 
        out.println("/* END OF STRING TABLE */\n\n");
    } 


    JOPClassTable classTable;

    Object tableChain(CCodeWriter out, int bucket, Object[] list) {
        int length = list.length;
        String prevKey = "NULL             ";
        out.print("/* Start of bucket " + bucket + " */\n");
        for (int i = length - 1; i >= 0; i--) { 
            String str = (String)list[i];
            int strLength = str.length();
            String keyFilled = 
                Integer.toHexString(getNameKey(str) + 0x10000).substring(1);
            if (!seenSizes.get(strLength)) { 
                out.println("DECLARE_USTRING_STRUCT(" + strLength + ");");
                seenSizes.set(strLength);
            } 
            seenStrings.put(str, str); // no need to declare it if defined
            out.print("DEFINE_USTRING(");
            out.print(keyFilled + ", " + prevKey + ", "+ strLength + ", ");
            out.printSafeString(str);
            prevKey = getUString(str);
            out.print(");");
            if (str.length() > 0 && str.charAt(0) < 20 && classTable != null) { 
                out.print(" /* ");
                out.print(classTable.decodeMethodSignature(str));
                out.print(" */");
            }
            out.println();
        }
        return length == 0 ? null : prevKey;
    }

    void tableEntry(CCodeWriter out, int bucket, Object token ) { 
        out.print(token);
    }

    public void addNewEntryCallback(int bucket, Object neww) {
        int value;
        String next = (String)getNext((String)neww);
        if (next == null) { 
            value = bucket + getSize();
        } else { 
            value = getKey(next) + getSize();
        }
        setKey(neww, value);
    }

    public String getUString(int key) { 
        String thisKey = Integer.toHexString(key + 0x10000).substring(1);
        return "&" + NameStringPrefix + thisKey;
    }

    public String getUString(String string) { 
        return getUString(getKey(string));
    }

    public int getNameKey(String string) { 
        return getKey(string);
    }

    Hashtable        seenStrings = new Hashtable();

    public void declareUString(CCodeWriter out, String name) { 
        int length = name.length();
        
        if (seenStrings.get(name) != null) { 
            return;
        }
        if (!seenSizes.get(length)) { 
            out.println("DECLARE_USTRING_STRUCT(" + length + ");");
            seenSizes.set(length);
        } 
        out.println("DECLARE_USTRING(" 
                    + getUString(name).substring(1) + ", " + length + ");");
        seenStrings.put(name, name);
    }
}


