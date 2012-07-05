--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2009, Martin Schoeberl (martin@jopdesign.com)
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
--	mem_sc.vhd
--
--	External memory interface with SimpCon.
--	Translates between JOP/extension memory interface
--	and SimpCon memory interface.
--	Does the method cache load.
--
--
--	2005-11-22  first version adapted from mem(_wb)
--	2006-06-15	removed unnecessary state in BC load
--				len decrement in bc_rn and exit from bc_wr
--	2007-04-13	Changed memory connection to records
--	2007-04-14	xaload and xastore in hardware
--	2008-02-19	put/getfield in hardware
--	2008-04-30  copy step in hardware
--	2008-10-10	correct array access for fast (SPM) memory (+iald23 state)
--	2009-11-11	split getfield and putfield state machine (for object cache)
--	2009-11-16	use bc operand for getfield index
--	2009-11-17	use bc operand for putfield index
--	2009-11-23	support for get/putstatic (stgs, stps)
--	2009-11-28	use the object cache, invalidate on native access
--
--	TODO: invalidate on monitorenter, volatile
--
--	comment: we could start putstatic SimpCon operation in the same cycle:
--		SimpCon wr on mem_in.putstatic, address selection from A

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.jop_types.all;
use work.sc_pack.all;

entity mem_sc is
generic (jpc_width : integer; block_bits : integer);

port (

-- jop interface

	clk, reset	: in std_logic;

	ain		: in std_logic_vector(31 downto 0);		-- TOS
	bin		: in std_logic_vector(31 downto 0);		-- NOS

-- exceptions

	np_exc		: out std_logic;
	ab_exc		: out std_logic;
	ia_exc		: out std_logic;	-- Illegal assignment flag, for scoped memory in scj.

-- extension connection (now within jopcpu)
	mem_in		: in mem_in_type;
	mem_out		: out mem_out_type;

-- jbc connections

	bc_wr_addr	: out std_logic_vector(jpc_width-3 downto 0);	-- address for jbc (in words!)
	bc_wr_data	: out std_logic_vector(31 downto 0);	-- write data for jbc
	bc_wr_ena	: out std_logic;

-- SimpCon interface

	sc_mem_out	: out sc_out_type;
	sc_mem_in	: in sc_in_type
);
end mem_sc;

architecture rtl of mem_sc is

component mcache is
generic (jpc_width : integer; block_bits : integer);

port (

	clk, reset	: in std_logic;

	bc_len		: in std_logic_vector(METHOD_SIZE_BITS-1 downto 0);	-- length of method in words
	bc_addr		: in std_logic_vector(17 downto 0);		-- memory address of bytecode

	find		: in std_logic;					-- start lookup

	bcstart		: out std_logic_vector(jpc_width-3 downto 0); 	-- start of method in bc cache

	rdy		: out std_logic;				-- lookup finished
	in_cache	: out std_logic					-- method is in cache

);
end component;

	constant ENABLE_COPY : boolean := true;

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, wr1,
							ps1,
							gs1,
							bc_cc, bc_r1, bc_w, bc_rn, bc_wr, bc_wl,
							iald0, iald1, iald2, iald23, iald3, iald4,
							iasrd, ialrb,
							iast0, iaswb, iasrb, iasst,
							gf0, gf1, gf2, gf3, gf4,
							pf0, pf1, pf2, pf3, pf4,
							cp0, cp1, cp2, cp3, cp4, cpstop,
							last,
							npexc, abexc, iaexc, excw
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	-- length should be 'real' RAM size and not RAM + Flash + NAND
	-- should also be considered in the cacheable range

	-- addr_reg used to 'store' the address for wr, bc load, and array access
	signal addr_reg		: unsigned(SC_ADDR_SIZE-1 downto 0);
	signal addr_next	: unsigned(SC_ADDR_SIZE-1 downto 0);

	-- MUX for SimpCon address and write data
	signal ram_addr		: std_logic_vector(SC_ADDR_SIZE-1 downto 0);
	signal ram_wr_data	: std_logic_vector(31 downto 0);
	signal ram_dcache	: sc_cache_type;

