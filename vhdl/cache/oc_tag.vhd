--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009-2010, Martin Schoeberl (martin@jopdesign.com)
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

entity oc_tag is 
	generic (
		addr_width	: integer;		-- address bits of cachable memory
		way_bits	: integer		-- 2**index_bits is number of entries
		);
	port (
		clk, reset	: in std_logic;
		invalidate	: in std_logic; -- flush the cache
		addr        : in std_logic_vector(addr_width-1 downto 0);
--		wraddr      : in std_logic_vector(addr_width-1 downto 0);
		wr          : in std_logic; -- update the tag memory
		hit         : out std_logic; -- field hit
--		hit_tag		: out std_logic; -- just tag hit
		line        : out unsigned(way_bits-1 downto 0) -- cache line on a hit
		);
end oc_tag;

architecture rtl of oc_tag is 

	constant line_cnt: integer := 2**way_bits;

	signal line_reg, next_line: unsigned(way_bits-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to line_cnt-1) of std_logic_vector(addr_width-1 downto 0);
	signal tag: tag_array;
	
	signal valid: std_logic_vector(line_cnt-1 downto 0);

	-- pointer to next block to be used on a miss
	signal nxt: unsigned(way_bits-1 downto 0);

	signal hit_reg, next_hit: std_logic;

begin

	hit <= hit_reg;
	line <= line_reg;

	process(tag, addr, valid)
		variable h: std_logic_vector(line_cnt-1 downto 0);
		variable h_or: std_logic;
		variable n: unsigned(way_bits-1 downto 0);
	begin

		-- hit detection
		h := (others => '0');
		for i in 0 to line_cnt-1 loop
			if tag(i)=addr and valid(i)='1' then
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
		for i in 0 to way_bits-1 loop
			for j in 0 to line_cnt-1 loop
				n := to_unsigned(j, way_bits);
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
			if wr='1' then
--				tag(to_integer(wrline)) <= wraddr;
--				valid(to_integer(wrline)) <= '1';
				tag(to_integer(nxt)) <= addr;
				valid(to_integer(nxt)) <= '1';
				nxt <= nxt + 1;
			end if;

			hit_reg <= next_hit;
			line_reg <= next_line;
			
			if invalidate = '1' then
				nxt <= (others => '0'); -- shall we reset nxt?
				valid <= (others => '0');			
			end if;
			

		end if;
	end process;

end;