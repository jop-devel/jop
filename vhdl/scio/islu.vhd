library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

--use ieee.std_logic_unsigned.all;
--use ieee.std_logic_misc.all;
use ieee.math_real.all;
use work.jop_types.all;
 
entity islu is

generic (cpu_cnt : integer := 4; lock_cnt : integer := 32);
	port (
		clock		: in std_logic;
		reset	: in std_logic;		
		
		sync_in	: in sync_in_array_type(0 to cpu_cnt-1);
		sync_out	: out sync_out_array_type(0 to cpu_cnt-1)
	);
end islu;

architecture rtl of islu is

	constant cpu_cnt_width : integer := integer(ceil(log2(real(cpu_cnt))));
	constant lock_cnt_width : integer := integer(ceil(log2(real(lock_cnt)))); 
	signal match  		: std_logic;
	signal empty  		: std_logic_vector(lock_cnt-1 downto 0);
	signal match_index,empty_index  		: integer range lock_cnt-1 downto 0;
	type ENTRY_ARRAY is array (integer range <>) of std_logic_vector(31 downto 0); -- Sets the lock width
	signal entry  : entry_array(lock_cnt-1 downto 0);
	signal cpu : std_logic_vector(cpu_cnt_width-1 downto 0);
	signal sync : std_logic_vector(cpu_cnt-1 downto 0);
	signal status : std_logic_vector(cpu_cnt-1 downto 0);
	type COUNT_ARRAY is array (lock_cnt-1 downto 0) of std_logic_vector(7 downto 0); -- Defines how many lock entries are allowed
	signal count : COUNT_ARRAY;
	type LOCK_CPU_ARRAY is array (lock_cnt-1 downto 0) of std_logic_vector(cpu_cnt_width-1 downto 0);
	signal current : LOCK_CPU_ARRAY;
	signal queue_head, queue_tail : LOCK_CPU_ARRAY;
	
	type LOCK_COUNT_ARRAY is array (cpu_cnt-1 downto 0) of std_logic_vector(lock_cnt_width-1 downto 0); -- Counts the number of locks that a core owns/waits for
	signal lock_count : LOCK_COUNT_ARRAY;
	
	signal data_r  : ENTRY_ARRAY(cpu_cnt-1 downto 0);
	signal op_r, register_i, register_o : std_logic_vector(cpu_cnt-1 downto 0);
	
	type state_type is (state_idle,state_ram,state_ram_delay,state_operation);
	signal state: state_type;
	
	signal ram_data_in, ram_data_out : std_logic_vector(cpu_cnt_width-1 downto 0);
	signal ram_write_address, ram_read_address : std_logic_vector(cpu_cnt_width+lock_cnt_width-1 downto 0);
	signal ram_we : std_logic;
	
	TYPE RAM_ARRAY IS ARRAY(2**cpu_cnt_width+lock_cnt_width-1 downto 0) OF std_logic_vector(cpu_cnt_width-1 DOWNTO 0);
   SIGNAL ram : RAM_ARRAY;
	
	signal total_lock_count : std_logic_vector(lock_cnt_width downto 0);
	