--
--      signals for access from the state machine
--
	signal state_bsy	: std_logic;
	signal state_rd		: std_logic;
	signal state_wr		: std_logic;
	signal state_dcache : sc_cache_type;

--
--	signals for object and array access
--
	signal index		: std_logic_vector(SC_ADDR_SIZE-1 downto 0);	-- array or field index
	signal value		: std_logic_vector(31 downto 0);		-- store value

	signal null_pointer	: std_logic;
	signal bounds_error	: std_logic;

	signal was_a_store	: std_logic;
	signal was_a_stidx	: std_logic;
	signal was_a_hwo	: std_logic;

--
--	values for bytecode read/cache
--
--	len is in words, 10 bits range is 'hardcoded' in JOPWriter.java
--	start is address in external memory (rest of the word)
--
	signal bc_len		: unsigned(METHOD_SIZE_BITS-1 downto 0);	-- length of method in words
	signal inc_addr_reg	: std_logic;
	signal dec_len		: std_logic;

	signal bc_addr		: std_logic_vector(jpc_width-3 downto 0);	-- address for jbc (in words!)
--
--	signals for method cache connection
--
	signal mcache_rdy	: std_logic;
	signal mcache_in_cache	: std_logic;
	signal mcache_bcstart	: std_logic_vector(jpc_width-3 downto 0);

--
-- signals for copying and address translation
--
	signal base_reg		: unsigned(SC_ADDR_SIZE-1 downto 0);
	signal pos_reg		: unsigned(SC_ADDR_SIZE-1 downto 0);
    signal offset_reg	: unsigned(SC_ADDR_SIZE-1 downto 0);
	signal translate_bit : std_logic;
	signal cp_stopbit   : std_logic;

--
-- signals for object cache
--
	signal ocin			: ocache_in_type;
	signal ocout		: ocache_out_type;

	signal read_ocache	: std_logic;	-- read data MUX
	
--	scj: signals for hw scope check
	signal putref_reg : std_logic; 
	signal putref_next: std_logic;
	signal dest_level : unsigned(6 downto 0);
	signal dest_level_reg: unsigned(6 downto 0);
	signal illegal_assignment : std_logic;

begin

process(sc_mem_in, state_bsy, state)
begin
	mem_out.bsy <= '0';
	if sc_mem_in.rdy_cnt=3 then
		mem_out.bsy <= '1';
	else
		if state/=ialrb and state/=last and state/=gf4 and state_bsy='1' then
			mem_out.bsy <= '1';
		end if;
	end if;
end process;

	mem_out.bcstart <= std_logic_vector(to_unsigned(0, 32-jpc_width)) & mcache_bcstart & "00";


	np_exc <= null_pointer;
	ab_exc <= bounds_error;
	
	-- scj: flag that indicates an illegal memory assignment
	ia_exc <= illegal_assignment;

	-- change byte order for jbc memory (high byte first)
	bc_wr_data <= sc_mem_in.rd_data(7 downto 0) &
				sc_mem_in.rd_data(15 downto 8) &
				sc_mem_in.rd_data(23 downto 16) &
				sc_mem_in.rd_data(31 downto 24);

	bc_wr_addr <= bc_addr;


	mc: mcache generic map (jpc_width, block_bits) port map(
		clk, reset,
		std_logic_vector(bc_len), std_logic_vector(addr_reg(17 downto 0)),
		mem_in.bc_rd,
		mcache_bcstart,
		mcache_rdy, mcache_in_cache
	);


--
--	Object cache connections
--

	oc: entity work.ocache
		port map (
			clk => clk,
			reset => reset,
			ocin => ocin,
			ocout => ocout
		);

	-- maximum fields is restricted to 8 bits, but just 32 fields currently allowed
	ocin.index <= mem_in.bcopd(OCACHE_MAX_INDEX_BITS-1 downto 0);
	ocin.handle <= ain(SC_ADDR_SIZE-1 downto 0);
	-- putfield has the handle in bin...
	-- check cache only on real getfield
	ocin.chk_gf <= mem_in.getfield and not was_a_stidx;
	ocin.gf_val <= sc_mem_in.rd_data;
	ocin.pf_val <= value;
	ocin.inval <= mem_in.stidx or mem_in.cinval;

