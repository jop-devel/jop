package com.jopdesign.jopeclipse.internal.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class ClassFileVisitor implements IResourceVisitor, IResourceDeltaVisitor  {
    private Map<Integer, Set<IResource>> classFiles = new HashMap<Integer, Set<IResource>>();
    

    public Map<Integer, Set<IResource>> getClassFiles() {
        return classFiles;
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
        if (visit(delta.getResource())) {
            if (!classFiles.containsKey(delta.getKind())) {
                classFiles.put(delta.getKind(), new HashSet<IResource>());
            }
            
            classFiles.get(delta.getKind()).add(delta.getResource());
        }
        
        return true;
    }

    @Override
    public boolean visit(IResource resource) throws CoreException {
        if (resource.getType() == IResource.FILE && "class".equalsIgnoreCase(resource.getFileExtension())) {
            return true;
        }
        
        return false;
    }
}
