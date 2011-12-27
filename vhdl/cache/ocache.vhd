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
--	ocache.vhd
--
--	Object cache
--
--	2009-11-11  first version
--	2009-11-28	single entry works
--	2011-01-06	multiple objects, single field (index 0) per object works
--		Simulation and usbmin is ok, usb100 crashes -- a fmax problem?
--		without GC redirection in mem_sc, usb100 is ok (GC crashes)
--	2011-01-07	multiple objects and fields works
--		TODO: check with ModelSim if all is ok
--

--
--	package for object cache types
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;

package oc_types is

	type oc_tag_in_type is record
		invalidate	: std_logic; -- flush the cache
		addr        : std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
		index		: std_logic_vector(OCACHE_MAX_INDEX_BITS-1 downto 0);
		wraddr      : std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
		wrline      : unsigned(OCACHE_WAY_BITS-1 downto 0);
		wrindex		: std_logic_vector(OCACHE_MAX_INDEX_BITS-1 downto 0);
		wr          : std_logic; -- update the tag memory
		inc_nxt     : std_logic; -- increment nxt pointer (was a miss) 
	end record;

	type oc_tag_out_type is record
		hit         : std_logic; -- field hit
		hit_tag		: std_logic; -- tag hit
		hit_line    : unsigned(OCACHE_WAY_BITS-1 downto 0); -- cache line on a hit
		nxt_line    : unsigned(OCACHE_WAY_BITS-1 downto 0); -- next cache line to use on a miss
		
	end record;
end oc_types;


--
--	Tag memory for a full associative cache with FIFO replacement
--
--	Used by the object cache, should also be used by the general
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

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.oc_types.all;

entity oc_tag is 
	port (
		clk, reset	: in std_logic;
		tag_in		: in oc_tag_in_type;
		tag_out		: out oc_tag_out_type
		);
end oc_tag;

architecture rtl of oc_tag is 

	constant line_cnt: integer := 2**OCACHE_WAY_BITS;
	constant FIELD_CNT: integer := 2**OCACHE_INDEX_BITS;
	constant VALID_ZERO: std_logic_vector(FIELD_CNT-1 downto 0) := (others => '0');
	

	signal line: unsigned(OCACHE_WAY_BITS-1 downto 0);
	signal idx, wridx: unsigned(OCACHE_INDEX_BITS-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to line_cnt-1) of std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
	signal tag: tag_array;
	
	type valid_array is array (0 to line_cnt-1) of std_logic_vector(FIELD_CNT-1 downto 0);
	signal valid: valid_array;

	-- pointer to next block to be used on a miss
	signal nxt: unsigned(OCACHE_WAY_BITS-1 downto 0);


begin

	tag_out.hit_line <= line;
	tag_out.nxt_line <= nxt;
	
	idx <= unsigned(tag_in.index(OCACHE_INDEX_BITS-1 downto 0));
	wridx <= unsigned(tag_in.wrindex(OCACHE_INDEX_BITS-1 downto 0));

	process(tag, tag_in.addr, valid, idx)
		variable h: std_logic_vector(line_cnt-1 downto 0);
		variable h_or: std_logic;
		variable th: std_logic_vector(line_cnt-1 downto 0);
		variable th_or: std_logic;
		variable n: unsigned(OCACHE_WAY_BITS-1 downto 0);
		variable v: std_logic_vector(FIELD_CNT-1 downto 0);
	begin

		-- hit detection
		-- TODO: tag and valid detection
		h := (others => '0');
		th := (others => '0');
		for i in 0 to line_cnt-1 loop
			-- TODO: we can remove v again and
			-- use valid(i)(to_integer(idx))
			v := valid(i);
			if tag(i)= tag_in.addr then
				-- hit on valid field
				if v(to_integer(idx))='1' then
					h(i) := '1';
				end if;
				-- tag is valid if any field is valid
				if v /= VALID_ZERO then
					th(i) := '1';
				end if;
			end if;
		end loop;

		h_or := '0';
		th_or := '0';
		for i in 0 to line_cnt-1 loop
			h_or := h_or or h(i);
			th_or := th_or or th(i);
		end loop;
		-- ignore uncacheable index values, it is checked in ocache
		tag_out.hit <= h_or;
		tag_out.hit_tag <= th_or;
		
		-- check if index is in the cachable area