--
--	SimpCon connections
--

	sc_mem_out.address <= ram_addr;
	sc_mem_out.wr_data <= ram_wr_data;
--	sc_mem_out.rd <= mem_in.rd or mem_in.rdc or mem_in.rdf or mem_in.getfield or mem_in.iaload or state_rd;
	sc_mem_out.rd <= mem_in.rd or mem_in.rdc or mem_in.rdf or mem_in.iaload or state_rd;
	sc_mem_out.wr <= mem_in.wr or mem_in.wrf or state_wr;
	sc_mem_out.cache <= ram_dcache;
	sc_mem_out.cinval <= mem_in.cinval;

--
--	read data MUX on cache hit
--
process(read_ocache, sc_mem_in, ocout)
begin

	if read_ocache='1' then
		mem_out.dout <= ocout.dout;
	else
		mem_out.dout <= sc_mem_in.rd_data;
	end if;
end process;


--
--	RAM address MUX (combinational)
--
--	TODO: check fmax without GC address translation - it's 94 MHz instead of 83 MHz
--	putstatic is more relaxed with one more cycle and registering
--	the address
--
process(ain, bin, addr_reg, offset_reg, mem_in, base_reg, pos_reg, translate_bit)
begin
	-- MS: why should the first memory operation on getfield, which is the handle
	-- read go through the GC copy translation?
--	if mem_in.rd='1' or mem_in.rdc='1' or mem_in.rdf='1' or mem_in.getfield='1' then
	if mem_in.rd='1' or mem_in.rdc='1' or mem_in.rdf='1' then
		if unsigned(ain(SC_ADDR_SIZE-1 downto 0)) >= base_reg and unsigned(ain(SC_ADDR_SIZE-1 downto 0)) < pos_reg then
			ram_addr <= std_logic_vector(unsigned(ain(SC_ADDR_SIZE-1 downto 0)) + offset_reg);
		else
			ram_addr <= ain(SC_ADDR_SIZE-1 downto 0);
		end if;
	elsif mem_in.iaload='1' then
		if unsigned(bin(SC_ADDR_SIZE-1 downto 0)) >= base_reg and unsigned(bin(SC_ADDR_SIZE-1 downto 0)) < pos_reg then
			ram_addr <= std_logic_vector(unsigned(bin(SC_ADDR_SIZE-1 downto 0)) + offset_reg);
		else
			ram_addr <= bin(SC_ADDR_SIZE-1 downto 0);
		end if;		
	else
		-- default is the registered address for wr, bc load
		-- MS: we could get rid of the adder when doing the addition
		-- one cycle before. Perhaps it's not worth the effort as the
		-- copy unit will go away at some point.
		if translate_bit='1' then
			ram_addr <= std_logic_vector(addr_reg(SC_ADDR_SIZE-1 downto 0) + offset_reg);
		else
			ram_addr <= std_logic_vector(addr_reg(SC_ADDR_SIZE-1 downto 0));
		end if;
	end if;
end process;


--
-- Data cache MUX
--
process (mem_in, state_dcache)
begin  -- process	
	-- MS: in what state is the cache marker on the other MMU
	-- commands? E.g. putfield, get/putstatic, stidx, iastore
	-- Looks ok as state_dcache is changed in the state machine
	ram_dcache <= state_dcache;
	if mem_in.rd = '1' then
		ram_dcache <= bypass;
	elsif mem_in.rdc = '1' then
		ram_dcache <= direct_mapped_const;
	elsif mem_in.rdf='1' or mem_in.wrf='1' or mem_in.getfield = '1' or mem_in.iaload = '1' then
		ram_dcache <= full_assoc;		
	end if;
