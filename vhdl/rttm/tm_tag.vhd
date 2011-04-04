--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Peter Hilber (peter@hilber.name)
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
--	Real-Time Transactional Memory
--

--
--	Tag memory
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tm_tag is 
generic (
	addr_width	: integer := 19;	-- address bits of cachable memory
	way_bits	: integer := 6		-- 2**way_bits is number of entries
);
port (
	clk, reset	: in std_logic;
	
	transaction_start: in std_logic;
	
	addr: in std_logic_vector(addr_width-1 downto 0);
	wr: in std_logic;
	
	hit: out std_logic;
	line: out unsigned(way_bits-1 downto 0);
	newline: out unsigned(way_bits downto 0)			
);
end tm_tag;

architecture rtl of tm_tag is 

	constant lines		: integer := 2**way_bits;

	signal l: unsigned(way_bits-1 downto 0);

	-- addr_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to lines-1) of std_logic_vector(addr_width-1 downto 0);
	signal tag			: tag_array;
	signal h: std_logic_vector(lines-1 downto 0); -- hit
	signal valid: std_logic_vector(lines-1 downto 0); -- valid

	-- pointer to next block to be used on a miss
	signal nxt			: unsigned(way_bits downto 0);

	signal h_res, hit_reg: std_logic;
	signal addr_dly: std_logic_vector(addr_width-1 downto 0);

begin


	hit <= h_res;
	line <= l;
	newline <= nxt;

-- asynchronous
hit_detection: process(tag, addr, h, valid)
	variable h_or: std_logic;
	variable n: unsigned(way_bits-1 downto 0);
begin

	-- hit detection
	h <= (others => '0');
	for i in 0 to lines-1 loop
		if tag(i)=addr and valid(i)='1' then
			h(i) <= '1';
		end if;
	end loop;

	h_or := '0';
	for i in 0 to lines-1 loop
		h_or := h_or or h(i);
	end loop;
	h_res <= h_or;

	-- the following should be optimized
	-- we don't need a priority encoder
--	l <= (others => '0');
--	for i in 0 to lines-1 loop
--		if h(i)='1' then
--			l <= to_unsigned(i, way_bits);
--			exit;
--		end if;
--	end loop;

	-- encoder without priority
	l <= (others => '0');
	for i in 0 to way_bits-1 loop
		for j in 0 to lines-1 loop
			n := to_unsigned(j, way_bits);
			if n(i)='1' and h(j)='1' then
				l(i) <= '1';
			end if;
		end loop;
	end loop;

	-- for 8 lines
--	l(0) <= h(1) or h(3) or h(5) or h(7);
--	l(1) <= h(2) or h(3) or h(6) or h(7);
--	l(2) <= h(4) or h(5) or h(6) or h(7);

	-- for 32 lines
--	l(0) <= h(1) or h(3) or h(5) or h(7) or h(9) or h(11) or h(13) or h(15) or h(17) or h(19) or h(21) or h(23) or h(25) or h(27) or h(29) or h(31);
--	l(1) <= h(2) or h(3) or h(6) or h(7) or h(10) or h(11) or h(14) or h(15) or h(18) or h(19) or h(22) or h(23) or h(26) or h(27) or h(30) or h(31);
--	l(2) <= h(4) or h(5) or h(6) or h(7) or h(12) or h(13) or h(14) or h(15) or h(20) or h(21) or h(22) or h(23) or h(28) or h(29) or h(30) or h(31);
--	l(3) <= h(8) or h(9) or h(10) or h(11) or h(12) or h(13) or h(14) or h(15) or h(24) or h(25) or h(26) or h(27) or h(28) or h(29) or h(30) or h(31);
--	l(4) <= h(16) or h(17) or h(18) or h(19) or h(20) or h(21) or h(22) or h(23) or h(24) or h(25) or h(26) or h(27) or h(28) or h(29) or h(30) or h(31);

end process;

tag_memory_update: process(clk, reset)
begin

	if reset='1' then

		nxt <= (others => '0');
		valid <= (others => '0');
		hit_reg <= '0';

	elsif rising_edge(clk) then
		hit_reg <= h_res;

		addr_dly <= addr;

		-- update tag memory in the next cycle
		if wr='1' then
			if hit_reg='0' then
				tag(to_integer(nxt(way_bits-1 downto 0))) <= addr_dly;
				valid(to_integer(nxt(way_bits-1 downto 0))) <= '1';
				nxt <= nxt + 1;
			end if;
		end if;
		
		if transaction_start = '1' then
			nxt <= (others => '0');
			valid <= (others => '0');			
		end if;
	end if;
end process;

end;
