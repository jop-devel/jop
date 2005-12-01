--
--	jopsc.vhd
--
--	top level for cycore borad
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
--	2004-09-11	new extension module
--	2004-10-08	mul operands from a and b, single instruction
--	2005-05-12	added the bsy routing through extension
--	2005-08-15	sp_ov can be used to show a stoack overflow on the wd pin
--	2005-11-30	SimpCon for IO devices
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;


entity jop is

generic (
	exta_width	: integer := 3;		-- length of exta part in JOP microcode
	io_addr_bits	: integer := 7;	-- address bits of internal io
	ram_cnt		: integer := 2;		-- clock cycles for external ram
--	rom_cnt		: integer := 3;		-- clock cycles for external rom OK for 20 MHz
	rom_cnt		: integer := 15;	-- clock cycles for external rom for 100 MHz
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4		-- 2*block_bits is number of cache blocks
);

port (
	clk		: in std_logic;
--
--	serial interface
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
generic (multiply_by : natural; divide_by : natural);
port (
	inclk0		: in std_logic;
	c0			: out std_logic
);
end component;

component core is
generic(jpc_width	: integer);			-- address bits of java bytecode pc
port (
	clk, reset	: in std_logic;

-- memio connection

	bsy			: in std_logic;
	din			: in std_logic_vector(31 downto 0);
	ext_addr	: out std_logic_vector(exta_width-1 downto 0);
	rd, wr		: out std_logic;

-- jbc connections

	jbc_addr	: out std_logic_vector(jpc_width-1 downto 0);
	jbc_data	: in std_logic_vector(7 downto 0);

-- interrupt from io

	irq			: in std_logic;
	irq_ena		: in std_logic;

	sp_ov		: out std_logic;

	aout		: out std_logic_vector(31 downto 0);
	bout		: out std_logic_vector(31 downto 0)
);
end component;


--
--	Signals
--
	signal clk_int			: std_logic;

	signal stack_tos		: std_logic_vector(31 downto 0);
	signal stack_nos		: std_logic_vector(31 downto 0);
	signal rd, wr			: std_logic;
	signal ext_addr			: std_logic_vector(exta_width-1 downto 0);
	signal stack_din		: std_logic_vector(31 downto 0);

-- extension/mem interface

	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_addr_wr		: std_logic;
	signal mem_bc_rd		: std_logic;
	signal mem_dout			: std_logic_vector(31 downto 0);
	signal mem_bcstart		: std_logic_vector(31 downto 0);
	signal mem_bsy			: std_logic;
	signal bsy				: std_logic;

	signal jbc_addr			: std_logic_vector(jpc_width-1 downto 0);
	signal jbc_data			: std_logic_vector(7 downto 0);

-- mem/sc interface

	signal sc_address		: std_logic_vector(17 downto 0);
	signal sc_wr_data		: std_logic_vector(31 downto 0);
	signal sc_rd, sc_wr		: std_logic;
	signal sc_rd_data		: std_logic_vector(31 downto 0);
	signal sc_rdy_cnt		: unsigned(1 downto 0);

-- memory interface

	signal ram_addr			: std_logic_vector(17 downto 0);
	signal ram_dout			: std_logic_vector(31 downto 0);
	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;

-- SimpCon io interface

	signal scio_address		: std_logic_vector(io_addr_bits-1 downto 0);
	signal scio_wr_data		: std_logic_vector(31 downto 0);
	signal scio_rd			: std_logic;
	signal scio_wr			: std_logic;
	signal scio_rd_data		: std_logic_vector(31 downto 0);
	signal scio_rdy_cnt		: unsigned(1 downto 0);

-- interrupt io interface

	signal io_irq			: std_logic;
	signal io_irq_ena		: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	signal wd_out, sp_ov	: std_logic;

	-- for generation of internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

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
	pll_inst : pll generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);