end process;


--
-- prepare RAM address registering
--
process(addr_reg, ain, bin, dest_level_reg, inc_addr_reg, index, mem_in, offset_reg, pos_reg, putref_reg, sc_mem_in, state, was_a_stidx)
begin

	-- default values
	addr_next <= addr_reg;
	putref_next <= putref_reg;
	dest_level	<= dest_level_reg;

	if inc_addr_reg='1' then
		addr_next <= addr_reg+1;
	end if;

	-- computations that depend on mem_in
	if mem_in.addr_wr='1' then
		addr_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
	end if;
	
	if mem_in.putstatic ='1' or mem_in.getstatic='1' then
		if was_a_stidx = '1' then
			addr_next <= to_unsigned(to_integer(unsigned(index)), SC_ADDR_SIZE);
		else
			addr_next <= to_unsigned(to_integer(unsigned(mem_in.bcopd)), SC_ADDR_SIZE);			
		end if;
	end if;

	if mem_in.bc_rd='1' then
		addr_next(17 downto 0) <= unsigned(ain(27 downto 10));
		-- addr_bits is 17
		if SC_ADDR_SIZE>18 then
			addr_next(SC_ADDR_SIZE-1 downto 18) <= (others => '0');
		end if;
	end if;
	
	if mem_in.iaload='1' then
		addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
	end if;

	if mem_in.getfield='1' then
		addr_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
	end if;

	if mem_in.putfield='1' then
		addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
		dest_level <= unsigned(bin(31 downto SC_ADDR_SIZE+2));
	end if;
	
	--
	if mem_in.putref='1' then
		putref_next <= '1';
	end if;

	-- computations that depend on the state
	if state=iast0 then
		addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
		dest_level <= unsigned(bin(31 downto SC_ADDR_SIZE+2));
	end if;

	-- get/putfield could be optimized for faster memory (e.g. SPM or SimpCon cache - see mem_sc_new)
	if state=iald3 or state=iald23 or state=gf2 or state=pf3 then
		addr_next <= unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0))+unsigned(index);
	end if;

	if state=cp0 then
		addr_next <= pos_reg;
	end if;		

	if state=cp3 then
		addr_next <= pos_reg + offset_reg;
	end if;
	
end process;


--
--	RAM write data MUX (combinational)
--
process(ain, addr_reg, mem_in, value)
begin
	if mem_in.wr='1' or mem_in.wrf='1' then
		ram_wr_data <= ain;
	else
		-- default is the registered value
		ram_wr_data <= value;
	end if;
end process;


--
--	next state logic
--
process(state, mem_in, sc_mem_in,
	mcache_rdy, mcache_in_cache, bc_len, value, index, 
	addr_reg, cp_stopbit, was_a_store, ocout, putref_reg, dest_level_reg)
begin

	next_state <= state;

	case state is

		when idl =>
			if mem_in.rd='1' then
				next_state <= rd1;
			elsif mem_in.wr='1' then 
				next_state <= wr1;
			elsif mem_in.putstatic='1' then
				next_state <= ps1;
			elsif mem_in.getstatic='1' then
				next_state <= gs1;
			elsif mem_in.rdc='1' then
				next_state <= rd1;
			elsif mem_in.rdf='1' then
				next_state <= rd1;
			elsif mem_in.wrf='1' then 
				next_state <= wr1;
			elsif mem_in.bc_rd='1' then
				next_state <= bc_cc;
			elsif mem_in.iaload='1' then
				next_state <= iald0;
			elsif mem_in.getfield='1' then
				if ocout.hit='1' then
					next_state <= idl;
				else
					next_state <= gf0;
				end if;
			elsif mem_in.putfield='1' then
				next_state <= pf0;
			elsif mem_in.copy='1' and ENABLE_COPY then
				next_state <= cp0;				
			elsif mem_in.iastore='1' then
				next_state <= iast0;
			end if;

		-- after a read the idl state is the result cycle
		-- where the data is available
		when rd1 =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

		-- We could avoid the idl state after wr1 to
		-- get back to back wr/wr or wr/rd.
		-- However, it is not used in JOP (at the moment).
		--
		-- Shared with putstatic
		when wr1 =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

		when ps1 =>
			-- perform the scope check
			if (putref_reg='1') and (unsigned(value(31 downto SC_ADDR_SIZE+2)) /= 0) then
				--test failed
				next_state <= iaexc;
			else
				next_state <= last;
			end if;

		when gs1 =>
			next_state <= last;

