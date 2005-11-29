--
--	mem_sc.vhd
--
--	External memory interface with SimpCon
--	Translates between JOP/extension memory interface
--	and SimpCon memory interface
--
--
--	todo:
--
--	2005-11-22  first version adapted from mem(_wb)
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;

entity mem_sc is
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
end mem_sc;

architecture rtl of mem_sc is

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
							bc_cc, bc_sa, bc_r1, bc_w, bc_rn, bc_wr, bc_wl
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal mem_wr_addr		: std_logic_vector(addr_bits-1 downto 0);
	signal ram_addr			: std_logic_vector(addr_bits-1 downto 0);

	signal bcl_bsy			: std_logic;


--
--	values for bytecode read/cache
--
--	len is in words, 10 bits range is 'hardcoded' in JOPWriter.java
--	start is address in external memory (rest of the word)
--
	signal bc_len			: unsigned(jpc_width-3 downto 0);	-- length of method in words
	signal bc_mem_start		: unsigned(17 downto 0);			-- memory address of bytecode
	signal inc_mem_start	: std_logic;
	signal dec_len			: std_logic;
	signal bc_wr_addr		: unsigned(jpc_width-3 downto 0);	-- address for jbc (in words!)
	signal bc_wr_data		: std_logic_vector(31 downto 0);	-- write data for jbc
	signal bc_wr_ena		: std_logic;

	signal bc_rd			: std_logic;

--
--	signals for cache connection
--
	signal cache_rdy		: std_logic;
	signal cache_in_cache	: std_logic;
	signal cache_bcstart	: std_logic_vector(jpc_width-3 downto 0);

begin

	bsy <= '1' when rdy_cnt=3 or bcl_bsy='1' else '0';

	bcstart <= std_logic_vector(to_unsigned(0, 32-jpc_width)) & cache_bcstart & "00";

	-- change byte order for jbc memory (high byte first)
	bc_wr_data <= rd_data(7 downto 0) &
				rd_data(15 downto 8) &
				rd_data(23 downto 16) &
				rd_data(31 downto 24);


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
--	SimpCon connections
--


	address <= ram_addr;
	wr <= mem_wr;
	rd <= mem_rd or bc_rd;
	wr_data <= din;
	dout <= rd_data;


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
			mem_wr_addr <= din(addr_bits-1 downto 0);	-- store write address
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
		else
			if inc_mem_start='1' then
				bc_mem_start <= bc_mem_start+1;
			end if;
			if dec_len='1' then
				bc_len <= bc_len-1;
			end if;
		end if;

	end if;
end process;

--
--	RAM address MUX (combinational)
--
process(din, mem_wr_addr, bc_mem_start, mem_rd, mem_wr)
begin
	if mem_rd='1' then
		ram_addr <= din(addr_bits-1 downto 0);
	elsif mem_wr='1' then
		ram_addr <= mem_wr_addr;
	else
		-- default use the bc address (simpled MUX selection)
		ram_addr(17 downto 0) <= std_logic_vector(bc_mem_start);
		-- addr_bits is 17
		-- ram_addr(addr_bits-1 downto 18) <= (others => '0');
	end if;
end process;


--
--	next state logic
--
process(state, mem_rd, mem_wr, mem_bc_rd, rdy_cnt,
	cache_rdy, cache_in_cache, bc_len)
begin

	next_state <= state;

	case state is

		when idl =>
			if mem_rd='1' then
				next_state <= rd1;
			elsif mem_wr='1' then
				next_state <= wr1;
			elsif mem_bc_rd='1' then
				next_state <= bc_cc;
			end if;

		-- after a read the idl state is the result cycle
		-- where the data is available
		when rd1 =>
			-- either 1 or 0
			if rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

		-- We could avoid the idl state after wr1 to
		-- get back to back wr/wr or wr/rd.
		-- However, it is not used in JOP (at the moment).
		when wr1 =>
			-- either 1 or 0
			if rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

--
--	bytecode read
--
		-- cache lookup
		when bc_cc =>
			if cache_rdy = '1' then
				if cache_in_cache = '1' then
					next_state <= idl;
				else
					next_state <= bc_sa;
				end if;
			end if;

		-- not in cache
		when bc_sa =>
			next_state <= bc_r1;

		-- start first read
		when bc_r1 =>
			next_state <= bc_w;

		-- wait
		when bc_w =>
			if bc_len=to_unsigned(0, jpc_width-3) then
				next_state <= bc_wl;
			-- this works with pipeline level 1
			-- elsif rdy_cnt(1)='0' then

			-- we need a pipeline level of 2 in
			-- the memory interface for this to work!
			elsif rdy_cnt/=3 then
				next_state <= bc_rn;
			end if;

		-- start read 2 to n
		when bc_rn =>
			if bc_len=to_unsigned(0, jpc_width-3) then
				next_state <= bc_wl;
			else
				next_state <= bc_wr;
			end if;

		when bc_wr =>
			-- w. pipeline level 2
			if rdy_cnt/=3 then
				next_state <= bc_rn;
			else
				next_state <= bc_w;
			end if;

		-- wait fot the last ack
		when bc_wl =>
			if rdy_cnt(1)='0' then
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
		bc_wr_ena <= '0';
		inc_mem_start <= '0';
		dec_len <= '0';
		bc_rd <= '0';
		bcl_bsy <= '0';
	elsif rising_edge(clk) then

		state <= next_state;

		bc_wr_ena <= '0';
		inc_mem_start <= '0';
		dec_len <= '0';
		bc_rd <= '0';

		case next_state is

			when idl =>
				bcl_bsy <= '0';

			when rd1 =>

			when wr1 =>

			when bc_cc =>
				bcl_bsy <= '1';
				-- cache check

			when bc_sa =>
				-- setup data
				bc_wr_addr <= unsigned(cache_bcstart);

			when bc_r1 =>
				-- first memory read
				inc_mem_start <= '1';
				bc_rd <= '1';

			when bc_w =>
				-- wait

			when bc_rn =>
				-- following memory reads
				inc_mem_start <= '1';
				bc_rd <= '1';

			when bc_wr =>
				-- BC write
				bc_wr_ena <= '1';
				dec_len <= '1';

			when bc_wl =>
				-- wait for last (unnecessary read)

		end case;
					
		-- increment in state write
		if state=bc_wr then
			bc_wr_addr <= bc_wr_addr+1;		-- next jbc address
		end if;
	end if;
end process;

end rtl;
