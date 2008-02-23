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
--	wb_top_guitar.vhd
--
--	The top level for wishbone devices connected to JOP.
--	Do the address decoding here for the various slaves.
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--          
--          WISHBONE DATA SHEET
--          
--          Revision Level: B.3, Released: September 7, 2002
--          Type: MASTER
--          
--          Signals: record and address size is defines in wb_pack.vhd
--          
--          Port    Width   Direction   Description
--          ------------------------------------------------------------------------
--          clk       1     Input       Master clock, see JOP top level
--          reset     1     Input       Reset, see JOP top level
--          dat_o    32     Output      Data from JOP
--          adr_o     8     Output      Address bits for the slaves, see wb_pack.vhd
--          we_o      1     Output      Write enable output
--          cyc_o     1     Output      Valid bus cycle output
--          stb_o     1     Output      Strobe signal output
--          dat_i    32     Input       Data from the slaves to JOP
--          ack_i     1     Input       Bus cycle acknowledge input
--          
--          Port size: 32-bit
--          Port granularity: 32-bit
--          Maximum operand size: 32-bit
--          Data transfer ordering: BIG/LITTLE ENDIAN
--          Sequence of data transfer: UNDEFINED
--          
--          
--
--	2005-06-30	top level for guitar project
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;

entity wb_top is

port (
	clk		: in std_logic;
	reset	: in std_logic;
	wb_out	: in wb_master_out_type;
	wb_in	: out wb_master_in_type;
	wb_io	: inout io_ports
);
end wb_top;

architecture rtl of wb_top is

component ac97_top is
port (

	clk_i			: in std_logic;
	rst_i			: in std_logic;

-- WISHBONE SLAVE INTERFACE 
	wb_data_i		: in std_logic_vector(31 downto 0);
	wb_data_o		: out std_logic_vector(31 downto 0);
	wb_addr_i		: in std_logic_vector(31 downto 0);
	wb_sel_i		: in std_logic_vector(3 downto 0);
	wb_we_i			: in std_logic;
	wb_cyc_i		: in std_logic;
	wb_stb_i		: in std_logic;
	wb_ack_o		: out std_logic;
	wb_err_o		: out std_logic ;

-- Misc Signals
	int_o			: out std_logic;
	dma_req_o		: out std_logic_vector(8 downto 0);
	dma_ack_i		: in std_logic_vector(8 downto 0);
-- Suspend Resume Interface
	suspended_o		: out std_logic;

-- AC97 Codec Interface
	bit_clk_pad_i	: in std_logic;
	sync_pad_o		: out std_logic;
	sdata_pad_o		: out std_logic;
	sdata_pad_i		: in std_logic;
	ac97_reset_pad_o	: out std_logic
);
end component;


	constant SLAVE_CNT : integer := 3;

	type wbs_in_array is array(0 to SLAVE_CNT-1) of wb_slave_in_type;
	signal wbs_in		: wbs_in_array;
	type wbs_out_array is array(0 to SLAVE_CNT-1) of wb_slave_out_type;
	signal wbs_out		: wbs_out_array;

	signal module_addr	: std_logic_vector(M_ADDR_SIZE-S_ADDR_SIZE-1 downto 0);
	signal addr_part	: std_logic_vector(1 downto 0);

--
--	AC97 signals
--
	signal ac97_nres	: std_logic;
	signal ac97_sdo		: std_logic;
	signal ac97_sdi		: std_logic;
	signal ac97_syn		: std_logic;
	signal ac97_bclk	: std_logic;

	signal ac97_wb_adr	: std_logic_vector(31 downto 0);


begin

--
--	unused and input pins tri state
--
	wb_io.l(15 downto 6) <= (others => 'Z');
	wb_io.l(20 downto 18) <= (others => 'Z');
	wb_io.r(20 downto 14) <= (others => 'Z');
	wb_io.t <= (others => 'Z');
	wb_io.b(10 downto 3) <= (others => 'Z');

