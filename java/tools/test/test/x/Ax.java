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

package test.x;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Ax {

    protected static class Ax1 {

        protected static class Ax2 {
            public void method5() {
            }
        }

        public void method1() {
            
        }
    }

    private static class Ax4 {

        private static class Ap {

            protected static class Ai {
                protected static void methodi() {
                }
                public static void methodp() {
                }
            }

            public void method() {
            }
        }

        protected static class Ax5 {

            public static void method2() {
            }
        }
    }

    public static class Ax3 extends Ax1.Ax2 {

    }


    public static class Ax6 extends Ax4.Ap {
                

    }
}
