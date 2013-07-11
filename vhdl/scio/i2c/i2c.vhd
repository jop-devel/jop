--! @todo Arbitration lost
--! @todo Clock Sync
--! @todo Hold SCL
--! @todo Currently, whenever the TX buffer is empty the transmission finishes with a TBUF_ERROR
--! @todo Status register should keep its value after the end of a transaction until a new transaction is started
--! @todo Problem when message size = 0
--! @todo Write some data in the TX buffer of the slave in tescase_2 (rep start)
--! @todo Adjust .ucf file (xilinx board)


--! @test Test 0: Timing in SCL clock generation
--! @test Test 1: Master single byte transfer
--! @test Test 2: Master multiple byte transfer (less than TBUF size)
--! @test Test 3: Master multiple byte transfer (bigger than TBUF size)
--! @test Test 4: Master single byte read
--! @test Test 5: Master multiple byte read (less than RBUF size)
--! @test Test 6: Master multiple byte read (bigger than RBUF size)
--! @test Test 7: Slave single byte write
--! @test Test 8: Slave multiple byte write (less than TBUF size)
--! @test Test 9: Slave multiple byte write (bigger than TBUF size)
--! @test Test 10: Slave single byte read
--! @test Test 11: Slave multiple byte read (less than TBUF size)
--! @test Test 12: Slave multiple byte read (bigger than TBUF size)

--! @bug In Slave transmitter, one additional byte is transmitted after the master stops the transmission with a NACK

--! @file i2c.vhd
--! @brief I2C protocol controller top level entity. 
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

entity i2c is
	generic(TBUFF_ADD : integer := 4;   --! Transmit buffer size (2**TBUFF_ADD)
		    RBUFF_ADD : integer := 4    --! Receive buffer size (2**RBUFF_ADD)
	);

	port(

		-- Clock and reset inputs
		clk                  : in    std_logic; --! System input clock
		reset                : in    std_logic; --! Global reset signal

		-- External world connections
		sda                  : inout std_logic; --! Serial data line 
		scl                  : inout std_logic; --! Bus clock

		-- Microprocessor interface
		control              : in    control_type; --! Record with different control inputs
		status               : out   status_type; --! Record with different status outputs
		address              : in    std_logic_vector(6 downto 0); --! Address of the device when used as slave
		msg_size             : in    std_logic_vector(15 downto 0);

		tx_fifo_occupancy_rd : out   std_logic_vector(15 downto 0); --! Transmitter buffer read occupancy level
		tx_fifo_occupancy_wr : out   std_logic_vector(15 downto 0); --! Transmitter buffer write occupancy level
		rx_fifo_occupancy_rd : out   std_logic_vector(15 downto 0); --! Receive buffer read occupancy level
		rx_fifo_occupancy_wr : out   std_logic_vector(15 downto 0); --! Receive buffer write occupancy level

		t_const              : in    timing_type; --! Time constants. Modify to adjust to your bus clock requirements

		irq                  : out   std_logic;

		-- RX fifo
		read_enable          : in    std_logic; --! Receive buffer read enable
		read_data_out        : out   std_logic_vector(I2C_DATA_SIZE - 1 downto 0); --! Receive buffer data read

		-- TX fifo
		write_enable         : in    std_logic; --! Transmit buffer write enable
		write_data_in        : in    std_logic_vector(I2C_DATA_SIZE - 1 downto 0) --! Transmit buffer data to write
	);

end entity i2c;

architecture i2c_arch of i2c is
	type int_state_type is (idle, gen_int, int_wait);
	signal int_state, int_next_state : int_state_type;

	signal from_sda_to_scl     : sda_control_in_type;
	signal from_scl_to_sda     : sda_control_out_type;
	signal from_sda_to_scl_reg : sda_control_in_type;
	signal from_scl_to_sda_reg : sda_control_out_type;

	signal status_a_int : status_type;

	signal tx_fifo_in_int  : fifo_in_type;
	signal tx_fifo_out_int : fifo_out_type;

	signal rx_fifo_in_int  : fifo_in_type;
	signal rx_fifo_out_int : fifo_out_type;

	signal sda_in : std_logic;
	signal scl_in : std_logic;

	signal sda_out : std_logic;
	signal scl_out : std_logic;

	signal sda_out_reg : std_logic := '1';
	signal scl_out_reg : std_logic := '1';

	component scl_control
		port(clk           : in  std_logic;
			 reset         : in  std_logic;
			 scl_in        : in  std_logic;
			 scl_out       : out std_logic;
			 ctrl_from_sda : in  sda_control_in_type;
			 ctrl_to_sda   : out sda_control_out_type;
			 control_in    : in  control_type;
			 status_in     : in  status_type;
			 t_const       : in  timing_type);
	end component scl_control;

	component sda_control
		port(clk           : in    std_logic;
			 reset         : in    std_logic;
			 scl_in        : in    std_logic;
			 sda_in        : inout std_logic;
			 sda_out       : out   std_logic;
			 address       : in    std_logic_vector(6 downto 0);
			 control_in    : in    control_type;
			 status_out    : out   status_type;
			 msg_size      : in    std_logic_vector(15 downto 0);
			 ctrl_from_scl : in    sda_control_out_type;
			 ctrl_to_scl   : out   sda_control_in_type;
			 read_enable   : out   std_logic;
			 read_data_out : in    std_logic_vector(I2C_DATA_SIZE - 1 downto 0);
			 tx_empty      : in    std_logic;
			 write_enable  : out   std_logic;
			 write_data_in : out   std_logic_vector(I2C_DATA_SIZE - 1 downto 0);
			 full          : in    std_logic;
			 rx_empty      : in    std_logic);
	end component sda_control;

