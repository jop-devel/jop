--
--	jop_de2.vhd
--
--	top level for Altera DE2 board
--
--	uses the single 256x16 SRAM (10 ns)
--
--	2006-08-06	adapted from jopcyc.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.jop_config.all;


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
	CLOCK_50		: in std_logic;
--
--	serial interface
--
	UART_TXD		: out std_logic;
	UART_RXD		: in std_logic;

--
--	watchdog
--
	LEDG		: out std_logic_vector(8 downto 0);

--
--	only one ram banks
--
	SRAM_ADDR		: out std_logic_vector(17 downto 0);
	SRAM_DQ		: inout std_logic_vector(15 downto 0);
	SRAM_CE_N	: out std_logic;
	SRAM_OE_N	: out std_logic;
	SRAM_LB_N	: out std_logic;
	SRAM_UB_N	: out std_logic;
	SRAM_WE_N	: out std_logic

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

	exc_int		: in std_logic;
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
	signal ram_dout			: std_logic_vector(15 downto 0);
	signal ram_din			: std_logic_vector(15 downto 0);
	signal ram_dout_en		: std_logic;

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

	signal exc_req			: exception_type;
	signal exc_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	signal wd_out, sp_ov	: std_logic;

	-- for generation of internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;
begin

	ser_ncts <= '0';
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
		inclk0	 => CLOCK_50,
		c0	 => clk_int
	);
-- clk_int <= clk;

	-- sp_ov indicates stack overflow
	-- We can use the wd LED
	-- wd <= sp_ov;
	LEDG(8) <= wd_out;
	LEDG(7 downto 0) <= (others => '0');

	cmp_core: core generic map(jpc_width)
		port map (clk_int, int_res,
			bsy,
			stack_din, ext_addr,
			rd, wr,
			jbc_addr, jbc_data,
			io_irq, io_irq_ena,
			exc_int, sp_ov,
			stack_tos, stack_nos
		);

	exc_req.spov <= sp_ov;

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
			exc_req => exc_req,
			exc_int => exc_int,
			
			txd => UART_TXD,
			rxd => UART_RXD,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open
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
			ram_ncs => SRAM_CE_N,
			ram_noe => SRAM_OE_N,
			ram_nwe => SRAM_WE_N
		);

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			SRAM_DQ <= ram_dout(15 downto 0);
		else
			SRAM_DQ <= (others => 'Z');
		end if;
	end process;

	ram_din <= SRAM_DQ;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	SRAM_ADDR <= ram_addr;
	SRAM_LB_N <= '0';
	SRAM_UB_N <= '0';


end rtl;
