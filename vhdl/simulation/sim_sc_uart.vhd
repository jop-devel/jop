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
--	sim_sc_uart.vhd
--
--	Serial interface for the simulation (SimpCon version)
--
--	Just prints the send character to stdout.
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--
--	2005-02-21	creation
--


library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sc_uart is

generic (addr_bits : integer;
	clk_freq : integer;
	baud_rate : integer;
	txf_depth : integer; txf_thres : integer;
	rxf_depth : integer; rxf_thres : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

	txd		: out std_logic;
	rxd		: in std_logic;
	ncts	: in std_logic;
	nrts	: out std_logic
);
end sc_uart;

architecture sim of sc_uart is

--
--	signals for uart connection
--
	signal ua_wr, tdre		: std_logic;
	signal rdrf				: std_logic;
	signal char				: std_logic_vector(7 downto 0);

begin

	rdy_cnt <= "00";	-- no wait states
--
--	io handling
--
process(clk, reset)
begin

	if (reset='1') then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			-- that's our very simple address decoder
			if address(0)='0' then
				rd_data <= std_logic_vector(to_unsigned(0, 30)) & rdrf & tdre;
			else
				rd_data <= std_logic_vector(to_unsigned(0, 32));
			end if;
		end if;
	end if;

end process;

	rdrf <= '0';	-- we don't receive characters in the simulation
	tdre <= '1';	-- the sender is always free in the simulation

process(clk)

	variable l : line;

begin
	if rising_edge(clk) then
		if address(0)='1' and wr='1' then
			char <= wr_data(7 downto 0);	-- we use char for ModelSim display
			write(l, character'val(to_integer(unsigned(wr_data(7 downto 0)))));
			writeline(output, l);
		end if;
	end if;
end process;


end sim;
