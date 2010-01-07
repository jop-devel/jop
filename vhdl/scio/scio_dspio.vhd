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
--	scio_dspio.vhd
--
--	io devices for dspio board
--
--
--	io address mapping:
--
--	IO Base is 0xffffff80 for 'fast' constants (bipush)
--
--		0x00 0-3		system clock counter, us counter, timer int, wd bit
--		0x10 0-1		uart (download)
--		0x20 0-1		USB connection (download)
-- 		0x30			SPI con
--		0x40 0-f		AC97 connection
--
--	status word in uart and usb:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--
--
--	2003-07-09	created
--	2005-08-27	ignore ncts on uart
--	2005-11-30	changed to SimpCon
--	2005-12-20	dspio board
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.jop_config.all;
use work.wb_pack.all;
use work.sc_pack.all;

entity scio is
generic (cpu_id : integer := 0; cpu_cnt : integer := 1);
port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon IO interface
--
	sc_io_out		: in sc_out_type;
	sc_io_in		: out sc_in_type;

--
--	Interrupts from IO devices
--
	irq_in			: out irq_bcf_type;
	irq_out			: in irq_ack_type;
	exc_req			: in exception_type;
		
-- CMP

	sync_out : in sync_out_type := NO_SYNC;
	sync_in	 : out sync_in_type;

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
end scio;


architecture rtl of scio is

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



component simple_spi_top is
port (

	clk_i			: in std_logic;
	rst_i			: in std_logic;

-- WISHBONE SLAVE INTERFACE 
	adr_i		: in std_logic_vector(1 downto 0);         --// lower address bits
	dat_i		: in std_logic_vector(31 downto 0);        --// databus input
	dat_o		: out std_logic_vector(31 downto 0);        --// databus output
--	sel_i		: in std_logic_vector(3 downto 0);         --// byte select inputs
	we_i			: in std_logic;          --// write enable input
	stb_i		: in std_logic;         --// stobe/core select signal
	cyc_i		: in std_logic;         --// valid bus cycle input
	ack_o		: out std_logic;         --// bus cycle acknowledge output
--	err_o		: out std_logic ;         --// termination w/ error
	inta_o		: out std_logic;         --// interrupt request signal output




-- Misc Signals
--	dma_req_o		: out std_logic_vector(8 downto 0);
--	dma_ack_i		: in std_logic_vector(8 downto 0);
-- Suspend Resume Interface
--	suspended_o		: out std_logic;


 --// SPI Interface                                     
	ss_o  		: out std_logic;        --// slave select
	sck_o		: out std_logic;       --// serial clock
	mosi_o		: out std_logic;       --// master out slave in
	miso_i		: in std_logic       --// master in slave out

);
end component;





	constant SLAVE_CNT : integer := 5;
	-- SLAVE_CNT <= 2**DECODE_BITS
	constant DECODE_BITS : integer := 3;
	-- number of bits that can be used inside the slave
	constant SLAVE_ADDR_BITS : integer := 4;

	type slave_bit is array(0 to SLAVE_CNT-1) of std_logic;
	signal sc_rd, sc_wr		: slave_bit;

	type slave_dout is array(0 to SLAVE_CNT-1) of std_logic_vector(31 downto 0);
	signal sc_dout			: slave_dout;

	type slave_rdy_cnt is array(0 to SLAVE_CNT-1) of unsigned(1 downto 0);
	signal sc_rdy_cnt		: slave_rdy_cnt;

	signal sel, sel_reg		: integer range 0 to 2**DECODE_BITS-1;











	-- Wishbone interface for the AC97
	signal wb_out			: wb_master_out_type;
	signal wb_in			: wb_master_in_type;

	constant WB_SLAVE_CNT : integer := 1;

	type wbs_in_array is array(0 to WB_SLAVE_CNT-1) of wb_slave_in_type;
	signal wbs_in		: wbs_in_array;
	type wbs_out_array is array(0 to WB_SLAVE_CNT-1) of wb_slave_out_type;
	signal wbs_out		: wbs_out_array;

--
--	AC97 signals
--
	constant WB_AC97 : integer := 0;

	signal ac97_nres	: std_logic;
	signal ac97_sdo		: std_logic;
	signal ac97_sdi		: std_logic;
	signal ac97_syn		: std_logic;
	signal ac97_bclk	: std_logic;

	signal ac97_wb_adr	: std_logic_vector(31 downto 0);
	signal not_reset : std_logic; -- for sim




	-- Wishbone interface for the SPI
	signal wb_out2			: wb_master_out_type;
	signal wb_in2			: wb_master_in_type;


	type wbs_in_array2 is array(0 to WB_SLAVE_CNT-1) of wb_slave_in_type;
	signal wbs_in2		: wbs_in_array;
	type wbs_out_array2 is array(0 to WB_SLAVE_CNT-1) of wb_slave_out_type;
	signal wbs_out2		: wbs_out_array;




