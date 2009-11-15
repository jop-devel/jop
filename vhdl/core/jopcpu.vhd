--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--	jopcpu.vhd
--
--	The JOP CPU
--
--	2007-03-16	creation
--	2007-04-13	Changed memory connection to records
--	2008-02-20	memory - I/O muxing after the memory controller (mem_sc)
--	2008-03-03	added scratchpad RAM
--	2008-03-04	correct MUX selection
--	2009-11-15	include extension code
--
--	todo: clean up: substitute all signals by records

-- comments from former extension.vhd
--
--	2004-09-11	first version
--	2005-04-05	Reserve negative addresses for wishbone interface
--	2005-04-07	generate bsy from delayed wr or'ed with mem_out.bsy
--	2005-05-30	added wishbone interface
--	2005-11-28	Substitute WB interface by the SimpCon IO interface ;-)
--				All IO devices are now memory mapped
--	2007-04-13	Changed memory connection to records
--				New array instructions
--	2007-12-22	Correction of data MUX bug for array read access
--	2008-02-20	Removed memory - I/O muxing
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;


entity jopcpu is

generic (
	jpc_width	: integer;			-- address bits of java bytecode pc = cache size
	block_bits	: integer;			-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits)
);

port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon memory interface
--
	sc_mem_out		: out sc_out_type;
	sc_mem_in		: in sc_in_type;

--
--	SimpCon IO interface
--
	sc_io_out		: out sc_out_type;
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

--
--	Signals
--

	signal stack_tos		: std_logic_vector(31 downto 0);
	signal stack_nos		: std_logic_vector(31 downto 0);
	signal rd, wr			: std_logic;
	signal mmu_instr			: std_logic_vector(MMU_WIDTH-1 downto 0);
	signal stack_din		: std_logic_vector(31 downto 0);

-- extension/mem interface

	signal mem_in			: mem_in_type;
	signal mem_out			: mem_out_type;

	signal sc_ctrl_mem_out	: sc_out_type;
	signal sc_ctrl_mem_in	: sc_in_type;

	signal sc_scratch_out	: sc_out_type;
	signal sc_scratch_in	: sc_in_type;

	signal next_mux_mem		: std_logic_vector(1 downto 0);
	signal dly_mux_mem		: std_logic_vector(1 downto 0);
	signal mux_mem			: std_logic_vector(1 downto 0);
	signal is_pipelined		: std_logic;

	signal mem_access		: std_logic;
	signal scratch_access	: std_logic;
	signal io_access		: std_logic;

	signal bsy				: std_logic;

	signal bc_wr_addr		: std_logic_vector(jpc_width-3 downto 0);	-- address for jbc (in words!)
	signal bc_wr_data		: std_logic_vector(31 downto 0);	-- write data for jbc
	signal bc_wr_ena		: std_logic;

-- SimpCon io interface

	signal sp_ov			: std_logic;

-- *************** signals from extension ****************

--
--	signals for mulitiplier
--
	signal mul_dout				: std_logic_vector(31 downto 0);
	signal mul_wr				: std_logic;


	signal wr_dly				: std_logic;	-- generate a bsy with delayed wr

	signal exr					: std_logic_vector(31 downto 0); 	-- extension data register


begin

