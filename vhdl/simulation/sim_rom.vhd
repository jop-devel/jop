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
--	sim_rom.vhd
--
--	A 'faster' simulation version of the JVM ROM.
--
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity rom is
generic (width : integer; addr_width : integer);
port (
	clk			: in std_logic;
	address		: in std_logic_vector(addr_width-1 downto 0);
	q			: out std_logic_vector(width-1 downto 0)
);

	subtype word is std_logic_vector(width-1 downto 0);
	constant nwords : integer := 2 ** addr_width;
	type ram_type is array(0 to nwords-1) of word;
end rom;


architecture sim of rom is

	shared variable ram : ram_type;

	signal areg		: std_logic_vector(addr_width-1 downto 0);
	signal data		: std_logic_vector(11 downto 0);

begin

process(clk) begin

	if rising_edge(clk) then
		q <= ram(to_integer(unsigned(address)));
	end if;

end process;


-- initialize at start with a second process accessing
-- the shared variable ram

initialize:
process

	variable address	: natural;

	file memfile		: text is "mem_rom.dat";
	variable memline	: line; 
	variable val		: integer;

begin
--	write(output, "load ROM memory...");
	for address in 0 to nwords-1 loop
		if endfile(memfile) then
			exit;
		end if;
		readline(memfile, memline);
		read(memline, val);
		ram(address) := std_logic_vector(to_signed(val, width));
	end loop;
	file_close(memfile);
	-- we're done, wait forever
	wait;
end process initialize;

end sim;
