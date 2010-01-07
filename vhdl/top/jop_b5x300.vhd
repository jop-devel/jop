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
--	jop_b5x300.vhd
--
--	top level for jop on Burched B5-X300 Board with Spartan-IIe XC2S300E FPGA
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
	clk_freq	: integer := 25000000;	-- 25 MHz clock frequency
	width		: integer := 32;	-- one data word
	ioa_width	: integer := 3;		-- address bits of internal io
	ram_cnt		: integer := 2;		-- clock cycles for external ram
	rom_cnt		: integer := 3		-- clock cycles for external rom
);

port (
	clk, Rst_n	: in std_logic;

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	ram_ax		: out std_logic;
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nmem_rd		: out std_logic;
	nmem_wr_l		: out std_logic;
	nmem_wr_h		: out std_logic;

--	WD
	wd			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic

);
end jop;

architecture rtl of jop is

--
--	components:
--

component core is
port (
	clk, reset	: in std_logic;

-- memio connection

	bsy			: in std_logic;
	din			: in std_logic_vector(width-1 downto 0);
	addr		: out std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: out std_logic;

-- interrupt from io

	irq			: in std_logic;
	irq_ena		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0)
);
end component;

component mem is
generic (width : integer; ioa_width : integer; ram_cnt : integer; rom_cnt : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	bsy			: out std_logic;
	dout		: out std_logic_vector(width-1 downto 0);

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic;

-- io interface

	io_din		: in std_logic_vector(width-1 downto 0);
	io_rd		: out std_logic;
	io_wr		: out std_logic;
	io_addr_wr	: out std_logic

);
end component;

component io is
generic (clk_freq : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);

-- interface to mem

	rd, wr		: in std_logic;
	addr_wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

-- interrupt

	irq			: out std_logic;
	irq_ena		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;

-- watch dog

	wd			: out std_logic

);
end component;

--
--	Signals
--

	signal memio_din		: std_logic_vector(width-1 downto 0);

	signal mem_addr			: std_logic_vector(ioa_width-1 downto 0);
	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_bsy			: std_logic;
	signal mem_dout			: std_logic_vector(width-1 downto 0);

	signal io_rd			: std_logic;
	signal io_wr			: std_logic;
	signal io_addr_wr		: std_logic;
	signal io_dout			: std_logic_vector(width-1 downto 0);
	signal io_irq			: std_logic;
	signal io_irq_ena		: std_logic;

	signal aint				: std_logic_vector(18 downto 0);

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0);

	signal reset				: std_logic;
	signal buf_rst_n 		: std_logic;
	signal nmem_wr			: std_logic;


begin

	Buf_Rst_n <= Rst_n;
	-- Register the reset signal
	process (Clk)
	begin
		if (Clk'event and Clk='1') then
			reset <= not Buf_Rst_n;
		end if;
	end process;
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
	core: core 
		port map (clk, int_res,
			mem_bsy,
			mem_dout, mem_addr,
			mem_rd, mem_wr,
			io_irq, io_irq_ena,
			memio_din
		);

	mem: mem generic map (width, ioa_width, ram_cnt, rom_cnt)
		port map (clk, int_res, memio_din, mem_addr, mem_rd, mem_wr, mem_bsy, mem_dout,
			aint, d, nram_cs, nrom_cs, nmem_rd, nmem_wr,
			io_dout, io_rd, io_wr, io_addr_wr
		);

	io: io generic map (clk_freq)
		port map (clk, int_res, memio_din,
			io_rd, io_wr, io_addr_wr, io_dout,
			io_irq, io_irq_ena,
			txd, rxd, ncts, nrts,
			wd
		);

--
--	some io pins changes
--
	a <= aint;
	ram_ax <= '1';						-- CS1 for 128kB ram
	-- ram_ax <= aint(17);				-- for 512kB ram

	nmem_wr_l <= nmem_wr;
	nmem_wr_h <= nmem_wr;

end rtl;
