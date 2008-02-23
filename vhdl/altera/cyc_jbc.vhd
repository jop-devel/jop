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
--		2005-02-17	extracted again from mem32.vhd
--		2005-05-03	address width is jpc_width
--
--

library altera_mf;
use altera_mf.altera_mf_components.all;

library ieee;
use ieee.std_logic_1164.all;

entity jbc is
generic (jpc_width : integer);
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
--	registered wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
architecture rtl of jbc is

--
--	generated with Quartus wizzard:
--
	COMPONENT altsyncram
	GENERIC (
		intended_device_family		: STRING;
		operation_mode		: STRING;
		width_a				: NATURAL;
		widthad_a			: NATURAL;
		numwords_a			: NATURAL;
		width_b				: NATURAL;
		widthad_b			: NATURAL;
		numwords_b			: NATURAL;
		lpm_type			: STRING;
		width_byteena_a		: NATURAL;
		outdata_reg_b		: STRING;
		indata_aclr_a		: STRING;
		wrcontrol_aclr_a	: STRING;
		address_aclr_a		: STRING;
		address_reg_b		: STRING;
		address_aclr_b		: STRING;
		outdata_aclr_b		: STRING;
		read_during_write_mode_mixed_ports		: STRING
	);
	PORT (
			wren_a		: IN STD_LOGIC ;
			clock0		: IN STD_LOGIC ;
			address_a	: IN STD_LOGIC_VECTOR (jpc_width-3 DOWNTO 0);
			address_b	: IN STD_LOGIC_VECTOR (jpc_width-1 DOWNTO 0);
			q_b			: OUT STD_LOGIC_VECTOR (7 DOWNTO 0);
			data_a		: IN STD_LOGIC_VECTOR (31 DOWNTO 0)
	);
	END COMPONENT;

begin

	alt_jbc : altsyncram
	GENERIC MAP (
		intended_device_family => "Cyclone",
		operation_mode => "DUAL_PORT",
		width_a => 32,
		widthad_a => jpc_width-2,
		numwords_a => 2**(jpc_width-2),
		width_b => 8,
		widthad_b => jpc_width,
		numwords_b => 2**jpc_width,
		lpm_type => "altsyncram",
		width_byteena_a => 1,
		outdata_reg_b => "UNREGISTERED",
		indata_aclr_a => "NONE",
		wrcontrol_aclr_a => "NONE",
		address_aclr_a => "NONE",
		address_reg_b => "CLOCK0",
		address_aclr_b => "NONE",
		outdata_aclr_b => "NONE",
		read_during_write_mode_mixed_ports => "DONT_CARE"
	)
	PORT MAP (
		wren_a => wr_en,
		clock0 => clk,
		address_a => wr_addr,
		address_b => rd_addr,
		data_a => data,
		q_b => q
	);


end rtl;
