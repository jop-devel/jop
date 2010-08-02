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

package com.jopdesign.common.logger;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * This is a log4j layout implementation which can be used to print log messages without stacktrace.
 *
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class ConsoleLayout extends PatternLayout {

    private boolean handleThrowable = true;

    public ConsoleLayout() {
    }

    public ConsoleLayout(String pattern) {
        super(pattern);
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public String format(LoggingEvent event) {
        StringBuffer out = new StringBuffer();
        out.append( super.format(event) );

        if ( handleThrowable ) {
            ThrowableInformation ti = event.getThrowableInformation();
            String newline = System.getProperty("line.separator");

            if ( ti != null ) {
                Throwable ex = ti.getThrowable();

                while ( ex != null) {
                    out.append("    Caused by [");

                    StackTraceElement[] trace = ex.getStackTrace();
                    if ( trace != null && trace.length > 0 ) {
                        out.append(trace[0].getClassName());
                        out.append("#");
                        out.append(trace[0].getMethodName());
                        out.append("] ");
                    }

                    out.append(ex.getMessage());
                    out.append(newline);

                    ex = ex.getCause();
                }
            }
        }

        return out.toString();
    }

    public boolean ignoresThrowable() {
        return !handleThrowable;
    }

    public void setHandleThrowable(boolean handleThrowable) {
        this.handleThrowable = handleThrowable;
    }
}
