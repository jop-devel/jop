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
--	mem_sc.vhd
--
--	External memory interface with SimpCon.
--	Translates between JOP/extension memory interface
--	and SimpCon memory interface.
--	Does the method cache load.
--
--
--	todo:
--
--	2005-11-22  first version adapted from mem(_wb)
--	2006-06-15	removed unnecessary state in BC load
--				len decrement in bc_rn and exit from bc_wr
--	2007-04-13	Changed memory connection to records
--	2007-04-14	xaload and xastore in hardware
--	2008-02-19	put/getfield in hardware
--	2008-04-30  copy step in hardware
--	2008-10-10	correct array access for fast (SPM) memory (+iald23 state)
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

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

-- extension connection
		mem_in		: in mem_in_type;
		mem_out		: out mem_out_type;

-- jbc connections

		jbc_addr	: in std_logic_vector(jpc_width-1 downto 0);
		jbc_data	: out std_logic_vector(7 downto 0);

-- SimpCon interface

		sc_mem_out	: out sc_out_type;
		sc_mem_in	: in sc_in_type
		);
end mem_sc;

architecture rtl of mem_sc is

	component cache is
		generic (jpc_width : integer; block_bits : integer);

		port (

			clk, reset	: in std_logic;

			bc_len		: in std_logic_vector(METHOD_SIZE_BITS-1 downto 0);	-- length of method in words
			bc_addr		: in std_logic_vector(17 downto 0);				-- memory address of bytecode

			find		: in std_logic;									-- start lookup

			bcstart		: out std_logic_vector(jpc_width-3 downto 0); 	-- start of method in bc cache

			rdy		: out std_logic;									-- lookup finished
			in_cache	: out std_logic									-- method is in cache

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
			clk		: in std_logic;
			data		: in std_logic_vector(31 downto 0);
			rd_addr		: in std_logic_vector(jpc_width-1 downto 0);
			wr_addr		: in std_logic_vector(jpc_width-3 downto 0);
			wr_en		: in std_logic;
			q		: out std_logic_vector(7 downto 0)
			);
	end component;


--
--	signals for mem interface
--
	type state_type		is (
		idl,
		bc_cc, bc_r1, bc_w, bc_rn, bc_wr,
		gf0, gf1,
		pf0, pf1, pf2,
		iald0, iald1, iald2, iald3, iald4,
		iast0, iast1, iast2, iast3, iast4,
		cp0, cp1, cp2,
		last, last_atomic
		);

	signal state, state_next 		: state_type;

	-- length should be 'real' RAM size and not RAM + Flash + NAND
	-- should also be considered in the cacheable range

	-- used to 'store' the address for wr, bc load, and array access
	signal addr_reg, addr_next		: unsigned(SC_ADDR_SIZE-1 downto 0);

	-- used to 'store' the index for object and array access
	signal index_reg, index_next	: unsigned(SC_ADDR_SIZE-1 downto 0);

	-- used to 'store' the value for object and array write access
	signal value_reg, value_next	: unsigned(31 downto 0);

	-- used to 'store' the size of arrays
	signal size_reg, size_next		: unsigned(SC_ADDR_SIZE-1 downto 0);

	-- registering exception signals
	signal np_exc_next, ab_exc_next : std_logic;

--
--	values for bytecode read/cache
--
--	len is in words, 10 bits range is 'hardcoded' in JOPWriter.java
--	start is address in external memory (rest of the word)
--
	signal bc_len, bc_len_next			: unsigned(METHOD_SIZE_BITS-1 downto 0);	-- length of method in words
	
	signal bc_wr_addr, bc_wr_addr_next	: unsigned(jpc_width-3 downto 0);	-- address for jbc (in words!)
	signal bc_wr_data					: std_logic_vector(31 downto 0);	-- write data for jbc
	signal bc_wr_ena					: std_logic;

--
--	signals for cache connection
--
	signal cache_rdy		: std_logic;
	signal cache_in_cache	: std_logic;
	signal cache_bcstart	: std_logic_vector(jpc_width-3 downto 0);

--
-- signals for copying and address translation
--
	signal base_reg, base_next		: unsigned(SC_ADDR_SIZE-1 downto 0);
	signal pos_reg, pos_next		: unsigned(SC_ADDR_SIZE-1 downto 0);
    signal offset_reg, offset_next	: unsigned(SC_ADDR_SIZE-1 downto 0);

