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
public class IntegerOption extends Option<Long> {

    private long minValue = Long.MIN_VALUE;
    private long maxValue = Long.MAX_VALUE;

    public IntegerOption(String key, String descr, boolean optional) {
        super(key, Long.class, descr, optional);
    }

    public IntegerOption(String key, String descr, long i) {
        super(key, descr, i);
    }

    public IntegerOption setMinMax(long min, long max) {
        minValue = min;
        maxValue = max;
        return this;
    }

    @Override
    protected Long parse(String s) throws IllegalArgumentException {
        long val;
        try {
            val = Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format '" + s + '"', e);
        }
        if (val < minValue || val > maxValue) {
            throw new IllegalArgumentException("Value out of range (min: " + minValue + ", max: " + maxValue + ")");
        }
        return val;
    }

    @Override
    public boolean isEnabled(OptionGroup options) {
        return options.hasValue(this) && options.getOption(this, 0L) != 0;
    }

    @Override
    public boolean isValue(String arg) {
        // if it starts with '-', it may be a negative integer
        if (arg.startsWith("-")) {
            try {
                Long val = Long.parseLong(arg);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        // in other cases, if it does not start with '-', we assume it is a value
        // (even if it is not well formatted!)
        return !arg.startsWith("--");
    }
}