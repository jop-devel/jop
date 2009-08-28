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
use work.sc_arbiter_pack.all;
use work.tm_pack.all;


entity tm is

generic (
	-- TODO flash has to be .nc to avoid duplicates
	addr_width		: integer := 18;	-- address bits of cachable memory
	way_bits		: integer := 5		-- 2**way_bits is number of entries
);
port (
	clk, reset		: in std_logic;
	from_cpu		: in sc_out_type;
	to_cpu			: out sc_in_type;
 	to_mem			: out sc_out_type;
 	from_mem		: in sc_in_type;
 	
 	broadcast		: in tm_broadcast_type;
 	conflict		: out std_logic;
 	
 	start_commit	: in std_logic;
 	committing		: buffer std_logic; -- TODO

	read_tag_of		: out std_logic;
	write_buffer_of	: out std_logic
	);

end tm;

architecture rtl of tm is

	type state_type is (
		no_commit,
		commit_read,
		commit_write,
		commit_wait
		);

	signal state			: state_type;

	signal tm_rdy_cnt		: unsigned(RDY_CNT_SIZE-1 downto 0); 

	--
	-- Write buffer
	--
	
	constant lines		: integer := 2**way_bits;
	-- TODO delete?
	-- constant mem_bits	: integer := SC_ADDR_SIZE-3;	-- should be 20 for 1 MB SRAM

	signal line_addr, newline: unsigned(way_bits-1 downto 0);

	--type data_array is array (0 to lines-1) of std_logic_vector(31 downto 0);
	signal data			: data_array(0 to lines-1);

	signal hit			: std_logic;

	signal from_cpu_dly: sc_out_type;
	signal rd_hit: std_logic;
	signal rd_miss				: std_logic;
	
	signal reg_data, save_data: std_logic_vector(31 downto 0);
	
	signal write_tags_full: std_logic;
	
	--
	-- Commit/broadcast logic
	--
	
	signal commit_line			: unsigned(way_bits-1 downto 0);
	signal shift				: std_logic;
	
	signal commit_addr			: std_logic_vector(addr_width-1 downto 0);
	
	--
	-- Read tag memory
	--
	
	signal read_tags_wr			: std_logic;
	signal read_tags_addr		: std_logic_vector(addr_width-1 downto 0);
	
	signal read_tags_hit		: std_logic;
	signal read_tags_full		: std_logic;
	
	signal is_conflict_check	: std_logic;
	
	signal broadcast_addr_del	: std_logic_vector(SC_ADDR_SIZE-1 downto 0);
	signal broadcast_check_del	: std_logic;
	signal next_broadcast_check_del	: std_logic;
	
begin

	-- TODO not an OF yet, but easier to handle
	read_tag_of <= read_tags_full;
	write_buffer_of <= write_tags_full;


--
-- Read tag memory
--

	read_tags_wr <= from_cpu.rd and not from_cpu.nc;

	read_tags: entity work.tag(rtl)
	generic map (
		addr_width => addr_width,
		way_bits => way_bits
		)
	port map (
		clk => clk,
		reset => reset,
		
		addr => read_tags_addr,
		wr => read_tags_wr,
		hit => read_tags_hit,
		line => open,
		newline => open,
		full => read_tags_full,
		
		shift => '0',
		lowest_addr => open
		); 



	write_tags: entity work.tag
		generic map(
			addr_width => addr_width,
			way_bits => way_bits
		)
		port map(
			clk => clk,
			reset => reset,
			
			addr => from_cpu.address(addr_width-1 downto 0),
			wr => from_cpu.wr,
			hit => hit,
			line => line_addr,
			newline => newline,
			full => write_tags_full,
			
			shift => shift,
			lowest_addr => commit_addr
		);
		
-- TODO why is is_conflict_check set here?
gen_read_tags_addr_async: process(from_cpu, broadcast, broadcast_addr_del, 
	broadcast_check_del) is
begin
	next_broadcast_check_del <= '0';

	if from_cpu.rd = '1' then
		read_tags_addr <= from_cpu.address(addr_width-1 downto 0);
		if broadcast.valid = '1' then
			next_broadcast_check_del <= '1';
		end if;
		
		is_conflict_check <= '0';
	else
		-- TODO e.g. here: use don't care?
		read_tags_addr <= broadcast.address(addr_width-1 downto 0);
		is_conflict_check <= broadcast.valid;
	
		if broadcast_check_del = '1' then			
			read_tags_addr <= broadcast_addr_del(addr_width-1 downto 0);
			is_conflict_check <= '1';
		end if;							
		-- TODO make sure conflicts that are detected one cycle later still
		-- prevent (early) commit
	end if;	
