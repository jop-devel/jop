--
--	jopcyc.vhd
--
--	top level for Cyclone board with EP1C12
--		use iocore.vhd for all io-pins
--
--	2002-06-27:	2088 LCs, 23.6 MHz
--	2002-07-27:	2308 LCs, 23.1 MHz	with some changes in jvm and baseio
--	2002-08-02:	2463 LCs
--	2002-08-08:	2431 LCs simpler sigdel
--
--	2002-03-28	creation
--	2002-06-27	isa bus for CS8900
--	2002-07-27	io for baseio
--	2002-08-02	second uart (use first for download and debug)
--	2002-11-01	removed second uart
--	2002-12-01	split memio
--	2002-12-07	disable clkout
--	2003-02-21	adapt for new Cyclone board with EP1C6
--	2003-07-08	invertion of cts, rts to uart
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity jop is

generic (
	clk_freq	: integer := 20000000;	-- 20 MHz clock frequency
-- clk_freq	: integer := 100000000;	-- 100 MHz clock frequency
	width		: integer := 32;	-- one data word
	ioa_width	: integer := 3;		-- address bits of internal io
	ram_cnt		: integer := 3;		-- clock cycles for external ram
	rom_cnt		: integer := 3		-- clock cycles for external rom
--	rom_cnt		: integer := 30		-- clock cycles for external rom
);

port (
	clk		: in std_logic;
--
---- serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;
	ser_ncts		: in std_logic;
	ser_nrts		: out std_logic;

--
--	watchdog
--
	wd		: out std_logic;
	freeio	: out std_logic;

--
--	two ram banks
--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;
	ramb_a		: out std_logic_vector(17 downto 0);
	ramb_d		: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_noe	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic;
	ramb_nwe	: out std_logic;

--
--	config/program flash and big nand flash
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic;

--
--	I/O pins of board
--
	io_b	: inout std_logic_vector(10 downto 1);
	io_l	: inout std_logic_vector(20 downto 1);
	io_r	: inout std_logic_vector(20 downto 1);
	io_t	: inout std_logic_vector(6 downto 1)
);
end jop;

architecture rtl of jop is

--
--	components:
--

component pll is
port (
	inclk0		: in std_logic;
	c0			: out std_logic
);
end component;

component core is
port (
	clk, reset	: in std_logic;

-- memio connection

	bsy			: in std_logic;
	din			: in std_logic_vector(width-1 downto 0);
	addr		: out std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: out std_logic;

-- interrupt from io

	irq			: in std_logic;
	irq_ena		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0)
);
end component;

component mem32 is
generic (width : integer; ioa_width : integer; ram_cnt : integer; rom_cnt : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	bsy			: out std_logic;
	dout		: out std_logic_vector(width-1 downto 0);

--
--	two ram banks
--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;
	ramb_a		: out std_logic_vector(17 downto 0);
	ramb_d		: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_noe	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic;
	ramb_nwe	: out std_logic;

--
--	config/program flash and big nand flash
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic;

-- io interface

	io_din		: in std_logic_vector(width-1 downto 0);
	io_rd		: out std_logic;
	io_wr		: out std_logic;
	io_addr_wr	: out std_logic

);
end component;

component io is
generic (clk_freq : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);

-- interface to mem

	rd, wr		: in std_logic;
	addr_wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

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

--	I/O pins of board

	b		: inout std_logic_vector(10 downto 1);
	l		: inout std_logic_vector(20 downto 1);
	r		: inout std_logic_vector(20 downto 1);
	t		: inout std_logic_vector(6 downto 1)
);
end component;

--
--	Signals
--
	signal clk_int			: std_logic;

	signal memio_din		: std_logic_vector(width-1 downto 0);

	signal mem_addr			: std_logic_vector(ioa_width-1 downto 0);
	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_bsy			: std_logic;
	signal mem_dout			: std_logic_vector(width-1 downto 0);

	signal io_rd			: std_logic;
	signal io_wr			: std_logic;
	signal io_addr_wr		: std_logic;
	signal io_dout			: std_logic_vector(width-1 downto 0);
	signal io_irq			: std_logic;
	signal io_irq_ena		: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0);

begin

--
--	intern reset
--	no extern reset, epm7064 has too less pins
--

process(clk_int)
begin
	if rising_edge(clk_int) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;

		int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
	end if;
end process;

--
--	components of jop
--
--	pll_inst : pll port map (
--		inclk0	 => clk,
--		c0	 => clk_int
--	);
	clk_int <= clk;

	cmp_core: core 
		port map (clk_int, int_res,
			mem_bsy,
			mem_dout, mem_addr,
			mem_rd, mem_wr,
			io_irq, io_irq_ena,
			memio_din
		);


	cmp_mem: mem32 generic map (width, ioa_width, ram_cnt, rom_cnt)
		port map (clk_int, int_res, memio_din, mem_addr, mem_rd, mem_wr, mem_bsy, mem_dout,
			rama_a, rama_d, rama_ncs, rama_noe, rama_nlb, rama_nub, rama_nwe,
			ramb_a, ramb_d, ramb_ncs, ramb_noe, ramb_nlb, ramb_nub, ramb_nwe,
			fl_a, fl_d, fl_ncs, fl_ncsb, fl_noe, fl_nwe, fl_rdy,
			io_dout, io_rd, io_wr, io_addr_wr
		);

	cmp_io: io generic map (clk_freq)
		port map (clk_int, int_res, memio_din,
			io_rd, io_wr, io_addr_wr, io_dout,
			io_irq, io_irq_ena,
			ser_txd, ser_rxd, ser_ncts, ser_nrts,
			wd,
			io_b, io_l, io_r, io_t
		);

--
--	EP1C12 additional power pins as tristatet output on EP1C6
--
	freeio <= 'Z';

end rtl;