begin
	scl <= 'Z' when scl_out_reg = '1' else '0';
	sda <= 'Z' when sda_out_reg = '1' else '0';

	status <= status_a_int;

	-- RX buffer
	read_data_out              <= rx_fifo_out_int.read_data_out;
	rx_fifo_in_int.read_enable <= read_enable;
	rx_fifo_occupancy_rd       <= rx_fifo_out_int.occupancy_rd;
	rx_fifo_occupancy_wr       <= rx_fifo_out_int.occupancy_wr;
	rx_fifo_in_int.flush       <= control.RX_FLUSH;

	-- TX buffer
	tx_fifo_in_int.write_enable  <= write_enable;
	tx_fifo_in_int.write_data_in <= write_data_in;
	tx_fifo_occupancy_rd         <= tx_fifo_out_int.occupancy_rd;
	tx_fifo_occupancy_wr         <= tx_fifo_out_int.occupancy_wr;
	tx_fifo_in_int.flush         <= control.TX_FLUSH;

	INTERRUPT : process(clk, reset)
	begin
		if reset = RESET_LEVEL then
			irq       <= '0';
			int_state <= idle;
	
		elsif rising_edge(clk) then
	
			case int_state is
				when idle =>
					irq <= '0';
					if status_a_int.DATA_RDY = '1' then
						int_state <= gen_int;
					else
						int_state <= idle;
					end if;
				when gen_int =>
					irq       <= '1';
					int_state <= int_wait;
				when int_wait =>
					irq <= '0';
					if status_a_int.DATA_RDY = '1' then
						int_state <= int_wait;
					else
						int_state <= idle;
					end if;
			end case;

		end if;

	end process INTERRUPT;

	SAMPLE : process(clk, reset)
	begin
		if reset = RESET_LEVEL then
			scl_in      <= '1';
			sda_in      <= '1';
			scl_out_reg <= '1';
			sda_out_reg <= '1';

			from_sda_to_scl_reg.RSTART <= '0';
			from_sda_to_scl_reg.START  <= '0';
			from_sda_to_scl_reg.STOP   <= '0';

			from_scl_to_sda_reg.SCL_ARB_LOST <= '0';
			from_scl_to_sda_reg.SDA_ENA      <= '0';
			from_scl_to_sda_reg.SDA_ENA      <= '0';

		elsif rising_edge(clk) then
			from_sda_to_scl_reg <= from_sda_to_scl;
			from_scl_to_sda_reg <= from_scl_to_sda;

			-- Sample SCL
			if scl = '0' then
				scl_in <= '0';
			else
				scl_in <= '1';
			end if;

			-- Sample SDA
			if sda = '0' then
				sda_in <= '0';
			else
				sda_in <= '1';
			end if;

			-- SCL output
			scl_out_reg <= scl_out;

			-- SDA output
			sda_out_reg <= sda_out;

		end if;

	end process SAMPLE;

	--	SCL_CTRL : entity work.scl_control
	SCL_CTRL : scl_control
		port map(clk           => clk,
			     reset         => reset,
			     scl_in        => scl_in,
			     scl_out       => scl_out,
			     ctrl_from_sda => from_sda_to_scl,
			     ctrl_to_sda   => from_scl_to_sda,
			     control_in    => control,
			     status_in     => status_a_int,
			     t_const       => t_const);

	--	SDA_CTRL : entity work.sda_control
	SDA_CTRL : sda_control
		port map(clk           => clk,
			     reset         => reset,
			     scl_in        => scl,
			     sda_in        => sda_in,
			     sda_out       => sda_out,
			     control_in    => control,
			     status_out    => status_a_int,
			     msg_size      => msg_size,
			     address       => address,
			     ctrl_from_scl => from_scl_to_sda_reg,
			     ctrl_to_scl   => from_sda_to_scl,
			     read_enable   => tx_fifo_in_int.read_enable,
			     read_data_out => tx_fifo_out_int.read_data_out,
			     tx_empty      => tx_fifo_out_int.empty,
			     write_enable  => rx_fifo_in_int.write_enable,
			     write_data_in => rx_fifo_in_int.write_data_in,
			     full          => rx_fifo_out_int.full,
			     rx_empty      => rx_fifo_out_int.empty
		);

	TX_FIFO : entity work.async_fifo
		generic map(asynch    => "FALSE",
			        ADDRESS   => TBUFF_ADD,
			        DATA_SIZE => I2C_DATA_SIZE)
		port map(reset    => reset,
			     wclk     => clk,
			     rclk     => clk,
			     fifo_in  => tx_fifo_in_int,
			     fifo_out => tx_fifo_out_int);

	RX_FIFO : entity work.async_fifo
		generic map(asynch    => "FALSE",
			        ADDRESS   => RBUFF_ADD,
			        DATA_SIZE => I2C_DATA_SIZE)
		port map(reset    => reset,
			     wclk     => clk,
			     rclk     => clk,
			     fifo_in  => rx_fifo_in_int,
			     fifo_out => rx_fifo_out_int);

end architecture i2c_arch;