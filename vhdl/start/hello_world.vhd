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
	--	hello_world.vhd
	--
	--	The 'Hello World' example for FPGA programming.
	--
	--	Author: Martin Schoeberl (martin@jopdesign.com)
	--
	--	2006-08-04	created
	--
	
	library ieee;
	use ieee.std_logic_1164.all;
	use ieee.numeric_std.all;
	
	entity hello_world is
	
	port (
		clk		: in std_logic;
		led		: out std_logic
	);
	end hello_world;
	
	architecture rtl of hello_world is
	
		constant CLK_FREQ : integer := 20000000;
		constant BLINK_FREQ : integer := 1;
		constant CNT_MAX : integer := CLK_FREQ/BLINK_FREQ/2-1;
	
		signal cnt		: unsigned(24 downto 0);
		signal blink	: std_logic;
	
	begin
	
		process(clk)
		begin
	
			if rising_edge(clk) then
				if cnt=CNT_MAX then
					cnt <= (others => '0');
					blink <= not blink;
				else
					cnt <= cnt + 1;
				end if;
			end if;
	
		end process;
	
		led <= blink;
	
	end rtl;
