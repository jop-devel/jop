--
--	iolego.vhd
--
--	io devices for LEGO mindstorms
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
--		8		ADC3 input (battery watch)
--		10		in pins, led outs
--		11		out pins
--		12		ADC1 input
--		13		ADC2 input
--	LEGO
--		12		analog sensor input
--		13		motor io
--
--	status word in uarts:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--
--
--	2004-08-09	copy from iotal
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
	lo			: out std_logic_vector(14 downto 1);
	bat			: out std_logic;
	o			: out std_logic_vector(4 downto 1)
);
end component tal;


component lesens is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	sp		: out std_logic;
	sdi		: in std_logic;
	sdo		: out std_logic
);
end component lesens;

component lemotor is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	en1		: out std_logic;
	in1a	: out std_logic;
	in1b	: out std_logic;
	en2		: out std_logic;
	in2a	: out std_logic;
	in2b	: out std_logic;

	sdia	: in std_logic;
	sdib	: in std_logic;
	sdoa	: out std_logic;
	sdob	: out std_logic
);
end component lemotor;


	signal addr		: std_logic_vector(3 downto 0);		-- io address

	signal lo		: std_logic_vector(14 downto 1);
	signal bat		: std_logic;
	signal isa_a	: std_logic_vector(4 downto 0);

begin

--
--	unused and input pins tri state
--
	t(5 downto 4) <= (others => 'Z');
	l(17 downto 16) <= (others => 'Z');
	l(11 downto 9) <= (others => 'Z');
	l(7 downto 1) <= (others => 'Z');
	r(20 downto 13) <= (others => 'Z');
	r(11 downto 1) <= (others => 'Z');
	b <= (others => 'Z');

	cmp_cnt : cnt generic map (0, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				irq, irq_ena,
				wd
		);

	-- Siemens TC35 sends up to 32!!! characters after cts deasert
	-- cmp_ua : uart generic map (4, clk_freq, 115200, 16, 2, 50, 16)

	-- smaller fifos for ACEX version for elevator
	cmp_ua : uart generic map (4, clk_freq, 115200, 2, 1, 2, 1)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				txd, rxd, ncts, nrts
		);

	cmp_sd1 : lesens generic map (12, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				l(12),					-- sensor power
				t(3),					-- sdi
				t(6)					-- sdo
		);

	cmp_lm1 : lemotor generic map (13, clk_freq)
			port map (clk, reset, addr,
				din, wr, dout, rd,
				l(14),					-- en1
				l(15),					-- in1a
				l(20),					-- in1b
				l(19),					-- en2
				l(13),					-- in2a
				l(18),					-- in2b
				t(1),					-- sdia
				t(2),					-- sdib
				r(12),					-- sdoa
				l(8)					-- sdob

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
	lo			: out std_logic_vector(14 downto 1);
	bat			: out std_logic;
	o			: out std_logic_vector(4 downto 1)
);
end tal;

architecture rtl of tal is

	signal inreg			: std_logic_vector(10 downto 1);
	signal led				: std_logic_vector(14 downto 1);

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
		bat <= '1';
		o <= (others => '0');

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4))
			and wr='1' then
			led <= din(13 downto 0);
			bat <= not din(31);
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
	lo(11) <= '0' when led(11)='1' else 'Z';
	lo(12) <= '0' when led(12)='1' else 'Z';
	lo(13) <= '0' when led(13)='1' else 'Z';
	lo(14) <= '0' when led(14)='1' else 'Z';

end rtl;