--
--	SPI Signals
--

	constant WB_SPI : integer := 0;

	--signal spi_ss  		: std_logic_vector(7 downto 0);      --// slave select
	signal spi_sclk		: std_logic;       --// serial clock
	signal spi_mosi		: std_logic;       --// master out slave in
	signal spi_miso		: std_logic; 
	signal spi_ss		: std_logic;   

	signal spi_wb_adr	: std_logic_vector(31 downto 0);
	--signal not_reset : std_logic; -- for sim


begin

	not_reset <= not reset;
--	unused and input pins tri state
--
	l(20 downto 5) <= (others => 'Z');
	--r(20 downto 14) <= (others => 'Z');
	t <= (others => 'Z');
	b <= (others => 'Z');

	assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer(unsigned(sc_io_out.address(SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS)));

	-- What happens when sel_reg > SLAVE_CNT-1??
	sc_io_in.rd_data <= sc_dout(sel_reg);
	sc_io_in.rdy_cnt <= sc_rdy_cnt(sel_reg);

	--
	-- Connect SLAVE_CNT slaves
	--
	gsl: for i in 0 to SLAVE_CNT-1 generate

		sc_rd(i) <= sc_io_out.rd when i=sel else '0';
		sc_wr(i) <= sc_io_out.wr when i=sel else '0';

	end generate;

	--
	--	Register read and write mux selector
	--
	process(clk, reset)
	begin
		if (reset='1') then
			sel_reg <= 0;
		elsif rising_edge(clk) then
			if sc_io_out.rd='1' or sc_io_out.wr='1' then
				sel_reg <= sel;
			end if;
		end if;
	end process;
			
	sys: entity work.sc_sys generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			cpu_id => cpu_id,
			cpu_cnt => cpu_cnt
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(0),
			wr => sc_wr(0),
			rd_data => sc_dout(0),
			rdy_cnt => sc_rdy_cnt(0),

			irq_in => irq_in,
			irq_out => irq_out,
			exc_req => exc_req,
			
			sync_out => sync_out,
			sync_in => sync_in,
			
			wd => wd
		);

	ua: entity work.sc_uart generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 115200,
			txf_depth => 2,
			txf_thres => 1,
			rxf_depth => 2,
			rxf_thres => 1
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(1),
			wr => sc_wr(1),
			rd_data => sc_dout(1),
			rdy_cnt => sc_rdy_cnt(1),

			txd	 => txd,
			rxd	 => rxd,
			ncts => '0',
			nrts => nrts
	);

	usb: entity work.sc_usb generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(2),
			wr => sc_wr(2),
			rd_data => sc_dout(2),
			rdy_cnt => sc_rdy_cnt(2),

			data => r(8 downto 1),
			nrxf => r(9),
			ntxe => r(10),
			nrd => r(11),
			ft_wr => r(12),
			nsi => r(13)
	);



	-- SimpCon Wishbone bridge for Simple SPI
	wbspi: entity work.sc2wb generic map (
			addr_bits => SLAVE_ADDR_BITS
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(3),
			wr => sc_wr(3),
			rd_data => sc_dout(3),
			rdy_cnt => sc_rdy_cnt(3),

			wb_out => wb_out2,
			wb_in => wb_in2
	);


	-- SimpCon Wishbone bridge for AC97
	wb: entity work.sc2wb generic map (
			addr_bits => SLAVE_ADDR_BITS
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(4),
			wr => sc_wr(4),
			rd_data => sc_dout(4),
			rdy_cnt => sc_rdy_cnt(4),

			wb_out => wb_out,
			wb_in => wb_in
	);


	--
	-- master/slave connection for AC97
	--
	gwsl: for i in 0 to WB_SLAVE_CNT-1 generate
		wbs_in(i).dat_i <= wb_out.dat_o;
		wbs_in(i).we_i <= wb_out.we_o;
		wbs_in(i).adr_i <= wb_out.adr_o(S_ADDR_SIZE-1 downto 0);
		wbs_in(i).cyc_i <= wb_out.cyc_o;
	end generate;


	gwsl2: for i in 0 to WB_SLAVE_CNT-1 generate
		wbs_in2(i).dat_i <= wb_out2.dat_o;
		wbs_in2(i).we_i <= wb_out2.we_o;
		wbs_in2(i).adr_i <= wb_out2.adr_o(S_ADDR_SIZE-1 downto 0);
		wbs_in2(i).cyc_i <= wb_out2.cyc_o;
	end generate;


			wbs_in(WB_AC97).stb_i <= wb_out.stb_o;
			wb_in.dat_i <= wbs_out(WB_AC97).dat_o;
			wb_in.ack_i <= wbs_out(WB_AC97).ack_o;

			wbs_in2(WB_SPI).stb_i <= wb_out2.stb_o;
			wb_in2.dat_i <= wbs_out2(WB_SPI).dat_o;
			wb_in2.ack_i <= wbs_out2(WB_SPI).ack_o;


