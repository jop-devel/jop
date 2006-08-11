--
--	jop_avalon.vhd
--
--	top level for Avalon (SPOC Builder) version
--
--	2006-08-10	adapted from jop_256x16.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;


entity jop_avalon is

generic (
	addr_bits	: integer := 24;	-- address range for the avalone interface
	exta_width	: integer := 3;		-- length of exta part in JOP microcode
	io_addr_bits	: integer := 7;	-- address bits of internal io
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4		-- 2*block_bits is number of cache blocks
);

port (
	clk, reset		: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;

--
--	watchdog
--
	wd				: out std_logic;

--
-- Avalon interface
--

	address		: out std_logic_vector(addr_bits-1+2 downto 0);
	writedata	: out std_logic_vector(31 downto 0);
	byteenable	: out std_logic_vector(3 downto 0);
	readdata	: in std_logic_vector(31 downto 0);
	read		: out std_logic;
	write		: out std_logic;
	waitrequest	: in std_logic

);
end jop_avalon;

architecture rtl of jop_avalon is

--
--	components:
--

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



component extension is

generic (exta_width : integer; io_addr_bits : integer);

port (
	clk, reset	: in std_logic;

-- core interface

	ain			: in std_logic_vector(31 downto 0);		-- TOS
	bin			: in std_logic_vector(31 downto 0);		-- NOS
	ext_addr	: in std_logic_vector(exta_width-1 downto 0);
	rd, wr		: in std_logic;
	bsy			: out std_logic;
	dout		: out std_logic_vector(31 downto 0);	-- to stack

-- mem interface

	mem_rd		: out std_logic;
	mem_wr		: out std_logic;
	mem_addr_wr	: out std_logic;
	mem_bc_rd	: out std_logic;
	mem_data	: in std_logic_vector(31 downto 0); 	-- output of memory module
	mem_bcstart	: in std_logic_vector(31 downto 0); 	-- start of method in bc cache
	mem_bsy		: in std_logic;
	
-- SimpCon master io interface

	scio_address		: out std_logic_vector(io_addr_bits-1 downto 0);
	scio_wr_data		: out std_logic_vector(31 downto 0);
	scio_rd, scio_wr	: out std_logic;
	scio_rd_data		: in std_logic_vector(31 downto 0);
	scio_rdy_cnt		: in unsigned(1 downto 0)

);
end component;


component scio is
generic (addr_bits : integer);

port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

-- interrupt

	irq			: out std_logic;
	irq_ena		: out std_logic;

-- exception

	exc_req		: in exception_type;
	exc_int		: out std_logic;

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
end component;


component mem_sc is
generic (jpc_width : integer; block_bits : integer; addr_bits : integer);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(31 downto 0);

	mem_rd		: in std_logic;
	mem_wr		: in std_logic;
	mem_addr_wr	: in std_logic;
	mem_bc_rd	: in std_logic;
	dout		: out std_logic_vector(31 downto 0);
	bcstart		: out std_logic_vector(31 downto 0); 	-- start of method in bc cache

	bsy			: out std_logic;

-- jbc connections

	jbc_addr	: in std_logic_vector(jpc_width-1 downto 0);
	jbc_data	: out std_logic_vector(7 downto 0);

-- SimpCon interface

	address		: out std_logic_vector(addr_bits-1 downto 0);
	wr_data		: out std_logic_vector(31 downto 0);
	rd, wr		: out std_logic;
	rd_data		: in std_logic_vector(31 downto 0);
	rdy_cnt		: in unsigned(1 downto 0)

);
end component;


component sc2avalon is
generic (addr_bits : integer);

port (

	clk, reset	: in std_logic;

-- SimpCon interface

	sc_address		: in std_logic_vector(addr_bits-1 downto 0);
	sc_wr_data		: in std_logic_vector(31 downto 0);
	sc_rd, sc_wr	: in std_logic;
	sc_rd_data		: out std_logic_vector(31 downto 0);
	sc_rdy_cnt		: out unsigned(1 downto 0);

-- Avalon interface

	av_address		: out std_logic_vector(addr_bits-1+2 downto 0);
	av_writedata	: out std_logic_vector(31 downto 0);
	av_byteenable	: out std_logic_vector(3 downto 0);
	av_readdata		: in std_logic_vector(31 downto 0);
	av_read			: out std_logic;
	av_write		: out std_logic;
	av_waitrequest	: in std_logic

);
end component;



--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------






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

	signal sc_address		: std_logic_vector(addr_bits-1 downto 0);
	signal sc_wr_data		: std_logic_vector(31 downto 0);
	signal sc_rd, sc_wr		: std_logic;
	signal sc_rd_data		: std_logic_vector(31 downto 0);
	signal sc_rdy_cnt		: unsigned(1 downto 0);

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

	signal wd_out, sp_ov	: std_logic;

-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;
begin

	ser_ncts <= '0';
--
--	avalon reset (hopefully synchronized...)
--
	int_res <= reset;

	-- no pll inside the avalon component
	clk_int <= clk;

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
			exc_int, sp_ov,
			stack_tos, stack_nos
		);

	exc_req.spov <= sp_ov;

	cmp_ext: extension 
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

	cmp_io: scio 
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
			
			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open
		);

	cmp_mem: mem_sc
		generic map (
			jpc_width => jpc_width,
			block_bits => block_bits,
			addr_bits => addr_bits
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

	sc2av: sc2avalon
		generic map (
			addr_bits => addr_bits
		)
		port map (
			clk => clk_int,
			reset => int_res,

			sc_address => sc_address,
			sc_wr_data => sc_wr_data,
			sc_rd => sc_rd,
			sc_wr => sc_wr,
			sc_rd_data => sc_rd_data,
			sc_rdy_cnt => sc_rdy_cnt,

			av_address => address,
			av_writedata => writedata,
			av_byteenable => byteenable,
			av_readdata => readdata,
			av_read => read,
			av_write => write,
			av_waitrequest => waitrequest
		);

end rtl;