--
--	bytecode read
--
		-- cache lookup
		when bc_cc =>
			if mcache_rdy = '1' then
				if mcache_in_cache = '1' then
					next_state <= idl;
				else
					next_state <= bc_r1;
				end if;
			end if;

		-- not in cache
		-- start first read
		when bc_r1 =>
			next_state <= bc_w;
			-- even for a two cycle memory we have to go to
			-- wait for the first time as rdy_cnt is 0 in
			-- this state. Becomes valid in the next cycle

		-- wait
		when bc_w =>
			-- this works with pipeline level 1
			-- if sc_mem_in.rdy_cnt(1)='0' then
			-- we need a pipeline level of 2 in
			-- the memory interface for this to work!
			if sc_mem_in.rdy_cnt/=3 then
				next_state <= bc_rn;
			end if;

		-- start read 2 to n
		when bc_rn =>
			next_state <= bc_wr;

		when bc_wr =>
			if bc_len=to_unsigned(0, jpc_width-3) then
				next_state <= bc_wl;
			else
				-- w. pipeline level 2
				if sc_mem_in.rdy_cnt/=3 then
					next_state <= bc_rn;
				else
					next_state <= bc_w;
				end if;
			end if;

		-- wait for the last ack
		when bc_wl =>
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

--
--	array access
--
		when iast0 =>
			-- just one cycle wait to store the value
			next_state <= iald0;

		--
		-- iald0 to iald3 are shared with iastore
		--
		when iald0 =>

			if addr_reg=0 then
				next_state <= npexc;
			elsif index(SC_ADDR_SIZE-1)='1' then
				next_state <= abexc;
			elsif (putref_reg='1') and (dest_level_reg < unsigned(value(31 downto SC_ADDR_SIZE+2))) then
 				--test failed
 				next_state <= iaexc;
			else
				next_state <= iald1;
				-- shortcut
				if sc_mem_in.rdy_cnt/=3 and was_a_store='0' then
					next_state <= iald2;
				end if;
			end if;
			
			
		when iald1 =>
			-- w. pipeline level 2
			-- would waste one cycle in a single cycle memory (similar
			-- to bc load) - SimpCon rd comes from registered state_rd.
--			if sc_mem_in.rdy_cnt<=1 then
--				next_state <= iald23;
--			els
			if sc_mem_in.rdy_cnt/=3 then
				next_state <= iald2;
			end if;

		--
		-- a quick hack for faster memory: we need to read
		-- it now! TODO: get better state names
		-- it's a mix of iald2 and iald3
		--
		when iald23 =>
			next_state <= iald4;
------ that's now load specific!
-- we start loading before we know the upper bound exception!
-- is there an issue with read peripherals????
			if was_a_store='1' then
				next_state <= iaswb;
			-- w. pipeline level 2
			elsif sc_mem_in.rdy_cnt/=3 then
				next_state <= iasrd;
			end if;

		when iald2 =>
			next_state <= iald3;

		when iald3 =>
			next_state <= iald4;
