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
--	Tag memory
--
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tag is 
generic (
	addr_width	: integer;		-- address bits of cachable memory
	way_bits	: integer		-- 2**way_bits is number of entries
);
port (
	clk, reset	: in std_logic;
	addr: in std_logic_vector(addr_width-1 downto 0);
	wr: in std_logic;
	hit: out std_logic;
	line: out unsigned(way_bits-1 downto 0);
	newline: out unsigned(way_bits-1 downto 0)
);
end tag;

architecture rtl of tag is 

	constant lines		: integer := 2**way_bits;

	signal l, line_addr: unsigned(way_bits-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to lines-1) of std_logic_vector(addr_width-1 downto 0);
	signal tag			: tag_array;
	signal h, v: std_logic_vector(lines-1 downto 0);

	-- pointer to next block to be used on a miss
	signal nxt			: unsigned(way_bits-1 downto 0);

	signal h_res, hit_reg, wr_dly: std_logic;
	signal addr_dly: std_logic_vector(addr_width-1 downto 0);

begin


	hit <= hit_reg;
	line <= line_addr;
	newline <= nxt;

process(tag, addr, h, v)
	variable h_or: std_logic;
	variable n: unsigned(way_bits-1 downto 0);
begin

	-- hit detection
	h <= (others => '0');
	for i in 0 to lines-1 loop
		if tag(i)=addr and v(i)='1' then
			h(i) <= '1';
		end if;
	end loop;

	h_or := '0';
	for i in 0 to lines-1 loop
		h_or := h_or or h(i);
	end loop;
	h_res <= h_or;

	-- the following should be optimized
	-- we don't need a priority encoder
--	l <= (others => '0');
--	for i in 0 to lines-1 loop
--		if h(i)='1' then
--			l <= to_unsigned(i, way_bits);
--			exit;
--		end if;
--	end loop;

	-- encoder without priority
	l <= (others => '0');
	for i in 0 to way_bits-1 loop
		for j in 0 to lines-1 loop
			n := to_unsigned(j, way_bits);
			if n(i)='1' and h(j)='1' then
				l(i) <= '1';
			end if;
		end loop;
	end loop;

	-- for 8 lines
--	l(0) <= h(1) or h(3) or h(5) or h(7);
--	l(1) <= h(2) or h(3) or h(6) or h(7);
--	l(2) <= h(4) or h(5) or h(6) or h(7);

	-- for 32 lines
--	l(0) <= h(1) or h(3) or h(5) or h(7) or h(9) or h(11) or h(13) or h(15) or h(17) or h(19) or h(21) or h(23) or h(25) or h(27) or h(29) or h(31);
--	l(1) <= h(2) or h(3) or h(6) or h(7) or h(10) or h(11) or h(14) or h(15) or h(18) or h(19) or h(22) or h(23) or h(26) or h(27) or h(30) or h(31);
--	l(2) <= h(4) or h(5) or h(6) or h(7) or h(12) or h(13) or h(14) or h(15) or h(20) or h(21) or h(22) or h(23) or h(28) or h(29) or h(30) or h(31);
--	l(3) <= h(8) or h(9) or h(10) or h(11) or h(12) or h(13) or h(14) or h(15) or h(24) or h(25) or h(26) or h(27) or h(28) or h(29) or h(30) or h(31);
--	l(4) <= h(16) or h(17) or h(18) or h(19) or h(20) or h(21) or h(22) or h(23) or h(24) or h(25) or h(26) or h(27) or h(28) or h(29) or h(30) or h(31);

end process;

process(clk, reset)
begin

	if reset='1' then

		nxt <= (others => '0');
		v <= (others => '0');
		hit_reg <= '0';
		for i in 0 to lines-1 loop
			tag(i) <= (others => '0');
		end loop;

	elsif rising_edge(clk) then


		hit_reg <= h_res;

		wr_dly <= wr;
		addr_dly <= addr;

		line_addr <= l;

		-- update tag memory in the next cycle
		if wr_dly='1' then
			if hit_reg='0' then
				tag(to_integer(nxt)) <= addr_dly;
				v(to_integer(nxt)) <= '1';
				nxt <= nxt + 1;
			end if;
		end if;

	end if;
end process;

end;

--
--	Trasnactional Memory
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
				rd_hit <= '1';	
			end if;
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
	
