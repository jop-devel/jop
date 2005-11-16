--
--	mem_wb.vhd
--
--	external memory interface (for the Cyclone board)
--
--
--	memory mapping
--	
--		000000-x7ffff	external ram (w mirror)	max. 512 kW (4*4 MBit)
--		080000-xfffff	external rom (w mirror)	max. 512 kB (4 MBit)
--		100000-xfffff	external NAND flash
--
--	ram: 32 bit word
--	rom: 8 bit word (for flash programming)
--
--	todo:
--		check timing for async mem (zero ws?)
--
--	2002-12-02	wait instruction for memory
--	2003-02-24	version for Cycore
--	2003-03-07	leave last cs of flash/NAND.
--	2003-07-09	extra tri state signal for data out (Quartus bug)
--				leave ram addr driving (no tri state)
--	2004-01-10	release mem_bsy two cycle earlier
--	2004-09-11	move data mux and mul to extension
--	2004-09-14	flash/ram mux after data register in dout path
--	2004-12-08	release mem_bsy three cycle earlier on write
--	2005-01-11	Added cache
--	2005-04-07	read mux after registering RAM and Flash data
--	2005-05-12  no bsy on ram_cnt=2
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;

entity mem_wb is
generic (jpc_width : integer; block_bits : integer);

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

-- internal ack that is one cycle earlier than wb ack

	early_ack	: in std_logic;

-- wishbone interface

	wb_out		: out wb_mem_out_type;
	wb_in		: in wb_mem_in_type

);
end mem_wb;

architecture rtl of mem_wb is

component cache is
generic (jpc_width : integer; block_bits : integer);

port (

	clk, reset	: in std_logic;

	bc_len		: in std_logic_vector(jpc_width-3 downto 0);		-- length of method in words
	bc_addr		: in std_logic_vector(17 downto 0);		-- memory address of bytecode

	find		: in std_logic;							-- start lookup

	bcstart		: out std_logic_vector(jpc_width-3 downto 0); 	-- start of method in bc cache

	rdy			: out std_logic;						-- lookup finished
	in_cache	: out std_logic							-- method is in cache

);
end component;

--
--	jbc component (use technology specific vhdl-file cyc_jbc,...)
--
--	ajbc,xjbc are OLD!
--	check if ajbc.vhd can still be used (multicycle write!)
--
--	dual port ram
--	wraddr and wrena registered
--	rdaddr is registered
--	indata registered
--	outdata is unregistered
--

component jbc is
generic (jpc_width : integer);
port (
	clk			: in std_logic;
	data		: in std_logic_vector(31 downto 0);
	rd_addr		: in std_logic_vector(jpc_width-1 downto 0);
	wr_addr		: in std_logic_vector(jpc_width-3 downto 0);
	wr_en		: in std_logic;
	q			: out std_logic_vector(7 downto 0)
);
end component;


--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, wr1,
							bc0, bc1, bc2, bc3, bc4, bc5, bc6
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal mem_wr_addr		: std_logic_vector(MEM_ADDR_SIZE-1 downto 0);
	signal ram_addr			: std_logic_vector(MEM_ADDR_SIZE-1 downto 0);


	signal mem_rd_reg		: std_logic_vector(31 downto 0);
	signal mem_bsy			: std_logic;
	signal bcl_bsy			: std_logic;

	-- the ack, that deasserts mem_bsy
	signal bsy_ack			: std_logic;


	signal wait_state		: unsigned(3 downto 0);

--
--	values for bytecode read/cache
--
--	len is in words, 10 bits range is 'hardcoded' in JOPWriter.java
--	start is address in external memory (rest of the word)
--
	signal bc_len			: unsigned(jpc_width-3 downto 0);	-- length of method in words
	signal bc_mem_start		: unsigned(17 downto 0);			-- memory address of bytecode
	signal inc_mem_start	: std_logic;
	signal bc_wr_addr		: unsigned(jpc_width-3 downto 0);	-- address for jbc (in words!)
	signal bc_wr_data		: std_logic_vector(31 downto 0);	-- write data for jbc
	signal bc_wr_ena		: std_logic;

	signal bc_cnt			: unsigned(jpc_width-3 downto 0);	-- I can't use bc_len???


