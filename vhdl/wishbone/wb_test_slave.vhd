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
--	wb_test_slave.vhd
--
--	A simple test slave for the Wishbone interface
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	resources on Cyclone
--
--		xx LCs, max xx MHz
--
--
--	2005-05-29	first version
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.wb_pack.all;

entity wb_test_slave is

port (
	clk		: in std_logic;
	reset	: in std_logic;
	wb_in	: in wb_slave_in_type;
	wb_out	: out wb_slave_out_type
);
end wb_test_slave;

architecture rtl of wb_test_slave is

	signal xyz			: std_logic_vector(31 downto 0);
	signal cnt			: unsigned(31 downto 0);

	signal ena			: std_logic;
	signal ack			: std_logic;
	signal wr, rd		: std_logic;

begin

	ena <= wb_in.cyc_i and wb_in.stb_i;
	wr <= wb_in.we_i and ena;
	rd <= not wb_in.we_i and ena;

	-- single cycle read and write
	wb_out.ack_o <= ack and ena;
	ack <= ena;

--
--	The MUX is all we need for a read
--
process(wb_in.adr_i(0), xyz, cnt, rd)
begin

--	if rd='1' and ack='1' then
		if wb_in.adr_i(0)='0' then
			wb_out.dat_o <= std_logic_vector(cnt);
		else
			wb_out.dat_o <= xyz;
		end if;
--	else
-- just for the modelsim test!
--		wb_out.dat_o <= (others => 'X');
--	end if;
end process;


--
--	WB write is also simple
--
process(clk, reset)

begin

	-- WB requests synchronous reset for the interface.
	-- However, asynchronous reset is more common and in
	-- this case we reset the internal data and not the
	-- WB interface.
	if (reset='1') then
		xyz <= (others => '0');
		cnt <= (others => '0');

	elsif rising_edge(clk) then

		if wr='1' and ack='1' then

			xyz <= wb_in.dat_i;

		end if;

		cnt <= cnt+1;

	end if;

end process;


end rtl;
