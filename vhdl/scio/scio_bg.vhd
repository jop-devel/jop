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
--	scio_bg.vhd
--
--	io devices for bg263 (OEBB)
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
--	BG263
--		0x30 0-1		modem uart
--		0x40 0-1		GPS uart
--		0x50 0-1		display
--		0x60 0-1		keyboard, IO pins
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
--	2007-03-28	use records
--	2008-05-22	changed modem baudrate to 115 kbit
--	2008-05-28	back to 38.4 kbit
--	2009-03-14	and forward to 115 kbit again
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

entity sc_bgio is
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

	key_in	: in std_logic_vector(3 downto 0);
	key_out	: out std_logic_vector(3 downto 0);
	led		: out std_logic;
	rela	: out std_logic;
	relb	: out std_logic;
	m_dtr	: out std_logic;
	i		: in std_logic
);
end sc_bgio;

architecture rtl of sc_bgio is

	signal key_inreg		: std_logic_vector(3 downto 0);
	signal key_outreg		: std_logic_vector(3 downto 0);
	signal ireg				: std_logic;

begin

	rdy_cnt <= "00";	-- no wait states

--
--	register inputs
--
process(clk, i)

begin
	if rising_edge(clk) then
		key_inreg <= not key_in;			-- invert keyboard (log 1 activ)
		ireg <= i;
	end if;
end process;

process(clk, reset)
begin

	if (reset='1') then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			if address(0)='0' then
				rd_data <= std_logic_vector(to_unsigned(0, 28)) & key_inreg;
			else
				rd_data <= std_logic_vector(to_unsigned(0, 31)) & ireg;
			end if;
		end if;

	end if;

end process;


process(clk, reset)

begin
	if (reset='1') then

		key_outreg <= (others => '0');
		led <= '0';
		rela <= '0';
		relb <= '0';

	elsif rising_edge(clk) then

		if wr='1' then
			if address(0)='0' then
				key_outreg <= wr_data(3 downto 0);
			else
				led <= wr_data(0);
				rela <= wr_data(1);
				relb <= wr_data(2);
				m_dtr <= wr_data(3);
			end if;
		end if;

	end if;
end process;

--
--	keyboard
--
	key_out(0) <= 'Z' when key_outreg(0)='0' else '0';	-- inv. OC output
	key_out(1) <= 'Z' when key_outreg(1)='0' else '0';
	key_out(2) <= 'Z' when key_outreg(2)='0' else '0';
	key_out(3) <= 'Z' when key_outreg(3)='0' else '0';
	
end rtl;

--
--
--	8-N-1 serial interface for Noritake display
--	
--	offset 0:	rd not bsy, wr reset display
--	offset 1:	wr display data
--
--	2003-06-21	first working version
--	2003-07-05	new IO standard
--	2003-09-12	one more stop bit (datasheet talks about parity bit 'non parity'.
--				Use ncts and tdrf for handshake with SW (paranoid).
--				However, the display does NOT deliver a correct busy signal.
--				Delay (min. 3 ms) is neccessary in sw!
--	2003-09-19	sync ncts in!!! Thats the solution to above problem.
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sc_disp is

generic (addr_bits : integer;
	clk_freq : integer;
	baud_rate : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

	resout	: out std_logic;
	txd		: out std_logic;
	ncts	: in std_logic
);
end sc_disp;

architecture rtl of sc_disp is

	signal ua_wr, tdre			: std_logic;

	type disp_tx_state_type		is (s0, s1);
	signal disp_tx_state 		: disp_tx_state_type;

	type disp_tdr_state_type	is (s0, s1);
	signal disp_tdr_state 		: disp_tdr_state_type;

	signal tdr			: std_logic_vector(7 downto 0); -- tx buffer
	signal tsr			: std_logic_vector(9 downto 0); -- tx shift register
	signal tdrf			: std_logic;					-- tdr has valid data
	signal tdr_rd		: std_logic;					-- tdr was read

	signal tx_clk		: std_logic;

	constant clk16_cnt	: integer := (clk_freq/baud_rate+8)/16-1;

	signal ncts_buf		: std_logic_vector(2 downto 0);	-- sync in

begin

	rdy_cnt <= "00";	-- no wait states

process(clk, reset)
begin

	if (reset='1') then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			rd_data <= std_logic_vector(to_unsigned(0, 31)) & tdre;
		end if;
	end if;

end process;

	-- write is on address offest 1
	ua_wr <= wr and address(0);

process(clk, reset)

begin
	if (reset='1') then

		resout <= '0';

	elsif rising_edge(clk) then

		if wr='1' then
			if address(0)='0' then
				resout <= wr_data(0);
			end if;
		end if;

	end if;
end process;

--
--
--	serial clock
--
process(clk, reset)

	variable clk16		: integer range 0 to clk16_cnt;
	variable clktx		: unsigned(3 downto 0);

begin
	if (reset='1') then
		clk16 := 0;
		clktx := "0000";
		tx_clk <= '0';

	elsif rising_edge(clk) then

		if (clk16=clk16_cnt) then		-- 16 x serial clock
			clk16 := 0;
--
--	tx clock
--
			clktx := clktx + 1;
			if (clktx="0000") then
				tx_clk <= '1';
			else
				tx_clk <= '0';
			end if;
		else
			clk16 := clk16 + 1;
			tx_clk <= '0';
		end if;


	end if;

end process;

--
--	state machine for tdr
--
process(clk, reset)

begin

	if (reset='1') then
		disp_tdr_state <= s0;
		tdrf <= '0';
		tdr <= "00000000";
	elsif rising_edge(clk) then

		case disp_tdr_state is

			when s0 =>
				if (ua_wr='1') then
					tdr <= wr_data(7 downto 0);
					tdrf <= '1';
					disp_tdr_state <= s1;
				end if;

			when s1 =>
				if (tdr_rd='1') then
					tdrf <= '0';
					disp_tdr_state <= s0;
				end if;

		end case;
	end if;

end process;


--
--	state machine for actual shift out
--
process(clk, reset)

	variable i : integer range 0 to 15;

begin

	if (reset='1') then
		disp_tx_state <= s0;
		tsr <= "1111111111";
		tdr_rd <= '0';
		ncts_buf <= "111";

	elsif rising_edge(clk) then

		ncts_buf(0) <= ncts;
		ncts_buf(2 downto 1) <= ncts_buf(1 downto 0);

		case disp_tx_state is

			when s0 =>
				i := 0;
				if (tdrf='1' and ncts_buf(2)='0') then
					disp_tx_state <= s1;
					tsr <= tdr & '0' & '1';
					tdr_rd <= '1';
				end if;

			when s1 =>
				tdr_rd <= '0';
				if (tx_clk='1') then
					tsr(9) <= '1';
					tsr(8 downto 0) <= tsr(9 downto 1);
					i := i+1;
					if (i=11) then
						disp_tx_state <= s0;
					end if;
				end if;
				
		end case;
	end if;

end process;

	txd <= not tsr(0);		-- inverted with digital transistor
	tdre <= not tdrf;

end rtl;



--
--	bg263 scio
--

library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
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
);
end scio;


