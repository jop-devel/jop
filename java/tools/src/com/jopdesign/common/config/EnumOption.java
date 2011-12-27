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

import java.util.Arrays;

/**
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class EnumOption<T extends Enum<T>> extends Option<T> {

    public EnumOption(String key, String descr, Class<T> clazz) {
        super(key, clazz, descr, false);
    }

    public EnumOption(String key, String descr, Class<T> clazz, boolean optional) {
        super(key, clazz, descr, optional);
    }

    public EnumOption(String key, String descr, T def) {
        super(key, descr + " " + enumDescr(def), def);
    }

    private static <U extends Enum<U>> String enumDescr(U v) {
        return Arrays.toString(v.getClass().getEnumConstants());
    }

    private static String enumDescr(Class<?> v) {
        return Arrays.toString(v.getEnumConstants());
    }

    @Override
    protected T parse(String s) throws IllegalArgumentException {
        try {
            return Enum.valueOf(this.valClass, s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("'" + s + "' failed to parse: not one of " + enumDescr(this.valClass), e);
        }
    }
}