--
--	components of jop
--

	core: entity work.core
		generic map(jpc_width)
		port map (clk, reset,
			bsy,
			stack_din, mmu_instr,
-- rd signal not used here
			rd, wr,
			bc_wr_addr, bc_wr_data, bc_wr_ena,
			irq_in, irq_out, sp_ov,
			stack_tos, stack_nos
		);

	exc_req.spov <= sp_ov;

	mem: entity work.mem_sc
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
	
			bc_wr_addr => bc_wr_addr,
			bc_wr_data => bc_wr_data,
			bc_wr_ena => bc_wr_ena,

			sc_mem_out => sc_ctrl_mem_out,
			sc_mem_in => sc_ctrl_mem_in
		);


	--
	-- Generate scratchpad memory when size is != 0.
	-- Results in warnings when the size is 0.
	--
	sc1: if spm_width /= 0 generate
		scm: entity work.sdpram
			generic map (
				width => 32,
				addr_width => spm_width
			)
			port map (
				wrclk => clk,
				data => sc_scratch_out.wr_data,
				wraddress => sc_scratch_out.address(spm_width-1 downto 0),
				wren => sc_scratch_out.wr,
				rdclk => clk,
				rdaddress => sc_scratch_out.address(spm_width-1 downto 0),
				rden => sc_scratch_out.rd,
				dout => sc_scratch_in.rd_data
		);
	end generate;

	sc_scratch_in.rdy_cnt <= (others => '0');

	--
	--	Select for the read mux
	--
	--	TODO: this mux selection works ONLY for two cycle pipelining!
	--

process(clk, reset)
begin
	if (reset='1') then
		dly_mux_mem <= (others => '0');
		next_mux_mem <= (others => '0');
		is_pipelined <= '0';
	elsif rising_edge(clk) then

		if sc_ctrl_mem_out.rd='1' or sc_ctrl_mem_out.wr='1' then
			-- highest address bits decides between IO, memory, and on-chip memory
			-- save the mux selection on read or write
			next_mux_mem <= sc_ctrl_mem_out.address(SC_ADDR_SIZE-1 downto SC_ADDR_SIZE-2);
			-- a read or write with rdy_cnt of 1 means pipelining
			if sc_ctrl_mem_in.rdy_cnt(1) = '0' then
				is_pipelined <= '1';
			end if;
		end if;
		-- delayed mux selection for pipelined access
		if sc_ctrl_mem_in.rdy_cnt(1) = '0' then
			dly_mux_mem <= next_mux_mem;
		end if;
		-- pipelining is over
		if sc_ctrl_mem_in.rdy_cnt = "00" then
			is_pipelined <= '0';
		end if;

	end if;
end process;

process(next_mux_mem, dly_mux_mem, sc_ctrl_mem_out, sc_ctrl_mem_in, sc_mem_in, sc_io_in, sc_scratch_in, is_pipelined, mux_mem)
begin

	mem_access <= '0';
	scratch_access <= '0';
	io_access <= '0';

	-- for one cycle peripherals we need to set the mux from next_mux_mem
	mux_mem <= next_mux_mem;
	-- for pipelining we need to delay the mux selection
	if is_pipelined='1' then
		mux_mem <= dly_mux_mem;
	end if;

	-- read MUX
	case mux_mem is
		when "10" =>
			sc_ctrl_mem_in <= sc_scratch_in;
		when "11" =>
			sc_ctrl_mem_in <= sc_io_in;
		when others =>
			sc_ctrl_mem_in <= sc_mem_in;
	end case;

	-- select
	case sc_ctrl_mem_out.address(SC_ADDR_SIZE-1 downto SC_ADDR_SIZE-2) is
		when "10" =>
			scratch_access <= '1';
		when "11" =>
			io_access <= '1';
		when others =>
			mem_access <= '1';
	end case;

end process;

	sc_mem_out.address <= sc_ctrl_mem_out.address;
	sc_mem_out.wr_data <= sc_ctrl_mem_out.wr_data;
	sc_mem_out.wr <= sc_ctrl_mem_out.wr and mem_access;
	sc_mem_out.rd <= sc_ctrl_mem_out.rd and mem_access;

	sc_scratch_out.address <= sc_ctrl_mem_out.address;
	sc_scratch_out.wr_data <= sc_ctrl_mem_out.wr_data;
	sc_scratch_out.wr <= sc_ctrl_mem_out.wr and scratch_access;
	sc_scratch_out.rd <= sc_ctrl_mem_out.rd and scratch_access;

	sc_io_out.address <= sc_ctrl_mem_out.address;
	sc_io_out.wr_data <= sc_ctrl_mem_out.wr_data;
	sc_io_out.wr <= sc_ctrl_mem_out.wr and io_access;
	sc_io_out.rd <= sc_ctrl_mem_out.rd and io_access;

