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
--	scio_mikjen.vhd
--
--	io devices for baseio print (TAL)
--	special version for Mikael and Jens
--
--
--	io address mapping:
--
--	IO Base is 0xffffff80 for 'fast' constants (bipush)
--
--		0x00 0-3		system clock counter, us counter, timer int, wd bit
--		0x10 0-1		uart (download)
--
--		0x20			reserved for USB port -- System.out.print() writes to it!
--	TAL
--		0x40 0			in pins, led outs
--		0x40 1			out pins
--		0x40 1			ADC1 input
--		0x40 2			ADC2 input
--		0x40 3			ADC3 input (battery watch)
--		0x50 0			isa control and addr write
--		0x50 1			isa data
--		0x60			sigdel for microphone
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
--	2005-12-27	change for SimpCon, HW hand shake is programmable
--				for uart_tal
--	2006-04-16	adapted for Jens & Mikael
--

--
--
--	IO devices specific for TAL
--
--	address:
--		0		input pins and led output
--		1		output pins
--		1-3		ADC
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sc_tal is
generic (addr_bits : integer;
	clk_freq : integer);

port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

-- io ports
	i			: in std_logic_vector(10 downto 1);
	lo			: out std_logic_vector(14 downto 1);
	bat			: out std_logic;
	o			: out std_logic_vector(4 downto 1);

	sdi			: in std_logic_vector(2 downto 0);
	sdo			: out std_logic_vector(2 downto 0)
);
end sc_tal;

architecture rtl of sc_tal is

	signal inreg			: std_logic_vector(10 downto 1);
	signal led				: std_logic_vector(14 downto 1);

	type sd_dout_type is array(0 to 2) of std_logic_vector(15 downto 0);
	signal sd_dout			: sd_dout_type;

begin

	rdy_cnt <= "00";	-- no wait states

	gsd: for i in 0 to 2 generate

		sd: entity work.sigdel
			generic map (
				clk_freq => clk_freq
			)
			port map (
				clk => clk,
				reset => reset,
				dout => sd_dout(i),
				sdi => sdi(i),
				sdo => sdo(i)
		);
	end generate;

--
--	register inputs
--
process(clk, i)

begin
	if rising_edge(clk) then
		inreg <= not i;			-- input is low active
	end if;
end process;

process(clk, reset)
begin

	if (reset='1') then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			case address(1 downto 0) is
				when "00" =>
					rd_data <= std_logic_vector(to_unsigned(0, 22)) & inreg;
				when "01" =>
					rd_data <= std_logic_vector(to_unsigned(0, 16)) & sd_dout(0);
				when "10" =>
					rd_data <= std_logic_vector(to_unsigned(0, 16)) & sd_dout(1);
				when others =>
					rd_data <= std_logic_vector(to_unsigned(0, 16)) & sd_dout(2);
			end case;
		end if;
	end if;

end process;


process(clk, reset)

begin
	if (reset='1') then

		led <= (others => '0');
		bat <= '1';
		o <= (others => '0');

	elsif rising_edge(clk) then

		if wr='1' then
			if address(0)='0' then
				led <= wr_data(13 downto 0);
				bat <= not wr_data(31);
			else
				o <= wr_data(3 downto 0);
			end if;
		end if;

	end if;
end process;

--
--	low activ OC for LEDs
--
	lo(1) <= '0' when led(1)='1' else 'Z';
	lo(2) <= '0' when led(2)='1' else 'Z';
	lo(3) <= '0' when led(3)='1' else 'Z';
	lo(4) <= '0' when led(4)='1' else 'Z';
	lo(5) <= '0' when led(5)='1' else 'Z';
	lo(6) <= '0' when led(6)='1' else 'Z';
	lo(7) <= '0' when led(7)='1' else 'Z';
	lo(8) <= '0' when led(8)='1' else 'Z';
	lo(9) <= '0' when led(9)='1' else 'Z';
	lo(10) <= '0' when led(10)='1' else 'Z';
	lo(11) <= '0' when led(11)='1' else 'Z';
	lo(12) <= '0' when led(12)='1' else 'Z';
	lo(13) <= '0' when led(13)='1' else 'Z';
	lo(14) <= '0' when led(14)='1' else 'Z';

end rtl;

--
--	basio scio
--

