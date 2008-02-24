--
--  This file is part of JOP, the Java Optimized Processor
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
--	avalon_test_slave.vhd
--
--	A simple test slave for the Avalon interface
--	
--	Author: Martin Schoeberl	mschoebe@mail.tuwien.ac.at
--
--
--
--	2006-08-09	Copy from SimpCon test slave
--	2006-08-12	Removed wd signal to get an even simpler slave
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity avalon_test_slave is
port (
	clk		: in std_logic;
	reset	: in std_logic;

	chipselect	: in std_logic;
	address			: in std_logic_vector(1 downto 0);
	writedata		: in std_logic_vector(31 downto 0);
	read, write		: in std_logic;
	readdata		: out std_logic_vector(31 downto 0)

);
end avalon_test_slave;

architecture rtl of avalon_test_slave is

	signal xyz			: std_logic_vector(31 downto 0);
	signal cnt			: unsigned(31 downto 0);

begin

--
--	The MUX is all we need for a read.
--
process(address, chipselect, read, xyz, cnt)
begin

	-- we also could just use the address decoder
	-- Avalon does not care on the output value
	-- when the slave is not selected
	readdata <= (others => '0');
	if read='1' and chipselect='1' then
		-- that's our very simple address decoder
		if address(0)='0' then
			readdata <= std_logic_vector(cnt);
		else
			readdata <= xyz;
		end if;
	end if;

end process;


--
--	Avalon write
--
process(clk, reset)

begin

	if (reset='1') then
		xyz <= (others => '0');
		cnt <= (others => '0');

	elsif rising_edge(clk) then

		if write='1' and chipselect='1' then
			xyz <= writedata;
		end if;

		cnt <= cnt+1;

	end if;

end process;


end rtl;
