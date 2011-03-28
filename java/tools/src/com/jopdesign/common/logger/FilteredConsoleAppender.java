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

package com.jopdesign.common.logger;

import com.jopdesign.common.config.Config;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class FilteredConsoleAppender extends ConsoleAppender {
    private Config config;

    private class MyFilter extends Filter {
        @Override
        public int decide(LoggingEvent event) {
            if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
                return NEUTRAL;
            }
            // level is INFO,DEBUG,TRACE; deny all warn-only
            for (String s : warnOnly) {
                if (matches(s, event.getLoggerName())) {
                    return Filter.DENY;
                }
            }
            if (event.getLevel().isGreaterOrEqual(Level.INFO)) {
                return NEUTRAL;
            }
            // level is DEBUG,TRACE; deny all info-only
            for (String s : infoOnly) {
                if (matches(s, event.getLoggerName())) {
                    return Filter.DENY;
                }
            }

            return NEUTRAL;
        }

        private boolean matches(String filter, String logger) {
            return (logger.equals(filter) || logger.startsWith(filter+"."));
        }
    }

    private Set<String> warnOnly = new HashSet<String>();
    private Set<String> infoOnly = new HashSet<String>();

    public FilteredConsoleAppender() {
        addFilter(new MyFilter());
    }

    public FilteredConsoleAppender(Layout layout) {
        super(layout);
        addFilter(new MyFilter());
    }

    public FilteredConsoleAppender(Layout layout, String target) {
        super(layout, target);
        addFilter(new MyFilter());
    }

    @Override
    public void activateOptions() {
        super.activateOptions();

        if (config != null) {
            String prefix = getName() + ".warnOnly";
            loadWarnOnly(prefix, config.getProperties());
            prefix = getName() + ".infoOnly";
            loadInfoOnly(prefix, config.getProperties());
        }
    }

    public void setConfig(Config config) {
        this.config = config;
        // for some reasons this is not called by the PropertyConfigurator, so we simply call it here...
        activateOptions();
    }

    /**
     * Set a comma-separated list of logger packages for which only warnings and errors will be printed.
     * Replaces the existing per-instance configuration.
     *
     * @param value comma separated list of logger names
     */
    public void addWarnOnly(String value) {
        warnOnly.addAll(Config.splitStringList(value));
    }

    /**
     * Set a comma-separated list of logger packages for which only infos, warnings and errors will be printed.
     * Replaces the existing per-instance configuration.
     *
     * @param value comma separated list of logger names
     */
    public void addInfoOnly(String value) {
        infoOnly.addAll(Config.splitStringList(value));
    }

    private void loadWarnOnly(String prefix, Properties props) {
        for (String key : props.stringPropertyNames()) {
            // does not has the correct prefix
            if (!key.startsWith(prefix+".")) continue;
            // is disabled
            if (!Boolean.valueOf(props.getProperty(key))) continue;
            // strip prefix
            String logger = key.substring(prefix.length()+1);

            warnOnly.add(logger);
        }
    }

    private void loadInfoOnly(String prefix, Properties props) {
        for (String key : props.stringPropertyNames()) {
            // does not has the correct prefix
            if (!key.startsWith(prefix+".")) continue;
            // is disabled
            if (!Boolean.valueOf(props.getProperty(key))) continue;
            // strip prefix
            String logger = key.substring(prefix.length()+1);

            infoOnly.add(logger);
        }
    }
}
