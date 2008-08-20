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

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;


architecture rtl of jop_avalon is

--
--	components:
--


component sc2avalon is
generic (addr_bits : integer);

port (

	clk, reset	: in std_logic;

-- SimpCon interface

	sc_address		: in std_logic_vector(addr_bits-1 downto 0);
	sc_wr_data		: in std_logic_vector(31 downto 0);
	sc_rd, sc_wr	: in std_logic;
	sc_rd_data		: out std_logic_vector(31 downto 0);
	sc_rdy_cnt		: out unsigned(1 downto 0);

-- Avalon interface

	av_address		: out std_logic_vector(addr_bits-1+2 downto 0);
	av_writedata	: out std_logic_vector(31 downto 0);
	av_byteenable	: out std_logic_vector(3 downto 0);
	av_readdata		: in std_logic_vector(31 downto 0);
	av_read			: out std_logic;
	av_write		: out std_logic;
	av_waitrequest	: in std_logic

);
end component;



--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------






--
--	Signals
--

	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

--
--	jopcpu connections
--
	signal sc_mem_out		: sc_out_type;
	signal sc_mem_in		: sc_in_type;
	signal sc_io_out		: sc_out_type;
	signal sc_io_in			: sc_in_type;
	signal irq_in			: irq_bcf_type;
	signal irq_out			: irq_ack_type;
	signal exc_req			: exception_type;

--
--	IO interface
--
	signal ser_in			: ser_in_type;
	signal ser_out			: ser_out_type;
	signal wd_out			: std_logic;

	-- for generation of internal reset
-- memory interface

	signal ram_addr			: std_logic_vector(17 downto 0);
	signal ram_dout			: std_logic_vector(15 downto 0);
	signal ram_din			: std_logic_vector(15 downto 0);
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;
	
	signal ram_cnt			: integer := 3;
	
-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;

begin

	ser_ncts <= '0';
--
--	avalon reset (hopefully synchronized...)
--
	int_res <= reset;

	-- no pll inside the avalon component
	clk_int <= clk;

	wd <= wd_out;

	cpm_cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => 0
		)
		port map(clk_int, int_res,
			sc_mem_out, sc_mem_in,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req);

	cmp_io: entity work.scio 
		port map (clk_int, int_res,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req,

			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open
		);

-- 	cmp_scm: entity work.sc_mem_if
-- 		generic map (
-- 			ram_ws => ram_cnt-1,
-- 			addr_bits => 18
-- 		)
-- 		port map (clk_int, int_res,
-- 			sc_mem_out, sc_mem_in,
--   
-- 			ram_addr => ram_addr,
-- 			ram_dout => ram_dout,
-- 			ram_din => ram_din,
-- 			ram_dout_en	=> ram_dout_en,
-- 			ram_ncs => ram_ncs,
-- 			ram_noe => ram_noe,
-- 			ram_nwe => ram_nwe
-- 		);

	sc2av: sc2avalon
		generic map (
			addr_bits => addr_bits
		)
		port map (
			clk => clk_int,
			reset => int_res,

			sc_address => "0" & sc_mem_out.address,
			sc_wr_data => sc_mem_out.wr_data,
			sc_rd => sc_mem_out.rd,
			sc_wr => sc_mem_out.wr,
			sc_rd_data => sc_mem_in.rd_data,
			sc_rdy_cnt => sc_mem_in.rdy_cnt,

			av_address => address,
			av_writedata => writedata,
			av_byteenable => byteenable,
			av_readdata => readdata,
			av_read => read,
			av_write => write,
			av_waitrequest => waitrequest
		);

end rtl;
