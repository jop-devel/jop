--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Peter Hilber (peter@hilber.name)
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


------------------------------------------------------------------
--
-- A very simple memory model adapted for RTTM simulation.
--
------------------------------------------------------------------
------------------------------------------------------------------
library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

-- use IEEE.std_logic_textio.all;          -- I/O for logic types

entity memory is
	generic(add_bits : integer := 18;
		data_bits : integer := 32);
	port(
		addr	: in std_logic_vector(add_bits-1 downto 0);
		data	: inout std_logic_vector(data_bits-1 downto 0);
		ncs		: in std_logic;
		noe		: in std_logic;
		nwr		: in std_logic);
 
	subtype word is std_logic_vector(data_bits-1 downto 0);
	constant nwords : integer := 2 ** add_bits;
	type ram_type is array(0 to nwords-1) of word;
end;


architecture sim of memory is
	------------------------------
	signal ram : ram_type;
	------------------------------
	constant tAcc : time := 17 ns;	-- original 15ns
--	constant tAcc : time := 27 ns;
	constant tDoe : time := 9 ns;	-- original 7ns
--	constant tDoe : time := 19 ns;	-- original 7ns
	constant tHold : time := 2 ns;

	signal cs_ok	: std_logic;
	signal oe_ok	: std_logic;

begin

memory:
process (addr, cs_ok, ncs, noe, nwr, oe_ok, ram, data)
	variable address : natural;

begin
		address := to_integer(unsigned(addr));
		if ncs='0' then
			cs_ok <= '1' after tAcc;
		else
			cs_ok <= '0';
		end if;
		if noe='0' then
			oe_ok <= '1' after tDoe;
		else
			oe_ok <= '0';
		end if;
		if cs_ok='1' and oe_ok='1' then
			data <= ram(address);
		else
			data <= (others => 'Z') after tHold;
		end if;
		if ncs='0' and rising_edge(nwr) then
			ram(address) <= data;
		end if;
end process;

end architecture sim;
