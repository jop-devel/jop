--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Peter Hilber (peter@hilber.name)
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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;

entity mem_no_arbiter is

generic (
	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 14 bits
	MEM_BITS	: integer
);	
port (
	clk: std_logic;
	reset: std_logic;
	sc_mem_out: sc_out_type;
	sc_mem_in: out sc_in_type
);

end mem_no_arbiter;

architecture behav of mem_no_arbiter is

--
--	Configuration
--



constant ram_cnt		: integer := 2;		-- clock cycles for external ram
constant rom_cnt		: integer := 10;	-- clock cycles for external rom for 100 MHz


signal fl_d	: std_logic_vector(7 downto 0);

-- memory interface

	signal ram_addr			: std_logic_vector(17 downto 0);
--	signal ram_dout			: std_logic_vector(31 downto 0);
--	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;
	
	signal ram_dout			: std_logic_vector(31 downto 0);
	signal ram_data			: std_logic_vector(31 downto 0);

begin

	mem_if: entity work.sc_mem_if(rtl)
	generic map (
		ram_ws => ram_cnt-1,
		rom_ws => rom_cnt-1
		)
	port map (
		clk => clk,
		reset => reset,
		sc_mem_out => sc_mem_out,
		sc_mem_in => sc_mem_in,
		ram_addr => ram_addr,
		ram_dout => ram_dout,
		ram_din => ram_data,
		ram_dout_en => ram_dout_en,
		ram_ncs => ram_ncs,
		ram_noe => ram_noe,
		ram_nwe => ram_nwe,
		fl_d => fl_d,
		fl_rdy => '1'
		);
		
	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			ram_data <= ram_dout;
		else
			ram_data <= (others => 'Z');
		end if;
	end process;

	main_mem: entity work.memory generic map(MEM_BITS, 32) port map(
			addr => ram_addr(MEM_BITS-1 downto 0),
			data => ram_data,
			ncs => ram_ncs,
			noe => ram_noe,
			nwr => ram_nwe
			);
						
end architecture behav;
