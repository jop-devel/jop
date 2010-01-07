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

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

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

	ocout.hit <= hit;
--	ocout.hit <= '0';

process(clk, reset)
begin
	if reset='1' then
		valid <= '0';
	elsif rising_edge(clk) then
		if ocin.chk_gf='1' or ocin.chk_pf='1' then
			hit_reg <= hit;
			ocin_reg <= ocin;
		end if;
		if ocin.wr_gf='1' then
			valid <= '1';
			tag <= ocin_reg.handle;
			index <= ocin_reg.index;
			data <= ocin.gf_val;
		end if;
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
