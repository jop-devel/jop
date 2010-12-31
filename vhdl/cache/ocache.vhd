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
--		wraddr      : std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
		wr          : std_logic; -- update the tag memory
	end record;

	type oc_tag_out_type is record
		hit         : std_logic; -- field hit
--		hit_tag		: out std_logic; -- just tag hit
		line        : unsigned(OCACHE_WAY_BITS-1 downto 0); -- cache line on a hit
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

	signal line_reg, next_line: unsigned(OCACHE_WAY_BITS-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to line_cnt-1) of std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
	signal tag: tag_array;
	
	signal valid: std_logic_vector(line_cnt-1 downto 0);

	-- pointer to next block to be used on a miss
	signal nxt: unsigned(OCACHE_WAY_BITS-1 downto 0);

	signal hit_reg, next_hit: std_logic;

begin

	tag_out.hit <= hit_reg;
	tag_out.line <= line_reg;

	process(tag, tag_in.addr, valid)
		variable h: std_logic_vector(line_cnt-1 downto 0);
		variable h_or: std_logic;
		variable n: unsigned(OCACHE_WAY_BITS-1 downto 0);
	begin

		-- hit detection
		h := (others => '0');
		for i in 0 to line_cnt-1 loop
			if tag(i)= tag_in.addr and valid(i)='1' then
				h(i) := '1';
			end if;
		end loop;

		h_or := '0';
		for i in 0 to line_cnt-1 loop
			h_or := h_or or h(i);
		end loop;
		next_hit <= h_or;

		-- encoder without priority
		next_line <= (others => '0');
		for i in 0 to OCACHE_WAY_BITS-1 loop
			for j in 0 to line_cnt-1 loop
				n := to_unsigned(j, OCACHE_WAY_BITS);
				if n(i)='1' and h(j)='1' then
					next_line(i) <= '1';
				end if;
			end loop;
		end loop;

	end process;

	process(clk, reset)
	begin

		if reset='1' then

			nxt <= (others => '0');
			valid <= (others => '0');
			hit_reg <= '0';
			
			for i in 0 to line_cnt-1 loop
				tag(i) <= (others => '0');
			end loop;

		elsif rising_edge(clk) then

			-- update tag memory in the next cycle
			if tag_in.wr='1' then
--				tag(to_integer(wrline)) <= wraddr;
--				valid(to_integer(wrline)) <= '1';
				tag(to_integer(nxt)) <= tag_in.addr;
				valid(to_integer(nxt)) <= '1';
				nxt <= nxt + 1;
			end if;

			hit_reg <= next_hit;
			line_reg <= next_line;
			
			if tag_in.invalidate = '1' then
				nxt <= (others => '0'); -- shall we reset nxt?
				valid <= (others => '0');			
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

entity ocache is
generic (size_bits : integer);

port (
	clk, reset	: in std_logic;

	ocin	: in ocache_in_type;
	ocout	: out ocache_out_type
);
end ocache;

architecture rtl of ocache is

	signal ocin_reg : ocache_in_type;

	signal valid : std_logic;
	signal tag : std_logic_vector(CACHE_ADDR_SIZE-1 downto 0);
	signal index : std_logic_vector(MAX_OBJECT_SIZE-1 downto 0);
	signal data : std_logic_vector(31 downto 0);

	signal hit, hit_reg : std_logic;

begin


-- TODO: make sure that data stays valid after a hit till
-- the next request

process(ocin, data, tag, index, valid)
begin
	hit <= '0';
	ocout.dout <= data;
	if tag=ocin.handle and index=ocin.index and valid='1' then
		hit <= '1';
	end if;
end process;

	ocout.hit <= hit and USE_OCACHE;
	
-- main signals:
--
--	chk_gf, chk_pf: hit detection on get/putfield - also on *non-cached* fields
--		=> no cache state update at this stage!
--	wr_gf: set when cached should be updated on a missed getfield
--	wr_pf: set on a cacheable putfield, but also on a missed write
--		=> decide on write allocation in the cache depending on the
--		former chk_pf (hit_reg)

process(clk, reset)
begin
	if reset='1' then
		valid <= '0';
	elsif rising_edge(clk) then
		-- remember handle, index, and if it was a hit
		if ocin.chk_gf='1' or ocin.chk_pf='1' then
			hit_reg <= hit;
			ocin_reg <= ocin;
		end if;
		-- update on a getfield miss
		if ocin.wr_gf='1' then
			valid <= '1';
			tag <= ocin_reg.handle;
			index <= ocin_reg.index;
			data <= ocin.gf_val;
		end if;
		-- update on a putfield hit
		-- no write allocaton, just update cached values
		if ocin.wr_pf='1' and hit_reg='1' then
			data <= ocin.pf_val;
		end if;
		-- invalidate the cache (e.g. on jopsys_get/putfield)
		-- TODO: also on monitorenter (or exit?) and GC start?
		if ocin.inval='1' then
			valid <= '0';
		end if;
	end if;
end process;

end rtl;
