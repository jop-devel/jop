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
--	Full associative cache with FIFO replacement
--

--
--	Tag memory
--
--	MS: don't understand why the next line is wired out
--	of the tag and back again.
--
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity fifo_cache_tag is 
	generic (
		addr_width	: integer;		-- address bits of cachable memory
		index_bits	: integer		-- 2**index_bits is number of entries
		);
	port (
		clk, reset	: in std_logic;
		addr        : in std_logic_vector(addr_width-1 downto 0);
		wraddr      : in std_logic_vector(addr_width-1 downto 0);
		wrline      : in unsigned(index_bits-1 downto 0);
		wr          : in std_logic;
		hit         : out std_logic;
		line        : out unsigned(index_bits-1 downto 0);
		newline     : out unsigned(index_bits-1 downto 0)
		);
end fifo_cache_tag;

architecture rtl of fifo_cache_tag is 

	constant line_cnt: integer := 2**index_bits;

	signal line_reg, next_line: unsigned(index_bits-1 downto 0);

	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	type tag_array is array (0 to line_cnt-1) of std_logic_vector(addr_width-1 downto 0);
	signal tag: tag_array;
	
	signal valid: std_logic_vector(line_cnt-1 downto 0);

	-- pointer to next block to be used on a miss
	signal nxt: unsigned(index_bits-1 downto 0);

	signal hit_reg, next_hit: std_logic;

begin

	hit <= hit_reg;
	line <= line_reg;
	newline <= nxt;

	process(tag, addr, valid)
		variable h: std_logic_vector(line_cnt-1 downto 0);
		variable h_or: std_logic;
		variable n: unsigned(index_bits-1 downto 0);
	begin

		-- hit detection
		h := (others => '0');
		for i in 0 to line_cnt-1 loop
			if tag(i)=addr and valid(i)='1' then
				h(i) := '1';
			end if;
		end loop;

		h_or := '0';
		for i in 0 to line_cnt-1 loop
			h_or := h_or or h(i);
		end loop;
		next_hit <= h_or;

		-- encoder without priority
		next_line <= (others => '0');
		for i in 0 to index_bits-1 loop
			for j in 0 to line_cnt-1 loop
				n := to_unsigned(j, index_bits);
				if n(i)='1' and h(j)='1' then
					next_line(i) <= '1';
				end if;
			end loop;
		end loop;

	end process;

	process(clk, reset)
	begin

		if reset='1' then

			nxt <= (others => '0');
			valid <= (others => '0');
			hit_reg <= '0';
			
			for i in 0 to line_cnt-1 loop
				tag(i) <= (others => '0');
			end loop;

		elsif rising_edge(clk) then

			-- update tag memory in the next cycle
			if wr='1' then
				tag(to_integer(wrline)) <= wraddr;
				valid(to_integer(wrline)) <= '1';
				nxt <= nxt + 1;
			end if;

			hit_reg <= next_hit;
			line_reg <= next_line;

		end if;
	end process;

end;

--
--	The actual cache
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity fifo_cache is

	generic (
		index_bits	: integer := 4		-- 2**index_bits is number of entries
		);
	port (
		clk, reset	: in std_logic;

		cpu_out		: in sc_out_type;
		cpu_in		: out sc_in_type;

		mem_out		: out sc_out_type;
		mem_in		: in sc_in_type
		);
end fifo_cache;

architecture rtl of fifo_cache is 

	constant line_cnt: integer := 2**index_bits;
	constant mem_bits: integer := SC_ADDR_SIZE-3;	-- should be 20 for 1 MB SRAM

	signal int_reset: std_logic;

	-- index for hit and new elements
	signal line, newline: unsigned(index_bits-1 downto 0);
	signal wrline_reg, next_wrline : unsigned(index_bits-1 downto 0);

	-- cache ram signals
	signal ram_data : std_logic_vector(31 downto 0);
	signal ram_wraddress: std_logic_vector(index_bits-1 downto 0);
	signal ram_rdaddress: std_logic_vector(index_bits-1 downto 0);
	signal ram_wren : std_logic;
	signal ram_dout : std_logic_vector(31 downto 0);

    -- signaling hits
	signal hit: std_logic;

	signal tag_addr : std_logic_vector(mem_bits-1 downto 0);
	signal tag_wraddr : std_logic_vector(mem_bits-1 downto 0);
	signal tag_wrline : unsigned(index_bits-1 downto 0);
	signal tag_wr : std_logic;
	
	-- register data from CPU
	signal cpu_out_reg, next_cpu_out: sc_out_type;
	-- register data to CPU
	signal rd_data_reg, next_rd_data : std_logic_vector(31 downto 0);
	-- register data to memory
	signal next_mem_out : sc_out_type;
	
	signal fetch_reg, next_fetch : std_logic;
	signal crd_reg, next_crd : std_logic;

	-- what state we're in
	type STATE_TYPE is (idle,
						wr0, wr1,
						rd0, rd1, rd2);
	signal state, next_state : state_type;
	
