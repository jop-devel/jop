--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)
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
--	acache.vhd
--
--	Array cache
--
--	2011-09-07  first version adapted from ocache
--

--
--	package for array cache types
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;

package ac_types is

	type ac_tag_in_type is record
		invalidate	: std_logic; -- flush the cache
		addr        : std_logic_vector(ACACHE_ADDR_BITS-1 downto 0);
		index		: std_logic_vector(ACACHE_MAX_INDEX_BITS-1 downto 0);
		wraddr      : std_logic_vector(ACACHE_ADDR_BITS-1 downto 0);
		wrline      : unsigned(ACACHE_WAY_BITS-1 downto 0);
		wrindex		: std_logic_vector(ACACHE_MAX_INDEX_BITS-1 downto 0);
		wr          : std_logic; -- update the tag memory
		inc_nxt     : std_logic; -- increment nxt pointer (was a miss) 
	end record;

	type ac_tag_out_type is record
		hit         : std_logic; -- field hit
		hit_tag		: std_logic; -- tag hit
		hit_line    : unsigned(ACACHE_WAY_BITS-1 downto 0); -- cache line on a hit
		nxt_line    : unsigned(ACACHE_WAY_BITS-1 downto 0); -- next cache line to use on a miss
		
	end record;
end ac_types;


--
--	Tag memory for a full associative cache with FIFO replacement
--
--	Used by the array cache, should also be used by the general
--	SimpCon based fifo cache and the RTTM buffer (use invalidate
--	for transaction start).
--
--	Should also be used by the method cache as it is smaller.
--
--	Could be reduced by a few bits, as the handle area uses some words
--	and lower bits need not be compared. However, take care as the handle
--	area for strings is shorter!
--
--	Cyclone 1 fmax (with 24 address bits):
--		 8 entries 202 MHz
--		16 entries 168 MHz
--		32 entries 150 MHz
--		64 entries 123 MHz
--		128 entries 104 MHz
--		

-- TODO: this should be shared by O$, A$, and M$
-- but it is a little bit different between those three

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.ac_types.all;

entity ac_tag is 
	port (
		clk, reset	: in std_logic;
		tag_in		: in ac_tag_in_type;
		tag_out		: out ac_tag_out_type
		);
end ac_tag;

architecture rtl of ac_tag is 

	constant line_cnt: integer := 2**ACACHE_WAY_BITS;
	constant FIELD_CNT: integer := 2**ACACHE_FIELD_BITS;
	

	signal line: unsigned(ACACHE_WAY_BITS-1 downto 0);

	-- part of index that is in the tag memory
	signal idx_upper, wr_idx_upper: std_logic_vector(ACACHE_MAX_INDEX_BITS-FIELD_CNT-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to line_cnt-1) of std_logic_vector(ACACHE_ADDR_BITS-1 downto 0);
	signal tag: tag_array;
	type tag_idx_array is array (0 to line_cnt-1) of std_logic_vector(ACACHE_MAX_INDEX_BITS-FIELD_CNT-1 downto 0);
	signal tag_idx: tag_idx_array;
	
	type valid_array is array (0 to line_cnt-1) of std_logic;
	signal valid: valid_array;

	-- pointer to next block to be used on a miss
	signal nxt: unsigned(ACACHE_WAY_BITS-1 downto 0);