begin

	-- change byte order for jbc memory (high byte first)
	bc_wr_data <= sc_mem_in.rd_data(7 downto 0) &
				  sc_mem_in.rd_data(15 downto 8) &
				  sc_mem_in.rd_data(23 downto 16) &
				  sc_mem_in.rd_data(31 downto 24);


	cache: cache generic map (jpc_width, block_bits) port map(
		clk, reset,
		std_logic_vector(bc_len),
		std_logic_vector(addr_reg(17 downto 0)),
		mem_in.bc_rd,
		cache_bcstart,
		cache_rdy,
		cache_in_cache
		);


	jbc: jbc generic map (jpc_width)
		port map(
			clk => clk,
			data => bc_wr_data,
			wr_en => bc_wr_ena,
			wr_addr => std_logic_vector(bc_wr_addr),
			rd_addr => jbc_addr,
			q => jbc_data
			);

--
--	the almighty synchronous process
--
	process(clk, reset)
	begin
		if reset='1' then
			
			addr_reg <= (others => '0');
			index_reg <= (others => '0');
			value_reg <= (others => '0');
			size_reg <= (others => '0');
			base_reg <= (others => '0');
			pos_reg <= (others => '0');
			offset_reg <= (others => '0');
			bc_len <= (others => '0');
			bc_wr_addr <= (others => '0');
			ab_exc <= '0';
			np_exc <= '0';
			state <= idl;
			
		elsif rising_edge(clk) then

			addr_reg <= addr_next;
			index_reg <= index_next;
			value_reg <= value_next;
			size_reg <= size_next;
			base_reg <= base_next;
			pos_reg <= pos_next;
			offset_reg <= offset_next;
			bc_len <= bc_len_next;
			bc_wr_addr <= bc_wr_addr_next;
			ab_exc <= ab_exc_next;
			np_exc <= np_exc_next;
			state <= state_next;
			
		end if;
	end process;

--
--	state machine logic
--
	process(mem_in, sc_mem_in, ain, bin,
			state,
			addr_reg, index_reg, value_reg, size_reg,
			base_reg, pos_reg, offset_reg,
			cache_rdy, cache_in_cache, cache_bcstart,
			bc_len, bc_wr_addr)

		variable field_addr : unsigned(SC_ADDR_SIZE-1 downto 0);
		
	begin

		-- default values to SimpCon
		sc_mem_out.address <= std_logic_vector(addr_reg);
		sc_mem_out.wr_data <= std_logic_vector(value_reg);
		sc_mem_out.rd <= '0';
		sc_mem_out.wr <= '0';
		sc_mem_out.atomic <= '0';
		sc_mem_out.cache <= bypass;

		-- default values to CPU
		mem_out.dout <= sc_mem_in.rd_data;
		mem_out.bcstart <= std_logic_vector(to_unsigned(0, 32-jpc_width)) & cache_bcstart & "00";
		mem_out.bsy <= '0';
		if sc_mem_in.rdy_cnt = 3 then
			mem_out.bsy <= '1';
		end if;

		-- no exceptions as default
		np_exc_next <= '0';
		ab_exc_next <= '0';

		-- address registering
		addr_next <= addr_reg;	

		index_next <= index_reg;
		value_next <= value_reg;
		size_next <= size_reg;

		base_next <= base_reg;
		pos_next <= pos_reg;
		offset_next <= offset_reg;
		
		-- default value for cache
		bc_len_next <= bc_len;
		bc_wr_ena <= '0';
		bc_wr_addr_next <= bc_wr_addr;

		-- keep state
		state_next <= state;

		-- compute field addresses
		field_addr := unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0)) + index_reg;
--		if field_addr >= base_reg and field_addr < pos_reg then
--			field_addr := field_addr + offset_reg;
--		end if;
		
		case state is

			when idl =>

				if mem_in.addr_wr='1' then					
					addr_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
				end if;
				if mem_in.rd='1' then
					sc_mem_out.address <= ain(SC_ADDR_SIZE-1 downto 0);
					sc_mem_out.cache <= direct_mapped;
					sc_mem_out.rd <= '1';
					state_next <= last;
				end if;
				if mem_in.wr='1' then	
					sc_mem_out.wr_data <= ain;
					sc_mem_out.cache <= direct_mapped;
					sc_mem_out.wr <= '1';
					state_next <= last;
				end if;
				if mem_in.bc_rd='1' then
					addr_next(17 downto 0) <= unsigned(ain(27 downto 10));
					-- addr_bits is 17
					if SC_ADDR_SIZE>18 then
						addr_next(SC_ADDR_SIZE-1 downto 18) <= (others => '0');
					end if;
					bc_len_next <= unsigned(ain(METHOD_SIZE_BITS-1 downto 0));
					state_next <= bc_cc;
				end if;
				if mem_in.iaload='1' then
					addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0))+1;
					index_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
					sc_mem_out.address <= bin(SC_ADDR_SIZE-1 downto 0);
					sc_mem_out.rd <= '1';					
					sc_mem_out.atomic <= '1';
					sc_mem_out.cache <= full_assoc;
					state_next <= iald0;
				end if;
				if mem_in.getfield='1' then
					addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
					index_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
					sc_mem_out.address <= bin(SC_ADDR_SIZE-1 downto 0);
					sc_mem_out.rd <= '1';					
					sc_mem_out.atomic <= '1';
					sc_mem_out.cache <= full_assoc;
					state_next <= gf0;
				end if;
				if mem_in.putfield='1' then					
					value_next <= unsigned(ain);					
					state_next <= pf0;
				end if;
				if mem_in.iastore='1' then
					value_next <= unsigned(ain);					
					state_next <= iast0;
				end if;
