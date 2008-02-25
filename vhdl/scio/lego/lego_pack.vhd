--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007, Peter Hilber
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--      lego_pack.vhd
--
--      For Lego PCB related stuff
--      
--      Author: Peter Hilber                    peter.hilber@student.tuwien.ac.at
--
--
--      2006-12-07      created for Lego PCB
--
--      todo:
--
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package lego_pack is
    subtype lego_motor_state is unsigned(1 downto 0);

    constant LEGO_MOTOR_STATE_OFF : lego_motor_state := "00";
    constant LEGO_MOTOR_STATE_FORWARD : lego_motor_state := "01";
    constant LEGO_MOTOR_STATE_BACKWARD : lego_motor_state := "10";
    constant LEGO_MOTOR_STATE_BRAKE : lego_motor_state := "11";

end lego_pack;
