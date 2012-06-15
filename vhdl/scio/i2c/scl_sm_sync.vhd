-- Ver. 0.00.00e
-- Ver. 0.00.01e


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

library work;
use work.i2c_pkg.all;

entity scl_sm_sync is

	port( 
	
	clk     : in std_logic; 		--! Input clock
	reset   : in std_logic;			--! Reset signal
	  
	scl_oe     : out std_logic;		--! SCL pin
	
	scl_int : in std_logic;
	
	busy : in std_logic;
	idle_state : in std_logic;
-- 	bit_cnt : in std_logic_vector(3 downto 0);
	master_stop : in std_logic;
	i2c_rep_strt : in std_logic;
	reset_rep_start : out std_logic;
	
	sda_scl : out std_logic;
	sda_ctrl : out std_logic;
	
	masl : in std_logic;
	strt : in std_logic;
	
	t_const : in timming;
	scl_monitor: out std_logic_vector(10 downto 0)
	
	); 
	
end entity scl_sm_sync;
 
architecture scl_sm_sync_rtl of scl_sm_sync is
	
  --constant COUNT_BASE : natural := TO_INTEGER(CNT_BASE);
  --constant COUNT_BASE : natural := 1;

  constant RESET_LEVEL : std_logic := '1';
  --constant COUNT_BASE : integer := 1;

--   constant HOLD_START : natural range 0 to (5*COUNT_BASE-1) := (5*COUNT_BASE-1);
--   constant T_LOW      : natural range 0 to (5*COUNT_BASE-1) := (5*COUNT_BASE-1);
--   constant T_HIGH     : natural range 0 to (5*COUNT_BASE-1) := (5*COUNT_BASE-1);
--   constant DELAY_STOP : natural range 0 to (2*COUNT_BASE):= (2*COUNT_BASE);  
--   constant T_SUSTO    : natural range 0 to 2*COUNT_BASE := 2*COUNT_BASE;
--   constant TBUF       : natural range 0 to 5*COUNT_BASE := 5*COUNT_BASE;
--   constant T_WAIT     : natural range 0 to 9*COUNT_BASE := 9*COUNT_BASE;
  
--   constant HOLD_START : std_logic_vector(7 downto 0) := "00000100";
--   constant T_LOW      : std_logic_vector(7 downto 0) := "00000100";
--   constant T_HIGH     : std_logic_vector(7 downto 0) := "00000100";
--   constant DELAY_STOP : std_logic_vector(7 downto 0) := "00000010";  
--   constant T_SUSTO    : std_logic_vector(7 downto 0) := "00000010";
--   --constant TBUF       : std_logic_vector(7 downto 0) := "00000100";
--   constant T_WAIT     : std_logic_vector(7 downto 0) := "00001001";
  
  	signal scl_count   : std_logic_vector(7 downto 0);
	signal reset_scl_counter : std_logic;
	signal scl_count_ena     : std_logic;
	
	type scl_state_type is (idle, start, scl_falling, scl_rising, scl_low, scl_high, scl_stop);
	signal scl_state         : scl_state_type;
	signal scl_next_state    : scl_state_type;
	
	signal scl_out            : std_logic;
	
	 
	signal gen_stop: std_logic;
	signal gen_stop_reg: std_logic;
	signal sda_out_reg: std_logic;
	signal sda_out: std_logic;
	signal scl_out_reg: std_logic;

	signal stop_state: std_logic;

	signal gen_rep_start: std_logic;
	signal gen_rep_start_reg: std_logic;
begin

-- BEGIN MONITOR SIGNALS ---

	scl_monitor(0) <= scl_out_reg;
	scl_monitor(1) <= sda_out_reg;
	scl_monitor(2) <= scl_count_ena;
	scl_monitor(9 downto 3) <= scl_count(6 downto 0);
	scl_monitor(10) <= stop_state;
	
-- END MONITOR SIGNALS ---	

-- 	scl_oe <= scl_out_reg and stop_scl;
	scl_oe <= scl_out_reg;
	sda_scl <= sda_out_reg;
	sda_ctrl <= '1' when ((scl_state = start) or (scl_state = scl_stop) or (gen_stop_reg = '1') or (gen_rep_start_reg = '1') or (idle_state = '1')) else '0';  
	
	
counter_m: process(clk, reset)
		
begin
		if reset = RESET_LEVEL then
			scl_count <= (others => '0');
			
		elsif clk'event and clk = '1' then
			
			-- Increment SCL counter 
			if reset_scl_counter = '1' then
				scl_count <= (others => '0');
			elsif scl_count_ena = '1' then
				scl_count <= std_logic_vector(unsigned(scl_count) + 1);
			end if;
	end if;
		
	end process counter_m; 	 
	
--scl_sm: process(busy, gen_stop_reg, masl, master_stop, scl_count, scl_state, sda_out_reg, strt)   
scl_sm: process(busy, gen_rep_start_reg, gen_stop_reg, i2c_rep_strt, masl, master_stop, scl_count, scl_int, scl_state, sda_out_reg, strt, t_const)

