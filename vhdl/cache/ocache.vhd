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
		index		: std_logic_vector(OCACHE_MAX_INDEX_BITS-1 downto 0);
		wraddr      : std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
		wr          : std_logic; -- update the tag memory
	end record;

	type oc_tag_out_type is record
		hit         : std_logic; -- field hit
--		hit_tag		: out std_logic; -- just tag hit
		hit_line    : unsigned(OCACHE_WAY_BITS-1 downto 0); -- cache line on a hit
		miss_line    : unsigned(OCACHE_WAY_BITS-1 downto 0); -- cache line on a hit
		
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

	signal hit: std_logic;
	signal line: unsigned(OCACHE_WAY_BITS-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to line_cnt-1) of std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
	signal tag: tag_array;
	
	signal valid: std_logic_vector(line_cnt-1 downto 0);

	-- pointer to next block to be used on a miss
	signal nxt: unsigned(OCACHE_WAY_BITS-1 downto 0);


begin

	tag_out.hit <= hit;
	tag_out.hit_line <= line;
	tag_out.miss_line <= nxt;

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
		-- hack for index
		if tag_in.index(4 downto 0) = "00000" then
			hit <= h_or;
		else
			hit <= '0';
		end if;

		-- encoder without priority
		line <= (others => '0');
		for i in 0 to OCACHE_WAY_BITS-1 loop
			for j in 0 to line_cnt-1 loop
				n := to_unsigned(j, OCACHE_WAY_BITS);
				if n(i)='1' and h(j)='1' then
					line(i) <= '1';
				end if;
			end loop;
		end loop;

	end process;

	process(clk, reset)
	begin

		if reset='1' then

			nxt <= (others => '0');
			valid <= (others => '0');
			
			for i in 0 to line_cnt-1 loop
				tag(i) <= (others => '0');
			end loop;

		elsif rising_edge(clk) then

			-- update tag memory when data is available
			if tag_in.wr='1' then
--				tag(to_integer(wrline)) <= wraddr;
--				valid(to_integer(wrline)) <= '1';
				tag(to_integer(nxt)) <= tag_in.wraddr;
				-- TODO: index ignored
				valid(to_integer(nxt)) <= '1';
				nxt <= nxt + 1;
			end if;
			
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
use work.oc_types.all;

entity ocache is

port (
	clk, reset	: in std_logic;

	ocin	: in ocache_in_type;
	ocout	: out ocache_out_type
);
end ocache;

architecture rtl of ocache is

	signal ocin_reg: ocache_in_type;
	signal miss_line_reg, hit_line_reg: unsigned(OCACHE_WAY_BITS-1 downto 0);
	signal hit_reg: std_logic;
	signal update_cache: std_logic;
	
	signal chk_gf_dly: std_logic;
	signal ram_dout_store: std_logic_vector(31 downto 0);
	
	
	signal oc_tag_in: oc_tag_in_type;
	signal oc_tag_out: oc_tag_out_type;
	

	-- RAM signals
	subtype word is std_logic_vector(31 downto 0);
	constant nwords : integer := 2 ** (OCACHE_WAY_BITS+0);
	type ram_type is array(0 to nwords-1) of word;

	signal ram : ram_type;
	signal ram_din, ram_dout : std_logic_vector(31 downto 0);
	signal ram_wraddr: unsigned(OCACHE_WAY_BITS-1 downto 0);
	-- TODO: ram output needs to stay longer than single cycle
	


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
	-- TODO: use field index, now it is hard coded to 0
	-- oc_tag_in.wrindex <= ocin_reg.index;
	oc_tag_in.wr <= update_cache;
	
-- TODO: ***** the following is needed for single field OC, but its not working!! ****
process(ocin, ocin_reg, hit_reg)
begin
	update_cache <= '0';
	if ocin.wr_gf='1' or (ocin.wr_pf='1' and hit_reg='1') then
		-- index hack
		if ocin_reg.index(4 downto 0) = "00000" then
			update_cache <= '1';
		end if;
	end if;
end process;


	ocout.hit <= oc_tag_out.hit and USE_OCACHE;

-- TODO: make sure that data stays valid after a hit till
-- the next request
-- Is this the solution?	
--	ocout.dout <= ram_dout;

-- that looks like a solution, but is it needed?
-- the ocache is still not working :-(

process(ocin.chk_gf, ram_dout, ram_dout_store)
begin
	if ocin.chk_gf='1' then
		ocout.dout <= ram_dout;
	else
		ocout.dout <= ram_dout_store;
	end if;
end process;
-- main signals:
--
--	chk_gf, chk_pf: hit detection on get/putfield - also on *non-cached* fields
--		=> no cache state update at this stage!
--	wr_gf: set when the cache should be updated on a missed getfield
--	wr_pf: set on a cacheable putfield, but also on a missed write
--		=> decide on write allocation in the cache depending on the
--		former chk_pf (hit_reg)

process(clk, reset)
begin
	if reset='1' then
		oc_tag_in.invalidate <= '1';
		chk_gf_dly <= '0';
		hit_reg <= '0';
		hit_line_reg <= (others => '0');
		miss_line_reg <= (others => '0');
		ram_dout_store <= (others => '0');
	elsif rising_edge(clk) then
		-- shuldn't we assign default values here to all signals?
		
		chk_gf_dly <= ocin.chk_gf;
		if ocin.chk_gf='1' then
			ram_dout_store <= ram_dout;
		end if;
		-- remember handle, index, and if it was a hit
		if ocin.chk_gf='1' or ocin.chk_pf='1' then
			hit_reg <= oc_tag_out.hit;
			-- could be simpler, as we alread now if it
			-- was a gf hit or miss, or pf hit
			hit_line_reg <= oc_tag_out.hit_line;
			miss_line_reg <= oc_tag_out.miss_line;
			ocin_reg <= ocin;
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

-- RAM write mux
process(ocin, miss_line_reg, hit_line_reg)
begin
	-- update on a getfield miss
	if ocin.wr_gf='1' then
		ram_din <= ocin.gf_val;
		ram_wraddr <= miss_line_reg;
	-- update on a putfield hit
	else
		ram_din <= ocin.pf_val;
		ram_wraddr <= hit_line_reg;
	end if;
end process;

-- RAM
process (clk)
begin
	if rising_edge(clk) then
		ram_dout <= ram(to_integer(unsigned(oc_tag_out.hit_line)));
		if update_cache='1' then
			ram(to_integer(unsigned(ram_wraddr))) <= ram_din;
		end if;
	end if;
end process;


end rtl;
