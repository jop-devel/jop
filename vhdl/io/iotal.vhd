--
--	iotal.vhd
--
--	io devices for TAL configuration
--
--
--	io address mapping:
--		0-3		system clock counter, us counter, timer int, wd bit
--		4-5		service uart (download)
--	BG263
--		6-7		modem uart
--		8-9		gps uart
--		10-11	display uart + display reset
--		12-13	keyboard io, bg io
--	TAL
--		10		in pins, led outs
--		11		out pins
--		12		ADC1 input
--		13		ADC2 input
--		14		isa control and addr write
--		15		isa data
--
--	status word in uarts:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--
--
--	2003-09-23	created for 'new' io standard
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

component tal is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

-- io ports
	i			: in std_logic_vector(10 downto 1);
	lo			: out std_logic_vector(10 downto 1);
	o			: out std_logic_vector(4 downto 1)
);
end component tal;


component isa is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	isa_d		: inout std_logic_vector(7 downto 0);
	isa_a		: out std_logic_vector(4 downto 0);
	isa_reset	: out std_logic;
	isa_nior	: out std_logic;
	isa_niow	: out std_logic

);
end component isa;

component sigdel is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	sdi		: in std_logic;
	sdo		: out std_logic
);
end component sigdel ;


	signal addr		: std_logic_vector(3 downto 0);		-- io address

	signal lo		: std_logic_vector(10 downto 1);
	signal isa_a	: std_logic_vector(4 downto 0);

begin

--
--	unused and input pins tri state
--
	l(16 downto 11) <= (others => 'Z');
	r(3 downto 1) <= (others => 'Z');
	r(20 downto 6) <= (others => 'Z');

--
--	for baseio 2002/08
--
--		remove this when the board 'disapears'
--
--	l(11) <= clk;

	cmp_cnt : cnt generic map (0, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				irq, irq_ena,
				wd
		);

	cmp_ua : uart generic map (4, clk_freq, 115200, 2, 1, 2, 1)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				txd, rxd, ncts, nrts
		);

	-- b(1 to 10) <= lo(10 downto 1);	Quartus does not compile!!!
	b(1) <= lo(10);
	b(2) <= lo(9);
	b(3) <= lo(8);
	b(4) <= lo(7);
	b(5) <= lo(6);
	b(6) <= lo(5);
	b(7) <= lo(4);
	b(8) <= lo(3);
	b(9) <= lo(2);
	b(10) <= lo(1);
	cmp_tal : tal generic map (10, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				r(20 downto 11),				-- in pins
				lo,								-- leds
				l(20 downto 17)					-- out pins
		);

	cmp_sd1 : sigdel generic map (12, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				r(7),					-- sdi
				r(4)					-- sdo
		);

	cmp_sd2 : sigdel generic map (13, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				r(6),					-- sdi
				r(5)					-- sdo
		);

	t(2) <= isa_a(4);
	t(3) <= isa_a(3);
	t(4) <= isa_a(2);
	t(5) <= isa_a(1);
	t(6) <= isa_a(0);
	cmp_isa : isa generic map (14, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				l(9 downto 2),			-- data bus
				isa_a,					-- address bus
				l(10),					-- reset
				t(1),					-- nior
				l(1)					-- niow
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

--
--	IO devices specific for TAL
--
--	address:
--		0		input pins and led output
--		1		output pins
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tal is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

-- io ports
	i			: in std_logic_vector(10 downto 1);
	lo			: out std_logic_vector(10 downto 1);
	o			: out std_logic_vector(4 downto 1)
);
end tal;

architecture rtl of tal is

	signal inreg			: std_logic_vector(10 downto 1);
	signal led				: std_logic_vector(10 downto 1);

begin

--
--	register inputs
--
process(clk, i)

begin
	if rising_edge(clk) then
		inreg <= not i;			-- input is low active
	end if;
end process;

process(addr, rd, inreg)

begin
	if addr=std_logic_vector(to_unsigned(io_addr, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 22)) & inreg;
	else
		dout <= (others => 'Z');
	end if;

end process;


process(clk, reset, wr, addr)

begin
	if (reset='1') then

		led <= (others => '0');
		o <= (others => '0');

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4))
			and wr='1' then
			led <= din(9 downto 0);
		elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4))
			and wr='1' then
			o <= din(3 downto 0);
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

end rtl;
