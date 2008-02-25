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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity debounce is
	generic (clk_freq : integer);
	
	port (
		clk: in std_logic;
		reset: in std_logic;
		
		input: in std_logic;
		output: buffer std_logic);
end debounce;
		
architecture rtl of debounce is
	constant debounce_count: integer := clk_freq / 20;

	signal counter: unsigned(26 downto 0);
begin
	synch: process(clk, reset)
	begin
		if reset = '1' then
			counter <= (others => '0');
			output <= '0';		
		elsif rising_edge(clk) then
			counter <= counter + 1;
					
			if input = output then
				counter <= (others => '0');
			end if;
			
			if counter = debounce_count then	-- XXX
				output <= not output;
				counter <= (others => '0');
			end if;
		end if;		
	end process;
end rtl;