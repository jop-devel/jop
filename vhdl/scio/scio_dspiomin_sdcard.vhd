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
--	scio_dspiomin.vhd
--
--	minimal io devices for dspio board
--
--
--	io address mapping:
--
--	IO Base is 0xffffff80 for 'fast' constants (bipush)
--
--		0x00 0-3		system clock counter, us counter, timer int, wd bit
--		0x10 0-1		uart (download)
--		0x20 0-1		USB connection (download)
--		0x40 0-24		spimaster (SD Card)
--
--	status word in uarts:
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
--	2007-03-17	use records
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.wb_pack.all;
use work.jop_config.all;

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
	
-- remove the comment for RAM access counting
-- ram_cnt 	: in std_logic
);
end scio;


architecture rtl of scio is

component spiMaster is
port (

	clk_i			: in std_logic;
	rst_i			: in std_logic;

-- WISHBONE SLAVE INTERFACE 
	address_i	: in std_logic_vector(5 downto 0);     -- lower adr. bits
	data_i	: in std_logic_vector(31 downto 0);    -- databus input
	data_o	: out std_logic_vector(31 downto 0);   -- databus output
	strobe_i	: in std_logic;     	-- stobe/core select signal
	we_i		: in std_logic;     	-- write enable input
	ack_o		: out std_logic;    	-- bus cycle acknowledge output


-- SPI logic clock
	spiSysClk		: in std_logic; 

 -- SPI Interface                                     
	spiClkOut		: out std_logic;    	-- serial clock
	spiDataIn		: in std_logic;       	-- master in slave out
	spiDataOut		: out std_logic;     	-- master out slave in
	spiCS_n  		: out std_logic     	-- slave select
);
end component;


	constant SLAVE_CNT : integer := 5;
	-- SLAVE_CNT <= 2**DECODE_BITS
	-- take care of USB address 0x20!
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
	
	-- remove the comment for RAM access counting 
	-- signal ram_count : std_logic;
--
-- Wishbone interface for the SPI
--
signal wb_out			: wb_master_out_type;
signal wb_in			: wb_master_in_type;

constant WB_SLAVE_CNT : integer := 1;

type wbs_in_array is array(0 to WB_SLAVE_CNT-1) of wb_slave_in_type;
signal wbs_in		: wbs_in_array;
type wbs_out_array is array(0 to WB_SLAVE_CNT-1) of wb_slave_out_type;
signal wbs_out		: wbs_out_array;

--
--	SPI Signals
--
	constant WB_SPI : integer := 0;

	signal spi_sclk		: std_logic;    	-- serial clock
	signal spi_mosi		: std_logic;    	-- master out slave in
	signal spi_miso		: std_logic; 	-- master in slave out
	signal spi_ss		: std_logic;   	-- slave select
	signal spi_wb_adr		: std_logic_vector(31 downto 0);



begin

--
--	unused and input pins tri state
--
	l <= (others => 'Z');
	--r(20 downto 14) <= (others => 'Z');
	t <= (others => 'Z');
	b <= (others => 'Z');

	assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer( unsigned( sc_io_out.address( SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS ) ) ) when sc_io_out.address( SLAVE_ADDR_BITS+DECODE_BITS-1 ) = '0' else   4;
	
	-- What happens when sel_reg > SLAVE_CNT-1??
	sc_io_in.rd_data <= sc_dout(sel_reg);
	sc_io_in.rdy_cnt <= sc_rdy_cnt(sel_reg);

	-- unused slave address
	sc_rdy_cnt(3) <= (others => '0');
	sc_dout(3) <= (others => '0');
	--
	-- Connect SLAVE_CNT simple slaves
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
			-- remove the comment for RAM access counting
			-- ram_count => ram_count
		);
		
	-- remove the comment for RAM access counting
	-- ram_count <= ram_cnt;

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
	
	--
-- SimpCon Wishbone bridge for SPI Master
--
wbspi: entity work.sc2wb generic map ( addr_bits => SLAVE_ADDR_BITS+2 )
	port map(
		clk => clk,
		reset => reset,

		address => sc_io_out.address(SLAVE_ADDR_BITS+1 downto 0),
		wr_data => sc_io_out.wr_data,
		rd => sc_rd(4),
		wr => sc_wr(4),
		rd_data => sc_dout(4),
		rdy_cnt => sc_rdy_cnt(4),

		wb_out => wb_out,
		wb_in => wb_in
	);



gwsl2: for i in 0 to WB_SLAVE_CNT-1 generate
	wbs_in(i).dat_i <= wb_out.dat_o;
	wbs_in(i).we_i  <= wb_out.we_o;
	wbs_in(i).adr_i <= wb_out.adr_o(S_ADDR_SIZE-1 downto 0);
	wbs_in(i).cyc_i <= wb_out.cyc_o;
end generate;



wbs_in(WB_SPI).stb_i <= wb_out.stb_o;
wb_in.dat_i <= wbs_out(WB_SPI).dat_o;
wb_in.ack_i <= wbs_out(WB_SPI).ack_o;


spi_wb_adr <= "00000000000000000000000000" & wbs_in(WB_SPI).adr_i;

wbspi: spiMaster  port map(

	clk_i			=> clk,
rst_i			=> reset,

-- WISHBONE SLAVE INTERFACE 
	address_i	=> spi_wb_adr(5 downto 0), 	-- lower address bits
	data_i	=> wbs_in(WB_SPI).dat_i,    	-- databus input
	data_o	=> wbs_out(WB_SPI).dat_o, 	-- databus output
	we_i		=> wbs_in(WB_SPI).we_i,     	-- write enable input
	strobe_i	=> wbs_in(WB_SPI).stb_i,    	-- stobe/core select signal
	ack_o		=> wbs_out(WB_SPI).ack_o,  	-- bus cycle ack output
	spiSysClk	=> clk,
	
 -- SPI Interface                                     
	spiClkOut		=>   spi_sclk,    	-- serial clock
	spiDataOut		=>   spi_mosi,    	-- master out slave in
	spiDataIn		=>   spi_miso,    	-- master in slave out
	spiCS_n		=>   spi_ss			-- slave select
	);

	r(16) <= spi_mosi;
	r(17) <= spi_sclk;	-- this one is in on SD-Card
	spi_miso <= r(18);
	r(18) <= 'Z';
	r(15) <= spi_ss;

	
	
end rtl;
