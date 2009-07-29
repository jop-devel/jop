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
--	Real-Time Transactional Memory
--

--
--	Transactional Memory
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity tm is

generic (
	addr_width	: integer := 18;	-- address bits of cachable memory
	way_bits	: integer := 5		-- 2**way_bits is number of entries
);
port (
	clk, reset	: in std_logic;
	from_cpu		: in sc_out_type;
	to_cpu			: out sc_in_type
--	to_mem			: out sc_out_type;
--	from_mem		: in sc_in_type;

);
end tm;

architecture rtl of tm is 

	constant lines		: integer := 2**way_bits;
	constant mem_bits	: integer := SC_ADDR_SIZE-3;	-- should be 20 for 1 MB SRAM

	signal line_addr, newline: unsigned(way_bits-1 downto 0);
	-- tag_width can be used to reduce cachable area - saves a lot in the comperators

	type data_array is array (0 to lines-1) of std_logic_vector(31 downto 0);
	signal data			: data_array;

	signal hit			: std_logic;

	signal from_cpu_dly: sc_out_type;
	signal rd_hit: std_logic;
	signal reg_data, save_data: std_logic_vector(31 downto 0);

	
begin

	tag: entity work.tag
		generic map(
			addr_width => mem_bits,
			way_bits => way_bits
		)
		port map(
			clk => clk,
			reset => reset,
			
			addr => from_cpu.address(mem_bits-1 downto 0),
			wr => from_cpu.wr,
			hit => hit,
			line => line_addr,
			newline => newline
		);


process(clk, reset)
begin

	if reset='1' then

		to_cpu.rdy_cnt <= "00";
		rd_hit <= '0';	

	elsif rising_edge(clk) then


		to_cpu.rdy_cnt <= "00";
		rd_hit <= '0';	

		from_cpu_dly <= from_cpu;

		if from_cpu.wr='1' or from_cpu.rd='1' then
			to_cpu.rdy_cnt <= "01";
		end if;

		-- write in the next cycle
		if from_cpu_dly.wr='1' then
			if hit='1' then
				data(to_integer(line_addr)) <= from_cpu_dly.wr_data;
			else
				data(to_integer(newline)) <= from_cpu_dly.wr_data;
			end if;
		end if;

		-- another cycle delay to infer on-chip memory
		reg_data <= data(to_integer(line_addr));
		if from_cpu_dly.rd='1' then
			if hit='1' then
				rd_hit <= '1'; -- delayed
			end if;
			-- TODO no hit => use main memory accessed in parallel(?)
			-- probably not if it is a cache 
		end if;

		if rd_hit='1' then
			save_data <= reg_data;
		end if;

	end if;
end process;

process (rd_hit, reg_data, save_data)
begin
	if rd_hit='1' then
		to_cpu.rd_data <= reg_data;
	else
		to_cpu.rd_data <= save_data;
	end if;
end process;

end;
	
