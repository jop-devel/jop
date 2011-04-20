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

package com.jopdesign.common.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A Helper to display a commandline (and maybe to provide a history, provided JLine or something will be used..)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Cmdline {

    private String ps;
    private BufferedReader br;

    private String lastCmd;

    public Cmdline() {
        this("");
    }

    /**
     * @param ps Text to display before the text input, like '>'
     */
    public Cmdline(String ps) {
        br = new BufferedReader(new InputStreamReader(System.in));
        setPs(ps);
    }

    /**
     * @param ps the text to display before text input from now on.
     */
    public void setPs(String ps) {
        this.ps = ps + "> ";
    }

    /**
     * @return the next command, split into arguments.
     */
    public String[] readInput() {
        System.out.print(ps);
        String line;
        try {
            line = br.readLine();
        } catch (IOException e) {
            throw new AppInfoError("Error reading from stdin", e);
        }
        line = line.trim();

        if ("#".equals(line)) {
            line = lastCmd;
            System.out.println("Arguments: "+line);
        } else if (line.startsWith("# ")) {
            line = lastCmd + line.substring(1);
            System.out.println("Arguments: "+line);
        }
        lastCmd = line;
        
        return line.split(" ");
    }

    public boolean isExit(String[] args) {
        return args.length == 1 && "exit".equals(args[0]);
    }
}
