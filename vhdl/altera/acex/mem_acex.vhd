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
--	mem.vhd
--
--	external memory interface for ACEX jopcore board
--
--
--	memory mapping
--	
--		x00000-x7ffff	external ram (w mirror)	max. 512 kW (4*4 MBit)
--		x80000-xfffff	external rom (w mirror)	max. 512 kB (4 MBit)
--
--	ram: 32 bit word
--		mapping ain(16 downto 0) to a(18 downto 2), ignoring ain(18,17)
--	rom: 8 bit word (for flash programming)
--		mapping ain(18 downto 0) to a(18 downto 0)
--
--	todo:
--		mem access to ram is low byte first. this is a little bit inconsistent.
--
--
--	2002-01-03	copy from memio.vhd for bb project
--	2002-01-06	added rs485 uart
--	2001-02-01	some changes in io port definitions
--	2002-05-07	added 'hw' mul
--	2002-07-27	io definitions for baseio
--	2002-08-02	second uart (use first for download and debug)
--	2002-11-01	removed second uart
--	2002-12-01	ram_cnt to 2 (for 20 MHz)
--	2002-12-01	moved all io to ioeth.vhd
--	2002-12-02	wait instruction for memory
--	2003-06-20	changed tristate output for Quartus
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity mem is
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
end mem;

architecture rtl of mem is


--
--	jbc component (use technology specific vhdl-file (ajbc/xjbc))
--
--	dual port ram
--	wraddr and wrena registered and delayed
--	rdaddr is registered
--	indata registered
--	outdata is unregistered
--
component jbc is
generic (width : integer; addr_width : integer);
port (
	data		: in std_logic_vector(31 downto 0);
	rdaddress	: in std_logic_vector(jpc_width-1 downto 0);
	wr_addr		: in std_logic;									-- load start address (=jpc)
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(7 downto 0)
);
end component;

--
--	signals for mem interface
--
	signal maddr				: std_logic_vector(18 downto 0);
	signal mdout				: std_logic_vector(7 downto 0);
	signal d_ena				: std_logic;

	type state_type		is (
							idl, rd1, rd2, rd3, rd4, wr1, wr2, wr3, wr4
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(19 downto 0);
	signal mem_wr_val		: std_logic_vector(31 downto 0);
	signal mem_rd_val		: std_logic_vector(31 downto 0);
	signal mem_bsy			: std_logic;

	signal nwr_int			: std_logic;
	signal ram_access		: std_logic;

begin

	bsy <= mem_bsy;
	dout <= mem_rd_val;
	bcstart <= (others => '0');	-- for now we load only at base 0

	cmp_jbc: jbc generic map (8, jpc_width) port map(din, jbc_addr, jpc_wr, bc_wr, clk, jbc_data);

--
--	wr_addr write
--		one cycle after io write (address is avaliable one cycle before ex stage)
--
process(clk, reset, din, mem_addr_wr)

begin
	if (reset='1') then

		mem_wr_addr <= std_logic_vector(to_unsigned(0, 20));

	elsif rising_edge(clk) then

		if (mem_addr_wr='1') then
			mem_wr_addr <= din(19 downto 0);	-- store write address
		end if;

	end if;
end process;


--
--	'delay' nwr 1/2 cycle -> change on falling edge
--
process(clk, reset, nwr_int)

begin
	if (reset='1') then
		nwr <= '1';
	elsif falling_edge(clk) then
		nwr <= nwr_int;
	end if;

end process;

--	tristate output
--	address on reset (just for the config)

process(reset, maddr, d_ena, mdout)

begin
	if (reset='1') then
		a <= (others => 'Z');
	else
		a <= maddr;
	end if;

	if (d_ena='1') then
		d <= mdout;
	else
		d <= (others => 'Z');
	end if;
end process;


--
--	state machine for external memory (single byte static ram, flash)
--
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr)

	variable i : integer range 0 to 7;

