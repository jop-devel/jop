--! @file scl_control.vhd
--! @brief I2C scl (clock) controller.  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2011-2012
--! @copyright GNU Public License.

library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;
use work.i2c_pkg.all;

entity scl_control is
	port(
		clk        : in    std_logic;   --! Input clock
		reset      : in    std_logic;   --! Reset signal

		scl_in	: in std_logic;
		scl_out : out std_logic;

		ctrl_from_sda     : in    sda_control_in_type;
		ctrl_to_sda    : out   sda_control_out_type;

		control_in : in    control_type;
		status_in  : in    status_type;

		t_const    : in    timing_type
	);

end entity scl_control;

architecture scl_arch of scl_control is
	constant RESET_LEVEL : std_logic := '1';

	signal scl_count     : std_logic_vector(7 downto 0);
	signal rst_scl_count : std_logic;
	signal ena_scl_count : std_logic;

	signal strt    : std_logic;
	
	type scl_state_type is (idle, start, scl_restart_1, scl_restart_2, scl_falling, scl_rising, scl_low, scl_high, scl_stop);
	signal state      : scl_state_type;
	signal next_state : scl_state_type;

	signal sda_ena      : std_logic;
	signal sda_ena_reg  : std_logic;
	signal set_sda      : std_logic;
	signal scl_arb_lost : std_logic;

begin

	SCL_REG : process(clk, reset)
	begin
		if reset = RESET_LEVEL then
			state       <= idle;
			sda_ena_reg <= '0';
			scl_count   <= (others => '0');
			strt        <= '0';

		elsif rising_edge(clk) then
			-- SCL clock state register      
			state <= next_state;

			-- Enable SDA process       
			sda_ena_reg <= sda_ena;

			-- SCL pulse counter
			if rst_scl_count = '1' then
				scl_count <= (others => '0');
			elsif ena_scl_count = '1' then
				scl_count <= std_logic_vector(unsigned(scl_count) + 1);
			end if;

			-- Start SCL state machine
			if ctrl_from_sda.START = '1' and control_in.MASL = '1' and status_in.BUSY = '0' then
				strt <= '1';
			else
				strt <= '0';
			end if;

		end if;

	end process SCL_REG;

	UPDATE : process(reset, set_sda, sda_ena_reg, scl_arb_lost)
	begin
		if reset = RESET_LEVEL then
			ctrl_to_sda.SET_SDA      <= '0';
			ctrl_to_sda.SDA_ENA      <= '0';
			ctrl_to_sda.SCL_ARB_LOST <= '0';
		else
			ctrl_to_sda.SET_SDA      <= set_sda;
			ctrl_to_sda.SDA_ENA      <= sda_ena_reg;
			ctrl_to_sda.SCL_ARB_LOST <= scl_arb_lost;
		end if;

	end process UPDATE;

	SCL_STATE : process(scl_count, state, strt, t_const, ctrl_from_sda, scl_in)
	begin

		-- Default values
		rst_scl_count <= '1';
		ena_scl_count <= '1';
		set_sda       <= '0';
		sda_ena       <= '0';
		scl_arb_lost  <= '0';

		case state is
			when idle =>
				sda_ena <= '1';
				ena_scl_count <= '1';
				scl_out <= '1';

				if strt = '1' then
					next_state <= start;
				else
					next_state <= idle;
				end if;

			when start =>
				rst_scl_count <= '0';
				scl_out       <= '1';
				ena_scl_count <= '1';
				sda_ena       <= '1';

				if scl_count = t_const.T_HOLD_START then
					rst_scl_count <= '1';
					next_state    <= scl_falling;
				else
					rst_scl_count <= '0';
					next_state    <= start;
				end if;

			when scl_restart_1 =>
				scl_out       <= '0';
				ena_scl_count <= '1';
				rst_scl_count <= '0';
				if scl_count = t_const.T_LOW then
					rst_scl_count <= '1';
					next_state    <= scl_restart_2;
				else
					rst_scl_count <= '0';
					next_state    <= scl_restart_1;
				end if;

			when scl_restart_2 =>
				scl_out       <= '1';
				ena_scl_count <= '1';
				rst_scl_count <= '0';
				
				if scl_count > t_const.T_HALF_HIGH then
					set_sda <= '1';
				end if;
				
				if scl_count = t_const.T_RSTART then
					rst_scl_count <= '1';
					next_state    <= scl_falling;
				else
					rst_scl_count <= '0';
					next_state    <= scl_restart_2;
				end if;

			when scl_falling =>
				scl_out       <= '0';
				rst_scl_count <= '0';
				ena_scl_count <= '1';
				next_state    <= scl_low;

			when scl_low =>
				scl_out       <= '0';
				ena_scl_count <= '1';
				rst_scl_count <= '0';

				if scl_count = t_const.T_LOW then
					rst_scl_count <= '1';
					next_state    <= scl_rising;
				else
					rst_scl_count <= '0';
					next_state    <= scl_low;
				end if;

			when scl_rising =>
				scl_out       <= '1';
				rst_scl_count <= '0';
				ena_scl_count <= '1';
				next_state    <= scl_high;

			-- I2C devices can slow down communication by stretching SCL: 
			-- During an SCL low phase, any I2C device on the bus may 
			-- additionally hold down SCL to prevent it to rise high again, 
			-- enabling them to slow down the SCL clock rate or to stop I2C 
			-- communication for a while.

			-- An I2C slave is allowed to hold down the clock if it needs to 
			-- reduce the bus speed. The master on the other hand is required
			-- to read back the clock signal after releasing it to high state
			-- and wait until the line has actually gone high.

			when scl_high =>
				scl_out       <= '1';
				rst_scl_count <= '0';
				ena_scl_count <= '1';

				if scl_in = '0' then
					scl_arb_lost <= '1';
				end if;

				if scl_count = t_const.T_HALF_HIGH then
					if ctrl_from_sda.STOP = '1' then
						next_state    <= scl_stop;
						rst_scl_count <= '1';
					else
						next_state    <= scl_high;
						rst_scl_count <= '0';
					end if;

				else
					if scl_count = t_const.T_HIGH then
						if ctrl_from_sda.RSTART = '1' then
							next_state    <= scl_restart_1;
							rst_scl_count <= '1';
						else
							rst_scl_count <= '1';
							next_state    <= scl_falling;
						end if;
					else
						next_state    <= scl_high;
						rst_scl_count <= '0';
					end if;

				end if;

			-- After t = T_SUSTO we have to set SDA to high to signal the
			-- stop condition. After that, we have to wait t = T_BUFF to 
			-- start another transfer or to perform a repeated start. In
			-- this case, T_WAIT = T_SUSTO + T_BUFF.
			when scl_stop =>
				rst_scl_count <= '0';
				ena_scl_count <= '1';
				scl_out       <= '1';

				if scl_count > t_const.T_SUSTO then
					set_sda <= '1';
				end if;

				if scl_count = t_const.T_WAIT then
					next_state <= idle;
				else
					next_state <= scl_stop;
				end if;

		end case;

	end process SCL_STATE;

end architecture scl_arch;