------ that's now load specific!
-- we start loading before we know the upper bound exception!
-- is there an issue with read peripherals????
			if was_a_store='1' then
				next_state <= iaswb;
			-- w. pipeline level 2
			elsif sc_mem_in.rdy_cnt/=3 then
				next_state <= iasrd;
			end if;

		when iald4 =>
			if sc_mem_in.rdy_cnt/=3 then
				next_state <= iasrd;
			end if;

		-- rdy_cnt is less than 3 we can move on
		when iasrd =>
			next_state <= ialrb;

		when ialrb =>
			-- can we optimize this when we increment index at some state?			
			if (unsigned(index) >= unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0))) and sc_mem_in.rdy_cnt /= 0 then
				next_state <= abexc;				
			-- either 1 or 0
			elsif sc_mem_in.rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

		when iaswb =>
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= iasrb;
			end if;

		when iasrb =>
			next_state <= iasst;
			-- can we optimize this when we increment index at some state?
			if unsigned(index) >= unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0)) then
				next_state <= abexc;
			end if;

		when iasst =>
			next_state <= last;

		when gf0 =>
			if addr_reg=0 then
				next_state <= npexc;
			else
				next_state <= gf1;
			end if;
		when gf1 =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= gf2;
			end if;
		when gf2 =>
			next_state <= gf3;
		when gf3 =>
			next_state <= gf4;
		when gf4 =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

		when pf0 =>
			-- just 'waste' a cycle to get opd in
			-- ok postion
			next_state <= pf1;
		when pf1 =>
			if addr_reg=0 then
				next_state <= npexc;
			--perform the scope check
 			elsif (putref_reg='1') and (dest_level_reg < unsigned(value(31 downto SC_ADDR_SIZE+2))) then
 				--test failed
 				next_state <= iaexc;
 			else
				next_state <= pf2;
			end if;
		when pf2 =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= pf3;
			end if;
		when pf3 =>
			next_state <= pf4;

		when pf4 =>
			next_state <= last;

		when cp0 =>
			next_state <= cp1;
			if cp_stopbit = '1' then
				next_state <= cpstop;
			end if;
		when cp1 =>
			next_state <= cp2;
		when cp2 =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= cp3;
			end if;
		when cp3 =>
			next_state <= cp4;
		when cp4 =>
			next_state <= last;
		when cpstop =>
			next_state <= idl;
			
		-- just wait on rdy_cnt to finish the memory transaction
		when last =>
			-- either 1 or 0
			if sc_mem_in.rdy_cnt(1)='0' then
				next_state <= idl;
			end if;

		when npexc =>
			next_state <= excw;

		when abexc =>
			next_state <= excw;
			
		when iaexc =>
			next_state <= excw;

		when excw =>
			if sc_mem_in.rdy_cnt="00" then
				next_state <= idl;
			end if;

	end case;
end process;

--
--	state machine register
--	and output register
--
--
--	Store the write address
--		and some other stuff for field and array access
--	TODO: wouldn't it be easier to use A and B
--		for data and address with a single write
--		command?
--		- see jvm.asm...
--
--	and array access stores
--
process(clk, reset)

