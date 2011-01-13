/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.KeyManager;
import com.jopdesign.common.logger.LogConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a helper class to repeatedly start the main method with different arguments,
 * useful for debugging.
 *
 * Use this class as entry point, and pass as argument the class containing the main method of the program
 * you want to test and optionally a list of arguments which should be added for every invocation.
 * You can then run the program multiple times with user supplied arguments.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MainRunner {

    public static void main(String[] args) {

        if ( args.length < 1 ) {
            System.out.println("Usage: MainRunner <mainclass> [<options>]");
            System.exit(1);
        }

        final String clsName = args[0].substring(args[0].lastIndexOf(".")+1);

        try {
            Class cls = Class.forName(args[0]);
            Method main = cls.getMethod("main", new Class[] {String[].class});

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print(clsName +"> ");

                String s = br.readLine();
                if ( s == null ) {
                    return;
                }
                String cmd = s.trim();
                if( "exit".equals(cmd) ) {
                    return;
                }

                // TODO quoted arguments are not supported
                String[] cmdArgs = cmd.split(" ");

                List<String> argList = new ArrayList<String>(args.length);
                argList.addAll( Arrays.asList(Arrays.copyOfRange(args, 1, args.length)) );
                argList.addAll( Arrays.asList(cmdArgs) );

                String[] mainArgs = argList.toArray(new String[0]);

                System.setSecurityManager(new SecurityManager() {
                    @Override
                    public void checkPermission(Permission perm) {}

                    @Override
                    public void checkPermission(Permission perm, Object context) {}

                    @Override
                    public void checkExit(int status) {
                        throw new SecurityException(clsName + " exited with " + status);
                    }
                });

                try {
                    main.invoke(null, new Object[] {mainArgs});
                } catch (Exception e) {
                    System.err.flush();
                    if ( e.getCause() instanceof SecurityException ) {
                        System.out.println(e.getCause().getMessage());
                    } else {
                        e.printStackTrace();
                    }
                } finally {
                    System.setSecurityManager(null);
                }
                
                // cleanup for next invoke
                AppInfo.getSingleton().clear(true);
                KeyManager.getSingleton().reset();
                LogConfig.stopLogger();
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Main class '"+args[0]+"' not found: "+e.getMessage());
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.out.println("Main method in class '"+args[0]+"' not found: "+e.getMessage());
            System.exit(1);            
        } catch (IOException e) {
            System.out.println("Could not read stdin: " + e.getMessage());
            System.exit(1);
        }

    }
}
