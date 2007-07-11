package com.jopdesign.jopeclipse.internal.ui;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class CommPortIdentifierContentProvider implements IStructuredContentProvider {
    public Object[] getElements(Object inputElement) {
        return // RXTXCommUtils.getAvailableSerialPorts().toArray();
            ((Set) inputElement).toArray();
    }

    public void dispose() {
        
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        
    }
}