begin
	if (reset='1') then
		state <= idl;
		maddr <= (others => '0');
		mdout <= (others => '0');
		d_ena <='0';
		nram_cs <= '1';
		nrom_cs <= '1';
		nrd <= '1';
		nwr_int <= '1';
		ram_access <= '1';
		mem_rd_val <= std_logic_vector(to_unsigned(0, 32));
		mem_wr_val <= std_logic_vector(to_unsigned(0, 32));
		mem_bsy <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				d_ena <='0';
				nrd <= '1';
				nwr_int <= '1';
				nram_cs <= '1';
				nrom_cs <= '1';
				ram_access <= '1';
				mem_bsy <= '0';

				if (mem_rd='1') then
					if (din(19)='1') then
						maddr <= din(18 downto 0);
						nrom_cs <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else
						maddr <= din(16 downto 0) & "00";
						nram_cs <= '0';
						ram_access <= '1';
						i := ram_cnt;
					end if;
					mem_bsy <= '1';
					nrd <= '0';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_wr_val <= din;
					if (mem_wr_addr(19)='1') then
						maddr <= mem_wr_addr(18 downto 0);
						nrom_cs <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else
						maddr <= mem_wr_addr(16 downto 0) & "00";
						nram_cs <= '0';
						ram_access <= '1';
						i := ram_cnt+1;			-- one more for single cycle read
					end if;
					mem_bsy <= '1';
					nwr_int <= '0';
					state <= wr1;
				end if;

--
--	memory read
--
			when rd1 =>
				i := i-1;
				if (i=0) then
					if (ram_access='1') then
						state <= rd2;
						mem_rd_val(7 downto 0) <= d;
						maddr(1 downto 0) <= "01";
						i := ram_cnt;
					else
						state <= idl;
						mem_bsy <= '0';
						mem_rd_val <= std_logic_vector(to_unsigned(0, 32-8)) & d;
					end if;
				end if;

			when rd2 =>
				i := i-1;
				if (i=0) then
					state <= rd3;
					mem_rd_val(15 downto 8) <= d;
					maddr(1 downto 0) <= "10";
					i := ram_cnt;
				end if;
					
			when rd3 =>
				i := i-1;
				if (i=0) then
					state <= rd4;
					mem_rd_val(23 downto 16) <= d;
					maddr(1 downto 0) <= "11";
					i := ram_cnt;
				end if;
					
			when rd4 =>
				i := i-1;
				if (i=1) then
					mem_bsy <= '0';					-- release mem_bsy one cycle earlier
				end if;
				if (i=0) then
					state <= idl;
					mem_rd_val(31 downto 24) <= d;
				end if;
--
--	memory write
--
			when wr1 =>
				i := i-1;
				d_ena <='1';
				mdout <= mem_wr_val(7 downto 0);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					if (ram_access='1') then
						nwr_int <= '0';
						state <= wr2;
						maddr(1 downto 0) <= "01";
						i := ram_cnt+1;
					else
						state <= idl;
						mem_bsy <= '0';
					end if;
				end if;

			when wr2 =>
				i := i-1;
				mdout <= mem_wr_val(15 downto 8);
				nwr_int <= '0';
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= wr3;
					nwr_int <= '0';
					maddr(1 downto 0) <= "10";
					i := ram_cnt+1;
				end if;
					
			when wr3 =>
				i := i-1;
				mdout <= mem_wr_val(23 downto 16);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= wr4;
					nwr_int <= '0';
					maddr(1 downto 0) <= "11";
					i := ram_cnt+1;
				end if;
					
			when wr4 =>
				i := i-1;
				mdout <= mem_wr_val(31 downto 24);
				if (i=1) then
					nwr_int <= '1';
					mem_bsy <= '0';					-- release mem_bsy one cycle earlier
				end if;
				if (i=0) then
					state <= idl;
					nwr_int <= '1';
				end if;
					
		end case;
					
	end if;
end process;


end rtl;
