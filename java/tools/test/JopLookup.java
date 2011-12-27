/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.Cmdline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Just a small helper class to look up various symbols in a .jop link file.
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class JopLookup {

    @SuppressWarnings({"PublicField"})
    private static class ClassEntry {
        public String name;
        public int methodTable;
        public int cpIndex;
        public int size;
        public int superRef;
        public Map<Integer, String> methods;
        public Map<Integer, Integer> constmap;

        private ClassEntry(String name, int methodTable, int cpIndex) {
            this.name = name;
            this.methodTable = methodTable;
            this.cpIndex = cpIndex;
            this.methods = new HashMap<Integer, String>();
            this.constmap = new HashMap<Integer, Integer>();
        }
    }

    @SuppressWarnings({"PublicField"})
    private static class MethodEntry {
        public String name, code;
        public List<Integer> positions;
        public Map<Integer, String> instructions;

        private MethodEntry(String name, String code) {
            this.name = name;
            this.code = code;
            positions = new ArrayList<Integer>();
            instructions = new HashMap<Integer, String>();
        }

        public String getClassName() {
            return name.substring(0, name.indexOf(':'));
        }

        public ClassEntry getClassEntry() {
            String clsName = getClassName();
            for (ClassEntry cls : classes.values()) {
                if (cls.name.equals(clsName)) {
                    return cls;
                }
            }
            return null;
        }

        public int getAddress() {
            ClassEntry entry = getClassEntry();
            for (Integer address : entry.methods.keySet()) {
                if (entry.methods.get(address).equals(name.replace(':','.'))) {
                    return address;
                }
            }
            return -1;
        }
    }

    private static Map<Integer, String> fields = new HashMap<Integer, String>();
    private static Map<Integer, String> bytecode = new HashMap<Integer, String>();
    private static Map<Integer, ClassEntry> classes = new HashMap<Integer, ClassEntry>();
    private static Map<String, MethodEntry> methods = new HashMap<String, MethodEntry>();

    public static void usage() {
        System.out.println("Usage: JopLookup [<jop-link-file>]");
        System.out.println();
        System.out.println("If no jop-file is specified, JopLookup tries to use java/target/dist/bin/*.jop");
        System.out.println("This tool can be used to find methods, fields and instructions in the .jop file by address");
    }

    public static void main(String[] args) {
        File jopFile = null;
        if (args.length == 0) {
            // try to find it ..
            File binDir = new File("java/target/dist/bin");
            String[] files = binDir.list();
            for (String file : files) {
                if (file.endsWith(".jop")) {
                    jopFile = new File(binDir, file);
                }
            }
            if (jopFile == null) {
                usage();
                System.exit(1);
            } else {
                System.out.println("Using link file "+jopFile);
            }
        } else if (args.length > 1) {
            usage();
            System.exit(1);
        } else if ("--help".equals(args[0]) || "-h".equals(args[0])) {
            usage();
            System.exit(0);
        } else {
            jopFile = new File(args[0]);
        }

        File txtFile = new File(jopFile.toString()+".txt");
        File linkFile = new File(jopFile.toString()+".link.txt");

        if (!jopFile.exists()) {
            System.out.println("Jopfile " + jopFile + " does not exist");
            System.exit(1);
        }
        if (!txtFile.exists()) {
            System.out.println("Jop-txt-file " + txtFile + " does not exist");
            System.exit(1);
        }
        if (!linkFile.exists()) {
            System.out.println("Linkfile " + linkFile + " does not exist");
            System.exit(1);
        }

        readLinkFile(linkFile);
        readTxtFile(txtFile);

        Cmdline cmdline = new Cmdline();

        while (true) {
            String[] qArgs = cmdline.readInput();
            if (cmdline.isExit(qArgs)) {
                return;
            }
            try {
                processQuery(qArgs);
            } catch (NumberFormatException e) {
                System.out.println("Error: "+e);
                e.printStackTrace();
            }
        }
    }

    private static void readLinkFile(File linkFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(linkFile));

            ClassEntry current = null;

            while (true) {
                String entry = reader.readLine();
                if (entry == null) {
                    break;
                }
                String[] tab = entry.trim().split(" ");
                if (entry.startsWith("static ")) {
                    fields.put(Integer.parseInt(tab[2]), tab[1]);
                }
                if (entry.startsWith("bytecode ")) {
                    bytecode.put(Integer.parseInt(tab[2]), tab[1]);
                }
                if (entry.startsWith("class")) {
                    current = new ClassEntry(tab[1], Integer.parseInt(tab[2]), Integer.parseInt(tab[3]));
                    classes.put(current.methodTable-5, current);
                }
                assert(current != null);
                if (entry.startsWith(" -instSize ")) {
                    current.size = Integer.parseInt(tab[1]);
                }
                if (entry.startsWith(" -super ")) {
                    current.superRef = Integer.parseInt(tab[1]);
                }
                if (entry.startsWith(" -mtab ")) {
                    current.methods.put(Integer.parseInt(tab[2]), tab[1]);
                }
                if (entry.startsWith(" -constmap ")) {
                    current.constmap.put(Integer.parseInt(tab[2]), Integer.parseInt(tab[1]));
                }
            }

        } catch (FileNotFoundException e) {
            throw new AppInfoError(e);
        } catch (IOException e) {
            throw new AppInfoError(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new AppInfoError(e);
                }
            }
        }
    }

    private static void readTxtFile(File txtFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(txtFile));

            String lastline = null;
            MethodEntry current = null;

            while (true) {
                String entry = reader.readLine();
                if (entry == null) {
                    break;
                }

                if (entry.isEmpty()) {
                    current = null;
                    continue;
                }
                if (entry.startsWith("Code(")) {
                    assert lastline != null;
                    current = new MethodEntry(lastline, entry);
                    methods.put(lastline.replace(':','.'), current);
                    continue;
                }
                if (current != null) {
                    int pos = entry.indexOf(':');
                    int loc = Integer.parseInt(entry.substring(0,pos));
                    current.positions.add(loc);
                    current.instructions.put(loc, entry);
                    continue;
                }

                lastline = entry;
            }

        } catch (FileNotFoundException e) {
            throw new AppInfoError(e);
        } catch (IOException e) {
            throw new AppInfoError(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new AppInfoError(e);
                }
            }
        }
    }

    private static void processQuery(String[] args) throws NumberFormatException {
        if (args.length == 0 || "".equals(args[0]) || "help".equals(args[0])) {
            System.out.println("Usage: ");
            System.out.println("<address>          Print link info at address");
            System.out.println("c[lass] <address>  Print class info of the class at the address");
            System.out.println("m[ethod] <mp|fqn>  Print instructions of method");
            System.out.println("pc [<mp>] <pc>     Print instruction at program counter");
            return;
        }

        if ("class".equals(args[0]) || "c".equals(args[0])) {
            int loc = Integer.parseInt(args[1]);
            if (classes.containsKey(loc)) {
                ClassEntry entry = classes.get(loc);
                System.out.println(entry.name);
                System.out.println("super: "+entry.superRef+", cp: "+entry.cpIndex);
                for (Integer key : entry.methods.keySet()) {
                    System.out.println("-mtab "+entry.methods.get(key)+" "+key);
                }
            }
            return;
        }
        if ("pc".equals(args[0])) {
            if (args.length == 2) {
                int pc = Integer.parseInt(args[1]);
                System.out.println(lookupPC(pc));
                return;
            } else {
                int mp = Integer.parseInt(args[1]);
                int pc = Integer.parseInt(args[2]);
                String method = lookupLoc(mp);
                MethodEntry entry = methods.get(method);
                if (entry == null) {
                    System.out.println("Unknown method");
                    return;
                }
                System.out.println(entry.instructions.get(pc));
                return;
            }
        }
        if ("method".equals(args[0]) || "m".equals(args[0])) {
            MethodEntry entry = methods.get(args[1]);
            if (entry == null) {
                int mp = Integer.parseInt(args[1]);
                String method = bytecode.get(mp);
                if (method == null) {
                    for (ClassEntry cls : classes.values()) {
                        for (Integer key : cls.methods.keySet()) {
                            if ( key == mp ) {
                                method = cls.methods.get(key);
                                break;
                            }
                        }
                    }
                }
                entry = methods.get(method);
            }
            if (entry == null) {
                System.out.println("Unknown method");
                return;
            }
            printMethod(entry);
            return;
        }

        int loc = Integer.parseInt(args[0]);
        System.out.println(lookupLoc(loc));
    }

    private static String lookupLoc(int loc) {
        if (fields.containsKey(loc)) {
            return "Field " + fields.get(loc);
        }
        if (bytecode.containsKey(loc)) {
            return "Bytecode of "+bytecode.get(loc);
        }
        if (classes.containsKey(loc)) {
            ClassEntry entry = classes.get(loc);
            return "Class "+entry.name;
        }
        for (ClassEntry entry: classes.values()) {
            String clsName = "class "+ (entry.methodTable-5) + " (" +entry.name+")";
            if (entry.cpIndex == loc) {
                return "Constantpool of "+clsName;
            }
            for (Integer key : entry.methods.keySet()) {
                if (key == loc) {
                    String method = entry.methods.get(key);
                    int bc = -1;
                    for (Integer k : bytecode.keySet()) {
                        if (bytecode.get(k).equals(method)) {
                            bc = k;
                            break;
                        }
                    }
                    return "MTab entry " + method + ", code at "+bc+", in " +clsName;
                }
            }
        }
        return lookupPC(loc);
    }

    private static String lookupPC(int pc) {
        int offset = Integer.MAX_VALUE;
        String method = null;
        for (Integer start : bytecode.keySet()) {
            // find nearest method start
            if (start > pc || pc - start > offset) {
                continue;
            }
            offset = pc - start;
            method = bytecode.get(start);
        }
        MethodEntry entry = methods.get(method);
        if (entry == null) {
            return "Unknown";
        }
        return method+" [address "+entry.getAddress()+", local pc: "+offset+"]: "+entry.instructions.get(offset);
    }

    private static void printMethod(MethodEntry entry) {
        System.out.println(entry.name);
        System.out.println(entry.code);
        for (Integer i : entry.positions) {
            System.out.println(entry.instructions.get(i));
        }
    }
}
