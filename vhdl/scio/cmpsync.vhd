-- 
--  This file is part of JOP, the Java Optimized Processor 
-- 
--  Copyright (C) 2007,2008,2010 Christof Pitter 
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
--	cmpsync.vhd
--
--	Author: CP, WP
--
--  2007-11-22  Global lock synchronization and bootup of CMP
--  2007-12-07  redesign of global lock synchronization
--  2007-12-19  included new lock arbitration in state "locked".
--              Otherwise wrong output if two CPUs are waiting for lock!
--  2010-09-06  Using round-robin for fair arbitration


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.jop_types.all;

entity cmpsync is
generic (
		cpu_cnt : integer
	);
port (
	clk, reset		 : in std_logic;		
	sync_in_array  : in sync_in_array_type(0 to cpu_cnt-1);
	sync_out_array : out sync_out_array_type(0 to cpu_cnt-1)
);
end cmpsync;


architecture rtl of cmpsync is

type state_type is (idle, locked);
signal state : state_type;
signal next_state : state_type;

signal locked_id	: integer;
signal next_locked_id : integer;

signal rr_index : integer;
signal next_rr_index : integer;

begin

  -- save who has the lock, next_state of fsm
  process(sync_in_array, state, locked_id, rr_index)	 
  	begin
  
  		next_state <= state;
  		next_locked_id <= locked_id;
		next_rr_index <= rr_index;
		
  		case state is
  			when idle =>
				next_locked_id <= cpu_cnt; -- means no CPU_ID is locked

				-- smallest CPU_ID bigger than rr_index wins
				-- if none, smallest CPU_ID wins
  				for i in cpu_cnt-1 downto 0 loop
  					if i <= rr_index and sync_in_array(i).lock_req = '1' then 
  						next_state <= locked;
  						next_locked_id <= i;
						next_rr_index <= i;
  					end if;
  				end loop;
  				for i in cpu_cnt-1 downto 0 loop
  					if i > rr_index and sync_in_array(i).lock_req = '1' then 
  						next_state <= locked;
  						next_locked_id <= i;
						next_rr_index <= i;
  					end if;
  				end loop;
  				
  			when locked =>
  				-- CPU frees the lock
  				if sync_in_array(locked_id).lock_req = '0' then
  					next_state <= idle;
  					next_locked_id <= cpu_cnt;
						
					-- new lock request
					for i in cpu_cnt-1 downto 0 loop
						if i <= rr_index and sync_in_array(i).lock_req = '1' then 
							next_state <= locked;
							next_locked_id <= i;
							next_rr_index <= i;
						end if;
					end loop;
					for i in cpu_cnt-1 downto 0 loop
						if i > rr_index and sync_in_array(i).lock_req = '1' then 
							next_state <= locked;
							next_locked_id <= i;
							next_rr_index <= i;
						end if;
					end loop;
				end if;
  		end case;
  	end process;
  	
  -- generates the FSM state
  	process (clk, reset)
  	begin
  		if (reset = '1') then
  			state <= idle;			
  			locked_id <= cpu_cnt; -- initially no id is locked
			rr_index <= cpu_cnt-1;
		elsif (rising_edge(clk)) then
  			state <= next_state;
  			locked_id <= next_locked_id;
			rr_index <= next_rr_index;
  		end if;
  	end process;
  	
  -- output
  process (next_state, next_locked_id, sync_in_array)
  begin

	for i in 0 to cpu_cnt-1 loop
		sync_out_array(i).s_out <= sync_in_array(0).s_in;  -- Bootup
	end loop;
	
  	case next_state is
  		when idle =>
  			for i in 0 to cpu_cnt-1 loop
  				sync_out_array(i).halted <= '0';
  			end loop;
  			
  		when locked =>
  			for i in 0 to cpu_cnt-1 loop
  				if (i = next_locked_id) then
  					sync_out_array(i).halted <= '0';
  				else
  					sync_out_array(i).halted <= '1';
  				end if;
  			end loop;	
  	end case;
  end process;

end rtl;





