/*
 *	JOPHashtable.java	1.3	00/02/10
 *
 * Copyright (c) 1997,1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package runtime;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

abstract 
public class JOPHashtable { 
    Class type;
    private int size;
    boolean closed;  // Cannot add any more entries
    private Object firstEntry[];
    private Hashtable nextEntry = new Hashtable();
    private Hashtable seen = new Hashtable();
    private Hashtable keys = new Hashtable();

    public Enumeration enumerate() { 
        return seen.keys();
    }

    static public long 
    stringHash(String string) { 
        int raw_hash = 0;
        for (int i = 0; i < string.length(); i++) { 
            raw_hash = raw_hash * 37 + string.charAt(i);
        }
        long result = (((long)raw_hash) << 32) >>> 32;
        return result;
    }

    JOPHashtable(int size, Class type) { 
        this.size = size;
        firstEntry = new Object[size];
        this.type = type;
        this.closed = false;
    }

    abstract long hash(Object x);

    public void chainPrefix(CCodeWriter c) {};
    abstract Object tableChain(CCodeWriter c, int bucket, Object[] chain);    
    public void chainSuffix(CCodeWriter c) {};

    abstract void tableEntry(CCodeWriter c, int bucket, Object token);

    public void addNewEntryCallback(int bucket, Object neww) {}

    public void addEntry(Object x) { 
        if (closed) {
            throw new Error("Hashtable is closed");
        } 
        // Don't do anything if we've already seen this guy.
        if (seen.get(x) != null) { 
            return;
        }
        checkType(x);
        seen.put(x, x);
        int bucket = (int)(hash(x) % size);
        if (bucket < 0) { 
            System.out.println("Bad bucket?? " + this);
        }
        // Add this guy to the beginning of his bucket.
        Object oldNext = firstEntry[bucket];
        firstEntry[bucket] = x;
        if (oldNext != null) { 
            nextEntry.put(x, oldNext);
        }
        addNewEntryCallback(bucket, x);
    }

    protected Object getFirst(int bucket) { 
        return firstEntry[bucket];
    }

    protected Object getNext(Object x) { 
        checkType(x);
        return nextEntry.get(x);
    }

    int getSize() { 
        return size;
    }

    protected void setKey(Object x, int key) { 
        checkType(x);
        Integer Key = new Integer(key);
        Object oldKey = keys.put(x, Key);
        Object oldVal = keys.put(Key, x);
        if (oldKey != null) { 
            System.out.println("Changing key(" + x + ") from " + oldKey + " to "
                   + Key);
        }
        if (oldVal != null) { 
            System.out.println("Both " + x + " and " + oldVal 
                   + " have the same key value " + Key);
        }
    }

    protected int getKey(Object x) { 
        Integer Key = (Integer)keys.get(x);
        if (Key == null) { 
            addEntry(x);
            Key = (Integer)keys.get(x);
        } 
        return Key.intValue();
    }

    protected Object getObject(int key) { 
        return keys.get(new Integer(key));
    }

    void writeTable(CCodeWriter out, String tableName) { 
        closed = true;
        chainPrefix(out);
        Vector list = new Vector();
        Object tokens[] = new Object[size];
        for (int i = 0; i < size; i++) { 
            list.setSize(0);
            for (Object item = firstEntry[i]; item != null; 
                item = nextEntry.get(item)) { 
                list.addElement(item);
            }
            Object[] listArray = new Object[list.size()];
            list.copyInto(listArray);
            tokens[i] = tableChain(out, i, listArray);
        }
        chainSuffix(out);

        out.println("static HASHTABLE_X(" + size + ") " + tableName + "Data = {");
        out.println("\tHASHTABLE_HEADER(" + size + ", " + seen.size() + "),");
        out.println("\t{");
        for (int i = 0; i < size; i++) { 
            if (tokens[i] == null) { 
                out.print("\t\tNULL,\n");
            } else { 
                out.print("\t\t((void *)");
                tableEntry(out, i, tokens[i]);
                out.print("),\n");
            }
        }
        out.println("\t}");
        out.println("};\n");    
        out.println("#if !RELOCATABLE_ROM");
        out.println("HASHTABLE " + tableName + " = (HASHTABLE)&" + tableName + "Data;\n");
        out.println("#endif");
    }
    
    private void checkType(Object x) {
        if (!type.isAssignableFrom(x.getClass())) {
            throw new Error(x.getClass() + " is not assignable to " + type);
        }
    }
}
