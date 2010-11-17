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
--	jopacx.vhd
--
--	top level for jop with AXEC 1K50
--
--	2002-06-27:	2088 LCs, 23.6 MHz
--	2002-07-27:	2308 LCs, 23.1 MHz	with some changes in jvm and baseio
--	2002-08-02:	2463 LCs
--	2002-08-08:	2431 LCs simpler sigdel
--
--	2002-03-28	creation
--	2002-06-27	isa bus for CS8900
--	2002-07-27	io for baseio
--	2002-08-02	second uart (use first for download and debug)
--	2002-11-01	removed second uart
--	2002-12-01	split memio
--	2002-12-07	disable clkout
--	2002-02-04	adapt jopeth for oebb test with tal (second serial line)
--	2003-07-08	invertion of cts, rts to uart
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

-- library exemplar;					-- for pin attributes
-- use exemplar.exemplar_1164.all;

entity jop is

generic (
	clk_freq	: integer := 20000000;	-- 20 MHz clock frequency
	ram_cnt		: integer := 2;		-- clock cycles for external ram
	rom_cnt		: integer := 3;		-- clock cycles for external rom
	jpc_width	: integer := 11		-- address bits of java byte code pc
);

port (
	clk, reset	: in std_logic;

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	ram_ax		: out std_logic;
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nmem_rd		: out std_logic;
	nmem_wr		: out std_logic;

--	WD
	wd			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;

-- unused io
	l			: inout std_logic_vector(20 downto 1);
	r			: inout std_logic_vector(20 downto 1);
	t			: inout std_logic_vector(6 downto 1);
	b			: inout std_logic_vector(10 downto 1)
);
end jop;

architecture rtl of jop is

--
--	components:
--

component core is
generic(jpc_width	: integer);			-- address bits of java bytecode pc
port (
	clk, reset	: in std_logic;

-- memio connection

	bsy			: in std_logic;
	din			: in std_logic_vector(31 downto 0);
	ext_addr	: out std_logic_vector(EXTA_WIDTH-1 downto 0);
	rd, wr		: out std_logic;

-- jbc connections

	jbc_addr	: out std_logic_vector(jpc_width-1 downto 0);
	jbc_data	: in std_logic_vector(7 downto 0);
	jpc_wr		: out std_logic;
	bc_wr		: out std_logic;

-- interrupt from io

	irq			: in std_logic;
	irq_ena		: in std_logic;

	dout		: out std_logic_vector(31 downto 0)
);
end component;

component extension is
port (
	clk, reset	: in std_logic;

-- core interface

	din			: in std_logic_vector(31 downto 0);		-- from stack
	ext_addr	: in std_logic_vector(EXTA_WIDTH-1 downto 0);
	rd, wr		: in std_logic;
	dout		: out std_logic_vector(31 downto 0);	-- to stack

-- mem interface

	mem_rd		: out std_logic;
	mem_wr		: out std_logic;
	mem_addr_wr	: out std_logic;
	mem_bc_rd	: out std_logic;
	mem_data	: in std_logic_vector(31 downto 0); 	-- output of memory module
	mem_bcstart	: in std_logic_vector(31 downto 0); 	-- start of method in bc cache
	
-- io interface

	io_rd		: out std_logic;
	io_wr		: out std_logic;
	io_addr_wr	: out std_logic;
	io_data		: in std_logic_vector(31 downto 0)		-- output of io module
);
end component;

component mem is
generic (jpc_width : integer; ram_cnt : integer; rom_cnt : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(31 downto 0);

	mem_rd		: in std_logic;
	mem_wr		: in std_logic;
	mem_addr_wr	: in std_logic;
	mem_bc_rd	: in std_logic;
	dout		: out std_logic_vector(31 downto 0);
	bcstart		: out std_logic_vector(31 downto 0); 	-- start of method in bc cache

	bsy			: out std_logic;

-- jbc connections

	jbc_addr	: in std_logic_vector(jpc_width-1 downto 0);
	jbc_data	: out std_logic_vector(7 downto 0);
	jpc_wr		: in std_logic;
	bc_wr		: in std_logic;

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic
);
end component;

component io is
generic (clk_freq : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(31 downto 0);

-- interface to mem

	rd, wr		: in std_logic;
	addr_wr		: in std_logic;

	dout		: out std_logic_vector(31 downto 0);

-- interrupt

	irq			: out std_logic;
	irq_ena		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;

-- watch dog

	wd			: out std_logic;

-- core i/o pins
	l			: inout std_logic_vector(20 downto 1);
	r			: inout std_logic_vector(20 downto 1);
	t			: inout std_logic_vector(6 downto 1);
	b			: inout std_logic_vector(10 downto 1)
);
end component;

--
--	Signals
--

	signal stack_tos		: std_logic_vector(31 downto 0);
	signal rd, wr			: std_logic;
	signal ext_addr			: std_logic_vector(EXTA_WIDTH-1 downto 0);
	signal stack_din		: std_logic_vector(31 downto 0);

	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_addr_wr		: std_logic;
	signal mem_bc_rd		: std_logic;
	signal mem_dout			: std_logic_vector(31 downto 0);
	signal mem_bcstart		: std_logic_vector(31 downto 0);
	signal mem_bsy			: std_logic;

	signal jbc_addr			: std_logic_vector(jpc_width-1 downto 0);
	signal jbc_data			: std_logic_vector(7 downto 0);
	signal jpc_wr			: std_logic;
	signal bc_wr			: std_logic;

	signal io_rd			: std_logic;
	signal io_wr			: std_logic;
	signal io_addr_wr		: std_logic;
	signal io_dout			: std_logic_vector(31 downto 0);
	signal io_irq			: std_logic;
	signal io_irq_ena		: std_logic;

	signal aint				: std_logic_vector(18 downto 0);

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- for generationg internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";


begin

--
--	intern reset
--
process(clk, reset)
begin
	if rising_edge(clk) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;

		int_res <= reset or not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
	end if;
end process;

--
--	components of jop
--
	core: core generic map(jpc_width)
		port map (clk, int_res,
			mem_bsy,
			stack_din, ext_addr,
			rd, wr,
			jbc_addr, jbc_data, jpc_wr, bc_wr,
			io_irq, io_irq_ena,
			stack_tos
		);

	ext: extension
		port map (clk, int_res, stack_tos,
			ext_addr, rd, wr, stack_din,
			mem_rd, mem_wr, mem_addr_wr, mem_bc_rd,
			mem_dout, mem_bcstart,
			io_rd, io_wr, io_addr_wr, io_dout
		);

	mem: mem generic map (jpc_width, ram_cnt, rom_cnt)
		port map (clk, int_res, stack_tos,
			mem_rd, mem_wr, mem_addr_wr, mem_bc_rd,
			mem_dout, mem_bcstart,
			mem_bsy,
			jbc_addr, jbc_data, jpc_wr, bc_wr,
			aint, d, nram_cs, nrom_cs, nmem_rd, nmem_wr
		);

	io: io generic map (clk_freq)
		port map (clk, int_res, stack_tos,
			io_rd, io_wr, io_addr_wr, io_dout,
			io_irq, io_irq_ena,
			txd, rxd, ncts, nrts,
			wd,
			l, r, t, b
		);

--
--	some io pins changes
--
	a <= aint;
	ram_ax <= '1';						-- CS1 for 128kB ram
	-- ram_ax <= aint(17);				-- for 512kB ram

end rtl;
