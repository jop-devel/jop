/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * A simple class for classes where only the name is known.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class UnknownClassInfo extends ClassInfo {

    private String className;

    public UnknownClassInfo(AppStruct appStruct, String className) {
        super(appStruct);
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getSuperClassName() {
        return null;
    }

    public boolean isInterface() {
        return false;
    }

    public ConstantPoolInfo getConstantPoolInfo() {
        return null;
    }

    public void writeClassFile(String filename) throws IOException {
    }

    protected Set loadInterfaces() {
        return Collections.EMPTY_SET;
    }

    protected void loadMethodInfos() {
    }

    protected void loadFieldInfos() {
    }

    public boolean isPublic() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isPrivate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isProtected() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isFinal() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStatic() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSynchronized() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setFinal(boolean val) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAccessType(int type) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
