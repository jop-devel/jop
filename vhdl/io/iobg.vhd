--
--	iobg.vhd
--
--	io devices for bg263 with new interface
--
--
--	'old' io address mapping:
--
--		0	in, out port (8/4 Bit)	not used in bb
--		1	uart status
--		2	uart data (rd/wr)
--		3	reserved for ecp
--		4	ADC rd, exp/lo write	BB
--		5	isa control and addr, I ADC on BB
--		6	isa data
--
--		7	watch dog bit
--
--		8	gps uart status			OEBB
--		9	gps uart (rd/wr)
--
--		8	leds, taster			BB
--		9	relais, sensor			BB
--		10	system clock counter
--		11	ms clock counter		obsolete
--		12	display out				BB
--		13	keybord (i/o)			BB
--		14	triac out, sense u/i in	BB
--		15	rs485 data (rd/wr)		BB
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
--	io address mapping:
--		0-3		system clock counter, us counter, timer int, wd bit
--		4-5		service uart (download)
--		6-7		modem uart
--		8-9		gps uart
--		10-11	display uart + display reset
--		12-13	keyboard io, bg io
--
--	status word in uarts:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--		ff against meta stability on all inputs
--
--
--	2002-12-01	extracted from memioes.vhd
--	2002-12-07	add led output, incr. in, invert in
--	2003-02		use TAL for Testfahrt with OEBB
--	2003-07-08	new IO interface (with tri-state bus)
--	2003-10-06	modem DTR
--	2004-02-07	ignore ncts on service uart (for simpler debugging)
--	2004-10-05	changed modem baud rate to 38400
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity io is
generic (
	clk_freq	: integer
);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(31 downto 0);

-- interface to mem

	rd, wr		: in std_logic;
	addr_wr		: in std_logic;

	dout		: out std_logic_vector(31 downto 0);

-- interrupt

	irq			: out std_logic;
	irq_ena		: out std_logic;

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
end io;

architecture rtl of io is

component cnt is
generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	irq		: out std_logic;
	irq_ena	: out std_logic;

	wd		: out std_logic
);
end component cnt ;


component uart is
generic (io_addr : integer; clk_freq : integer;
	baud_rate : integer;
	txf_depth : integer; txf_thres : integer;
	rxf_depth : integer; rxf_thres : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	txd		: out std_logic;
	rxd		: in std_logic;
	ncts	: in std_logic;
	nrts	: out std_logic
);
end component uart;

component disp is
generic (io_addr : integer; clk_freq : integer;
	baud_rate : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	resout	: out std_logic;
	txd		: out std_logic;
	ncts	: in std_logic
);
end component disp ;

component bgio is
generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	key_in	: in std_logic_vector(3 downto 0);
	key_out	: out std_logic_vector(3 downto 0);
	led		: out std_logic;
	rela	: out std_logic;
	relb	: out std_logic;
	m_dtr	: out std_logic;
	i		: in std_logic
);
end component bgio ;

	signal addr		: std_logic_vector(3 downto 0);		-- io address

	signal gps_nrts	: std_logic;				-- dummy signal for unused nrts

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


	cmp_cnt : cnt generic map (0, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				irq, irq_ena,
				wd
		);

	cmp_bgio : bgio generic map (12, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				b(5 downto 2), b(9 downto 6),	-- keyboard input and output
				r(1),							-- led
				r(20),							-- relay a
				b(10),							-- relay b
				r(19),							-- modem DTR
				b(1)							-- input pin
		);

--
--	three uarts
--
	-- service uart, but PC can send up to 16 character after cts deassert!
	cmp_ua : uart generic map (4, clk_freq, 115200, 4, 1, 4, 2)
			port map (clk, reset, addr,
				din, wr, dout, rd,
--				txd, rxd, ncts, nrts
				txd, rxd, '0', nrts
		);

	-- Siemens TC35 sends up to 32!!! characters after cts deasert
	cmp_ua2 : uart generic map (6, clk_freq, 38400, 16, 2, 50, 16)
	-- WW does not like the 115200 baud :-(
	-- cmp_ua2 : uart generic map (6, clk_freq, 115200, 16, 2, 50, 16)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				l(14), 		-- txd
				l(17),		-- rxd
				l(16),		-- ncts
				l(15)		-- nrts
		);

	-- a little bit more fifo on receive buffer would really help (missing GPS data)
	cmp_ua3 : uart generic map (8, clk_freq, 4800, 4, 2, 16, 2)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				r(18), 		-- txd
				r(17),		-- rxd
				'0',		-- ncts
				gps_nrts	-- nrts (not used)
		);

--
--	display
--

	cmp_disp : disp generic map (10, clk_freq, 38400)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				r(2),		-- display reset
				r(4),		-- txd
				r(3)		-- ncts (bsy)
		);


--
--	store io address
--
process(clk, reset, din, addr_wr)

begin
	if (reset='1') then
		addr <= (others => '0');
	elsif rising_edge(clk) then
		if (addr_wr='1') then
			addr <= din(3 downto 0);
		end if;
	end if;
end process;

end rtl;
