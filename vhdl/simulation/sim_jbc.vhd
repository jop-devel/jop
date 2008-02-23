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
--	sim_jbc.vhd
--
--	bytecode memory/cache for JOP3
--	Version for simulation (ModelSim)
--
--	address, data in are registered
--	data out is unregistered
--
--
--	Changes:
--		2005-02-17	first version
--
--	TODO make addr_width generic.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity jbc is
generic (jpc_width : integer);
port (
	clk			: in std_logic;
	data		: in std_logic_vector(31 downto 0);
	rd_addr		: in std_logic_vector(jpc_width-1 downto 0);
	wr_addr		: in std_logic_vector(jpc_width-3 downto 0);
	wr_en		: in std_logic;
	q			: out std_logic_vector(7 downto 0)
);
end jbc;

architecture sim of jbc is

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
	signal reg_data			: std_logic_vector(31 downto 0);
	signal reg_rd_addr		: std_logic_vector(jpc_width-1 downto 0);
	signal reg_wr_addr		: std_logic_vector(jpc_width-3 downto 0);
	signal reg_wr_en		: std_logic;


	subtype word is std_logic_vector(32-1 downto 0);
	constant nwords : integer := 2**(jpc_width-2);
	type ram_type is array(0 to nwords-1) of word;

	shared variable ram : ram_type;

begin

process(clk)
begin
	if rising_edge(clk) then
		reg_data <= data;
		reg_rd_addr <= rd_addr;
		reg_wr_addr <= wr_addr;
		reg_wr_en <= wr_en;
	end if;
end process;


-- read process
-- do I need to take care about write changes???

process(reg_rd_addr, reg_wr_en)

	variable d	: std_logic_vector(31 downto 0);
	variable address : natural;

begin
	address := to_integer(unsigned(reg_rd_addr(jpc_width-1 downto 2)));
	d := ram(address);
-- is this byte order correct???
	case reg_rd_addr(1 downto 0) is
		when "11" =>
			q <= d(31 downto 24);
		when "10" =>
			q <= d(23 downto 16);
		when "01" =>
			q <= d(15 downto 8);
		when "00" =>
			q <= d(7 downto 0);
		when others =>
			null;
	end case;
	
end process;

--	write process
--	I do not care about read during write

process(reg_wr_addr, reg_data, reg_wr_en)

	variable address : natural;

begin
		if reg_wr_en='1' then
			address := to_integer(unsigned(reg_wr_addr));
			ram(address) := reg_data;
		end if;
end process;

end sim;