-- *************** code from extension ****************

	ml : entity work.mul
			port map (
				clk => clk,
				ain => stack_tos,
				bin => stack_nos,
				wr => mul_wr,
				dout => mul_dout
		);

	stack_din <= exr;

--
--	read
--
--	TODO: the read MUX could be set by using the
--	according wr/mmu_instr from JOP and not the
--	following rd/mmu_instr
--	Than no intermixing of mul/mem and io operations
--	is allowed. But we are not using interleaved mul/mem/io
--	operations in jvm.asm anyway.
--
--	TAKE CARE when mem_out.bcstart is read!
--
--   ** bcstart is also read without a mem_bc_rd JOP wr !!! ***
--		=> a combinatorial mux select on rd and ext_adr==7!
--
--		The rest could be set with JOP wr start transaction 
--		Is this also true for io_data?
--
--	29.11.2005 evening: I think this solution driving the exr
--	mux from mmu_instr is quite ok. The pipelining from rd/ext_adr
--	to A is fixed.
--
process(clk, reset)
begin
	if (reset='1') then
		exr <= (others => '0');
	elsif rising_edge(clk) then

		if (mmu_instr=LDMRD) then
			exr <= mem_out.dout;
		elsif (mmu_instr=LDMUL) then
			exr <= mul_dout;
		-- elsif (mmu_instr=LDBCSTART) then
		else
			exr <= mem_out.bcstart;
		end if;

	end if;
end process;


--
--	write
--
process(clk, reset)
begin
	if (reset='1') then
		mem_in.rd <= '0';
		mem_in.wr <= '0';
		mem_in.addr_wr <= '0';
		mem_in.bc_rd <= '0';
		mem_in.iaload <= '0';
		mem_in.iastore <= '0';
		mem_in.getfield <= '0';
		mem_in.putfield <= '0';
		mul_wr <= '0';
		wr_dly <= '0';


	elsif rising_edge(clk) then
		mem_in.rd <= '0';
		mem_in.wr <= '0';
		mem_in.addr_wr <= '0';
		mem_in.bc_rd <= '0';
		mem_in.iaload <= '0';
		mem_in.iastore <= '0';
		mem_in.getfield <= '0';
		mem_in.putfield <= '0';
		mem_in.copy <= '0';
		mul_wr <= '0';

		wr_dly <= wr;

--
--	wr is generated in decode and one cycle earlier than
--	the data to be written (e.g. read address for the memory interface)
--
		if wr='1' then
			if mmu_instr=STMRA then
				mem_in.rd <= '1';		-- start memory or io read
			elsif mmu_instr=STMWA then
				mem_in.addr_wr <= '1';	-- store write address
			elsif mmu_instr=STMWD then
				mem_in.wr <= '1';		-- start memory or io write
			elsif mmu_instr=STALD then
				mem_in.iaload <= '1';	-- start an array load
			elsif mmu_instr=STAST then
				mem_in.iastore <= '1';	-- start an array store
			elsif mmu_instr=STGF then
				mem_in.getfield <= '1';	-- start getfield
			elsif mmu_instr=STPF then
				mem_in.putfield <= '1';	-- start getfield
			elsif mmu_instr=STCP then
				mem_in.copy <= '1';		-- start copy
			elsif mmu_instr=STMUL then
				mul_wr <= '1';			-- start multiplier
			-- elsif mmu_instr=STBCR then
			else
				mem_in.bc_rd <= '1';	-- start bc read
			end if;
		end if;

	end if;
end process;

	-- a JOP wr generates the first bsy cycle
	-- the following are generated by the memory
	-- system or the SimpCon device
	bsy <= wr_dly or mem_out.bsy;

end rtl;
