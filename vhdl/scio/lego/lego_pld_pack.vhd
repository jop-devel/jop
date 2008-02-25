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
--      lego_pld_pack.vhd
--
--      Types and constants for communication between JOP and PLD
--      
--      Author: Peter Hilber                    peter.hilber@student.tuwien.ac.at
--
--
--
--      2007-03-26      created for Lego PCB
--
--      todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package lego_pld_pack is

    subtype FORWARDED_PINS is std_logic_vector(20 downto 0);
    subtype FORWARDED_PINS_INDEX_TYPE is integer range FORWARDED_PINS'low to FORWARDED_PINS'high;

    -- pin assignments
    constant led0   : integer := 0;         -- leds
    constant led1   : integer := 1;
    constant led2   : integer := 2;
    constant led3   : integer := 3;
    constant btn0   : integer := 4;         -- buttons
    constant btn1   : integer := 5;
    constant btn2   : integer := 6;
    constant btn3   : integer := 7;
    constant i0     : integer := 8;         -- digital inputs
    constant i1     : integer := 9;
    constant i2     : integer := 10;
	constant unused0	: integer := 11;		-- free for future use
	constant unused1	: integer := 12;
	constant unused2	: integer := 13;
	constant unused3	: integer := 14;
	constant unused4	: integer := 15;
	constant unused5	: integer := 16;
	constant unused6	: integer := 17;
	constant unused7	: integer := 18;
	constant unused8	: integer := 19;		-- input only on pld
	constant unused9	: integer := 20;		-- input only on pld

    -- pin directions
    type DIRECTION_TYPE is (din, dout);

    type FORWARDED_PINS_DIRECTIONS_TYPE is array (FORWARDED_PINS'high downto FORWARDED_PINS'low) of DIRECTION_TYPE;

    constant FORWARDED_PINS_DIRECTIONS : FORWARDED_PINS_DIRECTIONS_TYPE := (
        led0 => dout,
        led1 => dout,
        led2 => dout,
        led3 => dout,
        btn0 => din,
        btn1 => din,
        btn2 => din,
        btn3 => din,
        i0 => din,
        i1 => din,
        i2 => din,
		unused0	=> din,
		unused1	=> dout,
		unused2	=> din,
		unused3	=> dout,
		unused4	=> din,
		unused5	=> dout,
		unused6	=> din,
		unused7	=> dout,
		unused8 => din,	-- input only on pld
		unused9 => din -- input only on pld
		);

end lego_pld_pack;