-- clk_int <= clk;

	-- sp_ov indicates stack overflow
	-- We can use the wd LED
	-- wd <= sp_ov;
	wd <= wd_out;

	cmp_core: core generic map(jpc_width)
		port map (clk_int, int_res,
			bsy,
			stack_din, ext_addr,
			rd, wr,
			jbc_addr, jbc_data,
			io_irq, io_irq_ena,
			sp_ov,
			stack_tos, stack_nos
		);

	cmp_ext: entity work.extension 
		generic map (
			exta_width => exta_width,
			io_addr_bits => io_addr_bits
		)
		port map (
			clk => clk_int,
			reset => int_res,
			ain => stack_tos,
			bin => stack_nos,

			ext_addr => ext_addr,
			rd => rd,
			wr => wr,
			bsy => bsy,
			dout => stack_din,

			mem_rd => mem_rd,
			mem_wr => mem_wr,
			mem_addr_wr => mem_addr_wr,
			mem_bc_rd => mem_bc_rd,
			mem_data => mem_dout,
			mem_bcstart => mem_bcstart,
			mem_bsy => mem_bsy,
	
			scio_address => scio_address,
			scio_wr_data => scio_wr_data,
			scio_rd => scio_rd,
			scio_wr => scio_wr,
			scio_rd_data => scio_rd_data,
			scio_rdy_cnt => scio_rdy_cnt
		);

	cmp_io: entity work.scio 
		generic map (
			addr_bits => io_addr_bits
		)
		port map (
			clk => clk_int,
			reset => int_res,

			address => scio_address,
			wr_data => scio_wr_data,
			rd => scio_rd,
			wr => scio_wr,
			rd_data => scio_rd_data,
			rdy_cnt => scio_rdy_cnt,

			irq => io_irq,
			irq_ena => io_irq_ena,
			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			l => io_l,
			r => io_r,
			t => io_t,
			b => io_b
		);

	cmp_mem: entity work.mem_sc
		generic map (
			jpc_width => jpc_width,
			block_bits => block_bits,
			addr_bits => 18
		)
		port map (
			clk => clk_int,
			reset => int_res,
			din => stack_tos,

			mem_rd => mem_rd,
			mem_wr => mem_wr,
			mem_addr_wr => mem_addr_wr,
			mem_bc_rd => mem_bc_rd,
			dout => mem_dout,
			bcstart => mem_bcstart,
			bsy => mem_bsy,

			jbc_addr => jbc_addr,
			jbc_data => jbc_data,

			address => sc_address,
			wr_data => sc_wr_data,
			rd => sc_rd,
			wr => sc_wr,
			rd_data => sc_rd_data,
			rdy_cnt => sc_rdy_cnt
		);

	cmp_scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			rom_cnt => rom_cnt,
			addr_bits => 18
		)
		port map (
			clk => clk_int,
			reset => int_res,

			address => sc_address,
			wr_data => sc_wr_data,
			rd => sc_rd,
			wr => sc_wr,
			rd_data => sc_rd_data,
			rdy_cnt => sc_rdy_cnt,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => ram_ncs,
			ram_noe => ram_noe,
			ram_nwe => ram_nwe,

			fl_a => fl_a,
			fl_d => fl_d,
			fl_ncs => fl_ncs,
			fl_ncsb => fl_ncsb,
			fl_noe => fl_noe,
			fl_nwe => fl_nwe,
			fl_rdy => fl_rdy

		);

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			rama_d <= ram_dout(15 downto 0);
			ramb_d <= ram_dout(31 downto 16);
		else
			rama_d <= (others => 'Z');
			ramb_d <= (others => 'Z');
		end if;
	end process;

	ram_din <= ramb_d & rama_d;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	rama_a <= ram_addr;
	rama_ncs <= ram_ncs;
	rama_noe <= ram_noe;
	rama_nwe <= ram_nwe;
	rama_nlb <= '0';
	rama_nub <= '0';

	ramb_a <= ram_addr;
	ramb_ncs <= ram_ncs;
	ramb_noe <= ram_noe;
	ramb_nwe <= ram_nwe;
	ramb_nlb <= '0';
	ramb_nub <= '0';


	freeio <= 'Z';

end rtl;