begin

	tag: entity work.fifo_cache_tag
		generic map(
			addr_width => mem_bits,
			index_bits => index_bits
			)
		port map(
			clk => clk,
			reset => int_reset,
			
			addr => tag_addr,
			wraddr => tag_wraddr,
			wrline => tag_wrline,
			wr => tag_wr,
			hit => hit,
			line => line,
			newline => newline
			);

	cache_ram: entity work.sdpram
		generic map (
			width	   => 32,
			addr_width => index_bits)
		port map (
			wrclk	   => clk,
			data	   => ram_data,
			wraddress  => ram_wraddress,
			wren	   => ram_wren,
			
			rdclk	   => clk,
			rdaddress  => ram_rdaddress,
			rden	   => '1',
			dout	   => ram_dout);

	int_reset <= reset or cpu_out.cinval;

	sync: process(clk, reset)
	begin

		if reset='1' then
			cpu_out_reg <= ((others => '0'), (others => '0'), '0', '0', '0', bypass, '0', '0', '0');
			rd_data_reg <= (others => '0');
			fetch_reg <= '0';
			crd_reg <= '0';
			wrline_reg <= (others => '0');
			state <= idle;
			
		elsif rising_edge(clk) then
			cpu_out_reg <= next_cpu_out;
			rd_data_reg <= next_rd_data;
			mem_out <= next_mem_out;
			fetch_reg <= next_fetch;
			crd_reg <= next_crd;
			wrline_reg <= next_wrline;
			state <= next_state;		

		end if;
	end process sync;

	async: process (cpu_out, cpu_out_reg, mem_in,
					ram_dout, rd_data_reg, fetch_reg, crd_reg, wrline_reg,
					hit, line, newline,
					state)
	begin

		next_wrline <= wrline_reg;
		
		tag_addr <= cpu_out.address(mem_bits-1 downto 0);
		tag_wraddr <= cpu_out_reg.address(mem_bits-1 downto 0);
		tag_wrline <= wrline_reg;		
		tag_wr <= '0';
		
		-- register data from CPU
		if cpu_out.rd = '1' or cpu_out.wr = '1' then
			next_cpu_out <= cpu_out;
		else
			next_cpu_out <= cpu_out_reg;
		end if;

		-- default values to CPU
		cpu_in.rdy_cnt <= "00";
		if fetch_reg = '1' then
			cpu_in.rd_data <= mem_in.rd_data;
			next_rd_data <= mem_in.rd_data;
		elsif crd_reg = '1' then
			cpu_in.rd_data <= ram_dout;
			next_rd_data <= ram_dout;
		else
			cpu_in.rd_data <= rd_data_reg;
			next_rd_data <= rd_data_reg;
		end if;
		next_fetch <= '0';
		next_crd <= '0';
			
		-- default outputs to memory
		next_mem_out.rd <= '0';
		next_mem_out.wr <= '0';
		next_mem_out.wr_data <= cpu_out_reg.wr_data;
		next_mem_out.address <= cpu_out_reg.address;
		next_mem_out.atomic <= cpu_out_reg.atomic;
		next_mem_out.cache <= cpu_out_reg.cache;

		-- signals for ram block
		ram_data <= cpu_out_reg.wr_data;
		ram_wraddress <= std_logic_vector(line);
		ram_rdaddress <= std_logic_vector(line);
		ram_wren <= '0';
		
		-- we're idle unless we know better
		next_state <= state;

		case state is

			when idle => null; 			-- handled below

			-- the write sequence, updating cache
			when wr0 =>  				-- pass on data to main memory
				cpu_in.rdy_cnt <= "11";
				next_state <= wr1;

			when wr1 =>  				-- wait for memory
				cpu_in.rdy_cnt <= mem_in.rdy_cnt;
				if mem_in.rdy_cnt <= 1 then
					next_state <= idle;
				else
					next_state <= wr1;
				end if;

			-- memory read sequence, updating cache
			when rd0 =>  				-- pass on data to main memory
				cpu_in.rdy_cnt <= "11";
				next_state <= rd1;
				
			when rd1 =>  				-- wait for memory
				if mem_in.rdy_cnt <= 1 then
					cpu_in.rdy_cnt <= "10";
					next_state <= rd2;
				else
					cpu_in.rdy_cnt <= "11";
					next_state <= rd1;
				end if;

			when rd2 =>  				-- write back data to cache
				-- write new data to cache, address/hit from previous cycle
				tag_wrline <= wrline_reg;
				tag_wr <= '1';

				ram_wren <= '1';
				ram_data <= mem_in.rd_data;
				ram_wraddress <= std_logic_vector(wrline_reg);
				
				cpu_in.rdy_cnt <= "01";
				next_fetch <= '1';
				next_state <= idle;

			when others => null;
		end case;

		-- start a new transaction
		if state = idle or state = wr1 then

			if cpu_out_reg.wr = '1' and cpu_out_reg.cache = full_assoc then

				next_cpu_out.wr <= cpu_out.wr;
				
				-- write new data to cache, address/hit from previous cycle
				ram_wren <= '1';
				ram_data <= cpu_out_reg.wr_data;
				if hit = '1' then
					ram_wraddress <= std_logic_vector(line);
				else
					tag_wrline <= newline;
					tag_wr <= '1';
					ram_wraddress <= std_logic_vector(newline);
				end if;

				-- trigger a write
				next_mem_out.wr <= '1';
				cpu_in.rdy_cnt <= "11";
				next_state <= wr0;

			elsif cpu_out_reg.rd = '1' and cpu_out_reg.cache = full_assoc then

				next_cpu_out.rd <= cpu_out.rd;

				if hit = '1' then
					
					-- read from cache
					cpu_in.rdy_cnt <= "11";
					next_crd <= '1';
					next_state <= idle;
					
				else

				    -- trigger a read
					next_wrline <= newline;
					next_mem_out.rd <= '1';
					cpu_in.rdy_cnt <= "11";
					next_state <= rd0;

				end if;

			end if;
		end if;

	end process async;

end;
	