--		if tag_in.index(OCACHE_MAX_INDEX_BITS-1 downto OCACHE_INDEX_BITS) = (others => '0') then
--			hit <= h_or;
--		else
--			hit <= '0';
--		end if;

		-- encoder without priority
		line <= (others => '0');
		for i in 0 to OCACHE_WAY_BITS-1 loop
			for j in 0 to line_cnt-1 loop
				n := to_unsigned(j, OCACHE_WAY_BITS);
				if n(i)='1' and th(j)='1' then
					line(i) <= '1';
				end if;
			end loop;
		end loop;

	end process;

	process(clk, reset)
		variable v: std_logic_vector(FIELD_CNT-1 downto 0);
	begin

		if reset='1' then

			nxt <= (others => '0');
			
			for i in 0 to line_cnt-1 loop
				tag(i) <= (others => '0');
				valid(i) <= (others => '0');
			end loop;

		elsif rising_edge(clk) then

			-- update tag memory when data is available
			if tag_in.wr='1' then
				tag(to_integer(tag_in.wrline)) <= tag_in.wraddr;
				v := valid(to_integer(tag_in.wrline));
				-- only reset other valid signals on a new tag
				if tag_in.inc_nxt='1' then
					v := (others => '0');
				end if;
				v(to_integer(wridx)) := '1';
				valid(to_integer(tag_in.wrline)) <= v; 
			end if;
			if tag_in.inc_nxt='1' then
				nxt <= nxt + 1;
			end if;
			
			if tag_in.invalidate = '1' then
				nxt <= (others => '0'); -- shall we reset nxt?
				for i in 0 to line_cnt-1 loop
					valid(i) <= (others => '0');
				end loop;	
			end if;
			
		end if;
	end process;

end;

--
--	Object cache
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.jop_types.all;
use work.sc_pack.all;
use work.oc_types.all;

entity ocache is

port (
	clk, reset	: in std_logic;

	ocin	: in ocache_in_type;
	ocout	: out ocache_out_type
);
end ocache;

architecture rtl of ocache is

	constant INDEX_ZERO: std_logic_vector(OCACHE_MAX_INDEX_BITS-OCACHE_INDEX_BITS-1 downto 0)
		:= (others => '0');
	signal ocin_reg: ocache_in_type;
	signal line_reg: unsigned(OCACHE_WAY_BITS-1 downto 0);
	signal hit_reg, hit_tag_reg: std_logic;
	signal inc_nxt_reg: std_logic;

	signal cacheable, cacheable_reg: std_logic;

	signal update_cache: std_logic;
	
	signal chk_gf_dly: std_logic;
	signal ram_dout_store: std_logic_vector(31 downto 0);
	
	
	signal oc_tag_in: oc_tag_in_type;
	signal oc_tag_out: oc_tag_out_type;
	

	-- RAM signals
	constant nwords : integer := 2 ** (OCACHE_WAY_BITS+OCACHE_INDEX_BITS);
	type ram_type is array(0 to nwords-1) of std_logic_vector(31 downto 0);

	signal ram : ram_type;
	signal ram_din, ram_dout : std_logic_vector(31 downto 0);
	signal ram_wraddr: unsigned(OCACHE_WAY_BITS+OCACHE_INDEX_BITS-1 downto 0);

begin

	tag: entity work.oc_tag
		port map(
			clk => clk,
			reset => reset,
			tag_in => oc_tag_in,
			tag_out => oc_tag_out
		);
		
	oc_tag_in.addr <= ocin.handle(OCACHE_ADDR_BITS-1 downto 0);
	oc_tag_in.index <= ocin.index;
	
	oc_tag_in.wraddr <= ocin_reg.handle;
	oc_tag_in.wrindex <= ocin_reg.index;
	oc_tag_in.wrline <= line_reg;
	oc_tag_in.wr <= update_cache;
	
