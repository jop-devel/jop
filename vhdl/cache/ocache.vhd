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

begin

process(ocin)
begin
	ocout.hit <= '0';
	ocout.dout <= data;
	if tag=ocin.handle and index=ocin.index and valid='1' then
		ocout.hit <= '1';
	end if;
end process;

process(clk, reset)
begin
	if reset='1' then
		valid <= '0';
	elsif rising_edge(clk) then
		if ocin.chk_gf='1' then
			ocin_reg <= ocin;
		end if;
		if ocin.wr_gf='1' then
			valid <= '1';
			tag <= ocin_reg.handle;
			index <= ocin_reg.index;
			data <= ocin.din;
		end if;
	end if;
end process;

end rtl;
