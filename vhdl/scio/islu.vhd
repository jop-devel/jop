library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

--use ieee.std_logic_unsigned.all;
--use ieee.std_logic_misc.all;
use ieee.math_real.all;
use work.jop_types.all;
 
entity islu is

generic (cpu_cnt : integer := 12; lock_cnt : integer := 32);
	port (
		clock		: in std_logic;
		reset	: in std_logic;		
		
		sync_in	: in sync_in_array_type(cpu_cnt-1 downto 0);
		sync_out	: out sync_out_array_type(cpu_cnt-1 downto 0)
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
	type COUNT_ARRAY is array (lock_cnt-1 downto 0) of std_logic_vector(7 downto 0); -- Defines how many lock entries are allowed
	signal count : COUNT_ARRAY;
	type LOCK_CPU_ARRAY is array (lock_cnt-1 downto 0) of std_logic_vector(cpu_cnt_width-1 downto 0);
	signal current : LOCK_CPU_ARRAY;
	signal queue_head, queue_tail, queue_front : LOCK_CPU_ARRAY;
	
	signal data_r  : ENTRY_ARRAY(cpu_cnt-1 downto 0);
	signal op_r, register_i, register_o : std_logic_vector(cpu_cnt-1 downto 0);
	
	type state_type is (state_idle,state_ram,state_ram_delay,state_operation);
	signal state: state_type;
	
	signal ram_data_in, ram_data_out : std_logic_vector(cpu_cnt_width-1 downto 0);
	signal ram_write_address, ram_read_address : std_logic_vector(cpu_cnt_width+lock_cnt_width-1 downto 0);
	signal ram_we : std_logic;
	
	signal lock_count : integer range lock_cnt downto 0 := 0;
	

	component islu_ram is
	generic (data_width : integer := 4; address_width : integer := 8);
   port
   (
      clock: in   std_logic;
      data:  in   std_logic_vector (data_width-1 downto 0);
      write_address:  in   std_logic_vector (address_width-1 downto 0);
      read_address:   in   std_logic_vector (address_width-1 downto 0);
      we:    in   std_logic;
      q:     out  std_logic_vector (data_width-1 downto 0)
   );
	end component;
	
begin

	sync_loop: for i in 0 to cpu_cnt-1 generate
		sync_out(i).halted <= '1' when sync_in(i).req = '1' or register_i(i) /= register_o(i) or sync(i) = '1' else '0';
	end generate;


	queue_ram : islu_ram generic map(
		data_width => cpu_cnt_width,
		address_width => cpu_cnt_width+lock_cnt_width
	)
	port map (
		clock	 => clock,
		data	 => ram_data_in,
		write_address	=> ram_write_address,
		read_address	=> ram_read_address,
		we	=> ram_we,
		q => ram_data_out
	);

	
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
			queue_front <= (others => (others => '0'));
			count <= (others => (others => '0'));
			entry <= (others => (others => '0'));
			empty <= (others => '1');
			sync <= (others => '0');
			current <= (others => (others => '0'));
			ram_we <= '0';
			for i in 0 to cpu_cnt-1 loop
				sync_out(i).status <= '0';
			end loop;
			lock_count <= 0;
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
					sync_out(to_integer(unsigned(cpu))).status <= '0';
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
								if(queue_head(match_index) = queue_tail(match_index)) then
									-- Queue is empty so insert current cpu as front
									queue_front(match_index) <= cpu;
								end if;
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
								else
									-- Unblock next cpu
									current(match_index) <= queue_front(match_index);
									queue_front(match_index) <= ram_data_out;
									sync(to_integer(unsigned(queue_front(match_index)))) <= '0';
									queue_head(match_index) <= std_logic_vector(unsigned(queue_head(match_index))+1);
								end if;
								lock_count <= lock_count-1;
							else
								count(match_index) <= std_logic_vector(unsigned(count(match_index))-1);
							end if;
						end if;
					else
						if(lock_count = lock_cnt) then
							sync_out(to_integer(unsigned(cpu))).status <= '1';
						elsif(op_r(to_integer(unsigned(cpu))) = '0') then
							empty(empty_index) <= '0';
							entry(empty_index) <= data_r(to_integer(unsigned(cpu)));
							current(empty_index) <= cpu;
							lock_count <= lock_count+1;
						end if;
					end if;
			end case;
		end if;
   end process;
end rtl;

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;
ENTITY islu_ram IS
	generic (data_width : integer := 4; address_width : integer := 8);
   PORT
   (
      clock: IN   std_logic;
      data:  IN   std_logic_vector (data_width-1 DOWNTO 0);
      write_address:  IN   std_logic_vector (address_width-1 DOWNTO 0);
      read_address:   IN   std_logic_vector (address_width-1 DOWNTO 0);
      we:    IN   std_logic;
      q:     OUT  std_logic_vector (data_width-1 DOWNTO 0)
   );
END islu_ram;
ARCHITECTURE rtl OF islu_ram IS
   TYPE mem IS ARRAY(2**address_width-1 downto 0) OF std_logic_vector(data_width-1 DOWNTO 0);
   SIGNAL ram_block : mem;
BEGIN
   PROCESS (clock)
   BEGIN
      IF (rising_edge(clock)) THEN
         IF (we = '1') THEN
            ram_block(to_integer(unsigned(write_address))) <= data;
         END IF;
         q <= ram_block(to_integer(unsigned(read_address)));
      END IF;
   END PROCESS;
END rtl;