begin
	if (reset='1') then

		-- address and index stuff
		addr_reg <= (others => '0');
		index <= (others => '0');
		value <= (others => '0');
		was_a_store <= '0';
		was_a_stidx <= '0';
		was_a_hwo <= '0';
		bc_len <= (others => '0');

		base_reg <= (others => '0');
		pos_reg <= (others => '0');
		offset_reg <= (others => '0');
		
		putref_reg <= '0';
		dest_level_reg <= (others => '0');

		-- state machine and registered outputs
		state <= idl;
		bc_wr_ena <= '0';
		inc_addr_reg <= '0';
		dec_len <= '0';
		state_rd <= '0';
		state_bsy <= '0';
		null_pointer <= '0';
		bounds_error <= '0';
		illegal_assignment <= '0';
		state_wr <= '0';
		sc_mem_out.atomic <= '0';
		sc_mem_out.tm_cache <= '1';
		ocin.wr_gf <= '0';
		ocin.chk_pf <= '0';
		ocin.wr_pf <= '0';
		read_ocache <= '0';

	elsif rising_edge(clk) then


		--
		-- address, index, bc registers
		--
		if mem_in.bc_rd='1' then
			bc_len <= unsigned(ain(METHOD_SIZE_BITS-1 downto 0));
		else
			if dec_len='1' then
				bc_len <= bc_len-1;
			end if;
		end if;

		-- save array address and index
		if mem_in.iaload='1' or mem_in.stidx='1' then
			index <= ain(SC_ADDR_SIZE-1 downto 0);		-- store array, field index
		end if;
		if mem_in.stidx='1' then
			was_a_stidx <= '1';
		end if;
		-- store the index on get/putfield when not yet stored via stidx
		-- if mem_in.getfield='1' or mem_in.putfield='1' then
		if mem_in.getfield='1' or state=pf0 then
			if was_a_stidx='0' then
				-- store the index from the bytecode operand, strange way to resize a slv
				index <= std_logic_vector(to_signed(to_integer(unsigned(mem_in.bcopd)), SC_ADDR_SIZE));
			end if;
		end if;

		-- first step of three-operand operations
		if mem_in.iastore='1' or mem_in.putfield='1' or mem_in.putstatic='1' then
			value <= ain;
		end if;
		-- get index for array stores
		if state=iast0 then
			index <= ain(SC_ADDR_SIZE-1 downto 0);		-- store array index
		end if;

		-- get source and index for copying
		if not ENABLE_COPY then
			base_reg <= (others => '0');
			pos_reg <= (others => '0');
		end if;
		if mem_in.copy='1' and ENABLE_COPY then
			base_reg <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
			pos_reg <= unsigned(ain(SC_ADDR_SIZE-1 downto 0)) + unsigned(bin(SC_ADDR_SIZE-1 downto 0));
			cp_stopbit <= ain(31);
		end if;
		
		-- get destination for copying
		if state=cp0 then
			offset_reg <= unsigned(bin(SC_ADDR_SIZE-1 downto 0)) - base_reg;
		end if;

		-- address and data tweaking for copying
		if state=cp3 then
			pos_reg <= pos_reg+1;
			value <= sc_mem_in.rd_data;
		end if;
		if state=cpstop then
			pos_reg <= base_reg;
		end if;

		-- precompute address translation
		if addr_next >= base_reg and addr_next < pos_reg then
			translate_bit <= '1';
		else
			translate_bit <= '0';
		end if;
		
		addr_reg <= addr_next;
		putref_reg <= putref_next;
		dest_level_reg <= dest_level;
		
		-- set flag for state sharing
		if mem_in.iaload='1' or mem_in.getfield='1' then
			was_a_store <= '0';
		elsif mem_in.iastore='1' or mem_in.putfield='1' then
			was_a_store <= '1';
		end if;		

		-- set MUX selection on getfield and hit
		if state=idl then
			if ocout.hit='1' and ocin.chk_gf='1' then
				read_ocache <= '1';
			end if;
		else
			-- default on every state other then idl
			read_ocache <= '0';
		end if;

		-- change arbiter to atomic mode
		if mem_in.atmstart='1' then
			sc_mem_out.atomic <= '1';
		elsif mem_in.atmend='1' then
			sc_mem_out.atomic <= '0';
		end if;
			
		--
		-- state machine and registered outputs
		-- default values
		--
		state <= next_state;

		-- single cycle signals have a default
		bc_wr_ena <= '0';
		inc_addr_reg <= '0';
		dec_len <= '0';
		state_rd <= '0';
		null_pointer <= '0';
		bounds_error <= '0';
		state_wr <= '0';
		state_dcache <= bypass;
		sc_mem_out.tm_cache <= '1';
		ocin.wr_gf <= '0';
		ocin.chk_pf <= '0';
		ocin.wr_pf <= '0';
		
		illegal_assignment <= '0';
		

		case next_state is

			when idl =>
				state_bsy <= '0';
				putref_reg <='0';
				-- update object cache on a missed getfield
				-- only valid on first idle cycle when comming
				-- from a 'real' getfield (no preceeding stidx)
				if state=gf4 then
					if was_a_stidx='0' and was_a_hwo='0' then
						ocin.wr_gf <= '1';
					end if;
				end if;
				if state=gf4 or state=last then
					was_a_stidx <= '0';		-- reset a former stidx marker
					was_a_hwo <= '0';		-- reset a former hwo marker
				end if;

			when rd1 =>
