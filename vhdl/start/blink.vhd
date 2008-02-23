--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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
--	blink.vhd
--
--	simple blinking watchdog led.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity blink is

port (
	clk				: in std_logic;
	wd				: out std_logic;
--
--	dummy input pins for EP1C6 on board with EP1C12 pinout
--	EP1C12 has additional GND and VCCINT pins.
--
	dummy_gnd		: in std_logic_vector(5 downto 0);
	dummy_vccint	: in std_logic_vector(5 downto 0)
);
end blink;

architecture rtl of blink is

	signal cnt		: unsigned(24 downto 0);

begin

	process(clk)
	begin

		if rising_edge(clk) then
			cnt <= cnt + 1;
		end if;

	end process;

	wd <= std_logic(cnt(24));

end rtl;
