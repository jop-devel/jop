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
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class StringOption extends Option<String> {

    public StringOption(String key, String descr, boolean optional) {
        super(key, String.class, descr, optional);
    }

    public StringOption(String key, String descr, String def) {
        super(key, descr, def);
    }

    public StringOption(String key, String descr, char shortKey, boolean optional) {
        super(key, String.class, descr, optional);
        this.shortKey = shortKey;
    }

    public StringOption(String key, String descr, char shortKey, String def) {
        super(key, descr, def);
        this.shortKey = shortKey;
    }

    public StringOption(String key, String descr, boolean optional, boolean replaceOptions) {
        super(key, String.class, descr, optional);
        this.replaceOptions = replaceOptions;
    }

    public StringOption(String key, String descr, String def, boolean replaceOptions) {
        super(key, descr, def);
        this.replaceOptions = replaceOptions;
    }

    @Override
    protected String parse(String s) {
        return s.trim();
    }

    @Override
    public String getDefaultValue(OptionGroup options) {
        if (defaultValue != null && replaceOptions) {
            // we allow replacement for defaults of string options as well!
            return parse(options, defaultValue);
        }
        return defaultValue;
    }

    public StringOption mandatory() {
        StringOption option = new StringOption(key, description, false);
        option.setShortKey(shortKey);
        return option;
    }

    @Override
    protected String getDefaultsText(String defaultValue) {
        if ("".equals(defaultValue)) {
            return "[optional]";
        } else {
            return super.getDefaultsText(defaultValue);
        }
    }
}