begin

	tag_out.hit_line <= line;
	tag_out.nxt_line <= nxt;
	
	idx_upper <= tag_in.index(ACACHE_MAX_INDEX_BITS-1 downto FIELD_CNT);
	wr_idx_upper <= tag_in.wrindex(ACACHE_MAX_INDEX_BITS-1 downto FIELD_CNT);

	process(tag, tag_idx, tag_in.addr, idx_upper, valid)
		variable h: std_logic_vector(line_cnt-1 downto 0);
		variable h_or: std_logic;
		variable th: std_logic_vector(line_cnt-1 downto 0);
		variable th_or: std_logic;
		variable n: unsigned(ACACHE_WAY_BITS-1 downto 0);
		variable v: std_logic;
	begin

		-- hit detection
		h := (others => '0');
		th := (others => '0');
		for i in 0 to line_cnt-1 loop
			v := valid(i);
			if tag(i)=tag_in.addr and valid(i)='1' then
				-- tag hit, might be useful for handle and len caching
				th(i) := '1';
				-- field hit
				if tag_idx(i)=idx_upper then
					h(i) := '1';
				end if;
			end if;
		end loop;

		h_or := '0';
		th_or := '0';
		for i in 0 to line_cnt-1 loop
			h_or := h_or or h(i);
			th_or := th_or or th(i);
		end loop;
		tag_out.hit <= h_or;
		tag_out.hit_tag <= th_or;
		
		-- encoder without priority
		line <= (others => '0');
		for i in 0 to ACACHE_WAY_BITS-1 loop
			for j in 0 to line_cnt-1 loop
				n := to_unsigned(j, ACACHE_WAY_BITS);
				if n(i)='1' and th(j)='1' then
					line(i) <= '1';
				end if;
			end loop;
		end loop;

	end process;

	process(clk, reset)
	begin

		if reset='1' then

			nxt <= (others => '0');
			
			for i in 0 to line_cnt-1 loop
				tag(i) <= (others => '0');
				tag_idx(i) <= (others => '0');
				valid(i) <= '0';
			end loop;

		elsif rising_edge(clk) then

			-- update tag memory when data is available
			if tag_in.wr='1' then
				tag(to_integer(tag_in.wrline)) <= tag_in.wraddr;
				tag_idx(to_integer(tag_in.wrline)) <= wr_idx_upper;
				valid(to_integer(tag_in.wrline)) <= '1'; 
			end if;
			if tag_in.inc_nxt='1' then
				nxt <= nxt + 1;
			end if;
			
			if tag_in.invalidate = '1' then
				nxt <= (others => '0'); -- shall we reset nxt?
				for i in 0 to line_cnt-1 loop
					valid(i) <= '0';
				end loop;	
			end if;
			
		end if;
	end process;

end;

--
--	Array cache
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.jop_types.all;
use work.sc_pack.all;
use work.ac_types.all;

entity acache is

port (
	clk, reset	: in std_logic;

	acin	: in acache_in_type;
	acout	: out acache_out_type
);
end acache;

architecture rtl of acache is

	constant INDEX_ZERO: std_logic_vector(ACACHE_MAX_INDEX_BITS-ACACHE_FIELD_BITS-1 downto 0)
		:= (others => '0');
	signal acin_reg: acache_in_type;
	signal line_reg: unsigned(ACACHE_WAY_BITS-1 downto 0);
	signal hit_reg, hit_tag_reg: std_logic;
	signal inc_nxt_reg: std_logic;
	signal idx_reg: std_logic_vector(ACACHE_FIELD_BITS-1 downto 0);

	signal cacheable, cacheable_reg: std_logic;

	signal update_cache: std_logic;
	
	signal chk_gf_dly: std_logic;
	signal ram_dout_store: std_logic_vector(31 downto 0);
	
	
	signal ac_tag_in: ac_tag_in_type;
	signal ac_tag_out: ac_tag_out_type;
	

	-- RAM signals
	constant nwords : integer := 2 ** (ACACHE_WAY_BITS+ACACHE_FIELD_BITS);
	type ram_type is array(0 to nwords-1) of std_logic_vector(31 downto 0);

	signal ram : ram_type;
	signal ram_din, ram_dout : std_logic_vector(31 downto 0);
	signal ram_wraddr: unsigned(ACACHE_WAY_BITS+ACACHE_FIELD_BITS-1 downto 0);

begin

	tag: entity work.ac_tag
		port map(
			clk => clk,
			reset => reset,
			tag_in => ac_tag_in,
			tag_out => ac_tag_out
		);
		
	ac_tag_in.addr <= acin.handle(ACACHE_ADDR_BITS-1 downto 0);
	ac_tag_in.index <= acin.index;
	
	ac_tag_in.wraddr <= acin_reg.handle;
	ac_tag_in.wrindex <= acin_reg.index;
	ac_tag_in.wrline <= line_reg;
	ac_tag_in.wr <= update_cache;
	
-- check for cachable fields 
process(acin, hit_reg, hit_tag_reg, inc_nxt_reg, cacheable_reg)
begin

	-- cachable useless here - remove it
	cacheable <= '1';

	-- TODO: tag update only needed on a (tag) miss, not on a wr_pf
	-- valid update needed by multi word cache line
	-- update_cache is used by the data memory too
	-- we could split into: update_data, update_tag, update_valid
	update_cache <= '0';
	if acin.wr_ial='1' or (acin.wr_ias='1' and hit_tag_reg='1') then
		-- update only on valid index
		if cacheable_reg='1' then
			update_cache <= '1';
		end if;
	end if;
	-- increment nxt pointer on a miss
	-- TODO: change when caching several fields
	--		should be ok now as it is in inc_nxt_reg considered
	-- TODO: adaption needed for write allocate
	ac_tag_in.inc_nxt <= '0';
	if acin.wr_ial='1' and cacheable_reg='1' then -- that's a getfield miss (now)
		ac_tag_in.inc_nxt <= inc_nxt_reg;
	end if;
