/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.common.config;

/**
 * Option class for a single boolean option.
 * This class is handled a bit different by the option-parser.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class BooleanOption extends Option<Boolean> {

    public BooleanOption(String key, String descr) {
        super(key, Boolean.class, descr, false);
    }

    public BooleanOption(String key, String descr, boolean def) {
        super(key, descr, def);
    }

    public BooleanOption(String key, String descr, char shortKey, boolean skipChecks) {
        this(key, descr, false);
        this.shortKey = shortKey;
        this.skipChecks = skipChecks;
    }

    @Override
    protected Boolean parse(String s) throws IllegalArgumentException {
        String sl = s.toLowerCase();
        if ("true".equals(sl) || "yes".equals(sl) || "y".equals(sl)) return Boolean.TRUE;
        else if ("false".equals(sl) || "no".equals(sl) || "n".equals(sl)) return Boolean.FALSE;
        else throw new IllegalArgumentException("Cannot parse boolean: " + sl);
    }

    @Override
    public boolean isEnabled(OptionGroup options) {
        return options.getOption(this);
    }

    @Override
    protected String getDefaultsText(String defaultValue) {
        if (optional && (defaultValue == null || "false".equalsIgnoreCase(defaultValue))) {
            return skipChecks ? "" : "[flag]";
        }
        return super.getDefaultsText(defaultValue);
    }

    @Override
    public boolean isValue(String arg) {
        try {
            parse(arg);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}