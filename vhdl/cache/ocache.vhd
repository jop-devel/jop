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

	signal wait4data : std_logic;
	signal hit : std_logic;

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

process(clk, reset)
begin
	if reset='1' then
		valid <= '0';
		wait4data <= '0';
	elsif rising_edge(clk) then
		-- if ocin.chk_gf='1' or ocin.chk_pf='1' then
		if ocin.chk_gf='1' then
			ocin_reg <= ocin;
			if hit='0' then
				wait4data <= '1';
			end if;
		end if;
		-- wr_gf is just connected to idl state!
		if ocin.wr_gf='1' then
--			if wait4data='1' then
				wait4data <= '0';
				valid <= '1';
				tag <= ocin_reg.handle;
				index <= ocin_reg.index;
				data <= ocin.din;
--			end if;
		end if;
		-- quick hack invaludate on pf hit
		-- at the moment too lazy to remember the value
		if ocin.wr_pf='1' then
			valid <= '0';
		end if;
	end if;
end process;

end rtl;