end process;

-- Cache update:
--	iaload => there was a miss, use nxt and increment
--		!! with several fields it could be a handle (tag) hit
--			=> no nxt increment, use line from tag hit
--	iastore => was a hit, use line from hit and don't increment nxt
--

	acout.hit <= ac_tag_out.hit and cacheable and USE_ACACHE;

	-- Make sure that data stays valid after a hit till
	-- the next request
	-- This should be ok	
	-- that's very late. Can we do better on a hit?
	acout.dout <= ram_dout_store;


-- main signals:
--
--	chk_ial, chk_ias: hit detection on iaload/store - also on *non-cached* fields
--		=> no cache state update at this stage!
--	wr_ial: set when the cache should be updated on a missed field
--		will fill a complete cache line with individual wr_gf signals
--	wr_ias: set on a cacheable array store, but also on a missed write
--		=> decide on write allocation in the cache depending on the
--		former chk_pf (hit_reg)
--
--	On non-cachable access (HWO, native get/putField) no wr_gf or
--	wr_pf is generated (check is in mem_sc). TODO: need ot be checked
--	for array access

process(clk, reset)
begin
	if reset='1' then
		ac_tag_in.invalidate <= '1';
		chk_gf_dly <= '0';
		hit_reg <= '0';
		hit_tag_reg <= '0';
		cacheable_reg <= '0';
		line_reg <= (others => '0');
		ram_dout_store <= (others => '0');
		idx_reg <= (others => '0');
	elsif rising_edge(clk) then

		-- store data from RAM that is one cycle later		
		chk_gf_dly <= acin.chk_ial;
		if chk_gf_dly='1' then
			ram_dout_store <= ram_dout;
		end if;
		
		-- remember handle, index, and if it was a hit
		if acin.chk_ial='1' or acin.chk_ias='1' then
			hit_reg <= ac_tag_out.hit and cacheable;
			hit_tag_reg <= ac_tag_out.hit_tag and cacheable;
			acin_reg <= acin;
			cacheable_reg <= cacheable;
		end if;
		if acin.chk_ias='1' then
			idx_reg <= acin.index(ACACHE_FIELD_BITS-1 downto 0);
		end if;
		-- decide on line address:
		-- chk_gf will result (at the moment) in a wr_gf on
		-- a miss
		if acin.chk_ial='1' then
			if ac_tag_out.hit_tag='1' then
				line_reg <= ac_tag_out.hit_line;
				inc_nxt_reg <= '0';
			else
				line_reg <= ac_tag_out.nxt_line;
				-- nxt increment only on a tag miss
				inc_nxt_reg <= '1';
			end if;
			idx_reg <= (others => '0');
		end if;
		-- increment field address
		if acin.wr_ial='1' then
			idx_reg <= std_logic_vector(unsigned(idx_reg)+1);
		end if;
		-- putfield update only on a hit
		-- TODO: needs adaption for write allocate
		if acin.chk_ias='1' then
			line_reg <= ac_tag_out.hit_line;
			inc_nxt_reg <= '0';
		end if;
		-- invalidate the cache (e.g. on jopsys_get/putfield)
		-- TODO: also on monitorenter (or exit?) and GC start?
		if acin.inval='1' then
			ac_tag_in.invalidate <= '1';
		else
			ac_tag_in.invalidate <= '0';
		end if;
		
	end if;
end process;

-- RAM for the ocache

	ram_wraddr <= line_reg & unsigned(idx_reg);
-- RAM write data mux
process(acin)
begin
	-- update on a iaload miss
	if acin.wr_ial='1' then
		ram_din <= acin.ial_val;
	-- update on a iastore hit
	else
		ram_din <= acin.ias_val;
	end if;
end process;

-- RAM
process (clk)
begin
	if rising_edge(clk) then
		ram_dout <= ram(to_integer(unsigned(ac_tag_out.hit_line &
						unsigned(acin.index(ACACHE_FIELD_BITS-1 downto 0)))));
		if update_cache='1' then
			ram(to_integer(unsigned(ram_wraddr))) <= ram_din;
		end if;
	end if;
end process;


end rtl;
