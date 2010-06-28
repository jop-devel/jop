-- 
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, 2010 Peter Hilber
--  Copyright (C) 2007,2008, Christof Pitter
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




-- 150407: first working version with records
-- 170407: produce number of registers depending on the cpu_cnt
-- 110507: * arbiter that can be used with prefered number of masters
--				 * full functional arbiter with two masters
--				 * short modelsim test with 3 masters carried out
-- 190607: Problem found: Both CPU1 and CPU2 start to read cache line!!!
-- 030707: Several bugs are fixed now. CMP with 3 running masters functions!
-- 150108: Quasi Round Robin Arbiter -- added sync signal to arbiter
-- 160108: First tests running with new Round Robin Arbiter
-- 190208: Development of TDMA Arbiter
-- 130308: * Renaming of this_state to mode, follow_state to next_mode
--				 * counter dependencies moved from FSM to slot generation
--				 * changed set to 2 bits
--				 * changed serv to servR and servW
--				 * added signal pipelined
--				 * added rd_data register for each CPU
-- 140308: Working version
-- 070808: removed combinatorial loop (pipelined bug)
-- 210808: - reg_in_rd_data(i) also gets loaded when rdy_cnt = 3 using pipelined access
--				 - arb_in(i).rd_data gets mem_in.rd_data when rdy_cnt 3 using pipelined access
-- 310809: Added transactional memory broadcast support
-- 111109: Integrated RTTM tm_broadcast signal
-- 150310: Use slots of CPUs executing hardware layer transaction for commit
-- 280310: Hack to speed up arbiter

-- TODO:
--				- reallocate slots of CPUs executing software layer transactions



library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.jop_types.all;

entity arbiter is
generic(
			addr_bits : integer;
			cpu_cnt	: integer);		-- number of masters for the arbiter
port (
			clk, reset	: in std_logic;			
			arb_out			: in arb_out_type(0 to cpu_cnt-1);
			arb_in			: out arb_in_type(0 to cpu_cnt-1);
			mem_out			: out sc_out_type;
			mem_in			: in sc_in_type;
			next_is_a_read	: in std_logic_vector(0 to cpu_cnt-1);
			committing 		: in std_logic_vector(0 to cpu_cnt-1);
			tm_in_transaction : in std_logic_vector(0 to cpu_cnt-1);
			tm_broadcast	: out tm_broadcast_type
);
end arbiter;


architecture rtl of arbiter is

-- stores the signals in a register of each master

	type reg_type is record
		rd : std_logic;
		wr : std_logic;
		tm_broadcast : std_logic;
		wr_data : std_logic_vector(31 downto 0);
		address : std_logic_vector(addr_bits-1 downto 0);
	end record; 
	
	type reg_out_type is array (0 to cpu_cnt-1) of reg_type;
	signal reg_out : reg_out_type;
	
-- register to CPU for rd_data
	
	type reg_in_type is array (0 to cpu_cnt-1) of std_logic_vector(31 downto 0);
	signal reg_in_rd_data : reg_in_type;
	
-- one fsm for each CPU

	type state_type is (idle, read, write, waitingR, sendR, 
	waitingW, sendW);
	type state_array is array (0 to cpu_cnt-1) of state_type;
	signal state : state_array;
	signal next_state : state_array;
	
-- one fsm for each serve

	type serve_type is (idl, servR, servW);
	type serve_array is array (0 to cpu_cnt-1) of serve_type;
	signal mode : serve_array;
	signal next_mode : serve_array;
	
-- arbiter 
	
	type set_type is array (0 to cpu_cnt-1) of std_logic_vector(1 downto 0);
	signal set : set_type;
	type pipelined_type is array (0 to cpu_cnt-1) of std_logic;
	signal pipelined : pipelined_type;
	signal next_pipelined : pipelined_type;
	
-- read and write gaps

-- Constants for read_gap, write_gap, slot_length:
-- DE2 board:   6 cycle memory write, 4 cycle memory read
--              write_gap <= 5; read_gap <= 3;
--              minimal slot_length <= 6;
-- cyc12 board: 3 cycle memory write, 2 cycle memory read
--              write_gap <= 2; read_gap <= 1;
--              minimal slot_length <= 3;

  constant write_gap : integer := 5;
  constant read_gap  : integer := 3;
  constant slot_length : integer := 6;
  constant period : integer := cpu_cnt*slot_length;

