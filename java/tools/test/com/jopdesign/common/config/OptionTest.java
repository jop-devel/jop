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

package com.jopdesign.common.config;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class OptionTest {

    public static <T> void test(T value, String cmp) {
        if ( value == null || !cmp.equals(value.toString()) ) {
            System.out.println(value + " != " + cmp);
        } else {
            System.out.println("ok");
        }
    }

    public static void main(String[] args) {

        Config config = new Config();
        config.setProperty("test1", "value1");
        config.setProperty("test2", "value2");

        StringOption option = new StringOption("option", "test option", "${test1}/${test2}");
        option.setReplaceOptions(true);
        config.addOption(option);

        IntegerOption intopt = new IntegerOption("int", "int option", 1);
        intopt.setReplaceOptions(true);
        config.addOption(intopt);

        test(config.getOption(option),"value1/value2");

        config.setProperty("option", "}test1}");
        test(config.getOption(option),"}test1}");
        config.setProperty("option", "${test1}/test${");
        test(config.getOption(option),"value1/test${");

        config.setProperty("option", "${test1}${test2}/${test${");
        test(config.getOption(option),"value1value2/${test${");
        config.setProperty("option", "${test1}${test2}/${test}");
        test(config.getOption(option),"value1value2/");

        config.setProperty("option", "${test1}/${int}/${int}");
        test(config.getOption(option),"value1/1/1");

        config.setProperty("test3", "4");
        config.setProperty("int", "${test3}");
        test(config.getOption(intopt),"4");
        test(config.getOption(option),"value1/4/4");

        config.setProperty("test3", "${test3}");
        config.setProperty("test4", "test/${test4}");
        config.setProperty("option", "${test1}/${test3}/${test4}");
        test(config.getOption(option),"value1//test/");

        config.setProperty("test3", "${option}");
        config.setProperty("option", "${test1}/${test3}");
        test(config.getOption(option),"value1/");
        config.setProperty("option", "${option}/test");
        test(config.getOption(option),"/test");

        test(config.getDefaultValue(intopt),"1");
        test(config.getDefaultValue(option),"value1/value2");



    }
}
