--
--	iotest.vhd
--
--	io devices test for new interface
--
--
--	io address mapping:
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
--
--	status word:
--		0	uart transmit data register empty
--		1	uart read data register full
--		2	second uart transmit data register empty !not used
--		3	second uart read data register full !not used
--		4	rs485 transmit data register empty	BB
--		5	rs485 read data register full		BB
--
--
--	todo:
--		ff against meta stability on all inputs
--
--
--	2002-12-01	extracted from memioes.vhd
--	2002-12-07	add led output, incr. in, invert in
--	2002-02		use TAL for Testfahrt with OEBB
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity io is
generic (
	clk_freq	: integer := 20000000;	-- 20 MHz clock frequency
	width		: integer := 32	-- one data word
);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);

-- interface to mem

	rd, wr		: in std_logic;
	addr_wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

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
	cts		: in std_logic;
	rts		: out std_logic
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

	txd		: out std_logic;
	tdre	: out std_logic;		-- should be in this file handled!!!
	cts		: in std_logic
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

	o		: out std_logic_vector(3 downto 0);
	i		: in std_logic_vector(1 downto 0)
);
end component bgio ;

	signal addr	: std_logic_vector(3 downto 0);		-- io address

--
-- signal for in/out port	should be in bgio
--
	signal o				: std_logic_vector(3 downto 0);
	signal i				: std_logic_vector(1 downto 0);

--
--	signals for display connection
--
	signal disp_tdre	: std_logic;
	signal disp_cts				: std_logic;
	signal disp_txd				: std_logic;

begin

--
--	unused and input pins tri state
--
	l <= (others => 'Z');
	r(19 downto 5) <= (others => 'Z');
	r(3) <= 'Z';
	t <= (others => 'Z');
	b(9 downto 1) <= (others => 'Z');

-- should be in bgio!!!
	r(1) <= o(0);		-- led
	r(2) <= o(1);		-- display reset
	r(20) <= o(2);		-- relay 1
	b(10) <= o(3);		-- relay 2

--
--	keyboard and input pins
--

process(clk, r(3), b(1))

begin
	if rising_edge(clk) then
		disp_cts <= not r(3);					-- display bsy
		i(1) <= b(1);							-- input pin
	end if;
end process;

-- should be in disp with new IO!!!
	i(0) <= disp_tdre;							-- display tx buffer empty

--	dout <= (others => 'Z');	is eigentlich redundant

	cmp_cnt : cnt generic map (10, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				wd
		);

	cmp_bgio : bgio generic map (0, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				o, i
		);

--
--	three uarts
--
	cmp_ua : uart generic map (1, clk_freq, 115200, 2, 1, 2, 1)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				txd, rxd, cts, rts
		);

--
--	display
--
-- this line is RIGHT, but Quartus II can't compile it!!!!!!!!!!!!!!!!!!!!
	r(4) <= not disp_txd;							-- inverted with transistor


	cmp_disp : disp generic map (14, clk_freq, 38400)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				disp_txd, disp_tdre, disp_cts
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