-- counter
	subtype counter_type is integer range 0 to period;
	signal counter : counter_type;
	type time_type is array (0 to cpu_cnt-1) of counter_type;
	signal cpu_time : time_type; -- how much clock cycles each CPU
	type slot_type is array (0 to cpu_cnt-1) of std_logic;
	signal slot, next_slot : slot_type; -- defines which CPU is on turn

-- for RTTM	
	signal conventional_slot : slot_type;		
	signal committing_reg : std_logic_vector(0 to cpu_cnt-1);
	signal tm_in_transaction_reg : std_logic_vector(0 to cpu_cnt-1);
	signal any_committing_reg : std_logic;
	
begin


-- generate slot information
slots: for i in 0 to cpu_cnt-1 generate
	cpu_time(i) <= (i+1)*slot_length;
end generate;	

-- and memory access time. Should use assert to check that the slot length
-- is longer than the memory access time.


-- Generates the input register and saves incoming data for each master
gen_register: for i in 0 to cpu_cnt-1 generate
	process(clk, reset)
	begin
		if reset = '1' then
			reg_out(i).rd <= '0';
			reg_out(i).wr <= '0';
			reg_out(i).tm_broadcast <= '0';
			reg_out(i).wr_data <= (others => '0'); 
			reg_out(i).address <= (others => '0');
		elsif rising_edge(clk) then
			if arb_out(i).rd = '1' or arb_out(i).wr = '1' then
				reg_out(i).rd <= arb_out(i).rd;
				reg_out(i).wr <= arb_out(i).wr;
				reg_out(i).tm_broadcast <= arb_out(i).tm_broadcast;
				reg_out(i).address <= arb_out(i).address;
				reg_out(i).wr_data <= arb_out(i).wr_data;
			end if;
		end if;
	end process;
end generate;
	
-- Generate Counter
process(clk, reset)
	begin
		if reset = '1' then
			counter <= 0;
		elsif rising_edge(clk) then
			counter <= counter + 1;
			if counter = period-1 then
				counter <= 0;
			end if;
		end if;
end process;
				
-- A time slot is assigned to each CPU 
conventional_slots: process(counter, cpu_time, next_is_a_read)
  variable lower_limit : integer;
begin
	for j in 0 to cpu_cnt-1 loop
		conventional_slot(j) <= '0';
	end loop;

    lower_limit := 0;
    for j in 0 to cpu_cnt-1 loop
      if (counter >= lower_limit) and (counter < cpu_time(j)-write_gap) then
        conventional_slot(j) <= '1';
        exit;
      elsif (counter >= lower_limit) and (counter < cpu_time(j)-read_gap) and
      	next_is_a_read(j) = '1' then -- rd is 2 cycles longer allowed
        conventional_slot(j) <= '1';
        exit;
      end if;
      lower_limit := cpu_time(j);
    end loop;
end process;	

override_slots: process(conventional_slot, tm_in_transaction_reg, 
	committing_reg, any_committing_reg) is
	variable slot_for_transaction: std_logic;
begin
	next_slot <= conventional_slot;
	
	slot_for_transaction := '0';
	for j in 0 to cpu_cnt-1 loop
		if tm_in_transaction_reg(j) = '1' and conventional_slot(j) = '1' then
			slot_for_transaction := '1';
		end if;
	end loop;		
	
	if any_committing_reg = '1' and slot_for_transaction = '1' then
		for j in 0 to cpu_cnt-1 loop
			if tm_in_transaction_reg(j) = '1' then
				next_slot(j) <= committing_reg(j);
			end if;
		end loop;
	end if;
end process;


sync: process(clk, reset) is
begin
	if reset = '1' then
		any_committing_reg <= '0';
		committing_reg <= (others => '0');
		slot <= (others => '0'); 
	elsif rising_edge(clk) then
		any_committing_reg <= '0';
		
		for j in 0 to cpu_cnt-1 loop
			if committing(j) = '1' then
				any_committing_reg <= '1';
			end if;
		end loop;
		
		committing_reg <= committing;
		tm_in_transaction_reg <= tm_in_transaction;
		slot <= next_slot;
	end if;