--				read_ocache<='0';
				state_bsy <= '0';

			when wr1 =>
--				read_ocache<='0';
				state_bsy <= '0';

			when ps1 =>
--				read_ocache<='0';
				state_bsy <= '1';
				state_wr <= '1';
				state_dcache <= direct_mapped;

			when gs1 =>
--				read_ocache<='0';
				state_bsy <= '1';
				state_rd <= '1';
				state_dcache <= direct_mapped;

			when bc_cc =>
--				read_ocache<='0';
				state_bsy <= '1';
				-- cache check
				sc_mem_out.tm_cache <= '0';				

			when bc_r1 =>
				-- setup data
				bc_addr <= mcache_bcstart;
				-- first memory read
				inc_addr_reg <= '1';
				state_rd <= '1';
				sc_mem_out.tm_cache <= '0';

			when bc_w =>
				-- wait
				sc_mem_out.tm_cache <= '0';

			when bc_rn =>
				-- following memory reads
				inc_addr_reg <= '1';
				dec_len <= '1';
				state_rd <= '1';
				sc_mem_out.tm_cache <= '0';

			when bc_wr =>
				-- BC write
				bc_wr_ena <= '1';				
				sc_mem_out.tm_cache <= '0';

			when bc_wl =>
				-- wait for last (unnecessary read)
				sc_mem_out.tm_cache <= '0';

			when iast0 =>
--				read_ocache<='0';
				state_bsy <= '1';

			when iald0 =>
--				read_ocache<='0';
				state_bsy <= '1';
				inc_addr_reg <= '1';
				-- read for store cannot happen earlier
				if state=iast0 then
					state_rd <= '1';
				end if;
				state_dcache <= full_assoc;

			when iald1 =>

			when iald2 =>
				state_rd <= '1';
				state_dcache <= full_assoc;

			when iald23 =>
				state_rd <= '1';
				state_dcache <= full_assoc;

			when iald3 =>

			when iald4 =>

			when iasrd =>
				state_rd <= '1';

			when ialrb =>

			when iaswb =>

			when iasrb =>
				
			when iasst =>
				state_wr <= '1';

			when gf0 =>
--				read_ocache<='0';
				state_rd <= '1';
				state_bsy <= '1';

			when gf1 =>

			when gf2 =>

			when gf3 =>
				state_rd <= '1';
				was_a_hwo <= sc_mem_in.rd_data(31);
				state_dcache <= full_assoc;
                          
			when gf4 =>

			when pf0 =>
--				read_ocache<='0';
				state_bsy <= '1';
				if was_a_stidx='0' then
					ocin.chk_pf <= '1';
				end if;

			when pf1 =>
				state_rd <= '1';
				state_dcache <= full_assoc;

			when pf2 =>

			when pf3 =>

			when pf4 =>
				-- update object cache on a putfield
				-- no write allocation is determined in ocache
				-- only on 'normal' putfield (no Native, no I/O)
				-- decision on write allocate is in cache
				if was_a_stidx='0' and sc_mem_in.rd_data(31)='0' then
					ocin.wr_pf <= '1';
				end if;
				state_wr <= '1';
				state_dcache <= full_assoc;
                          
			when cp0 =>
--				read_ocache<='0';
				state_bsy <= '1';

			when cp1 =>
				state_rd <= '1';
				
			when cp2 =>

			when cp3 =>

			when cp4 =>
				state_wr <= '1';

			when cpstop =>

			when last =>
				state_bsy <= '0';

			when npexc =>
				null_pointer <= '1';

			when abexc =>
				bounds_error <= '1';
				
			when iaexc =>
				illegal_assignment <= '1';

			when excw =>

		end case;
					
		-- increment in state write
		if state=bc_wr then
			-- next jbc address
			bc_addr <= std_logic_vector(unsigned(bc_addr)+1);		
		end if;
	end if;

end process;

end rtl;
