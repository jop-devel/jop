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
--
--	Testbench for the jop with 16 bit SRAM
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tb_jop is
end;

architecture tb of tb_jop is

component jop is

port (
	clk				: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;

--
--	watchdog
--
	wd				: out std_logic;

--
--	only one ram bank
--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic

);
end component;


component memory is
	generic(add_bits : integer := 12;
		data_bits : integer := 32);
	port(
		addr	: in std_logic_vector(add_bits-1 downto 0);
		data	: inout std_logic_vector(data_bits-1 downto 0);
		ncs		: in std_logic;
		noe		: in std_logic;
		nwr		: in std_logic);
 
end component;


	signal clk		: std_logic := '1';
	signal ser_rxd	: std_logic := '1';

--
--	RAM connection. We use address and control lines only
--	from rama.
--
	signal ram_addr		: std_logic_vector(17 downto 0);
	signal ram_data		: std_logic_vector(31 downto 0);
	signal ram_noe		: std_logic;
	signal ram_ncs		: std_logic;
	signal ram_nwr		: std_logic;

	signal txd			: std_logic;

begin

	joptop: jop port map(
		clk => clk,
		ser_txd => txd,
		ser_rxd => ser_rxd,
		rama_a => ram_addr,
		rama_d => ram_data(15 downto 0),
		rama_noe => ram_noe,
		rama_ncs => ram_ncs,
		rama_nwe => ram_nwr
	);

	main_mem: memory generic map(16, 16) port map(
			addr => ram_addr(15 downto 0),
			data => ram_data(15 downto 0),
			ncs => ram_ncs,
			noe => ram_noe,
			nwr => ram_nwr
		);
		
--	100 MHz clock
		
clock : process
   begin
   wait for 5 ns; clk  <= not clk;
end process clock;

--
--	print out data from uart
--
process

	variable data : std_logic_vector(8 downto 0);
	variable l : line;

begin
	wait until txd='0';
	wait for 4.34 us;
	for i in 0 to 8 loop
		wait for 8.68 us;
		data(i) := txd;
	end loop;
	write(l, character'val(to_integer(unsigned(data(7 downto 0)))));
	writeline(output, l);

end process;

--
--	simulate download for jvm.asm test
--
process

	variable data : std_logic_vector(10 downto 0);
	variable l : line;

begin

	data := "11010100110";
	wait for 10 us;
	for i in 0 to 9 loop
		wait for 8.68 us;
		ser_rxd <= data(i);
	end loop;

end process;

end tb;