--TODO Starting the SCL process. 
-- We start the process when indicated by the environment and 
-- the bus is free.

	begin
	
	-- Default values
	reset_scl_counter <= '1';
	--scl_count_ena     <= '1';
	sda_out  <= sda_out_reg;
	gen_stop <=  gen_stop_reg;
	gen_rep_start <=  gen_rep_start_reg;
	stop_state <= '0';
	reset_rep_start <= '0';
	
	
	case scl_state is 
	
		when idle =>
		
			
			sda_out    <= '1';
 			scl_count_ena <= '1';
 			scl_out <= '1';
 			gen_stop <= '0';
 			gen_rep_start <= '0'; 
 			
			if strt = '1' and masl = '1' and busy = '0' then
				scl_next_state <= start;
			else
				scl_next_state <= idle;
			end if;

		when start =>
			reset_scl_counter <= '0';
			sda_out    <= '0'; 
			scl_out <= '1';
			gen_stop <= '0';
			gen_rep_start <= '0';
			scl_count_ena <= '1';
			
			if i2c_rep_strt = '1' then
				reset_rep_start <= '1';
			end if;
			 			
			if scl_count = t_const.HOLD_START then
				reset_scl_counter <= '1';
				scl_next_state <=  scl_falling;
			else
				reset_scl_counter <= '0';
				scl_next_state <= start;
			end if;
			
		when scl_falling =>
 			scl_out            <= '0';
 			reset_scl_counter <= '0';
 			scl_count_ena     <= '1';
 			scl_next_state    <= scl_low;
 			gen_stop <= '0';
 			gen_rep_start <= '0';
 			
		when scl_low =>
 			scl_out <= '0';
 			scl_count_ena <= '1';

 			if master_stop = '1' then
 				sda_out <= '0';
 				gen_stop <= '1';
 				gen_rep_start <= '0';
 			elsif i2c_rep_strt = '1' then
 				sda_out <= '1';
 				gen_stop <= '0';
 				gen_rep_start <= '1'; 
 			else
 				sda_out <= '1';
 				gen_stop <= '0';
 				gen_rep_start <= '0';
 			end if;
 					
 			if scl_count = t_const.T_LOW then
				reset_scl_counter <= '1';
				scl_next_state <= scl_rising;
			else 
				reset_scl_counter <= '0';
				scl_next_state    <= scl_low;
			end if;
			
		when scl_rising =>
			scl_out <= '1';
			--reset_scl_counter <= '0';
			scl_count_ena     <= '0'; 

			if master_stop = '1' then
				sda_out <= '0';
				gen_stop <= '1';
				gen_rep_start <= '0';
			elsif i2c_rep_strt = '1' then
				sda_out <= '1';
				gen_rep_start <= '1';
				gen_stop <= '0';
			else
				sda_out <= '1';
				gen_stop <= '0';
				gen_rep_start <= '0';
			end if;
			
			-- I2C devices can slow down communication by stretching SCL: 
			-- During an SCL low phase, any I2C device on the bus may 
			-- additionally hold down SCL to prevent it to rise high again, 
			-- enabling them to slow down the SCL clock rate or to stop I2C 
			-- communication for a while.
			
			-- An I2C slave is allowed to hold down the clock if it needs to 
			-- reduce the bus speed. The master on the other hand is required
			-- to read back the clock signal after releasing it to high state
			-- and wait until the line has actually gone high.
			if scl_int = '0' then
				scl_next_state <= scl_rising;
			else
				scl_next_state <= scl_high;
			end if;
				
		when scl_high =>
			scl_out <= '1';
			reset_scl_counter <= '0';
			scl_count_ena <= '1';
			
			if scl_count = t_const.DELAY_STOP then
				--reset_scl_counter <= '0';
				if gen_stop_reg = '1' then
					scl_next_state <= scl_stop;
					reset_scl_counter <= '1';
					sda_out <= '0';
					
				elsif gen_rep_start_reg = '1' then
					scl_next_state <= start;
					reset_scl_counter <= '1';
					sda_out <= '1';
					
				else
					scl_next_state <= scl_high;
					reset_scl_counter <= '0';
					sda_out <= '1';
				end if;
			else
				if scl_count = t_const.T_HIGH then
					reset_scl_counter <= '1';
					scl_next_state <= scl_falling;
				else 
					scl_next_state <= scl_high;
					reset_scl_counter <= '0';
				end if;
			end if;
		
		-- After t = T_SUSTO we have to set SDA to high to signal the
		-- stop condition. After that, we have to wait t = T_BUFF to 
		-- start another transfer or to perform a repeated start. In
		-- this case, T_WAIT = T_SUSTO + T_BUFF.
		when scl_stop =>
			reset_scl_counter <= '0';
			scl_count_ena     <= '1';
			scl_out            <= '1';
			gen_stop <= '0';
			gen_rep_start <= '0';
			
			stop_state <= '1';
	
			if scl_count < t_const.T_SUSTO then
				sda_out  <= '0';
			else 
				sda_out  <= '1';
			end if;

 			if scl_count = t_const.T_WAIT then 
				scl_next_state <= idle;
			else
				scl_next_state <= scl_stop;
			end if;
			
	end case;
			
	end process scl_sm;
	
	scl_reg: process(clk, reset)

	begin
		
		if reset = RESET_LEVEL then
			scl_state <= idle;
			sda_out_reg <= '1';
			scl_out_reg <= '1';
			gen_stop_reg <= '0';
			gen_rep_start_reg <= '0';

		elsif clk'event and clk = '1' then
			scl_state <= scl_next_state;
			sda_out_reg <= sda_out;
			scl_out_reg <= scl_out;
			gen_stop_reg <= gen_stop;
			gen_rep_start_reg <= gen_rep_start;

		end if;
		
	end process scl_reg;
	
	
end architecture scl_sm_sync_rtl;