end process;

	
-- Generates next state of the FSM for each master
gen_next_state: for i in 0 to cpu_cnt-1 generate
	process(state, mode, slot, mem_in, arb_out, pipelined)	 
	begin

		next_state(i) <= state(i);

		case state(i) is
			when idle =>
				next_pipelined(i) <= '0';
			
				-- is CPU allowed to access
				if (slot(i) = '1') then
					
					-- pipelined read access
					if (mode(i) = servR) and (mem_in.rdy_cnt = 1) and (arb_out(i).rd = '1') then
						next_state(i) <= read;
						next_pipelined(i) <= '1';
						
					elsif (mode(i) = servR) and (mem_in.rdy_cnt = 0) then
						if arb_out(i).rd = '1' then
							next_state(i) <= read;
						elsif arb_out(i).wr = '1' then
							next_state(i) <= write;				
						end if;
					
					elsif (mode(i) = servW) and (mem_in.rdy_cnt = 0) then
						if arb_out(i).rd = '1' then
							next_state(i) <= read;
						elsif arb_out(i).wr = '1' then
							next_state(i) <= write;				
						end if;
		
					elsif (mode(i) = idl) and (mem_in.rdy_cnt = 0) then
						if arb_out(i).rd = '1' then
							next_state(i) <= read;
						elsif arb_out(i).wr = '1' then
							next_state(i) <= write;				
						end if;
											
					-- all other kinds (can that happen at all?)
					else
						if arb_out(i).rd = '1' then
							next_state(i) <= waitingR;
						elsif arb_out(i).wr = '1' then
							next_state(i) <= waitingW;
						end if;
					end if;

				-- CPU is not allowed to access
				else
					if arb_out(i).rd = '1' then
						next_state(i) <= waitingR;
					elsif arb_out(i).wr = '1' then
						next_state(i) <= waitingW;
					end if;
				end if;
						
			when read =>
				next_state(i) <= idle;
				next_pipelined(i) <= '0';
				
				if pipelined(i) = '1' then
					next_pipelined(i) <= '1';
				end if;
				
			when write =>
				next_state(i) <= idle;
				next_pipelined(i) <= '0';
			
			when waitingR =>				
				next_pipelined(i) <= '0';
				if ((mem_in.rdy_cnt = 0) and (slot(i) = '1')) then
					next_state(i) <= sendR;
				else
					next_state(i) <= waitingR;
				end if;
			
			when sendR =>
				next_state(i) <= idle;
				next_pipelined(i) <= '0';
				
			when waitingW =>
				next_pipelined(i) <= '0';
				if ((mem_in.rdy_cnt = 0) and (slot(i) = '1')) then
					next_state(i) <= sendW;
				else
					next_state(i) <= waitingW;
				end if;
			
			when sendW =>
				next_state(i) <= idle;
				next_pipelined(i) <= '0';
		
		end case;
	end process;
end generate;


-- Generates the FSM state for each master
gen_state: for i in 0 to cpu_cnt-1 generate
	process (clk, reset)
	begin
		if (reset = '1') then
			state(i) <= idle;
			pipelined(i) <= '0';
  	elsif (rising_edge(clk)) then
			state(i) <= next_state(i);	
			pipelined(i) <= next_pipelined(i);
		end if;
	end process;
end generate;


-- The arbiter output
process (arb_out, reg_out, next_state)
begin

	mem_out.rd <= '0';
	mem_out.wr <= '0';
	mem_out.address <= (others => '0');
	mem_out.wr_data <= (others => '0');
	mem_out.atomic <= '0';
	tm_broadcast <= (valid => '0', address => (others => '0'));
	
	for i in 0 to cpu_cnt-1 loop
		set(i) <= "00";
		
		case next_state(i) is
			when idle =>
				
			when read =>
				set(i) <= "01";
				mem_out.rd <= arb_out(i).rd;
				mem_out.address <= arb_out(i).address;
			
			when write =>
				set(i) <= "10";
				mem_out.wr <= arb_out(i).wr;
				mem_out.address <= arb_out(i).address;
				mem_out.wr_data <= arb_out(i).wr_data;			
				tm_broadcast <= (
						valid => arb_out(i).wr and arb_out(i).tm_broadcast,
						address => arb_out(i).address);
				
			
			when waitingR =>
				
			when sendR =>
				set(i) <= "01";
				mem_out.rd <= reg_out(i).rd;
				mem_out.address <= reg_out(i).address;
			
			when waitingW =>
			
			when sendW =>
				set(i) <= "10";
				mem_out.wr <= reg_out(i).wr;
				mem_out.address <= reg_out(i).address;
				mem_out.wr_data <= reg_out(i).wr_data;
				tm_broadcast <= (
						valid => reg_out(i).wr and arb_out(i).tm_broadcast,
						address => reg_out(i).address);
				
				
		end case;
	end loop;
