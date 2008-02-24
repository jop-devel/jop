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
--	jop_avalon.vhd
--
--	top level for Avalon (SPOC Builder) version
--
--	2006-08-10	adapted from jop_256x16.vhd
--
--
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity jop_avalon is

generic (
	addr_bits	: integer := 24;	-- address range for the avalone interface
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4		-- 2*block_bits is number of cache blocks
);

port (
	clk, reset		: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;

--
--	watchdog
--
	wd				: out std_logic;

--
-- Avalon interface
--

	address		: out std_logic_vector((addr_bits-1+2) downto 0);
	writedata	: out std_logic_vector(31 downto 0);
	byteenable	: out std_logic_vector(3 downto 0);
	readdata	: in std_logic_vector(31 downto 0);
	read		: out std_logic;
	write		: out std_logic;
	waitrequest	: in std_logic

);
end jop_avalon;