-- check for cachable fields 
process(ocin, hit_reg, hit_tag_reg, inc_nxt_reg, cacheable_reg)
begin

	cacheable <= '0';
	if ocin.index(OCACHE_MAX_INDEX_BITS-1 downto OCACHE_INDEX_BITS) = INDEX_ZERO then
		cacheable <= '1';
	end if;

	-- TODO: tag update only needed on a (tag) miss, not on a wr_pf
	-- valid update needed by multi word cache line
	-- update_cache is used by the data memory too
	-- we could split into: update_data, update_tag, update_valid
	update_cache <= '0';
	if ocin.wr_gf='1' or (ocin.wr_pf='1' and hit_tag_reg='1') then
		-- update only on valid index
		if cacheable_reg='1' then
			update_cache <= '1';
		end if;
	end if;
	-- increment nxt pointer on a miss
	-- TODO: change when caching several fields
	--		should be ok now as it is in inc_nxt_reg considered
	-- TODO: adaption needed for write allocate
	oc_tag_in.inc_nxt <= '0';
	if ocin.wr_gf='1' and cacheable_reg='1' then -- that's a getfield miss (now)
		oc_tag_in.inc_nxt <= inc_nxt_reg;
	end if;
end process;

-- Cache update:
--	getfield => there was a miss, use nxt and increment
--		!! with several fields it could be a handle (tag) hit
--			=> no nxt increment, use line from tag hit
--	putfield => was a hit, use line from hit and don't increment nxt
--

	ocout.hit <= oc_tag_out.hit and cacheable and USE_OCACHE;

	-- Make sure that data stays valid after a hit till
	-- the next request
	-- This should be ok	
	-- that's very late. Can we do better on a hit?
	ocout.dout <= ram_dout_store;


-- main signals:
--
--	chk_gf, chk_pf: hit detection on get/putfield - also on *non-cached* fields
--		=> no cache state update at this stage!
--	wr_gf: set when the cache should be updated on a missed getfield
--	wr_pf: set on a cacheable putfield, but also on a missed write
--		=> decide on write allocation in the cache depending on the
--		former chk_pf (hit_reg)
--
--	On non-cachable access (HWO, native get/putField) no wr_gf or
--	wr_pf is generated (check is in mem_sc).

process(clk, reset)
begin
	if reset='1' then
		oc_tag_in.invalidate <= '1';
		chk_gf_dly <= '0';
		hit_reg <= '0';
		hit_tag_reg <= '0';
		cacheable_reg <= '0';
		line_reg <= (others => '0');
		ram_dout_store <= (others => '0');
	elsif rising_edge(clk) then

		-- store data from RAM that is one cycle later		
		chk_gf_dly <= ocin.chk_gf;
		if chk_gf_dly='1' then
			ram_dout_store <= ram_dout;
		end if;
		
		-- remember handle, index, and if it was a hit
		if ocin.chk_gf='1' or ocin.chk_pf='1' then
			hit_reg <= oc_tag_out.hit and cacheable;
			hit_tag_reg <= oc_tag_out.hit_tag and cacheable;
			ocin_reg <= ocin;
			cacheable_reg <= cacheable;
		end if;
		-- decide on line address:
		-- chk_gf will result (at the moment) in a wr_gf on
		-- a miss
		if ocin.chk_gf='1' then
			if oc_tag_out.hit_tag='1' then
				line_reg <= oc_tag_out.hit_line;
				inc_nxt_reg <= '0';
			else
				line_reg <= oc_tag_out.nxt_line;
				-- nxt increment only on a tag miss
				inc_nxt_reg <= '1';
			end if;
		end if;
		-- putfield update only on a hit
		-- TODO: needs adaption for write allocate
		if ocin.chk_pf='1' then
			line_reg <= oc_tag_out.hit_line;
			inc_nxt_reg <= '0';
		end if;
		-- invalidate the cache (e.g. on jopsys_get/putfield)
		-- TODO: also on monitorenter (or exit?) and GC start?
		if ocin.inval='1' then
			oc_tag_in.invalidate <= '1';
		else
			oc_tag_in.invalidate <= '0';
		end if;
		
	end if;
end process;

-- RAM for the ocache

	ram_wraddr <= line_reg & unsigned(ocin_reg.index(OCACHE_INDEX_BITS-1 downto 0));
-- RAM write data mux
process(ocin)
begin
	-- update on a getfield miss
	if ocin.wr_gf='1' then
		ram_din <= ocin.gf_val;
	-- update on a putfield hit
	else
		ram_din <= ocin.pf_val;
	end if;
end process;

-- RAM
process (clk)
begin
	if rising_edge(clk) then
		ram_dout <= ram(to_integer(unsigned(oc_tag_out.hit_line &
						unsigned(ocin.index(OCACHE_INDEX_BITS-1 downto 0)))));
		if update_cache='1' then
			ram(to_integer(unsigned(ram_wraddr))) <= ram_din;
		end if;
	end if;
end process;


end rtl;
