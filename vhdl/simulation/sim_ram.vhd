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
--	sim_ram.vhd
--
--	internal memory for JOP3
--	Version for simulation
--
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity ram is
generic (width : integer := 32; addr_width : integer := 8);
port (
	reset		: in std_logic;
	data		: in std_logic_vector(width-1 downto 0);
	wraddress	: in std_logic_vector(addr_width-1 downto 0);
	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(width-1 downto 0)
);
end ram ;

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
--	with normal clock on wrclock:
--		=> read during write on same address!!! (ok in ACEX)
--	for Cyclone use not clock for wrclock, but works also on ACEX
--
architecture sim of ram is

	signal wraddr_dly	: std_logic_vector(addr_width-1 downto 0);
	signal wren_dly		: std_logic;

	signal reg_data			: std_logic_vector(width-1 downto 0);
	signal reg_wraddress	: std_logic_vector(addr_width-1 downto 0);
	signal reg_rdaddress	: std_logic_vector(addr_width-1 downto 0);
	signal reg_wren			: std_logic;

	subtype word is std_logic_vector(width-1 downto 0);
	constant nwords : integer := 2 ** addr_width;
	type ram_type is array(0 to nwords-1) of word;

	shared variable ram : ram_type;

begin

-- initialize at start with a second process accessing
-- the shared variable ram

initialize:
process

	variable address	: natural;

	file memfile		: text is "mem_ram.dat";
	variable memline	: line; 
	variable val		: integer;

	begin
--		write(output, "load stack ram...");
		for address in 0 to nwords-1 loop
			if endfile(memfile) then
				exit;
			end if;
			readline(memfile, memline);
			read(memline, val);
			ram(address) := std_logic_vector(to_signed(val, 32));
		end loop;
		file_close(memfile);
		-- we're done, wait forever
		wait;

end process initialize;


--
--	delay wr addr and ena because of registerd indata
--
process(clock) begin

	if rising_edge(clock) then
		wraddr_dly <= wraddress;
		wren_dly <= wren;
	end if;
end process;

--
--	Simulation starts here
--

--
--	register addresses and in data
--
--	write uses inverted clock in aram.vhd!
--
process(clock) begin

	if rising_edge(clock) then
		reg_rdaddress <= rdaddress;
	end if;
	if falling_edge(clock) then
		reg_data <= data;
		reg_wraddress <= wraddr_dly;
		reg_wren <= wren_dly;
	end if;
end process;


-- read process
-- do I need to take care about write changes???

process(reg_rdaddress, reg_wren)

	variable address : natural;

begin
	address := to_integer(unsigned(reg_rdaddress));
	q <= ram(address);
end process;


--	write process
--	I do not care about read during write

process(reg_wraddress, reg_data, reg_wren)

	variable address : natural;

begin
		if reg_wren='1' then
			address := to_integer(unsigned(reg_wraddress));
			ram(address) := reg_data;
		end if;
end process;

--	load init data
--				LPM_FILE => "../../asm/generated/ram.mif", 



end sim;