begin
	
	process (reset,sync_in,sync,register_i,register_o,status)
	begin
		for i in 0 to cpu_cnt-1 loop
			if((sync_in(i).req = '1') or (register_i(i) /= register_o(i)) or (sync(i) = '1')) then
				sync_out(i).halted <= '1';
			else
				sync_out(i).halted <= '0';
			end if;
			if(to_integer(unsigned(lock_count(i))) = 0) then
				sync_out(i).int_ena <= '1';
			else
				sync_out(i).int_ena <= '0';
			end if;
			sync_out(i).status <= status(i);
			sync_out(i).s_out <= sync_in(0).s_in;  -- Bootup signal used in jvm.asm
		end loop;
	end process;
	
	process (clock)
   begin
      if (rising_edge(clock)) then
         if (ram_we = '1') then
            ram(to_integer(unsigned(ram_write_address))) <= ram_data_in;
         end if;
         ram_data_out <= ram(to_integer(unsigned(ram_read_address)));
      end if;
   end process;
	
	empty_encoder: process(clock,reset)
	begin
		if(reset='1') then
			empty_index <= 0;
		elsif(rising_edge(clock)) then
			empty_index <= 0;
			for i in lock_cnt-1 downto 0 loop
				if (empty(i) = '1') then
					empty_index <= i;
				end if;
			end loop;
		end if;
	end process;
	
	match_encoder: process(clock,reset)
	begin
		if(reset='1') then
			match_index <= 0;
			match <= '0';
		elsif(rising_edge(clock)) then
			match_index <= 0;
			match <= '0';
			for i in lock_cnt-1 downto 0 loop
				if(entry(i) = data_r(to_integer(unsigned(cpu))) and empty(i) = '0') then
					match_index <= i;
					match <= '1';
				end if;
			end loop;
		end if;
	end process;
	
	register_fill: process(clock,reset)
	begin
		if(reset='1') then
			op_r <= (others => '0');
			data_r <= (others =>(others => '0'));
			register_i <= (others => '0');
		elsif(rising_edge(clock)) then
			for i in 0 to cpu_cnt-1 loop
				if(sync_in(i).req = '1') then
					op_r(i) <= sync_in(i).op;
					data_r(i) <= sync_in(i).data;
					register_i(i) <= not(register_i(i));
				end if;
			end loop;
		end if;
	end process;
	
	
	statemachine: process(clock,reset)
   begin
		if(reset='1') then
			cpu <= (others => '0');
			state <= state_idle;
			
			register_o <= (others => '0');
			
			ram_read_address <= (others => '0');
			ram_write_address <= (others => '0');
			ram_data_in <= (others => '0');
			
			queue_head <= (others => (others => '0'));
			queue_tail <= (others => (others => '0'));
			count <= (others => (others => '0'));
			entry <= (others => (others => '0'));
			empty <= (others => '1');
			sync <= (others => '0');
			status <= (others => '0');
			current <= (others => (others => '0'));
			ram_we <= '0';
			total_lock_count <= (others => '0');
			lock_count <= (others => (others => '0'));
		elsif(rising_edge(clock)) then
			ram_we <= '0';
			
			case state is
				when state_idle =>
					if(register_i(to_integer(unsigned(cpu))) /= register_o(to_integer(unsigned(cpu)))) then
						state <= state_ram;
					else
						cpu <= std_logic_vector(unsigned(cpu)+1);
					end if;
					
				when state_ram =>
					state <= state_ram_delay;
					
					ram_read_address <= std_logic_vector(to_unsigned(match_index, lock_cnt_width)) & queue_head(match_index);
					ram_write_address <= std_logic_vector(to_unsigned(match_index, lock_cnt_width)) & queue_tail(match_index);	
					ram_data_in <= cpu;
					
				when state_ram_delay =>
					state <= state_operation;
		
				when state_operation =>
					state <= state_idle;
					register_o(to_integer(unsigned(cpu))) <= not(register_o(to_integer(unsigned(cpu))));
					status(to_integer(unsigned(cpu))) <= '0';
					if(match = '1') then
						if(op_r(to_integer(unsigned(cpu))) = '0') then
							if(current(match_index) = cpu) then 
								-- Current cpu is owner so increment entry
								count(match_index) <= std_logic_vector(unsigned(count(match_index))+1);
							else
								-- Current cpu is not owner so enqueue
								ram_we <= '1'; -- Writes cpu to the address written at the previous pipeline stage
								queue_tail(match_index) <= std_logic_vector(unsigned(queue_tail(match_index))+1);
								sync(to_integer(unsigned(cpu))) <= '1';
								lock_count(to_integer(unsigned(cpu))) <= std_logic_vector(unsigned(lock_count(to_integer(unsigned(cpu))))+1);
							end if;
						else
							-- Erase lock
							-- We assume that only the current owner will try to modify the lock
							-- (Enqueued cores should be blocked)
							
							if(unsigned(count(match_index)) = 0) then 
								-- Current cpu is finished with lock
								if(queue_head(match_index) = queue_tail(match_index)) then
									-- Queue is empty
									empty(match_index) <= '1';
									total_lock_count <= std_logic_vector(unsigned(total_lock_count)-1);
								else
									-- Unblock next cpu
									current(match_index) <= ram_data_out;
									sync(to_integer(unsigned(ram_data_out))) <= '0';
									queue_head(match_index) <= std_logic_vector(unsigned(queue_head(match_index))+1);
								end if;
								lock_count(to_integer(unsigned(cpu))) <= std_logic_vector(unsigned(lock_count(to_integer(unsigned(cpu))))-1);
							else
								count(match_index) <= std_logic_vector(unsigned(count(match_index))-1);
							end if;
						end if;
					else
						if(to_integer(unsigned(total_lock_count)) = lock_cnt) then
							-- No lock entries left so return error
							status(to_integer(unsigned(cpu))) <= '1';
						elsif(op_r(to_integer(unsigned(cpu))) = '0') then
							empty(empty_index) <= '0';
							entry(empty_index) <= data_r(to_integer(unsigned(cpu)));
							current(empty_index) <= cpu;
							total_lock_count <= std_logic_vector(unsigned(total_lock_count)+1);
							lock_count(to_integer(unsigned(cpu))) <= std_logic_vector(unsigned(lock_count(to_integer(unsigned(cpu))))+1);
						end if;
					end if;
			end case;
		end if;
   end process;
end rtl;