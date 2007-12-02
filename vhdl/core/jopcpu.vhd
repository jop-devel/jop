--
--	jopcpu.vhd
--
--	The JOP CPU
--
--	2007-03-16	creation
--	2007-04-13	Changed memory connection to records
--
--	todo: clean up: substitute all signals by records


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;


entity jopcpu is

generic (
	jpc_width	: integer;			-- address bits of java bytecode pc = cache size
	block_bits	: integer			-- 2*block_bits is number of cache blocks
);

port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon memory interface
--
	sc_mem_out		: out sc_mem_out_type;
	sc_mem_in		: in sc_in_type;

--
--	SimpCon IO interface
--
	sc_io_out		: out sc_io_out_type;
	sc_io_in		: in sc_in_type;

--
--	Interrupts from sc_sys
--
	irq_in			: in irq_bcf_type;
	irq_out			: out irq_ack_type;
	exc_req			: out exception_type
);
end jopcpu;

architecture rtl of jopcpu is

	constant EXTA_WIDTH : integer := 3;

--
--	Signals
--

	signal stack_tos		: std_logic_vector(31 downto 0);
	signal stack_nos		: std_logic_vector(31 downto 0);
	signal rd, wr			: std_logic;
	signal ext_addr			: std_logic_vector(EXTA_WIDTH-1 downto 0);
	signal stack_din		: std_logic_vector(31 downto 0);

-- extension/mem interface

	signal mem_in			: mem_in_type;
	signal mem_out			: mem_out_type;

	signal bsy				: std_logic;

	signal jbc_addr			: std_logic_vector(jpc_width-1 downto 0);
	signal jbc_data			: std_logic_vector(7 downto 0);

-- SimpCon io interface

	signal sp_ov			: std_logic;

begin

--
--	components of jop
--

	cmp_core: entity work.core
		generic map(jpc_width)
		port map (clk, reset,
			bsy,
			stack_din, ext_addr,
			rd, wr,
			jbc_addr, jbc_data,
			irq_in, irq_out, sp_ov,
			stack_tos, stack_nos
		);

	exc_req.spov <= sp_ov;

	cmp_ext: entity work.extension 
		generic map (exta_width => EXTA_WIDTH)
		port map (
			clk => clk,
			reset => reset,
			ain => stack_tos,
			bin => stack_nos,

			ext_addr => ext_addr,
			rd => rd,
			wr => wr,
			bsy => bsy,
			dout => stack_din,

			mem_in => mem_in,
			mem_out => mem_out,
	
			sc_io_out => sc_io_out,
			sc_io_in => sc_io_in
		);

	cmp_mem: entity work.mem_sc
		generic map (
			jpc_width => jpc_width,
			block_bits => block_bits
		)
		port map (
			clk => clk,
			reset => reset,
			ain => stack_tos,
			bin => stack_nos,

			np_exc => exc_req.np,
			ab_exc => exc_req.ab,

			mem_in => mem_in,
			mem_out => mem_out,
	
			jbc_addr => jbc_addr,
			jbc_data => jbc_data,

			sc_mem_out => sc_mem_out,
			sc_mem_in => sc_mem_in
		);

end rtl;
