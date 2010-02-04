--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2010, Peter Hilber (peter@hilber.name)
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

entity flags_ram is
	generic (
		way_bits		: integer
	);
	port (
		address		: in std_logic_vector(way_bits-1 downto 0);
		clock		: in std_logic;
		data		: in std_logic_vector(0 downto 0);
		wren		: in std_logic ;
		q		: out std_logic_vector(0 downto 0)
	);
end flags_ram;


architecture rtl of flags_ram is

signal flags		: std_logic_vector(2**way_bits-1 downto 0);
signal next_q		: std_logic;

begin

	sync: process (clock) is
	begin
	    if rising_edge(clock) then
	    	q(0) <= next_q;
	    	next_q <= flags(to_integer(unsigned(address)));
	    	if wren = '1' then	    		 
	    		flags(to_integer(unsigned(address))) <= data(0);
	    	end if; 
	    end if;
	end process sync;

end rtl;