end process gen_read_tags_addr_async;

-- TODO this assumes that there are 2 cycles delay 
-- between successive reads and writes (during commit)
--
-- sets conflict 
gen_read_tags_addr_sync: process(clk, reset) is
begin
	if reset = '1' then
		broadcast_check_del <= '0';
		
		conflict <= '0';
	elsif rising_edge(clk) then
		broadcast_addr_del <= broadcast.address;
		broadcast_check_del <= next_broadcast_check_del;
		
		conflict <= is_conflict_check and read_tags_hit;
	end if;
end process gen_read_tags_addr_sync;


--
-- Write buffer
--

gen_write_buffer_signals: process(clk, reset)
begin

	if reset='1' then

		tm_rdy_cnt <= "00";
		rd_hit <= '0';
		rd_miss <= '0';	

	elsif rising_edge(clk) then


		tm_rdy_cnt <= "00";
		rd_hit <= '0';	

		from_cpu_dly <= from_cpu;

		if from_cpu.wr='1' or from_cpu.rd='1' then
			tm_rdy_cnt <= "01";
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
		-- TODO line_addr was already clocked in tag, why is it clocked again?
		if committing = '0' then
			reg_data <= data(to_integer(line_addr));
		else
			reg_data <= data(to_integer(commit_line));
		end if;		
		
		if from_cpu_dly.rd='1' then
			-- TODO check how fast memory can be
			rd_miss <= not hit;
			
			if hit='1' then
				rd_hit <= '1'; -- delayed
			end if;
		end if;

		if rd_hit='1' then
			save_data <= reg_data;
		end if;
		
		if rd_miss = '1' and from_mem.rdy_cnt = "00" then
			save_data <= from_mem.rd_data;
			rd_miss <= '0';
		end if;
	end if;
end process gen_write_buffer_signals;

gen_rdy_cnt: process(tm_rdy_cnt, from_mem.rdy_cnt) is
begin
	if tm_rdy_cnt > from_mem.rdy_cnt then
		to_cpu.rdy_cnt <= tm_rdy_cnt;
	else
		to_cpu.rdy_cnt <= from_mem.rdy_cnt;
	end if;
end process gen_rdy_cnt;

process (rd_hit, reg_data, save_data, rd_miss, from_mem)
begin
	-- TODO test
	-- TODO
	if rd_miss = '1' then
		to_cpu.rd_data <= from_mem.rd_data;
	elsif rd_hit='1' then
		to_cpu.rd_data <= reg_data;
	else
		to_cpu.rd_data <= save_data;
	end if;
end process;



--
-- Commit
--

-- sets state, commit_line, committing
commit: process (clk, reset) is
begin
    if reset = '1' then
    	state <= no_commit;
    	
    	commit_line <= (others => '0');
    	committing <= '0';
    elsif rising_edge(clk) then
    	shift <= '0';
    	to_mem.wr <= '0';
    
		case state is
			when no_commit =>
				if start_commit = '1' then					
					state <= commit_read;
					commit_line <= (others => '0');
					committing <= '1';
				end if;
			when commit_read =>				
				if commit_line = newline then
					state <= no_commit;
					committing <= '0';
				else
					state <= commit_write;
					
					shift <= '1';
					to_mem.wr <= '1'; 
				end if;
				
				-- for next commit_read state
				commit_line <= commit_line + 1;
			when commit_write =>
				
				state <= commit_wait;
			when commit_wait =>
				-- TODO rdy_cnt = "0X"
				if from_mem.rdy_cnt = "00" then
					state <= commit_read;
				end if;
		end case;
			
    end if;
end process commit;

to_mem.rd <= '0';

process(reset, clk) is
begin
	if reset = '1' then
		to_mem.address	<= (others => '0'); 
		to_mem.wr_data <= (others => '0');		
	elsif rising_edge(clk) then
		to_mem.address	<= (SC_ADDR_SIZE-1 downto addr_width => '0') 
			& commit_addr;
		to_mem.wr_data <= reg_data;		
	end if;
end process;


end;
	