architecture rtl of scio is

-- full decode
	constant SLAVE_CNT : integer := 8;
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

	signal gps_nrts			: std_logic;	-- dummy signal for unused nrts

begin

--
--	unused driving gnd
--	input pins tri state
--
	t <= (others => '0');

	l(13 downto 1) <= (others => '0');
-- two input pins ?
-- tris state serial input pins ???
	l(20 downto 18) <= (others => '0');

	r(16 downto 5) <= (others => '0');

	r(3) <= 'Z';

	b(5 downto 1) <= (others => 'Z');


	assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer(unsigned(sc_io_out.address(SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS)));

	-- What happens when sel_reg > SLAVE_CNT-1??
	sc_io_in.rd_data <= sc_dout(sel_reg);
	sc_io_in.rdy_cnt <= sc_rdy_cnt(sel_reg);
	-- slave 2 is reserved for USB and System.out writes to it!!!
	sc_rdy_cnt(2) <= (others => '0');
	sc_dout(2) <= (others => '0');
	sc_rdy_cnt(7) <= (others => '0');
	sc_dout(7) <= (others => '0');


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

	-- service uart, but PC can send up to 16 character after cts deassert!
	ua: entity work.sc_uart generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 115200,
			txf_depth => 4,
			txf_thres => 1,
			rxf_depth => 4,
			rxf_thres => 2
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

	-- Siemens TC35 sends up to 32!!! characters after cts deasert
	-- WW does not like the 115200 baud :-(
	ua2: entity work.sc_uart generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
--			baud_rate => 38400,
			baud_rate => 115200,
			txf_depth => 16,
			txf_thres => 2,
			rxf_depth => 50,
			rxf_thres => 16
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

			txd	 => l(14),
			rxd	 => l(17),
			ncts => l(16),
			nrts => l(15)
	);

	-- a little bit more fifo on receive buffer would really help (missing GPS data)
	ua3: entity work.sc_uart generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 4800,
			txf_depth => 4,
			txf_thres => 2,
			rxf_depth => 16,
			rxf_thres => 2
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

			txd	 => r(18),
			rxd	 => r(17),
			ncts => '0',
			nrts => gps_nrts	-- not used
	);

	--
	-- BG stuff: keyboard, IO pins
	--
	bgio: entity work.sc_bgio generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq
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

			key_in => b(5 downto 2),		-- keyboard input
			key_out => b(9 downto 6),		-- keyboard output
			led => r(1),					-- led
			rela => r(20),					-- relay a
			relb => b(10),					-- relay b
			m_dtr => r(19),					-- modem DTR
			i => b(1)						-- input pin
		);

	--
	-- BG display
	--
	disp: entity work.sc_disp generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 38400
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

			resout => r(2),		-- display reset
			txd => r(4),		-- txd
			ncts => r(3)		-- ncts (bsy)
		);
end rtl;