--	this is a simple point to point connection
--	wb_connect(wb_in, wb_out,
--		wb_s_in, wb_s_out);


	wbguit: entity work.wb_guitar port map(
		clk => clk,
		reset => reset,
		wb_in => wbs_in(0),
		wb_out => wbs_out(0),
		adc_in => wb_io.l(17),
		adc_out => wb_io.l(16),
		dac_l => wb_io.b(2),
		dac_r => wb_io.b(1)
	);


	wbusb: entity work.wb_usb port map(
		clk => clk,
		reset => reset,
		wb_in => wbs_in(1),
		wb_out => wbs_out(1),
		data => wb_io.r(8 downto 1),
		nrxf => wb_io.r(9),
		ntxe => wb_io.r(10),
		nrd => wb_io.r(11),
		wr => wb_io.r(12),
		nsi => wb_io.r(13)
	);

	ac97_wb_adr <= "00000000000000000000000000"
					& wbs_in(2).adr_i & "00";

	wbac97: ac97_top  port map(

		clk_i			=> clk,
		rst_i			=> not reset,	-- the AC97 core uses nreset!

-- WISHBONE SLAVE INTERFACE 
		wb_data_i		=> wbs_in(2).dat_i,
		wb_data_o		=> wbs_out(2).dat_o,
		wb_addr_i		=> ac97_wb_adr,
		wb_sel_i		=> "1111",
		wb_we_i			=> wbs_in(2).we_i,
		wb_cyc_i		=> wbs_in(2).cyc_i,
		wb_stb_i		=> wbs_in(2).stb_i,
		wb_ack_o		=> wbs_out(2).ack_o,
		wb_err_o		=> open,

-- Misc Signals
		int_o			=> open,
		dma_req_o		=> open,
		dma_ack_i		=> "000000000",
-- Suspend Resume Interface
		suspended_o		=> open,

-- AC97 Codec Interface
		bit_clk_pad_i	=> ac97_bclk,
		sync_pad_o		=> ac97_syn,
		sdata_pad_o		=> ac97_sdo,
		sdata_pad_i		=> ac97_sdi,
		ac97_reset_pad_o	=> ac97_nres
	);

	wb_io.l(1) <= ac97_sdo;
	ac97_bclk <= wb_io.l(2);	-- this one is inout on AC97/AD1981BL
	wb_io.l(2) <= 'Z';
	ac97_sdi <= wb_io.l(3);
	wb_io.l(3) <= 'Z';
	wb_io.l(4) <= ac97_syn;
	wb_io.l(5) <= ac97_nres;


	--
	-- two simple test slaves
	--
	gsl: for i in 0 to SLAVE_CNT-1 generate
		wbs_in(i).dat_i <= wb_out.dat_o;
		wbs_in(i).we_i <= wb_out.we_o;
		wbs_in(i).adr_i <= wb_out.adr_o(S_ADDR_SIZE-1 downto 0);
		wbs_in(i).cyc_i <= wb_out.cyc_o;
	end generate;

--
--	This is the address decoding and the data muxer.
--
--		we use negative addresse for fast constant load
--		base is 0xffffff80
--
	module_addr <= wb_out.adr_o(M_ADDR_SIZE-1 downto S_ADDR_SIZE);
	addr_part <= module_addr(1 downto 0);

process(addr_part, wb_out, wbs_out)
begin

	wbs_in(0).stb_i <= '0';
	wbs_in(1).stb_i <= '0';
	wbs_in(2).stb_i <= '0';

--	if wb_out.adr_o(S_ADDR_SIZE)='0' then
--		wbs_in(0).stb_i <= wb_out.stb_o;
--		wb_in.dat_i <= wbs_out(0).dat_o;
--		wb_in.ack_i <= wbs_out(0).ack_o;
--	else
--		wbs_in(1).stb_i <= wb_out.stb_o;
--		wb_in.dat_i <= wbs_out(1).dat_o;
--		wb_in.ack_i <= wbs_out(1).ack_o;
--	end if;

	if addr_part="00" then
		wbs_in(0).stb_i <= wb_out.stb_o;
		wb_in.dat_i <= wbs_out(0).dat_o;
		wb_in.ack_i <= wbs_out(0).ack_o;
	elsif addr_part="01" then
		wbs_in(1).stb_i <= wb_out.stb_o;
		wb_in.dat_i <= wbs_out(1).dat_o;
		wb_in.ack_i <= wbs_out(1).ack_o;
	else
		wbs_in(2).stb_i <= wb_out.stb_o;
		wb_in.dat_i <= wbs_out(2).dat_o;
-- wb_in.dat_i <= "00000000000000000000000000001010";
		wb_in.ack_i <= wbs_out(2).ack_o;
	end if;

end process;

end rtl;