library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.jop_config.all;
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

	constant SLAVE_CNT : integer := 7;
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

	signal lo		: std_logic_vector(14 downto 1);
	signal bat		: std_logic;

	signal sdi		: std_logic_vector(2 downto 0);
	signal sdo		: std_logic_vector(2 downto 0);

	signal isa_a	: std_logic_vector(4 downto 0);

	signal mic_sdi, mic_sdo, audio_out	: std_logic;

begin

--
--	unused and input pins tri state
--
	r(2 downto 1) <= (others => 'Z');
	r(20 downto 6) <= (others => 'Z');

	assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer(unsigned(sc_io_out.address(SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS)));

	-- What happens when sel_reg > SLAVE_CNT-1??
	sc_io_in.rd_data <= sc_dout(sel_reg);
	rdy_cnt <= sc_rdy_cnt(sel_reg);

	-- default for unused USB device
	sc_dout(2) <= (others => '0');
	sc_rdy_cnt(2) <= (others => '0');
	-- default for other unused devices
	sc_dout(3) <= (others => '0');
	sc_rdy_cnt(3) <= (others => '0');

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

	ua: entity work.sc_uart_tal generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 115200,
			txf_depth => 4,
			txf_thres => 2,
			rxf_depth => 16,
			rxf_thres => 8
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
			ncts => ncts,
			nrts => nrts,
			dtr => l(11)
	);

	-- b(1 to 10) <= lo(10 downto 1);	Quartus does not compile!!!

	-- Mikael & Jens:
	--	change to the pins you are using!
	--	be carful to NOT confuse the signals l and lo!

	-- l(12) <= lo(14);
	l(13) <= lo(13);
	-- l(14) <= lo(12);
	l(15) <= lo(11);
	b(1) <= lo(10);
	b(2) <= lo(9);
	b(3) <= lo(8);
	b(4) <= lo(7);
	b(5) <= lo(6);
	b(6) <= lo(5);
	b(7) <= lo(4);
	b(8) <= lo(3);
	-- b(9) <= lo(2);
	b(10) <= lo(1);
	l(16) <= bat;

	-- we use three of the LED pins for the micro sigdel
	-- and the audio out:
	mic_sdi <= l(12);
	l(14) <= mic_sdo;
	b(9) <= audio_out;

	-- slave 2 is reserved for USB and System.out writes to it!!!

	-- slave 3 is reserved for TAL simulation on the PC

	--
	--	TAL stuff (IO ports, sigdel converter)
	--
	tal: entity work.sc_tal generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq
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

			i => r(20 downto 11),		-- input pins
			lo => lo,					-- LEDs
			bat => bat,					-- battery on
			o => l(20 downto 17),		-- output pins
			sdi => sdi,
			sdo => sdo
		);

	-- ADC 1
	sdi(0) <= r(7);
	r(4) <= sdo(0);
	-- ADC 2
	sdi(1) <= r(6);
	r(5) <= sdo(1);
	-- ADC 3 (battery)
	sdi(2) <= r(8);
	r(3) <= sdo(2);


	--
	--	ISA bus to CS8900
	--
	t(2) <= isa_a(4);
	t(3) <= isa_a(3);
	t(4) <= isa_a(2);
	t(5) <= isa_a(1);
	t(6) <= isa_a(0);

	isa: entity work.sc_isa generic map (
			addr_bits => SLAVE_ADDR_BITS
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(5),
			wr => sc_wr(5),
			rd_data => sc_dout(5),
			rdy_cnt => sc_rdy_cnt(5),

			isa_d => l(9 downto 2),			-- data bus
			isa_a => isa_a,					-- address bus
			isa_reset => l(10),				-- reset
			isa_nior => t(1),				-- nior
			isa_niow => l(1)				-- niow
		);

--
--	IO devices for Mikael and Jens
--
--		microphone input and PWM output
--

	audio: entity work.sc_sigdel generic map (
			addr_bits => SLAVE_ADDR_BITS,
			fsamp => 8000
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(6),
			wr => sc_wr(6),
			rd_data => sc_dout(6),
			rdy_cnt => sc_rdy_cnt(6),

			sdi => mic_sdi,
			sdo => mic_sdo,
			dac => audio_out	
	);
end rtl;
