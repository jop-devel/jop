
--! @file sda_control.vhd
--! @brief I2C data line controller.  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2011-2012
--! @copyright GNU Public License.

library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;
use work.i2c_pkg.all;
use work.fifo_pkg.all;

entity sda_control is
	port(
		clk           : in  std_logic;  --! System clock
		reset         : in  std_logic;  --! Reset signal

		scl_in        : in  std_logic;  --! The clock from I2C bus

		sda_in        : in  std_logic;  --! Data to/from SDA bus line
		sda_out       : out std_logic;  --! Data to SDA bus line

		address       : in  std_logic_vector(I2C_DEV_ADDR_SIZE - 1 downto 0); --! Slave device address

		control_in    : in  control_type; --! Record with different control inputs
		status_out    : out status_type; --! Record with different status outputs
		msg_size      : in  std_logic_vector(I2C_MSG_SIZE - 1 downto 0);

		-- Signals between sda_control.vhd and scl_control.vhd
		ctrl_from_scl : in  sda_control_out_type; --! Input signals from scl_contrl.vhd
		ctrl_to_scl   : out sda_control_in_type; --! Output signals to scl_contrl.vhd

		-- TX fifo
		read_enable   : out std_logic;  --! Transmit buffer read enable
		read_data_out : in  std_logic_vector(I2C_DATA_SIZE - 1 downto 0); --! Transmit buffer data read
		tx_empty      : in  std_logic;  --! Transmit buffer empty flag

		-- RX fifo
		write_enable  : out std_logic;  --! Receive buffer write enable
		write_data_in : out std_logic_vector(I2C_DATA_SIZE - 1 downto 0); --! Receive buffer data to write
		full          : in  std_logic;  --! Receive buffer full flag
		rx_empty      : in  std_logic
	);

end entity sda_control;

architecture sda_arch of sda_control is

	-- Main state machine states
	type state_type is (idle, header, tx_data, rx_data, ack_header, send_ack, wait_ack, sda_stop, sda_rstart);
	signal state      : state_type;
	signal next_state : state_type;

	-- TX buffer read control state machine states
	type tx_fifo_state_type is (idle, read, waiting);
	signal tx_fifo_state      : tx_fifo_state_type;
	signal tx_fifo_next_state : tx_fifo_state_type;

	-- RX buffer write control state machine states
	type rx_fifo_state_type is (idle, write, waiting);
	signal rx_fifo_state      : rx_fifo_state_type;
	signal rx_fifo_next_state : rx_fifo_state_type;

	-- Bit counter: Keep track of how many bits have been sent/received  
	signal bit_count       : std_logic_vector(3 downto 0);
	signal bit_count_ena   : std_logic;
	signal clear_bit_count : std_logic;

	-- Byte counter: Keep track of how many bytes have been sent/received  
	signal byte_count       : std_logic_vector(15 downto 0);
	signal byte_count_ena   : std_logic;
	signal clear_byte_count : std_logic;

	-- TX/RX shift register
	signal load         : std_logic;    --! Load a new byte from TBUF into shift register
	signal store        : std_logic;    --! Store a received byte from shift register into RBUF 
	signal tx_shift_ena : std_logic;    --! Enable shifting of register 
	signal rx_shift_ena : std_logic;    --! Enable shifting of register 
	signal sda_data     : std_logic;    --! MSB of shifted data
	signal sda_data_in  : std_logic_vector(7 downto 0); --! Parallel data out

	-- Status
	signal busy : std_logic := '0';     --! Bus busy

	signal start         : std_logic;   --! Indicates that a START condition has been detected
	signal stop_detected : std_logic;   --! Indicates that a STOP condition has been detected
	signal stop_sda      : std_logic;   --! Stop the SDA process

	signal gen_start     : std_logic;   --! Signals the start of a transaction in master mode
	signal rst_gen_start : std_logic;   --! When set, the gen_start signal is set to '0'.

	signal gen_rep_start     : std_logic; --! A repeated start condition has been detected 
	signal rst_gen_rep_start : std_logic; --! When set, the gen_rep_start signal is set to '0'.

	signal hdr_ok : std_logic;          --! Set when the received header matches the slave address of the device 

	signal empty_reg : std_logic;       --! Set when the TX buffer contains no data
	signal full_reg  : std_logic;       --!  Set when the RX buffer can not hold more data

	-- Errors
	signal tbuf_err     : std_logic;    --! Indicates an error in the TX buffer (i.e. buffer empty)
	signal tbuf_err_reg : std_logic;

	signal rbuf_err     : std_logic;    --! Indicates an error in the RX buffer (i.e. buffer full)
	signal rbuf_err_reg : std_logic;

	signal ack_err     : std_logic;     --! Set when the device to whom we are communicating has not acknowledged the last byte sent 
	signal ack_err_reg : std_logic;

	signal hdr_err     : std_logic;     --! Set when there is no slave device with the specified address  
	signal hdr_err_reg : std_logic;

	signal sla_addressed     : std_logic; --! Indicates that the device has been addressed as slave
	signal sla_addressed_reg : std_logic; --! Indicates that the device has been addressed as slave

	signal data_ready : std_logic;      --! 
	signal rep_start  : std_logic;

	signal done_reg    : std_logic;
	signal dummy       : std_logic_vector(7 downto 0);
	signal dummy_1     : std_logic_vector(7 downto 0);
	signal load_reg    : std_logic;
	signal aux_load    : std_logic;
	signal header_load : std_logic;

	signal op_mode_reg : std_logic;
	signal op_mode     : std_logic;