--
--	signals for cache connection
--
	signal cache_rdy		: std_logic;
	signal cache_in_cache	: std_logic;
	signal cache_bcstart	: std_logic_vector(jpc_width-3 downto 0);

begin

	-- an ack that is one cycle earlier than the
	-- WB ack to deassert mem_bsy earlier!
	bsy_ack <= early_ack;

	-- If not available than use the normal WB ack.
	-- This results in one wasted cycle for a memory
	-- operation.
	-- bsy_ack <= wb_in.ack;

	-- TODO: this bsy ack shall NOT happen on bc read!!!!
	-- deassert bsy combinatorial on the ack
	bsy <= (mem_bsy and not bsy_ack) or bcl_bsy;

	bcstart <= std_logic_vector(to_unsigned(0, 32-jpc_width)) & cache_bcstart & "00";

	-- change byte order for jbc memory (high byte first)
	bc_wr_data <= mem_rd_reg(7 downto 0) &
				mem_rd_reg(15 downto 8) &
				mem_rd_reg(23 downto 16) &
				mem_rd_reg(31 downto 24);


	cmp_cache: cache generic map (jpc_width, block_bits) port map(
		clk, reset,
		std_logic_vector(bc_len), std_logic_vector(bc_mem_start),
		mem_bc_rd,
		cache_bcstart,
		cache_rdy, cache_in_cache
	);


	cmp_jbc: jbc generic map (jpc_width)
	port map(
		clk => clk,
		data => bc_wr_data,
		wr_en => bc_wr_ena,
		wr_addr => std_logic_vector(bc_wr_addr),
		rd_addr => jbc_addr,
		q => jbc_data
	);

--
--	wishbone connections
--
	wb_out.sel <= "1111";		-- we use only 32-bit access

	-- this is the memory input register
	dout <= mem_rd_reg;

	wb_out.adr <= ram_addr;


--
--	Store the write address
--	TODO: wouldn't it be easier to use A and B
--		for data and address with a single write
--		command?
--		- see jvm.asm...
--
process(clk, reset)
begin
	if reset='1' then
		mem_wr_addr <= (others => '0');
	elsif rising_edge(clk) then
		if mem_addr_wr='1' then
			mem_wr_addr <= din(MEM_ADDR_SIZE-1 downto 0);	-- store write address
		end if;
	end if;
end process;

process(clk, reset)
begin
	if reset='1' then
		bc_len <= (others => '0');
		bc_mem_start <= (others => '0');
	elsif rising_edge(clk) then
		if mem_bc_rd='1' then
			bc_len <= unsigned(din(jpc_width-3 downto 0));
			bc_mem_start <= unsigned(din(27 downto 10));
		elsif inc_mem_start='1' then
			bc_mem_start <= bc_mem_start+1;
			bc_len <= bc_len-1;
		end if;

	end if;
end process;


process(clk, reset)
begin
	if reset='1' then
		wb_out.dat <= (others => '0');
		mem_rd_reg <= (others => '0');
	elsif rising_edge(clk) then

		if mem_rd='1' then
		elsif mem_wr='1' then
			wb_out.dat <= din;
--		elsif bc_read='1' then
		end if;

		if wb_in.ack='1' then
			mem_rd_reg <= wb_in.dat;
		end if;

	end if;
end process;


--
--	next state logic
--
process(state, mem_rd, mem_wr, mem_bc_rd, wb_in, cache_rdy, cache_in_cache)
begin

	next_state <= state;

	case state is

		when idl =>
			if mem_rd='1' then
				next_state <= rd1;
			elsif mem_wr='1' then
				next_state <= wr1;
			elsif mem_bc_rd='1' then
				next_state <= bc0;
			end if;

		when rd1 =>
			if wb_in.ack='1' then
				next_state <= idl;
			end if;

		when wr1 =>
			if wb_in.ack='1' then
				next_state <= idl;
			end if;

		-- TODO: correct bsy for bc read!!!