end process;

-- generation of next_mode
gen_serve: for i in 0 to cpu_cnt-1 generate
	process(mem_in, set, mode)
	begin
		case mode(i) is
			when idl =>
				next_mode(i) <= idl;
				if set(i) = "01" then 
					next_mode(i) <= servR;
				elsif set(i) = "10" then
					next_mode(i) <= servW;
				end if;
			when servR =>
				next_mode(i) <= servR;
				if mem_in.rdy_cnt = 0 and set(i) = "00" then
					next_mode(i) <= idl;
				end if;
			when servW =>
				next_mode(i) <= servW;
				if mem_in.rdy_cnt = 0 and set(i) = "00" then
					next_mode(i) <= idl;
				end if;
		end case;
	end process;
end generate;
	
gen_serve2: for i in 0 to cpu_cnt-1 generate
	process (clk, reset)
	begin
		if (reset = '1') then
			mode(i) <= idl;
  	elsif (rising_edge(clk)) then
			mode(i) <= next_mode(i);	
		end if;
	end process;
end generate;



-- Registers rd_data for each CPU
gen_reg_in: for i in 0 to cpu_cnt-1 generate
	process(clk, reset)
	begin
		if reset = '1' then
			reg_in_rd_data(i) <= (others => '0'); 
		elsif rising_edge(clk) then
			if mode(i) = servR then
				if mem_in.rdy_cnt = 0 then
					reg_in_rd_data(i) <= mem_in.rd_data;
				-- added mem_in.rdy_cnt = 3. 
				-- More correct would be: ((mem_in.rdy_cnt = ram_cnt) or (mem_in.rdy_cnt = 3))
				elsif ((( mem_in.rdy_cnt = 2 ) or ( mem_in.rdy_cnt = 3 ))  and next_pipelined(i) = '1') then
					reg_in_rd_data(i) <= mem_in.rd_data;
				end if;			
			end if;
		end if;
	end process;
end generate;


				
-- Generates rdy_cnt and rd_data for all CPUs
gen_rdy_cnt: for i in 0 to cpu_cnt-1 generate
	process (mem_in, state, mode, next_pipelined, reg_in_rd_data)
	begin  
		
		arb_in(i).rd_data <= reg_in_rd_data(i);
		arb_in(i).rdy_cnt <= mem_in.rdy_cnt;
		
		case state(i) is
			when idle =>
				if (mode(i) = idl) then
					arb_in(i).rdy_cnt <= "00";
				elsif (mode(i) = servR) and (mem_in.rdy_cnt = 0) then
					arb_in(i).rd_data <= mem_in.rd_data;
				end if;
				
			when read =>
				if (mode(i) = servR) then
					if (mem_in.rdy_cnt = 0) then
						arb_in(i).rd_data <= mem_in.rd_data;
					-- added mem_in.rdy_cnt = 3. 
					-- More correct would be: ((mem_in.rdy_cnt = ram_cnt) or (mem_in.rdy_cnt = 3))
					elsif ((( mem_in.rdy_cnt = 2 ) or ( mem_in.rdy_cnt = 3 ))  and next_pipelined(i) = '1') then
						arb_in(i).rd_data <= mem_in.rd_data;
					end if;
				end if;
			
			when write =>		
			
			when waitingR =>
				arb_in(i).rdy_cnt <= "11";
				if mode(i) = servR then
					arb_in(i).rd_data <= mem_in.rd_data;
				end if;
				
			when sendR =>
			
			when waitingW =>
				arb_in(i).rdy_cnt <= "11";
			
			when sendW =>
				
		end case;
	end process;
end generate;

end rtl;
