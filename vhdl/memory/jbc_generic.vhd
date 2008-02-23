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


--LIBRARY ieee;
--USE ieee.std_logic_1164.ALL;
--ENTITY ram IS
--PORT (
--clock: IN STD_LOGIC;
--data: IN STD_LOGIC_VECTOR (7 DOWNTO 0);
--write_address: IN INTEGER RANGE 0 to 31;
--read_address: IN INTEGER RANGE 0 to 31;
--we: IN STD_LOGIC;
--q: OUT STD_LOGIC_VECTOR (7 DOWNTO 0)
--);
--END ram;
--ARCHITECTURE rtl OF ram IS
--TYPE MEM IS ARRAY(0 TO 31) OF STD_LOGIC_VECTOR(7 DOWNTO 0);
--SIGNAL ram_block: MEM;
--BEGIN
--PROCESS (clock)
--BEGIN
--IF (clock'event AND clock = '1') THEN
--IF (we = '1') THEN
--ram_block(write_address) <= data;
--END IF;
--q <= ram_block(read_address);
---- VHDL semantics imply that q doesn't get data
---- in this clock cycle
--END IF;
--END PROCESS;
--END rtl;

--
--	cyc_jbc.vhd
--
--	bytecode memory/cache for JOP3
--	Version for Altera Cyclone
--
--	address, data in are registered
--	data out is unregistered
--
--
--	Changes:
--		2003-08-14	load start address with jpc_wr and do autoincrement
--					load 32 bit data and do the 4 byte writes serial
--		2005-02-17	extrected again from mem32.vhd
--		2005-05-03	address width is jpc_width
--
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity jbc is
generic (jpc_width : integer := 10);
port (
	clk			: in std_logic;
	data		: in std_logic_vector(31 downto 0);
	rd_addr		: in std_logic_vector(jpc_width-1 downto 0);
	wr_addr		: in std_logic_vector(jpc_width-3 downto 0);
	wr_en		: in std_logic;
	q			: out std_logic_vector(7 downto 0)
);
end jbc;

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
architecture rtl of jbc is

	constant nwords : integer := 2**(jpc_width-2);
	type mem is array(0 to nwords-1) of std_logic_vector(31 downto 0);
	signal ram_block: mem;

	signal d: std_logic_vector(31 downto 0);

	signal wra0, wra1, wra2, wra3	: std_logic_vector(jpc_width-1 downto 0);
	signal rda_reg					: std_logic_vector(jpc_width-1 downto 0);

begin

--BEGIN
--PROCESS (clock)
--BEGIN
--IF (clock'event AND clock = '1') THEN
--IF (we = '1') THEN
--ram_block(write_address) <= data;
--END IF;
--q <= ram_block(read_address);
---- VHDL semantics imply that q doesn't get data
---- in this clock cycle
--END IF;
--END PROCESS;
--END rtl;

	wra0 <= wr_addr & "00";
	wra1 <= wr_addr & "01";
	wra2 <= wr_addr & "10";
	wra3 <= wr_addr & "11";

	d <= ram_block(to_integer(unsigned(rda_reg(jpc_width-1 downto 2))));

process(clk)
begin

	if rising_edge(clk) then
		if wr_en='1' then
			ram_block(to_integer(unsigned(wr_addr))) <= data;
--			ram_block(to_integer(unsigned(wra1))) <= data(15 downto 8);
--			ram_block(to_integer(unsigned(wra2))) <= data(23 downto 16);
--			ram_block(to_integer(unsigned(wra3))) <= data(31 downto 24);
		end if;

		rda_reg <= rd_addr;
		-- q <= ram_block(to_integer(unsigned(rd_addr(jpc_width-1 downto 0))));
		-- VHDL semantics imply that q doesn't get data
		-- in this clock cycle


	end if;
end process;


process(rda_reg, d)
begin
	case rda_reg(1 downto 0) is
		when "11" =>
			q <= d(31 downto 24);
		when "10" =>
			q <= d(23 downto 16);
		when "01" =>
			q <= d(15 downto 8);
		when "00" =>
			q <= d(7 downto 0);
		when others =>
			null;
	end case;
end process;

----
----	generated with Quartus wizzard:
----
--	COMPONENT altsyncram
--	GENERIC (
--		intended_device_family		: STRING;
--		operation_mode		: STRING;
--		width_a				: NATURAL;
--		widthad_a			: NATURAL;
--		numwords_a			: NATURAL;
--		width_b				: NATURAL;
--		widthad_b			: NATURAL;
--		numwords_b			: NATURAL;
--		lpm_type			: STRING;
--		width_byteena_a		: NATURAL;
--		outdata_reg_b		: STRING;
--		indata_aclr_a		: STRING;
--		wrcontrol_aclr_a	: STRING;
--		address_aclr_a		: STRING;
--		address_reg_b		: STRING;
--		address_aclr_b		: STRING;
--		outdata_aclr_b		: STRING;
--		read_during_write_mode_mixed_ports		: STRING
--	);
--	PORT (
--			wren_a		: IN STD_LOGIC ;
--			clock0		: IN STD_LOGIC ;
--			address_a	: IN STD_LOGIC_VECTOR (jpc_width-3 DOWNTO 0);
--			address_b	: IN STD_LOGIC_VECTOR (jpc_width-1 DOWNTO 0);
--			q_b			: OUT STD_LOGIC_VECTOR (7 DOWNTO 0);
--			data_a		: IN STD_LOGIC_VECTOR (31 DOWNTO 0)
--	);
--	END COMPONENT;
--
--begin
--
--	alt_jbc : altsyncram
--	GENERIC MAP (
--		intended_device_family => "Cyclone",
--		operation_mode => "DUAL_PORT",
--		width_a => 32,
--		widthad_a => jpc_width-2,
--		numwords_a => 2**(jpc_width-2),
--		width_b => 8,
--		widthad_b => jpc_width,
--		numwords_b => 2**jpc_width,
--		lpm_type => "altsyncram",
--		width_byteena_a => 1,
--		outdata_reg_b => "UNREGISTERED",
--		indata_aclr_a => "NONE",
--		wrcontrol_aclr_a => "NONE",
--		address_aclr_a => "NONE",
--		address_reg_b => "CLOCK0",
--		address_aclr_b => "NONE",
--		outdata_aclr_b => "NONE",
--		read_during_write_mode_mixed_ports => "DONT_CARE"
--	)
--	PORT MAP (
--		wren_a => wr_en,
--		clock0 => clk,
--		address_a => wr_addr,
--		address_b => rd_addr,
--		data_a => data,
--		q_b => q
--	);
--
--
end rtl;