--
--	bytecode read
--
		-- cache lookup
		when bc0 =>
			if cache_rdy = '1' then
				if cache_in_cache = '1' then
					next_state <= idl;
				else
					next_state <= bc1;
				end if;
			end if;

		-- not in cache
		when bc1 =>
			next_state <= bc2;

		-- first read
		when bc2 =>
			next_state <= bc3;

		when bc3 =>
			if wb_in.ack='1' then
				next_state <= bc4;
			end if;

		-- next reads with BC write
		when bc4 =>
			if bc_len=to_unsigned(0, jpc_width-3) then
				next_state <= bc6;
			else
				next_state <= bc5;
			end if;

		when bc5 =>
			if wb_in.ack='1' then
				next_state <= bc4;
			end if;

		-- wait fot the last ack
		when bc6 =>
			if wb_in.ack='1' then
				next_state <= idl;
			end if;

	end case;
end process;

--
--	state machine register
--	output register
--
process(clk, reset)

begin
	if (reset='1') then
		state <= idl;
		wb_out.we <= '0';
		wb_out.cyc <= '0';
		wb_out.stb <= '0';
		ram_addr <= (others => '0');
		bc_wr_ena <= '0';
		inc_mem_start <= '0';
		bcl_bsy <= '0';
	elsif rising_edge(clk) then

		state <= next_state;

		wb_out.we <= '0';
		wb_out.cyc <= '0';
		wb_out.stb <= '0';
		bc_wr_ena <= '0';
		inc_mem_start <= '0';

		if mem_rd='1' then
			ram_addr <= din(MEM_ADDR_SIZE-1 downto 0);
		elsif mem_wr='1' then
			ram_addr <= mem_wr_addr;
		end if;

		case next_state is

			when idl =>
				bcl_bsy <= '0';

			when rd1 =>
				wb_out.cyc <= '1';
				wb_out.stb <= '1';

			when wr1 =>
				wb_out.cyc <= '1';
				wb_out.stb <= '1';
				wb_out.we <= '1';

			when bc0 =>
				bcl_bsy <= '1';
				-- cache check

			when bc1 =>
				-- setup data
				bc_wr_addr <= unsigned(cache_bcstart);

			when bc2 =>
				-- first memory read
				ram_addr(17 downto 0) <= std_logic_vector(bc_mem_start);
				ram_addr(MEM_ADDR_SIZE-1 downto 18) <= (others => '0');
				inc_mem_start <= '1';
				wb_out.cyc <= '1';
				wb_out.stb <= '1';

			when bc3 =>
				-- first ack waiting
				wb_out.cyc <= '1';
				wb_out.stb <= '1';

			when bc4 =>
				-- following memory reads with BC write
				ram_addr(17 downto 0) <= std_logic_vector(bc_mem_start);
				ram_addr(MEM_ADDR_SIZE-1 downto 18) <= (others => '0');
				inc_mem_start <= '1';
				wb_out.cyc <= '1';
				wb_out.stb <= '1';
				bc_wr_ena <= '1';

			when bc5 =>
				wb_out.cyc <= '1';
				wb_out.stb <= '1';

			-- ?? strobes still set?
			when bc6 =>
				wb_out.cyc <= '1';
				wb_out.stb <= '1';

		end case;
					
		-- increment from state bc4 to bc5
		-- bc_wr_addr is used in bc4 for the write
		if state=bc4 then
			bc_wr_addr <= bc_wr_addr+1;		-- next jbc address
		end if;
	end if;
end process;

-- TODO: asign values

-- !!!!! mem_bsy is too long asserted with this wishbone interface !!!!!

--
--	state machine for mem_bsy
--
--	If possible we use an early ack.
--
process(reset, clk)
begin

	if (reset='1') then
		mem_bsy <= '0';
	elsif rising_edge(clk) then

		-- set mem_bsy on memory request
		if state=idl then
			if mem_rd='1' or mem_wr='1' or mem_bc_rd='1' then
				mem_bsy <= '1';
			else
				mem_bsy <= '0';
			end if;
		end if;

		if state=rd1 or state=wr1 then
			-- reset it on (early) ack from the WB memory system
			if bsy_ack='1' then
				mem_bsy <= '0';
			end if;
		end if;

	end if;
					
end process;

end rtl;
