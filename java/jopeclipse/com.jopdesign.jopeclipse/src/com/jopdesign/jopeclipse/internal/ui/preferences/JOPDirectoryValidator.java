package com.jopdesign.jopeclipse.internal.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.IPath;

public class JOPDirectoryValidator {
    public static boolean isValid(IPath path) {
        File f = path.toFile();
        
        if (!f.exists() || !f.isDirectory()) {
            return false;
        }
        
        File[] children = f.listFiles();
        
        if (children == null) {
            return false;
        }
        
        for (File c : children) {
            if (c.getName().equals("build.xml")) {
                return true;
            }
        }
        
        return false;
    }
}
