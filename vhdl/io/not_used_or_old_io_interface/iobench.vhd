--
--	iobench.vhd
--
--	test bench for iotest
--
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity iobench is
generic (width : integer := 32);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	rd, wr, addr_wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

--	WD
	wd			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts		: in std_logic;
	rts		: out std_logic;

-- unused io
	l			: inout std_logic_vector(20 downto 1);
	r			: inout std_logic_vector(20 downto 1);
	t			: inout std_logic_vector(6 downto 1);
	b			: inout std_logic_vector(10 downto 1)
);
end iobench;

architecture rtl of iobench is

component io is

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
end component io;


	signal io_dout, io_din		: std_logic_vector(width-1 downto 0);
	signal io_rd		: std_logic;

begin

	cmpio : io
			port map (clk, reset,
				din, io_rd, wr, addr_wr,
				io_dout,
				txd, rxd, cts, rts,
				wd,
				l, r, t, b
		);


--
--
--	read
--
process(clk, reset, rd, io_dout)
begin
	if (reset='1') then
		dout <= std_logic_vector(to_unsigned(0, width));
		io_rd <= '0';
	elsif rising_edge(clk) then
		dout <= std_logic_vector(to_unsigned(0, width));
		io_rd <= '0';

		if (rd='1') then
			dout <= io_dout;
			io_rd <= '1';
		end if;
	end if;
end process;


end rtl;