--				if mem_in.copy='1' then
--					base_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
--					addr_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0)) + unsigned(bin(SC_ADDR_SIZE-1 downto 0));

--					if ain(31) = '1' then
--						pos_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));
--						state_next <= idl;
--					else
--						state_next <= cp0;
--					end if;
--				end if;

--
--	bytecode read
--
				-- cache lookup
			when bc_cc =>
				mem_out.bsy <= '1';

				if cache_rdy = '1' then
					if cache_in_cache = '1' then
						state_next <= idl;
					else
						state_next <= bc_r1;
					end if;
				end if;

				-- not in cache
				-- start first read
			when bc_r1 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				-- first memory read
				addr_next <= addr_reg+1;
				-- setup data
				bc_wr_addr_next <= unsigned(cache_bcstart);

				sc_mem_out.rd <= '1';

				state_next <= bc_w;

				-- wait
			when bc_w =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				if sc_mem_in.rdy_cnt/=3 then
					state_next <= bc_rn;
				end if;

				-- start read 2 to n
			when bc_rn =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				-- following memory reads
				addr_next <= addr_reg+1;
				bc_len_next <= bc_len-1;

				sc_mem_out.rd <= '1';					

				state_next <= bc_wr;

			when bc_wr =>
				sc_mem_out.atomic	<= '1';
				mem_out.bsy <= '1';

				-- BC write
				bc_wr_ena <= '1';
				bc_wr_addr_next <= bc_wr_addr+1;		-- next jbc address
				
				if bc_len=to_unsigned(0, jpc_width-3) then
					state_next <= last_atomic;
				elsif sc_mem_in.rdy_cnt/=3 then
					state_next <= bc_rn;
				else
					state_next <= bc_w;
				end if;

				-- getfield
			when gf0 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				if sc_mem_in.rdy_cnt = 0 then					
					addr_next <= field_addr;
					state_next <= gf1;
				end if;

				-- NP check
				if addr_reg = 0 then
					np_exc_next <= '1';
					state_next <= last;
				end if;

			when gf1 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= full_assoc;
				mem_out.bsy <= '1';

				sc_mem_out.rd <= '1';

				state_next <= last_atomic;

				-- putfield
			when pf0 =>
				sc_mem_out.atomic <= '1';				
				sc_mem_out.cache <= full_assoc;
				mem_out.bsy <= '1';

				index_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
				addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0));

				sc_mem_out.address <= bin(SC_ADDR_SIZE-1 downto 0);
				sc_mem_out.rd <= '1';

				state_next <= pf1;
				
			when pf1 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				if sc_mem_in.rdy_cnt = 0 then					
					addr_next <= field_addr;
					state_next <= pf2;
				end if;

				-- NP check
				if addr_reg = 0 then
					np_exc_next <= '1';
					state_next <= last;
				end if;
					
			when pf2 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= full_assoc;
				mem_out.bsy <= '1';

				sc_mem_out.wr <= '1';

				state_next <= last_atomic;

				-- iaload
			when iald0 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= full_assoc;
				mem_out.bsy <= '1';					

				-- either 1 or 0
				if sc_mem_in.rdy_cnt(1) = '0' then
					if sc_mem_in.rdy_cnt(0) = '1' then
						sc_mem_out.rd <= '1';
						state_next <= iald1;
					end if;
					if sc_mem_in.rdy_cnt(0) = '0' then
						sc_mem_out.rd <= '1';
						-- register data _now_
						addr_next <= field_addr;
						state_next <= iald2;
					end if;
				else
					state_next <= iald0;
				end if;

				-- NP and AB checks
				if addr_reg = 1 then  	-- already added 1
					np_exc_next <= '1';
					state_next <= last;
				end if;
				if index_reg(SC_ADDR_SIZE-1) = '1' then
					ab_exc_next <= '1';
					state_next <= last;
				end if;

			when iald1 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				addr_next <= field_addr;
				state_next <= iald2;

			when iald2 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= bypass;
				mem_out.bsy <= '1';

				-- either 1 or 0
				if sc_mem_in.rdy_cnt(1) = '0' then
					if sc_mem_in.rdy_cnt(0) = '1' then
						sc_mem_out.rd <= '1';
						state_next <= iald3;
					end if;
					if sc_mem_in.rdy_cnt(0) = '0' then
						sc_mem_out.rd <= '1';
						-- register data _now_
						size_next <= unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0));
						state_next <= iald4;
					end if;
				else
					state_next <= iald2;
				end if;

			when iald3 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';
				
				size_next <= unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0));
				state_next <= iald4;

			when iald4 =>
				sc_mem_out.atomic <= '1';

				-- either 1 or 0					
				if sc_mem_in.rdy_cnt(1)='0' then
					state_next <= idl;
				end if;
				-- AB check
				if index_reg >= size_reg then
					ab_exc_next <= '1';
					state_next <= last;
				end if;
				
				-- iastore
			when iast0 =>
				sc_mem_out.atomic <= '1';				
				sc_mem_out.cache <= full_assoc;
				mem_out.bsy <= '1';

				index_next <= unsigned(ain(SC_ADDR_SIZE-1 downto 0));
				addr_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0))+1;

				sc_mem_out.address <= bin(SC_ADDR_SIZE-1 downto 0);
				sc_mem_out.rd <= '1';

				state_next <= iast1;

			when iast1 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= full_assoc;
				mem_out.bsy <= '1';					
				
				-- either 1 or 0
				if sc_mem_in.rdy_cnt(1) = '0' then
					if sc_mem_in.rdy_cnt(0) = '1' then
						sc_mem_out.rd <= '1';
						state_next <= iast2;						
					end if;
					if sc_mem_in.rdy_cnt(0) = '0' then
						sc_mem_out.rd <= '1';
						-- register data _now_
						addr_next <= field_addr;
						state_next <= iast3;
					end if;
				else
					state_next <= iast1;
				end if;

				-- NP and AB checks
				if addr_reg = 1 then  	-- already added 1
					np_exc_next <= '1';
					state_next <= last;
				end if;
				if index_reg(SC_ADDR_SIZE-1) = '1' then
					ab_exc_next <= '1';
					state_next <= last;
				end if;

			when iast2 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				addr_next <= field_addr;
				state_next <= iast3;

			when iast3 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				if sc_mem_in.rdy_cnt = 0 then
					size_next <= unsigned(sc_mem_in.rd_data(SC_ADDR_SIZE-1 downto 0));
					state_next <= iast4;
				else
					state_next <= iast3;
				end if;
				
			when iast4 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= bypass;
				mem_out.bsy <= '1';

				-- check bounds and trigger write only if it's ok
				if index_reg >= size_reg then
					ab_exc_next <= '1';
					state_next <= last;
				else
					sc_mem_out.wr <= '1';
					state_next <= last_atomic;
				end if;

			when cp0 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= bypass;
				mem_out.bsy <= '1';

				sc_mem_out.rd <= '1';
				
				pos_next <= addr_reg;				
				offset_next <= unsigned(bin(SC_ADDR_SIZE-1 downto 0)) - base_reg;
				state_next <= cp1;

			when cp1 =>
				sc_mem_out.atomic <= '1';
				mem_out.bsy <= '1';

				if sc_mem_in.rdy_cnt = 0 then					
					addr_next <= pos_reg+offset_reg;
					value_next <= unsigned(sc_mem_in.rd_data);
					pos_next <= pos_reg+1;
					state_next <= cp2;
				end if;

			when cp2 =>
				sc_mem_out.atomic <= '1';
				sc_mem_out.cache <= bypass;
				mem_out.bsy <= '1';

				sc_mem_out.wr <= '1';
				state_next <= last_atomic;

				-- wait for the last ack
			when last =>
				-- either 1 or 0					
				if sc_mem_in.rdy_cnt(1)='0' then
					state_next <= idl;
				end if;
				
			when last_atomic =>
				sc_mem_out.atomic <= '1';

				-- either 1 or 0					
				if sc_mem_in.rdy_cnt(1)='0' then
					state_next <= idl;
				end if;
				
		end case;
	end process;

end rtl;