begin
	hdr_ok        <= '1' when (address = sda_data_in(7 downto 1)) else '0';
	write_data_in <= sda_data_in;

	CLK_REG : process(clk, reset)
	begin
		if reset = RESET_LEVEL then
			ack_err_reg       <= '0';
			hdr_err_reg       <= '0';
			tbuf_err_reg      <= '0';
			rbuf_err_reg      <= '0';
			tx_fifo_state     <= idle;
			rx_fifo_state     <= idle;
			busy              <= '0';
			sla_addressed_reg <= '0';
			op_mode_reg       <= I2C_WR_TRANS;

			ctrl_to_scl.START   <= '0';
			ctrl_to_scl.RSTART  <= '0';
			ctrl_to_scl.STOP    <= '0';
			status_out.BUSY     <= '0';
			status_out.ACK_ERR  <= '0';
			status_out.HDR_ERR  <= '0';
			status_out.TBUF_ERR <= '0';
			status_out.RBUF_ERR <= '0';
			status_out.STOP     <= '0';
			status_out.DATA_RDY <= '0';

		elsif rising_edge(clk) then
			tx_fifo_state     <= tx_fifo_next_state;
			rx_fifo_state     <= rx_fifo_next_state;
			ack_err_reg       <= ack_err;
			hdr_err_reg       <= hdr_err;
			tbuf_err_reg      <= tbuf_err;
			rbuf_err_reg      <= rbuf_err;
			sla_addressed_reg <= sla_addressed;
			op_mode_reg       <= op_mode;

			-- Update status and signals that go to SCL control
			ctrl_to_scl.START   <= gen_start;
			ctrl_to_scl.RSTART  <= rep_start;
			ctrl_to_scl.STOP    <= stop_sda;
			status_out.BUSY     <= busy;
			status_out.ACK_ERR  <= ack_err_reg;
			status_out.HDR_ERR  <= hdr_err_reg;
			status_out.TBUF_ERR <= tbuf_err_reg;
			status_out.RBUF_ERR <= rbuf_err_reg;
			status_out.STOP     <= stop_detected;
			status_out.DATA_RDY <= data_ready;

			-- Bus BUSY
			if start = '1' then
				busy <= '1';
			end if;

			if stop_detected = '1' then
				busy <= '0';
			end if;

		end if;

	end process CLK_REG;

	START_DETECT : process(state, reset, sda_in)
	begin

		-- A HIGH to LOW transition on the SDA line while SCL is HIGH indicates
		-- a START condition. As soon as we leave the IDLE state we reset the
		-- start signal. In theory, SDA is not allowed to change during the high
		-- part of SCL so there should not be false starts. The start signal thus
		-- will be high until the next falling edge of SCL.
		if (reset = RESET_LEVEL) or (state = header) then
			start <= '0';
		elsif sda_in'event and sda_in = '0' then
			if scl_in /= '0' then
				start <= '1';
			else
				start <= '0';
			end if;
		end if;

	end process START_DETECT;

	STOP_DETECT : process(reset, sda_in, start)
	begin

		-- A LOW to HIGH transition on the SDA line while SCL is HIGH indicates
		-- a STOP condition. We should reset the stop signal when a start condi-
		-- tion is detected. In theory, SDA is not allowed to change during the 
		-- high part of SCL so there should not be false stops.
		if (reset = RESET_LEVEL) or (start = '1') then
			stop_detected <= '0';
		elsif sda_in'event and sda_in /= '0' then
			if scl_in /= '0' then
				stop_detected <= '1';
			else
				stop_detected <= '0';
			end if;
		end if;

	end process STOP_DETECT;

	DATA_RDY : process(reset, rx_empty, sla_addressed_reg)
	begin
		--! @todo Need another signal to set it back to 0
		if reset = RESET_LEVEL or rx_empty = '1' then
			data_ready <= '0';
		elsif falling_edge(sla_addressed_reg) then
			data_ready <= '1';
		end if;

	end process DATA_RDY;

	GENERATE_START : process(reset, control_in.STRT, rst_gen_start)
	begin
		if reset = RESET_LEVEL or rst_gen_start = '1' then
			gen_start <= '0';
		elsif rising_edge(control_in.STRT) then
			gen_start <= '1';
		end if;

	end process GENERATE_START;

	GENERATE_REP_START : process(reset, control_in.RSTA, rst_gen_rep_start)
	begin
		if reset = RESET_LEVEL or rst_gen_rep_start = '1' then
			gen_rep_start <= '0';
		elsif rising_edge(control_in.RSTA) then
			gen_rep_start <= '1';
		end if;

	end process GENERATE_REP_START;

	TX_SH_REG : entity work.shift_reg
		generic map(SIZE => I2C_DATA_SIZE,
			        EDGE => '0'
		)
		port map(clk       => scl_in,
			     rst       => reset,
			     shift_in  => '0',
			     shift_out => sda_data,
			     data_in   => read_data_out,
			     data_out  => dummy,
			     load      => aux_load,
			     enable    => tx_shift_ena);

	RX_SH_REG : entity work.shift_reg
		generic map(SIZE => I2C_DATA_SIZE,
			        EDGE => '0'
		)
		port map(clk       => not scl_in,
			     rst       => reset,
			     shift_in  => sda_in,
			     shift_out => dummy(0),
			     data_in   => dummy_1,
			     data_out  => sda_data_in,
			     load      => '0',
			     enable    => rx_shift_ena);

	-- Horrible hack but it works :)		     
	aux_load <= header_load or (load and load_reg);

	TX_FIFO_C : process(tx_fifo_state, aux_load)
	begin
		case tx_fifo_state is
			when idle =>
				read_enable <= '0';
				if aux_load = '1' then
					tx_fifo_next_state <= read;
				else
					tx_fifo_next_state <= idle;
				end if;

			when read =>
				read_enable        <= '1';
				tx_fifo_next_state <= waiting;

			when waiting =>
				read_enable <= '0';
				if aux_load = '1' then
					tx_fifo_next_state <= waiting;
				else
					tx_fifo_next_state <= idle;
				end if;

		end case;

	end process TX_FIFO_C;

	RX_FIFO_C : process(rx_fifo_state, store)
	begin
		case rx_fifo_state is
			when idle =>
				write_enable <= '0';
				if store = '1' then
					rx_fifo_next_state <= write;
				else
					rx_fifo_next_state <= idle;
				end if;

			when write =>
				write_enable       <= '1';
				rx_fifo_next_state <= waiting;

			when waiting =>
				write_enable <= '0';
				if store = '1' then
					rx_fifo_next_state <= waiting;
				else
					rx_fifo_next_state <= idle;
				end if;

		end case;

	end process RX_FIFO_C;

	--=============================================================================
	-- Begin of my_process
	-- <description>
	--=============================================================================
	-- read: busy, done_reg, gen_start, gen_rep_start, control_in, start, bit_count, 
	-- sda, sda_in, sda_data, state, hdr_ok, full_reg, full, empty_reg, tx_empty, 
	-- ack_err_reg, hdr_err_reg, tbuf_err_reg, rbuf_err_reg, sda_data_in, 
	-- sla_addressed_reg, data_ready

	-- write: s_clk_local
	-- r/w: led_o
	SDA_SM : process(op_mode_reg, op_mode, read_data_out, done_reg, gen_start, gen_rep_start, control_in, start, bit_count, sda_in, ctrl_from_scl, sda_data, state, hdr_ok, full_reg, full, empty_reg, tx_empty, ack_err_reg, hdr_err_reg, tbuf_err_reg, rbuf_err_reg, sda_data_in, sla_addressed_reg, data_ready)
	begin
		ack_err       <= ack_err_reg;
		hdr_err       <= hdr_err_reg;
		tbuf_err      <= tbuf_err_reg;
		rbuf_err      <= rbuf_err_reg;
		sla_addressed <= sla_addressed_reg;

		op_mode <= op_mode_reg;

		clear_bit_count   <= '0';
		clear_byte_count  <= '0';
		tx_shift_ena      <= '0';
		rx_shift_ena      <= '0';
		load              <= '0';
		header_load       <= '0';
		store             <= '0';
		rep_start         <= '0';
		stop_sda          <= '0';
		rst_gen_start     <= '0';
		rst_gen_rep_start <= '0';
		byte_count_ena    <= '0';

		case state is
			when idle =>
				hdr_err       <= '0';
				tbuf_err      <= tx_empty;
				rbuf_err      <= full;
				sla_addressed <= '0';

				sda_out <= not (gen_start and ctrl_from_scl.SDA_ENA);

				-- Master mode        	
				if (control_in.MASL = '1' and control_in.ENABLE = '1') then
					if gen_start = '1' and ctrl_from_scl.SDA_ENA = '1' then
						next_state    <= header;
						bit_count_ena <= '1';
						header_load   <= '1';
						stop_sda      <= '0';
					else
						next_state    <= idle;
						bit_count_ena <= '0';
						header_load   <= '0';
						stop_sda      <= '1';
					end if;

				-- Slave mode
				elsif (start = '1' and control_in.ENABLE = '1') then
					next_state    <= header;
					bit_count_ena <= '1';
					header_load   <= '0';
					stop_sda      <= '0';
				else
					next_state    <= idle;
					bit_count_ena <= '0';
					header_load   <= '0';
					stop_sda      <= '0';
				end if;

			when header =>
				bit_count_ena <= '1';
				stop_sda      <= '0';
				rep_start     <= '0';
				op_mode       <= read_data_out(0);

				-- In master mode we drive the SDA line, in 
				-- slave mode, we just release the bus
				sda_out <= (not control_in.MASL) or sda_data;
				if control_in.MASL = '1' then
					rst_gen_start <= '1';
					tx_shift_ena  <= '1';
					rx_shift_ena  <= '0';
				else
					rst_gen_start <= '0';
					tx_shift_ena  <= '0';
					rx_shift_ena  <= '1';

				end if;

				if bit_count = std_logic_vector(to_unsigned(I2C_HDR_SIZE, bit_count'length)) then
					next_state <= ack_header;
				else
					next_state <= header;
				end if;

			when ack_header =>
				stop_sda        <= '0';
				bit_count_ena   <= '0';
				clear_bit_count <= '1';
				sda_out         <= '1';

				-- Master mode we check for valid ACK
				if control_in.MASL = '1' then
					if sda_in = '0' then
						hdr_err       <= '0';
						sla_addressed <= '1';
						if op_mode = I2C_RD_TRANS then
							-- Check that rx buff is not full
							if (full = '0' and data_ready = '0') then
								next_state <= rx_data;
								load       <= '0';
								rbuf_err   <= '0';
							else
								next_state <= sda_stop;
								load       <= '0';
								rbuf_err   <= '1';
							end if;
						else
							-- Check that tx buff is not empty
							if empty_reg = '0' then
								next_state <= tx_data;
								load       <= '1';
								tbuf_err   <= '0';
							else
								next_state <= sda_stop;
								load       <= '0';
								tbuf_err   <= '1';
							end if;
						end if;

					else
						next_state <= sda_stop;
						hdr_err    <= '1';
						load       <= '0';
					end if;

				-- Slave mode we set the value of the returned ACK        		
				else
					if hdr_ok = '1' then
						sla_addressed <= not sda_data_in(0);
						if sda_data_in(0) = I2C_RD_TRANS then
							if empty_reg = '0' then
								next_state <= tx_data;
								hdr_err    <= '0';
								load       <= '1';
								tbuf_err   <= '0';
								sda_out    <= '0';
							else
								next_state <= sda_stop;
								hdr_err    <= '0';
								load       <= '0';
								tbuf_err   <= '1';
								sda_out    <= '1';
							end if;
						else
							if (full = '0' and data_ready = '0') then
								next_state <= rx_data;
								hdr_err    <= '0';
								load       <= '0';
								rbuf_err   <= '0';
								sda_out    <= '0';
							else
								next_state <= sda_stop;
								hdr_err    <= '0';
								load       <= '0';
								rbuf_err   <= '1';
								sda_out    <= '1';
							end if;
						end if;
					else
						sda_out       <= '1';
						next_state    <= sda_stop;
						hdr_err       <= '1';
						load          <= '0';
						sla_addressed <= '0';
					end if;

				end if;

			when tx_data =>
				stop_sda        <= '0';
				sda_out         <= sda_data;
				bit_count_ena   <= '1';
				clear_bit_count <= '0';
				tx_shift_ena    <= '1';
				rx_shift_ena    <= '0';

				if start = '1' then
					next_state <= header;
				elsif bit_count = std_logic_vector(to_unsigned(I2C_DATA_SIZE - 1, bit_count'length)) then
					next_state <= wait_ack;
				else
					next_state <= tx_data;
				end if;

			when rx_data =>
				stop_sda        <= '0';
				sda_out         <= '1';
				bit_count_ena   <= '1';
				clear_bit_count <= '0';
				tx_shift_ena    <= '0';
				rx_shift_ena    <= '1';

				if start = '1' then
					next_state <= header;
				elsif bit_count = std_logic_vector(to_unsigned(I2C_DATA_SIZE - 1, bit_count'length)) then
					next_state <= send_ack;
				else
					next_state <= rx_data;
				end if;

			when wait_ack =>
				bit_count_ena   <= '0';
				clear_bit_count <= '1';
				stop_sda        <= '0';
				sda_out         <= '1';

				if sda_in = '0' then
					ack_err        <= '0';
					byte_count_ena <= '1';

					-- Master mode				
					if control_in.MASL = '1' then
						if gen_rep_start = '0' then
							-- Check that:
							-- 1. TX buff is not empty
							-- 2. Transmission is not forced to stop
							-- 3. Byte count equals the message size
							if (empty_reg = '0' and control_in.STOP = '0' and done_reg = '0') then
								next_state <= tx_data;
								load       <= '1';
							else
								next_state <= sda_stop;
								tbuf_err   <= empty_reg;
								load       <= '0';
							end if;
						else
							next_state       <= sda_rstart;
							rep_start        <= '1';
							clear_byte_count <= '1';
						end if;
					-- Slave mode					
					else
						-- Check that:
						-- 1. TX buff is not empty
						if empty_reg = '0' then
							next_state <= tx_data;
							load       <= '1';
						else
							next_state <= sda_stop;
							tbuf_err   <= empty_reg;
							load       <= '0';
						end if;
					end if;
				else
					ack_err          <= '1';
					next_state       <= sda_stop;
					load             <= '0';
					clear_byte_count <= '1';
				end if;

			when send_ack =>
				bit_count_ena   <= '0';
				clear_bit_count <= '1';
				stop_sda        <= '0';
				byte_count_ena  <= '0';

				-- Master mode
				if control_in.MASL = '1' then
					if gen_rep_start = '0' then
						-- Check that:
						-- 1. RX buff is not full
						-- 2. Transaction is not forced to stop
						-- 3. Byte count equals the message size
						-- 4. We are returning a valid acknowledge
						if (control_in.ACK = '0' and full_reg = '0' and control_in.STOP = '0' and done_reg = '0') then
							sda_out        <= '0';
							ack_err        <= '0';
							store          <= '1';
							rbuf_err       <= '0';
							next_state     <= rx_data;
							byte_count_ena <= '1';
						else
							sda_out        <= '1';
							ack_err        <= control_in.ACK and control_in.MASL;
							store          <= not full_reg; -- Store the last byte if there is space
							rbuf_err       <= full_reg;
							next_state     <= sda_stop;
							byte_count_ena <= '0';
						end if;
					else
						sda_out          <= control_in.ACK;
						next_state       <= sda_rstart;
						rep_start        <= '1';
						clear_byte_count <= '1';
					end if;

				-- Slave mode
				else
					-- Check that:
					-- 1. RX buff is not full
					-- 2. We are returning a valid acknowledge
					if (control_in.ACK = '0' and full_reg = '0') then
						sda_out        <= '0';
						ack_err        <= '0';
						store          <= '1';
						rbuf_err       <= '0';
						next_state     <= rx_data;
						byte_count_ena <= '1';
					else
						sda_out        <= '1';
						ack_err        <= control_in.ACK and control_in.MASL;
						store          <= not full_reg; -- Store the last byte if there is space
						rbuf_err       <= full_reg;
						next_state     <= sda_stop;
						byte_count_ena <= '0';
					end if;

				end if;

			when sda_stop =>
				stop_sda          <= '1';
				bit_count_ena     <= '1';
				sla_addressed     <= '0';
				byte_count_ena    <= '0';
				clear_byte_count  <= '1';
				rst_gen_rep_start <= '1';
				op_mode           <= I2C_WR_TRANS;

				if control_in.MASL = '1' then
					if ctrl_from_scl.SET_SDA = '1' then
						sda_out <= '1';
					else
						sda_out <= '0';
					end if;
				else
					sda_out <= '1';
				end if;

				next_state <= idle;

			when sda_rstart =>
				bit_count_ena     <= '1';
				stop_sda          <= '0';
				load              <= '1';
				byte_count_ena    <= '0';
				rst_gen_rep_start <= '1';
				op_mode           <= I2C_WR_TRANS;

				if control_in.MASL = '1' then
					if ctrl_from_scl.SET_SDA = '1' then
						sda_out <= '0';
					else
						sda_out <= '1';
					end if;
				else
					sda_out <= '1';
				end if;

				next_state <= header;

		end case;

	end process SDA_SM;

	SDA_REG : process(scl_in, reset, stop_detected)
	begin
		if reset = RESET_LEVEL or stop_detected = '1' then

			-- Asynchronous reset when a stop condition is detected, 
			-- since there will be no SCL clock to trigger the transition 
			state <= idle;

			-- Bit counter
			bit_count <= (others => '0');

			-- TX buff empty flag
			empty_reg <= '0';

			-- RX buff full flag
			full_reg <= '0';

		elsif falling_edge(scl_in) then
			state <= next_state;

			-- Bit counter
			if clear_bit_count = '1' then
				bit_count <= (others => '0');
			elsif bit_count_ena = '1' then
				bit_count <= std_logic_vector(unsigned(bit_count) + 1);
			end if;

			-- TX buff empty flag
			empty_reg <= tx_empty;

			-- RX buff full flag
			full_reg <= full;

		end if;

	end process SDA_REG;

	SDA_HIGH_REG : process(scl_in, reset)
	begin
		if reset = RESET_LEVEL then
			byte_count <= (others => '0');
			done_reg   <= '0';
			load_reg   <= '0';
		elsif rising_edge(scl_in) then
			if clear_byte_count = '1' then
				byte_count <= (others => '0');
			elsif byte_count_ena = '1' then
				byte_count <= std_logic_vector(unsigned(byte_count) + 1);
			end if;

			if (byte_count = msg_size) then
				done_reg <= '1';
			else
				done_reg <= '0';
			end if;

			if load = '1' then
				load_reg <= '1';
			else
				load_reg <= '0';
			end if;

		end if;

	end process SDA_HIGH_REG;

end architecture sda_arch;