--process(wb_out, wbs_out)
--begin

--	if wb_out.adr_o(S_ADDR_SIZE)='0' then
--		wbs_in(0).stb_i <= wb_out.stb_o;
--		wbs_in(1).stb_i <= '0';
--		wb_in.dat_i <= wbs_out(0).dat_o;
--		wb_in.ack_i <= wbs_out(0).ack_o;
--	else
--		wbs_in(0).stb_i <= '0';
--		wbs_in(1).stb_i <= wb_out.stb_o;
--		wb_in.dat_i <= wbs_out(1).dat_o;
--		wb_in.ack_i <= wbs_out(1).ack_o;
--	end if;

--end process;



	-- AC97 Wishbone component
	ac97_wb_adr <= "00000000000000000000000000"
					& wbs_in(WB_AC97).adr_i & "00";

	wbac97: ac97_top  port map(

		clk_i			=> clk,
		rst_i			=> not_reset,	-- the AC97 core uses nreset!

-- WISHBONE SLAVE INTERFACE 
		wb_data_i		=> wbs_in(WB_AC97).dat_i,
		wb_data_o		=> wbs_out(WB_AC97).dat_o,
		wb_addr_i		=> ac97_wb_adr,
		wb_sel_i		=> "1111",
		wb_we_i			=> wbs_in(WB_AC97).we_i,
		wb_cyc_i		=> wbs_in(WB_AC97).cyc_i,
		wb_stb_i		=> wbs_in(WB_AC97).stb_i,
		wb_ack_o		=> wbs_out(WB_AC97).ack_o,
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

	l(1) <= ac97_sdo;
	ac97_bclk <= l(2);	-- this one is inout on AC97/AD1981BL
	l(2) <= 'Z';
	ac97_sdi <= l(3);
	l(3) <= 'Z';
	l(4) <= ac97_syn;
	l(5) <= ac97_nres;

	
	
	-- SPI Wishbone component
	spi_wb_adr <= "0000000000000000000000000000"
					& wbs_in2(WB_SPI).adr_i  ;

	wbspi: simple_spi_top  port map(

	clk_i			=> clk,
	rst_i			=> not_reset,

-- WISHBONE SLAVE INTERFACE 
	adr_i		=> spi_wb_adr(1 downto 0),         --// lower address bits
	dat_i		=> wbs_in2(WB_SPI).dat_i,        --// databus input
	dat_o		=> wbs_out2(WB_SPI).dat_o,        --// databus output
--	sel_i		=> "1111",         --// byte select inputs
	we_i		=> wbs_in2(WB_SPI).we_i,          --// write enable input
	stb_i		=> wbs_in2(WB_SPI).stb_i,        --// stobe/core select signal
	cyc_i		=> wbs_in2(WB_SPI).cyc_i,         --// valid bus cycle input
	ack_o		=> wbs_out2(WB_SPI).ack_o,         --// bus cycle acknowledge output
--	err_o		=> open,         --// termination w/ error
	inta_o		=> open,         --// interrupt request signal output



 --// SPI Interface                                     
	--ss_pad_o  		=>   spi_ss,      --// slave select
	sck_o		=>   spi_sclk,    --// serial clock
	mosi_o		=>   spi_mosi,    --// master out slave in
	miso_i		=>   spi_miso,    --// master in slave out
	ss_o		=>	 spi_ss
	);

	r(16) <= spi_mosi;
	r(17) <= spi_sclk;	-- this one is inout on AC97/AD1981BL
	--l(2) <= 'Z';
	spi_miso <= r(18);
	r(18) <= 'Z';
	r(15) <= spi_ss;


end rtl;
