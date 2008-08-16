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
--	sc_sram16skit.vhd
--
--	SimpCon compliant external memory interface
--	for 16-bit SRAM (skit board)
--
--
--  2008-13-08  skit sram setup : TODO just skeleton IF and rest from old file so far
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;

entity sc_mem_if is
generic (ram_ws : integer; addr_bits : integer);

port (

	clk, reset	: in std_logic;

--
--	SimpCon memory interface
--
	sc_mem_out		: in sc_out_type;
	sc_mem_in		: out sc_in_type;

-- memory interface

	ram_addr	: out std_logic_vector(addr_bits-1 downto 0);
	ram_dout	: out std_logic_vector(15 downto 0);
	ram_din		: in std_logic_vector(15 downto 0);
	ram_dout_en	: out std_logic;
	ram_ncs		: out std_logic;
--	ram_noe		: out std_logic;
	ram_nwe		: out std_logic;
	ram_ba0		: out std_logic;
	ram_ba1		: out std_logic;
	ram_dml		: out std_logic;
	ram_dmh		: out std_logic;
	ram_ncas	: out std_logic;
	ram_nras	: out std_logic
);
end sc_mem_if;

architecture rtl of sc_mem_if is


begin
	ram_addr 	<=	(others => 'Z');
	ram_dout 	<=	(others => 'Z');
	--ram_dout_en <=	(others => 'Z');
	ram_ncs		<=	'1';
	ram_nwe		<=	'1';
	ram_ba0		<=	'0';
	ram_ba1		<=	'0';
	ram_dml		<=	'0';
	ram_dmh		<=	'0';
	ram_ncas	<=	'1';
	ram_nras	<=	'1';

end rtl;
