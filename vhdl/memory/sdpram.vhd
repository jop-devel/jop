--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2006, Martin Schoeberl (martin@jopdesign.com)
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
--	sdpram.vhd
--
--	Simple dual port ram with read and write port
--		and independent clocks
--
--	When using different clocks following warning is generated:
--		Functionality differs from the original design.
--	Read during write at the same address is undefined.
--
--	Author: Martin Schoeberl (martin@jopdesign.com)
--
--	2006-08-03	adapted from simulation only version
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sdpram is
generic (width : integer := 32; addr_width : integer := 7);
port (
	wrclk		: in std_logic;
	data		: in std_logic_vector(width-1 downto 0);
	wraddress	: in std_logic_vector(addr_width-1 downto 0);
	wren		: in std_logic;

	rdclk		: in std_logic;
	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	dout		: out std_logic_vector(width-1 downto 0)
);
end sdpram ;

architecture rtl of sdpram is

	signal reg_dout			: std_logic_vector(width-1 downto 0);

	subtype word is std_logic_vector(width-1 downto 0);
	constant nwords : integer := 2 ** addr_width;
	type ram_type is array(0 to nwords-1) of word;

	signal ram : ram_type;

begin

process (wrclk)
begin
	if rising_edge(wrclk) then
		if wren='1' then
			ram(to_integer(unsigned(wraddress))) <= data;
		end if;
	end if;
end process;

process (rdclk)
begin
	if rising_edge(rdclk) then
		reg_dout <= ram(to_integer(unsigned(rdaddress)));
		dout <= reg_dout;
	end if;
end process;

end rtl;
